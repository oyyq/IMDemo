package net.oyyq.common.app;


import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import net.oyyq.common.R;


public abstract class ToolbarActivity extends Activity {


   protected Toolbar mToolbar;

    @Override
    protected void initWidget() {
        super.initWidget();
        initToolbar((Toolbar) findViewById(R.id.toolbar));
    }



    /**
     * 初始化toolbar
     * Toolbar上inflate Menu, 参考: https://blog.csdn.net/xieluoxixi/article/details/52949975
     * @param toolbar Toolbar
     */
    public void initToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//        }
        initTitleNeedBack();
    }


    protected void initTitleNeedBack() {
        // 设置左上角的返回按钮为实际的返回效果
        //ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//        }
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
    }


}
