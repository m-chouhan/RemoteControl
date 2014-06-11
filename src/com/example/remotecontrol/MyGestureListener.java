package com.example.remotecontrol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.remotecontrol.Data.Actions;
import com.example.remotecontrol.Data.InputModes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/* MyGesture Listener listens to touch events
 * and sends messages to server
 */

class MyGestureListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener {

	int primary = 0,second = 0;
	float dx = 0,dy = 0;
    private static final String TAG = "Gestures"; 
    boolean doubletap = false;
    GestureDetectorCompat gd;
    Handler handler;
	private float secondx;
	private float secondy;
	private ByteBuffer buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
	private float prevdx;
	private float prevdy;
	private Actions currentAction;
	private long secondTap;
    @Override
	public boolean onDoubleTap(MotionEvent event) {
        Log.d(TAG,"onDoubleTap" );
        doubletap = true;
		int pindex = event.findPointerIndex(primary);
		dx = event.getX(pindex)-dx;dy = event.getY(pindex)-dy;
		buffer.clear();
		buffer.put(InputModes.TOUCH.getValue()); //For screen based mouse movements
		buffer.putFloat(dx);buffer.putFloat(dy);
		buffer.put(Actions.LEFT_HOLD.getValue()); 
		//Action. Refer to severside code for mouse actions (1 for left click & release)
		// 3 for left hold and 4 for left release

		dx = event.getX(pindex);dy = event.getY(pindex);
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return super.onDoubleTapEvent(e);
	}
	
	public void sendRawMessage(ByteBuffer buffer)
	{
		if(handler == null) return;
		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putByteArray("RAW", buffer.array());
		msg.setData(b);msg.what = Data.RAW;
		handler.sendMessage(msg);
	}

	@Override
	public boolean onDown(MotionEvent event) {
		// TODO Auto-generated method stub
        //Log.d(TAG,"onDown" ); 
		int pindex = event.findPointerIndex(primary);
		//dx = event.getX(pindex)-dx;dy = event.getY(pindex)-dy;
		return true;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		
		int pindex = event.findPointerIndex(primary);
		dx = event.getX(pindex)-dx;dy = event.getY(pindex)-dy;

		Log.d(TAG,"onSingleTapUp"+ Math.hypot(dx, dy) ); 

		if(Math.hypot(dx, dy) > 100 ) currentAction = Actions.RIGHT_HOLD;
		else currentAction = Actions.LEFT_HOLD;
		buffer.clear();
		buffer.put(InputModes.TOUCH.getValue());
		
		buffer.putFloat(dx);buffer.putFloat(dy);
		buffer.put(currentAction.getValue());
		sendRawMessage(buffer);
		dx = event.getX(pindex);dy = event.getY(pindex);
		return true;
	}

    @Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub	        	
    	Log.d(TAG,"singleConfirm" );
    	
    	if(currentAction == Actions.LEFT_HOLD) currentAction = Actions.LEFT_RELEASE;
    	else currentAction = Actions.RIGHT_RELEASE;
    	
    	buffer.clear();
		buffer.put(InputModes.TOUCH.getValue());
		buffer.putFloat(dx);buffer.putFloat(dy);
		buffer.put(currentAction.getValue());
		sendRawMessage(buffer);
		return true;
	}

	@Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, 
            float velocityX, float velocityY) {
        Log.d(TAG, "onFling: " );//+ event1.toString()+event2.toString());
        return true;
    }
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int pointerIndex = event.getActionIndex();
		int id = event.getPointerId(pointerIndex);
		gd.onTouchEvent(event);
		switch(MotionEventCompat.getActionMasked(event))
		{
			case MotionEvent.ACTION_DOWN: 	{
												primary = id;
												Log.d(TAG,"ACTION_DOWN"+event.getDownTime()+"||"+(event.getEventTime()-event.getDownTime()));
												dx = event.getX();dy = event.getY();
												buffer.clear();
												buffer.put(InputModes.ABSOLUTE_MOUSE.getValue());
												buffer.putFloat(dx);buffer.putFloat(dy);
												buffer.put(Actions.MOVE.getValue());
												//sendRawMessage(buffer);
											}
											break;
											
			case MotionEvent.ACTION_UP:		Log.d(TAG,"ACTION_UP"+event.getDownTime()+"||"+(event.getEventTime()-event.getDownTime()));
											if(doubletap){
												long timeelasped = (event.getEventTime()-event.getDownTime());
												buffer.clear();
												buffer.put(InputModes.TOUCH.getValue()); 
												//For screen based mouse movements, 0 for keyboard ,1 for accmetr bsd movmnts
												buffer.putFloat(dx);buffer.putFloat(dy);
												buffer.put(Actions.LEFT_RELEASE.getValue()); 
												//Action. Refer to severside code for mouse actions (1 for left click & release)
												// 3 for left hold and 4 for left release
												sendRawMessage(buffer);
												if(timeelasped < 400)
												{
													buffer.clear();
													buffer.put(InputModes.TOUCH.getValue()); 
													//For screen based mouse movements, 0 for keyboard ,1 for accmetr bsd movmnts
													buffer.putFloat(dx);buffer.putFloat(dy);
													buffer.put(Actions.LEFT_SINGLE.getValue()); 
													sendRawMessage(buffer);
												}
												doubletap = false;}
											break;
			case MotionEvent.ACTION_POINTER_DOWN:
											second = id;
											Log.d(TAG,"ACTION_POINTER_DOWN"+id);
											pointerIndex = event.findPointerIndex(second);
											secondTap = event.getEventTime();
											secondx = event.getX(pointerIndex);
											secondy = event.getY(pointerIndex);
											
											break;
			case MotionEvent.ACTION_POINTER_UP: 											
											Log.d(TAG,"ACTION_POINTER_UP"+id);
											if(id == primary)
											{	
												Log.d(TAG,"PRIMARY");
												primary = second;
												int pindex = event.findPointerIndex(primary);
												dx = event.getX(pindex);dy = event.getY(pindex);
											}
											else
											{
												secondTap = event.getEventTime()-secondTap;
												Log.d(TAG,"SECONDRY:"+secondTap);
												if(secondTap <100)
												{
													buffer.clear();
													buffer.put(InputModes.TOUCH.getValue()); 
													//For screen based mouse movements, 0 for keyboard ,1 for accmetr bsd movmnts
													buffer.putFloat(dx);buffer.putFloat(dy);
													buffer.put(Actions.RIGHT_SINGLE.getValue()); 
													sendRawMessage(buffer);
												}
											}
											break;		
			case MotionEvent.ACTION_MOVE : 
											int pindex = event.findPointerIndex(primary);
											if(dx == event.getX(pindex) && dy == event.getY(pindex) 
													&& event.getPointerCount() > 1)
											{
												int sindex = event.findPointerIndex(second);

												Log.d("move","SECONDRY_MOVED");
												secondx = event.getX(sindex)-secondx;
												secondy = event.getY(sindex)-secondy;
												buffer.clear();
												buffer.put(InputModes.TOUCH.getValue());
												buffer.putFloat(secondx);buffer.putFloat(secondy);
												buffer.put(Actions.ROLL.getValue());
												Log.d(TAG,"|"+event.getX(sindex)+"|");
												if(secondy > 0 && prevdy > 0) 
												{
														buffer.putInt(50);
														Log.d(TAG,"RollUp");
												} 
												else if(secondy < 0 && prevdy < 0)
												{
														buffer.putInt(-50);
														Log.d(TAG,"RollDown|"+event.getX(sindex)+"|");
												}
												//else if(prevdx < 0) buffer.putInt(-40);
												//else buffer.putInt(40);	
												sendRawMessage(buffer);
												prevdx = secondx;prevdy = secondy;
												secondx = event.getX(sindex);secondy = event.getY(sindex);
												return true;
											}
											else Log.d("move","PRIMARY_MOVED");
											dx = event.getX(pindex)-dx;dy = event.getY(pindex)-dy;
											buffer.clear();
											buffer.put(InputModes.TOUCH.getValue());
											buffer.putFloat(dx);buffer.putFloat(dy);
											buffer.put(Actions.MOVE.getValue());
											sendRawMessage(buffer);
											dx = event.getX(pindex);dy = event.getY(pindex);
											//Log.d(TAG,"ACTION_MOVE:"+id);										   
		}
		return true;
	}

}

