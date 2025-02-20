package io.metersphere.api.dto.request.processors.extract;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * JSONPath提取
 */
@Data
@JsonTypeName("JSON_PATH")
public class JSONPathExtract extends ResultMatchingExtract {
}
