package com.klsjnh.web.system011.converter;

/*                JulyDictionaryConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary converter class
 *
 */

import com.klsjnh.domain.system011.dictionary.JulyDictionary;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryBundle;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItem;

import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryItemVo011;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyDictionary aggregates and the response VOs.
 */

@Component
public class JulyDictionaryConverter {

    /**
     * Map the dictionary aggregate to the response VO (without items).
     *
     * @param dictionary aggregate
     * @return response VO
     */
    public JulyDictionaryVo011 toVo(JulyDictionary dictionary) {
        JulyDictionaryVo011 vo = new JulyDictionaryVo011();
        fill(vo, dictionary);

        return vo;
    }

    /**
     * Map the bundle (dictionary + items) to the response VO.
     *
     * @param bundle domain bundle
     * @return response VO with items
     */
    public JulyDictionaryVo011 toVo(JulyDictionaryBundle bundle) {
        JulyDictionaryVo011 vo = new JulyDictionaryVo011();
        fill(vo, bundle.dictionary());
        vo.setItems(toItemVoList(bundle.items()));

        return vo;
    }

    /**
     * Map dictionary aggregates to response VOs (without items).
     *
     * @param dictionaries aggregates
     * @return response VO list
     */
    public List<JulyDictionaryVo011> toVoList(List<JulyDictionary> dictionaries) {
        List<JulyDictionaryVo011> result = new ArrayList<>();

        for (JulyDictionary dictionary : dictionaries) {
            result.add(toVo(dictionary));
        }

        return result;
    }

    /**
     * Map the item entity to the response VO.
     *
     * @param item entity
     * @return response VO
     */
    public JulyDictionaryItemVo011 toItemVo(JulyDictionaryItem item) {
        JulyDictionaryItemVo011 vo = new JulyDictionaryItemVo011();
        vo.setId(item.id().value());
        vo.setItemCode(item.itemCode());
        vo.setItemLabel(item.itemLabel());
        vo.setSortOrder(item.sortOrder());
        vo.setStatus(item.status());
        vo.setRemark(item.remark());

        return vo;
    }

    /**
     * Map item entities to response VOs.
     *
     * @param items entities
     * @return response VO list
     */
    public List<JulyDictionaryItemVo011> toItemVoList(List<JulyDictionaryItem> items) {
        List<JulyDictionaryItemVo011> result = new ArrayList<>();

        for (JulyDictionaryItem item : items) {
            result.add(toItemVo(item));
        }

        return result;
    }

    /**
     * Fill the shared dictionary fields.
     *
     * @param vo         target VO
     * @param dictionary aggregate
     */
    private void fill(JulyDictionaryVo011 vo, JulyDictionary dictionary) {
        vo.setId(dictionary.id().value());
        vo.setDictionaryCode(dictionary.dictionaryCode());
        vo.setSortOrder(dictionary.sortOrder());
        vo.setDictionaryName(dictionary.dictionaryName());
        vo.setStatus(dictionary.status());
        vo.setRemark(dictionary.remark());
        vo.setCreateBy(dictionary.audit().createBy());
        vo.setUpdateBy(dictionary.audit().updateBy());
        vo.setCreateTime(dictionary.audit().createTime());
        vo.setUpdateTime(dictionary.audit().updateTime());
    }
}
