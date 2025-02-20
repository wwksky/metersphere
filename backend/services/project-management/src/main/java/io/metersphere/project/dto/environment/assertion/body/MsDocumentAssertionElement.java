package io.metersphere.project.dto.environment.assertion.body;

import lombok.Data;

import java.util.List;


@Data
public class MsDocumentAssertionElement {
    private String id;
    /**
     * 参数名
     */
    private String paramName;
    /**
     * 必含
     */
    private Boolean include = false;
    /**
     * 类型
     */
    private String type;
    /**
     * 类型校验
     */
    private Boolean typeVerification = false;
    /**
     * 匹配条件
     */
    private String condition;
    /**
     * 匹配值
     * 即预期结果
     */
    private Object expectedResult;
    /**
     * 组内校验
     */
    private Boolean arrayVerification;
    /**
     * 子对象
     */
    private List<MsDocumentAssertionElement> children;
}
