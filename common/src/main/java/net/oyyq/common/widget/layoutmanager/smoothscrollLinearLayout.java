package net.oyyq.common.widget.layoutmanager;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 控制RecyclerView的滑动速度以及
 * 顺滑滑动到指定位置并置顶的Layoutmanager
 * 来源: https://mcochin.wordpress.com/2015/05/13/android-customizing-smoothscroller-for-the-recyclerview/
 * https://stackoverflow.com/questions/31235183/recyclerview-how-to-smooth-scroll-to-top-of-item-on-a-certain-position
 */
public class smoothscrollLinearLayout  extends LinearLayoutManager {
    //滑动速度
    private float MILLISECONDS_PER_INCH = 1f;
    private Context context;

    public smoothscrollLinearLayout(Context context) {
        this(context, VERTICAL, false);
    }

    /**
     * @param context
     * @param orientation
     * @param reverseLayout When set to true, layouts from end to start. 设置成true, 则adapter中的数据倒序展示,
     *                      并且RecyclerView自动滑动到最后
     */
    public smoothscrollLinearLayout(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.context = context;
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }


    private class TopSnappedSmoothScroller extends LinearSmoothScroller {
        public TopSnappedSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return smoothscrollLinearLayout.this.computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }


        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics){
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;

        }

    }





    //设置慢速
    public void setSpeedSlow() {
        //自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
        //0.3f是自己估摸的一个值，可以根据不同需求自己修改
        MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().densityDpi * 0.3f;
    }

    //快速
    public void setSpeedFast() {
        MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().densityDpi * 0.03f;
    }


}
