<template>
  <div class="mb-[16px] flex items-center justify-between">
    <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.localExecution') }}</div>
  </div>
  <a-spin class="w-full" :loading="loading">
    <div class="grid grid-cols-2 gap-[16px]">
      <div class="config-card">
        <div class="config-card-title">
          <div class="config-card-title-text">{{ t('ms.personal.apiLocalExecution') }}</div>
          <MsTag
            v-if="apiConfig.status !== 'none'"
            theme="outline"
            :type="tagMap[apiConfig.status].type"
            size="small"
            class="px-[4px]"
          >
            {{ tagMap[apiConfig.status].text }}
          </MsTag>
        </div>
        <a-input
          v-model:model-value="apiConfig.userUrl"
          :placeholder="t('ms.personal.apiLocalExecutionPlaceholder')"
          class="mb-[16px]"
          :max-length="255"
          @press-enter="testApi"
        ></a-input>
        <div class="config-card-footer">
          <a-button
            type="outline"
            class="px-[8px]"
            size="mini"
            :disabled="apiConfig.userUrl.trim() === ''"
            :loading="testApiLoading"
            @click="testApi"
          >
            {{ t('ms.personal.test') }}
          </a-button>
          <div class="flex items-center">
            <div class="mr-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
              {{ t('ms.personal.priorityLocalExec') }}
            </div>
            <a-switch
              v-model:model-value="apiConfig.enable"
              size="small"
              :disabled="apiConfig.id === '' || testApiLoading"
              :before-change="(val) => handleApiPriorityBeforeChange(val)"
              type="line"
            />
          </div>
        </div>
      </div>
      <div v-xpack class="config-card">
        <div class="config-card-title">
          <div class="config-card-title-text">{{ t('ms.personal.uiLocalExecution') }}</div>
          <MsTag
            v-if="uiConfig.status !== 'none'"
            theme="outline"
            :type="tagMap[uiConfig.status].type"
            size="small"
            class="px-[4px]"
          >
            {{ tagMap[uiConfig.status].text }}
          </MsTag>
        </div>
        <a-input
          v-model:model-value="uiConfig.userUrl"
          :placeholder="t('ms.personal.uiLocalExecutionPlaceholder')"
          class="mb-[16px]"
          :max-length="255"
          @press-enter="testUi"
        ></a-input>
        <div class="config-card-footer">
          <a-button
            type="outline"
            class="px-[8px]"
            size="mini"
            :disabled="uiConfig.userUrl.trim() === ''"
            :loading="testUiLoading"
            @click="testUi"
          >
            {{ t('ms.personal.test') }}
          </a-button>
          <div class="flex items-center">
            <div class="mr-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
              {{ t('ms.personal.priorityLocalExec') }}
            </div>
            <a-switch
              v-model:model-value="uiConfig.enable"
              size="small"
              :disabled="uiConfig.id === '' || testUiLoading"
              :before-change="(val) => handleUiPriorityBeforeChange(val)"
              type="line"
            />
          </div>
        </div>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { onBeforeMount } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsTag, { TagType } from '@/components/pure/ms-tag/ms-tag.vue';

  import {
    addLocalConfig,
    disableLocalConfig,
    enableLocalConfig,
    getLocalConfig,
    updateLocalConfig,
    validLocalConfig,
  } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';

  import { LocalConfig } from '@/models/user';

  const { t } = useI18n();

  type Status = 0 | 1 | 2 | 'none'; // 0 未配置 1 测试通过 2 测试失败
  interface TagMapItem {
    type: TagType;
    text: string;
  }
  interface Config {
    status: Status;
  }
  const tagMap: Record<Status, TagMapItem> = {
    0: {
      type: 'default',
      text: t('ms.personal.unConfig'),
    },
    1: {
      type: 'success',
      text: t('ms.personal.testPass'),
    },
    2: {
      type: 'danger',
      text: t('ms.personal.testFail'),
    },
    none: {
      type: 'default',
      text: '',
    },
  };
  const loading = ref(false);
  const testApiLoading = ref(false);
  const apiConfig = ref<LocalConfig & Config>({
    id: '',
    userUrl: '',
    enable: false,
    type: 'API',
    status: 0,
  });

  async function testApi() {
    try {
      testApiLoading.value = true;
      if (apiConfig.value.id) {
        // 已经存在配置
        await updateLocalConfig({
          id: apiConfig.value.id,
          userUrl: apiConfig.value.userUrl.trim(),
        });
      } else {
        const result = await addLocalConfig({
          type: 'API',
          userUrl: apiConfig.value.userUrl.trim(),
        });
        apiConfig.value.id = result.id;
      }
      const res = await validLocalConfig(apiConfig.value.id);
      apiConfig.value.status = res ? 1 : 2;
      if (res) {
        Message.success(t('ms.personal.testPass'));
      } else {
        Message.error(t('ms.personal.testFail'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      testApiLoading.value = false;
    }
  }

  async function handleApiPriorityBeforeChange(val: string | number | boolean) {
    try {
      if (val) {
        await enableLocalConfig(apiConfig.value.id);
      } else {
        await disableLocalConfig(apiConfig.value.id);
      }
      Message.success(val ? t('ms.personal.apiLocalExecutionOpen') : t('ms.personal.apiLocalExecutionClose'));
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  const testUiLoading = ref(false);
  const uiConfig = ref<LocalConfig & Config>({
    id: '',
    userUrl: '',
    enable: false,
    type: 'UI',
    status: 0,
  });

  async function testUi() {
    try {
      testUiLoading.value = true;
      if (uiConfig.value.id) {
        // 已经存在配置
        await updateLocalConfig({
          id: uiConfig.value.id,
          userUrl: uiConfig.value.userUrl.trim(),
        });
      } else {
        const result = await addLocalConfig({
          type: 'UI',
          userUrl: uiConfig.value.userUrl.trim(),
        });
        uiConfig.value.id = result.id;
      }
      const res = await validLocalConfig(uiConfig.value.id);
      uiConfig.value.status = res ? 1 : 2;
      if (res) {
        Message.success(t('ms.personal.testPass'));
      } else {
        Message.error(t('ms.personal.testFail'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      testUiLoading.value = false;
    }
  }

  async function handleUiPriorityBeforeChange(val: string | number | boolean) {
    try {
      if (val) {
        await enableLocalConfig(uiConfig.value.id);
      } else {
        await disableLocalConfig(uiConfig.value.id);
      }
      Message.success(val ? t('ms.personal.uiLocalExecutionOpen') : t('ms.personal.uiLocalExecutionClose'));
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  onBeforeMount(async () => {
    try {
      loading.value = true;
      const res = await getLocalConfig();
      if (Array.isArray(res)) {
        res.forEach((config) => {
          if (config.type === 'API') {
            apiConfig.value = {
              ...config,
              status: 'none',
            };
          } else if (config.type === 'UI') {
            uiConfig.value = {
              ...config,
              status: 'none',
            };
          }
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  });
</script>

<style lang="less" scoped>
  .config-card {
    padding: 16px;
    border: 2px solid white;
    border-radius: var(--border-radius-medium);
    background-color: var(--color-text-n9);
    box-shadow: 0 6px 15px 0 rgb(120 56 135 / 5%);
    .config-card-title {
      @apply flex items-center;

      gap: 8px;
      margin-bottom: 16px;
      .config-card-title-text {
        @apply font-medium;

        color: var(--color-text-1);
      }
    }
    .config-card-footer {
      @apply flex items-center justify-between;
    }
  }
</style>
