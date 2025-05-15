package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量新增订单明细
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 根据订单id查询订单明细
     * @param order_id
     * @return
     */
    @Select("select * from order_detail where order_id = #{order_id}")
    List<OrderDetail> getOrderDetailById(Long order_id);
}
