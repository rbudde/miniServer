package de.budde.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.budde.param.GenericResponse;
import de.fraunhofer.iais.dbc.DBCException;

@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Provider
public class DbcExceptionMapper implements ExceptionMapper<DBCException> {
    private static final Logger LOG = LoggerFactory.getLogger(DbcExceptionMapper.class);
    static final String ERROR_IN_ERROR = "{\"ok\":false,\"_version\":\"1\",\"msg\":\"Error in error processor :-)\"}";

    @Override
    public Response toResponse(DBCException e1) {
        LOG.error("server error - exception was caught", e1);
        try {
            GenericResponse gr = GenericResponse.make_1().setOk(false).setMsg(e1.getMessage() == null ? "no message" : e1.getMessage()).immutable();
            return Response.serverError().entity(gr.toJson().toString(2)).build();
        } catch ( Exception e2 ) {
            LOG.error("server error - exception in exception processor", e2);
            return Response.ok(ERROR_IN_ERROR).build();
        }
    }
}