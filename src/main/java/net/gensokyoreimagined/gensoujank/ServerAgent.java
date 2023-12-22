package net.gensokyoreimagined.gensoujank;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.jar.asm.*;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.jar.JarFile;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class ServerAgent {
    public static void premain(String arguments, Instrumentation instrumentation) {
        System.out.println(ServerAgent.class.getName() + " loaded");
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
}
