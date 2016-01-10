package wts;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true, value = "/AsyncServlet")
public class AsyncServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Work.add(request.startAsync());
	}

	public static class Work implements ServletContextListener {
		private static final BlockingQueue<AsyncContext> queue = new LinkedBlockingQueue<>();

		private volatile Thread thread;

		public static void add(AsyncContext c) {
			queue.add(c);
		}

		public void contextInitialized(ServletContextEvent servletContextEvent) {
			thread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(2000);
							AsyncContext context;
							while ((context = queue.poll()) != null) {
								try {
									ServletResponse response = context.getResponse();
									response.setContentType("text/plain");
									PrintWriter out = response.getWriter();
									out.printf("Thread %s completed the task", Thread.currentThread().getName());
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

		public void contextDestroyed(ServletContextEvent servletContextEvent) {
			thread.interrupt();
		}
	}
}