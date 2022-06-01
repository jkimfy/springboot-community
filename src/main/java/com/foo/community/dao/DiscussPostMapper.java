package com.foo.community.dao;

import com.foo.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //分页查询 offset：每一页起始位置  limit：每一页的条数
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit); //在首页上不会传入userId,在个人中心有个我的贴子需要用户id

    //想要知道页码，就需要知道这个表里一共有多少数据
    int selectDiscussPostRows(@Param("userId") int userId); //这个userId的作用也是在个人首页的时候起作用的

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    //添加评论的时候帖子表需要更新评论数量
    int updateCommentCount(int id,int commentCount);


}
