package net.oyyq.dbflowdemo.db.Card;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.Group_Table;

import java.util.Date;

/**
 * "我"所在的群的群员 (在群里的, 被移除的除外)
 *  该群员未必是"我"好友, 但我需要直到其userId
 */
public class GroupMemberCard implements Card<GroupMember> {

    private String id;
    private String alias;
    private String portrait;
    private boolean isAdmin;
    private boolean isOwner;
    private Date modifyAt;              //nullable
    private int notifyLevel;            //0 or +-1
    private String groupId;             //NON-NULL!
    private String userId;              //NON-NULL !


    public GroupMember build() {
        GroupMember member = new GroupMember();
        member.setId(id);
        member.setAlias(alias);
        member.setPortrait(portrait);
        member.setAdmin(isAdmin);
        member.setOwner(isOwner);
        member.setModifyAt(modifyAt == null ? new Date() : modifyAt);
        member.setMyNotifyLevel(notifyLevel);

        Group group = SQLite.select(Group_Table.id).from(Group.class).where(Group_Table.id.eq(groupId)).querySingle();
        member.setGroup(group);
        member.setUserId(userId);
        return member;
    }



    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
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

    public int getNotifyLevel() {
        return notifyLevel;
    }

    public void setNotifyLevel(int notifyLevel) {
        this.notifyLevel = notifyLevel;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
