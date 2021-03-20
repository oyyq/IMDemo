package net.oyyq.dbflowdemo.db.model.datamodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.model.AppDataBase;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import static com.raizlabs.android.dbflow.sql.language.Method.count;



/**
 * Group表
 */
@Table(database = AppDataBase.class)
public class Group extends BaseDbModel<Group> {

    @PrimaryKey
    private String id;              // 群Id
    @Column
    private String name;            // 群名称
    @Column
    private String desc;            // 群描述
    @Column
    private String picture;         // 群图片
    @Column
    private int notifyLevel;        // "我"在群中的消息通知级别-对象是我当前登录的账户, 可改
    @Column
    private Date joinAt;            // "我"的加入时间, 若"我"不在群里, joinAt == null
    @Column
    private Date modifyAt;         // 群的相关信息(群管理员, 群头像...)修改的时间, 可改
    @Column
    private int MemberNumber;       //当前记录的群员个数.

    @Column
    private String ownerId;       //当owner这个User记录在User_Table中被删除时, 不要级联删除Group记录, 因此不要外键关联User_Table的记录

    @Column
    private String sessionId;

    private Session session;

    public Object holder;       //预留字段


    List<GroupMember> groupMembers = new ArrayList<>();
    //variableName和List<GroupMember> groupMembers一样
    @OneToMany(methods = {OneToMany.Method.LOAD}, variableName = "groupMembers")
    public List<GroupMember> getGroupMembers(){
        this.groupMembers.clear();
        List<GroupMember> Members = SQLite.select(GroupMember_Table.id, GroupMember_Table.alias,
                GroupMember_Table.portrait, GroupMember_Table.userId, GroupMember_Table.group_id)
                .from(GroupMember.class).where(GroupMember_Table.group_id.eq(id)).queryList();
        this.groupMembers.addAll(Members);
        return this.groupMembers;
    }


    private int MembersCount(){
       return (int) SQLite.select(count(GroupMember_Table.id))
                .from(GroupMember.class)
                .where(GroupMember_Table.group_id.eq(id))
                .count();
    }


    @Override
    public void createCASCADEmodelsIfNeed() {
        if(joinAt != null){
            //"我"在这个群里
            this.session = SQLite.select(Session_Table.id, Session_Table.receiverType, Session_Table.resetAt, Session_Table.group_id)
                            .from(Session.class).where(Session_Table.id.eq(Session.prefix+this.id)).querySingle();

            if(session == null){
                //初次创建一个新的Session, 仅此一次
                Session.Identify identify = new Session.Identify();
                identify.id = Session.prefix+id;
                identify.type = PushModel.RECEIVER_TYPE_GROUP;
                identify.createAt = new Date();
                identify.title = getName();
                identify.picture = getPicture();
                identify.user = null;
                identify.group = this;

                Session session = new Session(identify);
                boolean succeed = session.save();
                this.session = session;
                this.sessionId = Session.prefix+id;
            }

            boolean update = this.update();
        }
    }




  /*
    //当Group换群头像签名时, 同步到Session上去
    @Override
    public Set<BaseDbModel> getCASCADEUpdatemodels() {
        Set<BaseDbModel> myCasCadeModels = new HashSet<>();
        myCasCadeModels.add(this.session);          //this.session != null
        return myCasCadeModels;
    }
*/


    @Override
    public boolean cascadeUpdate() {
        Group group = SQLite.select().from(Group.class).where(Group_Table.id.eq(this.id)).querySingle();

        this.name = group.getName();
        this.desc = group.getDesc();
        this.picture = group.getPicture();
        this.notifyLevel = group.getNotifyLevel();
        this.joinAt = group.getJoinAt();

        this.modifyAt = new Date();
        this.MemberNumber = MembersCount();
        this.ownerId = group.getOwnerId();
        this.session = group.getSession();
        this.holder = buildGroupHolder();

        return this.update();
    }



    public String buildGroupHolder(){

        List<GroupMember> userModels = GroupHelper.getMemberUsers(id, 4);
        if (userModels == null || userModels.size() == 0) return null;

        StringBuilder builder = new StringBuilder();
        for (GroupMember userModel : userModels) {
            builder.append(userModel.getAlias());
            builder.append(", ");
        }

        builder.delete(builder.lastIndexOf(", "), builder.length());
        return builder.toString();
    }


    @Override
    public Set<BaseDbModel> getCASCADEDeletemodels() {
        Set<BaseDbModel> myCasCadeModels = new HashSet<>();

        if(this.session == null){
            this.session = SQLite.select(Session_Table.id).from(Session.class).where(Session_Table.id.eq(Session.prefix+this.id)).querySingle();
        }
         myCasCadeModels.add(this.session);           //须保证: this.session != null
        return myCasCadeModels;
    }



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

    public void setGroupMembers(List<GroupMember> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public int getMemberNumber() { return MemberNumber; }

    public void setMemberNumber(int memberNumber) { MemberNumber = memberNumber; }

    public Session getSession() { return session; }

    public void setSession(Session session) { this.session = session; }


    public Object getHolder() {
        return holder;
    }

    public void setHolder(Object holder) {
        this.holder = holder;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    @Override
    public boolean isSame(Group old) {
        return this.equals(old);
    }

    @Override
    public boolean isUiContentSame(Group old) {
        return  (Objects.equals(this.name, old.name)
                        && Objects.equals(this.desc, old.desc)
                        && Objects.equals(this.picture, old.picture)
                        && Objects.equals(this.holder, old.holder));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
