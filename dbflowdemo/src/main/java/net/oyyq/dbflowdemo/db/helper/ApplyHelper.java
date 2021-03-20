package net.oyyq.dbflowdemo.db.helper;


import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.ApplyCard;
import net.oyyq.dbflowdemo.db.PushModel;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.model.ApplyCreateModel;
import net.oyyq.dbflowdemo.db.model.RspModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Apply;
import net.oyyq.dbflowdemo.db.model.datamodel.Apply_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.Session;
import net.oyyq.dbflowdemo.remote.Network;
import net.oyyq.dbflowdemo.remote.RemoteService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



/**
 * 操作Apply工具类
 */
public class ApplyHelper {

    //"我"发起apply: 向群管理员 申请加群
    public static void applyJoinGroup(Group group, final DataSource.Callback callback){

        RemoteService service = Network.remote();
        ApplyCreateModel model = new ApplyCreateModel.Builder()
                .init(Apply.applyjoingroup, Account.getUserId(), null)      //"我"现在还不知道接收申请者是谁
                .group(group.getId())
                .build();

        //ApplyCreateModel发送给服务器
        service.applyJoin(model).enqueue(new Callback<RspModel>() {
            @Override
            public void onResponse(Call<RspModel> call, Response<RspModel> response) {
                RspModel rspModel = response.body();
                if(rspModel.success()){
                    Apply apply = model.buildApply();           //将"我"发起的申请存储到Apply_Table
                    DbHelper.save(Apply.class, apply);

                    callback.onDataLoaded(null);            //通知外层, 申请发起成功
                } else {
                    callback.onDataNotAvailable(R.string.apply_fail);
                }
            }

            @Override
            public void onFailure(Call<RspModel> call, Throwable t) {
                callback.onDataNotAvailable( R.string.data_network_error);
            }
        });


    }




    //"我"通过 别人的加群请求, "我"的Apply_Table内已经有这条Apply记录, 操作Session, 级联更新Apply记录
    public static void passApply(Session session, DataSource.Callback<Session> callback){
        if(session.getReceiverType() == PushModel.ENTITY_TYPE_APPLY){
            session.setApplypassed(true);
        }

        Apply apply = SQLite.select().from(Apply.class).where(Apply_Table.id.eq(session.getApply().getId())).querySingle();
        apply.setPassed(true);

        //构造一张ApplyCard 传给服务器端, 告知"我"已经通过了这个请求, 让服务器端处理后续(通过后的操作)
        ApplyCard applyCard = ApplyCard.build(apply);

        RemoteService service = Network.remote();
        service.replyApply(applyCard).enqueue(new Callback<RspModel>() {
            @Override
            public void onResponse(Call<RspModel> call, Response<RspModel> response) {
                RspModel rspModel = response.body();
                if(rspModel.success()){
                    //成功将applyCard发送到服务器
                    session.update();
                    apply.cascadeUpdate();

                    callback.onDataLoaded(session);
                } else {
                    callback.onDataNotAvailable(R.string.apply_pass_fail);
                }
            }

            @Override
            public void onFailure(Call<RspModel> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });

    }



}
