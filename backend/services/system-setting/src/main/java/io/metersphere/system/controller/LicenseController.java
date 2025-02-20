package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.LicenseDTO;
import io.metersphere.system.service.LicenseService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/license")
@Tag(name = "系统设置-系统-授权管理")
public class LicenseController {
    @GetMapping("/validate")
    @Operation(summary = "license校验")
    public LicenseDTO validate() {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            return licenseService.validate();
        }
        return new LicenseDTO();
    }

    @PostMapping("/add")
    @Operation(summary = "添加有效的License")
    @RequiresPermissions(value= {PermissionConstants.SYSTEM_AUTH_READ, PermissionConstants.SYSTEM_AUTH_READ_UPDATE}, logical = Logical.OR)
    public LicenseDTO addLicense(@RequestBody String licenseCode) {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        // 前端传入的JSON会被加上双引号，需要去掉
        licenseCode = StringUtils.strip(licenseCode, "\"");
        if (licenseService != null) {
            return licenseService.addLicense(licenseCode, SessionUtils.getUserId());
        }
        return new LicenseDTO();
    }
}
