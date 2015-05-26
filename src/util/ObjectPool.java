package util;


import java.util.concurrent.ConcurrentLinkedQueue;


public class ObjectPool<T>
{
	
	public ConcurrentLinkedQueue<T> objectList;
	
	public ObjectPool()
	{
		objectList = new ConcurrentLinkedQueue<>();
	}
	
	public void addObject(T obj)
	{
		this.objectList.add(obj);
	}
	
	public T borrowObject()
	{
		return this.objectList.poll();
	}
	
	public void returnObject(T obj)
	{
		this.objectList.add(obj);
	}

	public int getPoolSize()
	{
		return this.objectList.size();
	}
}