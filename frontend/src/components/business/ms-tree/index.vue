<template>
  <div ref="treeContainerRef" :class="['ms-tree-container', containerStatusClass]">
    <a-tree
      v-show="treeData.length > 0"
      v-bind="props"
      ref="treeRef"
      v-model:expanded-keys="expandedKeys"
      v-model:selected-keys="innerSelectedKeys"
      :data="treeData"
      class="ms-tree"
      @drop="onDrop"
      @select="select"
      @check="checked"
    >
      <template v-if="$slots['title']" #title="_props">
        <a-tooltip
          :content="_props[props.fieldNames.title]"
          :mouse-enter-delay="500"
          :position="props.titleTooltipPosition"
        >
          <slot name="title" v-bind="_props"></slot>
        </a-tooltip>
      </template>
      <template v-if="$slots['extra']" #extra="_props">
        <div
          v-if="_props.hideMoreAction !== true"
          :class="[
            'ms-tree-node-extra',
            innerFocusNodeKey === _props[props.fieldNames.key] ? 'ms-tree-node-extra--focus' : '',
          ]"
        >
          <div
            class="ml-[-4px] flex h-[32px] items-center rounded-[var(--border-radius-small)] bg-[rgb(var(--primary-1))]"
          >
            <slot name="extra" v-bind="_props"></slot>
            <MsTableMoreAction
              v-if="props.nodeMoreActions"
              :list="props.nodeMoreActions"
              trigger="click"
              @select="handleNodeMoreSelect($event, _props)"
              @close="moreActionsClose"
            >
              <MsButton
                type="text"
                size="mini"
                class="ms-tree-node-extra__more"
                @click="innerFocusNodeKey = _props[props.fieldNames.key]"
              >
                <MsIcon type="icon-icon_more_outlined" size="14" class="text-[var(--color-text-4)]" />
              </MsButton>
            </MsTableMoreAction>
          </div>
        </div>
      </template>
    </a-tree>
    <slot name="empty">
      <div
        v-show="treeData.length === 0 && props.emptyText"
        class="rounded-[var(--border-radius-small)] bg-[var(--color-fill-1)] p-[8px] text-[12px] leading-[16px] text-[var(--color-text-4)]"
      >
        {{ props.emptyText }}
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
  import { h, nextTick, onBeforeMount, Ref, ref, watch, watchEffect } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import useContainerShadow from '@/hooks/useContainerShadow';
  import { mapTree } from '@/utils/index';

  import type { MsTreeFieldNames, MsTreeNodeData, MsTreeSelectedData } from './types';

  const props = withDefaults(
    defineProps<{
      data: MsTreeNodeData[];
      keyword?: string; // 搜索关键字
      searchDebounce?: number; // 搜索防抖 ms 数
      draggable?: boolean; // 是否可拖拽
      blockNode?: boolean; // 是否块级节点
      showLine?: boolean; // 是否展示连接线
      defaultExpandAll?: boolean; // 是否默认展开所有节点
      selectable?: boolean; // 是否可选中
      fieldNames?: MsTreeFieldNames; // 自定义字段名
      focusNodeKey?: string | number; // 聚焦的节点 key
      selectedKeys?: Array<string | number>; // 选中的节点 key
      nodeMoreActions?: ActionsItem[]; // 节点展示在省略号按钮内的更多操作
      expandAll?: boolean; // 是否展开/折叠所有节点，true 为全部展开，false 为全部折叠
      emptyText?: string; // 空数据时的文案
      checkable?: boolean; // 是否可选中
      checkedStrategy?: 'all' | 'parent' | 'child'; // 选中节点时的策略
      checkedKeys?: Array<string | number>; // 选中的节点 key
      virtualListProps?: Record<string, unknown>; // 虚拟滚动列表的属性
      titleTooltipPosition?:
        | 'top'
        | 'tl'
        | 'tr'
        | 'bottom'
        | 'bl'
        | 'br'
        | 'left'
        | 'lt'
        | 'lb'
        | 'right'
        | 'rt'
        | 'rb'; // 标题 tooltip 的位置
    }>(),
    {
      searchDebounce: 300,
      defaultExpandAll: false,
      selectable: true,
      draggable: false,
      titleTooltipPosition: 'right',
      fieldNames: () => ({
        key: 'key',
        title: 'title',
        children: 'children',
        isLeaf: 'isLeaf',
      }),
    }
  );

  const emit = defineEmits<{
    (e: 'select', selectedKeys: Array<string | number>, node: MsTreeNodeData): void;
    (
      e: 'drop',
      tree: MsTreeNodeData[],
      dragNode: MsTreeNodeData, // 被拖拽的节点
      dropNode: MsTreeNodeData, // 放入的节点
      dropPosition: number // 放入的位置，-1 为放入节点前，1 为放入节点后，0 为放入节点内
    ): void;
    (e: 'moreActionSelect', item: ActionsItem, node: MsTreeNodeData): void;
    (e: 'update:focusNodeKey', val: string | number): void;
    (e: 'update:selectedKeys', val: Array<string | number>): void;
    (e: 'moreActionsClose'): void;
    (e: 'check', val: Array<string | number>): void;
  }>();

  const treeContainerRef: Ref = ref(null);
  const treeRef: Ref = ref(null);
  const { isInitListener, containerStatusClass, setContainer, initScrollListener } = useContainerShadow({
    overHeight: 32,
    containerClassName: 'ms-tree-container',
  });
  const originalTreeData = ref<MsTreeNodeData[]>([]);

  function init() {
    originalTreeData.value = mapTree<MsTreeNodeData>(props.data, (node: MsTreeNodeData) => {
      if (!props.showLine) {
        // 不展示连接线时才设置节点图标，因为展示连接线时非叶子节点会展示默认的折叠图标。它不会覆盖 switcherIcon，但是会被 switcherIcon 覆盖
        node.icon = () => h('span', { class: 'hidden' });
      }
      if (
        node[props.fieldNames.isLeaf || 'isLeaf'] ||
        !node[props.fieldNames.children] ||
        node[props.fieldNames.children]?.length === 0
      ) {
        // 设置子节点图标，会覆盖 icon。当展示连接线时，需要设置 switcherIcon 以覆盖组件的默认图标；不展示连接线则是 icon
        node[props.showLine ? 'switcherIcon' : 'icon'] = () => h('span', { class: 'hidden' });
      }
      return node;
    });
    nextTick(() => {
      if (props.defaultExpandAll) {
        treeRef.value?.expandAll(true);
      }
      if (!isInitListener.value && treeRef.value) {
        setContainer(
          props.virtualListProps?.height
            ? (treeRef.value.$el.querySelector('.arco-virtual-list') as HTMLElement)
            : treeRef.value.$el
        );
        initScrollListener();
      }
    });
  }

  onBeforeMount(() => {
    init();
  });

  watch(
    () => props.data,
    () => {
      init();
    }
  );

  const expandedKeys = ref<(string | number)[]>([]);

  /**
   * 根据关键字过滤树节点
   * @param keyword 搜索关键字
   */
  function searchData(keyword: string) {
    const search = (data: MsTreeNodeData[]) => {
      const result: MsTreeNodeData[] = [];
      data.forEach((item) => {
        if (item[props.fieldNames.title].toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
          result.push({ ...item });
        } else if (item[props.fieldNames.children]) {
          const filterData = search(item[props.fieldNames.children]);
          if (filterData.length) {
            result.push({
              ...item,
              [props.fieldNames.children]: filterData,
            });
          }
        }
      });
      expandedKeys.value = result.map((item) => item[props.fieldNames.key]); // 搜索时，匹配的节点需要自动展开
      return result;
    };

    return search(originalTreeData.value);
  }

  const treeData = ref<MsTreeNodeData[]>([]);

  // 防抖搜索
  const updateDebouncedSearch = debounce(() => {
    if (props.keyword) {
      treeData.value = searchData(props.keyword);
    }
  }, props.searchDebounce);

  watchEffect(() => {
    if (!props.keyword) {
      treeData.value = originalTreeData.value;
    } else {
      updateDebouncedSearch();
    }
  });

  function loop(
    _data: MsTreeNodeData[],
    key: string | number | undefined,
    callback: (item: MsTreeNodeData, index: number, arr: MsTreeNodeData[]) => void
  ) {
    _data.some((item, index, arr) => {
      if (item[props.fieldNames.key] === key) {
        callback(item, index, arr);
        return true;
      }
      if (item[props.fieldNames.children]) {
        return loop(item[props.fieldNames.children], key, callback);
      }
      return false;
    });
  }

  /**
   * 处理拖拽结束
   */
  function onDrop({
    dragNode,
    dropNode,
    dropPosition,
  }: {
    dragNode: MsTreeNodeData; // 被拖拽的节点
    dropNode: MsTreeNodeData; // 放入的节点
    dropPosition: number; // 放入的位置，-1 为放入节点前，1 为放入节点后，0 为放入节点内
  }) {
    loop(originalTreeData.value, dragNode.key, (item, index, arr) => {
      arr.splice(index, 1);
    });

    if (dropPosition === 0) {
      // 放入节点内
      loop(originalTreeData.value, dropNode.key, (item) => {
        item.children = item.children || [];
        item.children.push(dragNode);
      });
      dropNode.isLeaf = false;
      if (props.showLine) {
        delete dropNode.switcherIcon; // 放入的节点的 children 放入了被拖拽的节点，需要删除 switcherIcon 以展示默认的折叠图标
      }
    } else {
      // 放入节点前或后
      loop(originalTreeData.value, dropNode.key, (item, index, arr) => {
        arr.splice(dropPosition < 0 ? index : index + 1, 0, dragNode);
      });
    }
    emit('drop', originalTreeData.value, dragNode, dropNode, dropPosition);
  }

  /**
   * 处理树节点选中（非复选框）
   */
  function select(selectedKeys: Array<string | number>, data: MsTreeSelectedData) {
    emit('select', selectedKeys, data.selectedNodes[0]);
  }

  function checked(checkedKeys: Array<string | number>) {
    emit('check', checkedKeys);
  }

  const innerFocusNodeKey = useVModel(props, 'focusNodeKey', emit); // 聚焦的节点，一般用于在操作扩展按钮时，高亮当前节点，保持扩展按钮持续显示

  const focusEl = ref<HTMLElement | null>(); // 存储聚焦的节点元素

  watch(
    () => innerFocusNodeKey.value,
    (val) => {
      if (val?.toString() !== '') {
        focusEl.value = treeRef.value?.$el.querySelector(`[data-key="${val}"]`);
        if (focusEl.value) {
          focusEl.value.style.backgroundColor = 'rgb(var(--primary-1))';
        }
      } else if (focusEl.value) {
        focusEl.value.style.backgroundColor = '';
      }
    }
  );

  /**
   * 处理树节点更多按钮事件
   * @param item
   */
  function handleNodeMoreSelect(item: ActionsItem, node: MsTreeNodeData) {
    emit('moreActionSelect', item, node);
  }

  function moreActionsClose() {
    emit('moreActionsClose');
  }

  watch(
    () => props.expandAll,
    (val) => {
      if (typeof val === 'boolean') {
        treeRef.value?.expandAll(val);
      }
    }
  );

  const innerSelectedKeys = useVModel(props, 'selectedKeys', emit);
</script>

<style lang="less">
  .ms-tree-container {
    .ms-container--shadow-y();
    .ms-tree {
      .ms-scroll-bar();
      .arco-tree-node {
        border-radius: var(--border-radius-small);
        &:hover {
          background-color: rgb(var(--primary-1));
          .arco-tree-node-title {
            background-color: rgb(var(--primary-1));
            &:not([draggable='false']) {
              .arco-tree-node-title-text {
                width: calc(100% - 22px);
              }
            }
          }
        }
        .arco-tree-node-indent-block {
          width: 1px;
        }
        .arco-tree-node-minus-icon,
        .arco-tree-node-plus-icon {
          border: 1px solid var(--color-text-4);
          border-radius: var(--border-radius-mini);
          background-color: white;
          &::after,
          &::before {
            background-color: var(--color-text-4);
          }
        }
        .arco-tree-node-switcher {
          .arco-tree-node-switcher-icon {
            @apply flex;

            color: var(--color-text-4);
          }
        }
        .arco-tree-node-title-highlight {
          background-color: transparent;
        }
        .arco-tree-node-title {
          &:hover {
            background-color: rgb(var(--primary-1));
            + .ms-tree-node-extra {
              @apply visible w-auto;
            }
          }
          .arco-tree-node-title-text {
            width: calc(100% - 8px);
          }
          .arco-tree-node-drag-icon {
            @apply cursor-move;

            right: 6px;
            .arco-icon {
              font-size: 14px;
            }
          }
        }
        .arco-tree-node-title-block {
          width: 80%;
        }
        .ms-tree-node-extra {
          @apply invisible relative w-0;
          &:hover {
            @apply visible w-auto;
          }
          .ms-tree-node-extra__btn,
          .ms-tree-node-extra__more {
            padding: 4px;
            border-radius: var(--border-radius-mini);
            &:hover {
              background-color: rgb(var(--primary-9));
              .arco-icon {
                color: rgb(var(--primary-5));
              }
            }
          }
          .ms-tree-node-extra__more {
            margin-right: 4px;
          }
        }
        .ms-tree-node-extra--focus {
          @apply visible w-auto;
        }
        .arco-tree-node-custom-icon {
          @apply hidden;
        }
      }
      .arco-tree-node-selected {
        .arco-tree-node-minus-icon,
        .arco-tree-node-plus-icon {
          border: 1px solid rgb(var(--primary-5));
          border-radius: var(--border-radius-mini);
          background-color: white;
          &::after,
          &::before {
            background-color: rgb(var(--primary-5));
          }
        }
        .arco-tree-node-switcher-icon .arco-icon,
        .arco-tree-node-title {
          font-weight: 500 !important;
          color: rgb(var(--primary-5));
          * {
            color: rgb(var(--primary-5));
          }
        }
      }
      .arco-tree-node-disabled {
        &:hover {
          background-color: transparent;
        }
        * {
          color: var(--color-text-4) !important;
        }
        .arco-tree-node-title {
          &:hover {
            background-color: transparent;
          }
        }
      }
    }
    .arco-tree-node-disabled-selectable {
      @apply !cursor-default;
    }
  }
</style>
