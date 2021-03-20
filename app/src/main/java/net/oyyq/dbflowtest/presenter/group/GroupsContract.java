package net.oyyq.dbflowtest.presenter.group;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;

public interface GroupsContract {

    interface Presenter extends BaseContract.Presenter {

    }

    interface View extends BaseContract.RecyclerView<Presenter, Group> {

    }
}
