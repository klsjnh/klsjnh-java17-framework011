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
 *      2026.09.20  check the instance the store actually writes to
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.storagecenter.object.StorageProviderCodes011;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Local storage gate for the media capabilities (image / tts): before a paid
 * generation call, require that the storage instance the media store actually
 * writes to ({@code krt.ai-center.media.storage-code}, blank = the default
 * instance) is an enabled {@code local011}. When not, the capability refuses to
 * serve (fast fail) and a single WARN is logged — the whole application is not
 * stopped, and the capability recovers automatically once the instance is fixed.
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
     * Storage code used when none is configured (the yaml-seeded default).
     */
    private static final String DEFAULT_STORAGE_CODE = "default";

    /**
     * Storage instance repository.
     */
    private final JulyStorageProviderRepository storageRepository;

    /**
     * Storage instance code the media store writes to, blank for the default.
     */
    private final String storageCode;

    /**
     * Whether the single warning has already been logged.
     */
    private final AtomicBoolean warned = new AtomicBoolean(false);

    /**
     * Create the gate.
     *
     * @param storageRepository storage instance repository
     * @param storageCode       storage instance code, blank for the default
     */
    public AiMediaStorageGate(JulyStorageProviderRepository storageRepository,
            @Value("${krt.ai-center.media.storage-code:}") String storageCode) {
        this.storageRepository = storageRepository;
        this.storageCode = storageCode;
    }

    /**
     * Require the media storage instance to be an enabled local011, or refuse
     * the capability.
     */
    public void assertLocalStorageAvailable() {
        String code = StringUtil011.isBlank(storageCode) ? DEFAULT_STORAGE_CODE : storageCode.trim();
        JulyStorageProvider storage = storageRepository.findEnabledByCode(code);

        if (storage != null && StorageProviderCodes011.LOCAL.equals(storage.provider())) {
            return;
        }

        if (warned.compareAndSet(false, true)) {
            log.warn("{}", MESSAGE);
        }

        throw BusinessException.badRequest(MESSAGE);
    }
}
