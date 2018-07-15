package com.sgchanges.engine.sequence;

public interface SequenceProcessor {

	public String getLastProcessedSeq() throws Exception;
	
	public void saveProcessedSeq(String seq) throws Exception;
	
}
