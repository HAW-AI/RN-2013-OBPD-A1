package de.haw_hamburg.common;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggingFormatter extends Formatter {

	@Override
	public String format(LogRecord rec) {
		StringBuilder result=new StringBuilder("");
		result.append(rec.getLoggerName());
		result.append(" [");
		result.append(new Date(rec.getMillis()));
		result.append("]: ");
		result.append(rec.getMessage());
		result.append("\n");
		return result.toString();
	}

}
