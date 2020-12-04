package com.brownie.videojs;

import java.io.File;
import java.nio.file.Paths;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("/test")
public class TestView extends Div {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6897951292704726063L;
	
	private VideoJS video;
	
	public TestView() {
		File mediaFile = Paths.get(".", "src", "main", "resources", "пример (super 1).mov").toFile();
		File posterImage = Paths.get(".", "src", "main", "resources", "poster.jpg").toFile();

        video = new VideoJS(null, mediaFile, posterImage);
        video.setHeight("50%");
        
        add(video);
    }

}
