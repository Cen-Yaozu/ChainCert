package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.model.entity.Application;
import org.apache.ibatis.annotations.Mapper;

/**
 * 申请Repository接口
 */
@Mapper
public interface ApplicationRepository extends BaseMapper<Application> {
}
