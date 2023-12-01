package io.metersphere.functional.service;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DeleteFunctionalCaseService {

    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;
    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private FunctionalCaseCommentMapper functionalCaseCommentMapper;


    public void deleteFunctionalCaseResource(List<String> ids, String projectId) {
        //TODO 删除各种关联关系？ 1.测试用例(接口/场景/ui/性能)？ 2.关联缺陷(是否需要同步？) 3.关联需求(是否需要同步？) 4.依赖关系？ 5.关联评审？ 6.关联测试计划？ 7.操作记录？ 8.评论？ 9.附件？ 10.自定义字段？ 11.用例基本信息(主表、附属表)？ 12...?
        //1.刪除用例与其他用例关联关系
        FunctionalCaseTestExample caseTestExample = new FunctionalCaseTestExample();
        caseTestExample.createCriteria().andCaseIdIn(ids);
        functionalCaseTestMapper.deleteByExample(caseTestExample);
        //8.评论
        FunctionalCaseCommentExample functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andCaseIdIn(ids);
        functionalCaseCommentMapper.deleteByExample(functionalCaseCommentExample);
        //9.附件 todo 删除关联关系
        //10.自定义字段
        FunctionalCaseCustomFieldExample fieldExample = new FunctionalCaseCustomFieldExample();
        fieldExample.createCriteria().andCaseIdIn(ids);
        functionalCaseCustomFieldMapper.deleteByExample(fieldExample);
        //11.用例基本信息
        FunctionalCaseBlobExample blobExample = new FunctionalCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        functionalCaseBlobMapper.deleteByExample(blobExample);
        FunctionalCaseExample caseExample = new FunctionalCaseExample();
        caseExample.createCriteria().andIdIn(ids).andProjectIdEqualTo(projectId);
        functionalCaseMapper.deleteByExample(caseExample);


    }
}