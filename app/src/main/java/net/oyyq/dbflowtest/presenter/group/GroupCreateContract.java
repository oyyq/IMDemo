package net.oyyq.dbflowtest.presenter.group;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.dbflowdemo.db.model.datamodel.User;

public interface GroupCreateContract {

    interface Presenter extends BaseContract.Presenter {
        // 创建
        void create(String name, String desc, String picture);

        // 更改一个Model的选中状态
        void changeSelect(ViewModel model, boolean isSelected);
    }

    interface View extends BaseContract.RecyclerView<Presenter, ViewModel> {
        // 创建成功
        void onCreateSucceed();
    }

    class ViewModel {
        // 用户信息
        public User author;
        // 是否选中
        public boolean isSelected;
    }

}
