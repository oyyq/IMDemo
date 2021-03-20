package net.oyyq.dbflowtest.presenter.contact;

import net.oyyq.common.factory.presenter.BasePresenter;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.UserHelper;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

public class FollowPresenter extends BasePresenter<FollowContract.View>
        implements FollowContract.Presenter, DataSource.Callback<UserCard> {


    public FollowPresenter(FollowContract.View view) {
        super(view);
    }

    @Override
    public void onDataLoaded(UserCard userCard) {

        final FollowContract.View view = getView();
        if (view != null) {
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    view.hideLoading();
                    view.onFollowSucceed(userCard);
                }
            });
        }
    }

    @Override
    public void onDataNotAvailable(int strRes) {

        final FollowContract.View view = getView();
        if (view != null) {
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    view.showError(strRes);
                }
            });
        }

    }



    @Override
    public void follow(String id) {
        start();   //view.showLoading
        UserHelper.follow(id, this);
    }
}
