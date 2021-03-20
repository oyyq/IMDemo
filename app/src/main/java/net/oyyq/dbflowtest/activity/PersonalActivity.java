package net.oyyq.dbflowtest.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import com.bumptech.glide.Glide;
import net.oyyq.common.app.PresenterToolbarActivity;
import net.oyyq.common.widget.layout.PortraitView;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.presenter.contact.PersonalContract;
import net.oyyq.dbflowtest.presenter.contact.PersonalPresenter;
import net.qiujuer.genius.res.Resource;
import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;


public class PersonalActivity extends PresenterToolbarActivity<PersonalContract.Presenter>
                        implements PersonalContract.View {

    private static final String BOUND_KEY_ID = "BOUND_KEY_ID";
    private String userId;


    @BindView(R.id.im_header)
    ImageView mHeader;
    @BindView(R.id.im_portrait)
    PortraitView mPortrait;
    @BindView(R.id.txt_name)
    TextView mName;
    @BindView(R.id.txt_desc)
    TextView mDesc;
    @BindView(R.id.txt_follows)
    TextView mFollows;
    @BindView(R.id.txt_following)
    TextView mFollowing;
    @BindView(R.id.btn_say_hello)
    Button mSayHello;

    private MenuItem mFollowItem;
    //"我"是否关注了他 ?
    private boolean mIsFollowUser = false;

    public static void show(Context context, String userId) {
        Intent intent = new Intent(context, PersonalActivity.class);
        intent.putExtra(BOUND_KEY_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_personal;
    }

    @Override
    protected boolean initArgs(Bundle bundle) {
        userId = bundle.getString(BOUND_KEY_ID);
        return !TextUtils.isEmpty(userId);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        setTitle("");
    }


    /**
     * 从PersonalActivity进入到MessageActivity走了onPause -> onResume的流程
     */
    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }


    @Override
    protected void initTitleNeedBack() {
        super.initTitleNeedBack();
        if(Account.getUserId().equalsIgnoreCase(userId)) return;


        mToolbar.inflateMenu(R.menu.personal);
        Menu menu = mToolbar.getMenu();
        mFollowItem = menu.findItem(R.id.action_follow);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_follow: {
                        ((PersonalPresenter) mPresenter).changeFollowStatus(mIsFollowUser, userId);
                        return true;
                    }
                }
                return false;
            }
        });

    }



    @OnClick(R.id.btn_say_hello)
    void onSayHelloClick() {
        // 发起聊天的点击
        User user = mPresenter.getUserPersonal();
        if (user == null) return;
        MessageActivity.show(this, user);
    }


    /**
     * 更改关注菜单状态
     */
    private void changeFollowItemStatus() {
        if (mFollowItem == null) return;

        // 根据关注状态设置颜色
        Drawable drawable = mIsFollowUser ? getResources()
                .getDrawable(R.drawable.ic_favorite) :
                getResources().getDrawable(R.drawable.ic_favorite_border);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, Resource.Color.WHITE);
        mFollowItem.setIcon(drawable);
    }


    @Override
    public String getUserId() {
        return userId;
    }


    @SuppressLint("StringFormatMatches")
    @Override
    public void onLoadDone(User user) {
        if (user == null) return;
        mPortrait.setup(Glide.with(this), user.getPortrait());
        mName.setText(user.getName());
        mDesc.setText(user.getDesc());
        mFollows.setText(String.format(getString(R.string.label_follows), user.getFollows()));
        mFollowing.setText(String.format(getString(R.string.label_following), user.getFollowing()));
        hideLoading();
    }

    @Override
    public void allowSayHello(boolean isAllow) {
        mSayHello.setVisibility(isAllow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setFollowStatus(boolean isFollow) {
        mIsFollowUser = isFollow;
        changeFollowItemStatus();
    }

    @Override
    protected PersonalContract.Presenter initPresenter() {
        return new PersonalPresenter(this);
    }



}
