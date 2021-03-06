package net.oyyq.dbflowtest.fragment.account;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import net.oyyq.common.app.PresenterFragment;
import net.oyyq.dbflowtest.presenter.account.RegisterContract;
import net.oyyq.dbflowtest.presenter.account.RegisterPresenter;
import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.activity.MainActivity;
import net.qiujuer.genius.ui.widget.Loading;

import butterknife.BindView;
import butterknife.OnClick;

public class RegisterFragment  extends PresenterFragment<RegisterContract.Presenter>
        implements RegisterContract.View {

    private AccountTrigger mAccountTrigger;

    @BindView(R.id.edit_phone)
    EditText mPhone;
    @BindView(R.id.edit_name)
    EditText mName;
    @BindView(R.id.edit_password)
    EditText mPassword;


    @BindView(R.id.loading)
    Loading mLoading;

    @BindView(R.id.btn_submit)
    Button mSubmit;


    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 拿到我们的Activity的引用
        mAccountTrigger = (AccountTrigger) context;
    }


    @Override
    protected RegisterContract.Presenter initPresenter() {
        return new RegisterPresenter(this);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_register;
    }


    @OnClick(R.id.btn_submit)
    void onSubmitClick() {
        String phone = mPhone.getText().toString();
        String name = mName.getText().toString();
        String password = mPassword.getText().toString();
        // 调用P层进行注册
        mPresenter.register(phone, name, password);
    }


    @OnClick(R.id.txt_go_login)
    void onShowLoginClick() {
        // 让AccountActivity进行界面切换
        mAccountTrigger.triggerView();
    }

    @Override
    public void showLoading() {
        super.showLoading();

        // 正在进行时，正在进行注册，界面不可操作
        mLoading.start();
        // 让控件不可以输入
        mPhone.setEnabled(false);
        mName.setEnabled(false);
        mPassword.setEnabled(false);
        // 提交按钮不可以继续点击
        mSubmit.setEnabled(false);
    }


    @Override
    public void hideLoading() {
        mLoading.stop();
    }


    @Override
    public void showError(int str) {
        super.showError(str);

        // 停止Loading
        mLoading.stop();
        // 让控件可以输入
        mPhone.setEnabled(true);
        mName.setEnabled(true);
        mPassword.setEnabled(true);
        // 提交按钮可以继续点击
        mSubmit.setEnabled(true);
    }


    @Override
    public void registerSuccess() {
        MainActivity.show(getContext());
        // AccountActivity
        getActivity().finish();
    }
}
