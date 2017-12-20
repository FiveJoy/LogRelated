package com.ifeng.liulu.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StartLogProcess {
	protected static final Log LOG = LogFactory.getLog(StartLogProcess.class);

	public static void main(String[] args) {
		LOG.info("LogProcessor Started");
		LogProcessor.initLogDate(args);
		// LogProcessor.initDataDestination();
		LogProcessor.StartProcessLog();
	}
}
