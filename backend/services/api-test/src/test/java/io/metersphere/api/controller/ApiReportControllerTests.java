package io.metersphere.api.controller;

import io.metersphere.api.constants.ShareInfoType;
import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiReportDetail;
import io.metersphere.api.domain.ApiReportStep;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportDTO;
import io.metersphere.api.dto.definition.ApiReportDetailDTO;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.mapper.ApiReportDetailMapper;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiReportStepMapper;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.sdk.mapper.ShareInfoMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiReportControllerTests extends BaseTest {

    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ApiReportStepMapper apiReportStepMapper;
    @Resource
    private ApiReportDetailMapper apiReportDetailMapper;
    @Resource
    private ShareInfoMapper shareInfoMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    private static final String BASIC = "/api/report/case";
    private static final String PAGE = BASIC + "/page";
    private static final String RENAME = BASIC + "/rename/";
    private static final String DELETE = BASIC + "/delete/";
    private static final String GET = BASIC + "/get/";
    private static final String BATCH_DELETE = BASIC + "/batch/delete";
    private static final String DETAIL = BASIC + "/get/detail/";


    @Test
    @Order(1)
    public void testInsert() {
        List<ApiReport> reports = new ArrayList<>();
        for (int i = 0; i < 2515; i++) {
            ApiReport apiReport = new ApiReport();
            apiReport.setId("api-report-id" + i);
            apiReport.setProjectId(DEFAULT_PROJECT_ID);
            apiReport.setName("api-report-name" + i);
            apiReport.setStartTime(System.currentTimeMillis());
            apiReport.setResourceId("api-resource-id" + i);
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
            apiReportStep.setStepId("api-report-step-id" + i);
            apiReportStep.setReportId("api-report-id-success" + i);
            apiReportStep.setSort(0L);
            apiReportStep.setStepType("case");
            steps.add(apiReportStep);
        }
        apiReportService.insertApiReportStep(steps);
    }

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    public static <T> T parseObjectFromMvcResult(MvcResult mvcResult, Class<T> parseClass) {
        try {
            String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            //返回请求正常
            Assertions.assertNotNull(resultHolder);
            return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), parseClass);
        } catch (Exception ignore) {
        }
        return null;
    }

    @Test
    @Order(2)
    public void testGetPage() throws Exception {
        ApiReportPageRequest request = new ApiReportPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProjectId(DEFAULT_PROJECT_ID);
        MvcResult mvcResult = responsePost(PAGE, request);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), request.getCurrent());
        ;
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiScenarioDTO>) returnPager.getList()).size() <= request.getPageSize());
        //过滤
        request.setFilter(new HashMap<>() {{
            put("status", List.of(ApiReportStatus.SUCCESS.name(), ApiReportStatus.ERROR.name()));
        }});
        mvcResult = responsePost(PAGE, request);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        Assertions.assertTrue(((List<ApiReport>) returnPager.getList()).size() <= request.getPageSize());
        List<ApiReport> list = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiReport.class);
        list.forEach(apiReport -> {
            Assertions.assertTrue(apiReport.getStatus().equals(ApiReportStatus.SUCCESS.name()) || apiReport.getStatus().equals(ApiReportStatus.ERROR.name()));
        });

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_REPORT_READ, PAGE, request);
    }

    protected ResultActions requestGetWithOk(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    public void testRename() throws Exception {
        // @@请求成功
        String newName = "api-report-new-name";
        requestGetWithOk(RENAME + "api-report-id0" + "/" + newName);
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey("api-report-id0");
        Assertions.assertNotNull(apiReport);
        Assertions.assertEquals(apiReport.getName(), newName);
        mockMvc.perform(getRequestBuilder(RENAME + "api-report" + "/" + newName))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_REPORT_UPDATE, RENAME + "api-report-id0" + "/" + newName);
    }

    @Test
    @Order(4)
    public void testDeleted() throws Exception {
        // @@请求成功
        requestGetWithOk(DELETE + "api-report-id0");
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey("api-report-id0");
        Assertions.assertNotNull(apiReport);
        Assertions.assertTrue(apiReport.getDeleted());
        mockMvc.perform(getRequestBuilder(RENAME + "api-report"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_REPORT_DELETE, DELETE + "api-report-id1");
    }

    @Test
    @Order(5)
    public void testBatch() throws Exception {
        // @@请求成功
        ApiReportBatchRequest request = new ApiReportBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectIds(List.of("api-report-id3"));
        request.setExcludeIds(List.of("api-report-id3"));
        responsePost(BATCH_DELETE, request);
        request.setSelectIds(List.of("api-report-id4"));
        responsePost(BATCH_DELETE, request);
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey("api-report-id4");
        Assertions.assertNotNull(apiReport);
        Assertions.assertTrue(apiReport.getDeleted());
        request.setSelectAll(true);
        responsePost(BATCH_DELETE, request);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_REPORT_DELETE, BATCH_DELETE, request);
    }

    @Test
    @Order(6)
    public void testGet() throws Exception {
        // @@请求成功
        List<ApiReport> reports = new ArrayList<>();
        ApiReport apiReport = new ApiReport();
        apiReport.setId("test-report-id");
        apiReport.setProjectId(DEFAULT_PROJECT_ID);
        apiReport.setName("test-report-name");
        apiReport.setStartTime(System.currentTimeMillis());
        apiReport.setResourceId("test-resource-id");
        apiReport.setCreateUser("admin");
        apiReport.setUpdateUser("admin");
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReport.setPoolId("api-pool-id");
        apiReport.setEnvironmentId("api-environment-id");
        apiReport.setRunMode("api-run-mode");
        apiReport.setStatus(ApiReportStatus.SUCCESS.name());
        apiReport.setTriggerMode("api-trigger-mode");
        apiReport.setVersionId("api-version-id");
        reports.add(apiReport);
        apiReportService.insertApiReport(reports);
        List<ApiReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ApiReportStep apiReportStep = new ApiReportStep();
            apiReportStep.setStepId("test-report-step-id" + i);
            apiReportStep.setReportId("test-report-id");
            apiReportStep.setSort((long) i);
            apiReportStep.setStepType("case");
            steps.add(apiReportStep);
        }

        apiReportService.insertApiReportStep(steps);
        MvcResult mvcResult = this.requestGetWithOk(GET + "test-report-id")
                .andReturn();
        ApiReportDTO apiReportDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiReportDTO.class);

        Assertions.assertNotNull(apiReportDTO);
        Assertions.assertEquals(apiReportDTO.getId(), "test-report-id");

        mockMvc.perform(getRequestBuilder(GET + "test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(getRequestBuilder(GET + "api-report-id10"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_REPORT_READ, GET + "api-report-id0");
    }

    @Test
    @Order(7)
    public void testGetDetail() throws Exception {
        // @@请求成功
        List<ApiReportDetail> reports = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ApiReportDetail apiReportDetail = new ApiReportDetail();
            apiReportDetail.setId("test-report-detail-id" + i);
            apiReportDetail.setReportId("test-report-id");
            apiReportDetail.setStepId("test-report-step-id1");
            apiReportDetail.setStatus("success");
            apiReportDetail.setResponseSize(0L);
            apiReportDetail.setRequestTime((long) i);
            apiReportDetail.setContent("{\"resourceId\":\"\",\"stepId\":null,\"threadName\":\"Thread Group\",\"name\":\"HTTP Request1\",\"url\":\"https://www.baidu.com/\",\"requestSize\":195,\"startTime\":1705570589125,\"endTime\":1705570589310,\"error\":1,\"headers\":\"Connection: keep-alive\\nContent-Length: 0\\nContent-Type: application/x-www-form-urlencoded; charset=UTF-8\\nHost: www.baidu.com\\nUser-Agent: Apache-HttpClient/4.5.14 (Java/21)\\n\",\"cookies\":\"\",\"body\":\"POST https://www.baidu.com/\\n\\nPOST data:\\n\\n\\n[no cookies]\\n\",\"status\":\"ERROR\",\"method\":\"POST\",\"assertionTotal\":1,\"passAssertionsTotal\":0,\"subRequestResults\":[],\"responseResult\":{\"responseCode\":\"200\",\"responseMessage\":\"OK\",\"responseTime\":185,\"latency\":180,\"responseSize\":2559,\"headers\":\"HTTP/1.1 200 OK\\nContent-Length: 2443\\nContent-Type: text/html\\nServer: bfe\\nDate: Thu, 18 Jan 2024 09:36:29 GMT\\n\",\"body\":\"<!DOCTYPE html>\\r\\n<!--STATUS OK--><html> <head><meta http-equiv=content-type content=text/html;charset=utf-8><meta http-equiv=X-UA-Compatible content=IE=Edge><meta content=always name=referrer><link rel=stylesheet type=text/css href=https://ss1.bdstatic.com/5eN1bjq8AAUYm2zgoY3K/r/www/cache/bdorz/baidu.min.css><title>百度一下，你就知道</title></head> <body link=#0000cc> <div id=wrapper> <div id=head> <div class=head_wrapper> <div class=s_form> <div class=s_form_wrapper> <div id=lg> <img hidefocus=true src=//www.baidu.com/img/bd_logo1.png width=270 height=129> </div> <form id=form name=f action=//www.baidu.com/s class=fm> <input type=hidden name=bdorz_come value=1> <input type=hidden name=ie value=utf-8> <input type=hidden name=f value=8> <input type=hidden name=rsv_bp value=1> <input type=hidden name=rsv_idx value=1> <input type=hidden name=tn value=baidu><span class=\\\"bg s_ipt_wr\\\"><input id=kw name=wd class=s_ipt value maxlength=255 autocomplete=off autofocus=autofocus></span><span class=\\\"bg s_btn_wr\\\"><input type=submit id=su value=百度一下 class=\\\"bg s_btn\\\" autofocus></span> </form> </div> </div> <div id=u1> <a href=http://news.baidu.com name=tj_trnews class=mnav>新闻</a> <a href=https://www.hao123.com name=tj_trhao123 class=mnav>hao123</a> <a href=http://map.baidu.com name=tj_trmap class=mnav>地图</a> <a href=http://v.baidu.com name=tj_trvideo class=mnav>视频</a> <a href=http://tieba.baidu.com name=tj_trtieba class=mnav>贴吧</a> <noscript> <a href=http://www.baidu.com/bdorz/login.gif?login&amp;tpl=mn&amp;u=http%3A%2F%2Fwww.baidu.com%2f%3fbdorz_come%3d1 name=tj_login class=lb>登录</a> </noscript> <script>document.write('<a href=\\\"http://www.baidu.com/bdorz/login.gif?login&tpl=mn&u='+ encodeURIComponent(window.location.href+ (window.location.search === \\\"\\\" ? \\\"?\\\" : \\\"&\\\")+ \\\"bdorz_come=1\\\")+ '\\\" name=\\\"tj_login\\\" class=\\\"lb\\\">登录</a>');\\r\\n                </script> <a href=//www.baidu.com/more/ name=tj_briicon class=bri style=\\\"display: block;\\\">更多产品</a> </div> </div> </div> <div id=ftCon> <div id=ftConw> <p id=lh> <a href=http://home.baidu.com>关于百度</a> <a href=http://ir.baidu.com>About Baidu</a> </p> <p id=cp>&copy;2017&nbsp;Baidu&nbsp;<a href=http://www.baidu.com/duty/>使用百度前必读</a>&nbsp; <a href=http://jianyi.baidu.com/ class=cp-feedback>意见反馈</a>&nbsp;京ICP证030173号&nbsp; <img src=//www.baidu.com/img/gs.gif> </p> </div> </div> </div> </body> </html>\\r\\n\",\"contentType\":\"text/html\",\"vars\":null,\"imageUrl\":null,\"socketInitTime\":14,\"dnsLookupTime\":0,\"tcpHandshakeTime\":0,\"sslHandshakeTime\":0,\"transferStartTime\":166,\"downloadTime\":5,\"bodySize\":2443,\"headerSize\":116,\"assertions\":[{\"name\":\"JSON Assertion\",\"content\":null,\"script\":null,\"message\":\"Expected to find an object with property ['test'] in path $ but found 'java.lang.String'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.\",\"pass\":false}]},\"isSuccessful\":false,\"fakeErrorMessage\":\"\",\"fakeErrorCode\":null}\n".getBytes());
            reports.add(apiReportDetail);
        }
        apiReportDetailMapper.batchInsert(reports);

        MvcResult mvcResult = this.requestGetWithOk(DETAIL + "test-report-id" + "/" + "test-report-step-id1")
                .andReturn();
        List<ApiReportDetailDTO> data = ApiDataUtils.parseArray(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiReportDetailDTO.class);
        Assertions.assertNotNull(data);

        mockMvc.perform(getRequestBuilder(DETAIL + "test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(getRequestBuilder(DETAIL + "api-report-id10"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_REPORT_READ, GET + "api-report-id0");
    }

    //分享报告
    @Test
    @Order(8)
    public void generateUrl() throws Exception {
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setCustomData("test-report-id".getBytes());
        shareInfo.setProjectId(DEFAULT_PROJECT_ID);
        shareInfo.setShareType(ShareInfoType.API_SHARE_REPORT.name());
        MvcResult mvcResult = responsePost("/api/report/share/gen", shareInfo);
        ShareInfoDTO shareInfoDTO = parseObjectFromMvcResult(mvcResult, ShareInfoDTO.class);
        Assertions.assertNotNull(shareInfoDTO);
        Assertions.assertNotNull(shareInfoDTO.getShareUrl());
        Assertions.assertNotNull(shareInfoDTO.getId());
        String shareId = shareInfoDTO.getId();
        MvcResult mvcResult1 = this.requestGetWithOk(GET + shareId + "/" + "test-report-id")
                .andReturn();
        ApiReportDTO apiReportDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult1).get("data")), ApiReportDTO.class);
        Assertions.assertNotNull(apiReportDTO);
        Assertions.assertEquals(apiReportDTO.getId(), "test-report-id");

        this.requestGetWithOk("/api/report/share/get/" + shareId)
                .andReturn();

        mockMvc.perform(getRequestBuilder("/api/report/share/get/" + "test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        mvcResult = this.requestGetWithOk(DETAIL + shareId + "/" + "test-report-id" + "/" + "test-report-step-id1")
                .andReturn();
        List<ApiReportDetailDTO> data = ApiDataUtils.parseArray(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiReportDetailDTO.class);
        Assertions.assertNotNull(data);
        //过期时间时间戳  1702950953000  2023-12-19 09:55:53
        ShareInfo shareInfo1 = shareInfoMapper.selectByPrimaryKey(shareId);
        shareInfo1.setUpdateTime(1702950953000L);
        shareInfoMapper.updateByPrimaryKey(shareInfo1);

        mockMvc.perform(getRequestBuilder(GET + shareId + "/" + "test-report-id"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        //TODO  过期的校验   未完成  需要补充
        //项目当前设置了分享时间  并且没有过期

        shareInfo = new ShareInfo();
        shareInfo.setCustomData("test-report-id".getBytes());
        shareInfo.setProjectId(DEFAULT_PROJECT_ID);
        shareInfo.setShareType(ShareInfoType.API_SHARE_REPORT.name());
        mvcResult = responsePost("/api/report/share/gen", shareInfo);
        shareInfoDTO = parseObjectFromMvcResult(mvcResult, ShareInfoDTO.class);
        Assertions.assertNotNull(shareInfoDTO);
        Assertions.assertNotNull(shareInfoDTO.getShareUrl());
        Assertions.assertNotNull(shareInfoDTO.getId());
        shareId = shareInfoDTO.getId();

        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andTypeEqualTo(ShareInfoType.API_SHARE_REPORT.name());
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(projectApplicationExample);
        if (projectApplications.isEmpty()) {
            ProjectApplication projectApplication = new ProjectApplication();
            projectApplication.setProjectId(DEFAULT_PROJECT_ID);
            projectApplication.setType(ShareInfoType.API_SHARE_REPORT.name());
            projectApplication.setTypeValue("1D");
            projectApplicationMapper.insert(projectApplication);
        }

        mvcResult1 = this.requestGetWithOk(GET + shareId + "/" + "test-report-id")
                .andReturn();
        apiReportDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult1).get("data")), ApiReportDTO.class);
        Assertions.assertNotNull(apiReportDTO);
        Assertions.assertEquals(apiReportDTO.getId(), "test-report-id");

        //项目当前设置了分享时间  并且过期
        shareInfo1 = shareInfoMapper.selectByPrimaryKey(shareId);
        shareInfo1.setUpdateTime(1702950953000L);
        shareInfoMapper.updateByPrimaryKey(shareInfo1);

        mockMvc.perform(getRequestBuilder(GET + shareId + "/" + "test-report-id"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

}
