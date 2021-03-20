package net.oyyq.dbflowdemo.db.helper;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.Card.UserCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.model.RspModel;
import net.oyyq.dbflowdemo.db.model.UserUpdateModel;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.db.model.datamodel.User_Table;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.dbflowdemo.remote.Network;
import net.oyyq.dbflowdemo.remote.RemoteService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class UserHelper {

    /**
     * 搜索一个用户，优先本地缓存，
     * 然后再从网络拉取
     */
    public static User search(String id) {
        User user = findFromLocal(id);
        if (user == null) {
            user = findFromNet(id);
        }
        return user;
    }

    // 从本地查询一个用户的信息
    public static User findFromLocal(String id) {
        return SQLite.select()
                .from(User.class)
                .where(User_Table.id.eq(id))
                .querySingle();
    }



    /**
     * "我"更新自己的用户信息
     * @param model
     * @param callback
     */
    public static void update(UserUpdateModel model, final DataSource.Callback<UserCard> callback) {
        RemoteService service = Network.remote();
        Call<RspModel<UserCard>> call = service.userUpdate(model);

        call.enqueue(new Callback<RspModel<UserCard>>() {
            @Override
            public void onResponse(Call<RspModel<UserCard>> call, Response<RspModel<UserCard>> response) {
                RspModel<UserCard> rspModel = response.body();
                if (rspModel.success()) {
                    UserCard userCard = rspModel.getResult();
                    Factory.getUserCenter().dispatch(userCard);
                    //通知外界用户信息更新成功
                    callback.onDataLoaded(userCard);
                } else {
                    Factory.decodeRspCode(rspModel, callback);
                }
            }

            @Override
            public void onFailure(Call<RspModel<UserCard>> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });
    }




    /**
     *  用户搜索界面搜索姓名模糊匹配为name的Users
     * @param name
     * @param callback
     * @return
     */
    public static Call search(String name, final DataSource.Callback<List<UserCard>> callback) {
        RemoteService service = Network.remote();
        Call<RspModel<List<UserCard>>> call = service.userSearch(name);

        call.enqueue(new Callback<RspModel<List<UserCard>>>() {
            @Override
            public void onResponse(Call<RspModel<List<UserCard>>> call, Response<RspModel<List<UserCard>>> response) {
                RspModel<List<UserCard>> rspModel = response.body();
                if (rspModel.success()) {
                    // 直接返回给UI层, 没走数据库存储
                    callback.onDataLoaded(rspModel.getResult());
                } else {
                    Factory.decodeRspCode(rspModel, callback);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<UserCard>>> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });

        return call;
    }





    /**
     * "我"关注其他人的网络请求
     * @param id
     * @param callback
     * @return
     */
    public static Call follow(String id, final DataSource.Callback<UserCard> callback) {
        RemoteService service = Network.remote();
        Call<RspModel<List<UserCard>>> call = service.userFollow(id);

        call.enqueue(new Callback<RspModel<List<UserCard>>>() {
            @Override
            public void onResponse(Call<RspModel<List<UserCard>>> call, Response<RspModel<List<UserCard>>> response) {
                RspModel<List<UserCard>> rspModel = response.body();
                if (rspModel.success()) {
                    List<UserCard> userCards = rspModel.getResult();
                    // 存储对方到数据库,同时更新"我"的关注数量
                    Factory.getUserCenter().dispatch(userCards.toArray(new UserCard[0]));
                    for(UserCard card : userCards) {
                        if(card.getId().equalsIgnoreCase(Account.getUserId())) continue;
                        callback.onDataLoaded(card);
                    }
                } else {
                    //通知外层, 关注失败了
                    Factory.decodeRspCode(rspModel, callback);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<UserCard>>> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });

        return call;
    }




    /**
     * "我"解除关注别人
     * @param userId   别人的Id
     * @param callback
     * @return
     */
    public static Call unfollow(final String userId, final DataSource.Callback<UserCard> callback) {

        RemoteService service = Network.remote();
        Call<RspModel> call = service.userUnFollow(userId);
        call.enqueue(new Callback<RspModel>() {
            @Override
            public void onResponse(Call<RspModel> call, Response<RspModel> response) {
                RspModel rspModel = response.body();
                if(rspModel.success()){
                    TransactionHelper.IUnfollowOther(userId);
                    callback.onDataLoaded(null);    //回调到外层通知解关注成功, 做界面端的逻辑
                } else {
                    //通知外层, 解关注失败了
                    Factory.decodeRspCode(rspModel, callback);
                }
            }

            @Override
            public void onFailure(Call<RspModel> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });

        return call;
    }




    /**
     * 刷新"我"的联系人
     */
    public static void refreshContacts() {
        RemoteService service = Network.remote();
        service.userContacts().enqueue(new Callback<RspModel<List<UserCard>>>() {

            @Override
            public void onResponse(Call<RspModel<List<UserCard>>> call, Response<RspModel<List<UserCard>>> response) {
                RspModel<List<UserCard>> rspModel = response.body();
                if (rspModel.success()) {
                    // 拿到"我"的联系人在服务器端的最新数据
                    final List<UserCard> cards = rspModel.getResult();
                    if (cards == null || cards.size() == 0) return;
                    UserCard[] cards1 = cards.toArray(new UserCard[0]);
                    Factory.getUserCenter().dispatch(cards1);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<UserCard>>> call, Throwable t) { }
        });
    }




    // 从网络查询某用户的信息, 同步处理
    public static User findFromNet(String id) {
        RemoteService remoteService = Network.remote();
        try {
            Response<RspModel<UserCard>> response = remoteService.userFind(id).execute();
            if(response != null) {
                UserCard card = response.body().getResult();
                if (card != null) {
                    User user = card.build();
                    Factory.getUserCenter().dispatch(card);
                    return user;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    /**
     * 本地查询"我"的联系人
     */
    public static List<User> getContact() {
        return SQLite.select(User_Table.id,  User_Table.name,
                User_Table.portrait, User_Table.desc)
                .from(User.class)
                .where(User_Table.isFollow.eq(true))
                .and(User_Table.id.notEq(Account.getUserId()))
                .orderBy(User_Table.name, true)
                .limit(100)
                .queryList();
    }





}
