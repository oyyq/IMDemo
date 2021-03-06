package net.oyyq.dbflowdemo.db.model;

import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.model.datamodel.User;

public class AccountRspModel {


    // 用户基本信息
    private UserCard userCard;
    // 当前登录的账号
    private String account;
    // 当前登录成功后获取的Token,
    // 可以通过Token获取用户的所有信息
    private String token;
    // 标示是否已经绑定到了设备PushId
    private boolean isBind;


    public UserCard getUserCard() {
        return userCard;
    }

    public void setUserCard(UserCard userCard) {
        this.userCard = userCard;
    }


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }



}
