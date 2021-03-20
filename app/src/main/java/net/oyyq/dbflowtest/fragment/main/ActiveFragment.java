package net.oyyq.dbflowtest.fragment.main;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.common.app.PresenterFragment;
import net.oyyq.common.face.Face;
import net.oyyq.common.widget.EmptyView;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.common.widget.layout.PortraitView;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.ApplyHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Apply;
import net.oyyq.dbflowdemo.db.model.datamodel.Apply_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Session;
import net.oyyq.dbflowtest.activity.MessageActivity;
import net.oyyq.dbflowtest.presenter.message.SessionContract;
import net.oyyq.dbflowtest.presenter.message.SessionPresenter;
import net.oyyq.dbflowtest.R;
import net.qiujuer.genius.ui.widget.Button;
import net.qiujuer.genius.ui.widget.Loading;
import java.text.SimpleDateFormat;
import butterknife.BindView;



public class ActiveFragment extends PresenterFragment<SessionContract.Presenter>
        implements SessionContract.View {

    @BindView(R.id.empty)
    EmptyView mEmptyView;   //this.onAdapterDataChanged(): mPresenter.onDataLoaded将数据加载到mAdapter, 加载到UI上时
    //需要判断数据是不是空的.

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    private RecyclerAdapter<Session> mAdapter;

    public ActiveFragment() {
        // Required empty public constructor
    }


    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setAdapter(mAdapter = new RecyclerAdapter<Session>() {
            @Override
            protected int getItemViewType(int position, Session session) {
                return R.layout.cell_chat_list;
            }

            @Override
            protected ViewHolder<Session> onCreateViewHolder(View root, int viewType) {
                return new ActiveFragment.ViewHolder(root);
            }
        });


        mAdapter.setListener(new RecyclerAdapter.AdapterListenerImpl<Session>() {
            @Override
            public void onItemClick(RecyclerAdapter.ViewHolder holder, Session session) {
                MessageActivity.show(getContext(), session);
            }
        });

        // 初始化占位布局
        mEmptyView.bind(mRecycler);
        setPlaceHolderView(mEmptyView);

    }


    @Override
    public void onStart() {
        super.onStart();
        ((SessionPresenter)mPresenter).start();
    }



    @Override
    protected SessionContract.Presenter initPresenter() {
        return new SessionPresenter(this);
    }


    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_active;
    }


    @Override
    public RecyclerView getRecyclerView() {
        return mRecycler;
    }


    @Override
    public void clear() {
        this.mAdapter.clear();
    }


    @Override
    public RecyclerAdapter<Session> getRecyclerAdapter() {
        return mAdapter;
    }

    @Override
    public void onAdapterDataChanged() {
        mPlaceHolderView.triggerOkOrEmpty(mAdapter.getItemCount() > 0);
    }


    /**
     * 自定义, 绑定Session布局的ViewHolder
     */
    class ViewHolder extends RecyclerAdapter.ViewHolder<Session> implements DataSource.Callback<Session> {

        @BindView(R.id.im_portrait)
        PortraitView mPortraitView;     //对方人, 或群的头像

        @BindView(R.id.txt_name)        //名称
        TextView mName;

        @BindView(R.id.txt_content)     //Session的content
        TextView mContent;

        @BindView(R.id.txt_time)        //Session#modifyAt
        TextView mTime;

        @BindView(R.id.red_circ)        //Session#unReadCount
        TextView UnRead;

        @BindView(R.id.permBtn)
        Button applyBtn;                //若是一个apply, 则是通过apply的按钮

        @BindView(R.id.loading)
        Loading loading;

        public ViewHolder(View itemView) {
            super(itemView);
        }


        @Override
        protected void onBind(Session session) {
            mPortraitView.setup(Glide.with(ActiveFragment.this), session.getPicture());
            mName.setText(session.getTitle());

            String str = TextUtils.isEmpty(session.getContent()) ? "" : session.getContent();
            Spannable spannable = new SpannableString(str);
            // 解析表情
            Face.decode(mContent, spannable, (int)mContent.getTextSize());
            // 把内容设置到布局上
            mContent.setText(spannable);

            SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
            String strDate = formatter.format(session.getModifyAt());
            mTime.setText(strDate);

            if(session.getReceiverType() != PushModel.ENTITY_TYPE_APPLY){
                applyBtn.setVisibility(View.GONE);

                final int unReadCount = session.getUnReadCount();
                if (unReadCount > 0) {
                    UnRead.setText(String.valueOf(unReadCount));
                    UnRead.setVisibility(View.VISIBLE);
                } else {
                    UnRead.setVisibility(View.GONE);
                }

            } else {
                //APPLY
                applyBtn.setVisibility(View.VISIBLE);
                UnRead.setVisibility(View.GONE);
                String applyId = session.getApply().getId();

                Apply apply = SQLite.select().from(Apply.class)
                        .where(Apply_Table.id.eq(applyId)).querySingle();
                if(apply.getType() == Apply.applyjoingroup ){
                    applyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loading.setVisibility(View.VISIBLE);
                            loading.start();
                            //applyBtn.setText("");

                            ApplyHelper.passApply(session, ViewHolder.this);
                        }
                    });
                }
            }
        }


        @Override
        public void onDataLoaded(Session session) {
            loading.stop();
            loading.setVisibility(View.GONE);
            applyBtn.setText("已通过");
        }

        @Override
        public void onDataNotAvailable(int strRes) {
            loading.stop();
            loading.setVisibility(View.GONE);
            applyBtn.setText(strRes);
        }
    }






}
