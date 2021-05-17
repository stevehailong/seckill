package com.Long.Controller.ViewObject;

import lombok.Data;

/**
 * @program: Seckill
 * @description: 安全性考虑，前端获得的数据仅有这些
 * @author: 寒风
 * @create: 2021-03-18 10:44
 **/
@Data
public class UserVo {
    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
}
