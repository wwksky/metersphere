package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UiScenarioModule implements Serializable {
    @Schema(description =  "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_module.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ui_scenario_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "父级ID")
    private String parentId;

    @Schema(description =  "模块等级")
    private Integer level;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario_module.pos.not_blank}", groups = {Created.class})
    private Double pos;

    @Schema(description =  "创建人")
    private String createUser;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        parentId("parent_id", "parentId", "VARCHAR", false),
        level("level", "level", "INTEGER", true),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        pos("pos", "pos", "DOUBLE", false),
        createUser("create_user", "createUser", "VARCHAR", false);

        private static final String BEGINNING_DELIMITER = "`";

        private static final String ENDING_DELIMITER = "`";

        private final String column;

        private final boolean isColumnNameDelimited;

        private final String javaProperty;

        private final String jdbcType;

        public String value() {
            return this.column;
        }

        public String getValue() {
            return this.column;
        }

        public String getJavaProperty() {
            return this.javaProperty;
        }

        public String getJdbcType() {
            return this.jdbcType;
        }

        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        public static Column[] excludes(Column ... excludes) {
            ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new Column[]{});
        }

        public static Column[] all() {
            return Column.values();
        }

        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }

        public String getAliasedEscapedColumnName() {
            return this.getEscapedColumnName();
        }
    }
}