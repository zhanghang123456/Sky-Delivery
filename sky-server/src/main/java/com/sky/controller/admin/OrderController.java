package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "管理端订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

//    /**
//     * 提交订单
//     * @param ordersSubmitDTO
//     * @return
//     */
//    @PostMapping("/submit")
//    @ApiOperation("提交订单")
//    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
//        log.info("用户提交订单，订单信息：{}", ordersSubmitDTO);
//        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
//        return Result.success(orderSubmitVO);
//
//    }

//    /**
//     * 订单支付
//     *
//     * @param ordersPaymentDTO
//     * @return
//     */
//    @PutMapping("/payment")
//    @ApiOperation("订单支付")
//    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        log.info("订单支付：{}", ordersPaymentDTO);
//        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
//        log.info("生成预支付交易单：{}", orderPaymentVO);
//        return Result.success(orderPaymentVO);
//    }

    /**
     * 查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("查询订单")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.adminpageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("根据订单id查询订单详情")
    public Result<OrderVO> getOrderDetailById(@PathVariable Long id) {
        log.info("根据订单id查询订单详情：{}", id);
        OrderVO orderVO = orderService.getOrderDetailById(id);
        return Result.success(orderVO);
    }

//    /**
//     * 再来一单
//     * @param id
//     * @return
//     */
//    @PostMapping("/repetition/{id}")
//    @ApiOperation("再来一单")
//    public Result repetitionOrder(@PathVariable Long id) {
//        log.info("再来一单：{}", id);
//        orderService.repetitionOrder(id);
//        return Result.success();
//    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单：{}", ordersCancelDTO);
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();

    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("取消订单：{}", ordersRejectionDTO);
        orderService.rejectionOrder(ordersRejectionDTO);
        return Result.success();

    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单：{}", ordersConfirmDTO);
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();

    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id) {
        log.info("派送订单：{}", id);
        orderService.deliveryOrder(id);
        return Result.success();

    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id) {
        log.info("完成订单：{}", id);
        orderService.completeOrder(id);
        return Result.success();

    }

    /**
     * 订单统计
     */
    @GetMapping("/statistics")
    @ApiOperation("订单统计")
    public Result<OrderStatisticsVO> statistics(){
        log.info("订单统计...");
        OrderStatisticsVO orderStatistics = orderService.getOrderStatistics();
        return Result.success(orderStatistics);

    }

}
