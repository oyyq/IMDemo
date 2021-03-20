package net.oyyq.dbflowtest.presenter.contact;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.model.datamodel.User;

import retrofit2.Call;

public interface PersonalContract {

    interface Presenter extends BaseContract.Presenter {
        // 获取用户信息
        User getUserPersonal();

        void changeFollowStatus(boolean mIsFollowUser, String userId);

    }

    interface View extends BaseContract.View<Presenter> {
        String getUserId();

        // 加载数据完成
        void onLoadDone(User user);

        // 是否发起聊天
        void allowSayHello(boolean isAllow);

        // 设置关注状态
        void setFollowStatus(boolean isFollow);
    }


}
