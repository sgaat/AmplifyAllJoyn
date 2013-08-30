package org.alljoyn.bus.sample.chat;

import java.io.IOException;
import java.util.zip.DataFormatException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Scribbler extends Activity implements Observer {
	private static final String TAG = "chat.Scribbler";
    DrawView drawView;
    
    private ChatApplication mChatApplication = null;
    
    //private Button orangeButton,redButton,greenButton,blueButton,
    private Button blueButton, clearPageButton, redButton;
    private SeekBar brushSlider, brushBoardSlider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mChatApplication = (ChatApplication) getApplication();
        mChatApplication.checkin();
        mChatApplication.addObserver(this);
        
        setContentView(R.layout.draw_view);
        
        drawView = (DrawView) findViewById(R.id.drawView);
        drawView.setChatApplication(mChatApplication);
        drawView.setBackgroundColor(Color.WHITE);
        drawView.requestFocus();
        
        
        blueButton = (Button) findViewById(R.id.blueButton);
        redButton = (Button) findViewById(R.id.redButton);
        
        brushSlider = (SeekBar) findViewById(R.id.brushSlider);
        brushSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				long actualColor = (long) (4278190080l | (long)(arg1));
				Log.e("COLOR : >> ", actualColor + " " + arg1 + " " + (int) actualColor);
				drawView.setPaintColor((int)actualColor);
				blueButton.setBackgroundColor((int)actualColor);
			}
		});
        
        brushBoardSlider = (SeekBar) findViewById(R.id.brushBoardSlider);
        brushBoardSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				if ( !isRemote ){
					long actualColor = (long) (0xff000000l | (long)(arg1));
					Log.e("COLOR : >> ", actualColor + " " + arg1 + " " + (int)actualColor);
					drawView.setBackgroundColor((int)actualColor);
					
					String coordinates = "=drawColor:" + actualColor + ":progressValue:"+arg1; 
					MessageObject mo = new MessageObject();
					mo.messageType = 3;
					mo.color= actualColor;
					mo.pointerPosition = arg1;
		            mChatApplication.newLocalUserMessage(mo);
		            redButton.setBackgroundColor((int)actualColor);
				}
				isRemote = false;
			}
		});
        
        
        clearPageButton= (Button) findViewById(R.id.clearButton);
        clearPageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				drawView.clearCanvas();
			}
		});
    }
    
    private boolean isRemote = false;

	@Override
	public void update(Observable o, Object arg) {
		  Log.i(TAG, "update(" + arg + ")");
		  String qualifier = (String)arg;
		  
		  if (qualifier.equals(ChatApplication.HISTORY_CHANGED_EVENT)) {
			  final MessageObject message = mChatApplication.getHistory().get(mChatApplication.getHistory().size()-1);
		        //for (String message : messages) {
			  if ( !message.isSelf ){  	
				  //if ( message.split("=").length == 2){
		        		
				            //String[] array = message.split("=")[1].split(":");
				        	if ( message.messageType == 1 ){
				        		Point point = new Point();
				        		point.x = message.xCord;
				        		point.y = message.yCord;
				        		point.oldX = message.xOldCord;
				        		point.oldY = message.yOldCord;
				        		point.colorCode = (int) message.color;
				        		drawView.drawPoints(point);
				        	}
				        	else if ( message.messageType == 2 ){
				        		Handler handle = new Handler(getMainLooper());
				        		handle.post(new Runnable() {
									
									@Override
									public void run() {
										drawView.clearCanvasFromRemote();
									}
								});
				        	}
				        	else if ( message.messageType == 4 ){
				        		Handler handle = new Handler(getMainLooper());
				        		handle.post(new Runnable() {
									
									@Override
									public void run() {
										try {
											String decompressedString = DrawView.decompress(message.message);
											String[] array = decompressedString.split(":");
											int i = 0;
											for ( int j = 0 ; j < array.length ; ){
												Point point = new Point();
								        		point.x = Float.parseFloat(array[j++]);
								        		point.y = Float.parseFloat(array[j++]);;
								        		point.oldX = Float.parseFloat(array[j++]);;
								        		point.oldY = Float.parseFloat(array[j++]);;
								        		point.colorCode = (int) Float.parseFloat(array[j++]);;
								        		drawView.drawPoints(point);
								        		i++;
								        		i = i %5;
											}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (DataFormatException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
				        	}
				        	else if ( message.messageType == 3 ){
				        		final long actualColor = message.color;
				        		final int progressValue = message.pointerPosition;
				        		Handler handle = new Handler(getMainLooper());
				        		handle.post(new Runnable() {
									
									@Override
									public void run() {
										drawView.setBackgroundColor((int)actualColor);
										brushBoardSlider.setProgress(progressValue);
										redButton.setBackgroundColor((int)actualColor);
										isRemote = true;
									}
								});
				        		
				        	}
		        		}
		        	//}
		        }		
		  //}
		
	}
}
