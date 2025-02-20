package io.metersphere.system.service;

import com.alibaba.excel.EasyExcelFactory;
import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.constants.UserSource;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.excel.UserExcel;
import io.metersphere.system.dto.excel.UserExcelRowDTO;
import io.metersphere.system.dto.request.UserInviteRequest;
import io.metersphere.system.dto.request.UserRegisterRequest;
import io.metersphere.system.dto.request.user.PersonalUpdatePasswordRequest;
import io.metersphere.system.dto.request.user.PersonalUpdateRequest;
import io.metersphere.system.dto.request.user.UserChangeEnableRequest;
import io.metersphere.system.dto.request.user.UserEditRequest;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.ExcelParseDTO;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.table.TableBatchProcessResponse;
import io.metersphere.system.dto.user.PersonalDTO;
import io.metersphere.system.dto.user.UserCreateInfo;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.dto.user.request.UserBatchCreateRequest;
import io.metersphere.system.dto.user.response.UserBatchCreateResponse;
import io.metersphere.system.dto.user.response.UserImportResponse;
import io.metersphere.system.dto.user.response.UserInviteResponse;
import io.metersphere.system.dto.user.response.UserTableResponse;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.*;
import io.metersphere.system.notice.sender.impl.MailNoticeSender;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.system.utils.UserImportEventListener;
import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserExtendMapper userExtendMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Resource
    private UserInviteService userInviteService;
    @Resource
    private UserRoleRelationService userRoleRelationService;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private GlobalUserRoleService globalUserRoleService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserLogService userLogService;
    @Resource
    private UserToolService userToolService;

    @Value("50MB")
    private DataSize maxImportFileSize;

    private Map<String, String> validateUserInfo(Collection<String> createEmails) {
        Map<String, String> errorMessage = new HashMap<>();
        String userEmailRepeatError = Translator.get("user.email.repeat");
        //判断参数内是否含有重复邮箱
        List<String> emailList = new ArrayList<>();
        var userInDbMap = baseUserMapper.selectUserIdByEmailList(createEmails)
                .stream().collect(Collectors.toMap(User::getEmail, User::getId));
        for (String createEmail : createEmails) {
            if (emailList.contains(createEmail)) {
                errorMessage.put(createEmail, userEmailRepeatError);
            } else {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(createEmail)) {
                    errorMessage.put(createEmail, userEmailRepeatError);
                } else {
                    emailList.add(createEmail);
                }
            }
        }
        return errorMessage;
    }

    public UserBatchCreateResponse addUser(UserBatchCreateRequest userCreateDTO, String source, String operator) {

        //检查用户权限的合法性
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(userCreateDTO.getUserRoleIdList(), true);

        UserBatchCreateResponse response = new UserBatchCreateResponse();
        //检查用户邮箱的合法性
        Map<String, String> errorEmails = this.validateUserInfo(userCreateDTO.getUserInfoList().stream().map(UserCreateInfo::getEmail).toList());
        if (MapUtils.isNotEmpty(errorEmails)) {
            response.setErrorEmails(errorEmails);
        } else {
            response.setSuccessList(this.saveUserAndRole(userCreateDTO, source, operator, "/system/user/addUser"));
        }
        return response;
    }

    private List<UserCreateInfo> saveUserAndRole(UserBatchCreateRequest userCreateDTO, String source, String operator, String requestPath) {
        List<UserCreateInfo> insertList = new ArrayList<>();
        long createTime = System.currentTimeMillis();
        List<User> saveUserList = new ArrayList<>();
        //添加用户
        for (UserCreateInfo userInfo : userCreateDTO.getUserInfoList()) {
            userInfo.setId(IDGenerator.nextStr());
            User user = new User();
            BeanUtils.copyBean(user, userInfo);
            user.setCreateUser(operator);
            user.setCreateTime(createTime);
            user.setUpdateUser(operator);
            user.setUpdateTime(createTime);
            user.setPassword(CodingUtils.md5(user.getEmail()));
            user.setSource(source);
            user.setDeleted(false);
            userMapper.insertSelective(user);
            saveUserList.add(user);
            insertList.add(userInfo);
        }
        userRoleRelationService.batchSave(userCreateDTO.getUserRoleIdList(), saveUserList);
        //写入操作日志
        operationLogService.batchAdd(userLogService.getBatchAddLogs(saveUserList, requestPath));
        return insertList;
    }

    public UserDTO getUserDTOByKeyword(String email) {
        UserDTO userDTO = baseUserMapper.selectDTOByKeyword(email);
        if (userDTO != null) {
            userDTO.setUserRoleRelations(
                    userRoleRelationService.selectByUserId(userDTO.getId())
            );
            userDTO.setUserRoles(
                    userRoleService.selectByUserRoleRelations(userDTO.getUserRoleRelations())
            );
        }
        return userDTO;
    }

    public PersonalDTO getPersonalById(String id) {
        UserDTO userDTO = baseUserMapper.selectDTOByKeyword(id);
        PersonalDTO personalDTO = new PersonalDTO();
        if (userDTO != null) {
            BeanUtils.copyBean(personalDTO, userDTO);
            personalDTO.setOrgProjectList(userRoleRelationService.selectOrganizationProjectByUserId(userDTO.getId()));
        }
        return personalDTO;
    }

    public List<UserTableResponse> list(BasePageRequest request) {
        List<UserTableResponse> returnList = new ArrayList<>();
        List<User> userList = baseUserMapper.selectByKeyword(request.getKeyword(), false);
        if (CollectionUtils.isNotEmpty(userList)) {
            List<String> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
            Map<String, UserTableResponse> roleAndOrganizationMap = userRoleRelationService.selectGlobalUserRoleAndOrganization(userIdList);
            for (User user : userList) {
                UserTableResponse userInfo = new UserTableResponse();
                BeanUtils.copyBean(userInfo, user);
                UserTableResponse roleOrgModel = roleAndOrganizationMap.get(user.getId());
                if (roleOrgModel != null) {
                    userInfo.setUserRoleList(roleOrgModel.getUserRoleList());
                    userInfo.setOrganizationList(roleOrgModel.getOrganizationList());
                }
                returnList.add(userInfo);
            }
        }
        return returnList;
    }

    public void checkUserEmail(String id, String email) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andEmailEqualTo(email).andIdNotEqualTo(id);
        if (userMapper.countByExample(userExample) > 0) {
            throw new MSException(Translator.get("user_email_already_exists"));
        }
    }

    private void checkOldPassword(String id, String password) {
        if (extUserMapper.countByIdAndPassword(id, password) != 1) {
            throw new MSException(Translator.get("password_modification_failed"));
        }
    }

    public UserEditRequest updateUser(UserEditRequest userEditRequest, String operator) {
        //检查用户组合法性
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(userEditRequest.getUserRoleIdList(), true);
        //检查用户邮箱的合法性
        this.checkUserEmail(userEditRequest.getId(), userEditRequest.getEmail());

        User user = new User();
        BeanUtils.copyBean(user, userEditRequest);
        user.setUpdateUser(operator);
        user.setUpdateTime(System.currentTimeMillis());
        user.setCreateUser(null);
        user.setCreateTime(null);
        userMapper.updateByPrimaryKeySelective(user);
        userRoleRelationService.updateUserSystemGlobalRole(user, user.getUpdateUser(), userEditRequest.getUserRoleIdList());
        return userEditRequest;
    }

    public TableBatchProcessResponse updateUserEnable(UserChangeEnableRequest request, String operatorId, String operatorName) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        this.checkUserInDb(request.getSelectIds());

        if (!request.isEnable()) {
            //不能禁用当前用户和admin
            this.checkProcessUserAndThrowException(request.getSelectIds(), operatorId, operatorName, Translator.get("user.not.disable"));
        }

        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(
                request.getSelectIds()
        );

        User updateUser = new User();
        updateUser.setEnable(request.isEnable());
        updateUser.setUpdateUser(operatorId);
        updateUser.setUpdateTime(System.currentTimeMillis());
        response.setSuccessCount(userMapper.updateByExampleSelective(updateUser, userExample));

        if (BooleanUtils.isFalse(request.isEnable())) {
            //如果是禁用，批量踢出用户
            request.getSelectIds().forEach(SessionUtils::kickOutUser);
        }

        return response;
    }

    private void checkUserInDb(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        List<String> userInDb = baseUserMapper.selectUnDeletedUserIdByIdList(userIdList);
        if (userIdList.size() != userInDb.size()) {
            throw new MSException(Translator.get("user.not.exist"));
        }
    }

    public UserImportResponse importByExcel(MultipartFile excelFile, String source, String sessionId) {

        if (excelFile.getSize() > maxImportFileSize.toBytes()) {
            throw new MSException(Translator.get("file.size.is.too.large"));
        }

        UserImportResponse importResponse = new UserImportResponse();
        try {
            ExcelParseDTO<UserExcelRowDTO> excelParseDTO = this.getUserExcelParseDTO(excelFile);
            if (CollectionUtils.isNotEmpty(excelParseDTO.getDataList())) {
                this.saveUserByExcelData(excelParseDTO.getDataList(), source, sessionId);
            }
            importResponse.generateResponse(excelParseDTO);
        } catch (Exception e) {
            LogUtils.info("import user  error", e);
        }
        return importResponse;
    }

    public ExcelParseDTO<UserExcelRowDTO> getUserExcelParseDTO(MultipartFile excelFile) throws Exception {
        UserImportEventListener userImportEventListener = new UserImportEventListener();
        EasyExcelFactory.read(excelFile.getInputStream(), UserExcel.class, userImportEventListener).sheet().doRead();
        return this.validateExcelUserInfo(userImportEventListener.getExcelParseDTO());
    }

    /**
     * 校验excel导入的数据是否与数据库中的数据冲突
     *
     */
    private ExcelParseDTO<UserExcelRowDTO> validateExcelUserInfo(@Valid @NotNull ExcelParseDTO<UserExcelRowDTO> excelParseDTO) {
        List<UserExcelRowDTO> prepareSaveList = excelParseDTO.getDataList();
        if (CollectionUtils.isNotEmpty(prepareSaveList)) {
            var userInDbMap = baseUserMapper.selectUserIdByEmailList(
                            prepareSaveList.stream().map(UserExcelRowDTO::getEmail).collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(User::getEmail, User::getId));
            for (UserExcelRowDTO userExcelRow : prepareSaveList) {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(userExcelRow.getEmail())) {
                    userExcelRow.setErrorMessage(Translator.get("user.email.repeat") + ": " + userExcelRow.getEmail());
                    excelParseDTO.addErrorRowData(userExcelRow.getDataIndex(), userExcelRow);
                }
            }
            excelParseDTO.getDataList().removeAll(excelParseDTO.getErrRowData().values());
        }
        return excelParseDTO;
    }

    private void saveUserByExcelData(@Valid @NotEmpty List<UserExcelRowDTO> dataList, @Valid @NotEmpty String source, @Valid @NotBlank String sessionId) {
        UserBatchCreateRequest userBatchCreateDTO = new UserBatchCreateRequest();
        userBatchCreateDTO.setUserRoleIdList(new ArrayList<>() {{
            add("member");
        }});
        List<UserCreateInfo> userCreateInfoList = new ArrayList<>();
        dataList.forEach(userExcelRowDTO -> {
            UserCreateInfo userCreateInfo = new UserCreateInfo();
            BeanUtils.copyBean(userCreateInfo, userExcelRowDTO);
            userCreateInfoList.add(userCreateInfo);
        });
        userBatchCreateDTO.setUserInfoList(userCreateInfoList);
        this.saveUserAndRole(userBatchCreateDTO, source, sessionId, "/system/user/import");
    }


    public TableBatchProcessResponse deleteUser(@Valid TableBatchProcessDTO request, String operatorId, String operatorName) {
        List<String> userIdList = userToolService.getBatchUserIds(request);
        this.checkUserInDb(userIdList);
        //检查是否含有Admin
        this.checkProcessUserAndThrowException(userIdList, operatorId, operatorName, Translator.get("user.not.delete"));
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIdList);
        //更新删除标志位
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(userIdList.size());
        response.setSuccessCount(this.deleteUserByList(userIdList, operatorId));
        //删除用户角色关系
        userRoleRelationService.deleteByUserIdList(userIdList);
        //批量踢出用户
        userIdList.forEach(SessionUtils::kickOutUser);
        return response;
    }

    /**
     * 检查要处理的用户并抛出异常
     *
     * @param userIdList
     * @param operatorId
     * @param operatorName
     */
    private void checkProcessUserAndThrowException(List<String> userIdList, String operatorId, String operatorName, String exceptionMessage) {
        for (String userId : userIdList) {
            if (StringUtils.equalsAny(userId, "admin", operatorId)) {
                throw new MSException(exceptionMessage + ":" + operatorName);
            }
        }
    }

    private int deleteUserByList(List<String> updateUserList, String operator) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        BaseUserMapper batchDeleteMapper = sqlSession.getMapper(BaseUserMapper.class);
        int insertIndex = 0;
        long deleteTime = System.currentTimeMillis();
        for (String userId : updateUserList) {
            batchDeleteMapper.deleteUser(userId, operator, deleteTime);
            insertIndex++;
            if (insertIndex % 50 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        return insertIndex;
    }

    public List<User> getUserList(String keyword) {
        return extUserMapper.selectUserList(keyword);
    }

    /**
     * 获取组织, 项目系统成员选项
     *
     * @param sourceId 组织ID, 项目ID
     * @return 系统用户选项
     */
    public List<UserExtendDTO> getMemberOption(String sourceId, String keyword) {
        return extUserMapper.getMemberOption(sourceId, keyword);
    }

    public TableBatchProcessResponse resetPassword(TableBatchProcessDTO request, String operator) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        this.checkUserInDb(request.getSelectIds());

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserMapper batchUpdateMapper = sqlSession.getMapper(UserMapper.class);
        int insertIndex = 0;
        long updateTime = System.currentTimeMillis();
        List<User> userList = userToolService.selectByIdList(request.getSelectIds());
        for (User user : userList) {
            User updateModel = new User();
            updateModel.setId(user.getId());
            if (StringUtils.equalsIgnoreCase("admin", user.getId())) {
                updateModel.setPassword(CodingUtils.md5("metersphere"));
            } else {
                updateModel.setPassword(CodingUtils.md5(user.getEmail()));
            }
            updateModel.setUpdateTime(updateTime);
            updateModel.setUpdateUser(operator);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModel);
            insertIndex++;
            if (insertIndex % 50 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        response.setSuccessCount(request.getSelectIds().size());
        return response;
    }

    public void checkUserLegality(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIds);
        if (userMapper.countByExample(example) != userIds.size()) {
            throw new MSException(Translator.get("user.id.not.exist"));
        }
    }


    public List<User> getUserListByOrgId(String organizationId, String keyword) {
        return extUserMapper.getUserListByOrgId(organizationId, keyword);
    }

    /**
     * 临时发送Email的方法。
     *
     * @param hashMap
     */
    @Resource
    MailNoticeSender mailNoticeSender;
    @Resource
    private SystemParameterMapper systemParameterMapper;

    public UserInviteResponse saveInviteRecord(UserInviteRequest request, SessionUser inviteUser) {
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(request.getUserRoleIds(), true);
        //校验邮箱和角色的合法性
        Map<String, String> errorMap = this.validateUserInfo(request.getInviteEmails());
        if (MapUtils.isNotEmpty(errorMap)) {
            throw new MSException(Translator.get("user.email.repeat"));
        }
        List<UserInvite> inviteList = userInviteService.batchInsert(request.getInviteEmails(), inviteUser.getId(), request.getUserRoleIds());
        //记录日志
        userLogService.addEmailInviteLog(inviteList, inviteUser.getId());
        this.sendInviteEmail(inviteList, inviteUser.getName());
        return new UserInviteResponse(inviteList);
    }

    private void sendInviteEmail(List<UserInvite> inviteList, String inviteUser) {
        HashMap<String, String> emailMap = new HashMap<>();
        List<SystemParameter> systemParameters = systemParameterMapper.selectByExample(new SystemParameterExample());
        systemParameters.forEach(systemParameter -> {
            if (systemParameter.getParamKey().equals(ParamConstants.MAIL.PASSWORD.getValue())) {
                if (!StringUtils.isBlank(systemParameter.getParamValue())) {
                    String string = EncryptUtils.aesDecrypt(systemParameter.getParamValue()).toString();
                    emailMap.put(systemParameter.getParamKey(), string);
                }
            } else {
                emailMap.put(systemParameter.getParamKey(), systemParameter.getParamValue());
            }
        });

        inviteList.forEach(userInvite -> {
            String emailContent = userInviteService.genInviteMessage(inviteUser, userInvite.getId(), emailMap.get("base.url"));
            emailMap.put("emailContent", emailContent);
            emailMap.put("smtp.recipient", userInvite.getEmail());
            try {
                this.sendInviteEmailTemporary(emailMap);
            } catch (Exception e) {
                throw new MSException("邮箱邀请失败!", e);
            }
        });
    }

    public void sendInviteEmailTemporary(HashMap<String, String> hashMap) throws Exception {
        JavaMailSenderImpl javaMailSender = null;
        try {
            javaMailSender = mailNoticeSender.getMailSender(hashMap);
            javaMailSender.testConnection();
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(Translator.get("connection_failed"));
        }

        String recipients = hashMap.get(ParamConstants.MAIL.RECIPIENTS.getValue());
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        String username = javaMailSender.getUsername();
        String email = username;
        InternetAddress from = new InternetAddress();
        from.setAddress(email);
        from.setPersonal(username);
        helper.setFrom(from);
        helper.setSubject("MeterSphere邀请注册");
        helper.setText(hashMap.get("emailContent"), true);
        helper.setTo(recipients);
        javaMailSender.send(mimeMessage);
    }

    public String registerByInvite(UserRegisterRequest request) throws Exception {
        UserInvite userInvite = userInviteService.selectEfficientInviteById(request.getInviteId());
        if (userInvite == null) {
            throw new MSException(Translator.get("user.not.invite.or.expired"));
        }
        //检查邮箱是否已经注册
        this.validateUserInfo(new ArrayList<>() {{
            this.add(userInvite.getEmail());
        }});

        //创建用户
        long createTime = System.currentTimeMillis();
        User user = new User();
        user.setId(IDGenerator.nextStr());
        user.setEmail(userInvite.getEmail());
        user.setPassword(CodingUtils.md5(RsaUtils.privateDecrypt(request.getPassword(), RsaUtils.getRsaKey().getPrivateKey())));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setCreateUser(userInvite.getInviteUser());
        user.setUpdateUser(userInvite.getInviteUser());
        user.setCreateTime(createTime);
        user.setUpdateTime(createTime);
        user.setSource(UserSource.LOCAL.name());
        user.setDeleted(false);
        userMapper.insertSelective(user);

        userRoleRelationService.batchSave(JSON.parseArray(userInvite.getRoles(), String.class), user);
        //删除本次邀请记录
        userInviteService.deleteInviteById(userInvite.getId());
        //写入操作日志
        userLogService.addRegisterLog(user, userInvite);
        return user.getId();
    }

    public boolean updateAccount(PersonalUpdateRequest request, String operator) {
        this.checkUserEmail(request.getId(), request.getEmail());
        User editUser = new User();
        editUser.setId(request.getId());
        editUser.setName(request.getUsername());
        editUser.setPhone(request.getPhone());
        editUser.setEmail(request.getEmail());
        editUser.setUpdateUser(operator);
        editUser.setUpdateTime(System.currentTimeMillis());

        if (StringUtils.isNotEmpty(request.getAvatar())) {
            UserExtend userExtend = userExtendMapper.selectByPrimaryKey(request.getId());
            if (userExtend == null) {
                userExtend = new UserExtend();
                userExtend.setId(request.getId());
                userExtend.setAvatar(request.getAvatar());
                userExtendMapper.insert(userExtend);
            } else {
                userExtend.setAvatar(request.getAvatar());
                userExtendMapper.updateByPrimaryKey(userExtend);
            }
        }

        return userMapper.updateByPrimaryKeySelective(editUser) > 0;
    }

    public boolean updatePassword(PersonalUpdatePasswordRequest request) {
        this.checkOldPassword(request.getId(), request.getOldPassword());
        return extUserMapper.updatePasswordByUserId(request.getId(), request.getNewPassword()) > 0;
    }
}
