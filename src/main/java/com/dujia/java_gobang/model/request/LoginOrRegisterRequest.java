package com.dujia.java_gobang.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginOrRegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
