package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


public class Test {
	

	@org.junit.Test
	public  void test() throws Exception {
		for (Method method : TestController.class.getMethods()) {
			for (Annotation[] annotations : method.getParameterAnnotations()) {
				for (Annotation annotation : annotations) {
					System.out.println(annotation);
					System.out.println(annotation.annotationType());
					System.out.println(annotation.annotationType().isAnnotation());
					System.out.println(annotation.annotationType().isAssignableFrom(RequestBody.class));
				}
			}
		}

	}

}
