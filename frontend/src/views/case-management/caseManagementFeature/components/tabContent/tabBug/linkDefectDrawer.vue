<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="false"
    :title="t('caseManagement.featureCase.linkDefect')"
    :ok-text="t('caseManagement.featureCase.associated')"
    :ok-disabled="propsRes.selectedKeys.size === 0"
    :width="1200"
    :mask-closable="false"
    unmount-on-close
    :show-continue="false"
    :ok-loading="props.drawerLoading"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="flex items-center justify-between">
      <div class="font-medium">{{ t('caseManagement.featureCase.defectList') }}</div>
      <div>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
          allow-clear
          class="mx-[8px] w-[240px]"
        ></a-input-search
      ></div>
    </div>
    <div>
      <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
        <template #name="{ record }">
          <span class="one-line-text max-w-[300px]"> {{ record.name }}</span>
          <a-popover title="" position="right">
            <span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
            <template #content>
              <div class="max-w-[600px] text-[14px] text-[var(--color-text-1)]">
                {{ record.name }}
              </div>
            </template>
          </a-popover>
        </template>
      </ms-base-table>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getDrawerDebugPage } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = defineProps<{
    visible: boolean;
    caseId: string;
    drawerLoading: boolean;
  }>();

  const emit = defineEmits(['update:visible', 'save']);
  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'id',
      width: 200,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUserName',
      dataIndex: 'handleUserName',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'status',
      dataIndex: 'status',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectSource',
      slotName: 'defectSource',
      dataIndex: 'defectSource',
      showInTable: true,
      showTooltip: true,
      width: 200,
      ellipsis: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getDrawerDebugPage, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEFECT,
    selectable: true,
    scroll: { x: 'auto' },
    heightUsed: 340,
    enableDrag: false,
  });

  const keyword = ref<string>('');

  function handleDrawerConfirm() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const params = {
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
      projectId: currentProjectId.value,
      keyword: keyword.value,
      searchMode: 'AND',
      combine: {},
      caseId: props.caseId,
    };
    emit('save', params);
  }
  function handleDrawerCancel() {
    resetSelector();
  }

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });

  function getFetch() {
    setLoadListParams({ keyword: keyword.value, projectId: currentProjectId.value, sourceId: props.caseId });
    loadList();
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        getFetch();
        resetSelector();
      }
    }
  );
</script>

<style scoped></style>
