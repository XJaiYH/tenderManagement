package com.bit.tenderManageSystem.service;

import com.bit.tenderManageSystem.dao.LoginTicketMapper;
import com.bit.tenderManageSystem.dao.UserMapper;
import com.bit.tenderManageSystem.entity.LoginTicket;
import com.bit.tenderManageSystem.entity.User;
import com.bit.tenderManageSystem.util.ManagementUtil;
import com.bit.tenderManageSystem.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class userService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private User user;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    List<User> findAllUsers() {
        return userMapper.selectUsers();
    }

    User findUserByName(String userName){
        return userMapper.selectByName(userName);
    }

    User findUserById(int userId){
        return userMapper.selectById(userId);
    }

    int updatePassword(int userId, String pwd){
        return userMapper.updatePassword(userId, pwd);
    }

    int updateUserName(int userId, String userName){
        return userMapper.updateUserName(userId, userName);
    }

    int updateStatus(int userId, int status){
        return userMapper.updateStatus(userId, status);
    }

    int updatePhone(int userId, String phone){
        return userMapper.updatePhone(userId, phone);
    }

    int insertUser(User user){
        return userMapper.insertUser(user);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUserName())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPhone())) {
            map.put("phoneMsg", "电话号码不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUserName());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 注册用户
        user.setSalt(ManagementUtil.generateUUID().substring(0, 5));
        user.setPassword(ManagementUtil.md5(user.getPassword() + user.getSalt()));
        user.setStatus(0);
        userMapper.insertUser(user);

        return map;
    }

    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证密码
        password = ManagementUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getUserId());
        loginTicket.setTicket(ManagementUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        //loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        userMapper.updateLastLoginTime(user.getUserId(), new Date());

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

}
