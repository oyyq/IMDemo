package net.oyyq.dbflowtest.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.common.app.Activity;
import net.oyyq.common.app.Fragment;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.Session;
import net.oyyq.dbflowdemo.db.model.datamodel.Session_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.fragment.ChatGroupFragment;
import net.oyyq.dbflowtest.fragment.ChatUserFragment;


/**
 * MessageActivity进入到后台, 再回到前台, 刷新这段间隔的消息
 */
public class MessageActivity extends Activity {
    public static final int request_code = 1024;


    private static final String KEY_RECEIVER_IS_GROUP = "KEY_RECEIVER_IS_GROUP";            // 是否是群
    public static final String KEY_RECEIVER_ID = "KEY_RECEIVER_ID";     // 接收者Id
    public static final String KEY_GROUP_ID = "KEY_GROUP_ID";           // 群Id
    public static final String MUNREADCOUNT = "MUNREADCOUNT";
    public static final String CUNREADCOUNT = "CUNREADCOUNT";


    private String mReceiverId;
    private String mGroupId;
    private boolean mIsGroup;
    //与对方的聊天的未读消息数量
    private int unReadCount;

    /**
     * 通过Session发起聊天
     *
     * @param context 上下文
     * @param session Session
     */
    public static void show(Context context, Session session) {
        if (session == null || context == null || TextUtils.isEmpty(session.getId())) return;

        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KEY_RECEIVER_IS_GROUP, session.getReceiverType() == PushModel.RECEIVER_TYPE_GROUP);

        if(session.getReceiverType() == PushModel.RECEIVER_TYPE_USER){
            intent.putExtra(KEY_RECEIVER_ID, session.getId().substring(Session.prefix.length()));
        } else if (session.getReceiverType() == PushModel.RECEIVER_TYPE_GROUP){
            intent.putExtra(KEY_GROUP_ID, session.getId().substring(Session.prefix.length()));
        }
        intent.putExtra(MessageActivity.MUNREADCOUNT, session.getUnReadCount());

        context.startActivity(intent);
    }

    /**
     * 显示人的聊天界面
     *
     * @param context 上下文
     * @param author  人的信息
     */
    public static void show(Context context, User author) {
        if (author == null || context == null || TextUtils.isEmpty(author.getId())) return;
        Session session = SQLite.select().from(Session.class).where(Session_Table.id.eq(Session.prefix+author.getId())).querySingle();

        Intent intent = new Intent(context,MessageActivity.class);
        intent.putExtra(KEY_RECEIVER_ID, author.getId());
        intent.putExtra(KEY_RECEIVER_IS_GROUP, false);
        intent.putExtra(MessageActivity.MUNREADCOUNT, session == null? 0: session.getUnReadCount());

        context.startActivity(intent);
    }

    /**
     * 发起群聊天
     *
     * @param context 上下文
     * @param group   群的Model
     */
    public static void show(Context context, Group group) {
        if (group == null || context == null || TextUtils.isEmpty(group.getId())) return;
        Session session = SQLite.select().from(Session.class).where(Session_Table.id.eq(Session.prefix+group.getId())).querySingle();

        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KEY_GROUP_ID, group.getId());
        intent.putExtra(KEY_RECEIVER_IS_GROUP, true);
        intent.putExtra(MessageActivity.MUNREADCOUNT, session == null? 0: session.getUnReadCount());
        context.startActivity(intent);
    }



    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_message;
    }


    @Override
    protected boolean initArgs(Bundle bundle) {
        mReceiverId = bundle.getString(KEY_RECEIVER_ID, null);
        mGroupId = bundle.getString(KEY_GROUP_ID, null);
        mIsGroup = bundle.getBoolean(KEY_RECEIVER_IS_GROUP, false);
        unReadCount = bundle.getInt(MessageActivity.MUNREADCOUNT, 0);

        if(mIsGroup) {
            return !TextUtils.isEmpty(mGroupId);
        }else {
            return !TextUtils.isEmpty(mReceiverId);
        }
    }


    @Override
    protected void initWidget() {
        super.initWidget();
        setTitle("");
        Fragment fragment;
        if (mIsGroup)
            fragment = new ChatGroupFragment();
        else
            fragment = new ChatUserFragment();

        // 从Activity传递参数到Fragment中去
        Bundle bundle = new Bundle();
        bundle.putString(KEY_RECEIVER_ID, mReceiverId);
        bundle.putString(KEY_GROUP_ID, mGroupId);
        bundle.putInt(MessageActivity.CUNREADCOUNT, unReadCount);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.lay_container, fragment)           //lay_container: activity_message根节点
                .commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == this.request_code && resultCode == GroupMemberActivity.EXIT_OK){
            MessageActivity.this.finish();
        }
    }

}
