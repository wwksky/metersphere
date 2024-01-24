package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
@Data
public class ApiScenarioStepRequest extends ApiScenarioStepCommonDTO {
    @Schema(description = "步骤名称")
    @NotBlank
    private String name;

    @Schema(description = "资源编号")
    private String resourceNum;

    @Schema(description = "项目fk")
    private String projectId;

    @Schema(description = "版本号")
    private String versionId;
}
