package com.Long.dao;

import com.Long.entity.stockLog;
import org.springframework.stereotype.Repository;

@Repository
public interface stockLogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sat Apr 10 10:17:23 CST 2021
     */
    int insert(stockLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sat Apr 10 10:17:23 CST 2021
     */
    int insertSelective(stockLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sat Apr 10 10:17:23 CST 2021
     */
    stockLog selectByPrimaryKey(String stockLogId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sat Apr 10 10:17:23 CST 2021
     */
    int updateByPrimaryKeySelective(stockLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sat Apr 10 10:17:23 CST 2021
     */
    int updateByPrimaryKey(stockLog record);
}