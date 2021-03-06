package com.Long.dao;

import com.Long.entity.Sequence;
import org.springframework.stereotype.Repository;

@Repository
public interface SequenceMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbggenerated Sun Mar 28 18:16:07 CST 2021
     */
    int insert(Sequence record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbggenerated Sun Mar 28 18:16:07 CST 2021
     */
    int insertSelective(Sequence record);

    Sequence getSequenceByName(String name);

    void updateByPrimaryKeySelective(Sequence sequence);
}