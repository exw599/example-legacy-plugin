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
import java.lang.Math;

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
threashold(ip);
Sobel_operator(ip);
}

/**
* Process an image
* @param image
*/
public void process_imageplus(ImagePlus image) {
System.out.println("Processing ImagePlus");
System.out.println("The image stack has "+image.getStackSize()+" slices");

if (image.getType() != ImagePlus.GRAY8) {
ImageConverter transform = new ImageConverter(image);
transform.convertToGray8();
}

for (int i = 1; i <= image.getStackSize(); i++){
System.out.println("Processing slice"+i);
threashold(image.getStack().getProcessor(i));
}       
}

// Select processing method depending on image type
public void threashold(ImageProcessor ip) {

byte[] pixels = (byte[]) ip.getPixels();
int max = 0;
int min = 0;

for (int y=0; y < height; y++) {
for (int x=0; x < width;  x++) {
if (x==0 && y == 0){
max = pixels[x+y*width]&0xff;
min = pixels[x+y*width]&0xff;
continue;
}

if ( (pixels[x+y*width]&0xff) > max) max = pixels[x+y*width]&0xff;
if ( (pixels[x+y*width]&0xff) < min) min = pixels[x+y*width]&0xff;
}
}

for (int y=0; y < height; y++) {
for (int x=0; x < width;  x++) {
if ( (pixels[x+y*width]&0xff) > min + (max-min) * 0.9 ) pixels[x+y*width] = (byte) 255;    
}
}
}

public void Sobel_operator(ImageProcessor ip) {

ImagePlus grad = IJ.createImage("Sobel_operator", width, height, 1, 32);
ImageProcessor grad_ip = grad.getProcessor();

float[] grad_pixels = (float[]) grad_ip.getPixels();
byte[] pixels = (byte[]) ip.getPixels();

int grad_x;
int grad_y;

for (int y=1; y < height-1; y++) {
for (int x=1; x < width -1; x++) {

grad_x=(2*(pixels[x-1+    y*width]&0xFF)
         +(pixels[x-1+(y-1)*width]&0xFF)
         +(pixels[x-1+(y+1)*width]&0xFF)
       -2*(pixels[x+1+    y*width]&0xFF) 
         -(pixels[x+1+(y-1)*width]&0xFF)
         -(pixels[x+1+(y+1)*width]&0xFF));

grad_y=(2*(pixels[x  +(y-1)*width]&0xFF)
         +(pixels[x-1+(y-1)*width]&0xFF)
         +(pixels[x+1+(y-1)*width]&0xFF)
       -2*(pixels[x  +(y+1)*width]&0xFF)
         -(pixels[x-1+(y+1)*width]&0xFF)
         -(pixels[x+1+(y+1)*width]&0xFF));

grad_pixels[x+y*width] = (float) Math.abs(grad_x*grad_x + grad_y*grad_y);

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

ImageJ imageJ = new ImageJ();

//ImagePlus image = FolderOpener.open("C:/Users/exw599/Desktop/sample");
ImagePlus image = FolderOpener.open("/Users/chuangu/Desktop/sample");
image.show();

// run the plugin
IJ.runPlugIn(clazz.getName(), "");
}
}
