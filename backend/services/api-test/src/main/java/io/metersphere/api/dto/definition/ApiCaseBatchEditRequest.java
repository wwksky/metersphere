package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiCaseBatchEditRequest extends ApiTestCaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
            @Size(min = 1, max = 64, message = "{api_test_case.tag.length_range}")
                    String> tags;
    @Schema(description = "批量编辑的类型  用例等级: Priority,状态 :Status,标签: Tags,用例环境: Environment")
    @NotBlank
    private String type;
    @Schema(description = "默认覆盖原标签")
    private boolean appendTag = false;
    @Schema(description = "环境id")
    @Size(max = 50, message = "{api_test_case.env_id.length_range}")
    private String envId;
    @Schema(description = "用例状态")
    @Size(max = 20, message = "{api_test_case.status.length_range}")
    private String status;
    @Schema(description = "用例等级")
    @Size(max = 50, message = "{api_test_case.priority.length_range_}")
    private String priority;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }

}
