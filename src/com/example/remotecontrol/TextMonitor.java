package com.example.remotecontrol;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.remotecontrol.Data.InputModes;
import com.example.remotecontrol.Data.V_KEYBUTTONS;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class TextMonitor implements TextWatcher,OnKeyListener,OnClickListener{

	ByteBuffer Buffer = ByteBuffer.allocate(80).order(ByteOrder.LITTLE_ENDIAN);
	EditText Et;
	
	public TextMonitor(EditText et)
	{
		Et = et;
		Et.setOnKeyListener(this);				
		Et.addTextChangedListener(this);
	}
	
	public void afterTextChanged(Editable arg0) {
		//Log.d("Edittext", "after"+arg0.toString());		
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}
	
	String prev="";
	private boolean flag = false;
	
	public void onTextChanged(CharSequence s, int start, int before,
			int count) {
		
		if(flag)
		{
			//Do Nothing
			flag = false;
			return;
		}
		String current = s.toString();
		int commonChars = Compare(current,prev);
		try {
				Buffer.clear();
				Buffer.put(InputModes.TEXT.getValue());
				int i = 0;
				for(;i<(prev.length()-commonChars);++i)
					Buffer.put(V_KEYBUTTONS.BACKSPACE.getValue());
				Log.d("EditText","Backspaces:"+i+"  common"+commonChars+prev+":"+current);
				byte text[] = current.substring(commonChars).getBytes("US-ASCII");
				Buffer.put(text);Buffer.put((byte) 0);
				LooperThread.sendRawMessage(Buffer);
				prev = current;
				} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		//Log.d("EditText",s.toString()+":"+start+":"+before+":"+count);
	}
	
	public int Compare(String s1,String s2)
	{
		if(s1.length() == 0 || s2.length() == 0) return 0;
		int len = Math.min(s1.length(), s2.length()),i = 0;
		Log.d("EditTExt","minlength"+len);
		try{
			while(i<len && s1.charAt(i) == s2.charAt(i))
			i++;
		}catch(IndexOutOfBoundsException e){
			Log.d("EditText","OutofBounds"+i);
			return 0;
		}
		return i;
	}
	
    public boolean onKey(View v, int keyCode, KeyEvent event) {

		Buffer.clear();
		Buffer.put(InputModes.TEXT.getValue());
		
		V_KEYBUTTONS data = null;
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_ENTER:
					data = V_KEYBUTTONS.RETURN;
					break;
			case KeyEvent.KEYCODE_DEL:
					data = V_KEYBUTTONS.BACKSPACE;
					break;
		}
    	switch(event.getAction())
        {
            case KeyEvent.ACTION_DOWN:
            		Log.d("EditText", "ActionDown:"+keyCode);
		        	if(data != null)
		        	{
		        		Buffer.put(data.getValue());
		        		Buffer.put((byte)0);
		        		LooperThread.sendRawMessage(Buffer);
		        	}
            		break;
            case KeyEvent.ACTION_UP:
            		Log.d("EditText", "ActionUP:"+keyCode);
            		break;
            case KeyEvent.ACTION_MULTIPLE:
        			Log.d("EditText", "ActionMultiple:"+keyCode);
            		break;
        }
        return false;
    }

	public void onClick(View arg0) {
		try 
		{			
			String text = Et.getText().toString();
			Buffer.clear();
			Buffer.put(InputModes.TEXT.getValue());
			int i = 0;
			for(;i<text.length();++i)
				Buffer.put(V_KEYBUTTONS.BACKSPACE.getValue());
			byte data[] = text.getBytes("US-ASCII");
			Buffer.put(data);Buffer.put((byte) 0);
			LooperThread.sendRawMessage(Buffer);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flag  = true;
		Et.setText("");
	}

}
