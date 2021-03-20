package net.oyyq.dbflowdemo.db.Card;

import android.annotation.SuppressLint;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.User;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 1. "我"新建的群,  2."我"加入的群
 * 3. "我"查询的群
 */
public class GroupCard implements Card<Group> {

    private String id;
    private String name;
    private String desc;          //nullable
    private String picture;
    private int notifyLevel;     //该群对我的通知级别, 有可能 = 0(我查询的我不在里面的群)
    private Date joinAt;         //nullable
    private Date modifyAt;       //nullable
    private String ownerId;      //nullable
    private String holder;      //nullable 群的额外信息



    @SuppressLint("NewApi")
    public Group build() {
        Group group = new Group();
        group.setId(id);
        group.setName(name);
        group.setDesc(desc);
        group.setPicture(picture);
        group.setNotifyLevel(notifyLevel);
        group.setJoinAt(joinAt);
        group.setModifyAt(modifyAt == null ? new Date() : modifyAt);
        group.setOwnerId(ownerId);
        if(holder == null || Objects.equals(holder, "")){
            holder = "";
            List<GroupMember> groupMembers = SQLite.select(GroupMember_Table.alias).from(GroupMember.class).where(GroupMember_Table.group_id.eq(id)).queryList();
            if(groupMembers != null && groupMembers.size() > 0) {
                groupMembers.forEach(new Consumer<GroupMember>() {
                    @Override
                    public void accept(GroupMember groupMember) {
                        holder += (groupMember.getAlias() + ", ");
                    }
                });
            }
            if(holder.contains(",")) holder = holder.substring(0, holder.lastIndexOf(","));
        }
        group.setHolder(holder);

        return group;
    }

    @Override
    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getNotifyLevel() {
        return notifyLevel;
    }

    public void setNotifyLevel(int notifyLevel) {
        this.notifyLevel = notifyLevel;
    }

    public Date getJoinAt() {
        return joinAt;
    }

    public void setJoinAt(Date joinAt) {
        this.joinAt = joinAt;
    }

    public Date getModifyAt() {
        return modifyAt;
    }

    public void setModifyAt(Date modifyAt) {
        this.modifyAt = modifyAt;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }


    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }
}
