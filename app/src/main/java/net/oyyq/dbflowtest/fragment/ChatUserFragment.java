package net.oyyq.dbflowtest.fragment;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.common.widget.layout.PortraitView;
import net.oyyq.dbflowdemo.db.datarepo.MessageRepository;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.db.model.datamodel.User_Table;
import net.oyyq.dbflowtest.presenter.message.ChatContract;
import net.oyyq.dbflowtest.presenter.message.ChatPresenter;
import net.oyyq.dbflowtest.presenter.message.ChatUserPresenter;
import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.activity.PersonalActivity;

import butterknife.BindView;
import butterknife.OnClick;


public class ChatUserFragment extends ChatFragment<User> implements ChatContract.UserView{

    @BindView(R.id.im_portrait)
    PortraitView mPortrait;
    private MenuItem mUserInfoMenuItem;

    public ChatUserFragment(){
        // Required empty public constructor
    }


    @Override
    protected int getHeaderLayoutId() {
        return R.layout.lay_chat_header_user;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

        Glide.with(this)
                .load(R.drawable.default_banner_chat)
                .centerCrop()
                .into(new ViewTarget<CollapsingToolbarLayout, Drawable>(mCollapsingLayout) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        this.view.setContentScrim(resource.getCurrent());
                    }
                });
    }


    @Override
    protected void initToolbar() {
        super.initToolbar();

        Toolbar toolbar = mToolbar;
        toolbar.inflateMenu(R.menu.chat_user);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_person) {
                    onPortraitClick();
                }
                return false;
            }
        });

        // 拿到菜单Icon
        mUserInfoMenuItem = toolbar.getMenu().findItem(R.id.action_person);
    }


    @OnClick(R.id.im_portrait)
    void onPortraitClick() {
        PersonalActivity.show(getContext(), mReceiverId);
    }



    @Override
    protected ChatContract.Presenter initPresenter() {
        //nullable
        MessageRepository messageRepository =
                (MessageRepository) DbHelper.getLisenterForId(Message.class, MessageRepository.repoPrefix + mReceiverId);

        ChatPresenter presenter = new ChatUserPresenter(messageRepository, this, mReceiverId) ;
        presenter.setInitialUnread(unReadCount);
        return presenter;
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        super.onOffsetChanged(appBarLayout, verticalOffset);
        View view = mPortrait;
        MenuItem menuItem = mUserInfoMenuItem;
        if (view == null || menuItem == null) return;

        if (verticalOffset == 0) {
            // 完全展开
            view.setVisibility(View.VISIBLE);
            view.setScaleX(1);
            view.setScaleY(1);
            view.setAlpha(1);

            // 隐藏菜单
            menuItem.setVisible(false);
            menuItem.getIcon().setAlpha(0);
        } else {
            // abs 运算
            verticalOffset = Math.abs(verticalOffset);
            final int totalScrollRange = appBarLayout.getTotalScrollRange();
            if (verticalOffset >= totalScrollRange) {
                // 关闭状态
                view.setVisibility(View.INVISIBLE);
                view.setScaleX(0);
                view.setScaleY(0);
                view.setAlpha(0);

                // 显示菜单
                menuItem.setVisible(true);
                menuItem.getIcon().setAlpha(255);

            } else {
                // 中间状态
                float progress = 1 - verticalOffset / (float) totalScrollRange;
                view.setVisibility(View.VISIBLE);
                view.setScaleX(progress);
                view.setScaleY(progress);
                view.setAlpha(progress);
                // 和头像恰好相反
                menuItem.setVisible(true);
                menuItem.getIcon().setAlpha(255 - (int) (255 * progress));
            }
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        this.receiver = SQLite.select(User_Table.id, User_Table.portrait, User_Table.name).from(User.class).where(User_Table.id.eq(mReceiverId)).querySingle();
        onInit(this.receiver);
    }


    @Override
    public void onInit(User user) {
        // 对和你聊天的朋友的信息进行初始化操作
        mPortrait.setup(Glide.with(this), user.getPortrait());
        mCollapsingLayout.setTitle(user.getName());
    }

}
