package com.klsjnh.domain.aicenter.file;

/*                AiFilePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file asset SPI
 *
 */

import com.klsjnh.domain.aicenter.capability.AiCapabilityCodes;
import com.klsjnh.domain.aicenter.capability.AiCapabilityPort;

import java.util.List;

/**
 * SPI: provider-side file assets (upload / get / list / delete / content).
 * Bytes stay at the vendor; the framework keeps only metadata. This is NOT a
 * replacement for the storage center — business files still live there.
 */

public interface AiFilePort extends AiCapabilityPort {

    /** {@inheritDoc} */
    @Override
    default String capabilityCode() {
        return AiCapabilityCodes.FILE;
    }

    /**
     * Upload one file (bytes or remote URL).
     *
     * @param command upload command
     * @return file metadata
     */
    AiFileRef upload(AiFileUploadCommand command);

    /**
     * Get one file's metadata.
     *
     * @param command lookup command
     * @return file metadata
     */
    AiFileRef get(AiFileCommand command);

    /**
     * List files, optionally filtered by purpose.
     *
     * @param command list command
     * @return file metadata list, never null
     */
    List<AiFileRef> list(AiFileListCommand command);

    /**
     * Delete one file.
     *
     * @param command lookup command
     */
    void delete(AiFileCommand command);

    /**
     * Download one file's bytes.
     *
     * @param command lookup command
     * @return file bytes
     */
    byte[] content(AiFileCommand command);
}
