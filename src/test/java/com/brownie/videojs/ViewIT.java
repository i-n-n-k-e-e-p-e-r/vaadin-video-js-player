package com.brownie.videojs;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.testbench.TestBenchElement;

public class ViewIT extends AbstractViewTest {

    @Test
    public void componentWorks() {
        final TestBenchElement videojs = $("video.js").first();
        // Check that video.js contains at least one other element, which means that
        // is has been upgraded to a custom element and not just rendered as an empty
        // tag
        Assert.assertTrue(
                videojs.$(TestBenchElement.class).all().size() > 0);
    }
}
