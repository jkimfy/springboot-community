package com.foo.community.controller;

import com.foo.community.entity.DiscussPost;
import com.foo.community.entity.Page;
import com.foo.community.entity.User;
import com.foo.community.service.DiscussPostService;
import com.foo.community.service.LikeService;
import com.foo.community.service.UserService;
import com.foo.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Resource
    private UserService userService;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private LikeService likeService;

    @GetMapping("/index")
    public String getIndexPage(Model model,Page page) {
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index"); //每次查询的时候就会加上这个路径
        List<Map<String,Object>> discussPostList = new ArrayList<>();

        List<DiscussPost> list = discussPostService.findDiscussPost(0, page.getOffset(), page.getLimit());

        //根据帖子id查询用户id,再根据用户id查询用户,建立多个map，一个map存一个user和一个帖子，让后将多个map用list存起来
        if(list != null) {
            for(DiscussPost discussPost: list) {
                Map<String,Object> map = new HashMap<>();
                map.put("discussPost",discussPost);
                int userId = discussPost.getUserId();
                User user = userService.findUserByUserId(userId);
                map.put("user",user);

                //查询赞的数量
                Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount",likeCount);
                discussPostList.add(map);
            }

        }
        model.addAttribute("discussPostList",discussPostList);

        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }
}
