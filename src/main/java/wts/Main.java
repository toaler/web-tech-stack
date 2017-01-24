package wts;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.http2.HTTP2Cipher;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static String PROPERTY_FILE = "wts.properties";

	public static void main(String[] args) throws Exception {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream(PROPERTY_FILE);

		if (input == null) {
			logger.error("Couldn't find " + PROPERTY_FILE);
		}
		Properties properties = new Properties();
		properties.load(input);

		properties.entrySet().stream()
				.forEach((entry) -> System.setProperty(entry.getKey().toString(), entry.getValue().toString()));

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
		String jarRegex = ".*" + Files.getNameWithoutExtension(url) + "\\." + Files.getFileExtension(url);
		context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", jarRegex);
		context.setParentLoaderPriority(true);

		server.setHandler(context);

		server.start();

		// server.dump(System.err);

		try (CuratorFramework curatorFramework = CuratorFrameworkFactory
				.newClient(System.getProperty("zookeeper.hosts"), new RetryNTimes(3, 1000))) {
			curatorFramework.start();

			final String address = InetAddress.getLocalHost().getHostAddress();
			ServiceInstance<String> serviceInstance;
			serviceInstance = ServiceInstance.<String>builder().uriSpec(new UriSpec("{scheme}://{address}:{port}"))
					.address(address).port(httpPort).name("wts").serviceType(ServiceType.DYNAMIC).build();

			final String basePath = "/service-discovery";
			ServiceDiscovery<String> s = ServiceDiscoveryBuilder.<String>builder(String.class).basePath(basePath)
					.client(curatorFramework).build();
			s.queryForInstances("wts").stream().forEach((i) -> {
				if (i.getAddress().equals(address) && i.getPort() == httpPort) {
					try {
						curatorFramework.delete().forPath(basePath + "/" + i.getName() + "/" + i.getId());
						logger.info("Unregistered left over entry in service discover " + i);
					} catch (Exception e) {
						logger.error("Couldn't unregister " + s, e);
					}
				}
			});

			try (ServiceDiscovery<String> sd = ServiceDiscoveryBuilder.<String>builder(String.class)
					.basePath("/service-discovery").client(curatorFramework).thisInstance(serviceInstance).build()) {

				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						if (sd != null) {
							try {
								sd.close();
							} catch (IOException e) {
								System.err
										.println("Failed to close service discover due to " + e.getMessage() + " cause="
												+ e.getCause() + " stack=" + Joiner.on(",").join(e.getStackTrace()));
							}
						}
					}
				});

				sd.start();
				sd.queryForInstances("wts").stream().forEach((e) -> logger.info("found = " + e));

				server.join();
			}

		}

	}

}
