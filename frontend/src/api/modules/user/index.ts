import MSR from '@/api/http/index';
import {
  AddAPIKEYUrl,
  AddLocalConfigUrl,
  DeleteAPIKEYUrl,
  DisableAPIKEYUrl,
  DisableLocalConfigUrl,
  EnableAPIKEYUrl,
  EnableLocalConfigUrl,
  GetAPIKEYListUrl,
  GetInfoUrl,
  GetLocalConfigUrl,
  GetMenuListUrl,
  GetPlatformAccountUrl,
  GetPlatformUrl,
  GetPublicKeyUrl,
  isLoginUrl,
  LoginUrl,
  LogoutUrl,
  SavePlatformUrl,
  UpdateAPIKEYUrl,
  UpdateInfoUrl,
  UpdateLocalConfigUrl,
  UpdatePswUrl,
  ValidAPIKEYUrl,
  ValidatePlatformUrl,
  ValidLocalConfigUrl,
} from '@/api/requrls/user';

import type {
  AddLocalConfigParams,
  APIKEY,
  LocalConfig,
  LoginData,
  LoginRes,
  PersonalInfo,
  UpdateAPIKEYParams,
  UpdateBaseInfo,
  UpdateLocalConfigParams,
  UpdatePswParams,
} from '@/models/user';

import type { RouteRecordNormalized } from 'vue-router';

export function login(data: LoginData) {
  return MSR.post<LoginRes>({ url: LoginUrl, data });
}

export function isLogin() {
  return MSR.get<LoginRes>({ url: isLoginUrl }, { ignoreCancelToken: true, errorMessageMode: 'none' });
}

export function logout() {
  return MSR.get<LoginRes>({ url: LogoutUrl });
}

export function getMenuList() {
  return MSR.post<RouteRecordNormalized[]>({ url: GetMenuListUrl });
}

export function getPublicKeyRequest() {
  return MSR.get<string>({ url: GetPublicKeyUrl }, { ignoreCancelToken: true });
}

// 个人设置-更新本地执行
export function updateLocalConfig(data: UpdateLocalConfigParams) {
  return MSR.post({ url: UpdateLocalConfigUrl, data });
}

// 个人设置-添加本地执行
export function addLocalConfig(data: AddLocalConfigParams) {
  return MSR.post({ url: AddLocalConfigUrl, data });
}

// 个人设置-验证本地执行配置
export function validLocalConfig(id: string) {
  return MSR.get<boolean>({ url: ValidLocalConfigUrl, params: id });
}

// 个人设置-获取本地执行配置
export function getLocalConfig() {
  return MSR.get<LocalConfig[]>({ url: GetLocalConfigUrl });
}

// 个人设置-启用本地执行配置
export function enableLocalConfig(id: string) {
  return MSR.get({ url: EnableLocalConfigUrl, params: id });
}

// 个人设置-禁用本地执行配置
export function disableLocalConfig(id: string) {
  return MSR.get({ url: DisableLocalConfigUrl, params: id });
}

// 个人设置-修改 APIKEY
export function updateAPIKEY(data: UpdateAPIKEYParams) {
  return MSR.post({ url: UpdateAPIKEYUrl, data });
}

// 个人设置-验证 APIKEY
export function validAPIKEY() {
  return MSR.get({ url: ValidAPIKEYUrl });
}

// 个人设置-获取 APIKEY 列表
export function getAPIKEYList() {
  return MSR.get<APIKEY[]>({ url: GetAPIKEYListUrl });
}

// 个人设置-开启 APIKEY
export function enableAPIKEY(id: string) {
  return MSR.get({ url: EnableAPIKEYUrl, params: id });
}

// 个人设置-关闭 APIKEY
export function disableAPIKEY(id: string) {
  return MSR.get({ url: DisableAPIKEYUrl, params: id });
}

// 个人设置-删除 APIKEY
export function deleteAPIKEY(id: string) {
  return MSR.get({ url: DeleteAPIKEYUrl, params: id });
}

// 个人设置-生成 APIKEY
export function addAPIKEY() {
  return MSR.get({ url: AddAPIKEYUrl });
}

// 个人信息-获取基本信息
export function getBaseInfo(id: string) {
  return MSR.get<PersonalInfo>({ url: GetInfoUrl, params: id });
}

// 个人信息-修改基本信息
export function updateBaseInfo(data: UpdateBaseInfo) {
  return MSR.post({ url: UpdateInfoUrl, data });
}

// 个人信息-修改密码
export function updatePsw(data: UpdatePswParams) {
  return MSR.post({ url: UpdatePswUrl, data });
}

// 个人信息-校验第三方平台账号信息
export function validatePlatform(id: string, orgId: string, data: Record<string, any>) {
  return MSR.post({ url: `${ValidatePlatformUrl}/${id}/${orgId}`, data });
}

// 个人信息-保存第三方平台账号信息
export function savePlatform(data: Record<string, any>) {
  return MSR.post({ url: SavePlatformUrl, data });
}

// 个人信息-获取第三方平台账号信息
export function getPlatform(orgId: string) {
  return MSR.get({ url: GetPlatformUrl, params: orgId });
}

// 个人信息-获取第三方平台账号信息-插件信息
export function getPlatformAccount() {
  return MSR.get({ url: GetPlatformAccountUrl });
}
