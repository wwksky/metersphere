package io.metersphere.system.config.interceptor;

import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.system.utils.MybatisInterceptorConfig;
import io.metersphere.ui.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class UiInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> uiCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(UiCustomCommandBlob.class, "scenarioDefinition", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(UiCustomCommandBlob.class, "commandViewStruct", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(UiScenarioBlob.class, "scenarioDefinition", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(UiScenarioBlob.class, "environmentJson", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(UiScenarioReportDetail.class, "content", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(UiScenarioReportDetail.class, "baseInfo", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(UiScenarioReportLog.class, "console", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(UiScenarioReportStructure.class, "resourceTree", CompressUtils.class, "zip", "unzip"));

        return configList;
    }
}
