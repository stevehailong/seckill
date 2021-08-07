package com.Long.service;
import com.Long.Error.BusinessException;
import com.Long.Model.UserModel;


/**
 * @program: Seckill
 * @description:
 * @author: 寒风
 * @create: 2021-03-17 21:18
 **/
public interface UserService {
    // 通过用户id获取用户对象信息
    UserModel getUserById(Integer id);

    /**
     * @Description // 根据用户信息进行注册
     * @Param [userModel]
     * @return void
     **/
    void register(UserModel userModel) throws BusinessException;

    /**
     * @Description 验证用户手机号和密码
     * @Param [telphone, encrptPassword]
     * @return com.Long.Model.UserModel
     **/
    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;

    // 通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);
}
