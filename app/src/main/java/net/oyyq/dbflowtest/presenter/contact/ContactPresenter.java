package net.oyyq.dbflowtest.presenter.contact;

import androidx.recyclerview.widget.DiffUtil;

import net.oyyq.common.adapterUtil.DiffUiDataCallback;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.dbflowdemo.db.datarepo.ContactDataSource;
import net.oyyq.dbflowdemo.db.datarepo.ContactRepository;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.UserHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowtest.presenter.BaseSourcePresenter;

import java.util.List;


/**
 *  范型 : 第一个User: 指定ContactDataSource extends DbDataSource<User>, Repository监听Use表
 *         第二个User: 指定ContactContract.View extends BaseContract.RecyclerView<Presenter, User>
 *
 *  implements DataSource.SucceedCallback<List<User>> : BaseDbRepository<User> 需要DataSource.SucceedCallback<List<User>>
 *  作为加载缓存User成功的回调
 */
public class ContactPresenter extends BaseSourcePresenter<User, User, ContactDataSource, ContactContract.View>
        implements ContactContract.Presenter, DataSource.SucceedCallback<List<User>>  {


    public ContactPresenter(ContactContract.View view) {
        super(ContactRepository.getInstance(), view);
    }


    @Override
    public void start() {
        super.start();

        UserHelper.refreshContacts();
    }


    @Override
    public void onDataLoaded(List<User> users) {

        final ContactContract.View view = getView();
        if (view == null) return;

        RecyclerAdapter<User> adapter = view.getRecyclerAdapter();
        List<User> old = adapter.getItems();

        // 进行数据对比
        DiffUtil.Callback callback = new DiffUiDataCallback<>(old, users);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        // 界面刷新
        refreshData(result, users);
    }




}
