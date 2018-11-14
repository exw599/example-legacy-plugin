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
 * @author Chuan Gu
 */
public class SedTrack implements PlugInFilter {

    protected ImagePlus image;

    private int width;

    private int height;

    private List<floc> floc_list;

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
        System.out.println("Image Size = " + width + "x" + height);
//process_imageplus(image);
        Threashold(ip);
        Sorting(ip);
//Sobel_operator(ip);
    }

    /**
     * Process an image
     *
     * @param image
     */
    public void Process_imageplus(ImagePlus image) {
        System.out.println("Processing ImagePlus");
        System.out.println("The image stack has " + image.getStackSize() + " slices");

        if (image.getType() != ImagePlus.GRAY8) {
            ImageConverter transform = new ImageConverter(image);
            transform.convertToGray8();
        }

        for (int i = 1; i <= image.getStackSize(); i++) {
            System.out.println("Processing slice" + i);
            Threashold(image.getStack().getProcessor(i));
        }
    }

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
                if ((pixels[x + y * width] & 0xff) > min + (max - min) * 0.95) {
                    pixels[x + y * width] = (byte) 255;
                }
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

    public void Sorting(ImageProcessor ip) {

        byte[] pixels = (byte[]) ip.getPixels();

        List<Point2D.Double> front = new ArrayList<>();
        floc floc_new;
        Point2D.Double pixel_scan;
        ListIterator<Point2D.Double> it;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if ((pixels[x + y * width] & 0xff) < 255) {
                    front.clear();
                    floc_new = new floc();
                    pixel_scan = new Point2D.Double(x, y);

                    floc_new.co.add(pixel_scan);
                    front.add(pixel_scan);

                    it = front.listIterator();

                    while (it.hasNext()) {

                        pixel_scan = it.next();

                        for (int yy = -1; yy < 2; yy++) {
                            for (int xx = -1; xx < 2; xx++) {

                                if ((pixels[(int) pixel_scan.getX() + xx + ((int) pixel_scan.getY() + yy) * width] & 0xff) < 255) {
                                    it.add(new Point2D.Double((int) pixel_scan.getX() + xx, (int) pixel_scan.getY() + yy));
                                    floc_new.co.add(new Point2D.Double((int) pixel_scan.getX() + xx, (int) pixel_scan.getY() + yy));
                                    pixels[(int) pixel_scan.getX() + xx + ((int) pixel_scan.getY() + yy) * width] = (byte) 255;
                                }
                            }
                        }
                    } //while it.hasNext

                    floc_list.add(floc_new);

                }// if pixel value < 255

            }
        }

    }// end of the class Sorting

    /**
     * Main method for debugging.
     */
    public static void main(String[] args) {

        Class<?> clazz = SedTrack.class;

        ImageJ imageJ = new ImageJ();

//ImagePlus image = FolderOpener.open("C:/Users/exw599/Desktop/sample");
        ImagePlus image = FolderOpener.open("/Users/chuangu/Desktop/sample");
        image.show();

// run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}//End of the class SedTrack

class floc {

    public double mass;
    public double m2;
    public double r_g;
    public List<Point2D.Double> co;

    public floc() {
        mass = 0.0;
        m2 = 0.0;
        r_g = 0.0;
        co = new ArrayList<>();
    }

}
