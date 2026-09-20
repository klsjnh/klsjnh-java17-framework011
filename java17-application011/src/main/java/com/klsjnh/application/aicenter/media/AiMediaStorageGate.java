package com.klsjnh.application.aicenter.media;

/*                AiMediaStorageGate class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai media local-storage gate
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.storagecenter.object.StorageProviderCodes011;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderQuerySpec;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Local storage gate for the media capabilities (image / tts): before a paid
 * generation call, require an enabled local storage instance. When none is
 * configured the capability refuses to serve (fast fail) and a single WARN is
 * logged — the whole application is not stopped, and the capability recovers
 * automatically once a local instance appears.
 */

@Slf4j
@Component
public class AiMediaStorageGate {

    /**
     * Refusal message.
     */
    private static final String MESSAGE =
            "AI media requires a local storage instance (local011); none is enabled, the capability is stopped";

    /**
     * Storage instance repository.
     */
    private final JulyStorageProviderRepository storageRepository;

    /**
     * Whether the single warning has already been logged.
     */
    private final AtomicBoolean warned = new AtomicBoolean(false);

    /**
     * Create the gate.
     *
     * @param storageRepository storage instance repository
     */
    public AiMediaStorageGate(JulyStorageProviderRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    /**
     * Require an enabled local storage instance, or refuse the capability.
     */
    public void assertLocalStorageAvailable() {
        JulyStorageProviderQuerySpec spec = new JulyStorageProviderQuerySpec(null, StorageProviderCodes011.LOCAL,
                Status011.ENABLED.getCode());

        if (storageRepository.count(spec) > 0) {
            return;
        }

        if (warned.compareAndSet(false, true)) {
            log.warn("{}", MESSAGE);
        }

        throw BusinessException.badRequest(MESSAGE);
    }
}
