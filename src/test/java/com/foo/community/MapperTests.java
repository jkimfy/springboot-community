package com.foo.community;

import com.foo.community.dao.DiscussPostMapper;
import com.foo.community.dao.LoginTicketMapper;
import com.foo.community.dao.MessageMapper;
import com.foo.community.dao.UserMapper;
import com.foo.community.entity.DiscussPost;
import com.foo.community.entity.LoginTicket;
import com.foo.community.entity.Message;
import com.foo.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Resource
    private UserMapper userMapper;

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testUserMapper() {
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void insertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setCreateTime(new Date());
        int row = userMapper.insert(user);
        System.out.println(row);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdate() {
        int i = userMapper.updatePassword(150, "1234560");
        System.out.println(i);
    }

    @Test
    public void testSelectDiscussPost() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost dp : discussPosts) {
            System.out.println(dp);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*60*10));

        int i = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(i);


    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket lt = loginTicketMapper.selectByTicket("abc");
        System.out.println(lt);

        loginTicketMapper.updateStatus("abc", 1);
        lt = loginTicketMapper.selectByTicket("abc");
        System.out.println(lt);
    }

    @Test
    public void testSelect(){
//        List<Message> messagesList = messageMapper.selectConversations(111, 0, 10);
//        for (Message message: messagesList) {
//            System.out.println(message);
//        }

//        int count = messageMapper.selectConversationCount(111);
//        System.out.println(count);

//        List<Message> messageList = messageMapper.selectLetters("111-112", 0,10);
//        for (Message message:messageList) {
//            System.out.println(message);
//        }

//        int letterCount = messageMapper.selectLetterCount("111_112");
//        System.out.println(letterCount);

        int unreadCount = messageMapper.selectLetterUnreadCount(111, "111_131");
        System.out.println(unreadCount);
    }

}
