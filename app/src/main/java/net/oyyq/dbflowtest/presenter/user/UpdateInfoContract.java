package net.oyyq.dbflowtest.presenter.user;

import net.oyyq.common.contract.BaseContract;

public interface UpdateInfoContract {

    interface Presenter extends BaseContract.Presenter {
        void update(String photoFilePath, String desc, boolean isMan);
    }

    interface View extends BaseContract.View<Presenter> {

        void updateSucceed();
    }
}
