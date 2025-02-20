<template>
  <div class="condition-content">
    <!-- 脚本操作 -->
    <template v-if="condition.type === 'script'">
      <a-radio-group v-model:model-value="condition.scriptType" size="small" class="mb-[16px]">
        <a-radio value="manual">{{ t('apiTestDebug.manual') }}</a-radio>
        <a-radio value="quote">{{ t('apiTestDebug.quote') }}</a-radio>
      </a-radio-group>
      <div
        v-if="condition.scriptType === 'manual'"
        class="relative rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]"
      >
        <div v-if="isShowEditScriptNameInput" class="absolute left-[12px] z-10 w-[calc(100%-24px)]">
          <a-input
            ref="scriptNameInputRef"
            v-model:model-value="condition.name"
            :placeholder="t('apiTestDebug.preconditionScriptNamePlaceholder')"
            :max-length="255"
            size="small"
            @press-enter="isShowEditScriptNameInput = false"
            @blur="isShowEditScriptNameInput = false"
          />
        </div>
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <a-tooltip :content="condition.name">
              <div class="script-name-container">
                <div class="one-line-text mr-[4px] max-w-[110px] font-medium text-[var(--color-text-1)]">
                  {{ condition.name }}
                </div>
                <MsIcon type="icon-icon_edit_outlined" class="edit-script-name-icon" @click="showEditScriptNameInput" />
              </div>
            </a-tooltip>
            <a-popover class="h-auto" position="top">
              <div class="text-[rgb(var(--primary-5))]">{{ t('apiTestDebug.scriptEx') }}</div>
              <template #content>
                <div class="mb-[8px] flex items-center justify-between">
                  <div class="text-[14px] font-medium text-[var(--color-text-1)]">
                    {{ t('apiTestDebug.scriptEx') }}
                  </div>
                  <a-button
                    type="outline"
                    class="arco-btn-outline--secondary p-[0_8px]"
                    size="mini"
                    @click="copyScriptEx"
                  >
                    {{ t('common.copy') }}
                  </a-button>
                </div>
                <div class="flex h-[412px]">
                  <MsCodeEditor
                    v-model:model-value="scriptEx"
                    class="flex-1"
                    theme="MS-text"
                    width="500px"
                    height="388px"
                    :show-full-screen="false"
                    :show-theme-change="false"
                    read-only
                  >
                  </MsCodeEditor>
                </div>
              </template>
            </a-popover>
          </div>
          <div class="flex items-center gap-[8px]">
            <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini">
              <template #icon>
                <MsIcon type="icon-icon_undo_outlined" class="text-var(--color-text-4)" size="12" />
              </template>
              {{ t('common.revoke') }}
            </a-button>
            <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="clearScript">
              <template #icon>
                <MsIcon type="icon-icon_clear" class="text-var(--color-text-4)" size="12" />
              </template>
              {{ t('common.clear') }}
            </a-button>
            <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="copyCondition">
              {{ t('common.copy') }}
            </a-button>
            <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="deleteCondition">
              {{ t('common.delete') }}
            </a-button>
          </div>
        </div>
      </div>
      <div v-else class="flex h-[calc(100%-47px)] flex-col">
        <div class="mb-[16px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
          <div class="text-[var(--color-text-2)]">
            {{ condition.quoteScript.name || '-' }}
          </div>
          <a-divider margin="8px" direction="vertical" />
          <MsButton type="text" class="font-medium">
            {{ t('apiTestDebug.quote') }}
          </MsButton>
        </div>
        <a-radio-group v-model:model-value="commonScriptShowType" size="small" type="button" class="mb-[8px] w-fit">
          <a-radio value="parameters">{{ t('apiTestDebug.parameters') }}</a-radio>
          <a-radio value="scriptContent">{{ t('apiTestDebug.scriptContent') }}</a-radio>
        </a-radio-group>
        <MsBaseTable v-show="commonScriptShowType === 'parameters'" v-bind="propsRes" v-on="propsEvent">
          <template #value="{ record }">
            <a-tooltip :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')">
              <div
                :class="[
                  record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
                  '!mr-[4px] !p-[4px]',
                ]"
              >
                <div>*</div>
              </div>
            </a-tooltip>
            {{ record.type }}
          </template>
        </MsBaseTable>
        <div v-show="commonScriptShowType === 'scriptContent'" class="h-[calc(100%-76px)]">
          <MsCodeEditor
            v-model:model-value="condition.quoteScript.script"
            theme="MS-text"
            height="100%"
            :show-full-screen="false"
            :show-theme-change="false"
            read-only
          >
          </MsCodeEditor>
        </div>
      </div>
    </template>
    <!-- SQL操作 -->
    <template v-else-if="condition.type === 'sql'">
      <div class="mb-[16px]">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('common.desc') }}</div>
        <a-input
          v-model:model-value="condition.desc"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          :max-length="255"
        />
      </div>
      <div class="mb-[16px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
        <div class="text-[var(--color-text-2)]">
          {{ condition.sqlSource.name || '-' }}
        </div>
        <a-divider margin="8px" direction="vertical" />
        <MsButton type="text" class="font-medium" @click="quoteSqlSourceDrawerVisible = true">
          {{ t('apiTestDebug.introduceSource') }}
        </MsButton>
      </div>
      <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.sqlScript') }}</div>
      <div class="mb-[16px] h-[300px]">
        <MsCodeEditor
          v-model:model-value="condition.sqlSource.script"
          theme="vs"
          height="276px"
          :language="LanguageEnum.SQL"
          :show-full-screen="false"
          :show-theme-change="false"
          read-only
        >
        </MsCodeEditor>
      </div>
      <div class="mb-[16px]">
        <div class="mb-[8px] flex items-center text-[var(--color-text-1)]">
          {{ t('apiTestDebug.storageType') }}
          <a-tooltip position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
            <template #content>
              <div>{{ t('apiTestDebug.storageTypeTip1') }}</div>
              <div>{{ t('apiTestDebug.storageTypeTip2') }}</div>
            </template>
          </a-tooltip>
        </div>
        <a-radio-group v-model:model-value="condition.sqlSource.storageType" size="small" type="button" class="w-fit">
          <a-radio value="column">{{ t('apiTestDebug.storageByCol') }}</a-radio>
          <a-radio value="result">{{ t('apiTestDebug.storageByResult') }}</a-radio>
        </a-radio-group>
      </div>
      <div v-if="condition.sqlSource.storageType === 'column'" class="mb-[16px]">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.storageByCol') }}</div>
        <a-input
          v-model:model-value="condition.sqlSource.storageByCol"
          :max-length="255"
          :placeholder="t('apiTestDebug.storageByColPlaceholder', { a: '{id_1}', b: '{username_1}' })"
        />
      </div>
      <div v-else class="mb-[16px]">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.storageByResult') }}</div>
        <a-input
          v-model:model-value="condition.sqlSource.storageByResult"
          :max-length="255"
          :placeholder="t('apiTestDebug.storageByResultPlaceholder', { a: '${result}' })"
        />
      </div>
      <div v-if="condition.sqlSource.storageType === 'column'" class="sql-table-container">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.extractParameter') }}</div>
        <paramTable
          v-model:params="condition.sqlSource.params"
          :columns="sqlSourceColumns"
          :selectable="false"
          @change="handleSqlSourceParamTableChange"
        />
      </div>
    </template>
    <!-- 等待时间 -->
    <div v-else-if="condition.type === 'waitTime'">
      <div class="mb-[8px] flex items-center">
        {{ t('apiTestDebug.waitTime') }}
        <div class="text-[var(--color-text-4)]">(ms)</div>
      </div>
      <a-input-number v-model:model-value="condition.time" mode="button" :step="100" :min="0" class="w-[160px]" />
    </div>
    <!-- 提取参数 -->
    <div v-else-if="condition.type === 'extract'">
      <paramTable
        ref="extractParamsTableRef"
        v-model:params="condition.extractParams"
        :default-param-item="defaultExtractParamItem"
        :columns="extractParamsColumns"
        :selectable="false"
        :scroll="{ x: '700px' }"
        :response="props.response"
        :height-used="(props.heightUsed || 0) + 62"
        @change="handleExtractParamTableChange"
        @more-action-select="handleExtractParamMoreActionSelect"
      >
        <template #expression="{ record }">
          <a-popover
            position="tl"
            :disabled="!record.expression || record.expression.trim() === ''"
            class="ms-params-input-popover"
          >
            <template #content>
              <div class="param-popover-title">
                {{ t('apiTestDebug.expression') }}
              </div>
              <div class="param-popover-value">
                {{ record.expression }}
              </div>
            </template>
            <a-input
              v-model:model-value="record.expression"
              class="ms-params-input"
              :max-length="255"
              @input="handleExpressionChange"
              @change="handleExpressionChange"
            >
              <template #suffix>
                <a-tooltip :disabled="!disabledExpressionSuffix">
                  <template #content>
                    <div>{{ t('apiTestDebug.expressionTip1') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip2') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip3') }}</div>
                  </template>
                  <MsIcon
                    type="icon-icon_flashlamp"
                    :size="15"
                    :class="
                      disabledExpressionSuffix ? 'ms-params-input-suffix-icon--disabled' : 'ms-params-input-suffix-icon'
                    "
                    @click.stop="() => showFastExtraction(record)"
                  />
                </a-tooltip>
              </template>
            </a-input>
          </a-popover>
        </template>
        <template #operationPre="{ record }">
          <a-popover
            v-model:popupVisible="record.moreSettingPopoverVisible"
            position="tl"
            trigger="click"
            :title="t('common.setting')"
            :content-style="{ width: '480px' }"
          >
            <template #content>
              <moreSetting v-model:config="activeRecord" is-popover class="mt-[12px]" />
              <div class="flex items-center justify-end gap-[8px]">
                <a-button type="secondary" size="mini" @click="record.moreSettingPopoverVisible = false">
                  {{ t('common.cancel') }}
                </a-button>
                <a-button type="primary" size="mini" @click="() => applyMoreSetting(record)">
                  {{ t('common.confirm') }}
                </a-button>
              </div>
            </template>
            <span class="invisible relative"></span>
          </a-popover>
        </template>
      </paramTable>
    </div>
  </div>
  <quoteSqlSourceDrawer v-model:visible="quoteSqlSourceDrawerVisible" @apply="handleQuoteSqlSourceApply" />
  <fastExtraction
    v-model:visible="fastExtractionVisible"
    :response="props.response"
    :config="activeRecord"
    @apply="handleFastExtractionApply"
  />
</template>

<script setup lang="ts">
  import { useClipboard, useVModel } from '@vueuse/core';
  import { InputInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import fastExtraction from '../fastExtraction/index.vue';
  import moreSetting from '../fastExtraction/moreSetting.vue';
  import paramTable, { type ParamTableColumn } from '../paramTable.vue';
  import quoteSqlSourceDrawer from '../quoteSqlSourceDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExpressionConfig } from '@/models/apiTest/debug';

  const props = defineProps<{
    data: Record<string, any>;
    response?: string; // 响应内容
    heightUsed?: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:data', data: Record<string, any>): void;
    (e: 'copy'): void;
    (e: 'delete', id: string): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const condition = useVModel(props, 'data', emit);
  // 是否显示脚本名称编辑框
  const isShowEditScriptNameInput = ref(false);
  const scriptNameInputRef = ref<InputInstance>();

  function showEditScriptNameInput() {
    isShowEditScriptNameInput.value = true;
    nextTick(() => {
      scriptNameInputRef.value?.focus();
    });
  }

  const scriptEx = ref(`2023-12-04 11:19:28 INFO 9026fd6a 1-1 Thread started: 9026fd6a 1-1
2023-12-04 11:19:28 ERROR 9026fd6a 1-1 Problem in JSR223 script JSR223Sampler, message: {}
In file: inline evaluation of: prev.getResponseCode() import java.net.URI; import org.apache.http.client.method . . . '' Encountered "import" at line 2, column 1.
in inline evaluation of: prev.getResponseCode() import java.net.URI; import org.apache.http.client.method . . . '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException
org.apache.http.client.method . . . '' at line number 2
`);
  const { copy, isSupported } = useClipboard();

  function copyScriptEx() {
    if (isSupported) {
      copy(scriptEx.value);
      Message.success(t('apiTestDebug.scriptExCopySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  function clearScript() {
    condition.value.script = '';
  }

  /**
   * 复制条件
   */
  function copyCondition() {
    emit('copy');
  }

  /**
   * 删除条件
   */
  function deleteCondition() {
    emit('delete', condition.value.id);
  }

  const commonScriptShowType = ref<'parameters' | 'scriptContent'>('parameters');
  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'desc',
      showTooltip: true,
    },
  ];
  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), {
    scroll: { x: '100%' },
    columns,
  });
  propsRes.value.data = [
    {
      id: new Date().getTime(),
      required: false,
      name: 'asdasd',
      type: 'string',
      value: '',
      desc: '',
    },
    {
      id: new Date().getTime(),
      required: true,
      name: '23d23d',
      type: 'string',
      value: '',
      desc: '',
    },
  ] as any;

  const sqlSourceColumns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      slotName: 'name',
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
      isNormal: true,
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];
  const quoteSqlSourceDrawerVisible = ref(false);
  function handleQuoteSqlSourceApply(sqlSource: any) {
    condition.value.sqlSource = sqlSource;
    emit('change');
  }

  function handleSqlSourceParamTableChange(resultArr: any[], isInit?: boolean) {
    condition.value.sqlSource.params = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  const extractParamsColumns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      slotName: 'name',
      width: 150,
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'type',
      slotName: 'type',
      typeOptions: [
        {
          label: t('apiTestDebug.globalParameter'),
          value: 'global',
        },
        {
          label: t('apiTestDebug.envParameter'),
          value: 'env',
        },
        {
          label: t('apiTestDebug.tempParameter'),
          value: 'temp',
        },
      ],
      width: 130,
    },
    {
      title: 'apiTestDebug.mode',
      dataIndex: 'expressionType',
      slotName: 'expressionType',
      typeOptions: [
        {
          label: t('apiTestDebug.regular'),
          value: 'regular',
        },
        {
          label: 'JSONPath',
          value: 'JSONPath',
        },
        {
          label: 'XPath',
          value: 'XPath',
        },
      ],
      width: 120,
    },
    {
      title: 'apiTestDebug.range',
      dataIndex: 'range',
      slotName: 'range',
      typeOptions: [
        {
          label: 'Body',
          value: 'body',
        },
        {
          label: 'Body (unescaped)',
          value: 'body_unescaped',
        },
        {
          label: 'Body as a Document',
          value: 'body_document',
        },
        {
          label: 'URL',
          value: 'url',
        },
        {
          label: 'Request Headers',
          value: 'request_headers',
        },
        {
          label: 'Response Headers',
          value: 'response_headers',
        },
        {
          label: 'Response Code',
          value: 'response_code',
        },
        {
          label: 'Response Message',
          value: 'response_message',
        },
      ],
      width: 190,
    },
    {
      title: 'apiTestDebug.expression',
      dataIndex: 'expression',
      slotName: 'expression',
      width: 200,
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      moreAction: [
        {
          eventTag: 'copy',
          label: 'common.copy',
        },
        {
          eventTag: 'setting',
          label: 'common.setting',
        },
      ],
      width: 80,
    },
  ];
  const disabledExpressionSuffix = ref(false);

  function handleExtractParamTableChange(resultArr: any[], isInit?: boolean) {
    condition.value.extractParams = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  const extractParamsTableRef = ref<InstanceType<typeof paramTable>>();
  const defaultExtractParamItem: Record<string, any> = {
    name: '',
    type: 'temp',
    range: 'body',
    expression: '',
    expressionType: 'regular',
    regexpMatchRule: 'expression',
    resultMatchRule: 'random',
    specifyMatchNum: 1,
    xmlMatchContentType: 'xml',
    moreSettingPopoverVisible: false,
  };
  const fastExtractionVisible = ref(false);
  const activeRecord = ref<any>({ ...defaultExtractParamItem }); // 用于暂存当前操作的提取参数表格项

  function showFastExtraction(record: Record<string, any>) {
    activeRecord.value = { ...record };
    fastExtractionVisible.value = true;
  }

  function handleExpressionChange(val: string) {
    extractParamsTableRef.value?.addTableLine(val, 'expression');
  }

  /**
   * 处理提取参数表格更多操作
   */
  function handleExtractParamMoreActionSelect(event: ActionsItem, record: Record<string, any>) {
    activeRecord.value = { ...record };
    if (event.eventTag === 'copy') {
      emit('copy');
    } else if (event.eventTag === 'setting') {
      record.moreSettingPopoverVisible = true;
    }
  }

  /**
   * 提取参数表格-应用更多设置
   */
  function applyMoreSetting(record: Record<string, any>) {
    condition.value.extractParams = condition.value.extractParams.map((e) => {
      if (e.id === activeRecord.value.id) {
        record.moreSettingPopoverVisible = false;
        return {
          ...activeRecord.value,
          moreSettingPopoverVisible: false,
        };
      }
      return e;
    });
    emit('change');
  }

  /**
   * 提取参数表格-保存快速提取的配置
   */
  function handleFastExtractionApply(config: ExpressionConfig) {
    condition.value.extractParams = condition.value.extractParams.map((e) => {
      if (e.id === activeRecord.value.id) {
        return {
          ...e,
          ...config,
        };
      }
      return e;
    });
    fastExtractionVisible.value = false;
    nextTick(() => {
      extractParamsTableRef.value?.addTableLine();
    });
    emit('change');
  }
</script>

<style lang="less" scoped>
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  .condition-content {
    @apply flex-1 overflow-y-auto;
    .ms-scroll-bar();

    padding: 16px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    .script-name-container {
      @apply flex items-center;

      margin-right: 16px;
      &:hover {
        .edit-script-name-icon {
          @apply visible;
        }
      }
      .edit-script-name-icon {
        @apply invisible cursor-pointer;

        color: rgb(var(--primary-5));
      }
    }
  }
  .param-popover-title {
    @apply font-medium;

    margin-bottom: 4px;
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--color-text-1);
  }
  .param-popover-subtitle {
    margin-bottom: 2px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-4);
  }
  .param-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
