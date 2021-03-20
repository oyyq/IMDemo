package net.oyyq.dbflowdemo.db.model.datamodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import net.oyyq.dbflowdemo.db.model.AppDataBase;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;

import java.util.Objects;


/**
 * 服务器给我 / 我给服务器的事务
 * 拿到事务类型, otherId, groupId.
 *  事务相关的一方必须是"我".
 */
@Table(database = AppDataBase.class)
public class Transaction extends BaseDbModel<Transaction> {

    // 事务类型
    public static final int TYPE_I_LOGOUT = -1;              //"我"在这台设备需退出登录
    public static final int TYPE_OTHER_UNFOLLOWME = 1;              //别人取关了"我"
    public static final int TYPE_IUNFOLLOWOTHER = 7;            //"我"取关了别人
    public static final int TYPE_I_OUTGROUP = 2;            //"我"被移除群聊
    public static final int TYPE_I_EXITGROUP = 3;           //"我"退出群聊
    public static final int TYPE_I_REMOVEMEMBER = 4;        //"我"在群聊移除别人
    public static final int TYPE_OTHER_OUTGROUP = 8;        //"我"得到通知, 别人被移除群聊
    public static final int TYPE_I_ADDADMIN  = 5;            //"我"给群添加管理员
    public static final int TYPE_I_NEWADMIN = 6;              //"我"成为了群的新的管理员


    @PrimaryKey
    private String id;                       //主键
    @Column
    private int transactionType;             //事务类型
    @Column
    private String oneId;                   //事务的一方的User.id, OneId or otherId中一个必须是"我".
    @Column
    private String otherId;                 //事务的另一方的User.id
    @Column
    private String groupId;                 //事务关联的群Group.id
    @Column
    private String groupMembers;           //GroupMembers的id


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


    public String getOneId() {
        return oneId;
    }

    public void setOneId(String oneId) {
        this.oneId = oneId;
    }

    public String getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        this.groupMembers = groupMembers;
    }

    @Override
    public boolean isSame(Transaction old) {
        return this.equals(old);
    }

    @Override
    public boolean isUiContentSame(Transaction old) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
