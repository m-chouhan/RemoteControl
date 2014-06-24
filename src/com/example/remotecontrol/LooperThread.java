package com.example.remotecontrol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.example.remotecontrol.Data.InputModes;

class LooperThread extends Thread{
	//For handling message passing through socket
	private InetAddress server_ip;
	private DatagramSocket Dsocket;
	private static final int SERVERPORT = 19000;
	private String SERVER_IP = "192.168.43.156";
	private Handler handler ;
	private MainActivity main;
	static final String TAG = "LooperThread";
	public Handler getHandler() {
		return handler;
	}
	public LooperThread(MainActivity main)
	{
		super();
		this.main = main;
	}
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
								handleBroadcast();
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
	
	private void handleBroadcast(){
		byte []arr = new byte[5];
		arr[0] = 4;  //dummy packet :)									
		try {
			Log.d(TAG,"Broadcasting..");
			InetAddress bcast = getBroadcastAddr();
			if(bcast == null) 
			{
				main.runOnUiThread(new Runnable(){
			
					public void run()
					{
						main.showAlert("Connection Failed","Please Connect to wifi");
					}
				});
				return;
			}
			DatagramPacket packet = new DatagramPacket(arr,arr.length,bcast,SERVERPORT);
			Dsocket.send(packet);
			Dsocket.receive(packet);
			Log.d(TAG,"Address::"+packet.getAddress().toString());
			server_ip = packet.getAddress();
			main.runOnUiThread(new Runnable(){
				public void run()
				{
					main.pb.setVisibility(View.INVISIBLE);
					main.connected.setText("connected");
				}
			});
			
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			Log.d(TAG,"UnknownHost");
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG,"IOEx");
			main.runOnUiThread(new Runnable(){											
				public void run()
				{
					main.showAlert("Connection Failed","Unable to find server");
				}
			});
		}													
	}		


	private InetAddress getBroadcastAddr() 
	{
		InetAddress bcast = null;
		System.setProperty("java.net.preferIPv4Stack","true");
		try
		{
			Enumeration<NetworkInterface> nienum = NetworkInterface.getNetworkInterfaces();
			while(nienum.hasMoreElements())
			{
				NetworkInterface ni = nienum.nextElement();
				if(!ni.isLoopback())
				{
					List<InterfaceAddress> addresses = ni.getInterfaceAddresses();
					for(InterfaceAddress iaddr :addresses)
					{
						InetAddress addr = iaddr.getBroadcast();
						if(addr != null) bcast = addr;						
					}
				}				
			}
			
		}catch (SocketException e){ e.printStackTrace();}
		return bcast;
	}

}
