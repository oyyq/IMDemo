package net.oyyq.dbflowtest.presenter.group;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import java.util.List;


public interface GroupMemberContract {

    interface BasePresenter extends BaseContract.Presenter{
        // 更改一个Model的选中状态
        void changeSelect(GroupMember member, boolean isSelected);
    }


    //操作已有群员的Presenter
    interface Presenter extends BasePresenter{
        //删除群员
        void delete();

        //"我"新增群管理员
        void addAmdin();

        //"我"退群
        void exit();

    }

    //操作已有群员的View
    interface View extends BaseContract.RecyclerView<Presenter, GroupMember>{

        //"我"删除群员成功的回调
        void  onDeleteSucceed();
        //"我"添加新管理员成功
        void onAddAdminSucceed();

        //"我"退群成功
        void onExitSucceed();

    }




    //添加新群员的Presenter
    interface addPresenter extends BaseContract.Presenter{
        void search();

        void changeSelect(User user, boolean isSelected);

        //"我"添加新的群员
        void addnewMembers();

    }


    //添加新群员的View
    interface addView extends BaseContract.View<addPresenter>{

        void onSearchDone(List<User> users);

        void onAddSuccedd();
    }


}
