package de.budde.guice;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class P1Injected {
    private final String p;

    @Inject
    public P1Injected(@Named("p1") String p) {
        this.p = p;
    }

    public String getP() {
        return this.p;
    }
}
