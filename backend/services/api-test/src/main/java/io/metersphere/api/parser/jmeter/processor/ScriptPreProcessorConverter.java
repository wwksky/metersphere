package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.api.dto.request.processors.ScriptProcessor;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public class ScriptPreProcessorConverter extends ScriptProcessorConverter {
    @Override
    public void parse(HashTree hashTree, ScriptProcessor scriptProcessor, ParameterConfig config) {
        if (!needParse(scriptProcessor, config)) {
            return;
        }
        // todo 处理公共脚本
        TestElement processor;
        if (isJSR233(scriptProcessor)) {
            processor = new JSR223PreProcessor();
        } else {
            processor = new BeanShellPreProcessor();
        }
        parse(processor, scriptProcessor);
        hashTree.add(processor);
    }
}
