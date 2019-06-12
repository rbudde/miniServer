package de.budde.guice;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class MiniServerGuiceServletContextListener extends GuiceServletContextListener {
    private final Injector injector;

    public MiniServerGuiceServletContextListener(AbstractModule... modules) {
        JerseyServletModule jerseyServletModule = new JerseyServletModule() {
            @Override
            protected void configureServlets() {
                for ( AbstractModule module : modules ) {
                    install(module);
                }
                Map<String, String> initParams = new HashMap<>();
                // initParams.put("com.sun.jersey.config.feature.Trace", "true");
                initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
                String commaSeparatedPackages = "de.budde.resources,de.budde.provider";
                initParams.put("com.sun.jersey.config.property.packages", commaSeparatedPackages);
                serve("/*").with(GuiceContainer.class, initParams);
            }
        };
        this.injector = Guice.createInjector(jerseyServletModule);
    }

    @Override
    protected Injector getInjector() {
        return this.injector;
    }
}
