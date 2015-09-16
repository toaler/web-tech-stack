package wts.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The term 'redirect' is in web development world the action of sending the
 * client an empty HTTP response with just a Location header with therein the
 * new URL on which the client has to send a brand new GET request.
 * <p>
 * <li>  Client sends a HTTP request to some page
 * <li>  Server send a HTTP response back with Location: 'other page' header.
 * <li>  Client sends a HTTP request to 'other page'
 * <li>  Server sends a HTTP response back with the content of 'other page'
 * <p>
 * 1. In he case of sendRedirect, request is transfered to another resource to
 * different domain or different server fro further processing.
 * <p>
 * 2. When you usesendRedirect container transfers the request to client or
 * browser so URL given inside the sendRedirect method is visible as a new
 * request to the client.
 * <p>
 * 3. In case fo sendRedirect call, old request and response objects are lost
 * becuase it's treated as new request by the browser.
 * <p>
 * 4. In the address bar, we are able to see the new redirect address, it's not
 * transparent.
 * <p>
 * 5. sendRedirect is slower because on extra round trip is require because
 * completely new requets is created and old request object is lost. Two
 * browswer requests are required.
 * <p>
 * 6. But in sendRedirect, if we want to use, we have to store the data in
 * session or pass onling in the URL.
 * 
 * Useful when you want conrol of transfer to new server or context that is
 * treated as a completel new task.
 * 
 * @author btoal
 *
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/redirect" })
public class Redirect extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.sendRedirect("/hello");
	}
}