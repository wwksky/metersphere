<template>
  <div>
    <div class="flex flex-row items-center gap-[8px]">
      <a-switch type="line" size="small" />
      <div class="text-[var(--color-text-1)]">{{ t('project.environmental.host.config') }}</div>
    </div>
    <div class="mt-[8px]">
      <MsBatchForm
        ref="batchFormRef"
        :models="batchFormModels"
        :form-mode="ruleFormMode"
        add-text="project.menu.rule.addRule"
        :default-vals="currentList"
        show-enable
        :is-show-drag="false"
      ></MsBatchForm>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { onClickOutside } from '@vueuse/core';

  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import { FormItemModel } from '@/components/business/ms-batch-form/types';

  import { useI18n } from '@/hooks/useI18n';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { EnvConfigItem } from '@/models/projectManagement/environmental';
  import { FakeTableListItem } from '@/models/projectManagement/menuManagement';

  const { t } = useI18n();

  const store = useProjectEnvStore();

  const currentList = computed({
    get: () => (store.currentEnvDetailInfo.config.hostConfig || []) as FakeTableListItem[],
    set: (value: EnvConfigItem[] | undefined) => {
      store.currentEnvDetailInfo.config.hostConfig = value;
    },
  });
  const batchFormRef = ref();
  onClickOutside(batchFormRef, () => {
    currentList.value = batchFormRef.value?.getFormResult();
  });
  type UserModalMode = 'create' | 'edit';
  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      filed: 'ip',
      type: 'input',
      label: 'project.environmental.host.ip',
      placeholder: 'project.environmental.host.ipPlaceholder',
      rules: [
        { required: true, message: t('project.environmental.host.ipIsRequire') },
        { notRepeat: true, message: 'project.environmental.host.ipNotRepeat' },
      ],
    },
    {
      filed: 'hostName',
      type: 'input',
      label: 'project.environmental.host.hostName',
      placeholder: 'project.environmental.host.hostNamePlaceholder',
      rules: [{ required: true, message: t('project.environmental.host.hostNameIsRequire') }],
    },
    {
      filed: 'desc',
      type: 'input',
      label: 'project.environmental.host.desc',
      placeholder: 'project.environmental.host.descPlaceholder',
    },
  ]);
  const ruleFormMode = ref<UserModalMode>('create');
</script>

<style lang="less" scoped></style>
