package net.oyyq.dbflowdemo.db.helper;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.datarepo.GroupMembersRepository;
import net.oyyq.dbflowdemo.db.datarepo.MessageRepository;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.SysNotify;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 *  用同步的增删改查 => 改成同步后, 哪些是耗时操作, 需要放在线程中执行 ?
 *  当Session记录被移除时, 相应的MessageRepository要解注册
 */
public class DbHelper {
    private static final DbHelper instance;
    private Executor notifier = Executors.newSingleThreadExecutor();

    static { instance = new DbHelper(); }
    public static DbHelper getInstance(){
        return instance;
    }
    private DbHelper() { }


    /**
     * 观察者的集合
     * Class<?>： 观察的 表的类
     * Map<String, ChangedListener>：每一个表对应的观察者有很多, 同一个表的每个 观察者 有序号String id
     */
    private static final Map<Class<?>, Map<String, ChangedListener>> changedListeners = new HashMap<>();
    public Map<Class<?>, Map<String, ChangedListener>> getChangedListeners() { return changedListeners; }


    /**
     * 从所有的监听者中，获取某一个表的所有监听者
     *
     * @param modelClass 表对应的Class信息
     * @param <Model>    范型
     * @return Set<ChangedListener>
     */
    private <Model extends BaseDbModel>  Map<String, ChangedListener> getListeners(Class<Model> modelClass) {
        return changedListeners.get(modelClass);
    }



    /**
     * 添加一个监听
     * @param tClass   对某个表关注
     * @param listener 监听者
     * @param <Model>  表的范型
     */
    public static <Model extends BaseDbModel> void addChangedListener(final Class<Model> tClass, ChangedListener<Model> listener) {

        Map<String, ChangedListener> changedlistner = instance.getListeners(tClass);
        if (changedlistner == null) {
            changedlistner = new HashMap<>();
            instance.changedListeners.put(tClass, changedlistner);
        }

        ChangedListener originListener = changedlistner.get(listener.getId());       //listener: **Repository实例
        //同一个对象, 监听器已经在列表中
        if(originListener != null && originListener == listener ) return;
        changedlistner.put(listener.getId(), listener);
    }


    /**
     * 删除某一个表的某一个监听器
     *
     * @param tClass   表
     * @param listener 监听器
     * @param <Model>  表的范型
     */
    public static <Model extends BaseDbModel> void removeChangedListener(final Class<Model> tClass, ChangedListener<Model> listener) {

        Map<String, ChangedListener> changedListeners = instance.getListeners(tClass);
        if (changedListeners == null) { return; }
        //删除监听者
        changedListeners.remove(listener.getId());
    }

    /**
     * 创建新记录, 更新旧记录
     * @param tClass
     * @param models
     * @param <Model>
     */
    public static <Model extends BaseDbModel> void save(final Class<Model> tClass, final Model... models) {

        if (models == null || models.length == 0) return;

        //1. 更新Model... models
        ModelAdapter<Model> adapter = FlowManager.getModelAdapter(tClass);
        adapter.saveAll(Arrays.asList(models));
        instance.notifySave(tClass, Arrays.asList(models));

        //2. 在数据库的Model表记录已经更新后, 更新与之级联的表记录,  放在另一个事务中执行
        cascadeModelUpdate(models);
    }



    private static final <Model extends BaseDbModel> void cascadeModelUpdate(Model... models){

        HashMap<Class<? extends BaseDbModel>,  List<BaseDbModel>> updateModels = new HashMap<>();
        for(Model model: models){
            model.createCASCADEmodelsIfNeed();
            Set<BaseDbModel> cascadeUpdatemodels = model.getCASCADEUpdatemodels();
            if(cascadeUpdatemodels == null) continue;

            for(BaseDbModel cascadeModel: cascadeUpdatemodels){
                if(cascadeModel == null) continue;

                Type type = ((ParameterizedType)cascadeModel.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                List<BaseDbModel> baseDbModels = updateModels.get(type);

                if (baseDbModels == null) {
                    baseDbModels = new ArrayList<>();
                    updateModels.put((Class)type, baseDbModels);
                }

                //移除旧的, 根据Objects.equals比较
                baseDbModels.remove(cascadeModel);
                //添加新的 TODO 为什么? 在A_Table多个记录list级联到同一条B_Table的记录时, 该记录的级联更新不依赖于list的任何一条记录
                //        这时我们可以待list的记录都更新完毕, 对B_Table的记录做统一的级联更新
                baseDbModels.add(cascadeModel);

            }
        }


        Set<Map.Entry<Class<? extends BaseDbModel>, List<BaseDbModel>>> entries = updateModels.entrySet();
        for (Map.Entry<Class<? extends BaseDbModel>, List<BaseDbModel>> entry : entries) {
            Class<? extends BaseDbModel> clz = entry.getKey();
            List<BaseDbModel> dbModels = entry.getValue();
            final List<BaseDbModel> tempModels = new ArrayList<>();
            for(BaseDbModel dbModel : dbModels){
                if(dbModel.cascadeUpdate()){ tempModels.add(dbModel); }
            }
            instance.notifySave(clz, tempModels);
        }

    }




    public static <Model extends BaseDbModel> void delete(final Class<Model> tClass, final Model... models) {
        if (models == null || models.length == 0) return;

        //1. 删除某表的一些记录
        ModelAdapter<Model> adapter = FlowManager.getModelAdapter(tClass);
        adapter.deleteAll(Arrays.asList(models));
        instance.notifyDelete(tClass, Arrays.asList(models));

        //2. 删除这些记录级联的需要删除的其他表的记录, 在数据库层面, 外键关联OnDelete = CASCADE, 保证了表记录的级联删除
        // Repository中的缓存我们手动在cascadeModelDelete将它删除
        cascadeModelDelete(models);
    }



    private static final<Model extends BaseDbModel> void cascadeModelDelete(Model... models){
        HashMap<Class<? extends BaseDbModel>,  List<BaseDbModel>> deleteModels = new HashMap<>();

        for(Model model: models){
            Set<BaseDbModel> cascadeDeletemodels = model.getCASCADEDeletemodels();
            if(cascadeDeletemodels == null) continue;

            for(BaseDbModel cascadeModel: cascadeDeletemodels){
                if(cascadeModel == null) continue;

                Type type = ((ParameterizedType)cascadeModel.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                List<BaseDbModel> baseDbModels = deleteModels.get((Class) type);

                if (baseDbModels == null) {
                    baseDbModels = new ArrayList<>();
                    deleteModels.put((Class) type, baseDbModels);
                }

                baseDbModels.remove(cascadeModel);
                baseDbModels.add(cascadeModel);

            }
        }

        Set<Map.Entry<Class<? extends BaseDbModel>, List<BaseDbModel>>> entries = deleteModels.entrySet();
        for (Map.Entry<Class<? extends BaseDbModel>, List<BaseDbModel>> entry : entries) {
            Class<? extends BaseDbModel> clz = entry.getKey();
            List<BaseDbModel> dbModels = entry.getValue();
            final List<BaseDbModel> tempModels = new ArrayList<>();
            for(BaseDbModel dbModel : dbModels){
                if(dbModel.cascadeDelete()){ tempModels.add(dbModel); }
            }
            instance.notifyDelete(clz, tempModels);
        }

    }



    public synchronized final <Model extends BaseDbModel> void notifySave(final Class<Model> tClass, List<BaseDbModel> models) {
        createListenersIfNeed(tClass, models);

        final Map<String, ChangedListener> listeners = instance.getListeners(tClass);

        if(listeners != null && listeners.size() >0) {
            Iterator<Map.Entry<String, ChangedListener>> listeneriterator = listeners.entrySet().iterator();
            while (listeneriterator.hasNext()) {
                Map.Entry<String, ChangedListener> entry = listeneriterator.next();
                entry.getValue().onDataSave(true, models.toArray(new BaseDbModel[0]));
            }
        }
    }





    /**
     * 创建或者更新User / Group 表记录, 关心MessageRepository / GroupMemberRepositor是否存在了
     * @param tClass
     * @param models
     * @param <Model>
     */
    public synchronized static <Model extends BaseDbModel> void createListenersIfNeed(final Class<Model> tClass, List<BaseDbModel> models){
        //User, Session, Group的表监听器Repository已经存在了, 不需要创建并添加
        if (User.class.equals(tClass)) {
            for(BaseDbModel model : models){
                User user = (User) model;
                //单聊消息监听器
                int receiverType = PushModel.RECEIVER_TYPE_USER;
                String userId = user.getId();
                String repoId = MessageRepository.repoPrefix+userId;
                if(instance.getListeners(Message.class) != null && instance.getListeners(Message.class).get(repoId) != null) return;
                final MessageRepository messageRepo = new MessageRepository(receiverType, repoId, userId, null);
                addChangedListener(Message.class, messageRepo);
            }

        } else if (SysNotify.class.equals(tClass)) {
            //TODO SysNotifyRepository
        } else if (Group.class.equals(tClass)) {
            for(BaseDbModel model : models){
                Group group = (Group)model;
                //群员监听器
                String repoId1 = GroupMembersRepository.repoPrefix+group.getId();

                if(instance.getListeners(GroupMember.class) != null && instance.getListeners(GroupMember.class).get(repoId1) != null) return;
                final GroupMembersRepository groupMembersRepo = new GroupMembersRepository(repoId1, group.getId());
                addChangedListener(GroupMember.class, groupMembersRepo);
                //群消息监听器
                int receiverType = PushModel.RECEIVER_TYPE_GROUP;
                String groupId = group.getId();
                String repoId2 = MessageRepository.repoPrefix+groupId;
                if(instance.getListeners(Message.class) != null   && instance.getListeners(Message.class).get(repoId2) != null) return;
                final MessageRepository messageRepo = new MessageRepository(receiverType, repoId2, null, groupId);
                addChangedListener(Message.class, messageRepo);
            }
        }

    }




    public synchronized static final <Model extends BaseDbModel> void notifyDelete(final Class<Model> tClass, List<BaseDbModel> models) {

        removeListenersIfNeed(tClass, models);

        final Map<String, ChangedListener> listeners = instance.getListeners(tClass);

        if (listeners != null && listeners.size() > 0) {
            Iterator<Map.Entry<String, ChangedListener>> listeneriterator = listeners.entrySet().iterator();
            while (listeneriterator.hasNext()) {
                Map.Entry<String, ChangedListener> entry = listeneriterator.next();
                entry.getValue().onDataDelete(true, models.toArray(new BaseDbModel[0]));
            }
        }

    }




    /**
     * Group记录被删除, 关联GroupMemberRepository被解注册
     * Session记录被删除, MessageRepo 被销毁
     * @param tClass
     * @param models
     * @param <Model>
     */
    public synchronized static  <Model extends BaseDbModel> void removeListenersIfNeed(final Class<Model> tClass, List<BaseDbModel> models){
        if(tClass.equals(User.class)){
            for(BaseDbModel model : models){
                User user = (User) model;
                String userId = user.getId();


                String repoId = MessageRepository.repoPrefix+userId;
                MessageRepository messageRepo = (MessageRepository) instance.getListeners(Message.class).get(repoId);
                if(messageRepo != null){ messageRepo.dispose(); }
            }
        } else if (tClass.equals(Group.class)){
            for(BaseDbModel model : models){
                Group group = (Group)model;
                String groupId = group.getId();

                String repoId1 = GroupMembersRepository.repoPrefix+groupId;
                GroupMembersRepository groupMembersRepo = (GroupMembersRepository) instance.getListeners(GroupMember.class).get(repoId1);
                if(groupMembersRepo != null) groupMembersRepo.dispose();

                String repoId2 = MessageRepository.repoPrefix+groupId;
                MessageRepository messageRepo = (MessageRepository) instance.getListeners(Message.class).get(repoId2);
                if(messageRepo != null) messageRepo.dispose();

            }
        }

    }


    /**
     * 给外界获取一个ChangedListener实例(Repository)
     * @param tClass
     * @param repoId
     * @param <Model>
     * @return
     */
    public static <Model extends BaseDbModel> ChangedListener getLisenterForId(final Class<Model> tClass, String repoId){
        if(instance.getListeners(tClass) == null) return null;
        return instance.getListeners(tClass).get(repoId);
    }


    /**
     * 数据监听器
     */
    @SuppressWarnings({"unused", "unchecked"})
    public interface ChangedListener<Data extends BaseDbModel> {
        void onDataSave(boolean notifyout, Data... list);

        void onDataDelete(boolean notifyout,  Data... list);

        String getId();
    }


}
