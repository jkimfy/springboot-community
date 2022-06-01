package com.foo.community;

import com.foo.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TestSensitive {

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testFilter(){
        String text = "这里可以赌博，嫖娼，吸毒";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以**赌**博，**嫖**娼，吸**毒";
        System.out.println(text);

    }
}
