package net.oyyq.dbflowtest.presenter.message;


import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.datarepo.MessageDataSource;
import net.oyyq.dbflowdemo.db.datarepo.MessageRepository;

/**
 * 群聊天的调度
 */
public class ChatGroupPresenter extends ChatPresenter<ChatContract.GroupView>
        implements ChatContract.Presenter{


    public ChatGroupPresenter(MessageDataSource source, ChatContract.GroupView view, String groupId) {
        super(source == null? new MessageRepository(PushModel.RECEIVER_TYPE_GROUP,  MessageRepository.repoPrefix+groupId, null, groupId):source,
        view, null, groupId, PushModel.RECEIVER_TYPE_GROUP);
    }

    //view.showAdminOption & view.onInitGroupMembers在View内部自己调用

}
