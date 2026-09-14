package com.klsjnh.infrastructure.persistence.entity;

/*                MasterLinked interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  master linked interface
 *
 */

/**
 * Contract for child POs of a master-sub table: the fixed master link column
 * {@code pk_mt} mapped to the {@code pkMt} field. Child POs of
 * BaseMasterSubRepository021 must implement this interface so the cascade
 * save can set the master link without reflection.
 */

public interface MasterLinked {

    /**
     * Get the master id.
     *
     * @return master id
     */
    String getPkMt();

    /**
     * Set the master id.
     *
     * @param masterId master id
     */
    void setPkMt(String masterId);
}
