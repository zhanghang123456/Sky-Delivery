package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController") //防止和用户端的ShopController冲突
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {



    @Autowired
    private ShopService shopService;


    @PutMapping("/{status}")
    @ApiOperation("修改店铺状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("修改店铺状态:{}",status == 1 ? "营业中" : "打样中");
        //将营业状态存储到redis中
        shopService.setStatus(status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    public Result<Integer> getStatus(){
        Integer status = shopService.getStatus();
        log.info("查询店铺状态,{}",status == 1 ? "营业中" : "打样中");
        return Result.success(status);

    }
}
