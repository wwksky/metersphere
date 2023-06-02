-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化工作空间
INSERT INTO workspace (id, name, description, create_user, create_time, update_time) VALUES (uuid(), '默认工作空间', '系统默认创建的工作空间', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
-- 初始化项目
INSERT INTO project (id, workspace_id, name, description, create_user, create_time, update_time) VALUES (uuid(), (SELECT id FROM workspace WHERE name LIKE '默认工作空间'), '默认项目', '系统默认创建的项目', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);

-- 初始化用户
insert into user(id, name, email, password, status, create_time, update_time, language, last_workspace_id, phone, source, last_project_id, create_user)
VALUES ('admin', 'Administrator', 'admin@metersphere.io', MD5('metersphere'), '1', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin');

-- 初始化用户组
INSERT INTO user_role (id, name, description, `system`, type, create_time, update_time, create_user, scope_id) VALUES ('admin', '系统管理员(系统)', '默认用户组', 1, 'SYSTEM', 1621224000000, 1621224000000, 'admin', 'global');
INSERT INTO user_role (id, name, description, `system`, type, create_time, update_time, create_user, scope_id) VALUES ('project_admin', '项目管理员(系统)', '项目管理员', 1, 'PROJECT', 1620674220004, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, `system`, type, create_time, update_time, create_user, scope_id) VALUES ('project_member', '项目成员(系统)', '项目成员', 1, 'PROJECT', 1620674220005, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, `system`, type, create_time, update_time, create_user, scope_id) VALUES ('read_only', '只读用户(系统)', '只读用户', 1, 'PROJECT', 1620674220006, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, `system`, type, create_time, update_time, create_user, scope_id) VALUES ('super', '超级管理员(系统)', '拥有系统全部工作空间以及项目的操作权限', 1, 'SYSTEM', 1671008474000, 1671008474000, 'admin', 'system');
INSERT INTO user_role (id, name, description, `system`, type, create_time, update_time, create_user, scope_id) VALUES ('ws_admin', '工作空间管理员(系统)', '工作空间管理员', 1, 'WORKSPACE', 1620674220007, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, `system`, type, create_time, update_time, create_user, scope_id) VALUES ('ws_member', '工作空间成员(系统)', '工作空间成员', 1, 'WORKSPACE', 1620674220008, 1620674220000, 'admin', 'global');

-- 初始化用户和组的关系
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, update_time) VALUES ('c3bb9b4f-46d8-4952-9681-8889974487d1', 'admin', 'super', 'system', 1684747668375, 1684747668375);


-- 初始化用户组权限
-- 系统管理员权限
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('fff37fb4-f922-4fd3-be47-16333b46bb26', 'admin', 'SYSTEM_QUOTA:READ', 'SYSTEM_QUOTA');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('fdeea7c6-c3ff-4ca9-ac09-3a1532f6a8d5', 'admin', 'SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('fba82fd0-8d4c-467a-a3f9-81adb0f36f57', 'admin', 'SYSTEM_QUOTA:READ+UPDATE', 'SYSTEM_QUOTA');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('fab9f7e1-63f3-4788-bcf7-045b072b80da', 'admin', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_WORKSPACE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('f883333d-51d3-4634-9d7c-e25eb57fdcab', 'admin', 'SYSTEM_WORKSPACE:READ+ADD', 'SYSTEM_WORKSPACE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('e7dffcf6-383a-4284-ba67-5e77813b916e', 'admin', 'SYSTEM_SETTING:READ+UPDATE', 'SYSTEM_SETTING');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('e02ca9bf-7518-4e2a-8d6e-c1086a6ce390', 'admin', 'SYSTEM_ROLE:READ+UPDATE', 'SYSTEM_ROLE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('dadfbec0-984d-4189-b4c3-89561cd04721', 'admin', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_TEST_POOL');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('d27f93b2-296f-4552-91c3-f691f122beb3', 'admin', 'SYSTEM_USER:READ', 'SYSTEM_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('be7a5483-8b60-4518-b443-68281465b9dd', 'admin', 'SYSTEM_USER:READ+UPDATE_PASSWORD', 'SYSTEM_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('b5f06276-15c4-409c-b964-b5d29434b277', 'admin', 'PERSONAL_INFORMATION:READ+THIRD_ACCOUNT', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('b5d7fe45-ab1f-4026-b7dc-c8c8e3f666c4', 'admin', 'SYSTEM_OPERATING_LOG:READ', 'SYSTEM_OPERATING_LOG');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('af6aedae-7ffa-4ff6-978c-3f4bc0b278c2', 'admin', 'SYSTEM_AUTH:READ+UPDATE', 'SYSTEM_AUTH');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('a7775e04-7b9f-4afc-a164-43b63bfe900b', 'admin', 'SYSTEM_SETTING:READ', 'SYSTEM_SETTING');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('a4d438c5-8f08-4708-ab34-3e78e536c3fe', 'admin', 'PERSONAL_INFORMATION:READ+UPDATE_PASSWORD', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('a0f476bd-5b09-4469-8fdb-1009e8111d8a', 'admin', 'SYSTEM_USER:READ+ADD', 'SYSTEM_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('9809c98a-dbf9-4835-9e06-c9ed43bc4809', 'admin', 'SYSTEM_WORKSPACE:READ+UPDATE', 'SYSTEM_WORKSPACE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('9630020e-e366-43d9-a29a-b257dbbcfe1b', 'admin', 'SYSTEM_AUTH:READ', 'SYSTEM_AUTH');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('8b59dede-3c21-4100-84a1-aa3a188408c4', 'admin', 'SYSTEM_USER:READ+DELETE', 'SYSTEM_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('87832d17-1a4e-4cd6-910c-bf04dc0aa8ca', 'admin', 'PERSONAL_INFORMATION:READ+UPDATE', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('77c91998-654b-476f-b5fd-7b6b6a761899', 'admin', 'SYSTEM_PLUGIN:DEL', 'SYSTEM_PLUGIN');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('72c35c0c-6d9d-4d2e-aeb8-cb6dccf5d963', 'admin', 'SYSTEM_TEST_POOL:READ+ADD', 'SYSTEM_TEST_POOL');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('6d30341a-d891-45c8-bd58-99926829314b', 'admin', 'SYSTEM_TEST_POOL:READ+DELETE', 'SYSTEM_TEST_POOL');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('5f3fd8d2-7ebc-427b-8faf-800e77452319', 'admin', 'SYSTEM_TEST_POOL:READ+UPDATE', 'SYSTEM_TEST_POOL');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('58035f15-0a2a-4b75-b7b1-186751309f0b', 'admin', 'SYSTEM_WORKSPACE:READ+DELETE', 'SYSTEM_WORKSPACE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('37b01825-632d-4859-bd74-53edb006c05c', 'admin', 'PERSONAL_INFORMATION:READ+API_KEYS', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('307be610-31b1-420b-b2bd-b236fdfd48a5', 'admin', 'SYSTEM_PLUGIN:UPLOAD', 'SYSTEM_PLUGIN');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b6a6eea-cb3a-4b16-a6a5-29fbb1ef6cf7', 'admin', 'SYSTEM_PLUGIN:READ', 'SYSTEM_PLUGIN');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2a96546f-b9a5-4f9a-bcbb-b8ac507a761b', 'admin', 'SYSTEM_USER:READ+IMPORT', 'SYSTEM_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('1caa1b07-9d55-40c4-9aa8-9ab96bcba3e7', 'admin', 'SYSTEM_ROLE:READ+SETTING_PERMISSION', 'SYSTEM_ROLE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('1c877074-0c51-47b2-be9d-320ee956a658', 'admin', 'SYSTEM_ROLE:READ', 'SYSTEM_ROLE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('15bfd321-d812-46e5-a98f-3220e9515ad6', 'admin', 'PERSONAL_INFORMATION:READ+UI_SETTING', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('152ead91-a672-4753-904c-815a8d4602a3', 'admin', 'SYSTEM_ROLE:READ+DELETE', 'SYSTEM_ROLE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('03a829ec-0306-4d92-9a4f-b451dc01617d', 'admin', 'SYSTEM_ROLE:READ+ADD', 'SYSTEM_ROLE');
-- 工作空间管理员权限
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2ef69c80-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+DELETE_USER', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2ef68cda-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+ADD_USER', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2ef67aba-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+UPDATE_USER', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2eb8cdb1-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_QUOTA:READ+UPDATE', 'WORKSPACE_QUOTA');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2eb8bc41-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_QUOTA:READ', 'WORKSPACE_QUOTA');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2dde1a80-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'PERSONAL_INFORMATION:READ+UPDATE_PASSWORD', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2dde1a2e-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'PERSONAL_INFORMATION:READ+API_KEYS', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2dde19c9-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'PERSONAL_INFORMATION:READ+THIRD_ACCOUNT', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2dde18f5-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'PERSONAL_INFORMATION:READ+UPDATE', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2d140aab-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+DELETE_GROUP', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2d13f889-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+COPY_GROUP', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2d13e8c8-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+UPDATE_GROUP', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2d13d1c8-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+CREATE_GROUP', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2cea1857-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+UPLOAD_JAR', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caf4318-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_MESSAGE:READ+UPDATE', 'WORKSPACE_MESSAGE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caf15cd-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_MESSAGE:READ', 'WORKSPACE_MESSAGE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caf0246-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_SERVICE:READ+UPDATE', 'WORKSPACE_SERVICE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caed957-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_SERVICE:READ', 'WORKSPACE_SERVICE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b817d1d-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_OPERATING_LOG:READ', 'WORKSPACE_OPERATING_LOG');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b816897-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+DELETE', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81585a-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+UPDATE', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b8146ea-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+EXPORT', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b812d70-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+IMPORT', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b811ba3-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+ADD', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b810b30-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+COPY', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80fb44-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80e844-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+UPDATE', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80d69a-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+DELETE', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80c5fe-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+ADD', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80b588-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80a580-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_TEMPLATE:READ+CASE_TEMPLATE', 'WORKSPACE_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b8091ab-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_TEMPLATE:READ+ISSUE_TEMPLATE', 'WORKSPACE_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b8081e3-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_TEMPLATE:READ', 'WORKSPACE_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b806fbf-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_USER:READ+UPDATE', 'WORKSPACE_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80601c-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_USER:READ+DELETE', 'WORKSPACE_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b805052-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_USER:READ', 'WORKSPACE_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b802f2a-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_TEMPLATE:READ+CUSTOM', 'WORKSPACE_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b801d1f-3bc6-11ed-9680-0242ac130008', 'ws_admin', 'WORKSPACE_USER:READ+ADD', 'WORKSPACE_USER');
-- 工作空间成员权限
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2cea2a4c-3bc6-11ed-9680-0242ac130008', 'ws_member', 'WORKSPACE_PROJECT_MANAGER:READ+UPLOAD_JAR', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caf28b0-3bc6-11ed-9680-0242ac130008', 'ws_member', 'WORKSPACE_MESSAGE:READ', 'WORKSPACE_MESSAGE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caeef78-3bc6-11ed-9680-0242ac130008', 'ws_member', 'WORKSPACE_SERVICE:READ', 'WORKSPACE_SERVICE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81d869-3bc6-11ed-9680-0242ac130008', 'ws_member', 'WORKSPACE_OPERATING_LOG:READ', 'WORKSPACE_OPERATING_LOG');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81c48b-3bc6-11ed-9680-0242ac130008', 'ws_member', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81b3ae-3bc6-11ed-9680-0242ac130008', 'ws_member', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81a16c-3bc6-11ed-9680-0242ac130008', 'ws_member', 'WORKSPACE_TEMPLATE:READ', 'WORKSPACE_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b818ee7-3bc6-11ed-9680-0242ac130008', 'ws_member', 'WORKSPACE_USER:READ', 'WORKSPACE_USER');


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;