package org.example;

import org.example.annotations.*;

public class TestClasses {
    interface AI { }

    static class A implements AI { }

    static class B {
        @Inject
        A aField;
    }

    static class C {
        @Inject B bField;
    }

    @Default(D.class)
    interface DI { }

    static class D implements DI { }

    static class E {
        A aField;

        @Inject
        public E(A afield) {
            this.aField = afield;
        }
    }

    static class F {
        @Inject @Named
        A iname;
    }

    static class FS {
        @Inject @Named String email;
    }

//    class FSI implements Initializer {
//        @Inject @Named String email;
//
//        @Override
//        public void init() throws Exception {
//            email = "mailto:" + email;
//        }
//    }
}
