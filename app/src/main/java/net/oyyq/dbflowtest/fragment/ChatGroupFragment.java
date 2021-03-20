package net.oyyq.dbflowtest.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.datarepo.MessageRepository;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Group_Table;

import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowtest.R;
import net.oyyq.dbflowtest.activity.GroupMemberActivity;
import net.oyyq.dbflowtest.activity.MessageActivity;
import net.oyyq.dbflowtest.activity.PersonalActivity;
import net.oyyq.dbflowtest.presenter.message.ChatContract;
import net.oyyq.dbflowtest.presenter.message.ChatGroupPresenter;

import java.util.List;
import butterknife.BindView;



public class ChatGroupFragment extends ChatFragment<Group> implements ChatContract.GroupView {

    @BindView(R.id.im_header)
    ImageView mHeader;

    @BindView(R.id.lay_members)
    LinearLayout mLayMembers;

    @BindView(R.id.txt_member_more)
    TextView mMemberMore;

    private boolean isAdmin;


    public ChatGroupFragment() {
        // Required empty public constructor
    }


    @Override
    protected int getHeaderLayoutId() {
        return  R.layout.lay_chat_header_group;
    }

    @Override
    protected ChatContract.Presenter initPresenter() {
       MessageRepository messageRepository
               = (MessageRepository) DbHelper.getLisenterForId(Message.class, MessageRepository.repoPrefix + groupId);

        ChatGroupPresenter presenter = new ChatGroupPresenter(messageRepository, this, groupId);
        presenter.setInitialUnread(unReadCount);
        return presenter;
    }



    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

        Glide.with(this)
                .load(R.drawable.default_banner_group)
                .centerCrop()
                .into(new ViewTarget<CollapsingToolbarLayout, Drawable>(mCollapsingLayout) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        this.view.setContentScrim(resource.getCurrent());
                    }

                });

    }



    @Override
    public void onInit(Group group) {
        //设置群头像和banner背景图片
        mCollapsingLayout.setTitle(group.getName());
        Glide.with(this)
                .load(group.getPicture())
                .centerCrop()
                .placeholder(R.drawable.default_banner_group)
                .into(mHeader);
    }



    @Override
    public void onStart() {
        super.onStart();
        this.group = SQLite.select(Group_Table.id, Group_Table.name, Group_Table.picture).from(Group.class).where(Group_Table.id.eq(this.groupId)).querySingle();
        onInit(this.group);

        //查询"我"是不是group admin
        GroupMember selfMember = SQLite.select(GroupMember_Table.isAdmin, GroupMember_Table.isOwner).from(GroupMember.class)
                .where(GroupMember_Table.userId.eq(Account.getUserId()), GroupMember_Table.group_id.eq(groupId))
                .querySingle();

        isAdmin = selfMember.isAdmin() || selfMember.isOwner();
        showAdminOption(isAdmin);

        //初始化banner上的群员头像
        List<GroupMember> formembers = GroupHelper.getMemberUsers(groupId, 4);
        final long memberCount = GroupHelper.getGroupMemberCount(groupId);
        long moreCount = memberCount - formembers.size();
        onInitGroupMembers(formembers, moreCount);
    }


    @Override
    public void showAdminOption(boolean isAdmin) {
        if (isAdmin) {
            mToolbar.inflateMenu(R.menu.chat_group);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_add) {
                        //跳转到GroupMemberActivity
                        Intent intent = new Intent(getActivity(), GroupMemberActivity.class);
                        intent.putExtra(GroupMemberActivity.KEY_GROUP_ID, groupId);
                        intent.putExtra(GroupMemberActivity.KEY_GROUP_ADMIN, isAdmin);
                        getActivity().startActivityForResult(intent, MessageActivity.request_code);
                        return true;
                    }
                    return false;
                }
            });
        }

    }




    @Override
    public void onInitGroupMembers(List<GroupMember> members, long moreCount) {
        if (members == null || members.size() == 0) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (final GroupMember member : members) {
            // 添加成员头像
            ImageView p = (ImageView) inflater.inflate(R.layout.lay_chat_group_portrait, mLayMembers, false);
            mLayMembers.addView(p, 0);

            Glide.with(this)
                    .load(member.getPortrait())
                    .placeholder(R.drawable.default_portrait)
                    .centerCrop()
                    .dontAnimate()
                    .into(p);

            p.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转个人信息界面
                    PersonalActivity.show(getContext(), member.getUserId());
                }
            });
        }

        // 更多的按钮
        if (moreCount > 0) {
            mMemberMore.setText(String.format("+%s", moreCount));
            mMemberMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转群成员详情Activity
                    Intent intent = new Intent(getActivity(), GroupMemberActivity.class);
                    intent.putExtra(GroupMemberActivity.KEY_GROUP_ID, groupId);
                    intent.putExtra(GroupMemberActivity.KEY_GROUP_ADMIN, isAdmin);
                    getActivity().startActivityForResult(intent, MessageActivity.request_code);
                }
            });
        } else {
            mMemberMore.setVisibility(View.GONE);
        }

    }



    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        super.onOffsetChanged(appBarLayout, verticalOffset);
        View view = mLayMembers;
        if (view == null) return;

        if (verticalOffset == 0) {
            // 完全展开
            view.setVisibility(View.VISIBLE);
            view.setScaleX(1);
            view.setScaleY(1);
            view.setAlpha(1);
        } else {
            // abs 运算
            verticalOffset = Math.abs(verticalOffset);
            final int totalScrollRange = appBarLayout.getTotalScrollRange();
            if (verticalOffset >= totalScrollRange) {
                // 关闭状态
                view.setVisibility(View.INVISIBLE);
                view.setScaleX(0);
                view.setScaleY(0);
                view.setAlpha(0);
            } else {
                // 中间状态
                float progress = 1 - verticalOffset / (float) totalScrollRange;
                view.setVisibility(View.VISIBLE);
                view.setScaleX(progress);
                view.setScaleY(progress);
                view.setAlpha(progress);
            }
        }

    }





}
