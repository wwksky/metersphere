package io.metersphere.project.dto.environment.script.post;


import io.metersphere.project.dto.environment.script.ScriptContent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvironmentPostScript {
    @Schema(description = "接口测试")
    private ScriptContent.ApiScript apiPostScript;
    @Schema(description = "UI测试")
    private UiPostScript uiPostScript;


}