package com.foo.community.controller;

import com.foo.community.entity.Comment;
import com.foo.community.entity.DiscussPost;
import com.foo.community.entity.Page;
import com.foo.community.entity.User;
import com.foo.community.service.CommentService;
import com.foo.community.service.DiscussPostService;
import com.foo.community.service.LikeService;
import com.foo.community.service.UserService;
import com.foo.community.util.CommunityConstant;
import com.foo.community.util.CommunityUtil;
import com.foo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private LikeService likeService;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403,"你还没有登录");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        //报错的情况统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    //帖子详情
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
//        作者
        User user = userService.findUserByUserId(post.getUserId());
        model.addAttribute("user",user);

        //点赞
        Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUser()==null?0:
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //评论VO列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        for (Comment comment : commentList) {
            Map<String,Object> commentVo = new HashMap<>();
            //评论
            commentVo.put("comment",comment);
            //作者
            commentVo.put("user",userService.findUserByUserId(comment.getUserId()));
            //点赞
            likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("likeCount",likeCount);
            //点赞状态
            likeStatus = hostHolder.getUser()==null?0:
                    likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("likeStatus",likeStatus);

            //评论的评论,也就是回复列表,不分页
            List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
            //用来存储展示回复的List,有Vo，给前端用的
            List<Map<String,Object>> replyVoList = new ArrayList<>();
            if (replyList != null) {
                for (Comment reply: replyList) {
                    Map<String,Object> replyVo = new HashMap<>();
                    //回复
                    replyVo.put("reply",reply);
                    //作者
                    replyVo.put("user",userService.findUserByUserId(reply.getUserId()));
                    //点赞
                    likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                    replyVo.put("likeCount",likeCount);
                    //点赞状态
                    likeStatus = hostHolder.getUser()==null?0:
                            likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                    replyVo.put("likeStatus",likeStatus);
                    //回复目标
                    User target = reply.getTargetId() == 0 ? null : userService.findUserByUserId(reply.getTargetId());
                    replyVo.put("target",target);
                    replyVoList.add(replyVo);
                }
            }
            commentVo.put("replys",replyVoList);

            //回复数量
            int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("replyCount",replyCount);

            commentVoList.add(commentVo);
        }

        model.addAttribute("comments",commentVoList);
        return "site/discuss-detail";
    }


}
