package com.foo.community.service;

import com.foo.community.dao.CommentMapper;
import com.foo.community.entity.Comment;
import com.foo.community.util.CommunityConstant;
import com.foo.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Resource
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType,int entityId,int offset,int limit) {
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    public int findCommentCount(int entityType,int entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED) //隔离级别，传播机制
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //需要敏感词过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //更新帖子评论数量
        if (comment.getEntityType()== ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(ENTITY_TYPE_POST, comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }
}
