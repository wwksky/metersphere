package io.metersphere.system.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationEditRequest implements Serializable {

    @Schema(description =  "组织ID")
    private String id;

    @Schema(description =  "组织名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 100, message = "{organization.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "描述")
    private String description;

    @Schema(description =  "成员ID集合")
    private List<String> userIds;
}
