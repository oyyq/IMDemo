package net.oyyq.common.widget.layout;

import android.content.Context;
import android.util.AttributeSet;

import com.bumptech.glide.RequestManager;

import net.oyyq.common.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PortraitView  extends CircleImageView {
    public PortraitView(Context context) {
        super(context);
    }


    public PortraitView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortraitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setup(RequestManager manager, String url) {
        setup(manager, R.drawable.default_portrait, url);
    }


    public void setup(RequestManager manager, int resourceId, String url) {
        if (url == null)
            url = "";
        manager.load(url)
                .placeholder(resourceId)
                .centerCrop()
                .dontAnimate()     // CircleImageView 控件中不能使用渐变动画，会导致显示延迟
                .into(this);

    }


    public void setEnabled(boolean b) {
        super.setEnabled(b);
    }
}
