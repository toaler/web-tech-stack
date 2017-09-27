package wts.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(asyncSupported = true, value = "/async")
public class Async extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(Async.class.getSimpleName());

	private static final long serialVersionUID = 1L;
	private static final int QUEUE_SIZE = 500;
	private static final int THREADS = 4;
	private static final long TIMEOUT = 5000;
	private ThreadPoolExecutor executor;
	private ArrayBlockingQueue<Runnable> queue;
	private Stats stats;

	@Override
	public void init() {
		queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
		executor = new ThreadPoolExecutor(THREADS, THREADS, 0, TimeUnit.SECONDS, queue);
		stats = new Stats();
		
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

		logger.info("Launching statistics thread");
		exec.scheduleAtFixedRate(() -> {
			try {
			logger.info(stats.toString());
			} catch (Exception x) {
				logger.error("Error in statistics logging thread", x);
			}
		}, 0, 5, TimeUnit.SECONDS);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			logger.debug("In doGet");
			long st = System.nanoTime();
			AsyncContext ac = request.startAsync();
			ac.addListener(new AsyncListenerImpl());
			ac.setTimeout(TIMEOUT);

			executor.execute(new AsyncRequestProcessor(ac, st, stats));
			
		} catch (Exception x) {
			stats.incError();
			logger.debug("Failed handling request ", x);
		}
	}

	private static class AsyncListenerImpl implements AsyncListener {

		@Override
		public void onComplete(AsyncEvent event) throws IOException {
			logger.debug("In onComplete");
		}

		@Override
		public void onTimeout(AsyncEvent event) throws IOException {
			logger.debug("In onTimeout");
		}

		@Override
		public void onError(AsyncEvent event) throws IOException {
			logger.debug("In onError");
		}

		@Override
		public void onStartAsync(AsyncEvent event) throws IOException {
			logger.debug("In onStartAsync");
		}
	}

	private static class AsyncRequestProcessor implements Runnable {
		private final AsyncContext wc;
		private final long servletStartTime;
		private final Stats stats;

		public AsyncRequestProcessor(AsyncContext wc, long servletStartTime, Stats stats) {
			this.wc = wc;
			this.servletStartTime = servletStartTime;
			this.stats = stats;
		}

		public void run() {
			stats.incRequest();
			long st = System.nanoTime();
			logger.debug("In AsyncRequestProcessor.run");
			try {
				Thread.sleep(Integer.valueOf(wc.getRequest().getParameter("sleeptime")));

				PrintWriter pw = wc.getResponse().getWriter();
				pw.printf("Thread %s completed.", Thread.currentThread().getName());
				pw.flush();

			} catch (Exception e) {
				stats.incError();
				throw new RuntimeException(e);
			} finally {
				long et = System.nanoTime();
				stats.updateWaitLatency(st - servletStartTime);
				stats.updateServiceLatency(et - st);
				stats.updateResponseLatency(et - servletStartTime);
			}

			wc.complete();
		}
		
	}
	
	private static class Stats {
		private final AtomicLong requests;
		private final AtomicLong errors;
		private final Recorder waitTimes;
		private final Recorder serviceTimes;
		private final Recorder responseTimes;

		public Stats() {
			requests = new AtomicLong();
			errors = new AtomicLong();
			waitTimes = new Recorder(3);
			serviceTimes = new Recorder(3);
			responseTimes = new Recorder(3);
		}
		
		public void updateWaitLatency(long t) {
			waitTimes.recordValue(t);
		}
		
		public void updateServiceLatency(long t) {
			serviceTimes.recordValue(t);
		}
		
		public void updateResponseLatency(long t) {
			responseTimes.recordValue(t);
		}
		
		public void incRequest() {
			requests.incrementAndGet();
		}
		
		public void incError() {
			errors.incrementAndGet();
		}
		
		
		public String toString() {
			Histogram w = waitTimes.getIntervalHistogram();
			Histogram s = serviceTimes.getIntervalHistogram();
			Histogram r = responseTimes.getIntervalHistogram();
			long rx = requests.getAndSet(0);
			long e = errors.getAndSet(0);
		
			return String.format("[STATS] requests=%d, errors=%d, wtp50=%d, wtp99=%d, stp50=%d, stp99=%d, rtp50=%d, rtp99=%d",
					rx, e, 
					w.getValueAtPercentile(50), w.getValueAtPercentile(99),
					s.getValueAtPercentile(50), s.getValueAtPercentile(99),
					r.getValueAtPercentile(50), r.getValueAtPercentile(99));
		}
		
	}

}