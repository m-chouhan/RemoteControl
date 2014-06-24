package com.example.remotecontrol;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.example.remotecontrol.Data.Actions;
import com.example.remotecontrol.Data.InputModes;
import com.example.remotecontrol.Data.V_KEYBUTTONS;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements Button.OnClickListener, SensorEventListener, OnLongClickListener {

	final String TAG = "RemoteControl";
	
	private Handler handler = null;
	//FIFO Queue for low pass filtering
	private Queue Qx = new Queue(20),Qy = new Queue(20),Qz = new Queue(20);
	private SensorManager sm;
	private GestureDetectorCompat gd;
	TextView text;

	private float[] angle = new float[3];
	private float stamp;
	private boolean backgroundThread = true;
	ByteBuffer Buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
	View v;
	private MyGestureListener listen;

	public ProgressBar pb;

	public Button connected;

	private EditText Et;
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SlidingDrawer s = (SlidingDrawer)findViewById(R.id.slidingDrawer1);
		s.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
			
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				ImageView i = (ImageView)findViewById(R.id.handle);
				i.setImageResource(R.drawable.slide_left);
			}
		});
				
		s.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
			
			public void onDrawerOpened() {
				Log.d(TAG,"SDOpened");
				ImageView i = (ImageView)findViewById(R.id.handle);
				i.setImageResource(R.drawable.slide_right);
			}
		});

		SlidingDrawer s2 = (SlidingDrawer)findViewById(R.id.slidingDrawer2);
		s2.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
			
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				ImageView i = (ImageView)findViewById(R.id.handle2);
				i.setImageResource(R.drawable.slideup);
			}
		});
				
		s2.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
			
			public void onDrawerOpened() {
				Log.d(TAG,"SDOpened");
				ImageView i = (ImageView)findViewById(R.id.handle2);
				i.setImageResource(R.drawable.slide_down);
			}
		});
		
		
		text = (TextView)findViewById(R.id.Text);

		Et = (EditText)findViewById(R.id.AutoText);
		TextMonitor tmon = new TextMonitor(Et);
		((ImageButton)findViewById(R.id.reset)).setOnClickListener(tmon);

//		Et.setOnKeyListener(tmon);		
		
//		Et.addTextChangedListener(tmon);
		
		((ImageButton)findViewById(R.id.backspace)).setOnLongClickListener(this);
		((ImageButton)findViewById(R.id.enter)).setOnLongClickListener(this);
		((ImageButton)findViewById(R.id.up)).setOnLongClickListener(this);
		((ImageButton)findViewById(R.id.down)).setOnLongClickListener(this);
		((ImageButton)findViewById(R.id.left)).setOnLongClickListener(this);
		((ImageButton)findViewById(R.id.right)).setOnLongClickListener(this);
		((ImageButton)findViewById(R.id.space)).setOnLongClickListener(this);

		pb = (ProgressBar)findViewById(R.id.pb);
		connected = (Button)findViewById(R.id.connect);
		v = (View)findViewById(R.id.view1);
		getApplicationContext();
		sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		listen = new MyGestureListener();
		gd = new GestureDetectorCompat(this,listen);
		listen.gd = gd;
		v.setOnTouchListener(listen);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		backgroundThread = true;
		multipress = new MultipressThread();		
		multipress.start();		
	}

	@Override
	public void onStop()
	{
		super.onStop();
		backgroundThread = false;
		multipress.interrupt();
		//sm.unregisterListener(this);
		//sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION));
		//sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
	}
	public void onToggleClicked(View view)
	{
		boolean status = ((ToggleButton)view).isChecked();
		if(status)
		{
			v.setVisibility(View.VISIBLE);
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME);
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);			
			//sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),SensorManager.SENSOR_DELAY_GAME);				
		}
		else
		{
			sm.unregisterListener(this);
			v.setVisibility(View.INVISIBLE);
		}
	}

/*	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d("EditText","Dispatch:"+event.getAction());
		return super.dispatchKeyEvent(event);
	}
*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generand d method stub
		Log.d("EditText","OnKeyDown");
		switch(keyCode){
			case KeyEvent.KEYCODE_MENU: 
			     Toast.makeText(this, "Menu key pressed", Toast.LENGTH_SHORT).show();
			    return true; 
		   case KeyEvent.KEYCODE_SEARCH:
			     Toast.makeText(this, "Search key pressed", Toast.LENGTH_SHORT).show();
			     return true;
		   case KeyEvent.KEYCODE_BACK:
			     onBackPressed();
			     return true;
		   case KeyEvent.KEYCODE_VOLUME_UP:			   
			   		Buffer.clear();
					Buffer.put(InputModes.KEYBOARD.getValue());
					Buffer.put(V_KEYBUTTONS.VOLUP.value);Buffer.put(V_KEYBUTTONS.VOLUP.value);
					Buffer.putInt(0);
					LooperThread.sendRawMessage(Buffer);
					return true;
		   case KeyEvent.KEYCODE_VOLUME_DOWN:
			     //Toast.makeText(this,"Volumen Down pressed", Toast.LENGTH_SHORT).show();
			   		Buffer.clear();
					Buffer.put(InputModes.KEYBOARD.getValue());
					Buffer.put(V_KEYBUTTONS.VOLDOWN.value);Buffer.put(V_KEYBUTTONS.VOLDOWN.value);
					Buffer.putInt(0);
					LooperThread.sendRawMessage(Buffer);
					return true;
		   case KeyEvent.KEYCODE_CAPS_LOCK:
		   case KeyEvent.KEYCODE_SPACE:case KeyEvent.KEYCODE_X:
			     Toast.makeText(this,"CapsLock", Toast.LENGTH_SHORT).show();
			     Log.d("EditText","CapsLock");
			     return true;
		 }
		 return super.onKeyDown(keyCode, event);	
	}

	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d("EditText","OnKeyShort");
		return super.onKeyShortcut(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			Buffer.clear();
			Buffer.put(InputModes.TOUCH.getValue());
			Buffer.putFloat((float) (Qx.getMean()) );
			Buffer.putFloat((float) (Qy.getMean()) );
			Buffer.put(Actions.LEFT_RELEASE.getValue());
			LooperThread.sendRawMessage(Buffer);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	public void resetText(View view) {
	}
	public void onClick(View view) {
		
			Log.d(TAG,"ButtonClicked");
			if(view.getId() == R.id.connect) 
			{				
				pb.setVisibility(View.VISIBLE);
				connected.setText("connecting..");
				//Log.d(TAG,"H:"+v.getHeight()+"W:"+v.getWidth());
				LooperThread background = new LooperThread(this);
				background.start();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler = background.getHandler();
				Message msg = handler.obtainMessage();
				msg.what = Data.BROADCAST ;
				handler.sendMessage(msg);/**/
				//sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
				
				if( sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null)
					Log.d(TAG,"NOT avlble");
				return;
			}
			
			V_KEYBUTTONS key = null ;
			switch(view.getId())
			{
				case R.id.up  : 	key = V_KEYBUTTONS.UP;break;
				case R.id.down:		key = V_KEYBUTTONS.DOWN;break;
				case R.id.left:		key = V_KEYBUTTONS.LEFT;break;
				case R.id.right:	key = V_KEYBUTTONS.RIGHT;break;
				case R.id.space:	key = V_KEYBUTTONS.SPACE;break;
				case R.id.backspace:key = V_KEYBUTTONS.BACKSPACE;break;
				case R.id.enter:	key = V_KEYBUTTONS.RETURN;break;
			}
			if(handler == null) return;
			Message msg = handler.obtainMessage();
			msg.arg1 = key.getValue();msg.what = Data.KEYBOARD;
			
			handler.sendMessage(msg);/**/
			multipress.Unpress();
			Log.d("TAG", "OnClick:"+key);
		} 			


	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void showAlert(String heading,String content) {
		new AlertDialog.Builder(this)
		.setTitle(heading)
		.setMessage(content)
		.setNeutralButton("Ok fine..",null)
		.show();
		pb.setVisibility(View.INVISIBLE);
		connected.setText("Connect Again");
	}
		
	float[] accelro = new float[3];float []mag = new float[3];
	float[] rotationmat = new float[9];float []orient = new float[3];
	public void onSensorChanged(SensorEvent event) {
		
		if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD )
		{
			System.arraycopy(event.values, 0,mag,0, 3);
			Buffer.clear();
			Buffer.put(InputModes.ACCELERO.getValue());
			Buffer.putFloat((float) (Math.toDegrees(Qx.getMean())) );
			Buffer.putFloat((float) (Math.toDegrees(Qy.getMean())) );
			Buffer.putLong(event.timestamp);
			LooperThread.sendRawMessage(Buffer);
		}
		else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			System.arraycopy(event.values, 0,accelro,0, 3);

		SensorManager.getRotationMatrix(rotationmat, null, accelro, mag);
		SensorManager.getOrientation(rotationmat, orient);
		Qx.insert(orient[0]);Qy.insert(orient[1]);Qz.insert(orient[2]);
		
		float xdeg = (float) Math.toDegrees(orient[0]);
		float ydeg = (float) Math.toDegrees(Qy.getMean());
		text.setText("a = "+xdeg+"\t"+mag[0]+"\nb = "+ ydeg +
					 "\t"+mag[1]+"\nc = "+Math.toDegrees(Qz.getMean()) +
					 "\nSum"+(Math.toDegrees(Qx.getMean()) + Math.toDegrees(Qy.getMean())*Math.sin(Qz.getMean())) );
	}

	public boolean onLongClick(View v) {
		byte key = 0;
		Log.d(TAG,"LongClicked");
		switch(v.getId())
		{
			case R.id.up  : 	key = V_KEYBUTTONS.UP.value;break;
			case R.id.down:		key = V_KEYBUTTONS.DOWN.value;break;
			case R.id.left:		key = V_KEYBUTTONS.LEFT.value;break;
			case R.id.right:	key = V_KEYBUTTONS.RIGHT.value;break;
			case R.id.space:	key = V_KEYBUTTONS.SPACE.value;break;
			case R.id.backspace:key = V_KEYBUTTONS.BACKSPACE.value;break;
			case R.id.enter:	key = V_KEYBUTTONS.RETURN.value;break;
		}
		multipress.ButtonPressed(key);
		return false;
	}
	
	private MultipressThread multipress;
	private class MultipressThread extends Thread{
		public  boolean pressed = false;
		public  byte key = 0;
		public void ButtonPressed(byte key)
		{
			this.key = key;
			pressed = true;
			Log.d(TAG,"ButtonPressed"+pressed);
		}
		public void Unpress()
		{
			pressed = false;
			key = 0;
		}
		public void run()
		{
			while(backgroundThread)
			{
				try 
				{
					while(!pressed) sleep(1200);
					Buffer.clear();
					Log.d(TAG,"InsideLoop");
					Buffer.put(InputModes.KEYBOARD.getValue());
					Buffer.put(key);Buffer.put((byte)0);
					LooperThread.sendRawMessage(Buffer);				

					this.sleep(60);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}