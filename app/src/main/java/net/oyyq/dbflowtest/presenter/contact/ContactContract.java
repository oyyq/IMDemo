package net.oyyq.dbflowtest.presenter.contact;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.model.datamodel.User;

/**
 * "我"的联系人页面的契约
 */
public interface ContactContract {
    interface Presenter extends BaseContract.Presenter { }

    interface View extends BaseContract.RecyclerView<Presenter, User> { }


}
