package net.oyyq.common.contract;


import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import net.oyyq.common.widget.adapter.RecyclerAdapter;

/**
 *  MVP模式中公共的基本契约
 */
public class BaseContract {


    // 基本的界面职责
   public  interface View<T extends Presenter> {

        // 支持设置一个Presenter
        void setPresenter(T presenter);

        // 公共的：显示进度条
        void showLoading();

        void hideLoading();

        // 公共的：显示一个字符串错误
        void showError(@StringRes int str);

    }


    // 基本的Presenter职责
    public interface Presenter {
        // 共用的开始触发
        void start();

        // 共用的销毁触发
        void destroy();
    }



   public interface RecyclerView<T extends Presenter, ViewModel> extends View<T> {

       RecyclerAdapter<ViewModel> getRecyclerAdapter();

        // 当适配器数据更改了的时候触发, 单刷或全刷 => 刷新界面
        void onAdapterDataChanged();

        androidx.recyclerview.widget.RecyclerView getRecyclerView();

       //清理RecyclerAdapter的缓存数据
        void clear();

   }


}
