package net.oyyq.dbflowtest.adapter;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import net.oyyq.common.face.Face;
import net.oyyq.common.widget.adapter.RecyclerAdapter;
import net.oyyq.dbflowtest.R;

import java.util.List;

import butterknife.BindView;

public class FaceAdapter extends RecyclerAdapter<Face.Bean> {
    public FaceAdapter(List<Face.Bean> beans, AdapterListener<Face.Bean> listener) {
        super(beans, listener);
    }

    @Override
    protected int getItemViewType(int position, Face.Bean bean) {
        return R.layout.cell_face;
    }

    @Override
    protected ViewHolder<Face.Bean> onCreateViewHolder(View root, int viewType) {
        return new FaceHolder(root);
    }


    class FaceHolder extends ViewHolder<Face.Bean> {

        @BindView(R.id.im_face)
        ImageView mFace;

        public FaceHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(Face.Bean bean) {
            if (bean != null
                    // drawable 资源 id
                    && ((bean.preview instanceof Integer)
                    // face zip 包资源路径
                    || bean.preview instanceof String))
                Glide.with(itemView.getContext())
                        .asBitmap()
                        .load(bean.preview)
                        .format(DecodeFormat.PREFER_ARGB_8888) //设置解码格式8888，保证清晰度
                        .placeholder(R.drawable.default_face)
                        .into(mFace);
        }


    }


}
