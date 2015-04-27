package net.techcable.cbpatcher;


import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import javassist.*;
import net.techcable.cbpatcher.annotation.Inject;
import tk.ivybits.agent.AgentLoader;
import tk.ivybits.agent.Tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * Represents a cb patcher implementation
 */
public abstract class AbstractCBPatcher implements ClassFileTransformer {
    protected abstract File getNativeDir();
    public void tranformAll() {
        try {
            Tools.CACHE_DIR = getNativeDir().getAbsolutePath();
            Tools.loadAgentLibrary();
            AgentLoader.attachAgentToJVM(Tools.getCurrentPID(), AbstractCBPatcher.class, AgentLoader.class);
        } catch (Exception e) {
            log("Exception: " + e.getMessage());
            logStack(e);
        }
    }

    public void log(String format, Object... args) {
        log0(String.format(format, args));
    }
    public void logStack(Throwable t) {
        t.printStackTrace(new PrintWriter(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                log0(new String(cbuf));
            }
            @Override
            public void flush() throws IOException {}
            @Override
            public void close() throws IOException {}
        }));
    }
    protected abstract void log0(String s);

    private final Multimap<String, CtClass> injectionClasses = HashMultimap.create();
    private Set<ClassLoader> addedLoaders = Sets.newSetFromMap(new WeakHashMap<ClassLoader, Boolean>());

    public void injectClass(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Inject.class)) return;
        Inject annotation = clazz.getAnnotation(Inject.class);
        try {
            injectionClasses.put(annotation.injectInto().getName(), ClassPool.getDefault().get(clazz.getName()));
        } catch (NotFoundException e) {
            //Should never happen
            throw Throwables.propagate(e);
        }
    }

    /**
     * This method should not be called from outside code
     *
     * @deprecated internal use only
     */
    @Override
    @Deprecated
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith("net.techcable.cbpatcher")) return null;
        if (!addedLoaders.contains(loader)) {
            ClassPool.getDefault().appendClassPath(new LoaderClassPath(loader));
            addedLoaders.add(loader);
        }
        CtClass clazz;
        try {
            clazz = ClassPool.getDefault().get(className);
        } catch (NotFoundException e) {
            log("Severe Error: %s", e.getMessage());
            throw Throwables.propagate(e); //This should never happen
        }
        if (injectionClasses.containsKey(clazz.getName())) {
            Map<String, CtMethod> methodsToInject = new HashMap<>();
            for (CtClass toInject : injectionClasses.get(clazz.getName())) {
                for (CtMethod methodToInject : toInject.getMethods()) {
                    methodsToInject.put(methodToInject.getLongName(), methodToInject);
                }
            }
            for (CtMethod method : clazz.getMethods()) {
                CtMethod overridden = methodsToInject.get(method.getLongName());
                try {
                    method.setBody(overridden, null);
                } catch (CannotCompileException e) {
                    log("Unable to inject into %s from %s", method.getLongName(), overridden.getDeclaringClass().getName());
                    logStack(e);
                }
                methodsToInject.remove(method.getLongName());
            }
            for (CtMethod newMethod : methodsToInject.values()) {
                try {
                    clazz.addMethod(newMethod);
                } catch (CannotCompileException e) {
                    log("Unable to inject %s from %s into %s", newMethod.getLongName(), newMethod.getDeclaringClass().getName(), clazz.getName());
                    logStack(e);
                }
            }
            try {
                byte[] bytecode = clazz.toBytecode();
                clazz.detach();
                return bytecode;
            } catch (IOException | CannotCompileException e) {
                log("Exception: %s", e.getMessage());
                logStack(e);
                return null;
            }
        }
        return null;
    }
}
