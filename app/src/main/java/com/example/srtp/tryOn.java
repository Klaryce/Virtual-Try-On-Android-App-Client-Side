package com.example.srtp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import org.json.JSONArray;
import org.json.JSONObject;

public class tryOn extends AppCompatActivity {

    private int clothJSONID;
    private String photo;
    private String cloth;
    private ImageView imageView;
    Bitmap bmp;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                try {
                    //imageView.setImageBitmap(bmp);
                } catch (Exception e) {

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_on);
        imageView = findViewById(R.id.iv_try_on);

        Intent intent = getIntent();
        //第二个参数表示没有接收到的时候 给的默认值
        clothJSONID = intent.getIntExtra("clothJSONID",0);
        photo = intent.getStringExtra("photo");

        new Thread(){
                public void run(){
                    try{
                        String allClothes= ConAPI.getAllClothes();
                        JSONObject obj = new JSONObject(allClothes);
                        JSONArray data_all = obj.getJSONArray("data");
                        JSONObject current = data_all.getJSONObject(clothJSONID);
                        cloth = current.getString("image");
                        String post_paths_result = ConAPI.postPaths(cloth,photo);
                        System.out.println(post_paths_result);
                        handler.sendEmptyMessage(1);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }.start();
    }
}
