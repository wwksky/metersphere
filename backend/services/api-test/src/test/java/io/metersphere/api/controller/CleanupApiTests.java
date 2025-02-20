package io.metersphere.api.controller;

import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.CleanupApiReportServiceImpl;
import io.metersphere.api.service.CleanupApiResourceServiceImpl;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.service.schedule.SwaggerUrlImportJob;
import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupApiTests {
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private CleanupApiResourceServiceImpl cleanupApiResourceServiceImpl;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private CleanupApiReportServiceImpl cleanupApiReportServiceImpl;

    @Autowired
    public CleanupApiTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public void initData() throws Exception {
        //创建接口模块
        ApiDefinitionModule apiDefinitionModule = new ApiDefinitionModule();
        apiDefinitionModule.setId("test-module");
        apiDefinitionModule.setProjectId("test");
        apiDefinitionModule.setName("test");
        apiDefinitionModule.setPos(1L);
        apiDefinitionModule.setCreateUser("admin");
        apiDefinitionModule.setUpdateUser("admin");
        apiDefinitionModule.setCreateTime(System.currentTimeMillis());
        apiDefinitionModule.setUpdateTime(System.currentTimeMillis());
        apiDefinitionModuleMapper.insertSelective(apiDefinitionModule);
        //创建场景模块
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId("test-scenario-module");
        apiScenarioModule.setProjectId("test");
        apiScenarioModule.setName("test");
        apiScenarioModule.setPos(1L);
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);
        //创建接口
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId("test");
        apiDefinition.setProjectId("test");
        apiDefinition.setModuleId("test-module");
        apiDefinition.setName("test");
        apiDefinition.setPath("test");
        apiDefinition.setProtocol("http");
        apiDefinition.setMethod("test");
        apiDefinition.setCreateUser("admin");
        apiDefinition.setUpdateUser("admin");
        apiDefinition.setNum(1L);
        apiDefinition.setVersionId("test");
        apiDefinition.setRefId("test");
        apiDefinition.setPos(1L);
        apiDefinition.setLatest(true);
        apiDefinition.setStatus("api-status");
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.insertSelective(apiDefinition);
        //创建用例
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId("test");
        apiTestCase.setProjectId("test");
        apiTestCase.setApiDefinitionId("test");
        apiTestCase.setCreateUser("admin");
        apiTestCase.setUpdateUser("admin");
        apiTestCase.setCreateTime(System.currentTimeMillis());
        apiTestCase.setUpdateTime(System.currentTimeMillis());
        apiTestCase.setPos(1L);
        apiTestCase.setNum(1L);
        apiTestCase.setStatus("test");
        apiTestCase.setVersionId("test");
        apiTestCase.setPriority("test");
        apiTestCase.setName("test");
        apiTestCaseMapper.insertSelective(apiTestCase);
        //创建mock
        ApiDefinitionMock apiDefinitionMock = new ApiDefinitionMock();
        apiDefinitionMock.setId("test");
        apiDefinitionMock.setApiDefinitionId("test");
        apiDefinitionMock.setProjectId("test");
        apiDefinitionMock.setCreateUser("admin");
        apiDefinitionMock.setCreateTime(System.currentTimeMillis());
        apiDefinitionMock.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMock.setName("test");
        apiDefinitionMock.setExpectNum("test");
        apiDefinitionMockMapper.insertSelective(apiDefinitionMock);


    }

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        initData();
        initReportData("test");
        initScheduleData();
        serviceInvoker.invokeServices("test");
        cleanupApiResourceServiceImpl.deleteResources("test");
    }

    private void initScheduleData() {
        Schedule schedule = new Schedule();
        schedule.setName("test");
        schedule.setResourceId("test");
        schedule.setEnable(true);
        schedule.setValue("test");
        schedule.setKey("test");
        schedule.setCreateUser("admin");
        schedule.setProjectId("test");
        schedule.setConfig("config");
        schedule.setJob(SwaggerUrlImportJob.class.getName());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setId(IDGenerator.nextStr());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        scheduleMapper.insertSelective(schedule);
    }

    @Test
    @Order(2)
    public void testCleanupReport() throws Exception {
        initReportData("test-clean-project");
        Map<String, String> map = new HashMap<>();
        map.put(ProjectApplicationType.API.API_CLEAN_REPORT.name(), "1D");
        cleanupApiReportServiceImpl.cleanReport(map, "test-clean-project");
    }

    private void initReportData(String projectId) {
        List<ApiReport> reports = new ArrayList<>();
        for (int i = 0; i < 2515; i++) {
            ApiReport apiReport = new ApiReport();
            apiReport.setId("clean-report-id" + projectId + i);
            apiReport.setProjectId(projectId);
            apiReport.setName("clean-report-name" + i);
            apiReport.setStartTime(1703174400000L);
            apiReport.setResourceId("clean-resource-id" + i);
            apiReport.setCreateUser("admin");
            apiReport.setUpdateUser("admin");
            apiReport.setUpdateTime(System.currentTimeMillis());
            apiReport.setPoolId("api-pool-id" + i);
            apiReport.setEnvironmentId("api-environment-id" + i);
            apiReport.setRunMode("api-run-mode" + i);
            if (i % 50 == 0) {
                apiReport.setStatus(ApiReportStatus.SUCCESS.name());
            } else if (i % 39 == 0) {
                apiReport.setStatus(ApiReportStatus.ERROR.name());
            }
            apiReport.setTriggerMode("api-trigger-mode" + i);
            apiReport.setVersionId("api-version-id" + i);
            reports.add(apiReport);
        }
        apiReportService.insertApiReport(reports);
        List<ApiReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 1515; i++) {
            ApiReportStep apiReportStep = new ApiReportStep();
            apiReportStep.setStepId("clean-api-report-step-id" + projectId + i);
            apiReportStep.setReportId("clean-report-id" + projectId + i);
            apiReportStep.setSort(0L);
            apiReportStep.setStepType("case");
            steps.add(apiReportStep);
        }
        apiReportService.insertApiReportStep(steps);

        List<ApiScenarioReport> scenarioReports = new ArrayList<>();
        for (int i = 0; i < 2515; i++) {
            ApiScenarioReport scenarioReport = new ApiScenarioReport();
            scenarioReport.setId("clean-scenario-report-id" + projectId + i);
            scenarioReport.setProjectId(projectId);
            scenarioReport.setName("clean--scenario-report-name" + i);
            scenarioReport.setStartTime(1703174400000L);
            scenarioReport.setScenarioId("scenario-scenario-id" + i);
            scenarioReport.setCreateUser("admin");
            scenarioReport.setUpdateUser("admin");
            if (i % 50 == 0) {
                scenarioReport.setStatus(ApiReportStatus.SUCCESS.name());
            } else if (i % 39 == 0) {
                scenarioReport.setStatus(ApiReportStatus.ERROR.name());
            }
            scenarioReport.setUpdateTime(System.currentTimeMillis());
            scenarioReport.setPoolId("api-pool-id" + i);
            scenarioReport.setEnvironmentId("api-environment-id" + i);
            scenarioReport.setRunMode("api-run-mode" + i);
            scenarioReport.setTriggerMode("api-trigger-mode" + i);
            scenarioReport.setVersionId("api-version-id" + i);
            scenarioReports.add(scenarioReport);
        }
        apiScenarioReportService.insertApiScenarioReport(scenarioReports);

        List<ApiScenarioReportStep> scenarioReportSteps = new ArrayList<>();
        for (int i = 0; i < 1515; i++) {
            ApiScenarioReportStep apiScenarioReportStep = new ApiScenarioReportStep();
            apiScenarioReportStep.setStepId("clean-step-id" + projectId + i);
            apiScenarioReportStep.setReportId("clean-scenario-report-id" + projectId + i);
            apiScenarioReportStep.setSort(0L);
            apiScenarioReportStep.setStepType("case");
            scenarioReportSteps.add(apiScenarioReportStep);
        }
        apiScenarioReportService.insertApiScenarioReportStep(scenarioReportSteps);
    }

}
