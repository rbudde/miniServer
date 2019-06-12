package de.budde.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import de.budde.param.RandomRequest;

@Provider
@Consumes("application/json")
public class RandomDataProvider implements MessageBodyReader<RandomRequest> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == RandomRequest.class;
    }

    @Override
    public RandomRequest readFrom(
        Class<RandomRequest> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders,
        InputStream entityStream)
        throws IOException,
        WebApplicationException {

        String entity = convertStreamToString(entityStream);
        return RandomRequest.make_1(entity);
    }

    static String convertStreamToString(InputStream is) {
        try (Scanner s = new Scanner(is)) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }
}