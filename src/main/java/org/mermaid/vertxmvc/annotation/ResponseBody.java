package org.mermaid.vertxmvc.annotation;

import org.mermaid.vertxmvc.converters.ResponseConverter;
import org.mermaid.vertxmvc.converters.JsonResponseConverter;

import java.lang.annotation.*;

@Target({
		ElementType.TYPE, ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {

	// 格式化的方式，默认json
	Class<? extends ResponseConverter> converterType() default JsonResponseConverter.class;
}
