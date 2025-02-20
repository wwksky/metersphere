<template>
  <ms-drawer
    :mask="false"
    :width="680"
    :visible="currentVisible"
    unmount-on-close
    :footer="false"
    class="ms-drawer-no-mask"
    :title="t('system.organization.addMemberTitle', { name: props.currentName })"
    @cancel="handleCancel"
  >
    <div>
      <div class="flex flex-row justify-between">
        <a-button type="primary" @click="handleAddMember">
          {{ t('system.organization.addMember') }}
        </a-button>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('system.organization.searchUserPlaceholder')"
          class="w-[230px]"
          allow-clear
          @search="searchUser"
          @press-enter="searchUser"
          @clear="searchUser"
        ></a-input-search>
      </div>
      <ms-base-table class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
        <template #name="{ record }">
          <span>{{ record.name }}</span>
          <span v-if="record.adminFlag" class="ml-[4px] text-[var(--color-text-4)]">{{
            `(${t('common.admin')})`
          }}</span>
        </template>
        <template #operation="{ record }">
          <MsRemoveButton
            :title="t('system.organization.removeName', { name: record.name })"
            :sub-title-tip="props.organizationId ? t('system.organization.removeTip') : t('system.project.removeTip')"
            @ok="handleRemove(record)"
          />
        </template>
      </ms-base-table>
    </div>
  </ms-drawer>
  <AddUserModal
    :project-id="props.projectId"
    :organization-id="props.organizationId"
    :visible="userVisible"
    @cancel="handleHideUserModal"
    @submit="handleAddMembeSubmit"
  />
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { Message, TableData } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import AddUserModal from './addUserModal.vue';

  import {
    deleteUserFromOrgOrProject,
    postUserTableByOrgIdOrProjectId,
  } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';

  export interface projectDrawerProps {
    visible: boolean;
    organizationId?: string;
    projectId?: string;
    currentName: string;
  }
  const { t } = useI18n();
  const props = defineProps<projectDrawerProps>();
  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'requestFetchData'): void;
  }>();

  const currentVisible = ref(props.visible);

  const userVisible = ref(false);

  const keyword = ref('');

  const projectColumn: MsTableColumn = [
    {
      title: 'system.organization.userName',
      slotName: 'name',
      dataIndex: 'nameTooltip',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.organization.email',
      dataIndex: 'email',
      width: 200,
      showTooltip: true,
    },
    {
      title: 'system.organization.phone',
      dataIndex: 'phone',
    },
    { title: 'system.organization.operation', slotName: 'operation' },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(
    postUserTableByOrgIdOrProjectId,
    {
      columns: projectColumn,
      scroll: { x: '100%' },
      heightUsed: 240,
      selectable: false,
      noDisable: false,
      pageSimple: true,
    },
    (record: any) => ({
      ...record,
      nameTooltip: record.name + (record.adminFlag ? `(${t('common.admin')})` : ''),
    })
  );

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

  const handleCancel = () => {
    keyword.value = '';
    emit('cancel');
  };

  const fetchData = async () => {
    if (props.organizationId) {
      setLoadListParams({ organizationId: props.organizationId });
    }
    if (props.projectId) {
      setLoadListParams({ projectId: props.projectId });
    }
    await loadList();
  };

  const handleAddMember = () => {
    userVisible.value = true;
  };
  const handleAddMembeSubmit = () => {
    fetchData();
    emit('requestFetchData');
  };

  const handleHideUserModal = () => {
    userVisible.value = false;
  };

  const handleRemove = async (record: TableData) => {
    try {
      if (props.organizationId) {
        await deleteUserFromOrgOrProject(props.organizationId, record.id);
      }
      if (props.projectId) {
        await deleteUserFromOrgOrProject(props.projectId, record.id, false);
      }
      Message.success(t('common.removeSuccess'));
      fetchData();
      emit('requestFetchData');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  watch(
    () => props.organizationId,
    () => {
      fetchData();
    }
  );
  watch(
    () => props.projectId,
    () => {
      fetchData();
    }
  );
  watch(
    () => props.visible,
    (visible) => {
      currentVisible.value = visible;
      if (visible) {
        fetchData();
      }
    }
  );
</script>
