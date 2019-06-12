package de.budde.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.iais.dbc.DBC;
import de.fraunhofer.iais.dbc.DBCException;

/**
 * wrapper of class HttpClient. Efficient management of connections, http and https support. Basic authentification.
 */
public class HttpClientWrapper {
    private static final String UTF_8 = "UTF-8";
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientWrapper.class);
    private static final String APP_JSON = "application/json";
    private static final ContentType CONTENT_TYPE = ContentType.create(APP_JSON, UTF_8);

    private final HttpClient client;
    private final String url;
    private final IdleConnectionMonitorThread idleConnectionMonitorThread;
    private RequestConfig requestConfig;

    final Credentials creds;

    /**
     * create the client. Setup both a HHTP and HTTPS connection factory. If user & pwd are not null, an authorization header is added to each request. If both
     * proxy* parameter are not null, the proxy server is added to the route planner<br>
     * - user and pwd either both null or both not null
     * - proxyDns and proxyPort either both null/0 or both not null/<>0
     *
     * @param url to send the requests to. Optional, thus may be null
     * @param user for UsernamePasswordCredentials
     * @param pwd for UsernamePasswordCredentials
     * @param proxyDns name of the proxy server (in most cases null)
     * @param proxyPort port of the proxy server (in most cases 0)
     */
    public HttpClientWrapper(String url, String user, String pwd, String proxyDns, int proxyPort) {
        this.url = url;
        if ( user == null && pwd == null ) {
            this.creds = null;
        } else if ( user != null && pwd != null ) {
            this.creds = new UsernamePasswordCredentials(user, pwd);
        } else {
            throw new DBCException("user / pwd either both null or both not null: " + user + " - " + pwd);
        }
        if ( (proxyDns == null && proxyPort == 0) || (proxyDns != null && proxyPort > 0) ) {
            // ok
        } else {
            throw new DBCException("proxyDns / proxyPort either both null/0 or both not null/>0: " + proxyDns + " - " + proxyPort);
        }

        this.requestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000).build();

        SSLContext sslContext = buildSSLContext();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                LOG.info("accepting SSLConnection for host: {}, port: {}", session.getPeerHost(), session.getPeerPort());
                return true;
            }
        });

        ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Charset.forName(UTF_8)).build();
        PlainConnectionSocketFactory plainSF = PlainConnectionSocketFactory.getSocketFactory();

        RegistryBuilder<ConnectionSocketFactory> regBuilder = RegistryBuilder.<ConnectionSocketFactory> create();
        Registry<ConnectionSocketFactory> registry = regBuilder.register("http", plainSF).register("https", sslsf).build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(100);
        connectionManager.setValidateAfterInactivity(10000); // good value for the check interval?
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(connectionManager);

        if ( proxyDns != null ) {
            // add one step proxy support
            HttpHost proxy = new HttpHost(proxyDns, proxyPort);
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(routePlanner);
        }

        this.client = httpClientBuilder.build();
        this.idleConnectionMonitorThread = new IdleConnectionMonitorThread(connectionManager);
        new Thread(null, this.idleConnectionMonitorThread, "IdleConn").start();
    }

    /**
     * shutdown the connections
     */
    public void shutdown() {
        this.idleConnectionMonitorThread.shutdown();
    }

    /**
     * POST request to the url as taken from the constructor call
     *
     * @param requestEntity payload, may be null
     * @return the response entity as string
     */
    public String post(Object requestEntity) {
        return postImpl(this.url, requestEntity);
    }

    /**
     * POST request
     *
     * @param url target of the request, never null
     * @param requestEntity payload, may be null
     * @return the response entity as string
     */
    public String post(String url, Object requestEntity) {
        return postImpl(mkUrl(url), requestEntity);
    }

    /**
     * GET request to the url as taken from the constructor call
     *
     * @param url target of the request, never null
     * @return the response entity as string
     * @throws Exception if something goes wrong
     */
    public String get() {
        return getImpl(this.url);
    }

    /**
     * GET request
     *
     * @param url target of the request, never null
     * @return the response entity as string
     * @throws Exception if something goes wrong
     */
    public String get(String url) {
        return getImpl(mkUrl(url));
    }

    private SSLContext buildSSLContext() {
        try {
            SSLContextBuilder builder = SSLContexts.custom();
            TrustStrategy trustStrategy = (X509Certificate[] chain, String authType) -> {
                for ( X509Certificate x509Certificate : chain ) {
                    X500Principal subject = x509Certificate.getSubjectX500Principal();
                    X500Principal issuer = x509Certificate.getIssuerX500Principal();
                    LOG.info("using certificate with X500 subject: " + subject + ", X500 issuer:  " + issuer);
                    x509Certificate.checkValidity(new Date());
                }
                return true;
            };
            builder.loadTrustMaterial(null, trustStrategy);
            SSLContext sslContext = builder.build();
            return sslContext;
        } catch ( KeyManagementException | NoSuchAlgorithmException | KeyStoreException e ) {
            throw new DBCException("Exception during build of SSL context", e);
        }
    }

    private String postImpl(String url, Object requestEntity) {
        DBC.notNull(url);
        HttpPost post = new HttpPost(url);
        post.setConfig(this.requestConfig);
        post.addHeader(HttpHeaders.ACCEPT, APP_JSON);
        HttpEntity httpEntity = null;
        if ( requestEntity instanceof File ) {
            httpEntity = new FileEntity((File) requestEntity, CONTENT_TYPE);
        } else if ( requestEntity instanceof String ) {
            httpEntity = new StringEntity((String) requestEntity, CONTENT_TYPE);
        } else if ( requestEntity != null ) {
            LOG.error("Entity is ignored. It should be either a file or a String, but is a " + requestEntity.getClass().getName());
        }
        if ( httpEntity != null ) {
            post.setEntity(httpEntity);
        }
        return mkRequest(this.client, post);
    }

    private String getImpl(String url) {
        DBC.notNull(url);
        HttpGet get = new HttpGet(url);
        get.setConfig(this.requestConfig);
        get.addHeader(HttpHeaders.ACCEPT, APP_JSON);
        addAuthHeaderOpt(this.creds, get);
        return mkRequest(this.client, get);
    }

    private String mkUrl(String url) {
        DBC.notNull(url);
        if ( this.url == null ) {
            return url;
        } else {
            return this.url + url;
        }
    }

    static void addAuthHeaderOpt(Credentials creds, HttpRequest req) {
        if ( creds != null ) {
            try {
                req.addHeader(new BasicScheme().authenticate(creds, req, null));
            } catch ( AuthenticationException e ) {
                throw new DBCException("authentication header could not be build", e);
            }
        }
    }

    static String mkRequest(HttpClient client, HttpUriRequest uriRequest) {
        try {
            HttpResponse response = client.execute(uriRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            DBC.isTrue(statusCode == 200, "status of http response is not 200: " + statusCode);
            return httpEntityToString(response.getEntity());
        } catch ( IOException e ) {
            throw new DBCException("request failed: " + uriRequest, e);
        }
    }

    /**
     * prepare status message to inform about errors from the server side
     *
     * @param statusLine
     *        from {@link HttpResponse}, maybe null, only read
     * @return null, if no error occured, an error message otherwise
     */
    static String mkMessage(StatusLine statusLine) {
        String msg = null;
        if ( statusLine == null ) {
            msg = "no response from server";
        } else if ( statusLine.getStatusCode() >= 300 ) {
            msg = "status code from server: " + statusLine.getStatusCode();
        }
        return msg;
    }

    static String httpEntityToString(HttpEntity responseEntityObject) throws IOException {
        try {
            if ( responseEntityObject != null ) {
                try (InputStream responseEntityStream = responseEntityObject.getContent()) {
                    return convertStreamToString(responseEntityStream);
                }
            } else {
                return null;
            }
        } finally {
            EntityUtils.consume(responseEntityObject);
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        try (java.util.Scanner scanner = new java.util.Scanner(is, UTF_8)) {
            java.util.Scanner s = scanner.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }

    /**
     * responsible to remove expired or closed connections from the connection pool.<br>
     * You may have to adjust the timing of the IdleConnectionMonitorThread
     */
    public static class IdleConnectionMonitorThread implements Runnable {
        private static final long CHECK_INTERVAL_MSEC = 15000;
        private final HttpClientConnectionManager cm;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connectionManager) {
            this.cm = connectionManager;
        }

        @Override
        public synchronized void run() {
            while ( !this.shutdown ) {
                try {
                    wait(CHECK_INTERVAL_MSEC);
                } catch ( InterruptedException e ) {
                    // https://stackoverflow.com/questions/1087475/when-does-javas-thread-sleep-throw-interruptedexception
                    Thread.currentThread().interrupt();
                }
                this.cm.closeExpiredConnections(); // Close expired connections
                this.cm.closeIdleConnections(30, TimeUnit.SECONDS); // Optionally, close connections that have been idle longer than 30 sec
            }
        }

        public synchronized void shutdown() {
            this.shutdown = true;
            notifyAll();
        }
    }
}