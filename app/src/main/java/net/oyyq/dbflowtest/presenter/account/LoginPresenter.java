package net.oyyq.dbflowtest.presenter.account;

import android.text.TextUtils;

import net.oyyq.common.factory.presenter.BasePresenter;
import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.AccountHelper;
import net.oyyq.dbflowdemo.db.model.LoginModel;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

public class LoginPresenter extends BasePresenter<LoginContract.View> implements LoginContract.Presenter, DataSource.Callback<UserCard>  {


    public LoginPresenter(LoginContract.View view) {
        super(view);
    }

    @Override
    public void onDataLoaded(UserCard userCard) {

        final LoginContract.View view = getView();
        if (view == null) return;

        // 强制执行在主线程中
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                view.hideLoading();
                view.loginSuccess();
            }
        });

    }

    @Override
    public void onDataNotAvailable(int strRes) {
        // 网络请求告知注册失败
        final LoginContract.View view = getView();
        if (view == null) return;


        // 此时是从网络回送回来的，并不保证处于主线程, 强制执行在主线程中
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                view.hideLoading();
                // 调用主界面注册失败显示错误
                view.showError(strRes);
            }
        });

    }


    @Override
    public void login(String phone, String password) {
        start();
        final LoginContract.View view = getView();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            view.showError(R.string.data_account_login_invalid_parameter);
        } else {
            // 尝试传递PushId
            LoginModel model = new LoginModel(phone, password, Account.getPushId());
            AccountHelper.login(model, this);
        }

    }
}
