package com.klsjnh.web.aicenter.converter;

/*                AiFileConverter011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file vo converter
 *
 */

import com.klsjnh.domain.aicenter.file.AiFileRef;

import com.klsjnh.web.aicenter.vo.aifile.AiFileContentVo011;
import com.klsjnh.web.aicenter.vo.aifile.AiFileVo011;

import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Provider file domain to VO converter.
 */

@Component
public class AiFileConverter011 {

    /**
     * Convert file metadata.
     *
     * @param ref file reference
     * @return vo
     */
    public AiFileVo011 toVo(AiFileRef ref) {
        AiFileVo011 vo = new AiFileVo011();
        vo.setProviderCode(ref.providerCode());
        vo.setFileId(ref.fileId());
        vo.setFileName(ref.fileName());
        vo.setBytes(ref.bytes());
        vo.setPurpose(ref.purpose());
        vo.setStatus(ref.status());
        vo.setCreatedAt(ref.createdAt());

        return vo;
    }

    /**
     * Convert file content.
     *
     * @param fileId file id
     * @param bytes  bytes
     * @return vo
     */
    public AiFileContentVo011 toContentVo(String fileId, byte[] bytes) {
        AiFileContentVo011 vo = new AiFileContentVo011();
        vo.setFileId(fileId);
        vo.setBytes((long) (bytes == null ? 0 : bytes.length));
        vo.setBase64(bytes == null ? null : Base64.getEncoder().encodeToString(bytes));

        return vo;
    }
}
