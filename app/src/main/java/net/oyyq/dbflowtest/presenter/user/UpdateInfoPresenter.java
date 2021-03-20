package net.oyyq.dbflowtest.presenter.user;

import android.text.TextUtils;
import net.oyyq.common.factory.presenter.BasePresenter;
import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.UserHelper;
import net.oyyq.dbflowdemo.db.model.UserUpdateModel;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.dbflowtest.helper.UploadHelper;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;



public class UpdateInfoPresenter extends BasePresenter<UpdateInfoContract.View>
        implements UpdateInfoContract.Presenter, DataSource.Callback<UserCard> {

    public UpdateInfoPresenter(UpdateInfoContract.View view) {
        super(view);
    }

    @Override
    public void onDataLoaded(UserCard userCard) {
        final UpdateInfoContract.View view = getView();
        if (view == null) return;

        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                view.hideLoading();
                view.updateSucceed();
            }
        });
    }

    @Override
    public void onDataNotAvailable(int strRes) {
        final UpdateInfoContract.View view = getView();
        if (view == null) return;


        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                view.hideLoading();
                view.showError(strRes);
            }
        });
    }

    @Override
    public void update(String photoFilePath, String desc, boolean isMan) {
        start();

        final UpdateInfoContract.View view = getView();

        if (TextUtils.isEmpty(photoFilePath) || TextUtils.isEmpty(desc)) {
            view.hideLoading();
            view.showError(R.string.data_account_update_invalid_parameter);
        } else {
            // 上传头像
            Factory.runOnAsync(new Runnable() {
                @Override
                public void run() {
                    String url = UploadHelper.uploadPortrait(photoFilePath);
                    if (TextUtils.isEmpty(url)) {
                        view.hideLoading();
                        // 上传失败
                        view.showError(R.string.data_upload_error);
                    } else {
                        // 构建Model
                        UserUpdateModel model = new UserUpdateModel(null, url, desc, isMan ? User.SEX_MAN : User.SEX_WOMAN);
                        // 进行网络请求，上传
                        UserHelper.update(model, UpdateInfoPresenter.this);
                    }
                }
            });
        }
    }
}
