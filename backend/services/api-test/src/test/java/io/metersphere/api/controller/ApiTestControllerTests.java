package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiTestPluginOptionRequest;
import io.metersphere.api.service.BaseResourcePoolTestService;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.dto.environment.KeyValueParam;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.base.BasePluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.service.PluginService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  17:07
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiTestControllerTests extends BaseTest {

    private static final String BASE_PATH = "/api/test/";
    protected static final String PROTOCOL_LIST = "protocol/{0}";
    protected static final String MOCK = "mock/{0}";
    protected static final String CUSTOM_FUNC_RUN = "custom/func/run";
    protected static final String PLUGIN_FORM_OPTION = "plugin/form/option";

    @Resource
    private BaseResourcePoolTestService baseResourcePoolTestService;
    @Resource
    private PluginService pluginService;
    @Resource
    private BasePluginTestService basePluginTestService;
    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    public void getProtocols() throws Exception {
        // @@请求成功
        this.requestGetWithOk(PROTOCOL_LIST, this.DEFAULT_ORGANIZATION_ID).andReturn();
    }

    @Test
    public void getMock() throws Exception {
        // @@请求成功
        this.requestGetWithOk(MOCK, "@integer").andReturn();
    }

    @Test
    public void runCustomFunc() throws Exception {
        mockPost("/api/debug", "");
        // 初始化资源池
        baseResourcePoolTestService.initProjectResourcePool();

        CustomFunctionRunRequest request = new CustomFunctionRunRequest();
        request.setReportId(IDGenerator.nextStr());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ScriptLanguageType.BEANSHELL.getValue());
        request.setScript("""
                    log.info("========");
                    log.info("${test}");
                """);
        // @@请求成功
        this.requestPostWithOk(CUSTOM_FUNC_RUN, request);

        KeyValueParam keyValueParam = new KeyValueParam();
        keyValueParam.setKey("test");
        keyValueParam.setValue("value");
        request.setParams(List.of(keyValueParam));
        // @@请求成功
        this.requestPostWithOk(CUSTOM_FUNC_RUN, request);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_EXECUTE, CUSTOM_FUNC_RUN, request);
    }

    @Test
    public void getFormOptions() throws Exception {
        ApiTestPluginOptionRequest request = new ApiTestPluginOptionRequest();
        request.setOrgId(DEFAULT_ORGANIZATION_ID);
        request.setQueryParam(new HashMap<>());
        request.setPluginId("aaa");
        request.setOptionMethod("testGetApiPluginSelectOption");
        assertErrorCode(this.requestPost(PLUGIN_FORM_OPTION, request), NOT_FOUND);
        Plugin jiraPlugin = basePluginTestService.addJiraPlugin();
        request.setPluginId(jiraPlugin.getId());
        assertErrorCode(this.requestPost(PLUGIN_FORM_OPTION, request), NOT_FOUND);
        // @@请求成功
        Plugin plugin = addOptionTestPlugin();
        request.setPluginId(plugin.getId());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PLUGIN_FORM_OPTION, request);
        List<ApiPluginSelectOption> options = getResultDataArray(mvcResult, ApiPluginSelectOption.class);
        Assertions.assertEquals(options.size(), 1);

        basePluginTestService.deleteJiraPlugin();
        pluginService.delete(plugin.getId());

        // @@校验权限
        requestPostPermissionsTest(new ArrayList<>() {{
            add(PermissionConstants.PROJECT_API_DEFINITION_READ);
            add(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
            add(PermissionConstants.PROJECT_API_DEBUG_READ);
            add(PermissionConstants.PROJECT_API_SCENARIO_READ);
        }}, PLUGIN_FORM_OPTION, request);
    }

    public Plugin addOptionTestPlugin() throws Exception {
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/dubbo-option-test-sampler-v3.x.jar")
                        .getPath()
        );
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("测试获取选项插件");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        return pluginService.add(request, mockMultipartFile);
    }

}
