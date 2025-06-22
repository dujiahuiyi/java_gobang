package com.dujia.java_gobang.service;

import com.dujia.java_gobang.model.dataobject.User;

import javax.validation.constraints.NotBlank;

public interface UserService {

    User normalLogin(@NotBlank String username, @NotBlank String password);

    User normalRegister(@NotBlank String username, @NotBlank String password) throws Exception;

    User normalGetUserInfo(User curUser);

    User getUserById(int userId);

    User getUserByName(String username);
}
