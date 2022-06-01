package com.foo.community.service;

import com.foo.community.dao.DiscussPostMapper;
import com.foo.community.entity.DiscussPost;
import com.foo.community.util.SensitiveFilter;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DiscussPostService {

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPost(int userId,int offset,int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    //增加帖子
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //将titile里的标签进行转义，使用HtmlUtils的htmlEscape方法
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    //帖子详情
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    //更新评论数量
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id,commentCount);
    }


}
