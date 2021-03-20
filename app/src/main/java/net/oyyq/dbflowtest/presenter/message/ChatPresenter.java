package net.oyyq.dbflowtest.presenter.message;


import android.text.TextUtils;
import androidx.recyclerview.widget.DiffUtil;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.common.adapterUtil.DiffUiDataCallback;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.datarepo.BaseDbRepository;
import net.oyyq.dbflowdemo.db.datarepo.MessageDataSource;
import net.oyyq.dbflowdemo.db.datarepo.MessageRepository;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.helper.MessageHelper;
import net.oyyq.dbflowdemo.db.model.MsgCreateModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.Session;
import net.oyyq.dbflowdemo.db.model.datamodel.Session_Table;
import net.oyyq.dbflowtest.presenter.BaseSourcePresenter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 聊天Presenter的基础类
 * TODO 消息在MessageRepository越存越多, 需要清理机制(暂时就不去管了)
 * 1. 进入界面: 已知未读消息数量. 根据未读消息数量加载初始化数据, 进行AppBarLayout的折叠
 * 2. "我"已在聊天界面, 新消息到达, 聊天滚动到底部
 */
public class ChatPresenter<View extends ChatContract.View> extends BaseSourcePresenter<Message, Message, MessageDataSource, View>
        implements ChatContract.Presenter{


    // 聊天对方的id
    protected String mReceiverId;
    //群的Id
    protected String groupId;
    // 区分是人还是群Id
    protected int mReceiverType;

    private boolean mFirstLoad = true;              //是否是初始加载消息
    private int initialUnread = -1;          //进入MessageActivity需要向上滚动信息的条数


    public int getInitialUnread() {
        return initialUnread;
    }
    public void setInitialUnread(int initialUnread) {
        this.initialUnread = initialUnread;
        ((MessageRepository)mSource).setUnread(initialUnread);
    }



    public ChatPresenter(MessageDataSource source, View view, String receiverId, String groupId, int receiverType) {
        super(source, view);
        this.mReceiverId = receiverId;
        this.groupId = groupId;
        this.mReceiverType = receiverType;
    }



    @Override
    public void onDataLoaded(List<Message> messages) {
        ChatContract.View view = getView();
        if (view == null) return;

        List<Message> old = view.getRecyclerAdapter().getItems();
        if(mFirstLoad) {
            //初次加载
            if (old.size() > 0) return;
            mFirstLoad = false;
        }

        DiffUiDataCallback<Message> callback = new DiffUiDataCallback<>(old, messages);
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        refreshData(result, messages);


        int messageSize = messages.size();
        //将未读消息改成已读
        LinkedList<Message> toRead = new LinkedList<>();
        for(int i = messageSize-1; i >= 0 ; i--){
            Message message = messages.get(i);
            if(!message.isUnread()) break;
            message.setUnread(false);
            toRead.addFirst(message);
        }
        DbHelper.save(Message.class, toRead.toArray(new Message[0]));

    }



    @Override
    public void pushText(String content) {
        // 构建一个新的消息
        MsgCreateModel model = new MsgCreateModel.Builder()
                .receiver(mReceiverId, groupId,  mReceiverType)
                .content(content, Message.TYPE_STR)
                .isRepushed(false)
                .build();

        // 进行网络发送
        MessageHelper.push(model);
    }

    @Override
    public void pushAudio(String path, long time) {
        if(TextUtils.isEmpty(path)){
            return;
        }

        // 构建一个新的消息, 将语音的本地绝对路径放在消息体, 后将语音上传到云存储
        MsgCreateModel model = new MsgCreateModel.Builder()
                .receiver(mReceiverId, groupId, mReceiverType)
                .content(path, Message.TYPE_AUDIO)
                .attach(String.valueOf(time))
                .isRepushed(false)
                .build();

        MessageHelper.push(model);
    }

    @Override
    public void pushImages(String[] paths) {
        if (paths == null || paths.length == 0)
            return;
        // 此时路径是本地的手机上的路径
        for (String path : paths) {
            // 构建一个新的消息
            MsgCreateModel model = new MsgCreateModel.Builder()
                    .receiver(mReceiverId, groupId,  mReceiverType)
                    .content(path, Message.TYPE_PIC)
                    .isRepushed(false)
                    .build();

            MessageHelper.push(model);
        }
    }



    @Override
    public boolean rePush(Message message) {
        if (Account.getUserId().equalsIgnoreCase(message.getSender().getId()) && message.getStatus() == Message.STATUS_FAILED) {

            MsgCreateModel model = MsgCreateModel.buildWithMessage(message);
            MessageHelper.push(model);
            return true;
        }

        return false;
    }




    /**
     * 退出 某个聊天界面时调用 !
     * 将关联的Session记录的unRead & reset字段修改 !
     */
    @Override
    public void destroy() {
        //Presenter层和View层解耦
        if (mView != null) {
            mView.setPresenter(null);
            mView.clear();
        }
        mView = null;

        BaseDbRepository repoSource = (BaseDbRepository)mSource;
        if(repoSource != null) repoSource.removeCallback(this);
        mSource = null;

        //将聊天Session的未读数量置为0
        Session session = null;
        if(mReceiverType == PushModel.RECEIVER_TYPE_USER) {
            session = SQLite.select().from(Session.class).where(Session_Table.id.eq(Session.prefix+mReceiverId)).querySingle();
        }else if(mReceiverType == PushModel.RECEIVER_TYPE_GROUP){
            session = SQLite.select().from(Session.class).where(Session_Table.id.eq(Session.prefix+groupId)).querySingle();
        }
        if(session != null) {
            //改变2字段值, 其他字段值不变
            session.setUnReadCount(0);
            session.setResetAt(new Date());
            DbHelper.save(Session.class, session);
        }

        mFirstLoad = true;
        initialUnread = -1;
    }


}
