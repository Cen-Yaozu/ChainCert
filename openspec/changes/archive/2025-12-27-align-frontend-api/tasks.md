# 前端 API 对齐任务清单

## 1. 认证模块 (auth.ts)
- [x] 1.1 修改 `logout` 方法，移除 refreshToken 参数
- [x] 1.2 修改 `refreshToken` 返回类型为 `{token: string}`

## 2. 用户模块 (user.ts)
- [x] 2.1 修改 `getUserList` 参数，将 `username`, `realName` 改为 `keyword`
- [x] 2.2 修改 `resetPassword` 方法，使用 query param 传递 newPassword
- [x] 2.3 修改 `toggleUserStatus` 方法，添加 `enabled` 参数
- [x] 2.4 修改 `batchImport` 方法，从 FormData 改为 JSON 数组

## 3. 个人中心模块 (user.ts - profileApi)
- [x] 3.1 修改 `uploadAvatar` 方法，从 POST + FormData 改为 PUT + query param

## 4. 申请模块 (application.ts)
- [x] 4.1 删除 `getMyApplications` 方法（后端会根据角色自动过滤）
- [x] 4.2 修改 `create` 方法的字段名匹配后端 (title, certificateType, collegeId, majorId, description, proofFiles)

## 5. 审批模块 (approval.ts)
- [x] 5.1 修改 `getApprovalHistory` 返回类型为分页结果
- [x] 5.2 添加分页参数支持
- [x] 5.3 添加 `getAllApprovals` 方法

## 6. 证书模块 (certificate.ts)
- [x] 6.1 修改 `revoke` 方法，使用 query param 传递 reason
- [x] 6.2 修改 `verify` 方法，使用 query param 传递 certificateNo
- [x] 6.3 添加 `verificationApi` 导出

## 7. 管理员模块 - 学院管理 (admin.ts, college.ts)
- [x] 7.1 修改 `getMajorsByCollege` 路径为 `/admin/majors/by-college/{collegeId}`
- [x] 7.2 修改 `assignApprovers` 方法为单个审批人 `assignApprover`，使用 query param

## 8. 管理员模块 - 模板管理 (admin.ts, template.ts)
- [x] 8.1 删除 `enable` 方法
- [x] 8.2 删除 `disable` 方法
- [x] 8.3 删除 `setDefault` 方法
- [x] 8.4 删除 `getByType` 方法
- [x] 8.5 修改 `toggleTemplateStatus` 方法，使用 query param 传递 enabled

## 9. 管理员模块 - 日志管理 (admin.ts, log.ts)
- [x] 9.1 修改 `cleanup`/`cleanLogs` 路径为 `/admin/logs/clean`
- [x] 9.2 删除 `exportLogs` 方法（后端不支持）
- [x] 9.3 修改参数名 `retentionDays` 匹配后端

## 10. 管理员模块 - 统计 (admin.ts)
- [x] 10.1 修改 `getCollegeStats` 路径为 `/admin/statistics/college/{collegeId}`
- [x] 10.2 修改 `getTimeRangeStats` 参数名为 `startTime`/`endTime`

## 11. 类型定义更新 (types/)
- [x] 11.1 更新 `UserRole` 枚举，使用 `COLLEGE_TEACHER`/`SCHOOL_TEACHER`
- [x] 11.2 更新 `ApplicationRequest` 类型定义
- [x] 11.3 更新 `ApprovalResponse` 类型定义
- [x] 11.4 更新分页相关类型定义
- [x] 11.5 更新所有 ID 类型从 number 改为 string

## 12. 视图组件更新
- [x] 12.1 更新 SystemLog.vue - 移除导出日志功能
- [x] 12.2 更新 CollegeManagement.vue - 角色名改为 COLLEGE_TEACHER
- [x] 12.3 更新 UserManagement.vue - 角色名改为 COLLEGE_TEACHER/SCHOOL_TEACHER
- [x] 12.4 更新 Profile.vue - 角色名改为 COLLEGE_TEACHER/SCHOOL_TEACHER
- [x] 12.5 更新 Dashboard.vue - 角色名改为 COLLEGE_TEACHER/SCHOOL_TEACHER

## 13. 测试验证
- [ ] 13.1 验证登录/登出流程
- [ ] 13.2 验证用户管理功能
- [ ] 13.3 验证申请/审批流程
- [ ] 13.4 验证证书管理功能
- [ ] 13.5 验证管理员功能

---

## 完成情况

**已完成**: 12/13 个模块
**待测试**: 1 个模块（需要运行项目进行集成测试）

### 主要变更总结

1. **API 路径和参数对齐**: 所有前端 API 调用现在与后端 Controller 接口一致
2. **角色名称统一**: 使用 `COLLEGE_TEACHER`/`SCHOOL_TEACHER` 替代 `COLLEGE_ADMIN`/`SCHOOL_ADMIN`
3. **ID 类型统一**: 所有 ID 从 `number` 改为 `string` 以匹配后端
4. **移除不支持的功能**: 删除了后端不支持的 API 方法（如 exportLogs, getMyApplications 等）
5. **参数传递方式**: 部分方法从 request body 改为 query params