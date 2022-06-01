package com.foo.community;

import com.foo.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Test
    public void testMail() {
        mailClient.sendMail("2430481949@qq.com","TEST","Welcome");
    }

    @Test
    public void TestHtmlMail() {
        Context context = new Context();
        context.setVariable("username","jklmfy");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("jklmfy@qq.com","HtmlTest",content);
    }
}

