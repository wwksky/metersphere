package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiTestPluginOptionRequest;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.jmeter.mock.Mock;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.ProtocolDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  10:54
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "接口测试")
public class ApiTestController {

    @Resource
    private ApiTestService apiTestService;
    @Resource
    private ApiExecuteService apiExecuteService;

    @GetMapping("/protocol/{organizationId}")
    @Operation(summary = "获取协议插件的的协议列表")
    public List<ProtocolDTO> getProtocols(@PathVariable String organizationId) {
        return apiTestService.getProtocols(organizationId);
    }


    @GetMapping("/mock/{key}")
    @Operation(summary = "获取mock数据")
    public String mock(@PathVariable String key) {
        return Mock.calculate(key).toString();
    }

    @PostMapping("/custom/func/run")
    @Operation(summary = "项目管理-公共脚本-脚本测试")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_EXECUTE)
    public String run(@Validated @RequestBody CustomFunctionRunRequest runRequest) {
        return apiExecuteService.runScript(runRequest);
    }

    @PostMapping("/plugin/form/option")
    @Operation(summary = "接口测试-获取插件表单选项")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_DEBUG_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ
    }, logical = Logical.OR)
    public List<ApiPluginSelectOption> getFormOptions(@Validated @RequestBody ApiTestPluginOptionRequest request) {
        return apiTestService.getFormOptions(request);
    }
}
