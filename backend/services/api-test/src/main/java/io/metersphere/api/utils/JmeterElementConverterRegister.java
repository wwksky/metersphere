package io.metersphere.api.utils;

import io.metersphere.api.parser.jmeter.MsCommentScriptElementConverter;
import io.metersphere.api.parser.jmeter.MsCommonElementConverter;
import io.metersphere.api.parser.jmeter.MsHTTPElementConverter;
import io.metersphere.api.parser.jmeter.MsScenarioConverter;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.spi.MsTestElement;
import io.metersphere.plugin.sdk.util.PluginLogUtils;
import io.metersphere.sdk.exception.MSException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-03  17:14
 */
public class JmeterElementConverterRegister {

    /**
     * 解析器集合
     * key 为 MsTestElement 实现类的 Class
     * value 为对应的转换器
     */
    private static final Map<Class<? extends MsTestElement>, AbstractJmeterElementConverter<? extends MsTestElement>> parserMap = new HashMap<>();

    static {
        // 注册默认的转换器 todo 注册插件的转换器
        register(MsHTTPElementConverter.class);
        register(MsCommonElementConverter.class);
        register(MsCommentScriptElementConverter.class);
        register(MsScenarioConverter.class);
    }

    /**
     * 注册 MsTestElement 对应的转换器
     *
     * @param elementConverterClass 转换器的类
     */
    public static void register(Class<? extends AbstractJmeterElementConverter<? extends MsTestElement>> elementConverterClass) {
        try {
            AbstractJmeterElementConverter<? extends MsTestElement> elementConverter = elementConverterClass.getDeclaredConstructor().newInstance();
            // 设置获取转换器的方法
            elementConverter.setGetConverterFunc(JmeterElementConverterRegister::getConverter);
            // 注册到解析器集合中
            parserMap.put(elementConverter.testElementClass, elementConverter);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            handleRegistrationException(elementConverterClass, e);
        }
    }

    /**
     * 获取对应组件的转换器
     *
     * @param msTestElementClass 组件的类
     * @return 转换器
     */
    public static AbstractJmeterElementConverter getConverter(Class<? extends MsTestElement> msTestElementClass) {
        return parserMap.computeIfAbsent(msTestElementClass, cls -> {
            throw new MSException("No corresponding converter found: " + cls);
        });
    }

    /**
     * 处理注册转换器时的异常
     *
     * @param elementConverterClass 转换器的类
     * @param e                     异常
     */
    private static void handleRegistrationException(Class<?> elementConverterClass, Exception e) {
        PluginLogUtils.error("注册转换器失败: " + elementConverterClass, e);
    }
}

