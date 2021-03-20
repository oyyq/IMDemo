package net.oyyq.dbflowtest.presenter.contact;


import net.oyyq.common.factory.presenter.BasePresenter;

import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.UserHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.dbflowtest.DemoApplication;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;
import retrofit2.Call;


/**
 * 用户个人信息界面(别人)
 */
public class PersonalPresenter extends BasePresenter<PersonalContract.View> implements PersonalContract.Presenter,
                                DataSource.Callback<UserCard> {

    private User user;
    private Call changefollowCall;


    public PersonalPresenter(PersonalContract.View view) {
        super(view);
    }


    @Override
    public void start() {
        super.start();
        // 个人界面用户数据优先从网络拉取
        Factory.runOnAsync(new Runnable() {
            @Override
            public void run() {
                PersonalContract.View view = getView();
                if (view != null) {
                    String id = view.getUserId();
                    //从网络搜索用户
                    User user = UserHelper.findFromNet(id);
                    onLoaded(user);
                }
            }
        });
    }



    /**
     * 进行界面的设置
     * @param user 用户信息
     */
    private void onLoaded(final User user) {
        this.user = user;
        // 是否就是我自己
        final boolean isSelf = Account.getUserId().equals(user.getId());
        // 是否已经关注
        final boolean isFollow = !isSelf && user.isFollow();
        // 已经关注同时不是自己才能聊天
        final boolean allowSayHello = isFollow ;

        // 切换到Ui线程
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                final PersonalContract.View view = getView();
                if (view == null) return;

                view.onLoadDone(user);
                view.setFollowStatus(isFollow);
                view.allowSayHello(allowSayHello);
            }
        });
    }


    /**
     * 关注或者解关注的回调
     * @param userCard
     */
    @Override
    public void onDataLoaded(UserCard userCard) {

        if(userCard != null && userCard.isFollow()){
            User user = userCard.build();
            //"我"关注了对方成功
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    final PersonalContract.View view = getView();
                    if (view == null) return;
                    //view.onLoadDone(user);            //不需要重新onLoadDone一次, 界面状态保留
                    view.setFollowStatus(!Account.getUserId().equalsIgnoreCase(user.getId()));
                    view.allowSayHello(!Account.getUserId().equalsIgnoreCase(user.getId()));
                    PersonalPresenter.this.changefollowCall = null;
                }
            });
        } else  {
            //"我"对对方解关注成功
            if(!Account.getUserId().equalsIgnoreCase(user.getId())) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        final PersonalContract.View view = getView();
                        if (view == null) return;

                        view.setFollowStatus(false);
                        view.allowSayHello(false);
                        PersonalPresenter.this.changefollowCall = null;
                    }
                });
            }

        }
    }

    @Override
    public void onDataNotAvailable(int strRes) {
        DemoApplication.showToast(strRes);
    }

    @Override
    public User getUserPersonal() {
        return user;
    }


    /**
     * 改变"我"对该人的关注状态, 如果页面就是"我"自己则不应该被唤起
     * @param mIsFollowUser
     * @param userId
     */
    @Override
    public void changeFollowStatus(boolean mIsFollowUser, String userId) {
        Call changeCall = this.changefollowCall;

        if ( changeCall != null && !changeCall.isCanceled()) {
            // 如果有上一次的请求，并且没有取消，
            // 则调用取消请求操作
            changeCall.cancel();
            return;
        }

        if(mIsFollowUser == false){
            this.changefollowCall = follow(userId);
        }else{
            this.changefollowCall = unfollow(userId);
        }

    }

    public Call unfollow(String userId) {
        //解关注
        if(userId.equalsIgnoreCase(user.getId()))
            return UserHelper.unfollow(userId, this);

        return null;
    }


    public Call follow(String userId) {
        if(userId.equalsIgnoreCase(user.getId()))
            return UserHelper.follow(userId, this);
        return null;
    }


}
