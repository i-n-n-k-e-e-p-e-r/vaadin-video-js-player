package com.brownie.videojs;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class VideoJSServiceInitListener implements VaadinServiceInitListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7571737454702938957L;

	@Override
	public void serviceInit(ServiceInitEvent event) {
	    event.addRequestHandler(new ContentRangeRequestHandler());
	}
}
