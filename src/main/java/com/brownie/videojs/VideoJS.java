package com.brownie.videojs;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import java.io.File;
import java.net.URI;
import java.util.*;


@Tag("video")
@NpmPackage(value = "video.js", version = "^7.8.2")
@JsModule("video.js/dist/video.js")
@CssImport("video.js/dist/video-js.css")

/*
 If you wish to include your own JS modules in the add-on jar, add the module
 files to './src/main/resources/META-INF/resources/frontend' and insert an
 annotation @JsModule("./my-module.js") here.
*/
public class VideoJS extends Component implements HasSize {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8431497077091431929L;
	
	private static final PropertyDescriptor<String, String> videoTagIdProperty = 
			PropertyDescriptors.propertyWithDefault("id", "");
	
	private static final PropertyDescriptor<String, String> classProperty = 
			PropertyDescriptors.propertyWithDefault("class", "video-js");

	private static final PropertyDescriptor<String, String> srcProperty = 
			PropertyDescriptors.propertyWithDefault("src", "");
	
	private static final PropertyDescriptor<String, String> posterProperty = 
			PropertyDescriptors.propertyWithDefault("poster", "");
	
	/**
	 * Сouldn't find a way to get the stream from file in vaadin request handler
	 * so this container is for linking files with stream resources
	 */	
	private StreamResource mediaResource;
	private static final Map<String, AbstractMap.SimpleImmutableEntry<StreamRegistration, File>> resourcesRegistrations = 
			Collections.synchronizedMap(new HashMap<>());

	public VideoJS(VaadinSession session, File mediaFile, File posterImage) {
		if (mediaFile == null ) {
			throw new IllegalArgumentException("Media file can't be NULL.");
		}
		
		StreamResource mediaResource = new StreamResource(
				mediaFile.getName(),
				new FileStreamFactory(mediaFile));
		
		StreamResource posterResource = null;
		if (posterImage != null) {
			posterResource = new StreamResource(
					posterImage.getName(), 
					new FileStreamFactory(posterImage));
		}
		
		setVideoTagId("video-js-" + this.hashCode() + "-" + System.nanoTime());
		setVideoTagClass("video-js");

		setMediaResource(mediaResource);

		final StreamRegistration mediaRegistration = registerResource(session, 
				mediaFile, 
				mediaResource);
		
		final StreamRegistration posterRegistration = registerResource(session, 
				posterImage, 
				posterResource);

		setSourceURI(mediaRegistration.getResourceUri());
		if (posterResource != null) {
			this.setPosterURI(posterRegistration.getResourceUri());
		}

		this.addAttachListener(event -> enableControls(true));
		
		this.addDetachListener(event -> {
			unregisterResource(mediaRegistration);
			unregisterResource(posterRegistration);
		});
    }
	
	public StreamRegistration registerResource(VaadinSession session, File file, StreamResource streamResource) {
		if (file == null || !file.exists() || streamResource == null) return null;
		
		StreamRegistration registration = session.getResourceRegistry().registerResource(streamResource);

		getResourcesRegistrations().put("/" + registration.getResourceUri().toString().replace(" ", "%20"),
				new AbstractMap.SimpleImmutableEntry<>(registration, file));
		
		return registration;
	}
	
	public AbstractMap.SimpleImmutableEntry<StreamRegistration, File> unregisterResource(StreamRegistration registration) {
		if (registration == null) return null;
		
		registration.unregister();
		
		return getResourcesRegistrations().remove(registration.getResourceUri().toString());
	}
	
	public void unregisterAllResources(StreamRegistration registration) {
		if (registration == null) return;
		
		getResourcesRegistrations().forEach((key, value) -> value.getKey().unregister());
		getResourcesRegistrations().clear();
	}
	
	public void pause() {
		try {
			var ui = this.getUI().isPresent() ? this.getUI().get() : null;
			if (ui == null) return;
			ui.getPage().executeJs(("document.getElementById(\"" + getVideoTagId() + "\").pause();"));
		} catch (NoSuchElementException ex) {
			ex.printStackTrace();
		}
	}
	
	public void play() {
		try {
			var ui = this.getUI().isPresent() ? this.getUI().get() : null;
			if (ui == null) return;
			ui.getPage().executeJs(("document.getElementById(\"" + getVideoTagId() + "\").play();"));
		} catch (NoSuchElementException ex) {
			ex.printStackTrace();
		}
	}
	
	public String getPosterURI() {
		return posterProperty.get(this);
	}
	
	public VideoJS setPosterURI(URI path) {
		posterProperty.set(this, path.toString());
		return this;
	}
	
	public String getSourceURI() {
		return srcProperty.get(this);
	}
	
	public VideoJS setSourceURI(URI path) {
		srcProperty.set(this, path.toString());
		return this;
	}
	
	public VideoJS enableControls(boolean value) {
		try {
			var ui = this.getUI().isPresent() ? this.getUI().get() : null;
			if (ui == null) return this;
			ui.getPage().executeJs(("document.getElementById(\"" + getVideoTagId() + "\").controls = " + value + ";"));
		} catch (NoSuchElementException ex) {
			ex.printStackTrace();
		}
		
		return this;
	}
	
	public VideoJS setPreload(boolean value) {
		try {
			var ui = this.getUI().isPresent() ? this.getUI().get() : null;
			if (ui == null) return this;
			ui.getPage().executeJs(("document.getElementById(\"" + getVideoTagId() + "\").preload = " + value + ";"));
		} catch (NoSuchElementException ex) {
			ex.printStackTrace();
		}
		
		return this;
	}
	
	public String getVideoTagClass() {
		return classProperty.get(this);
	}
	
	public VideoJS setVideoTagClass(String value) {
		classProperty.set(this, value);
		return this;
	}
	
	public String getVideoTagId() {
		return videoTagIdProperty.get(this);
	}
	
	public VideoJS setVideoTagId(String value) {
		videoTagIdProperty.set(this, value);
		return this;
	}
	
	public StreamResource getMediaResource() {
		return mediaResource;
	}

	public void setMediaResource(StreamResource mediaResource) {
		this.mediaResource = mediaResource;
	}

	public static Map<String, AbstractMap.SimpleImmutableEntry<StreamRegistration, File>> getResourcesRegistrations() {
		return resourcesRegistrations;
	}
}
