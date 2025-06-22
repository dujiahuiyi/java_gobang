package com.dujia.java_gobang.service.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dujia.java_gobang.common.utils.SecurityUtils;
import com.dujia.java_gobang.constant.Constants;
import com.dujia.java_gobang.mapper.UserMapper;
import com.dujia.java_gobang.model.dataobject.User;
import com.dujia.java_gobang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service(Constants.USER_SERVICE)
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public User normalLogin(String username, String password) {
        User loginUser = getUserByName(username);
        if (loginUser == null) {
            log.error("[normalLogin] 未找到用户");
            return null;
        }
        if (!SecurityUtils.verify(password, loginUser.getPassword())) {
            log.info("[normalLogin] 用户密码错误");
            return null;
        }
        return loginUser;
    }

    @Override
    public User normalRegister(String username, String password) throws Exception {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        if (userMapper.selectCount(queryWrapper) > 0) {
            log.info("[normalRegister] 用户名已存在");
            return null;
        }

        User user = new User();
        user.setUsername(username);
        val encrypt = SecurityUtils.encrypt(password);
        user.setPassword(encrypt);
        int result = insertUser(user);
        if (result < 0) {
            log.info("[normalRegister] 注册失败");
            throw new Exception("数据库插入失败");
        }
        return user;
    }

    @Override
    public User normalGetUserInfo(User curUser) {
        User user = getUserById(curUser.getUserId());
        if (user == null) {
            log.info("[normalGetUserInfo] 未找到用户");
            return null;
        }
        return user;
    }

    @Override
    public User getUserById(int userId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User getUserByName(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    private Integer insertUser(User user) {
        return userMapper.insert(user);
    }


}
