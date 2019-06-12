package de.budde.guice;

import java.util.Properties;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.budde.resources.HelloWorld;

public class GuiceModuleTest {

    /**
     * test the injection basics, independent from server requirements
     */
    @Test
    public void testWithTestProperties() throws Exception {
        // load the test properties and override one of them
        Properties properties = new Properties();
        properties.load(GuiceModuleTest.class.getResourceAsStream("/test.properties"));
        properties.put("p2", "property-2-overridden");

        // create the injector and then objects with constructor parameters injected
        MiniServerGuiceModule module = new MiniServerGuiceModule(properties);
        Injector injector = Guice.createInjector(module);
        P1Injected p1 = injector.getInstance(P1Injected.class);
        P2Injected p2 = injector.getInstance(P2Injected.class);

        // check, whether the injection is ok
        Assert.assertEquals("property-1", p1.getP());
        Assert.assertEquals("property-2-overridden", p2.getP());
    }

    /**
     * test injection close to server requirements
     */
    @Test
    public void testWithServerProperties() throws Exception {
        Properties testProperties = new Properties();
        testProperties.put("person.greeter", "Pid, the cavy");
        MiniServerGuiceModule module = new MiniServerGuiceModule(testProperties);
        MiniServerGuiceServletContextListener config = new MiniServerGuiceServletContextListener(module);
        Injector injector = config.getInjector();
        HelloWorld helloWorldInjected = injector.getInstance(HelloWorld.class); // the same as below: new HelloWorld("Pid, the cavy");
        HelloWorld helloWorldDirect = new HelloWorld("Pid, the cavy");
        check(helloWorldInjected);
        check(helloWorldDirect);
    }

    private void check(HelloWorld test) throws Exception {
        JSONObject entity = new JSONObject((String) test.doHelloWorldGet().getEntity());
        Assert.assertEquals("Pid, the cavy", entity.getString("from"));
        Assert.assertEquals("the world", entity.getString("to"));
    }

}
