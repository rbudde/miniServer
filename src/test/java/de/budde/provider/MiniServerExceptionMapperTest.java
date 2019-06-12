package de.budde.provider;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import de.budde.param.GenericResponse;
import de.fraunhofer.iais.dbc.DBCException;

public class MiniServerExceptionMapperTest {

    @Test
    public void testToResponse() {
        String errorMsg = "Test message!";
        Response resp = new DbcExceptionMapper().toResponse(new DBCException(errorMsg));
        Assert.assertEquals(500, resp.getStatus());
        GenericResponse entity = GenericResponse.make_1((String) resp.getEntity());
        Assert.assertEquals(false, entity.getOk());
        Assert.assertEquals(errorMsg, entity.getMsg());
    }

    @Test
    public void testErrorInError() {
        GenericResponse entity = GenericResponse.make_1(DbcExceptionMapper.ERROR_IN_ERROR);
        Assert.assertEquals(false, entity.getOk());
        Assert.assertEquals("Error in error processor :-)", entity.getMsg());
    }

}
