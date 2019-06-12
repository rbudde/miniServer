package de.budde.resources;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import de.budde.param.GenericResponse;
import de.budde.param.RandomRequest;
import de.budde.param.RandomResponse;
import de.budde.util.NoSecurity;
import de.budde.util.RandomWorker;

public class RandomGeneratorTest {
    private final RandomWorker worker = new RandomWorker();

    @Test
    /**
     * test whether a GET returns a 200 response with a random number
     * Dependency injection makes testing easy: you supply the parameters that are injected in "production" by your DI framework
     */
    public void testGetRandom() {
        Response response = new RandomGenerator(this.worker, 50, 100, new NoSecurity(), null).getRandom();
        Assert.assertEquals(200, response.getStatus());
        RandomResponse entity = RandomResponse.make_1((String) response.getEntity());
        Assert.assertTrue(entity.getOk());
        Assert.assertEquals(1, entity.getRnd().size());
        Integer rnd = entity.getRnd().get(0);
        Assert.assertTrue(0 <= rnd && rnd < 100);
    }

    @Test
    /**
     * test whether a GET returns a 200 response with 50 random numbers between 0 and 100(excl.).<br>
     * Dependency injection makes testing easy: you supply the parameters that are injected in "production" by your DI framework
     */
    public void testGetManyRandoms() {
        Response response = new RandomGenerator(this.worker, 1000000, 2000000, new NoSecurity(), null).getManyRandoms(50, 100);
        checkResponseWhenOk(50, 100, response);
    }

    @Test
    /**
     * test whether a GET returns an error response when parameter are set wrong
     */
    public void testGetManyRandomsError() {
        Response response = new RandomGenerator(this.worker, 100, 200, new NoSecurity(), null).getManyRandoms(110, 199);
        checkResponseWhenError(response);
        response = new RandomGenerator(this.worker, 100, 200, new NoSecurity(), null).getManyRandoms(99, 210);
        checkResponseWhenError(response);
        response = new RandomGenerator(this.worker, 100, 200, new NoSecurity(), null).getManyRandoms(99, -5);
        checkResponseWhenError(response);
        response = new RandomGenerator(this.worker, 100, 200, new NoSecurity(), null).getManyRandoms(-5, 199);
        checkResponseWhenError(response);
    }

    @Test
    /**
     * test whether a POST returns a 200 response with 12 random numbers between 0 and 8(excl.)<br>
     * Dependency injection makes testing easy: you supply the parameters that are injected in "production" by your DI framework
     */
    public void testPostManyRandoms() {
        RandomRequest rr = RandomRequest.make_1().setLimit(8).setNumber(12).immutable();
        Response response = new RandomGenerator(this.worker, 100, 100, new NoSecurity(), null).postManyRandoms(rr);
        checkResponseWhenOk(8, 12, response);
    }

    private void checkResponseWhenOk(int limit, int number, Response response) {
        Assert.assertEquals(200, response.getStatus());
        RandomResponse entity = RandomResponse.make_1((String) response.getEntity());
        Assert.assertTrue(entity.getOk());
        Assert.assertEquals(number, entity.getRnd().size());
        for ( Integer rnd : entity.getRnd() ) {
            Assert.assertTrue(0 <= rnd && rnd < limit);
        }
    }

    private void checkResponseWhenError(Response response) {
        Assert.assertEquals(400, response.getStatus());
        GenericResponse entity = GenericResponse.make_1((String) response.getEntity());
        Assert.assertFalse(entity.getOk());
    }
}
