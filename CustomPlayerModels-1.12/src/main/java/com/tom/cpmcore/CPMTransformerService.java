package com.tom.cpmcore;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class CPMTransformerService implements IClassTransformer {
	private static Map<String, UnaryOperator<ClassNode>> transformers;

	public static void init() {
		transformers = new HashMap<>();

		transformers.put("net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				int modelBase = 10;
				lst.add(new VarInsnNode(Opcodes.ALOAD, modelBase));//ModelBase modelbase
				lst.add(new VarInsnNode(Opcodes.ALOAD, 7));//GameProfile profile
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/tom/cpmcore/CPMASMClientHooks", "renderSkull", "(Lnet/minecraft/client/model/ModelBase;Lcom/mojang/authlib/GameProfile;)V", false));

				MethodNode m = null;

				for(MethodNode method : input.methods) {
					if(method.desc.equals("(FFFLnet/minecraft/util/EnumFacing;FILcom/mojang/authlib/GameProfile;IF)V")) {
						m = method;
						System.out.println("CPM Skull Hook: found method");
						break;
					}
				}

				for (ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); ) {
					AbstractInsnNode insnNode = it.next();
					if(insnNode instanceof VarInsnNode){
						VarInsnNode nd = (VarInsnNode) insnNode;
						if(nd.getOpcode() == Opcodes.ALOAD && nd.var == modelBase) {
							m.instructions.insertBefore(nd, lst);
							System.out.println("CPM Skull Hook: injected");
							break;
						}
					}
				}
				return input;
			}
		});
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(transformers == null)return basicClass;
		UnaryOperator<ClassNode> tr = transformers.get(transformedName);
		if(tr != null) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);
			System.out.println("Applying cpm transformer: " + transformedName);
			classNode = tr.apply(classNode);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			return writer.toByteArray();
		}
		return basicClass;
	}

}
