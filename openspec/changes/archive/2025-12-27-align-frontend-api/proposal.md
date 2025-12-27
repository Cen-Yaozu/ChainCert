# Change: 前端 API 对齐后端接口

## Why
前端 API 调用与后端 Controller 接口存在大量不匹配，导致前后端无法正常通信。需要修改前端 API 文件以匹配后端实际接口。

## What Changes
### 1. 认证模块 (auth.ts)
- 修改 `logout` 方法：移除 refreshToken 参数（后端不需要）
- 修改 `refreshToken` 返回类型：从 `LoginResponse` 改为 `{token: string}`

### 2. 用户模块 (user.ts)
- 修改 `getUserList` 参数：将 `username`, `realName` 改为 `keyword`
- 修改 `resetPassword` 方法：从 body 改为 query param
- 修改 `toggleUserStatus` 方法：添加 `enabled` 参数
- 修改 `batchImport` 方法：从 FormData 改为 JSON 数组

### 3. 申请模块 (application.ts)
- 删除 `getMyApplications` 方法（后端没有此接口，使用 getList 替代）
- 修改 `create` 方法的字段名以匹配后端 `ApplicationRequest`

### 4. 审批模块 (approval.ts)
- 修改角色检查：从 `COLLEGE_ADMIN`/`SCHOOL_ADMIN` 改为 `COLLEGE_TEACHER`/`SCHOOL_TEACHER`
- 修改 `getApprovalHistory` 返回类型：从数组改为分页结果

### 5. 证书模块 (certificate.ts)
- 修改 `revoke` 方法：从 body 改为 query param
- 修改 `verify` 方法：从 body 改为 query param

### 6. 个人中心模块 (user.ts - profileApi)
- 修改 `uploadAvatar` 方法：从 POST + FormData 改为 PUT + query param

### 7. 管理员模块 (admin.ts, college.ts, template.ts, log.ts)
- 修改 `getMajorsByCollege` 路径：从 `/college/` 改为 `/by-college/`
- 修改 `assignApprovers` 方法：从多个审批人改为单个
- 删除 `enable`/`disable`/`setDefault`/`getByType` 方法（后端没有）
- 修改 `cleanup` 路径：从 `/cleanup` 改为 `/clean`
- 删除 `exportLogs` 方法（后端没有）
- 修改统计接口参数：从 `startDate`/`endDate` 改为 `startTime`/`endTime`
- 修改 `getCollegeStats` 路径：从 query param 改为 path param

### 8. 类型定义更新 (types/)
- 更新角色枚举：添加 `COLLEGE_TEACHER`/`SCHOOL_TEACHER`
- 更新相关接口类型定义

## Impact
- Affected specs: 无（这是修复性变更）
- Affected code: 
  - `frontend/src/api/*.ts` - 所有 API 文件
  - `frontend/src/types/*.ts` - 类型定义文件
  - `frontend/src/views/**/*.vue` - 使用这些 API 的视图组件
  - `frontend/src/stores/*.ts` - 使用这些 API 的状态管理