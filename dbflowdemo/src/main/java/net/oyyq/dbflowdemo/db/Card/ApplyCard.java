package net.oyyq.dbflowdemo.db.Card;

import net.oyyq.dbflowdemo.db.model.datamodel.Apply;

import java.util.Date;


public class ApplyCard implements Card<Apply>{

    private String id;
    private int type;
    private String applicantId;
    private String targetId;
    private String groupId;
    private boolean passed;
    private Date createAt;


    //"我"首次接收一个Apply; 服务器回送别人通过后的Apply给"我"
    public Apply build(){
        Apply apply = new Apply();
        apply.setId(id);
        apply.setType(type);
        apply.setApplicantId(applicantId);
        apply.setTargetId(targetId);
        apply.setGroupId(groupId);
        apply.setPassed(passed);
        apply.setCreateAt(createAt == null?new Date():createAt);

        return apply;
    }


    public static ApplyCard build(Apply apply){
        ApplyCard applyCard = new ApplyCard();
        applyCard.setId(apply.getId());
        applyCard.setType(apply.getType());
        applyCard.setApplicantId(apply.getApplicantId());
        applyCard.setTargetId(apply.getTargetId());
        applyCard.setGroupId(apply.getGroupId());
        applyCard.setCreateAt(apply.getCreateAt());
        applyCard.setPassed(apply.isPassed());

        return applyCard;
    }




    @Override
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

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }


}
