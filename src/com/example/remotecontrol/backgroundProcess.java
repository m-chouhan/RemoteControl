package com.example.remotecontrol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class backgroundProcess extends IntentService {

	static final String TAG = "BackgroundProcess";
	private static final int SERVERPORT = 9211;
	private static String SERVER_IP = "192.168.43.156";
	private DatagramPacket packet;
	private DatagramSocket Dsocket;
	private InetAddress server_ip;
    public backgroundProcess(String name) {
        super(name);
        Log.d(TAG,"Started");
    }

	public backgroundProcess() {
		super("backgroundProcess");
		Log.d(TAG,"started");
		//try 
		{
			//server_ip = InetAddress.getByName(SERVER_IP);
			//Dsocket = new DatagramSocket();			
			Log.d(TAG,"created");
		}
		//catch (IOException e1) {
		//	e1.printStackTrace();
		//}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		String str = intent.getStringExtra(Intent.EXTRA_TEXT);
		byte []data = str.getBytes();
		packet = new DatagramPacket(data,data.length,server_ip,SERVERPORT);
		Log.d(TAG, "Intent"+str);
		try {
			Dsocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();}
		}		

}
