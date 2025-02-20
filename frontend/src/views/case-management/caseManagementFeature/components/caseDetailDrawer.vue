<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawerVisible"
    :width="1200"
    :footer="false"
    :mask="false"
    :title="t('caseManagement.featureCase.caseDetailTitle', { id: detailInfo?.id, name: detailInfo?.name })"
    :detail-id="props.detailId"
    :detail-index="props.detailIndex"
    :get-detail-func="getCaseDetail"
    :pagination="props.pagination"
    :table-data="props.tableData"
    :page-change="props.pageChange"
    :mask-closable="true"
    @loaded="loadedCase"
  >
    <template #titleLeft>
      <div class="flex items-center"><caseLevel :case-level="caseLevels" /></div>
    </template>
    <template #titleRight="{ loading }">
      <div class="rightButtons flex items-center">
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="editLoading"
          @click="updateHandler('edit')"
        >
          <MsIcon type="icon-icon_edit_outlined" class="mr-1 font-[16px]" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="shareLoading"
          @click="shareHandler"
        >
          <MsIcon type="icon-icon_share1" class="mr-1 font-[16px]" />
          {{ t('caseManagement.featureCase.share') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="followLoading"
          @click="followHandler"
        >
          <MsIcon
            :type="detailInfo.followFlag ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
            class="mr-1 font-[16px]"
            :class="[detailInfo.followFlag ? 'text-[rgb(var(--warning-6))]' : '']"
          />
          {{ t('caseManagement.featureCase.follow') }}
        </MsButton>
        <MsButton type="icon" status="secondary" class="!rounded-[var(--border-radius-small)]">
          <a-dropdown position="br" :hide-on-select="false">
            <div>
              <icon-more class="mr-1" />
              <span> {{ t('caseManagement.featureCase.more') }}</span>
            </div>

            <template #content>
              <a-doption>
                <a-switch class="mr-1" size="small" type="line" />{{ t('caseManagement.featureCase.addToPublic') }}
              </a-doption>
              <a-doption @click="updateHandler('copy')">
                <MsIcon type="icon-icon_copy_filled" class="font-[16px]" />{{ t('common.copy') }}</a-doption
              >
              <a-doption class="error-6 text-[rgb(var(--danger-6))]" @click="deleteHandler()">
                <MsIcon type="icon-icon_delete-trash_outlined" class="font-[16px] text-[rgb(var(--danger-6))]" />
                {{ t('common.delete') }}
              </a-doption>
            </template>
          </a-dropdown>
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="!rounded-[var(--border-radius-small)]"
          @click="toggleFullScreen"
        >
          <MsIcon :type="isFullScreen ? 'icon-icon_off_screen' : 'icon-icon_full_screen_one'" class="mr-1" size="16" />
          {{ t('caseManagement.featureCase.fullScreen') }}
        </MsButton>
      </div>
    </template>
    <template #default>
      <div ref="wrapperRef" class="wrapperRef bg-white">
        <MsSplitBox ref="wrapperRef" expand-direction="right" :max="0.7" :min="0.7" :size="900">
          <template #first>
            <div class="leftWrapper">
              <div class="header h-[50px]">
                <a-menu mode="horizontal" :default-selected-keys="[activeTab]" @menu-item-click="clickMenu">
                  <a-menu-item key="detail">{{ t('caseManagement.featureCase.detail') }} </a-menu-item>
                  <a-menu-item v-for="tab of tabSetting" :key="tab.key">
                    <div class="flex items-center">
                      <span>{{ t(tab.title) }}</span>
                      <a-badge
                        class="ml-1"
                        :class="activeTab === tab.key ? 'active' : ''"
                        :count="1000"
                        :max-count="99"
                      /> </div
                  ></a-menu-item>
                  <a-menu-item key="setting">
                    <span @click="showMenuSetting">{{
                      t('caseManagement.featureCase.detailDisplaySetting')
                    }}</span></a-menu-item
                  >
                </a-menu>
              </div>
              <div class="leftContent mt-4 px-4">
                <TabDetail
                  v-if="activeTab === 'detail'"
                  ref="tabDetailRef"
                  :form="detailInfo"
                  :allow-edit="true"
                  :form-rules="formItem"
                  @update-success="updateSuccess"
                />
                <TabDemand v-else-if="activeTab === 'requirement'" :case-id="props.detailId" />
                <TabCaseTable v-else-if="activeTab === 'case'" :case-id="props.detailId" />
                <TabDefect v-else-if="activeTab === 'bug'" :case-id="props.detailId" />
                <TabDependency v-else-if="activeTab === 'dependency'" :case-id="props.detailId" />
                <TabCaseReview v-else-if="activeTab === 'caseReview'" :case-id="props.detailId" />
                <TabTestPlan v-else-if="activeTab === 'testPlan'" />
                <TabComment v-else-if="activeTab === 'comments'" ref="commentRef" :case-id="props.detailId" />
                <TabChangeHistory v-else-if="activeTab === 'changeHistory'" :case-id="props.detailId" />
              </div>
            </div>
          </template>
          <template #second>
            <div class="rightWrapper p-[24px]">
              <div class="mb-4 font-medium">{{ t('caseManagement.featureCase.basicInfo') }}</div>
              <div class="baseItem">
                <span class="label"> {{ t('caseManagement.featureCase.tableColumnModule') }}</span>
                <span class="w-[calc(100%-36%)]">
                  <a-tree-select
                    v-model="detailInfo.moduleId"
                    :data="caseTree"
                    class="w-full"
                    :allow-search="true"
                    :field-names="{
                      title: 'name',
                      key: 'id',
                      children: 'children',
                    }"
                    :tree-props="{
                      virtualListProps: {
                        height: 200,
                      },
                    }"
                    @change="handleChangeModule"
                  ></a-tree-select>
                </span>
              </div>
              <!-- 自定义字段开始 -->
              <MsFormCreate
                v-if="formRules.length"
                ref="formCreateRef"
                v-model:api="fApi"
                v-model:form-item="formItem"
                :form-rule="formRules"
                class="w-full"
                :option="options"
                @change="changeHandler"
              />
              <!-- 自定义字段结束 -->
              <div class="baseItem">
                <span class="label"> {{ t('caseManagement.featureCase.tableColumnCreateUser') }}</span>
                <span>{{ detailInfo?.createUser }}</span>
              </div>
              <div class="baseItem">
                <span class="label"> {{ t('caseManagement.featureCase.tableColumnCreateTime') }}</span>
                <span>{{ dayjs(detailInfo?.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
              </div>
            </div>
          </template>
        </MsSplitBox>
      </div>
      <inputComment
        v-model:content="content"
        v-model:notice-user-ids="noticeUserIds"
        v-permission="['FUNCTIONAL_CASE:READ+COMMENT']"
        :is-active="isActive"
        is-show-avatar
        is-use-bottom
        @publish="publishHandler"
        @cancel="cancelPublish"
      />
    </template>
  </MsDetailDrawer>
  <SettingDrawer ref="settingDrawerRef" v-model:visible="showSettingDrawer" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import inputComment from '@/components/business/ms-comment/input.vue';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import SettingDrawer from './tabContent/settingDrawer.vue';
  import TabDefect from './tabContent/tabBug/tabDefect.vue';
  import TabCaseTable from './tabContent/tabCase/tabCaseTable.vue';
  import TabCaseReview from './tabContent/tabCaseReview.vue';
  import TabChangeHistory from './tabContent/tabChangeHistory.vue';
  import TabComment from './tabContent/tabComment/tabCommentIndex.vue';
  import TabDemand from './tabContent/tabDemand/demand.vue';
  import TabDependency from './tabContent/tabDependency/tabDependency.vue';
  import TabDetail from './tabContent/tabDetail.vue';
  import TabTestPlan from './tabContent/tabTestPlan.vue';

  import {
    createCommentList,
    deleteCaseRequest,
    followerCaseRequest,
    getCaseDetail,
    getCaseModuleTree,
  } from '@/api/modules/case-management/featureCase';
  import useFullScreen from '@/hooks/useFullScreen';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import useUserStore from '@/store/modules/user';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { CustomAttributes, DetailCase, TabItemType } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode } from '@/models/projectManagement/file';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  import { getCaseLevels } from './utils';
  import { LabelValue } from '@arco-design/web-vue/es/tree-select/interface';
  import debounce from 'lodash-es/debounce';

  const router = useRouter();
  const detailDrawerRef = ref<InstanceType<typeof MsDetailDrawer>>();
  const wrapperRef = ref();
  const { isFullScreen, toggleFullScreen } = useFullScreen(wrapperRef);
  const featureCaseStore = useFeatureCaseStore();
  const userStore = useUserStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const props = defineProps<{
    visible: boolean;
    detailId: string; // 详情 id
    detailIndex: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
  }>();

  const emit = defineEmits(['update:visible']);

  const userId = computed(() => userStore.userInfo.id);
  const appStore = useAppStore();
  provide('caseId', props.detailId);

  const currentProjectId = computed(() => appStore.currentProjectId);

  const showDrawerVisible = ref<boolean>(false);

  const showSettingDrawer = ref<boolean>(false);
  function showMenuSetting() {
    showSettingDrawer.value = true;
  }

  const tabSettingList = computed(() => {
    return featureCaseStore.tabSettingList;
  });

  const tabSetting = ref<TabItemType[]>([...tabSettingList.value]);
  const activeTab = ref<string | number>('detail');
  function clickMenu(key: string | number) {
    activeTab.value = key;
    switch (activeTab.value) {
      case 'setting':
        activeTab.value = 'detail';
        showMenuSetting();
        break;
      default:
        showSettingDrawer.value = false;
        break;
    }
  }
  const initDetail: DetailCase = {
    id: '',
    projectId: '',
    templateId: '',
    name: '',
    prerequisite: '', // prerequisite
    caseEditType: '', // 编辑模式：步骤模式/文本模式
    steps: '',
    textDescription: '',
    expectedResult: '', // 预期结果
    description: '',
    publicCase: false, // 是否公共用例
    moduleId: '',
    versionId: '',
    tags: [],
    customFields: [], // 自定义字段集合
    relateFileMetaIds: [], // 关联文件ID集合
    functionalPriority: '',
    reviewStatus: 'UN_REVIEWED',
  };

  const caseTree = ref<ModuleTreeNode[]>([]);

  async function getCaseTree() {
    try {
      caseTree.value = await getCaseModuleTree({ projectId: currentProjectId.value });
    } catch (error) {
      console.log(error);
    }
  }

  const detailInfo = ref<DetailCase>({ ...initDetail });
  const customFields = ref<CustomAttributes[]>([]);
  const caseLevels = ref<CaseLevel>('P0');
  function loadedCase(detail: DetailCase) {
    getCaseTree();
    detailInfo.value = { ...detail };
    customFields.value = detailInfo.value.customFields;
    caseLevels.value = getCaseLevels(customFields.value) as CaseLevel;
  }

  const editLoading = ref<boolean>(false);

  function updateSuccess() {
    detailDrawerRef.value?.initDetail();
  }

  function updateHandler(type: string) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
      query: {
        id: detailInfo.value.id,
      },
      params: {
        mode: type,
      },
    });
  }

  const shareLoading = ref<boolean>(false);

  function shareHandler() {}

  const followLoading = ref<boolean>(false);
  // 关注
  async function followHandler() {
    followLoading.value = true;
    try {
      if (detailInfo.value.id) {
        await followerCaseRequest({ userId: userId.value as string, functionalCaseId: detailInfo.value.id });
        updateSuccess();
        Message.success(
          detailInfo.value.followFlag
            ? t('caseManagement.featureCase.cancelFollowSuccess')
            : t('caseManagement.featureCase.followSuccess')
        );
      }
    } catch (error) {
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  // 删除用例
  function deleteHandler() {
    const { id, name } = detailInfo.value;
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.deleteCaseTitle', { name: characterLimit(name) }),
      content: t('caseManagement.featureCase.beforeDeleteCase'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const params = {
            id,
            deleteAll: false,
            projectId: currentProjectId.value,
          };
          await deleteCaseRequest(params);
          Message.success(t('common.deleteSuccess'));
          updateSuccess();
          detailDrawerRef.value?.openPrevDetail();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);

  const isDisabled = ref<boolean>(false);

  // 表单配置项
  const options = {
    resetBtn: false, // 不展示默认配置的重置和提交
    submitBtn: false,
    on: false, // 取消绑定on事件
    form: {
      layout: 'horizontal',
      labelAlign: 'left',
      labelColProps: {
        span: 9,
      },
      wrapperColProps: {
        span: 15,
      },
    },
    // 暂时默认
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
      'hide-asterisk': true,
    },
  };

  const fApi = ref(null);
  // 初始化表单
  function initForm() {
    formRules.value = customFields.value.map((item: any) => {
      const multipleType = ['MULTIPLE_SELECT', 'CHECKBOX', 'MULTIPLE_MEMBER', 'MULTIPLE_INPUT'];
      const currentDefaultValue = multipleType.includes(item.type) ? JSON.parse(item.defaultValue) : item.defaultValue;
      return {
        ...item,
        type: item.type,
        name: item.fieldId,
        label: item.fieldName,
        value: currentDefaultValue,
        required: item.required,
        options: item.options || [],
        props: {
          modelValue: currentDefaultValue,
          disabled: !hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']),
          options: item.options || [],
        },
      };
    }) as FormItem[];
  }

  const tabDetailRef = ref();
  function handleChangeModule(value: string | number | LabelValue | Array<string | number> | LabelValue[] | undefined) {
    detailInfo.value.moduleId = value as string;
    tabDetailRef.value.handleOK();
  }

  watch(
    () => customFields.value,
    () => {
      initForm();
    },
    { deep: true }
  );
  const settingDrawerRef = ref();
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        showDrawerVisible.value = val;
        activeTab.value = 'detail';
        settingDrawerRef.value.getTabModule();
      }
    }
  );

  watch(
    () => showDrawerVisible.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  watch(
    () => tabSettingList.value,
    () => {
      tabSetting.value = featureCaseStore.getTab();
    },
    { deep: true, immediate: true }
  );

  const content = ref('');
  const commentRef = ref();
  const isActive = ref<boolean>(false);

  const noticeUserIds = ref<string[]>([]);
  async function publishHandler(currentContent: string) {
    try {
      const params: CommentParams = {
        caseId: detailInfo.value.id,
        notifier: noticeUserIds.value.join(';'),
        replyUser: '',
        parentId: '',
        content: currentContent,
        event: noticeUserIds.value.join(';') ? 'AT' : 'COMMENT', // 任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
      };
      await createCommentList(params);
      commentRef.value.getAllCommentList();
      Message.success(t('common.publishSuccessfully'));
    } catch (error) {
      console.log(error);
    }
  }

  function cancelPublish() {
    isActive.value = !isActive.value;
  }

  const changeHandler = debounce(() => {
    tabDetailRef.value.handleOK();
  }, 300);

  watch(
    () => props.detailId,
    (val) => {
      if (val) {
        updateSuccess();
      }
    }
  );
</script>

<style scoped lang="less">
  .wrapperRef {
    height: calc(100% - 78px);
  }
  :deep(.arco-menu-light) {
    height: 50px;
    background: none !important;
    .arco-menu-inner {
      overflow: hidden;
      height: 50px;
    }
  }
  .leftWrapper {
    .header {
      padding: 0 16px;
      border-bottom: 1px solid var(--color-text-n8);
    }
  }
  .rightWrapper {
    .baseItem {
      margin-bottom: 16px;
      height: 32px;
      line-height: 32px;
      @apply flex;
      .label {
        width: 38%;
        color: var(--color-text-3);
      }
    }
    :deep(.arco-form-item-layout-horizontal) {
      margin-bottom: 16px !important;
    }
    :deep(.arco-form-item-label-col > .arco-form-item-label) {
      color: var(--color-text-3) !important;
    }
  }
  .rightButtons {
    :deep(.ms-button--secondary):hover,
    :hover > .arco-icon {
      color: rgb(var(--primary-5)) !important;
      background: var(--color-bg-3);
      .arco-icon:hover {
        color: rgb(var(--primary-5)) !important;
      }
    }
  }
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
  :deep(.active .arco-badge-number) {
    background: rgb(var(--primary-5));
  }
</style>
