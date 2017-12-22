package com.ifeng.liulu.hotboostLog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StartLogProcess {
	protected static final Log LOG = LogFactory.getLog(StartLogProcess.class);

	/*
	 * hotboost��־�����������
	 * 
	 * ע�����ز���ʱע���޸�sys.properties�е�log_online_pathΪ����·��
	 */
	public static void main(String[] args) {
		LOG.info("LogProcessor Started");
		LogProcessor.initLogDate(args);
		LogProcessor.StartProcessLog();
	}
}
