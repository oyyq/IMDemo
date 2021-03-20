package net.oyyq.dbflowtest.presenter.message;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.model.datamodel.Session;

public interface SessionContract {

    // 什么都不需要额外定义，调用start即可
    interface Presenter extends BaseContract.Presenter {
    }

    interface View extends BaseContract.RecyclerView<Presenter, Session> {

    }

}

