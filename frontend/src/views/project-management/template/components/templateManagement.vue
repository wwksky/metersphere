<template>
  <MsCard has-breadcrumb simple>
    <div class="mb-4 flex items-center justify-between">
      <span v-if="isEnableOrdTemplate" class="font-medium">{{ t('system.orgTemplate.templateList') }}</span>
      <a-button
        v-else
        v-permission="['PROJECT_TEMPLATE:READ+ADD']"
        type="primary"
        :disabled="false"
        @click="createTemplate"
      >
        {{ t('system.orgTemplate.createTemplate') }}
      </a-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('system.orgTemplate.searchTip')"
        class="w-[230px]"
        allow-clear
        @search="searchFiled"
        @press-enter="searchFiled"
      ></a-input-search>
    </div>
    <MsBaseTable v-bind="propsRes" ref="tableRef" v-on="propsEvent">
      <template #defaultTemplate="{ record }">
        <a-switch
          v-model="record.enableDefault"
          :disabled="record.enableDefault || isEnableOrdTemplate"
          size="small"
          type="line"
          @change="(value) => changeDefault(value, record)"
        />
      </template>
      <template #enableThirdPart="{ record }">
        {{ record.enableThirdPart ? t('system.orgTemplate.yes') : t('system.orgTemplate.no') }}
      </template>
      <template #name="{ record }">
        <div class="flex items-center">
          <span class="ml-2 cursor-pointer text-[rgb(var(--primary-5))]" @click="previewDetail(record)">{{
            record.name
          }}</span>
          <MsTag v-if="record.internal" size="small" class="ml-2">{{ t('system.orgTemplate.isSystem') }}</MsTag>
        </div>
      </template>
      <template #operation="{ record }">
        <div v-if="!record.enablePlatformDefault" class="flex flex-row flex-nowrap items-center">
          <MsButton v-permission="['PROJECT_TEMPLATE:READ+UPDATE']" @click="editTemplate(record.id)">{{
            t('system.orgTemplate.edit')
          }}</MsButton>
          <MsButton v-permission="['PROJECT_TEMPLATE:READ+ADD']" class="!mr-0" @click="copyTemplate(record.id)">{{
            t('system.orgTemplate.copy')
          }}</MsButton>
          <a-divider
            v-if="!record.internal"
            v-permission="['PROJECT_TEMPLATE:READ+ADD']"
            class="h-[16px]"
            direction="vertical"
          />
          <MsTableMoreAction
            v-if="!record.internal"
            v-permission="['PROJECT_TEMPLATE:READ+DELETE']"
            :list="moreActions"
            @select="(item) => handleMoreActionSelect(item, record)"
          />
        </div>
      </template>
    </MsBaseTable>
    <MsDrawer
      v-model:visible="showDetailVisible"
      :title="titleDetail"
      :width="1200"
      :footer="false"
      unmount-on-close
      @cancel="handleCancel"
    >
      <PreviewTemplate
        :select-field="(selectData as DefinedFieldItem[])"
        :template-type="route.query.type"
        :defect-form="defectForm"
      />
    </MsDrawer>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-项目-模版-模版管理列表
   */
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import PreviewTemplate from '@/views/setting/organization/template/components/viewTemplate.vue';

  import {
    deleteOrdTemplate,
    getProjectFieldList,
    getProjectTemplateInfo,
    getProjectTemplateList,
    setDefaultTemplate,
  } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import router from '@/router';
  import { useAppStore, useTableStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';
  import { characterLimit } from '@/utils';

  import type { DefinedFieldItem, OrdTemplateManagement } from '@/models/setting/template';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import {
    getCardList,
    getCustomDetailFields,
    getTotalFieldOptionList,
  } from '@/views/setting/organization/template/components/fieldSetting';

  const route = useRoute();
  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const templateStore = useTemplateStore();
  const { openModal } = useModal();

  const keyword = ref('');
  const currentProjectId = computed(() => appStore.currentProjectId);

  const sceneType = computed(() => route.query.type);

  const fieldColumns: MsTableColumn = [
    {
      title: 'system.orgTemplate.columnTemplateName',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      fixed: 'left',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.defaultTemplate',
      dataIndex: 'remark',
      slotName: 'defaultTemplate',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.description',
      dataIndex: 'remark',
      showDrag: true,
      showInTable: true,
      width: 300,
      showTooltip: true,
    },
    {
      title: 'system.orgTemplate.columnFieldUpdatedTime',
      dataIndex: 'updateTime',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 200,
      showInTable: true,
      showDrag: false,
    },
  ];

  const isThirdParty = {
    title: 'system.orgTemplate.isThirdParty',
    dataIndex: 'enableThirdPart',
    slotName: 'enableThirdPart',
    showDrag: true,
    showInTable: true,
  };

  await tableStore.initColumn(TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT, fieldColumns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams, setProps } = useTable(getProjectTemplateList, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT,
    scroll: { x: '1400px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: true,
    showPagination: false,
    heightUsed: 380,
  });
  const scene = route.query.type;
  const isEnableOrdTemplate = computed(() => {
    return !templateStore.projectStatus[scene as string];
  });

  const totalList = ref<OrdTemplateManagement[]>([]);

  // 查询字段
  const searchFiled = async () => {
    try {
      totalList.value = await getProjectTemplateList({ projectId: currentProjectId.value, scene });
      const filterData = totalList.value.filter((item: OrdTemplateManagement) => item.name.includes(keyword.value));
      setProps({ data: filterData });
    } catch (error) {
      console.log(error);
    }
  };

  const moreActions: ActionsItem[] = [
    {
      label: 'system.userGroup.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  // 删除模板
  const handlerDelete = (record: any) => {
    openModal({
      type: 'error',
      title: t('system.orgTemplate.deleteTemplateTitle', { name: characterLimit(record.name) }),
      content: t('system.orgTemplate.deleteProjectTemplateTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          if (record.id) await deleteOrdTemplate(record.id);
          Message.success(t('common.deleteSuccess'));
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  // 更多操作
  const handleMoreActionSelect = (item: ActionsItem, record: any) => {
    if (item.eventTag === 'delete') {
      handlerDelete(record);
    }
  };

  const fetchData = async () => {
    setLoadListParams({ projectId: currentProjectId.value, scene: route.query.type });
    await loadList();
  };

  // 设置默认模版
  const changeDefault = async (value: any, record: OrdTemplateManagement) => {
    if (value) {
      try {
        await setDefaultTemplate(currentProjectId.value, record.id);
        Message.success(t('system.orgTemplate.setSuccessfully'));
        fetchData();
      } catch (error) {
        console.log(error);
      }
    }
  };

  // 创建模板
  const createTemplate = () => {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_DETAIL,
      query: {
        type: route.query.type,
      },
      params: {
        mode: 'create',
      },
    });
  };

  // 编辑模板
  const editTemplate = (id: string) => {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_DETAIL,
      query: {
        id,
        type: route.query.type,
      },
      params: {
        mode: 'edit',
      },
    });
  };

  // 复制模板
  const copyTemplate = (id: string) => {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_DETAIL,
      query: {
        id,
        type: route.query.type,
      },
      params: {
        mode: 'copy',
      },
    });
  };

  const showDetailVisible = ref<boolean>(false);
  const selectData = ref<DefinedFieldItem[]>([]);
  const totalData = ref<DefinedFieldItem[]>([]);

  // 处理自定义字段列表
  const getFieldOptionList = () => {
    totalData.value = getTotalFieldOptionList(totalData.value as DefinedFieldItem[]);
  };

  const initDetailForm = {
    name: '',
    description: '',
  };
  const titleDetail = ref<string>();
  const defectForm = ref<Record<string, any>>({ ...initDetailForm });
  // 预览详情
  const previewDetail = async (record: OrdTemplateManagement) => {
    showDetailVisible.value = true;
    titleDetail.value = record.name;
    try {
      totalData.value = await getProjectFieldList({ scopedId: currentProjectId.value, scene: route.query.type });
      getFieldOptionList();
      const res = await getProjectTemplateInfo(record.id);
      selectData.value = getCustomDetailFields(totalData.value as DefinedFieldItem[], res.customFields);
      res.systemFields.forEach((item: any) => {
        defectForm.value[item.fieldId] = item.defaultValue;
      });
    } catch (error) {
      console.log(error);
    }
  };

  const handleCancel = () => {
    showDetailVisible.value = false;
    defectForm.value = { ...initDetailForm };
  };

  // 更新面包屑根据不同的模版
  const updateBreadcrumbList = () => {
    const { breadcrumbList } = appStore;
    const breadTitle = getCardList('project').find((item: any) => item.key === route.query.type);
    if (breadTitle) {
      breadcrumbList[0].locale = breadTitle.name;
      appStore.setBreadcrumbList(breadcrumbList);
    }
  };

  const tableRef = ref();

  function updateColumns() {
    const columns =
      sceneType.value === 'BUG' ? fieldColumns.slice(0, 2).concat(isThirdParty, fieldColumns.slice(2)) : fieldColumns;
    if (isEnableOrdTemplate.value) {
      const result = columns.slice(0, fieldColumns.length - 1);
      tableRef.value.initColumn(result);
    } else {
      tableRef.value.initColumn(columns);
    }
  }

  onMounted(() => {
    updateBreadcrumbList();
    fetchData();
    updateColumns();
  });
</script>

<style scoped lang="less">
  .system-flag {
    background: var(--color-text-n8);
    @apply ml-2 rounded p-1 text-xs;
  }
</style>
