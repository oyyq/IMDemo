package net.oyyq.dbflowtest.fragment.group;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.oyyq.dbflowtest.DemoApplication;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.common.widget.layout.PortraitView;
import net.oyyq.dbflowdemo.db.model.datamodel.User;

import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.fragment.media.GalleryFragment;
import net.oyyq.dbflowtest.presenter.group.GroupMemberAddPresenter;
import net.oyyq.dbflowtest.presenter.group.GroupMemberContract;
import net.qiujuer.genius.ui.compat.UiCompat;
import java.util.List;



public class addGroupMemberFragment extends BottomSheetDialogFragment
        implements GroupMemberContract.addView  {

    public static final String GROUPID = "GROUPID";
    private String groupId;
    RecyclerView mRecycler;
    Toolbar mToolbar;


    private Adapter mAdapter;
    private GroupMemberContract.addPresenter mPresenter;


    public static addGroupMemberFragment newInstance(String groupId) {
        addGroupMemberFragment fragment = new addGroupMemberFragment();
        Bundle args = new Bundle();
        args.putString(GROUPID, groupId);
        fragment.setArguments(args);
        return fragment;
    }



    @SuppressWarnings("NullableProblems")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new GalleryFragment.TransStatusBottomSheetDialog(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        groupId = getArguments().getString(GROUPID);

        View root = inflater.inflate(R.layout.fragment_group_member_add, container, false);
        mRecycler = root.findViewById(R.id.recycler);
        mToolbar = root.findViewById(R.id.toolbar);

        initRecycler();
        initToolbar();

        initPresenter();
        return root;
    }


    private void initPresenter() {
        mPresenter = new GroupMemberAddPresenter(this, groupId);
        mPresenter.search();
    }


    private void initRecycler() {
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setAdapter(mAdapter = new Adapter());
    }


    private void initToolbar() {
        mToolbar.inflateMenu(R.menu.member_add);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.add_member) {
                    if (mPresenter != null) mPresenter.addnewMembers();
                    return true;
                }
                return false;
            }
        });

        MenuItem item = mToolbar.getMenu().findItem(R.id.add_member);
        Drawable drawable = item.getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, UiCompat.getColor(getResources(), R.color.alertImportant));
        item.setIcon(drawable);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPresenter != null) mPresenter.destroy();
    }


    @Override
    public void onSearchDone(List<User> users) {
        mAdapter.replace(users);
    }

    @Override
    public void onAddSuccedd() {
        dismiss();
    }


    @Override
    public void setPresenter(GroupMemberContract.addPresenter presenter) {
        this.mPresenter = presenter;
    }


    @Override
    public void showLoading() { }
    @Override
    public void hideLoading() { }

    @Override
    public void showError(int strRes) {
        DemoApplication.showToast(strRes);
    }


    private class Adapter extends RecyclerAdapter<User> {

        @Override
        protected int getItemViewType(int position, User user) {
            return R.layout.cell_group_member_add;
        }

        @Override
        protected ViewHolder<User> onCreateViewHolder(View root, int viewType) {
            return new addGroupMemberFragment.ViewHolder(root);
        }
    }



    class ViewHolder extends RecyclerAdapter.ViewHolder<User>{

        PortraitView mPortrait;
        TextView mName;
        CheckBox mSelect;
        boolean isSelected = false;

        public ViewHolder(View itemView) {
            super(itemView);
            mPortrait = itemView.findViewById(R.id.im_portrait);
            mName = itemView.findViewById(R.id.txt_name);
            mSelect = itemView.findViewById(R.id.cb_select);
            mSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mPresenter.changeSelect(mData, isChecked);
                    isSelected = isChecked;
                    mSelect.setChecked(isSelected);
                }
            });
        }

        @Override
        protected void onBind(User user) {
            mPortrait.setup(Glide.with(addGroupMemberFragment.this), user.getPortrait());
            mName.setText(user.getName());
            mSelect.setChecked(isSelected);
        }

    }




}
