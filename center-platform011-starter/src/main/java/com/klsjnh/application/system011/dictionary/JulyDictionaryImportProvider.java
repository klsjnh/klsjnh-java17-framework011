package com.klsjnh.application.system011.dictionary;

/*                JulyDictionaryImportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  july dictionary import provider class
 *
 */

import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportSheetSpec;
import com.klsjnh.domain.platform011.importdata.ImportBundle;
import com.klsjnh.domain.platform011.importdata.ImportProvider;
import com.klsjnh.domain.platform011.importdata.ImportResult;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Import provider for julyDictionary — delegates apply to the use case.
 */

@Component
public class JulyDictionaryImportProvider implements ImportProvider {

    /**
     * Master sheet columns (must match export).
     */
    private static final List<ExportColumn> MASTER_COLUMNS = List.of(
            new ExportColumn("dictionaryCode", "字典编码"),
            new ExportColumn("dictionaryName", "字典名称"),
            new ExportColumn("sortOrder", "排序"),
            new ExportColumn("status", "状态"),
            new ExportColumn("remark", "备注"));

    /**
     * Children sheet columns.
     */
    private static final List<ExportColumn> CHILD_COLUMNS = List.of(
            new ExportColumn("dictionaryCode", "字典编码"),
            new ExportColumn("itemCode", "字典项编码"),
            new ExportColumn("itemLabel", "字典项名称"),
            new ExportColumn("sortOrder", "排序"),
            new ExportColumn("status", "状态"),
            new ExportColumn("remark", "备注"));

    /**
     * Dictionary use case.
     */
    private final JulyDictionaryUseCase useCase;

    /**
     * Create the provider.
     *
     * @param useCase dictionary use case
     */
    public JulyDictionaryImportProvider(JulyDictionaryUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Object code.
     *
     * @return julyDictionary
     */
    @Override
    public String objectCode() {
        return "julyDictionary";
    }

    /**
     * Sheet specs.
     *
     * @return master + children
     */
    @Override
    public List<ExportSheetSpec> sheetSpecs() {
        return List.of(new ExportSheetSpec("master", MASTER_COLUMNS), new ExportSheetSpec("children", CHILD_COLUMNS));
    }

    /**
     * Apply the bundle via the use case transaction.
     *
     * @param bundle import bundle
     * @return result
     */
    @Override
    public ImportResult apply(ImportBundle bundle) {
        return useCase.importWorkbook(bundle);
    }
}
