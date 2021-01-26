package com.cyp.robot.nas;

import com.cyp.robot.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jun on 2020/10/29 22:23.
 */
@Slf4j
@RestController
@RequestMapping("/nas")
public class NasController {

    public static final String BASE = "C:\\Users\\jun\\Desktop\\nas";
    public static final String TEMP_DIR = "/temp";
    public static final String DOMAIN = "http://127.0.0.1:8080";

    /**
     * 文件限制大小不超过 10M
     *
     * @param fileName
     * @param filePath
     */
    @RequestMapping("/upload")
    private List<String> upload(@RequestParam("fileName") MultipartFile[] fileName, @RequestParam(required = false) String filePath) {
        log.info("批量上传文件数量=" + fileName.length);
        List<String> list = new ArrayList<>();
        for (MultipartFile multipartFile : fileName) {
            String upload = FileUtils.uploadMultipartFile(multipartFile, filePath);
            list.add(upload);
        }
        return list;
    }


    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) {
        FileUtils.downloadFile(request, response);
    }


    public String date2String() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String format = now.format(formatter);
        return format;
    }
}
