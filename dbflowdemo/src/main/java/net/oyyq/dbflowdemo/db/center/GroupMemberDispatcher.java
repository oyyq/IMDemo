package net.oyyq.dbflowdemo.db.center;

import android.os.Build;
import androidx.annotation.RequiresApi;
import net.oyyq.dbflowdemo.db.Card.GroupMemberCard;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.helper.UserHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 1. "我"拉取了我所在的 群, 某个群员的信息(如GroupMemberActivity群详情界面)
 * 2. "我"所在的群的群员发生了信息更改, 如添加新的群员, 群员改名, 改头像, 签名
 */
public class GroupMemberDispatcher implements CardCenter<GroupMember, GroupMemberCard>{

    private static GroupMemberDispatcher instance;
    private Executor executor = Executors.newSingleThreadExecutor();

    public static GroupMemberDispatcher instance() {
        if (instance == null) {
            synchronized (GroupMemberDispatcher.class) {
                if (instance == null) { instance = new GroupMemberDispatcher(); }
            }
        }
        return instance;
    }


    @Override
    public void dispatch(GroupMemberCard... cards) {
        if (cards == null || cards.length == 0) return;

        executor.execute(new MemberRspHandler(Arrays.asList(cards)));
    }



    private class MemberRspHandler implements Runnable {
        private final List<GroupMemberCard> cards;

        MemberRspHandler(List<GroupMemberCard> cards) {
            this.cards = cards;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            final GroupMember[] filteredMembers
                    = cards.stream()
                    .map(memberCard -> {
                        //User user =  UserHelper.search(memberCard.getUserId());
                        Group group = GroupHelper.findFromLocal(memberCard.getGroupId());
                        if( group != null && group.getJoinAt() != null) {
                            return memberCard.build();
                        }
                        return null;
                    })
                    .filter(member -> member != null)
                    .toArray(GroupMember[]::new);

            if (filteredMembers.length > 0) {
                DbHelper.save(GroupMember.class, filteredMembers);
            }

        }
    }



}
