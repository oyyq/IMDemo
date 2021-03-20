package net.oyyq.dbflowdemo.remote;


import net.oyyq.dbflowdemo.db.Card.ApplyCard;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.Card.GroupMemberCard;
import net.oyyq.dbflowdemo.db.Card.MessageCard;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.model.AccountRspModel;
import net.oyyq.dbflowdemo.db.model.ApplyCreateModel;
import net.oyyq.dbflowdemo.db.model.GroupCreateModel;
import net.oyyq.dbflowdemo.db.model.GroupMemberModel;
import net.oyyq.dbflowdemo.db.model.LoginModel;
import net.oyyq.dbflowdemo.db.model.MsgCreateModel;
import net.oyyq.dbflowdemo.db.model.RegisterModel;
import net.oyyq.dbflowdemo.db.model.RspModel;
import net.oyyq.dbflowdemo.db.model.UserUpdateModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;



public interface RemoteService {


    /**
     * 注册接口
     * @param model 传入的是RegisterModel
     * @return 返回的是RspModel<AccountRspModel>
     */
    @POST("account/register")
    Call<RspModel<AccountRspModel>> accountRegister(@Body RegisterModel model);

    /**
     * 登录接口
     * @param model LoginModel
     * @return RspModel<AccountRspModel>
     */
    @POST("account/login")
    Call<RspModel<AccountRspModel>> accountLogin(@Body LoginModel model);


    /**
     * 绑定设备Id
     *
     * @param pushId 设备Id
     * @return RspModel<AccountRspModel>
     */
    @POST("account/bind/{pushId}")
    Call<RspModel<AccountRspModel>> accountBind(@Path(encoded = true, value = "pushId") String pushId);


    /**
     * "我"更新自己的UserInfo, 传送给服务器
     * @param model
     * @return 更新后的"我"的用户卡片
     */
    @PUT("user")
    Call<RspModel<UserCard>> userUpdate(@Body UserUpdateModel model);


    /**
     * "我"关注别人
     * @param userId
     * @return  关注成功后, 拿回的对方的用户卡片
     */
    @PUT("user/follow/{userId}")
    Call<RspModel<List<UserCard>>> userFollow(@Path("userId") String userId);


    /**
     * "我"解除关注别人
     * @param userId
     * @return 从RspModel拿到状态码, 得知服务器端是否操作成功, 若不成功则重试一次
     */
    @PUT("user/unfollow/{userId}")
    Call<RspModel> userUnFollow(@Path("userId") String userId);


    /**
     * 搜索在服务器端, 姓名为name的用户
     * @param name
     * @return 拿到用户卡片
     */
    @GET("user/search/{name}")
    Call<RspModel<List<UserCard>>> userSearch(@Path("name") String name);


    /**
     * 从服务器端查询"我"的所有联系人
     * @return
     */
    @GET("user/contact")
    Call<RspModel<List<UserCard>>> userContacts();


    /**
     * 查询用户Id为userId的用户, 如搜索某个不是"我"好友的群员
     * @param userId
     * @return
     */
    @GET("user/{userId}")
    Call<RspModel<UserCard>> userFind(@Path("userId") String userId);


    /**
     * 推送一条消息到服务器端
     * @param model
     * @return MessageCard, 在服务器端建立消息后, 拿回该消息的推送状态和服务器创建消息时间
     */
    @POST("msg")
    Call<RspModel<MessageCard>> msgPush(@Body MsgCreateModel model);




/*

    // 另earlier -- later时间段Server向"我"推送的    所有类型的卡片   再次被推送一遍,
    // 这边由MessageReceiver对接收到的PushModel的entities进行解析
    @GET("pushHistory/{earlier}/{later}/toUser/{userId}")
    Call<RspModel> getPushedEntities(@Path(value = "earlier", encoded = true) String earlierStr,
                                                     @Path(value = "later", encoded = true) String laterStr,
                                                     @Path("userId") String userId);
*/


    /**
     * "我"创建一个群
     * @param model
     * @return 创建成功的群卡片
     */
    @POST("group")
    Call<RspModel<GroupCard>> groupCreate(@Body GroupCreateModel model);


    /**
     * 搜索一个群
     * @param groupId
     * @return 搜索到的群卡片
     */
    @GET("group/{groupId}")
    Call<RspModel<GroupCard>> groupFind(@Path("groupId") String groupId);


    @GET("group/search/{name}")
    Call<RspModel<List<GroupCard>>> groupSearch(@Path(value = "name", encoded = true) String name);

    @GET("group/list/{date}")
    Call<RspModel<List<GroupCard>>> groups(@Path(value = "date", encoded = true) String date);




    /**
     * "我"给群添加群成员.
     * @param groupId
     * @param model
     * @return  服务器端操作成功后, 拿回来的群员卡片
     */
    @POST("group/{groupId}/member")
    Call<RspModel<List<GroupMemberCard>>> groupMemberAdd(@Path("groupId") String groupId, @Body GroupMemberModel model);

    /**
     * "我"查询"我"所在的群(不是由"我"创建, 否则直接本地查询)的群员列表, TODO 该接口应该不会用, 原因在于"我"进群后, 服务器会将群员卡片都推送给"我"
     * @param groupId
     * @return 群员列表
     */
    @GET("group/{groupId}/member")
    Call<RspModel<List<GroupMemberCard>>> groupMembers(@Path("groupId") String groupId);




    /**
     * "我"删除了一些群员, 通知给服务器端进行 删除
     * @param groupId
     * @param model
     * @return 在服务器端没删除成功的群员卡片. (这种情况下重试一次删除操作)
     */
    @POST("group/{groupId}/DeleteMembers")
    Call<RspModel<List<GroupMemberCard>>> groupMemberDelete(@Path("groupId") String groupId, @Body GroupMemberModel model);


    /**
     * "我"退出某群
     * @param groupId
     * @return RspModel拿到状态码, 得知在服务器端"我"是否退出成功了
     */
    @POST("group/{groupId}/MyExit")
    Call<RspModel> ExitGroup(@Path("groupId") String groupId);



    /**
     * "我"添加新Admin
     * @return 服务器没操作成功的群员卡片, 此情况下重试一次
     */
    @POST("group/NewAdmins/{groupId}")
    Call<RspModel<List<GroupMemberCard>>> AddAdmin(@Path("groupId") String groupId, @Body GroupMemberModel model );



    //@Headers({"CONNECT_TIMEOUT:20000", "READ_TIMEOUT:20000", "WRITE_TIMEOUT:20000"})
    /**
     * "我"申请加入群聊,Header添加key-value pair, 并通过Interceptor进行解析, 将这些Headers参数移除
     *
     * @param  applyCreateModel  发送一个新创建的ApplyCreateModel到服务器端
     * @return RspModel 状态码 返回服务器端是否成功将"我"的请求推送到群主(待通过)
     */
    @POST("apply/applyJoin")
    Call<RspModel> applyJoin(@Body ApplyCreateModel applyCreateModel);


    /**
     * 回复一个Apply
     * @param card
     * @return
     */
    @POST("apply/reply")
    Call<RspModel> replyApply(@Body ApplyCard card);



}
