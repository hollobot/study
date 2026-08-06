package com.hello.springevent.mapper;

import com.hello.springevent.domain.PetInteractionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetInteractionLogMapper {

    /**
     * 插入倒霉狗交互日志。
     *
     * @param interactionLog 交互日志
     * @return 影响行数
     */
    int insertLog(PetInteractionLog interactionLog);
}
