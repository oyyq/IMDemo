package net.oyyq.dbflowdemo.db.model.datamodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.model.AppDataBase;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Table(database = AppDataBase.class)
public class Message extends BaseDbModel<Message> {

    // 消息类型
    public static final int TYPE_STR = 1;
    public static final int TYPE_PIC = 2;
    public static final int TYPE_FILE = 3;
    public static final int TYPE_AUDIO = 4;
    public static final int TYPE_NOTIFY = 5;          //"拍一拍", "**加入群", "你被移除群聊"

    // 消息状态
    public static final int STATUS_DONE = 0;                // 正常状态
    public static final int STATUS_CREATED = 1;             // 创建状态
    public static final int STATUS_FAILED = 2;              // 发送失败状态


    @PrimaryKey
    private String id;//主键
    @Column
    private String content;// 内容
    @Column
    private String attach;  // 附属信息
    @Column
    private int type = TYPE_STR;       // 消息类型, 默认是
    @Column
    private Date createAt;      // 创建时间
    @Column
    private int status;     // 当前消息的状态
    @Column
    private boolean isRepushed = false;        //是否是重新发送的Message

    @Column
    private boolean isUnread = true;        //"我"是否读了这条消息 ?

    @ForeignKey(tableClass = Group.class, stubbedRelationship = true, onDelete = ForeignKeyAction.CASCADE)
    private Group group;            // 接收者群外键

    @Column
    private String receiverId;
    @Column
    private String senderId;

    @ForeignKey(tableClass = Session.class, stubbedRelationship = true, onDelete = ForeignKeyAction.CASCADE)
    private Session session;                //聊天消息送达的session, 当这个Session被删除的时候, 所有关联到该session的消息都被删除


    @Override
    public void createCASCADEmodelsIfNeed() {
        if(group!= null){
            this.session = SQLite.select(Session_Table.id)
                    .from(Session.class).where(Session_Table.id.eq(Session.prefix+this.group.getId())).querySingle();
        } else if( getOther() != null){
            this.session = SQLite.select(Session_Table.id)
                    .from(Session.class).where(Session_Table.id.eq(Session.prefix+getOther().getId())).querySingle();
        }

        if (this.session == null) {
            Session session = new Session(this);
            boolean succeed = session.save();
            this.session = session;
        }

        boolean update = this.update();            //给记录的 Session字段赋值
    }


    @Override
    public Set<BaseDbModel> getCASCADEUpdatemodels() {
        Set<BaseDbModel> myCasCadeModels = new HashSet<>();
        myCasCadeModels.add(this.session);          //this.session != null
        return myCasCadeModels;
    }



    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setGroup(Group group) {
        this.group = group;
    }


    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean isRepushed() {
        return isRepushed;
    }

    public void setRepushed(boolean repushed) {
        isRepushed = repushed;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public String getContent() {
        return content;
    }

    public Date getCreateAt() {
        return createAt;
    }



    public User getSender() {
        return SQLite.select(User_Table.id).from(User.class).where(User_Table.id.eq(senderId)).querySingle();
    }


    public User getReceiver() {
        return SQLite.select(User_Table.id).from(User.class).where(User_Table.id.eq(receiverId)).querySingle();
    }


    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public Group getGroup() {
        return group;
    }

    public int getPushType() {
        return PushModel.ENTITY_TYPE_MESSAGE;
    }



    /**
     * 构建一个简单的消息描述
     * 用于简化消息显示
     *
     * @return 一个消息描述
     */
    public String getSampleContent() {
        if (type == TYPE_PIC)
            return "[图片]";
        else if (type == TYPE_AUDIO)
            return "🎵";
        else if (type == TYPE_FILE)
            return "📃";
        return content;
    }



    /**
     * "我"聊天的对方
     * @return
     */
    public User getOther(){
        if (Account.getUserId().equals(senderId)) {
            return SQLite.select(User_Table.id).from(User.class).where(User_Table.id.eq(receiverId)).querySingle();
        } else {
            return SQLite.select(User_Table.id).from(User.class).where(User_Table.id.eq(senderId)).querySingle();
        }
    }



    @Override
    public boolean isSame(Message old) {
        return Objects.equals(id, old.getId());
    }

    @Override
    public boolean isUiContentSame(Message old) {
        return (status == old.getStatus() && Objects.equals(content, old.getContent()));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return type == message.type && status == message.status && Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, status);
    }
}
