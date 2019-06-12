package de.budde.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.fraunhofer.iais.dbc.DBCException;

public class RandomWorkerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * DBC: precondition failed. Throw exception
     */
    @Test
    public void test1() {
        this.thrown.expect(DBCException.class);
        this.thrown.expectMessage("DBC violation");
        new RandomWorker().getRandom(0);
    }

    /**
     * DBC: preconditions are respected in the result
     */
    @Test
    public void test2() {
        RandomWorker worker = new RandomWorker();
        for ( int i = 0; i < 100; i++ ) {
            int random = worker.getRandom(10);
            assertTrue(random >= 0 && random < 10);
        }
    }

    /**
     * DBC: precondition failed. Throw exception
     */
    @Test
    public void test3() {
        this.thrown.expect(DBCException.class);
        this.thrown.expectMessage("DBC violation");
        new RandomWorker().getManyRandoms(0, 100);
    }

    /**
     * DBC: precondition failed. Throw exception
     */
    @Test
    public void test4() {
        this.thrown.expect(DBCException.class);
        this.thrown.expectMessage("DBC violation");
        new RandomWorker().getManyRandoms(10, -1);
    }

    /**
     * DBC: preconditions are respected in the result
     */
    @Test
    public void test5() {
        RandomWorker worker = new RandomWorker();
        for ( int i = 0; i < 100; i++ ) {
            List<Integer> randoms = worker.getManyRandoms(20, 50);
            for ( Integer random : randoms ) {
                assertTrue(random >= 0 && random < 20);
            }
        }
    }

    @Test
    /**
     * workers are independant from each other, i.e. no global data is used
     */
    public void test6() {
        RandomWorker worker1 = new RandomWorker();
        RandomWorker worker2 = new RandomWorker();
        List<Integer> deterministicRandoms = Arrays.asList(20, 3, 18, 14, 0, 25, 5, 8, 19, 23);
        for ( Integer deterministicRandom : deterministicRandoms ) {
            assertTrue(deterministicRandom == worker1.getRandom(30));
            assertTrue(deterministicRandom == worker2.getRandom(30));
        }
    }
}