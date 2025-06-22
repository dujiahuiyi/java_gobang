package com.dujia.java_gobang.model.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MatchResult {
    private boolean success;
    private String message;
}
