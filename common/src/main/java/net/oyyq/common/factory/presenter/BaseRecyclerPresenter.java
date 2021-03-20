package net.oyyq.common.factory.presenter;

import androidx.recyclerview.widget.DiffUtil;

import net.oyyq.common.contract.BaseContract;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.List;


/**
 *
 * @param <ViewModel>  RecyclerAdapter持有的数据类型
 * @param <View>
 */

public abstract class BaseRecyclerPresenter<ViewModel, View extends BaseContract.RecyclerView> extends BasePresenter<View> {

    public BaseRecyclerPresenter(View view) {
        super(view);
    }


    /**
     * 将RecyclerAdapter的数据进行整体替换刷新
     * @param dataList 新数据
     * 主线程中执行
     */
    protected void refreshData(final List<ViewModel> dataList) {
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                View view = getView();
                if (view == null) return;

                //这里强行指定RecyclerAdapter的范型为ViewModel
                RecyclerAdapter<ViewModel> adapter = view.getRecyclerAdapter();
                adapter.replace(dataList);
                view.onAdapterDataChanged();
            }
        });
    }



    /**
     * 刷新界面操作，该操作可以保证执行方法在主线程进行
     * 差异刷新和刷出初始数据均可
     * @param diffResult 一个差异的结果集
     * @param dataList   具体的新数据
     */
    protected void refreshData(final DiffUtil.DiffResult diffResult, final List<ViewModel> dataList) {
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                // 这里是主线程运行时
                refreshDataOnUiThread(diffResult, dataList);
            }
        });
    }



    private void refreshDataOnUiThread(final DiffUtil.DiffResult diffResult, final List<ViewModel> dataList) {
        View view = getView();
        if (view == null) return;
        view.hideLoading();

        RecyclerAdapter<ViewModel> adapter = view.getRecyclerAdapter();
        // 改变数据集合并不通知界面刷新
        adapter.getItems().clear();
        adapter.getItems().addAll(dataList);
        // 进行增量更新
        diffResult.dispatchUpdatesTo(adapter);


        view.onAdapterDataChanged();
    }




}
