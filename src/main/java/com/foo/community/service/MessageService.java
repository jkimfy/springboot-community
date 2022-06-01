package com.foo.community.service;

import com.foo.community.dao.MessageMapper;
import com.foo.community.entity.Message;
import com.foo.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService {
    @Resource
    private MessageMapper messageMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId,int offset,int limit){
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit){
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }
    //添加一条消息，发消息
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    //将消息改为已读
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids,1);
    }
}
