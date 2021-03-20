package net.oyyq.dbflowdemo.db.datarepo;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import net.oyyq.common.util.CollectionUtil;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Data, 数据表实体类, dataList缓存的数据和数据表中更新的最新记录一样
 *
 * BaseDbRepository: 实现对数据库的基本监听操作, 并缓存一些数据
 * 注意: 1. 每个BaseDbRepository有一个id, 场景: 多个Repository监听一张Message表, 到达不同会话的Message要交给
 *          某个id的BaseDbRepository进行监听缓存, 同时, 当Message中对应某会话的记录全部被删除("我"不再关注会话的消息),
 *          那么对应id的Repository也从DbHelper中解注册
 *
 *      2.  Presenter层是callback, 当Presenter层销毁时, Repo不随之销毁(解注册), 而是继续监听.
 *          所有的Repo, 要么在数据表记录删除时被解注册并销毁, 要么在MainActivity finish后全部解注册
 *
 *      3. 一个Repository可以持有多个callback (Presenter)
 *      4. 生命周期从
 *
 * @param <Data>
 */
public abstract class BaseDbRepository<Data extends BaseDbModel<Data>>
        implements DbDataSource<Data>, DbHelper.ChangedListener<Data>,
        QueryTransaction.QueryResultListCallback<Data> {

    //所有关注Repository缓存数据的Presenter
    private List<SucceedCallback<List<Data>> > callbacks = new LinkedList<>();
    protected LinkedList<Data> dataList = new LinkedList<>();         // 当前缓存的数据
    protected Class<Data> dataClass;                                     //表实体类类型, 在子类中解析
    private static Executor executor = Executors.newSingleThreadExecutor();


    /**
     * 子类必须复写
     */
    @Override
    public String getId() {
        return "";
    }
    @Override
    public LinkedList<Data> getDataList() {
        return dataList;
    }
    public List<SucceedCallback<List<Data>>> getCallbacks() {
        return callbacks;
    }


    /**
     * Presenter层第一次加载数据 => 加载初始化数据, 同时注册一个Repository到DbHelper(若还没注册的情况下)
     * 在一个Repository被注册到DbHelper并且没有被解注册(生命周期内), 要一直监听数据表
     * 在Presenter被注册到Repository, 首先拉取dataList的缓存数据, 若dataList.size() == 0,
     * 则从数据表中加载数据.
     *
     * @param callback 传递一个callback回调，一般回调到Presenter
     */
    @Override
    public void load(SucceedCallback<List<Data>> callback) {
        //注册Repository到DbHelper, 监听数据表
        registerDbChangedListener();
        if(callback != null) {
            if(this.callbacks == null) this.callbacks = new LinkedList<>();
            if(this.callbacks.contains(callback)) return;
            this.callbacks.add(callback);
        }
    }


    public void removeCallback( SucceedCallback<List<Data>> callback ){
        if(this.callbacks != null)
            this.callbacks.remove(callback);
    }

    // 数据库统一通知的地方: 本地数据库 增加新数据 / 替换旧数据时, 通知到dataList
    @Override
    public void onDataSave(boolean notifyout,  Data... list) {
        boolean isChanged = false;
        for (Data data : list) {
            if (isRequired(data)) {
                if (insertOrUpdate(data)) {
                    isChanged = true;
                }
            }
        }

        if (notifyout && isChanged) notifyDataChange();
    }



    // 插入或者更新, 没有"删除"的逻辑, 删除逻辑在子类复写, 只有onDataSave调用
    protected boolean insertOrUpdate(Data data) {
        int index = indexOf(data);
        if (index >= 0) {
            if(!data.isUiContentSame(dataList.get(index))) {
                //能够改变View层的UI才替换
                replace(index, data);
                return true;
            }
            return false;
        } else {
            //没有这条新数据, 插入到缓存
            insert(data);
            return true;
        }
    }



    // 数据库统一通知删除的地方, 将dataList中对应项删除
    @Override
    public void onDataDelete(boolean notifyout,  Data... list) {
        boolean isChanged = false;
        for(Data data: list){
            int index = indexOf(data);
            if( index >= 0 ){
                dataList.remove(index);
                isChanged= true;
            }
        }
        if (notifyout && isChanged) notifyDataChange();
    }


    /**
     * Repository没有缓存数据,从数据表加载初始数据进行缓存
     * @param transaction
     * @param tResult
     */
    @Override
    public void onListQueryResult(QueryTransaction transaction, @NonNull List<Data> tResult) {
        if (tResult.size() == 0) {
            //从数据表查询数据失败 !
            dataList.clear();
            notifyDataChange();
            return;
        }

        Data[] data = CollectionUtil.toArray(tResult, dataClass);

        onDataSave(true, data);
    }


    //将Repository缓存的数据替换成数据库的最新记录
    protected void replace(int index, Data data) {
        dataList.remove(index);
        dataList.add(index, data);
    }


    //dataList添加数据, 子类复写
    protected abstract void insert(Data data);


    /**
     * 检查一个数据是否是我需要的数据.
     * @param data Data
     * @return  True是我关注的数据
     */
    protected abstract boolean isRequired(Data data);


    /**
     * @param data Data
     * @return Data的索引
     */
    public int indexOf(Data data){
        int index = -1;
        for(Data data1 : dataList){
            index++;
            if (data1.isSame(data)) {
                return index;
            }
        }
        return -1;
    }


    protected void registerDbChangedListener(){
        DbHelper.addChangedListener(dataClass, this);
    }



    /**
     * 通知界面刷新, 对所有关注dataList变化的Presenter通知刷新
     * 放在线程里做
     */
    @SuppressLint("NewApi")
    protected void notifyDataChange() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<SucceedCallback<List<Data>>> callbacks = BaseDbRepository.this.callbacks;
                callbacks.stream().forEach(callback -> { if(callback != null) callback.onDataLoaded(dataList); });
            }
        });
    }




    /**
     * 销毁, 缓存数据也不要了
     */
    @Override
    public void dispose() {
        DbHelper.removeChangedListener(dataClass, this);
        dataList.clear();
        this.callbacks.clear();
        this.callbacks = null;
    }



}
