package org.mermaid.vertxmvc.classreading;

public class Metadata {
	private ClassMetadata classMetadata;
	private AnnotationMetadata annotationMetadata;

	public ClassMetadata getClassMetadata() {
		return classMetadata;
	}

	public void setClassMetadata(ClassMetadata classMetadata) {
		this.classMetadata = classMetadata;
	}

	public AnnotationMetadata getAnnotationMetadata() {
		return annotationMetadata;
	}

	public void setAnnotationMetadata(AnnotationMetadata annotationMetadata) {
		this.annotationMetadata = annotationMetadata;
	}
}
