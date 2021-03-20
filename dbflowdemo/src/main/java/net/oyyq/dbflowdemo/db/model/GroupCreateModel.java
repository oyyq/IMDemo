package net.oyyq.dbflowdemo.db.model;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 新建一个 群模型
 */
public class GroupCreateModel {
    private final String id;              //群id
    private String name;            // 群名称
    private String desc;            // 群描述
    private String picture;         // 群图片
    private int notifyLevel;        //建群者的通知级别
    private Set<String> users = new HashSet<>();        //除了建群者之外的其他成员
    private String ownerId;         //建群者id


    public GroupCreateModel(String id){
        this.id = id;
    }

    /**
     * 建造者模式，快速的建立一个GroupCreateModel
     */
    public static class Builder {
        private GroupCreateModel model;

        public Builder() {
            this.model = new GroupCreateModel(UUID.randomUUID().toString());
        }

        // 群的基本信息
        public GroupCreateModel.Builder basicInfo(String name, String desc, String picture, String ownerId) {
            this.model.name = name;
            this.model.desc =desc;
            this.model.picture = picture;
            this.model.ownerId = ownerId;
            return this;
        }

        //"我"(群主)的通知级别
        public GroupCreateModel.Builder notifyLevel(int myNotifyLevel) {
            this.model.notifyLevel = myNotifyLevel;
            return this;
        }

        //除了"我"之外的群员
        public GroupCreateModel.Builder members(Set<String> users) {
            this.model.users = users;
            return this;
        }

        public GroupCreateModel build() {
            return this.model;
        }

    }


}
