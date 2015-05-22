package net.techcable.cbpatcher;

import javassist.CtClass;

public interface ClassTransformListener {
    public void onTransform(CtClass clazz);
}
