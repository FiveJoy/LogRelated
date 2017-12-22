package com.ifeng.liulu.hotboostLog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StartLogProcess {
	protected static final Log LOG = LogFactory.getLog(StartLogProcess.class);

	/*
	 * hotboost日志解析程序入口
	 * 
	 * 注：本地测试时注意修改sys.properties中的log_online_path为本地路径
	 */
	public static void main(String[] args) {
		LOG.info("LogProcessor Started");
		LogProcessor.initLogDate(args);
		LogProcessor.StartProcessLog();
	}
}
