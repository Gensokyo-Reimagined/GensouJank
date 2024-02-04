package net.gensokyoreimagined.gensoujank;

import com.sun.tools.attach.VirtualMachine;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.jar.asm.*;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.*;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.jar.JarFile;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class ServerAgent {
    private static boolean isLoaded = false;

    public static void premain(String arguments, Instrumentation instrumentation) {
        if (isLoaded) {
            return;
        }

        System.out.println(ServerAgent.class.getName() + " loaded");
        isLoaded = true;

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader l,
                    String name,
                    Class c,
                    ProtectionDomain d,
                    byte[] b) {
                if(name.equals("net/minecraft/server/players/PlayerList")) {
                    ClassReader classReader = new ClassReader(b);
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
                    classReader.accept(new ClassVisitor(Opcodes.ASM7, classWriter){
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                            if (name.equals("canPlayerLogin")) {
                                return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
                                    @Override
                                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                                        if (opcode == Opcodes.INVOKESPECIAL && "net/minecraft/server/level/EntityPlayer".equals(owner) && "<init>".equals(name)) {
                                            super.visitMethodInsn(opcode, "net/gensokyoreimagined/gensoujank/TouhouPlayer", name, desc, itf);
                                        } else {
                                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                                        }
                                    }
                                    @Override
                                    public void visitTypeInsn(int opcode, String type) {
                                        if (type.contains("EntityPlayer"))
                                            super.visitTypeInsn(opcode, "net/gensokyoreimagined/gensoujank/TouhouPlayer");
                                        else
                                            super.visitTypeInsn(opcode, type);
                                    }
                                };
                            }
                            return super.visitMethod(access, name, desc, signature, exceptions);
                        }
                    }, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                    return classWriter.toByteArray();
                }
                return b;
            }
        });
        new AgentBuilder.Default()
                .type(named("org.bukkit.Bukkit"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
                            try {
                                for(URL url: ((URLClassLoader)classLoader).getURLs()){
                                    instrumentation.appendToSystemClassLoaderSearch((new JarFile(new File(url.toURI()))));
                                }
                            } catch (URISyntaxException | IOException e) {
                                throw new RuntimeException(e);
                            }
                    new ClassInjector.UsingUnsafe(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(TouhouPlayer.class), ClassFileLocator.ForClassLoader.read(TouhouPlayer.class)));
                    return builder;
                })
                .installOn(instrumentation);

    }

    private String getPid() {
        var bean = ManagementFactory.getRuntimeMXBean();
        var pid = bean.getName();
        if (pid.contains("@")) {
            pid = pid.substring(0, pid.indexOf("@"));
        }
        return pid;
    }

    public void attachAgent() {
        try {
            System.loadLibrary("attach");
            var vm = VirtualMachine.attach(getPid());
            var props = vm.getSystemProperties();
            vm.loadAgent(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath());
            vm.detach();
        } catch (Exception e) {
            System.out.println("Failed to attach agent");
            e.printStackTrace();
        }
    }

    public static void reTransformAll(Instrumentation instrumentation){
        for(var clazz : instrumentation.getAllLoadedClasses()){

            if(clazz != null && clazz.getPackage() != null && clazz.getSuperclass() != null){
                if(!clazz.isPrimitive() && !clazz.getPackage().getName().startsWith("java.lang")){
                    if(instrumentation.isModifiableClass(clazz)){
                        try {
                            instrumentation.retransformClasses(clazz);
                        } catch (UnmodifiableClassException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
