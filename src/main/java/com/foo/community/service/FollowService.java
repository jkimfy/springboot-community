package com.foo.community.service;

import com.foo.community.entity.User;
import com.foo.community.util.CommunityConstant;
import com.foo.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

//关注业务
@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //关注
    public void follow(int userId,int entityType,int entityId) {
        //保证事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweekey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();

                redisOperations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }

    //取消关注
    public void unfollow(int userId,int entityType,int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweekey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();
                redisOperations.opsForZSet().remove(followeeKey,entityId);
                redisOperations.opsForZSet().remove(followerKey,userId);
                return redisOperations.exec();
            }
        });
    }

    //查询我关注的人的数量
    public long findFolloweeCount(int userId,int entityType) {
        String followeekey = RedisKeyUtil.getFolloweekey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeekey);
    }

    //查询我的粉丝数量
    public long findFollowerCount(int entityType,int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否已关注实体
    public boolean hasFollowed(int userId,int entityType,int entityId) {
        String followeekey = RedisKeyUtil.getFolloweekey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeekey,entityId) != null;
    }

    //查询用户关注的人
    public List<Map<String,Object>> findFollowees(int userId, int offset, int limit) {
        //拼key
        String followeeKey = RedisKeyUtil.getFolloweekey(userId, ENTITY_TYPE_USER);
        //查询用户id集合
        Set<Integer> targetIds= redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String,Object> map = new HashMap<>();
            //查用户
            User user = userService.findUserByUserId(targetId);
            map.put("user",user);
            //查分数
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));//longValue:转成long类型
            list.add(map);
        }
        return list;
    }

    //查询用户的粉丝
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey,offset,offset+limit);//起始索引和结束索引
        if (targetIds == null) {
            return null;
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String,Object> map = new HashMap<>();
            //查用户
            User user = userService.findUserByUserId(targetId);
            map.put("user",user);
            //查分数
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));//longValue:转成long类型
            list.add(map);
        }
        return list;
    }
}
