package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.model.entity.Approval;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批记录Repository接口
 */
@Mapper
public interface ApprovalRepository extends BaseMapper<Approval> {
}
