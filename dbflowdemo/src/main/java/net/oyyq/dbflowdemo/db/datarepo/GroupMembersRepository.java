package net.oyyq.dbflowdemo.db.datarepo;


import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember_Table;


import java.util.List;

/**
 * 监听"我"所在的某个群的群成员
 * 生命周期: 从第一次在DbHelper中注册, 到退出APP 或"我"主动退出群聊 ("我"被移除群聊不是生命周期的结束, "我"被移除群聊后, 服务器自然不会发给"我"
 * 群员信息的更新了, 不必删除已在GroupMember_Table存储的记录和删除缓存)
 * GroupMemberActivity 群详情页面绑定
 */
public class GroupMembersRepository extends BaseDbRepository<GroupMember> implements GroupMembersDataSource   {
    public static final String repoPrefix = "GroupMembers";
    private String id;
    private String groupId;

    public GroupMembersRepository(String id,  String groupId){
        this.dataClass = GroupMember.class;
        this.id = id;
        this.groupId = groupId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    protected void insert(GroupMember groupMember) {
        dataList.add(groupMember);
    }

    @Override
    protected boolean isRequired(GroupMember groupMember) {
        return groupMember.getGroup().getId().equals(groupId);
    }


    //微信: 点进群详情, 先加载一部分群员, 点击群员详情, 再加载全部群员. 这里为了省事, 直接加载全部群员
    @Override
    public void load(SucceedCallback<List<GroupMember>> callback) {
        super.load(callback);
        if(dataList.size() > 0){
            //dataList缓存的已经是GroupMember_Table的最新记录
            notifyDataChange();
        } else {
            SQLite.select()
                    .from(GroupMember.class).where(GroupMember_Table.group_id.eq(groupId))
                    .limit(-1)//将群员全查出
                    .async()
                    .queryListResultCallback(this)
                    .execute();
        }
    }


}
