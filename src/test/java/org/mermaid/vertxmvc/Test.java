package org.mermaid.vertxmvc;

import ognl.DefaultTypeConverter;
import ognl.Ognl;
import org.mermaid.vertxmvc.annotation.RequestBody;
import org.mermaid.vertxmvc.utils.ognl.ExpressionEvaluator;
import org.mermaid.vertxmvc.utils.ognl.OgnlCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
						"Action.equals(\"Exec\")",
						new HashMap<String, String>() {
							{
								put("Action", "Exec");
							}
						}));

	}

	@org.junit.Test
	public void test4() throws Exception {
		Foo foo = new Foo();
		Map context = Ognl.createDefaultContext( this );

		/* Create an anonymous inner class to handle special conversion */
		Ognl.setTypeConverter( context, new DefaultTypeConverter() {

			public Object convertValue(Map context, Object value, Class toType)
			{

				if (toType == Date.class) {
					String text = (String) value;
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					Date date = null;
					try {
						date = format.parse(text);
					} catch (ParseException e) {
						format = new SimpleDateFormat("yyyy-MM-dd");
						try {
							date = format.parse(text);
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
					}
					return date;
				} else
					return super.convertValue(context, value, toType);
			}
		});
		OgnlCache.setValue("date", context,
				foo,
				"2018-01-02");
		System.out.println(foo);

	}


	class Foo {
		Date date = null;

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		@Override
		public String toString() {
			return "Foo{" +
					"date=" + date +
					'}';
		}
	}

}
