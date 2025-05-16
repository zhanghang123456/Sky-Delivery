package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult userPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);



    /**
     * 根据订单号查询订单详情
     * @param id
     * @return
     */
    OrderVO getOrderDetailById(Long id);

    /**
     * 再来一单
     * @param id
     */
    void repetitionOrder(Long id);

    /**
     * 管理员查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult adminpageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancelOrder(OrdersCancelDTO ordersCancelDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 派送订单
     * @param id
     */
    void deliveryOrder(Long id);

    /**
     * 完成订单
     * @param id
     */
    void completeOrder(Long id);

    /**
     * 订单统计
     */
    OrderStatisticsVO getOrderStatistics();

    /**
     * 取消订单
     * @param orderId
     */
    void cancelOrderById(Long orderId);
}
