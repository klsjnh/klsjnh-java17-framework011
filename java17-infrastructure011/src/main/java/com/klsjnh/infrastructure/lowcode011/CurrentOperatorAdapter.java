package com.klsjnh.infrastructure.lowcode011;

/*                CurrentOperatorAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  current operator adapter class
 *
 */

import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.identity.OperatorContext011;

import com.klsjnh.domain.lowcode011.CurrentOperatorPort;

import org.springframework.stereotype.Component;

/**
 * Adapter backing {@link CurrentOperatorPort} with the request-scoped
 * {@link OperatorContext011}.
 */

@Component
public class CurrentOperatorAdapter implements CurrentOperatorPort {

    /**
     * The current operator.
     *
     * @return operator, null when unauthenticated
     */
    @Override
    public Operator011 current() {
        return OperatorContext011.get();
    }
}
