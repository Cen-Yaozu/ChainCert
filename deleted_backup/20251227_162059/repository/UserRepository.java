package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.domain.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户Repository接口
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    /**
     * 根据ID查询用户（兼容方法）
     */
    default Optional<User> findById(String id) {
        return Optional.ofNullable(selectById(id));
    }
    
    /**
     * 保存用户（兼容方法）
     */
    default void save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            insert(user);
        } else {
            updateById(user);
        }
    }
    
    /**
     * 分页查询（兼容方法）
     * 注意：这是一个默认方法，实际调用的是MyBatis Plus的selectPage
     */
    default <E extends com.baomidou.mybatisplus.core.metadata.IPage<User>> E page(
            E page,
            com.baomidou.mybatisplus.core.conditions.Wrapper<User> queryWrapper) {
        return selectPage(page, queryWrapper);
    }
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted = 0")
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM t_user WHERE username = #{username} AND deleted = 0")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * 检查学号/工号是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM t_user WHERE student_no = #{studentId} AND deleted = 0")
    boolean existsByStudentId(@Param("studentId") String studentId);
    
    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM t_user WHERE email = #{email} AND deleted = 0")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * 统计指定角色的用户数量
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE role = #{role} AND deleted = 0")
    long countByRole(@Param("role") String role);
    
    /**
     * 统计指定角色且启用状态的用户数量
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE role = #{role} AND enabled = #{enabled} AND deleted = 0")
    long countByRoleAndEnabled(@Param("role") String role, @Param("enabled") Boolean enabled);
}
