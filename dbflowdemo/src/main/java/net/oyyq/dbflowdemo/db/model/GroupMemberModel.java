package net.oyyq.dbflowdemo.db.model;

import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;

import java.util.HashSet;
import java.util.Set;

/**
 * 只需要操作的一些列User.id
 */
public class GroupMemberModel {

    private Set<String> userIds = new HashSet<>();
    private Set<String> memberIds = new HashSet<>();

    public GroupMemberModel () {}


    public Set<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<String> userIds) {
        this.userIds = userIds;
    }

    public Set<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(Set<String> memberIds) {
        this.memberIds = memberIds;
    }
}
