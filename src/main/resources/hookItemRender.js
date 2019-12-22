function initializeCoreMod() {
	return {
		'ItemRenderer.renderItemModelIntoGUI.postHook': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.renderer.ItemRenderer',
				// protected void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel)
				 'methodName': 'renderItemModelIntoGUI',
				 'methodDesc': '(Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/renderer/model/IBakedModel;)V',
			},
			'transformer': function (method) {
				var Opcodes = Java.type('org.objectweb.asm.Opcodes');
				var numInstructions = method.instructions.size();
				for (var i = 0; i < numInstructions; i++) {
					var insn = method.instructions.get(i);
					if (insn.getOpcode() == Opcodes.RETURN) {
						var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
						var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
						var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
						var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

						// com.maienm.accessibilitymod.CoreModHooks.INSTANCE.onItemRendererRenderItemModelIntoGUI(this, fr, stack, xPosition, yPosition);
						var toInsert = new InsnList();
						toInsert.add(new FieldInsnNode(Opcodes.GETSTATIC, 'com/maienm/accessibilitymod/CoreModHooks', 'INSTANCE', 'Lcom/maienm/accessibilitymod/CoreModHooks;'));
						toInsert.add(new VarInsnNode(Opcodes.ALOAD, 0)); // ItemRenderer (this)
						toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ItemStack
						toInsert.add(new VarInsnNode(Opcodes.ILOAD, 2)); // x
						toInsert.add(new VarInsnNode(Opcodes.ILOAD, 3)); // y
						toInsert.add(new VarInsnNode(Opcodes.ALOAD, 4)); // IBakedModel
						toInsert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 'com/maienm/accessibilitymod/CoreModHooks', 'onItemRendererRenderItemModelIntoGUI', '(Lnet/minecraft/client/renderer/ItemRenderer;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/renderer/model/IBakedModel;)V', false));

						method.instructions.insertBefore(insn, toInsert);
					}
				}
				return method
			}
		}
	}
}