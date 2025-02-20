<template>
  <div>
    <a-alert v-if="isShowTip" class="mb-6" type="warning">
      <div class="flex items-start justify-between">
        <span class="w-[80%]">{{ t('caseManagement.featureCase.changeHistoryTip') }}</span>
        <span class="cursor-pointer text-[var(--color-text-2)]" @click="noRemindHandler">{{
          t('caseManagement.featureCase.noReminders')
        }}</span>
      </div>
    </a-alert>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #name="{ record }">
        <a-button type="text" class="px-0">{{ record.name }}</a-button>
      </template>
      <template #type> </template>
      <template #operation="{ record }">
        <MsRemoveButton
          position="br"
          :title="
            t('caseManagement.featureCase.confirmRecoverChangeHistoryTitle', { name: characterLimit(record.name) })
          "
          :sub-title-tip="
            t('caseManagement.featureCase.recoverChangeHistoryTip', { name: characterLimit(record.name) })
          "
          :loading="recoverLoading"
          @ok="recoverHandler(record)"
        />
        <MsButton @click="saveAsHandler(record)">{{ t('caseManagement.featureCase.saveAsVersion') }}</MsButton>
      </template>
    </ms-base-table>
    <a-modal
      v-model:visible="showModal"
      title-align="start"
      class="ms-modal-form ms-modal-medium"
      :ok-text="t('organization.member.Confirm')"
      :cancel-text="t('organization.member.Cancel')"
      unmount-on-close
      @close="handleCancel"
    >
      <template #title> {{ t('caseManagement.featureCase.saveAsVersion') }} </template>
      <div class="form">
        <a-form ref="versionFormRef" :model="form" size="large" layout="vertical">
          <a-form-item
            field="versionId"
            :label="t('caseManagement.featureCase.tableColumnVersion')"
            asterisk-position="end"
            :rules="[{ required: true, message: t('caseManagement.featureCase.saveAsVersionPlaceholder') }]"
          >
            <a-select
              v-model="form.versionId"
              multiple
              allow-clear
              :placeholder="t('organization.member.selectUserScope')"
            >
              <a-option v-for="item of versionOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
            </a-select>
            <MsFormItemSub :text="t('caseManagement.featureCase.saveAsVersionTip')" :show-fill-icon="false" />
          </a-form-item>
        </a-form>
      </div>
      <template #footer>
        <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
        <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleOK">
          {{ t('common.save') }}
        </a-button>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';

  import { getChangeHistoryList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import { useAppStore } from '@/store';
  import { characterLimit } from '@/utils';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const appStore = useAppStore();

  const visitedKey = 'notRemindChangeHistoryTip';
  const { addVisited } = useVisit(visitedKey);
  const { getIsVisited } = useVisit(visitedKey);

  const props = defineProps<{
    caseId: string;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.changeNumber',
      dataIndex: 'id',
      showTooltip: true,
      width: 90,
    },
    {
      title: 'caseManagement.featureCase.changeType',
      slotName: 'type',
      dataIndex: 'type',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.operator',
      dataIndex: 'createUser',
      slotName: 'createUser',
      width: 150,
    },
    {
      title: 'caseManagement.featureCase.tableColumnUpdateTime',
      slotName: 'updateTime',
      dataIndex: 'updateTime',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getChangeHistoryList, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_CHANGE_HISTORY,
    scroll: { x: '100%' },
    selectable: true,
    heightUsed: 340,
    enableDrag: true,
  });

  const form = ref({
    versionId: '',
  });

  const versionOptions = ref([
    {
      id: '1001',
      name: 'v1.0',
    },
    {
      id: '1002',
      name: 'v1.1',
    },
  ]);

  const recoverLoading = ref<boolean>(false);
  // 恢复
  async function recoverHandler(record: any) {
    recoverLoading.value = true;
    try {
      Message.success(t('caseManagement.featureCase.recoveredSuccessfully'));
      resetSelector();
    } catch (error) {
      console.log(error);
    } finally {
      recoverLoading.value = false;
    }
  }

  const showModal = ref<boolean>(false);

  // 另存为版本
  function saveAsHandler(record: any) {
    showModal.value = true;
  }

  const handleCancel = () => {
    showModal.value = false;
  };

  const confirmLoading = ref<boolean>(false);
  const versionFormRef = ref<FormInstance | null>(null);

  const handleOK = () => {
    versionFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        try {
          confirmLoading.value = true;
          Message.success(t('common.saveSuccess'));
          handleCancel();
        } catch (error) {
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      } else {
        return false;
      }
    });
  };

  const isShowTip = ref<boolean>(true);

  const noRemindHandler = () => {
    isShowTip.value = false;
    addVisited();
  };

  const doCheckIsTip = () => {
    isShowTip.value = !getIsVisited();
  };

  function initData() {
    setLoadListParams({
      projectId: appStore.currentProjectId,
      sourceId: props.caseId,
      module: 'FUNCTIONAL_CASE',
    });
    loadList();
  }

  onBeforeMount(() => {
    doCheckIsTip();
    initData();
  });
</script>

<style scoped></style>
