package com.klsjnh.infrastructure.storagecenter.object.provider;

/*                LocalStorageProviderFactory class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  built-in local011 provider factory
 *
 */

import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.ObjectStorageProviderFactory;
import com.klsjnh.domain.storagecenter.object.StorageConnectionConfig;
import com.klsjnh.domain.storagecenter.object.StorageProviderCodes011;

import com.klsjnh.infrastructure.storagecenter.object.adapter.LocalObjectStorageAdapter;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Built-in provider factory for the {@code local011} disk adapter.
 */

@Component
public class LocalStorageProviderFactory implements ObjectStorageProviderFactory {

    @Override
    public List<String> providers() {
        return List.of(StorageProviderCodes011.LOCAL);
    }

    @Override
    public ObjectStoragePort create(StorageConnectionConfig config) {
        return new LocalObjectStorageAdapter(config);
    }
}
