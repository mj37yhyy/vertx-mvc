package org.mermaid.vertxmvc.annotation;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.*;

@Target({
		ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

	String[] value() default {};

	/**
	 * 路由表达式
	 */
	String routeWithRegex() default "";

	/**
	 * 路径表达式
	 */
	String pathRegex() default "";

	/**
	 * http方法
	 */
	HttpMethod[] method() default {};

	String[] consumes() default {};

	String[] produces() default {};

}
