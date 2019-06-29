package com.example.ChenFengWeather.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.example.ChenFengWeather.R;

public class BrokenLineView extends View {
    private String price1,price2,price3,price4,price5,price6,price7;
    private String maxPrice,minPrice;
    private int wide=0,high=0;

    public BrokenLineView(Context context) {
        super(context);
    }

    public BrokenLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrokenLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**七个价格坐标点*/
    public void setSevenPrice(String price1,String price2,String price3,String price4,String price5,String price6,String price7){
        this.price1=price1;
        this.price2=price2;
        this.price3=price3;
        this.price4=price4;
        this.price5=price5;
        this.price6=price6;
        this.price7=price7;
    }

    /**Y轴最大值最小值*/
    public void setMaxMinPrice(String maxPrice,String minPrice){
        this.maxPrice=maxPrice;
        this.minPrice=minPrice;
    }

    /**获得宽高*/
    public void setWideHigh(int wide,int high){
        this.wide=wide;
        this.high=high;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();
        p.setStrokeWidth((float) 2.0);
        p.setAntiAlias(true);// 设置画笔的锯齿效果
        p.setColor(getResources().getColor(R.color.blue));

        if(wide>0){

            if(time<=5&&time>0){

                double s_y = getRoundY(price1);
                double e_y = getRoundY(price2);
                double a=(s_y-e_y)/5;
                double b=s_y-a*time;
                canvas.drawLine(0,(float) getRoundY(price1), wide/30*time,(float)  b, p);

            }else if(time>5&&time<=10){

                double s_y = getRoundY(price2);
                double e_y = getRoundY(price3);
                double a=(s_y-e_y)/5;
                double b=s_y-a*(time-5);

                canvas.drawLine(0,(float) getRoundY(price1), wide/6, (float) getRoundY(price2), p);
                canvas.drawLine(wide/6,  (float) getRoundY(price2), wide/6+(wide/6)*(time-5)/5,   (float) b, p);
            }else if(time>10&&time<=15){

                double s_y = getRoundY(price3);
                double e_y = getRoundY(price4);
                double a=(s_y-e_y)/5;
                double b=s_y-a*(time-10);

                canvas.drawLine(0,(float) getRoundY(price1), wide/6, (float) getRoundY(price2), p);
                canvas.drawLine(wide/6,  (float) getRoundY(price2), wide/3,   (float) getRoundY(price3), p);
                canvas.drawLine(wide/3,  (float) getRoundY(price3), wide/3+(wide/6)*(time-10)/5,   (float) b,  p);
            }else if(time>15&&time<=20){

                double s_y = getRoundY(price4);
                double e_y = getRoundY(price5);
                double a=(s_y-e_y)/5;
                double b=s_y-a*(time-15);

                canvas.drawLine(0,(float) getRoundY(price1), wide/6, (float) getRoundY(price2), p);
                canvas.drawLine(wide/6,  (float) getRoundY(price2), wide/3,   (float) getRoundY(price3), p);
                canvas.drawLine(wide/3,  (float) getRoundY(price3), wide/2,   (float) getRoundY(price4),  p);
                canvas.drawLine(wide/2,  (float) getRoundY(price4), wide/2+(wide/6)*(time-15)/5, (float) b,  p);
            }else if(time>20&&time<=25){

                double s_y = getRoundY(price5);
                double e_y = getRoundY(price6);
                double a=(s_y-e_y)/5;
                double b=s_y-a*(time-20);

                canvas.drawLine(0,(float) getRoundY(price1), wide/6, (float) getRoundY(price2), p);
                canvas.drawLine(wide/6,  (float) getRoundY(price2), wide/3,   (float) getRoundY(price3), p);
                canvas.drawLine(wide/3,  (float) getRoundY(price3), wide/2,   (float) getRoundY(price4),  p);
                canvas.drawLine(wide/2,  (float) getRoundY(price4), wide/6*4, (float) getRoundY(price5),  p);
                canvas.drawLine(wide/6*4,(float) getRoundY(price5), wide/6*4+(wide/6)*(time-20)/5, (float) b,  p);
            }else if(time>25){

                double s_y = getRoundY(price6);
                double e_y = getRoundY(price7);
                double a=(s_y-e_y)/5;
                double b=s_y-a*(time-25);

                canvas.drawLine(0,(float) getRoundY(price1), wide/6, (float) getRoundY(price2), p);
                canvas.drawLine(wide/6,  (float) getRoundY(price2), wide/3,   (float) getRoundY(price3), p);
                canvas.drawLine(wide/3,  (float) getRoundY(price3), wide/2,   (float) getRoundY(price4),  p);
                canvas.drawLine(wide/2,  (float) getRoundY(price4), wide/6*4, (float) getRoundY(price5),  p);
                canvas.drawLine(wide/6*4,(float) getRoundY(price5), wide/6*5, (float) getRoundY(price6),  p);
                canvas.drawLine(wide/6*5,(float) getRoundY(price6), wide/6*5+(wide/6)*(time-25)/5,     (float) b,  p);
            }

//            canvas.drawLine(0,(float) getRoundY(price1), wide/6, (float) getRoundY(price2), p);
//            canvas.drawLine(wide/6,  (float) getRoundY(price2), wide/3,   (float) getRoundY(price3), p);
//            canvas.drawLine(wide/3,  (float) getRoundY(price3), wide/2,   (float) getRoundY(price4),  p);
//            canvas.drawLine(wide/2,  (float) getRoundY(price4), wide/6*4, (float) getRoundY(price5),  p);
//            canvas.drawLine(wide/6*4,(float) getRoundY(price5), wide/6*5, (float) getRoundY(price6),  p);
//            canvas.drawLine(wide/6*5,(float) getRoundY(price6), wide,     (float) getRoundY(price7),  p);


        }
    }

    //获得Y轴坐标点
    private double getRoundY(String str){
        double d_max = Double.parseDouble(maxPrice);
        double d_min = Double.parseDouble(minPrice);

        //return ((double) high)-((Double.parseDouble(str)-d_min)/(d_max-d_min))*high;
        return ((Double.parseDouble(str)-d_min)/(d_max-d_min))*high;
    }


    //动画异步任务
    private int time=0;
    public class task extends AsyncTask {

        private Handler handler;
        public task(Handler handler){
            this.handler=handler;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            for(int i=1;i<31;i++){
                try {
                    time=i;
                    handler.sendEmptyMessage(1);
                    Thread.sleep((long) (600/30));
                }catch (Exception p){
                    p.printStackTrace();
                }
            }
            handler.sendEmptyMessage(2);
            return null;
        }
    }

    //接受消息刷新界面
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    invalidate();
                    break;
                case 2:
                    if(a!=null){
                        a.cancel(true);
                        a=null;
                    }
                    break;
            }
        }
    };

    private AsyncTask a;

    //开始动画
    public void start(){
        a=new task(handler);
        a.execute();
    }

    //手动结束动画
    public void stop(){
        if(a!=null){
            a.cancel(true);
            a=null;
        }
        time=0;
        invalidate();
    }

}
