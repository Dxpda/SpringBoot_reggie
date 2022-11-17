//开发时间 : 2022/10/29 12:31

package com.example.controller;

import com.example.entity.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.file}")
    private String filePath;
    /**
     * 文件上传,接收前端传来的文件数据
     * @param file
     * @return
     */
    @PostMapping("/upload")
    //当前属性名要和前端的name保持一致,否则无法获取,file是一个临时文件,需要转存到指定位置,否则本次请求完成后临时文件会删除
    public Result<String> upload(MultipartFile file){
        log.info(file.toString());
        //获取原始文件名
        String originalFileName = file.getOriginalFilename();

        //截取指定位置的字符串,subString(坐标)截取,lastIndexOf("."):返回当前字符最后一次出现的坐标
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));

        //使用UUID重新生成文件名,防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString();

        //判断当前文件夹是否存在
        File file1 = new File(filePath);

        if (!file1.exists()){
            //mkdirs:创建文件目录
            boolean mkdirs = file1.mkdirs();
            if (!mkdirs){
                log.error("文件路径有误!请更改后再试");
                Result.error("系统异常,请稍后再试~");
            }
        }

        try {
//            log.info("{}{}{}",filePath,fileName,suffix);
            //transferTo() //文件转存,把临时文件转存到另外一个位置
            file.transferTo(new File(filePath+fileName+suffix));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.success(fileName+suffix);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        FileInputStream fileInputStream = null;
        ServletOutputStream outputStream = null;
        response.setContentType("image/jpeg");
        try {
//            System.out.println(name);
            //输入流,通过输入流读取文件内容
            fileInputStream = new FileInputStream(new File(filePath+name));
            int len = 0;
            byte[] bytes = new byte[1024];
            //输出流,通过输出流将文件写回浏览器,在浏览器展示图片
            outputStream = response.getOutputStream();
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
//                flush() 是清空的意思。 一般主要用在IO中，即清空缓冲区数据，所以应该在关闭读写流之前先flush()，先清空数据。
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            //关闭资源
            try {
                assert fileInputStream != null;
                fileInputStream.close();
                assert outputStream != null;
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
