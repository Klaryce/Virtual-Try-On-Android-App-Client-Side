package com.example.srtp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import static android.app.AlertDialog.THEME_HOLO_LIGHT;

public class clothes extends AppCompatActivity implements View.OnClickListener {

    private ScrollView scrollView;
    private Button toTopBtn;
    private View contentView;
    private int scrollY = 0;// 标记上次滑动位置
    private final String TAG = "kk123456789";
    EditText search_content;
    ArrayList<String> clothes_list = new ArrayList<String>();

    ArrayList<String> names = new ArrayList<String>();
    ArrayList<Bitmap> bmps = new ArrayList<Bitmap>();
    int len = -1;
    String search_info = "";
    Bitmap bmp;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what == 1){
                try{
                    int pxWid = getScreenWidth(clothes.this);
                    int marginWid = dip2px(clothes.this, 10);
                    addMovies(len, (pxWid - marginWid)/ 2);
                }catch (Exception e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(clothes.this);
                    builder.setTitle("Error!");
                    String s0 = e.toString();
                    builder.setMessage("Fail to make layout. " + s0);
                    builder.setPositiveButton("Ok.", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }
    };


    // 获取屏幕宽度
    public static int getScreenWidth(Context ctx){
        WindowManager wm = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int dip2px(Context context, float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5f);
    }

    /**
     * 初始化视图
     */
    private void initView() {
        scrollView = (ScrollView) findViewById(R.id.scroll_all);
        if (contentView == null) {
            contentView = scrollView.getChildAt(0);
        }

        toTopBtn = (Button) findViewById(R.id.top_btn);
        toTopBtn.setOnClickListener(this);
        /******************** 监听ScrollView滑动停止 *****************************/
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            private int lastY = 0;
            private int touchEventId = -9983761;
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    View scroller = (View) msg.obj;
                    if (msg.what == touchEventId) {
                        if (lastY == scroller.getScrollY()) {
                            handleStop(scroller);
                        } else {
                            handler.sendMessageDelayed(handler.obtainMessage(
                                    touchEventId, scroller), 5);
                            lastY = scroller.getScrollY();
                        }
                    }
                }
            };

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    handler.sendMessageDelayed(
                            handler.obtainMessage(touchEventId, v), 5);
                }
                return false;
            }

            /**
             * ScrollView 停止
             *
             * @param view
             */
            private void handleStop(Object view) {

                Log.i(TAG, "handleStop");
                ScrollView scroller = (ScrollView) view;
                scrollY = scroller.getScrollY();

                doOnBorderListener();
            }
        });
    }
    private void doOnBorderListener() {
        // 底部判断
        if (contentView != null
                && contentView.getMeasuredHeight() <= scrollView.getScrollY()
                + scrollView.getHeight()) {
            toTopBtn.setVisibility(View.VISIBLE);
            Log.i(TAG, "bottom");
        }
        // 顶部判断
        else if (scrollView.getScrollY() <= 30) {
            toTopBtn.setVisibility(View.GONE);
            Log.i(TAG, "top");
        } else  {
            toTopBtn.setVisibility(View.VISIBLE);
            Log.i(TAG, "test");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothes);
        initView();

        new Thread(){

            public void run(){

                try{
                    System.out.println("enter");
                    clothes_list.clear();
                    String detail_all = ConAPI.getAllClothes();
                    System.out.println("all detail is: " + detail_all);
                    if (!TextUtils.isEmpty(detail_all)) {

                        JSONObject obj = new JSONObject(detail_all);
                        JSONArray data = obj.getJSONArray("data");
                        len = data.length();
                        bmps.clear();
                        System.out.println(len);
                        for(int i = 0; i < len; i++){
                            JSONObject data_obj = data.getJSONObject(i);
                            //String name = data_obj.getString("name");
                            String pic = data_obj.getString("image");
                            System.out.println("image:" + pic);
                            byte[] image = ConAPI.getCloth(pic);
                            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                            //names.add(name);
                            bmps.add(bmp);
                        }
                    }
                }catch (Exception e){
                    System.out.println("error all movie");
                    e.printStackTrace();
                }


                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }.start();
    }

    class WelOnClickListener implements ImageButton.OnClickListener{

        @SuppressLint("ResourceType")
        @Override
        public void onClick(View v) { // 点击事件的处理方法
            Intent intent =new Intent(clothes.this,MainActivity.class);
            System.out.println(v.getId());
            intent.putExtra("clothJSONID",v.getId());
            startActivity(intent);
        }
    }


    private void addMovies(int len, int wid){

        LinearLayout rootLayout = (LinearLayout)findViewById(R.id.root_layout);
        int hei = wid / 3 * 4;
        String text = "movie1";

        int posterNum = 0;
        for(int i = 0; i < len-1; i = i + 2){
            LinearLayout horizontal = new LinearLayout(this);
            LinearLayout.LayoutParams horizontal_parent_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            for(int j = 0; j < 2; j++) {

                // 图片
                ImageButton image = new ImageButton(this);
                image.setBackgroundColor(Color.parseColor("#00ffffff"));

                Bitmap bitmap = bmps.get(i+j);
                image.setVisibility(View.VISIBLE);
                image.setImageBitmap(bitmap);
                image.setId(posterNum);
                System.out.println(posterNum);
                System.out.println(image.getId());
                image.setOnClickListener(new WelOnClickListener());
                posterNum += 1;

                //图片参数设置
                LinearLayout.LayoutParams image_parent_params
                        = new LinearLayout.LayoutParams(wid, hei);

                // 图片适应布局尺寸
                image.setScaleType(ImageButton.ScaleType.FIT_CENTER);

                //布局加入水平布局
                horizontal.addView(image, image_parent_params);
            }
            //水平布局加入总布局
            rootLayout.addView(horizontal, horizontal_parent_params);
        }

        if(len % 2 == 1){

            // image
            ImageButton image = new ImageButton(this);
            image.setBackgroundColor(Color.WHITE);

            Bitmap bitmap = bmps.get(len-1);
            image.setVisibility(View.VISIBLE);
            image.setImageBitmap(bitmap);
            image.setId(posterNum);
            image.setOnClickListener(new WelOnClickListener());
            posterNum += 1;

            LinearLayout.LayoutParams image_parent_params
                    = new LinearLayout.LayoutParams(wid, hei);

            // adjust image
            image.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            image.setBackgroundColor(Color.parseColor("#00ffffff"));


            // add to rootLayout
            rootLayout.addView(image, image_parent_params);
        }
    }

    @Override
    public void onClick(View v) { // 点击事件的处理方法

        if (v.getId() == R.id.top_btn) {

            scrollView.post(new Runnable() {
                @Override
                public void run() {
//                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);滚动到底部
//                        scrollView.fullScroll(ScrollView.FOCUS_UP);滚动到顶部
//
//                        需要注意的是，该方法不能直接被调用
//                        因为Android很多函数都是基于消息队列来同步，所以需要一部操作，
//                        addView完之后，不等于马上就会显示，而是在队列中等待处理，虽然很快，但是如果立即调用fullScroll， view可能还没有显示出来，所以会失败
//                                应该通过handler在新线程中更新
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                }
            });
            toTopBtn.setVisibility(View.GONE);
        }
    }
}
