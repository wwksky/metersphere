<template>
  <div class="page">
    <a-tabs v-model:active-key="activeKey" class="no-content">
      <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="item.label" />
    </a-tabs>
    <a-divider :margin="0" class="!mb-[16px]" />
    <RequestHeader v-if="activeKey === 'requestHeader'" v-model:params="headerParams" @change="canSave = true" />
    <AllPrams
      v-else-if="activeKey === 'globalVariable'"
      v-model:params="GlobalVariable"
      :table-key="TableKeyEnum.PROJECT_MANAGEMENT_ENV_ALL_PARAM_VARIABLE"
      @change="canSave = true"
    />
    <div class="footer" :style="{ width: '100%' }">
      <a-button :disabled="!canSave" type="primary" @click="handleSave">{{ t('common.save') }}</a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { Message } from '@arco-design/web-vue';

  import AllPrams from './allParams/index.vue';
  import RequestHeader from './requestHeader/index.vue';

  import { updateOrAddGlobalParam } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { EnvConfigItem } from '@/models/projectManagement/environmental';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const projectEnvStore = useProjectEnvStore();
  const appStore = useAppStore();

  const activeKey = ref('globalVariable');
  const headerParams = ref<EnvConfigItem[]>([]);
  const GlobalVariable = ref<EnvConfigItem[]>([]);
  const { t } = useI18n();

  const canSave = ref(false);

  const loading = ref(false);

  const contentTabList = [
    {
      value: 'requestHeader',
      label: t('project.environmental.requestHeader'),
    },
    {
      value: 'globalVariable',
      label: t('project.environmental.globalVariable'),
    },
  ];

  const handleSave = async () => {
    try {
      loading.value = true;
      const params = {
        id: projectEnvStore.allParamDetailInfo?.id,
        projectId: appStore.currentProjectId,
        globalParams: {
          headers: headerParams.value,
          commonVariables: GlobalVariable.value.map((item) => {
            return {
              key: item.key,
              value: item.value,
              description: item.description,
              tags: item.tags || [],
            };
          }),
        },
      };
      await updateOrAddGlobalParam(params);
      Message.success(t('common.saveSuccess'));
      canSave.value = false;
    } catch (error) {
      Message.error(t('common.saveFailed'));
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };
  onBeforeMount(() => {
    projectEnvStore.initEnvDetail().then(() => {
      headerParams.value = projectEnvStore.allParamDetailInfo?.globalParams.headers || [];
      GlobalVariable.value = projectEnvStore.allParamDetailInfo?.globalParams.commonVariables || [];
    });
  });
</script>

<style lang="less" scoped>
  .page {
    transform: scale3d(1, 1, 1);
    padding: 24px;
    .no-content {
      :deep(.arco-tabs-content) {
        padding-top: 0;
      }
    }
    .footer {
      gap: 16px;
      position: fixed;
      right: 0;
      bottom: 0;
      z-index: 999;
      display: flex;
      justify-content: flex-end;
      padding: 24px;
      box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
    }
  }
</style>
