package net.oyyq.dbflowdemo.db.model.datamodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.helper.UserHelper;
import net.oyyq.dbflowdemo.db.model.AppDataBase;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Table(database = AppDataBase.class)
public class Session extends BaseDbModel<Session> {
    public static final String prefix = "ses";

    @PrimaryKey
    private String id;          // Id, 是Message /SysNotify中的接收者User的Id或者接收群的"ses"+id;
    @Column
    private String picture;                                     // 图片，接收者用户的头像，或者群的图片
    @Column
    private String title;                                           // 标题，用户的名称，或者群的名称
    @Column
    private String content;                                         // 显示在界面上的简单内容，是Message的一个描述
    @Column
    private int receiverType = PushModel.RECEIVER_TYPE_USER;            // 类型，"对方"是一个人，或者一个群 或者无状态推送
    @Column
    private int unReadCount = 0;                                 // 未读数量，当没有在当前界面时，应当增加未读数量
    @Column
    private boolean  needUpdateUnReadCount = true;             //是否需要更新unReadCount ?
    @Column
    private Date modifyAt = null;                           // 最后更改时间, 对应消息或通知的时间
    @Column
    private Date resetAt = null;                            //上一次, "我"与对方(人 or 群)在聊天界面聊天完毕, 退出聊天界面的时间


    @Column
    private String messageId;                   //最近聊天消息Id

    private Message message;                    // 对应的最近聊天消息

    @Column
    private String notifyId;                 //最近系统消息Id

    private SysNotify notify;                   // 对应的最近系统消息


    @ForeignKey(tableClass = User.class, stubbedRelationship = true, onDelete = ForeignKeyAction.CASCADE)
    private User user;               //聊天对方人, "我"不关注对方了, Session记录从表中删除

    @ForeignKey(tableClass = Group.class, stubbedRelationship = true, onDelete = ForeignKeyAction.CASCADE)
    private Group group;             //聊天群, "我"退出群了, Session记录从表中删除

    @ForeignKey(tableClass = Apply.class, stubbedRelationship = true, onDelete = ForeignKeyAction.CASCADE)
    private Apply apply;               //申请, 当申请被删除, Session也被删除

    @Column
    private boolean applypassed = false;        //"我"是否通过了申请 ? 默认为false


    public Session(){}


    public Session(Identify identify) {
        this.id = identify.id;
        this.receiverType = identify.type;
        this.picture = identify.picture;
        this.modifyAt = identify.createAt;
        this.title = identify.title;
        this.user = identify.user;
        this.group =  identify.group;
    }


    public Session(Message message) {
        if(message.getOther() == null && message.getGroup() == null) return;
        if (message.getGroup() == null) {
            receiverType = PushModel.RECEIVER_TYPE_USER;
            User other = UserHelper.findFromLocal(message.getOther().getId());

            id = Session.prefix+other.getId();
            picture = other.getPortrait();
            title = other.getName();
            this.user = other;
        } else {
            receiverType = PushModel.RECEIVER_TYPE_GROUP;
            Group group = GroupHelper.findFromLocal( message.getGroup().getId() );

            id = Session.prefix+group.getId();
            picture = group.getPicture();
            title = group.getName();
            this.group = group;
        }

        this.message = message;
        this.messageId = message.getId();

        this.notify = null;
        this.content = message.getSampleContent();
        this.modifyAt = message.getCreateAt();
    }



    public Session(Apply apply){
        this.id = Session.prefix+apply.getId();
        this.receiverType = PushModel.RECEIVER_TYPE_APPLY;
        User applicant = UserHelper.search(apply.getApplicantId());

        this.picture =  applicant.getPortrait();
        this.title = applicant.getName();

        if(apply.getGroupId() != null){
            Group group = SQLite.select(Group_Table.id, Group_Table.name).from(Group.class).where(Group_Table.id.eq(apply.getGroupId())).querySingle();
            this.content  = "申请加入群聊"+group.getName();
        } else {
            this.content = "请求添加你为好友";
        }

        this.modifyAt = new Date();
        this.applypassed = false;           //收到一个申请, 初始未通过

        this.apply = apply;
        this.message = null;
        this.notify = null;
    }




    public Session(SysNotify notify){
        if(notify.getSenderId() == null) return;
        receiverType = PushModel.RECEIVER_TYPE_SYSTEM;
        id = Session.prefix + notify.getSenderId();
        picture = notify.getPicture();
        title = notify.getName();

        this.notify = notify;
        this.notifyId = notify.getId();

        this.message = null;
        content = notify.getSampleContent();
        this.modifyAt = notify.getCreateAt();
    }





    @Override
    public boolean cascadeUpdate(){
        ModelAdapter<Session> adapter = FlowManager.getModelAdapter(Session.class);
        Session session = SQLite.select(Session_Table.receiverType, Session_Table.resetAt,
                Session_Table.user_id, Session_Table.group_id)
                .from(Session.class).where(Session_Table.id.eq(this.id)).querySingle();

        this.receiverType =  session.getReceiverType();
        this.resetAt = session.getResetAt();
        String userId = session.getUser()!= null? session.getUser().getId():null;
        String groupId = session.getGroup()!= null? session.getGroup().getId():null;

        if(this.receiverType == PushModel.RECEIVER_TYPE_USER && userId != null) {
            User user = SQLite.select(User_Table.id, User_Table.name, User_Table.portrait)
                    .from(User.class).where(User_Table.id.eq(userId)).querySingle();
            this.user = user;
            this.title = user.getName();
            this.picture = user.getPortrait();

            if(resetAt == null) {           //查全表
                long count = SQLite.selectCountOf().from(Message.class).where(
                        OperatorGroup.clause(Message_Table.senderId.eq(userId), Message_Table.group_id.isNull(),
                                Message_Table.isUnread.is(true))
                ).count();
                this.unReadCount = (int) count;
            } else {                        //查上次
                long count = SQLite.selectCountOf().from(Message.class).where(
                        OperatorGroup.clause(Message_Table.senderId.eq(userId), Message_Table.group_id.isNull(),
                                Message_Table.createAt.greaterThan(resetAt), Message_Table.isUnread.is(true))
                ).count();
                this.unReadCount = (int) count;
            }

            Message message = SQLite.select(Message_Table.id, Message_Table.content, Message_Table.type, Message_Table.createAt)
                    .from(Message.class)
                    .where(Message_Table.senderId.eq(userId), Message_Table.group_id.isNull())
                    .or(Message_Table.receiverId.eq(userId))
                    .orderBy(Message_Table.createAt, false)
                    .querySingle();

            this.message = message;
            this.messageId = message.getId();
            this.modifyAt = message != null? message.getCreateAt(): null;
            this.content = message != null? message.getSampleContent(): null;

            boolean succeed =  adapter.update(this);
            return succeed;

        } else if (this.receiverType == PushModel.RECEIVER_TYPE_GROUP && groupId != null) {
            Group group = SQLite.select(Group_Table.id, Group_Table.name, Group_Table.picture)
                    .from(Group.class).where(Group_Table.id.eq(groupId)).querySingle();
            this.group = group;
            this.title = group.getName();
            this.picture = group.getPicture();

            if(resetAt == null) {           //查全表
                long count = SQLite.selectCountOf().from(Message.class).where(
                        OperatorGroup.clause(Message_Table.group_id.eq(groupId), Message_Table.senderId.notEq(Account.getUserId()),
                                Message_Table.isUnread.is(true))
                ).count();
                this.unReadCount = (int) count;
            } else {                        //查上次
                long count = SQLite.selectCountOf().from(Message.class).where(
                        OperatorGroup.clause(Message_Table.group_id.eq(groupId), Message_Table.senderId.notEq(Account.getUserId()),
                                Message_Table.createAt.greaterThan(resetAt), Message_Table.isUnread.is(true))
                ).count();
                this.unReadCount = (int) count;
            }

            Message message =SQLite.select(Message_Table.id, Message_Table.content, Message_Table.type, Message_Table.createAt)
                    .from(Message.class)
                    .where(Message_Table.group_id.eq(groupId))
                    .orderBy(Message_Table.createAt, false) // 倒序查询
                    .querySingle();

            this.message = message;
            this.messageId = message.getId();
            this.modifyAt = message != null ? message.getCreateAt(): null;
            this.content = message != null ? message.getSampleContent(): null;

            boolean succeed =  adapter.update(this);
            return succeed;

        } else if(receiverType == PushModel.RECEIVER_TYPE_SYSTEM) {
            if(!(userId == null && groupId == null)) return false;

            SysNotify notify = SQLite.select().from(SysNotify.class)
                    .where(SysNotify_Table.senderId.eq(this.id.substring(Session.prefix.length())))
                    .orderBy(SysNotify_Table.createAt, false)
                    .querySingle();             // notify != null

            this.notify = notify;
            this.notifyId = notify.getId();
            this.title = notify.getName();
            this.picture = notify.getPicture();
            this.modifyAt = notify.getCreateAt();
            this.content = notify.getSampleContent();

            if(resetAt == null) {           //查全表
                long count = SQLite.selectCountOf().from(SysNotify.class)
                        .where(OperatorGroup.clause(SysNotify_Table.senderId.eq(this.id.substring(Session.prefix.length())),
                                SysNotify_Table.isUnread.is(true))).count();
                this.unReadCount = (int) count;
            } else {                        //查上次
                long count = SQLite.selectCountOf().from(SysNotify.class)
                        .where(OperatorGroup.clause(SysNotify_Table.senderId.eq(this.id.substring(Session.prefix.length())),
                                SysNotify_Table.createAt.greaterThan(resetAt), SysNotify_Table.isUnread.is(true))
                ).count();
                this.unReadCount = (int) count;
            }

            boolean succeed =  adapter.update(this);
            return succeed;
        } else if (receiverType == PushModel.RECEIVER_TYPE_APPLY){
            //"我"收到了一个Apply, 创建了一个Session将它放入repository
            return true;
        }

        return false;
    }


    /**
     * "我"操作通过一个申请后, 级联更新Apply_Table的记录
     * @return
     */
    @Override
    public Set<BaseDbModel> getCASCADEUpdatemodels() {
        if(this.receiverType == PushModel.RECEIVER_TYPE_APPLY) {
            Set<BaseDbModel> myCasCadeModels = new HashSet<>();
            this.apply = SQLite.select().from(Apply.class).where(Apply_Table.id.eq(this.apply.getId())).querySingle();
            myCasCadeModels.add(this.apply);          //this.apply != null
            return myCasCadeModels;
        }
        return null;
    }


    @Override
    public boolean cascadeDelete() {
        //在User / Group / Apply记录被删除, Session也被级联删除了
        if(this.id != null) {
            boolean delete = this.delete();          //SQLite.delete().from(Session.class).where(Session_Table.id.eq(this.id)).execute();
            return true;
        }

        return false;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(int receiverType) {
        this.receiverType = receiverType;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public boolean isNeedUpdateUnReadCount() {
        return needUpdateUnReadCount;
    }

    public void setNeedUpdateUnReadCount(boolean needUpdateUnReadCount) {
        this.needUpdateUnReadCount = needUpdateUnReadCount;
    }


    public Date getModifyAt() {
        return modifyAt;
    }

    public void setModifyAt(Date modifyAt) {
        this.modifyAt = modifyAt;
    }


    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public SysNotify getNotify() {
        return notify;
    }

    public void setNotify(SysNotify notify) {
        this.notify = notify;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setResetAt(Date resetAt) {
        this.resetAt = resetAt;
    }

    public Date getResetAt() {
        return resetAt;
    }

    public Apply getApply() {
        return apply;
    }

    public void setApply(Apply apply) {
        this.apply = apply;
    }

    public boolean isApplypassed() {
        return applypassed;
    }

    public void setApplypassed(boolean applypassed) {
        this.applypassed = applypassed;
    }


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }


    public String getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(String notifyId) {
        this.notifyId = notifyId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;
        return Objects.equals(id, session.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public boolean isSame(Session old) {
        return this.equals(old);
    }


    @Override
    public boolean isUiContentSame(Session old) {
        //根据receiverType分类讨论不同的情况
        if(receiverType == PushModel.RECEIVER_TYPE_USER ||
                receiverType ==PushModel.RECEIVER_TYPE_GROUP || receiverType == PushModel.RECEIVER_TYPE_SYSTEM){
            return  (Objects.equals(this.title, old.title) && Objects.equals(this.picture, old.picture)
                && Objects.equals(this.content, old.content)
                && Objects.equals(this.modifyAt, old.modifyAt)
                && this.unReadCount == old.unReadCount);

        } else if (receiverType == PushModel.RECEIVER_TYPE_APPLY){
            return this.applypassed && old.applypassed;
        }
        return true;
    }


    public static class Identify {
        public String id;
        public int type;
        public Date createAt;
        public String picture;
        public String title;
        public User user;
        public Group group;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Identify identify = (Identify) o;
            return type == identify.type && Objects.equals(id, identify.id);
        }


        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }



}
