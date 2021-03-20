package net.oyyq.dbflowtest.presenter.account;

import net.oyyq.common.contract.BaseContract;

public interface RegisterContract {

    interface View extends BaseContract.View<Presenter> {
        void registerSuccess();
    }

    interface Presenter extends BaseContract.Presenter {

        void register(String phone, String name, String password);

        boolean checkMobile(String phone);
    }

}
