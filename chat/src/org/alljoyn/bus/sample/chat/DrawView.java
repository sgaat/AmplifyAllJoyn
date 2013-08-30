package org.alljoyn.bus.sample.chat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawView extends View implements OnTouchListener {
    List<Point> points = new ArrayList<Point>();
    //List<Point> pointsRemote = new ArrayList<Point>();
    Paint paint = new Paint();
    ChatApplication mChatApplication = null;
    Context context;

    public DrawView(Context context, AttributeSet attrs) {
        super(context,attrs);
        this.context = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5.0f);
        
        
    }
    
    public void setChatApplication(ChatApplication chatApplication){
    	mChatApplication = chatApplication;
    }
    
    public void setPaintColor(int color){
    	paint.setColor(color);
    }
    
    public void clearCanvas(){
    	points.clear();
    	isRemote = false;
    	invalidate();
    	//pointsRemote.clear();
    	//isRemote = true;
    	invalidate();
    	oldX = -1.0f;
    	oldY = -1.0f;
    	
    	//if ( !isRemote ){
    		//String coordinates = "=x:-1:y:-1:oldX:-1:oldY:-1:color:-1";
    		MessageObject mo = new MessageObject();
    		mo.messageType = 2;
    		mChatApplication.newLocalUserMessage(mo);
        	
        //}
    }
    
    public void clearCanvasFromRemote(){
    	points.clear();
    	//pointsRemote.clear();
    	Handler handler = new Handler(context.getMainLooper());
    	handler.post(new Runnable() {
			
			@Override
			public void run() {
				invalidate();
			}
		});
    	
    }

    @Override
    public void onDraw(Canvas canvas) {
    	
    	String coordinates = "";  
    	//Point prevPoint = null;
    	List<Point> tempPoints = new ArrayList<Point>();
    	//if ( isRemote ){
    	//	tempPoints.addAll(pointsRemote);
    	//}else{
    		tempPoints.addAll(points) ;
    	//}
        for (Point point : tempPoints) {
        	paint.setColor(point.colorCode);
        	//Log.e(" ACTION >>>>> " , point.x + " " + point.y + " " + point.oldX + " " + point.oldY);
        	canvas.drawCircle(point.x, point.y, 2, paint);
            if ( point.oldX != -1.0f && point.oldY != -1.0f ){
            	canvas.drawLine(point.oldX, point.oldY,point.x, point.y, paint);
            }
            //prevPoint = point;
            //coordinates = "=x:" + String.valueOf(point.x) + ":y:" + String.valueOf(point.y)+":oldX:" + String.valueOf(point.oldX) + ":oldY:" + String.valueOf(point.oldY)+":color:"+point.colorCode;  
           
        }
    }
    
    
    boolean isRemote = false;
    
    public void drawPoints(Point point){
    	if ( point.x == -1.0f && point.y == -1.0f ){
    		clearCanvasFromRemote();
    	}else{
    		points.add(point);
    		isRemote = true;
    	}
    	
    	Handler handler = new Handler(context.getMainLooper());
    	handler.post(new Runnable() {
			
			@Override
			public void run() {
				invalidate();
			}
		});
                
    }
    
    @Override
    public void invalidate() {
        super.invalidate();
        
    }
    
    public static String compress(String stringToCompress) throws UnsupportedEncodingException
    {
        byte[] compressedData = new byte[1024];
        byte[] stringAsBytes = stringToCompress.getBytes("UTF-8");

        Deflater compressor = new Deflater();
        compressor.setInput(stringAsBytes);
        compressor.finish();
        int compressedDataLength = compressor.deflate(compressedData);

        byte[] bytes =  Arrays.copyOf(compressedData, compressedDataLength);
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    public static String decompress(String base64String) throws UnsupportedEncodingException, DataFormatException
    {   
    	byte[] compressedData = Base64.decode(base64String,Base64.DEFAULT);
    	
        Inflater deCompressor = new Inflater();
        deCompressor.setInput(compressedData, 0, compressedData.length);
        byte[] output = new byte[1024];
        int decompressedDataLength = deCompressor.inflate(output);
        deCompressor.end();

        return new String(output, 0, decompressedDataLength, "UTF-8");
    }

    private Point point = new Point();
    private float oldX=-1.0f, oldY=-1.0f;
    private List<Point> currentTouch = new ArrayList<Point>();
    public boolean onTouch(View view, MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN ){
    		oldX=-1.0f;
    		oldY=-1.0f;
    		point = new Point();
            point.x = event.getX();
            point.y = event.getY();
            point.oldX = oldX;
            point.oldY = oldY;
            point.colorCode = paint.getColor();
            //points.add(point);
            currentTouch = new ArrayList<Point>();
    	}else if ( event.getAction() == MotionEvent.ACTION_MOVE ){
    		
    		point = new Point();
	        point.x = event.getX();
	        point.y = event.getY();
	        point.oldX = oldX;
	        point.oldY = oldY;
	        point.colorCode = paint.getColor();
	        points.add(point);
	        currentTouch.add(point);
	        
	        oldX = point.x;
    		oldY = point.y;
	        isRemote = false;
	        
	        MessageObject mo = new MessageObject();
			mo.messageType = 1;
			mo.xCord = point.x;
			mo.yCord = point.y;
			mo.xOldCord = point.oldX;
			mo.yOldCord = point.oldY;
			mo.color = point.colorCode;
	        if ( !isRemote && !point.sent){
	        	
	        	mChatApplication.newLocalUserMessage(mo);
	        	point.sent = true;
	        }
	        
    	}
//    	else if ( event.getAction() == MotionEvent.ACTION_UP ){
//    		StringBuilder sb = new StringBuilder();
//    		boolean isFirst = true;
//    		for ( Point temp : currentTouch ){
//	        
//    			if ( isFirst ){
//    				isFirst = false;
//    				sb.append(temp.x+":"+temp.y+":"+temp.oldX+":"+temp.oldY+":"+temp.colorCode);
//    			}else{
//    				sb.append(":"+temp.x+":"+temp.y+":"+temp.oldX+":"+temp.oldY+":"+temp.colorCode);
//    			}
//    		}
//    		
//    		MessageObject mo = new MessageObject();
//    		mo.messageType = 4;
//    		try {
//				mo.message = compress(sb.toString());
//				mChatApplication.newLocalUserMessage(mo);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//	        
//    	}
    	invalidate();
        return true;
    }
}

