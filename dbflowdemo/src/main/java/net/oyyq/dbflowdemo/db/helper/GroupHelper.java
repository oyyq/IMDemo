package net.oyyq.dbflowdemo.db.helper;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.dbflowdemo.R;
import net.oyyq.dbflowdemo.db.Card.GroupCard;
import net.oyyq.dbflowdemo.db.Card.GroupMemberCard;
import net.oyyq.dbflowdemo.db.datarepo.DataSource;
import net.oyyq.dbflowdemo.db.model.GroupCreateModel;
import net.oyyq.dbflowdemo.db.model.GroupMemberModel;
import net.oyyq.dbflowdemo.db.model.RspModel;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Group_Table;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.dbflowdemo.remote.Network;
import net.oyyq.dbflowdemo.remote.RemoteService;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class GroupHelper {

    public static List<GroupMember> getMemberUsers(String groupId, int size) {

        return SQLite.select(GroupMember_Table.userId, GroupMember_Table.alias, GroupMember_Table.portrait)
                .from(GroupMember.class)
                .where(GroupMember_Table.group_id.eq(groupId)).limit(size).queryList();

    }


    // 从本地找Group, "我"必须是群成员
    public static Group findFromLocal(String groupId) {
        return SQLite.select()
                .from(Group.class)
                .where(Group_Table.id.eq(groupId))
                .and(Group_Table.joinAt.isNotNull())
                .querySingle();
    }



    /**
     * "我"创建了一个群
     */
    public static void create(GroupCreateModel model, final DataSource.Callback<GroupCard> callback) {
        RemoteService service = Network.remote();
        service.groupCreate(model).enqueue(new Callback<RspModel<GroupCard>>() {
            @Override
            public void onResponse(Call<RspModel<GroupCard>> call, Response<RspModel<GroupCard>> response) {
                RspModel<GroupCard> rspModel = response.body();
                if (rspModel.success()) {
                    GroupCard groupCard = rspModel.getResult();
                    Factory.getGroupCenter().dispatch(groupCard);
                    callback.onDataLoaded(groupCard);

                    RemoteService service0 = Network.remote();
                    service0.groupMembers(groupCard.getId()).enqueue(new Callback<RspModel<List<GroupMemberCard>>>() {
                        @Override
                        public void onResponse(Call<RspModel<List<GroupMemberCard>>> call, Response<RspModel<List<GroupMemberCard>>> response) {
                            RspModel<List<GroupMemberCard>> rspModel0 = response.body();
                            if(rspModel0.success()) {
                                List<GroupMemberCard> membercards = rspModel0.getResult();

                                Factory.getGroupMemberCenter().dispatch(membercards.toArray(new GroupMemberCard[0]));
                            }
                        }

                        @Override
                        public void onFailure(Call<RspModel<List<GroupMemberCard>>> call, Throwable t) {

                        }
                    });



                } else {
                    Factory.decodeRspCode(rspModel, callback);
                }
            }

            @Override
            public void onFailure(Call<RspModel<GroupCard>> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });
    }




    // 从服务器端搜群
    public static Call search(String name, final DataSource.Callback<List<GroupCard>> callback) {
        RemoteService service = Network.remote();
        Call<RspModel<List<GroupCard>>> call = service.groupSearch(name);

        call.enqueue(new Callback<RspModel<List<GroupCard>>>() {
            @Override
            public void onResponse(Call<RspModel<List<GroupCard>>> call, Response<RspModel<List<GroupCard>>> response) {
                RspModel<List<GroupCard>> rspModel = response.body();
                if (rspModel.success()) {
                    // 返回数据直接刷新到界面, 查询群无需存储
                    callback.onDataLoaded(rspModel.getResult());
                } else {
                    Factory.decodeRspCode(rspModel, callback);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<GroupCard>>> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });

        //把当前的Call返回
        return call;
    }



    /**
     * 对"我"已经建立的群添加新的群员
     * @param groupId
     * @param model
     * @param callback
     */
    public static void addMembers(String groupId, GroupMemberModel model, final DataSource.Callback<List<GroupMemberCard>> callback) {
        RemoteService service = Network.remote();
        service.groupMemberAdd(groupId, model).enqueue(new Callback<RspModel<List<GroupMemberCard>>>() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<RspModel<List<GroupMemberCard>>> call, Response<RspModel<List<GroupMemberCard>>> response) {
                RspModel<List<GroupMemberCard>> rspModel = response.body();
                if (rspModel.success()) {
                    List<GroupMemberCard> memberCards = rspModel.getResult();
                    if (memberCards != null && memberCards.size() > 0) {
                        // 新增成员分发给本地存储以及数据监听器
                        Factory.getGroupMemberCenter().dispatch(memberCards.toArray(new GroupMemberCard[0]));
                        callback.onDataLoaded(memberCards);         //删除成功的回调
                    }
                } else {
                    Factory.decodeRspCode(rspModel, null);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<GroupMemberCard>>> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });
    }



    /**
     * "我"删除一些"我"创建的群的一些群员
     * @param groupId
     * @param deleteModel
     * @param callback
     */
    public static void deleteMembers(String groupId, GroupMemberModel deleteModel, final DataSource.Callback<List<GroupMember>> callback) {

        RemoteService service = Network.remote();
        service.groupMemberDelete(groupId, deleteModel).enqueue(
                new Callback<RspModel<List<GroupMemberCard>>>() {
                    @Override
                    public void onResponse(Call<RspModel<List<GroupMemberCard>>> call, Response<RspModel<List<GroupMemberCard>>> response) {
                        RspModel<List<GroupMemberCard>> rspModel = response.body();
                        List<GroupMemberCard> undelete = rspModel.getResult();
                        if (rspModel.success()) {
                            //群员全部删除成功了
                            if(undelete == null || undelete.size() == 0){
                                //本地操作删除
                                String[] deleteIds = deleteModel.getMemberIds().toArray(new String[0]);
                                TransactionHelper.me_remove_gpmember(deleteIds);
                                callback.onDataLoaded(null);
                            } else {
                                //有部分群员没有删除成功
                                Set<String> deletemembers = deleteModel.getMemberIds();
                                if (undelete.size() > 0) {
                                    for (GroupMemberCard card : undelete)
                                        deletemembers.remove(card.getId());
                                }

                                TransactionHelper.me_remove_gpmember(deletemembers.toArray(new String[0]));
                                callback.onDataLoaded(null);
                            }
                        } else {
                            callback.onDataNotAvailable(R.string.delete_member_fail);
                        }
                    }

                    @Override
                    public void onFailure(Call<RspModel<List<GroupMemberCard>>> call, Throwable t) {
                        callback.onDataNotAvailable( R.string.data_network_error);
                    }
                }
        );
    }





    /**
     * "我"添加了"我"创建的群的一些群员为管理员
     * @param groupId
     * @param model
     * @param callback
     */
    public static void AddAdmins(String groupId, GroupMemberModel model, final DataSource.Callback<List<GroupMemberCard>> callback) {

        RemoteService service = Network.remote();
        service.AddAdmin(groupId, model).enqueue(new Callback<RspModel<List<GroupMemberCard>>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<RspModel<List<GroupMemberCard>>> call, Response<RspModel<List<GroupMemberCard>>> response) {

                RspModel<List<GroupMemberCard>> rspModel = response.body();
                final List<GroupMemberCard> memberCards = rspModel.getResult();
                if(rspModel.success()){

                    if (memberCards == null || memberCards.size() == 0) {
                        Set<String> memberIds = model.getMemberIds();
                        TransactionHelper.me_addAdmin(memberIds.toArray(new String[0]));
                        callback.onDataLoaded(null);
                    } else {
                        //有部分群员没有添加成功
                        Set<String> adminmembers = model.getMemberIds();
                        if (memberCards.size() > 0) {
                            for (GroupMemberCard card : memberCards)
                                adminmembers.remove(card.getId());
                        }

                        TransactionHelper.me_addAdmin(adminmembers.toArray(new String[0]));
                        callback.onDataLoaded(null);
                    }

                } else {
                    callback.onDataNotAvailable(R.string.add_admin_fail);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<GroupMemberCard>>> call, Throwable t) {
                callback.onDataNotAvailable( R.string.data_network_error);
            }
        });

    }





    /**
     * "我"退出了某个群
     * @param groupId
     */
    public static void me_exitgroup(String groupId, final DataSource.Callback callback){

        RemoteService service = Network.remote();
        service.ExitGroup(groupId).enqueue(new Callback<RspModel>() {
            @Override
            public void onResponse(Call<RspModel> call, Response<RspModel> response) {
                RspModel rspModel = response.body();
                if(rspModel.success()){
                    callback.onDataLoaded(null);            //通知外层"我"退群成功了
                    //执行事务
                    TransactionHelper.me_exitgroup(groupId);
                } else {
                    callback.onDataNotAvailable(R.string.exit_group_fail);
                }
            }

            @Override
            public void onFailure(Call<RspModel> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });
    }


    /**
     * 查询一个群有多少个群员
     * @param groupId
     * @return
     */
    public static long getGroupMemberCount(String groupId) {
        return SQLite.selectCountOf()
                .from(GroupMember.class).where(GroupMember_Table.group_id.eq(groupId)).count();
    }



    /**
     * 刷新"我"加入的群组列表
     */
    public static void refreshGroups() {
        RemoteService service = Network.remote();
        service.groups("").enqueue(new Callback<RspModel<List<GroupCard>>>() {
            @Override
            public void onResponse(Call<RspModel<List<GroupCard>>> call, Response<RspModel<List<GroupCard>>> response) {
                RspModel<List<GroupCard>> rspModel = response.body();
                if (rspModel.success()) {
                    final List<GroupCard> groupCards = rspModel.getResult();
                    if (groupCards != null && groupCards.size() > 0) {
                        Factory.getGroupCenter().dispatch(groupCards.toArray(new GroupCard[0]));
                    }
                } else {
                    Factory.decodeRspCode(rspModel, new DataSource.Callback<Group>() {
                        @Override
                        public void onDataLoaded(Group group) {}

                        @Override
                        public void onDataNotAvailable(int strRes) {
                            //DemoApplication.showToast(strRes);
                        }

                    });
                }

            }

            @Override
            public void onFailure(Call<RspModel<List<GroupCard>>> call, Throwable t) {
                // 不做任何事情
            }
        });
    }



    /**
     * 从服务器端拉取群员的最新数据到本地
     * @param group
     */
    public static void refreshGroupMembers(Group group){

        RemoteService service = Network.remote();
        service.groupMembers(group.getId())
                .enqueue(new Callback<RspModel<List<GroupMemberCard>>>() {
                    @Override
                    public void onResponse(Call<RspModel<List<GroupMemberCard>>> call, Response<RspModel<List<GroupMemberCard>>> response) {

                        RspModel<List<GroupMemberCard>> rspModel = response.body();
                        if (rspModel.success()) {
                            List<GroupMemberCard> memberCards = rspModel.getResult();
                            if (memberCards != null && memberCards.size() > 0) {
                                Factory.getGroupMemberCenter().dispatch(memberCards.toArray(new GroupMemberCard[0]));
                            }
                        } else {
                            Factory.decodeRspCode(rspModel, new DataSource.Callback<GroupMember>() {
                                @Override
                                public void onDataNotAvailable(int strRes) {
                                    //DemoApplication.showToast(strRes);
                                }

                                @Override
                                public void onDataLoaded(GroupMember groupMember) {}
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<RspModel<List<GroupMemberCard>>> call, Throwable t) { }
                });


    }








}



