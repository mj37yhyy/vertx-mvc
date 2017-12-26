/*
 *    Copyright 2009-2012 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.mermaid.vertxmvc.utils.ognl;

import ognl.*;

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Caches OGNL parsed expressions. Have a look at
 * http://code.google.com/p/mybatis/issues/detail?id=342
 *
 */
public class OgnlCache {

	private static final Map<String, Node> expressionCache = new ConcurrentHashMap<String, Node>();

	public static void setValue(String expression, Object root, Object value)
			throws OgnlException {
		Ognl.setValue(parseExpression(expression), root, value);
	}

	public static Object getValue(String expression, Object root)
			throws OgnlException {
		return Ognl.getValue(parseExpression(expression), root);
	}

	private static Object parseExpression(String expression)
			throws OgnlException {
		try {
			Node node = expressionCache.get(expression);
			if (node == null) {
				node = new OgnlParser(new StringReader(expression))
						.topLevelExpression();
				expressionCache.put(expression, node);
			}
			return node;
		} catch (ParseException | TokenMgrError e) {
			throw new ExpressionSyntaxException(expression, e);
		}
	}

}
