import { defineStore } from 'pinia';

import { isLogin as userIsLogin, login as userLogin, logout as userLogout } from '@/api/modules/user';
import { useI18n } from '@/hooks/useI18n';
import { getHashParameters } from '@/utils';
import { clearToken, setToken } from '@/utils/auth';
import { composePermissions } from '@/utils/permission';
import { removeRouteListener } from '@/utils/route-listener';

import type { LoginData } from '@/models/user';

import useAppStore from '../app';
import useLicenseStore from '../setting/license';
import { UserState } from './types';

const useUserStore = defineStore('user', {
  // 开启数据持久化
  persist: true,
  state: (): UserState => ({
    name: undefined,
    avatar: undefined,
    job: undefined,
    organization: undefined,
    location: undefined,
    email: undefined,
    introduction: undefined,
    personalWebsite: undefined,
    jobName: undefined,
    organizationName: undefined,
    locationName: undefined,
    phone: undefined,
    registrationDate: undefined,
    id: undefined,
    certification: undefined,
    role: '',
    userRolePermissions: [],
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
    isAdmin(state: UserState): boolean {
      if (!state.userRolePermissions) return false;
      return state.userRolePermissions.findIndex((ur) => ur.userRole.id === 'admin') > -1;
    },
    currentRole(state: UserState): {
      projectPermissions: string[];
      orgPermissions: string[];
      systemPermissions: string[];
    } {
      const appStore = useAppStore();
      return {
        projectPermissions: composePermissions(state.userRolePermissions || [], 'PROJECT', appStore.currentProjectId),
        orgPermissions: composePermissions(state.userRolePermissions || [], 'ORGANIZATION', appStore.currentOrgId),
        systemPermissions: composePermissions(state.userRolePermissions || [], 'SYSTEM', 'global'),
      };
    },
  },

  actions: {
    switchRoles() {
      return new Promise((resolve) => {
        this.role = this.role === 'user' ? 'admin' : 'user';
        resolve(this.role);
      });
    },
    // 设置用户信息
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial);
    },
    // 重置用户信息
    resetInfo() {
      this.$reset();
    },

    // 登录
    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm);
        setToken(res.sessionId, res.csrfToken);
        const appStore = useAppStore();
        appStore.setCurrentOrgId(res.lastOrganizationId || '');
        appStore.setCurrentProjectId(res.lastProjectId || '');
        this.setInfo(res);
      } catch (err) {
        clearToken();
        throw err;
      }
    },
    // 登出回调
    logoutCallBack() {
      const appStore = useAppStore();
      const licenseStore = useLicenseStore();
      this.resetInfo();
      clearToken();
      removeRouteListener();
      licenseStore.removeLicenseStatus();
      appStore.clearServerMenu();
      appStore.hideLoading();
      appStore.resetSystemPackageType();
    },
    // 登出
    async logout() {
      try {
        const { t } = useI18n();
        const appStore = useAppStore();
        appStore.showLoading(t('message.logouting'));
        await userLogout();
      } finally {
        this.logoutCallBack();
      }
    },
    // 是否已经登录
    async isLogin() {
      try {
        const res = await userIsLogin();
        const appStore = useAppStore();
        setToken(res.sessionId, res.csrfToken);
        this.setInfo(res);
        const { organizationId, projectId } = getHashParameters();
        // 如果访问页面的时候携带了组织 ID和项目 ID，则不设置
        if (!organizationId) {
          appStore.setCurrentOrgId(res.lastOrganizationId || '');
        }
        if (!projectId) {
          appStore.setCurrentProjectId(res.lastProjectId || '');
        }
        return true;
      } catch (err) {
        // eslint-disable-next-line no-console
        console.log(err);
        return false;
      }
    },
  },
});

export default useUserStore;
