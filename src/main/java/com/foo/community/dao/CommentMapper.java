package com.foo.community.dao;

import com.foo.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    //根据评论对象的类型，id，起始行，每页条数
    List<Comment> selectCommentByEntity(int entityType,int entityId,int offset,int limit);

    //评论数量
    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);
}
