package net.oyyq.common.widget.adapter;


/**
 * 刷新一条RecyclerAdapter中的数据
 * @param <Data>
 */
public interface AdapterCallback<Data> {
    void update(Data data, RecyclerAdapter.ViewHolder<Data> holder);
}
