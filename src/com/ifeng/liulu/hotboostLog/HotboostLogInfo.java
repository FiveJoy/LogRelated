package com.ifeng.liulu.hotboostLog;

import java.util.Date;

public class HotboostLogInfo {
	public String docid;
	public String simid;
	public Double globalScore;
	public Boolean isBills;
	public String sourceEvalLevel;
	public Date time;
	public Integer updateTurns;

	public String getDocid() {
		return docid;
	}

	public void setDocid(String docid) {
		this.docid = docid;
	}

	public String getSimid() {
		return simid;
	}

	public void setSimid(String simid) {
		this.simid = simid;
	}

	public Double getGlobalScore() {
		return globalScore;
	}

	public void setGlobalScore(Double globalScore) {
		this.globalScore = globalScore;
	}

	public Boolean getIsBills() {
		return isBills;
	}

	public void setIsBills(Boolean isBills) {
		this.isBills = isBills;
	}

	public String getSourceEvalLevel() {
		return sourceEvalLevel;
	}

	public void setSourceEvalLevel(String sourceEvalLevel) {
		this.sourceEvalLevel = sourceEvalLevel;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Integer getUpdateTurns() {
		return updateTurns;
	}

	public void setUpdateTurns(Integer updateTurns) {
		this.updateTurns = updateTurns;
	}

	public Integer getUnUpdateTurns() {
		return unUpdateTurns;
	}

	public void setUnUpdateTurns(Integer unUpdateTurns) {
		this.unUpdateTurns = unUpdateTurns;
	}

	public Double getPriorScore() {
		return priorScore;
	}

	public void setPriorScore(Double priorScore) {
		this.priorScore = priorScore;
	}

	public String getHotParameter() {
		return hotParameter;
	}

	public void setHotParameter(String hotParameter) {
		this.hotParameter = hotParameter;
	}

	public String getVerticalScore() {
		return verticalScore;
	}

	public void setVerticalScore(String verticalScore) {
		this.verticalScore = verticalScore;
	}

	public Double getQualityEvalLevel() {
		return qualityEvalLevel;
	}

	public void setQualityEvalLevel(Double qualityEvalLevel) {
		this.qualityEvalLevel = qualityEvalLevel;
	}

	public Integer unUpdateTurns;
	public Double priorScore;
	public String hotParameter;
	public String verticalScore;
	public Double qualityEvalLevel;

}
