package org.openbw.tsbw;

public interface Subscriber<T> {

	public void onReceive(T t);
}
