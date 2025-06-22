package com.dujia.java_gobang.api;

import com.dujia.java_gobang.constant.Constants;
import com.dujia.java_gobang.model.dataobject.User;
import com.dujia.java_gobang.model.request.LoginOrRegisterRequest;
import com.dujia.java_gobang.model.result.HttpResult;
import com.dujia.java_gobang.model.response.UserResponse;
import com.dujia.java_gobang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Slf4j
@RequestMapping()
@RestController
public class UserAPI {

    @Resource(name = Constants.USER_SERVICE)
    private UserService userService;

    @RequestMapping(Constants.LOGIN)
    public HttpResult<UserResponse> login(@Valid LoginOrRegisterRequest request, HttpSession session) {
        log.info("用户{}登录", request.getUsername());
        User loginUser = userService.normalLogin(request.getUsername(), request.getPassword());
        if (loginUser == null) {
            return HttpResult.fail("用户名或密码错误", new UserResponse());
        }
        session.setAttribute(Constants.SESSION_KEY, loginUser);
        return HttpResult.success(UserResponse.fromUser(loginUser));
    }

    @RequestMapping(Constants.REGISTER)
    public HttpResult<UserResponse> register(@Valid LoginOrRegisterRequest request, HttpSession session) throws Exception {
        log.info("用户{}注册", request.getUsername());
        User registerUser = userService.normalRegister(request.getUsername(), request.getPassword());
        if (registerUser == null) {
            return HttpResult.fail("用户名已存在", new UserResponse());
        }
        session.setAttribute(Constants.SESSION_KEY, registerUser);
        return HttpResult.success(UserResponse.fromUser(registerUser));
    }

    @RequestMapping(Constants.USERINFO)
    public HttpResult<UserResponse> getUserInfo(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User curUser = (User) session.getAttribute(Constants.SESSION_KEY);
        User user = userService.normalGetUserInfo(curUser);
        if (user == null) {
            return HttpResult.fail("未找到用户", new UserResponse());
        }
        return HttpResult.success(UserResponse.fromUser(user));
    }
}
