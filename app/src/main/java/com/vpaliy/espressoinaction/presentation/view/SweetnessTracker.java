package com.vpaliy.espressoinaction.presentation.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.vpaliy.espressoinaction.R;

public class SweetnessTracker extends View {

    private static final String STATE_PARENT = "parent";
    private static final String STATE_ANGLE = "angle";
    private static final int TEXT_SIZE_DEFAULT_VALUE = 25;
    private static final int END_WHEEL_DEFAULT_VALUE = 360;
    public static final int COLOR_WHEEL_STROKE_WIDTH_DEF_VALUE = 16;
    public static final float POINTER_RADIUS_DEF_VALUE = 8;
    public static final int MAX_POINT_DEF_VALUE = 100;
    public static final int START_ANGLE_DEF_VALUE = 0;

    private OnStartTracking onStartTrackingTouch;
    private OnStopTracking onStopTrackingTouch;

    private Paint colorWheelPaint;
    private Paint pointerPaint;
    private Paint pointerColor;
    private int wheelStrokeWidth;
    private float pointerRadius;
    private RectF colorWheelRectangle = new RectF();
    private boolean isUserDraggingPointer = false;
    private float translationOffset;
    private float colorWheelRadius;
    private float angle;
    private Paint textPaint;
    private String text;
    private int max = 100;

    private Paint mArcColor;
    private int wheelColor;
    private int unactiveWheelColor;
    private int pointerColorX;
    private int pointerHaloColor;
    private int text_size;
    private int text_color;
    private int initPosition = -1;
    private boolean blockEnd = false;
    private float lastX;
    private int lastRadians = 0;
    private boolean blockStart = false;
    private int arcFinishRadians = 360;
    private int startArc = 270;
    private float[] pointerPosition;
    private RectF colorCenterHaloRectangle = new RectF();
    private int endWheel;
    private boolean showText = true;
    private Rect bounds = new Rect();

    public SweetnessTracker(Context context) {
        super(context);
        init(null, 0);
    }

    public SweetnessTracker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SweetnessTracker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.SweetnessTracker, defStyle, 0);
        initAttributes(a);
        a.recycle();

        colorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorWheelPaint.setColor(unactiveWheelColor);
        colorWheelPaint.setStyle(Style.STROKE);
        colorWheelPaint.setStrokeWidth(wheelStrokeWidth);

        Paint mColorCenterHalo = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorCenterHalo.setColor(Color.CYAN);
        mColorCenterHalo.setAlpha(0xCC);

        pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointerPaint.setColor(pointerHaloColor);
        pointerPaint.setStrokeWidth(pointerRadius + 10);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setColor(text_color);
        textPaint.setStyle(Style.FILL_AND_STROKE);
        textPaint.setTextAlign(Align.LEFT);
        textPaint.setTextSize(text_size);

        pointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointerColor.setStrokeWidth(pointerRadius);
        pointerColor.setColor(pointerColorX);

        mArcColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcColor.setColor(wheelColor);
        mArcColor.setStyle(Style.STROKE);
        mArcColor.setStrokeWidth(wheelStrokeWidth);

        Paint circleTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleTextColor.setColor(Color.WHITE);
        circleTextColor.setStyle(Style.FILL);

        arcFinishRadians = (int) calculateAngleFromText(initPosition) - 90;

        if (arcFinishRadians > endWheel)
            arcFinishRadians = endWheel;
        angle = calculateAngleFromRadians(arcFinishRadians > endWheel ? endWheel
                : arcFinishRadians);
        setTextFromAngle(calculateValueFromAngle(arcFinishRadians));

        invalidate();
    }

    private void setTextFromAngle(int angleValue) {
        this.text = String.valueOf(angleValue);
    }

    private void initAttributes(TypedArray a) {
        wheelStrokeWidth = a.getInteger(
                R.styleable.SweetnessTracker_wheel_size, COLOR_WHEEL_STROKE_WIDTH_DEF_VALUE);
        pointerRadius = a.getDimension(
                R.styleable.SweetnessTracker_pointer_size, POINTER_RADIUS_DEF_VALUE);
        max = a.getInteger(R.styleable.SweetnessTracker_max, MAX_POINT_DEF_VALUE);

        String wheel_color_attr = a
                .getString(R.styleable.SweetnessTracker_wheel_active_color);
        String wheel_unactive_color_attr = a
                .getString(R.styleable.SweetnessTracker_wheel_unactive_color);
        String pointer_color_attr = a
                .getString(R.styleable.SweetnessTracker_pointer_color);
        String pointer_halo_color_attr = a
                .getString(R.styleable.SweetnessTracker_pointer_halo_color);

        String text_color_attr = a.getString(R.styleable.SweetnessTracker_text_color);

        text_size = a.getDimensionPixelSize(R.styleable.SweetnessTracker_text_size, TEXT_SIZE_DEFAULT_VALUE);

        initPosition = a.getInteger(R.styleable.SweetnessTracker_init_position, 0);

        startArc = a.getInteger(R.styleable.SweetnessTracker_start_angle, START_ANGLE_DEF_VALUE);
        endWheel = a.getInteger(R.styleable.SweetnessTracker_end_angle, END_WHEEL_DEFAULT_VALUE);

        showText = a.getBoolean(R.styleable.SweetnessTracker_show_text, true);

        lastRadians = endWheel;

        if (initPosition < startArc)
            initPosition = calculateTextFromStartAngle(startArc);

        if (wheel_color_attr != null) {
            try {
                wheelColor = Color.parseColor(wheel_color_attr);
            } catch (IllegalArgumentException e) {
                wheelColor = Color.DKGRAY;
            }

        } else {
            wheelColor = Color.DKGRAY;
        }
        if (wheel_unactive_color_attr != null) {
            try {
                unactiveWheelColor = Color
                        .parseColor(wheel_unactive_color_attr);
            } catch (IllegalArgumentException e) {
                unactiveWheelColor = Color.CYAN;
            }

        } else {
            unactiveWheelColor = Color.CYAN;
        }

        if (pointer_color_attr != null) {
            try {
                pointerColorX = Color.parseColor(pointer_color_attr);
            } catch (IllegalArgumentException e) {
                pointerColorX = Color.CYAN;
            }

        } else {
            pointerColorX = Color.CYAN;
        }

        if (pointer_halo_color_attr != null) {
            try {
                pointerHaloColor = Color.parseColor(pointer_halo_color_attr);
            } catch (IllegalArgumentException e) {
                pointerHaloColor = Color.CYAN;
            }

        } else {
            pointerHaloColor = Color.DKGRAY;
        }

        if (text_color_attr != null) {
            try {
                text_color = Color.parseColor(text_color_attr);
            } catch (IllegalArgumentException e) {
                text_color = Color.CYAN;
            }
        } else {
            text_color = Color.CYAN;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
         canvas.translate(translationOffset, translationOffset);

        canvas.drawArc(colorWheelRectangle, startArc + 270, endWheel
                - (startArc), false, colorWheelPaint);

        canvas.drawArc(colorWheelRectangle, startArc + 270,
                (arcFinishRadians) > (endWheel) ? endWheel - (startArc)
                        : arcFinishRadians - startArc, false, mArcColor);

        canvas.drawCircle(pointerPosition[0], pointerPosition[1],
                pointerRadius, pointerPaint);

        canvas.drawCircle(pointerPosition[0], pointerPosition[1],
                (float) (pointerRadius / 1.2), pointerColor);
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        if (showText)
            canvas.drawText(
                    text,
                    (colorWheelRectangle.centerX())
                            - (textPaint.measureText(text) / 2),
                    colorWheelRectangle.centerY() + bounds.height() / 2,
                    textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);

        translationOffset = min * 0.5f;
        colorWheelRadius = translationOffset - pointerRadius;

        colorWheelRectangle.set(-colorWheelRadius, -colorWheelRadius,
                colorWheelRadius, colorWheelRadius);

        colorCenterHaloRectangle.set(-colorWheelRadius / 2,
                -colorWheelRadius / 2, colorWheelRadius / 2,
                colorWheelRadius / 2);

        updatePointerPosition();

    }

    private int calculateValueFromAngle(float angle) {
        float m = angle - startArc;

        float f = (endWheel - startArc) / m;

        return (int) (max / f);
    }

    private int calculateTextFromStartAngle(float angle) {
        float f = (endWheel - startArc) / angle;

        return (int) (max / f);
    }

    private double calculateAngleFromText(int position) {
        if (position == 0 || position >= max)
            return (float) 90;

        double f = (double) max / (double) position;

        double f_r = 360 / f;

        return f_r + 90;
    }

    private int calculateRadiansFromAngle(float angle) {
        float unit = (float) (angle / (2 * Math.PI));
        if (unit < 0) {
            unit += 1;
        }
        int radians = (int) ((unit * 360) - ((360 / 4) * 3));
        if (radians < 0)
            radians += 360;
        return radians;
    }

    private float calculateAngleFromRadians(int radians) {
        return (float) (((radians + 270) * (2 * Math.PI)) / 360);
    }


    public int getValue() {
        return Integer.valueOf(text);
    }

    public void setMax(int max) {
        this.max = max;
        setTextFromAngle(calculateValueFromAngle(arcFinishRadians));
        updatePointerPosition();
        invalidate();
    }

    public void setValue(float newValue) {
        if (newValue == 0) {
            arcFinishRadians = startArc;
        } else if (newValue == this.max) {
            arcFinishRadians = endWheel;
        } else {
            float newAngle = (float) (360.0 * (newValue / max));
            arcFinishRadians = (int) calculateAngleFromRadians(calculateRadiansFromAngle(newAngle)) + 1;
        }

        angle = calculateAngleFromRadians(arcFinishRadians);
        setTextFromAngle(calculateValueFromAngle(arcFinishRadians));
        updatePointerPosition();
        if(onStartTrackingTouch!=null){
            onStartTrackingTouch.onStart(this);
        }
        if(onStopTrackingTouch!=null){
            onStopTrackingTouch.onStop(this);
        }
        invalidate();
    }

    private void updatePointerPosition() {
        pointerPosition = calculatePointerPosition(angle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - translationOffset;
        float y = event.getY() - translationOffset;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                angle = (float) Math.atan2(y, x);

                blockEnd = false;
                blockStart = false;
                isUserDraggingPointer = true;

                arcFinishRadians = calculateRadiansFromAngle(angle);

                if (arcFinishRadians > endWheel) {
                    arcFinishRadians = endWheel;
                    blockEnd = true;
                }

                if (!blockEnd) {
                    setTextFromAngle(calculateValueFromAngle(arcFinishRadians));
                    updatePointerPosition();
                    invalidate();
                }
                if (onStartTrackingTouch != null) {
                    onStartTrackingTouch.onStart(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isUserDraggingPointer) {
                    angle = (float) Math.atan2(y, x);

                    int radians = calculateRadiansFromAngle(angle);

                    if (lastRadians > radians && radians < (360 / 6) && x > lastX
                            && lastRadians > (360 / 6)) {
                        if (!blockEnd && !blockStart)
                            blockEnd = true;
                    } else if (lastRadians >= startArc
                            && lastRadians <= (360 / 4) && radians <= (360 - 1)
                            && radians >= ((360 / 4) * 3) && x < lastX) {
                        if (!blockStart && !blockEnd)
                            blockStart = true;
                    } else if (radians >= endWheel && !blockStart
                            && lastRadians < radians) {
                        blockEnd = true;
                    } else if (radians < endWheel && blockEnd
                            && lastRadians > endWheel) {
                        blockEnd = false;
                    } else if (radians < startArc && lastRadians > radians
                            && !blockEnd) {
                        blockStart = true;
                    } else if (blockStart && lastRadians < radians
                            && radians > startArc && radians < endWheel) {
                        blockStart = false;
                    }

                    if (blockEnd) {
                        arcFinishRadians = endWheel - 1;
                        setTextFromAngle(max);
                        angle = calculateAngleFromRadians(arcFinishRadians);
                        updatePointerPosition();
                    } else if (blockStart) {
                        arcFinishRadians = startArc;
                        angle = calculateAngleFromRadians(arcFinishRadians);
                        setTextFromAngle(0);
                        updatePointerPosition();
                    } else {
                        arcFinishRadians = calculateRadiansFromAngle(angle);
                        setTextFromAngle(calculateValueFromAngle(arcFinishRadians));
                        updatePointerPosition();
                    }
                    invalidate();
                    lastRadians = radians;

                }
                break;
            case MotionEvent.ACTION_UP:
                isUserDraggingPointer = false;
                if (onStopTrackingTouch != null) {
                    onStopTrackingTouch.onStop(this);
                }
                break;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        lastX = x;

        return true;
    }

    private float[] calculatePointerPosition(float angle) {
        float x = (float) (colorWheelRadius * Math.cos(angle));
        float y = (float) (colorWheelRadius * Math.sin(angle));
        return new float[]{x, y};
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloat(STATE_ANGLE, angle);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        angle = savedState.getFloat(STATE_ANGLE);
        arcFinishRadians = calculateRadiansFromAngle(angle);
        setTextFromAngle(calculateValueFromAngle(arcFinishRadians));
        updatePointerPosition();
    }

    public void setInitPosition(int init) {
        initPosition = init;
        setTextFromAngle(initPosition);
        angle = calculateAngleFromRadians(initPosition);
        arcFinishRadians = calculateRadiansFromAngle(angle);
        updatePointerPosition();
        invalidate();
    }

    public void setOnStartTrackingTouch(OnStartTracking onStartTrackingTouch) {
        this.onStartTrackingTouch = onStartTrackingTouch;
    }

    public void setOnStopTrackingTouch(OnStopTracking onStopTrackingTouch) {
        this.onStopTrackingTouch = onStopTrackingTouch;
    }

    public int getMaxValue() {
        return max;
    }


    public interface OnStartTracking{
        void onStart(SweetnessTracker tracker);
    }

    public interface OnStopTracking{
        void onStop(SweetnessTracker tracker);
    }

}