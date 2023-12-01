package io.metersphere.bug.dto.request;

import io.metersphere.bug.dto.BugCustomFieldDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugBatchUpdateRequest extends BugBatchRequest{

    @Schema(description = "处理人")
    private String handleUser;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "自定义字段")
    private BugCustomFieldDTO customField;

    @Schema(description = "是否追加", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean append;
}