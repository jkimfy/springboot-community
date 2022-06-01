package com.foo.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String salt;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;

    private User user;

}
