<template>
  <div class="navbar">
    <div class="left-side">
      <a-space>
        <div class="one-line-text flex max-w-[145px] items-center">
          <img :src="props.logo" class="mr-[4px] h-[34px] w-[32px]" />
          <div class="font-['Helvetica_Neue'] text-[16px] font-bold text-[rgb(var(--primary-5))]">{{ props.name }}</div>
        </div>
      </a-space>
    </div>
    <div v-if="!props.isPreview" class="center-side">
      <template v-if="showProjectSelect">
        <a-divider direction="vertical" class="ml-0" />
        <a-select
          class="w-[200px] focus-within:!bg-[var(--color-text-n8)] hover:!bg-[var(--color-text-n8)]"
          :default-value="appStore.currentProjectId"
          :bordered="false"
          allow-search
          @change="selectProject"
        >
          <template #arrow-icon>
            <icon-caret-down />
          </template>
          <a-tooltip v-for="project of projectList" :key="project.id" :mouse-enter-delay="500" :content="project.name">
            <a-option
              :value="project.id"
              :class="project.id === appStore.currentProjectId ? 'arco-select-option-selected' : ''"
            >
              {{ project.name }}
            </a-option>
          </a-tooltip>
        </a-select>
        <a-divider direction="vertical" class="mr-0" />
      </template>
      <TopMenu />
    </div>
    <ul v-if="!props.isPreview && !props.hideRight" class="right-side">
      <li>
        <a-tooltip :content="t('settings.navbar.search')">
          <a-button type="secondary">
            <template #icon>
              <icon-search />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="t('settings.navbar.alerts')">
          <div class="message-box-trigger">
            <a-badge :count="9" dot>
              <a-button type="secondary" @click="setPopoverVisible">
                <template #icon>
                  <icon-notification />
                </template>
              </a-button>
            </a-badge>
          </div>
        </a-tooltip>
        <a-popover
          trigger="click"
          :arrow-style="{ display: 'none' }"
          :content-style="{ padding: 0, minWidth: '400px' }"
          content-class="message-popover"
        >
          <div ref="refBtn" class="ref-btn"></div>
          <template #content>
            <message-box />
          </template>
        </a-popover>
      </li>
      <li>
        <a-tooltip :content="t('settings.navbar.task')">
          <a-button type="secondary">
            <template #icon>
              <icon-calendar-clock />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-dropdown trigger="click" position="br">
          <a-tooltip :content="t('settings.navbar.help')">
            <a-button type="secondary">
              <template #icon>
                <icon-question-circle />
              </template>
            </a-button>
          </a-tooltip>
          <template #content>
            <a-doption v-for="item in helpCenterList" :key="item.name" :value="item.name">
              <component :is="item.icon"></component>
              {{ t(item.name) }}
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <a-dropdown trigger="click" position="br" @select="changeLocale as any">
          <a-tooltip :content="t('settings.language')">
            <a-button type="secondary">
              <template #icon>
                <icon-translate />
              </template>
            </a-button>
          </a-tooltip>
          <template #content>
            <a-doption v-for="item in locales" :key="item.value" :value="item.value">
              <template #icon>
                <icon-check v-show="item.value === currentLocale" />
              </template>
              {{ item.label }}
            </a-doption>
          </template>
        </a-dropdown>
      </li>
    </ul>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onBeforeMount, Ref, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import TopMenu from '@/components/business/ms-top-menu/index.vue';
  import MessageBox from '../message-box/index.vue';

  import { getProjectList, switchProject } from '@/api/modules/project-management/project';
  import { MENU_LEVEL, type PathMapRoute } from '@/config/pathMap';
  import { useI18n } from '@/hooks/useI18n';
  import usePathMap from '@/hooks/usePathMap';
  import { LOCALE_OPTIONS } from '@/locale';
  import useLocale from '@/locale/useLocale';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { hasAnyPermission } from '@/utils/permission';

  import type { ProjectListItem } from '@/models/setting/project';

  import { IconInfoCircle, IconQuestionCircle } from '@arco-design/web-vue/es/icon';

  const props = defineProps<{
    isPreview?: boolean;
    logo?: string;
    name?: string;
    hideRight?: boolean;
  }>();

  const appStore = useAppStore();
  const userStore = useUserStore();
  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();

  const projectList: Ref<ProjectListItem[]> = ref([]);

  async function initProjects() {
    try {
      if (appStore.currentOrgId && hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
        const res = await getProjectList(appStore.getCurrentOrgId);
        projectList.value = res;
      } else {
        projectList.value = [];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initProjects();
  });

  watch(
    () => appStore.currentOrgId,
    async () => {
      initProjects();
    }
  );

  const showProjectSelect = computed(() => {
    const { getRouteLevelByKey } = usePathMap();
    // 非项目级别页面不需要展示项目选择器
    return getRouteLevelByKey(route.name as PathMapRoute) === MENU_LEVEL[2];
  });

  async function selectProject(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    appStore.setCurrentProjectId(value as string);
    try {
      await switchProject({
        projectId: value as string,
        userId: userStore.id || '',
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      router.replace({
        path: route.path,
        query: {
          ...route.query,
          organizationId: appStore.currentOrgId,
          projectId: appStore.currentProjectId,
        },
      });
    }
  }

  const helpCenterList = [
    // {
    //   name: 'settings.help.guide',
    //   icon: IconCompass,
    //   route: '/help-center/guide',
    // },
    {
      name: 'settings.help.doc',
      icon: IconQuestionCircle,
      route: '/help-center/guide',
    },
    // {
    //   name: 'settings.help.APIDoc',
    //   icon: IconFile,
    //   route: '/help-center/guide',
    // },
    {
      name: 'settings.help.version',
      icon: IconInfoCircle,
      route: '/help-center/guide',
    },
  ];

  const { changeLocale, currentLocale } = useLocale();
  const locales = [...LOCALE_OPTIONS];

  const refBtn = ref();
  const setPopoverVisible = () => {
    const event = new MouseEvent('click', {
      view: window,
      bubbles: true,
      cancelable: true,
    });
    refBtn.value.dispatchEvent(event);
  };
</script>

<style scoped lang="less">
  .navbar {
    @apply flex h-full justify-between bg-transparent;
  }
  .left-side {
    @apply flex items-center;

    padding-left: 24px;
    width: 185px;
  }
  .center-side {
    @apply flex flex-1 items-center;
  }
  .right-side {
    @apply flex list-none;

    padding-right: 20px;
    :deep(.locale-select) {
      border-radius: 20px;
    }
    li {
      @apply flex items-center;

      padding-left: 10px;
      .arco-btn-secondary {
        @apply !bg-transparent;

        color: var(--color-text-4) !important;
        &:hover,
        &:focus-visible {
          color: var(--color-text-1) !important;
        }
      }
    }
    a {
      @apply no-underline;

      color: var(--color-text-1);
    }
    .nav-btn {
      font-size: 16px;
      border-color: rgb(var(--gray-2));
      color: rgb(var(--gray-8));
      line-height: 24px;
    }
    .trigger-btn,
    .ref-btn {
      @apply absolute;

      bottom: 14px;
    }
    .trigger-btn {
      margin-left: 14px;
    }
  }
</style>

<style lang="less">
  .message-popover {
    .arco-popover-content {
      @apply mt-0;
    }
  }
  .arco-menu-horizontal {
    .arco-menu-inner {
      .arco-menu-item,
      .arco-menu-overflow-sub-menu {
        @apply !bg-transparent;
      }
      .arco-menu-selected {
        @apply !font-normal;

        color: rgb(var(--primary-5)) !important;
        .arco-menu-selected-label {
          bottom: -11px;
          background-color: rgb(var(--primary-5)) !important;
        }
      }
    }
  }
  .arco-trigger-menu-vertical {
    max-height: 500px;
    .arco-trigger-menu-selected {
      @apply !font-normal;

      color: rgb(var(--primary-5)) !important;
    }
  }
</style>
@/models/setting/project @/api/modules/setting/project @/api/modules/project-management/project
