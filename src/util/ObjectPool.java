package util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ObjectPool<T> {
	
	public ConcurrentLinkedQueue<T> objectList;
	
	public ObjectPool(){
		objectList = new ConcurrentLinkedQueue<T>();
	}
	
	public void addObject(T obj){
		objectList.add(obj);
	}
	
	public T borrowObject(){
		return objectList.poll();
	}
	
	public void returnObject(T obj){
		objectList.add(obj);
	}

	public int poolSize()
	{
		return objectList.size();
	}
}