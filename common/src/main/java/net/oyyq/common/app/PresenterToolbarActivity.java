package net.oyyq.common.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import net.oyyq.common.R;
import net.oyyq.common.contract.BaseContract;



/**
 * 有supportActionBar的Activity的公共父类
 * @param <Presenter>
 */
public abstract class PresenterToolbarActivity <Presenter extends BaseContract.Presenter>
        extends ToolbarActivity implements BaseContract.View<Presenter> {


    protected Presenter mPresenter;
    protected ProgressDialog mLoadingDialog;


    @Override
    protected void initBefore() {
        super.initBefore();
        // 初始化Presenter
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
        mPresenter = presenter;
    }



    @Override
    public void showError(int str) {
        hideDialogLoading();

        if (mPlaceHolderView != null) {
            mPlaceHolderView.triggerError(str);
        }
    }



    /**
     * hideLoading(): 用于显示成功界面
     */
    @Override
    public void hideLoading() {
        hideDialogLoading();
        if (mPlaceHolderView != null) {
            mPlaceHolderView.triggerOk();
        }
    }



    @Override
    public void showLoading() {
        if (mPlaceHolderView != null) {
            mPlaceHolderView.triggerLoading();
        } else {
            ProgressDialog dialog = mLoadingDialog;
            if (dialog == null) {
                dialog = new ProgressDialog(this, R.style.AppTheme_Dialog_Alert_Light);
                // 不可触摸取消
                dialog.setCanceledOnTouchOutside(false);
                // 强制取消关闭界面
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                mLoadingDialog = dialog;
            }

            dialog.setMessage(getText(R.string.prompt_loading));
            dialog.show();
        }
    }



    protected void hideDialogLoading() {
        ProgressDialog dialog = mLoadingDialog;
        if (dialog != null) {
            mLoadingDialog = null;
            dialog.dismiss();
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 界面关闭时进行销毁的操作
        if (mPresenter != null) {
            mPresenter.destroy();
        }
    }



}
