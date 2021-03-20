package net.oyyq.dbflowdemo.db.datarepo;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.Message_Table;

import java.util.Collections;
import java.util.List;


/**
 * 消息推送过来时相关Session记录已经在Session_Table里面了
 * 人消息, 群消息
 * 当关联的Session记录保持在Session_Table时, MessageRepository就一直保持注册, dataList一直拿到最新消息 & 消息的最新状态
 * 生命周期: 人消息: "我"开始关注此人 => "我"不再关注 or 退出APP
 *          群消息: "我"加入群=> "我"退群 or 退出APP
 */
public class MessageRepository extends BaseDbRepository<Message> implements MessageDataSource {
    public static final String repoPrefix = "Message";

    protected String id;                    //聊天对方id, 群id or 人id
    public final int receiverType;          //User / Group
    protected String userId;                //聊天对方
    protected String groupId;
    protected int unread = -1;             //由ChatPresenter告诉Repository最少加载多少条

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public MessageRepository(int receiverType, String id, String userId, String groupId){
        dataClass = Message.class;
        this.receiverType = receiverType;
        this.id = id;
        this.userId = userId;
        this.groupId = groupId;
    }


    @Override
    public String getId() {
        return id;
    }


    @Override
    protected void insert(Message message) {
        dataList.add(message);          //到达时间靠后的Message放在dataList后面
    }


    @Override
    protected boolean isRequired(Message message) {
        if(message.getSender() == null) return false;
        switch (receiverType){
            case PushModel.RECEIVER_TYPE_USER:
                return message.getGroup() == null && userId.equalsIgnoreCase(message.getOther().getId());
            case PushModel.RECEIVER_TYPE_GROUP:
                return message.getGroup() != null && groupId.equalsIgnoreCase(message.getGroup().getId());
            default:
                return false;
        }
    }


    /**
     * 进入聊天界面的初始加载
     * @param callback 传递一个callback回调，一般回调到Presenter
     */
    @Override
    public void load(SucceedCallback<List<Message>> callback) {
        super.load(callback);
        if(dataList.size() > 0){
            notifyDataChange();
        } else {
            int toload = unread > 0 ? unread : 25;
            //加载25条历史消息
            switch (receiverType){
                case PushModel.RECEIVER_TYPE_USER:
                    SQLite.select().from(Message.class)
                            .where(Message_Table.senderId.eq(userId), Message_Table.group_id.isNull())
                            .or(Message_Table.receiverId.eq(userId))
                            .orderBy(Message_Table.createAt, false).limit(toload)
                            .async()
                            .queryListResultCallback(this).execute();
                    break;
                case PushModel.RECEIVER_TYPE_GROUP:
                    SQLite.select()
                            .from(Message.class)
                            .where(Message_Table.group_id.eq(groupId))
                            .orderBy(Message_Table.createAt, false).limit(toload) // 倒序查询
                            .async()
                            .queryListResultCallback(this).execute();
                    break;
            }
        }
    }



    @Override
    public void onListQueryResult(QueryTransaction transaction, @NonNull List<Message> tResult) {
        Collections.reverse(tResult);           //tResult 从时间逆序改成时间正序
        super.onListQueryResult(transaction, tResult);
    }

}
