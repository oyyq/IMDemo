package net.oyyq.dbflowtest.presenter.group;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.DiffUtil;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.common.adapterUtil.DiffUiDataCallback;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.GroupMemberCard;
import net.oyyq.dbflowdemo.db.datarepo.BaseDbRepository;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.datarepo.GroupMembersDataSource;
import net.oyyq.dbflowdemo.db.datarepo.GroupMembersRepository;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.model.GroupMemberModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember_Table;
import net.oyyq.dbflowdemo.factory.Factory;

import net.oyyq.dbflowtest.DemoApplication;
import net.oyyq.dbflowtest.presenter.BaseSourcePresenter;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * 对已有群成员操作的调度
 */
public class GroupMemberPresenter extends BaseSourcePresenter<GroupMember, GroupMember, GroupMembersDataSource, GroupMemberContract.View>
        implements GroupMemberContract.Presenter  {

    //"我"操作的群员的userId, ""
    private Set<String> members = new HashSet<>();
    private String myMemberId;      //"我"的GroupMemberId
    private String groupId;
    private Group group;

    //source ==> GroupMembersRepository
    public GroupMemberPresenter(GroupMembersDataSource source, GroupMemberContract.View view, String groupId) {
        super(source == null ? new GroupMembersRepository(GroupMembersRepository.repoPrefix+groupId, groupId):source, view);
        this.groupId = groupId;
        this.group = GroupHelper.findFromLocal(groupId);

        GroupMember selfMember = SQLite.select(GroupMember_Table.id).from(GroupMember.class).where(GroupMember_Table.userId.eq(Account.getUserId())).querySingle();
        if(selfMember != null) myMemberId = selfMember.getId();

    }


    @Override
    public void start() {
        super.start();

        Factory.runOnAsync(new Runnable() {
            @Override
            public void run() {
                GroupMemberContract.View view = getView();
                if (view == null) return;
                //刷新一下群成员, 群成员最新数据显示到界面.
                GroupHelper.refreshGroupMembers(group);
            }
        });

    }



    @Override
    public void onDataLoaded(List<GroupMember> groupMembers) {
        //刷新
        GroupMemberContract.View view = mView;
        if (view == null) return;

        //差异刷新
        RecyclerAdapter<GroupMember> adapter = view.getRecyclerAdapter();
        List<GroupMember> old = adapter.getItems();

        // 进行数据对比
        DiffUtil.Callback callback = new DiffUiDataCallback<>(old, groupMembers);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        //谁是Admin界面显示出来
        refreshData(result, groupMembers);
    }



    @Override
    public void changeSelect(GroupMember member, boolean isSelected) {
        if (isSelected)
            members.add(member.getId());
        else
            members.remove(member.getId());
    }


    //this.users中是待删除的群员
    @SuppressLint("NewApi")
    @Override
    public void delete() {
        GroupMemberContract.View view = mView;

        //判断参数
        if (members.size() == 0){
            view.showError(R.string.label_group_member_delete_invalid);
            return;
        }else if ( members.contains(myMemberId)){
            view.showError(R.string.no_delete_self);
            return;
        }
        view.showLoading();

        GroupMemberModel deleteModel = new GroupMemberModel();
        deleteModel.setMemberIds(members);


        GroupHelper.deleteMembers(groupId, deleteModel, new DataSource.Callback<List<GroupMember>>() {
            @Override
            public void onDataNotAvailable(int strRes) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        view.hideLoading();
                    }
                });
                DemoApplication.showToast(strRes);
                members.clear();
            }
            @Override
            public void onDataLoaded(List<GroupMember> groupMembers) {
                //groupMembers == null
                view.onDeleteSucceed();
                members.clear();
            }
        });

    }


    //"我"添加了一些群管理员
    @Override
    public void addAmdin() {

        GroupMemberContract.View view = mView;
        //判断参数
        if (members.size() == 0){
            view.showError(R.string.label_group_admin_add_invalid);
            return;
        }else if ( members.contains(myMemberId)){
            view.showError(R.string.no_add_admin_self);
            return;
        }
        view.showLoading();


        GroupMemberModel newadminModel = new GroupMemberModel();
        newadminModel.setMemberIds(members);

        GroupHelper.AddAdmins(groupId, newadminModel, new DataSource.Callback<List<GroupMemberCard>>() {
            @Override
            public void onDataNotAvailable(int strRes) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        view.hideLoading();
                    }
                });
                DemoApplication.showToast(strRes);
                members.clear();
            }

            @Override
            public void onDataLoaded(List<GroupMemberCard> groupMemberCards) {
                //groupMemberCards == null
                view.onAddAdminSucceed();
                members.clear();
            }
        });
    }


    //"我"自己退出了群聊
    @Override
    public void exit() {

        GroupMemberContract.View view = mView;
        // 检查是否有其他群管理员
//        List<GroupMember> otherAdmins = SQLite.select(GroupMember_Table.id)
//                .from(GroupMember.class).where(GroupMember_Table.group_id.eq(groupId),
//                        GroupMember_Table.isAdmin.eq(true), GroupMember_Table.userId.notEq(Account.getUserId())).queryList();

//        if(otherAdmins == null || otherAdmins.size()== 0){
//            view.showError(R.string.exit_only_Admin);
//            return;
//        }

        view.showLoading();
        GroupHelper.me_exitgroup(groupId, new DataSource.Callback() {
            @Override
            public void onDataNotAvailable(int strRes) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        view.hideLoading();
                    }
                });
                DemoApplication.showToast(strRes);
            }

            @Override
            public void onDataLoaded(Object o) {
                view.onExitSucceed();
            }
        });
    }



    @Override
    public void destroy() {

        if (mView != null) {
            mView.setPresenter(null);
            mView.clear();
        }
        mView = null;
        BaseDbRepository repoSource = (BaseDbRepository)mSource;
        if(repoSource != null) repoSource.removeCallback(this);
        mSource = null;
    }




}
