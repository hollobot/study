package com.hello.scene.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hello.scene.entity.BigData;
import com.hello.scene.mapper.BigDataMapper;
import org.springframework.stereotype.Service;

@Service
public class BigDataService extends ServiceImpl<BigDataMapper, BigData> {
}
