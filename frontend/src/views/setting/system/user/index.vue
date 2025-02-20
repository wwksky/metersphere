<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button v-permission="['SYSTEM_USER:READ+ADD']" class="mr-3" type="primary" @click="showUserModal('create')">
          {{ t('system.user.createUser') }}
        </a-button>
        <a-button v-permission="['SYSTEM_USER_INVITE']" class="mr-3" type="outline" @click="showEmailInviteModal">
          {{ t('system.user.emailInvite') }}
        </a-button>
        <a-button v-permission="['SYSTEM_USER:READ+IMPORT']" class="mr-3" type="outline" @click="showImportModal">{{
          t('system.user.importUser')
        }}</a-button>
      </div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('system.user.searchUser')"
        class="w-[230px]"
        allow-clear
        @search="searchUser"
        @press-enter="searchUser"
      ></a-input-search>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #action="{ record }">
        <template v-if="!record.enable">
          <MsButton @click="enableUser(record)">{{ t('system.user.enable') }}</MsButton>
          <MsButton v-permission="['SYSTEM_USER:READ+DELETE']" @click="deleteUser(record)">{{
            t('system.user.delete')
          }}</MsButton>
        </template>
        <template v-else>
          <MsButton v-permission="['SYSTEM_USER:READ+UPDATE']" @click="showUserModal('edit', record)">{{
            t('system.user.editUser')
          }}</MsButton>
          <MsTableMoreAction
            v-if="hasAnyPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE'])"
            :list="tableActions"
            @select="handleSelect($event, record)"
          ></MsTableMoreAction>
        </template>
      </template>
    </ms-base-table>
  </MsCard>
  <a-modal
    v-model:visible="visible"
    :title="userFormMode === 'create' ? t('system.user.createUserModalTitle') : t('system.user.editUserModalTitle')"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :mask-closable="false"
    @close="handleUserModalClose"
  >
    <a-alert class="mb-[16px]">{{ t('system.user.createUserTip') }}</a-alert>
    <a-form ref="userFormRef" class="rounded-[4px]" :model="userForm" layout="vertical">
      <MsBatchForm
        ref="batchFormRef"
        :models="batchFormModels"
        :form-mode="userFormMode"
        add-text="system.user.addUser"
        :default-vals="userForm.list"
        max-height="250px"
      ></MsBatchForm>
      <a-form-item
        class="mb-0"
        field="userGroup"
        :label="t('system.user.createUserUserGroup')"
        required
        asterisk-position="end"
      >
        <a-select
          v-model="userForm.userGroup"
          multiple
          :placeholder="t('system.user.createUserUserGroupPlaceholder')"
          allow-clear
        >
          <a-option
            v-for="item of userGroupOptions"
            :key="item.id"
            :tag-props="{ closable: userForm.userGroup.length > 1 }"
            :value="item.id"
            :disabled="userForm.userGroup.includes(item.id) && userForm.userGroup.length === 1"
          >
            {{ item.name }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" :disabled="loading" @click="cancelCreate">
        {{ t('system.user.editUserModalCancelCreate') }}
      </a-button>
      <a-button v-if="userFormMode === 'create'" type="secondary" :loading="loading" @click="saveAndContinue">
        {{ t('system.user.editUserModalSaveAndContinue') }}
      </a-button>
      <a-button type="primary" :loading="loading" @click="beforeCreateUser">
        {{ t(userFormMode === 'create' ? 'system.user.editUserModalCreateUser' : 'system.user.editUserModalEditUser') }}
      </a-button>
    </template>
  </a-modal>
  <a-modal
    v-model:visible="importVisible"
    :title="t('system.user.importModalTitle')"
    title-align="start"
    class="ms-modal-upload"
  >
    <a-alert class="mb-[16px]" closable>
      {{ t('system.user.importModalTip') }}
      <a-button type="text" size="small" @click="downLoadUserTemplate">
        {{ t('system.user.importDownload') }}
      </a-button>
    </a-alert>
    <MsUpload
      v-model:file-list="userImportFile"
      accept="excel"
      :show-file-list="false"
      :auto-upload="false"
      :disabled="importLoading"
    ></MsUpload>
    <template #footer>
      <a-button type="secondary" :disabled="importLoading" @click="cancelImport">
        {{ t('system.user.importModalCancel') }}
      </a-button>
      <a-button type="primary" :loading="importLoading" :disabled="userImportFile.length === 0" @click="importUser">
        {{ t('system.user.importModalConfirm') }}
      </a-button>
    </template>
  </a-modal>
  <a-modal v-model:visible="importResultVisible" title-align="start" class="ms-modal-upload">
    <template #title>
      <icon-exclamation-circle-fill
        v-if="importResult === 'fail'"
        class="mr-[8px] text-[20px] text-[rgb(var(--warning-6))]"
      />
      <icon-close-circle-fill
        v-if="importResult === 'allFail'"
        class="mr-[8px] text-[20px] text-[rgb(var(--danger-6))]"
      />
      {{ importResultTitle }}
    </template>
    <div v-if="importResult === 'success'" class="flex flex-col items-center justify-center">
      <icon-check-circle-fill class="text-[32px] text-[rgb(var(--success-6))]" />
      <div class="mb-[8px] mt-[16px] text-[16px] font-medium leading-[24px] text-[var(--color-text-000)]">
        {{ t('system.user.importSuccess') }}
      </div>
      <div class="sub-text">
        {{ t('system.user.importResultSuccessContent', { successNum: importSuccessCount }) }}
      </div>
    </div>
    <template v-else>
      <div>
        {{ t('system.user.importResultContent', { successNum: importSuccessCount, failNum: importFailCount }) }}
      </div>
      <div>
        {{ t('system.user.importResultContentSubStart') }}
        <a-link
          class="text-[rgb(var(--primary-5))]"
          :href="importErrorFileUrl"
          :download="`${t('system.user.importErrorFile')}.pdf`"
        >
          {{ t('system.user.importResultContentDownload') }}
        </a-link>
        {{ t('system.user.importResultContentSubEnd') }}
      </div>
    </template>
    <template #footer>
      <a-button type="text" class="!text-[var(--color-text-1)]" @click="cancelImport">
        {{ t('system.user.importResultReturn') }}
      </a-button>
      <a-button type="text" @click="continueImport">
        {{ t('system.user.importResultContinue') }}
      </a-button>
    </template>
  </a-modal>
  <inviteModal v-model:visible="inviteVisible" :user-group-options="userGroupOptions"></inviteModal>
  <batchModal
    v-model:visible="showBatchModal"
    :table-selected="tableSelected"
    :action="batchAction"
    :batch-params="batchModalParams"
    :keyword="keyword"
    @finished="resetSelector"
  ></batchModal>
</template>

<script setup lang="ts">
  /**
   * @description 系统设置-用户
   */
  import { onBeforeMount, Ref, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import type { FormItemModel, MsBatchFormInstance } from '@/components/business/ms-batch-form/types';
  import batchModal from './components/batchModal.vue';
  import inviteModal from './components/inviteModal.vue';

  import {
    batchCreateUser,
    deleteUserInfo,
    getSystemRoles,
    getUserList,
    importUserInfo,
    resetUserPassword,
    toggleUserStatus,
    updateUserInfo,
  } from '@/api/modules/setting/user';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';
  import { validateEmail, validatePhone } from '@/utils/validate';

  import type { SimpleUserInfo, SystemRole, UserListItem } from '@/models/setting/user';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import type { FileItem, FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const route = useRoute();

  const columns: MsTableColumn = [
    {
      title: 'system.user.tableColumnEmail',
      dataIndex: 'email',
      showTooltip: true,
      sortIndex: 0,
    },
    {
      title: 'system.user.tableColumnName',
      dataIndex: 'name',
      showTooltip: true,
      sortIndex: 1,
    },
    {
      title: 'system.user.tableColumnPhone',
      dataIndex: 'phone',
    },
    {
      title: 'system.user.tableColumnOrg',
      dataIndex: 'organizationList',
      isTag: true,
      width: 300,
    },
    {
      title: 'system.user.tableColumnUserGroup',
      isTag: true,
      dataIndex: 'userRoleList',
      width: 300,
    },
    {
      title: 'system.user.tableColumnStatus',
      slotName: 'enable',
      dataIndex: 'enable',
    },
    {
      title: 'system.user.tableColumnActions',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 110,
    },
  ];
  const tableStore = useTableStore();
  const { propsRes, propsEvent, loadList, setKeyword, resetSelector } = useTable(
    getUserList,
    {
      tableKey: TableKeyEnum.SYSTEM_USER,
      columns,
      size: 'default',
      selectable: true,
      showSetting: true,
      showJumpMethod: true,
      heightUsed: 288,
    },
    (record) => ({
      ...record,
      organizationList: record.organizationList.filter((e: any) => e),
      userRoleList: record.userRoleList.filter((e: any) => e),
    })
  );

  const keyword = ref('');

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

  const tableSelected = ref<(string | number)[]>([]);

  const { openModal } = useModal();

  /**
   * 重置密码
   */
  function resetPassword(record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('system.user.resetPswTip', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('system.user.batchResetPswTip', { count: params?.currentSelectCount || tableSelected.value.length });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'warning',
      title,
      content: t('system.user.resetPswContent'),
      okText: t('system.user.resetPswConfirm'),
      cancelText: t('system.user.resetPswCancel'),
      onBeforeOk: async () => {
        try {
          await resetUserPassword({
            selectIds,
            selectAll: !!params?.selectAll,
            excludeIds: params?.excludeIds || [],
            condition: { keyword: keyword.value },
          });
          Message.success(t('system.user.resetPswSuccess'));
          resetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 禁用用户
   */
  function disabledUser(record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('system.user.disableUserTip', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('system.user.batchDisableUserTip', { count: params?.currentSelectCount || tableSelected.value.length });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'warning',
      title,
      content: t('system.user.disableUserContent'),
      okText: t('system.user.disableUserConfirm'),
      cancelText: t('system.user.disableUserCancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await toggleUserStatus({
            selectIds,
            selectAll: !!params?.selectAll,
            excludeIds: params?.excludeIds || [],
            condition: { keyword: keyword.value },
            enable: false,
          });
          Message.success(t('system.user.disableUserSuccess'));
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 启用用户
   */
  function enableUser(record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('system.user.enableUserTip', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('system.user.batchEnableUserTip', { count: params?.currentSelectCount || tableSelected.value.length });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'info',
      title,
      content: t('system.user.enableUserContent'),
      okText: t('system.user.enableUserConfirm'),
      cancelText: t('system.user.enableUserCancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await toggleUserStatus({
            selectIds,
            selectAll: !!params?.selectAll,
            excludeIds: params?.excludeIds || [],
            condition: { keyword: keyword.value },
            enable: true,
          });
          Message.success(t('system.user.enableUserSuccess'));
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 删除用户
   */
  function deleteUser(record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('system.user.deleteUserTip', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('system.user.batchDeleteUserTip', { count: params?.currentSelectCount || tableSelected.value.length });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('system.user.deleteUserContent'),
      okText: t('system.user.deleteUserConfirm'),
      cancelText: t('system.user.deleteUserCancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteUserInfo({
            selectIds,
            selectAll: !!params?.selectAll,
            excludeIds: params?.excludeIds || [],
            condition: { keyword: keyword.value },
          });
          Message.success(t('system.user.deleteUserSuccess'));
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.resetPassword',
      eventTag: 'resetPassword',
      permission: ['SYSTEM_USER:READ+UPDATE'],
    },
    {
      label: 'system.user.disable',
      eventTag: 'disabled',
      permission: ['SYSTEM_USER:READ+UPDATE'],
    },
    {
      isDivider: true,
    },
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
      permission: ['SYSTEM_USER:READ+DELETE'],
    },
  ];

  const tableBatchActions = {
    baseAction: [
      {
        label: 'system.user.batchActionAddProject',
        eventTag: 'batchAddProject',
        permission: ['SYSTEM_USER:READ+ADD'],
      },
      {
        label: 'system.user.batchActionAddUserGroup',
        eventTag: 'batchAddUserGroup',
        permission: ['SYSTEM_USER:READ+ADD'],
      },
      {
        label: 'system.user.batchActionAddOrganization',
        eventTag: 'batchAddOrganization',
        permission: ['SYSTEM_USER:READ+ADD'],
      },
    ],
    moreAction: [
      {
        label: 'system.user.resetPassword',
        eventTag: 'resetPassword',
        permission: ['SYSTEM_USER:READ+UPDATE'],
      },
      {
        label: 'system.user.disable',
        eventTag: 'disabled',
        permission: ['SYSTEM_USER:READ+UPDATE'],
      },
      {
        label: 'system.user.enable',
        eventTag: 'enable',
        permission: ['SYSTEM_USER:READ+UPDATE'],
      },
      {
        isDivider: true,
      },
      {
        label: 'system.user.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['SYSTEM_USER:READ+DELETE'],
      },
    ],
  };

  const showBatchModal = ref(false);
  const batchAction = ref(''); // 表格选中批量操作动作
  const batchModalParams = ref();

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    switch (event.eventTag) {
      case 'batchAddProject':
      case 'batchAddUserGroup':
      case 'batchAddOrganization':
        batchAction.value = event.eventTag;
        batchModalParams.value = params;
        showBatchModal.value = true;
        break;
      case 'resetPassword':
        resetPassword(undefined, true, params);
        break;
      case 'disabled':
        disabledUser(undefined, true, params);
        break;
      case 'enable':
        enableUser(undefined, true, params);
        break;
      case 'delete':
        deleteUser(undefined, true, params);
        break;
      default:
        break;
    }
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleSelect(item: ActionsItem, record: UserListItem) {
    switch (item.eventTag) {
      case 'resetPassword':
        resetPassword(record);
        break;
      case 'disabled':
        disabledUser(record);
        break;
      case 'delete':
        deleteUser(record);
        break;
      default:
        break;
    }
  }

  type UserModalMode = 'create' | 'edit';

  interface UserForm {
    list: SimpleUserInfo[];
    userGroup: string[];
  }

  const visible = ref(false);
  const loading = ref(false);
  const userFormMode = ref<UserModalMode>('create');
  const userFormRef = ref<FormInstance | null>(null);
  const defaultUserForm = {
    list: [
      {
        name: '',
        email: '',
        phone: '',
      },
    ],
    userGroup: [],
  };
  const userForm = ref<UserForm>(cloneDeep(defaultUserForm));
  const userGroupOptions = ref<SystemRole[]>([]);

  async function init() {
    try {
      userGroupOptions.value = await getSystemRoles();
      if (userGroupOptions.value.length) {
        userForm.value.userGroup = userGroupOptions.value
          .filter((e: SystemRole) => e.selected === true)
          .map((e: SystemRole) => e.id);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(async () => {
    if (route.query.id) {
      keyword.value = route.query.id as string;
    }
    setKeyword(keyword.value);
    init();
    loadList();
  });

  /**
   * 重置用户表单
   */
  function resetUserForm() {
    userForm.value.list = [];
    userFormRef.value?.resetFields();
    userForm.value.userGroup = userGroupOptions.value
      .filter((e: SystemRole) => e.selected === true)
      .map((e: SystemRole) => e.id);
  }

  /**
   * 显示用户表单弹窗
   * @param mode 模式，编辑或创建
   * @param record 编辑时传入的用户信息
   */
  function showUserModal(mode: UserModalMode, record?: UserListItem) {
    visible.value = true;
    userFormMode.value = mode;
    if (mode === 'edit' && record) {
      userForm.value.list = [{ id: record.id, name: record.name, email: record.email, phone: record.phone }];
      userForm.value.userGroup = record.userRoleList.map((e) => e.id);
    }
  }

  /**
   * 校验用户姓名
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   */
  function checkUerName(value: string | undefined, callback: (error?: string) => void) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserNameNotNull'));
    } else if (value.length > 50) {
      callback(t('system.user.createUserNameOverLength'));
    }
  }

  /**
   * 校验用户邮箱
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   * @param index 当前输入的表单项对应 list 的下标，用于校验重复输入的时候排除自身
   */
  function checkUerEmail(value: string | undefined, callback: (error?: string) => void) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserEmailNotNull'));
    } else if (!validateEmail(value)) {
      callback(t('system.user.createUserEmailErr'));
    }
  }

  /**
   * 校验用户手机号
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   */
  function checkUerPhone(value: string | undefined, callback: (error?: string) => void) {
    if (value !== '' && value !== undefined && !validatePhone(value)) {
      callback(t('system.user.createUserPhoneErr'));
    }
  }

  const batchFormRef = ref<MsBatchFormInstance | null>(null);
  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      filed: 'name',
      type: 'input',
      label: 'system.user.createUserName',
      rules: [{ required: true, message: t('system.user.createUserNameNotNull') }, { validator: checkUerName }],
      placeholder: 'system.user.createUserNamePlaceholder',
    },
    {
      filed: 'email',
      type: 'input',
      label: 'system.user.createUserEmail',
      rules: [
        { required: true, message: t('system.user.createUserEmailNotNull') },
        { validator: checkUerEmail },
        { notRepeat: true, message: 'system.user.createUserEmailNoRepeat' },
      ],
      placeholder: 'system.user.createUserEmailPlaceholder',
    },
    {
      filed: 'phone',
      type: 'input',
      label: 'system.user.createUserPhone',
      rules: [{ validator: checkUerPhone }],
      placeholder: 'system.user.createUserPhonePlaceholder',
    },
  ]);

  /**
   * 取消创建，重置用户表单
   */
  function cancelCreate() {
    visible.value = false;
    resetUserForm();
  }

  /**
   * 更新用户
   */
  async function updateUser() {
    const activeUser = userForm.value.list[0];
    const params = {
      id: activeUser.id as string,
      name: activeUser.name,
      email: activeUser.email,
      phone: activeUser.phone,
      userRoleIdList: userForm.value.userGroup,
    };
    await updateUserInfo(params);
    Message.success(t('system.user.updateUserSuccess'));
    visible.value = false;
    loadList();
  }

  /**
   * 创建用户
   * @param isContinue 是否继续创建
   */
  async function createUser(isContinue?: boolean) {
    const params = {
      userInfoList: userForm.value.list,
      userRoleIdList: userForm.value.userGroup,
    };
    await batchCreateUser(params);
    Message.success(t('system.user.addUserSuccess'));
    if (!isContinue) {
      visible.value = false;
    }
    loadList();
  }

  /**
   * 触发创建用户表单校验
   * @param cb 校验通过后执行回调
   */
  function userFormValidate(cb: () => Promise<any>) {
    userFormRef.value?.validate((errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      batchFormRef.value?.formValidate(async (list: any) => {
        try {
          loading.value = true;
          userForm.value.list = [...list];
          await cb();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
        }
      });
    });
  }

  /**
   * 创建前的校验
   */
  function beforeCreateUser() {
    if (userFormMode.value === 'create') {
      userFormValidate(createUser);
    } else {
      userFormValidate(updateUser);
    }
  }

  /**
   * 保存并继续创建，重置用户表单
   */
  function saveAndContinue() {
    userFormValidate(async () => {
      await createUser(true);
      resetUserForm();
    });
  }

  /**
   * 处理用户表单弹窗关闭
   */
  function handleUserModalClose() {
    resetUserForm();
    batchFormRef.value?.resetForm();
  }

  const inviteVisible = ref(false);
  function showEmailInviteModal() {
    inviteVisible.value = true;
  }

  const importVisible = ref(false);
  const importLoading = ref(false);
  const userImportFile = ref<FileItem[]>([]);
  const importResultVisible = ref(false);
  const importSuccessCount = ref(0);
  const importFailCount = ref(0);
  const importErrorFileUrl = ref('');
  const importResult = ref<'success' | 'allFail' | 'fail'>('allFail');
  const importResultTitle = ref(t('system.user.importSuccessTitle'));

  function showImportModal() {
    importVisible.value = true;
    importFailCount.value = 0;
    importSuccessCount.value = 0;
    importResult.value = 'allFail';
  }

  function cancelImport() {
    importVisible.value = false;
    importResultVisible.value = false;
    userImportFile.value = [];
  }

  /**
   * 根据导入结果展示结果弹窗
   */
  function showImportResult() {
    importVisible.value = false;
    switch (importResult.value) {
      case 'success':
        importResultTitle.value = t('system.user.importSuccessTitle');
        loadList();
        break;
      case 'allFail':
        importResultTitle.value = t('system.user.importAllFailTitle');
        break;
      case 'fail':
        importResultTitle.value = t('system.user.importFailTitle');
        loadList();
        break;
      default:
        break;
    }
    importResultVisible.value = true;
  }

  function continueImport() {
    importResultVisible.value = false;
    userImportFile.value = [];
    importVisible.value = true;
  }

  async function importUser() {
    try {
      importLoading.value = true;
      const res = await importUserInfo({
        fileList: [userImportFile.value[0].file],
      });
      const failCount = res.importCount - res.successCount;
      if (failCount === res.importCount) {
        importResult.value = 'allFail';
      } else if (failCount > 0) {
        importResult.value = 'fail';
      } else {
        importResult.value = 'success';
      }
      importSuccessCount.value = res.successCount;
      importFailCount.value = failCount;
      showImportResult();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      importLoading.value = false;
    }
  }

  function downLoadUserTemplate() {
    window.open('/templates/user_import.xlsx', '_blank');
  }
  await tableStore.initColumn(TableKeyEnum.SYSTEM_USER, columns, 'drawer');
</script>

<style lang="less" scoped>
  .disableTag {
    border-color: rgb(var(--primary-3));
    color: rgb(var(--primary-3));
  }
  .enableTag {
    border-color: rgb(var(--primary-5));
    color: rgb(var(--primary-5));
  }
</style>
