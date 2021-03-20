package net.oyyq.dbflowdemo.db.Card;

import net.oyyq.dbflowdemo.db.model.datamodel.Transaction;

/**
 * 事务卡片, 服务器推送或自己造
 */
public class TransactionCard implements Card<Transaction>  {

    private String id;
    private int transactionType;
    private String oneId;
    private String otherId;
    private String groupId;
    private String groupMembers;

    public Transaction build(){
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setTransactionType(transactionType);
        transaction.setOneId(oneId);
        transaction.setOtherId(otherId);
        transaction.setGroupId(groupId);
        transaction.setGroupMembers(groupMembers);
        return transaction;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public String getOneId() {
        return oneId;
    }

    public void setOneId(String oneId) {
        this.oneId = oneId;
    }

    public String getOtherId() {
        return otherId;
    }

    public void setOtherId(String otherId) {
        this.otherId = otherId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        this.groupMembers = groupMembers;
    }
}
