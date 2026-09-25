package com.klsjnh.test011;

/*                BaseUseCaseTest011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  application-layer unit test base (strict mocks)
 *
 */

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Base class for application-layer use case unit tests: Mockito extension
 * with strict stubbing. Subclasses declare {@code @Mock} collaborators and
 * construct the use case by hand — no Spring context is started.
 */

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public abstract class BaseUseCaseTest011 {
}
