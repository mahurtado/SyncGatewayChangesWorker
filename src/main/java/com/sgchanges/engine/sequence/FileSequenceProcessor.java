package com.sgchanges.engine.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;

import com.sgchanges.config.ChangesWorkerConfig;

public class FileSequenceProcessor implements SequenceProcessor {
	
	private static final Logger log = Logger.getLogger( FileSequenceProcessor.class.getName() );
	
	private String seqFileName;
	private PrintWriter output;
	
	public FileSequenceProcessor() {
		seqFileName = ChangesWorkerConfig.getProperty("FileSequenceProcessor.seqFile");
	}
	
	@Override
	public String getLastProcessedSeq() throws Exception {
		BufferedReader seqFile = null;
		try {
			seqFile = new BufferedReader(new FileReader(seqFileName));
			String lastLine = "";
			String sCurrentLine = "";
			while ((sCurrentLine = seqFile.readLine()) != null) {
			    lastLine = sCurrentLine;
			}
			log.info("Last processed seq: " + lastLine);
			return lastLine;
		} catch (FileNotFoundException e) {
			// File not exist. Will be created on first seq
			return null;
		}
		finally {
			if(seqFile != null)
				try {
					seqFile.close();
				} catch (Exception e) {}
		}
	}

	@Override
	public void saveProcessedSeq(String seq) throws Exception {
		if(output == null) {
			File fSeq = new File(seqFileName);
			output = new PrintWriter(new FileWriter(fSeq, true), true);
			log.info("Writing sequences to file: " + fSeq.getAbsolutePath());
		}
		output.println(seq);
	}

}
