package com.klsjnh.domain.system011.dictionary;

/*                JulyDictionaryBundle class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary bundle class
 *
 */

import java.util.List;

/**
 * Program read result: a dictionary type together with its enabled items
 * (sort order ascending), returned by the getByType entry.
 *
 * @param dictionary the dictionary aggregate
 * @param items      the enabled items, ordered
 */

public record JulyDictionaryBundle(JulyDictionary dictionary, List<JulyDictionaryItem> items) {
}
