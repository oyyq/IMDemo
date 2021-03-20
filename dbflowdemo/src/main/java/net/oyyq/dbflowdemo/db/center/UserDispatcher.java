package net.oyyq.dbflowdemo.db.center;

import android.text.TextUtils;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * 1. "我"从服务器拉取某人信息, "我" 不一定关注了这个人
 * 2. "我"关注的人发生了信息更改
 */
public class UserDispatcher implements CardCenter<User, UserCard>{

    private static UserDispatcher instance;
    private final Executor executor = Executors.newSingleThreadExecutor();


    public static UserDispatcher instance() {
        if (instance == null) {
            synchronized (UserDispatcher.class) {
                if (instance == null)
                    instance = new UserDispatcher();
            }
        }
        return instance;
    }


    @Override
    public void dispatch(UserCard... cards) {
        if (cards == null || cards.length == 0) return;

        executor.execute(new UserCardHandler(cards));
    }


    private class UserCardHandler implements Runnable{

        private final UserCard[] cards;
        UserCardHandler(UserCard[] cards) {
            this.cards = cards;
        }

        @Override
        public void run() {
            List<User> users = new ArrayList<>();
            for (UserCard card : cards) {
                if (card == null || TextUtils.isEmpty(card.getId())) continue;
                User user= card.build();
                users.add(user);
            }

            DbHelper.save(User.class, users.toArray(new User[0]));
        }
    }

}
