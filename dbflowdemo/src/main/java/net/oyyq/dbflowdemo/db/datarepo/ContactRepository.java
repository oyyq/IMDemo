package net.oyyq.dbflowdemo.db.datarepo;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;

import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.db.model.datamodel.User_Table;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;


/**
 * 监听并缓存"我"已经关注的人
 * 生命周期:
 */
public class ContactRepository extends BaseDbRepository<User> implements ContactDataSource {

    public static final ContactRepository instance = new ContactRepository();
    public static final String repoPrefix = "Contact";
    public static final String id = UUID.randomUUID().toString();

    public static ContactRepository getInstance(){ return instance; }

    @Override
    public String getId() {
        return repoPrefix+id;
    }


    public ContactRepository(){
        Type type = ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        dataClass = (Class<User>) type;
    }


    @Override
    public void load(SucceedCallback<List<User>> callback) {
        super.load(callback);
        if(dataList.size() == 0){
            //首次加载
            SQLite.select()
                    .from(User.class)
                    .where(User_Table.isFollow.eq(true))
                    .and(User_Table.id.notEq(Account.getUserId()))
                    .orderBy(User_Table.name, true)
                    .limit(-1)
                    .async()
                    .queryListResultCallback(this)
                    .execute();
        } else {
            notifyDataChange();
        }
    }


    @Override
    protected void insert(User user) {
        dataList.add(user);
    }



    @Override
    protected boolean isRequired(User user) {
        return user.isFollow() && !Account.getUserId().equals(user.getId());
    }
}
