package net.oyyq.dbflowdemo.factory;


import android.app.Application;

import androidx.annotation.StringRes;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import net.oyyq.common.Common;
import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.ApplyCard;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.Card.GroupMemberCard;
import net.oyyq.dbflowdemo.db.Card.MessageCard;
import net.oyyq.dbflowdemo.db.Card.TransactionCard;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.center.ApplyDispatcher;
import net.oyyq.dbflowdemo.db.center.CardCenter;
import net.oyyq.dbflowdemo.db.center.GroupDispatcher;
import net.oyyq.dbflowdemo.db.center.GroupMemberDispatcher;
import net.oyyq.dbflowdemo.db.center.MessageDispatcher;
import net.oyyq.dbflowdemo.db.center.TransactionDispatcher;
import net.oyyq.dbflowdemo.db.center.UserDispatcher;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.model.RspModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Apply;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.Transaction;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;



/**
 * 提供公共工具
 */
public class Factory {
    private static final String TAG = Factory.class.getSimpleName();
    // 单例模式
    private static final Factory instance;
    // 全局的线程池
    private final Executor executor;
    // 全局的Gson
    private final Gson gson;
    private static Application context;

    static {
        instance = new Factory();
    }

    private Factory() {
        // 新建一个4个线程的线程池
        executor = Executors.newFixedThreadPool(4);
        gson = Common.gson;
    }


    public static void setContext(Application context) {
        Factory.context = context;
    }

    /**
     * Factory 中的初始化
     */
    public static void setup() {
        // 初始化数据库
        FlowManager.init(new FlowConfig.Builder(context)
                .openDatabasesOnInit(true)          // 初始化数据库并打开
                .build());

        // 持久化的数据进行Account初始化 => 用于登陆账户
        Account.load(context);
    }



    /**
     * 异步运行
     *
     * @param runnable Runnable
     */
    public static void runOnAsync(Runnable runnable) {
        // 拿到单例，拿到线程池，然后异步执行
        instance.executor.execute(runnable);
    }

    /**
     * 返回一个全局的Gson
     *
     * @return Gson
     */
    public static Gson getGson() {
        return instance.gson;
    }




    /**
     * 进行错误Code的解析，TODO 还有几个错误码待解析
     * 把网络返回的Code值进行统一的规划并返回为一个String资源
     *
     * @param model    RspModel
     * @param callback DataSource.FailedCallback 用于返回一个错误的资源Id
     */
    public static void decodeRspCode(RspModel model, DataSource.FailedCallback callback) {
        if (model == null)
            return;

        // 进行Code区分 TODO 还有一些code 没写..
        switch (model.getCode()) {
            case RspModel.SUCCEED:
                return;
            case RspModel.ERROR_SERVICE:
                decodeRspCode(R.string.data_rsp_error_service, callback);
                break;
            case RspModel.ERROR_NOT_FOUND_USER:
                decodeRspCode(R.string.data_rsp_error_not_found_user, callback);
                break;
            case RspModel.ERROR_NOT_FOUND_GROUP:
                decodeRspCode(R.string.data_rsp_error_not_found_group, callback);
                break;
            case RspModel.ERROR_NOT_FOUND_GROUP_MEMBER:
                decodeRspCode(R.string.data_rsp_error_not_found_group_member, callback);
                break;
            case RspModel.ERROR_CREATE_USER:
                decodeRspCode(R.string.data_rsp_error_create_user, callback);
                break;
            case RspModel.ERROR_CREATE_GROUP:
                decodeRspCode(R.string.data_rsp_error_create_group, callback);
                break;
            case RspModel.ERROR_CREATE_MESSAGE:
                decodeRspCode(R.string.data_rsp_error_create_message, callback);
                break;
            case RspModel.ERROR_PARAMETERS:
                decodeRspCode(R.string.data_rsp_error_parameters, callback);
                break;
            case RspModel.ERROR_PARAMETERS_EXIST_ACCOUNT:
                decodeRspCode(R.string.data_rsp_error_parameters_exist_account, callback);
                break;
            case RspModel.ERROR_PARAMETERS_EXIST_NAME:
                decodeRspCode(R.string.data_rsp_error_parameters_exist_name, callback);
                break;
            case RspModel.ERROR_ACCOUNT_TOKEN:
                decodeRspCode(R.string.data_rsp_error_account_token, callback);
                instance.logout();
                break;
            case RspModel.ERROR_ACCOUNT_LOGIN:
                decodeRspCode(R.string.data_rsp_error_account_login, callback);
                break;
            case RspModel.ERROR_ACCOUNT_REGISTER:
                decodeRspCode(R.string.data_rsp_error_account_register, callback);
                break;
            case RspModel.ERROR_ACCOUNT_NO_PERMISSION:
                decodeRspCode(R.string.data_rsp_error_account_no_permission, callback);
                break;
            case RspModel.ERROR_UNKNOWN:
            default:
                decodeRspCode(R.string.data_rsp_error_unknown, callback);
                break;
        }
    }



    private static void decodeRspCode(@StringRes final int resId, final DataSource.FailedCallback callback) {
        if (callback != null) callback.onDataNotAvailable(resId);
    }




    /**
     * 处理服务器的推送(MessageReceiver接收到的)
     * @param str
     */
    public static void dispatchPush(String str) {
        if (!Account.isLogin()) return;

        PushModel model = PushModel.decode(str);
        if (model == null) return;

        for (PushModel.Entity entity : model.getEntities()) {
            switch (entity.type) {
                case PushModel.ENTITY_TYPE_USER: {
                    UserCard card = getGson().fromJson(entity.content, UserCard.class);
                    getUserCenter().dispatch(card);
                    break;
                }
                case PushModel.ENTITY_TYPE_GROUP: {
                    GroupCard card = getGson().fromJson(entity.content, GroupCard.class);
                    getGroupCenter().dispatch(card);
                    break;
                }
                case PushModel.ENTITY_TYPE_GROUPMEMBER: {
//                    Type type = new TypeToken<List<GroupMemberCard>>() {}.getType();

                    GroupMemberCard card = getGson().fromJson(entity.content, GroupMemberCard.class);
                    getGroupMemberCenter().dispatch(card);
                    break;
                }
                case PushModel.ENTITY_TYPE_MESSAGE: {
                    MessageCard card = getGson().fromJson(entity.content, MessageCard.class);
                    getMessageCenter().dispatch(card);
                    break;
                }
                case PushModel.ENTITY_TYPE_TRANSACTION: {
                    TransactionCard card = getGson().fromJson(entity.content, TransactionCard.class);
                    getTranctionCenter().dispatch(card);
                    break;
                }
                case PushModel.ENTITY_TYPE_APPLY: {
                    ApplyCard applyCard = getGson().fromJson(entity.content, ApplyCard.class);
                    getApplyCenter().dispatch(applyCard);
                    break;
                }
                case PushModel.ENTITY_TYPE_SYSNOTIFY: {
                    break;
                }
                default:
                    continue;
            }
        }
    }



    private void logout(){
        // "我"退出登陆, 回到登陆页面
    }


    /**
     * 获取一个用户中心的实现类
     *
     * @return 用户中心的规范接口
     */
    public static CardCenter<User, UserCard> getUserCenter() {
        return UserDispatcher.instance();
    }

    /**
     * 获取一个消息中心的实现类
     *
     * @return 消息中心的规范接口
     */
    public static CardCenter<Message, MessageCard> getMessageCenter() {
        return MessageDispatcher.instance();
    }


    /**
     * 获取一个群处理中心的实现类
     *
     * @return 群中心的规范接口
     */
    public static CardCenter<Group, GroupCard>  getGroupCenter() {
        return GroupDispatcher.instance();
    }


    /**
     * 获取一个群员处理中心的实现类
     *
     * @return
     */
    public static CardCenter<GroupMember, GroupMemberCard> getGroupMemberCenter(){
        return GroupMemberDispatcher.instance();
    }


    /**
     * 获取一个事务处理中心的实现类
     * @return
     */
    public static CardCenter<Transaction, TransactionCard> getTranctionCenter(){
        return TransactionDispatcher.instance();
    }


    /**
     * 获取一个Apply处理中心的实现类
     * @return
     */
    public static CardCenter<Apply, ApplyCard> getApplyCenter(){
        return ApplyDispatcher.instance();
    }



}
