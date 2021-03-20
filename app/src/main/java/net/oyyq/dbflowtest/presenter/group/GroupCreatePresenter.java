package net.oyyq.dbflowtest.presenter.group;

import android.text.TextUtils;
import net.oyyq.common.factory.presenter.BaseRecyclerPresenter;

import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.helper.UserHelper;
import net.oyyq.dbflowdemo.db.model.GroupCreateModel;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.dbflowtest.helper.UploadHelper;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * "我"建群界面
 */
public class GroupCreatePresenter extends BaseRecyclerPresenter<GroupCreateContract.ViewModel, GroupCreateContract.View>
        implements GroupCreateContract.Presenter, DataSource.Callback<GroupCard> {

    //除了"我"之外的其他群员
    private Set<String> users = new HashSet<>();


    public GroupCreatePresenter(GroupCreateContract.View view) {
        super(view);

    }


    @Override
    public void start() {
        super.start();
        //将"我"的所有联系人刷到界面上
        Factory.runOnAsync(new Runnable() {
            @Override
            public void run() {
                List<User> myContacts = UserHelper.getContact();
                List<GroupCreateContract.ViewModel> models = new ArrayList<>();
                for(User contact : myContacts){

                    GroupCreateContract.ViewModel viewModel = new GroupCreateContract.ViewModel();
                    viewModel.author = contact;
                    viewModel.isSelected = false;
                    models.add(viewModel);
                }

                refreshData(models);
            }
        });

    }

    @Override
    public void onDataLoaded(GroupCard groupCard) {
        // 建群成功
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                GroupCreateContract.View view = getView();
                if (view != null) {
                    view.onCreateSucceed();
                }
            }
        });
    }



    @Override
    public void onDataNotAvailable(int strRes) {
        //建群失败 !
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                GroupCreateContract.View view = getView();
                if (view != null) {
                    view.showError(strRes);
                }
            }
        });
    }




    @Override
    public void create(String name, String desc, String picture) {
        GroupCreateContract.View view = getView();

        // 判断参数
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(picture) || users.size() == 0) {
            view.showError(R.string.label_group_create_invalid);
            return;
        }

        Factory.runOnAsync(new Runnable() {
            @Override
            public void run() {
                String url = uploadPicture(picture);
                if (TextUtils.isEmpty(url)) return;

                GroupCreateModel model = new  GroupCreateModel.Builder().basicInfo(name, desc, url, Account.getUserId())
                        .members(users).notifyLevel(GroupMember.NOTIFY_LEVEL_NONE).build();
                GroupHelper.create(model, GroupCreatePresenter.this);
            }
        });
    }



    @Override
    public void changeSelect(GroupCreateContract.ViewModel model, boolean isSelected) {
        if (isSelected)
            users.add(model.author.getId());
        else
            users.remove(model.author.getId());
    }


    /**
     * 同步上传群头像到阿里云OSS
     * @param path
     * @return
     */
    private String uploadPicture(String path) {
        String url = UploadHelper.uploadPortrait(path);
        if (TextUtils.isEmpty(url)) {
            // 切换到UI线程 提示信息
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    GroupCreateContract.View view = getView();
                    if (view != null) {
                        view.showError(R.string.data_upload_error);
                    }
                }
            });
        }
        return url;
    }



}
