package net.oyyq.dbflowdemo.db.Card;


import net.oyyq.dbflowdemo.db.model.datamodel.User;

import java.util.Date;


/**
 * 1. "我"查询的用户 2. "我"首次关注的用户
 * 3. "我"关注的用户更新了他的信息
 */
public class UserCard implements Card<User> {

    private String id;
    private String name;
    private String phone;           //nullable
    private String portrait;
    private String desc;
    private int sex;
    private Date modifyAt;          //nullable
    private int follows;            //nullable
    private int following;          //nullable
    private boolean isFollow;


    @Override
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



    // 缓存一个对应的User, 不能被GSON框架解析使用ø
    //private transient User user;

    public User build(){
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setPhone(phone);
        user.setPortrait(portrait);
        user.setDesc(desc);
        user.setSex(sex);
        user.setFollows(follows);
        user.setFollowing(following);
        user.setFollow(isFollow);
        //首次关注; 信息更新; 查询陌生人;
        user.setModifyAt(modifyAt == null? new Date() : modifyAt);
        return user;
    }




}
