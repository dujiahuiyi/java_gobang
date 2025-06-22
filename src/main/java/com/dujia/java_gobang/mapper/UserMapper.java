package com.dujia.java_gobang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dujia.java_gobang.model.dataobject.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE user set totalCount = totalCount + 1, winCount = winCount + 1, score = score + 30 where userId = #{winnerId}")
    void updateWinner(int winnerId);

    @Update("update user set totalCount = totalCount + 1, score = score - 30 where userId = #{loserId}")
    void updateLoser(int loserId);
}
