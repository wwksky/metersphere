package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.mapper.ApiReportDetailMapper;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiReportStepMapper;
import io.metersphere.api.mapper.ExtApiReportMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.sdk.dto.api.result.RequestResult;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiReportService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ExtApiReportMapper extApiReportMapper;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private ApiReportStepMapper apiReportStepMapper;
    @Resource
    private ApiReportDetailMapper apiReportDetailMapper;
    @Resource
    private ApiReportLogService apiReportLogService;


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiReport(List<ApiReport> reports) {
        if (CollectionUtils.isNotEmpty(reports)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiReportMapper reportMapper = sqlSession.getMapper(ApiReportMapper.class);
            SubListUtils.dealForSubList(reports, 1000, subList -> {
                subList.forEach(reportMapper::insertSelective);
            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiReportStep(List<ApiReportStep> reportSteps) {
        if (CollectionUtils.isNotEmpty(reportSteps)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiReportStepMapper stepMapper = sqlSession.getMapper(ApiReportStepMapper.class);
            SubListUtils.dealForSubList(reportSteps, 1000, subList -> {
                subList.forEach(stepMapper::insertSelective);
            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public List<ApiReport> getPage(ApiReportPageRequest request) {
        List<ApiReport> list = extApiReportMapper.list(request);
        //取所有的userid
        Set<String> userSet = list.stream()
                .flatMap(apiReport -> Stream.of(apiReport.getUpdateUser(), apiReport.getDeleteUser(), apiReport.getCreateUser()))
                .collect(Collectors.toSet());
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
        list.forEach(apiReport -> {
            apiReport.setCreateUser(userMap.get(apiReport.getCreateUser()));
            apiReport.setUpdateUser(userMap.get(apiReport.getUpdateUser()));
            apiReport.setDeleteUser(userMap.get(apiReport.getDeleteUser()));
        });
        return list;
    }

    public void rename(String id, String name, String userId) {
        ApiReport apiReport = checkResource(id);
        apiReport.setName(name);
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReport.setUpdateUser(userId);
        apiReportMapper.updateByPrimaryKeySelective(apiReport);
    }

    public void delete(String id, String userId) {
        ApiReport apiReport = checkResource(id);
        apiReport.setDeleted(true);
        apiReport.setDeleteTime(System.currentTimeMillis());
        apiReport.setDeleteUser(userId);
        apiReportMapper.updateByPrimaryKeySelective(apiReport);
    }

    private ApiReport checkResource(String id) {
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey(id);
        if (apiReport == null) {
            throw new RuntimeException(Translator.get("api_case_report_not_exist"));
        }
        return apiReport;
    }

    public void batchDelete(ApiReportBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        SubListUtils.dealForSubList(ids, 2000, subList -> {
            ApiReportExample example = new ApiReportExample();
            example.createCriteria().andIdIn(subList);
            ApiReport apiReport = new ApiReport();
            apiReport.setDeleted(true);
            apiReport.setDeleteTime(System.currentTimeMillis());
            apiReport.setDeleteUser(userId);
            apiReportMapper.updateByExampleSelective(apiReport, example);
            //TODO 记录日志
            apiReportLogService.batchDeleteLog(subList, userId, request.getProjectId());
        });
    }

    public List<String> doSelectIds(ApiReportBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extApiReportMapper.getIds(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            request.getSelectIds().removeAll(request.getExcludeIds());
            return request.getSelectIds();
        }
    }

    public ApiReportDTO get(String id) {
        ApiReportDTO apiReportDTO = new ApiReportDTO();
        ApiReport apiReport = checkResource(id);
        BeanUtils.copyBean(apiReportDTO, apiReport);
        //需要查询出所有的步骤
        List<ApiReportStepDTO> apiReportSteps = extApiReportMapper.selectStepsByReportId(id);
        if (CollectionUtils.isEmpty(apiReportSteps)) {
            throw new MSException(Translator.get("api_case_report_not_exist"));
        }
        apiReportSteps.sort(Comparator.comparingLong(ApiReportStepDTO::getSort));
        apiReportDTO.setChildren(apiReportSteps);
        return apiReportDTO;
    }

    public List<ApiReportDetailDTO> getDetail(String stepId, String reportId) {
        List<ApiReportDetail> apiReportDetails = checkResourceStep(stepId, reportId);
        List<ApiReportDetailDTO> results = new ArrayList<>();
        apiReportDetails.forEach(apiReportDetail -> {
            ApiReportDetailDTO apiReportDetailDTO = new ApiReportDetailDTO();
            BeanUtils.copyBean(apiReportDetailDTO, apiReportDetail);
            apiReportDetailDTO.setContent(ApiDataUtils.parseObject(new String(apiReportDetail.getContent()), RequestResult.class));
            results.add(apiReportDetailDTO);
        });
        return results;
    }

    private List<ApiReportDetail> checkResourceStep(String stepId, String reportId) {
        ApiReportDetailExample apiReportDetailExample = new ApiReportDetailExample();
        apiReportDetailExample.createCriteria().andStepIdEqualTo(stepId).andReportIdEqualTo(reportId);
        List<ApiReportDetail> apiReportDetails = apiReportDetailMapper.selectByExampleWithBLOBs(apiReportDetailExample);
        if (CollectionUtils.isEmpty(apiReportDetails)) {
            throw new MSException(Translator.get("api_case_report_not_exist"));
        }
        return apiReportDetails;
    }
}
