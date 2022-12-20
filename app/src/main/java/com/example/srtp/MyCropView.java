package com.example.srtp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyCropView extends View{

    private static final float BMP_LEFT = 0f;
    private static final float BMP_TOP = 20f;

    private static final float DEFAULT_BORDER_RECT_WIDTH = 100f;
    private static final float DEFAULT_BORDER_RECT_HEIGHT = 100f;

    private static final int POS_TOP_LEFT = 0;
    private static final int POS_TOP_RIGHT = 1;
    private static final int POS_BOTTOM_LEFT = 2;
    private static final int POS_BOTTOM_RIGHT = 3;
    private static final int POS_TOP = 4;
    private static final int POS_BOTTOM = 5;
    private static final int POS_LEFT = 6;
    private static final int POS_RIGHT = 7;
    private static final int POS_CENTER = 8;

    private static final float BORDER_LINE_WIDTH = 6f;
    private static final float BORDER_CORNER_LENGTH = 30f;
    private static final float TOUCH_FIELD = 10f;

    //图片路径
    private String mBmpPath;
    //图片（原图大小）
    private Bitmap mBmpToCrop, currentBmp;
    //经过缩小的图片
    private RectF mBmpBound;
    //画笔
    private Paint mBmpPaint;
    // 裁剪区边框（伸缩框）的笔
    private Paint mBorderPaint;
    private Paint mGuidelinePaint;
    private Paint mCornerPaint;
    private Paint mBgPaint;

    private RectF mDefaultBorderBound;
    private RectF mBorderBound;

    private PointF mLastPoint = new PointF();

    private float mBorderWidth;
    private float mBorderHeight;

    private int touchPos;


    public MyCropView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public MyCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        // super.onSizeChanged(w, h, oldw, oldh);
    }

    //Canvas 画布
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        // super.onDraw(canvas);
        if (mBmpPath != null) {
            //
            canvas.drawBitmap(mBmpToCrop, null, mBmpBound, mBmpPaint);
            //将画布变成方形
            canvas.drawRect(mBorderBound.left, mBorderBound.top, mBorderBound.right, mBorderBound.bottom, mBorderPaint);
            drawGuidlines(canvas);
            drawBackground(canvas);
        }
    }

    //识别裁剪框的伸缩位置
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        // super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setLastPosition(event);
                getParent().requestDisallowInterceptTouchEvent(true);
                // onActionDown(event.getX(), event.getY());
                touchPos = detectTouchPosition(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                onActionMove(event.getX(), event.getY());
                setLastPosition(event);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

    public String getBmpPath() {
        return mBmpPath;
    }

    public void setBmpPath(String picPath) {
        this.mBmpPath = picPath;
        setBmp();
    }

    //缩小原图bitmap，将传来的大小不一致的图片缩小成规定大小
    private static Bitmap smallBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        //将原图片按照一定的大小比例缩小
        matrix.postScale(0.3f, 0.2f);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }


    //得到经过裁剪的图片
    public Bitmap getCroppedImage() {
        Bitmap b = MyCropView.smallBitmap(mBmpToCrop);
        return Bitmap.createBitmap(b, (int) mBorderBound.left, (int) mBorderBound.top, (int) mBorderWidth,
                (int) mBorderHeight);
    }


    //初始化，对各类画笔的初始化
    private void init(Context context) {

        mBmpPaint = new Paint();
        // 以下是抗锯齿
        mBmpPaint.setAntiAlias(true);// 防止边缘的锯齿
        mBmpPaint.setFilterBitmap(true);// 对位图进行滤波处理

        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setColor(Color.parseColor("#AAFFFFFF"));
        mBorderPaint.setStrokeWidth(BORDER_LINE_WIDTH);

        mGuidelinePaint = new Paint();
        mGuidelinePaint.setColor(Color.parseColor("#AAFFFFFF"));
        mGuidelinePaint.setStrokeWidth(1f);

        mCornerPaint = new Paint();

        mBgPaint = new Paint();
        mBgPaint.setColor(Color.parseColor("#B0000000"));
        mBgPaint.setAlpha(150);

    }

    //将要裁剪的图片放置到com.example.suxiaofang.view.MyCropView组件的布局里面，这里需要足够的耐心调哦
    private void setBmp() {
        //这个是原图
        mBmpToCrop = ((BitmapDrawable)getResources().getDrawable(R.drawable.snow_white)).getBitmap();
        //这个就是经过缩小的图片
        currentBmp = MyCropView.smallBitmap(mBmpToCrop);

        //接下来都是设置原图显示的大小
        mBmpBound = new RectF();
        //图片左边距离com.example.suxiaofang.view.MyCropView组件左边边的距离
        mBmpBound.left = BMP_LEFT;
        //图片的上边距离com.example.suxiaofang.view.MyCropView组件上边边的距离
        mBmpBound.top = BMP_TOP;
        //图片的右边距离com.example.suxiaofang.view.MyCropView组件左边边的距离
        mBmpBound.right = currentBmp.getWidth();
        //图片的下边距离com.example.suxiaofang.view.MyCropView组件上边边的距离
        mBmpBound.bottom = currentBmp.getHeight();

        // 使裁剪框一开始出现在图片内自设定的位置，如果裁剪框不在被裁剪的图片内，容易报错
        //其实这也是设定裁剪框的大小
        mDefaultBorderBound = new RectF();
        mDefaultBorderBound.left = (mBmpBound.left + mBmpBound.right) / 3;
        mDefaultBorderBound.top = (mBmpBound.top + mBmpBound.bottom) / 4;
        mDefaultBorderBound.right = mDefaultBorderBound.left + DEFAULT_BORDER_RECT_WIDTH;
        mDefaultBorderBound.bottom = mDefaultBorderBound.top + DEFAULT_BORDER_RECT_HEIGHT;

        mBorderBound = new RectF();
        mBorderBound.left = mDefaultBorderBound.left;
        mBorderBound.top = mDefaultBorderBound.top;
        mBorderBound.right = mDefaultBorderBound.right;
        mBorderBound.bottom = mDefaultBorderBound.bottom;

        getBorderEdgeLength();
        invalidate();
    }

    private void drawBackground(Canvas canvas) {

        /*-
          -------------------------------------
          |                top                |
          -------------------------------------
          |      |                    |       |<——————————mBmpBound
          |      |                    |       |
          | left |                    | right |
          |      |                    |       |
          |      |                  <─┼───────┼────mBorderBound
          -------------------------------------
          |              bottom               |
          -------------------------------------
         */

        // Draw "top", "bottom", "left", then "right" quadrants.
        // because the border line width is larger than 1f, in order to draw a complete border rect ,
        // i have to change zhe rect coordinate to draw
        float delta = BORDER_LINE_WIDTH / 2;
        float left = mBorderBound.left - delta;
        float top = mBorderBound.top - delta;
        float right = mBorderBound.right + delta;
        float bottom = mBorderBound.bottom + delta;

        // -------------------------------------------------------------------------------移动到上下两端会多出来阴影
        canvas.drawRect(mBmpBound.left, mBmpBound.top, mBmpBound.right, top, mBgPaint);
        canvas.drawRect(mBmpBound.left, bottom, mBmpBound.right, mBmpBound.bottom, mBgPaint);
        canvas.drawRect(mBmpBound.left, top, left, bottom, mBgPaint);
        canvas.drawRect(right, top, mBmpBound.right, bottom, mBgPaint);
    }

    // 画裁剪区域中间的参考线
    private void drawGuidlines(Canvas canvas) {
        // Draw vertical guidelines.
        final float oneThirdCropWidth = mBorderBound.width() / 3;

        final float x1 = mBorderBound.left + oneThirdCropWidth;
        canvas.drawLine(x1, mBorderBound.top, x1, mBorderBound.bottom, mGuidelinePaint);
        final float x2 = mBorderBound.right - oneThirdCropWidth;
        canvas.drawLine(x2, mBorderBound.top, x2, mBorderBound.bottom, mGuidelinePaint);

        // Draw horizontal guidelines.
        final float oneThirdCropHeight = mBorderBound.height() / 3;

        final float y1 = mBorderBound.top + oneThirdCropHeight;
        canvas.drawLine(mBorderBound.left, y1, mBorderBound.right, y1, mGuidelinePaint);
        final float y2 = mBorderBound.bottom - oneThirdCropHeight;
        canvas.drawLine(mBorderBound.left, y2, mBorderBound.right, y2, mGuidelinePaint);
    }

    private void onActionDown(float x, float y) {

    }

    //移动裁剪框时定位到的位置大小
    private void onActionMove(float x, float y) {
        Log.e("mBmpBound.right~~~~~~~", String.valueOf(mBmpBound.right));
        Log.e("mBorderBound.right~~~~", String.valueOf(mBorderBound.right));
        float deltaX = x - mLastPoint.x;
        float deltaY = y - mLastPoint.y;
        // 这里先不考虑裁剪框放最大的情况【这个就是原博主没有考虑到将裁剪框放置到最大】
        switch (touchPos) {
            case POS_CENTER:
                mBorderBound.left += deltaX;
                // fix border position
                if (mBorderBound.left < mBmpBound.left) {
                    mBorderBound.left = mBmpBound.left;
                }

                if ((mBorderBound.left > mBmpBound.right - mBorderWidth) || (mBorderBound.left == mBmpBound.right - mBorderWidth)) {
                    mBorderBound.left = mBmpBound.right - mBorderWidth;
                }


                mBorderBound.top += deltaY;
                if (mBorderBound.top < mBmpBound.top)
                    mBorderBound.top = mBmpBound.top;

                if (mBorderBound.top > mBmpBound.bottom - mBorderHeight)
                    mBorderBound.top = mBmpBound.bottom - mBorderHeight;

                mBorderBound.right = mBorderBound.left + mBorderWidth;
                mBorderBound.bottom = mBorderBound.top + mBorderHeight;

                break;

            case POS_TOP:
                resetTop(deltaY);
                break;
            case POS_BOTTOM:
                resetBottom(deltaY);
                break;
            case POS_LEFT:
                resetLeft(deltaX);
                break;
            case POS_RIGHT:
                resetRight(deltaX);
                break;
            case POS_TOP_LEFT:
                resetTop(deltaY);
                resetLeft(deltaX);
                break;
            case POS_TOP_RIGHT:
                resetTop(deltaY);
                resetRight(deltaX);
                break;
            case POS_BOTTOM_LEFT:
                resetBottom(deltaY);
                resetLeft(deltaX);
                break;
            case POS_BOTTOM_RIGHT:
                resetBottom(deltaY);
                resetRight(deltaX);
                break;
            default:

                break;
        }
        invalidate();
    }

    private void onActionUp(float x, float y) {

    }

    private int detectTouchPosition(float x, float y) {
        if (x > mBorderBound.left + TOUCH_FIELD && x < mBorderBound.right - TOUCH_FIELD
                && y > mBorderBound.top + TOUCH_FIELD && y < mBorderBound.bottom - TOUCH_FIELD)
            return POS_CENTER;

        if (x > mBorderBound.left + BORDER_CORNER_LENGTH && x < mBorderBound.right - BORDER_CORNER_LENGTH) {
            if (y > mBorderBound.top - TOUCH_FIELD && y < mBorderBound.top + TOUCH_FIELD)
                return POS_TOP;
            if (y > mBorderBound.bottom - TOUCH_FIELD && y < mBorderBound.bottom + TOUCH_FIELD)
                return POS_BOTTOM;
        }

        if (y > mBorderBound.top + BORDER_CORNER_LENGTH && y < mBorderBound.bottom - BORDER_CORNER_LENGTH) {
            if (x > mBorderBound.left - TOUCH_FIELD && x < mBorderBound.left + TOUCH_FIELD)
                return POS_LEFT;
            if (x > mBorderBound.right - TOUCH_FIELD && x < mBorderBound.right + TOUCH_FIELD)
                return POS_RIGHT;
        }

        // 前面的逻辑已经排除掉了几种情况 所以后面的 ┏ ┓ ┗ ┛ 边角就按照所占区域的方形来判断就可以了
        if (x > mBorderBound.left - TOUCH_FIELD && x < mBorderBound.left + BORDER_CORNER_LENGTH) {
            if (y > mBorderBound.top - TOUCH_FIELD && y < mBorderBound.top + BORDER_CORNER_LENGTH)
                return POS_TOP_LEFT;
            if (y > mBorderBound.bottom - BORDER_CORNER_LENGTH && y < mBorderBound.bottom + TOUCH_FIELD)
                return POS_BOTTOM_LEFT;
        }

        if (x > mBorderBound.right - BORDER_CORNER_LENGTH && x < mBorderBound.right + TOUCH_FIELD) {
            if (y > mBorderBound.top - TOUCH_FIELD && y < mBorderBound.top + BORDER_CORNER_LENGTH)
                return POS_TOP_RIGHT;
            if (y > mBorderBound.bottom - BORDER_CORNER_LENGTH && y < mBorderBound.bottom + TOUCH_FIELD)
                return POS_BOTTOM_RIGHT;
        }

        return -1;
    }

    private void setLastPosition(MotionEvent event) {
        mLastPoint.x = event.getX();
        mLastPoint.y = event.getY();
    }

    private void getBorderEdgeLength() {
        mBorderWidth = mBorderBound.width();
        mBorderHeight = mBorderBound.height();

    }
//接下来，这里的设置可以让裁剪框拉伸到最大而且不报错哦
    /**
     * 这里设置的是mBorderWidth（裁剪框的宽度），但是会出现将裁剪框左右拉到最大的时候，会出现裁剪框上的白边有些超出图片，所以会出现mBorderWidth>mBmpBound.width而报错，
     * 所以设置一下的if条件来设置当左右拉伸为最大时，裁剪框的宽度和图片宽度一致
     */

    private void getBorderEdgeWidth() {
        mBorderWidth = mBorderBound.width();
        if (mBorderWidth > mBmpBound.width()) {
            mBorderWidth = mBmpBound.width();
        }
    }
    /**
     * 这里设置的是mBorderHeight（裁剪框的高度），但是会出现将裁剪框上下拉到最大的时候，会出现裁剪框上的白边有些超出图片，所以会出现mBorderHeight>mBmpBound.height()而报错，
     * 所以设置一下的if条件来设置当上下拉伸为最大时，裁剪框的高度和图片高度一致
     */
    private void getBorderEdgeHeight() {
        mBorderHeight = mBorderBound.height();
        if (mBorderHeight > mBmpBound.height()) {
            mBorderHeight = mBmpBound.height();
        }
    }

    private void resetLeft(float delta) {
        mBorderBound.left += delta;
        getBorderEdgeWidth();
        fixBorderLeft();
    }

    private void resetTop(float delta) {
        mBorderBound.top += delta;
        getBorderEdgeHeight();
        fixBorderTop();
    }

    private void resetRight(float delta) {
        //这句话，如果注销，就不能够左右伸缩裁剪框，只能上下伸缩
        mBorderBound.right += delta;
        getBorderEdgeWidth();
        fixBorderRight();

    }

    private void resetBottom(float delta) {
        //这句话，如果注销，就不能够上下伸缩裁剪框，只能左右伸缩
        mBorderBound.bottom += delta;
        getBorderEdgeHeight();
        fixBorderBottom();
    }

    private void fixBorderLeft() {
        // fix left
        if (mBorderBound.left < mBmpBound.left)
            mBorderBound.left = mBmpBound.left;
        if (mBorderWidth < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.left = mBorderBound.right - 2 * BORDER_CORNER_LENGTH;
    }

    private void fixBorderTop() {
        // fix top
        if (mBorderBound.top < mBmpBound.top)
            mBorderBound.top = mBmpBound.top;
        if (mBorderHeight < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.top = mBorderBound.bottom - 2 * BORDER_CORNER_LENGTH;
    }

    private void fixBorderRight() {
        // fix right
        if (mBorderBound.right > mBmpBound.right) {

            mBorderBound.right = mBmpBound.right;
        }

        if (mBorderWidth < 2 * BORDER_CORNER_LENGTH) {
            mBorderBound.right = mBorderBound.left + 2 * BORDER_CORNER_LENGTH;
        }

    }

    private void fixBorderBottom() {
        // fix bottom
        if (mBorderBound.bottom > mBmpBound.bottom)
            mBorderBound.bottom = mBmpBound.bottom;
        if (mBorderHeight < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.bottom = mBorderBound.top + 2 * BORDER_CORNER_LENGTH;
    }
}