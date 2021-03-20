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


/**
 * 系统通知, 接受者一定是"我"
 * 系统通知, 不属于单聊或群聊
 */
@Table(database = AppDataBase.class)
public class SysNotify extends BaseDbModel<SysNotify> {

    @PrimaryKey
    private String id;              //SysNotify的id
    @Column
    private String senderId;        //推送者Id, 如"腾讯新闻"有一个senderId, 一个senderId可对应多个SysNotify, 随意一个senderId
    @Column
    private String content;         //SysNotify通知内容
    @Column
    private int PushType = PushModel.ENTITY_TYPE_SYSNOTIFY;
    @Column
    private Date createAt;           //创建时间
    @Column
    private boolean isUnread = true;     //发过来的通知是否未读 ?
    @Column
    private  String picture;
    @Column
    private String name;

    @ForeignKey(tableClass = Session.class, stubbedRelationship = true, onDelete = ForeignKeyAction.CASCADE)
    private Session session;                //聊天消息送达的session


    @Override
    public void createCASCADEmodelsIfNeed() {
        if(senderId != null) {
            this.session = SQLite.select(Session_Table.id)
                .from(Session.class).where(Session_Table.id.eq(Session.prefix+senderId)).querySingle();
        }

        if(this.session == null){
            Session session = new Session(this);
            boolean succeed = session.save();
            this.session = session;
        }

        boolean update = this.update();
    }


    @Override
    public Set<BaseDbModel> getCASCADEUpdatemodels() {
        Set<BaseDbModel> myCasCadeModels = new HashSet<>();
        myCasCadeModels.add(this.session);          //this.session != null
        return myCasCadeModels;
    }



    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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

    public void setPushType(int pushType) {
        PushType = pushType;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getContent() {
        return content;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public User getReceiver() {
        return SQLite.select(User_Table.id).from(User.class).where(User_Table.id.eq(Account.getUserId())).querySingle();
    }

    public int getPushType() {
        return PushType;
    }

    public String getSampleContent() {
        return getContent();
    }

    public Session getSession() {
        return session;
    }


    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean isSame(SysNotify old) {
        return this.equals(old);
    }

    @Override
    public boolean isUiContentSame(SysNotify old) {
        return  Objects.equals(this.content, old.content) && Objects.equals(this.picture, old.picture) ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysNotify notify = (SysNotify) o;
        return Objects.equals(id, notify.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
