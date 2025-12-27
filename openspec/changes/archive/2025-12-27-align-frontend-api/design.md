# 前端 API 对齐 - 技术设计文档

## Context
前端 API 调用与后端 Controller 接口存在大量不匹配，这是项目开发过程中前后端独立开发导致的问题。需要系统性地修复这些不匹配，确保前后端能够正常通信。

### 约束条件
- 后端 API 已经稳定，不做修改
- 前端已配置 `baseURL: '/api'`，路径前缀问题不存在
- 需要保持向后兼容，避免破坏现有功能

## Goals / Non-Goals

### Goals
- 修复所有前端 API 调用与后端接口的不匹配
- 更新相关类型定义
- 确保所有功能正常工作

### Non-Goals
- 不修改后端 API
- 不添加新功能
- 不重构前端架构

## Decisions

### Decision 1: 修改策略 - 前端适配后端
**选择**: 修改前端 API 文件以匹配后端接口
**原因**: 
- 后端可能已有其他系统在调用
- 后端 API 设计更符合 RESTful 规范
- 修改前端成本更低

### Decision 2: 角色命名 - 保持后端命名
**选择**: 使用后端的 `COLLEGE_TEACHER`/`SCHOOL_TEACHER` 命名
**原因**:
- 后端数据库和安全配置已使用此命名
- 修改后端涉及数据库迁移，风险更高

### Decision 3: 参数传递方式 - 遵循后端设计
**选择**: 按后端要求使用 query param 或 body
**原因**:
- 后端已实现，修改前端更简单
- 保持一致性

### Decision 4: 缺失接口处理
**选择**: 删除前端调用，或使用替代接口
**原因**:
- `/applications/my` → 使用 `/applications` + 后端自动过滤
- 模板 enable/disable → 使用 `/templates/{id}/status?enabled=true/false`
- 日志导出 → 暂时移除功能

## 详细修改方案

### 1. auth.ts 修改

```typescript
// 修改前
logout(refreshToken: string): Promise<void> {
  return request.post('/auth/logout', { refreshToken })
}

// 修改后
logout(): Promise<void> {
  return request.post('/auth/logout')
}

// 修改前
refreshToken(data: RefreshTokenRequest): Promise<LoginResponse> {
  return request.post('/auth/refresh', data)
}

// 修改后
refreshToken(data: RefreshTokenRequest): Promise<{ token: string }> {
  return request.post('/auth/refresh', data)
}
```

### 2. user.ts 修改

```typescript
// 修改前
getUserList(params: {
  page: number
  size: number
  username?: string
  realName?: string
  role?: string
  enabled?: boolean
})

// 修改后
getUserList(params: {
  page: number
  size: number
  keyword?: string  // 合并 username 和 realName
  role?: string
  collegeId?: string
})

// 修改前
resetPassword(id: number, data: PasswordResetRequest) {
  return request.put<void>(`/users/${id}/reset-password`, data)
}

// 修改后
resetPassword(id: number, newPassword: string) {
  return request.put<void>(`/users/${id}/reset-password`, null, {
    params: { newPassword }
  })
}

// 修改前
toggleUserStatus(id: number) {
  return request.put<void>(`/users/${id}/status`)
}

// 修改后
toggleUserStatus(id: number, enabled: boolean) {
  return request.put<void>(`/users/${id}/status`, null, {
    params: { enabled }
  })
}

// 修改前
batchImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/users/batch-import', formData, {...})
}

// 修改后
batchImport(users: UserRequest[]) {
  return request.post<void>('/users/batch-import', users)
}
```

### 3. profileApi 修改

```typescript
// 修改前
uploadAvatar(file: File): Promise<{ avatarUrl: string }> {
  const formData = new FormData()
  formData.append('avatar', file)
  return request.upload('/profile/avatar', formData)
}

// 修改后
updateAvatar(avatarUrl: string): Promise<void> {
  return request.put('/profile/avatar', null, {
    params: { avatarUrl }
  })
}
```

### 4. application.ts 修改

```typescript
// 删除 getMyApplications 方法
// 后端会根据用户角色自动过滤

// 修改 create 方法的字段名
create(data: ApplicationRequest): Promise<Application> {
  const formData = new FormData()
  formData.append('title', data.title)
  formData.append('certificateType', data.certificateType)
  formData.append('collegeId', data.collegeId)
  if (data.majorId) formData.append('majorId', data.majorId)
  if (data.description) formData.append('description', data.description)
  data.proofFiles?.forEach(file => {
    formData.append('proofFiles', file)
  })
  return request.upload('/applications', formData)
}
```

### 5. approval.ts 修改

```typescript
// 修改返回类型
getApprovalHistory(applicationId: number, params?: PageQuery): Promise<PageResponse<ApprovalHistoryItem>> {
  return request.get(`/approvals/history/${applicationId}`, { params })
}
```

### 6. certificate.ts 修改

```typescript
// 修改前
revoke(id: number, reason: string): Promise<void> {
  return request.put(`/certificates/${id}/revoke`, { reason })
}

// 修改后
revoke(id: number, reason?: string): Promise<void> {
  return request.put(`/certificates/${id}/revoke`, null, {
    params: { reason }
  })
}

// 修改前
verify(data: VerificationRequest): Promise<VerificationResult> {
  return request.post('/verification/verify', data)
}

// 修改后
verify(certificateNo: string): Promise<VerificationResult> {
  return request.post('/verification/verify', null, {
    params: { certificateNo }
  })
}
```

### 7. admin.ts / college.ts 修改

```typescript
// 修改路径
getMajorsByCollege(collegeId: number): Promise<Major[]> {
  return request.get(`/admin/majors/by-college/${collegeId}`)
}

// 修改为单个审批人
assignApprover(collegeId: number, approverId: number): Promise<void> {
  return request.put(`/admin/colleges/${collegeId}/approver`, null, {
    params: { approverId }
  })
}
```

### 8. template.ts 修改

```typescript
// 删除 enable, disable, setDefault, getByType 方法

// 修改 toggleTemplateStatus
toggleTemplateStatus(id: number, enabled: boolean): Promise<void> {
  return request.put(`/admin/templates/${id}/status`, null, {
    params: { enabled }
  })
}
```

### 9. log.ts 修改

```typescript
// 修改路径
cleanLogs(retentionDays: number): Promise<number> {
  return request.delete('/admin/logs/clean', {
    params: { retentionDays }
  })
}

// 删除 exportLogs 方法
```

### 10. 统计接口修改

```typescript
// 修改路径
getCollegeStats(collegeId: number): Promise<Statistics> {
  return request.get(`/admin/statistics/college/${collegeId}`)
}

// 修改参数名
getTimeRangeStats(startTime: string, endTime: string): Promise<Statistics> {
  return request.get('/admin/statistics/date-range', {
    params: { startTime, endTime }
  })
}
```

## Risks / Trade-offs

### Risk 1: 功能缺失
- **风险**: 删除 exportLogs、模板 setDefault 等功能
- **缓解**: 这些功能后端未实现，可以后续添加

### Risk 2: 类型不兼容
- **风险**: 修改类型定义可能导致编译错误
- **缓解**: 逐步修改，确保每个文件编译通过

### Risk 3: 视图组件调用失败
- **风险**: 视图组件使用旧的 API 调用方式
- **缓解**: 全局搜索并更新所有调用点

## Migration Plan

### 阶段 1: API 文件修改
1. 修改 auth.ts
2. 修改 user.ts
3. 修改 application.ts
4. 修改 approval.ts
5. 修改 certificate.ts
6. 修改 admin.ts, college.ts, template.ts, log.ts

### 阶段 2: 类型定义更新
1. 更新 types/user.ts
2. 更新 types/application.ts
3. 更新 types/approval.ts
4. 更新 types/certificate.ts

### 阶段 3: 视图组件更新
1. 搜索所有使用被删除/修改方法的组件
2. 更新调用方式
3. 更新角色判断逻辑

### 阶段 4: 测试验证
1. 启动前后端服务
2. 测试各功能模块
3. 修复发现的问题

## Open Questions
- [ ] 是否需要添加后端缺失的接口（如日志导出）？
- [ ] 头像上传是否需要改为文件上传方式？