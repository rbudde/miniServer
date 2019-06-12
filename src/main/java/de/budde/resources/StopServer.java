package de.budde.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST resource stopping the server when /stop is called - for demonstration purposes only.
 */
@Path("/stop")
public class StopServer {
    private static final Logger LOG = LoggerFactory.getLogger(StopServer.class);

    /**
     * process the request to stop the server. No response, of course. Use as exit code the parameter supplied in the call.
     * The parameter must be >=0 and <=16, otherwise 1 is taken.
     *
     * @return null
     */
    @GET
    @Path("/{exitCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stop(@PathParam("exitCode") int exitCode) {
        LOG.info("/stop/{} is called", exitCode);
        System.exit(exitCode >= 0 && exitCode <= 16 ? exitCode : 1);
        return null;
    }
}