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
 private void load(){changed=SystemClock.uptimeMillis();bitmap=read("characters/"+modes[mode]+"/"+emotions[emotion]+".png");if(bitmap==null)bitmap=read("characters/"+modes[mode]+"/calm.png");setContentDescription(modes[mode]+" "+emotions[emotion]);invalidate();}
 @Override protected void onDetachedFromWindow(){handler.removeCallbacks(resetEmotion);super.onDetachedFromWindow();}
 @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)invalidate();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);if(bitmap!=null){float s=Math.min(getWidth()/(float)bitmap.getWidth(),(getHeight()*.94f)/bitmap.getHeight());float w=bitmap.getWidth()*s,h=bitmap.getHeight()*s,x=(getWidth()-w)/2,y=(getHeight()-h)/2+(float)Math.sin(SystemClock.uptimeMillis()/650.0)*getHeight()*.015f;float progress=Math.min(1f,(SystemClock.uptimeMillis()-changed)/220f);float pulse=1f+(1f-progress)*0.04f+(speaking?(float)Math.sin(SystemClock.uptimeMillis()/100.0)*0.018f:0f); c.save();c.scale(pulse,pulse,getWidth()/2f,getHeight()/2f);paint.setAlpha((int)(180+75*progress));destination.set(x,y,x+w,y+h);c.drawBitmap(bitmap,null,destination,paint);paint.setAlpha(255);c.restore();if(isShown()&&hasWindowFocus())postInvalidateDelayed(speaking||progress<1f?33:50);}else{paint.setColor(0xffa9b9d4);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);c.drawText(modes[mode]+" • Artwork pending",getWidth()/2f,getHeight()/2f,paint);}}
}
