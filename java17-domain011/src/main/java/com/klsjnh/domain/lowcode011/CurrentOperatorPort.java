package com.klsjnh.domain.lowcode011;

/*                CurrentOperatorPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  current operator port
 *
 */

import com.klsjnh.common.identity.Operator011;

/**
 * Current-operator port: the low-code engine reads the request's operator from
 * here (instead of every use case carrying an {@code Operator011}), so the
 * gateway can stamp audit columns in one place. The web filter fills it, an
 * infrastructure adapter backs it.
 */

public interface CurrentOperatorPort {

    /**
     * The current operator.
     *
     * @return operator, null when unauthenticated (debug / open API)
     */
    Operator011 current();
}
