package org.mermaid.vertxmvc.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateRequestConverter implements Converter<Date> {

	@Override
	public Date convert(String text) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
	}
}
