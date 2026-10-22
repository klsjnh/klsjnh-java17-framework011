package com.klsjnh.application.aicenter.file;

/*                AiFileUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file asset use case
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.application.aicenter.AiProviderResolver;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.file.AiFileCommand;
import com.klsjnh.domain.aicenter.file.AiFileListCommand;
import com.klsjnh.domain.aicenter.file.AiFilePermissionCodes011;
import com.klsjnh.domain.aicenter.file.AiFilePort;
import com.klsjnh.domain.aicenter.file.AiFilePurpose;
import com.klsjnh.domain.aicenter.file.AiFileRef;
import com.klsjnh.domain.aicenter.file.AiFileUploadCommand;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.iam.auth.AuthorizationPort;

import com.klsjnh.infrastructure.aicenter.config.KrtAiConfig011;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI provider file use case: resolves the provider / key by id or code and
 * delegates upload / get / list / delete / content to the file port. Bytes stay
 * at the vendor; business files still belong to the storage center.
 */

@Service
public class AiFileUseCase {

    /**
     * Provider / key resolver.
     */
    private final AiProviderResolver resolver;

    /**
     * Capability registry.
     */
    private final AiCapabilityRegistry registry;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * AI-center config (upload limit).
     */
    private final KrtAiConfig011 config;

    /**
     * Create the use case.
     *
     * @param resolver          provider resolver
     * @param registry          capability registry
     * @param authorizationPort authorization port
     * @param config            ai-center config
     */
    public AiFileUseCase(AiProviderResolver resolver, AiCapabilityRegistry registry,
            AuthorizationPort authorizationPort, KrtAiConfig011 config) {
        this.resolver = resolver;
        this.registry = registry;
        this.authorizationPort = authorizationPort;
        this.config = config;
    }

    /**
     * Upload one file.
     *
     * @param operatorId operator id
     * @param target     routing header (provider / key)
     * @param fileName   file name
     * @param bytes      file bytes, nullable when url is given
     * @param url        remote file url, nullable when bytes are given
     * @param purpose    purpose
     * @return file metadata
     */
    public AiFileRef upload(String operatorId, AiInvokeTarget target, String fileName, byte[] bytes, String url,
            AiFilePurpose purpose) {
        if (purpose == null) {
            throw BusinessException.badRequest("file purpose is required");
        }

        boolean hasBytes = bytes != null && bytes.length > 0;
        boolean hasUrl = !StringUtil011.isBlank(url);

        if (!hasBytes && !hasUrl) {
            throw BusinessException.badRequest("file bytes or url is required");
        }

        if (hasBytes && bytes.length > config.getFile().getMaxUploadBytes()) {
            throw BusinessException.badRequest("file exceeds upload limit: " + bytes.length + " bytes");
        }

        Resolved resolved = resolve(operatorId, target, AiFilePermissionCodes011.UPLOAD);

        return resolved.port().upload(new AiFileUploadCommand(resolved.provider().baseUrl(), resolved.apiKey(),
                resolved.provider().providerCode(), fileName, hasBytes ? bytes : null, hasUrl ? url : null, purpose));
    }

    /**
     * Get one file's metadata.
     *
     * @param operatorId operator id
     * @param target     routing header
     * @param fileId     provider file id
     * @return file metadata
     */
    public AiFileRef get(String operatorId, AiInvokeTarget target, String fileId) {
        Resolved resolved = resolve(operatorId, target, AiFilePermissionCodes011.SELECT);
        requireFileId(fileId);

        return resolved.port().get(command(resolved, fileId));
    }

    /**
     * List files.
     *
     * @param operatorId operator id
     * @param target     routing header
     * @param purpose    purpose filter, nullable for all
     * @return file metadata list
     */
    public List<AiFileRef> list(String operatorId, AiInvokeTarget target, AiFilePurpose purpose) {
        Resolved resolved = resolve(operatorId, target, AiFilePermissionCodes011.SELECT);

        return resolved.port().list(new AiFileListCommand(resolved.provider().baseUrl(), resolved.apiKey(),
                resolved.provider().providerCode(), purpose));
    }

    /**
     * Delete one file.
     *
     * @param operatorId operator id
     * @param target     routing header
     * @param fileId     provider file id
     */
    public void delete(String operatorId, AiInvokeTarget target, String fileId) {
        Resolved resolved = resolve(operatorId, target, AiFilePermissionCodes011.DELETE);
        requireFileId(fileId);
        resolved.port().delete(command(resolved, fileId));
    }

    /**
     * Download one file's bytes.
     *
     * @param operatorId operator id
     * @param target     routing header
     * @param fileId     provider file id
     * @return bytes
     */
    public byte[] content(String operatorId, AiInvokeTarget target, String fileId) {
        Resolved resolved = resolve(operatorId, target, AiFilePermissionCodes011.CONTENT);
        requireFileId(fileId);

        return resolved.port().content(command(resolved, fileId));
    }

    /**
     * Resolve provider / key / port and check the permission.
     *
     * @param target         routing header
     * @param permissionCode permission code
     * @return resolved context
     */
    private Resolved resolve(String operatorId, AiInvokeTarget target, String permissionCode) {
        authorizationPort.assertHas(operatorId, permissionCode);

        if (target == null) {
            throw BusinessException.badRequest("target is required");
        }

        AiModelProvider provider = resolver.resolveProvider(target.providerCode(), target.providerId());
        AiModelProviderApi api = resolver.resolveApi(provider, target.keyCode(), target.keyId());
        AiFilePort port = registry.filePort(provider.providerCode());

        if (port == null) {
            throw BusinessException.badRequest("no file provider for: " + provider.providerCode());
        }

        return new Resolved(provider, api.apiKey(), port);
    }

    /**
     * Require a file id.
     *
     * @param fileId file id
     */
    private void requireFileId(String fileId) {
        if (StringUtil011.isBlank(fileId)) {
            throw BusinessException.badRequest("fileId is required");
        }
    }

    /**
     * Build a lookup command.
     *
     * @param resolved resolved context
     * @param fileId   file id
     * @return command
     */
    private AiFileCommand command(Resolved resolved, String fileId) {
        return new AiFileCommand(resolved.provider().baseUrl(), resolved.apiKey(),
                resolved.provider().providerCode(), fileId);
    }

    /**
     * Resolved provider / key / port.
     *
     * @param provider provider
     * @param apiKey   api key
     * @param port     file port
     */
    private record Resolved(AiModelProvider provider, String apiKey, AiFilePort port) {
    }
}
