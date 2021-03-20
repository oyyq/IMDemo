package net.oyyq.dbflowdemo.db.helper;

import android.text.TextUtils;

import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.center.UserDispatcher;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.model.AccountRspModel;
import net.oyyq.dbflowdemo.db.model.LoginModel;
import net.oyyq.dbflowdemo.db.model.RegisterModel;
import net.oyyq.dbflowdemo.db.model.RspModel;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.dbflowdemo.remote.Network;
import net.oyyq.dbflowdemo.remote.RemoteService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AccountHelper {

    /**
     * 用户注册
     */
    public static void register(final RegisterModel model, final DataSource.Callback<UserCard> callback) {
        // 调用Retrofit对我们的网络请求接口做代理
        RemoteService service = Network.remote();
        // 得到一个Call
        Call<RspModel<AccountRspModel>> call = service.accountRegister(model);
        // 异步的请求
        call.enqueue(new AccountRspCallback(callback));
    }



    /**
     * 登录的调用
     *
     * @param model    登录的Model
     * @param callback 成功与失败的接口回送
     */
    public static void login(final LoginModel model, final DataSource.Callback<UserCard> callback) {
        // 在register后, "我"拿到了服务器分配的Token, 再次发起请求时, 请求头多了Token
        RemoteService service = Network.remote();
        Call<RspModel<AccountRspModel>> call = service.accountLogin(model);
        call.enqueue(new AccountRspCallback(callback));
    }



    /**
     * 对设备Id进行绑定的操作
     *
     * @param callback Callback
     */
    public static void bindPush(final DataSource.Callback<UserCard> callback) {
        String pushId = Account.getPushId();
        if (TextUtils.isEmpty(pushId)) return;


        RemoteService service = Network.remote();
        Call<RspModel<AccountRspModel>> call = service.accountBind(pushId);
        call.enqueue(new AccountRspCallback(callback));
    }




    private static class AccountRspCallback implements Callback<RspModel<AccountRspModel>>{
        final DataSource.Callback<UserCard> callback;

        AccountRspCallback(DataSource.Callback<UserCard> callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<RspModel<AccountRspModel>> call, Response<RspModel<AccountRspModel>> response) {
            RspModel<AccountRspModel> rspModel = response.body();
            if(rspModel.success()){
                AccountRspModel accoutModel = rspModel.getResult();
                UserCard myCard = accoutModel.getUserCard();
                //将"我"的卡片存储到本地
                Factory.getUserCenter().dispatch(myCard);

                //1. 拿到服务器给"我"分配的Token并存储在sharedpreference,
                //此时"我"在服务器端还没有绑定pushId
                //但是"我"在客户端已经拿到了GETUI分配给"我"的pushId
                Account.login(accoutModel);

                if(accoutModel.isBind()){     //accountModel.isBind()告诉"我",服服务器端"我"的记录是否绑定了pushId
                    Account.setBind(true);
                    if (callback != null) callback.onDataLoaded(myCard);

                } else {
                    // 发起服务器端绑定"我"的pushId
                    bindPush(callback);
                }

            }else {
                // 错误解析
                Factory.decodeRspCode(rspModel, callback);
            }

        }

        @Override
        public void onFailure(Call<RspModel<AccountRspModel>> call, Throwable t) {
            callback.onDataNotAvailable(R.string.data_network_error);
        }

    }


}
