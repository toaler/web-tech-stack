package wts.listener;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletResponse;

public class Work implements ServletContextListener {
	public static final BlockingQueue<AsyncContext> queue = new LinkedBlockingQueue<>();

	private volatile Thread thread;

	public static void add(AsyncContext c) {
		queue.add(c);
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		System.out.println("In Work.contextInitialized");
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
						AsyncContext context;
						while ((context = queue.poll()) != null) {
							try {
								ServletResponse response = context
										.getResponse();
								response.setContentType("text/plain");
								PrintWriter out = response.getWriter();
								out.printf("Thread %s completed the task",
										Thread.currentThread().getName());
								out.flush();
							} catch (Exception e) {
								throw new RuntimeException(e.getMessage(), e);
							} finally {
								context.complete();
							}
						}
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
		thread.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		thread.interrupt();
	}
}
