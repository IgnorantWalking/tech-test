package com.example.techtest.termfrequency.stream;

import java.util.concurrent.BlockingQueue;

/**
 * Interface to be implemented by basic stream components capable of read and
 * process some specific entities, without generating any other new event
 * 
 * @author dmacia
 *
 * @param <E> the type of entities consumed by the component
 */
public interface Sink<E> {

	/**
	 * Define the queue the source entities are received from
	 * 
	 * @param queue
	 */
	public void from(BlockingQueue<E> queue);
}
