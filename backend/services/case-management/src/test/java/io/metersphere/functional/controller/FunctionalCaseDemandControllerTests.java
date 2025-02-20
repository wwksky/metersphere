package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.domain.FunctionalCaseDemandExample;
import io.metersphere.functional.dto.DemandDTO;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import io.metersphere.functional.mapper.FunctionalCaseDemandMapper;
import io.metersphere.functional.request.FunctionalCaseDemandRequest;
import io.metersphere.functional.request.FunctionalThirdDemandPageRequest;
import io.metersphere.functional.request.QueryDemandListRequest;
import io.metersphere.plugin.platform.dto.reponse.PlatformDemandDTO;
import io.metersphere.plugin.platform.utils.PluginPager;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.OperationLog;
import io.metersphere.sdk.domain.OperationLogExample;
import io.metersphere.sdk.mapper.OperationLogMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BasePluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.mapper.SystemParameterMapper;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseDemandControllerTests extends BaseTest {

    @Resource
    private FunctionalCaseDemandMapper functionalCaseDemandMapper;
    @Resource
    private OperationLogMapper operationLogMapper;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private BasePluginTestService basePluginTestService;
    @Resource
    private MockServerClient mockServerClient;
    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private int mockServerHostPort;



    private static final String URL_DEMAND_PAGE = "/functional/case/demand/page";
    private static final String URL_DEMAND_ADD = "/functional/case/demand/add";
    private static final String URL_DEMAND_UPDATE = "/functional/case/demand/update";
    private static final String URL_DEMAND_CANCEL = "/functional/case/demand/cancel/";
    private static final String URL_DEMAND_BATCH_RELEVANCE = "/functional/case/demand/batch/relevance";
    private static final String URL_DEMAND_PAGE_DEMAND = "/functional/case/demand/third/list/page";


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_case_demand.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void addDemandSuccess() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入1");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertFalse(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入孩子");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertFalse(functionalCaseDemands.isEmpty());
    }

    @Test
    @Order(2)
    public void addDemandEmpty() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID3");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入3");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID3");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertFalse(functionalCaseDemands.isEmpty());
    }

    @Test
    @Order(3)
    public void addDemandFalse() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_ADD, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_ADD, functionalCaseDemandRequest).andExpect(status().is4xxClientError());
    }

    @Test
    @Order(4)
    public void updateDemandSuccess() throws Exception {
        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setParamKey("ui.platformName");
        systemParameter.setParamValue("Metersphere");
        systemParameter.setType("text");
        systemParameterMapper.insertSelective(systemParameter);
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCaseDemandExample functionalCaseDemandExample;
        List<FunctionalCaseDemand> functionalCaseDemands;
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId(id);
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入2");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_UPDATE, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(StringUtils.equals(functionalCaseDemands.get(0).getDemandName(), "手动加入2"));
    }

    @Test
    @Order(5)
    public void updateDemandEmpty() throws Exception {
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCaseDemandExample functionalCaseDemandExample;
        List<FunctionalCaseDemand> functionalCaseDemands;
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId(id);
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_UPDATE, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(StringUtils.equals(functionalCaseDemands.get(0).getDemandName(), "手动加入2"));
    }

    @Test
    @Order(6)
    public void updateDemandFalse() throws Exception {
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCaseDemandExample functionalCaseDemandExample;
        List<FunctionalCaseDemand> functionalCaseDemands;
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId(id);
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_UPDATE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertNull(functionalCaseDemands.get(0).getDemandId());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId("hehe");
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandDTO.setDemandName("手动执行2");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_UPDATE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandDTO.setDemandName("手动执行2");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_UPDATE, functionalCaseDemandRequest).andExpect(status().is4xxClientError());
    }

    private String getId(String caseId) {
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo(caseId);
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        return functionalCaseDemands.get(0).getId();
    }

    @Test
    @Order(7)
    public void getDemandList() throws Exception {
        QueryDemandListRequest queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        Pager<List<FunctionalDemandDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());

        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(tableData.getList())).size() <= queryDemandListRequest.getPageSize());
        queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());
        //返回的数据量为空
        Assertions.assertTrue(CollectionUtils.isEmpty(tableData.getList()));

        queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID3");
        mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(tableData.getList())).size() <= queryDemandListRequest.getPageSize());
    }

    @NotNull
    private static QueryDemandListRequest getQueryDemandListRequest(String caseId) {
        QueryDemandListRequest queryDemandListRequest = new QueryDemandListRequest();
        queryDemandListRequest.setProjectId("project-case-demand-test");
        queryDemandListRequest.setCurrent(1);
        queryDemandListRequest.setPageSize(5);
        queryDemandListRequest.setCaseId(caseId);
        return queryDemandListRequest;
    }

    @Test
    @Order(8)
    public void cancelDemand() throws Exception {
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        List<FunctionalCaseDemand> beforeList = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        mockMvc.perform(MockMvcRequestBuilders.get(URL_DEMAND_CANCEL+id).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        List<FunctionalCaseDemand> after = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(beforeList.size()>after.size());
        checkLog("DEMAND_TEST_FUNCTIONAL_CASE_ID", OperationLogType.DISASSOCIATE);
    }

    @Test
    @Order(9)
    public void batchRelevanceSuccess() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("TAPD");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("100001");
        demandDTO.setDemandName("手动加入Tapd");
        demandList.add(demandDTO);
        DemandDTO demandDTO2 = new DemandDTO();
        demandDTO2.setDemandId("100002");
        demandDTO2.setParent("100001");
        demandDTO2.setDemandName("手动加入Tapd1");
        demandDTO2.setDemandUrl("https://www.tapd.cn/55049933/prong/stories/view/1155049933001012783");
        demandList.add(demandDTO2);
        DemandDTO demandDTO3 = new DemandDTO();
        demandDTO3.setDemandId("100003");
        demandDTO3.setParent("100002");
        demandDTO3.setDemandName("手动加入Tapd2");
        demandList.add(demandDTO3);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertEquals(functionalCaseDemands.size(), demandList.size());

        QueryDemandListRequest queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        Pager<List<FunctionalDemandDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        List<FunctionalDemandDTO> list1 = JSON.parseArray(JSON.toJSONString(tableData.getList()), FunctionalDemandDTO.class);
        for (FunctionalDemandDTO functionalDemandDTO : list1) {
            Assertions.assertTrue(CollectionUtils.isNotEmpty(functionalDemandDTO.getChildren()));
        }
    }

    @Test
    @Order(10)
    public void batchRelevanceEmpty() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("ZanDao");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2").andDemandPlatformEqualTo("ZanDao");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(functionalCaseDemands));

        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2").andDemandPlatformEqualTo("TAPD");
        List<FunctionalCaseDemand> functionalCaseDemandOld = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);


        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("TAPD");
        demandList = new ArrayList<>();
        DemandDTO demandDTO3 = new DemandDTO();
        demandDTO3.setDemandId("100003");
        demandDTO3.setParent("100002");
        demandDTO3.setDemandName("手动加入Tapd2");
        demandList.add(demandDTO3);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandRequest);

        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2").andDemandPlatformEqualTo("TAPD");
        List<FunctionalCaseDemand> functionalCaseDemandNew = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);

        Assertions.assertEquals(functionalCaseDemandOld.size(), functionalCaseDemandNew.size());
    }

    @Test
    @Order(11)
    public void batchRelevanceFalse() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("jira");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("100005");
        demandDTO.setDemandName("手动加入jira");
        demandList.add(demandDTO);
        DemandDTO demandDTO2 = new DemandDTO();
        demandDTO2.setDemandId("100006");
        demandList.add(demandDTO2);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2").andDemandPlatformEqualTo("jira");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(functionalCaseDemands));

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("jira");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("100007");
        demandDTO.setDemandName("手动加入jira");
        demandList.add(demandDTO);
        demandDTO2 = new DemandDTO();
        demandDTO2.setDemandName("手动加入jira2");
        demandList.add(demandDTO2);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(12)
    public void cancelDemandNoLog() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL_DEMAND_CANCEL+"DEMAND_TEST_FUNCTIONAL_CASE_X").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_X");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(functionalCaseDemands));
        OperationLogExample example = new OperationLogExample();
        example.createCriteria().andSourceIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_X").andTypeEqualTo(OperationLogType.DISASSOCIATE.name());
        List<OperationLog> operationLogs = operationLogMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isEmpty(operationLogs));

    }

    @Test
    @Order(13)
    public void pageDemandSuccess() throws Exception {
        basePluginTestService.addJiraPlugin();
        basePluginTestService.addServiceIntegration(DEFAULT_ORGANIZATION_ID);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/rest/api/2/search"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("{\"id\":\"123456\",\"name\":\"test\"}")

                );
        FunctionalThirdDemandPageRequest functionalThirdDemandPageRequest = new FunctionalThirdDemandPageRequest();
        functionalThirdDemandPageRequest.setProjectId("gyq_project-case-demand-test");
        functionalThirdDemandPageRequest.setPageSize(10);
        functionalThirdDemandPageRequest.setCurrent(1);
        MvcResult mvcResultDemand= this.requestPostWithOkAndReturn(URL_DEMAND_PAGE_DEMAND, functionalThirdDemandPageRequest);
        PluginPager<PlatformDemandDTO> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultDemand.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                PluginPager.class);

        System.out.println(JSON.toJSONString(tableData));
    }

}
