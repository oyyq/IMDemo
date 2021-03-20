package net.oyyq.dbflowtest.presenter.group;

import androidx.recyclerview.widget.DiffUtil;

import net.oyyq.common.adapterUtil.DiffUiDataCallback;
import net.oyyq.dbflowdemo.db.datarepo.GroupsDataSource;
import net.oyyq.dbflowdemo.db.datarepo.GroupsRepository;
import net.oyyq.dbflowdemo.db.helper.GroupHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowtest.presenter.BaseSourcePresenter;


import java.util.List;

public class GroupsPresenter extends BaseSourcePresenter<Group, Group,
        GroupsDataSource, GroupsContract.View> implements GroupsContract.Presenter {


    public GroupsPresenter(GroupsContract.View view) {
        super(GroupsRepository.getInstance(),  view);
    }

    @Override
    public void start() {
        super.start();
        //GroupHelper.refreshGroups();
    }


    @Override
    public void onDataLoaded(List<Group> groups) {
        final GroupsContract.View view = getView();
        if (view == null) return;

        for(Group group: groups){
            if(group.getHolder() == null) {
                String holder = group.buildGroupHolder();

                group.setHolder(holder);
            }
        }

        List<Group> old = view.getRecyclerAdapter().getItems();
        DiffUiDataCallback<Group> callback = new DiffUiDataCallback<>(old, groups);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        refreshData(result, groups);
    }
}
