package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseModule implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{functional_case_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "父节点ID")
    private String parentId;

    @Schema(description =  "节点的层级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case_module.level.not_blank}", groups = {Created.class})
    private Integer level;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "同一节点下的顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case_module.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description =  "创建人")
    private String createUser;

    private static final long serialVersionUID = 1L;
}