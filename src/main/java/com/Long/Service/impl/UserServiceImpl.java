package com.Long.Service.impl;


import com.Long.Dao.UserPasswordMapper;
import com.Long.Error.BusinessException;
import com.Long.Dao.UserMapper;
import com.Long.Error.EmBusinessError;
import com.Long.Service.Model.UserModel;
import com.Long.Service.UserService;
import com.Long.entity.User;
import com.Long.entity.UserPassword;
import com.Long.validator.ValidationResult;
import com.Long.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;


/**
 * @program: Seckill
 * @description:
 * @author: 寒风
 * @create: 2021-03-17 20:54
 **/
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserPasswordMapper userPasswordMapper;
    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override //根据获取用户信息
    public UserModel getUserById(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) return null;
        // 通过用户Id获取用户加密信息
        UserPassword userPassword = userPasswordMapper.selectByUserId(user.getId());
        return convertUser(user, userPassword);
    }

    private UserModel convertUser(User user, UserPassword userPassword) {
        if (user == null) return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(user, userModel);
        if (userPassword != null) {
            //密码不能使用copyProperties，因为id属性重复
            userModel.setEncrptPassword(userPassword.getEncrptPassword());
        }
        return userModel;
    }

    @Transactional
    @Override
    public void register(UserModel userModel) throws BusinessException, DuplicateKeyException {
        if (userModel == null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors())
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        User user = convertFromUserModel(userModel);
        //使用insertSelective方法时，手机设置了唯一索引，若重复则报出异常
        try {
            userMapper.insertSelective(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"该手机号已经注册，请更换！");
        }
        userModel.setId(user.getId());
        UserPassword userPassword = convertPasswordFromModel(userModel);
        System.out.println(userPasswordMapper.insertSelective(userPassword));
    }

    //将userModel转换为UserPassword实体
    private UserPassword convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) return null;
        UserPassword userPassword = new UserPassword();
        userPassword.setEncrptPassword(userModel.getEncrptPassword());
        userPassword.setUserId(userModel.getId());
        return userPassword;
    }

    //将userModel转换为User实体
    private User convertFromUserModel(UserModel userModel) {
        if (userModel == null) return null;
        User user = new User();
        BeanUtils.copyProperties(userModel, user);
        return user;
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过用户手机号获取用户信息
        User user = userMapper.selectByTelphone(telphone);
        if (user == null)
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        UserPassword userPassword = userPasswordMapper.selectByUserId(user.getId());
        UserModel userModel = convertFromEntity(user, userPassword);
        //将数据库中的密码与输入的密码进行比对
        if (!StringUtils.equals(encrptPassword, userModel.getEncrptPassword()))
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        return userModel;
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_"+id);
        // 如果Redis中不存在就从数据库中取，并存入缓存中
        if (userModel == null){
            userModel = this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_"+id,userModel);
            redisTemplate.expire("user_validate_"+id,10, TimeUnit.MINUTES);
        }
        return userModel;
    }

    private UserModel convertFromEntity(User user, UserPassword userPassword) {
        if (user == null) return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(user, userModel);
        if (userPassword != null) userModel.setEncrptPassword(userPassword.getEncrptPassword());
        return userModel;
    }
}

