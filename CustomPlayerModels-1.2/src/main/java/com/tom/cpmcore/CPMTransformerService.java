package com.tom.cpmcore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
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
import com.tom.cpm.retro.FileLogger;

public class CPMTransformerService {
	public static final ILogger LOG = new FileLogger("CPM Core");
	private static final String HOOKS_CLASS = "com/tom/cpmcore/CPMASMClientHooks";
	private static final String HOOKS_CLASS_SERVER = "com/tom/cpmcore/CPMASMServerHooks";
	private static final String NO_MODEL_SETUP_FIELD = "cpm$noModelSetup";
	private static final String HAS_MOD_FIELD = "cpm$hasMod";
	private static final String DATA_FIELD = "cpm$data";
	private static Map<String, Pair<String, String>> methodObfs = new HashMap<>();
	private static Map<String, String> fieldObfs = new HashMap<>();
	private static Map<String, List<String>> fieldATs = new HashMap<>();
	private static Map<String, UnaryOperator<ClassNode>> transformers;
	private static Map<String, String> classObfs = new HashMap<>();
	private static Map<String, String> classObfsR = new HashMap<>();
	private static Map<String, Set<String>> overrides = new HashMap<>();
	private static boolean isDedicatedServer;
	private static boolean spc = System.getProperty("cpmcore.spc", "false").equalsIgnoreCase("true");
	private static boolean dump = System.getProperty("cpmcore.asm_dump", "false").equalsIgnoreCase("true");
	private static boolean isClient = System.getProperty("cpmcore.env.client", "false").equalsIgnoreCase("true");
	private static boolean bukkit;
	private static Map<String, String> classObfsBukkit;

	private static boolean checkMethod(MethodNode mn, Pair<String, String> mth) {
		return mn.name.equals(mth.getKey()) && mn.desc.equals(mth.getValue());
	}

	private static boolean checkMethod(MethodInsnNode mn, Pair<String, String> mth) {
		return mn.name.equals(mth.getKey()) && mn.desc.equals(mth.getValue());
	}

	@SuppressWarnings("unchecked")
	public static void init() {
		transformers = new HashMap<>();

		isDedicatedServer = !isClient && CPMTransformerService.class.getResource("/net/minecraft/server/MinecraftServer.class") != null;

		if (isDedicatedServer)LOG.info("Detected Dedicated Server Installation");
		else LOG.info("Detected Client Installation");

		if (isDedicatedServer) {
			bukkit = System.getProperty("cpmcore.bukkit", "false").equalsIgnoreCase("true") || CPMTransformerService.class.getResource("/org/bukkit/util/FileUtil.class") != null;
			if (bukkit)
				LOG.info("Detected Bukkit Server Installation");
		}

		try (BufferedReader rd = new BufferedReader(new InputStreamReader(CPMTransformerService.class.getResourceAsStream("/META-INF/cpm_core.cfg")))) {
			String ln;
			int map = isDedicatedServer ? 3 : 2;
			while ((ln = rd.readLine()) != null) {
				ln = ln.trim();
				if (ln.isEmpty() || ln.startsWith("#"))continue;
				String[] sp = ln.split(" ");
				if (sp[0].equals("method")) {
					if (sp[map].equals("?"))continue;
					int i = sp[map].indexOf('(');
					i = sp[map].lastIndexOf('/', i);
					String nameDesc = sp[map].substring(i + 1);
					int ind = nameDesc.indexOf('(');
					String mth = nameDesc.substring(0, ind);
					String par = nameDesc.substring(ind).replace('.', '/');
					methodObfs.put(sp[1], Pair.of(mth, par));
				} else if (sp[0].equals("class")) {
					if (sp[map].equals("?"))continue;
					classObfs.put(sp[map], sp[1]);
					classObfsR.put(sp[1], sp[map]);
				} else if (sp[0].equals("field")) {
					if (sp[map].equals("?"))continue;
					fieldObfs.put(sp[1], sp[map]);
				} else if (sp[0].equals("override")) {
					overrides.computeIfAbsent(sp[1], __ -> new HashSet<>()).add(sp[2]);
				}
			}
			LOG.info("Loaded Obfuscation Mappings");
		} catch (IOException e) {
			throw new RuntimeException("Failed to load obf mapping, installed mod jar is corrupted!", e);
		}

		if (bukkit) {
			classObfsBukkit = new HashMap<>();
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(CPMTransformerService.class.getResourceAsStream("/META-INF/cpm_bukkit.cfg")))) {
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
					} else if (sp[0].equals("class")) {
						classObfsBukkit.put(sp[1], sp[2]);
					} else if (sp[0].equals("field")) {
						fieldObfs.put(sp[1], sp[2]);
					}
				}
				LOG.info("Loaded Bukkit Mappings");
			} catch (IOException e) {
				throw new RuntimeException("Failed to load obf mapping, installed mod jar is corrupted!", e);
			}
		}

		transformers.put("net.minecraft.src.NetServerHandler", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("NetHandler;handleCustomPayload(Lnet.minecraft.src.Packet250CustomPayload;)V");

				InsnList lst = new InsnList();
				lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
				lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS_SERVER, "onServerPacket", obfMethodDesc("(Lnet/minecraft/src/Packet250CustomPayload;Lnet/minecraft/src/NetServerHandler;)Z")));
				LabelNode lbln = new LabelNode();
				lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
				lst.add(new InsnNode(Opcodes.RETURN));
				lst.add(lbln);

				input.interfaces.add("com/tom/cpm/common/ServerNetworkImpl");

				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, HAS_MOD_FIELD, "Z", null, 0));
				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, DATA_FIELD, "Lcom/tom/cpm/shared/config/PlayerData;", null, null));

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if (checkMethod(method, mth)) {
						LOG.info("CPM ServerNet Hook: Found processCustomPayload");
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM ServerNet Hook: injected");
					}
				}

				injectHasMod("ServerNet", input);

				injectInterfaceWrappers("ServerNet", HOOKS_CLASS_SERVER, input, "cpm$sendPacket(Ljava/lang/String;[B)V", "cpm$getPlayer()Lnet/minecraft/src/EntityPlayer;", "cpm$sendChat(Ljava/lang/String;)V", "cpm$kickPlayer(Ljava/lang/String;)V");

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
		transformers.put("net.minecraft.src.NetClientHandler", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				InsnList lst = new InsnList();
				lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
				lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onClientPacket", obfMethodDesc("(Lnet/minecraft/src/Packet250CustomPayload;Lnet/minecraft/src/NetClientHandler;)Z")));
				LabelNode lbln = new LabelNode();
				lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
				lst.add(new InsnNode(Opcodes.RETURN));
				lst.add(lbln);

				input.interfaces.add("com/tom/cpm/client/ClientNetworkImpl");

				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, HAS_MOD_FIELD, "Z", null, 0));

				Pair<String, String> mth = lookupMethod("NetHandler;handleCustomPayload(Lnet.minecraft.src.Packet250CustomPayload;)V");

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(checkMethod(method, mth)) {
						LOG.info("CPM ClientNet Hook: Found handleCustomPayload");
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM ClientNet Hook: injected");
					}
				}

				injectHasMod("ClientNet", input);

				injectInterfaceWrappers("ClientNet", HOOKS_CLASS, input, "cpm$sendPacket(Ljava/lang/String;[B)V", "cpm$getEntityByID(I)Lnet/minecraft/src/Entity;");

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
		transformers.put("net.minecraft.src.ModelBiped", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				input.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, NO_MODEL_SETUP_FIELD, "Z", null, 0));

				Pair<String, String> mth = lookupMethod("ModelBiped;setRotationAngles(FFFFFF)V");

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
		transformers.put("net.minecraft.src.RenderLiving", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("RenderLiving;doRenderLiving(Lnet.minecraft.src.EntityLiving;DDDFF)V");
				Pair<String, String> mth2 = lookupMethod("ModelBase;render(Lnet.minecraft.src.Entity;FFFFFF)V");

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
									args[args.length - 2] = obfType(Type.getObjectType("net/minecraft/src/RenderLiving"));
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
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.client.Minecraft", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("Minecraft;changeWorld(Lnet.minecraft.src.World;Ljava/lang/String;Lnet.minecraft.src.EntityPlayer;)V");
				Pair<String, String> mth2 = lookupMethod("Minecraft;displayGuiScreen(Lnet.minecraft.src.GuiScreen;)V");
				Pair<String, String> mth3 = lookupMethod("Minecraft;startWorld(Ljava/lang/String;Ljava/lang/String;Lnet.minecraft.src.WorldSettings;)V");
				String f = lookupField("Minecraft;thePlayer");
				Pair<String, String> mth4 = lookupMethod("Minecraft;runTick()V");
				Pair<String, String> mth5 = lookupMethod("Minecraft;isMultiplayerWorld()Z");
				Pair<String, String> mth6 = lookupMethod("Minecraft;lineIsCommand(Ljava/lang/String;)Z");

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(checkMethod(method, mth)) {
						LOG.info("CPM ClientLogout Hook: Found loadWorld");
						InsnList lst = new InsnList();
						lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
						lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onLogout", obfMethodDesc("(Lnet/minecraft/src/World;)V")));
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM ClientLogout Hook: injected");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof FieldInsnNode && insnNode.getOpcode() == Opcodes.PUTFIELD) {
								FieldInsnNode fn = (FieldInsnNode) insnNode;
								if (fn.name.equals(f)) {
									LOG.info("CPM ClientLogin Hook: found create player");
									method.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onSinglePlayerLogin", "()V"));
									LOG.info("CPM ClientLogin Hook: injected");
								}
							}
						}
					} else if(checkMethod(method, mth2)) {
						LOG.info("CPM Open Gui Hook: Found displayGuiScreen");
						InsnList list = new InsnList();
						list.add(new VarInsnNode(Opcodes.ALOAD, 1));
						list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "openGui", obfMethodDesc("(Lnet/minecraft/src/GuiScreen;)Lnet/minecraft/src/GuiScreen;")));
						list.add(new VarInsnNode(Opcodes.ASTORE, 1));
						method.instructions.insertBefore(method.instructions.getFirst(), list);
						LOG.info("CPM Open Gui Hook: injected");
					} else if (checkMethod(method, mth3)) {
						LOG.info("CPM Start World Hook: Found startWorld");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if (mn.name.equals("<init>") && mn.owner.equals(obfObj("net/minecraft/src/World"))) {
									LOG.info("CPM Button Click Hook: Found World constructor");
									InsnList list = new InsnList();
									list.add(new VarInsnNode(Opcodes.ALOAD, 4));
									list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "startSinglePlayer", obfMethodDesc("(Lnet/minecraft/src/ISaveHandler;)V")));
									method.instructions.insert(mn, list);
									LOG.info("CPM Start World Hook: injected");
								}
							}
						}
					} else if(spc && checkMethod(method, mth4)) {
						LOG.info("CPM SPC Hook: Found runTick");

						int i = 0;
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof MethodInsnNode && checkMethod((MethodInsnNode) insnNode, mth5) && i++ < 2) {
								LOG.info("CPM SPC Hook: Found isMultiplayerWorld " + i);
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								mn.owner = HOOKS_CLASS;
								mn.name = "isChatEnabled";
								mn.desc = "(Lnet/minecraft/client/Minecraft;)Z";
								mn.setOpcode(Opcodes.INVOKESTATIC);
								LOG.info("CPM SPC Hook: injected " + i);
							}
						}
					} else if (spc && checkMethod(method, mth6)) {
						LOG.info("CPM SPC Hook: Found lineIsCommand");
						method.instructions.clear();
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
						method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "testCommand", obfMethodDesc("(Lnet/minecraft/client/Minecraft;Ljava/lang/String;)Z")));
						method.instructions.add(new InsnNode(Opcodes.IRETURN));
						LOG.info("CPM SPC Hook: Found injected");
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.src.RenderPlayer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("RenderPlayer;renderSpecials(Lnet.minecraft.src.EntityPlayer;F)V");
				Pair<String, String> mth2 = lookupMethod("RenderPlayer;drawFirstPersonHand()V");
				Pair<String, String> mth3 = lookupMethod("RenderPlayer;renderPlayer(Lnet.minecraft.src.EntityPlayer;DDDFF)V");
				Pair<String, String> mth4 = lookupMethod("RenderPlayer;renderName(Lnet.minecraft.src.EntityPlayer;DDD)V");
				Pair<String, String> mth5 = lookupMethod("Render;loadDownloadableImageTexture(Ljava/lang/String;Ljava/lang/String;)Z");

				input.interfaces.add("com/tom/cpmcore/IPlayerRenderer");
				for(MethodNode method : (List<MethodNode>) input.methods) {
					if(checkMethod(method, mth)) {
						LOG.info("CPM Cape Hook: Found renderEquippedItems");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if(checkMethod(mn, mth5)) {
									LOG.info("CPM Cape Hook: Found renderCape");
									InsnList lst = new InsnList();
									lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
									lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
									lst.add(new VarInsnNode(Opcodes.FLOAD, 2));
									lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "renderCape", obfMethodDesc("(ZLnet/minecraft/src/RenderPlayer;Lnet/minecraft/src/EntityPlayer;F)Z")));
									method.instructions.insert(insnNode, lst);
									LOG.info("CPM Cape Hook: injected");
								}
							}
						}
					} else if(checkMethod(method, mth2)) {
						LOG.info("CPM Hand Hook: Found renderFirstPersonArm");
						InsnList lst = new InsnList();
						lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onHandPre", obfMethodDesc("(Lnet/minecraft/src/RenderPlayer;)V")));
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM Render Hand Hook Pre: injected");

						InsnList lst2 = new InsnList();
						lst2.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onHandPost", obfMethodDesc("(Lnet/minecraft/src/RenderPlayer;)V")));

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof InsnNode){
								if(insnNode.getOpcode() == Opcodes.RETURN) {
									method.instructions.insertBefore(insnNode, lst2);
									LOG.info("CPM Render Hand Hook Post: injected");
								}
							}
						}
					} else if(checkMethod(method, mth3)) {
						LOG.info("CPM Player Render Hook: Found renderPlayer");
						InsnList lst2 = new InsnList();
						lst2.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst2.add(new VarInsnNode(Opcodes.ALOAD, 1));
						lst2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "playerRenderPre", obfMethodDesc("(Lnet/minecraft/src/RenderPlayer;Lnet/minecraft/src/EntityPlayer;)V")));

						InsnList lst3 = new InsnList();
						lst3.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst3.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "playerRenderPost", obfMethodDesc("(Lnet/minecraft/src/RenderPlayer;)V")));

						method.instructions.insertBefore(method.instructions.getFirst(), lst2);
						LOG.info("CPM Player Render Hook Pre: injected");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if(insnNode instanceof InsnNode){
								if(insnNode.getOpcode() == Opcodes.RETURN) {
									method.instructions.insertBefore(insnNode, lst3);
									LOG.info("CPM Player Render Hook Post: injected");
								}
							}
						}
					} else if(checkMethod(method, mth4)) {
						LOG.info("CPM Name Hook: Found renderName method");
						InsnList lst = new InsnList();
						lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
						lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
						lst.add(new VarInsnNode(Opcodes.DLOAD, 2));
						lst.add(new VarInsnNode(Opcodes.DLOAD, 4));
						lst.add(new VarInsnNode(Opcodes.DLOAD, 6));
						lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onRenderName", obfMethodDesc("(Lnet/minecraft/src/RenderPlayer;Lnet/minecraft/src/EntityPlayer;DDD)Z")));
						LabelNode lbln = new LabelNode();
						lst.add(new JumpInsnNode(Opcodes.IFEQ, lbln));
						lst.add(new InsnNode(Opcodes.RETURN));
						lst.add(lbln);
						method.instructions.insertBefore(method.instructions.getFirst(), lst);
						LOG.info("CPM Name Hook: injected");
					}
				}
				//func_77036_a(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V
				//renderModel(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V
				String name = CPMLoadingPlugin.deobf ? "renderModel" : "func_77036_a";
				LOG.info("CPM Render Invis Hook: Injecting method " + (CPMLoadingPlugin.deobf ? "deobf" : ""));
				MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, name, obfMethodDesc("(Lnet/minecraft/src/EntityLiving;FFFFFF)V"), null, null);
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 4));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 5));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 6));
				mn.instructions.add(new VarInsnNode(Opcodes.FLOAD, 7));
				mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onRenderPlayerModel", obfMethodDesc("(Lnet/minecraft/src/RenderPlayer;Lnet/minecraft/src/EntityLiving;FFFFFF)Z")));
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
				mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, obfObj("net/minecraft/src/RenderLiving"), name, obfMethodDesc("(Lnet/minecraft/src/EntityLiving;FFFFFF)V")));
				mn.instructions.add(new InsnNode(Opcodes.RETURN));
				mn.maxLocals = 8;
				mn.maxStack = 8;
				input.methods.add(mn);
				LOG.info("CPM Render Invis Hook: injected");

				name = CPMLoadingPlugin.deobf ? "bindEntityTexture" : "func_110777_b";
				mn = new MethodNode(Opcodes.ACC_PUBLIC, "cpm$bindEntityTexture", obfMethodDesc("(Lnet/minecraft/src/EntityPlayer;)V"), null, null);
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, input.name, name, obfMethodDesc("(Lnet/minecraft/src/Entity;)V")));
				mn.instructions.add(new InsnNode(Opcodes.RETURN));
				mn.maxLocals = 2;
				mn.maxStack = 2;
				input.methods.add(mn);
				LOG.info("CPM Render Invis Hook/Bind Texture: injected");

				return input;
			}
		});
		transformers.put("net.minecraft.src.ThreadDownloadImage", new UnaryOperator<ClassNode>() {

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
		transformers.put("net.minecraft.src.EntityTrackerEntry", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("EntityTrackerEntry;tryStartWachingThis(Lnet.minecraft.src.EntityPlayerMP;)V");
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
								in.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS_SERVER, "startTracking", obfMethodDesc("(Lnet/minecraft/src/EntityPlayer;Lnet/minecraft/src/EntityPlayerMP;)V")));
								method.instructions.insertBefore(insnNode, in);
								LOG.info("CPM Start Tracking Hook: injected");
							}
						}
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.src.EntityRenderer", new UnaryOperator<ClassNode>() {

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
		transformers.put("net.minecraft.src.GuiScreen", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("GuiScreen;setWorldAndResolution(Lnet.minecraft.client.Minecraft;II)V");
				Pair<String, String> mth2 = lookupMethod("GuiScreen;mouseClicked(III)V");
				Pair<String, String> mth3 = lookupMethod("GuiScreen;actionPerformed(Lnet.minecraft.src.GuiButton;)V");

				for (MethodNode method : (List<MethodNode>) input.methods) {
					if (checkMethod(method, mth)) {
						LOG.info("CPM Init Screen Hook: Found setWorldAndResolution");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof InsnNode && insnNode.getOpcode() == Opcodes.RETURN) {
								InsnList list = new InsnList();
								list.add(new VarInsnNode(Opcodes.ALOAD, 0));
								list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onInitScreen", obfMethodDesc("(Lnet/minecraft/src/GuiScreen;)V")));
								method.instructions.insertBefore(insnNode, list);
								LOG.info("CPM Init Screen Hook: injected");
							}
						}
					} else if (checkMethod(method, mth2) || method.name.equals("getEntityID")) {
						LOG.info("CPM Button Click Hook: Found mouseClicked");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof MethodInsnNode && (checkMethod((MethodInsnNode) insnNode, mth3) || ((MethodInsnNode) insnNode).name.equals("useItem"))){
								LOG.info("CPM Button Click Hook: Found actionPerformed");
								InsnList list = new InsnList();
								list.add(new InsnNode(Opcodes.DUP));
								list.add(new VarInsnNode(Opcodes.ALOAD, 0));
								list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "onGuiButtonClick", obfMethodDesc("(Lnet/minecraft/src/GuiButton;Lnet/minecraft/src/GuiScreen;)V")));
								method.instructions.insertBefore(insnNode, list);
								LOG.info("CPM Button Click Hook: injected");
							}
						}
					}
				}
				return input;
			}
		});
		transformers.put("net.minecraft.src.OpenGlHelper", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("OpenGlHelper;setLightmapTextureCoords(IFF)V");

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if (checkMethod(method, mth)) {
						LOG.info("CPM OpenGlHelper Hook: Found setLightmapTextureCoords");

						InsnList list = new InsnList();
						list.add(new VarInsnNode(Opcodes.ILOAD, 0));
						list.add(new VarInsnNode(Opcodes.FLOAD, 1));
						list.add(new VarInsnNode(Opcodes.FLOAD, 2));
						list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "setLightmap", "(IFF)V"));

						method.instructions.insertBefore(method.instructions.getFirst(), list);
						LOG.info("CPM OpenGlHelper Hook: Found setLightmapTextureCoords");
					}
				}
				return input;
			}
		});
		transformers.put("com.tom.cpm.CustomPlayerModels", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if (method.name.equals("makeProxy")) {
						LOG.info("CPM Proxy Hook: Found makeProxy");

						String clazz = isDedicatedServer ? "com/tom/cpm/server/ServerProxy" : "com/tom/cpm/client/ClientProxy";

						method.instructions.clear();
						method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, clazz, method.name, method.desc));
						method.instructions.add(new InsnNode(Opcodes.ARETURN));

						LOG.info("CPM Proxy Hook: injected");
					}
				}
				return input;
			}
		});
		if (spc) {
			transformers.put("net.minecraft.src.GuiChat", new UnaryOperator<ClassNode>() {

				@Override
				public ClassNode apply(ClassNode input) {
					Pair<String, String> mth = lookupMethod("GuiScreen;keyTyped(CI)V");
					Pair<String, String> mth2 = lookupMethod("GuiChat;completePlayerName()V");
					Pair<String, String> mth3 = lookupMethod("GuiScreen;doesGuiPauseGame()Z");
					boolean pauseMthFound = false;

					for(MethodNode method : (List<MethodNode>) input.methods) {
						if (checkMethod(method, mth)) {
							LOG.info("CPM SPC Hook: Found keyTyped");

							for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
								AbstractInsnNode insnNode = it.next();
								if (insnNode instanceof MethodInsnNode){
									MethodInsnNode mn = (MethodInsnNode) insnNode;
									if (checkMethod(mn, mth2)) {
										LOG.info("CPM SPC Hook: Found completePlayerName");
										mn.setOpcode(Opcodes.INVOKESTATIC);
										mn.owner = HOOKS_CLASS;
										mn.name = "tabComplete";
										mn.desc = obfMethodDesc("(Lnet/minecraft/src/GuiChat;)V");
										LOG.info("CPM SPC Hook: injected");
									}
								}
							}
						} else if (checkMethod(method, mth3)) {
							pauseMthFound = true;
							LOG.info("CPM SPC Pause Hook: doesGuiPauseGame found skipping");
						}
					}

					if (!pauseMthFound) {
						MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, mth3.getKey(), mth3.getValue(), null, null);
						mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
						mn.instructions.add(new InsnNode(Opcodes.IRETURN));
						mn.maxLocals = 2;
						mn.maxStack = 2;
						input.methods.add(mn);
						LOG.info("CPM SPC Pause Hook: injected");
					}

					return input;
				}
			});
		}
		transformers.put("net.minecraft.server.MinecraftServer", new UnaryOperator<ClassNode>() {

			@Override
			public ClassNode apply(ClassNode input) {
				Pair<String, String> mth = lookupMethod("MinecraftServer;stopServer()V");
				Pair<String, String> mth2 = lookupMethod("MinecraftServer;initWorld(Lnet.minecraft.src.ISaveFormat;Ljava/lang/String;JLnet.minecraft.src.WorldType;)V");

				for(MethodNode method : (List<MethodNode>) input.methods) {
					if (checkMethod(method, mth)) {
						LOG.info("CPM Server Hook: Found stopServer");

						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof InsnNode && insnNode.getOpcode() == Opcodes.RETURN) {
								method.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS_SERVER, "onStopped", "()V"));
								LOG.info("CPM Server Hook: injected");
							}
						}
					} else if (checkMethod(method, mth2)) {
						LOG.info("CPM Server Hook: Found initWorld");
						InsnList list = new InsnList();
						list.add(new VarInsnNode(Opcodes.ALOAD, 9));
						list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS_SERVER, "onStarting", obfMethodDesc("(Lnet/minecraft/src/AnvilSaveHandler;)V")));
						String ws = classObfsR.get("net.minecraft.src.WorldServer");
						for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
							AbstractInsnNode insnNode = it.next();
							if (insnNode instanceof MethodInsnNode) {
								MethodInsnNode mn = (MethodInsnNode) insnNode;
								if (mn.owner.equals(ws) && mn.name.equals("<init>")) {
									method.instructions.insert(insnNode, list);
									LOG.info("CPM Server Hook: injected");
								}
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

	@SuppressWarnings("unchecked")
	private static void injectInterfaceWrappers(String name, String hooks, ClassNode input, String... methods) {
		for (int i = 0; i < methods.length; i++) {
			String mnd = methods[i];

			int in = mnd.indexOf('(');
			String mn = mnd.substring(0, in);
			String md = mnd.substring(in);

			md = obfMethodDesc(md);

			MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, mn, md, null, null);
			input.methods.add(method);

			Type mdt = Type.getMethodType(md);
			Type[] args = new Type[mdt.getArgumentTypes().length + 1];
			args[0] = Type.getObjectType(input.name);
			System.arraycopy(mdt.getArgumentTypes(), 0, args, 1, mdt.getArgumentTypes().length);

			for (int j = 0; j < args.length; j++) {
				Type a = args[j];
				method.instructions.add(new VarInsnNode(a.getOpcode(Opcodes.ILOAD), j));
			}

			method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "inj_" + mn.substring(4), Type.getMethodDescriptor(mdt.getReturnType(), args)));
			method.instructions.add(new InsnNode(mdt.getReturnType().getOpcode(Opcodes.IRETURN)));

			method.maxLocals = mdt.getArgumentTypes().length + 2;
			method.maxStack = mdt.getArgumentTypes().length + 2;
			LOG.info("CPM " + name + "/" + mn.substring(4) + ": injected");
		}
	}

	private static Type obfType(Type type) {
		if (type.getSort() == Type.OBJECT) {
			String in = type.getInternalName();
			if (in.startsWith("net/minecraft") || (isDedicatedServer && in.equals("BaseMod"))) {
				if (isDedicatedServer && in.equals("BaseMod"))return Type.getObjectType("forge/NetworkMod");
				String id = in.replace('/', '.');
				if (bukkit) {
					String obf = classObfsBukkit.get(id);
					if (obf != null)
						return Type.getObjectType(obf.replace('.', '/'));
					return Type.getObjectType("net/minecraft/server/" + in.substring(in.lastIndexOf('/') + 1));
				}
				String obf = classObfsR.get(id);
				if (obf == null) {
					LOG.info("Missing mapping: class " + id);
					return type;
				}
				if (CPMLoadingPlugin.deobf) {
					return type;
				}
				return Type.getObjectType(obf);
			}
		}
		return type;
	}

	private static String obfObj(String in) {
		return obfType(Type.getObjectType(in)).getInternalName();
	}

	private static String obfMethodDesc(String descIn) {
		Type t = Type.getMethodType(descIn);
		Type[] args = t.getArgumentTypes();
		Type ret = t.getReturnType();
		ret = obfType(ret);
		for (int i = 0; i < args.length; i++)
			args[i] = obfType(args[i]);

		if (CPMLoadingPlugin.deobf) {
			return descIn;
		}
		return Type.getMethodDescriptor(ret, args);
	}

	private static Pair<String, String> lookupMethod(String fullDesc) {
		if (!methodObfs.containsKey(fullDesc)) {
			LOG.info("Missing mapping: method " + fullDesc);
		}
		if (CPMLoadingPlugin.deobf || fullDesc.contains(";cpm$")) {
			String nameDesc = fullDesc.split(";", 2)[1];
			int ind = nameDesc.indexOf('(');
			String mth = nameDesc.substring(0, ind);
			String par = nameDesc.substring(ind).replace('.', '/');
			return Pair.of(mth, par);
		} else {
			Pair<String, String> p = methodObfs.get(fullDesc);
			if (p.getKey().startsWith("!")) {
				return Pair.of(p.getKey().substring(1), obfMethodDesc(p.getValue()));
			}
			return p;
		}
	}

	private static String lookupField(String fullDesc) {
		if (!fieldObfs.containsKey(fullDesc)) {
			LOG.warn("Missing mapping: field " + fullDesc);
		}
		if (CPMLoadingPlugin.deobf) {
			return fullDesc.split(";")[1];
		}
		String d = fieldObfs.get(fullDesc);
		return d.substring(d.lastIndexOf('/') + 1);
	}

	@SuppressWarnings("unchecked")
	public static byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(transformers == null)return basicClass;
		UnaryOperator<ClassNode> tr = transformers.get(transformedName);
		List<String> ats = fieldATs.get(name);
		boolean remap = !CPMLoadingPlugin.deobf && (name.startsWith("com.tom.cpm") || name.equals("mod_CPM")) && !name.startsWith("com.tom.cpm.shared") && !name.startsWith("com.tom.cpm.externals") && !name.startsWith("com.tom.cpm.api");
		if(tr != null || ats != null || remap) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
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
			if (remap) {
				ClassNode cn = new ClassNode();
				classNode.accept(new RemappingClassAdapter(cn, new DistRemapper(classNode)));
				classNode = cn;
				LOG.info("Applied sided mappings");
			}
			ClassWriter writer = new ClassWriter(0);//ClassWriter.COMPUTE_FRAMES
			classNode.accept(writer);
			if (dump) {
				File d = new File("asm/dump");
				d.mkdirs();
				File o = new File(d, name + ".class");
				try (FileOutputStream f = new FileOutputStream(o)) {
					f.write(writer.toByteArray());
				} catch (IOException e) {
				}
			}
			return writer.toByteArray();
		}
		return basicClass;
	}

	public static byte[] transform(String var1, byte[] var2) {
		String d = classObfs.getOrDefault(var1, var1);
		return transform(var1, d, var2);
	}

	public static class DistRemapper extends Remapper {
		private ClassNode classNode;
		private Set<String> fs = new HashSet<>();
		private Set<String> ms = new HashSet<>();
		private boolean sm;
		private Set<String> override;

		@SuppressWarnings("unchecked")
		public DistRemapper(ClassNode classNode) {
			this.classNode = classNode;

			for (FieldNode f : (List<FieldNode>) classNode.fields) {
				fs.add(f.name);
			}

			for (MethodNode f : (List<MethodNode>) classNode.methods) {
				ms.add(f.name);
			}

			sm = classNode.superName.startsWith("net/minecraft");

			override = overrides.getOrDefault(classNode.name, Collections.emptySet());
		}

		@Override
		public String map(String var1) {
			return obfObj(var1);
		}

		@Override
		public String mapFieldName(String owner, String name, String desc) {
			if (owner.startsWith("net/minecraft")) {
				return lookupField(owner.substring(owner.lastIndexOf('/') + 1) + ";" + name);
			} else if (!fs.contains(name) && owner.equals(classNode.name) && sm) {
				return lookupField(classNode.superName.substring(classNode.superName.lastIndexOf('/') + 1) + ";" + name);
			}
			return name;
		}

		@Override
		public String mapMethodName(String owner, String name, String desc) {
			if (name.equals("<init>") || name.equals("<clinit>"))return name;
			if (owner.startsWith("net/minecraft")) {
				return lookupMethod(owner.substring(owner.lastIndexOf('/') + 1) + ";" + name + desc).getKey();
			}
			if ((!ms.contains(name) || override.contains(name + desc)) && owner.equals(classNode.name) && sm) {
				return lookupMethod(classNode.superName.substring(classNode.superName.lastIndexOf('/') + 1) + ";" + name + desc).getKey();
			}
			return name;
		}
	}
}
