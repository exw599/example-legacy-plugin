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

import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

/**
* @author Chuan Gu, SEMS, Queen Mary University of London
*/
public class SedTrack implements PlugInFilter {

protected ImagePlus image;

private int width;

private int height;

private List<floc> floc_list_storage;

private List<floc> floc_list_current;

@Override
public int setup(String arg, ImagePlus imp) {
image = imp;
floc_list_storage = new ArrayList<>();
floc_list_current = new ArrayList<>();
return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
}

@Override
public void run(ImageProcessor ip) {

// get width and height
width = ip.getWidth();
height = ip.getHeight();

System.out.println("The image stack has " + image.getStackSize() + " slices");
System.out.println("Image size = " + width + "x" + height);

//process_imageplus(image);
Threashold(ip);
Sorting(ip);
//Sobel_operator(ip);
}




/**
* Process an ImagePlus object (can be a single image or a stack of images)
* @param image
*/
public void Process_imageplus(ImagePlus image) {

//Convert the ImagePlus to 8-bit if it is not
if (image.getType() != ImagePlus.GRAY8) {
ImageConverter transform = new ImageConverter(image);
transform.convertToGray8();
}

//Processing the image stack one slice by one slice
for (int i = 1; i <= image.getStackSize(); i++) {
System.out.println("Processing slice" + i);
Threashold(image.getStack().getProcessor(i));
}

} // End of Process_imageplus



/*
*Applying the threasholding operation on the current ImageProcessor
*/
public void Threashold(ImageProcessor ip) {

byte[] pixels = (byte[]) ip.getPixels();
int max = 0;
int min = 0;

for (int y = 0; y < height; y++) {
for (int x = 0; x < width; x++) {
if (x == 0 && y == 0) {
max = pixels[x + y * width] & 0xff;
min = pixels[x + y * width] & 0xff;
continue;
}

if ((pixels[x + y * width] & 0xff) > max) {
max = pixels[x + y * width] & 0xff;
}
if ((pixels[x + y * width] & 0xff) < min) {
min = pixels[x + y * width] & 0xff;
}
}
}

for (int y = 0; y < height; y++) {
for (int x = 0; x < width; x++) {
if ((pixels[x + y * width] & 0xff) > min + (max - min) * 0.9) {
pixels[x + y * width] = (byte) 255;
}
}
}

}


/*
*Applying the Sobel operation to calculate the magnitude of the first spatial derivative of the 2D image
*The result is stored in ImagePlus "grad"
*/
public void Sobel_operator(ImageProcessor ip) {

ImagePlus grad = IJ.createImage("Sobel_operator", width, height, 1, 32);
ImageProcessor grad_ip = grad.getProcessor();

float[] grad_pixels = (float[]) grad_ip.getPixels();
byte[] pixels = (byte[]) ip.getPixels();

int grad_x;
int grad_y;

for (int y = 1; y < height - 1; y++) {
for (int x = 1; x < width - 1; x++) {

grad_x = (2 * (pixels[x - 1 + y * width] & 0xFF)
+ (pixels[x - 1 + (y - 1) * width] & 0xFF)
+ (pixels[x - 1 + (y + 1) * width] & 0xFF)
- 2 * (pixels[x + 1 + y * width] & 0xFF)
- (pixels[x + 1 + (y - 1) * width] & 0xFF)
- (pixels[x + 1 + (y + 1) * width] & 0xFF));

grad_y = (2 * (pixels[x + (y - 1) * width] & 0xFF)
+ (pixels[x - 1 + (y - 1) * width] & 0xFF)
+ (pixels[x + 1 + (y - 1) * width] & 0xFF)
- 2 * (pixels[x + (y + 1) * width] & 0xFF)
- (pixels[x - 1 + (y + 1) * width] & 0xFF)
- (pixels[x + 1 + (y + 1) * width] & 0xFF));

grad_pixels[x + y * width] = (float) Math.abs(grad_x * grad_x + grad_y * grad_y);

}
}

grad.show();

}

/*
*Identify all the flocs within the currrent ImageProcessor and record them into floc_list_current
*/
public void Sorting(ImageProcessor ip) {

byte[] pixels = (byte[]) ip.getPixelsCopy();

List<Point2D.Double> front = new ArrayList<>();
floc floc_new;
Point2D.Double pixel_scan;
ListIterator<Point2D.Double> it;

for (int y = 0; y < height; y++) {
for (int x = 0; x < width; x++) {

if ((pixels[x + y*width] & 0xff) < 255) {

front.clear();
floc_new = new floc();
pixel_scan = new Point2D.Double(x, y);

floc_new.co.add(pixel_scan);
front.add(pixel_scan);
pixels[x + y*width] = (byte) 255;


it = front.listIterator();
while (it.hasNext()) {

pixel_scan = it.next();

for (int yy = -1; yy < 2; yy++) {
for (int xx = -1; xx < 2; xx++) {

if (pixel_scan.getX()+xx<0 || pixel_scan.getX()+xx>width ) continue;
if (pixel_scan.getY()+yy<0 || pixel_scan.getY()+yy>height) continue;


if ((pixels[(int) pixel_scan.getX() + xx + ((int) pixel_scan.getY() + yy) * width] & 0xff) < 255) {
it.add(new Point2D.Double((int) pixel_scan.getX() + xx, (int) pixel_scan.getY() + yy));
it.previous();
floc_new.co.add(new Point2D.Double((int) pixel_scan.getX() + xx, (int) pixel_scan.getY() + yy));
pixels[(int) pixel_scan.getX() + xx + ((int) pixel_scan.getY() + yy)*width] = (byte) 255;

}
}
}
} //while it.hasNext

//Calculating the mass, centre of mass, moment of inertia and radius of gyration of the floc_new
for (Point2D.Double pts : floc_new.co) {
floc_new.mass += (255.0 - ip.getPixel( (int) pts.getX(), (int) pts.getY()))/255.0;
floc_new.cm.setLocation
        (floc_new.cm.getX() + pts.getX()*(255.0 - ip.getPixel( (int) pts.getX(), (int) pts.getY()))/255.0,
         floc_new.cm.getY() + pts.getY()*(255.0 - ip.getPixel( (int) pts.getX(), (int) pts.getY()))/255.0);
}
floc_new.cm.setLocation(floc_new.cm.getX()/floc_new.mass, floc_new.cm.getY()/floc_new.mass);

for (Point2D.Double pts : floc_new.co) {
floc_new.m2 += pts.distanceSq(floc_new.cm) * (255.0 - ip.getPixel( (int) pts.getX(), (int) pts.getY()))/255.0;    
}

floc_new.rg = floc_new.m2/floc_new.mass;

floc_list_current.add(floc_new);

}// if pixel value < 255

}
}

}// end of the class Sorting







/**
* Main method for debugging.
* @param args
*/
public static void main(String[] args) {

Class<?> clazz = SedTrack.class;

ImageJ imageJ = new ImageJ();

ImagePlus image = FolderOpener.open("C:/Users/exw599/Desktop/sample");
//ImagePlus image = FolderOpener.open("/Users/chuangu/Desktop/sample");
image.show();

// run the plugin
IJ.runPlugIn(clazz.getName(), "");
}
}//End of the class SedTrack


/*
* Support class
* Record each identified floc within the image
*/
class floc {

public double mass;
public Point2D.Double cm;
public double m2;
public double rg;
public List<Point2D.Double> co;

public floc() {
mass = 0.0;
cm = new Point2D.Double(0.0, 0.0);
m2 = 0.0;
rg = 0.0;
co = new ArrayList<>();
}

}
