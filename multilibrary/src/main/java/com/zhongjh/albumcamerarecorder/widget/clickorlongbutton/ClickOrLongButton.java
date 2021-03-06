package com.zhongjh.albumcamerarecorder.widget.clickorlongbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Looper;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.utils.DisplayMetricsUtils;

import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_BOTH;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_ONLY_CLICK;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_ONLY_LONGCLICK;

/**
 * 点击或者长按的按钮
 */
public class ClickOrLongButton extends View {

    private static final String TAG = "ClickOrLongButton";
    private static final long TIME_TO_START_RECORD = 1000L; // 1秒后启动录制
    private float timeLimitInMils = 10000.0F;       // 录制时间
    private int mMinDuration = 1500;       // 最短录制时间限制
    private long mRecordedTime;             // 记录当前录制多长的时间秒
    private static final float PROGRESS_LIM_TO_FINISH_STARTING_ANIM = 0.1F;
    private int BOUNDING_BOX_SIZE;
    private int OUT_CIRCLE_WIDTH;
    private int OUTER_CIRCLE_WIDTH_INC;
    private float INNER_CIRCLE_RADIUS;

    private TouchTimeHandler touchTimeHandler;
    private boolean touchable;
    private boolean recordable;

    private Paint centerCirclePaint;
    private Paint outBlackCirclePaint;
    private Paint outMostBlackCirclePaint;
    private float innerCircleRadiusToDraw;
    private RectF outMostCircleRect; // 外圈的画布
    private float outBlackCircleRadius;
    private float outMostBlackCircleRadius;
    private int colorRoundBorder;
    private int colorRecord;
    private int colorWhiteP60;
    //top
    private float startAngle270;
    private float percentInDegree;
    private float centerX;
    private float centerY;
    private Paint processBarPaint;
    private Paint outMostWhiteCirclePaint;
    private Paint translucentPaint;
    private int translucentCircleRadius = 0;
    private float outMostCircleRadius;
    private float innerCircleRadiusWhenRecord;
    private long btnPressTime;
    private int outBlackCircleRadiusInc;
    private int recordState;                        // 当前状态
    private static final int RECORD_NOT_STARTED = 0; // 未启动状态
    private static final int RECORD_STARTED = 1;     // 启动状态
    private static final int RECORD_ENDED = 2;       // 结束状态

    private int mButtonState;        // 按钮可执行的功能状态（拍照,录制,两者）

    private float event_Y;  // Touch_Event_Down时候记录的Y值

    private TouchTimeHandler.Task updateUITask = new TouchTimeHandler.Task() {
        public void run() {
            long timeLapse = System.currentTimeMillis() - btnPressTime;
            mRecordedTime = (timeLapse - TIME_TO_START_RECORD);
            float percent = mRecordedTime / timeLimitInMils;
            if (!isActionDown && timeLapse >= 1) {
                if (mClickOrLongListener != null && (mButtonState == BUTTON_STATE_ONLY_CLICK || mButtonState == BUTTON_STATE_BOTH)) {
                    // 如果禁止点击也不能触发该事件
                    mClickOrLongListener.actionDown();
                    isActionDown = true;
                }
            }

            if (timeLapse >= TIME_TO_START_RECORD) {
                synchronized (ClickOrLongButton.this) {
                    if (recordState == RECORD_NOT_STARTED) {
                        recordState = RECORD_STARTED;
                        if (mClickOrLongListener != null) {
                            mClickOrLongListener.onLongClick();
                            // 如果禁止点击，那么就轮到长按触发actionDown
                            if (!isActionDown && mClickOrLongListener != null && mButtonState == BUTTON_STATE_ONLY_LONGCLICK) {
                                // 如果禁止点击也不能触发该事件
                                mClickOrLongListener.actionDown();
                                isActionDown = true;
                            }
                        }
                    }
                }
                if (!recordable) return;
                centerCirclePaint.setColor(colorRecord);
                outMostWhiteCirclePaint.setColor(colorRoundBorder);
                percentInDegree = (360.0F * percent);
                if (percent <= 1.0F) {
                    if (percent <= PROGRESS_LIM_TO_FINISH_STARTING_ANIM) {
                        float calPercent = percent / PROGRESS_LIM_TO_FINISH_STARTING_ANIM;
                        float outIncDis = outBlackCircleRadiusInc * calPercent;
                        float curOutCircleWidth = OUT_CIRCLE_WIDTH + OUTER_CIRCLE_WIDTH_INC * calPercent;
                        processBarPaint.setStrokeWidth(curOutCircleWidth);
                        outMostWhiteCirclePaint.setStrokeWidth(curOutCircleWidth);
                        outBlackCircleRadius = (outMostCircleRadius + outIncDis - curOutCircleWidth / 2.0F);
                        outMostBlackCircleRadius = (curOutCircleWidth / 2.0F + (outMostCircleRadius + outIncDis));
                        outMostCircleRect = new RectF(centerX - outMostCircleRadius - outIncDis, centerY - outMostCircleRadius - outIncDis, centerX + outMostCircleRadius + outIncDis, centerY + outMostCircleRadius + outIncDis);
                        translucentCircleRadius = (int) (outIncDis + outMostCircleRadius);
                        innerCircleRadiusToDraw = calPercent * innerCircleRadiusWhenRecord;
                    }
                    invalidate();
                } else {
                    reset();
                }
            }
        }
    };

    public ClickOrLongButton(Context paramContext) {
        super(paramContext);
        init();
    }

    public ClickOrLongButton(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    public ClickOrLongButton(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void init() {
        touchable = recordable = true;
        BOUNDING_BOX_SIZE = DisplayMetricsUtils.dip2px(100.0F); // 整块
        OUT_CIRCLE_WIDTH = DisplayMetricsUtils.dip2px(2.3F);// 外线宽度
        OUTER_CIRCLE_WIDTH_INC = DisplayMetricsUtils.dip2px(4.3F);
        INNER_CIRCLE_RADIUS = DisplayMetricsUtils.dip2px(32.0F);

        // 调取样式中的颜色
        TypedArray arrayRoundBorder = getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.click_long_button_round_border});
        int defaultRoundBorderColor = ResourcesCompat.getColor(
                getResources(), R.color.click_long_button_round_border,
                getContext().getTheme());
        TypedArray arrayInnerCircleInOperation = getContext().getTheme().obtainStyledAttributes(new int[]{ R.attr.click_long_button_inner_circle_in_operation });
        int defaultInnerCircleInOperationColor = ResourcesCompat.getColor(
                getResources(), R.color.click_long_button_inner_circle_in_operation,
                getContext().getTheme());
        TypedArray arrayInnerCircleNoOperation = getContext().getTheme().obtainStyledAttributes(new int[]{ R.attr.click_long_button_inner_circle_no_operation });
        int defaultInnerCircleNoOperationColor = ResourcesCompat.getColor(
                getResources(), R.color.click_long_button_inner_circle_no_operation,
                getContext().getTheme());

        colorRecord = arrayInnerCircleInOperation.getColor(0, defaultInnerCircleInOperationColor);
        colorRoundBorder = arrayRoundBorder.getColor(0, defaultRoundBorderColor);
        colorWhiteP60 = arrayInnerCircleNoOperation.getColor(0, defaultInnerCircleNoOperationColor);
        int colorBlackP40 = getResources().getColor(R.color.black_forty_percent);
        int colorBlackP80 = getResources().getColor(R.color.black_eighty_percent);
        int colorTranslucent = getResources().getColor(R.color.circle_shallow_translucent_bg);
        // 内圈操作中样式
        processBarPaint = new Paint();
        processBarPaint.setColor(colorRecord);
        processBarPaint.setAntiAlias(true);
        processBarPaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        processBarPaint.setStyle(Style.STROKE);
        processBarPaint.setStrokeCap(Cap.ROUND);
        // 外圈样式
        outMostWhiteCirclePaint = new Paint();
        outMostWhiteCirclePaint.setColor(colorRoundBorder);
        outMostWhiteCirclePaint.setAntiAlias(true);
        outMostWhiteCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outMostWhiteCirclePaint.setStyle(Style.STROKE);
        // 内圈未操作中样式
        centerCirclePaint = new Paint();
        centerCirclePaint.setColor(colorWhiteP60);
        centerCirclePaint.setAntiAlias(true);
        centerCirclePaint.setStyle(Style.FILL_AND_STROKE);
        outBlackCirclePaint = new Paint();
        outBlackCirclePaint.setColor(colorBlackP40);
        outBlackCirclePaint.setAntiAlias(true);
        outBlackCirclePaint.setStyle(Style.STROKE);
        outBlackCirclePaint.setStrokeWidth(1.0F);
        outMostBlackCirclePaint = new Paint();
        outMostBlackCirclePaint.setColor(colorBlackP80);
        outMostBlackCirclePaint.setAntiAlias(true);
        outMostBlackCirclePaint.setStyle(Style.STROKE);
        outMostBlackCirclePaint.setStrokeWidth(1.0F);
        translucentPaint = new Paint();
        translucentPaint.setColor(colorTranslucent);
        translucentPaint.setAntiAlias(true);
        translucentPaint.setStyle(Style.FILL_AND_STROKE);
        centerX = (BOUNDING_BOX_SIZE / 2);
        centerY = (BOUNDING_BOX_SIZE / 2);
        outMostCircleRadius = DisplayMetricsUtils.dip2px(37.0F);
        outBlackCircleRadiusInc = DisplayMetricsUtils.dip2px(7.0F);
        innerCircleRadiusWhenRecord = DisplayMetricsUtils.dip2px(35.0F);
        innerCircleRadiusToDraw = INNER_CIRCLE_RADIUS;
        outBlackCircleRadius = (outMostCircleRadius - OUT_CIRCLE_WIDTH / 2.0F);
        outMostBlackCircleRadius = (outMostCircleRadius + OUT_CIRCLE_WIDTH / 2.0F);
        startAngle270 = 270.0F;
        percentInDegree = 0.0F;
        outMostCircleRect = new RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius);
        touchTimeHandler = new TouchTimeHandler(Looper.getMainLooper(), updateUITask);
        mButtonState = BUTTON_STATE_BOTH;   // 状态为两者都可以
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, translucentCircleRadius, translucentPaint);

        //center white-p40 circle  中心点+半径32，所以直接64就是内圈的宽高度了
        canvas.drawCircle(centerX, centerY, innerCircleRadiusToDraw, centerCirclePaint);

        //static out-most white circle
        canvas.drawArc(outMostCircleRect, startAngle270, 360.0F, false, outMostWhiteCirclePaint);

        //progress bar
        canvas.drawArc(outMostCircleRect, startAngle270, percentInDegree, false, processBarPaint);

        canvas.drawCircle(centerX, centerY, outBlackCircleRadius, outBlackCirclePaint);
        canvas.drawCircle(centerX, centerY, outMostBlackCircleRadius, outMostBlackCirclePaint);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(BOUNDING_BOX_SIZE, BOUNDING_BOX_SIZE);
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (!touchable) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                event_Y = event.getY(); // 记录Y值
                Log.d(TAG, "onTouchEvent: down");
                // 禁止长按方式
                if (mClickOrLongListener != null
                        && (mButtonState == BUTTON_STATE_ONLY_LONGCLICK || mButtonState == BUTTON_STATE_BOTH)) {
                    startTicking();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: move");
                if (mClickOrLongListener != null
                        && recordState == RECORD_STARTED
                        && (mButtonState == BUTTON_STATE_ONLY_LONGCLICK || mButtonState == BUTTON_STATE_BOTH)) {
                    // 记录当前Y值与按下时候Y值的差值，调用缩放回调接口
                    mClickOrLongListener.onLongClickZoom(event_Y - event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: up");
                reset();
                break;

        }
        return true;
    }

    /**
     * 重置
     */
    private void reset() {
        Log.d(TAG, "reset: " + recordState);
        synchronized (ClickOrLongButton.this) {
            if (recordState == RECORD_STARTED) {
                if (mClickOrLongListener != null) {
                    if (mRecordedTime < mMinDuration)
                        mClickOrLongListener.onLongClickShort(mRecordedTime);//回调录制时间过短
                    else
                        mClickOrLongListener.onLongClickEnd(mRecordedTime);  //回调录制结束
                }
                recordState = RECORD_ENDED;
            } else if (recordState == RECORD_ENDED) {
                recordState = RECORD_NOT_STARTED;// 回到初始状态
            } else {
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onClick();// 拍照
            }
        }
        isActionDown = false;
        touchTimeHandler.clearMsg();
        percentInDegree = 0.0F;
        centerCirclePaint.setColor(colorWhiteP60);
        outMostWhiteCirclePaint.setColor(colorRoundBorder);
        innerCircleRadiusToDraw = INNER_CIRCLE_RADIUS;
        outMostCircleRect = new RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius);
        translucentCircleRadius = 0;
        processBarPaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outMostWhiteCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outBlackCircleRadius = (outMostCircleRadius - OUT_CIRCLE_WIDTH / 2.0F);
        outMostBlackCircleRadius = (outMostCircleRadius + OUT_CIRCLE_WIDTH / 2.0F);
        invalidate();
    }

    public boolean isTouchable() {
        return touchable;
    }

    public boolean isRecordable() {
        return recordable;
    }

    public void setRecordable(boolean recordable) {
        this.recordable = recordable;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    private void startTicking() {
        synchronized (ClickOrLongButton.this) {
            if (recordState != RECORD_NOT_STARTED)
                recordState = RECORD_NOT_STARTED;
        }
        btnPressTime = System.currentTimeMillis();
        touchTimeHandler.sendLoopMsg(0L, 16L);
    }

    private ClickOrLongListener mClickOrLongListener;       // 按钮回调接口
    private boolean isActionDown;                           // 判断是否已经调用过isActionDwon,结束后重置此值

    // region 对外方法

    /**
     * 设置最长录制时间
     *
     * @param duration 时间
     */
    public void setDuration(int duration) {
        timeLimitInMils = duration;
    }

    /**
     * 最短录制时间
     *
     * @param duration 时间
     */
    public void setMinDuration(int duration){
        mMinDuration = duration;
    }

    /**
     * 设置回调接口
     *
     * @param clickOrLongListener 回调接口
     */
    public void setRecordingListener(ClickOrLongListener clickOrLongListener) {
        this.mClickOrLongListener = clickOrLongListener;
    }

    /**
     * 设置按钮功能（点击和长按）
     *
     * @param buttonStateBoth {@link com.zhongjh.albumcamerarecorder.camera.common.Constants#BUTTON_STATE_ONLY_CLICK 只能点击
     * @link com.zhongjh.albumcamerarecorder.camera.common.Constants#BUTTON_STATE_ONLY_LONGCLICK 只能长按
     * @link com.zhongjh.albumcamerarecorder.camera.common.Constants#BUTTON_STATE_BOTH 两者皆可
     * }
     */
    public void setButtonFeatures(int buttonStateBoth) {
        this.mButtonState = buttonStateBoth;
    }

    /**
     * 重置状态
     */
    public void resetState() {
        recordState = RECORD_NOT_STARTED;// 回到初始状态
    }

    // endregion

}
