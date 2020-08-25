package com.cyp.robot.utils;


import com.cyp.robot.api.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Decoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.UUID;

/**
 * Created by Jun on 2020/6/30 23:35.
 */
@Slf4j
public class FileUtils {

    /**
     * 文件上传到 nas盘
     *
     * @param src
     * @param dist
     */
    public static void uploadToNas(String src, String dist) {
        long beginTime = System.currentTimeMillis();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        File srcFile = new File(src);
        File distFile = new File(dist);
        File parentFile = distFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(distFile);
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();
            //方式一  效率与使用buffer缓冲流复制差不多，不推荐
//            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            while (inChannel.read(buffer) != -1) {
//                buffer.flip();
//                outChannel.write(buffer);
//                buffer.clear();
//            }

            //方式二 transferTo 方法限制文件大小不能超过 2G
//            inChannel.transferTo(0, inChannel.size(), outChannel);

            //方式三 transferFrom 与 transferTo效率相当 方法限制文件大小不能超过 8G
            outChannel.transferFrom(inChannel, 0, inChannel.size());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inChannel);
            IOUtils.closeQuietly(outChannel);
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
        System.out.println("copyByFileChannel,耗时:" + (System.currentTimeMillis() - beginTime));
    }

    /**
     * 从nas盘 下载文件
     *
     * @param request
     * @param response
     */
    public static void downloadFromNas(HttpServletRequest request, HttpServletResponse response) {
        DataInputStream in = null;
        ServletOutputStream out = null;
        try {
            String file = request.getParameter("file");
            if (!file.startsWith("http")) {
                file="C:\\JAVA\\download\\"+file;
            }
            String suffix = getFileType(file);
            if (suffix == null) {
                log.info("文件编码错误.....................");
                return;
            }
            String fileName = getFileName(request);
            response.setHeader("content-disposition", "attachment;filename=" + fileName + "." + suffix);

            if (file.startsWith("http")) {
                URL url = new URL(file);
                in = new DataInputStream(url.openStream());
            } else {
                in = new DataInputStream(new FileInputStream(new File(file)));
            }

            out = response.getOutputStream();
//            FileChannelImpl.open(fd, path, true, false, this);

            byte[] buffer = new byte[1204];
            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
    }

    /**
     * 设置文件名编码
     */
    public static String getFileName(HttpServletRequest request) throws UnsupportedEncodingException {
        String fileName = request.getParameter("fileName");

        if (StringUtils.isEmpty(request.getParameter("fileName"))) {
            fileName = UUID.randomUUID().toString().replace("-", "");
        } else {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent.toUpperCase().contains("MOZILLA") || userAgent.toUpperCase().contains("CHROME")) {
                fileName = new String(fileName.getBytes(), "ISO-8859-1");
            } else {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            }
        }
        return fileName;
    }

    /**
     * 获取文件头信息，该方法可以获取所有文件的类型
     */
    public static String getFileType(String httpUrl) {
        byte[] byteArray = null;

        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            if (httpUrl.startsWith("http")) {
                URL url = new URL(httpUrl);
                in = new DataInputStream(url.openStream());
            } else {
                in = new DataInputStream(new FileInputStream(new File(httpUrl)));
            }

            out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            byteArray = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }


        byte[] src = new byte[28];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < src.length; i++) {
            int v = byteArray[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        String type = stringBuilder.toString();
        for (Constants.FileType enums : Constants.FileType.values()) {
            if (type.startsWith(enums.getFileType())) {
                return enums.name().toLowerCase();
            }
        }
        return null;
    }

    public static void base64ToImage(String base64Code, String imgPathName) {
        BASE64Decoder base64Decoder = new BASE64Decoder();
        FileOutputStream out = null;
        try {
            byte[] bytes = base64Decoder.decodeBuffer(base64Code);
            File file = new File(imgPathName);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            out = new FileOutputStream(new File(imgPathName));
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}