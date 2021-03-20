package net.oyyq.dbflowdemo.db.helper;


import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Card.MessageCard;
import net.oyyq.dbflowdemo.db.center.MessageDispatcher;
import net.oyyq.dbflowdemo.db.model.MsgCreateModel;
import net.oyyq.dbflowdemo.db.model.RspModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.Message_Table;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.dbflowdemo.remote.Network;
import net.oyyq.dbflowdemo.remote.RemoteService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MessageHelper {

    // 从本地找消息
    public static Message findFromLocal(String id) {
        return SQLite.select()
                .from(Message.class)
                .where(Message_Table.id.eq(id))
                .querySingle();
    }

    public static Message findLastWithGroup(String groupId) {
        return SQLite.select()
                .from(Message.class)
                .where(Message_Table.group_id.eq(groupId))
                .orderBy(Message_Table.createAt, false) // 倒序查询
                .querySingle();
    }

    public static Message findLastWithUser(String userId) {
        return SQLite.select()
                .from(Message.class)
                .where( OperatorGroup.clause(Message_Table.senderId.eq(userId), Message_Table.group_id.isNull())
                        .or(Message_Table.receiverId.eq(userId)) )
                .orderBy(Message_Table.createAt, false)   // 倒序查询
                .querySingle();
    }


    /**
     * "我"异步发送一条消息
     * 1. 发送一条新消息 2. 重发一条上次发送失败的消息  3. 对方解除关注"我", "我"发了一条失败的消息()
     * @param model
     */
    public static void push(final MsgCreateModel model) {
        Factory.runOnAsync(new Runnable() {
            @Override
            public void run() {
                // 成功状态：如果是一个已经发送过的消息，则不能重新发送
                // 正在发送状态：如果是一个消息正在发送，则不能重新发送
                Message message = findFromLocal(model.getId());
                if (message != null && message.getStatus() != Message.STATUS_FAILED) return;


                final MessageCard card = model.buildCard();
                if(MessageDispatcher.DispatchOrNot_Status(card)) return;
                Factory.getMessageCenter().dispatch(card);


               /*
                //TODO 先不考虑非文字消息, 兼容也很简单
                // 发送文件消息分两部：上传到云服务器，消息Push到我们自己的服务器, 如果是文件类型的（语音，图片，文件），需要先上传后才发送
                if (card.getType() != Message.TYPE_STR) {
                    // 没有上传到云服务器的，还是本地手机文件
                    if (!card.getContent().startsWith(UploadHelper.ENDPOINT)) {
                        String content;

                        switch (card.getType()) {
                            case Message.TYPE_PIC:
                                content = uploadPicture(card.getContent());
                                break;
                            case Message.TYPE_AUDIO:
                                content = uploadAudio(card.getContent());
                                break;
                            default:
                                content = "";
                                break;
                        }


                        if (TextUtils.isEmpty(content)) {
                            // 失败, 但是本地可以播放(语, 图), card.content还是本地路径
                            card.setStatus(Message.STATUS_FAILED);
                            Factory.getMessageCenter().dispatch(card);
                            // 直接返回
                            return;
                        }

                        // 成功则把网络路径进行替换
                        card.setContent(content);
                        Factory.getMessageCenter().dispatch(card);
                        // 因为卡片的内容改变了，而我们上传到服务器是使用的model，所以model也需要跟着更改
                        model.refreshByCard();
                    }
                }

            */


                if(card.getStatus() == Message.STATUS_CREATED) {
                    //只有创建完成的消息发向服务器
                    RemoteService service = Network.remote();

                    service.msgPush(model).enqueue(new Callback<RspModel<MessageCard>>() {
                        @Override
                        public void onResponse(Call<RspModel<MessageCard>> call, Response<RspModel<MessageCard>> response) {
                            RspModel<MessageCard> rspModel = response.body();
                            if ( rspModel.success()) {              //TODO null pointer exception
                                MessageCard rspCard = rspModel.getResult();

                                if (rspCard != null) {
                                    //rspCard的时间是Message在Server端的创建时间
                                    Factory.getMessageCenter().dispatch(rspCard);
                                }

                            } else {
                                // 检查异常原因
                                Factory.decodeRspCode(rspModel, null);
                                // 走失败流程
                                onFailure(call, null);
                            }
                        }

                        @Override
                        public void onFailure(Call<RspModel<MessageCard>> call, Throwable t) {
                            // 通知失败
                            card.setStatus(Message.STATUS_FAILED);
                            Factory.getMessageCenter().dispatch(card);
                        }
                    });


                }
            }
        });
    }







}
