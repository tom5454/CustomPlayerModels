package com.tom.cpmcore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.tom.cpl.util.ILogger;
import com.tom.cpl.util.Pair;
import com.tom.cpm.retro.JavaLogger;

import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IClassTransformer;

public class CPMTransformerService implements IClassTransformer {
	public static final ILogger LOG = new JavaLogger(FMLRelaunchLog.log.getLogger(), "CPM Core");
	private static final String HOOKS_CLASS = "com/tom/cpmcore/CPMASMClientHooks";
	private static final String HOOKS_CLASS_SERVER = "com/tom/cpmcore/CPMASMServerHooks";
	private static final String NO_MODEL_SETUP_FIELD = "cpm$noModelSetup";
	private static final String HAS_MOD_FIELD = "cpm$hasMod";
	private static final String DATA_FIELD = "cpm$data";
	private static Map<String, Pair<String, String>> methodObfs = new HashMap<>();
	private static Map<String, List<String>> fieldATs = new HashMap<>();
	private static Map<String, UnaryOperator<ClassNode>> transformers;

	private static boolean checkMethod(MethodNode mn, Pair<String, String> mth) {
		return mn.name.equals(mth.getKey()) && mn.desc.equals(mth.getValue());
	}

	private static boolean checkMethod(MethodInsnNode mn, Pair<String, String> mth) {
		return mn.name.equals(mth.getKey()) && mn.desc.equals(mth.getValue());
	}

	@SuppressWarnings("unchecked")
	public static void init() {
		transformers = new HashMap<>();

		try (BufferedReader rd = new BufferedReader(new InputStreamReader(CPMTransformerService.class.getResourceAsStream("/META-INF/cpm_core.cfg")))) {
			String ln;
			while ((ln = rd.readLine()) != null) {
				ln = ln.trim();
				if (ln.isEmpty() || ln.startsWith("#"))continue;
				String[] sp = ln.split(" ");
				if (sp[0].equals("method")) {
					int i = sp[2].indexOf('(');
					i = sp[2].lastIndexOf('/', i);
					String nameDesc = sp[2].substring(i + 1);
					int ind = nameDesc.indexOf('(');
					String mth = nameDesc.substring(0, ind);
					String par = nameDesc.substring(ind).replace('.', '/');
					methodObfs.put(sp[1], Pair.of(mth, par));
				}
			}
			LOG.info("Loaded Obfuscation Mappings");
		} catch (IOException e) {
			throw new RuntimeException("Failed to load obf mapping, installed mod jar is corrupted!", e);
		}

		transformers.put("net.minecraft.network.NetServerHandler", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
				lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS_SERVER, "onServerPacket", "(Lnet/minecraft/network/packet/Packet250CustomPayload;Lnet/minecraft/network/NetServerHandler;)Z"));
				LabelNode lbln = new LabelNode();
				lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
				lst.add(new InsnNode(Opcodes.RETURN));
				lst.add(lbln);

				input.interfaces.add("com/tom/cpm/shared/network/NetH$ServerNetH");

				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, HAS_MOD_FIELD, "Z", null, 0));
				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, DATA_FIELD, "Lcom/tom/cpm/shared/config/PlayerData;", null, null));

				Pair<String, String> mth = lookupMethod("NetServerHandler;handleCustomPayload(Lnet.minecraft.network.packet.Packet250CustomPayload;)V");

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if (checkMethod(method, mth)) {
						LOG.info("CPM ServerNet Hook: Found processCustomPayload");
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM ServerNet Hook: injected");
					}
				}

				injectHasMod("ServerNet", input);

				MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "cpm$getEncodedModelData", "()Lcom/tom/cpm/shared/config/PlayerData;", null, null);
				input.methods.add(method);
				method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, DATA_FIELD, "Lcom/tom/cpm/shared/config/PlayerData;"));
				method.instructions.add(new InsnNode(Opcodes.ARETURN));
				method.maxLocals = 2;
				method.maxStack = 2;
				LOG.info("CPM ServerNet/getData: injected");

				method = new MethodNode(Opcodes.ACC_PUBLIC, "cpm$setEncodedModelData", "(Lcom/tom/cpm/shared/config/PlayerData;)V", null, null);
				input.methods.add(method);
				method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, input.name, DATA_FIELD, "Lcom/tom/cpm/shared/config/PlayerData;"));
				method.instructions.add(new InsnNode(Opcodes.RETURN));
				method.maxLocals = 2;
				method.maxStack = 2;
				LOG.info("CPM ServerNet/setData: injected");
				return input;
			}
		});
		transformers.put("net.minecraft.client.multiplayer.NetClientHandler", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
				lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onClientPacket", "(Lnet/minecraft/network/packet/Packet250CustomPayload;Lnet/minecraft/client/multiplayer/NetClientHandler;)Z"));
				LabelNode lbln = new LabelNode();
				lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
				lst.add(new InsnNode(Opcodes.RETURN));
				lst.add(lbln);

				input.interfaces.add("com/tom/cpm/shared/network/NetH");

				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, HAS_MOD_FIELD, "Z", null, 0));

				Pair<String, String> mth = lookupMethod("NetClientHandler;handleCustomPayload(Lnet.minecraft.network.packet.Packet250CustomPayload;)V");

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(checkMethod(method, mth)) {
						LOG.info("CPM ClientNet Hook: Found handleCustomPayload");
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM ClientNet Hook: injected");
					}
				}

				injectHasMod("ClientNet", input);

				return input;
			}
		});
		transformers.put("com.tom.cpmcore.CPMClientAccess", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(method.name.equals("setNoSetup")) {
						method.instructions.clear();
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
						method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, Type.getArgumentTypes(method.desc)[0].getInternalName(), NO_MODEL_SETUP_FIELD, "Z"));
						method.instructions.add(new InsnNode(Opcodes.RETURN));
						LOG.info("CPM ASM fields/No Render: injected");
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.model.ModelBiped", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, NO_MODEL_SETUP_FIELD, "Z", null, 0));

				Pair<String, String> mth = lookupMethod("ModelBiped;setRotationAngles(FFFFFFLnet.minecraft.entity.Entity;)V");

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(checkMethod(method, mth)) {
						LOG.info("CPM Armor Hook/No setup: found setRotationAngles method");
						InsnList lst = new InsnList();
						lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, NO_MODEL_SETUP_FIELD, "Z"));
						LabelNode lbln = new LabelNode();
						lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
						lst.add(new InsnNode(Opcodes.RETURN));
						lst.add(lbln);
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM Armor Hook/No setup: injected");
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				int modelBase = 8;
				lst.add(new VarInsnNode(Opcodes.ALOAD, modelBase));//ModelBase modelbase
				lst.add(new VarInsnNode(Opcodes.ALOAD, 7));//String name
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "renderSkull", "(Lnet/minecraft/client/model/ModelBase;Ljava/lang/String;)V"));

				InsnList lst2 = new InsnList();
				lst2.add(new VarInsnNode(Opcodes.ALOAD, modelBase));//ModelBase modelbase
				lst2.add(new VarInsnNode(Opcodes.ALOAD, 7));//String name
				lst2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "renderSkullPost", "(Lnet/minecraft/client/model/ModelBase;Ljava/lang/String;)V"));

				MethodNode m = null;

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(method.desc.equals("(FFFIFILjava/lang/String;)V")) {
						m = method;
						LOG.info("CPM Skull Hook: found method");
						break;
					}
				}

				for (ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); ) {
					AbstractInsnNode insnNode = it.next();
					if(insnNode instanceof VarInsnNode){
						VarInsnNode nd = (VarInsnNode) insnNode;
						if(nd.getOpcode() == Opcodes.ALOAD && nd.var == modelBase) {
							m.instructions.insertBefore(nd, lst);
							LOG.info("CPM Skull Hook: injected (Pre)");
						}
					} else if(insnNode instanceof InsnNode){
						if(insnNode.getOpcode() == Opcodes.RETURN) {
							m.instructions.insertBefore(insnNode, lst2);
							LOG.info("CPM Skull Hook: injected (Post)");
						}
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.renderer.entity.RenderLiving", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("RenderLiving;doRenderLiving(Lnet.minecraft.entity.EntityLiving;DDDFF)V");
				Pair<String, String> mth2 = lookupMethod("ModelBase;render(Lnet.minecraft.entity.Entity;FFFFFF)V");
				Pair<String, String> mth3 = lookupMethod("RenderLiving;passSpecialRender(Lnet.minecraft.entity.EntityLiving;DDD)V");

				for(MethodNode m : (List<MethodNode>) input.methods) {
					if(checkMethod(m, mth) || m.name.equals("renderPlayer")) {
						LOG.info("CPM Armor Hook: Found doRender method");
						m.instructions.insertBefore(m.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "prePlayerRender", "()V"));
						int callLoc = 0;
						m.maxStack += 3;
						for (ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if(checkMethod(mn, mth2)) {
									LOG.info("CPM Armor Hook: Found render call");
									Type[] argsD = Type.getArgumentTypes(mn.desc);
									Type[] args = new Type[argsD.length + 3];
									args[0] = Type.getObjectType(mn.owner);
									System.arraycopy(argsD, 0, args, 1, argsD.length);
									args[args.length - 2] = Type.getType("Lnet/minecraft/client/renderer/entity/RenderLiving;");
									args[args.length - 1] = Type.INT_TYPE;
									mn.desc = Type.getMethodDescriptor(Type.VOID_TYPE, args);
									mn.name = "renderPass";
									mn.setOpcode(Opcodes.INVOKESTATIC);
									mn.owner = HOOKS_CLASS;
									InsnList lst = new InsnList();
									lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
									lst.add(new LdcInsnNode(callLoc++));
									m.instructions.insertBefore(insnNode, lst);
									LOG.info("CPM Armor Hook/Layer: injected");
								} else if (mn.name.equals("glColor4f")) {
									mn.owner = HOOKS_CLASS;
									LOG.info("CPM Player Renderer/Color hook: injected");
								}
							}
						}
					} else if(checkMethod(m, mth3)) {
						LOG.info("CPM Name Hook: Found renderSpecial method");
						InsnList lst = new InsnList();
						lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
						lst.add(new VarInsnNode(Opcodes.DLOAD, 2));
						lst.add(new VarInsnNode(Opcodes.DLOAD, 4));
						lst.add(new VarInsnNode(Opcodes.DLOAD, 6));
						lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onRenderName", "(Lnet/minecraft/client/renderer/entity/RenderLiving;Lnet/minecraft/entity/EntityLiving;DDD)Z"));
						LabelNode lbln = new LabelNode();
						lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
						lst.add(new InsnNode(Opcodes.RETURN));
						lst.add(lbln);
						m.instructions.insertBefore(m.instructions.getFirst(), lst);
						LOG.info("CPM Name Hook: injected");
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.Minecraft", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onLogout", "(Lnet/minecraft/client/multiplayer/WorldClient;)V"));

				Pair<String, String> mth = lookupMethod("Minecraft;loadWorld(Lnet.minecraft.client.multiplayer.WorldClient;Ljava/lang/String;)V");
				Pair<String, String> mth2 = lookupMethod("Minecraft;displayGuiScreen(Lnet.minecraft.client.gui.GuiScreen;)V");

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(checkMethod(method, mth)) {
						LOG.info("CPM ClientLogout Hook: Found loadWorld");
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM ClientLogout Hook: injected");
					} else if(checkMethod(method, mth2)) {
						LOG.info("CPM Open Gui Hook: Found displayGuiScreen");
						InsnList list = new InsnList();
						list.add(new VarInsnNode(Opcodes.ALOAD, 1));
						list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "openGui", "(Lnet/minecraft/client/gui/GuiScreen;)Lnet/minecraft/client/gui/GuiScreen;"));
						list.add(new VarInsnNode(Opcodes.ASTORE, 1));
						method.instructions.insertBefore(method.instructions.getFirst(), list);
						LOG.info("CPM Open Gui Hook: injected");
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.renderer.entity.RenderPlayer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				boolean isDeobf = false;
				Pair<String, String> mth = lookupMethod("RenderPlayer;renderSpecials(Lnet.minecraft.entity.player.EntityPlayer;F)V");
				Pair<String, String> mth2 = lookupMethod("RenderPlayer;renderFirstPersonArm(Lnet.minecraft.entity.player.EntityPlayer;)V");

				input.interfaces.add("com/tom/cpmcore/IPlayerRenderer");
				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(checkMethod(method, mth)) {
						LOG.info("CPM Cape Hook: Found renderEquippedItems");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof FieldInsnNode) {
								FieldInsnNode mn = (FieldInsnNode) insnNode;
								if(mn.owner.equals("net/minecraftforge/client/event/RenderPlayerEvent$Specials$Pre") && mn.name.equals("renderCape")) {
									LOG.info("CPM Cape Hook: Found renderCape");
									InsnList lst = new InsnList();
									lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
									lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
									lst.add(new VarInsnNode(Opcodes.FLOAD, 2));
									lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "renderCape", "(ZLnet/minecraft/client/renderer/entity/RenderPlayer;Lnet/minecraft/entity/player/EntityPlayer;F)Z"));
									method.instructions.insert(insnNode, lst);
									LOG.info("CPM Cape Hook: injected");
								}
							}
						}
					}
					if(checkMethod(method, mth2)) {
						LOG.info("CPM Hand Hook: Found renderFirstPersonArm");
						if(method.name.equals("renderFirstPersonArm"))isDeobf = true;
						InsnList lst = new InsnList();
						lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
						lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onHandPre", "(Lnet/minecraft/client/renderer/entity/RenderPlayer;Lnet/minecraft/entity/player/EntityPlayer;)V"));
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM Render Hand Hook Pre: injected");

						InsnList lst2 = new InsnList();
						lst2.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst2.add(new VarInsnNode(Opcodes.ALOAD, 1));
						lst2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onHandPost", "(Lnet/minecraft/client/renderer/entity/RenderPlayer;Lnet/minecraft/entity/player/EntityPlayer;)V"));

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof InsnNode){
								if(insnNode.getOpcode() == Opcodes.RETURN) {
									method.instructions.insertBefore(insnNode, lst2);
									LOG.info("CPM Render Hand Hook Post: injected");
								}
							}
						}
					}
					for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
						AbstractInsnNode insnNode = it.next();
						if(insnNode instanceof MethodInsnNode) {
							MethodInsnNode mn = (MethodInsnNode) insnNode;
							if (mn.name.equals("glColor4f")) {
								mn.owner = HOOKS_CLASS;
								LOG.info("CPM Player Renderer/Color hook: injected");
							} else if (mn.name.equals("glColor3f")) {
								mn.owner = HOOKS_CLASS;
								LOG.info("CPM Player Renderer/Color hook: injected");
							}
						}
					}
				}
				//func_77036_a(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V
				//renderModel(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V
				String name = isDeobf ? "renderModel" : "func_77036_a";
				LOG.info("CPM Render Invis Hook: Injecting method " + (isDeobf ? "deobf" : ""));
				MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, name, "(Lnet/minecraft/entity/EntityLiving;FFFFFF)V", null, null);
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 4));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 5));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 6));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 7));
				mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onRenderPlayerModel", "(Lnet/minecraft/client/renderer/entity/RenderPlayer;Lnet/minecraft/entity/EntityLiving;FFFFFF)Z"));
				LabelNode lbln = new LabelNode();
				mn.instructions.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
				mn.instructions.add(new InsnNode(Opcodes.RETURN));
				mn.instructions.add(lbln);

				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 4));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 5));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 6));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 7));
				mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/client/renderer/entity/RenderLiving", name, "(Lnet/minecraft/entity/EntityLiving;FFFFFF)V"));
				mn.instructions.add(new InsnNode(Opcodes.RETURN));
				mn.maxLocals = 8;
				mn.maxStack = 8;
				input.methods.add(mn);
				LOG.info("CPM Render Invis Hook: injected");

				name = isDeobf ? "bindEntityTexture" : "func_110777_b";
				mn = new MethodNode(Opcodes.ACC_PUBLIC, "cpm$bindEntityTexture", "(Lnet/minecraft/entity/player/EntityPlayer;)V", null, null);
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, input.name, name, "(Lnet/minecraft/entity/Entity;)V"));
				mn.instructions.add(new InsnNode(Opcodes.RETURN));
				mn.maxLocals = 2;
				mn.maxStack = 2;
				input.methods.add(mn);
				LOG.info("CPM Render Invis Hook/Bind Texture: injected");

				return input;
			}
		});
		transformers.put("net.minecraft.client.renderer.ThreadDownloadImage", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				for(MethodNode method : (List<MethodNode>) input.methods) {
					if (method.name.equals("run")) {
						LOG.info("CPM Skin Fixer Hook: Found run");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof MethodInsnNode){
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if (mn.owner.equals("java/net/URL") && mn.name.equals("<init>")) {
									method.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "fixSkinURL", "(Ljava/lang/String;)Ljava/lang/String;"));
									LOG.info("CPM Skin Fixer Hook: injected");
								}
							}
						}
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.entity.EntityTrackerEntry", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("EntityTrackerEntry;tryStartWachingThis(Lnet.minecraft.entity.player.EntityPlayerMP;)V");
				Pair<String, String> mth2 = lookupMethod("EntityPlayer;isPlayerSleeping()Z");

				for (MethodNode method : (List<MethodNode>) input.methods) {
					if (checkMethod(method, mth)) {
						LOG.info("CPM Start Tracking Hook: Found tryStartWachingThis");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof MethodInsnNode && checkMethod((MethodInsnNode) insnNode, mth2)){
								LOG.info("CPM Start Tracking Hook: Found isPlayerSleeping");
								InsnList in = new InsnList();
								in.add(new InsnNode(Opcodes.DUP));
								in.add(new VarInsnNode(Opcodes.ALOAD, 1));
								in.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS_SERVER, "startTracking", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/player/EntityPlayerMP;)V"));
								method.instructions.insertBefore(insnNode, in);
								LOG.info("CPM Start Tracking Hook: injected");
							}
						}
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.renderer.EntityRenderer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("EntityRenderer;updateCameraAndRender(F)V");
				Pair<String, String> mth2 = lookupMethod("GuiScreen;drawScreen(IIF)V");

				for (MethodNode method : (List<MethodNode>) input.methods) {
					if (checkMethod(method, mth)) {
						LOG.info("CPM Draw Screen Hook: Found updateCameraAndRender");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof MethodInsnNode && checkMethod((MethodInsnNode) insnNode, mth2)){
								LOG.info("CPM Draw Screen Hook: Found drawScreen");
								method.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onDrawScreenPre", "()V"));
								method.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onDrawScreenPost", "()V"));
								LOG.info("CPM Draw Screen Hook: injected");
							}
						}
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.gui.GuiScreen", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("GuiScreen;setWorldAndResolution(Lnet.minecraft.client.Minecraft;II)V");
				Pair<String, String> mth2 = lookupMethod("GuiScreen;mouseClicked(III)V");
				Pair<String, String> mth3 = lookupMethod("GuiScreen;actionPerformed(Lnet.minecraft.client.gui.GuiButton;)V");

				for (MethodNode method : (List<MethodNode>) input.methods) {
					if (checkMethod(method, mth)) {
						LOG.info("CPM Init Screen Hook: Found setWorldAndResolution");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof InsnNode && insnNode.getOpcode() == Opcodes.RETURN) {
								InsnList list = new InsnList();
								list.add(new VarInsnNode(Opcodes.ALOAD, 0));
								list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onInitScreen", "(Lnet/minecraft/client/gui/GuiScreen;)V"));
								method.instructions.insertBefore(insnNode, list);
								LOG.info("CPM Init Screen Hook: injected");
							}
						}
					} else if (checkMethod(method, mth2)) {
						LOG.info("CPM Button Click Hook: Found mouseClicked");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof MethodInsnNode && checkMethod((MethodInsnNode) insnNode, mth3)){
								LOG.info("CPM Button Click Hook: Found actionPerformed");
								InsnList list = new InsnList();
								list.add(new InsnNode(Opcodes.DUP));
								list.add(new VarInsnNode(Opcodes.ALOAD, 0));
								list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onGuiButtonClick", "(Lnet/minecraft/client/gui/GuiButton;Lnet/minecraft/client/gui/GuiScreen;)V"));
								method.instructions.insertBefore(insnNode, list);
								LOG.info("CPM Button Click Hook: injected");
							}
						}
					}
				}
				return input;
			}
		});

		if (!CPMLoadingPlugin.deobf) {
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(CPMTransformerService.class.getResourceAsStream("/META-INF/cpm_at.cfg")))) {
				String ln;
				while ((ln = rd.readLine()) != null) {
					ln = ln.trim();
					if (ln.isEmpty() || ln.startsWith("#"))continue;
					String[] sp = ln.split(" ")[1].split("\\.");
					fieldATs.computeIfAbsent(sp[0], __ -> new ArrayList<>()).add(sp[1]);
				}
				LOG.info("Loaded Access Transformer");
			} catch (IOException e) {
				throw new RuntimeException("Failed to load obf mapping, installed mod jar is corrupted!", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void injectHasMod(String name, ClassNode input) {
		MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "cpm$hasMod", "()Z", null, null);
		input.methods.add(method);
		method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, HAS_MOD_FIELD, "Z"));
		method.instructions.add(new InsnNode(Opcodes.IRETURN));
		method.maxLocals = 2;
		method.maxStack = 2;
		LOG.info("CPM " + name + "/hasMod: injected");

		method = new MethodNode(Opcodes.ACC_PUBLIC, "cpm$setHasMod", "(Z)V", null, null);
		input.methods.add(method);
		method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
		method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, input.name, HAS_MOD_FIELD, "Z"));
		method.instructions.add(new InsnNode(Opcodes.RETURN));
		method.maxLocals = 2;
		method.maxStack = 2;
		LOG.info("CPM " + name + "/setHasMod: injected");
	}

	private static Pair<String, String> lookupMethod(String fullDesc) {
		if (!methodObfs.containsKey(fullDesc))throw new RuntimeException("Unknown method: " + fullDesc);
		if (CPMLoadingPlugin.deobf) {
			String nameDesc = fullDesc.split(";", 2)[1];
			int ind = nameDesc.indexOf('(');
			String mth = nameDesc.substring(0, ind);
			String par = nameDesc.substring(ind).replace('.', '/');
			return Pair.of(mth, par);
		} else {
			return methodObfs.get(fullDesc);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(transformers == null)return basicClass;
		UnaryOperator<ClassNode> tr = transformers.get(transformedName);
		List<String> ats = fieldATs.get(name);
		if(tr != null || ats != null) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);
			LOG.info("Applying cpm transformer: " + transformedName);
			if (tr != null)classNode = tr.apply(classNode);
			if (ats != null) {
				for (FieldNode f : (List<FieldNode>) classNode.fields) {
					if (ats.contains(f.name)) {
						f.access &= ~(Opcodes.ACC_FINAL | 0b111);
						f.access |= Opcodes.ACC_PUBLIC;
					}
				}
				LOG.info("Applied " + ats.size() + " field ATs");
			}
			ClassWriter writer = new ClassWriter(0);
			classNode.accept(writer);
			return writer.toByteArray();
		}
		return basicClass;
	}

}
