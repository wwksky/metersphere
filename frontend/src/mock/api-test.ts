import { mock } from '@/utils/setup-mock';

import { RequestEnum } from '@/enums/httpEnum';

const getList = () => {
  return {
    data: {
      list: [
        {
          id: 'e7bd7179-d63a-43a5-1a65-218473ee69ca',
          projectId: '1a0666f0-2cb8-436b-8bfa-7be43497061c',
          name: 'ceshi',
          method: 'GET',
          modulePath: '/未规划接口',
          environmentId: '',
          schedule: null,
          status: 'Underway',
          moduleId: '0432d873-c16e-4d15-bef9-76f0cf971db6',
          userId: 'admin',
          createTime: 1680770595817,
          updateTime: 1680770604939,
          protocol: 'HTTP',
          path: '/s',
          num: 100012,
          tags: '',
          originalState: null,
          createUser: 'Administrator',
          caseTotal: '1',
          caseStatus: 'SUCCESS',
          casePassingRate: '100.00%',
          deleteTime: null,
          deleteUserId: null,
          order: null,
          refId: 'e7bd7179-d63a-43a5-1a65-218473ee69ca',
          versionId: 'cd41a097-2483-409b-83ea-a17256b032b7',
          latest: true,
          toBeUpdated: null,
          toBeUpdateTime: null,
          description: null,
          request:
            '{"type":"HTTPSamplerProxy","clazzName":"io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy","id":"e7bd7179-d63a-43a5-1a65-218473ee69ca","resourceId":null,"name":"ceshi","label":null,"referenced":null,"active":false,"index":null,"enable":true,"refType":null,"hashTree":[{"type":"Assertions","clazzName":"io.metersphere.api.dto.definition.request.assertions.MsAssertions","id":"54fe1204-0213-4aa0-01a7-552170d36190","resourceId":"5ec0cc23-9642-4dc5-aaaa-cb3f41cc0a2e","name":null,"label":null,"referenced":null,"active":false,"index":null,"enable":true,"refType":null,"hashTree":null,"projectId":null,"isMockEnvironment":false,"environmentId":null,"pluginId":null,"stepName":null,"parent":null,"scenarioAss":false,"regex":[],"jsonPath":[],"jsr223":[],"xpath2":[],"duration":{"enable":true,"type":"Duration","value":0,"valid":false},"document":{"enable":true,"type":"JSON","data":{"jsonFollowAPI":"false","xmlFollowAPI":"false","json":[],"xml":[],"assertionName":null}},"mockEnvironment":false}],"projectId":null,"isMockEnvironment":false,"environmentId":null,"pluginId":null,"stepName":null,"parent":null,"protocol":"HTTP","domain":null,"port":null,"method":"GET","path":"/s","connectTimeout":"60000","responseTimeout":"60000","headers":[{"name":"","value":"","type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"file":false,"valid":false}],"body":{"type":"KeyValue","raw":null,"format":null,"kvs":[],"binary":[],"jsonSchema":null,"tmpFilePath":null,"valid":false,"kv":false,"xml":false,"json":false},"rest":[],"url":null,"followRedirects":true,"autoRedirects":false,"doMultipartPost":false,"useEnvironment":null,"arguments":[{"name":null,"value":null,"type":"text","files":null,"description":null,"contentType":"text/plain","enable":true,"urlEncode":false,"required":false,"min":0,"max":0,"file":false,"valid":false}],"authManager":null,"isRefEnvironment":null,"alias":null,"customizeReq":false,"implementation":null,"mockEnvironment":false}',
          response:
            '{"id":null,"name":null,"enable":null,"type":"HTTP","headers":[{"name":"","value":"","type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"file":false,"valid":false}],"statusCode":[{"name":"","value":"","type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"file":false,"valid":false}],"body":{"type":"KeyValue","raw":null,"format":null,"kvs":[],"binary":[],"jsonSchema":null,"tmpFilePath":null,"valid":false,"kv":false,"xml":false,"json":false}}',
          remark: '',
          projectName: 'JIra',
          userName: 'Administrator',
          scenarioTotal: 0,
          deleteUser: null,
          scenarioIds: null,
          caseType: 'apiCase',
          apiType: null,
          versionName: 'v1.0.0',
          versionEnable: true,
          updated: false,
          fields: null,
        },
        {
          id: '937be890-79bb-1b68-e03e-7d37a8b0a607',
          projectId: '1a0666f0-2cb8-436b-8bfa-7be43497061c',
          name: 'copy_copy_copy_copy_copy_asfasdf',
          method: 'GET',
          modulePath: '/未规划接口',
          environmentId: '',
          schedule: null,
          status: 'Underway',
          moduleId: '0432d873-c16e-4d15-bef9-76f0cf971db6',
          userId: 'admin',
          createTime: 1671009286163,
          updateTime: 1672816917472,
          protocol: 'HTTP',
          path: '/safdddsafdfsdafsadfVBBBVV',
          num: 100006,
          tags: '',
          originalState: null,
          createUser: 'Administrator',
          caseTotal: '0',
          caseStatus: '-',
          casePassingRate: '-',
          deleteTime: null,
          deleteUserId: null,
          order: null,
          refId: '937be890-79bb-1b68-e03e-7d37a8b0a607',
          versionId: 'cd41a097-2483-409b-83ea-a17256b032b7',
          latest: true,
          toBeUpdated: null,
          toBeUpdateTime: null,
          description: null,
          request:
            '{"type":"HTTPSamplerProxy","clazzName":"io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy","id":"937be890-79bb-1b68-e03e-7d37a8b0a607","resourceId":null,"name":"copy_copy_copy_copy_copy_asfasdf","label":null,"referenced":null,"active":false,"index":null,"enable":true,"refType":null,"hashTree":[{"type":"Assertions","clazzName":"io.metersphere.api.dto.definition.request.assertions.MsAssertions","id":"956ee64d-ddf0-2cab-b390-896e12a84848","resourceId":"0cf5742b-b699-425b-b966-6e19a09d58c9","name":null,"label":null,"referenced":null,"active":false,"index":null,"enable":true,"refType":null,"hashTree":null,"projectId":null,"isMockEnvironment":false,"environmentId":null,"pluginId":null,"stepName":null,"parent":null,"scenarioAss":false,"regex":[],"jsonPath":[],"jsr223":[],"xpath2":[],"duration":{"enable":true,"type":"Duration","value":0,"valid":false},"document":{"enable":true,"type":"JSON","data":{"jsonFollowAPI":"false","xmlFollowAPI":"false","json":[],"xml":[],"assertionName":null}},"mockEnvironment":false}],"projectId":null,"isMockEnvironment":false,"environmentId":null,"pluginId":null,"stepName":null,"parent":null,"protocol":"HTTP","domain":null,"port":null,"method":"GET","path":"/safdddsafdfsdafsadfVBBBVV","connectTimeout":"60000","responseTimeout":"60000","headers":[{"name":"","value":"","type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"valid":false,"file":false}],"body":{"type":"KeyValue","raw":null,"format":null,"kvs":[],"binary":[],"jsonSchema":null,"tmpFilePath":null,"oldKV":true,"kv":false,"xml":false,"json":false,"valid":false},"rest":[],"url":null,"followRedirects":true,"autoRedirects":false,"doMultipartPost":false,"useEnvironment":null,"arguments":[{"name":"a","value":null,"type":"text","files":null,"description":null,"contentType":"text/plain","enable":true,"urlEncode":false,"required":false,"min":null,"max":null,"valid":true,"file":false},{"name":null,"value":null,"type":"text","files":null,"description":null,"contentType":"text/plain","enable":true,"urlEncode":false,"required":false,"min":null,"max":null,"valid":false,"file":false}],"authManager":null,"isRefEnvironment":null,"alias":null,"customizeReq":false,"implementation":null,"mockEnvironment":false}',
          response:
            '{"id":null,"name":null,"enable":null,"type":"HTTP","headers":[{"name":"","value":"","type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"valid":false,"file":false}],"statusCode":[{"name":"","value":"","type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"valid":false,"file":false}],"body":{"type":"KeyValue","raw":null,"format":null,"kvs":[],"binary":[],"jsonSchema":null,"tmpFilePath":null,"oldKV":true,"kv":false,"xml":false,"json":false,"valid":false}}',
          remark: '',
          projectName: 'JIra',
          userName: 'Administrator',
          scenarioTotal: 0,
          deleteUser: null,
          scenarioIds: null,
          caseType: 'apiCase',
          apiType: null,
          versionName: 'v1.0.0',
          versionEnable: true,
          updated: false,
          fields: null,
        },
      ],
      total: 2,
      current: 1,
    },
  };
};

mock(RequestEnum.POST, '/mock/api-test/define/list/', getList(), 200);
