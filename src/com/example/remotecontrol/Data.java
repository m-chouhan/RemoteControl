package com.example.remotecontrol;

import android.os.Handler;

final class Data {
	public static enum V_KEYBUTTONS{
		L_BUTTON(0x01),
		R_BUTTON(0x02),
		M_BUTTON(0x04),
		TAB(0x09),
		RETURN(0x0D),
		ESCAPE(0x1B),
		LEFT(0x25),
		UP(0x26),
		RIGHT(0x27),
		DOWN(0x28),
		DELETE(0x2E),
		SPACE(0x20),
		BACKSPACE(0x08),
		VOLUP(0xAF),
		VOLDOWN(0xAE),
		MEDIANEXT(0xB0),
		MEDIAPREV(0xB1),
		PLAY(0xFA),
		ZOOM(0xFB);
		byte value;
		V_KEYBUTTONS(int val){ value = (byte)val;}
		public byte getValue(){return value;}
	}
	static final int BROADCAST = 4;
	static final int RAW = 1;
	static final int KEYBOARD = 0;
	//static final int TEXT = 3;
	public static enum InputModes{
		ACCELERO(1),KEYBOARD(0),TOUCH(2),
		TEXT(5),ABSOLUTE_MOUSE(3);
		final byte val;
		InputModes(int value)
		{
			this.val = (byte)value;
		}
		public byte getValue()
		{
			return val;
		}
	};
    public static enum Actions {
  
        MOVE(0),LEFT_SINGLE(1),LEFT_DOUBLE(2),LEFT_HOLD(3),LEFT_RELEASE(4),
        ROLL(5),
        RIGHT_SINGLE(6),RIGHT_DOUBLE(7),RIGHT_HOLD(8),RIGHT_RELEASE(9);
        private final byte val;
        Actions(int value)
        {
        	this.val = (byte)value;
        }
        public byte getValue()
        {
        	return val;
        }
    };
}
