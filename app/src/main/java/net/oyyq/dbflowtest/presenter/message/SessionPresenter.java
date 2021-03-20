package net.oyyq.dbflowtest.presenter.message;

import androidx.recyclerview.widget.DiffUtil;
import net.oyyq.common.adapterUtil.DiffUiDataCallback;
import net.oyyq.dbflowdemo.db.datarepo.SessionDataSource;
import net.oyyq.dbflowdemo.db.datarepo.SessionRepository;
import net.oyyq.dbflowdemo.db.model.datamodel.Session;
import net.oyyq.dbflowtest.presenter.BaseSourcePresenter;

import java.util.List;


public class SessionPresenter extends BaseSourcePresenter<Session, Session,
        SessionDataSource, SessionContract.View>
        implements SessionContract.Presenter{


    public SessionPresenter(SessionContract.View view) {
        super(SessionRepository.getInstance(),  view);
    }


    @Override
    public void onDataLoaded(List<Session> sessions) {

        SessionContract.View view = getView();
        if (view == null) return;

        // 差异对比
        List<Session> old = view.getRecyclerAdapter().getItems();
        DiffUiDataCallback<Session> callback = new DiffUiDataCallback<>(old, sessions);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        // 刷新界面
        refreshData(result, sessions);
    }


}
