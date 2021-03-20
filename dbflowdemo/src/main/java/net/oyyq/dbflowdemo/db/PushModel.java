package net.oyyq.dbflowdemo.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.oyyq.dbflowdemo.factory.Factory;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PushModel {

    // MessageCard送达
    public static final int ENTITY_TYPE_MESSAGE = 200;
    //UserCard送达
    public static final int ENTITY_TYPE_USER = 300;
    //GroupCard送达
    public static final int ENTITY_TYPE_GROUP = 400;
    //GroupMemberCard送达
    public static final int ENTITY_TYPE_GROUPMEMBER = 500;
    //TransactionCard送达
    public static final int ENTITY_TYPE_TRANSACTION = 600;
    //"我"收到的ApplyCard送达
    public static final int ENTITY_TYPE_APPLY = 700;
    //SysNotifyCard送达
    public static final int ENTITY_TYPE_SYSNOTIFY = 800;



    // Session类型, 判断Session是单聊还是群聊
    public static final int RECEIVER_TYPE_USER = 1;             //单聊
    public static final int RECEIVER_TYPE_GROUP = 2;            // 群聊
    public static final int RECEIVER_TYPE_SYSTEM = 3;             //无状态推送, 系统通知
    public static final int RECEIVER_TYPE_APPLY = 4;


    private List<Entity> entities = new ArrayList<>();

    private PushModel(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }



    public static class Entity {
        public Entity() { }

        // 类型
        public int type;
        // 实体, 是JsonArray, 从中解析出Card子类
        public String content;
        // 生成时间
        public Date createAt;

        @Override
        public String toString() {
            return "Entity{" +
                    "type=" + type +
                    ", content='" + content + '\'' +
                    ", createAt=" + createAt +
                    '}';
        }
    }



    /**
     * 把一个Json字符串，转化为一个实体数组
     * 并把数组封装到PushModel中，方便后面的数据流处理
     *
     * @param json Json数据
     * @return
     */
    public static PushModel decode(String json) {
        Gson gson = Factory.getGson();
        Type type = new TypeToken<List<Entity>>() {}.getType();

        try {
            List<Entity> entities = gson.fromJson(json, type);
            if (entities != null && entities.size() > 0)
                return new PushModel(entities);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
