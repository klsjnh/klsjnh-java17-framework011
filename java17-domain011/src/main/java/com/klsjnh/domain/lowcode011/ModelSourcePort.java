package com.klsjnh.domain.lowcode011;

/*                ModelSourcePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  modeling source adapter port
 *
 */

import com.klsjnh.domain.lowcode011.records.MetadataContent;

/**
 * A modeling source adapter: turns one kind of source (probe SQL, a template,
 * an existing object, a natural-language prompt, a business-modeling record,
 * ...) into the shared content contract. The intake engine routes by
 * {@link #kind()}; the review / publish / sync downstream stays unique.
 */

public interface ModelSourcePort {

    /**
     * Source kind handled by this adapter.
     *
     * @return kind (sql / table / template / ai / modeling / copy)
     */
    String kind();

    /**
     * Turn the request into the content contract (reconstitute validates).
     *
     * @param request source request
     * @return validated content
     */
    MetadataContent intake(SourceRequest request);

    /**
     * Rename the content's object when the request supplies a target name
     * (e.g. copy an object under a new name). Blank or identical names keep the
     * original.
     *
     * @param content    content
     * @param objectName target object name, nullable
     * @return content, possibly renamed
     */
    default MetadataContent rename(MetadataContent content, String objectName) {
        if (objectName == null || objectName.isBlank() || objectName.equals(content.objectName())) {
            return content;
        }

        return MetadataContent.reconstitute(objectName, content.objectType(), content.description(),
                content.businessField(), content.packageName(), content.routerPath(), content.fields(),
                content.displays(), content.services());
    }
}
