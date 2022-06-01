package com.foo.community.controller;

import com.foo.community.entity.Message;
import com.foo.community.entity.Page;
import com.foo.community.entity.User;
import com.foo.community.service.MessageService;
import com.foo.community.service.UserService;
import com.foo.community.util.CommunityUtil;
import com.foo.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {
    @Resource
    private MessageService messageService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    //私信列表，消息页面
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());

        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null) {
            for(Message message: conversationList) {
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));

                int targetId = user.getId() == message.getFromId()?message.getToId():message.getFromId();
                map.put("target",userService.findUserByUserId(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("leeterUnreadCount",letterUnreadCount);
        return "site/letter";
    }

    //私信详情
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page, Model model) {
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message:letterList) {
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserByUserId(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //设置消息已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    //获取消息目标对象
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserByUserId(id1);
        } else {
            return userService.findUserByUserId(id0);
        }
    }

    //未读消息的id
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() ==0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }
    //发私信,异步
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName,String content) {
        User target = userService.findUserByUserName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

}
