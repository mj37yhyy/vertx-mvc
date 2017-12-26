package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.annotation.RequestBody;
import org.mermaid.vertxmvc.utils.ognl.ExpressionEvaluator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Test {

	@org.junit.Test
	public void test() throws Exception {
		for (Method method : TestController.class.getMethods()) {
			for (Annotation[] annotations : method.getParameterAnnotations()) {
				for (Annotation annotation : annotations) {
					System.out.println(annotation);
					System.out.println(annotation.annotationType());
					System.out.println(
							annotation.annotationType().isAnnotation());
					System.out.println(annotation.annotationType()
							.isAssignableFrom(RequestBody.class));
				}
			}
		}

	}

	@org.junit.Test
	public void test2() throws Exception {
		ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();
		System.out.println(expressionEvaluator.evaluateBoolean(
				"a==2 && b==3", new HashMap() {
					{
						put("a", 2);
						put("b", 3);
					}
				}));

	}

	@org.junit.Test
	public void test3() throws Exception {
		System.out.println(
				new ExpressionEvaluator().evaluateBoolean(
						"Action.equals(\"Exec\")", new HashMap<String, String>() {
							{
								put("Action", "Exec");
							}
						}));


	}

}
