package com.dujia.java_gobang.common.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SecurityUtils {

    /**
     * 生成密码
     *
     * @param password 用户密码
     * @return 数据库密码
     */
    public static String encrypt(String password) {
        String salt = String.valueOf(UUID.randomUUID()).replace("-", ""); // 盐值
        String encryptedPassword = DigestUtils.md5DigestAsHex((salt + password).getBytes(StandardCharsets.UTF_8)); // 加密后的密码
        return salt + encryptedPassword;
    }

    /**
     * 校验密码
     *
     * @param password     用户密码
     * @param dataPassword 数据库密码
     * @return true - 正确   false - 失败
     */
    public static Boolean verify(String password, String dataPassword) {
        String salt = dataPassword.substring(0, 32);
        String curPassword = DigestUtils.md5DigestAsHex((salt + password).getBytes(StandardCharsets.UTF_8));
        return dataPassword.equals(salt + curPassword);
    }
}
