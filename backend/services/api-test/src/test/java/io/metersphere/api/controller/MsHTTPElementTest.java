package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.assertion.*;
import io.metersphere.api.dto.request.assertion.body.*;
import io.metersphere.api.dto.request.http.*;
import io.metersphere.api.dto.request.http.auth.BasicAuth;
import io.metersphere.api.dto.request.http.auth.DigestAuth;
import io.metersphere.api.dto.request.http.auth.HTTPAuth;
import io.metersphere.api.dto.request.http.auth.NoAuth;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.dto.request.processors.*;
import io.metersphere.api.dto.request.processors.extract.JSONPathExtract;
import io.metersphere.api.dto.request.processors.extract.RegexExtract;
import io.metersphere.api.dto.request.processors.extract.ResultMatchingExtract;
import io.metersphere.api.dto.request.processors.extract.XPathExtract;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.parser.TestElementParser;
import io.metersphere.api.parser.TestElementParserFactory;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.util.BeanUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  11:17
 */
public class MsHTTPElementTest {
    public MsHTTPElementTest() {
        ApiDataUtils.setResolver(MsHTTPElement.class);
    }

    @Test
    public void bodyTest() {
        MsHTTPElement msHTTPElement = getMsHttpElement();
        msHTTPElement.setBody(getGeneralBody());
        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);
    }

    public static Body getGeneralBody() {
        Body body = new Body();
        body.setBodyType(Body.BodyType.FORM_DATA.name());

        FormDataBody formDataBody = new FormDataBody();
        FormDataKV formDataKV = new FormDataKV();
        formDataKV.setEnable(false);
        formDataKV.setContentType("text/plain");
        formDataKV.setEncode(true);
        formDataKV.setMaxLength(10);
        formDataKV.setMinLength(8);
        formDataKV.setParamType("text");
        formDataKV.setDescription("test");
        formDataKV.setRequired(true);
        formDataKV.setValue("@email");
        formDataKV.setKey("key");
        FormDataKV formDataFileKV = new FormDataKV();
        ApiFile bodyFile = new ApiFile();
        bodyFile.setFileId("aaa");
        bodyFile.setFileName("aaa");
        formDataFileKV.setFiles(List.of(bodyFile));
        formDataFileKV.setKey("fileKey");
        formDataBody.setFromValues(List.of(formDataKV));
        body.setFormDataBody(formDataBody);

        WWWFormBody wwwFormBody = new WWWFormBody();
        wwwFormBody.setFromValues(List.of(formDataKV));
        body.setWwwFormBody(wwwFormBody);

        JsonBody jsonBody = new JsonBody();
        jsonBody.setJsonSchema(new JsonSchemaItem());
        jsonBody.setEnableJsonSchema(false);
        body.setJsonBody(jsonBody);

        body.setNoneBody(new NoneBody());

        RawBody rawBody = new RawBody();
        rawBody.setValue("A");
        body.setRawBody(rawBody);

        XmlBody xmlBody = new XmlBody();
        xmlBody.setValue("<a/>");
        body.setXmlBody(xmlBody);

        body.setBinaryBody(BeanUtils.copyBean(new BinaryBody(), bodyFile));
        return body;
    }

    @Test
    public void authConfigTest() {

        MsHTTPElement msHTTPElement = getMsHttpElement();

        List authConfigs = new ArrayList<>();

        authConfigs.add(new NoAuth());

        BasicAuth basicAuth = new BasicAuth();
        basicAuth.setUserName("test");
        basicAuth.setPassword("passwd");
        authConfigs.add(basicAuth);

        DigestAuth digestAuth = new DigestAuth();
        digestAuth.setUserName("test");
        digestAuth.setPassword("passwd");
        authConfigs.add(digestAuth);

        for (Object authConfig : authConfigs) {
            msHTTPElement.setAuthConfig((HTTPAuth) authConfig);
            String json = ApiDataUtils.toJSONString(msHTTPElement);
            Assertions.assertNotNull(json);
            Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);
        }
    }

    @Test
    public void processorParseTest() {
        MsHTTPElement msHTTPElement = getMsHttpElement();

        List processors = new ArrayList<>();

        ScriptProcessor scriptProcessor = new ScriptProcessor();
        scriptProcessor.setEnable(true);
        scriptProcessor.setScript("script");
        scriptProcessor.setScriptLanguage(ScriptLanguageType.JAVASCRIPT.getValue());
        processors.add(scriptProcessor);

        ScriptProcessor beanShellScriptProcessor = new ScriptProcessor();
        beanShellScriptProcessor.setEnable(true);
        beanShellScriptProcessor.setScript("script");
        beanShellScriptProcessor.setScriptLanguage(ScriptLanguageType.BEANSHELL.getValue());
        processors.add(beanShellScriptProcessor);

        SQLProcessor sqlProcessor = new SQLProcessor();
        sqlProcessor.setScript("script");
        sqlProcessor.setEnable(true);
        sqlProcessor.setDataSourceId("dataSourceId");
        KeyValueEnableParam keyValueParam = new KeyValueEnableParam();
        keyValueParam.setKey("key");
        keyValueParam.setValue("value");
        sqlProcessor.setVariables(List.of(keyValueParam));
        sqlProcessor.setResultVariable("ddd");
        sqlProcessor.setQueryTimeout(1111);
        sqlProcessor.setVariableNames("test");
        processors.add(sqlProcessor);

        TimeWaitingProcessor timeWaitingProcessor = new TimeWaitingProcessor();
        timeWaitingProcessor.setDelay(1000);
        timeWaitingProcessor.setEnable(true);
        processors.add(timeWaitingProcessor);

        List postProcessors = new ArrayList<>();

        ExtractPostProcessor extractPostProcessor = new ExtractPostProcessor();
        RegexExtract regexExtract = new RegexExtract();
        regexExtract.setVariableName("test");
        regexExtract.setExpressionMatchingRule("$1$");
        regexExtract.setExpression("test");

        RegexExtract regexExtract2 = new RegexExtract();
        regexExtract2.setVariableName("test");
        regexExtract2.setExpressionMatchingRule("$0$");
        regexExtract2.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.ALL.name());
        regexExtract2.setExtractScope("unescaped");
        regexExtract2.setExpression("test");

        JSONPathExtract jsonPathExtract = new JSONPathExtract();
        jsonPathExtract.setExpression("test");
        jsonPathExtract.setVariableName("test");
        jsonPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.RANDOM.name());

        XPathExtract xPathExtract = new XPathExtract();
        xPathExtract.setExpression("test");
        xPathExtract.setVariableName("test");
        xPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.SPECIFIC.name());
        xPathExtract.setResultMatchingRuleNum(2);

        XPathExtract xPathExtract2 = new XPathExtract();
        xPathExtract2.setExpression("test");
        xPathExtract2.setVariableName("test");
        xPathExtract2.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.SPECIFIC.name());
        xPathExtract2.setResultMatchingRuleNum(2);
        xPathExtract2.setResponseFormat(XPathExtract.ResponseFormat.HTML.name());

        extractPostProcessor.setExtractors(List.of(regexExtract, regexExtract2, jsonPathExtract, xPathExtract, xPathExtract2));
        postProcessors.addAll(processors);
        postProcessors.add(extractPostProcessor);

        MsProcessorConfig msProcessorConfig = new MsProcessorConfig();
        msProcessorConfig.setProcessors(processors);

        MsProcessorConfig msPostProcessorConfig = new MsProcessorConfig();
        msPostProcessorConfig.setProcessors(postProcessors);

        MsCommonElement msCommonElement = new MsCommonElement();
        msCommonElement.setPreProcessorConfig(msProcessorConfig);
        msCommonElement.setPostProcessorConfig(msPostProcessorConfig);

        LinkedList linkedList = new LinkedList();
        linkedList.add(msCommonElement);
        msHTTPElement.setChildren(linkedList);

        // 测试序列化
        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);

        // 测试脚本解析
        ParameterConfig parameterConfig = new ApiParamConfig();
        parameterConfig.setReportId("reportId");
        TestElementParser defaultParser = TestElementParserFactory.getDefaultParser();
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(json, AbstractMsTestElement.class);
        defaultParser.parse(msTestElement, parameterConfig);
    }

    @Test
    public void msAssertionTest() {

        MsHTTPElement msHTTPElement = getMsHttpElement();
        List<MsAssertion> assertions = getGeneralAssertions();

        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        msAssertionConfig.setEnableGlobal(false);
        msAssertionConfig.setAssertions(assertions);

        MsCommonElement msCommonElement = new MsCommonElement();
        msCommonElement.setAssertionConfig(msAssertionConfig);

        LinkedList linkedList = new LinkedList();
        linkedList.add(msCommonElement);
        msHTTPElement.setChildren(linkedList);

        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);

        // 测试脚本解析
        ParameterConfig parameterConfig = new ApiParamConfig();
        parameterConfig.setReportId("reportId");
        TestElementParser defaultParser = TestElementParserFactory.getDefaultParser();
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(json, AbstractMsTestElement.class);
        defaultParser.parse(msTestElement, parameterConfig);
    }

    public static List<MsAssertion> getGeneralAssertions() {
        List<MsAssertion> assertions = new ArrayList<>();
        MsResponseCodeAssertion responseCodeAssertion = new MsResponseCodeAssertion();
        responseCodeAssertion.setCondition(MsAssertionCondition.EMPTY.name());
        responseCodeAssertion.setExpectedValue("value");
        responseCodeAssertion.setName("name");
        assertions.add(responseCodeAssertion);

        MsResponseHeaderAssertion responseHeaderAssertion = new MsResponseHeaderAssertion();
        MsResponseHeaderAssertion.ResponseHeaderAssertionItem responseHeaderAssertionItem = new MsResponseHeaderAssertion.ResponseHeaderAssertionItem();
        responseHeaderAssertionItem.setHeader("header");
        responseHeaderAssertionItem.setExpectedValue("value");
        responseHeaderAssertionItem.setCondition(MsAssertionCondition.EMPTY.name());
        responseHeaderAssertion.setAssertions(List.of(responseHeaderAssertionItem));
        assertions.add(responseHeaderAssertion);

        MsResponseBodyAssertion regexResponseBodyAssertion = new MsResponseBodyAssertion();
        regexResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.REGEX.name());
        MsRegexAssertion regexAssertion = new MsRegexAssertion();

        MsRegexAssertionItem msRegexAssertionItem = new MsRegexAssertionItem();
        msRegexAssertionItem.setExpression("^test");
        regexAssertion.setAssertions(List.of(msRegexAssertionItem));
        regexResponseBodyAssertion.setRegexAssertion(regexAssertion);
        assertions.add(regexResponseBodyAssertion);

        MsResponseBodyAssertion documentResponseBodyAssertion = new MsResponseBodyAssertion();
        documentResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.DOCUMENT.name());
        MsDocumentAssertion msDocumentAssertion = new MsDocumentAssertion();
        documentResponseBodyAssertion.setDocumentAssertion(msDocumentAssertion);
        assertions.add(documentResponseBodyAssertion);

        MsResponseBodyAssertion jsonPathResponseBodyAssertion = new MsResponseBodyAssertion();
        jsonPathResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.JSON_PATH.name());
        MsJSONPathAssertion msJSONPathAssertion = new MsJSONPathAssertion();
        MsJSONPathAssertionItem msJSONPathAssertionItem = new MsJSONPathAssertionItem();
        msJSONPathAssertionItem.setExpression("^test");
        msJSONPathAssertionItem.setCondition(MsAssertionCondition.REGEX.name());
        msJSONPathAssertionItem.setExpectedValue("expectedValue");
        msJSONPathAssertion.setAssertions(List.of(msJSONPathAssertionItem));
        jsonPathResponseBodyAssertion.setJsonPathAssertion(msJSONPathAssertion);
        assertions.add(jsonPathResponseBodyAssertion);

        MsResponseBodyAssertion xpathPathResponseBodyAssertion = new MsResponseBodyAssertion();
        xpathPathResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.XPATH.name());
        MsXPathAssertion xPathPathAssertion = new MsXPathAssertion();
        xPathPathAssertion.setResponseFormat(MsXPathAssertion.ResponseFormat.XML.name());
        MsXPathAssertionItem xPathAssertionItem = new MsXPathAssertionItem();
        xPathAssertionItem.setExpression("^test");
        xPathAssertionItem.setExpectedValue("expectedValue");
        xPathPathAssertion.setAssertions(List.of(xPathAssertionItem));
        xpathPathResponseBodyAssertion.setXpathAssertion(xPathPathAssertion);
        assertions.add(xpathPathResponseBodyAssertion);

        MsResponseTimeAssertion responseTimeAssertion = new MsResponseTimeAssertion();
        responseTimeAssertion.setExpectedValue(1000L);
        responseTimeAssertion.setEnable(true);
        responseTimeAssertion.setName("aa");
        assertions.add(responseTimeAssertion);

        MsScriptAssertion scriptAssertion = new MsScriptAssertion();
        scriptAssertion.setScriptId("1111");
        scriptAssertion.setScript("1111");
        scriptAssertion.setName("1111");
        assertions.add(scriptAssertion);

        MsVariableAssertion msVariableAssertion = new MsVariableAssertion();
        MsVariableAssertion.VariableAssertionItem variableAssertionItem = new MsVariableAssertion.VariableAssertionItem();
        variableAssertionItem.setCondition(MsAssertionCondition.GT.name());
        variableAssertionItem.setExpectedValue("ev");
        variableAssertionItem.setVariableName("vn");
        msVariableAssertion.setVariableAssertionItems(List.of(variableAssertionItem));
        assertions.add(msVariableAssertion);
        return assertions;
    }

    public static MsHTTPElement getMsHttpElement() {
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        msHTTPElement.setUrl("http://www.test.com");
        msHTTPElement.setPath("/test");
        msHTTPElement.setMethod("GET");
        msHTTPElement.setName("name");
        msHTTPElement.setEnable(true);

        Header header = new Header();
        header.setEnable(false);
        header.setValue("value");
        header.setKey("key");
        header.setDescription("desc");
        msHTTPElement.setHeaders(List.of(header));

        RestParam restParam = new RestParam();
        restParam.setKey("key");
        restParam.setValue("value");
        restParam.setEnable(false);
        restParam.setDescription("desc");
        restParam.setRequired(true);
        msHTTPElement.setRest(List.of(restParam));

        QueryParam queryParam = new QueryParam();
        queryParam.setKey("key");
        queryParam.setValue("value");
        queryParam.setEnable(false);
        queryParam.setDescription("desc");
        queryParam.setRequired(true);
        msHTTPElement.setQuery(List.of(queryParam));

        MsHTTPConfig msHTTPConfig = new MsHTTPConfig();
        msHTTPConfig.setFollowRedirects(true);
        msHTTPConfig.setAutoRedirects(true);
        msHTTPConfig.setResponseTimeout(1000L);
        msHTTPConfig.setConnectTimeout(1000L);
        msHTTPConfig.setCertificateAlias("alias");
        msHTTPElement.setOtherConfig(msHTTPConfig);

        return msHTTPElement;
    }

    public static List<HttpResponse> getMsHttpResponse() {
        List<HttpResponse> httpResponses = new ArrayList<>();
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setName("Response1");
        httpResponse.setStatusCode("200");
        httpResponse.setDefaultFlag(true);

        Header header = new Header();
        header.setEnable(false);
        header.setValue("value");
        header.setKey("key");
        header.setDescription("desc");
        httpResponse.setHeaders(List.of(header));

        FormDataBody formDataBody = new FormDataBody();
        FormDataKV formDataKV = new FormDataKV();
        formDataKV.setEnable(false);
        formDataKV.setContentType("text/plain");
        formDataKV.setEncode(true);
        formDataKV.setMaxLength(10);
        formDataKV.setMinLength(8);
        formDataKV.setParamType("text");
        formDataKV.setDescription("test");
        formDataKV.setRequired(true);
        formDataKV.setValue("value");
        formDataKV.setKey("key");
        formDataBody.setFromValues(List.of(formDataKV));
        Body body = new Body();
        body.setBodyType(Body.BodyType.FORM_DATA.name());
        httpResponse.setBody(body);

        httpResponses.add(httpResponse);
        return httpResponses;
    }
}
