package com.dujia.java_gobang.model.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dujia.java_gobang.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("user")
public class User {
    @TableId(value = "userId", type = IdType.AUTO)
    private Integer userId;
    @TableField("username")
    private String username;
    private String password;
    private int score = Constants.SCORE;
    @TableField("totalCount")
    private int totalCount;
    @TableField("winCount")
    private int winCount;
}
