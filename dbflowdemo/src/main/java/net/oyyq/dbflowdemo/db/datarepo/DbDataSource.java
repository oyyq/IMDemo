package net.oyyq.dbflowdemo.db.datarepo;

import java.util.LinkedList;
import java.util.List;

/**
 * 基础的数据库
 *
 * @author oyyq Email:19110190008@fudan.edu.cn
 * @param <Data>
 */
public interface DbDataSource<Data> extends DataSource {
    /**
     * 基本的数据源加载方法, Presenter加载初始数据 (init data)
     * @param callback 传递一个callback回调，一般回调到Presenter
     */
    void load(SucceedCallback<List<Data>> callback);

    /**
     * 拿到DbDataSource缓存(不是在数据库)的数据
     * @return
     */
    LinkedList<Data> getDataList();

}
