package de.budde.jetty;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.budde.util.HttpClientWrapper;
import de.budde.util.TestHelper;

public class RestHttpIT {
    private static final String ADDRESS = "0.0.0.0";
    private static final String HTTP_PORT = "1998";
    private static final String HTTP_URL = "http://" + ADDRESS + ":" + HTTP_PORT + "/rest/";

    private static Server httpServer;
    private static HttpClientWrapper cwHttp;

    @BeforeClass
    /**
     * setup a server and a httpclient for serving http
     */
    public static void setUp() throws Exception {
        List<String> genericDefines = new ArrayList<>();
        genericDefines.add("self.address=" + ADDRESS);
        genericDefines.add("random.limit=200");
        genericDefines.add("random.number=300");
        genericDefines.add("delegate.address=" + ADDRESS);

        List<String> httpDefines = new ArrayList<>(genericDefines);
        httpDefines.add("self.http=" + HTTP_PORT);
        httpDefines.add("self.https=");
        httpDefines.add("delegate.protocol=http");
        httpDefines.add("delegate.port=" + HTTP_PORT);
        httpDefines.add("security.secret.16=1305199829121999");
        httpServer = new ServerStarter().start(httpDefines);
        cwHttp = new HttpClientWrapper(HTTP_URL, null, null, null, 0);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        httpServer.stop();
        cwHttp.shutdown();
    }

    @Test
    public void testHttpGet() throws Exception {
        String helloResponse = cwHttp.get("simple/hello");
        String rndResponse = cwHttp.get("json/rnd");
        String rndsResponse = cwHttp.get("json/rnds/15/15");
        assertTrue(helloResponse.contains("Pid, the cavy"));
        assertTrue(helloResponse.contains("Hello!"));
        assertTrue(rndResponse.contains("random generated"));
        assertTrue(rndResponse.contains("rnd"));
        assertTrue(rndsResponse.contains("15 random numbers generated. Interval: [0,15)"));
        assertTrue(rndsResponse.contains("rnd"));
    }

    @Test
    public void testHttpPostRnds() throws Exception {
        JSONObject o = TestHelper.jo("{'limit':150,'number':50}");
        String rndsResponse = cwHttp.post("json/rnds", o.toString());
        assertTrue(rndsResponse.contains("50 random numbers generated. Interval: [0,150)"));
        assertTrue(rndsResponse.contains("rnd"));
    }

    @Test
    public void testHttpPostDelegate() throws Exception {
        JSONObject o = TestHelper.jo("{'limit':150,'number':50}");
        String rndsResponse = cwHttp.post("json/delegate", o.toString());
        assertTrue(rndsResponse.contains("50 random numbers generated. Interval: [0,150)"));
        assertTrue(rndsResponse.contains("rnd"));
    }
}
