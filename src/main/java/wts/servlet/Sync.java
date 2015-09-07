package wts.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/Sync")
/**
 * A blocking servlet, which would effectively exhaust app server resources under a significant load
 */
public class Sync extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			long start = System.currentTimeMillis();
			
			// Every 2 seconds some event happens. E.g. stock quote arrives, chat is updated and so on.
			// End-user request arrives, announcing interest to monitor certain events
			// The thread is blocked until the next event arrives
			Thread.sleep(new Random().nextInt(2000));
			
			String name = Thread.currentThread().getName();
			long duration = System.currentTimeMillis() - start;
			
			// Upon receiving the event, the response is compiled and sent back to the client
			response.getWriter().printf(
					"Thread %s completed the task in %d ms.", name, duration);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}