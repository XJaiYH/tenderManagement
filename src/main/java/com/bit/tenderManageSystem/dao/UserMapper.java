package com.bit.tenderManageSystem.dao;


import com.bit.tenderManageSystem.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 根据id\用户名查询用户
     */
    User selectByName(String userName);

    User selectById(int userId);

    /**
    管理员查询全部user
     */
    List<User> selectUsers();

    /**
    更新
     */
    int updatePassword(int userId, String pwd);

    int updatePhone(int userId, String phone);

    int updateUserName(int userId, String userName);

    int updateStatus(int userId, int status);

    int updateLastLoginTime(int userId, Date date);

    /**
    插入
     */
    int insertUser(User user);
}
