package net.oyyq.dbflowtest.presenter.message;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import java.util.List;


/**
 * 聊天界面的契约
 */
public interface ChatContract {

    /**
     * 发送消息的 动作
     */
    interface Presenter extends BaseContract.Presenter {
        // 发送文字
        void pushText(String content);

        // 发送语音
        void pushAudio(String path, long time);

        // 发送图片
        void pushImages(String[] paths);

        // 重新发送一个消息，返回是否调度成功
        boolean rePush(Message message);
    }


    // 界面的基类
    interface View<InitModel> extends BaseContract.RecyclerView<Presenter,Message> {
        void onInit(InitModel model);


        //在Presenter层加载到数据后(初始化加载 / 新消息到达),
        // view层的RecyclerView要将界面滚动到的adapterPosition
        void smoothScrollToAdapterPosition(int adapterPosition);

    }

    // 人聊天的界面
    interface UserView extends View<User> { }


    // 群聊天的界面
    interface GroupView extends View<Group> {
        // 显示管理员菜单
        void showAdminOption(boolean isAdmin);

        // 初始化成员信息
        void onInitGroupMembers(List<GroupMember> members, long moreCount);
    }


}
