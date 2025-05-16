package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 根据订单id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> userPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 管理员查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> adminPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单统计
     * @return
     */
    @Select("SELECT  count(case when status = 2 then 1 end) as to_be_confirmed, count(case when status = 3 then 1 end) as confirmed, count(case when status = 4 then 1 end) as delivery_in_progress from orders")
    OrderStatisticsVO getOrderStatistics();

    /**
     * 根据订单状态和超时时间查询订单
     * @param unPaid
     * @param timeout
     */
    @Select("select * from orders where status = #{unPaid} and order_time < #{timeout}")
    List<Orders> getByStatusAndTimeOutLT(Integer unPaid, LocalDateTime timeout);

    /**
     * 根据订单状态查询订单
     * @param deliveryInProgress
     * @return
     */
    @Select("select * from orders where status = #{deliveryInProgress}")
    List<Orders> getByStatus(Integer deliveryInProgress);
}
