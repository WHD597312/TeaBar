package com.peihou.teabar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {
    private Paint paint;//定义一个画笔

    private float ring_width;//圆环宽度
    private int ring_color;//圆环颜色
    public CircleView(Context context) {
        this(context,null);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs,defStyleAttr);
        initPaint();
    }
    private void initPaint(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        给画笔设置颜色
        paint.setColor(ring_color);
//        设置画笔属性
//        paint.setStyle(Paint.Style.FILL);//画笔属性是实心圆
        paint.setStyle(Paint.Style.FILL_AND_STROKE);//画笔属性是空心圆
        paint.setStrokeWidth(ring_width);//设置画笔粗细
        paint.setAntiAlias(true);
    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a=getContext().obtainStyledAttributes(attrs,R.styleable.CircleView,defStyle,0);
        ring_width=a.getDimension(R.styleable.CircleView_ring_width,getDimen(R.dimen.dp_2));
        ring_color=a.getColor(R.styleable.CircleView_ring_color,getResources().getColor(R.color.color_white));
        a.recycle();
    }
    private float getDimen(int dimenId) {
        return getResources().getDimension(dimenId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width=getWidth();
        int height=getHeight();
        int x0=width/2;
        int y0=height/2;
        float r = (width)/2;
        paint.setStyle(Paint.Style.STROKE);//画笔属性是空心圆
        paint.setColor(Color.parseColor("#eeeeee"));
        canvas.drawCircle(x0,y0,r,paint);
        canvas.save();
        r=(width)/2-ring_width/2;
        paint.setStyle(Paint.Style.FILL);//画笔属性是空心圆
        paint.setColor(ring_color);
        canvas.drawCircle(x0,y0,r,paint);

    }

    public void setRing_color(int ring_color) {
        this.ring_color = ring_color;
        invalidate();
    }

    public int getRing_color() {
        return ring_color;
    }
}
