package net.oyyq.dbflowtest.presenter.account;

import net.oyyq.common.contract.BaseContract;

public interface LoginContract {
    /**
     * View层: 显示隐藏Loading, 发起某动作经Presenter执行后
     * 执行结果的回调
     */
    interface View extends BaseContract.View<Presenter> {
        void loginSuccess();
    }

    /**
     * Presenter层: 真正执行某个动作
     */
    interface Presenter extends BaseContract.Presenter {
        void login(String phone, String password);
    }

}
