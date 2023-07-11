package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.PermissionDefinitionItem;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.request.OrganizationUserRoleEditRequest;
import io.metersphere.system.request.OrganizationUserRoleMemberEditRequest;
import io.metersphere.system.request.OrganizationUserRoleMemberRequest;
import io.metersphere.system.service.OrganizationUserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Tag(name = "组织-用户组与权限")
@RestController
@RequestMapping("/user/role/organization")
public class OrganizationUserRoleController {

    @Resource
    OrganizationUserRoleService organizationUserRoleService;

    @GetMapping("/list/{organizationId}")
    @Operation(summary = "获取组织用户组列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public List<UserRole> list(@PathVariable String organizationId) {
        return organizationUserRoleService.list(organizationId);
    }

    @PostMapping("/add")
    @Operation(summary = "添加组织用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_ADD)
    public UserRole add(@Validated @RequestBody OrganizationUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getUserId());
        BeanUtils.copyBean(userRole, request);
        return organizationUserRoleService.add(userRole);
    }

    @PostMapping("/update")
    @Operation(summary = "修改组织用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    public UserRole update(@Validated @RequestBody OrganizationUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyBean(userRole, request);
        return organizationUserRoleService.update(userRole);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除组织用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_DELETE)
    public void delete(@PathVariable String id) {
        organizationUserRoleService.delete(id);
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "获取组织用户组对应的权限配置")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return organizationUserRoleService.getPermissionSetting(id);
    }

    @PostMapping("/permission/update")
    @Operation(summary = "编辑组织用户组对应的权限配置")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        organizationUserRoleService.updatePermissionSetting(request);
    }

    @PostMapping("/list-member")
    @Operation(summary = "获取组织用户组-成员")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_USER_ROLE_READ, PermissionConstants.SYSTEM_USER_READ})
    public Pager<List<User>> listMember(@Validated @RequestBody OrganizationUserRoleMemberRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, organizationUserRoleService.listMember(request));
    }

    @PostMapping("/add-member")
    @Operation(summary = "添加组织用户组成员")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    public void addMember(@Validated @RequestBody OrganizationUserRoleMemberEditRequest request) {
        organizationUserRoleService.addMember(request, SessionUtils.getUserId());
    }

    @PostMapping("/remove-member")
    @Operation(summary = "删除组织用户组成员")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    public void removeMember(@Validated @RequestBody OrganizationUserRoleMemberEditRequest request) {
        organizationUserRoleService.removeMember(request);
    }
}
