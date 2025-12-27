package com.blockchain.certificate.domain.approval.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.domain.approval.model.Approval;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批记录Repository接口
 */
@Mapper
public interface ApprovalRepository extends BaseMapper<Approval> {
}
