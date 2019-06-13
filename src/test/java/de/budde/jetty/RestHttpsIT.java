package de.budde.jetty;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.budde.util.HttpClientWrapper;
import de.budde.util.TestHelper;

@Ignore
public class RestHttpsIT {
    private static final String ADDRESS = "0.0.0.0";
    private static final String HTTPS_PORT = "2998";
    private static final String HTTPS_URL = "https://" + ADDRESS + ":" + HTTPS_PORT + "/rest/";

    private static Server httpsServer;
    private static HttpClientWrapper cwHttps;

    @BeforeClass
    /**
     * setup a server and a httpclient for serving https
     */
    public static void setUp() throws Exception {
        List<String> genericDefines = new ArrayList<>();
        genericDefines.add("self.address=" + ADDRESS);
        genericDefines.add("random.limit=200");
        genericDefines.add("random.number=300");
        genericDefines.add("delegate.address=" + ADDRESS);

        List<String> httpsDefines = new ArrayList<>(genericDefines);
        httpsDefines.add("self.http=");
        httpsDefines.add("self.https=" + HTTPS_PORT);
        httpsDefines.add("delegate.protocol=https");
        httpsDefines.add("delegate.port=" + HTTPS_PORT);

        httpsServer = new ServerStarter().start(httpsDefines);
        cwHttps = new HttpClientWrapper(HTTPS_URL, null, null, null, 0);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        httpsServer.stop();
        cwHttps.shutdown();
    }

    @Test
    public void testHttpsGet() throws Exception {
        String helloResponse = cwHttps.get("simple/hello");
        String rndResponse = cwHttps.get("json/rnd");
        String rndsResponse = cwHttps.get("json/rnds/15/15");
        assertTrue(helloResponse.contains("Pid, the cavy"));
        assertTrue(helloResponse.contains("Hello!"));
        assertTrue(rndResponse.contains("random generated"));
        assertTrue(rndResponse.contains("rnd"));
        assertTrue(rndsResponse.contains("15 random numbers generated. Interval: [0,15)"));
        assertTrue(rndsResponse.contains("rnd"));
    }

    @Test
    public void testHttpsPostRnds() throws Exception {
        JSONObject o = TestHelper.jo("{'limit':150,'number':50}");
        String rndsResponse = cwHttps.post("json/rnds", o.toString());
        assertTrue(rndsResponse.contains("50 random numbers generated. Interval: [0,150)"));
        assertTrue(rndsResponse.contains("rnd"));
    }

    @Test
    public void testHttpsPostDelegate() throws Exception {
        JSONObject o = TestHelper.jo("{'limit':150,'number':50}");
        String rndsResponse = cwHttps.post("json/delegate", o.toString());
        assertTrue(rndsResponse.contains("50 random numbers generated. Interval: [0,150)"));
        assertTrue(rndsResponse.contains("rnd"));
    }
}
