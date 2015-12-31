package org.mermaid.vertxmvc.classreading;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

public class AnnotationMetadataReadingVisitor extends
		ClassMetadataReadingVisitor implements AnnotationMetadata {

	protected final Set<String> annotationSet = new LinkedHashSet<String>(4);

	protected final Map<String, Set<String>> metaAnnotationMap = new LinkedHashMap<String, Set<String>>(
			4);

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		String className = Type.getType(desc).getClassName();
		this.annotationSet.add(className);
		return super.visitAnnotation(desc, visible);
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public Set<String> getAnnotationTypes() {
		return annotationSet;
	}

	@Override
	public boolean hasAnnotation(String annotationType) {
		return annotationSet.contains(annotationType);
	}
}
