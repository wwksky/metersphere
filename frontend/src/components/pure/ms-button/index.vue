<template>
  <div
    :class="`ms-button ms-button-${props.type} ms-button--${props.status} ${
      props.disabled || props.loading ? `ms-button--disabled ms-button--${props.status}--disabled` : ''
    }`"
    :disabled="props.disabled"
    @click.stop="clickHandler"
  >
    <slot></slot>
    <icon-loading v-if="props.loading" />
  </div>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      type?: 'text' | 'icon' | 'button';
      status?: 'primary' | 'danger' | 'secondary' | 'default';
      disabled?: boolean;
      loading?: boolean;
    }>(),
    {
      type: 'text',
      status: 'primary',
    }
  );

  const emit = defineEmits(['click']);

  function clickHandler() {
    if (!props.disabled) {
      emit('click');
    }
  }
</script>

<style lang="less" scoped>
  .ms-button {
    @apply flex cursor-pointer items-center align-middle;
    &:not(:last-child) {
      @apply mr-4;
    }

    padding: 0 4px;
    font-size: 14px;
    border-radius: var(--border-radius-mini);
  }
  .ms-button--primary--disabled {
    color: rgb(var(--primary-3)) !important;
  }
  .ms-button--danger--disabled {
    color: rgb(var(--danger-2)) !important;
    cursor: not-allowed;
  }
  .ms-button-text {
    @apply p-0;

    color: rgb(var(--primary-5));
  }
  .ms-button-icon {
    padding: 4px;
    color: var(--color-text-4);
    &:not(.ms-button--disabled):hover {
      color: rgb(var(--primary-5));
      background-color: rgb(var(--primary-9));
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
  .ms-button--default {
    color: var(--color-text-1);
    &:not(.ms-button-text, .ms-button--disabled):hover {
      background-color: var(--color-text-n8);
    }
  }
  .ms-button--secondary {
    color: var(--color-text-2);
    &:not(.ms-button-text, .ms-button--disabled):hover {
      background-color: var(--color-text-n8);
    }
  }
  .ms-button--primary {
    color: rgb(var(--primary-5));
    &:not(.ms-button-text, .ms-button--disabled):hover {
      background-color: rgb(var(--primary-9));
    }
  }
  .ms-button--danger {
    color: rgb(var(--danger-6));
    &:not(.ms-button-text, .ms-button--disabled):hover {
      color: rgb(var(--danger-6));
      background-color: rgb(var(--danger-1));
    }
  }
  .ms-button--disabled {
    @apply cursor-not-allowed;
  }
  .ms-button--secondary--disabled {
    color: var(--color-text-brand);
  }
</style>
