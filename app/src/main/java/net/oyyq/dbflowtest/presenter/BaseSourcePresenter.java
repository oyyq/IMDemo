package net.oyyq.dbflowtest.presenter;


import net.oyyq.common.contract.BaseContract;
import net.oyyq.common.factory.presenter.BaseRecyclerPresenter;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.datarepo.DbDataSource;

import java.util.List;

/**
 * 在BaseDbRepository注册的Presenter, 监听BaseDbRepository缓存的,
 * 与本地数据表的记录保持一致的最新数据
 * 范型:
 *      1. Data: BaseSourcePresenter真实监听的数据类型
 *      2. ViewModel: 与Data关联的ViewModel
 *      3. Source : BaseSourcePresenter监听的数据源, 一般是BaseDbrepository
 *      4. 与Presenter关联的View
 */
public abstract class BaseSourcePresenter<Data, ViewModel, Source extends DbDataSource<Data>, View extends BaseContract.RecyclerView>
        extends BaseRecyclerPresenter<ViewModel, View> implements DataSource.SucceedCallback<List<Data>> {

    public BaseSourcePresenter(View view) {
        super(view);
    }

    protected Source mSource;


    public BaseSourcePresenter(Source source, View view) {
        super(view);
        this.mSource = source;
    }



    /**
     * 1. 让View showloading (UI)
     * 2. Presenter在Repository中注册监听Repository的数据
     * 3. Presenter从Repository中取已经缓存好的数据, 并刷新到UI层
     *     3.1 若Repository中没有缓存数据, 那么从数据表中查本地的持久化数据
     */
    @Override
    public void start() {
        super.start();
        if (mSource != null) mSource.load(this);
    }


    /**
     * 1. View与Presenter相互解除关联
     * 2. Presenter销毁了, 它监听的Repository也随之销毁了(解除监听数据表, 清空缓存的数据)
     * 3. Presenter和Reposiotry 相互解绑
     * TODO 避免出现多个Presenter监听一个Repository, 其中一个Presenter销毁时连带销毁Repository的情况!
     */
    @Override
    public void destroy() {
        super.destroy();
        mSource.dispose();
        mSource = null;
    }

    /**
     * 拿到Presenter监听的Repository
     * @return
     */
    public Source getSource(){
        return mSource;
    }


}
