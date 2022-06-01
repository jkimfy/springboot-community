package com.foo.community.dao;

import com.foo.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户会话列表，针对每个会话返回一条最新的私信，支持分页
    List<Message> selectConversations(int userId,int offset,int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话的私信列表,需要的会话id，支持分页
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    int selectLetterUnreadCount(int userId,String conversationId);


    int insertMessage(Message message);

    int updateStatus(List<Integer> ids,int status);

}
