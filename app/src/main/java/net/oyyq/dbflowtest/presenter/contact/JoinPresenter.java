package net.oyyq.dbflowtest.presenter.contact;

import net.oyyq.common.factory.presenter.BasePresenter;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.helper.ApplyHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;


public class JoinPresenter extends BasePresenter<JoinContract.View>
        implements JoinContract.Presenter, DataSource.Callback<GroupCard> {

    public JoinPresenter(JoinContract.View view) {
        super(view);
    }

    @Override
    public void onDataLoaded(GroupCard groupCard) {
        final JoinContract.View view = getView();
        if (view != null) {
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    view.hideLoading();
                    view.onJoinSucceed();
                }
            });
        }

    }

    @Override
    public void onDataNotAvailable(int strRes) {
        final JoinContract.View view = getView();
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
    public void join(Group group) {
        start();
        ApplyHelper.applyJoinGroup(group, this);
    }


}
