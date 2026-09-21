package com.klsjnh.domain.storagecenter.object;

/*                ObjectListing record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  native paged object listing (delimiter + marker)
 *
 */

import java.util.List;

/**
 * One page of a native object listing: the objects and the folder-like prefixes
 * (when a delimiter is given), plus the marker to continue from and whether the
 * listing is truncated.
 *
 * @param objects    object metadata rows of this page, never null
 * @param prefixes   folder-like prefixes (delimiter mode), never null
 * @param nextMarker continue-after key for the next page, nullable when not truncated
 * @param truncated  whether more entries remain
 */

public record ObjectListing(List<ObjectStat> objects, List<String> prefixes, String nextMarker, boolean truncated) {
}
