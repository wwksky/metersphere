package io.metersphere.api.parser.step;

import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.domain.ApiScenarioStep;
import io.metersphere.api.domain.ApiScenarioStepBlob;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.api.mapper.ApiScenarioStepBlobMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-20  15:43
 */
public abstract class StepParser {

    /**
     * 将步骤详情解析为 MsTestElement
     * @param step 步骤
     * @param resourceBlob 关联的资源详情
     * @param stepDetail 步骤详情
     * @return
     */
    public abstract AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail);

    /**
     * 将步骤解析为步骤详情
     * 场景步骤，返回 ScenarioConfig
     * 其余返回 MsTestElement
     * @param step
     * @return
     */
    public abstract Object parseDetail(ApiScenarioStep step);


    protected boolean isRef(String refType) {
        return StringUtils.equalsAny(refType, ApiScenarioStepRefType.REF.name(), ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    protected static AbstractMsTestElement parse2MsTestElement(String blobContent) {
        if (StringUtils.isBlank(blobContent)) {
            return null;
        }
        return ApiDataUtils.parseObject(blobContent, AbstractMsTestElement.class);
    }

    protected String getStepBlobString(String stepId) {
        ApiScenarioStepBlobMapper apiScenarioStepBlobMapper = CommonBeanFactory.getBean(ApiScenarioStepBlobMapper.class);
        ApiScenarioStepBlob apiScenarioStepBlob = apiScenarioStepBlobMapper.selectByPrimaryKey(stepId);
        if (apiScenarioStepBlob == null) {
            return null;
        }
        return new String(apiScenarioStepBlob.getContent());
    }
}
