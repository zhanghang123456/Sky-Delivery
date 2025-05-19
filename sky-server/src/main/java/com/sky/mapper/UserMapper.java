package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.sky.entity.User;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);

    /**
     * 新增用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据用户id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 用户统计
     * @param map
     * @return
     */
    Integer sumByMap(Map<String, Object> map);
}
