package net.oyyq.dbflowdemo.db.model.datamodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.model.AppDataBase;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * User表, 记录: "我", "我"的好友, 陌生人.
 *
 */
@Table(database = AppDataBase.class)
public class User extends BaseDbModel<User> {
    public static final int SEX_MAN = 1;
    public static final int SEX_WOMAN = 2;

    // 主键
    @PrimaryKey
    @Column
    private String id;
    @Column
    private String name;
    @Column
    private String phone;
    @Column
    private String portrait;
    @Column
    private String desc;
    @Column
    private int sex = 0;
    // 我对某人的备注信息
    @Column
    private String alias;
    // 用户关注人的数量
    @Column
    private int follows;
    // 用户粉丝的数量
    @Column
    private int following;
    // 我与当前User的关系状态，是否已经关注了这个人
    @Column
    private boolean isFollow;
    // 时间字段: name / phone / portrait / desc / sex / alias属性改变时要修改
    @Column
    private Date modifyAt;

    @Column
    private String sessionId;

    private Session session;


    @Override
    public void createCASCADEmodelsIfNeed() {
        if(!Account.getUserId().equalsIgnoreCase(id) && isFollow){
            //拿到Session表的最近字段
            this.session = SQLite.select(Session_Table.id, Session_Table.receiverType, Session_Table.resetAt, Session_Table.user_id)
                    .from(Session.class).where(Session_Table.id.eq(Session.prefix+this.id)).querySingle();

            if(this.session == null) {
                Session.Identify identify = new Session.Identify();
                identify.id = Session.prefix + id;
                identify.type = PushModel.RECEIVER_TYPE_USER;
                identify.createAt = new Date();
                identify.title = getName();
                identify.picture = getPortrait();
                identify.user = this;
                identify.group = null;

                Session session = new Session(identify);
                boolean suceed = session.save();
                this.session = session;
                this.sessionId = Session.prefix + id;
            }

            boolean update = this.update();
        }
    }


/*

    @Override
    public Set<BaseDbModel> getCASCADEUpdatemodels() {
        Set<BaseDbModel> myCasCadeModels = new HashSet<>();
        myCasCadeModels.add(this.session);          //this.session != null
        return myCasCadeModels;
    }
*/

    /**
     * User记录被删除 => "我"不再关注这个User, 那么"我"与他的Session也要删除
     * 目的是删除Repository的缓存
     */
    @Override
    public Set<BaseDbModel> getCASCADEDeletemodels() {
        Set<BaseDbModel> myCasCadeModels = new HashSet<>();

        if(this.session == null){
            this.session = SQLite.select(Session_Table.id).from(Session.class).where(Session_Table.id.eq(Session.prefix+this.id)).querySingle();
        }
        myCasCadeModels.add(this.session);          //须保证: this.session != null
        return myCasCadeModels;

    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getFollows() {
        return follows;
    }

    public void setFollows(int follows) {
        this.follows = follows;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public boolean isFollow() {
        return isFollow;
    }

    public void setFollow(boolean follow) {
        isFollow = follow;
    }

    public Date getModifyAt() {
        return modifyAt;
    }

    public void setModifyAt(Date modifyAt) {
        this.modifyAt = modifyAt;
    }


    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean isSame(User old) {
        return this.equals(old);
    }

    @Override
    public boolean isUiContentSame(User old) {
        return (Objects.equals(name, old.name)
                        && Objects.equals(portrait, old.portrait)
                        && Objects.equals(desc, old.desc)
                        && Objects.equals(sex, old.sex)
                        && Objects.equals(isFollow, old.isFollow));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }


    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
