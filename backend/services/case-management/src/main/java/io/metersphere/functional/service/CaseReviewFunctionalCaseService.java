package io.metersphere.functional.service;


import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.CaseFileSourceType;
import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.UserRoleScope;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 用例评审和功能用例的中间表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CaseReviewFunctionalCaseService {


    @Resource
    private ExtCaseReviewFunctionalCaseMapper extCaseReviewFunctionalCaseMapper;
    @Resource
    private ExtFunctionalCaseModuleMapper extFunctionalCaseModuleMapper;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private ExtCaseReviewFunctionalCaseUserMapper extCaseReviewFunctionalCaseUserMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private CaseReviewService caseReviewService;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper;
    @Resource
    private BaseCaseProvider provider;
    @Resource
    private ReviewSendNoticeService reviewSendNoticeService;
    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;
    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;
    @Resource
    private ExtCaseReviewHistoryMapper extCaseReviewHistoryMapper;
    @Resource
    private CaseReviewUserMapper caseReviewUserMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;


    private static final String CASE_MODULE_COUNT_ALL = "all";

    /**
     * 通过评审id获取关联的用例id集合
     *
     * @param reviewId reviewId
     * @return String
     */
    public List<String> getCaseIdsByReviewId(String reviewId) {
        return extCaseReviewFunctionalCaseMapper.getCaseIdsByReviewId(reviewId);
    }

    /**
     * 评审详情分页列表查询
     *
     * @param request request
     * @param deleted deleted
     * @return ReviewFunctionalCaseDTO
     */
    public List<ReviewFunctionalCaseDTO> page(ReviewFunctionalCasePageRequest request, boolean deleted, String userId) {
        List<ReviewFunctionalCaseDTO> list = extCaseReviewFunctionalCaseMapper.page(request, deleted, userId, request.getSortString());
        return doHandleDTO(list, request.getReviewId());
    }

    private List<ReviewFunctionalCaseDTO> doHandleDTO(List<ReviewFunctionalCaseDTO> list, String reviewId) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> moduleIds = list.stream().map(ReviewFunctionalCaseDTO::getModuleId).toList();
            List<BaseTreeNode> modules = extFunctionalCaseModuleMapper.selectBaseByIds(moduleIds);
            Map<String, String> moduleMap = modules.stream().collect(Collectors.toMap(BaseTreeNode::getId, BaseTreeNode::getName));

            List<String> versionIds = list.stream().map(ReviewFunctionalCaseDTO::getVersionId).toList();
            List<ProjectVersion> versions = extBaseProjectVersionMapper.getVersionByIds(versionIds);
            Map<String, String> versionMap = versions.stream().collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));

            List<String> caseIds = list.stream().map(ReviewFunctionalCaseDTO::getCaseId).toList();
            List<ReviewsDTO> reviewers = extCaseReviewFunctionalCaseUserMapper.selectReviewers(caseIds, reviewId);
            Map<String, String> userIdMap = reviewers.stream().collect(Collectors.toMap(ReviewsDTO::getCaseId, ReviewsDTO::getUserIds));
            Map<String, String> userNameMap = reviewers.stream().collect(Collectors.toMap(ReviewsDTO::getCaseId, ReviewsDTO::getUserNames));

            list.forEach(item -> {
                item.setModuleName(moduleMap.get(item.getModuleId()));
                item.setVersionName(versionMap.get(item.getVersionId()));
                item.setReviewers(Collections.singletonList(userIdMap.get(item.getCaseId())));
                item.setReviewNames(Collections.singletonList(userNameMap.get(item.getCaseId())));
            });
        }
        return list;
    }

    /**
     * 批量删除
     *
     * @param request request
     */
    public void disassociate(BaseReviewCaseBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            CaseReviewFunctionalCaseExample example = new CaseReviewFunctionalCaseExample();
            example.createCriteria().andIdIn(ids);
            caseReviewFunctionalCaseMapper.deleteByExample(example);
            Map<String, Object> param = new HashMap<>();
            param.put(CaseEvent.Param.REVIEW_ID, request.getReviewId());
            param.put(CaseEvent.Param.CASE_IDS, ids);
            param.put(CaseEvent.Param.USER_ID, userId);
            param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.DISASSOCIATE);
            provider.updateCaseReview(param);
        }
    }


    public List<String> doSelectIds(BaseReviewCaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extCaseReviewFunctionalCaseMapper.getIds(request, request.getUserId(), false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public List<CaseReviewFunctionalCase> doCaseReviewFunctionalCases(BaseReviewCaseBatchRequest request) {
        if (request.isSelectAll()) {
            return extCaseReviewFunctionalCaseMapper.getListByRequest(request, request.getUserId(), false);
        } else {
            CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
            caseReviewFunctionalCaseExample.createCriteria().andIdIn(request.getSelectIds());
            return caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        }
    }


    /**
     * 评审详情页面 创建用例并关联
     *
     * @param caseId   功能用例ID
     * @param userId   当前操作人
     * @param reviewId 评审id
     */
    public void addCaseReviewFunctionalCase(String caseId, String userId, String reviewId) {
        CaseReviewFunctionalCase reviewFunctionalCase = new CaseReviewFunctionalCase();
        reviewFunctionalCase.setId(IDGenerator.nextStr());
        reviewFunctionalCase.setCaseId(caseId);
        reviewFunctionalCase.setReviewId(reviewId);
        reviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        reviewFunctionalCase.setCreateUser(userId);
        reviewFunctionalCase.setCreateTime(System.currentTimeMillis());
        reviewFunctionalCase.setUpdateTime(System.currentTimeMillis());
        reviewFunctionalCase.setPos(caseReviewService.getCaseFunctionalCaseNextPos(reviewId));
        caseReviewFunctionalCaseMapper.insertSelective(reviewFunctionalCase);

        //评审人
        CaseReviewUserExample caseReviewUserExample = new CaseReviewUserExample();
        caseReviewUserExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<CaseReviewUser> caseReviewUsers = caseReviewUserMapper.selectByExample(caseReviewUserExample);
        if (CollectionUtils.isNotEmpty(caseReviewUsers)) {
            List<CaseReviewFunctionalCaseUser> list = new ArrayList<>();
            caseReviewUsers.forEach(item -> {
                CaseReviewFunctionalCaseUser caseUser = new CaseReviewFunctionalCaseUser();
                caseUser.setCaseId(caseId);
                caseUser.setReviewId(reviewId);
                caseUser.setUserId(item.getUserId());
                list.add(caseUser);
            });
            caseReviewFunctionalCaseUserMapper.batchInsert(list);
            //更新评审的整体状态
            Map<String, Integer> countMap = new HashMap<>();
            countMap.put(reviewFunctionalCase.getStatus(), 1);
            Map<String, String> statusMap = new HashMap<>();
            statusMap.put(caseId, reviewFunctionalCase.getStatus());
            Map<String, Object> param = new HashMap<>();
            param.put(CaseEvent.Param.REVIEW_ID, reviewId);
            param.put(CaseEvent.Param.USER_ID, userId);
            param.put(CaseEvent.Param.CASE_IDS, List.of(caseId));
            param.put(CaseEvent.Param.COUNT_MAP, countMap);
            param.put(CaseEvent.Param.STATUS_MAP, statusMap);
            param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
            provider.updateCaseReview(param);
        }
    }


    /**
     * 用例更新 更新状态为重新评审
     */
    public void reReviewedCase(FunctionalCaseEditRequest request, FunctionalCaseBlob blob, String name, String userId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andTypeEqualTo(ProjectApplicationType.CASE.CASE_RE_REVIEW.name());
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(projectApplications) && Boolean.valueOf(projectApplications.get(0).getTypeValue())) {
            if (!StringUtils.equals(name, request.getName())
                    || !StringUtils.equals(new String(blob.getSteps(), StandardCharsets.UTF_8), request.getSteps())
                    || !StringUtils.equals(new String(blob.getTextDescription(), StandardCharsets.UTF_8), request.getTextDescription())
                    || !StringUtils.equals(new String(blob.getExpectedResult(), StandardCharsets.UTF_8), request.getExpectedResult())) {
                doHandleStatusAndHistory(blob, userId);
            }
        }
    }

    private void doHandleStatusAndHistory(FunctionalCaseBlob blob, String userId) {
        CaseReviewFunctionalCaseExample reviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        reviewFunctionalCaseExample.createCriteria().andCaseIdEqualTo(blob.getId());
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(reviewFunctionalCaseExample);
        if (CollectionUtils.isNotEmpty(caseReviewFunctionalCases)) {
            caseReviewFunctionalCases.forEach(item -> {
                updateReviewCaseAndCaseStatus(item);
                insertHistory(item);
                //更新用例触发重新提审-需要重新计算评审的整体状态
                Map<String, Integer> countMap = new HashMap<>();
                countMap.put(item.getStatus(), 1);
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put(item.getCaseId(), item.getStatus());
                Map<String, Object> param = new HashMap<>();
                param.put(CaseEvent.Param.REVIEW_ID, item.getReviewId());
                param.put(CaseEvent.Param.USER_ID, userId);
                param.put(CaseEvent.Param.CASE_IDS, List.of(item.getCaseId()));
                param.put(CaseEvent.Param.COUNT_MAP, countMap);
                param.put(CaseEvent.Param.STATUS_MAP, statusMap);
                param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
                provider.updateCaseReview(param);

            });
        }
    }

    private void insertHistory(CaseReviewFunctionalCase item) {
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setId(IDGenerator.nextStr());
        caseReviewHistory.setCaseId(item.getCaseId());
        caseReviewHistory.setReviewId(item.getReviewId());
        caseReviewHistory.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.name());
        caseReviewHistory.setCreateUser("system");
        caseReviewHistory.setCreateTime(System.currentTimeMillis());
        caseReviewHistory.setDeleted(false);
        caseReviewHistoryMapper.insertSelective(caseReviewHistory);
    }

    private void updateReviewCaseAndCaseStatus(CaseReviewFunctionalCase item) {
        item.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.name());
        item.setUpdateTime(System.currentTimeMillis());
        caseReviewFunctionalCaseMapper.updateByPrimaryKeySelective(item);

        FunctionalCase functionalCase = new FunctionalCase();
        functionalCase.setId(item.getCaseId());
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.RE_REVIEWED.name());
        functionalCaseMapper.updateByPrimaryKeySelective(functionalCase);
    }

    /**
     * 拖拽关联用例的排序
     */
    public void editPos(CaseReviewFunctionalCasePosRequest request) {
        ServiceUtils.updatePosField(request,
                CaseReviewFunctionalCase.class,
                caseReviewFunctionalCaseMapper::selectByPrimaryKey,
                extCaseReviewFunctionalCaseMapper::getPrePos,
                extCaseReviewFunctionalCaseMapper::getLastPos,
                caseReviewFunctionalCaseMapper::updateByPrimaryKeySelective);
    }

    /**
     * 批量评审
     */
    public void batchReview(BatchReviewFunctionalCaseRequest request, String userId) {
        String reviewId = request.getReviewId();

        List<CaseReviewFunctionalCase> caseReviewFunctionalCaseList = doCaseReviewFunctionalCases(request);
        if(CollectionUtils.isEmpty(caseReviewFunctionalCaseList)) {
            return;
        }
        List<String> caseIds = caseReviewFunctionalCaseList.stream().map(CaseReviewFunctionalCase::getCaseId).toList();
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andCaseIdIn(caseIds).andReviewIdEqualTo(reviewId).andDeletedEqualTo(false);
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Map<String, List<CaseReviewHistory>> caseHistoryMap = caseReviewHistories.stream().collect(Collectors.groupingBy(CaseReviewHistory::getCaseId, Collectors.toList()));

        CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
        caseReviewFunctionalCaseUserExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdIn(caseIds);
        List<CaseReviewFunctionalCaseUser> caseReviewFunctionalCaseUsers = caseReviewFunctionalCaseUserMapper.selectByExample(caseReviewFunctionalCaseUserExample);
        Map<String, List<CaseReviewFunctionalCaseUser>> reviewerMap = caseReviewFunctionalCaseUsers.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCaseUser::getCaseId, Collectors.toList()));

        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andIdIn(caseIds);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        Map<String, String> caseProjectIdMap = functionalCases.stream().collect(Collectors.toMap(FunctionalCase::getId, FunctionalCase::getProjectId));

        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andRoleIdEqualTo(InternalUserRole.ADMIN.getValue()).andSourceIdEqualTo(UserRoleScope.SYSTEM).andOrganizationIdEqualTo(UserRoleScope.SYSTEM);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        List<String> systemUsers = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        CaseReviewHistoryMapper caseReviewHistoryMapper = sqlSession.getMapper(CaseReviewHistoryMapper.class);
        CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper = sqlSession.getMapper(CaseReviewFunctionalCaseMapper.class);

        Map<String, String> statusMap = new HashMap<>();
        for (CaseReviewFunctionalCase caseReviewFunctionalCase : caseReviewFunctionalCaseList) {
            //校验当前操作人是否是该用例的评审人或者是系统管理员，是增加评审历史，不是过滤掉
            String caseId = caseReviewFunctionalCase.getCaseId();
            List<CaseReviewFunctionalCaseUser> userList = reviewerMap.get(caseId);

            if (!systemUsers.contains(userId) && (CollectionUtils.isEmpty(userList) || CollectionUtils.isEmpty(userList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getUserId(), userId)).toList()))) {
                LogUtils.error(caseId + ": no review user, please check");
                continue;
            }
            boolean isAdmin = systemUsers.contains(userId) && (CollectionUtils.isEmpty(userList) || CollectionUtils.isEmpty(userList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getUserId(), userId)).toList()));
            CaseReviewHistory caseReviewHistory = buildCaseReviewHistory(request, userId, caseId);
            caseReviewHistoryMapper.insert(caseReviewHistory);
            if (caseHistoryMap.get(caseId) == null) {
                List<CaseReviewHistory> histories = new ArrayList<>();
                histories.add(caseReviewHistory);
                caseHistoryMap.put(caseId, histories);
            } else {
                caseHistoryMap.get(caseId).add(caseReviewHistory);
            }
            //根据评审规则更新用例评审和功能用例关系表中的状态 1.单人评审直接更新评审结果 2.多人评审需要计算
            setStatus(request, caseReviewFunctionalCase, caseHistoryMap, reviewerMap, isAdmin);
            statusMap.put(caseReviewFunctionalCase.getCaseId(), caseReviewFunctionalCase.getStatus());
            caseReviewFunctionalCaseMapper.updateByPrimaryKeySelective(caseReviewFunctionalCase);

            //检查是否有@，发送@通知
            if (StringUtils.isNotBlank(request.getNotifier())) {
                List<String> relatedUsers = Arrays.asList(request.getNotifier().split(";"));
                reviewSendNoticeService.sendNoticeCase(relatedUsers, userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_AT, reviewId);
            }
            //发送评审通过不通过通知（评审中不发）
            if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
                reviewSendNoticeService.sendNoticeCase(new ArrayList<>(), userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_FAIL, reviewId);
            }
            if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
                reviewSendNoticeService.sendNoticeCase(new ArrayList<>(), userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_PASSED, reviewId);
            }

            functionalCaseAttachmentService.uploadMinioFile(caseId, caseProjectIdMap.get(caseId), request.getReviewCommentFileIds(), userId, CaseFileSourceType.REVIEW_COMMENT.toString());
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

        Map<String, Object> param = new HashMap<>();
        Map<String, List<CaseReviewFunctionalCase>> collect = caseReviewFunctionalCaseList.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getStatus));
        Map<String, Integer> countMap = new HashMap<>();
        collect.forEach((k, v) -> {
            countMap.put(k, v.size());
        });
        param.put(CaseEvent.Param.CASE_IDS, CollectionUtils.isNotEmpty(caseIds) ? caseIds : new ArrayList<>());
        param.put(CaseEvent.Param.REVIEW_ID, reviewId);
        param.put(CaseEvent.Param.STATUS_MAP, statusMap);
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
        param.put(CaseEvent.Param.COUNT_MAP, countMap);
        provider.updateCaseReview(param);
    }


    private static void setStatus(BatchReviewFunctionalCaseRequest request, CaseReviewFunctionalCase caseReviewFunctionalCase, Map<String, List<CaseReviewHistory>> caseHistoryMap, Map<String, List<CaseReviewFunctionalCaseUser>> reviewerMap, boolean isAdmin) {
        if (StringUtils.equals(request.getReviewPassRule(), CaseReviewPassRule.SINGLE.toString())) {
            if (!StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()) && !isAdmin) {
                caseReviewFunctionalCase.setStatus(request.getStatus());
            }
        } else {
            if (isAdmin) {
                return;
            }
            //根据用例ID 查询所有评审人 再查所有评审人最后一次的评审结果（只有通过/不通过算结果）
            List<CaseReviewHistory> caseReviewHistoriesExp = caseHistoryMap.get(caseReviewFunctionalCase.getCaseId());
            Map<String, List<CaseReviewHistory>> hasReviewedUserMap = caseReviewHistoriesExp.stream().sorted(Comparator.comparingLong(CaseReviewHistory::getCreateTime).reversed()).collect(Collectors.groupingBy(CaseReviewHistory::getCreateUser, Collectors.toList()));
            List<CaseReviewFunctionalCaseUser> caseReviewFunctionalCaseUsersExp = reviewerMap.get(caseReviewFunctionalCase.getCaseId());
            AtomicInteger passCount = new AtomicInteger();
            AtomicInteger unPassCount = new AtomicInteger();
            hasReviewedUserMap.forEach((k, v) -> {
                if (StringUtils.equalsIgnoreCase(v.get(0).getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
                    passCount.set(passCount.get() + 1);
                }
                if (StringUtils.equalsIgnoreCase(v.get(0).getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
                    unPassCount.set(unPassCount.get() + 1);
                }
            });
            if (unPassCount.get() > 0) {
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
            } else if (caseReviewFunctionalCaseUsersExp != null && caseReviewFunctionalCaseUsersExp.size() > hasReviewedUserMap.size()) {
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
            } else {
                //检查是否全部是通过，全是才是PASS,否则是评审中
                if (passCount.get() == hasReviewedUserMap.size()) {
                    caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.PASS.toString());
                } else {
                    caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
                }
            }
        }
    }

    private static CaseReviewHistory buildCaseReviewHistory(BatchReviewFunctionalCaseRequest request, String userId, String caseId) {
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setId(IDGenerator.nextStr());
        caseReviewHistory.setReviewId(request.getReviewId());
        caseReviewHistory.setCaseId(caseId);
        caseReviewHistory.setStatus(request.getStatus());
        caseReviewHistory.setDeleted(false);
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
            if (StringUtils.isBlank(request.getContent())) {
                throw new MSException(Translator.get("case_review_content.not.exist"));
            }
        } else {
            if (StringUtils.isNotBlank(request.getContent())) {
                caseReviewHistory.setContent(request.getContent().getBytes());
            }
        }
        caseReviewHistory.setNotifier(request.getNotifier());
        caseReviewHistory.setCreateUser(userId);
        caseReviewHistory.setCreateTime(System.currentTimeMillis());
        return caseReviewHistory;
    }


    public void batchEditReviewUser(BatchEditReviewerRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            String reviewPassRule = caseReviewService.getReviewPassRule(request.getReviewId());
            //评审人处理
            List<CaseReviewFunctionalCase> cases = extCaseReviewFunctionalCaseMapper.getCaseIdsByIds(ids);
            handleReviewers(request, cases, reviewPassRule, userId);

        }
    }

    private void handleReviewers(BatchEditReviewerRequest request, List<CaseReviewFunctionalCase> cases, String reviewPassRule, String userId) {
        List<String> caseIds = cases.stream().map(CaseReviewFunctionalCase::getCaseId).toList();
        CaseReviewFunctionalCaseUserExample example = new CaseReviewFunctionalCaseUserExample();
        example.createCriteria().andCaseIdIn(caseIds).andReviewIdEqualTo(request.getReviewId());
        List<CaseReviewFunctionalCaseUser> oldReviewUsers = caseReviewFunctionalCaseUserMapper.selectByExample(example);
        Map<String, List<CaseReviewFunctionalCaseUser>> oldReviewUserMap = oldReviewUsers.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCaseUser::getCaseId));

        //处理评审人数据
        handleReviewCaseUsers(request, caseIds, oldReviewUserMap);

        if (CaseReviewPassRule.MULTIPLE.name().equals(reviewPassRule)) {
            //如果是多人评审 需要重新评估用例评审状态
            List<CaseReviewFunctionalCaseUser> newReviewers = caseReviewFunctionalCaseUserMapper.selectByExample(example);
            Map<String, List<CaseReviewFunctionalCaseUser>> newReviewersMap = newReviewers.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCaseUser::getCaseId));

            CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
            caseReviewHistoryExample.createCriteria().andCaseIdIn(caseIds).andReviewIdEqualTo(request.getReviewId()).andDeletedEqualTo(false);
            List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
            Map<String, List<CaseReviewHistory>> caseHistoryMap = caseReviewHistories.stream().collect(Collectors.groupingBy(CaseReviewHistory::getCaseId, Collectors.toList()));

            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            Map<String, String> statusMap = new HashMap<>();
            CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper = sqlSession.getMapper(CaseReviewFunctionalCaseMapper.class);
            cases.forEach(caseReview -> {
                String status = multipleReview(caseReview, caseHistoryMap.get(caseReview.getCaseId()), newReviewersMap.get(caseReview.getCaseId()), oldReviewUserMap.get(caseReview.getCaseId()));
                caseReview.setStatus(status);
                caseReviewFunctionalCaseMapper.updateByPrimaryKeySelective(caseReview);
                statusMap.put(caseReview.getCaseId(), caseReview.getStatus());
            });
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            Map<String, List<CaseReviewFunctionalCase>> collect = cases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getStatus));
            Map<String, Integer> countMap = new HashMap<>();
            collect.forEach((k, v) -> {
                countMap.put(k, v.size());
            });
            Map<String, Object> param = new HashMap<>();
            param.put(CaseEvent.Param.REVIEW_ID, request.getReviewId());
            param.put(CaseEvent.Param.USER_ID, userId);
            param.put(CaseEvent.Param.CASE_IDS, caseIds);
            param.put(CaseEvent.Param.COUNT_MAP, countMap);
            param.put(CaseEvent.Param.STATUS_MAP, statusMap);
            param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
            provider.updateCaseReview(param);
        }

    }

    private String multipleReview(CaseReviewFunctionalCase caseReviewFunctionalCase, List<CaseReviewHistory> reviewHistories, List<CaseReviewFunctionalCaseUser> newReviewers, List<CaseReviewFunctionalCaseUser> oldReviewers) {
        if (CollectionUtils.isNotEmpty(reviewHistories)) {
            List<String> historyUsers = reviewHistories.stream().map(CaseReviewHistory::getCreateUser).toList();
            List<String> newUsers = newReviewers.stream().map(CaseReviewFunctionalCaseUser::getUserId).toList();
            if (CollectionUtils.isEmpty(oldReviewers)) {
                oldReviewers = new ArrayList<>();
            }
            List<String> oldUsers = oldReviewers.stream().map(CaseReviewFunctionalCaseUser::getUserId).toList();

            if (CollectionUtils.isEqualCollection(newUsers, oldUsers)) {
                return caseReviewFunctionalCase.getStatus();
            }

            Collection intersection = CollectionUtils.intersection(historyUsers, newUsers);
            if (CollectionUtils.isNotEmpty(intersection)) {
                //存在已经评审的人 状态列表
                List<String> statusList = reviewHistories.stream().filter(item -> intersection.contains(item.getCreateUser())).map(CaseReviewHistory::getStatus).toList();
                if (statusList.contains(FunctionalCaseReviewStatus.UN_PASS.name())) {
                    return FunctionalCaseReviewStatus.UN_PASS.name();
                }
                long count = statusList.stream().filter(item -> StringUtils.equalsIgnoreCase(FunctionalCaseReviewStatus.PASS.name(), item)).count();
                if (count == statusList.size() && newUsers.size() <= oldUsers.size()) {
                    return FunctionalCaseReviewStatus.PASS.name();
                } else {
                    return FunctionalCaseReviewStatus.UNDER_REVIEWED.name();
                }
            } else {
                return FunctionalCaseReviewStatus.UN_REVIEWED.name();
            }
        } else {
            return FunctionalCaseReviewStatus.UN_REVIEWED.name();
        }
    }


    private void handleReviewCaseUsers(BatchEditReviewerRequest request, List<String> caseIds, Map<String, List<CaseReviewFunctionalCaseUser>> listMap) {
        if (request.isAppend()) {
            //追加评审人
            List<CaseReviewFunctionalCaseUser> list = new ArrayList<>();
            caseIds.forEach(caseId -> {
                //原评审人
                List<CaseReviewFunctionalCaseUser> users = listMap.get(caseId);

                //新评审人
                List<String> reviewerIds = request.getReviewerId();
                if (CollectionUtils.isNotEmpty(users)) {
                    List<String> userIds = users.stream().map(CaseReviewFunctionalCaseUser::getUserId).toList();
                    reviewerIds.removeAll(userIds);
                }
                reviewerIds.forEach(reviewer -> {
                    CaseReviewFunctionalCaseUser caseUser = new CaseReviewFunctionalCaseUser();
                    caseUser.setReviewId(request.getReviewId());
                    caseUser.setCaseId(caseId);
                    caseUser.setUserId(reviewer);
                    list.add(caseUser);
                });
            });
            if (CollectionUtils.isNotEmpty(list)) {
                caseReviewFunctionalCaseUserMapper.batchInsert(list);
            }
        } else {
            //更新评审人
            extCaseReviewFunctionalCaseUserMapper.deleteByCaseIds(caseIds, request.getReviewId());
            List<String> reviewerIds = request.getReviewerId();
            List<CaseReviewFunctionalCaseUser> list = new ArrayList<>();
            caseIds.forEach(caseId -> {
                reviewerIds.forEach(reviewer -> {
                    CaseReviewFunctionalCaseUser caseUser = new CaseReviewFunctionalCaseUser();
                    caseUser.setReviewId(request.getReviewId());
                    caseUser.setCaseId(caseId);
                    caseUser.setUserId(reviewer);
                    list.add(caseUser);
                });
            });
            caseReviewFunctionalCaseUserMapper.batchInsert(list);
        }
    }

    public List<BaseTreeNode> getTree(String reviewId) {
        List<BaseTreeNode> returnList = new ArrayList<>();
        List<FunctionalCaseModuleDTO> functionalModuleIds = extFunctionalCaseModuleMapper.selectBaseByProjectIdAndReviewId(reviewId);
        Map<String, List<FunctionalCaseModuleDTO>> projectModuleMap = functionalModuleIds.stream().collect(Collectors.groupingBy(FunctionalCaseModule::getProjectId));
        projectModuleMap.forEach((projectId,moduleList)->{
            BaseTreeNode projectNode = new BaseTreeNode(projectId, moduleList.get(0).getProjectName(), Project.class.getName());
            returnList.add(projectNode);
            List<String> projectModuleIds = moduleList.stream().map(FunctionalCaseModule::getId).toList();
            List<BaseTreeNode> nodeByNodeIds = functionalCaseModuleService.getNodeByNodeIds(projectModuleIds);
            List<BaseTreeNode> list = nodeByNodeIds.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getParentId(), ModuleConstants.DEFAULT_NODE_ID)).toList();
            boolean haveVirtualRootNode = list.isEmpty();
            List<BaseTreeNode> baseTreeNodes = functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, !haveVirtualRootNode, Translator.get("default.module"));
            for (BaseTreeNode baseTreeNode : baseTreeNodes) {
                projectNode.addChild(baseTreeNode);
            }
        });
        return returnList;
    }

    public Map<String, Long> moduleCount(ReviewFunctionalCasePageRequest request, boolean deleted, String userId) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<FunctionalCaseModuleCountDTO> projectModuleCountDTOList = extCaseReviewFunctionalCaseMapper.countModuleIdByRequest(request, deleted, userId);
        Map<String, List<FunctionalCaseModuleCountDTO>> projectCountMap = projectModuleCountDTOList.stream().collect(Collectors.groupingBy(FunctionalCaseModuleCountDTO::getProjectId));
        Map<String, Long> projectModuleCountMap = new HashMap<>();
        projectCountMap.forEach((projectId,moduleCountDTOList)->{
            List<ModuleCountDTO>moduleCountDTOS = new ArrayList<>();
            for (FunctionalCaseModuleCountDTO functionalCaseModuleCountDTO : moduleCountDTOList) {
                ModuleCountDTO moduleCountDTO = new ModuleCountDTO();
                BeanUtils.copyBean(moduleCountDTO,functionalCaseModuleCountDTO);
                moduleCountDTOS.add(moduleCountDTO);
            }
            int sum = moduleCountDTOList.stream().mapToInt(FunctionalCaseModuleCountDTO::getDataCount).sum();
            Map<String, Long> moduleCountMap = getModuleCountMap(projectId, request.getReviewId(), moduleCountDTOS);
            moduleCountMap.forEach((k,v)->{
                if (projectModuleCountMap.get(k)==null || projectModuleCountMap.get(k) == 0L){
                    projectModuleCountMap.put(k,v);
                }
            });
            projectModuleCountMap.put(projectId, (long)sum);
        });
        //查出全部用例数量
        long allCount = extCaseReviewFunctionalCaseMapper.caseCount(request, deleted, userId);
        projectModuleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return projectModuleCountMap;
    }

    /**
     * 查找当前项目下模块每个节点对应的资源统计
     */
    public Map<String, Long> getModuleCountMap(String projectId, String reviewId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(projectId, reviewId, moduleCountDTOList);

        //通过广度遍历的方式构建返回值
        return functionalCaseModuleService.getIdCountMapByBreadth(treeNodeList);
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, String reviewId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<String> moduleIds = extFunctionalCaseModuleMapper.selectIdByProjectIdAndReviewId(projectId, reviewId);
        List<BaseTreeNode> nodeByNodeIds = functionalCaseModuleService.getNodeByNodeIds(moduleIds);
        return functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, moduleCountDTOList, true, Translator.get("default.module"));
    }

    public List<OptionDTO> getUserStatus(String reviewId, String caseId) {
        List<CaseReviewHistoryDTO> list = extCaseReviewHistoryMapper.list(caseId, reviewId);
        Map<String, List<CaseReviewHistoryDTO>> collect = list.stream().sorted(Comparator.comparingLong(CaseReviewHistoryDTO::getCreateTime).reversed()).collect(Collectors.groupingBy(CaseReviewHistoryDTO::getCreateUser, Collectors.toList()));
        List<OptionDTO> optionDTOS = new ArrayList<>();
        collect.forEach((k, v) -> {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(v.get(0).getUserName());
            optionDTO.setName(v.get(0).getStatus());
            optionDTOS.add(optionDTO);
        });
        return optionDTOS;
    }
}