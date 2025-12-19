package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 申请创建请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    
    /**
     * 申请标题
     */
    @NotBlank(message = "申请标题不能为空")
    @Size(max = 200, message = "申请标题长度不能超过200个字符")
    private String title;
    
    /**
     * 证书类型
     */
    @NotBlank(message = "证书类型不能为空")
    @Size(max = 50, message = "证书类型长度不能超过50个字符")
    private String certificateType;
    
    /**
     * 证明文件列表（1-3个文件）
     */
    @NotNull(message = "证明文件不能为空")
    @Size(min = 1, max = 3, message = "证明文件数量必须在1-3个之间")
    private List<MultipartFile> files;
}