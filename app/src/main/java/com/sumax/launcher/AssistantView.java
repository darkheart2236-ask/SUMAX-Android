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
 private Movie movie;
 private final java.util.HashMap<String,Movie> movies=new java.util.HashMap<>();
 private final java.util.HashSet<String> missing=new java.util.HashSet<>();
 private final android.util.LruCache<String,Bitmap> cache=new android.util.LruCache<String,Bitmap>(12*1024*1024){@Override protected int sizeOf(String k,Bitmap b){return b.getByteCount();}};
 private final RectF destination=new RectF();
 private boolean speaking;
 private final android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
 private final Runnable resetEmotion=()->setEmotion(0);
 public void setSpeaking(boolean value){speaking=value;invalidate();}
 private long changed=SystemClock.uptimeMillis();
 public AssistantView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setOnClickListener(v->setEmotion((emotion+1)%emotions.length));load();}
 public void setMode(int m){handler.removeCallbacks(resetEmotion);mode=Math.max(0,Math.min(m,2));emotion=0;load();}
 public void setEmotion(int e){emotion=Math.max(0,Math.min(e,emotions.length-1));handler.removeCallbacks(resetEmotion);if(emotion!=0&&emotion!=2)handler.postDelayed(resetEmotion,4500);load();}
 private Bitmap read(String path){if(missing.contains(path))return null;Bitmap cached=cache.get(path);if(cached!=null)return cached;try(InputStream in=getContext().getAssets().open(path)){Bitmap b=BitmapFactory.decodeStream(in);if(b!=null)cache.put(path,b);else missing.add(path);return b;}catch(java.io.IOException e){missing.add(path);return null;}}
 private String assetPath(String name){return "characters/"+(name.equals("hunter")?"8484397f010ce4e937551569f6712341edd14375f2ed0a219465ca5a08e480b1":name.equals("shadow")?"326fca824b5a7f137a156a89982bd6f1432db4bd105dff438ed8592f1b9d4d02":"2e6e0d2b8745bc1065ce5a01815b9e8b08199c9a7d1982a54d2d1a9017ff1968")+".gif";}
 private android.graphics.drawable.Drawable animated;
 private Movie readMovie(String name){
 animated=null;
 if(android.os.Build.VERSION.SDK_INT>=28){try{
 animated=android.graphics.ImageDecoder.decodeDrawable(android.graphics.ImageDecoder.createSource(getContext().getAssets(),assetPath(name)));
 if(animated instanceof android.graphics.drawable.AnimatedImageDrawable){android.graphics.drawable.AnimatedImageDrawable a=(android.graphics.drawable.AnimatedImageDrawable)animated;a.setRepeatCount(-1);a.setCallback(this);a.start();}
 return null;
 }catch(Exception e){android.util.Log.e("SUMAX","ImageDecoder failed",e);}}
 try(InputStream in=getContext().getAssets().open(assetPath(name))){return Movie.decodeStream(in);}catch(Exception e){android.util.Log.e("SUMAX","GIF failed",e);return null;}
 }
 @Override protected boolean verifyDrawable(android.graphics.drawable.Drawable d){return d==animated||super.verifyDrawable(d);}
 private void load(){if(animated instanceof android.graphics.drawable.AnimatedImageDrawable)((android.graphics.drawable.AnimatedImageDrawable)animated).stop();movie=readMovie(modes[mode]);changed=SystemClock.uptimeMillis();bitmap=read("characters/"+modes[mode]+"/"+emotions[emotion]+".png");if(bitmap==null&&movie==null&&animated==null)bitmap=read("characters/"+modes[mode]+"/calm.png");setContentDescription(modes[mode]+" "+emotions[emotion]);invalidate();}
 @Override protected void onDetachedFromWindow(){handler.removeCallbacks(resetEmotion);if(animated instanceof android.graphics.drawable.AnimatedImageDrawable)((android.graphics.drawable.AnimatedImageDrawable)animated).stop();super.onDetachedFromWindow();}
 @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)invalidate();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);if(animated!=null&&(bitmap==null||emotion==0)){int iw=Math.max(1,animated.getIntrinsicWidth()),ih=Math.max(1,animated.getIntrinsicHeight());float s=Math.min(getWidth()/(float)iw,getHeight()*.60f/ih);int w=(int)(iw*s),h=(int)(ih*s),x=(getWidth()-w)/2,y=(int)(getHeight()*.04f);animated.setBounds(x,y,x+w,y+h);animated.draw(c);if(isShown()&&hasWindowFocus())postInvalidateDelayed(33);return;}if((bitmap==null||emotion==0)&&movie!=null&&movie.width()>0&&movie.height()>0){float scale=Math.min(getWidth()/(float)movie.width(),getHeight()*.60f/movie.height());float w=movie.width()*scale,h=movie.height()*scale;float x=(getWidth()-w)/2f,y=getHeight()*.04f;movie.setTime((int)((SystemClock.uptimeMillis()-changed)%Math.max(1,movie.duration())));double t=SystemClock.uptimeMillis()/1000.0;
 float breath=(float)Math.sin(t*2)*getHeight()*.008f;
 float pulse=1f+(speaking?(float)Math.sin(t*15)*.025f:0f)+(emotion==6?.035f:0f);
 float sway=emotion==5?(float)Math.sin(t*22)*3f:0f;
 int accent=new int[]{0xff55ddff,0xffb49aff,0xffffca70}[mode];
 c.save();c.translate(x+sway,y+breath);c.scale(scale*pulse,scale*pulse,w/2f,h/2f);movie.draw(c,0,0);c.restore();
 float cx=getWidth()/2f,cy=y+h+getHeight()*.035f;
 paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2f);paint.setColor(accent);
 int rings=emotion==2?3:speaking?2:1;
 for(int i=0;i<rings;i++){float phase=(float)((t*.65+i/(float)rings)%1);float radius=w*(.12f+phase*.32f);paint.setAlpha((int)((1-phase)*(speaking?220:120)));c.drawOval(cx-radius,cy-radius*.16f,cx+radius,cy+radius*.16f,paint);}
 paint.setAlpha(255);paint.setStyle(Paint.Style.FILL);if(isShown()&&hasWindowFocus())postInvalidateDelayed(33);return;}if(bitmap!=null){float s=Math.min(getWidth()/(float)bitmap.getWidth(),(getHeight()*.94f)/bitmap.getHeight());float w=bitmap.getWidth()*s,h=bitmap.getHeight()*s,x=(getWidth()-w)/2,y=(getHeight()-h)/2+(float)Math.sin(SystemClock.uptimeMillis()/650.0)*getHeight()*.015f;float progress=Math.min(1f,(SystemClock.uptimeMillis()-changed)/220f);float pulse=1f+(1f-progress)*0.04f+(speaking?(float)Math.sin(SystemClock.uptimeMillis()/100.0)*0.018f:0f); c.save();c.scale(pulse,pulse,getWidth()/2f,getHeight()/2f);paint.setAlpha((int)(180+75*progress));destination.set(x,y,x+w,y+h);c.drawBitmap(bitmap,null,destination,paint);paint.setAlpha(255);c.restore();if(isShown()&&hasWindowFocus())postInvalidateDelayed(speaking||progress<1f?33:50);}else{paint.setColor(0xffa9b9d4);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);c.drawText(modes[mode]+" • Artwork pending",getWidth()/2f,getHeight()/2f,paint);}}
}
