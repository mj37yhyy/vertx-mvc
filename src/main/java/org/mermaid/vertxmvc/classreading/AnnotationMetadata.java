package org.mermaid.vertxmvc.classreading;

import java.util.Set;

public interface AnnotationMetadata {

	/**
	 * Return the names of all annotation types that are <em>present</em> on the
	 * underlying class.
	 * 
	 * @return the annotation type names
	 */
	Set<String> getAnnotationTypes();

	/**
	 * Determine whether an annotation of the given type is <em>present</em> on
	 * the underlying class.
	 * 
	 * @param annotationType
	 *            the annotation type to look for
	 * @return whether a matching annotation is present
	 */
	boolean hasAnnotation(String annotationType);

}
