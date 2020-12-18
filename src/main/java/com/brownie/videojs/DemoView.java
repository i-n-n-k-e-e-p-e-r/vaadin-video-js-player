package com.brownie.videojs;

import java.io.File;
import java.nio.file.Paths;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class DemoView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8264657013295724664L;

	public DemoView() {		
		setAlignItems(Alignment.CENTER);
		setPadding(true);
		setSpacing(true);
		
		File mediaFile = Paths.get(".", "src", "main", "resources", "пример (super 1).mov").toFile();
		File posterImage = Paths.get(".", "src", "main", "resources", "poster.jpg").toFile();

        final VideoJS video = new VideoJS(UI.getCurrent().getSession(), mediaFile, posterImage);
        video.setHeight("50%");
		
        HorizontalLayout hl = new HorizontalLayout();
		Button play = new Button("play");
		play.addClickListener(event -> video.play());
		Button pause = new Button("pause");
		pause.addClickListener(event -> video.pause());
		
		hl.add(play);
		hl.add(pause);
		hl.setAlignItems(Alignment.CENTER);
		
		add(hl);
		add(video);
    }
}
