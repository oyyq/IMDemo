package net.oyyq.dbflowdemo.db.model.datamodel;


import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.model.AppDataBase;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;



/**
 * 申请加好友, 或加群
 */
@Table(database = AppDataBase.class)
public class Apply extends BaseDbModel<Apply> {

    public static final int applyfollow = 1;        //"我"申请添加targetId为好友, TODO 暂不实现
    public static final int applyjoingroup = 2;     //"我"向targetId申请加入群


    @PrimaryKey
    private String id;                  //主键

    @Column
    private int type;                   // 申请类型, applyfollow || applyjoingroup

    @Column
    private String applicantId;            //申请发出人, 就是"我"

    @Column
    private String groupId;                //和申请有关的群

    @Column
    private String targetId;            //申请接收人的userId

    @Column
    private Date createAt;              // 创建时间

    @Column
    private boolean passed = false;             //该申请是否被通过? 默认为false

    @Column
    private String sessionId;

    private Session session;                 //Apply关联的Session


    //创建一个关联Apply的Session
    @Override
    public void createCASCADEmodelsIfNeed() {
        if(Account.getUserId().equalsIgnoreCase(targetId)){
            //"我"是被申请者
            if (this.session == null) {
                Session session = new Session(this);
                boolean succeed = session.save();
                this.sessionId = session.getId();
                this.session = session;
            }

            boolean update = this.update();            //给记录的 Session字段赋值
        }
    }


    //拿到关联Apply的Session
    @Override
    public Set<BaseDbModel> getCASCADEUpdatemodels() {

        if(Account.getUserId().equalsIgnoreCase(targetId)){
            Set<BaseDbModel> myCasCadeModels = new HashSet<>();
            if(this.session == null) {
                this.session = SQLite.select().from(Session.class).where(Session_Table.id.eq((Session.prefix + this.id))).querySingle();
            }
            myCasCadeModels.add(this.session);          //this.session != null
            return myCasCadeModels;
        }
        return null;
    }



    /**
     * "我"操作Session通过Apply后, Apply_Table被级联修改
     * @return
     */
    @Override
    public boolean cascadeUpdate() {
        if(Account.getUserId().equalsIgnoreCase(targetId)){
            this.passed = true;
            this.update();
            return false;       //不需要被notifySave
        }
        return false;
    }



    @Override
    public Set<BaseDbModel> getCASCADEDeletemodels() {
        if(Account.getUserId().equalsIgnoreCase(targetId)){
            Set<BaseDbModel> myCasCadeModels = new HashSet<>();
            if(this.session == null) this.session = SQLite.select(Session_Table.id).from(Session.class)
                    .where(Session_Table.id.eq(Session.prefix+this.id)).querySingle();
            myCasCadeModels.add(this.session);          //this.session != null
            return myCasCadeModels;
        }
        return null;
    }




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }





    @Override
    public boolean isSame(Apply old) {
        return this.equals(old);
    }

    @Override
    public boolean isUiContentSame(Apply old) {
        return passed && old.passed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Apply apply = (Apply) o;
        return type == apply.type && Objects.equals(id, apply.id)
                && Objects.equals(this.applicantId, apply.applicantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }


}



