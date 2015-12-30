package org.mermaid.vertxmvc.classreading;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

public class MetadataReader {

	public Metadata getMetadataReader(Resource resource) throws IOException {
		InputStream is = new BufferedInputStream(resource.getInputStream());
		ClassReader classReader;
		try {
			classReader = new ClassReader(is);
		} catch (IllegalArgumentException ex) {
			throw new IOException(
					"ASM ClassReader failed to parse class file - "
							+ "probably due to a new Java class file version that isn't supported yet: "
							+ resource, ex);
		} finally {
			is.close();
		}

		AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor();
		classReader.accept(visitor, ClassReader.SKIP_DEBUG);

		Metadata metadata = new Metadata();
		metadata.setClassMetadata(visitor);
		metadata.setAnnotationMetadata(visitor);

		return metadata;
	}
}
