package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanConfigExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andTestPlanIdIsNull() {
            addCriterion("test_plan_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdIsNotNull() {
            addCriterion("test_plan_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdEqualTo(String value) {
            addCriterion("test_plan_id =", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotEqualTo(String value) {
            addCriterion("test_plan_id <>", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdGreaterThan(String value) {
            addCriterion("test_plan_id >", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_id >=", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLessThan(String value) {
            addCriterion("test_plan_id <", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_id <=", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLike(String value) {
            addCriterion("test_plan_id like", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotLike(String value) {
            addCriterion("test_plan_id not like", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdIn(List<String> values) {
            addCriterion("test_plan_id in", values, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotIn(List<String> values) {
            addCriterion("test_plan_id not in", values, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdBetween(String value1, String value2) {
            addCriterion("test_plan_id between", value1, value2, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_id not between", value1, value2, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateIsNull() {
            addCriterion("automatic_status_update is null");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateIsNotNull() {
            addCriterion("automatic_status_update is not null");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateEqualTo(Boolean value) {
            addCriterion("automatic_status_update =", value, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateNotEqualTo(Boolean value) {
            addCriterion("automatic_status_update <>", value, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateGreaterThan(Boolean value) {
            addCriterion("automatic_status_update >", value, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateGreaterThanOrEqualTo(Boolean value) {
            addCriterion("automatic_status_update >=", value, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateLessThan(Boolean value) {
            addCriterion("automatic_status_update <", value, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateLessThanOrEqualTo(Boolean value) {
            addCriterion("automatic_status_update <=", value, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateIn(List<Boolean> values) {
            addCriterion("automatic_status_update in", values, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateNotIn(List<Boolean> values) {
            addCriterion("automatic_status_update not in", values, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateBetween(Boolean value1, Boolean value2) {
            addCriterion("automatic_status_update between", value1, value2, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andAutomaticStatusUpdateNotBetween(Boolean value1, Boolean value2) {
            addCriterion("automatic_status_update not between", value1, value2, "automaticStatusUpdate");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseIsNull() {
            addCriterion("repeat_case is null");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseIsNotNull() {
            addCriterion("repeat_case is not null");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseEqualTo(Boolean value) {
            addCriterion("repeat_case =", value, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseNotEqualTo(Boolean value) {
            addCriterion("repeat_case <>", value, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseGreaterThan(Boolean value) {
            addCriterion("repeat_case >", value, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseGreaterThanOrEqualTo(Boolean value) {
            addCriterion("repeat_case >=", value, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseLessThan(Boolean value) {
            addCriterion("repeat_case <", value, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseLessThanOrEqualTo(Boolean value) {
            addCriterion("repeat_case <=", value, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseIn(List<Boolean> values) {
            addCriterion("repeat_case in", values, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseNotIn(List<Boolean> values) {
            addCriterion("repeat_case not in", values, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseBetween(Boolean value1, Boolean value2) {
            addCriterion("repeat_case between", value1, value2, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andRepeatCaseNotBetween(Boolean value1, Boolean value2) {
            addCriterion("repeat_case not between", value1, value2, "repeatCase");
            return (Criteria) this;
        }

        public Criteria andPassThresholdIsNull() {
            addCriterion("pass_threshold is null");
            return (Criteria) this;
        }

        public Criteria andPassThresholdIsNotNull() {
            addCriterion("pass_threshold is not null");
            return (Criteria) this;
        }

        public Criteria andPassThresholdEqualTo(Double value) {
            addCriterion("pass_threshold =", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdNotEqualTo(Double value) {
            addCriterion("pass_threshold <>", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdGreaterThan(Double value) {
            addCriterion("pass_threshold >", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdGreaterThanOrEqualTo(Double value) {
            addCriterion("pass_threshold >=", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdLessThan(Double value) {
            addCriterion("pass_threshold <", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdLessThanOrEqualTo(Double value) {
            addCriterion("pass_threshold <=", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdIn(List<Double> values) {
            addCriterion("pass_threshold in", values, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdNotIn(List<Double> values) {
            addCriterion("pass_threshold not in", values, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdBetween(Double value1, Double value2) {
            addCriterion("pass_threshold between", value1, value2, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdNotBetween(Double value1, Double value2) {
            addCriterion("pass_threshold not between", value1, value2, "passThreshold");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}