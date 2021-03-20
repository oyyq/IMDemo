package net.oyyq.dbflowtest.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import net.oyyq.common.app.PresenterToolbarActivity;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.common.widget.layout.PortraitView;
import net.oyyq.common.widget.layout.RefreshLayout;
import net.oyyq.dbflowdemo.db.datarepo.GroupMembersRepository;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.helper.UserHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.User;

import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.fragment.group.addGroupMemberFragment;
import net.oyyq.dbflowtest.presenter.group.GroupMemberContract;
import net.oyyq.dbflowtest.presenter.group.GroupMemberPresenter;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;



/**
 * 群成员列表Activity, MessageActivity.startActivityForResult(..)
 */
public class GroupMemberActivity extends PresenterToolbarActivity<GroupMemberContract.Presenter>
        implements AppBarLayout.OnOffsetChangedListener, GroupMemberContract.View{

    public static final String KEY_GROUP_ID = "KEY_GROUP_ID";
    public static final String KEY_GROUP_ADMIN = "KEY_GROUP_ADMIN";
    public static final int EXIT_OK = -888;



    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.im_portrait)
    PortraitView mPortrait;


    @BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout mCollapsinglayout;


    RecyclerView mRecycler;
    private RecyclerAdapter<GroupMember> mAdapter;

    //Toolbar上的所有MenuItem
    private List<MenuItem> mInfoMenuItems = new ArrayList<>();

    private RefreshLayout mRefreshLayout;

    private String groupId;
    private Group group;
    private boolean mIsAdmin;


    private View Addadmin;
    private View Deletemember;
    private View Exitgroup;


    @Override
    protected boolean initArgs(Bundle bundle) {
        groupId = bundle.getString(KEY_GROUP_ID);
        mIsAdmin = bundle.getBoolean(KEY_GROUP_ADMIN);
        group = GroupHelper.findFromLocal(groupId);
        return !TextUtils.isEmpty(groupId) && group != null;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        initAppbar();
        initPortrait();
        initCollapsingLayout();
        initRecyclerView();
        AdminSetting();
    }


    private void initAppbar(){
        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    private void initPortrait() {
        mPortrait.setup(Glide.with(this), group.getPicture());    //群图片在OSS存储路径
    }


    private void initCollapsingLayout(){

        Glide.with(this)
                .load(R.drawable.default_banner_chat)
                .centerCrop()
                //into(ImageView),但mCollapsinglayout并不是ImageView, 所以要ViewTarget, 并将mColl..传递进去
                .into(new ViewTarget<CollapsingToolbarLayout, Drawable>(mCollapsinglayout) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        this.view.setContentScrim(resource.getCurrent());       //保持比例折叠
                    }

                });

        mCollapsinglayout.setTitle(group.getName());
    }


    /**
     * 初始化RefreshHeader
     */
    private void AdminSetting(){
        mRefreshLayout = findViewById(R.id.refreshlayout);
        if(mIsAdmin){
            mRefreshLayout.setRefreshHeader(LayoutInflater.from(this).inflate(R.layout.admin_headerview,null));
            View refreshHeader = mRefreshLayout.getRefreshHeader();
            // 找到refreshHeader的控件
            Addadmin = refreshHeader.findViewById(R.id.admin_add);
            Addadmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.addAmdin();
                }
            });
            Deletemember = refreshHeader.findViewById(R.id.delete_member);
            Deletemember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.delete();
                }
            });

            Exitgroup = refreshHeader.findViewById(R.id.exit);
        }else {
            mRefreshLayout.setRefreshHeader(LayoutInflater.from(this).inflate(R.layout.nonadmin_headerview,null));
            View refreshHeader = mRefreshLayout.getRefreshHeader();
            Exitgroup = refreshHeader.findViewById(R.id.exit);
        }


        Exitgroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.exit();
            }
        });


        if (mRefreshLayout != null) {
            // 刷新状态的回调
            mRefreshLayout.setRefreshListener(new RefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mRefreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRefreshLayout.refreshComplete();
                        }
                    }, 1000);
                }
            });

        }

    }



    private void initRecyclerView(){
        mRecycler = findViewById(R.id.recycler);
        RecyclerView recyclerView = mRecycler;
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        recyclerView.setAdapter(mAdapter = new RecyclerAdapter<GroupMember>() {
            @Override
            protected int getItemViewType(int position, GroupMember memberUserModel) {
                return R.layout.cell_group_member;
            }

            @Override
            protected ViewHolder<GroupMember> onCreateViewHolder(View root, int viewType) {
                return new GroupMemberActivity.ViewHolder(root);
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }




    @Override
    protected void initTitleNeedBack() {
        super.initTitleNeedBack();

        if(mIsAdmin){
            mToolbar.inflateMenu(R.menu.base_tool_bar_menu);
            mInfoMenuItems.add(mToolbar.getMenu().findItem(R.id.toolbar_add));
            mInfoMenuItems.add(mToolbar.getMenu().findItem(R.id.toolbar_search));

        } else {
            mToolbar.inflateMenu(R.menu.nonadmin_menu);
            mInfoMenuItems.add(mToolbar.getMenu().findItem(R.id.toolbar_search));
        }


        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.toolbar_add:
                        addGroupMemberFragment.newInstance(groupId)
                                .show(getSupportFragmentManager(), addGroupMemberFragment.class.getName());
                        return true;
                    case R.id.toolbar_search:
                        // 搜索群成员, 不拓展了
                        return true;
                }
                return false;
            }
        });

    }



    @Override
    protected GroupMemberContract.Presenter initPresenter() {
        GroupMembersRepository repo = (GroupMembersRepository) DbHelper.getLisenterForId(GroupMember.class, GroupMembersRepository.repoPrefix + groupId);
        return new GroupMemberPresenter(repo, this, groupId);
    }


    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_group_member;
    }



    @Override
    public void onDeleteSucceed() {
        hideLoading();
    }

    @Override
    public void onAddAdminSucceed() {
        hideLoading();
    }


    /**
     * 退出GroupMemberActivity && MessageActivity
     */
    @Override
    public void onExitSucceed() {
        hideLoading();
        Intent intent = new Intent();
        //把返回数据存入Intent
        intent.putExtra("result", "exit group succeed");
        //设置返回数据
        GroupMemberActivity.this.setResult(EXIT_OK, intent);
        //关闭Activity
        GroupMemberActivity.this.finish();
    }


    @Override
    public RecyclerAdapter<GroupMember> getRecyclerAdapter() {
        return mAdapter;
    }

    @Override
    public void onAdapterDataChanged() {
        //mPlaceHolderView.triggerOkOrEmpty(mAdapter.getItemCount() > 0);
    }

    @Override
    public RecyclerView getRecyclerView() {
        return mRecycler;
    }


    @Override
    public void clear() {
        mAdapter.clear();
    }



    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        View view = mPortrait;
        List<MenuItem> menuItems = mInfoMenuItems;

        if(view == null || menuItems == null)
            return;

        if(verticalOffset == 0){
            //完全展开
            view.setVisibility(View.VISIBLE);
            //没有缩放
            view.setScaleX(1);
            view.setScaleY(1);
            //设置透明度
            view.setAlpha(1);

            for(MenuItem item: menuItems) {
                item.setVisible(false);
                item.getIcon().setAlpha(0);
            }
            //在完全展开时refreshLayout能够监听并分发触摸事件, 能调用dispatchTouchEvent
            // 参考: https://www.jianshu.com/p/44769ef64ffa
            mRefreshLayout.setEnabled(true);

        }else{

            verticalOffset = Math.abs(verticalOffset);
            // AppBarLayout最多能够滚动的距离, 必须在onOffsetChanged中获取才是准确值
            final int totalScrollRange = appBarLayout.getTotalScrollRange();

            if(verticalOffset >= totalScrollRange){
                //拉动距离已经不小于最大拉动距离, 说明已经关闭掉了
                view.setVisibility(View.INVISIBLE);
                view.setScaleX(0);
                view.setScaleY(0);
                view.setAlpha(0);

                for(MenuItem item: menuItems) {
                    item.setVisible(true);
                    item.getIcon().setAlpha(255);          //对Drawable setAlpha与View.setAlpha是不同的
                }
            }else{
                //没完全拉动到关闭
                float progress =1- verticalOffset/(float)totalScrollRange;
                view.setVisibility(View.VISIBLE);
                view.setScaleX(progress);
                view.setScaleY(progress);
                view.setAlpha(progress);

                //和头像正好相反
                for(MenuItem item: menuItems) {
                    item.setVisible(true);
                    item.getIcon().setAlpha(255 - (int) (255 * progress));
                }

            }

            //setEnabled(false): 不能调用dispatchTouchEvent
            mRefreshLayout.setEnabled(false);
        }

    }



    class ViewHolder extends RecyclerAdapter.ViewHolder<GroupMember> {
        @BindView(R.id.im_portrait)
        PortraitView mPortrait;

        @BindView(R.id.txt_name)
        TextView mName;

        @BindView(R.id.cb_select)
        CheckBox mSelect;

        private boolean isSelected = false;


        ViewHolder(View itemView) {
            super(itemView);
            if(!mIsAdmin) mSelect.setVisibility(View.GONE);
        }

        @Override
        protected void onBind(GroupMember member) {
            User user = UserHelper.search(member.getUserId());
            mPortrait.setup(Glide.with(GroupMemberActivity.this), user.getPortrait());
            mName.setText(user.getName());

            if(member.isAdmin()){
                Drawable drawable= getResources().getDrawable(R.drawable.ic_group);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mName.setCompoundDrawables(null, null, drawable, null);
                mName.setCompoundDrawablePadding(-4);
            }

            mSelect.setChecked(isSelected);
        }


        @OnCheckedChanged(R.id.cb_select)
        void onCheckedChanged(boolean checked){
            isSelected = checked;
            mPresenter.changeSelect(mData, checked);
        }


        @OnClick(R.id.im_portrait)
        void onPortraitClick() {
            PersonalActivity.show(GroupMemberActivity.this, mData.getUserId());
        }
    }

}
