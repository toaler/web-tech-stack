package wts.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wts.listener.Work;

@WebServlet(asyncSupported = true, value = "/async")
public class Async extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
		System.out.println("In Async.doGet");
			Work.add(request.startAsync());
			System.out.println("Queue size = " + Work.queue.size());
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
}