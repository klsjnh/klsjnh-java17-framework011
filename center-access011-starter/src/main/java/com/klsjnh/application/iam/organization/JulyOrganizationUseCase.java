package com.klsjnh.application.iam.organization;

/*                JulyOrganizationUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization use case class
 *      2026.09.15  package moved under application/system011
 *      2026.09.15  tree method renamed to getTree
 *      2026.09.26  explicit permission checks (julyOrganization auth)
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.user.JulyUserRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.iam.organization.JulyOrganization;
import com.klsjnh.domain.iam.organization.JulyOrganizationPermissionCodes011;
import com.klsjnh.domain.iam.organization.JulyOrganizationRepository;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JulyOrganization use cases: org tree CRUD with level maintenance, move
 * cycle guard, delete double-constraint (children / mounted users), and the
 * member-count badge source. Management actions assert permission codes via
 * {@link AuthorizationPort}.
 */

@Service
public class JulyOrganizationUseCase {

    /**
     * JulyOrganization repository.
     */
    private final JulyOrganizationRepository repository;

    /**
     * JulyUser repository (member counts).
     */
    private final JulyUserRepository userRepository;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Platform export use case.
     */
    private final ExportUseCase exportUseCase;

    /**
     * Platform backup use case.
     */
    private final BackupUseCase backupUseCase;

    /**
     * Create the use case.
     *
     * @param repository        july organization repository
     * @param userRepository    july user repository
     * @param authorizationPort authorization port
     * @param exportUseCase     export use case
     * @param backupUseCase     backup use case
     */
    public JulyOrganizationUseCase(JulyOrganizationRepository repository, JulyUserRepository userRepository,
            AuthorizationPort authorizationPort, ExportUseCase exportUseCase, BackupUseCase backupUseCase) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.authorizationPort = authorizationPort;
        this.exportUseCase = exportUseCase;
        this.backupUseCase = backupUseCase;
    }

    /**
     * Insert a new organization node; the level derives from the parent
     * (root = 1).
     *
     * @param operatorId operator user id
     * @param orgCode    organization code, unique
     * @param orgName    organization name
     * @param pkUser     leader user id, nullable
     * @param parentId   parent organization id, blank for root
     * @param sortOrder  sort order
     * @return new organization id
     */
    @Transactional
    public String insert(String operatorId, String orgCode, String orgName, String pkUser, String parentId,
            Integer sortOrder) {
        authorizationPort.assertHas(operatorId, JulyOrganizationPermissionCodes011.INSERT);

        if (repository.findByCode(orgCode) != null) {
            throw BusinessException.badRequest("org code already exists: " + orgCode);
        }

        int level = resolveLevel(parentId);
        JulyOrganization organization = JulyOrganization.create(EntityId.generate(), orgCode, orgName, pkUser, level,
                parentId == null ? "" : parentId, sortOrder, AuditInfo.empty());
        repository.insert(organization);

        return organization.id().value();
    }

    /**
     * Update an organization (code immutable; parent move allowed with a
     * cycle guard — the subtree is re-leveled after a move).
     *
     * @param operatorId operator user id
     * @param id         organization id
     * @param orgName    organization name
     * @param pkUser     leader user id, nullable
     * @param parentId   parent organization id
     * @param sortOrder  sort order
     * @return organization id
     */
    @Transactional
    public String update(String operatorId, String id, String orgName, String pkUser, String parentId,
            Integer sortOrder) {
        authorizationPort.assertHas(operatorId, JulyOrganizationPermissionCodes011.UPDATE);
        JulyOrganization organization = require(id);
        String targetParent = parentId == null ? "" : parentId;

        if (id.equals(targetParent)) {
            throw BusinessException.badRequest("org parent cannot be itself");
        }

        int newLevel;
        boolean parentChanged = !targetParent.equals(organization.parentId());

        if (parentChanged) {
            newLevel = resolveLevel(targetParent);
            guardMove(id, targetParent);
        } else {
            newLevel = organization.orgLevel() == null ? 1 : organization.orgLevel();
        }

        organization.updateBasics(orgName, pkUser, targetParent, sortOrder);
        organization.changeLevel(newLevel);
        repository.update(organization);

        if (parentChanged) {
            relevelSubtree(id, newLevel + 1);
        }

        return organization.id().value();
    }

    /**
     * Logic delete an organization; double constraint — alive children and
     * mounted users both reject.
     *
     * @param operatorId operator user id
     * @param id         organization id
     * @return deleted organization id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyOrganizationPermissionCodes011.LOGIC_DELETE);
        require(id);

        if (repository.hasChildren(id)) {
            throw BusinessException.badRequest("org has children, delete children first");
        }

        if (userRepository.countByOrg(id) > 0) {
            throw BusinessException.badRequest("org has mounted users, move them first");
        }

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Find by primary key.
     *
     * @param operatorId operator user id
     * @param id         organization id
     * @return aggregate
     */
    public JulyOrganization getById(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyOrganizationPermissionCodes011.SELECT);
        return require(id);
    }

    /**
     * Get the full alive organization tree with member counts.
     *
     * @param operatorId operator user id
     * @return tree plus a orgId → member count map
     */
    public TreeWithCounts getTree(String operatorId) {
        authorizationPort.assertHas(operatorId, JulyOrganizationPermissionCodes011.SELECT);
        return new TreeWithCounts(repository.getTree(), userRepository.countByOrgGrouped());
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param operatorId operator user id
     * @param pageQuery  page query, null falls back to page 1 / size 10
     * @param keyword    org code / name keyword, nullable
     * @return page result
     */
    public PageResult011<JulyOrganization> selectListByPage(String operatorId, PageQuery011 pageQuery,
            String keyword) {
        authorizationPort.assertHas(operatorId, JulyOrganizationPermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyOrganization> rows = repository.findPage(query.offset(), query.pageSize(), keyword);
        long total = repository.count(keyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Export all organization rows (permission-gated; payload is export result only).
     *
     * @param operator authenticated operator
     * @return export result
     */
    public ExportResult export(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyOrganizationPermissionCodes011.EXPORT);
        return exportUseCase.export(AuditObjectCodes011.JULY_ORGANIZATION, operator);
    }

    /**
     * Backup all organization rows to object storage (permission-gated).
     * Returns the storage object key only — never the organization row payload.
     *
     * @param operator authenticated operator
     * @return storage object key
     */
    public String backup(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyOrganizationPermissionCodes011.BACKUP);
        return backupUseCase.backup(AuditObjectCodes011.JULY_ORGANIZATION, operator);
    }

    /**
     * Resolve the level of a parent position (root = 1).
     *
     * @param parentId parent organization id, blank for root
     * @return level of the parent plus one offset base
     */
    private int resolveLevel(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return 1;
        }

        JulyOrganization parent = repository.findById(parentId);

        if (parent == null) {
            throw BusinessException.badRequest("parent org not found, id=" + parentId);
        }

        return (parent.orgLevel() == null ? 1 : parent.orgLevel()) + 1;
    }

    /**
     * Guard a parent move: the new parent chain must not contain the moved
     * node itself (no cycles).
     *
     * @param id           moved organization id
     * @param newParentId new parent id
     */
    private void guardMove(String id, String newParentId) {
        String cursor = newParentId;

        while (cursor != null && !cursor.isBlank()) {
            if (cursor.equals(id)) {
                throw BusinessException.badRequest("org parent cannot be its own descendant");
            }

            JulyOrganization parent = repository.findById(cursor);
            cursor = parent == null ? null : parent.parentId();
        }
    }

    /**
     * Re-level a subtree after a parent move.
     *
     * @param orgId subtree root id
     * @param level new level of the subtree root
     */
    private void relevelSubtree(String orgId, int level) {
        for (JulyOrganization child : repository.findChildren(orgId)) {
            child.changeLevel(level);
            repository.update(child);
            relevelSubtree(child.id().value(), level + 1);
        }
    }

    /**
     * Require an authenticated operator.
     *
     * @param operator operator
     */
    private void requireOperator(Operator011 operator) {
        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized("not authenticated");
        }
    }

    /**
     * Require an existing organization.
     *
     * @param id organization id
     * @return aggregate
     */
    private JulyOrganization require(String id) {
        JulyOrganization organization = repository.findById(id);

        if (organization == null) {
            throw BusinessException.recordNotFound(id);
        }

        return organization;
    }

    /**
     * Tree load result: the assembled tree plus the member-count badge source.
     *
     * @param tree   root nodes with nested children
     * @param counts orgId → member count
     */
    public record TreeWithCounts(List<JulyOrganization> tree, Map<String, Long> counts) {

        /**
         * Member count of one organization.
         *
         * @param orgId organization id
         * @return member count, 0 when none
         */
        public long countOf(String orgId) {
            return counts.getOrDefault(orgId, 0L);
        }
    }
}
