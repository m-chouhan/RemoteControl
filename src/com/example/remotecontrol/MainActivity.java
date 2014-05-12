package com.example.remotecontrol;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.remotecontrol.Data.Actions;
import com.example.remotecontrol.Data.InputModes;
import com.example.remotecontrol.Data.KEYBUTTONS;


import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements Button.OnClickListener, SensorEventListener {

	private Handler handler = null;
	//FIFO Queue for low pass filtering
	private Queue Qx = new Queue(20),Qy = new Queue(20),Qz = new Queue(20);
	private SensorManager sm;
	private GestureDetectorCompat gd;
	TextView text;

	private float[] angle = new float[3];
	private float stamp;
	ByteBuffer Buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
	View v;
	private MyGestureListener listen;
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		text = (TextView)findViewById(R.id.Text);
		Button B = (Button)findViewById(R.id.up);
		B.setOnClickListener(this);
		
		ImageButton but = (ImageButton)findViewById(R.id.down);
		but.setOnClickListener(this);
		but = (ImageButton)findViewById(R.id.left);
		but.setOnClickListener(this);
		but = (ImageButton)findViewById(R.id.right);
		but.setOnClickListener(this);
		but = (ImageButton)findViewById(R.id.space);
		but.setOnClickListener(this);/**/
		B = (Button)findViewById(R.id.connect);
		but.setOnClickListener(this);
		v = (View)findViewById(R.id.view1);
		getApplicationContext();
		sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		listen = new MyGestureListener();
		gd = new GestureDetectorCompat(this,listen);
		listen.gd = gd;
		v.setOnTouchListener(listen);
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
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
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
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
					Buffer.put(InputModes.TOUCH.getValue());
					Buffer.putFloat((float) (Qx.getMean()) );
					Buffer.putFloat((float) (Qy.getMean()) );
					Buffer.put(Actions.LEFT_HOLD.getValue());
					listen.sendRawMessage(Buffer);
					return true;
		   case KeyEvent.KEYCODE_VOLUME_DOWN:
			     Toast.makeText(this,"Volumen Down pressed", Toast.LENGTH_SHORT).show();
			     return true;
		 }
		 return super.onKeyDown(keyCode, event);	
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
			listen.sendRawMessage(Buffer);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	public void onClick(View view) {
		

			if(view.getId() == R.id.connect) 
			{
				Log.d(TAG,"H:"+v.getHeight()+"W:"+v.getWidth());
				Thread background = new LooperThread();
				background.start();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				listen.handler = handler;
				Message msg = handler.obtainMessage();
				msg.what = Data.BROADCAST ;
				handler.sendMessage(msg);/**/

				//sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
				
				if( sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null)
					Log.d(TAG,"NOT avlble");
				return;
			}
			KEYBUTTONS key = null ;
			switch(view.getId())
			{
				case R.id.up  : 	key = KEYBUTTONS.UP;break;
				case R.id.down:		key = KEYBUTTONS.DOWN;break;
				case R.id.left:		key = KEYBUTTONS.LEFT;break;
				case R.id.right:	key = KEYBUTTONS.RIGHT;break;
				case R.id.space:	key = KEYBUTTONS.SPACE;break;
			}
			
			Message msg = handler.obtainMessage();
			msg.arg1 = key.getValue();msg.what = Data.KEYBOARD;
			
			handler.sendMessage(msg);/**/
			Log.d("TAG", "OnClick:"+key);
		} 			


	public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
			listen.sendRawMessage(Buffer);
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
	class LooperThread extends Thread{
		//For handling message passing through socket
		private InetAddress server_ip;
		private DatagramSocket Dsocket;
		private static final int SERVERPORT = 19000;
		private String SERVER_IP = "192.168.43.156";

		public void run()
		{
			try{
				Dsocket = new DatagramSocket();
				Dsocket.setSoTimeout(4000);
				server_ip = InetAddress.getByName(SERVER_IP);

			} catch (SocketException e1) {
				e1.printStackTrace();return;
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			Looper.prepare();
			handler = new Handler(){
				private static final String TAG = "SocketHandlerThread";
				
				public void handleMessage(Message msg)
				{
					DatagramPacket packet = null;;
					switch(msg.what)
					{
						case Data.KEYBOARD:
									byte []key = new byte[]{(byte) InputModes.KEYBOARD.getValue(),(byte)msg.arg1};
									packet = new DatagramPacket(key,key.length,server_ip,SERVERPORT);
									break;
						case Data.RAW:		
									Bundle bundle = msg.getData();
									byte []buff = bundle.getByteArray("RAW");
									packet = new DatagramPacket(buff,buff.length,server_ip,SERVERPORT);
									break;
						case Data.BROADCAST:		
									byte []arr = new byte[5];
									arr[0] = 4;  //dummy packet :)									
									try {
										Log.d(TAG,"Broadcasting..");
										packet = new DatagramPacket(arr,arr.length,InetAddress.getByName("192.168.43.255"),SERVERPORT);
										Dsocket.send(packet);
										Dsocket.receive(packet);
										Log.d(TAG,"Address::"+packet.getAddress().toString());
										server_ip = packet.getAddress();
									} catch (UnknownHostException e1) {
										// TODO Auto-generated catch block
										Log.d(TAG,"UnknownHost");
										e1.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										Log.d(TAG,"IOEx");
									}										
									return;										
					}
					
					//Log.d(TAG, "Handler:"+msg.what);
					try {
						Dsocket.send(packet);
						Log.d(TAG,"SENT...");
					} catch (IOException e) {
						e.printStackTrace();}
				}		
			};
			Looper.loop();
		}
	}
	
	final String TAG = "RemoteControl";
}