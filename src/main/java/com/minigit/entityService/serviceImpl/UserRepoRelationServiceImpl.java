package com.minigit.entityService.serviceImpl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minigit.entity.UserRepoRelation;
import com.minigit.entityService.UserRepoRelationService;
import com.minigit.mapper.UserRepoRelationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserRepoRelationServiceImpl extends ServiceImpl<UserRepoRelationMapper, UserRepoRelation> implements UserRepoRelationService {
}
