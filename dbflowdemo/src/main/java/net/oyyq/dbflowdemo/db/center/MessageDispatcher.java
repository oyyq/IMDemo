package net.oyyq.dbflowdemo.db.center;

import android.text.TextUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.MessageCard;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.helper.MessageHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.Group_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.db.model.datamodel.User_Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;



public class MessageDispatcher implements CardCenter<Message, MessageCard> {

    private static MessageDispatcher instance;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public static MessageDispatcher instance() {
        if (instance == null) {
            synchronized (MessageDispatcher.class) {
                if (instance == null)
                    instance = new MessageDispatcher();
            }
        }
        return instance;
    }


    @Override
    public void dispatch(MessageCard... cards) {
        if (cards == null || cards.length == 0) return;
        executor.execute(new MessageCardHandler(cards));
    }



    private class MessageCardHandler implements Runnable{

        private final MessageCard[] cards;
        MessageCardHandler(MessageCard[] cards) {
            this.cards = cards;
        }

        @Override
        public void run() {
            List<Message> messages = new ArrayList<>();
            for(MessageCard card : cards) {
                //不合法卡片过滤
                if (card == null  || TextUtils.isEmpty(card.getId())
                        || TextUtils.isEmpty(card.getSenderId())
                        || (TextUtils.isEmpty(card.getReceiverId()) && TextUtils.isEmpty(card.getGroupId())))
                    continue;


                // 消息卡片有可能是推送过来的，也有可能是自己造的
                // 推送来的代表服务器一定有，我们可以查询到（本地有可能有，有可能没有）
                // 如果是直接造的，那么先存储本地，后发送网络
                // 发送消息流程：写消息->存储本地->发送网络->网络返回->刷新本地状态
                Message message = MessageHelper.findFromLocal(card.getId());
                if(message != null){
                    //本地已有这条消息, status >= created.
                    //1. "我"接收 / 群接收的旧消息 记录的 status == DONE 略过
                    //2. "我"发送到人 / 群的消息,  记录的status == created, 服务器回送回来新的状态, DONE || FAILED
                    //3. "我"发送失败的消息, 记录的status == FAILED, 重发一遍, 更改状态为status == CREATED
                    if (message.getStatus() == Message.STATUS_DONE) continue;
                    if (message.getStatus() == Message.STATUS_FAILED && card.getStatus() == Message.STATUS_DONE) continue;
                    if (message.getStatus() == Message.STATUS_FAILED && card.getStatus() == Message.STATUS_FAILED) continue;
                    if (message.getStatus() == Message.STATUS_CREATED && card.getStatus() == Message.STATUS_CREATED) continue;


                    if (card.getStatus() == Message.STATUS_DONE || card.getStatus() == Message.STATUS_CREATED) {
                        //DONE. "我"发出的消息经服务器转发成功的回送.  2.CREATED "我"重发曾发送失败的消息
                        message.setCreateAt(card.getCreateAt());
                    }

                    message.setContent(card.getContent());
                    message.setAttach(card.getAttach());
                    message.setStatus(card.getStatus());
                    message.setRepushed(card.isRepushed());
                    //message.setUnread(card.isUnread());    //message.setUnread(false);

                } else {
                    //1. "我"接收到的新消息 / "我"所在的群接收到的新消息, status == DONE 2. "我"初次发送的消息
                    //if(Account.getUserId().equalsIgnoreCase(card.getSenderId()) && card.getStatus() == Message.STATUS_DONE) continue;
                    if(!Account.getUserId().equalsIgnoreCase(card.getSenderId()) && !(card.getStatus() == Message.STATUS_DONE)) continue;
                    message = card.build();
                }

                messages.add(message);
            }

            if (messages.size() > 0) { DbHelper.save(Message.class, messages.toArray(new Message[0])); }
        }

    }



    /**
     * 判断MessageCard是否应该构建Message实体并更新到数据库, 以及是否要置status 为 FAILED
     * @param card
     * @return true, 这个MessageCard不应该被dispatch到DB层. false, MessageCard应该被dispatch到DB层
     */
    public static boolean DispatchOrNot_Status(MessageCard card){
        if(card.getGroupId() != null){
            Group group = SQLite.select(Group_Table.joinAt).from(Group.class)
                     .where(Group_Table.id.eq(card.getGroupId())).querySingle();

            if(group == null) return true;
            Date myJoinAt = group.getJoinAt();
            if(myJoinAt == null) {
                if(!Account.getUserId().equalsIgnoreCase(card.getSenderId())){
                    //"我"不在该群, 不应接收Message.
                    return true;
                } else {
                    //"我"不在该群("我"被移除群聊了), 可以发送失败的Message到本地.
                    card.setStatus(Message.STATUS_FAILED);
                    return false;
                }
            }
            //正常的消息
            return false;
        } else if (Account.getUserId().equalsIgnoreCase(card.getSenderId())){
            User receiver = SQLite.select(User_Table.isFollow).from(User.class)
                    .where(User_Table.id.eq(card.getReceiverId())).querySingle();
            if(receiver == null || !receiver.isFollow()) {
                //"我"是发送方, "我"和对方互不关注了
                card.setStatus(Message.STATUS_FAILED);
                return false;
            }
            //正常的消息
            return false;
        } else if (Account.getUserId().equalsIgnoreCase(card.getReceiverId())) {
            User sender = SQLite.select(User_Table.isFollow).from(User.class)
                    .where(User_Table.id.eq(card.getSenderId())).querySingle();

            if(sender == null || !sender.isFollow()) {
                //"我"是接收方, "我"和对方互不关注了
                return true;
            }
            //正常的消息
            return false;
        }

        //不是群消息, "我"不是发送方, 也不是接收方 => 坏消息
        return true;
    }



}
