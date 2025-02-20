<template>
  <span>
    <a-dropdown :trigger="props.trigger || 'hover'" @select="selectHandler" @popup-visible-change="visibleChange">
      <slot>
        <MsButton type="text" size="mini" class="more-icon">
          <MsIcon type="icon-icon_more_outlined" size="16" class="text-[var(--color-text-4)]" />
        </MsButton>
      </slot>
      <template #content>
        <template v-for="item of props.list">
          <a-divider v-if="item.isDivider" :key="`${item.label}-divider`" margin="4px" />
          <a-doption
            v-else
            :key="item.label"
            v-permission="item.permission || []"
            :class="item.danger ? 'error-6' : ''"
            :disabled="item.disabled"
            :value="item.eventTag"
          >
            <MsIcon v-if="item.icon" :type="item.icon" />
            {{ t(item.label || '') }}
          </a-doption>
        </template>
      </template>
    </a-dropdown>
  </span>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { ActionsItem, SelectedValue } from './types';

  const { t } = useI18n();
  const props = defineProps<{
    list: ActionsItem[];
    trigger?: 'click' | 'hover' | 'focus' | 'contextMenu' | ('click' | 'hover' | 'focus' | 'contextMenu')[] | undefined;
  }>();

  const emit = defineEmits(['select', 'close']);

  function selectHandler(value: SelectedValue) {
    const item = props.list.find((e: ActionsItem) => e.eventTag === value);
    emit('select', item);
  }

  function visibleChange(val: boolean) {
    if (!val) {
      emit('close');
    }
  }
</script>

<style lang="less" scoped>
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
  .more-icon {
    padding: 4px;
    border-radius: var(--border-radius-mini);
    &:hover {
      background-color: rgb(var(--primary-9));
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
