<template>
  <div class="ms-table-select-all">
    <a-checkbox
      v-model="checked"
      :disabled="props.disabled"
      class="text-base"
      :indeterminate="indeterminate"
      @change="handleCheckChange"
    />
    <a-dropdown v-if="props.showSelectAll" :disable="props.disabled" position="bl" @select="handleSelect">
      <div>
        <MsIcon type="icon-icon_down_outlined" class="dropdown-icon" />
      </div>
      <template #content>
        <a-doption :value="SelectAllEnum.CURRENT">{{ t('msTable.current') }}</a-doption>
        <a-doption :value="SelectAllEnum.ALL">{{ t('msTable.all') }}</a-doption>
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watchEffect } from 'vue';

  import MsIcon from '../ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { SelectAllEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'change', value: SelectAllEnum): void;
  }>();

  const props = withDefaults(
    defineProps<{ current: number; total: number; showSelectAll: boolean; disabled: boolean }>(),
    {
      current: 0,
      total: 0,
      showSelectAll: true,
      disabled: false,
    }
  );

  const checked = ref(false);
  const indeterminate = ref(false);

  watchEffect(() => {
    if (props.current === 0) {
      checked.value = false;
      indeterminate.value = false;
    } else if (props.current < props.total) {
      checked.value = false;
      indeterminate.value = true;
    } else if (props.current === props.total) {
      checked.value = true;
      indeterminate.value = false;
    }
  });

  const handleSelect = (v: string | number | Record<string, any> | undefined) => {
    emit('change', v as SelectAllEnum);
  };

  const handleCheckChange = () => {
    if (checked.value) {
      handleSelect(SelectAllEnum.CURRENT);
    } else {
      handleSelect(SelectAllEnum.NONE);
    }
  };
</script>

<style lang="less" scoped>
  .ms-table-select-all {
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    .dropdown-icon {
      margin-left: 4px;
      font-size: 12px;
      border-radius: 50%;
      color: var(--color-text-4);
      line-height: 16px;
    }
    .dropdown-icon:hover {
      color: rgb(var(--primary-5));
    }
  }
</style>
