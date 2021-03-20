package net.oyyq.dbflowdemo.db.model;



import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.MessageCard;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import java.util.Date;
import java.util.UUID;


/**
 * 新建一条消息模型
 */
public class MsgCreateModel {

    // ID从客户端生产，一个UUID
    private final String id;
    private String content;
    private String attach;
    // 消息类型
    private int type = Message.TYPE_STR;
    // 接收者 可为空
    private String receiverId;
    private String groupId;
    // 接收者类型，群，人
    private int receiverType = PushModel.RECEIVER_TYPE_USER;
    //是否是重新发送的消息
    private boolean isRepushed = false;


    private MsgCreateModel( String id ) {
        this.id = id;    // UUID.randomUUID().toString();
    }

    public boolean isRepushed() {
        return isRepushed;
    }

    public void setRepushed(boolean repushed) {
        isRepushed = repushed;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getAttach() {
        return attach;
    }

    public int getType() {
        return type;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public int getReceiverType() {
        return receiverType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setReceiverType(int receiverType) {
        this.receiverType = receiverType;
    }


    // 当我们需要发送一个文件的时候，content刷新, transient: 不被Gson解析
    private transient MessageCard card;


    //由MsgCreateModel创建一个MessageCard, 是新建消息或重发消息
    public MessageCard buildCard() {
        if (card == null) {
            MessageCard card = new MessageCard();
            card.setId(id);
            card.setContent(content);
            card.setAttach(attach);
            card.setType(type);
            //创建卡片时 的时间: 客户端Message第一次被创建("我"发送)的时间, 或被我点击重发的时间
            card.setCreateAt(new Date());
            // 通过当前model建立的Card就是一个初步状态的Card
            card.setStatus(Message.STATUS_CREATED);
            //是不是重发消息
            card.setRepushed(isRepushed);
            card.setUnread(false);      //"我"创建的消息, 肯定是已读

            // 如果是群
            if (receiverType == PushModel.RECEIVER_TYPE_GROUP) {
                card.setGroupId(groupId);
            } else {
                card.setReceiverId(receiverId);
            }
            card.setSenderId(Account.getUserId());

            this.card = card;
        }
        return this.card;
    }



    // 同步到卡片的最新状态
    public void refreshByCard() {
        if (card == null) return;
        // 刷新内容和附件信息
        this.content = card.getContent();
        this.attach = card.getAttach();
    }



    /**
     * 建造者模式，快速的建立一个发送Model TODO 用于"我"建立一条新消息
     */
    public static class Builder {
        private MsgCreateModel model;

        public Builder() {
            this.model = new MsgCreateModel(UUID.randomUUID().toString());
        }

        // 设置接收者
        public Builder receiver(String receiverId, String groupId,  int receiverType) {
            this.model.receiverId = receiverId;
            this.model.groupId =  groupId;
            this.model.receiverType = receiverType;
            return this;
        }

        // 设置内容
        public Builder content(String content, int type) {
            this.model.content = content;
            this.model.type = type;
            return this;
        }

        public Builder attach(String attach) {
            this.model.attach = attach;
            return this;
        }

        public Builder isRepushed(boolean isRepushed){
            this.model.isRepushed = isRepushed;
            return this;
        }

        public MsgCreateModel build() {
            return this.model;
        }

    }




    /**
     * 把一个Message消息，转换为一个创建状态的CreateModel
     * TODO 用于"我"重发消息: Message构造MsgCreateModel
     * @param message Message
     * @return MsgCreateModel
     */
    public static MsgCreateModel buildWithMessage(Message message) {
        MsgCreateModel model = new MsgCreateModel(message.getId());
        model.content = message.getContent();
        model.attach = message.getAttach();
        model.type = message.getType();
        model.isRepushed = true;                // 能确定这是一条重发消息

        if (message.getGroup() != null) {
            model.groupId = message.getGroup().getId();
            model.receiverType = PushModel.RECEIVER_TYPE_GROUP;
        } else {
            model.receiverId = message.getReceiverId();
            model.receiverType = PushModel.RECEIVER_TYPE_USER;
        }

        return model;
    }




}
