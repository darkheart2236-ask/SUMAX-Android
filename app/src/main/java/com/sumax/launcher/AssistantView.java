package com.sumax.launcher;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
public class AssistantView extends View {
 private final Paint p=new Paint(3);
 private int emotion=0;
 private final String[] names={"Calm","Happy","Focused","Surprised","Concerned","Sleepy"};
 public AssistantView(Context c){super(c);setContentDescription("SUMAX assistant, tap to change expression");setOnClickListener(v->setEmotion((emotion+1)%names.length));}
 public void setEmotion(int e){emotion=Math.max(0,Math.min(e,names.length-1));setContentDescription("SUMAX assistant: "+names[emotion]);invalidate();}
 private void oval(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawOval(l,t,r,b,p);}
 private void shape(Canvas c,int color,float... xy){Path a=new Path();a.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)a.lineTo(xy[i],xy[i+1]);a.close();p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawPath(a,p);}
 protected void onDraw(Canvas c){super.onDraw(c);long t=SystemClock.uptimeMillis();float s=Math.min(getWidth()/240f,getHeight()/250f);c.save();c.translate(getWidth()/2f,getHeight()/2f+(float)Math.sin(t/650.0)*3);c.scale(s,s);c.translate(-120,-125);
 int hair=0xff28365b,skin=0xffffd9ca,ink=0xff17203c,accent=0xff60eaff;
 shape(c,hair,45,105,30,185,70,170,170,170,210,185,195,100,175,35,120,12,65,35);
 shape(c,ink,25,238,42,194,90,174,150,174,198,194,215,238);
 shape(c,accent,42,200,90,181,120,222,150,181,198,200,160,192,120,240,80,192);
 oval(c,skin,104,145,136,190);oval(c,skin,58,42,182,170);
 shape(c,hair,48,106,52,48,90,22,145,20,190,53,187,113,162,67,140,95,127,55,96,100,100,58,70,110);
 boolean blink=t%4200<130||emotion==5;
 for(int i=0;i<2;i++){float x=i==0?91:149; if(blink||emotion==1){p.setColor(ink);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawArc(x-15,105,x+15,120,190,160,false,p);}else{oval(c,ink,x-15,100,x+15,127);oval(c,accent,x-9,103,x+9,125);oval(c,ink,x-4,106,x+4,122);oval(c,-1,x-6,104,x-1,110);} }
 p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(ink);
 if(emotion==2||emotion==4){c.drawLine(77,95,104,emotion==2?101:90,p);c.drawLine(136,emotion==2?101:90,163,95,p);}
 if(emotion==3)oval(c,ink,114,139,126,153);else if(emotion==1||emotion==0)c.drawArc(106,132,134,149,10,160,false,p);else if(emotion==4)c.drawArc(109,143,131,154,190,160,false,p);else c.drawLine(112,144,128,144,p);
 if(emotion==1){oval(c,0xffefa6aa,65,128,82,135);oval(c,0xffefa6aa,158,128,175,135);}
 c.restore();if(isShown())postInvalidateDelayed(33);}
}
