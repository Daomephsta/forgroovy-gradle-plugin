package daomephsta.forgroovy.gradleplugin;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import groovy.lang.GroovyObject;
import net.minecraftforge.gradle.user.ReobfTransformer;

public class IndyCallReobfuscatingTransformer implements ReobfTransformer
{
	private static final long serialVersionUID = -7205961699415466890L;
	private static final String GROOVY_OBJ_INTERNAL_NAME = GroovyObject.class.getName().replace('.', '/');
	
	@Override
	public byte[] transform(byte[] classBytes)
	{
		ClassReader reader = new ClassReader(classBytes);
		ClassNode classNode = new ClassNode();
		reader.accept(classNode, ClassReader.SKIP_DEBUG);
		
	
		//NO OP on non-groovy classes
		if(!classNode.interfaces.contains(GROOVY_OBJ_INTERNAL_NAME))
			return classBytes;	
		
		File f = new File("asmdump/" + reader.getClassName().replace('/', '.'));
		f.getParentFile().mkdirs();
		try(Writer w = new FileWriter(f))
		{
			for(MethodNode methodNode : classNode.methods)
			{
				w.write(methodNode.name + System.lineSeparator());
				Deque<Object> operandStack = new ArrayDeque<>();
				for(int i = 0; i < methodNode.instructions.size(); i++)
				{
					AbstractInsnNode insnNode = methodNode.instructions.get(i);
					if (insnNode instanceof LdcInsnNode)
					{
						operandStack.push(((LdcInsnNode) insnNode).cst);
						w.write("LDC " + ((LdcInsnNode) insnNode).cst + System.lineSeparator());
					}
					else if (insnNode instanceof InsnNode)
					{
						InsnNode noArgsInsn = (InsnNode) insnNode;
						switch(noArgsInsn.getType())
						{
							case Opcodes.POP:
								operandStack.pop();
								w.write("POP" + System.lineSeparator());
								break;
							default:
								w.write("INSNNODE" + System.lineSeparator());
								break;
						}
					}
					else if (insnNode instanceof InvokeDynamicInsnNode)
					{
						InvokeDynamicInsnNode invDynInsn = (InvokeDynamicInsnNode) insnNode;
						w.write(String.format("Name: %s\nDesc: %s\nBootstrap: %s\nBootstrap args: %s\nStack: %s\n", 
								invDynInsn.name, invDynInsn.desc, invDynInsn.bsm, invDynInsn.bsmArgs, operandStack));
						
					}
					else
						w.write(insnNode.getClass().getSimpleName() + System.lineSeparator());
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		//ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES)
		return classBytes;
	}
}
