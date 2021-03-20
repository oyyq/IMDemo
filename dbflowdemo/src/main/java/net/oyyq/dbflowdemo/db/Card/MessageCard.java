package net.oyyq.dbflowdemo.db.Card;


import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import java.util.Date;


/**
 * 1. "我"发出的消息(到人, 群)
 * 2. "我"收到的消息
 * 3/ "我"所在收到的消息
 */
public class MessageCard implements Card<Message> {
    private String id;
    private String content;
    private String attach;
    private int type;                   //文字, 语音, 图片, 文字
    private Date createAt;
    private int status;
    private boolean isRepushed;
    private boolean isUnread;           //服务器推送过来的新消息isUnread == true. 服务器回送 "我"发出的消息, isUnread == false
    private String groupId;             //nullable
    private String senderId;            //non-null
    private String receiverId;          //nullable

    //首次接收; 回送
    public Message build() {
        Message message = new Message();
        message.setId(id);
        message.setContent(content);
        message.setAttach(attach);
        message.setType(type);
        message.setCreateAt(createAt);
        message.setStatus(status);
        message.setRepushed(isRepushed);
        message.setUnread(isUnread);

        if(groupId != null)  {
            Group group = new Group();  group.setId(groupId);
            message.setGroup(group);
        } else if (receiverId != null){
            message.setReceiverId(receiverId);
        }

        message.setSenderId(senderId);

        return message;
    }



    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isRepushed() {
        return isRepushed;
    }

    public void setRepushed(boolean repushed) {
        isRepushed = repushed;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }


}
