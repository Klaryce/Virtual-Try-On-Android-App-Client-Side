package com.example.srtp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;


import java.util.Map;

import com.example.srtp.FileSelectFragment;
import com.example.srtp.FileSelectFragment.FileSelectCallbacks;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("SetTextI18n")
public class FileSelectActivity extends AppCompatActivity implements
        OnClickListener, FileSelectCallbacks {
    private ImageView iv_image_select; // 声明一个图像视图对象
    private TextView tv_image_select; // 声明一个文本视图对象

    @Override
    protected void onCreate(Bundle selectdInstanceState) {
        super.onCreate(selectdInstanceState);
        setContentView(R.layout.activity_file_select);
        iv_image_select = findViewById(R.id.iv_image_select);
        tv_image_select = findViewById(R.id.tv_image_select);
        findViewById(R.id.btn_image_select).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_image_select) {
            // 声明一个图片文件的扩展名数组
            String[] imgExt = new String[]{"jpg", "png"};
            // 打开文件选择对话框
            FileSelectFragment.show(this, imgExt, null);
        }
    }

    // 点击文件选择对话框的确定按钮后触发
    public void onConfirmSelect(String absolutePath, String fileName, Map<String, Object> map_param) {
        // 拼接文件的完整路径
        String path = String.format("%s/%s", absolutePath, fileName);
        // 把要打开的图片文件显示在图像视图上面
        iv_image_select.setImageURI(Uri.parse(path));
        // 把要打开的图片文件路径显示在文本视图上面
        tv_image_select.setText("打开图片的路径为：" + path);
    }

    @SuppressLint("InlinedApi")
    public void selectPicture(Activity activity) {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            //由于startActivityForResult()的第二个参数"requestCode"为常量，
            //个人喜好把常量用一个类全部装起来
            activity.startActivityForResult(intent, AppConstant.KITKAT_LESS);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            //由于Intent.ACTION_OPEN_DOCUMENT的版本是4.4以上的内容
            //所以注意这个方法的最上面添加了@SuppressLint("InlinedApi")
            //如果客户使用的不是4.4以上的版本，因为前面有判断，所以根本不会走else，
            //也就不会出现任何因为这句代码引发的错误
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            activity.startActivityForResult(intent, AppConstant.KITKAT_ABOVE);
        }
    }

    // 检查文件是否合法时触发
    public boolean isFileValid(String absolutePath, String fileName, Map<String, Object> map_param) {
        return true;
    }

}

