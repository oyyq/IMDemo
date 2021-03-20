package net.oyyq.dbflowtest.presenter.contact;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;


public interface JoinContract {

    interface Presenter extends BaseContract.Presenter {
        //发起入群请求
        void join(Group group);
    }

    interface View extends BaseContract.View<Presenter> {
        // 请求成功后返回一个群信息
        void onJoinSucceed();
    }
}
