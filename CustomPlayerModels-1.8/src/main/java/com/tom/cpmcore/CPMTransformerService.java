package com.tom.cpmcore;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class CPMTransformerService implements IClassTransformer {
	private static final String HOOKS_CLASS = "com/tom/cpmcore/CPMASMClientHooks";
	private static final String NO_MODEL_SETUP_FIELD = "cpm$noModelSetup";
	private static final String RENDER_PLAYER_FIELD = "cpm$renderPlayer";
	private static Map<String, UnaryOperator<ClassNode>> transformers;

	public static void init() {
		transformers = new HashMap<>();

		transformers.put("net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				int modelBase = 9;
				lst.add(new VarInsnNode(Opcodes.ALOAD, modelBase));//ModelBase modelbase
				lst.add(new VarInsnNode(Opcodes.ALOAD, 7));//GameProfile profile
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "renderSkull", "(Lnet/minecraft/client/model/ModelBase;Lcom/mojang/authlib/GameProfile;)V", false));

				MethodNode m = null;

				for(MethodNode method : input.methods) {
					if(method.desc.equals("(FFFLnet/minecraft/util/EnumFacing;FILcom/mojang/authlib/GameProfile;I)V") ||
							method.desc.equals("(FFFLcq;FILcom/mojang/authlib/GameProfile;I)V")) {
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
		transformers.put("net.minecraft.client.model.ModelBiped", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, NO_MODEL_SETUP_FIELD, "Z", null, 0));

				for(MethodNode method : input.methods) {
					if((method.name.equals("a") && method.desc.equals("(FFFFFFLpk;)V")) || method.name.equals("setRotationAngles")) {
						System.out.println("CPM Armor Hook/No setup: found setRotationAngles method");
						InsnList lst = new InsnList();
						lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, NO_MODEL_SETUP_FIELD, "Z"));
						LabelNode lbln = new LabelNode();
						lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
						lst.add(new InsnNode(Opcodes.RETURN));
						lst.add(lbln);
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						System.out.println("CPM Armor Hook/No setup: injected");
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.renderer.entity.layers.LayerArmorBase", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				FieldNode rendererField = null;
				for(FieldNode fn : input.fields) {
					if(fn.name.equals("a") || fn.name.equals("renderer")) {
						rendererField = fn;
						System.out.println("CPM Armor Hook/Layer: Found renderer field");
					}
				}
				Type renderer = Type.getType(rendererField.desc);

				for(MethodNode method : input.methods) {
					if((method.name.equals("a") && method.desc.equals("(Lpr;FFFFFFFI)V")) || method.name.equals("renderLayer")) {
						System.out.println("CPM Armor Hook/Layer: Found renderLayer method");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if((mn.name.equals("a") && mn.desc.equals("(Lpk;FFFFFF)V")) || mn.name.equals("render")) {
									System.out.println("CPM Armor Hook/Layer: Found render call");
									Type[] argsD = Type.getArgumentTypes(mn.desc);
									Type[] args = new Type[argsD.length + 2];
									args[0] = Type.getObjectType(mn.owner);
									System.arraycopy(argsD, 0, args, 1, argsD.length);
									args[args.length - 1] = renderer;
									mn.desc = Type.getMethodDescriptor(Type.VOID_TYPE, args);
									mn.name = "renderArmor";
									mn.setOpcode(Opcodes.INVOKESTATIC);
									mn.owner = HOOKS_CLASS;
									InsnList lst = new InsnList();
									lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
									lst.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, rendererField.name, rendererField.desc));
									method.instructions.insertBefore(insnNode, lst);
									System.out.println("CPM Armor Hook/Layer: injected");
								}
							}
						}
					}
				}
				return input;
			}
		});
		transformers.put("com.tom.cpmcore.CPMASMClientHooks", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				for(MethodNode method : input.methods) {
					if(method.name.equals("setNoSetup")) {
						method.instructions.clear();
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
						method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, Type.getArgumentTypes(method.desc)[0].getInternalName(), NO_MODEL_SETUP_FIELD, "Z"));
						method.instructions.add(new InsnNode(Opcodes.RETURN));
						System.out.println("CPM ASM fields/No Render: injected");
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.renderer.entity.layers.LayerCustomHead", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, RENDER_PLAYER_FIELD, "Lnet/minecraft/client/renderer/entity/RenderPlayer;", null, null));
				for(MethodNode method : input.methods) {
					if(method.name.equals("a") || method.name.equals("doRenderLayer")) {
						System.out.println("CPM Armor Hook/Skull: Found doRenderLayer method");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if((mn.name.equals("c") || mn.name.equals("postRender")) && mn.desc.equals("(F)V")) {
									System.out.println("CPM Armor Hook/Skull: Found postRender call");
									InsnList lst = new InsnList();
									lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
									lst.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, RENDER_PLAYER_FIELD, "Lnet/minecraft/client/renderer/entity/RenderPlayer;"));
									Type[] argsD = Type.getArgumentTypes(mn.desc);
									Type[] args = new Type[argsD.length + 2];
									args[0] = Type.getObjectType(mn.owner);
									System.arraycopy(argsD, 0, args, 1, argsD.length);
									args[args.length - 1] = Type.getType("Lnet/minecraft/client/renderer/entity/RenderPlayer;");
									mn.desc = Type.getMethodDescriptor(Type.VOID_TYPE, args);
									mn.name = "postRenderSkull";
									mn.setOpcode(Opcodes.INVOKESTATIC);
									mn.owner = HOOKS_CLASS;
									method.instructions.insertBefore(insnNode, lst);
									System.out.println("CPM Armor Hook/Skull: injected");
								}
							}
						}
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.renderer.entity.RenderPlayer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				for(MethodNode method : input.methods) {
					if(method.name.equals("<init>")) {
						System.out.println("CPM RenderPlayer Hook: Found constructor");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof FieldInsnNode) {
								FieldInsnNode fn = (FieldInsnNode) insnNode;
								System.out.println("FieldNode: " + fn.name + " " + fn.desc);
								if(fn.name.equals("e") || fn.name.equals("bipedHead")) {
									System.out.println("CPM RenderPlayer Hook: Found bipedHead field insn");
									InsnList lst = new InsnList();
									lst.add(new InsnNode(Opcodes.DUP));
									lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
									lst.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/renderer/entity/layers/LayerCustomHead", RENDER_PLAYER_FIELD, "Lnet/minecraft/client/renderer/entity/RenderPlayer;"));
									method.instructions.insert(fn.getNext(), lst);
									System.out.println("CPM RenderPlayer Hook: injected");
								}
							}
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
			ClassWriter writer = new ClassWriter(0);//ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES
			classNode.accept(writer);
			return writer.toByteArray();
		}
		return basicClass;
	}

}
