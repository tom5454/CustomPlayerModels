function initializeCoreMod() {
    return {
        'cpm:PlayerRenderer#getTextureLocation': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.player.PlayerRenderer',
                'methodName': 'm_5478_',
                'methodDesc': '(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;'
            },
            'transformer': function(method) {
				var owner = "com/tom/cpm/core/CPMASMClientHooks";
                var name = "onGetEntityTexture";
                var desc = "(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;)Lnet/minecraft/resources/ResourceLocation;";
				var instr = method.instructions;
				
 				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				
				ASMAPI.log('INFO', 'Patching PlayerRenderer#getTextureLocation');
				var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.ARETURN) {
                        var insn = new InsnList();
						insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
						insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc, false));
                        instr.insertBefore(n, insn);
                        break;
					}
				}
                return method;
            }
        }
    }
}