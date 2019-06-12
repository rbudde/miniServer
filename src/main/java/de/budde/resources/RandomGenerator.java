package de.budde.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.budde.param.GenericResponse;
import de.budde.param.RandomRequest;
import de.budde.param.RandomResponse;
import de.budde.util.HttpClientWrapper;
import de.budde.util.ISecurity;
import de.budde.util.RandomWorker;

/**
 * a REST resource which<br>
 * - generates random numbers or<br>
 * - delegates the generation to another server.
 */
@Path("/json")
public class RandomGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(RandomGenerator.class);

    private final RandomWorker worker;
    private final int limit;
    private final int number;
    private final ISecurity security;
    private final HttpClientWrapper clientWrapper;

    @Inject
    public RandomGenerator(
        RandomWorker worker,
        @Named("random.limit") int limit,
        @Named("random.number") int number,
        ISecurity security,
        HttpClientWrapper clientWrapper) //
    {
        this.worker = worker;
        this.limit = limit;
        this.number = number;
        this.security = security;
        this.clientWrapper = clientWrapper;
    }

    /**
     * generate an integer random number from the interval [0,100)
     *
     * @return an response with an entity with the random number generated, never null
     */
    @Path("/rnd")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRandom() {
        LOG.info("GET /json/rnd request");
        int random = this.worker.getRandom(this.limit);
        RandomResponse rr = RandomResponse.make_1().addRnd(random).setOk(true).setMsg("random generated").immutable();
        return Response.ok(rr.toJson().toString()).build();
    }

    /**
     * generate an array of {number} many integer random numbers from the interval [0,{limit}). Both parameters are part of the URI.<br>
     * <b>pre:</b> 0 < limit <= max limit as set in the constructor<br>
     * <b>pre:</b> 0 < number <= max number as set in the constructor<br>
     *
     * @return an response with an entity with the array of random numbers generated or an error description if a precondition fails, never null
     */
    @Path("/rnds/{limit}/{number}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getManyRandoms(@PathParam("limit") int limit, @PathParam("number") int number) {
        LOG.info("GET /json/rnd/{}/{} request", limit, number);
        return createRandomResponse(limit, number);
    }

    /**
     * generate an array of {number} many integer random numbers from the interval [0,{limit}). Both parameters are given in a JSON request entity as
     * properties.<br>
     * <b>pre:</b> the request entity is valid JSON with properties "limit" and "number"<br>
     * <b>pre:</b> 0 < limit <= max limit as set in the constructor<br>
     * <b>pre:</b> 0 < number <= max number as set in the constructor<br>
     *
     * @return an response with an entity containing the array of random numbers generated or an error description if a precondition fails, never null
     */
    @Path("/rnds")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postManyRandoms(RandomRequest request) {
        LOG.info("POST /rnds request with entity {}", request);
        return createRandomResponse(request.getLimit(), request.getNumber());
    }

    /**
     * get a request with a JSON entity and delegate it to a server. The server's URI is created in the constructor
     * {@link #JsonWork(String, String, HttpClientWrapper)}. Eventually the URI is retrieved from server properties).
     *
     * @param requestEntity JSON object describing the service to be delegated
     * @return the response delivered by the delegation server, never null
     * @throws Exception
     */
    @Path("/delegate")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postDelegate(String requestEntity) {
        String encrypted = this.security.fromClearText(requestEntity);
        LOG.info("POST /delegate entity {} encrypted as {}", requestEntity, encrypted);
        String responseEntity = this.clientWrapper.post(encrypted);
        return Response.ok(responseEntity).build();
    }

    /**
     * see {@link RandomGenerator#postManyRandoms}
     *
     * @return an response with an entity containing the array of random numbers generated or an error description if a precondition fails, never null
     */
    @Path("/rndsE")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postManyRandomsEncrypted(String requestEntity) {
        // TODO: this could be designed better. Write a specific provider? Same problem with #postDelegate
        String decrypted = this.security.toClearText(requestEntity);
        LOG.info("POST /rndsE request with entity {} decrypted from {}", decrypted, requestEntity);
        RandomRequest request = RandomRequest.make_1(decrypted);
        return createRandomResponse(request.getLimit(), request.getNumber());
    }

    /**
     * create a Response object with an entity that either contains an array of integer random numbers or a GenericResponse object with an error message.
     *
     * @param limit force that the generated numbers are in the interval [0,limit). 0 < limit <= this.limit
     * @param number the number of generated random numbers. 0 < number <= this.number
     * @return a Response object, never null
     */
    private Response createRandomResponse(int limit, int number) {
        if ( limit > 0 && limit <= this.limit && number > 0 && number <= this.number ) {
            List<Integer> randoms = this.worker.getManyRandoms(limit, number);
            RandomResponse rr = RandomResponse.make_1().setRnd(randoms).setOk(true).setMsg(number + " random numbers generated. Interval: [0," + limit + ")");
            return Response.ok(rr.immutable().toJson().toString()).build();
        } else {
            GenericResponse gr = GenericResponse.make_1();
            gr.setOk(false).setMsg("limit or number are invalid").setDetail("number=" + number + ", limit=" + limit);
            return Response.status(400).entity(gr.immutable().toJson().toString()).build();
        }
    }
}