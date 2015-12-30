package org.mermaid.vertxmvc.classreading;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class AnnotationAttributesReadingVisitor extends AnnotationVisitor {

	public AnnotationAttributesReadingVisitor() {
		super(Opcodes.ASM5);
	}

}
