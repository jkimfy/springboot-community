package com.foo.community.dao;

import com.foo.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByUserName(String username);

    List<User> selectByEmail(String email);

    int insert(User user);

    int updateHeader(int id,String headerUrl);

    int updatePassword(int id, String password);

    int updateStatus(int id, int status);

}
