package net.oyyq.dbflowtest.fragment;


import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.common.app.PresenterFragment;

import net.oyyq.common.face.Face;
import net.oyyq.dbflowtest.DemoApplication;
import net.oyyq.common.tools.AudioPlayHelper;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.common.widget.adapter.TextWatcherAdapter;
import net.oyyq.common.widget.layout.PortraitView;
import net.oyyq.common.widget.layoutmanager.smoothscrollLinearLayout;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.db.model.datamodel.User_Table;
import net.oyyq.dbflowtest.file.FileCache;

import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.activity.MessageActivity;
import net.oyyq.dbflowtest.presenter.message.ChatContract;
import net.oyyq.dbflowtest.presenter.message.ChatPresenter;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;
import net.qiujuer.genius.ui.Ui;
import net.qiujuer.genius.ui.compat.UiCompat;
import net.qiujuer.genius.ui.widget.Loading;
import net.qiujuer.widget.airpanel.AirPanel;
import net.qiujuer.widget.airpanel.Util;
import java.io.File;
import java.util.Objects;
import butterknife.BindView;
import butterknife.OnClick;




public abstract class ChatFragment<InitModel> extends PresenterFragment<ChatContract.Presenter>
        implements AppBarLayout.OnOffsetChangedListener,
        ChatContract.View<InitModel>,
        PannelFragment.PanelCallback  {

    protected String mReceiverId;
    protected User receiver;
    protected String groupId;
    protected int unReadCount;
    protected Group group;
    protected Adapter mAdapter;


    //???????????????????????????
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler)
    RecyclerView  mRecyclerView;
    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout mCollapsingLayout;
    @BindView(R.id.edit_content)
    EditText mContent;
    @BindView(R.id.btn_submit)
    View mSubmit;


    // ???????????????????????????????????????Boss??????
    private AirPanel.Boss mPanelBoss;
    private PannelFragment mPanelFragment;
    RecyclerView.LayoutManager mLayoutManager;


    // ???????????????
    private FileCache<AudioHolder> mAudioFileCache = new FileCache<>("audio/cache", "mp3", new FileCache.CacheListener<AudioHolder>() {
        @Override
        public void onDownloadSucceed(final AudioHolder holder, final File file) {
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    // ???????????????, ??????trigger???????????? ???????????? ??? ???????????????holder
                    // ??????????????? -> trigger??????, ???????????? -> ????????????????????????, ??????holder
                    mAudioPlayer.trigger(holder, file.getAbsolutePath());
                }
            });
        }

        @Override
        public void onDownloadFailed(AudioHolder holder) {
            DemoApplication.showToast(R.string.toast_download_error);
        }
    });


    private AudioPlayHelper<AudioHolder> mAudioPlayer = new AudioPlayHelper<>(new AudioPlayHelper.RecordPlayListener<AudioHolder>() {
        @Override
        public void onPlayStart(AudioHolder audioHolder) {
            audioHolder.onPlayStart();
        }

        @Override
        public void onPlayStop(AudioHolder audioHolder) {
            audioHolder.onPlayStop();
        }

        @Override
        public void onPlayError(AudioHolder audioHolder) {
            DemoApplication.showToast(R.string.toast_audio_play_error);
        }
    });



    @Override
    protected void initArgs(Bundle bundle) {
        super.initArgs(bundle);
        mReceiverId = bundle.getString(MessageActivity.KEY_RECEIVER_ID);
        groupId = bundle.getString(MessageActivity.KEY_GROUP_ID);
        unReadCount = bundle.getInt(MessageActivity.CUNREADCOUNT);
    }


    @Override
    protected final int getContentLayoutId() {
        return R.layout.fragment_chat_common;
    }

    @LayoutRes
    protected abstract int getHeaderLayoutId();


    @Override
    protected void initData() {
        super.initData();
        mPresenter.start();
    }



    @Override
    protected void initWidget(View root) {
        // ??????????????????
        // ???????????????????????????????????????super??????
        // ????????????????????????
        ViewStub stub = (ViewStub) root.findViewById(R.id.view_stub_header);
        stub.setLayoutResource(getHeaderLayoutId());
        stub.inflate();

        // ??????????????????????????????
        super.initWidget(root);

        // ?????????????????????
        mPanelBoss = (AirPanel.Boss) root.findViewById(R.id.lay_content);
        mPanelBoss.setup(new AirPanel.PanelListener() {
            @Override
            public void requestHideSoftKeyboard() {
                // ?????????????????????
                Util.hideKeyboard(mContent);
            }
        });

        mPanelBoss.setOnStateChangedListener(new AirPanel.OnStateChangedListener() {
            @Override
            public void onPanelStateChanged(boolean isOpen) {
                // ????????????
                if (isOpen) {
                    onBottomPanelOpened();
                }
            }

            @Override
            public void onSoftKeyboardStateChanged(boolean isOpen) {
                // ???????????????
                if (isOpen) {
                    onBottomPanelOpened();
                }
            }
        });

        mPanelFragment = (PannelFragment) getChildFragmentManager().findFragmentById(R.id.frag_panel);
        mPanelFragment.setup(this);

        initToolbar();
        initAppbar();
        initEditContent();


        //?????????????????????LayoutManager
        mLayoutManager = new smoothscrollLinearLayout(getContext());
        ((LinearLayoutManager)mLayoutManager).setOrientation(LinearLayoutManager.VERTICAL);
        ((LinearLayoutManager)mLayoutManager).setStackFromEnd(false);         //?????????????????????????????????(?????????Adapter?????????????????????)
        mRecyclerView.setLayoutManager(mLayoutManager);



        mAdapter = new Adapter();
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setListener(new RecyclerAdapter.AdapterListenerImpl<Message>() {
            @Override
            public void onItemClick(RecyclerAdapter.ViewHolder holder, Message message) {
                if( message.getType() == Message.TYPE_AUDIO && holder instanceof ChatFragment.AudioHolder ) {
                    //?????? ??????, ??????????????????????????????, ?????????????????????, ??????????????????, ????????????
                    mAudioFileCache.download((AudioHolder) holder, message.getContent());          //????????????, oss?????? || ??????????????????
                    //??????????????????pushed.content????????????????????????
                }
            }
        });

    }


    // ?????????????????????????????????????????????
    private void onBottomPanelOpened() {
        CollapseAppBar();
        //??????????????????
        if(mAdapter.getItemCount() > 0) {
            smoothScrollToAdapterPosition(mAdapter.getItemCount() - 1);
        }
    }



    protected void initToolbar(){
        Toolbar toolbar = mToolbar;
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

    }

    protected void initAppbar(){
        mAppBarLayout.addOnOffsetChangedListener(this);
    }



    protected void initEditContent(){
        mContent.addTextChangedListener(new TextWatcherAdapter(mContent) {
            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);

                String content = editable.toString().trim();
                boolean needSendMsg = !TextUtils.isEmpty(content);
                // ??????????????????????????????Icon
                mSubmit.setActivated(needSendMsg);
            }
        });

    }



    /**
     * AppBarLayout?????????????????????
     */
    private State mCurrentState = State.IDLE;
    public enum State {
        EXPANDED,           //??????
        COLLAPSED,          //??????
        IDLE                //?????????????????????
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (verticalOffset == 0) {
            if (mCurrentState != State.EXPANDED) {
                // onExpanded
            }
            mCurrentState = State.EXPANDED;
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                //onCollapsed
            }
            mCurrentState = State.COLLAPSED;
        } else {
            if (mCurrentState != State.IDLE) {
                //onIdle
            }
            mCurrentState = State.IDLE;
        }

    }



    //?????????????????????AppbarLayout
    void  CollapseAppBar(){
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                if (mAppBarLayout != null) mAppBarLayout.setExpanded(false, true);
            }
        });
    }



    @OnClick(R.id.btn_face)
    void onFaceClick() {
        // ??????????????????????????????
        mPanelBoss.openPanel();
        mPanelFragment.showFace();
    }

    @OnClick(R.id.btn_record)
    void onRecordClick() {
        mPanelBoss.openPanel();
        mPanelFragment.showRecord();
    }

    private void onMoreClick() {
        mPanelBoss.openPanel();
        mPanelFragment.showGallery();
    }


    @OnClick(R.id.btn_submit)
    void onSubmitClick() {
        if (mSubmit.isActivated()) {
            // ??????
            String content = mContent.getText().toString();
            mContent.setText("");
            mPresenter.pushText(content);
        } else {
            onMoreClick();
        }
    }






    private boolean isFirstResume = true;
    @Override
    public void onResume() {
        super.onResume();

        //????????????????????????
        if(isFirstResume) {
            //???AppbarLayout??????
            CollapseAppBar();
            //?????????????????????
            ChatPresenter chatPresenter = (ChatPresenter)mPresenter;
            int Initunread = chatPresenter.getInitialUnread();

            if (Initunread > 0) {
                int position = mAdapter.getItemCount() - Initunread;            //?????????adapter????????????
                smoothScrollToAdapterPosition(position);
            }
            isFirstResume =  false;
        }
    }



    @Override
    public void clear() {
        this.mAdapter.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAudioPlayer.destroy();
    }


    @Override
    public RecyclerAdapter<Message> getRecyclerAdapter() {
        return mAdapter;
    }

    @Override
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void onAdapterDataChanged() {
        smoothScrollToAdapterPosition(mAdapter.getItemCount()-1);
    }


    @Override
    public EditText getInputEditText() {
        // ???????????????
        return mContent;
    }

    @Override
    public void onSendGallery(String[] paths) {
        // ??????????????????
        mPresenter.pushImages(paths);
    }


    /**
     * ????????????????????????
     * @param file
     * @param time
     */
    @Override
    public void onRecordDone(File file, long time) {
        // ??????????????????
        mPresenter.pushAudio(file.getAbsolutePath(), time);
    }



    @Override
    public void smoothScrollToAdapterPosition(int adapterPosition) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(adapterPosition);
            }
        });
    }


    @Override
    public boolean onBackPressed() {
        if (mPanelBoss.isOpen()) {
            // ????????????????????????true??????????????????????????? "??????", ???????????????MessageActivity????????????????????????
            mPanelBoss.closePanel();
            return true;
        }
        return super.onBackPressed();
    }




    private class Adapter extends RecyclerAdapter<Message> {

        @Override
        protected int getItemViewType(int position, Message message) {

            //????????????"??????"
            boolean isRight = Objects.equals(message.getSender().getId(), Account.getUserId());
            switch ( message.getType()) {
                //??????
                case Message.TYPE_STR:
                    return isRight ? R.layout.cell_chat_text_right : R.layout.cell_chat_text_left;
                //??????
                case Message.TYPE_AUDIO:
                    return isRight ? R.layout.cell_chat_audio_right : R.layout.cell_chat_audio_left;
                //??????
                case Message.TYPE_PIC:
                    return isRight ? R.layout.cell_chat_pic_right : R.layout.cell_chat_pic_left;
                //"???"????????????????????????????????????
               case Message.TYPE_NOTIFY:
                    return R.layout.sysnotify;
                //??????????????????
                default:
                    return isRight ? R.layout.cell_chat_text_right : R.layout.cell_chat_text_left;
            }
        }

        @Override
        protected ViewHolder<Message> onCreateViewHolder(View root, int viewType) {
            switch (viewType){
                //?????????????????????TextHolder, ???cell_chat..xml??????????????????FrameLayout,
                case R.layout.cell_chat_text_right:
                case R.layout.cell_chat_text_left:
                    return new TextHolder(root);

                case R.layout.cell_chat_audio_right:
                case R.layout.cell_chat_audio_left:
                    return new AudioHolder(root);

                case R.layout.cell_chat_pic_right:
                case R.layout.cell_chat_pic_left:
                    return new PicHolder(root);

                case R.layout.sysnotify:
                    return new NotifyHolder(root);
                default:
                    return new TextHolder(null);
            }

        }
    }




    // Holder?????????, ????????????private --> private????????????Butterknife??????
    class BaseHolder extends RecyclerAdapter.ViewHolder<Message> {
        @BindView(R.id.im_portrait)
        PortraitView mPortrait;

        // ???????????????????????????????????????
        @Nullable
        @BindView(R.id.loading)
        Loading mLoading;

        public BaseHolder(View itemView) {
            super(itemView);
        }


        @Override
        protected void onBind(Message message) {
            String senderId =  message.getSenderId();
            User sender = SQLite.select(User_Table.portrait).from(User.class).where(User_Table.id.eq(senderId)).querySingle();

            mPortrait.setup(Glide.with(ChatFragment.this), sender.getPortrait());

            // ?????????????????????
            if (mLoading != null) {
                //??????message????????????????????????????????????Loading
                int status = message.getStatus();
                if (status == Message.STATUS_DONE) {
                    // ????????????, ??????Loading
                    mLoading.stop();
                    mLoading.setVisibility(View.GONE);
                } else if (status == Message.STATUS_CREATED) {
                    // ????????????????????????
                    mLoading.setVisibility(View.VISIBLE);
                    mLoading.setProgress(0);
                    mLoading.setForegroundColor(UiCompat.getColor(getResources(), R.color.colorAccent));
                    mLoading.start();
                } else if (status == Message.STATUS_FAILED) {
                    // ??????????????????, ??????????????????
                    mLoading.setVisibility(View.VISIBLE);
                    mLoading.stop();
                    mLoading.setProgress(1);
                    mLoading.setForegroundColor(UiCompat.getColor(getResources(), R.color.alertImportant));
                }

                // ??????????????????????????????????????????
                mPortrait.setEnabled(status == Message.STATUS_FAILED);

            }
        }


        @OnClick(R.id.im_portrait)
        void onRePushClick() {
            // ???????????? ??????mData.STATUS == Message.STATUS_FAILED
            if (mLoading != null && mPresenter.rePush(mData)) {
                // ????????????????????????????????????????????????
                // ???????????????????????????????????????????????????
                updateData(mData);
            }
        }

    }




        // ?????????Holder
    class TextHolder extends BaseHolder {
        @BindView(R.id.txt_content)
        TextView mContent;
        public TextHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(Message message) {
            super.onBind(message);

            Spannable spannable = new SpannableString(message.getContent());
            // ????????????
            Face.decode(mContent, spannable, (int) Ui.dipToPx(getResources(), 20));
            // ???????????????????????????
            mContent.setText(spannable);
        }
    }




    // ?????????Holder
    class AudioHolder extends BaseHolder {
        @BindView(R.id.txt_content)
        TextView mContent;
        @BindView(R.id.im_audio_track)
        ImageView mAudioTrack;

        public AudioHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(Message message) {
            super.onBind(message);
            //?????????????????? long 30000
            String attach = TextUtils.isEmpty(message.getAttach()) ? "0" : message.getAttach();
            mContent.setText(formatTime(attach));
        }

        // ???????????????
        void onPlayStart() {
            // ??????
            mAudioTrack.setVisibility(View.VISIBLE);
        }

        // ???????????????
        void onPlayStop() {
            // ???????????????
            mAudioTrack.setVisibility(View.INVISIBLE);
        }

        private String formatTime(String attach) {
            float time;
            try {
                // ??????????????????
                time = Float.parseFloat(attach) / 1000f;
            } catch (Exception e) {
                time = 0;
            }
            // 12000/1000f = 12.0000000
            // ????????????????????? 1.234 -> 1.2 1.02 -> 1.0
            String shortTime = String.valueOf(Math.round(time * 10f) / 10f);
            // 1.0 -> 1     1.2000 -> 1.2
            shortTime = shortTime.replaceAll("[.]0+?$|0+?$", "");
            return String.format("%s???", shortTime);
        }
    }




    // ?????????Holder
    class PicHolder extends BaseHolder {
        @BindView(R.id.im_image)
        ImageView mContent;


        public PicHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(Message message) {
            super.onBind(message);
            // ??????????????????????????????Content????????????????????????
            String content = message.getContent();

            Glide.with(ChatFragment.this)
                    .load(content)
                    .fitCenter()
                    .into(mContent);

        }
    }



    class NotifyHolder extends RecyclerAdapter.ViewHolder<Message>{

        @BindView(R.id.txt_notify)
        TextView mNotify;

        public NotifyHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(Message message) {
            mNotify.setText(message.getContent());
        }

    }




}
