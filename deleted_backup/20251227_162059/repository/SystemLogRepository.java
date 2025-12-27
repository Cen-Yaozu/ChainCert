package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.model.entity.SystemLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志Repository接口
 */
@Mapper
public interface SystemLogRepository extends BaseMapper<SystemLog> {
}
