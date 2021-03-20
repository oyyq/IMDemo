package net.oyyq.dbflowdemo.db.center;

import android.text.TextUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.TransactionCard;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.helper.TransactionHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.Group_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class TransactionDispatcher implements CardCenter<Transaction, TransactionCard> {

    private static TransactionDispatcher instance;
    private final Executor executor = Executors.newSingleThreadExecutor();


    public static TransactionDispatcher instance() {
        if (instance == null) {
            synchronized (UserDispatcher.class) {
                if (instance == null)
                    instance = new TransactionDispatcher();
            }
        }
        return instance;
    }


    @Override
    public void dispatch(TransactionCard... cards) {
        if (cards == null || cards.length == 0) return;
        executor.execute(new TransactionHandler(cards));
    }


    private class TransactionHandler implements Runnable{
        public final TransactionCard[] cards;
        public TransactionHandler(TransactionCard... cards){
            this.cards = cards;
        }

        @Override
        public void run() {

            List<Transaction> transactions = new ArrayList<>();
            for (TransactionCard card : cards) {

                if (card == null || TextUtils.isEmpty(card.getId())
                        ||  (!Account.getUserId().equalsIgnoreCase(card.getOneId())
                        && !Account.getUserId().equalsIgnoreCase(card.getOtherId()) && card.getGroupId() == null)  )
                    continue;

                if(card.getGroupId() != null){
                    Group group = SQLite.select(Group_Table.id).from(Group.class)
                            .where(Group_Table.id.eq(card.getGroupId())).querySingle();
                    if(group == null) continue;
                }


                Transaction transaction = card.build();
                int transactionType = transaction.getTransactionType();

                switch (transactionType){
                    case Transaction.TYPE_I_LOGOUT:
                        TransactionHelper.logout();
                        break;
                    case Transaction.TYPE_OTHER_UNFOLLOWME:{
                        String oneId = transaction.getOneId();
                        String otherId = transaction.getOtherId();
                        if(oneId.equalsIgnoreCase(Account.getUserId())){
                            TransactionHelper.otherUnfollowMe(otherId);
                        } else if(otherId.equalsIgnoreCase(Account.getUserId())) {
                            TransactionHelper.otherUnfollowMe(oneId);
                        }
                        break;
                    }

                    case Transaction.TYPE_IUNFOLLOWOTHER: {
                        String oneId = transaction.getOneId();
                        String otherId = transaction.getOtherId();
                        if(oneId.equalsIgnoreCase(Account.getUserId())){
                            TransactionHelper.IUnfollowOther(otherId);
                        } else if(otherId.equalsIgnoreCase(Account.getUserId())) {
                            TransactionHelper.IUnfollowOther(oneId);
                        }
                        break;
                    }
                    case Transaction.TYPE_I_OUTGROUP: {
                        String groupId = transaction.getGroupId();
                        TransactionHelper.me_outgroup(groupId);
                        break;
                    }
                    case Transaction.TYPE_I_EXITGROUP: {
                        String groupId = transaction.getGroupId();
                        TransactionHelper.me_exitgroup(groupId);
                        break;
                    }
                    case Transaction.TYPE_I_REMOVEMEMBER:
                    case Transaction.TYPE_OTHER_OUTGROUP: {
                        String groupMembers = transaction.getGroupMembers();
                        String[] ids = groupMembers.split(", ");
                        TransactionHelper.me_remove_gpmember(ids);
                        break;
                    }

                    case Transaction.TYPE_I_ADDADMIN:{
                        String groupMembers = transaction.getGroupMembers();
                        String[] ids = groupMembers.split(", ");
                        TransactionHelper.me_addAdmin(ids);
                        break;
                    }
                    case Transaction.TYPE_I_NEWADMIN:{
                        String groupId= transaction.getGroupId();
                        TransactionHelper.me_newAdmin(groupId);
                        break;
                    }
                    default:
                        break;
                }

                transactions.add(transaction);
            }

            DbHelper.save(Transaction.class, transactions.toArray(new Transaction[0]));

        }
    }



}
