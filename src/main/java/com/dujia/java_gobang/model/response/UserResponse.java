package com.dujia.java_gobang.model.response;

import com.dujia.java_gobang.model.dataobject.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserResponse {
    private int userId;
    private String username;
    private int score;
    private int totalCount;
    private int winCount;

    public static UserResponse fromUser(User user) {
        UserResponse userResponse = new UserResponse();
        if (user == null) {
            return userResponse;
        }
        BeanUtils.copyProperties(user, userResponse);
        return userResponse;
    }
}
