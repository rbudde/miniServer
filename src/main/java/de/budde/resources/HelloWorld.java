package de.budde.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * REST resource answering to a /hello request
 */
@Path("/simple")
public class HelloWorld {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorld.class);
    private static final Random RANDOM = new Random(42);
    private static final Map<String, String> BOOK = new HashMap<>();
    static {
        BOOK.put("cavy", "Meerschweinchen");
        BOOK.put("hello", "Hallo");
        BOOK.put("translate", "übersetzen");
    }
    private final String greeter;

    /**
     * when the object is created (this happens when a request for this resource hits the server), jetty/guice will call this constructor
     * and inject the String bound to the property with key @Named
     *
     * @param greeter
     */
    @Inject
    public HelloWorld(@Named("person.greeter") String greeter) {
        this.greeter = greeter;
    }

    /**
     * say hello
     *
     * @return an JSON object as entity. Never null. The entity contains the greeting and a random number.
     * @throws JSONException
     */
    @Path("/hello")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response doHelloWorldGet() throws JSONException {
        LOG.info("GET /rest/simple/hello is called");
        JSONObject resp = new JSONObject();
        resp.put("service", "greet").put("say", "Hello!").put("from", this.greeter).put("to", "the world").put("rnd", RANDOM.nextInt(100));
        return Response.ok(resp.toString(2)).build();
    }

    /**
     * translate
     *
     * @return an JSON object as entity. Never null. The entity contains the translation of a word or an excuse if the word is not in the dictionary.
     * @throws JSONException
     */
    @Path("/translate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response doTranslatePost(String requestEntity) throws JSONException {
        LOG.info("POST /rest/simple/translate is called");
        String from = requestEntity.trim();
        JSONObject resp = new JSONObject().put("service", "translation").put("from", from).put("to", translate(from));
        return Response.ok(resp.toString(2)).build();
    }

    private String translate(String from) {
        if ( from == null || from.isEmpty() ) {
            return "null";
        } else {
            String to = BOOK.get(from);
            if ( to == null ) {
                return "sorry, don't know";
            } else {
                return to;
            }
        }
    }
}