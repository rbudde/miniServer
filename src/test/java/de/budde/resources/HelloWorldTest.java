package de.budde.resources;

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class HelloWorldTest {

    @Test
    /**
     * test of REST service HelloWorld::getHw. Call the service and inspect the returnes entity<br>
     * - 200 OK is expected<br>
     * - entity contains the expected substring
     */
    public void test() throws Exception {
        Response response = new HelloWorld("Reinhard").doHelloWorldGet();
        Assert.assertEquals(200, response.getStatus());
        JSONObject entity = new JSONObject((String) response.getEntity());
        Assert.assertEquals("Reinhard", entity.getString("from"));
        Assert.assertEquals("the world", entity.getString("to"));
        Assert.assertTrue(entity.has("rnd"));
    }
}