package net.oyyq.dbflowtest.presenter.group;


import android.annotation.SuppressLint;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.common.factory.presenter.BasePresenter;
import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Card.GroupMemberCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.model.GroupMemberModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.db.model.datamodel.User_Table;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.dbflowtest.DemoApplication;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



/**
 * 添加新群成员的调度
 */
public class GroupMemberAddPresenter extends BasePresenter<GroupMemberContract.addView>
        implements GroupMemberContract.addPresenter, DataSource.Callback<List<User>> {


    private String groupId;
    private Group group;
    private Set<String> users = new HashSet<>();


    public GroupMemberAddPresenter(GroupMemberContract.addView view, String groupId) {
        super(view);
        this.groupId = groupId;
        this.group = GroupHelper.findFromLocal(groupId);
    }


    /**
     * 搜索"我"的联系人中不在这个群中的
     */
    @Override
    @SuppressLint("NewApi")
    public void search() {
        start();            //loading

        Factory.runOnAsync(new Runnable() {
            @Override
            public void run() {

                List<User> allcontacts = SQLite.select(User_Table.id, User_Table.name, User_Table.portrait, User_Table.desc)
                        .from(User.class).where(User_Table.isFollow.eq(true)).queryList();

                List<GroupMember> groupMembers = SQLite.select(GroupMember_Table.id, GroupMember_Table.userId).from(GroupMember.class)
                        .where(GroupMember_Table.group_id.eq(groupId)).queryList();
                List<String> memberUser = groupMembers.stream().map(member->member.getUserId())
                        .collect(Collectors.toList());


                Iterator<User> contactIterator = allcontacts.iterator();
                while (contactIterator.hasNext()){
                    if( memberUser.contains( contactIterator.next().getId()) ) contactIterator.remove();
                }

                //拿到结果刷新到界面
                if(allcontacts.size() > 0) {
                    onDataLoaded(allcontacts);
                } else {
                    onDataNotAvailable(R.string.no_one_can_add);
                }

            }
        });

    }



    @Override
    public void changeSelect(User user, boolean isSelected) {
        if (isSelected)
            users.add(user.getId());
        else
            users.remove(user.getId());
    }


    @Override
    public void addnewMembers() {

        GroupMemberContract.addView view = mView;

        if (users.size() == 0){
            view.showError(R.string.label_group_member_add_invalid);
            return;
        }
        view.showLoading();


        GroupMemberModel model = new GroupMemberModel();
        model.setUserIds(users);

        GroupHelper.addMembers(groupId, model, new DataSource.Callback<List<GroupMemberCard>>() {
            @Override
            public void onDataNotAvailable(int strRes) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        view.hideLoading();
                    }
                });
                DemoApplication.showToast(strRes);
                users.clear();
            }

            @Override
            public void onDataLoaded(List<GroupMemberCard> groupMemberCards) {
                view.onAddSuccedd();
                users.clear();
            }
        });

    }



    @Override
    public void onDataLoaded(List<User> users) {
        GroupMemberContract.addView view = getView();

        Run.onUiAsync(new Action() {
              @Override
              public void call() {
                  view.hideLoading();
                  //已经在UI线程
                  view.onSearchDone(users);
              }
        });

    }


    @Override
    public void onDataNotAvailable(int strRes) {

        GroupMemberContract.addView view = getView();

        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                view.hideLoading();
            }
        });

        DemoApplication.showToast(strRes);

    }
}
