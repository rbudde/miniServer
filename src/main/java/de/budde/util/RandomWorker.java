package de.budde.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.fraunhofer.iais.dbc.DBC;

/**
 * Generates random numbers. The idea is, that for simulation purposes many clients share one object of this class (it behaves like a singleton :-).
 * This is to avoid that clients get all the same sequence of random numbers (this could hide errors)
 */
public class RandomWorker {
    private final Random rnd = new Random(42);

    /**
     * generate an random number from the interval [0,{limit})
     *
     * @param limit parameter to limit the generation to [0,{limit})
     * @return the random number
     */
    public int getRandom(int limit) {
        DBC.isTrue(limit > 0);
        return this.rnd.nextInt(limit);
    }

    /**
     * generate an array of {number} many integer random numbers from the interval [0,{limit})
     *
     * @param limit parameter to limit the generation to [0,{limit}), > 0
     * @param number of randoms to be generated, >= 0
     * @return the array, never null
     */
    public List<Integer> getManyRandoms(int limit, int number) {
        DBC.isTrue(limit > 0 && number >= 0);
        List<Integer> randoms = new ArrayList<>(number);
        for ( int i = 0; i < number; i++ ) {
            randoms.add(this.rnd.nextInt(limit));
        }
        return randoms;
    }
}