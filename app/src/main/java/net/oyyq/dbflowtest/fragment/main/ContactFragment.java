package net.oyyq.dbflowtest.fragment.main;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import net.oyyq.common.app.PresenterFragment;
import net.oyyq.common.widget.EmptyView;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.common.widget.layout.PortraitView;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowtest.presenter.contact.ContactContract;
import net.oyyq.dbflowtest.presenter.contact.ContactPresenter;
import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.activity.PersonalActivity;
import butterknife.BindView;
import butterknife.OnClick;


public class ContactFragment extends PresenterFragment<ContactContract.Presenter>
        implements ContactContract.View  {


    @BindView(R.id.empty)
    EmptyView mEmptyView;

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    private RecyclerAdapter<User> mAdapter;


    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    protected ContactContract.Presenter initPresenter() {
        return new ContactPresenter(this);
    }

    @Override
    protected int getContentLayoutId() {
        return  R.layout.fragment_contact;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setAdapter(mAdapter = new RecyclerAdapter<User>() {
            @Override
            protected int getItemViewType(int position, User userCard) {
                return R.layout.cell_contact_list;
            }

            @Override
            protected ViewHolder<User> onCreateViewHolder(View root, int viewType) {
                return new ContactFragment.ViewHolder(root);
            }
        });

        // 点击mAdapter的每个Item的事件监听
        mAdapter.setListener(new RecyclerAdapter.AdapterListenerImpl<User>() {
            @Override
            public void onItemClick(RecyclerAdapter.ViewHolder holder, User user) {
                // 跳转到聊天界面
                //MessageActivity.show(getContext(), user);
            }
        });

        // 占位布局是RecyclerView
        mEmptyView.bind(mRecycler);
        setPlaceHolderView(mEmptyView);

    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public RecyclerAdapter<User> getRecyclerAdapter() {
        return mAdapter;
    }

    @Override
    public void onAdapterDataChanged() {
        mPlaceHolderView.triggerOkOrEmpty(mAdapter.getItemCount() > 0);
    }

    @Override
    public RecyclerView getRecyclerView() {
        return mRecycler;
    }

    @Override
    public void clear() {
        mAdapter.clear();
    }


    /**
     * 展示"我"的联系人
     */
    class ViewHolder extends RecyclerAdapter.ViewHolder<User> {
        @BindView(R.id.im_portrait)
        PortraitView mPortraitView;

        @BindView(R.id.txt_name)
        TextView mName;

        @BindView(R.id.txt_desc)
        TextView mDesc;

        public ViewHolder(View itemView) {
            super(itemView);
        }


        @Override
        protected void onBind(User user) {
            mPortraitView.setup(Glide.with(ContactFragment.this), user.getPortrait());
            mName.setText(user.getName());
            mDesc.setText(user.getDesc());
        }


        @OnClick(R.id.im_portrait)
        void onPortraitClick() {
            // 显示信息
            PersonalActivity.show(getContext(), mData.getId());
        }

    }




}
