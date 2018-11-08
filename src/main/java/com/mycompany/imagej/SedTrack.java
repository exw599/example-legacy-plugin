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
import ij.process.ImageConverter;

/**
 * @author Chuan Gu
 */
public class SedTrack implements PlugInFilter {
    
	protected ImagePlus image;
        
	private int width;
	
        private int height;

	public double value = 100;

	@Override
	public int setup(String arg, ImagePlus imp) {
            
            image = imp;
            
            return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}

	@Override
	public void run(ImageProcessor ip) {
            // get width and height
            width = ip.getWidth();
            height = ip.getHeight();
            System.out.println("Image Size = "+width+"x"+height);
            //process_imageplus(image);
            //image.updateAndDraw();
            Sobel_operator(ip);
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
	public void process_imageplus(ImagePlus image) {
            System.out.println("Processing ImagePlus");
            System.out.println("The image stack has "+image.getStackSize()+" slices");
            
            if (image.getType() != ImagePlus.GRAY16) {
                ImageConverter transform = new ImageConverter(image);
                transform.convertToGray16();
            }
            
            for (int i = 1; i <= image.getStackSize(); i++){
                System.out.println("Processing slice"+i);
                process_ip(image.getStack().getProcessor(i));
            }       
        }

	// Select processing method depending on image type
	public void process_ip(ImageProcessor ip) {
            
            short[] pixels = (short[]) ip.getPixels();
            short max = 0;
            short min = 0;
            
            for (int y=0; y < height; y++) {
            for (int x=0; x < width;  x++) {
                if (x==0 && y == 0){
                    max = pixels[x+y*width];
                    min = pixels[x+y*width];
                    continue;
                }
                
                if (pixels[x+y*width] > max) max = pixels[x+y*width];
                if (pixels[x+y*width] < min) min = pixels[x+y*width];
            }
            }
            
            for (int y=0; y < height; y++) {
            for (int x=0; x < width;  x++) {
                if (pixels[x+y*width] > min + (max-min) * 0.9 ) pixels[x+y*width] = 255;    
            }
            }
        }
        
        public void Sobel_operator(ImageProcessor ip) {
            
            ImagePlus grad = IJ.createImage("Sobel_operator", width, height, 1, 8);
            ImageProcessor grad_ip = grad.getProcessor();
            byte[] grad_pixels = (byte[]) grad_ip.getPixels();
            byte[] pixels = (byte[]) ip.getPixels();
            
            byte gradx, grady;
            
            for (int y=1; y < height-1; y++) {
            for (int x=1; x < width -1; x++) {
                gradx = (byte) (2*pixels[x-1+    y*width] 
                                 + pixels[x-1+(y-1)*width]
                                 + pixels[x-1+(y+1)*width]
                                -2*pixels[x+1+    y*width] 
                                 - pixels[x+1+(y-1)*width]
                                 - pixels[x+1+(y+1)*width]);
                grad_pixels[x+y*width] = gradx;
            }
            }
            
            grad.show();
            
        }
        
        
        
        
        
        
        
	/**
	 * Main method for debugging.
	 */
	public static void main(String[] args) {
            // set the plugins.dir property to make the plugin appear in the Plugins menu
            Class<?> clazz = SedTrack.class;
		
            // start ImageJ
            ImageJ imageJ = new ImageJ();

            //ImagePlus image = FolderOpener.open("C:/Users/exw599/Desktop/sample");
            ImagePlus image = FolderOpener.open("/Users/chuangu/Desktop/sample");
            image.show();

            // run the plugin
            IJ.runPlugIn(clazz.getName(), "");
	}
}
