package no.kapelrud;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Rotary Knob Widget with zoom-on-touch for Android.
 * Created by André on 18.02.2015.
 */
public class RotaryKnob extends View {

    private static final String TAG = "RotaryKnob";
    //private static final int SCROLL_VELOCITY_DOWNSCALE = 1;
    private static final int SCROLL_ANGULAR_SCALE_DP = 48;
    private final int OVERLAY_PADDING_DP = 12;
    private static final int ROTATION_SNAP_BUFFER = 30;
        // Used when mSectorHalfOpening is small, to prevent jumping from max to min value too quick.

    private final int DEFAULT_KNOB_DIAMETER = 88;
        // 96dp allowing for the standard 4dp padding on each side.

    private final int OPENING_TEXT_MARGIN = dpToPx(2);
    private boolean mbScrolling = false;

    private int mNumSteps = 1;
    private float mMinValue = 0;
    private float mMaxValue = 100;
    private int mValueNumDigits = 1;

    private String mValueStr;
    private String mUnitStr = "";
    private float mValue = 50;
    private float mTextSize = spToPx(20);
    private float mTextWidth = 0.0f;
    private float mTextHeight = 0.0f;

    private int mSectorRotation = 0; // degrees. Extra rotation of the knob. User set.
    private float mSectorHalfOpening = 30; // degrees
    private float mSectorMinRadiusScale = 0.4f;
    private float mSectorMaxRadiusScale = 0.75f;
    private float mTickMinRadiusScale = 0.8f;
    private float mTickMaxRadiusScale = 1.0f;

    private boolean mShowValue = true;
    private boolean mShowNeedle = true;
    private boolean mShowTicks = true;
    private boolean mSubtractTicks = true;
    private boolean mShowUnit = true;

    private boolean mTrackValue = false;
    private float mStartScrollValue;

    private int mNumTicks = 2; // +1 sections

    private int mTextColor = 0xff000000;
    private int mSectorColor = 0xffdddddd;
    private int mValueSectorColor = 0xffaaaaaa;
    private int mTicksColor = 0xff006699;
    private int mNeedleColor = 0xff880000;

    private float mNeedleWidth = dpToPx(4);
    private float mTicksWidth = dpToPx(4);
    private float mTicksSubtractWidth = dpToPx(2);
    private float mNeedleRadius = 1.0f;

    private Rect mOverlayGlobalBounds = new Rect();
    private final int mOverlaySizeDP = 192; // size of overlay in dp-s.

    private static final int VALUEPOS_BOTTOM = 0;
    private static final int VALUEPOS_LEFT = 1;
    private static final int VALUEPOS_TOP = 2;
    private static final int VALUEPOS_RIGHT = 3;
    private static final int VALUEPOS_CENTER = 4;

    private static final int VALUEPOS_MAX = VALUEPOS_CENTER;

    private int mValuePosition = VALUEPOS_BOTTOM;
    private float mRotation = 0.0f;
    private float mAccumulatedAngleChange;

    private RotaryKnobDrawable mOverlayKnobProxy;
    private RotaryKnobImpl mLayedOutKnob;
    private RotaryKnobImpl mOverlayKnob;
    private LayerDrawable mOverlay;

    private Paint mSectorPaint;
    private Paint mValueSectorPaint;

    private GestureDetector mDetector;
    private OnValueChangedListener mListener = null;

    public RotaryKnob(Context context) {
        super(context);
        init();
    }

    public interface OnValueChangedListener {
        void onValueChanged(RotaryKnob sourceKnob, float value);
    }

    public RotaryKnob(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.RotaryKnob, 0, 0);
        try{
            mShowValue = a.getBoolean(R.styleable.RotaryKnob_showValue, mShowValue);
            mShowUnit = a.getBoolean(R.styleable.RotaryKnob_showUnit, mShowUnit);
            mValueNumDigits = a.getInteger(R.styleable.RotaryKnob_numDigits, mValueNumDigits);
            mUnitStr = a.getString(R.styleable.RotaryKnob_unit);
            mMinValue = a.getFloat(R.styleable.RotaryKnob_minValue, mMinValue);
            mMaxValue = a.getFloat(R.styleable.RotaryKnob_maxValue, mMaxValue);
            mValue = a.getFloat(R.styleable.RotaryKnob_value, mValue);
            mValuePosition = a.getInt(R.styleable.RotaryKnob_valuePosition, mValuePosition);
            mNumSteps = a.getInteger(R.styleable.RotaryKnob_numSteps, mNumSteps);
            mTextColor = a.getColor(R.styleable.RotaryKnob_valueColor, mTextColor);
            mTextSize = a.getDimension(R.styleable.RotaryKnob_textSize, mTextSize);

            mTrackValue = a.getBoolean(R.styleable.RotaryKnob_trackValue, mTrackValue);

            mSectorHalfOpening = 0.5f*a.getFloat(R.styleable.RotaryKnob_sector_openAngle, 2.0f*mSectorHalfOpening);
            mSectorRotation = a.getInt(R.styleable.RotaryKnob_sector_rotation, mSectorRotation);
            mSectorMinRadiusScale = a.getFloat(R.styleable.RotaryKnob_sector_minRadius, mSectorMinRadiusScale);
            mSectorMaxRadiusScale = a.getFloat(R.styleable.RotaryKnob_sector_maxRadius, mSectorMaxRadiusScale);
            mSectorColor = a.getColor(R.styleable.RotaryKnob_sector_backgroundColor, mSectorColor);
            mValueSectorColor = a.getColor(R.styleable.RotaryKnob_sector_foregroundColor, mValueSectorColor);

            mShowTicks = a.getBoolean(R.styleable.RotaryKnob_showTicks, mShowTicks);
            mSubtractTicks = a.getBoolean(R.styleable.RotaryKnob_subtractTicks, mSubtractTicks);
            mTickMinRadiusScale = a.getFloat(R.styleable.RotaryKnob_ticks_minRadius, mTickMinRadiusScale);
            mTickMaxRadiusScale = a.getFloat(R.styleable.RotaryKnob_ticks_maxRadius, mTickMaxRadiusScale);
            mTicksWidth = a.getDimension(R.styleable.RotaryKnob_ticks_thickness, mTicksWidth);
            mTicksSubtractWidth = a.getDimension(R.styleable.RotaryKnob_ticks_subtractionThickness, mTicksSubtractWidth);
            mTicksColor = a.getColor(R.styleable.RotaryKnob_ticks_color, mTicksColor);
            mNumTicks = a.getInteger(R.styleable.RotaryKnob_numTicks, mNumTicks);

            mShowNeedle = a.getBoolean(R.styleable.RotaryKnob_showNeedle, mShowNeedle);
            mNeedleColor = a.getColor(R.styleable.RotaryKnob_needle_color, mNeedleColor);
            mNeedleWidth = a.getDimension(R.styleable.RotaryKnob_needle_thickness, mNeedleWidth);
            mNeedleRadius = a.getFloat(R.styleable.RotaryKnob_needle_radius, mNeedleRadius);

        } finally {
            a.recycle();
        }

        init();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d(TAG, "Saving instance state");
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putFloat("value", mValue);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if(state instanceof Bundle) {
            Bundle bundle = (Bundle)state;
            state = bundle.getParcelable("instanceState");

            mValue = bundle.getFloat("value");
            mRotation = valueToRotation();
            updateText();
        }
        super.onRestoreInstanceState(state);
    }

    private void init() {
        setLayerToSW(this);

        mOverlayKnobProxy = new RotaryKnobDrawable();
        mOverlay = new LayerDrawable(new Drawable[]{
                getResources().getDrawable(R.drawable.container_dropshadow),
                mOverlayKnobProxy
        });

        checkValueBounds();
        mValue = snapValueToSteps(mValue);
        mRotation = valueToRotation();
        updateText();

        Paint tmpTextPaint = getTextPaint(1.0f);
        // These sizes are needed upon measuring
        mTextHeight = tmpTextPaint.getTextSize();
        mTextWidth = getTextWidth(tmpTextPaint);

        // These paints does not change between overlay and displayed widget:
        mSectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectorPaint.setStyle(Paint.Style.FILL);
        mSectorPaint.setColor(mSectorColor);
        mValueSectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValueSectorPaint.setStyle(Paint.Style.FILL);
        mSectorPaint.setColor(mValueSectorColor);

        mDetector = new GestureDetector(RotaryKnob.this.getContext(), new mGestureListener());
        mDetector.setIsLongpressEnabled(false);

        if(mValuePosition < 0 || mValuePosition > VALUEPOS_MAX)
            mValuePosition = VALUEPOS_BOTTOM;
    }

    private String formatValueString(float value) {
        String res = String.format("%." + mValueNumDigits + "f", value);
        if(mShowUnit && mUnitStr != null && !mUnitStr.equals(""))
            res += " "+mUnitStr;
        return res;
    }

    private void updateText() {
        mValueStr = formatValueString(mValue);
    }

    private void checkValueBounds() {
        if(mMaxValue < mMinValue) {
            float tmp = mMinValue;
            mMinValue = mMaxValue;
            mMaxValue = tmp;
        }

        if(mValue > mMaxValue)
            mValue = mMaxValue;
        else if(mValue < mMinValue)
            mValue = mMinValue;
    }

    public float snapValueToSteps(float value) {
        final float VALUE_STEP_SIZE = (mMaxValue-mMinValue)/mNumSteps;
        return ( mMinValue+VALUE_STEP_SIZE*Math.round( (value-mMinValue)/VALUE_STEP_SIZE ));
    }

    public void setLayerToSW(View v) {
        if(!v.isInEditMode() && Build.VERSION.SDK_INT >= 11)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setLayerToHW(View v) {
        if(!v.isInEditMode() && Build.VERSION.SDK_INT >= 11)
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void setValueChangedListener(OnValueChangedListener listener) {
        mListener = listener;
        listener.onValueChanged(this, mValue);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!mbScrolling)
            mLayedOutKnob.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(!changed)
            return;

        // We
        RectF bounds = mLayedOutKnob.getBounds();
        float aspectRatio = bounds.width()/bounds.height();
        calculateOverlayBounds(aspectRatio);

        int padding = dpToPx(OVERLAY_PADDING_DP);
        RectF overlayBounds = new RectF(0,0,
                mOverlayGlobalBounds.width()-2*padding,
                mOverlayGlobalBounds.height()-2*padding);
        float overlayRelativeScale = overlayBounds.width()/bounds.width();
        mOverlayKnob = new RotaryKnobImpl(overlayBounds, overlayRelativeScale);
        mOverlay.setBounds(mOverlayGlobalBounds);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float xpad = (float)(getPaddingLeft()+getPaddingRight());
        float ypad = (float)(getPaddingTop()+getPaddingBottom());
        float ww = (float)w-xpad;
        float hh = (float)h-ypad;

        RectF bounds = new RectF(0, 0, ww, hh);
        bounds.offsetTo(getPaddingLeft(), getPaddingTop());
        mLayedOutKnob = new RotaryKnobImpl(bounds);

        // Overlay size is calculated on layout to get the proper global position of this view.
    }

    public float clampRotation(float rotation) { // TODO: should allow rotation == 360.
        rotation %= 360;
        if(rotation < 0) rotation+=360;
        return rotation;
    }

    public float valueToRotation() {
        float rotation = (270- mSectorHalfOpening -(mValue-mMinValue)/(mMaxValue-mMinValue)*(360-2* mSectorHalfOpening));
        return clampRotation(rotation);
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        int res = 0;
        switch(mValuePosition) {
            case VALUEPOS_BOTTOM:
            case VALUEPOS_TOP:
                res = (int)mTextHeight+getSuggestedMinimumWidth();
                break;
            case VALUEPOS_CENTER:
            case VALUEPOS_RIGHT:
            case VALUEPOS_LEFT:
                res = (int)mTextHeight*2;
                break;
        }
        return res;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        int res = 0;
        switch(mValuePosition) {
            case VALUEPOS_BOTTOM:
            case VALUEPOS_TOP:
                res = (int)mTextWidth;
                break;
            case VALUEPOS_CENTER:
                res = getSuggestedMinimumHeight();
                break;
            case VALUEPOS_RIGHT:
            case VALUEPOS_LEFT:
                res = (int)mTextWidth+getSuggestedMinimumHeight();
                break;
        }
        return res;
    }

    /**
     * Get the suggested height of this view given a determined width.
     * @param width View's width (without padding)
     * @param maxHeight Maximum allowed height
     * @return suggested height (without padding)
     */
    private int getSuggestedHeight(int width, int maxHeight) {
        if(width<=0 || maxHeight<=0)
            return 0;

        int h = width;
        switch(mValuePosition) {
            case VALUEPOS_CENTER:
                // Do nothing, try to have same height as width
                break;
            case VALUEPOS_BOTTOM:
            case VALUEPOS_TOP:
                h += (int)mTextHeight;
                break;
            case VALUEPOS_RIGHT:
            case VALUEPOS_LEFT:
                int rw = width-(int)mTextWidth;
                    // real width of knob
                h = Math.max((rw > 0 ? rw : 0), (int) mTextHeight);
                    // biggest of knob width or text height
                break;
        }
        if(h>maxHeight)
            h = maxHeight;
        return h;
    }

    public float getTextOffset(float textSize) {
        return 0.5f * (textSize + OPENING_TEXT_MARGIN) / (float) Math.tan(mSectorHalfOpening / 180.0d * Math.PI);
    }

    /**
     * Get the suggested width of this view given a set height.
     * @param height View's height (without padding)
     * @param maxWidth
     * @return suggested width (without padding)
     */
    private int getSuggestedWidth(int height, int maxWidth) {
        if(height <= 0 || maxWidth <=0)
            return 0;

        int w = height;
        switch(mValuePosition) {
            case VALUEPOS_CENTER:
                // Do nothing, try to have same width as height
                break;
            case VALUEPOS_BOTTOM:
            case VALUEPOS_TOP:
                int rh = height-(int)mTextHeight;
                w = Math.max((rh > 0 ? rh:0), (int)mTextHeight);
                break;
            case VALUEPOS_RIGHT:
            case VALUEPOS_LEFT:
                float offset = getTextOffset(mTextSize);
                if(offset >  0.5f*w)
                    offset = 0.5f*w;
                w += (int)(mTextWidth+offset-w/2);
                break;
        }
        if(w>maxWidth)
            w = maxWidth;
        return w;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);

        int xPad = getPaddingLeft()+getPaddingRight();
        int yPad = getPaddingTop()+getPaddingBottom();

        int w=0;
        int h=0;

        //Log.d(TAG, "measurespecs: "+widthMode+", "+heightMode);

        float radii = 0.5f*dpToPx(DEFAULT_KNOB_DIAMETER);
        switch(widthMode) {
            case MeasureSpec.EXACTLY:
                w = specWidth;
                switch(heightMode) {
                    case MeasureSpec.EXACTLY:
                        h = specHeight;
                        break;
                    case MeasureSpec.AT_MOST:
                        h = getSuggestedHeight(w-xPad, specHeight-yPad)+yPad;
                        break;
                    case MeasureSpec.UNSPECIFIED:
                        h = getSuggestedHeight(w-xPad, Integer.MAX_VALUE-yPad)+yPad;
                        break;
                }
                break;
            case MeasureSpec.AT_MOST:
                switch(heightMode) {
                    case MeasureSpec.EXACTLY:
                        h = specHeight;
                        w = getSuggestedWidth(h-yPad, specWidth-xPad)+xPad;
                        break;
                    case MeasureSpec.AT_MOST:
                        w = dpToPx(DEFAULT_KNOB_DIAMETER)+xPad;
                        h = dpToPx(DEFAULT_KNOB_DIAMETER)+yPad;
                        switch(mValuePosition) {
                            case VALUEPOS_BOTTOM:
                            case VALUEPOS_TOP:
                                h += mTextHeight;
                                break;
                            case VALUEPOS_RIGHT:
                            case VALUEPOS_LEFT:
                                float offset = radii - getTextOffset(mTextHeight);
                                if(offset < 0.0f)
                                    offset = 0.0f;
                                w += mTextWidth+offset;
                                break;
                        }
                        break;
                    case MeasureSpec.UNSPECIFIED:
                        w = specWidth;
                        h = getSuggestedHeight(w-xPad, Integer.MAX_VALUE-yPad)+yPad;
                        break;
                }
                break;
            case MeasureSpec.UNSPECIFIED:
                switch(heightMode) {
                    case MeasureSpec.EXACTLY:
                        h = specHeight;
                        w = getSuggestedWidth(h-yPad, Integer.MAX_VALUE-xPad)+xPad;
                        break;
                    case MeasureSpec.AT_MOST:
                        h = specHeight;
                        w = getSuggestedWidth(h-yPad, Integer.MAX_VALUE-xPad)+xPad;
                        break;
                    case MeasureSpec.UNSPECIFIED:
                        w = dpToPx(DEFAULT_KNOB_DIAMETER)+xPad;
                        h = dpToPx(DEFAULT_KNOB_DIAMETER)+yPad;
                        switch(mValuePosition) {
                            case VALUEPOS_BOTTOM:
                            case VALUEPOS_TOP:
                                h += mTextHeight;
                                break;
                            case VALUEPOS_RIGHT:
                            case VALUEPOS_LEFT:
                                float offset = radii - getTextOffset(mTextHeight);
                                if(offset < 0.0f)
                                    offset = 0.0f;
                                w += mTextWidth+offset;
                                break;
                        }
                        break;
                }
                break;
        }
        /*int w = MEASURED_SIZE_MASK & resolveSizeAndState(minw, widthMeasureSpec, 1);
        int minh = getPaddingBottom()+getPaddingTop()+w;
        if(mValuePosition == VALUEPOS_BOTTOM || mValuePosition == VALUEPOS_TOP)
            minh += mTextHeight;
        //int h = Math.min(MeasureSpec.getSize(heightMeasureSpec),minh);
        int h = MEASURED_SIZE_MASK & resolveSizeAndState(minh, heightMeasureSpec, 1);*/

        setMeasuredDimension(w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mDetector.onTouchEvent(event);
        if(!result) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                getRootView().getOverlay().remove(mOverlay);

                // Make sure we are drawing correctly, once the overlay is removed.
                mLayedOutKnob.recreatePaths();
                invalidate();
                mbScrolling = false;

                if(!mTrackValue && mStartScrollValue != mValue && mListener!=null)
                    mListener.onValueChanged(this, mValue);
            }
        }
        return result;
    }

    public float getKnobRotation() {
        return mRotation;
    }

    public void addRotationChange(float deltaAlpha) {
        mAccumulatedAngleChange += deltaAlpha;
        setKnobRotation(mRotation+mAccumulatedAngleChange);
    }

    public void setKnobRotation(float rotation) {
        rotation = clampRotation(rotation);
        float oldRotation = mRotation;
            // make sure we are working with a rotation in [0,360] deg
        boolean forbidden = (rotation > (270- mSectorHalfOpening) && rotation < (270+ mSectorHalfOpening));
        if(mRotation <= (270- mSectorHalfOpening) && forbidden)
            mRotation = 270- mSectorHalfOpening;
        else if(mRotation >= (270+ mSectorHalfOpening) && forbidden)
            mRotation = 270+ mSectorHalfOpening;
        else
            mRotation = rotation;

        float newValue = rotationToValidValue(mRotation);
        final float rotDiff = oldRotation-mRotation;
        boolean snap = false;
        if(oldRotation >= 270 && oldRotation < 360 && mRotation < 270 && mRotation > 180 && mValue != mMinValue) {
            if(rotDiff<ROTATION_SNAP_BUFFER) {
                newValue = mMaxValue;
                snap = true;
            }else if(mValue != mMinValue)
                newValue = mMinValue;
        }else if(oldRotation <= 270 && oldRotation > 180 && mRotation > 270 && mRotation < 360 && mValue != mMaxValue) {
            if(-rotDiff<ROTATION_SNAP_BUFFER) {
                newValue = mMinValue;
                snap = true;
            }else if(mValue != mMaxValue)
                newValue = mMaxValue;
        }

        boolean notify = mValue != newValue;

        mValue = newValue;
        mRotation = valueToRotation(); // move needle to the validated value.
        if(!snap && mRotation != oldRotation)
            mAccumulatedAngleChange = 0.0f;

        updateText();

        if(!mbScrolling) {
            if (mLayedOutKnob != null)
                mLayedOutKnob.recreatePaths();
            invalidate();
        }else {
            if(mOverlayKnob != null)
                mOverlayKnob.recreatePaths();
            mOverlayKnobProxy.invalidateSelf();
        }

        if(mTrackValue && notify && mListener != null)
            mListener.onValueChanged(this, mValue);
    }

    public float rotationToSweep(float rotation) {
        float sweep = 270.0f- mSectorHalfOpening -rotation;
        if(rotation == 270.0f && mValue == mMaxValue)
            sweep = 360.0f-2* mSectorHalfOpening;
        else if(rotation > 270.0f)
                sweep += 360.0f;
        return sweep;
    }

    public float rotationToValidValue(float rotation) {
        rotation = clampRotation(rotation);
        final float MAX_SWEEP = 360-2* mSectorHalfOpening;
        final float sweepAngle = rotationToSweep(rotation);
        float sweepRatio = sweepAngle / MAX_SWEEP;
        if(sweepRatio > 1.0f)
            sweepRatio = 1.0f;
        else if (sweepRatio < 0.0f)
            sweepRatio = 0.0f;

        return snapValueToSteps((mMaxValue - mMinValue) * sweepRatio + mMinValue);
    }

    public boolean showValue() { return mShowValue; }

    public void setShowValue(boolean show) {
        mShowValue = show;
        invalidate();
    }

    private void calculateOverlayBounds(float aspectRatio) {
        // Global coordinates of this RotaryKnob:
        Rect visibleRect = new Rect();
        getGlobalVisibleRect(visibleRect);
        /*int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0)
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);*/

        View root = getRootView();
        int rootWidth = root.getWidth();
        int rootHeight = root.getHeight();

        // get knob center
        /*int centerX = visibleRect.left + getPaddingLeft() + (int)(mLayedOutKnob.getBounds().width()*0.5f);
        int centerY = visibleRect.top + getPaddingTop() + (int)(mLayedOutKnob.getBounds().height()*0.5f);//-statusBarHeight;
        */

        int centerX = visibleRect.centerX() + (getPaddingLeft()-getPaddingRight());
        int centerY = visibleRect.centerY() + (getPaddingTop()-getPaddingBottom());

        int overlayHeight = dpToPx(mOverlaySizeDP);
        int overlayWidth = (int)(overlayHeight*aspectRatio);
        // TODO: make sure overlay size is not larger than screen:

        mOverlayGlobalBounds.left = mOverlayGlobalBounds.top = 0; // to avoid bugs below.
        mOverlayGlobalBounds.right = overlayWidth;
        mOverlayGlobalBounds.bottom = overlayHeight;

        int posX, posY;
        if(centerX < overlayWidth/2)
            posX = 0; // at left edge
        else if(centerX > (rootWidth-overlayWidth/2))
            posX = rootWidth-overlayWidth; // push in from right
        else
            posX = centerX-overlayWidth/2;

        if(centerY < overlayHeight/2)
            posY = 0; // at top edge
        else if(centerY > (rootHeight-overlayHeight/2))
            posY = rootHeight-overlayHeight; // push in from bottom
        else
            posY = centerY-overlayHeight/2;

        mOverlayGlobalBounds.offsetTo(posX, posY);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * displayMetrics.density+0.5f);
    }

    public int spToPx(int sp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(sp * displayMetrics.scaledDensity);
    }

    class RotaryKnobDrawable extends Drawable {

        public RotaryKnobDrawable() {
            super();
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            canvas.translate(bounds.left, bounds.top);
            int padding = dpToPx(OVERLAY_PADDING_DP);
            canvas.translate(padding, padding); // a little padding.
            mOverlayKnob.draw(canvas);
        }

        @Override
        public void setAlpha(int alpha) {}

        @Override
        public void setColorFilter(ColorFilter cf) {}

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    class mGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            mbScrolling = true;
            mStartScrollValue = mValue;
            mAccumulatedAngleChange = 0.0f;
            invalidate(); // force redraw, where we don't draw the layed out View (this)
            getRootView().getOverlay().add(mOverlay);
            return true; // must return true for onScroll to be called (!)
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Rect visibleRect = new Rect();
            getGlobalVisibleRect(visibleRect);

            final float cX = mOverlayGlobalBounds.left + dpToPx(OVERLAY_PADDING_DP) + mOverlayKnob.center().x;
            final float cY = mOverlayGlobalBounds.top + dpToPx(OVERLAY_PADDING_DP) + mOverlayKnob.center().y;
            float vx = (e2.getX()+visibleRect.left) - cX;
            float vy = -((e2.getY()+visibleRect.top) - cY);
                // invert y-coordinates so that up on the screen is positive y (wrt. the overlay center).
            float vxPrev = vx+distanceX;
            float vyPrev = vy-distanceY;

            float deltaAlpha = (float)Math.atan2(vy, vx)-(float)Math.atan2(vyPrev, vxPrev);
            // Correct for -PI to PI jumps (and v.v.) in deltaAlpha:
            if(deltaAlpha > Math.PI)
                deltaAlpha-=2*Math.PI;
            else if(deltaAlpha < -Math.PI)
                deltaAlpha+=2*Math.PI;

            final float vLen = (float)Math.sqrt(vx*vx+vy*vy);
            addRotationChange(deltaAlpha/(float)Math.PI*180.0f * (vLen/dpToPx(SCROLL_ANGULAR_SCALE_DP)));
                //Scale angle with length from center, do give user control.
                // TODO: implement inverse option as selectable attribute.
            return true;
        }
    }

    /**
     * Helper method for translating (x,y) scroll vectors into scalar rotation of the pie.
     *
     * @param dx The x component of the current scroll vector.
     * @param dy The y component of the current scroll vector.
     * @param x  The x position of the current touch, relative to the center.
     * @param y  The y position of the current touch, relative to the center.
     * @return The scalar representing the change in angular position for this scroll.
     */
    private static float vectorToScalarScroll(float dx, float dy, float x, float y) {
        // get the length of the vector
        float l = (float) Math.sqrt(dx * dx + dy * dy);

        // decide if the scalar should be negative or positive by finding
        // the dot product of the vector perpendicular to (x,y).
        float crossX = -y;
        float crossY = x;

        float dot = (crossX * dx + crossY * dy);
        float sign = Math.signum(dot);

        return l * sign;
    }

    public String getUnitStr() { return (mUnitStr == null ? "" : mUnitStr); }

    private Paint getTextPaint(float scaling) {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(mTextColor);
        // TODO: Fix font size for overlays
        if(mTextSize <= 0)
            mTextSize = spToPx(10);
        textPaint.setTextSize(mTextSize*scaling);
        return textPaint;
    }

    private int getTextWidth(Paint textPaint) {
        Rect textBounds = new Rect();
        String minString = formatValueString(mMinValue);
        String maxString = formatValueString(mMaxValue);
        textPaint.getTextBounds(minString, 0, minString.length(), textBounds);
        int minStringWidth = textBounds.width();
        textPaint.getTextBounds(maxString, 0, maxString.length(), textBounds);
        int maxStringWidth = textBounds.width();
        return Math.max(minStringWidth, maxStringWidth);
    }

    private class RotaryKnobImpl {
        private Path mSectorPath;
        private Path mValuePath;
        private float mRadius;
        private RectF mBounds;
        private PointF mKnobCenter;

        private Paint mTextPaint;
        private Paint mNeedlePaint;
        private Paint mTicksPaint;

        private float mTextX = 0.0f;
        private float mTextY = 0.0f;
        private float mScaling;

        private float mTextWidth;
        private float mTextHeight;

        public RotaryKnobImpl( RectF bounds) {
            this(bounds, 1.0f);
        }

        public RotaryKnobImpl (RectF bounds, float scaling) {
            mBounds = bounds;
            mScaling = scaling;

            mTextPaint = getTextPaint(mScaling);
            mTextHeight = mTextPaint.getTextSize();
            mTextWidth = getTextWidth(mTextPaint);

            mNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mNeedlePaint.setStyle(Paint.Style.STROKE);
            mNeedlePaint.setStrokeCap(Paint.Cap.ROUND);
            mNeedlePaint.setColor(mNeedleColor);
            mNeedlePaint.setStrokeWidth(mNeedleWidth*mScaling);

            mTicksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTicksPaint.setStyle(Paint.Style.STROKE);
            mTicksPaint.setColor(mTicksColor);
            mTicksPaint.setStrokeWidth(mTicksWidth*mScaling);

            float dW = bounds.width();
            float dH = bounds.height();
            float cX = bounds.centerX();
            float cY = bounds.centerY();

            float d = 0f;
            float offset = 0.0f;
            switch(mValuePosition) {
                case VALUEPOS_CENTER:
                    mTextPaint.setTextAlign(Paint.Align.CENTER);
                    d = Math.min(dW, dH);
                    mTextX = cX;
                    mTextY = cY-mTextPaint.getFontMetrics().ascent/2;
                    break;
                case VALUEPOS_BOTTOM:
                    mTextPaint.setTextAlign(Paint.Align.CENTER);
                    dH -= mTextHeight;
                    d = Math.min(dW, dH);
                    cY -= 0.5f*mTextHeight;
                    mTextX = cX;
                    mTextY = cY+0.5f*d-mTextPaint.getFontMetrics().ascent;
                    break;
                case VALUEPOS_TOP:
                    mTextPaint.setTextAlign(Paint.Align.CENTER);
                    dH -= mTextHeight;
                    d = Math.min(dW, dH);
                    cY += 0.5f*mTextHeight;
                    mTextX = cX;
                    mTextY = cY-0.5f*d+mTextPaint.getFontMetrics().descent;
                    break;
                case VALUEPOS_RIGHT:
                    mTextPaint.setTextAlign(Paint.Align.LEFT);
                    offset = getTextOffset(mTextHeight); // TODO, fix positioning of knob!
                    d = dH;
                    /*dW -= mTextWidth;
                    d = Math.min(dW, dH);
                    cX -= 0.5f * mTextWidth;*/
                    if (offset > 0.5f * d)
                        offset = 0.5f * d;
                    cX -= 0.5f * (mTextWidth+offset-0.5f*d);
                    mTextX = cX + offset;
                    mTextY = cY - mTextPaint.getFontMetrics().ascent / 2;
                    break;
                case VALUEPOS_LEFT:
                    mTextPaint.setTextAlign(Paint.Align.RIGHT);
                    offset = getTextOffset(mTextHeight);
                    d = dH;
                    if(offset > 0.5f*d)
                        offset = 0.5f*d;
                    /*dW -= mTextWidth;
                    d = Math.min(dW, dH);*/

                    cX += 0.5f * (mTextWidth+offset-0.5f*d);
                    mTextX = cX-offset;
                    mTextY = cY-mTextPaint.getFontMetrics().ascent/2;
                    break;
            }

            mRadius = 0.5f*d;
            mKnobCenter = new PointF(cX, cY);
            recreatePaths();
        }

        public PointF center() { return mKnobCenter; }
        public float getRadius() { return mRadius; }
        public float getTextSize() { return mTextPaint.getTextSize(); }
        public RectF getBounds() { return mBounds; }

        public void draw(Canvas canvas) {

            float rot = 0.0f;
            switch(mValuePosition) {
                case VALUEPOS_TOP:
                    rot = 180.0f;
                    break;
                case VALUEPOS_RIGHT:
                    rot = -90.0f;
                    break;
                case VALUEPOS_LEFT:
                    rot = 90.0f;
                    break;
            }
            if(mSectorRotation != 0)
                rot += mSectorRotation;
            canvas.rotate(rot, mKnobCenter.x, mKnobCenter.y);

            canvas.drawPath(mSectorPath, mSectorPaint);
            canvas.drawPath(mValuePath, mValueSectorPaint);

            if(mShowTicks && mNumTicks > 0) {
                float tickAngle = (270- mSectorHalfOpening)*(float)Math.PI/180.0f;
                float tickAngleIncrement = (float)Math.PI/180.0f*(360-2* mSectorHalfOpening)/(mNumTicks-1);
                for(int i=0; i<mNumTicks; i++) {
                    canvas.drawLine(
                            mKnobCenter.x+ mRadius *mTickMinRadiusScale*(float)Math.cos(tickAngle-i*tickAngleIncrement),
                            mKnobCenter.y- mRadius *mTickMinRadiusScale*(float)Math.sin(tickAngle-i*tickAngleIncrement),
                            mKnobCenter.x+ mRadius *mTickMaxRadiusScale*(float)Math.cos(tickAngle-i*tickAngleIncrement),
                            mKnobCenter.y- mRadius *mTickMaxRadiusScale*(float)Math.sin(tickAngle-i*tickAngleIncrement),
                            mTicksPaint
                    );
                }
            }

            if(mShowNeedle) {
                final float needleAngle = mRotation * (float) Math.PI / 180.f; // convert to radians
                canvas.drawLine(mKnobCenter.x, mKnobCenter.y,
                        mKnobCenter.x + mRadius * mNeedleRadius * (float) Math.cos(needleAngle),
                        mKnobCenter.y - mRadius * mNeedleRadius * (float) Math.sin(needleAngle),
                        mNeedlePaint
                );
            }

            canvas.rotate(-rot, mKnobCenter.x, mKnobCenter.y);

            // draw the value text
            if(mShowValue)
                canvas.drawText(mValueStr, mTextX, mTextY, mTextPaint);
        }

        private Path createSectorPath(float sweepAngle) {
            float startAngle = 90+mSectorHalfOpening;

            Path path = new Path();
            if(sweepAngle == 360) {
                path.addOval(circleBounds(mSectorMaxRadiusScale), Path.Direction.CCW);
                path.addOval(circleBounds(mSectorMinRadiusScale), Path.Direction.CW);
            }else {
                path.arcTo(circleBounds(mSectorMinRadiusScale), startAngle, sweepAngle);
                path.arcTo(circleBounds(mSectorMaxRadiusScale), startAngle + sweepAngle, -sweepAngle);
                path.close();
            }

            if(mSubtractTicks && mNumTicks > 0) {
                Path tickPath = new Path();
                tickPath.addRect(
                        0.0f, -0.5f*mTicksSubtractWidth*mScaling/mRadius,
                        1.0f,  0.5f*mTicksSubtractWidth*mScaling/mRadius,
                        Path.Direction.CCW);
                float tickAngle = (270- mSectorHalfOpening);
                float tickAngleIncrement = (360-2*mSectorHalfOpening)/(mNumTicks-1);
                Matrix tickMatrix = new Matrix();
                tickMatrix.postRotate(-tickAngle);
                Path rotatedTick = new Path();
                for(int i=0; i<mNumTicks; i++) {
                    tickPath.transform(tickMatrix, rotatedTick);
                    path.op(rotatedTick, Path.Op.DIFFERENCE);
                        // TODO: rewrite in terms of regions for lower API versions.
                    tickMatrix.postRotate(tickAngleIncrement);
                }
            }

            Matrix matrix = new Matrix();
            matrix.postScale(mRadius, mRadius);
            matrix.postTranslate(mKnobCenter.x, mKnobCenter.y);
            path.transform(matrix);

            return path;
        }

        private RectF circleBounds(float radius) {
            return new RectF(-radius,-radius,radius,radius);
        }

        public void recreatePaths() {
            mSectorPath = createSectorPath(360-2* mSectorHalfOpening);
            mValuePath = createSectorPath(rotationToSweep(mRotation));
        }
    }
}
