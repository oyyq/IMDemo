package net.oyyq.dbflowtest.fragment.search;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.oyyq.common.app.PresenterFragment;
import net.oyyq.common.widget.EmptyView;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.common.widget.layout.PortraitView;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowtest.presenter.contact.JoinContract;
import net.oyyq.dbflowtest.presenter.contact.JoinPresenter;
import net.oyyq.dbflowtest.presenter.search.SearchContract;
import net.oyyq.dbflowtest.presenter.search.SearchGroupPresenter;
import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.activity.SearchActivity;
import net.qiujuer.genius.ui.Ui;
import net.qiujuer.genius.ui.compat.UiCompat;
import net.qiujuer.genius.ui.drawable.LoadingCircleDrawable;
import net.qiujuer.genius.ui.drawable.LoadingDrawable;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 搜索群: "我"不在的陌生群, 不在Group_Table存储记录
 * "我"发起申请加入群
 */

public class SearchGroupFragment extends PresenterFragment<SearchContract.Presenter>
        implements SearchActivity.SearchFragment, SearchContract.GroupView{

    @BindView(R.id.empty)
    EmptyView mEmptyView;

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    private RecyclerAdapter<GroupCard> mAdapter;


    public SearchGroupFragment() {
        // Required empty public constructor
    }


    @Override
    protected void initData() {
        super.initData();
        // 发起首次搜索
        search("");
    }


    @Override
    protected SearchContract.Presenter initPresenter() {
        return new SearchGroupPresenter(this);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_search_group;
    }


    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setAdapter(mAdapter = new RecyclerAdapter<GroupCard>() {
            @Override
            protected int getItemViewType(int position, GroupCard userCard) {
                return R.layout.cell_search_group_list;
            }

            @Override
            protected ViewHolder<GroupCard> onCreateViewHolder(View root, int viewType) {
                return new SearchGroupFragment.ViewHolder(root);
            }
        });

        mEmptyView.bind(mRecycler);
        setPlaceHolderView(mEmptyView);
    }



    @Override
    public void onSearchDone(List<GroupCard> groupCards) {
        mAdapter.replace(groupCards);
        mPlaceHolderView.triggerOkOrEmpty(mAdapter.getItemCount() > 0);
    }

    @Override
    public void search(String content) {
        mPresenter.search(content);
    }



    class ViewHolder extends RecyclerAdapter.ViewHolder<GroupCard> implements JoinContract.View {
        @BindView(R.id.im_portrait)
        PortraitView mPortraitView;

        @BindView(R.id.txt_name)
        TextView mName;

        @BindView(R.id.im_join)
        ImageView mJoin;

        JoinContract.Presenter mPresenter;
        private Group group;

        public ViewHolder(View itemView) {
            super(itemView);
            new JoinPresenter(this);
        }

        @Override
        protected void onBind(GroupCard groupCard) {
            mPortraitView.setup(Glide.with(SearchGroupFragment.this), groupCard.getPicture());
            mName.setText(groupCard.getName());
            // 判断是否加入群 ->
            mJoin.setEnabled(groupCard.getJoinAt() == null);
            this.group = groupCard.build();
        }

        @OnClick(R.id.im_join)
        void onJoinClick() {
            //发起入群申请
            mPresenter.join(group);
        }

        @Override
        public void onJoinSucceed() {
            mJoin.setEnabled(false);
        }

        @Override
        public void setPresenter(JoinContract.Presenter presenter) {
            mPresenter = presenter;
        }

        @Override
        public void showLoading() {
            int minSize = (int) Ui.dipToPx(getResources(), 22);
            int maxSize = (int) Ui.dipToPx(getResources(), 30);
            // 初始化一个圆形的动画的Drawable
            LoadingDrawable drawable = new LoadingCircleDrawable(minSize, maxSize);
            drawable.setBackgroundColor(0);

            int[] color = new int[]{UiCompat.getColor(getResources(), R.color.white_alpha_208)};
            drawable.setForegroundColor(color);
            // 设置进去
            mJoin.setImageDrawable(drawable);
            // 启动动画
            drawable.start();
        }

        @Override
        public void hideLoading() {
            if (mJoin.getDrawable() instanceof LoadingDrawable) {
                ((LoadingDrawable) mJoin.getDrawable()).stop();
                mJoin.setImageResource(R.drawable.sel_opt_done_add);
            }
        }


        @Override
        public void showError(int str) {
            if (mJoin.getDrawable() instanceof LoadingDrawable) {
                // 失败则停止动画，并且显示一个圆圈
                LoadingDrawable drawable = (LoadingDrawable) mJoin.getDrawable();
                drawable.setProgress(1);
                drawable.stop();
            }
        }


    }




}
