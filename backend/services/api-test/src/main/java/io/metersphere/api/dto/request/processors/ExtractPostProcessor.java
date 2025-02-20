package io.metersphere.api.dto.request.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.api.dto.request.processors.extract.MsExtract;
import lombok.Data;

import java.util.List;

/**
 * 提取处理器配置
 * @Author: jianxing
 * @CreateTime: 2023-11-22  11:08
 */
@Data
@JsonTypeName("EXTRACT")
public class ExtractPostProcessor extends MsProcessor {
    /**
     * 提取器列表
     */
    private List<MsExtract> extractors;
}
