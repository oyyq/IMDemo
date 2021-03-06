package net.oyyq.dbflowtest.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;
import net.oyyq.common.app.PresenterToolbarActivity;

import net.oyyq.dbflowtest.DemoApplication;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.common.widget.layout.PortraitView;

import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.fragment.media.GalleryFragment;
import net.oyyq.dbflowtest.presenter.group.GroupCreateContract;
import net.oyyq.dbflowtest.presenter.group.GroupCreatePresenter;

import java.io.File;
import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class GroupCreateActivity extends PresenterToolbarActivity<GroupCreateContract.Presenter>
        implements GroupCreateContract.View {

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    @BindView(R.id.edit_name)
    EditText mName;

    @BindView(R.id.edit_desc)
    EditText mDesc;

    @BindView(R.id.im_portrait)
    PortraitView mPortrait;

    private String mPortraitPath;
    private Adapter mAdapter;


    public static void show(Context context) {
        context.startActivity(new Intent(context, GroupCreateActivity.class));
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_group_create;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        setTitle("");

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(mAdapter = new Adapter());
    }

    @Override
    protected void initData() {
        super.initData();
        mPresenter.start();
    }

    @OnClick(R.id.im_portrait)
    void onPortraitClick() {
        hideSoftKeyboard();

        new GalleryFragment()
                .setListener(new GalleryFragment.OnSelectedListener() {
                    @Override
                    public void onSelectedImage(String path) {
                        UCrop.Options options = new UCrop.Options();
                        // ???????????????????????????JPEG
                        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                        // ??????????????????????????????
                        options.setCompressionQuality(96);

                        // ???????????????????????????
                        File dPath = DemoApplication.getPortraitTmpFile();

                        // ????????????,
                        UCrop.of(Uri.fromFile(new File(path)), Uri.fromFile(dPath))
                                .withAspectRatio(1, 1) // 1???1??????
                                .withMaxResultSize(520, 520) // ?????????????????????
                                .withOptions(options) // ????????????
                                .start(GroupCreateActivity.this);
                    }
                })
                .show(getSupportFragmentManager(), GalleryFragment.class.getName());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // ?????????Activity??????????????????????????????????????????????????????????????????
        // ?????????????????????????????????
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            // ??????UCrop???????????????Uri
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                loadPortrait(resultUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            DemoApplication.showToast(R.string.data_rsp_error_unknown);
        }
    }


    /**
     * ??????Uri?????????????????????
     *
     * @param uri Uri
     */
    private void loadPortrait(Uri uri) {
        // ??????????????????
        mPortraitPath = uri.getPath();

        Glide.with(this)
                .asBitmap()
                .load(uri)
                .centerCrop()
                .into(mPortrait);
    }



    @Override
    protected void initTitleNeedBack() {
        super.initTitleNeedBack();

        mToolbar.inflateMenu(R.menu.group_create);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_create: {
                        onCreateClick();
                        return true;
                    }
                }
                return false;
            }
        });
    }




    // ??????????????????
    private void onCreateClick() {
        hideSoftKeyboard();
        String name = mName.getText().toString().trim();
        String desc = mDesc.getText().toString().trim();
        mPresenter.create(name, desc, mPortraitPath);
    }




    // ???????????????
    private void hideSoftKeyboard() {
        // ???????????????View
        View view = getCurrentFocus();
        if (view == null)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    @Override
    public void onCreateSucceed() {
        // ????????????
        hideLoading();
        DemoApplication.showToast(R.string.label_group_create_succeed);
        finish();
    }

    @Override
    protected GroupCreateContract.Presenter initPresenter() {
        return new GroupCreatePresenter(this);
    }

    @Override
    public RecyclerAdapter<GroupCreateContract.ViewModel> getRecyclerAdapter() {
        return mAdapter;
    }

    @Override
    public void onAdapterDataChanged() {
        hideLoading();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return mRecycler;
    }


    @Override
    public void clear() {
        this.mAdapter.clear();
    }


    private class Adapter extends RecyclerAdapter<GroupCreateContract.ViewModel> {

        @Override
        protected int getItemViewType(int position, GroupCreateContract.ViewModel viewModel) {
            return R.layout.cell_group_create_contact;
        }

        @Override
        protected ViewHolder<GroupCreateContract.ViewModel> onCreateViewHolder(View root, int viewType) {
            return new GroupCreateActivity.ViewHolder(root);
        }
    }


    class ViewHolder extends RecyclerAdapter.ViewHolder<GroupCreateContract.ViewModel> {
        @BindView(R.id.im_portrait)
        PortraitView mPortrait;
        @BindView(R.id.txt_name)
        TextView mName;
        @BindView(R.id.cb_select)
        CheckBox mSelect;


        ViewHolder(View itemView) {
            super(itemView);
        }

        @OnCheckedChanged(R.id.cb_select)
        void onCheckedChanged(boolean checked) {
            // ??????????????????
            mPresenter.changeSelect(mData, checked);
        }

        @Override
        protected void onBind(GroupCreateContract.ViewModel viewModel) {
            mPortrait.setup(Glide.with(GroupCreateActivity.this), viewModel.author.getPortrait());
            mName.setText(viewModel.author.getName());
            mSelect.setChecked(viewModel.isSelected);
        }
    }


}
