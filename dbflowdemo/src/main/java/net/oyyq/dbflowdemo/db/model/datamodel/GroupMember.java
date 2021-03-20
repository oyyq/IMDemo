package net.oyyq.dbflowdemo.db.model.datamodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import net.oyyq.dbflowdemo.db.model.AppDataBase;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Table(database = AppDataBase.class)
public class GroupMember extends BaseDbModel<GroupMember> {

    // 消息通知级别
    public static final int NOTIFY_LEVEL_INVALID = -1;      // 不接收消息
    public static final int NOTIFY_LEVEL_NONE = 1;           // 正常
    public static final int NOTIFY_LEVEL_CLOSE = 2;         //接收消息不提示

    @PrimaryKey
    private String id;              // 主键
    @Column
    private String alias;           // 别名，备注名. GroupMember表增加 alias, portrait 2个字段的原因:  GroupMember关联的User在本地可能没有记录, 或者记录中只存储了User.id
    @Column
    private String portrait;         //在群里的头像
    @Column
    private boolean isAdmin;          // 是否是管理员
    @Column
    private boolean isOwner;           // 是否是群创建者
    @Column
    private Date modifyAt;              // 更新时间, 更改alias, portrait时间, 或第一次在GroupMember_Table中被存储的时间
    @Column
    private int myNotifyLevel = 0;          //"我"的接收级别,  别人的, 代表"我"不关心, "我"的, +-1

    @ForeignKey(tableClass = Group.class, stubbedRelationship = true, onDelete = ForeignKeyAction.CASCADE)      //onDelete = CASCADE, 当Group被删除时, GroupMember字段也被删除
    private Group group;                    // 对应的群外键

    @Column
    private String userId;              //NON_NULL! 关联的用户Id, 当User记录被删除时, 不要删除GroupMember记录, 因此不要外键关联User_table


    @Override
    public Set<BaseDbModel> getCASCADEUpdatemodels() {
        Set<BaseDbModel> myCasCadeModels = new HashSet<>();
        myCasCadeModels.add(this.group);          //this.group != null
        return myCasCadeModels;
    }


    public int getMyNotifyLevel() {
        return myNotifyLevel;
    }

    public void setMyNotifyLevel(int myNotifyLevel) {
        this.myNotifyLevel = myNotifyLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public Date getModifyAt() {
        return modifyAt;
    }

    public void setModifyAt(Date modifyAt) {
        this.modifyAt = modifyAt;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAlias() { return alias; }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }


    @Override
    public boolean isSame(GroupMember old) {
        return this.equals(old);
    }


    @Override
    public boolean isUiContentSame(GroupMember old) {
        return  (Objects.equals(this.alias, old.alias)
                && Objects.equals(this.portrait, old.portrait))
                && (this.isAdmin && old.isAdmin)
                && (this.isOwner && old.isOwner);       //isAdmin也会影响界面
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMember member = (GroupMember) o;
        return Objects.equals(id, member.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
