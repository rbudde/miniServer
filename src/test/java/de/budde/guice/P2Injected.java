package de.budde.guice;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class P2Injected {
    private final String p;

    @Inject
    public P2Injected(@Named("p2") String p) {
        this.p = p;
    }

    public String getP() {
        return this.p;
    }
}
