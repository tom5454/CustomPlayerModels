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

import com.tom.cpm.shared.config.PlayerData;

public class CPMTransformerService implements IClassTransformer {
	private static Map<String, UnaryOperator<ClassNode>> transformers;
	private static final String HOOKS_CLASS = "com/tom/cpmcore/CPMASMClientHooks";
	private static final String NO_MODEL_SETUP_FIELD = "cpm$noModelSetup";
	private static final String RENDER_PLAYER_FIELD = "cpm$renderPlayer";
	private static final String HAS_MOD_FIELD = "cpm$hasMod";
	private static final String DATA_FIELD = "cpm$data";

	public static void init() {
		transformers = new HashMap<>();

		transformers.put("net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				int modelBase = 10;
				lst.add(new VarInsnNode(Opcodes.ALOAD, modelBase));//ModelBase modelbase
				lst.add(new VarInsnNode(Opcodes.ALOAD, 7));//GameProfile profile
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "renderSkull", "(Lnet/minecraft/client/model/ModelBase;Lcom/mojang/authlib/GameProfile;)V", false));

				MethodNode m = null;

				for(MethodNode method : input.methods) {
					if(method.desc.equals("(FFFLnet/minecraft/util/EnumFacing;FILcom/mojang/authlib/GameProfile;IF)V") ||
							method.desc.equals("(FFFLfa;FILcom/mojang/authlib/GameProfile;IF)V")) {
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
					if((method.name.equals("a") && method.desc.equals("(FFFFFFLvg;)V")) || method.name.equals("setRotationAngles")) {
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
					if((method.name.equals("a") && method.desc.equals("(Lvp;FFFFFFFLvl;)V")) || method.name.equals("renderArmorLayer")) {
						System.out.println("CPM Armor Hook/Layer: Found renderArmorLayer method");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if((mn.name.equals("a") && mn.desc.equals("(Lvg;FFFFFF)V")) || mn.name.equals("render")) {
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
					if(method.name.equals("hasMod")) {
						method.instructions.clear();
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getArgumentTypes(method.desc)[0].getInternalName(), HAS_MOD_FIELD, "Z"));
						method.instructions.add(new InsnNode(Opcodes.IRETURN));
						System.out.println("CPM ASM fields/Net/hasMod: injected");
					}
					if(method.name.equals("setHasMod")) {
						method.instructions.clear();
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
						method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, Type.getArgumentTypes(method.desc)[0].getInternalName(), HAS_MOD_FIELD, "Z"));
						method.instructions.add(new InsnNode(Opcodes.RETURN));
						System.out.println("CPM ASM fields/Net/setHasMod: injected");
					}
					if(method.name.equals("getEncodedModelData")) {
						method.instructions.clear();
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getArgumentTypes(method.desc)[0].getInternalName(), DATA_FIELD, Type.getDescriptor(PlayerData.class)));
						method.instructions.add(new InsnNode(Opcodes.ARETURN));
						System.out.println("CPM ASM fields/ServerNet/getData: injected");
					}
					if(method.name.equals("setEncodedModelData")) {
						method.instructions.clear();
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
						method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, Type.getArgumentTypes(method.desc)[0].getInternalName(), DATA_FIELD, Type.getDescriptor(PlayerData.class)));
						method.instructions.add(new InsnNode(Opcodes.RETURN));
						System.out.println("CPM ASM fields/ServerNet/setData: injected");
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
					if((method.name.equals("a") && method.desc.equals("(Lvp;FFFFFFF)V")) || method.name.equals("doRenderLayer")) {
						System.out.println("CPM Armor Hook/Skull: Found doRenderLayer method");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if((mn.name.equals("c") && mn.desc.equals("(F)V")) || mn.name.equals("postRender")) {
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
		transformers.put("net.minecraft.client.Minecraft", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onLogout", "()V", false));

				for(MethodNode method : input.methods) {
					if(method.name.equals("loadWorld") || (method.name.equals("a") && method.desc.equals("(Lbsb;Ljava/lang/String;)V"))) {
						System.out.println("CPM ClientLogout Hook: Found loadWorld");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if((mn.name.equals("k") && mn.desc.equals("()V") && mn.getPrevious() instanceof FieldInsnNode && ((FieldInsnNode)mn.getPrevious()).name.equals("o")) || mn.name.equals("resetData")) {
									method.instructions.insert(mn, lst);
									System.out.println("CPM ClientLogout Hook: injected");
								}
							}
						}
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.network.NetHandlerPlayClient", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
				lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onClientPacket", "(Lnet/minecraft/network/play/server/SPacketCustomPayload;Lnet/minecraft/client/network/NetHandlerPlayClient;)Z", false));
				LabelNode lbln = new LabelNode();
				lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
				lst.add(new InsnNode(Opcodes.RETURN));
				lst.add(lbln);

				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, HAS_MOD_FIELD, "Z", null, 0));

				for(MethodNode method : input.methods) {
					if(method.name.equals("handleCustomPayload") || (method.name.equals("a") && method.desc.equals("(Liw;)V"))) {
						System.out.println("CPM ClientNet Hook: Found handleCustomPayload");
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						System.out.println("CPM ClientNet Hook: injected");
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.network.NetHandlerPlayServer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
				lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onServerPacket", "(Lnet/minecraft/network/play/client/CPacketCustomPayload;Lnet/minecraft/network/NetHandlerPlayServer;)Z", false));
				LabelNode lbln = new LabelNode();
				lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
				lst.add(new InsnNode(Opcodes.RETURN));
				lst.add(lbln);

				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, HAS_MOD_FIELD, "Z", null, 0));
				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, DATA_FIELD, Type.getDescriptor(PlayerData.class), null, null));

				for(MethodNode method : input.methods) {
					if(method.name.equals("processCustomPayload") || (method.name.equals("a") && method.desc.equals("(Llh;)V"))) {
						System.out.println("CPM ServerNet Hook: Found processCustomPayload");
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						System.out.println("CPM ServerNet Hook: injected");
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
