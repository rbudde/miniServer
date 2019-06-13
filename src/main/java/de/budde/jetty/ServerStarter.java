package de.budde.jetty;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.servlet.GuiceFilter;

import de.budde.guice.MiniServerGuiceModule;
import de.budde.guice.MiniServerGuiceServletContextListener;
import de.budde.util.H;
import de.fraunhofer.iais.dbc.DBC;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * the main class. Reads the properties. Setup of the Guice injector. Starts the (embedded) jetty server.
 */
public class ServerStarter {
    private static final Logger LOG = LoggerFactory.getLogger(ServerStarter.class);

    /**
     * the path to the resource with the server properties.
     */
    public static final String PROPERTY_PATH = "/server.properties"; //NOSONAR

    /**
     * read the command line arguments and call the server starter
     *
     * @param args command line arguments
     * @throws Exception and this will stop the server in case of intialization errors
     */
    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser();
        OptionSpec<String> defineOpt = parser.accepts("d").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);
        Server server = new ServerStarter().start(defineOpt.values(options));
        server.join();
    }

    /**
     * start the server:<br>
     * - jerseyHandler: create a servlet handler for REST services. Use guice for dependency injection.<br>
     * - staticHandler: create a servlet handler for the static resources.<br>
     * - create a http connector for the given ip and port<br>
     * - start the server
     *
     * @param ip the ip address of this server (e.g. 0.0.0.0)
     * @param port the port this server should listen to
     * @param defines definition from the command line which will replace the default from the server.properties file
     * @return the server created
     * @throws Exception and this will stop the server in case of intialization errors
     */
    Server start(List<String> defines) throws Exception {
        Properties propertiesMergedWithComandLine = loadAndMergeProperties(defines);
        MiniServerGuiceModule guiceModule = new MiniServerGuiceModule(propertiesMergedWithComandLine);

        HandlerList handlers = new HandlerList();

        ServletContextHandler jerseyHandler = new ServletContextHandler(handlers, "/rest", ServletContextHandler.NO_SESSIONS);
        jerseyHandler.addEventListener(new MiniServerGuiceServletContextListener(guiceModule));
        jerseyHandler.addFilter(GuiceFilter.class, "/*", null);
        jerseyHandler.addServlet(DefaultServlet.class, "/*");

        ServletContextHandler staticHandler = new ServletContextHandler(handlers, "/static", ServletContextHandler.NO_SESSIONS);
        ServletHolder staticHolder = new ServletHolder(DefaultServlet.class);
        staticHolder.setInitParameter("resourceBase", "staticResources");
        staticHolder.setInitParameter("dirAllowed", "true");
        staticHolder.setInitParameter("pathInfoOnly", "true");
        staticHandler.addServlet(staticHolder, "/*");

        String host = propertiesMergedWithComandLine.getProperty("self.address");
        int httpPort = Integer.parseInt(propertiesMergedWithComandLine.getProperty("self.http", "-1"));
        int httpsPort = Integer.parseInt(propertiesMergedWithComandLine.getProperty("self.https", "-1"));
        DBC.notNull(host, "host must NOT be null. Otherwise a server makes no sense at all :-)");
        DBC.isTrue(httpPort > 0 || httpsPort > 0, "either a http or a https port must be enabled. Otherwise a  server makes no sense at all :-)");

        String serverUrl = getServerUrlForLogging(host, httpPort, httpsPort);
        LOG.info("starting at " + serverUrl);
        Server server = new Server();

        List<ServerConnector> connectors = new ArrayList<>();
        if ( httpPort > 0 ) {
            ServerConnector http = new ServerConnector(server);
            http.setHost(host);
            http.setPort(httpPort);
            connectors.add(http);
        }
        if ( httpsPort > 0 ) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(ServerStarter.class.getResource("/keystore.jks").toExternalForm());
            sslContextFactory.setKeyStorePassword("MiniServer");
            sslContextFactory.setKeyManagerPassword("MiniServer");
            ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory());
            sslConnector.setHost(host);
            sslConnector.setPort(httpsPort);
            connectors.add(sslConnector);
        }
        server.setConnectors(connectors.toArray(new ServerConnector[0]));
        server.setHandler(handlers);
        server.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(true, serverUrl));
        return server;
    }

    /**
     * load the property file "server.properties".<br>
     * This methods loads the properties. It does NOT store them.<br>
     * It overrides the properties with the command line properties given as -d key:value -d key:value ...<br>
     * <br>
     * <b>If the property file cannot be loaded, the program TERMINATES with exit code 12.</b>
     *
     * @param defines the list of command line properties. Maybe empty. Maybe null.
     * @return the properties. Never null.
     */
    private static Properties loadAndMergeProperties(List<String> defines) {
        Properties serverProperties = new Properties();
        try {
            serverProperties.load(ServerStarter.class.getResourceAsStream(PROPERTY_PATH));
        } catch ( Exception e ) {
            LOG.error("Exception when loading properties from " + PROPERTY_PATH, e);
            System.exit(12);
        }
        if ( defines != null ) {
            for ( String define : defines ) {
                String[] property = H.splitter(define);
                if ( property == null ) {
                    LOG.info("command line property is invalid and thus ignored: {}", define);
                } else if ( H.notEmpty(property[1]) ) {
                    LOG.info("added property from command line: {}={}", property[0], property[0].contains("secret") ? "*" : property[1]);
                    serverProperties.put(property[0], property[1]);
                } else {
                    LOG.info("removed property from command line: {}", define);
                    serverProperties.remove(property[0]);
                }
            }
        }
        return serverProperties;
    }

    private static String getServerUrlForLogging(String host, int httpPort, int httpsPort) {
        StringBuilder sb = new StringBuilder();
        if ( httpPort > 0 ) {
            sb.append("http://").append(host).append(":").append(httpPort);
        }
        if ( httpPort > 0 && httpsPort > 0 ) {
            sb.append(" and ");
        }
        if ( httpsPort > 0 ) {
            sb.append("https://").append(host).append(":").append(httpsPort);
        }
        return sb.toString();
    }
}