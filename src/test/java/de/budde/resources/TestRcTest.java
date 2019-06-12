package de.budde.resources;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

public class TestRcTest {

    @Test
    /**
     * test 200
     */
    public void test200() throws Exception {
        Response response = new TestRc().handle200();
        Assert.assertEquals(200, response.getStatus());
        String entity = (String) response.getEntity();
        Assert.assertTrue(entity.contains("\"path\":\"200\""));
    }

    @Test
    /**
     * test 418
     */
    public void test418() throws Exception {
        Response response = new TestRc().handle418();
        Assert.assertEquals(418, response.getStatus());
        String entity = (String) response.getEntity();
        Assert.assertTrue(entity.contains("\"path\":\"418\""));
    }

    @Test
    /**
     * test 500
     */
    public void test500() throws Exception {
        Response response = new TestRc().handle500();
        Assert.assertEquals(500, response.getStatus());
        String entity = (String) response.getEntity();
        Assert.assertTrue(entity.contains("\"path\":\"500\""));
    }

}
