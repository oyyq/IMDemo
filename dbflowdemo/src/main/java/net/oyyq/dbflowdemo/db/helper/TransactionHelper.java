package net.oyyq.dbflowdemo.db.helper;


import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.datarepo.ContactRepository;
import net.oyyq.dbflowdemo.db.datarepo.GroupMembersRepository;
import net.oyyq.dbflowdemo.db.datarepo.GroupsRepository;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Group_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.db.model.datamodel.User_Table;
import java.util.ArrayList;
import java.util.List;



public class TransactionHelper {

    //"我"退出登陆此台设备
    public static void logout(){
        //TODO
    }


    //对方取关了"我"
    public static void otherUnfollowMe(String otherId){
        //1.  "我"在客户端取关别人: 将对方User记录, Session记录, Message记录都删除;
        //    ===> ContactRepository, SessionRepository的缓存删去, MessagRepository解注册.
        //2. 别人取关了"我", "我"接到服务器的TransactionCard: 将对方User记录的isFollow改成false
        //   将ContactRepository的缓存删去, MessageRepository不能接收对方的消息(服务器不会转发给"我"), "我"可以发status==FAILED的Message到本地(不到服务器)
        User other = SQLite.select().from(User.class).where(User_Table.id.eq(otherId)).querySingle();
        if(other == null) return;
        other.setFollow(false);
        other.update();

        //"我"的关注数量和被关注数量-1
        User self = SQLite.select().from(User.class).where(User_Table.id.eq(Account.getUserId())).querySingle();
        self.setFollowing(self.getFollowing()-1);
        self.setFollows(self.getFollows()-1);
        self.update();

        ContactRepository contactRepo = (ContactRepository) DbHelper.getLisenterForId(User.class, ContactRepository.repoPrefix+ContactRepository.id);
        contactRepo.onDataDelete(true, other);
    }



    //"我"取关了别人
    public static void IUnfollowOther(String otherId){
        User other = SQLite.select().from(User.class).where(User_Table.id.eq(otherId)).querySingle();
        if(other == null) return;

        //"我"的关注数量和被关注数量-1
        User self = SQLite.select().from(User.class).where(User_Table.id.eq(Account.getUserId())).querySingle();
        self.setFollowing(self.getFollowing()-1);
        self.setFollows(self.getFollows()-1);
        self.update();

        //删除对方的记录
        DbHelper.delete(User.class, other);
    }




    //"我"退出群聊
    public static void me_exitgroup(String groupId){
        //将Group记录, 关联GroupMember记录, Session记录, Message记录都删除
        //GroupsRepository, SessionRepository的缓存删去, GroupMemberRepository, MessageRepository解注册
        Group group = SQLite.select().from(Group.class).where(Group_Table.id.eq(groupId)).querySingle();
        if(group == null || group.getJoinAt() == null) return;           //"我"已退群
        DbHelper.delete(Group.class, group);
    }




    //"我"被移除群聊
    public static void me_outgroup(String groupId){
        //将Group记录joinAt改为null, GroupsRepository中的缓存删去,
        // MessageRepository不能接收到消息(服务器不会转发给 "我"), "我"可以发status==FAILED的Message到本地(不到服务器)
        Group group = SQLite.select().from(Group.class).where(Group_Table.id.eq(groupId)).querySingle();
        if(group == null || group.getJoinAt() == null) return;

        group.setJoinAt(null);
        group.update();

        GroupsRepository groupsRepository = (GroupsRepository) DbHelper.getLisenterForId(Group.class, GroupsRepository.repoPrefix+GroupsRepository.id);
        groupsRepository.onDataDelete(true,  group);
    }



    //"我"移除了某些群员, 这些群员在同一个群; 服务器通知"我"某些群员退出了群
    public static void me_remove_gpmember(String... memberIds){

        //将GroupMember记录删除, GroupMemberRepository的缓存删除
        List<GroupMember> deleteMembers = new ArrayList<>();
        Group group = null;
        for(String memberId : memberIds){
            GroupMember member = SQLite.select(GroupMember_Table.id, GroupMember_Table.group_id).from(GroupMember.class).where(GroupMember_Table.id.eq(memberId)).querySingle();
            if(member == null) continue;
            if(group == null) group = SQLite.select(Group_Table.id).from(Group.class).where(Group_Table.id.eq(member.getGroup().getId())).querySingle();
            deleteMembers.add(member);
        }


        DbHelper.delete(GroupMember.class, deleteMembers.toArray(new GroupMember[0]));
        group.cascadeUpdate();          //更新Group表

        GroupsRepository groupsRepository = (GroupsRepository) DbHelper.getLisenterForId(Group.class, GroupsRepository.repoPrefix+GroupsRepository.id);
        groupsRepository.onDataSave(true, group);
    }




    //"我"添加了一些人作管理员
    public static void me_addAdmin(String... memberIds){
        String groupId = null;
        //修改GroupMember表的isAdmin
        List<GroupMember> adminMembers = new ArrayList<>();

        for(String memberId : memberIds){
            GroupMember member = SQLite.select().from(GroupMember.class).where(GroupMember_Table.id.eq(memberId)).querySingle();
            if(member == null || member.isAdmin()) continue;
            if(groupId == null) groupId = member.getGroup().getId();
            member.setAdmin(true);
            member.update();
            adminMembers.add(member);
        }

        String gpmemberRepoId = GroupMembersRepository.repoPrefix + groupId;
        GroupMembersRepository gpMemberRepo =
                (GroupMembersRepository) DbHelper.getLisenterForId(GroupMember.class, gpmemberRepoId);
        gpMemberRepo.onDataSave(true, adminMembers.toArray(new GroupMember[0]));

    }



    //"我"被添加为管理员 TODO 用不到这个事务
    public static void me_newAdmin(String groupId){
        //修改GroupMember表的isAdmin
        GroupMember Me = SQLite.select()
                .from(GroupMember.class).where(GroupMember_Table.id.eq(Account.getUserId()), GroupMember_Table.group_id.eq(groupId)).querySingle();
        if(Me == null || Me.isAdmin()) return;
        Me.setAdmin(true);
        Me.update();

        String gpmemberRepoId = GroupMembersRepository.repoPrefix + Me.getGroup().getId();
        GroupMembersRepository gpMemberRepo  = (GroupMembersRepository) DbHelper.getLisenterForId(GroupMember.class, gpmemberRepoId);
        gpMemberRepo.onDataSave(false, Me);
    }



}
