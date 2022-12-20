package com.example.srtp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import com.example.srtp.AppConstant;
import com.example.srtp.Utils;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity implements OnClickListener, UploadHttpTask.OnUploadHttpListener {

    private Button btnSelect;
    private ImageView ivResult;
    private String mFileName;
    private String et_http_url = ConAPI.getServerURL() + "/upload"; // ClientThread.REQUEST_URL
    private TextView tv_file_path;
    private Button testButton;
    private Bitmap cloth_bmp;
    private int clothJSONID;
    private String photo;
    private String pic;
    private ArrayList<String> clothes_list = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        init();
        Intent intent = getIntent();
        //第二个参数表示没有接收到的时候 给的默认值
        clothJSONID = intent.getIntExtra("clothJSONID",0);
        new Thread() {
            public void run() {
                try{
                    String allClothes= ConAPI.getAllClothes();
                    JSONObject obj = new JSONObject(allClothes);
                    JSONArray data_all = obj.getJSONArray("data");
                    JSONObject current = data_all.getJSONObject(clothJSONID);
                    pic = current.getString("image");
                    byte[] cloth = ConAPI.getCloth(pic);
                    cloth_bmp = BitmapFactory.decodeByteArray(cloth, 0, cloth.length);
                }catch (Exception e){
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(1);
            }
        }.start();
    }

    private  final int REQUEST_EXTERNAL_STORAGE = 1;
    private  String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    public  void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            System.out.println("permission not granted");
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    // 初始化控件
    private void init() {
        // TODO Auto-generated method stub
        btnSelect = (Button) findViewById(R.id.btn_select);
        btnSelect.setOnClickListener(this);
        ivResult = (ImageView) findViewById(R.id.iv_result);
        tv_file_path = findViewById(R.id.tv_tips);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                try {
                    ivResult.setImageBitmap(cloth_bmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_select:
                Utils.getInstance().selectPicture(this);
                break;

            case R.id.iv_result:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (null == data) {
            return;
        }
        Uri uri = null;
        if (requestCode == AppConstant.KITKAT_LESS) {
            uri = data.getData();
            Log.d("tag", "uri=" + uri);
            // 调用裁剪方法
            Utils.getInstance().cropPicture(this, uri);
        } else if (requestCode == AppConstant.KITKAT_ABOVE) {

            uri = data.getData();
            Log.d("tag", "uri=" + uri);
            // 先将这个uri转换为path，然后再转换为uri
            String thePath = Utils.getInstance().getPath(this, uri);
            System.out.println(BuildConfig.APPLICATION_ID + ".provider");
            Uri contentUri = FileProvider.getUriForFile(this.getApplicationContext(),BuildConfig.APPLICATION_ID+ ".provider", new File(thePath));
            File newfile = new File((thePath));
            Bitmap bitmap2 = BitmapFactory.decodeFile(thePath);
            Utils.getInstance().cropPicture(this,
                    contentUri);
        } else if (requestCode == AppConstant.INTENT_CROP) {
            Bitmap bitmap = data.getParcelableExtra("data");

            File temp = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/yourAppCacheFolder/");// 自已缓存文件夹
            if (!temp.exists()) {
                temp.mkdir();
            }
            File tempFile = new File(temp.getAbsolutePath()+"/"
                    + Calendar.getInstance().getTimeInMillis() + ".jpg"); // 以时间秒为文件名
            System.out.println("tempfile_url: " + tempFile.toURI());
            // 图像保存到文件中
            FileOutputStream foutput = null;
            try {
                foutput = new FileOutputStream(tempFile);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, foutput)) {
                    Toast.makeText(MainActivity.this,
                            "已生成缓存文件，等待上传！文件位置：" + tempFile.getAbsolutePath(),
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + tempFile.getAbsolutePath()));
                    this.sendBroadcast(intent);
                    UploadHttpTask uploadTask = new UploadHttpTask();
                    // 设置文件上传监听器
                    uploadTask.setOnUploadHttpListener(this);
                    // 把文件上传线程加入到处理队列
                    uploadTask.execute(et_http_url, tempFile.getAbsolutePath());
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // 在文件上传结束后触发
    public void onUploadFinish(String result) {
        try{
            JSONObject obj = new JSONObject(result);
            photo = obj.getString("name");
            Intent intent = new Intent(MainActivity.this, tryOn.class);
            intent.putExtra("clothJSONID", clothJSONID);
            intent.putExtra("photo", photo);
            startActivityForResult(intent, 0);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}