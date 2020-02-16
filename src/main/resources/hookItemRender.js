function initializeCoreMod() {
    // Format of debugInfo:
    // {
    //      calls: String[]
    //      toInsert: InsnList
    // }
    var debugInfo = null

    function toList(insns) {
        debugInfo.calls.push(['toList', insns])
        var list = []
        for (var i = 0; i < insns.size(); i++) {
            list.push(insns.get(i))
        }
        return list
    }

    function findInstructions(method, filter) {
        debugInfo.calls.push(['findInstructions', method, filter])
        return toList(method.instructions).filter(filter)
    }

    function findSingleInstruction(method, filter) {
        debugInfo.calls.push(['findSingleInstruction', method, filter])
        var matches = findInstructions(method, filter)
        if (matches.length == 0) {
            throw 'Found no matching instructions.'
        }
        else if (matches.length > 1) {
            throw 'Found multiple matching instructions.'
        }
        return matches[0]
    }

    function findNextInstruction(insn, filter) {
        debugInfo.calls.push(['findNextInstruction', insn, filter])
        assertNonNull(insn)
        do {
            insn = insn.getNext()
        } while (insn != null && !filter(insn))
        return insn
    }

    function findPreviousInstruction(insn, filter) {
        debugInfo.calls.push(['findPreviousInstruction', insn, filter])
        assertNonNull(insn)
        do {
            insn = insn.getPrevious()
        } while (insn != null && !filter(insn))
        return insn
    }

    function getRealNextInstruction(insn) {
        debugInfo.calls.push(['getRealNextInstruction', insn])
        return findNextInstruction(insn, function (insn) { return insn.getOpcode() >= 0 })
    }

    function getRealPreviousInstruction(insn) {
        debugInfo.calls.push(['getRealPreviousInstruction', insn])
        return findPreviousInstruction(insn, function (insn) { return insn.getOpcode() >= 0 })
    }

    function assertNonNull(val) {
        debugInfo.calls.push(['assertNonNull', val])
        if (val == null) {
            throw 'Got null'
        }
        return val
    }

    function withDebugHandling(func) {
        return function (method) {
            debugInfo = {
                calls: [],
            }
            try {
                return func(method)
            } catch (error) {
                function formatInstruction(insn) {
                    if (!insn) {
                        return 'No instruction'
                    }

                    var message = insn.toString()
                    function logAttribute(attr) {
                        if (insn[attr] != undefined) {
                            message += ', ' + attr + ' = ' + insn[attr]
                        }
                    }
                    logAttribute('opcode')
                    logAttribute('var')
                    logAttribute('owner')
                    logAttribute('name')
                    logAttribute('desc')
                    logAttribute('cst')
                    return message;
                }

                function formatInstructionList(insns) {
                    var message = ''
                    for (var i = 0; i < insns.length; i++) {
                        message += '\n' + formatInstruction(insns[i])
                    }
                    return message
                }

                var message = error;

                message += '\n\nRecent calls (last first):'
                for (var i = debugInfo.calls.length - 1; i > debugInfo.calls.length - 11 && i >= 0; i--) {
                    var call = debugInfo.calls[i]
                    message += '\n' +  call[0] + '(' + call.slice(1).join(', ') + ')'
                }

                message += '\n\nFull instruction list:'
                message += formatInstructionList(toList(method.instructions))

                message += '\n\nReal instructions:'
                message += formatInstructionList(findInstructions(method, function(insn) { return insn.getOpcode() >= 0 }))

                if (debugInfo.toInsert != null) {
                    message += '\n\nTo be inserted:'
                    message += formatInstructionList(toList(debugInfo.toInsert))
                }

                throw message + '\n'
            } finally {
                debugInfo = null
            }
        }
    }

	return {
		'ItemRenderer.renderItemModelIntoGUI.postHook': {
			'target': {
			    'type': 'METHOD',
		        'class': 'net.minecraft.client.renderer.ItemRenderer',
				// protected void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel)
			    'methodName': 'renderItemModelIntoGUI',
			    'methodDesc': '(Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/renderer/model/IBakedModel;)V',
			},
			'transformer': withDebugHandling(function (method) {
                var Opcodes = Java.type('org.objectweb.asm.Opcodes')
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList')
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode')
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode')

                // com.maienm.accessibilitymod.CoreModHooks.INSTANCE.onItemRendererRenderItemModelIntoGUI(stack, xPosition, yPosition)
                var toInsert = debugInfo.toInsert = new InsnList()
                toInsert.add(new FieldInsnNode(Opcodes.GETSTATIC, 'com/maienm/accessibilitymod/CoreModHooks', 'INSTANCE', 'Lcom/maienm/accessibilitymod/CoreModHooks;'))
                toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1)) // ItemStack
                toInsert.add(new VarInsnNode(Opcodes.ILOAD, 2)) // x
                toInsert.add(new VarInsnNode(Opcodes.ILOAD, 3)) // y
                toInsert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 'com/maienm/accessibilitymod/CoreModHooks', 'onItemRendererRenderItemModelIntoGUI', '(Lnet/minecraft/item/ItemStack;II)V', false))

                var insn = findSingleInstruction(method, function (insn) { return insn.getOpcode() == Opcodes.RETURN })
                method.instructions.insertBefore(insn, toInsert)
				return method
			}),
		},
		// JEI uses a separate method to render items in the right bar (but the regular method for items in the recipes).
		'ItemStackFastRenderer.uncheckedRenderItemAndEffectIntoGUI.postHook': {
			'target': {
			    // private void uncheckedRenderItemAndEffectIntoGUI(IEditModeConfig editModeConfig, IWorldConfig worldConfig)
				'type': 'METHOD',
				'class': 'mezz.jei.render.ItemStackFastRenderer',
			    'methodName': 'uncheckedRenderItemAndEffectIntoGUI',
			    'methodDesc': '(Lmezz/jei/config/IEditModeConfig;Lmezz/jei/config/IWorldConfig;)V',
			},
			'transformer': withDebugHandling(function (method) {
                var Opcodes = Java.type('org.objectweb.asm.Opcodes')
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList')
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode')
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode')

                // com.maienm.accessibilitymod.CoreModHooks.INSTANCE.onItemStackFastRendererUncheckedRenderItemAndEffectIntoGUI(stack, x, y, z)
                var toInsert = debugInfo.toInsert = new InsnList()
                toInsert.add(new FieldInsnNode(Opcodes.GETSTATIC, 'com/maienm/accessibilitymod/CoreModHooks', 'INSTANCE', 'Lcom/maienm/accessibilitymod/CoreModHooks;'))
                toInsert.add(new VarInsnNode(Opcodes.ALOAD, 3)) // ItemStack
                // area.getX()
                toInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                toInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'mezz/jei/render/ItemStackFastRenderer', 'area', 'Lnet/minecraft/client/renderer/Rectangle2d;'))
                toInsert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 'net/minecraft/client/renderer/Rectangle2d', 'getX', '()I'))
                // area.getY()
                toInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                toInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'mezz/jei/render/ItemStackFastRenderer', 'area', 'Lnet/minecraft/client/renderer/Rectangle2d;'))
                toInsert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 'net/minecraft/client/renderer/Rectangle2d', 'getY', '()I'))
                toInsert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 'com/maienm/accessibilitymod/CoreModHooks', 'onItemStackFastRendererUncheckedRenderItemAndEffectIntoGUI', '(Lnet/minecraft/item/ItemStack;II)V', false))

                var insn = findSingleInstruction(method, function (insn) { return insn.getOpcode() == Opcodes.RETURN && getRealNextInstruction(insn) == null })
                method.instructions.insertBefore(insn, toInsert)
				return method
			}),
		},
	}
}