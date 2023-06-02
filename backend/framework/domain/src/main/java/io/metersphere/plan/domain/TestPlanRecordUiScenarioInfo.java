package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanRecordUiScenarioInfo implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_record_ui_scenario_info.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_record_ui_scenario_info.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "测试计划执行记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_record_ui_scenario_info.test_plan_execute_record_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_record_ui_scenario_info.test_plan_execute_record_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanExecuteRecordId;

    @Schema(title = "测试计划UI场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_record_ui_scenario_info.test_plan_ui_scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_record_ui_scenario_info.test_plan_ui_scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanUiScenarioId;

    @Schema(title = "报告ID;报告ID(预生成）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_record_ui_scenario_info.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_record_ui_scenario_info.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    private static final long serialVersionUID = 1L;
}