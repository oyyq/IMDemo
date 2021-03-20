package net.oyyq.dbflowdemo;


import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.center.MessageDispatcher;
import net.oyyq.dbflowdemo.db.datarepo.ContactRepository;
import net.oyyq.dbflowdemo.db.datarepo.GroupMembersRepository;
import net.oyyq.dbflowdemo.db.datarepo.GroupsRepository;
import net.oyyq.dbflowdemo.db.datarepo.MessageRepository;
import net.oyyq.dbflowdemo.db.datarepo.SessionRepository;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.helper.TransactionHelper;

import net.oyyq.dbflowdemo.db.model.datamodel.Apply;
import net.oyyq.dbflowdemo.db.model.datamodel.Group;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember;
import net.oyyq.dbflowdemo.db.model.datamodel.GroupMember_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;
import net.oyyq.dbflowdemo.db.model.datamodel.Message_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Session;
import net.oyyq.dbflowdemo.db.model.datamodel.Session_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import net.oyyq.dbflowdemo.db.model.datamodel.User_Table;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.util.Date;
import java.util.List;


/**
 * 测试DbHelper 增删该查, Repository 增删改查
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class repositoryTest {

    private Context context;
    @org.junit.Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        FlowManager.init(context);
    }


    @Test
    public void testRepository() throws Exception {

        ContactRepository contactRepo = new ContactRepository();
        contactRepo.load(null);
        final String contactRepoId = ContactRepository.repoPrefix+ContactRepository.id;

        SessionRepository sessionRepo = new SessionRepository();
        sessionRepo.load(null);
        final String sessionRepoId = SessionRepository.repoPrefix+SessionRepository.id;


        GroupsRepository groupsRepo = new GroupsRepository();
        groupsRepo.load(null);
        final String groupRepoId = GroupsRepository.repoPrefix + GroupsRepository.id;


//        Assert.assertEquals(DbHelper.getLisenterForId(User.class, contactRepoId), contactRepo);
//        Assert.assertEquals(DbHelper.getLisenterForId(Session.class, sessionRepoId), sessionRepo);
//        Assert.assertEquals(DbHelper.getLisenterForId(Group.class, groupRepoId), groupsRepo);


        User user = new User();
        user.setId("123");
        user.setName("oyyq");       //"oyyq" => "我"
        user.setFollow(false);
        user.setSex(2);
        user.setDesc("Happy123");
        user.setPortrait("***");
        user.setModifyAt(new Date());
        Account.setUserId(user.getId());            //"oyyq"是登陆账户

        User user1 = new User();
        user1.setId("456");
        user1.setName("xxy");       //关注"xxy"
        user1.setSex(1);
        user1.setDesc("Happy456");
        user1.setPortrait("***");
        user1.setModifyAt(new Date());
        user1.setFollow(true);

        User user2 = new User();
        user2.setId("789");
        user2.setName("jlp");       //关注"jlp"
        user2.setSex(2);
        user2.setDesc("Happy789");
        user2.setPortrait("***");
        user2.setModifyAt(new Date());
        user2.setFollow(true);

        DbHelper.save(User.class, user, user1, user2);

//        Assert.assertEquals(2, contactRepo.getDataList().size());
//        Assert.assertEquals(0, sessionRepo.getDataList().size());


        //建立"oyyq", "xxy", "jlp"所在的群
        Group group = new Group();
        group.setId("our group");
        group.setName("oyyq-xxy-jlp");
        group.setDesc("We are happy~~");
        group.setPicture("pppp");
        group.setNotifyLevel(GroupMember.NOTIFY_LEVEL_NONE);
        Date date = new Date();
        group.setModifyAt(date);
        group.setJoinAt(date);                    //"oyyq"加入了该群
        group.setDesc("we are happy~");
        group.setOwnerId(user.getId());


        DbHelper.save(Group.class, group);
        //Assert.assertEquals(1, groupsRepo.getDataList().size());


        //建立3个群员
        GroupMember groupMember1 = new GroupMember();         //"oyyq"
        groupMember1.setId("DIDADI");
        groupMember1.setGroup(group);
        groupMember1.setUserId(user.getId());
        groupMember1.setAlias("DD");
        groupMember1.setPortrait("...");
        groupMember1.setMyNotifyLevel(GroupMember.NOTIFY_LEVEL_NONE);
        groupMember1.setAdmin(true);
        groupMember1.setOwner(true);
        groupMember1.setModifyAt(new Date());


        GroupMember groupMember2 = new GroupMember();         //"xxy"
        groupMember2.setId("LGG");
        groupMember2.setGroup(group);
        groupMember2.setUserId(user1.getId());
        groupMember2.setAlias("qinqin");
        groupMember2.setPortrait("xxx");
        groupMember2.setAdmin(false);
        groupMember2.setOwner(false);
        groupMember2.setModifyAt(new Date());


        GroupMember groupMember3 = new GroupMember();         //"jlp"
        groupMember3.setId("MAMA");
        groupMember3.setGroup(group);
        groupMember3.setUserId(user2.getId());
        groupMember3.setAlias("mm");
        groupMember3.setPortrait("kkk");
        groupMember3.setAdmin(false);
        groupMember3.setOwner(false);
        groupMember3.setModifyAt(new Date());

        DbHelper.save(GroupMember.class, groupMember1, groupMember2, groupMember3);

        String gpMemberRepoId = GroupMembersRepository.repoPrefix+group.getId();
        GroupMembersRepository gpMemberRepo  = (GroupMembersRepository) DbHelper.getLisenterForId(GroupMember.class, gpMemberRepoId);

//        Assert.assertNotNull(gpMemberRepo);
//        Assert.assertEquals(3, gpMemberRepo.getDataList().size());
//        group = SQLite.select().from(Group.class).where(Group_Table.id.eq(group.getId())).querySingle();
//        Assert.assertEquals(3, group.getMemberNumber());

        //Message测试
        //"xxy" -> "oyyq"
        Message message1 = new Message();
        message1.setId("0000000");
        message1.setContent("I am xxy");
        message1.setAttach(null);
        message1.setType(Message.TYPE_STR);
        message1.setCreateAt(new Date());
        message1.setStatus(Message.STATUS_DONE);
        message1.setUnread(true);
        message1.setSenderId(user1.getId());
        message1.setReceiverId(user.getId());


        //"xxy" -> "oyyq"
        Message message2 = new Message();
        message2.setId("0000001");
        message2.setContent("Hello oyyq");
        message2.setAttach(null);
        message2.setType(Message.TYPE_STR);
        message2.setCreateAt(new Date(System.currentTimeMillis()+2000));
        message2.setStatus(Message.STATUS_DONE);
        message2.setUnread(true);
        message2.setSenderId(user1.getId());
        message2.setReceiverId(user.getId());


        //"oyyq" -> "our group"
        Message message3 = new Message();
        message3.setId("0000002");
        message3.setContent("@jlp");
        message3.setAttach(null);
        message3.setType(Message.TYPE_STR);
        message3.setCreateAt(new Date(System.currentTimeMillis()+3000));
        message3.setStatus(Message.STATUS_CREATED);
        message3.setUnread(false);
        message3.setSenderId(user.getId());
        message3.setGroup(group);

        //"jlp" -> "our group"
        Message message4 = new Message();
        message4.setId("0000003");
        message4.setContent("@xxy");
        message4.setAttach(null);
        message4.setType(Message.TYPE_STR);
        message4.setCreateAt(new Date(System.currentTimeMillis() + 1000));
        message4.setStatus(Message.STATUS_DONE);
        message4.setUnread(false);
        message4.setSenderId(user2.getId());
        message4.setGroup(group);

        DbHelper.save(Message.class, message1, message2, message4, message3);


        Apply applyJoin = new Apply();
        applyJoin.setId("apply1");
        applyJoin.setType(Apply.applyjoingroup);
        applyJoin.setApplicantId(user1.getId());
        applyJoin.setTargetId(user.getId());
        applyJoin.setCreateAt(new Date());
        applyJoin.setPassed(false);
        DbHelper.save(Apply.class, applyJoin);



//        Session sessionXXYOYYQ = SQLite.select().from(Session.class).where(Session_Table.id.eq(Session.prefix+user1.getId())).querySingle();      //XXY是聊天对方
//        Message message = sessionXXYOYYQ.getMessage();      //外键查询, 只有Message.id != null, Message其他属性 == null.
//        int unReadCount = sessionXXYOYYQ.getUnReadCount();
//        String content = sessionXXYOYYQ.getContent();
//        Assert.assertEquals(message2.getId(), message.getId());
//        Assert.assertEquals(2, unReadCount);
//        Assert.assertEquals(message2.getContent(), content);


//        String xxyoyyqRepoId = MessageRepository.repoPrefix+user1.getId();
//        MessageRepository xxyoyyqRepo = (MessageRepository) DbHelper.getLisenterForId(Message.class, xxyoyyqRepoId);
//        Assert.assertEquals(2, xxyoyyqRepo.getDataList().size());
//        List<Message> oyyqxxyMes = SQLite.select(Message_Table.id).from(Message.class)
//                .where(Message_Table.group_id.isNull(), Message_Table.sender_id.eq(user1.getId()))
//                .or(Message_Table.receiver_id.eq(user1.getId())).queryList();
//        Assert.assertEquals(2, oyyqxxyMes.size());


//
//
//        Session sessionOurGroup = SQLite.select().from(Session.class).where(Session_Table.id.eq(Session.prefix+group.getId())).querySingle();      //XXY是聊天对方
//        Message ourGroupMessage = sessionOurGroup.getMessage();      //外键查询, 只有Message.id != null, Message其他属性 == null.
//        int unReadCount = sessionOurGroup.getUnReadCount();
//        String content = sessionOurGroup.getContent();
//        Assert.assertEquals(0, unReadCount);
//        Assert.assertEquals(message3.getId(), ourGroupMessage.getId());
//        Assert.assertEquals(message3.getContent(), content);
//
//
//        String ourgroupRepoId = MessageRepository.repoPrefix+group.getId();
//        MessageRepository groupMesRepo = (MessageRepository) DbHelper.getLisenterForId(Message.class, ourgroupRepoId);
//        Assert.assertEquals(2, groupMesRepo.getDataList().size());



        //Transaction测试
        //1."oyyq"取关"xxy"
/*
        Session dbSession = SQLite.select(Session_Table.id).from(Session.class).where(Session_Table.id.eq(user1.getSession().getId())).querySingle();
        Assert.assertNotNull(dbSession);

        TransactionHelper.IUnfollowOther(user1.getId());
        User tempXXY = SQLite.select().from(User.class).where(User_Table.id.eq(user1.getId())).querySingle();
        Assert.assertNull(tempXXY);

        Session sessionXXY = user1.getSession();
        Assert.assertEquals(Session.prefix+user1.getId(), sessionXXY.getId());

        dbSession = SQLite.select(Session_Table.id).from(Session.class).where(Session_Table.id.eq(sessionXXY.getId())).querySingle();
        Assert.assertNull(dbSession);
        Assert.assertEquals(-1, sessionRepo.indexOf(sessionXXY));
        List<Message> oyyqxxyMes = SQLite.select(Message_Table.id).from(Message.class).where(Message_Table.group_id.isNull(), Message_Table.senderId.eq(user1.getId()))
                .or(Message_Table.receiverId.eq(user1.getId())).queryList();
        Assert.assertEquals(0, oyyqxxyMes.size());

        String oyyqxxyRepoId = MessageRepository.repoPrefix+user1.getId();
        MessageRepository oyyqxxyRepo = (MessageRepository) DbHelper.getLisenterForId(Message.class,oyyqxxyRepoId);
        Assert.assertNull(oyyqxxyRepo);

        */

        MessageDispatcher messageDispatcher = MessageDispatcher.instance();

/*
        //2. "jlp"取关"oyyq"
        TransactionHelper.otherUnfollowMe(user2.getId());
        Assert.assertEquals(-1, contactRepo.indexOf(user2));


        //测试发送一条失败消息
        MessageCard failMes = new MessageCard();
        failMes.setId("0000004");
        failMes.setContent("mamanihao");
        failMes.setAttach(null);
        failMes.setType(Message.TYPE_STR);
        failMes.setCreateAt(new Date(System.currentTimeMillis() + 5000));
        failMes.setStatus(Message.STATUS_CREATED);
        failMes.setUnread(false);
        failMes.setSenderId(user.getId());
        failMes.setReceiverId(user2.getId());
        messageDispatcher.dispatch(failMes);
        Thread.sleep(3000);

        Message failMessage = SQLite.select(Message_Table.id, Message_Table.status).from(Message.class).where(Message_Table.id.eq("0000004")).querySingle();
        Assert.assertEquals(Message.STATUS_FAILED, failMessage.getStatus());
        Session jlpoyyqSession = SQLite.select().from(Session.class).where(Session_Table.id.eq(user2.getSession().getId())).querySingle();
        String failMesId = jlpoyyqSession.getMessage().getId();
        Assert.assertEquals(failMessage.getId(), failMesId);
        Assert.assertEquals(Message.STATUS_FAILED, failMessage.getStatus());

*/

/*
        //"我"被移除群聊
        TransactionHelper.me_outgroup(group.getId());
        Group testGroup = SQLite.select(Group_Table.id, Group_Table.joinAt).from(Group.class).where(Group_Table.id.eq(group.getId())).querySingle();
        Assert.assertNull(testGroup.getJoinAt());
        Assert.assertEquals(-1, groupsRepo.getDataList().indexOf(group));

        //"我"发到群里一条消息
        MessageCard failMescard = new MessageCard();
        failMescard.setId("0000005");
        failMescard.setContent("today is a good day");
        failMescard.setAttach(null);
        failMescard.setType(Message.TYPE_STR);
        failMescard.setCreateAt(new Date(System.currentTimeMillis() + 5000));
        failMescard.setStatus(Message.STATUS_CREATED);
        failMescard.setUnread(false);
        failMescard.setSenderId(user.getId());
        failMescard.setGroupId(group.getId());
        messageDispatcher.dispatch(failMescard);
        Thread.sleep(3000);

        Message failMessage = SQLite.select(Message_Table.id, Message_Table.status).from(Message.class)
                                    .where(Message_Table.id.eq(failMescard.getId())).querySingle();
        Assert.assertEquals(Message.STATUS_FAILED, failMessage.getStatus());

        Session groupSession = SQLite.select().from(Session.class).where(Session_Table.id.eq(group.getSession().getId())).querySingle();
        String failMesId = groupSession.getMessage().getId();
        Assert.assertEquals(failMessage.getId(), failMesId);
        */



/*

        String mesRepoId =  MessageRepository.repoPrefix+group.getId();
        MessageRepository messageRepo = (MessageRepository) DbHelper.getLisenterForId(Message.class, mesRepoId);
        Assert.assertNotNull(messageRepo);
        List<Message> messages = SQLite.select(Message_Table.id).from(Message.class).where(Message_Table.group_id.eq(group.getId())).queryList();
        Assert.assertEquals(2, messages.size());
        Assert.assertEquals(2,messageRepo.getDataList().size());

        Session sessionRecord = SQLite.select().from(Session.class).where(Session_Table.id.eq(Session.prefix + group.getId())).querySingle();
        Assert.assertNotNull(sessionRecord);

       //"我"退出群聊
        TransactionHelper.me_exitgroup(group.getId());
        Assert.assertEquals(-1,  groupsRepo.getDataList().indexOf(group));
        //所有群消息记录删除了
        messages = SQLite.select(Message_Table.id).from(Message.class).where(Message_Table.group_id.eq(group.getId())).queryList();
        Assert.assertEquals(0, messages.size());
        //MessageRepository解注册了
        messageRepo = (MessageRepository) DbHelper.getLisenterForId(Message.class, mesRepoId);
        Assert.assertNull(messageRepo);
        //Session记录删除了
        sessionRecord = SQLite.select().from(Session.class).where(Session_Table.id.eq(Session.prefix + group.getId())).querySingle();
        Assert.assertNull(sessionRecord);
        //Session从SessionRepository中删除缓存了
        Session Groupsession = group.getSession();
        Assert.assertNotNull(Groupsession);
        Assert.assertEquals(-1, sessionRepo.getDataList().indexOf(Groupsession));
        //所有群员记录删除了
        List<GroupMember> groupMembers = SQLite.select().from(GroupMember.class).where(GroupMember_Table.group_id.eq(group.getId())).queryList();
        Assert.assertEquals(0, groupMembers.size());
        //GroupMemberRepository解除注册了
        gpMemberRepo = (GroupMembersRepository) DbHelper.getLisenterForId(GroupMember.class, gpMemberRepoId);
        Assert.assertNull(gpMemberRepo);

*/

/*
        //"我"移除了群员Id"LGG"
        TransactionHelper.me_remove_gpmember(groupMember2.getId());
        GroupMember tempMember = SQLite.select().from(GroupMember.class).where(GroupMember_Table.id.eq(groupMember2.getId())).querySingle();
        Assert.assertNull(tempMember);

        //拿到更新后的数据
        Group group1 = groupsRepo.getDataList().get(0);
        Assert.assertEquals(2,  group1.getMemberNumber());

        List<GroupMember> userModels = GroupHelper.getMemberUsers(group.getId(), 4);
        StringBuilder builder = new StringBuilder();
        if (userModels != null && userModels.size() != 0) {
            for (GroupMember userModel : userModels) {
                builder.append(userModel.getAlias());
                builder.append(", ");
            }
            builder.delete(builder.lastIndexOf(", "), builder.length());
        }
        String holder = builder.toString();
        //级联群的更新传递到了GroupsRepo的缓存
        Assert.assertEquals(holder, (String)group1.getHolder());

        */

    }



    /*@After
    public void tearDown(){
        FlowManager.getDatabase(AppDataBase.NAME).reset(context);
        //FlowManager.destroy();
    }

*/




}
