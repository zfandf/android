package cn.m15.app.android.gotransfer.ui.widget.animation;

import cn.m15.gotransfer.sdk.SdkConst;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;

/**
 * @author shaohx
 * 
 */
public class Reciver {
	// Reciver图片资源(头像)
	public Bitmap bmpEnemy;
	// 接收者坐标坐标
	public int x, y;
	// 是否被点击
	public Boolean isClick = false;
	// 中心点坐标
	private int centerx;
	private int centery;
	public boolean flag = false;// 标识位
	private String name; 
	private String decodename;
	private String Ip;//ip用于取被点击的对象
	public String getIp() {
		return Ip;
	}

	public void setIp(String ip) {
		Ip = ip;
	}

	public String getDecodename() {
		return decodename;
	}

	public void setDecodename(String decodename) {
		this.decodename = decodename;
	}

	public Reciver(Bitmap bmpEnemy, String name, int x, int y) {
		this.bmpEnemy = bmpEnemy;
		this.name = name;
		this.x = x;
		this.y = y;
	}

	public Bitmap getBmpEnemy() {
		return bmpEnemy;
	}

	public void setBmpEnemy(Bitmap bmpEnemy) {
		this.bmpEnemy = bmpEnemy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Boolean getIsClick() {
		return isClick;
	}

	public void setIsClick(Boolean isClick) {
		this.isClick = isClick;
	}

	// reciver绘图函数
	public void draw(Canvas canvas, TextPaint paint) {
		canvas.save();
		if (flag) {
			canvas.drawBitmap(bmpEnemy, x, y, paint);
			StaticLayout staticLayout=new StaticLayout(name, paint, bmpEnemy.getHeight()*2, Alignment.ALIGN_CENTER, 1f, 0f, false);
			canvas.translate(x-bmpEnemy.getHeight()/2, y+bmpEnemy.getHeight());
			staticLayout.draw(canvas);
		} else {
			canvas.drawBitmap(bmpEnemy, x, y, paint);

			StaticLayout staticLayout=new StaticLayout(name, paint, bmpEnemy.getHeight()*2, Alignment.ALIGN_CENTER, 1f, 0f, false);
			
			canvas.translate(x-bmpEnemy.getHeight()/2, y+bmpEnemy.getHeight());
			staticLayout.draw(canvas);
		}
		canvas.restore();
	}

	// 判断点击
	public boolean isClick(int tx, int ty) {
		centerx = x + bmpEnemy.getWidth() / 2;
		centery = y + bmpEnemy.getHeight() / 2;
		int l = bmpEnemy.getWidth() / 2 * bmpEnemy.getWidth() / 2
				+ bmpEnemy.getHeight() / 2 * bmpEnemy.getHeight() / 2;
		int d = (tx - centerx) * (tx - centerx) + (ty - centery)
				* (ty - centery);
		if (d < l) {
			isClick = true;
			return isClick;
		}
		isClick = false;
		return isClick;
	}

	public boolean getFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

}
