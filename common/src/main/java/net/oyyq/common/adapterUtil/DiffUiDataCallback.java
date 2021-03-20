package net.oyyq.common.adapterUtil;


import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

/**
 * Diff计算是耗时操作, 子线程中运行
 * 网络查询拿回来的数据放在子线程中做对比, 刷新到界面
 *
 * 范型的目的, 就是你是和一个你这个类型的数据进行比较
 */
public class DiffUiDataCallback <T extends DiffUiDataCallback.UiDataDiffer<T>> extends  DiffUtil.Callback {
    private List<T> mOldList, mNewList;

    public DiffUiDataCallback(List<T> mOldList, List<T> mNewList) {
        this.mOldList = mOldList;
        this.mNewList = mNewList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    // 两个类是否就是同一个东西，比如Id相等的User
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        T beanOld = mOldList.get(oldItemPosition);
        T beanNew = mNewList.get(newItemPosition);
        return beanNew.isSame(beanOld);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        T beanOld = mOldList.get(oldItemPosition);
        T beanNew = mNewList.get(newItemPosition);
        return beanNew.isUiContentSame(beanOld);
    }


    public interface UiDataDiffer<T> {
        // 传递一个旧的数据给你，问你是否和你标示的是同一个数据
        boolean isSame(T old);

        // 你和旧的数据对比，内容是否相同
        boolean isUiContentSame(T old);
    }

}
