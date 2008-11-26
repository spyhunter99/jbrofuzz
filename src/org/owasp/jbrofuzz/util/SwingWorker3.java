package org.owasp.jbrofuzz.util;

import javax.swing.SwingUtilities;

/**
 * This is the 3rd version of SwingWorker (also known as SwingWorker 3), an
 * abstract class that you subclass to perform GUI-related work in a dedicated
 * thread. For instructions on and examples of using this class, see:
 * 
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 * 
 * Note that the API changed slightly in the 3rd version: You must now invoke
 * start() on the SwingWorker after creating it.
 * 
 * @author Sun
 * @version 3
 */
public abstract class SwingWorker3 {
	/**
	 * <p>
	 * Class to maintain reference to current worker thread under separate
	 * synchronization control.
	 * </p>
	 */
	private static class ThreadVar {
		private Thread thread;

		ThreadVar(final Thread t) {
			this.thread = t;
		}

		synchronized void clear() {
			this.thread = null;
		}

		synchronized Thread get() {
			return this.thread;
		}
	}

	private Object value;

	private ThreadVar threadVar;

	/**
	 * <p>
	 * Start a thread that will call the <code>construct</code> method and then
	 * exit.
	 * </p>
	 */
	public SwingWorker3() {
		final Runnable doFinished = new Runnable() {
			public void run() {
				SwingWorker3.this.finished();
			}
		};

		final Runnable doConstruct = new Runnable() {
			public void run() {
				try {
					SwingWorker3.this.setValue(SwingWorker3.this.construct());
				} finally {
					SwingWorker3.this.threadVar.clear();
				}

				SwingUtilities.invokeLater(doFinished);
			}
		};

		final Thread t = new Thread(doConstruct);
		this.threadVar = new ThreadVar(t);
	}

	/**
	 * <p>
	 * Compute the value to be returned by the <code>get</code> method.
	 * </p>
	 * 
	 * @return Object
	 */
	public abstract Object construct();

	/**
	 * <p>
	 * Called on the event dispatching thread (not on the worker thread) after the
	 * <code>construct</code> method has returned.
	 * </p>
	 */
	public void finished() {
	}

	/**
	 * <p>
	 * Return the value created by the <code>construct</code> method. Returns
	 * null if either the constructing thread or the current thread was
	 * interrupted before a value was produced.
	 * </p>
	 * 
	 * @return the value created by the <code>construct</code> method
	 */
	public Object get() {
		while (true) {
			final Thread t = this.threadVar.get();
			if (t == null) {
				return this.getValue();
			}
			try {
				t.join();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt(); // propagate
				return null;
			}
		}
	}

	/**
	 * <p>
	 * Get the value produced by the worker thread, or null if it hasn't been
	 * constructed yet.
	 * </p>
	 * 
	 * @return Object
	 */
	protected synchronized Object getValue() {
		return this.value;
	}

	/**
	 * <p>
	 * A new method that interrupts the worker thread. Call this method to force
	 * the worker to stop what it's doing.
	 * </p>
	 */
	public void interrupt() {
		final Thread t = this.threadVar.get();
		if (t != null) {
			t.interrupt();
		}
		this.threadVar.clear();
	}

	/**
	 * <p>
	 * Set the value produced by worker thread
	 * </p>
	 * 
	 * @param x
	 *          Object
	 */
	private synchronized void setValue(final Object x) {
		this.value = x;
	}

	/**
	 * <p>
	 * Start the worker thread.
	 * </p>
	 */
	public void start() {
		final Thread t = this.threadVar.get();
		if (t != null) {
			t.start();
		}
	}
}
