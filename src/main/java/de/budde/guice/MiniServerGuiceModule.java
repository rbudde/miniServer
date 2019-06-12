package de.budde.guice;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.budde.util.HttpClientWrapper;
import de.budde.util.ISecurity;
import de.budde.util.NoSecurity;
import de.budde.util.RandomWorker;
import de.budde.util.StrongSecurity;

public class MiniServerGuiceModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(MiniServerGuiceModule.class);
    private final Properties serverProperties;

    public MiniServerGuiceModule(Properties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    protected void configure() {
        try {
            // example of binding: bind(clazz.class).in(interface.class);
            // example of binding: bind(new TypeLiteral<Map<String, Clazz>>() {}).annotatedWith(Names.named("AnnotationWithout@")).toInstance(singletonObject);
            Names.bindProperties(binder(), this.serverProperties); // bind all properties from the property file (1)
            String protocol = this.serverProperties.getProperty("delegate.protocol"); // construct object to inject for delegation (2)
            String address = this.serverProperties.getProperty("delegate.address");
            String port = this.serverProperties.getProperty("delegate.port");
            String url = protocol + "://" + address + ":" + port;
            String urlToDelegateTo = url + "/rest/json/rndsE/";
            bind(HttpClientWrapper.class).toInstance(new HttpClientWrapper(urlToDelegateTo, null, null, null, 0)); // no credentials
            String secret = this.serverProperties.getProperty("security.secret.16"); // bind secret depending on the existance of a property (3)
            ISecurity security = secret == null ? new NoSecurity() : new StrongSecurity(secret, "AES");
            bind(ISecurity.class).toInstance(security);
            bind(RandomWorker.class).toInstance(new RandomWorker()); // no credentials
        } catch ( Exception e ) {
            LOG.error("Could not configure the dependency injector", e);
            System.exit(12);
        }
    }
}