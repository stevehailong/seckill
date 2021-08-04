package com.Long.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @program: Seckill
 * @description: 验证器类
 * @author: 寒风
 * @create: 2021-03-19 20:54
 **/
@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;

    /**
     * @Author 寒峰
     * @Description /  实现校验方法并返回校验结果
     * @Date 8:49 PM 3/19/2021
     * @Param [bean]
     * @return ValidationResult
     **/
    public ValidationResult validate(Object bean) {
        ValidationResult validationResult = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if (constraintViolationSet.size() > 0) {
            //有错误
            validationResult.setHasErrors(true);
            //遍历每个字段发生了什么错误
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                validationResult.getErrorMsgMap().put(propertyName, errMsg);
            });
        }
        return validationResult;
    }


    /**
     * @return void
     * @Author 寒峰
     * @Description // Bean初始化后，会调用这个方法
     * @Date 8:41 PM 3/19/2021
     * @Param []
     **/
    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过工厂的初始化方式使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
