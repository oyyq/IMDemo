package net.oyyq.dbflowdemo.db.datarepo;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.Group_Table;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

/**
 * 监听"我"加入的所有群
 */
public class GroupsRepository extends BaseDbRepository<Group> implements GroupsDataSource {
    public static final GroupsRepository instance = new GroupsRepository();
    public static final String repoPrefix = "Groups";
    public static final String id = UUID.randomUUID().toString();

    public static GroupsRepository getInstance() {
        return instance;
    }

    @Override
    public String getId() {
        return repoPrefix+id;
    }


    public GroupsRepository(){
        Type type = ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        dataClass = (Class<Group>) type;
    }


    @Override
    public void load(SucceedCallback<List<Group>> callback) {
        super.load(callback);

        if(dataList.size() == 0){
            //首次加载
            SQLite.select()
                    .from(Group.class)
                    .orderBy(Group_Table.name, true)
                    .limit(100)
                    .async()
                    .queryListResultCallback(this)
                    .execute();
        } else {
            //SessionRepository的缓存和Session_Table的记录一致, 直接将dataLis缓存通知到Presenter
            notifyDataChange();
        }

    }


    @Override
    protected void insert(Group group) {
        dataList.add(group);
    }


    @Override
    protected boolean isRequired(Group group) {
        //"我"在这个群里
        return group.getJoinAt() != null;
    }

}
