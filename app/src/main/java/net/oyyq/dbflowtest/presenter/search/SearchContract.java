package net.oyyq.dbflowtest.presenter.search;


import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.Card.UserCard;

import java.util.List;

/**
 * @author qiujuer Email:qiujuer@live.cn
 * @version 1.0.0
 */
public interface SearchContract {
    interface Presenter extends BaseContract.Presenter {
        // 搜索内容
        void search(String content);
    }

    // 搜索人的界面
    interface UserView extends BaseContract.View<Presenter> {
        void onSearchDone(List<UserCard> userCards);
    }

    // 搜索群的界面
    interface GroupView extends BaseContract.View<Presenter> {
        void onSearchDone(List<GroupCard> groupCards);
    }

}
