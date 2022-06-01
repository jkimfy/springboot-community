package com.foo.community.util;

import com.foo.community.entity.User;
import org.springframework.stereotype.Component;


/**
 *持有用户信息
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    //清理用户信息
    public void clear(){
        users.remove();
    }
}
