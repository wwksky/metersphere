<template>
  <MsDrawer
    v-model:visible="showScriptDrawer"
    :title="t('project.commonScript.addPublicScript')"
    :width="768"
    :footer="true"
    unmount-on-close
    :ok-loading="props.confirmLoading"
    save-continue-text="project.commonScript.saveAsDraft"
    ok-text="project.commonScript.apply"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        field="name"
        :label="t('project.commonScript.publicScriptName')"
        :rules="[{ required: true, message: t('project.commonScript.publicScriptNameNotEmpty') }]"
      >
        <a-input v-model="form.name" :max-length="255" :placeholder="t('project.commonScript.pleaseEnterScriptName')" />
      </a-form-item>
      <a-form-item field="status" :label="t('project.commonScript.scriptEnabled')">
        <a-select v-model="form.status" class="max-w-[396px]" :placeholder="t('project.commonScript.scriptEnabled')">
          <a-option value="DRAFT">{{ t('project.commonScript.draft') }}</a-option>
          <a-option value="PASSED">{{ t('project.commonScript.testsPass') }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="description" :label="t('system.organization.description')">
        <a-textarea
          v-model="form.description"
          :placeholder="t('system.organization.descriptionPlaceholder')"
          allow-clear
          :auto-size="{ minRows: 1 }"
          :max-length="1000"
        />
      </a-form-item>
      <a-form-item field="tags" :label="t('project.commonScript.tags')">
        <MsTagsInput v-model:modelValue="form.tags"></MsTagsInput>
      </a-form-item>
      <a-form-item field="inputParameters" :label="t('project.commonScript.inputParams')">
        <paramTable
          v-model:params="innerParams"
          :scroll="{ x: '100%' }"
          :columns="columns"
          :height-used="heightUsed"
          @change="handleParamTableChange"
        />
      </a-form-item>
      <div class="mb-2 flex items-center justify-between">
        <a-radio-group v-model:model-value="scriptType" type="button" size="small">
          <a-radio value="commonScript">{{ t('project.commonScript.commonScript') }}</a-radio>
          <a-radio value="executionResult">{{ t('project.commonScript.executionResult') }}</a-radio>
        </a-radio-group>
        <a-button
          v-permission="['PROJECT_CUSTOM_FUNCTION:READ+EXECUTE']"
          type="outline"
          :loading="loading"
          @click="testScript"
          >{{ t('project.commonScript.scriptTest') }}</a-button
        >
      </div>
      <ScriptDefined
        v-model:language="form.type"
        v-model:code="form.script"
        :show-type="scriptType"
        :enable-radio-selected="props.enableRadioSelected"
      />
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { FormInstance } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import ScriptDefined from './scriptDefined.vue';
  import paramTable from '@/views/api-test/components/paramTable.vue';

  import { getCommonScriptDetail, getSocket, testCommonScript } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId, sleep } from '@/utils';

  import type { AddOrUpdateCommonScript, ParamsRequestType } from '@/models/projectManagement/commonScript';

  const appStore = useAppStore();

  const heightUsed = ref<number | undefined>(undefined);
  const formRef = ref<FormInstance>();
  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    params: ParamsRequestType[];
    confirmLoading: boolean;
    scriptId?: string; // 脚本id
    enableRadioSelected?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'update:visible', value: boolean): void;
    (e: 'change'): void;
    (e: 'save', form: AddOrUpdateCommonScript): void;
    (e: 'close'): void;
  }>();

  const showScriptDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

  const initForm: AddOrUpdateCommonScript = {
    name: '',
    status: '',
    tags: [],
    description: '',
    projectId: '',
    params: '',
    script: '',
    type: 'beanshell-jsr233',
    result: '',
  };

  const form = ref({ ...initForm });

  const columns: MsTableColumn = [
    {
      title: 'project.commonScript.ParameterNames',
      slotName: 'name',
      dataIndex: 'name',
    },
    {
      title: 'project.commonScript.ParameterValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'project.commonScript.description',
      slotName: 'desc',
      dataIndex: 'desc',
    },
    {
      title: 'project.commonScript.isRequired',
      slotName: 'mustContain',
      dataIndex: 'required',
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];

  const scriptType = ref<'commonScript' | 'executionResult'>('commonScript');

  const innerParams = useVModel(props, 'params', emit);

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    innerParams.value = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  function handleDrawerConfirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        emit('save', form.value);
      }
    });
  }

  function reset() {
    formRef.value?.resetFields();
    form.value = { ...initForm };
    innerParams.value = [];
  }

  function handleDrawerCancel() {
    emit('close');
    reset();
  }

  const editScriptId = ref<string | undefined>('');

  async function getDetail() {
    try {
      if (editScriptId.value) {
        const result = await getCommonScriptDetail(editScriptId.value);
        form.value = cloneDeep(result);
        innerParams.value = JSON.parse(result.params);
      }
    } catch (error) {
      console.log(error);
    }
  }

  watch(
    () => props.scriptId,
    (val) => {
      if (val) {
        editScriptId.value = val;
        if (val) {
          getDetail();
        }
      }
    }
  );

  watch(
    () => showScriptDrawer.value,
    (val) => {
      if (val) {
        form.value = { ...initForm };
        innerParams.value = [];
      }
    }
  );

  const loading = ref<boolean>(false);
  const websocket = ref<any>();
  const reportId = ref('');
  async function run() {
    try {
      const { type, script } = form.value;
      const parameters = innerParams.value
        .filter((item: any) => item.name && item.value)
        .map((item) => {
          return {
            key: item.name,
            value: item.value,
            valid: item.mustContain,
          };
        });
      const params = {
        type,
        script,
        params: parameters,
        projectId: appStore.currentProjectId,
        reportId: reportId.value,
      };
      await testCommonScript(params);
    } catch (error) {
      console.log(error);
    }
  }
  // 提交测试请求
  function onOpen() {
    run();
  }

  const executionResult = ref<string>('');
  // TODO 提交给后台测试
  function onDebugMessage(e: any) {
    // console.log(e.data);
    if (e.data) {
      try {
        // websocket.value.close();
        executionResult.value = JSON.parse(e.data);
        console.log(JSON.parse(e.data));
      } catch (error) {
        // websocket.value.close();
        console.log(error);
      }
    }
  }
  // TODO 提交给后台测试
  function debugSocket() {
    websocket.value = getSocket(reportId.value);
    websocket.value.onmessage = onDebugMessage;
    websocket.value.onopen = onOpen;

    websocket.value.addEventListener('open', (event) => {
      console.log('打开:', event);
    });

    websocket.value.addEventListener('message', (event) => {
      console.log('接收:', event.data);
    });

    websocket.value.addEventListener('close', (event) => {
      console.log('关闭:', event);
    });

    websocket.value.addEventListener('error', (event) => {
      console.error('错误:', event);
    });
  }

  // 测试脚本
  async function testScript() {
    reportId.value = getGenerateId();
  }

  watch(
    () => reportId.value,
    (val) => {
      if (val) {
        debugSocket();
      }
    }
  );
</script>

<style scoped></style>
