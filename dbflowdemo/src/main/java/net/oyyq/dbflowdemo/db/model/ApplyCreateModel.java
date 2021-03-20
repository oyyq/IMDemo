package net.oyyq.dbflowdemo.db.model;

import net.oyyq.dbflowdemo.db.model.datamodel.Apply;

import java.util.Date;
import java.util.UUID;

/**
 * 申请者快速创建一个ApplyCreateModel, 发送到服务器端
 */
public class ApplyCreateModel {

    private String id;
    private int type;
    private String applicantId;
    private String targetId;
    private String groupId;
    private Date createAt;
    //不需要passed, 申请者开始申请时passed = false;
    // 通过后由服务器回送一个ApplyCard给申请者, Apply记录passed改成true


    public ApplyCreateModel(String id){
        this.id = id;
    }

    public static class Builder {
        private ApplyCreateModel model;

        public Builder() {
            this.model = new ApplyCreateModel(UUID.randomUUID().toString());
        }

        // 设置接收者
        public ApplyCreateModel.Builder init(int type, String applicantId, String targetId) {
            this.model.type = type;
            this.model.applicantId =  applicantId;
            this.model.targetId= targetId;
            this.model.createAt = new Date();     //一个apply的创建时间
            return this;
        }

        // 设置内容
        public ApplyCreateModel.Builder group(String groupId) {
            this.model.groupId = groupId;
            return this;
        }

        public ApplyCreateModel build() {
            return this.model;
        }
    }


    /**
     * 由AppyCreateModel创建一个Apply
     * @return
     */
    public Apply buildApply(){
        Apply apply = new Apply();
        apply.setId(id);
        apply.setType(type);
        apply.setApplicantId(applicantId);
        apply.setTargetId(targetId);
        apply.setGroupId(groupId);
        apply.setCreateAt(createAt);
        apply.setPassed(false);         //新创建的Apply记录默认没有通过
        return apply;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }


}
