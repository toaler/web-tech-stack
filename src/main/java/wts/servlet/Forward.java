package wts.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Forwarding doesn't send a redirect, instead it uses the content of the target
 * page as HTTP response:
 * <p>
 * <ul>
 * <li>Client sends a HTTP request to some.jsp.
 * <li>Server sends a HTTP response back with the content of other.jsp
 * </ul>
 * <p>
 * However as the original HTTP request was to some.jsp, the URL in the browser
 * address bar remains unchanged.
 * <p>
 * The requestDIspatcher is extremely useful in MVC paradigm, and/or when you
 * want to hide JSP's from direct access. You can put JSP's in /WEB-INF folder
 * and use aServlet which controls, preprocess and postpocesses the requests.
 * The JSPs in /WEB-INF folder are not directly accessible by URL, but the
 * Servlets can access them using
 * {@link RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
 * <p>
 * You can for example have a JSP file /WEB-INF/login.jsp and a LoginServlet
 * which is mapped on and url-pattern of /login. When you invoke
 * http://exmpale.com/content/login, then the servlet doGet() will be invoked,
 * you can do any preprocssing stuff in there and finally forward the request
 * like:
 * <p>
 * <code>request.getRequestDispatcher("/WEB-INFO/login.jsp").forward(request,
 * response);</code>
 * <p>
 * When you submit a form you normally want to use POST:
 * <p>
 * <form action="login" method="post">
 * <p>
 * This way the Servlet doPost() will be invoked and you can do any
 * post-processing stuff in there (eg. validation, business logic, login the
 * user, etc).
 * <p>
 * If there are errors, then you normally want ot forward the request back to
 * the same page and display the errors there next to the input fields and so
 * on. You can use the RequestDisptercher for this.
 * <p>
 * If a POST is been successful, you normally want to redirect the request, so
 * that the request won't be resubmitted when the user refreshed the reqeust.
 * <p>
 * <p>
 * ADD CODE SNIPPET
 * <p>
 * A redirect thus instructs the client to fire a new GET request on the given
 * URL. Refreshing the request would then only refresh the redirected request
 * and not the initial request. This will avoid "double submits" and confusion
 * and bad user experience. This is also called the POST-Redirect-Get Pattern.
 * 
 * @author btoal
 *
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/forward" })
public class Forward extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		request.getRequestDispatcher("/hello").forward(request, response);
	}
}