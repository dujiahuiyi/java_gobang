package com.dujia.java_gobang.model.response;

import lombok.Data;

@Data
public class MatchResponse {
    private boolean ok;  // 是否匹配成功
    private String reason; // 错误原因
    private String message; // 返回的具体字符串，相当于data
}
