package com.Long.Service;


import com.Long.Error.BusinessException;
import com.Long.Service.Model.UserModel;
import org.springframework.dao.DuplicateKeyException;

/**
 * @program: Seckill
 * @description:
 * @author: 寒风
 * @create: 2021-03-17 21:18
 **/
public interface UserService {
    // 通过用户id获取用户对象信息
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException, DuplicateKeyException;
    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;
    // 通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);
}
