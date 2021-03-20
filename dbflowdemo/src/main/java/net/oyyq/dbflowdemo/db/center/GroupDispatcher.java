package net.oyyq.dbflowdemo.db.center;


import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.RequiresApi;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;



/**
 * 1. "我"拉取了某个群的信息, "我"是否加入到这个群中 ? => 不一定
 * 2. "我"所在的群发生了信息更改
 */
public class GroupDispatcher implements CardCenter<Group, GroupCard> {

    private static GroupDispatcher instance;
    private Executor executor = Executors.newSingleThreadExecutor();

    public static GroupDispatcher instance() {
        if (instance == null) {
            synchronized (GroupDispatcher.class) {
                if (instance == null)
                    instance = new GroupDispatcher();
            }
        }
        return instance;
    }


    @Override
    public void dispatch(GroupCard... cards) {
        if (cards == null || cards.length == 0) return;
        executor.execute(new GroupHandler(cards));
    }


    private class GroupHandler implements Runnable {
        private final List<GroupCard> cards;

        GroupHandler(GroupCard[] cards) {
            this.cards = Arrays.asList(cards);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {

            List<Group> groups = new ArrayList<>();
            for (GroupCard card : cards) {
                if (card == null || TextUtils.isEmpty(card.getId())
                        || TextUtils.isEmpty(card.getOwnerId()) ) continue;

                groups.add(card.build());
            }

            DbHelper.save(Group.class, groups.toArray(new Group[0]));
        }
    }



}
