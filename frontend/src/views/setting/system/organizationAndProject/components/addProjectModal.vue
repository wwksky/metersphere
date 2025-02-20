<template>
  <a-modal
    v-model:visible="currentVisible"
    class="ms-modal-form ms-modal-medium"
    :ok-text="isEdit ? t('common.update') : t('common.create')"
    title-align="start"
    unmount-on-close
    @cancel="handleCancel(false)"
  >
    <template #title>
      <span v-if="isEdit">
        {{ t('system.project.updateProject') }}
        <span class="text-[var(--color-text-4)]">({{ props.currentProject?.name }})</span>
      </span>
      <span v-else>
        {{ t('system.project.createProject') }}
      </span>
    </template>
    <div class="form">
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item
          field="name"
          required
          asterisk-position="end"
          :label="t('system.project.name')"
          :rules="[{ required: true, message: t('system.project.projectNameRequired') }]"
        >
          <a-input v-model="form.name" :max-length="255" :placeholder="t('system.project.projectNamePlaceholder')" />
        </a-form-item>
        <a-form-item
          required
          field="organizationId"
          asterisk-position="end"
          :label="t('system.project.affiliatedOrg')"
          :rules="[{ required: true, message: t('system.project.affiliatedOrgRequired') }]"
        >
          <a-select
            v-model="form.organizationId"
            :disabled="!isXpack"
            allow-search
            :options="affiliatedOrgOption"
            :placeholder="t('system.project.affiliatedOrgPlaceholder')"
            :field-names="{ label: 'name', value: 'id' }"
            :extra="t('system.project.affiliatedOrgExtra')"
          >
          </a-select>
        </a-form-item>
        <a-form-item field="userIds" :label="t('system.project.projectAdmin')">
          <MsUserSelector
            v-model="form.userIds"
            :type="UserRequestTypeEnum.SYSTEM_PROJECT_ADMIN"
            placeholder="system.project.projectAdminPlaceholder"
          />
        </a-form-item>
        <a-form-item field="module" :label="t('system.project.moduleSetting')">
          <a-checkbox-group v-model="form.moduleIds" :options="moduleOption">
            <template #label="{ data }">
              <span>{{ t(data.label) }}</span>
            </template>
          </a-checkbox-group>
        </a-form-item>
        <a-form-item v-if="showPool" field="resourcePool" :label="t('system.project.resourcePool')">
          <MsSystemPool v-model:modelValue="form.resourcePoolIds" :organization-id="form.organizationId" />
        </a-form-item>
        <a-form-item field="description" :label="t('system.organization.description')">
          <a-textarea
            v-model="form.description"
            :max-length="1000"
            :placeholder="t('system.organization.descriptionPlaceholder')"
            allow-clear
            :auto-size="{ minRows: 1 }"
          />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <div class="flex flex-row justify-between">
        <div class="flex flex-row items-center gap-[4px]">
          <a-switch v-model="form.enable" size="small" type="line" />
          <span>{{ t('system.organization.status') }}</span>
          <a-tooltip :content="t('system.project.createTip')" position="top">
            <MsIcon type="icon-icon-maybe_outlined" class="text-[var(--color-text-4)]" />
          </a-tooltip>
        </div>
        <div class="flex flex-row gap-[14px]">
          <a-button type="secondary" :disabled="loading" @click="handleCancel(false)">
            {{ t('common.cancel') }}
          </a-button>
          <a-button type="primary" :loading="loading" @click="handleBeforeOk">
            {{ isEdit ? t('common.confirm') : t('common.create') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, watchEffect } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSystemPool from '@/components/business/ms-system-pool/MsSystemPool.vue';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { createOrUpdateProject, getSystemOrgOption } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import useLicenseStore from '@/store/modules/setting/license';

  import { CreateOrUpdateSystemProjectParams, SystemOrgOption } from '@/models/setting/system/orgAndProject';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    currentProject?: CreateOrUpdateSystemProjectParams;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);
  const isEdit = computed(() => !!props.currentProject?.id);
  const affiliatedOrgOption = ref<SystemOrgOption[]>([]);
  const licenseStore = useLicenseStore();
  const moduleOption = [
    { label: 'menu.workbench', value: 'workstation' },
    { label: 'menu.testPlan', value: 'testPlan' },
    { label: 'menu.bugManagement', value: 'bugManagement' },
    { label: 'menu.caseManagement', value: 'caseManagement' },
    { label: 'menu.apiTest', value: 'apiTest' },
    { label: 'menu.uiTest', value: 'uiTest' },
    { label: 'menu.performanceTest', value: 'loadTest' },
  ];

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
  }>();

  const allModuleIds = ['workstation', 'testPlan', 'bugManagement', 'caseManagement', 'apiTest', 'uiTest', 'loadTest'];

  const showPoolModuleIds = ['uiTest', 'apiTest', 'loadTest'];

  const form = reactive<CreateOrUpdateSystemProjectParams>({
    name: '',
    userIds: [],
    organizationId: '',
    description: '',
    enable: true,
    moduleIds: allModuleIds,
    resourcePoolIds: [],
  });

  const currentVisible = ref(props.visible);
  const showPool = computed(() => showPoolModuleIds.some((item) => form.moduleIds?.includes(item)));

  const isXpack = computed(() => {
    return licenseStore.hasLicense();
  });

  const formReset = () => {
    form.name = '';
    form.userIds = [];
    form.organizationId = '';
    form.description = '';
    form.enable = true;
    form.moduleIds = allModuleIds;
  };
  const handleCancel = (shouldSearch: boolean) => {
    emit('cancel', shouldSearch);
  };

  const handleBeforeOk = async () => {
    await formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        await createOrUpdateProject({ id: props.currentProject?.id, ...form });
        Message.success(
          isEdit.value ? t('system.project.updateProjectSuccess') : t('system.project.createProjectSuccess')
        );
        handleCancel(true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };
  const initAffiliatedOrgOption = async () => {
    try {
      const res = await getSystemOrgOption();
      affiliatedOrgOption.value = res;
      if (!isXpack.value) {
        form.organizationId = affiliatedOrgOption.value[0].id;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };
  watchEffect(() => {
    if (props.currentProject) {
      form.id = props.currentProject.id;
      form.name = props.currentProject.name;
      form.description = props.currentProject.description;
      form.enable = props.currentProject.enable;
      form.userIds = props.currentProject.userIds;
      form.organizationId = props.currentProject.organizationId;
      form.moduleIds = props.currentProject.moduleIds;
      form.resourcePoolIds = props.currentProject.resourcePoolIds;
    }
  });
  watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
      if (!val) {
        formReset();
      } else {
        initAffiliatedOrgOption();
      }
    }
  );
</script>
