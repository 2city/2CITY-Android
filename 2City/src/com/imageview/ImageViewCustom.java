package com.imageview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ImageViewCustom extends ImageView {
	public ImageViewCustom(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public ImageViewCustom(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public ImageViewCustom(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setImageDrawable(Drawable drawable) {
		// TODO Auto-generated method stub
		super.setImageDrawable(drawable);
		
	}
	
	@Override 
	  protected void onMeasure(int widthMeasureSpec,
	       int heightMeasureSpec) {
	    try {
	    	 int width = MeasureSpec.getSize(widthMeasureSpec);
		     Drawable mDrawable = getDrawable();
		     if(mDrawable != null)
		     {
		      int height = width * mDrawable.getIntrinsicHeight() / mDrawable.getIntrinsicWidth();
		      setMeasuredDimension(width, height);
		     }
		     else
		      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} catch (Exception e) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	   }
	

}
