/*
 * This plugin is developed for tracking speed, size and shape of settling flocs
 * from a series of 2D motion images
 */

package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.FolderOpener;
import ij.ImageJ;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * A template for processing each pixel of either
 * GRAY8, GRAY16, GRAY32 or COLOR_RGB images.
 *
 * @author Chuan Gu
 */
public class SedTrack implements PlugInFilter {
    
	protected ImagePlus image;
        
	private int width;
	
                     private int height;

	// plugin parameters
	public double value = 100;

	@Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
   		
                                           image = imp;
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}

	@Override
	public void run(ImageProcessor ip) {
		// get width and height
		width = ip.getWidth();
		height = ip.getHeight();
                                           System.out.println("Image Size = "+width+"x"+height);
		process(image);
		//image.updateAndDraw();
	}
                     
	/**
	 * Process an image.
	 * <p>
	 * Please provide this method even if {@link ij.plugin.filter.PlugInFilter} does require it;
	 * the method {@link ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)} can only
	 * handle 2-dimensional data.
	 * </p>
	 * <p>
	 * If your plugin does not change the pixels in-place, make this method return the results and
	 * change the {@link #setup(java.lang.String, ij.ImagePlus)} method to return also the
	 * <i>DOES_NOTHING</i> flag.
	 * </p>
	 *
	 * @param image the image (possible multi-dimensional)
	 */
	public void process(ImagePlus image) {
                                           System.out.println("Processing ImagePlus");
                                           System.out.println("The image stack has "+image.getStackSize()+" slices");
		for (int i = 1; i <= image.getStackSize(); i++)
                                           {
                                                System.out.println("Processing slice"+i);
                                                process(image.getStack().getProcessor(i));
                                            }   
                       }

	// Select processing method depending on image type
	public void process(ImageProcessor ip) {

		int type = image.getType();
                
		if (type == ImagePlus.GRAY8)
			process( (byte[]) ip.getPixels() );
		else if (type == ImagePlus.GRAY16)
			process( (short[]) ip.getPixels() );
		else if (type == ImagePlus.GRAY32)
			process( (float[]) ip.getPixels() );
		else {
			throw new RuntimeException("Image format not supported");
		}
	}

	// processing of GRAY8 images
	public void process(byte[] pixels) {
		for (int y=0; y < height; y++) {
		for (int x=0; x < width;  x++) {
                                                pixels[x + y * width] += (byte)value;
                                                pixels[x + y* width] = 0;
		}
		}
                
                System.out.println("GRAY8");
	}

	// processing of GRAY16 images
	public void process(short[] pixels) {
		for (int y=0; y < height; y++) {
		for (int x=0; x < width;  x++) {
                    // process each pixel of the line
                    // example: add 'number' to each pixel
                    pixels[x + y * width] += (short)value;
		}
		}
                System.out.println("GRAY16");
	}

	// processing of GRAY32 images
	public void process(float[] pixels) {
		for (int y=0; y < height; y++) {
		for (int x=0; x < width;  x++) {
			// process each pixel of the line
			// example: add 'number' to each pixel
			pixels[x + y * width] += (float)value;
		}
		}
                System.out.println("GRAY32");
	}

	// processing of COLOR_RGB images
	public void process(int[] pixels) {
		for (int y=0; y < height; y++) {
                                           for (int x=0; x < width;  x++) {
			// process each pixel of the line
			// example: add 'number' to each pixel
			pixels[x + y * width] += (int)value;
		}
		}
                System.out.println("RGB");
	}

	public void showAbout() {
                        IJ.showMessage("SedTrack","Tracking settling flocs from 2D motion images");
	}

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = SedTrack.class;
		
                                          // start ImageJ
                                          ImageJ imageJ = new ImageJ();

                                           ImagePlus image = FolderOpener.open("C:/Users/exw599/Desktop/sample");
		//ImagePlus image = FolderOpener.open("/Users/chuangu/Desktop/sample");
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}
}
