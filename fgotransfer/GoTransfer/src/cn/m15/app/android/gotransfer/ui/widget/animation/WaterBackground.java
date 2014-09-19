package cn.m15.app.android.gotransfer.ui.widget.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
/**
 * 
 * @author shaohx
 *	封装背景的对象
 */
public class WaterBackground {
	//背景图片资源为了循环播放定义两个对象，引用同一图片
	private int width,height;//屏幕宽高
	private Bitmap bmpBackGround1;
	private Bitmap bmpBackGround2;
	//背景坐标
	private int bg1x, bg1y, bg2x, bg2y;
	//背景滚动速度
	private int speed =2;//每次x坐标+speed；speed越大速度越快

	//背景构造函数
	public WaterBackground(Bitmap bmpBackGround,int width,int height) {
		this.bmpBackGround1 = bmpBackGround;
		this.bmpBackGround2 = bmpBackGround;
		this.width=width;this.height=height;
		//首先让第一张背景右边正好填满整个屏幕
		//bg1x = bmpBackGround1.getWidth() - width;
		bg1x = bmpBackGround1.getWidth() + width;
		bg1y=height/2-bmpBackGround1.getHeight()*5/4;
		//第二张背景图紧接在第一张背景的左方
		//bg2x = bg1x - bmpBackGround1.getWidth();
		bg2x = bg1x + bmpBackGround1.getWidth();
		bg2y=height/2-bmpBackGround2.getHeight()*5/4;
	}
	//背景的绘图函数
	public void draw(Canvas canvas, TextPaint paint) {
		//绘制两张背景
		canvas.drawBitmap(bmpBackGround1, bg1x, bg1y, paint);
		canvas.drawBitmap(bmpBackGround2, bg2x, bg2y, paint);
	}
	//背景的逻辑函数
	public void logic() {
		bg1x += speed;
		bg2x += speed;
		//当第一张图片的x坐标超出屏幕，
		//立即将其坐标设置到第二张图的左侧
		if (bg1x > width) {
			bg1x = bg2x - bmpBackGround1.getWidth();
		}
		//当第二张图片的Y坐标超出屏幕，
		//立即将其坐标设置到第一张图的上方
		if (bg2x > width) {
			bg2x = bg1x - bmpBackGround1.getWidth();
		}
	}
}
