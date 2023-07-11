# 组织用户组数据准备
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-org-role-id-1', 'default-org-role-1', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-organization-2');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-org-role-id-2', 'default-org-role-2', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-organization-2');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-org-role-id-3', 'default-org-role-3', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-organization-2');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUE
    (uuid(), 'default-org-role-id-3', 'ORGANIZATION_USER_ROLE:READ');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, create_user) VALUE
    (UUID(), 'default-admin', 'default-org-role-id-3', 'default-organization-2', UNIX_TIMESTAMP() * 1000, 'admin');