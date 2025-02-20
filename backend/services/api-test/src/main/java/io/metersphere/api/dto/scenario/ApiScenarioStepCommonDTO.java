package io.metersphere.api.dto.scenario;

import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.system.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 步骤解析使用的核心数据
 * 用于步骤解析的统一处理
 *
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
@Data
public class ApiScenarioStepCommonDTO {
    @Schema(description = "步骤id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_step.id.not_blank}")
    @Size(max = 50, message = "{api_scenario_step.id.length_range}")
    private String id;

    @Schema(description = "启用/禁用")
    private Boolean enable = true;

    @Schema(description = "资源id")
    private String resourceId;

    /**
     * @see ApiScenarioStepType
     */
    @Schema(description = "步骤类型/API/CASE等")
    @NotBlank
    @EnumValue(enumClass = ApiScenarioStepType.class)
    private String stepType;

    /**
     * 引用模式：默认完全引用
     * - 完全引用：步骤状态不可调整
     * - 部分引用：步骤状态可调整
     *
     * @see ApiScenarioStepRefType
     */
    @Schema(description = "引用/复制/自定义")
    @EnumValue(enumClass = ApiScenarioStepRefType.class)
    private String refType;

    @Schema(description = "循环等组件基础数据")
    private Object config;

    @Schema(description = "csv文件id集合")
    private List<String> csvFileIds;

    @Valid
    @Schema(description = "子步骤")
    private List<? extends ApiScenarioStepCommonDTO> children;
}
