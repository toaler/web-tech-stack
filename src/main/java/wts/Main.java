package wts;

import java.net.URL;

import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NegotiatingServerConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import com.google.common.io.Files;

public class Main {
	public static void main(String[] args) throws Exception {
		Server server = new Server();

		int httpPort = 8080;
		int httpsPort = 8443;

		// Setup HTTP Connector
		HttpConfiguration httpConf = new HttpConfiguration();
		httpConf.setSecurePort(httpsPort);
		httpConf.setSecureScheme("https");
		httpConf.setSendXPoweredBy(true);
		httpConf.setSendServerVersion(true);

		// Establish the HTTP ServerConnector
		ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(httpConf));
		httpConnector.setPort(httpPort);
		httpConnector.setIdleTimeout(5000);
		server.addConnector(httpConnector);

		// Find Keystore for SSL
		ClassLoader cl = Main.class.getClassLoader();
		String keystoreResource = "ssl/keystore";
		URL f = cl.getResource(keystoreResource);
		if (f == null) {
			throw new RuntimeException("Unable to find " + keystoreResource);
		}

		// Setup SSL
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(f.toExternalForm());
		sslContextFactory.setKeyStorePassword("jettyjetty");
		sslContextFactory.setKeyManagerPassword("storepwd");
		sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

		// Setup HTTPS Configuration
		HttpConfiguration httpsConf = new HttpConfiguration(httpConf);
		httpsConf.addCustomizer(new SecureRequestCustomizer()); // adds ssl info
																// to request
																// object

		// HTTP/2 Connection Factory
		HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpsConf);

		NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
		ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
		alpn.setDefaultProtocol(httpConnector.getDefaultProtocol());

		// SSL Connection Factory
		SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

		// HTTP/2 Connector
		ServerConnector http2Connector = new ServerConnector(server, ssl, alpn, h2,
				new HttpConnectionFactory(httpsConf));
		http2Connector.setPort(httpsPort);
		ALPN.debug = true;
		server.addConnector(http2Connector);

		WebAppContext context = new WebAppContext();
		context.setResourceBase("/");

		context.setConfigurations(new Configuration[] { new AnnotationConfiguration(), new WebInfConfiguration(),
				new WebXmlConfiguration(), new MetaInfConfiguration(), new FragmentConfiguration(),
				new EnvConfiguration(), new PlusConfiguration(), new JettyWebXmlConfiguration() });

		context.setContextPath("/");

		String url = Main.class.getProtectionDomain().getCodeSource().getLocation().toString();
		System.out.println("URL URL URL url = " + url);
		String jarRegex = ".*" + Files.getNameWithoutExtension(url) + "\\." + Files.getFileExtension(url);
		context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", jarRegex);
		context.setParentLoaderPriority(true);

		server.setHandler(context);

		server.start();
                server.dump(System.err);
		server.join();
	}

}
