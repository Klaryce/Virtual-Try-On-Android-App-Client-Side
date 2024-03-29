package com.example.srtp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.example.srtp.FileSelectFragment;
import com.example.srtp.FileSelectFragment.FileSelectCallbacks;
import com.example.srtp.UploadHttpTask;
import com.example.srtp.UploadHttpTask.OnUploadHttpListener;
import com.example.srtp.ClientThread;

@SuppressLint("SetTextI18n")
public class UploadHttpActivity extends AppCompatActivity implements
        OnClickListener, FileSelectCallbacks, OnUploadHttpListener {
    private EditText et_http_url;
    private TextView tv_file_path;
    private String mFileName;

    @Override
    protected void onCreate(Bundle selectdInstanceState) {
        super.onCreate(selectdInstanceState);
        setContentView(R.layout.activity_upload_http);
        et_http_url = findViewById(R.id.et_http_url);
        et_http_url.setText(ClientThread.REQUEST_URL + "/upload");
        tv_file_path = findViewById(R.id.tv_file_path);
        findViewById(R.id.btn_file_select).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_file_select) {
            // 声明一个可上传文件的扩展名数组，包括图片文件、文本文件、视频文件、音频文件
            String[] fileExt = new String[]{"jpg", "png", "txt", "3gp", "mp4", "amr", "aac", "mp3"};
            // 打开文件选择对话框
            FileSelectFragment.show(this, fileExt, null);
        }
    }

    // 点击文件选择对话框的确定按钮后触发
    public void onConfirmSelect(String absolutePath, String fileName, Map<String, Object> map_param) {
        mFileName = fileName;
        // 拼接文件的完整路径
        String path = String.format("%s/%s", absolutePath, fileName);
        tv_file_path.setText("上传文件的路径为：" + path);
        // 创建文件上传线程
        UploadHttpTask uploadTask = new UploadHttpTask();
        // 设置文件上传监听器
        uploadTask.setOnUploadHttpListener(this);
        // 把文件上传线程加入到处理队列
        uploadTask.execute(et_http_url.getText().toString(), path);
    }

    // 检查文件是否合法时触发
    public boolean isFileValid(String absolutePath, String fileName, Map<String, Object> map_param) {
        return true;
    }

    // 在文件上传结束后触发
    public void onUploadFinish(String result) {
        // 以下拼接文件上传的结果描述
        String desc = String.format("%s\n上传结果为：%s",
                tv_file_path.getText().toString(), result);
        if (result.equals("SUCC")) {
            String uploadUrl = et_http_url.getText().toString();
            desc = String.format("%s\n预计下载地址为：%s%s", desc,
                    uploadUrl.substring(0, uploadUrl.lastIndexOf("/") + 1), mFileName);
        }
        tv_file_path.setText(desc);
    }

}
