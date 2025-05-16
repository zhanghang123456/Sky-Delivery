package com.sky.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.AmapBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.AmapProperties;
import com.sky.properties.ShopProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.apache.http.entity.ContentType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private ShopProperties shopProperties;

    @Autowired
    private AmapProperties amapProperties;

    private static final Long DISTANCE = 5000L;

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //判断用户地址是否为空

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //判断用户地址与商户地址是否超过一定的距离
        String user_address = addressBook.getProvinceName() + addressBook.getCityName()
                + addressBook.getDistrictName() + addressBook.getDetail();
        String shop_location = getGeocode(shopProperties.getAddress());
        String user_location = getGeocode(user_address);
        if(shop_location != null && user_location != null) {
            String distanceStr = getDistance(shop_location, user_location);
            if (distanceStr == null) {
                throw new OrderBusinessException(MessageConstant.DISTANCE_EXCEEDS_LIMIT);
            }else {
                Long distance = Long.valueOf(distanceStr);
                if (distance > DISTANCE){
                    throw new OrderBusinessException(MessageConstant.DISTANCE_EXCEEDS_LIMIT);
                }
            }
        }else {
            throw new OrderBusinessException(MessageConstant.GEOCODE_ANALYSIS_ANOMALY);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        //判断购物车是否为空
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list == null || list.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //往订单表中插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setAddress(addressBook.getDetail());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetails = new ArrayList<>();
        //往订单明细表中插入n条数据
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetails);

        //清空购物车
        shoppingCartMapper.delete(userId);

        //返回封装VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        return orderSubmitVO;
    }

    private String getGeocode(String address) {
        Map<String, String> map = new HashMap<>();
        map.put("key", amapProperties.getKey());
        map.put("address", address);
        String json = HttpClientUtil.doGet(amapProperties.getGeocodeUrl(), map);
        JSONObject jsonObject = JSONObject.parseObject(json);
        if (jsonObject != null && "1".equals(jsonObject.getString("status"))) {
            JSONArray geocodes = jsonObject.getJSONArray("geocodes");
            if (geocodes != null && !geocodes.isEmpty()) {
                JSONObject firstJsonObject = geocodes.getJSONObject(0);
                if (firstJsonObject != null) {
                    return firstJsonObject.getString("location");
                }
            }
        }
        return null;
    }

    private String getDistance(String origin, String destination) {
        Map<String, String> map = new HashMap<>();
        map.put("key", amapProperties.getKey());
        map.put("origin", origin);
        map.put("destination", destination);
        String json = HttpClientUtil.doGet(amapProperties.getDirectionUrl(), map);
        JSONObject jsonObject = JSONObject.parseObject(json);
        if (jsonObject != null && "1".equals(jsonObject.getString("status"))) {
            JSONObject route = jsonObject.getJSONObject("route");
            if (route != null) {
                JSONArray paths = route.getJSONArray("paths");
                if (paths != null && !paths.isEmpty()) {
                    JSONObject firstJsonObject = paths.getJSONObject(0);
                    if( firstJsonObject != null){
                        return firstJsonObject.getString("distance");
                    }
                }
            }
        }
        return null;
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        //模拟微信支付接口返回数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));// 模拟的时间戳
        jsonObject.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));// 模拟的随机字符串
        jsonObject.put("package", "prepay_id=simulated_prepay_id_for_" + ordersPaymentDTO.getOrderNumber());
        jsonObject.put("signType", "RSA");// 模拟的签名类型
        jsonObject.put("paySign", "SIMULATED_SIGNATURE"); // 模拟的支付签名

//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        paySuccess(ordersPaymentDTO.getOrderNumber());

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }


    /**
     * 分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult userPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        Page<Orders> page = orderMapper.userPageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        if(page != null && !page.isEmpty()) {
            for (Orders orders : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailById(orders.getId());
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);

            }
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    public OrderVO getOrderDetailById(Long id) {

        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetailById = orderDetailMapper.getOrderDetailById(id);
        orderVO.setOrderDetailList(orderDetailById);
        return orderVO;
    }

    /**
     * 再来一单
     * @param id
     */
    @Transactional
    public void repetitionOrder(Long id) {


        //重新设置属性


        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailById(id);

//        List<ShoppingCart> shoppingCartList = new ArrayList<>();
//
//        for (OrderDetail orderDetail : orderDetailList) {
//            ShoppingCart shoppingCart = new ShoppingCart();
//            BeanUtils.copyProperties(orderDetail, shoppingCart);
//            shoppingCart.setUserId(BaseContext.getCurrentId());
//            shoppingCart.setCreateTime(LocalDateTime.now());
//            shoppingCartList.add(shoppingCart);
//        }

        //方式二：利用Stream API完成orderDetailList集合到shoppingCartList集合的转换
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart, "id"); //第三个参数用于指定排除项，也就是这里在复制时会排除掉id属性
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);


    }

    /**
     * 管理员查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult adminpageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.adminPageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        if(page != null && !page.isEmpty()) {
            for (Orders orders : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailById(orders.getId());

                orderVO.setOrderDetailList(orderDetailList);
//               StringBuilder sb = new StringBuilder(); //利用StringBuilder比使用加号更加高效
//               Boolean first = true;
//               if (orderDetailList != null && !orderDetailList.isEmpty()) {
//                   for (OrderDetail orderDetail : orderDetailList) {
//                       if (orderDetail != null && orderDetail.getName() != null) {
//                           if (!first){
//                               sb.append(",");
//                           }
//                           sb.append(orderDetail.getName());
//                           first = false;
//                       }
//                   }
//               }
//               orderVO.setOrderDishes(sb.toString()); //sb.toString()将StringBuilder转换为最终的String
                //方式二: 利用Stream API将订单中所有菜品名转换为字符串
                List<String> dishNames = orderDetailList.stream().map(x -> {
                    String dishName = x.getName() + "*" + x.getNumber() + ";";
                    return dishName;
                }).collect(Collectors.toList());

                String orderDishStr = String.join("", dishNames);
                orderVO.setOrderDishes(orderDishStr);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        //查询订单是否存在
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断订单状态是否为待支付或待接单
        if (orders.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //如果订单的状态为待接单则要退款

        if(orders.getStatus() == 2){
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {

        //查询订单是否存在
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null || orders.getStatus() != 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (orders.getPayStatus() == 1){
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    public void deliveryOrder(Long id) {
        //查询订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null || orders.getStatus() != 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    public void completeOrder(Long id) {
        //查询订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null || orders.getStatus() != 4) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 订单统计
     */
    public OrderStatisticsVO getOrderStatistics() {
        return orderMapper.getOrderStatistics();
    }

    /**
     * 用户取消订单
     * @param orderId
     */
    public void cancelOrderById(Long orderId) {

        //查询订单是否存在
        Orders orders = orderMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断订单状态是否为待支付或待接单
        if (orders.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //如果订单的状态为待接单则要退款

        if(orders.getStatus() == 2){
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消订单");
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }


}
