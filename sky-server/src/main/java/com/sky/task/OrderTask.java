package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?") //每分钟执行一次
    public void processTimeOutOrder(){
        log.info("处理超时订单:{}", LocalDateTime.now());

        LocalDateTime timeout = LocalDateTime.now().plusMinutes(-15);

        //查询超时订单
        List<Orders> timeoutOrders = orderMapper.getByStatusAndTimeOutLT(Orders.UN_PAID, timeout);

        for (Orders timeoutOrder : timeoutOrders) {
            timeoutOrder.setStatus(Orders.CANCELLED);
            timeoutOrder.setCancelTime(LocalDateTime.now());
            timeoutOrder.setCancelReason("超时未支付，订单自动取消");
            orderMapper.update(timeoutOrder);
        }


    }

    /**
     * 处理派送中订单
     */
    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点执行一次
    public void processDliveryOrder(){
        log.info("处理派送中订单:{}", LocalDateTime.now());

        //查询超时订单
        List<Orders> timeoutOrders = orderMapper.getByStatus(Orders.DELIVERY_IN_PROGRESS);

        for (Orders timeoutOrder : timeoutOrders) {
            timeoutOrder.setStatus(Orders.COMPLETED);
            orderMapper.update(timeoutOrder);
        }
    }
}
