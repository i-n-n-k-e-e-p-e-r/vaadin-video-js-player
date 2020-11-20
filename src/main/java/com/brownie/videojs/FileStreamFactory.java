package com.brownie.videojs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileStreamFactory implements com.vaadin.flow.server.InputStreamFactory {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8697163605403815521L;

	private File file;
	
	public FileStreamFactory(File file) {
		this.file = file;
	}
	
	@Override
	public InputStream createInputStream() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
