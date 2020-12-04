package com.brownie.videojs;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.StreamRequestHandler;

public class ContentRangeRequestHandler extends StreamRequestHandler {

	/**
	 * Request handler to provide compatibility with Safari content-range requests
	 *  (with out it we will get broken pipe error in Safari).
	 *  ContentRangeRequestHandler connected with VideoJSServiceInitListener.
	 *  
	 *  It based on:
	 *  https://github.com/TatuLund/audiovideo/blob/master/gwtav-addon/src/main/java/org/vaadin/gwtav/AbstractAudioVideo.java#L100
	 *  
	 *  And
	 *  
	 *  https://github.com/TatuLund/audiovideo/blob/master/gwtav-addon/src/main/java/org/vaadin/gwtav/IOUtil.java#L24
	 *  
	 *  with original comments
	 */
	private static final long serialVersionUID = -2414216433642616050L;
	
	public static final int BUFFER_SIZE = 32 * 1024;

	@Override
	public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
			throws IOException {
		
		String agent = request.getHeader("User-Agent");
		String header = request.getHeader("Range");
		
		if (header == null || (agent != null && !agent.contains("Safari") && !agent.contains("AppleWebKit"))) {
			return super.handleRequest(session, request, response); 
		}

//		String resourcePathFromRequest = request.getPathInfo().replace(" ", "%20");
		String splitter = "/";
		String resourcePathFromRequest = Arrays.stream(request.getPathInfo().split(splitter))
				.map(value ->
						URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20"))
				.collect(Collectors.joining(splitter));
		System.out.println("GET " + resourcePathFromRequest);
	    File file = VideoJS.getResourcesRegistrations().get(resourcePathFromRequest).getValue();
	    if (!file.exists()) {
	    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    	return true;
	    }
	    
	    response.setHeader("Accept-Ranges", "bytes");
	    
	    FileInputStream input = new FileInputStream(file);
	    session.access(() -> {
		    long rangeStart;
		    long rangeEnd = -1;
		    
	    	String[] split = header.substring(6).split("-");
	    	rangeStart = Long.parseLong(split[0]);
	    	if (split.length == 2) {
	    		rangeEnd = Long.parseLong(split[1]);
	    	}

		    StreamResource streamResource = 
		    		(StreamResource) VideoJS.getResourcesRegistrations().get(resourcePathFromRequest).getKey().getResource();

		    try {
				writeResponse(response, file, input, streamResource, rangeStart, rangeEnd);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    });

	    return true;
	}

    public static void writeResponse(VaadinResponse response,
									 File file,
									 FileInputStream data,
									 StreamResource streamResource,
									 long rangeStart,
									 long rangeEnd) throws IOException {

        if (data == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        OutputStream out = null;
        try {
            // Sets content type
            response.setContentType(Files.probeContentType(file.toPath()));

            // Sets cache headers
            response.setCacheTime(streamResource.getCacheTime());

            final byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // Calculate range that is going to be served if this was range request
            long bytesToWrite = -1;
            data.skip(rangeStart); // Skip to start offset of the request
            if (rangeStart > 0 || rangeEnd > 0) {
            	response.setStatus(206); // 206 response code needed since this is partial data
            	long contentLength = data.available();
            	if (rangeEnd == -1) rangeEnd = contentLength - 1;
            	response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + contentLength);
            	bytesToWrite = rangeEnd - rangeStart + 1;
				response.setHeader("Content-Length", "" + bytesToWrite);
            }
            out = response.getOutputStream();
            
            long totalWritten = 0;
            while ((bytesToWrite == -1 || totalWritten < bytesToWrite) && (bytesRead = data.read(buffer)) > 0) {
            	// Check if this was last part, hence possibly less
            	if (bytesToWrite != -1) {
            		bytesRead = (int) Math.min(bytesRead, bytesToWrite - totalWritten);
            	}
           		out.write(buffer, 0, bytesRead);

                totalWritten += bytesRead;
                if (totalWritten >= buffer.length) {
                    // Avoid chunked encoding for small resources
                    out.flush();
                }
            }
        } catch (EOFException ignore) {
    		// Browser aborts when it notices that range requests are supported
        	// Swallow e.g. Jetty
        } catch (IOException e) {
        	String name = e.getClass().getName();
        	if (name.equals("org.apache.catalina.connector.ClientAbortException")) {
        		// Browser aborts when it notices that range requests are supported
        		// Swallow e.g. Tomcat
        	} else {
        		throw e;
        	}
        } finally {
        	tryToCloseStream(out);
        	tryToCloseStream(data);
        }
    }
    
    public static void tryToCloseStream(InputStream in) {
        try {
            // try to close input stream (e.g. file handle)
            if (in != null) {
                in.close();
            }
        } catch (IOException e1) {
            // NOP
        }
    }
    
    public static void tryToCloseStream(OutputStream out) {
        try {
            // try to close output stream (e.g. file handle)
            if (out != null) {
                out.close();
            }
        } catch (IOException e1) {
            // NOP
        }
    }
}
