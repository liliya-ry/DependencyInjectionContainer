package org.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import org.example.TestClasses.*;

public class ContainerTest {
    Container container;

    @Before
    public void init() {
        container = new Container();
    }


    @Test
    public void autoInject() throws Exception {
        B inst = container.getInstance(B.class);
        assertNotNull(inst);
        assertNotNull(inst.aField);
    }

    @Test
    public void injectImplementation() throws Exception {
        container.registerImplementation(A.class);
        B inst = container.getInstance(B.class);

        assertNotNull(inst);
        assertNotNull(inst.aField);
    }

    @Test
    public void injectInstance() throws Exception {
        A a = new A();
        container.registerInstance(a);
        B inst = container.getInstance(B.class);

        assertNotNull(inst);
        assertSame(a, inst.aField);
    }

    @Test
    public void injectNamedInstance() throws Exception {
        A a = new A();
        container.registerInstance("iname", a);
        F inst = container.getInstance(F.class);

        assertNotNull(inst);
        assertSame(a, inst.iname);
    }

    @Test
    public void injectStringProperty() throws Exception {
        String email = "name@yahoo.com";
        container.registerInstance("email", email);
        FS inst = container.getInstance(FS.class);

        assertNotNull(inst);
        assertNotNull(inst.email);
        assertSame(inst.email, email);
    }

//    @Test
//    public void constructorInject() throws Exception {
//        E inst = container.getInstance(E.class);
//
//        assertNotNull(inst);
//        assertNotNull(inst.aField);
//    }
//
    @Test
    public void injectInterface() throws Exception {
        container.registerImplementation(AI.class, A.class);
        B inst = container.getInstance(B.class);

        assertNotNull(inst);
        assertNotNull(inst.aField);
    }

    @Test
    public void injectDefaultImplementationForInterface() throws Exception {
        DI inst = container.getInstance(DI.class);
        assertNotNull(inst);
    }

    @Test //(expected=RegistryException.class)
    public void injectMissingDefaultImplementationForInterface() throws Exception {
        AI inst = container.getInstance(AI.class);
        assertNull(inst);
    }

    @Test
    public void decorateInstance() throws Exception {
        C ci = new C();
        container.decorateInstance(ci);

        assertNotNull(ci.bField);
        assertNotNull(ci.bField.aField);
    }

//    @Test
//    public void initializer() throws Exception {
//        String email = "name@yahoo.com";
//        container.registerInstance("email", email);
//        FSI inst = container.getInstance(FSI.class);
//
//        assertNotNull(inst);
//        assertNotNull(inst.email);
//        assertEquals(inst.email, "mailto:" + email);
//    }
}
