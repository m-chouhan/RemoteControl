package com.example.remotecontrol;

/*Class Queue for creating FIFO queues and calculating their mean
 * i.e act as an low pass filter
 */

public class Queue{
	private int Size = 0;
	private float array[];
	private int index = 0;
	public Queue(int size) {
		array = new float[size];
		Size = size;
	}
	public void reset(float val)
	{		
			for(int i = 0;i<Size;++i) array[i] = val;
	}
	public void insert(float value)
	{
		array[index++] = value;
		index = index % Size;
	}
	public float get(int i)
	{
		return array[(index+i)%Size];
	}
	public float getMean()
	{
		float sum = 0;
		for(int i = 0;i<Size;++i) sum += array[i];
		return sum/Size;
	}
	public float getWMean()
	{
		float sum = 0;
		int i = 0;
		for(;i<10;++i) sum += array[(index+i) % Size];
		for(;i<15;++i) sum += 2*array[ (index+i) %Size];
		for(;i<20;++i) sum += 3*array[ (index+i)%Size];
		return sum/(35);		
	}
}
