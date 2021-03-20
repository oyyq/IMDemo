package net.oyyq.dbflowdemo.db.center;

import android.text.TextUtils;

import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.ApplyCard;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Apply;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class ApplyDispatcher implements CardCenter<Apply, ApplyCard> {

    private static ApplyDispatcher instance;
    private Executor executor = Executors.newSingleThreadExecutor();

    public static ApplyDispatcher instance() {
        if (instance == null) {
            synchronized (GroupDispatcher.class) {
                if (instance == null)
                    instance = new ApplyDispatcher();
            }
        }
        return instance;
    }


    @Override
    public void dispatch(ApplyCard... cards) {
        if (cards == null || cards.length == 0) return;
        executor.execute(new ApplyHandler(cards));
    }

    private class ApplyHandler implements Runnable {

        private final List<ApplyCard> cards;

        ApplyHandler(ApplyCard[] cards) {
            this.cards = Arrays.asList(cards);
        }

        @Override
        public void run() {

            List<Apply> applys = new ArrayList<>();
            for (ApplyCard card : cards) {
                if (card == null || TextUtils.isEmpty(card.getApplicantId())
                        || TextUtils.isEmpty(card.getTargetId())
                        || !Account.getUserId().equalsIgnoreCase(card.getTargetId())) continue;

                applys.add(card.build());
            }

            DbHelper.save(Apply.class, applys.toArray(new Apply[0]));
        }
    }





}
