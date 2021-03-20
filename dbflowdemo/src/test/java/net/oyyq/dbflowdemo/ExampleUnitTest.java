package net.oyyq.dbflowdemo;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.db.helper.DbHelper;
import net.oyyq.dbflowdemo.db.model.datamodel.Message;

import net.oyyq.dbflowdemo.db.model.datamodel.Message_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.Session;

import net.oyyq.dbflowdemo.db.model.datamodel.Session_Table;
import net.oyyq.dbflowdemo.db.model.datamodel.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class ExampleUnitTest {
    private Context context;

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @org.junit.Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        FlowManager.init(context);
    }


    /**
     * 以下单元测试通过
     * @throws Exception
     */
    @Test
    public void testOneToMany() throws Exception {


        User user = new User();
        user.setId("123");
        user.setName("oyyq");       //"oyyq"
        user.setFollow(false);
        Account.setUserId(user.getId());            //"oyyq"是登陆账户

        User user1 = new User();
        user1.setId("456");
        user1.setName("xxy");       //关注1
        user1.setFollow(true);

        User user2 = new User();
        user2.setId("789");
        user2.setName("jlp");       //关注2
        user2.setFollow(true);

        DbHelper.save(User.class, user, user1, user2);
        Thread.sleep(500);

        //"xxy" -> "oyyq"
        Message message1 = new Message();
        message1.setId("0000000");
        message1.setContent("I am xxy");
        message1.setCreateAt(new Date());
        message1.setStatus(Message.STATUS_DONE);
        message1.setSenderId(user1.getId());
        message1.setReceiverId(user.getId());
        message1.setUnread(true);


        //"xxy" -> "oyyq"
        Message message2 = new Message();
        message2.setId("0000001");
        message2.setContent("Hello oyyq");
        message2.setCreateAt(new Date(System.currentTimeMillis()+2000));
        message2.setStatus(Message.STATUS_DONE);
        message2.setSenderId(user1.getId());
        message2.setReceiverId(user.getId());
        message2.setUnread(true);



//        Group group = new Group();
//        group.setId("our group");
//        group.setName("oyyq-xxy-jlp");
//        Date date = new Date();
//        group.setModifyAt(date);
//        group.setJoinAt(date);
//        group.setDesc("we are happy~");
//
//        DbHelper.save(Group.class, group);
//        Thread.sleep(500);

//
//        GroupMember groupMember1 = new GroupMember();         //"oyyq"
//        groupMember1.setGroup(group);
//        groupMember1.setUser(user);
//        groupMember1.setMyNotifyLevel(GroupMember.NOTIFY_LEVEL_NONE);
//        groupMember1.setAdmin(true);
//        groupMember1.setOwner(true);
//        groupMember1.setModifyAt(new Date());
//
//
//        GroupMember groupMember2 = new GroupMember();         //"xxy"
//        groupMember2.setGroup(group);
//        groupMember2.setUser(user1);
//        groupMember2.setAdmin(false);
//        groupMember2.setOwner(false);
//        groupMember2.setModifyAt(new Date());
//
//
//        GroupMember groupMember3 = new GroupMember();         //"jlp"
//        groupMember3.setGroup(group);
//        groupMember3.setUser(user2);
//        groupMember3.setAdmin(false);
//        groupMember3.setOwner(false);
//        groupMember3.setModifyAt(new Date());
//
//        DbHelper.save(GroupMember.class, groupMember1, groupMember2);
//        Thread.sleep(1000);
//        DbHelper.save(GroupMember.class, groupMember3);
//        Thread.sleep(500);


//        //"oyyq" -> "our group"
//        Message message3 = new Message();
//        message3.setId("0000002");
//        message3.setContent("Hello our group");
//        message3.setCreateAt(new Date());
//        message3.setStatus(Message.STATUS_CREATED);
//        message3.setSender(user);
//        message3.setGroup(group);
//        message3.setUnread(false);


        DbHelper.save(Message.class, message1, message2);
        Thread.sleep(1000);
//        DbHelper.save(Message.class, message3);
//        Thread.sleep(1000);


//        Session sessionXXYOYYQ = SQLite.select().from(Session.class).where(Session_Table.id.eq("ses456")).querySingle();      //XXY是聊天对方
//        Message message = sessionXXYOYYQ.getMessage();      //外键查询, 只有Message.id != null, Message其他属性 == null.
//        int unReadCount = sessionXXYOYYQ.getUnReadCount();
//        String content = sessionXXYOYYQ.getContent();
//
//        Assert.assertNotNull(message);
//        Assert.assertEquals("messge id", "0000001", message.getId());
//        Assert.assertEquals("message content", "Hello oyyq", content);
//        Assert.assertEquals("unReadCount", 2, unReadCount);

//        Session sessionGroup = SQLite.select().from(Session.class).where(Session_Table.group_id.eq("our group")).querySingle();
//        Message message = sessionGroup.getMessage();
//        Assert.assertNotNull(message);


//        DbHelper.delete(Group.class, group);
//        Thread.sleep(1000);
//        sessionGroup = SQLite.select().from(Session.class).where(Session_Table.group_id.eq("our group")).querySingle();
//        Assert.assertNull(sessionGroup);
//
//        List<Message> groupMessages = SQLite.select().from(Message.class).where(Message_Table.group_id.eq("our group")).queryList();
//        Assert.assertEquals("groupMessages", 0, groupMessages.size());


        Session xxySession = SQLite.select().from(Session.class).where(Session_Table.user_id.eq(user1.getId())).querySingle();
        Assert.assertNotNull(xxySession);

        List<Message> XXYmessages = SQLite.select().from(Message.class)
                .where(Message_Table.senderId.eq(user1.getId()), Message_Table.group_id.isNull()).or(Message_Table.receiverId.eq(user1.getId())).queryList();
        Assert.assertEquals("XXYmessages", 2, XXYmessages.size());

        Assert.assertEquals("Hello oyyq", xxySession.getContent());
        Assert.assertEquals(message2.getCreateAt(), xxySession.getModifyAt());


        DbHelper.delete(User.class, user1);
        Thread.sleep(10000);
//        User user3 = SQLite.select().from(User.class).where(User_Table.id.eq(user1.getId())).querySingle();
//        Assert.assertNull(user3);


        xxySession = SQLite.select().from(Session.class).where(Session_Table.user_id.eq(user1.getId())).querySingle();
        Assert.assertNull(xxySession);

        XXYmessages = SQLite.select().from(Message.class)
                .where(Message_Table.senderId.eq(user1.getId()), Message_Table.group_id.isNull()).or(Message_Table.receiverId.eq(user1.getId())).queryList();      //返回长度 = 0的List

        Assert.assertEquals("XXYmessages", 0, XXYmessages.size());

    }


}