package net.oyyq.common.app;

import android.content.Context;

import net.oyyq.common.contract.BaseContract;


public abstract class PresenterFragment<Presenter extends BaseContract.Presenter>
        extends Fragment implements BaseContract.View<Presenter>{


    protected Presenter mPresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initPresenter();
    }


    /**
     * 初始化Presenter
     *
     * @return Presenter
     */
    protected abstract Presenter initPresenter();


    @Override
    public void setPresenter(Presenter presenter) {
        // View中赋值Presenter
        mPresenter = presenter;
    }


    @Override
    public void showLoading() {
        if (mPlaceHolderView != null) {
            mPlaceHolderView.triggerLoading();
        }
    }


    @Override
    public void hideLoading() { }

    @Override
    public void showError(int str) {
        if (mPlaceHolderView != null) {
            mPlaceHolderView.triggerError(str);
        } else {
            //DemoApplication.showToast(str);
        }
    }



    //Fragment完全销毁, 后面回调onDetach
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPresenter != null) mPresenter.destroy();
    }


}

