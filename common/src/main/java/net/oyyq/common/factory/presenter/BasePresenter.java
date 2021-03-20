package net.oyyq.common.factory.presenter;


import net.oyyq.common.contract.BaseContract;

/**
 * presenter层基础父类
 */
public abstract class BasePresenter<T extends BaseContract.View> implements BaseContract.Presenter {

    protected T mView;

    public BasePresenter(T view) {
        setView(view);
    }


    //Presenter和View相互持有
    @SuppressWarnings("unchecked")
    protected void setView(T view) {
        this.mView = view;
        this.mView.setPresenter(this);
    }



    protected final T getView() {
        return mView;
    }


    //Presenter开始调度时界面显示一个Loading
    @Override
    public void start() {
        T view = mView;
        if (view != null) {
            view.showLoading();
        }
    }



    @SuppressWarnings("unchecked")
    @Override
    public void destroy() {
        T view = mView;
        mView = null;
        if (view != null) {
            // View不再绑定Presenter
            view.setPresenter(null);
        }
    }


}
