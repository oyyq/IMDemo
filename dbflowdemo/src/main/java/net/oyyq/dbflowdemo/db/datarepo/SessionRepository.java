package net.oyyq.dbflowdemo.db.datarepo;


import androidx.annotation.NonNull;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import net.oyyq.dbflowdemo.db.model.datamodel.Session;
import net.oyyq.dbflowdemo.db.model.datamodel.Session_Table;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.UUID;



/**
 * SessionRepository, 监听所有的会话列表
 * APP只有一个SessionRepository, 登陆时注册, 结束App时解注册
 * 单例
 */
public class SessionRepository extends BaseDbRepository<Session> implements SessionDataSource {
    public static final SessionRepository instance = new SessionRepository();
    public static final String repoPrefix = "Session";
    public static final String id = UUID.randomUUID().toString();

    public SessionRepository(){
        Type type = ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        dataClass = (Class<Session>) type;
    }


    public static SessionRepository getInstance(){
        return instance;
    }


    @Override
    public String getId() { return repoPrefix+id; }

    @Override
    protected void insert(Session session) {
        dataList.addFirst(session);
    }

    @Override
    protected boolean isRequired(Session session) {
        return true;
    }


    @Override
    public void load(SucceedCallback<List<Session>> callback) {
        super.load(callback);
        if(dataList.size() == 0){
            //首次加载
            SQLite.select()
                    .from(Session.class)
                    .orderBy(Session_Table.modifyAt, false)
                    .limit(-1)
                    .async()
                    .queryListResultCallback(this)
                    .execute();
        } else {
            //SessionRepository的缓存和Session_Table的记录一致, 直接将dataLis缓存通知到Presenter
            notifyDataChange();
        }
    }


    @Override
    public void onListQueryResult(QueryTransaction transaction, @NonNull List<Session> tResult) {
        // 复写数据库回来的方法, 进行一次反转
        Collections.reverse(tResult);
        super.onListQueryResult(transaction, tResult);
    }

}
