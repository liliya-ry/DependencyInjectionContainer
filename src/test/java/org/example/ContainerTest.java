package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import java.util.Properties;

import org.example.TestClasses.*;
import org.example.exceptions.*;

class ContainerTest {
    Container container;

    @BeforeEach
    void init() {
        container = new Container();
    }

    @Test
    void autoInject() throws Exception {
        B inst = container.getInstance(B.class);
        assertNotNull(inst);
        assertNotNull(inst.getaField());
    }

    @Test
    void injectImplementation() throws Exception {
        container.registerImplementation(A.class);
        B inst = container.getInstance(B.class);

        assertNotNull(inst);
        assertNotNull(inst.getaField());
    }

    @Test
    void injectInstance() throws Exception {
        A a = new A();
        container.registerInstance(a);
        B inst = container.getInstance(B.class);

        assertNotNull(inst);
        assertSame(a, inst.getaField());
    }

    @Test
    void injectNamedInstance() throws Exception {
        A a = new A();
        container.registerInstance("iname", a);
        F inst = container.getInstance(F.class);

        assertNotNull(inst);
        assertSame(a, inst.iname);
    }

    @Test
    void injectStringProperty() throws Exception {
        Properties properties = new Properties();
        properties.put("percent", "99.9");
        container = new Container(properties);

        String email = "name@yahoo.com";
        container.registerInstance("email", email);
        FS inst = container.getInstance(FS.class);

        assertNotNull(inst);
        assertNotNull(inst.email);
        assertSame(inst.email, email);
    }

    @Test
    void constructorInject() throws Exception {
        Properties properties = new Properties();
        properties.put("percent", "99.9");
        container = new Container(properties);

        E inst = container.getInstance(E.class);
        assertNotNull(inst);
        assertNotNull(inst.aField);
    }

    @Test
    void injectInterface() throws Exception {
        container.registerImplementation(AI.class, A.class);
        B inst = container.getInstance(B.class);
        assertNotNull(inst);
        assertNotNull(inst.getaField());
    }

    @Test
    void injectDefaultImplementationForInterface() throws Exception {
        DI inst = container.getInstance(DI.class);
        assertNotNull(inst);
    }

    @Test
    void injectMissingDefaultImplementationForInterface()  {
        assertThrows(ConfigurationException.class, () -> container.getInstance(AI.class));
    }

    @Test
    void decorateInstance() throws Exception {
        C ci = new C();
        container.decorateInstance(ci);

        assertNotNull(ci.bField);
        assertNotNull(ci.bField.getaField());
    }

    @Test
    void initializer() throws Exception {
        String email = "name@yahoo.com";
        container.registerInstance("email", email);
        FSI inst = container.getInstance(FSI.class);

        assertNotNull(inst);
        assertNotNull(inst.email);
        assertEquals(inst.email, "mailto:" + email);
    }

    @Test
    void registerInstanceTwice() throws Exception {
        A a = new A();
        container.registerInstance(A.class, a);
        assertThrows(ConfigurationException.class, () -> container.registerInstance(A.class, a));
    }

    @Test
    void twoInjectConstructors() {
        assertThrows(ConfigurationException.class, () -> container.getInstance(G.class));
    }

    @Test
    void unnamedPrimitiveConstructorParam() {
        assertThrows(ConfigurationException.class, () -> container.getInstance(G.class));
    }

    @Test
    void missingProperty() {
        assertThrows(ConfigurationException.class, () -> container.getInstance(I.class));
    }

    @Test
    void circularDependency() throws Exception {
        M m = container.getInstance(M.class);
        assertNotNull(m);
        N n = container.getInstance(N.class);
        assertNotNull(n);
    }

    @Test
    void lazyTest() throws Exception {
        K k = container.getInstance(K.class);
        k.lField.print();
        System.out.println(k.lField);
    }
}
