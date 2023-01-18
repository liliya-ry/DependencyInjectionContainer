package org.example;

import org.example.annotations.*;

public class TestClasses {
    interface AI { }

    static class A implements AI {}

    static class B {
        @Inject
        private A aField;

        public A getaField() {
            return aField;
        }
    }

    static class C {
        @Inject
        @Lazy
        B bField;
    }

    @Default(D.class)
    interface DI { }

    static class D implements DI { }

    static class E {
        A aField;
        float percent;

        @Inject
        public E(A afield, @Named("percent") float percent) {
            this.aField = afield;
            this.percent = percent;
        }
    }

    static class F {
        @Inject @Named
        A iname;
    }

    static class FS {
        @Inject @Named String email;
    }

    static class FSI implements Initializer {
        @Inject @Named String email;

        @Override
        public void init() throws Exception {
            email = "mailto:" + email;
        }
    }

    static class G {
        A aField;
        B bField;

        @Inject
        public G(A afield) {
            this.aField = afield;
        }

        @Inject
        public G(B bfield) {
            this.bField = bfield;
        }
    }

    static class H {
        boolean flag;

        @Inject
        public H(boolean flag) {
            this.flag = flag;
        }
    }

    static class I {
        boolean flag;

        @Inject
        public I(@Named("flag") boolean flag) {
            this.flag = flag;
        }
    }

    static class M {
        @Inject
        N nField;
    }

    static class N {
        @Inject
        M mField;
    }

    static class K {
        @Inject
        @Lazy
        L lField;

////        @Lazy
////        @Inject
//        public K(L lField) {
//            this.lField = lField;
//        }
    }

    static class L {
        public void print() {
            System.out.println("lazy class");
        }
    }
}
