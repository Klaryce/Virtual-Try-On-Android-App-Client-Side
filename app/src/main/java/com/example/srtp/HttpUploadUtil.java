package com.example.srtp;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TransferQueue;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class HttpUploadUtil {
    private static final String TAG = "HttpUploadUtil";

    // 把文件上传给指定的URL
    public static String upload(String uploadUrl, String uploadFile) {
        String fileName = "";
        int pos = uploadFile.lastIndexOf("/");
        if (pos >= 0) {
            fileName = uploadFile.substring(pos + 1);
            System.out.println("fileName: " + fileName);
        }

        String end = "\r\n";
        String Hyphens = "--";
        //String boundary = "WUm4580jbtwfJhNp7zi1djFEO3wNNm";
        String boundary = "--------------------------600186768768323108267359";
        try {
            URL url = new URL(uploadUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            System.out.println("after set timeout");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            System.out.println("before dataoutputstream");
            DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
            System.out.println("after set outputstream");
            ds.writeBytes(Hyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; "
                    + "name=\"file\";filename=\"" + fileName + "\"" + end);
            ds.writeBytes("Content-Type: image/jpg" + end); //; charset=" + "UTF-8
            ds.writeBytes(end);
            FileInputStream fStream = new FileInputStream(uploadFile);
            System.out.println("after fstream");
            // 每次写入1024字节
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length;
            // 将文件数据写入到缓冲区
            while ((length = fStream.read(buffer)) != -1) {
                ds.write(buffer, 0, length);
                System.out.println(length);
            }
            System.out.println("after writing buffer");
            ds.writeBytes(end);
            ds.writeBytes(Hyphens + boundary + Hyphens + end);
            fStream.close();
            ds.flush();
//            System.in.read();
            // 获取返回内容
            InputStream is = conn.getInputStream();
            System.out.println("after get inputstream");
            int ch;
            StringBuilder b = new StringBuilder();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            ds.close();
            System.out.println(b);
            int res = conn.getResponseCode();

            if (res == 200) {
                return b.toString();
            }
            else{
                System.out.println(res);
            }
            return "FAIL" + res;
        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败:" + e.getMessage();
        }
    }
}
