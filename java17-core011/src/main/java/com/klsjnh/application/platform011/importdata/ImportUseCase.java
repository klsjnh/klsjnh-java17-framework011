package com.klsjnh.application.platform011.importdata;

/*                ImportUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  import use case class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;

import com.klsjnh.domain.iam.user.UserAuditPort;
import com.klsjnh.domain.platform011.export.ExportSheetSpec;
import com.klsjnh.domain.platform011.export.XlsxWorkbookPort;
import com.klsjnh.domain.platform011.importdata.ImportBundle;
import com.klsjnh.domain.platform011.importdata.ImportProvider;
import com.klsjnh.domain.platform011.importdata.ImportResult;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Platform import use case: decode xlsx, apply the registered provider, write
 * IMPORT audit. Large sheets are accepted in full; providers may page their
 * own writes — {@link #BATCH_SIZE} is the suggested write-batch size, not a
 * reject threshold.
 */

@Service
public class ImportUseCase {

    /**
     * Suggested rows per write batch when a provider pages apply work. Not a
     * total-row reject limit.
     */
    public static final int BATCH_SIZE = 500;

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ImportUseCase.class);

    /**
     * Provider registry.
     */
    private final ImportProviderRegistry registry;

    /**
     * Xlsx codec port.
     */
    private final XlsxWorkbookPort xlsxWorkbookPort;

    /**
     * User audit port.
     */
    private final UserAuditPort userAuditPort;

    /**
     * Create the use case.
     *
     * @param registry          import provider registry
     * @param xlsxWorkbookPort  xlsx port
     * @param userAuditPort     audit port
     */
    public ImportUseCase(ImportProviderRegistry registry, XlsxWorkbookPort xlsxWorkbookPort,
            UserAuditPort userAuditPort) {
        this.registry = registry;
        this.xlsxWorkbookPort = xlsxWorkbookPort;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Import an xlsx workbook for the given object.
     *
     * @param objectCode object code
     * @param bytes      xlsx bytes
     * @param operator   current operator
     * @return import result
     */
    public ImportResult importXlsx(String objectCode, byte[] bytes, Operator011 operator) {
        String funcName = "import xlsx";

        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized(funcName + ": not authenticated");
        }

        ImportProvider provider = registry.get(objectCode);

        if (provider == null) {
            throw BusinessException.badRequest(funcName + ": unknown import object " + objectCode);
        }

        List<ExportSheetSpec> specs = provider.sheetSpecs();
        ImportBundle bundle = xlsxWorkbookPort.read(objectCode, bytes, specs);

        ImportResult result = provider.apply(bundle);

        userAuditPort.record(operator.id(), operator.userAccount(), AuditType011.IMPORT, objectCode,
                "import masters+" + result.mastersInserted() + "/" + result.mastersUpdated() + " children "
                        + result.childrenWritten(),
                operator.ip());

        logger.info("{} {} masters +{} ~{} children {}", funcName, objectCode, result.mastersInserted(),
                result.mastersUpdated(), result.childrenWritten());

        return result;
    }
}
