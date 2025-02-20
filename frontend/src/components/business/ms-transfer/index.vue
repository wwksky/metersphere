<template>
  <a-transfer
    v-model:model-value="innerTarget"
    :title="props.title"
    :data="transferData"
    :show-search="props.showSearch"
    :source-input-search-props="{
      ...props.sourceInputSearchProps,
      onInput: inputChange,
    }"
    :target-input-search-props="props.targetInputSearchProps"
  >
    <template #source="{ selectedKeys, onSelect }">
      <MsTree
        :checkable="true"
        checked-strategy="child"
        :checked-keys="selectedKeys"
        :data="getTreeData()"
        :keyword="sourceKeyword"
        block-node
        default-expand-all
        @check="onSelect"
      >
        <template #title="nodeData">
          <div class="one-line-text">
            {{ nodeData.title }}
          </div>
        </template>
      </MsTree>
    </template>
  </a-transfer>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeFieldNames, MsTreeNodeData } from '@/components/business/ms-tree/types';

  export interface TransferDataItem {
    value: string;
    label: string;
    disabled: boolean;
    [key: string]: any;
  }

  const props = withDefaults(
    defineProps<{
      modelValue: string[];
      title?: string[]; // [left, right]左右标题
      data: MsTreeNodeData[]; // 树结构数据
      treeFiled?: MsTreeFieldNames; // 自定义树结构字段
      showSearch?: boolean; // 是否显示搜索框
      height?: string; // 穿梭框高度
      sourceInputSearchProps?: Record<string, any>;
      targetInputSearchProps?: Record<string, any>;
    }>(),
    {
      treeFiled: () => ({
        key: 'key',
        title: 'title',
        children: 'children',
        disabled: 'disabled',
      }),
    }
  );
  const emit = defineEmits(['update:modelValue']);

  const innerTarget = ref<string[]>([]);
  const transferData = ref<TransferDataItem[]>([]);

  watch(
    () => props.modelValue,
    (val) => {
      innerTarget.value = val;
    }
  );

  watch(
    () => innerTarget.value,
    (val) => {
      emit('update:modelValue', val);
    }
  );

  const sourceKeyword = ref<string>('');

  /**
   * 获取穿梭框数据，根据树结构获取
   * @param _treeData 树结构
   * @param transferDataSource 穿梭框数组
   */
  const getTransferData = (_treeData: MsTreeNodeData[], transferDataSource: TransferDataItem[]) => {
    _treeData.forEach((item) => {
      const itemChildren = item[props.treeFiled.children];
      if (Array.isArray(itemChildren) && itemChildren.length > 0) getTransferData(itemChildren, transferDataSource);
      else
        transferDataSource.push({
          label: item[props.treeFiled.title],
          value: item[props.treeFiled.key],
          disabled: props.treeFiled.disabled ? item[props.treeFiled.disabled] : false,
        });
    });
    return transferDataSource;
  };
  /**
   * 获取树结构数据，根据穿梭框过滤的数据获取
   */
  const getTreeData = () => {
    const travel = (_treeData: MsTreeNodeData[]) => {
      const treeDataSource: MsTreeNodeData[] = [];
      _treeData.forEach((item) => {
        const itemChildren = item[props.treeFiled.children];
        const itemKey = item[props.treeFiled.key];
        const itemTitle = item[props.treeFiled.title];
        // 需要判断当前父节点下的子节点是否全部选中，若选中则不会 push 进穿梭框数组内，否则会出现空的节点无法选中
        const allSelected =
          innerTarget.value.length > 0 &&
          Array.isArray(itemChildren) &&
          itemChildren.length > 0 &&
          itemChildren?.every((child: MsTreeNodeData) => innerTarget.value.includes(child[props.treeFiled.key]));
        if (!allSelected && !innerTarget.value.includes(itemKey)) {
          // 非选中父节点时，需要判断每个子节点是否已经在右侧的选中的数组内，不在才渲染到左侧
          treeDataSource.push({
            title: itemTitle,
            key: itemKey,
            label: itemTitle,
            children: itemChildren ? travel(itemChildren) : [],
          });
        }
      });
      return treeDataSource;
    };
    return travel(props.data);
  };

  function inputChange(val: string) {
    sourceKeyword.value = val;
  }

  watch(
    () => props.data,
    (arr) => {
      transferData.value = getTransferData(arr, []);
    }
  );
</script>

<style lang="less">
  /** 穿梭框 **/
  .arco-transfer {
    .arco-transfer-view {
      width: calc(50% - 34px);
      height: v-bind(height);
      .arco-transfer-view-header {
        @apply bg-white;
      }
    }
    .arco-transfer-operations {
      .arco-btn-secondary {
        border-color: rgb(var(--primary-5));
        border-radius: var(--border-radius-small);
        background-color: rgb(var(--primary-1)) !important;
        .arco-btn-icon {
          color: rgb(var(--primary-5));
        }
        &:disabled {
          border-color: var(--color-text-input-border) !important;
          background-color: var(--color-text-n8) !important;
          .arco-btn-icon {
            color: var(--color-text-4);
          }
        }
        &:not(:disabled):hover {
          border-color: rgb(var(--primary-4)) !important;
          background-color: rgb(var(--primary-1)) !important;
          .arco-btn-icon {
            color: rgb(var(--primary-7));
          }
        }
        &:not(:disabled):active {
          border-color: rgb(var(--primary-7)) !important;
          background-color: rgb(var(--primary-9)) !important;
          .arco-btn-icon {
            color: rgb(var(--primary-7));
          }
        }
      }
    }
  }
</style>
