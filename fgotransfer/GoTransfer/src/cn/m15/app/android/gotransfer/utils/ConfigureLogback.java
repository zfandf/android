package cn.m15.app.android.gotransfer.utils;

import java.io.File;

import org.slf4j.LoggerFactory;

import cn.m15.app.android.gotransfer.GoTransferApplication;

import android.os.Environment;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

public class ConfigureLogback {

	public static void configureLogbackDirectly() {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.reset();

		RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
		rollingFileAppender.setAppend(true);
		rollingFileAppender.setContext(lc);
		
		String filePath = "";
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			filePath = Environment.getExternalStorageDirectory()
					+ File.separator + "GoTransfer" + File.separator;
		} else {
			filePath = GoTransferApplication.getInstance().getApplicationContext().getFilesDir()
					+ File.separator + "GoTransfer" + File.separator;
		}
		rollingFileAppender.setFile(filePath + "GoTransfer.log");

		TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
		rollingPolicy.setFileNamePattern(filePath + "GoTransfer.%d.log");
		rollingPolicy.setMaxHistory(2);
		// parent and context required!
		rollingPolicy.setParent(rollingFileAppender); 
		rollingPolicy.setContext(lc);
		rollingPolicy.start();

		rollingFileAppender.setRollingPolicy(rollingPolicy);

		// text pattern
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
		encoder.setContext(lc);
		encoder.start();

		rollingFileAppender.setEncoder(encoder);
		rollingFileAppender.start();
		
		// add the newly created appenders to the root logger;
		// qualify Logger to disambiguate from org.slf4j.Logger
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.DEBUG);
		root.addAppender(rollingFileAppender);
	}
	
	public static void stop() {
		// assume SLF4J is bound to logback-classic in the current environment
	    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
	    loggerContext.stop();
	}
}
