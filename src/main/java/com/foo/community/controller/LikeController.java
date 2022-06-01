package com.foo.community.controller;

import com.foo.community.entity.User;
import com.foo.community.service.LikeService;
import com.foo.community.util.CommunityUtil;
import com.foo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    //处理点赞请求
    @PostMapping("/like")
    @ResponseBody //异步请求
    public String like(int entityType,int entityId,int entityUserId) {
        User user = hostHolder.getUser();

        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);

        //数量
        Long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String,Object> map = new HashMap<>();

        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        return CommunityUtil.getJSONString(0,null,map);
    }
}
