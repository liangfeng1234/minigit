package com.minigit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minigit.entity.UserRepoRelation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepoRelationMapper extends BaseMapper<UserRepoRelation> {
}
