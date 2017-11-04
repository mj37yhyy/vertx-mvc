package org.mermaid.vertxmvc.annotation;

import java.lang.annotation.*;

import io.vertx.core.http.HttpMethod;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

	/**
	 * Assign a name to this mapping.
	 * <p>
	 * <b>Supported at the type level as well as at the method level!</b> When
	 * used on both levels, a combined name is derived by concatenation with "#"
	 * as separator.
	 * 
	 * @see org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
	 * @see org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy
	 */
	String name() default "";

	/**
	 * The primary mapping expressed by this annotation.
	 * <p>
	 * In a Servlet environment: the path mapping URIs (e.g. "/myPath.do").
	 * Ant-style path patterns are also supported (e.g. "/myPath/*.do"). At the
	 * method level, relative paths (e.g. "edit.do") are supported within the
	 * primary mapping expressed at the type level. Path mapping URIs may
	 * contain placeholders (e.g. "/${connect}")
	 * <p>
	 * In a Portlet environment: the mapped portlet modes (i.e. "EDIT", "VIEW",
	 * "HELP" or any custom modes).
	 * <p>
	 * <b>Supported at the type level as well as at the method level!</b> When
	 * used at the type level, all method-level mappings inherit this primary
	 * mapping, narrowing it for a specific handler method.
	 * 
	 * @see org.springframework.web.bind.annotation.ValueConstants#DEFAULT_NONE
	 */
	String[] value() default {};

	/**
	 * 路由表达式
	 */
	String routeWithRegex() default "";

	/**
	 * 路径表达式
	 */
	String pathRegex() default "";


	HttpMethod[] method() default {};

}
