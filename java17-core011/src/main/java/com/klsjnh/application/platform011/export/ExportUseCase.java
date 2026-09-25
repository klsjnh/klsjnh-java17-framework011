package com.klsjnh.application.platform011.export;

/*                ExportUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  export use case class
 *      2026.09.15  rebuilt as a batched collector, transport concern removed
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;

import com.klsjnh.domain.iam.user.UserAuditPort;
import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.domain.platform011.export.ExportSheet;
import com.klsjnh.domain.platform011.export.ExportSheetSpec;
import com.klsjnh.domain.platform011.export.ExportWorkbook;
import com.klsjnh.domain.platform011.export.XlsxWorkbookPort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Platform data export use case: resolves the object's registered provider and
 * collects its rows in batches into one {@link ExportResult}.
 * <p>
 * Batching is the point: the provider is called with an offset and a fixed
 * page size so the database never sees a single full-table statement. Rows are
 * accumulated in memory as they arrive — the database load is decided purely
 * by the batch size, not by where the rows are kept.
 * </p>
 * <p>
 * The returned result carries data only. Serialization and delivery belong to
 * the caller, so the download path and the backup path share this very method.
 * </p>
 */

@Service
public class ExportUseCase {

    /**
     * Rows fetched per database round trip when collecting an export. The
     * exporter loops until a short batch signals the end — this is a batch
     * size, not a total row cap.
     */
    public static final int BATCH_SIZE = 500;

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ExportUseCase.class);

    /**
     * Provider registry.
     */
    private final ExportProviderRegistry registry;

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
     * @param registry         export provider registry
     * @param xlsxWorkbookPort xlsx port
     * @param userAuditPort    user audit port
     */
    public ExportUseCase(ExportProviderRegistry registry, XlsxWorkbookPort xlsxWorkbookPort,
            UserAuditPort userAuditPort) {
        this.registry = registry;
        this.xlsxWorkbookPort = xlsxWorkbookPort;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Collect the object's full row set in batches.
     *
     * @param objectCode      object code (registered provider)
     * @param operatorId      current operator id
     * @param operatorAccount current operator account
     * @param ip              client IP
     * @return export result (object info + column info + all rows)
     */
    public ExportResult export(String objectCode, String operatorId, String operatorAccount, String ip) {
        return export(objectCode, new Operator011(operatorId, operatorAccount, ip));
    }

    /**
     * Collect the object's full row set in batches, with the operator carried
     * as one value.
     *
     * @param objectCode object code (registered provider)
     * @param operator   current operator (id + account + ip)
     * @return export result (object info + column info + all rows)
     */
    public ExportResult export(String objectCode, Operator011 operator) {
        String funcName = "export";

        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized(funcName + ": not authenticated");
        }

        ExportProvider provider = registry.get(objectCode);

        if (provider == null) {
            throw BusinessException.badRequest(funcName + ": unknown export object " + objectCode);
        }

        List<ExportColumn> columns = List.copyOf(provider.columns());
        List<Map<String, Object>> rows = collect(provider);

        userAuditPort.record(operator.id(), operator.userAccount(), AuditType011.EXPORT, objectCode,
                "export " + rows.size() + " rows", operator.ip());

        logger.info("{} {} collected {} rows", funcName, objectCode, rows.size());

        return ExportResult.of(objectCode, columns, rows);
    }

    /**
     * Collect every sheet into an xlsx workbook (paged with {@link #BATCH_SIZE}
     * per database round trip until exhausted).
     *
     * @param objectCode object code
     * @param operator   current operator
     * @return xlsx bytes
     */
    public byte[] exportXlsx(String objectCode, Operator011 operator) {
        String funcName = "export xlsx";

        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized(funcName + ": not authenticated");
        }

        ExportProvider provider = registry.get(objectCode);

        if (provider == null) {
            throw BusinessException.badRequest(funcName + ": unknown export object " + objectCode);
        }

        List<ExportSheet> sheets = new ArrayList<>();
        int totalRows = 0;

        for (ExportSheetSpec spec : provider.sheetSpecs()) {
            List<Map<String, Object>> rows = collectSheet(provider, spec.name());
            totalRows += rows.size();
            sheets.add(new ExportSheet(spec.name(), spec.columns(), rows));
        }

        userAuditPort.record(operator.id(), operator.userAccount(), AuditType011.EXPORT, objectCode,
                "export xlsx " + totalRows + " rows", operator.ip());

        logger.info("{} {} collected {} rows across {} sheets", funcName, objectCode, totalRows, sheets.size());

        return xlsxWorkbookPort.write(new ExportWorkbook(objectCode, sheets));
    }

    /**
     * Drive the provider batch by batch until a short batch signals the end.
     *
     * @param provider export provider
     * @return all rows the provider's live query returns
     */
    private List<Map<String, Object>> collect(ExportProvider provider) {
        return collectSheet(provider, "master");
    }

    /**
     * Collect one sheet in {@link #BATCH_SIZE} pages until exhausted.
     *
     * @param provider  export provider
     * @param sheetName sheet name
     * @return all rows
     */
    private List<Map<String, Object>> collectSheet(ExportProvider provider, String sheetName) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int offset = 0;

        while (true) {
            List<Map<String, Object>> batch = provider.exportSheetRows(sheetName, offset, BATCH_SIZE);

            if (batch == null || batch.isEmpty()) {
                break;
            }

            rows.addAll(batch);
            offset += batch.size();

            if (batch.size() < BATCH_SIZE) {
                break;
            }
        }

        return rows;
    }
}
