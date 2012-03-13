package org.regenstrief.linkage.util;

public class Token {
	String token_name;
	Integer frequency;
	Double probability;
	Double log_value;
	
	public String getToken_name() {
		return token_name;
	}
	public void setToken_name(String token_name) {
		this.token_name = token_name;
	}
	public Integer getFrequency() {
		return frequency;
	}
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	public Double getProbability() {
		return probability;
	}
	public void setProbability(Double probability) {
		this.probability = probability;
	}
	public Double getLog_value() {
		return log_value;
	}
	public void setLog_value(Double log_value) {
		this.log_value = log_value;
	}


}
