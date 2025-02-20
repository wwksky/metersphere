<template>
  <MsDrawer
    :visible="props.visible"
    :width="480"
    unmount-on-close
    :footer="false"
    :title="t('msTable.columnSetting.display')"
    @cancel="handleCancel"
  >
    <div class="ms-table-column-seletor">
      <template v-if="showJumpMethod">
        <div class="mb-2 flex items-center">
          <span class="text-[var(--color-text-4)]">{{ t('msTable.columnSetting.mode') }}</span>
          <a-tooltip>
            <template #content>
              <span>{{ t('msTable.columnSetting.tooltipContentDrawer') }}</span
              ><br />
              <span>{{ t('msTable.columnSetting.tooltipContentWindow') }}</span>
            </template>
            <span class="inline-block align-middle"
              ><icon-question-circle
                class="ml-[4px] mt-[3px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            /></span>
          </a-tooltip>
        </div>
        <a-radio-group class="mb-2" :model-value="currentMode" type="button" @change="handleModeChange">
          <a-radio value="drawer">
            <div class="mode-button">
              <MsIcon :class="{ 'active-color': currentMode === 'drawer' }" type="icon-icon_drawer" />
              <span class="mode-button-title">{{ t('msTable.columnSetting.drawer') }}</span>
            </div>
          </a-radio>
          <a-radio value="new_window">
            <div class="mode-button">
              <MsIcon :class="{ 'active-color': currentMode === 'new_window' }" type="icon-icon_into-item_outlined" />
              <span class="mode-button-title">{{ t('msTable.columnSetting.newWindow') }}</span>
            </div>
          </a-radio>
        </a-radio-group>
      </template>
      <template v-if="props.showPagination">
        <div class="text-[var(--color-text-4)]">{{ t('msTable.columnSetting.pageSize') }} </div>
        <PageSizeSelector
          v-model:model-value="pageSize"
          class="mt-2"
          @page-size-change="(v: number) => emit('pageSizeChange',v)"
        />
      </template>
      <a-divider />
      <div class="mb-2 flex items-center justify-between">
        <div class="text-[var(--color-text-4)]">{{ t('msTable.columnSetting.header') }}</div>
        <MsButton :disabled="!hasChange" @click="handleReset">{{ t('msTable.columnSetting.resetDefault') }}</MsButton>
      </div>
      <div class="flex-col">
        <div v-for="item in nonSortColumn" :key="item.dataIndex" class="column-item">
          <div>{{ t((item.title || item.columnTitle) as string) }}</div>
          <a-switch
            v-model="item.showInTable"
            size="small"
            :disabled="item.dataIndex === 'name' || item.dataIndex === 'operation'"
            type="line"
            @change="handleSwitchChange"
          />
        </div>
      </div>
      <a-divider orientation="center" class="non-sort"
        ><span class="one-line-text text-xs text-[var(--color-text-4)]">{{
          t('msTable.columnSetting.nonSort')
        }}</span></a-divider
      >
      <VueDraggable v-model="couldSortColumn" handle=".sort-handle" ghost-class="ghost" @change="handleSwitchChange">
        <div v-for="element in couldSortColumn" :key="element.dataIndex" class="column-drag-item">
          <div class="flex w-[90%] items-center">
            <MsIcon type="icon-icon_drag" class="sort-handle cursor-move text-[16px] text-[var(--color-text-4)]" />
            <span class="ml-[8px]">{{ t((element.title || element.columnTitle) as string) }}</span>
          </div>
          <a-switch v-model="element.showInTable" size="small" type="line" @change="handleSwitchChange" />
        </div>
      </VueDraggable>
    </div>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { onBeforeMount, ref } from 'vue';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import PageSizeSelector from './comp/pageSizeSelector.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';
  import { TableOpenDetailMode } from '@/store/modules/components/ms-table/types';

  import { MsTableColumn } from './type';

  const tableStore = useTableStore();
  const { t } = useI18n();
  const currentMode = ref('');
  const pageSize = ref();
  // 不能拖拽的列
  const nonSortColumn = ref<MsTableColumn>([]);
  // 可以拖拽的列
  const couldSortColumn = ref<MsTableColumn>([]);
  // 是否有改动
  const hasChange = ref(false);

  const emit = defineEmits<{
    (e: 'initData'): void;
    (e: 'pageSizeChange', value: number): void;
    (e: 'update:visible', value: boolean): void;
  }>();

  const props = defineProps<{
    visible: boolean;
    tableKey: string;
    showJumpMethod: boolean;
    showPagination: boolean;
  }>();

  const handleCancel = async () => {
    await tableStore.setColumns(
      props.tableKey,
      [...nonSortColumn.value, ...couldSortColumn.value],
      currentMode.value as TableOpenDetailMode
    );
    emit('update:visible', false);
    emit('initData');
  };

  const loadColumn = (key: string) => {
    tableStore.getColumns(key).then((res) => {
      const { nonSort, couldSort } = res;
      nonSortColumn.value = nonSort;
      couldSortColumn.value = couldSort;
    });
  };

  const handleReset = () => {
    loadColumn(props.tableKey);
  };

  const handleModeChange = (value: string | number | boolean) => {
    currentMode.value = value as string;
    tableStore.setMode(props.tableKey, value as TableOpenDetailMode);
  };

  const handleSwitchChange = () => {
    hasChange.value = true;
  };

  onBeforeMount(() => {
    if (props.tableKey) {
      tableStore.getMode(props.tableKey).then((res) => {
        if (res) {
          currentMode.value = res;
        }
      });
      tableStore.getPageSize(props.tableKey).then((res) => {
        pageSize.value = res;
      });
      loadColumn(props.tableKey);
    }
  });
</script>

<style lang="less" scoped>
  :deep(.arco-divider-horizontal) {
    margin: 16px 0;
    border-bottom-color: var(--color-text-n8);
  }
  :deep(.arco-divider-text) {
    padding: 0 8px;
  }
  .mode-button {
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    .active-color {
      color: rgba(var(--primary-5));
    }
    .mode-button-title {
      margin-left: 4px;
    }
  }
  .column-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px 8px 36px;
    &:hover {
      border-radius: 6px;
      background: var(--color-text-n9);
    }
  }
  .column-drag-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    cursor: move;
    &:hover {
      border-radius: 6px;
      background-color: var(--color-text-n9);
    }
  }
  .ghost {
    border: 1px dashed rgba(var(--primary-5));
    background-color: rgba(var(--primary-1));
  }
  .non-sort {
    font-size: 12px;
    line-height: 16px;
  }
</style>
