package io.metersphere.bug.dto.request;

import io.metersphere.bug.dto.BugExportColumn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugExportRequest extends BugBatchRequest {
    @Schema(description = "导出的字段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{bug.system_columns.not_empty}")
    private List<BugExportColumn> exportColumns;
}
