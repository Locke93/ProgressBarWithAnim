package com.github.library.progressbarview;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import com.example.library.R;

/**
 * @author pengj
 * @version 4.3 on 2016/9/6
 */
public class HorizontalProgressBarWithAnim extends ProgressBar {

    private int mBackgroundColor; // 背景颜色

    private Paint mProgressPaint, mRoundPaint;
    private int mWidth, mHeight;
    private Bitmap mBitmap;
    private int mBitmapWidth, mBitmapHeight;
    private float mBitmapRate = 1.0f;

    private RectF mRectFBg, mRectFFg, mRectRound;

    private ValueAnimator mValueAnimator;
    private int mAnimOffset;
    private long mAnimDuration = 1000L;

    public HorizontalProgressBarWithAnim(Context context) {
        this(context, null);
    }

    public HorizontalProgressBarWithAnim(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.progressBarStyleHorizontal);//android.R.style.Widget_ProgressBar_Horizontal
    }

    public HorizontalProgressBarWithAnim(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttributes(attrs);
        init();
    }

    private void obtainStyledAttributes(AttributeSet attrs) {
        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.HorizontalProgressBarWithAnim);
        try {
            mBackgroundColor = attributes.getColor(R.styleable.HorizontalProgressBarWithAnim_progress_background_color,
                    Color.GRAY);
            int rid = attributes.getResourceId(R.styleable.HorizontalProgressBarWithAnim_progress_src, R.drawable.progrssbar);
            mBitmap = drawableToBitmap(getResources().getDrawable(rid));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            attributes.recycle();
        }
    }

    private void init() {
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mRoundPaint = new Paint();
        mRoundPaint.setAntiAlias(true);
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        mRectFBg = new RectF();
        mRectFFg = new RectF();
        mRectRound = new RectF();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size = 0;
        if (heightMode == MeasureSpec.EXACTLY) {
            size = heightSize;
        } else {
            size = getPaddingTop() + getPaddingBottom() + mBitmap.getHeight();
            if (heightMode == MeasureSpec.AT_MOST) {
                size = Math.min(size, heightSize);
            }
        }
        setMeasuredDimension(widthSize, size);

        mWidth = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();
        mHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

        // 按比例拉伸图片，防止变形(如不需要等比拉伸，请注释下面代码)
        mBitmapRate = mHeight * 1.0f / mBitmap.getHeight();
        mBitmapWidth = (int) (mBitmapRate * mBitmap.getWidth());
        mBitmapHeight = mHeight;
        // 等比拉伸 END

        mRectFBg.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        mProgressPaint.setStyle(Paint.Style.FILL);//充满
        mProgressPaint.setColor(mBackgroundColor);
        canvas.drawRoundRect(mRectFBg, mRectFBg.height() / 2, mRectFBg.height() / 2, mProgressPaint); // 背景， 第二个参数是x半径，第三个参数是y半径
        int progressWidth = (int) ((getProgress() * 1.0f / getMax()) * mWidth);

        if (progressWidth > 0) {
            mRectFFg.set(getPaddingLeft(), getPaddingTop(), progressWidth + getPaddingLeft(), mHeight + getPaddingTop());
            canvas.drawBitmap(createRoundConerImage(spliceImage(progressWidth)), null, mRectFFg, mProgressPaint);
        }
    }


    /**
     * 拼接图片
     *
     * @return
     */
    private Bitmap spliceImage(int progressWidth) {
        int dw = progressWidth + mAnimOffset;
        Bitmap target = Bitmap.createBitmap(progressWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rectF = new RectF();
        while (dw >= mBitmapWidth) {
            rectF.set(progressWidth - dw, 0, progressWidth - dw + mBitmapWidth, mHeight);
            canvas.drawBitmap(mBitmap, null, rectF, mProgressPaint);
            dw = dw - mBitmapWidth;
        }
        if (dw > 0) {
            rectF.set(progressWidth - dw, 0, progressWidth, mHeight);
            int cropW = (int) (dw / mBitmapRate);
            if (cropW > 0) {
                canvas.drawBitmap(ImageCrop(mBitmap, cropW), null, rectF, mProgressPaint);
            }
        }
        return target;
    }

    /**
     * 裁切图片
     */
    private Bitmap ImageCrop(Bitmap bitmap, int cropWidth) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        cropWidth = cropWidth > w ? w : cropWidth;

        //下面这句是关键
        return Bitmap.createBitmap(bitmap, 0, 0, cropWidth, h, null, false);
    }

    /**
     * 根据原图添加圆角
     *
     * @param source
     * @return
     */
    private Bitmap createRoundConerImage(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        mRectRound.set(0, 0, width, height);
        if (width < height) {
            int r = (int) Math.sqrt(height * width / 2.0f - width * width / 4.0f);
            mRectRound.set(0, height / 2 - r, width, height / 2 + r);
            canvas.drawOval(mRectRound, mRoundPaint);
        } else {
            canvas.drawRoundRect(mRectRound, height / 2, height / 2, mRoundPaint);
        }
        mRoundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, mRoundPaint);
        mRoundPaint.setXfermode(null);
        return target;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean isRunning() {
        return mValueAnimator != null && mValueAnimator.isRunning();
    }

    public void startAnim(long duration) {
        mAnimDuration = duration;
        startAnim();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void startAnim() {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.end();
        }
        mValueAnimator = ValueAnimator.ofInt(mBitmapWidth, 0);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        //设置重复模式
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        mValueAnimator.setDuration(mAnimDuration);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimOffset = (int) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        mValueAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void stopAnim() {
        if (mValueAnimator == null || !mValueAnimator.isRunning()) {
            return;
        }
        mValueAnimator.end();
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        } else if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
