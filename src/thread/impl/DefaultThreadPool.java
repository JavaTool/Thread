package thread.impl;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import thread.base.ThreadCall;
import thread.base.ThreadPool;

public class DefaultThreadPool implements ThreadPool {
	
	protected ExecutorService executor;
	
	public DefaultThreadPool(int min,int max){
		executor = new MeasureThreadPoolExectuor(min, max, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	@Override
	public void execute(Runnable call) {
		executor.execute(call);
	}
	
	private static class MeasureThreadPoolExectuor extends ThreadPoolExecutor {
		
		private HashMap<Runnable, Long> thread2time = new HashMap<Runnable, Long>();
		
		public MeasureThreadPoolExectuor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue,
				RejectedExecutionHandler handler) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
		}

		public MeasureThreadPoolExectuor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
				RejectedExecutionHandler handler) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
					threadFactory, handler);
		}

		public MeasureThreadPoolExectuor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
					threadFactory);
		}

		public MeasureThreadPoolExectuor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}
		
		@Override
		protected void beforeExecute(Thread t, Runnable r) { 
			synchronized(thread2time) {
				thread2time.put(r, System.nanoTime());
			}
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			if (r instanceof ThreadCall) {
				((ThreadCall) r).callFinish();
				remove(r);
			}
		}
		
	}
	
}
