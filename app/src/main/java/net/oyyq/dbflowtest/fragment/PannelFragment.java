package net.oyyq.dbflowtest.fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import net.oyyq.common.app.Fragment;
import net.oyyq.common.face.Face;
import net.oyyq.dbflowtest.DemoApplication;
import net.oyyq.common.tools.AudioRecordHelper;
import net.oyyq.common.tools.UiTool;
import net.oyyq.common.widget.AudioRecordView;
import net.oyyq.common.widget.GalleryView;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.adapter.FaceAdapter;
import net.qiujuer.genius.ui.Ui;
import java.io.File;
import java.util.List;




/**
 * 底部面板
 */
public class PannelFragment extends Fragment {
    //表情面板, 图片面板, 录音面板
    private View mFacePanel, mGalleryPanel, mRecordPanel;
    private PanelCallback mCallback;


    public PannelFragment() { }


    @Override
    protected int getContentLayoutId() {
        return  R.layout.fragment_panel ;
    }


    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

        initFace(root);
        initRecord(root);
        initGallery(root);
    }


    public void setup(PanelCallback callback) {
        mCallback = callback;
    }



    // 初始化表情
    private void initFace(View root) {
        final View facePanel = mFacePanel = root.findViewById(R.id.lay_panel_face);

        View backspace = facePanel.findViewById(R.id.im_backspace);
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除逻辑
                PanelCallback callback = mCallback;
                if (callback == null)
                    return;

                // 模拟一个键盘删除点击
                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL,
                        0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                callback.getInputEditText().dispatchKeyEvent(event);
            }
        });

        TabLayout tabLayout = (TabLayout) facePanel.findViewById(R.id.tab);
        ViewPager viewPager = (ViewPager) facePanel.findViewById(R.id.pager);
        tabLayout.setupWithViewPager(viewPager);

        // 每一表情显示48dp
        final int minFaceSize = (int) Ui.dipToPx(getResources(), 48);
        final int totalScreen = UiTool.getScreenWidth(getActivity());
        final int spanCount = totalScreen / minFaceSize;


        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return Face.all(getContext()).size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }


            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // 添加的
                LayoutInflater inflater = LayoutInflater.from(getContext());
                RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.lay_face_content, container, false);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

                // 设置Adapter
                List<Face.Bean> faces = Face.all(getContext()).get(position).faces;
                FaceAdapter adapter = new FaceAdapter(faces, new RecyclerAdapter.AdapterListenerImpl<Face.Bean>() {
                    @Override
                    public void onItemClick(RecyclerAdapter.ViewHolder holder, Face.Bean bean) {
                        //点击表情后的回调, 回调到界面端(继承PanelCallback)的输入框去
                        if (mCallback == null)
                            return;
                        // 表情添加到输入框
                        EditText inputEditText = mCallback.getInputEditText();
                        int start = Math.max(inputEditText.getSelectionStart(), 0);
                        int end = Math.max(inputEditText.getSelectionEnd(), 0);

                        Face.inputFace(getContext(),  inputEditText.getEditableText(), bean,
                                (int)(inputEditText.getTextSize() + Ui.dipToPx(getResources(), 2)), start, end);

                    }
                });

                recyclerView.setAdapter(adapter);
                // 添加
                container.addView(recyclerView);

                return recyclerView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                // 移除的
                container.removeView((View) object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                // 拿到表情盘的描述
                return Face.all(getContext()).get(position).name;
            }
        });

    }



    // 初始化语音
    private void initRecord(View root) {
        View recordView = mRecordPanel = root.findViewById(R.id.lay_panel_record);

        final AudioRecordView audioRecordView = (AudioRecordView) recordView
                .findViewById(R.id.view_audio_record);


        // 录音的缓存文件
        File tmpFile = DemoApplication.getAudioTmpFile(true);
        // 录音辅助工具类
        final AudioRecordHelper helper = new AudioRecordHelper(tmpFile,
                new AudioRecordHelper.RecordCallback() {
                    @Override
                    public void onRecordStart() {
                        //...
                    }

                    @Override
                    public void onProgress(long time) {
                        //...
                    }

                    @Override
                    public void onRecordDone(File file, long time) {
                        // 时间是毫秒，小于1秒则不发送
                        if (time < 1000) {
                            return;
                        }

                        // 更改为一个发送的录音文件
                        File audioFile = DemoApplication.getAudioTmpFile(false);
                        if (file.renameTo(audioFile)) {
                            // 通知到聊天界面
                            PanelCallback panelCallback = mCallback;
                            if (panelCallback != null) {
                                panelCallback.onRecordDone(audioFile, time);
                            }
                        }
                    }
                });


        // 初始化
        audioRecordView.setup(new AudioRecordView.Callback() {
            @Override
            public void requestStartRecord() {
                // 请求开始
                helper.recordAsync();
            }

            @Override
            public void requestStopRecord(int type) {
                // 请求结束
                switch (type) {
                    case AudioRecordView.END_TYPE_CANCEL:
                    case AudioRecordView.END_TYPE_DELETE:
                        // 删除和取消都代表想要取消
                        helper.stop(true);
                        break;
                    case AudioRecordView.END_TYPE_NONE:
                    case AudioRecordView.END_TYPE_PLAY:
                        // 播放暂时当中就是想要发送
                        helper.stop(false);
                        break;
                }
            }
        });

    }



    // 初始化图片 TODO 没搞清楚
    private void initGallery(View root) {
        final View galleryPanel = mGalleryPanel = root.findViewById(R.id.lay_gallery_panel);
        final GalleryView galleryView = (GalleryView) galleryPanel.findViewById(R.id.view_gallery);
        final TextView selectedSize = (TextView) galleryPanel.findViewById(R.id.txt_gallery_select_count);

        galleryView.setup(getLoaderManager(), new GalleryView.SelectedChangeListener() {
            @Override
            public void onSelectedCountChanged(int count) {
                String resStr = getText(R.string.label_gallery_selected_size).toString();
                selectedSize.setText(String.format(resStr, count));
            }
        });

        // 点击事件
        galleryPanel.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGalleySendClick(galleryView, galleryView.getSelectedPath());
            }
        });

    }


    // 点击的时候触发，传回一个控件和选中的路径
    private void onGalleySendClick(GalleryView galleryView, String[] paths) {
        // 通知给聊天界面
        // 清理状态
        galleryView.clear();

        // 删除逻辑
        PanelCallback callback = mCallback;
        if (callback == null)
            return;

        callback.onSendGallery(paths);
    }



    public void showFace() {
        mFacePanel.setVisibility(View.VISIBLE);
        mRecordPanel.setVisibility(View.GONE);
        mGalleryPanel.setVisibility(View.GONE);
    }

    public void showRecord() {
        mRecordPanel.setVisibility(View.VISIBLE);
        mGalleryPanel.setVisibility(View.GONE);
        mFacePanel.setVisibility(View.GONE);
    }

    public void showGallery() {
        mGalleryPanel.setVisibility(View.VISIBLE);
        mRecordPanel.setVisibility(View.GONE);
        mFacePanel.setVisibility(View.GONE);
    }


    public interface PanelCallback {
        //输入框输入完毕的回调
        EditText getInputEditText();

        // 返回需要发送的图片
        void onSendGallery(String[] paths);

        // 返回录音文件和时长
        void onRecordDone(File file, long time);
    }


}
