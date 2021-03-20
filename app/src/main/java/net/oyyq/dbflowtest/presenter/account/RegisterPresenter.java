package net.oyyq.dbflowtest.presenter.account;

import android.text.TextUtils;

import net.oyyq.common.Common;
import net.oyyq.common.factory.presenter.BasePresenter;
import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.AccountHelper;
import net.oyyq.dbflowdemo.db.model.RegisterModel;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.regex.Pattern;


/**
 *  1. extends BasePresenter<RegisterContract.View>:
 *      执行基本Presenter的职责
 *  2.  implements RegisterContract.Presenter: 执行RegisterPresenter的特有职责
 *  3.  implements DataSource.Callback<UserCard> 连接服务器, 接收服务器的返回结果
 */

public class RegisterPresenter extends BasePresenter<RegisterContract.View>
        implements RegisterContract.Presenter, DataSource.Callback<UserCard>  {


    public RegisterPresenter(RegisterContract.View view) {
        super(view);
    }


    @Override
    public void onDataLoaded(UserCard userCard) {
        final RegisterContract.View view = getView();
        if (view == null) return;


        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                view.hideLoading();

                view.registerSuccess();
            }
        });

    }

    @Override
    public void onDataNotAvailable(int strRes) {
        final RegisterContract.View view = getView();
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
    public void register(String phone, String name, String password) {

        start();
        RegisterContract.View view = getView();

        // 校验
        if (!checkMobile(phone)) {
            // 提示
            view.showError(R.string.data_account_register_invalid_parameter_mobile);
        } else if (name.length() < 2) {
            // 姓名需要大于2位
            view.showError(R.string.data_account_register_invalid_parameter_name);
        } else if (password.length() < 6) {
            // 密码需要大于6位
            view.showError(R.string.data_account_register_invalid_parameter_password);
        } else {
            // 进行网络请求
            // 构造Model，进行请求调用
            RegisterModel model = new RegisterModel(phone, password, name, Account.getPushId());
            // 进行网络请求，并设置回送接口为自己
            AccountHelper.register(model, this);
        }

    }

    @Override
    public boolean checkMobile(String phone) {
        // 手机号不为空，并且满足格式
        return !TextUtils.isEmpty(phone)
                && Pattern.matches(Common.Constance.REGEX_MOBILE, phone);

    }
}
