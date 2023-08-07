package io.metersphere.sdk.controller;


import io.metersphere.sdk.constants.UserSource;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.LoginRequest;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.service.BaseUserService;
import io.metersphere.sdk.service.LoginLogService;
import io.metersphere.sdk.util.RsaKey;
import io.metersphere.sdk.util.RsaUtil;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.sdk.util.Translator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Tag(name="登陆")
public class LoginController {

    @Resource
    private BaseUserService baseUserService;


    @GetMapping(value = "/is-login")
    @Operation(summary = "是否登录")
    public ResultHolder isLogin() throws Exception {
        RsaKey rsaKey = RsaUtil.getRsaKey();
        SessionUser user = SessionUtils.getUser();
        if (user != null) {
            UserDTO userDTO = baseUserService.getUserDTO(user.getId());
            if (StringUtils.isBlank(userDTO.getLanguage())) {
                userDTO.setLanguage(LocaleContextHolder.getLocale().toString());
            }

            baseUserService.autoSwitch(userDTO);
            SessionUser sessionUser = SessionUser.fromUser(userDTO, SessionUtils.getSessionId());
            SessionUtils.putUser(sessionUser);
            // 用户只有工作空间权限
            if (StringUtils.isBlank(sessionUser.getLastProjectId())) {
                sessionUser.setLastProjectId("no_such_project");
            }
            return ResultHolder.success(sessionUser);
        }
        throw new MSException(rsaKey.getPublicKey());
    }

    @PostMapping(value = "/login")
    @Operation(summary = "登录")
    @Log(type = OperationLogType.LOGIN, expression = "#msClass.loginLog()", msClass = LoginLogService.class)
    public ResultHolder login(@RequestBody LoginRequest request) {
        SessionUser sessionUser = SessionUtils.getUser();
        if (sessionUser != null) {
            if (!StringUtils.equals(sessionUser.getId(), request.getUsername())) {
                throw new MSException(Translator.get("please_logout_current_user"));
            }
        }
        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LOCAL.name());
        ResultHolder result = baseUserService.login(request);
        // 检查管理员是否需要改密码
        boolean changePassword = baseUserService.checkWhetherChangePasswordOrNot(request);
        result.setMessage(BooleanUtils.toStringTrueFalse(changePassword));
        return result;
    }

    @GetMapping(value = "/signout")
    @Operation(summary = "退出登录")
    @Log(type = OperationLogType.LOGOUT, expression = "#msClass.logoutLog()", msClass = LoginLogService.class)
    public ResultHolder logout(HttpServletResponse response) throws Exception {
        SecurityUtils.getSubject().logout();
        return ResultHolder.success("logout success");
    }

}