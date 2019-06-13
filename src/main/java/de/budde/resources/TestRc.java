package de.budde.resources;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a REST resource for testing purposes. Generates a response with various status'
 *
 * @author rbudde
 */
@Path("/rc")
public class TestRc {
    private static final Logger LOG = LoggerFactory.getLogger(TestRc.class);

    /**
     * generate a response with status 200 and an JSON object as entity
     *
     * @return the response generated
     * @throws JSONException
     * @throws Exception
     */
    @GET
    @Path("/200")
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle200() throws JSONException {
        LOG.info("200 is called");
        JSONObject answer = new JSONObject().put("path", "200").put("date", new Date());
        return Response.ok(answer.toString()).build();
    }

    /**
     * generate a response with status 418 and an JSON object as entity<br>
     * see https://www.ietf.org/rfc/rfc2324.txt<br>
     * Use it, because any attempt to brew coffee with a teapot should result in the error code "418 I'm a teapot"
     *
     * @return the response generated
     * @throws JSONException
     */
    @GET
    @Path("/418")
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle418() throws JSONException {
        LOG.info("418 is called");
        JSONObject answer = new JSONObject().put("path", "418").put("date", new Date());
        return Response.status(418).entity(answer.toString()).build();
    }

    /**
     * generate a response with status 500 and an JSON object as entity (yes, this is possible)
     *
     * @return the response generated
     * @throws Exception
     */
    @GET
    @Path("/500")
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle500() throws JSONException {
        LOG.info("500 is called");
        JSONObject answer = new JSONObject().put("path", "500").put("date", new Date());
        return Response.status(500).entity(answer.toString()).build();
    }
}