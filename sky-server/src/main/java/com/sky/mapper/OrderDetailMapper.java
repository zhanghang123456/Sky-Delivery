package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 根据订单id查询订单明细
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);
}
