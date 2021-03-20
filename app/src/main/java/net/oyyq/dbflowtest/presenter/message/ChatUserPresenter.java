package net.oyyq.dbflowtest.presenter.message;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.datarepo.MessageDataSource;
import net.oyyq.dbflowdemo.db.datarepo.MessageRepository;

public class ChatUserPresenter extends ChatPresenter<ChatContract.UserView> implements ChatContract.Presenter {

    public ChatUserPresenter(MessageDataSource source, ChatContract.UserView view, String receiverId) {
        super(source == null? new MessageRepository(PushModel.RECEIVER_TYPE_USER, MessageRepository.repoPrefix+receiverId, receiverId, null):source,
                view, receiverId, null, PushModel.RECEIVER_TYPE_USER);
    }


}
