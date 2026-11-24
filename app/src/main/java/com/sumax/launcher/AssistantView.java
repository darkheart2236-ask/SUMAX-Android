package com.sumax.launcher;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.io.InputStream;
public class AssistantView extends View {
 private final Paint paint=new Paint(3);
 private final String[] modes={"hunter","shadow","guardian"};
 private final String[] emotions={"calm","happy","thinking","surprised","sad","angry","excited"};
 private int mode=1, emotion=0;
 private Bitmap bitmap;
 public AssistantView(Context c){super(c);setOnClickListener(v->setEmotion((emotion+1)%emotions.length));load();}
 public void setMode(int m){mode=Math.max(0,Math.min(m,2));emotion=0;load();}
 public void setEmotion(int e){emotion=Math.max(0,Math.min(e,emotions.length-1));load();}
 private Bitmap read(String path){try(InputStream in=getContext().getAssets().open(path)){return BitmapFactory.decodeStream(in);}catch(java.io.IOException e){return null;}}
 private void load(){bitmap=read("characters/"+modes[mode]+"/"+emotions[emotion]+".png");if(bitmap==null)bitmap=read("characters/"+modes[mode]+"/calm.png");setContentDescription(modes[mode]+" "+emotions[emotion]);invalidate();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);if(bitmap!=null){float s=Math.min(getWidth()/(float)bitmap.getWidth(),(getHeight()*.94f)/bitmap.getHeight());float w=bitmap.getWidth()*s,h=bitmap.getHeight()*s,x=(getWidth()-w)/2,y=(getHeight()-h)/2+(float)Math.sin(SystemClock.uptimeMillis()/650.0)*getHeight()*.015f;c.drawBitmap(bitmap,null,new RectF(x,y,x+w,y+h),paint);if(isShown())postInvalidateDelayed(33);}else{paint.setColor(0xffa9b9d4);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);c.drawText(modes[mode]+" • Artwork pending",getWidth()/2f,getHeight()/2f,paint);}}
}
