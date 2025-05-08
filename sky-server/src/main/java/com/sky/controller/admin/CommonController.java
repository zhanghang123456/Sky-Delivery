package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    //在配置好OssConfiguration之后，就可以直接注入
    @Autowired
    private AliOssUtil aliOssUtil;

    //由于文件上传后需要根据返回值来进行一个回显，而返回值为url，所以需要返回一个String类型的数据
    //MultipartFile是SpringMVC提供的一个接口，用于接收上传的文件需要与前端发送的参数名一致
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}",file);



        //上传文件，返回文件请求路径
        try {
            //获取文件的原始名称

            String originalFilename = file.getOriginalFilename();

            //截取原始文件的后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            //利用UUID构造新文件名
            String objectName = UUID.randomUUID().toString() + extension;

            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.info("文件上传失败：{}",e);
        }

    return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
