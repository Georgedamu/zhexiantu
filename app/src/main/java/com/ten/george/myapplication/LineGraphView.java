package com.creditwealth.client.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

import com.creditwealth.client.R;
import com.creditwealth.client.entities.FundIncome;
import com.creditwealth.common.util.UIUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：折线图
 * 类名称：LineGraphView
 *
 * @version: 1.0
 * @author: George
 * @time: 2016/5/30 10:37
 */
public class LineGraphView extends View {

    private List<String> xString = new ArrayList<String>();//x坐标字符
    private List<String> yString = new ArrayList<String>();//y坐标字符

    private float[] yFloat;//y轴坐标

    private int xPoint;//X轴坐标原点
    private int yPoint;//Y轴坐标原点

    private Context mContext;
    private int width;
    private int height;

    private int yCount = 3;//Y轴的数量,默认是3

    private int margin = 50;//预留空间写坐标默认五十

    private float yScale ;//单位间距
    private float yPer;//单位长度对应的y值
    private float yFirst;//第一个数轴代表的值
    private int yFirstLocation ;//第一个Y标志位的 坐标


    public LineGraphView(Context context) {
        super(context);
    }

    public LineGraphView(Context context,AttributeSet attrs){
        super(context, attrs);

    }

    public void init(Context context,List<FundIncome> dataMapList,int yCount){
        if (dataMapList != null && dataMapList.size() > 0) {
            xString.clear();
            yString.clear();
        }else {
            return;
        }
        for(int n =0;n<dataMapList.size();n++){
            xString.add(dataMapList.get(n).day);
            yString.add(dataMapList.get(n).dayIncome);
        }
        yFloat = new float[yString.size()];
        for(int i = 0;i<yString.size();i++){
            yFloat[i] = Float.parseFloat(yString.get(i));
        }
        this.yCount = yCount;
        mContext = context;
        margin = mContext.getResources().getDimensionPixelSize(R.dimen.size_7);
        invalidate();
    }

    public void init(Context context,List<FundIncome> dataMapList){
        this.init(context,dataMapList,3);
    }

    private void drawTable(Canvas canvas){

        //初始化x,y轴的原点坐标
        width = getWidth() - margin*3/2;//预留空间显示坐标值
        height = getHeight() -margin;
        xPoint = margin*3/2;
        yPoint = height;

        //初始化画笔
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);

        //初始化背景画笔
        Paint paintbg = new Paint();
        paintbg.setStyle(Paint.Style.FILL);
        paintbg.setColor(mContext.getResources().getColor(R.color.line_graph_bg));
        paintbg.setAntiAlias(true);

        //划竖线
        for (int i = 0; i < xString.size(); i++) {
            if (i % 2 == 1) {
                canvas.drawRect(xPoint + (width / xString.size()) * i, yPoint - height, xPoint + (width / xString.size()) * (i + 1), yPoint, paintbg);
            }
            canvas.drawLine(xPoint + (width / xString.size()) * i, yPoint, xPoint + (width / xString.size()) * i, yPoint - height, paint);
            paint.setTextSize(margin / 2);
            canvas.drawText(xString.get(i), xPoint + (width / xString.size()) * i - margin / 2, yPoint + margin / 2, paint);
        }

        //划横线
        for (int i = 0; i < yCount; i++) {
            canvas.drawLine(xPoint, yPoint - i * (height / yCount),xPoint + width - margin, yPoint - i * (height / yCount),paint);
        }
    }

    private void drawLine(Canvas canvas){

        yScale = (getMaxY(yFloat) - getMinY(yFloat))/(yCount-1);
        yPer = (height/yCount)/yScale;
        yFirst = getMinY(yFloat) + yScale/4;
        yFirstLocation = yPoint - height/yCount;

        //初始化折现画笔
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(margin /4);
        //设置颜色渐变
        float xLineStart = xPoint;
        float yLineStart = yFirstLocation - (yFloat[0] - yFirst)*yPer;
        float xLineStop = xPoint + (width/xString.size()) * (xString.size() -1) -margin/8;
        float yLineStop = yFirstLocation - (yFloat[xString.size()-1] - yFirst)*yPer+margin/8;
        LinearGradient linearGradient = new LinearGradient(xLineStart,yLineStart,xLineStop ,
                yLineStop,mContext.getResources().getColor(R.color.line_graph_begin),
                mContext.getResources().getColor(R.color.line_graph_end), Shader.TileMode.MIRROR);
        paint.setShader(linearGradient);

        //初始化文字画笔
        Paint paintText = new Paint();
        paintText.setStyle(Paint.Style.STROKE);
        paintText.setColor(Color.GRAY);
        paintText.setAntiAlias(true);

        //初始化白色画笔（相当于橡皮擦）
        Paint paintWhite = new Paint();
        paintWhite.setStyle(Paint.Style.FILL);
        paintWhite.setColor(Color.WHITE);
        paintWhite.setAntiAlias(true);

        for(int i = 0;i<xString.size()-1;i++){
            float xStart = xPoint + (width/xString.size()) * i;
            float xStop = xStart + width/xString.size();
            float yStart = yFirstLocation - (yFloat[i] - yFirst)*yPer;
            float yStop = yFirstLocation - (yFloat[i+1] - yFirst)*yPer;
            if(i==0){
                //先画线
                canvas.drawLine(xStart, yStart, xStop, yStop, paint);
                canvas.drawCircle(xStop, yStop, paint.getStrokeWidth() / 2, paint);
                //处理头部问题
                float r = paint.getStrokeWidth() / 2;//画笔宽度的1/2
                double h = Math.sqrt(Math.pow(xStop - xStart, 2) + Math.pow(yStart - yStop, 2));//用于计算正余弦的斜边长度
                float sin = (float) (Math.abs(yStart - yStop) / h);//正弦
                float cos = (float) (Math.abs(xStop - xStart) / h);//余弦
                Path pathAngle = new Path();//着色的三角
                Path pathAngle2 = new Path();//白色的三角
                if(yStart > yStop) {
                    //下面的三角形（）有色
                    pathAngle.moveTo(xStart, yStart);
                    pathAngle.lineTo(xStart + r * sin, yStart + r * cos);
                    pathAngle.lineTo(xStart, yStart + r / cos);
                    pathAngle.close();
                    canvas.drawPath(pathAngle, paint);
                    //上面的三角形（白色）
                    pathAngle2.moveTo(xStart, yStart);
                    pathAngle2.lineTo(xStart - r * sin, yStart - r * cos);
                    pathAngle2.lineTo(xStart, yStart - r / cos);
                    pathAngle2.close();
                    canvas.drawPath(pathAngle2, paintWhite);
                    //补表格
                    canvas.drawLine(xStart,yStart+r/cos,xStart,yStart-r/cos,paintText);
                }else if(yStart < yStop){
                    //上面面的三角形（有色）
                    pathAngle.moveTo(xStart, yStart);
                    pathAngle.lineTo(xStart + r * sin, yStart - r * cos);
                    pathAngle.lineTo(xStart, yStart - r / cos);
                    pathAngle.close();
                    canvas.drawPath(pathAngle, paint);
                    //下面的三角形（白色）
                    pathAngle2.moveTo(xStart, yStart);
                    pathAngle2.lineTo(xStart - r * sin, yStart + r * cos);
                    pathAngle2.lineTo(xStart, yStart + r / cos);
                    pathAngle2.close();
                    canvas.drawPath(pathAngle2, paintWhite);
                    //补表格
                    canvas.drawLine(xStart, yStart -r/cos, xStart, yStart + r / cos, paintText);
                }else {
                    canvas.drawLine(xStart, yStart-r, xStart, yStart+r, paintText);
                }
            } else if (i < xString.size() - 2) {
                canvas.drawLine(xStart, yStart, xStop, yStop, paint);
                canvas.drawCircle(xStop, yStop, paint.getStrokeWidth() / 2, paint);
            }else {
                if (yStart > yStop) {
                    canvas.drawLine(xStart, yStart, xStop - margin / 8, yStop + margin / 8, paint);
                } else if (yStart < yStop) {
                    canvas.drawLine(xStart, yStart, xStop - margin / 8, yStop - margin / 8, paint);
                } else {
                    canvas.drawLine(xStart, yStart, xStop - margin / 8, yStop, paint);
                }
                paint.setStrokeWidth(margin/8);//修改画笔粗细画圆圈
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(xStop, yStop, margin / 4-margin/16, paint);

                //画最后的数字和 背景
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(xStop - margin/2,yStop + margin*3/4,margin/4+margin/10,paint);
                canvas.drawRect(xStop - margin / 2, yStop + margin / 2-margin/10, xStop + margin, yStop + margin+margin/10, paint);
                canvas.drawCircle(xStop + margin,yStop + margin*3/4,margin/4+margin/10,paint);
                paintText.setColor(Color.WHITE);
                paintText.setTextSize(margin / 2);
                canvas.drawText(yString.get(i+1) + "", xStop - margin / 2, yStop + margin-margin/10, paintText);

            }
        }

        //绘制Y轴显示的坐标，保留三位小数
        DecimalFormat decimalFormat  =   new  DecimalFormat("##0.000");
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        for (int i = 0; i < yCount-1; i++) {
            paintText.setColor(Color.GRAY);
            paintText.setTextSize(margin / 2);
            canvas.drawText(decimalFormat.format(yFirst + i*yScale), xPoint - margin*3/2, yFirstLocation - i*yScale*yPer, paintText);
        }
    }

    /**
     * get the min y
     * @param y
     * @return
     */
    private float getMinY(float[] y){
        float temp = y[0];
        for(int i = 1;i<y.length;i++){
            if(y[i]<temp){
                temp = y[i];
            }
        }
        return temp;
    }

    /**
     * get the max y
     * @param y
     * @return
     */
    private float getMaxY(float[] y){
        float temp = y[0];
        for(int i = 1;i<y.length;i++){
            if(y[i]>temp){
                temp = y[i];
            }
        }
        return temp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTable(canvas);
        drawLine(canvas);
    }

}
