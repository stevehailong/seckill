package com.Long.Service.Model;

import lombok.Data;


import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @program: Seckill
 * @description: 这是业务的核心模型，与数据库中的模型不是一一对应的，数据库中user_info和user_password是分离的，但是业务里面要合并成一个模型
 * @author: 寒风
 * @create: 2021-03-18 10:10
 **/
@Data
public class UserModel implements Serializable {
    private Integer id;

    @NotNull(message = "姓名不能为空")
    private String name;

    @NotNull(message = "姓名不能为空")
    private Byte gender;

    @NotNull(message = "年龄不能为空")
    @Min(value = 0,message = "年龄不能小于0")
    @Max(value = 150,message = "年龄不能大于150")
    private Integer age;

    @NotNull(message = "手机号不能为空")
    private String telphone;

    private String registerMode;

    private String thirdPartyId;

    @NotNull(message = "密码不能为空")
    private String encrptPassword;
}
