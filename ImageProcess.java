

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;

public class ImageProcess {
	
	ArrayList<Mat> image_list;
	ArrayList<Splatter> all_splatter_list = new ArrayList<Splatter>();
	ArrayList<MeltPool> all_meltpool_list = new ArrayList<MeltPool>();
	JFreeChart lineChart_meltpool1;
	JFreeChart lineChart_splatter1;
	JFreeChart lineChart_meltpool2;
	JFreeChart lineChart_splatter2;
	DefaultCategoryDataset dataset_meltpool1 = new DefaultCategoryDataset();
	DefaultCategoryDataset dataset_splatter1 = new DefaultCategoryDataset();
	DefaultCategoryDataset dataset_meltpool2 = new DefaultCategoryDataset();
	DefaultCategoryDataset dataset_splatter2 = new DefaultCategoryDataset();
	String series1 = "MeltPool";
	String series2 = "Splatter";
	Imgcodecs imagecodecs = new Imgcodecs();
	MiddleTier middle_tier;
	
	// Constructor
	public ImageProcess(ArrayList<Mat> image_list) {
		this.image_list = image_list;	
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.middle_tier = new MiddleTier();
	}
	
	// Load images into the list by the path of images
	public void load_images(String path) {
		Mat image = Imgcodecs.imread(path);
		this.image_list.add(image);
	}
	
	// Directly load images
	public void load_images2(Mat image) {
		this.image_list.add(image);
	}
	
	
	public ArrayList<Mat> get_images(){
		return this.image_list;
	}
	
	// Convert images to binary
	public ArrayList<Mat> image_to_binary(ArrayList<Mat> images){
		
		ArrayList<Mat> binarys = new ArrayList<Mat>();
		
		for(Mat image: images){
			Mat dst = new Mat();
			Imgproc.threshold(image, dst, 205, 255, Imgproc.THRESH_BINARY);
			binarys.add(dst);
		}
		
		return binarys;
	}
	
	// Canny edge detection
	public int[] auto_canny(Mat image){
		
		int[] thresholds = new int[2];
		int min_threshold;
		int max_threshold;
		
		Mat blurred = new Mat();
		
		Imgproc.GaussianBlur(image, blurred, new Size(3,3), 0);
		
		ArrayList<Double> list = new ArrayList<Double>();
		
		for(int i = 0; i < image.cols(); i++) {
			for(int j = 0; j < image.rows(); j++) {
				list.add(image.get(j, i)[0]);
				list.add(image.get(j, i)[1]);
				list.add(image.get(j, i)[2]);
			}
		}
		
		list.sort(null);
		
		double medain = this.get_median(list);
		
		min_threshold = (int)Math.max(0, medain * 0.67);
		max_threshold = (int)Math.min(255, medain * 1.33);
				
		thresholds[0] = min_threshold;
		thresholds[1] = max_threshold;
		
		Mat canny = new Mat();
		
		Imgproc.Canny(blurred, canny, min_threshold, max_threshold);
		
		return thresholds;
	}
	
	// Process the images
	public void generate_data(ArrayList<Mat> images) {
		
		
		
		ArrayList<Mat> binary_images = this.image_to_binary(images);
		
		for(int i = 0; i < images.size(); i++) {
			
			Mat image = images.get(i);
			Mat binary_image = binary_images.get(i);
			
			int min_threshold = this.auto_canny(image)[0];
			int max_threshold = this.auto_canny(image)[1];
			
			Mat edges = new Mat();
			Imgproc.Canny(binary_image, edges, min_threshold, max_threshold);
			
			ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
			
			if(contours.size() == 0) {
				dataset_meltpool1.addValue(0, series1, "0");
				dataset_splatter1.addValue(0, series2, "0");	}
			else {
			MatOfPoint melt_pool_contour = (MatOfPoint) this.find_melt_pool(contours)[0];
			ArrayList<MatOfPoint> splatters_contours = (ArrayList<MatOfPoint>)this.find_melt_pool(contours)[1];
	
			
			double major_axis = this.find_axes(melt_pool_contour)[0];
			double minor_axis = this.find_axes(melt_pool_contour)[1];
			
			System.out.println((melt_pool_contour.toList().size()));
			
			MeltPool meltpool = new MeltPool(melt_pool_contour, major_axis, minor_axis, max_threshold, min_threshold);
			
			all_meltpool_list.add(meltpool);
			
			ArrayList<Splatter> splatter_list = new ArrayList<Splatter>();
			
			for(Mat splatter_contour: splatters_contours) {
				ArrayList<MatOfPoint> new_splatter_contours = new ArrayList<MatOfPoint>();
				new_splatter_contours.add((MatOfPoint)splatter_contour);
				Splatter splatter = new Splatter(new_splatter_contours, major_axis, minor_axis, max_threshold, min_threshold);
				
				splatter_list.add(splatter);
			}
			
			for(Splatter splatter: splatter_list) {
				all_splatter_list.add(splatter);
			}
			
			dataset_meltpool1.addValue(this.get_melt_pool_average(all_meltpool_list)[2], series1, Integer.toString(i+1));
			dataset_splatter1.addValue(splatter_list.size(), series2, Integer.toString(i+1));
			
			}
			
			System.out.println(i);
		}
	}
	
	// Process the video
public void generate_data_video(ArrayList<Mat> images) throws Exception {
		
		
		
		ArrayList<Mat> binary_images = this.image_to_binary(images);
		
		int i = 0;
		
		// To divide the frames of video into two parts when spot to line chart, because of length limit
	/*	while(i < images.size()/2) {
			
			Mat image = images.get(i);
			Mat binary_image = binary_images.get(i);
			
			int min_threshold = this.auto_canny(image)[0];
			int max_threshold = this.auto_canny(image)[1];
			
			Mat edges = new Mat();
			Imgproc.Canny(binary_image, edges, min_threshold, max_threshold);
			
			ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
			
			if(contours.size() == 0) {
				dataset_meltpool1.addValue(0, series1, "0");
				dataset_splatter1.addValue(0, series2, "0");	}
			else {
			MatOfPoint melt_pool_contour = (MatOfPoint) this.find_melt_pool(contours)[0];
			ArrayList<MatOfPoint> splatters_contours = (ArrayList<MatOfPoint>)this.find_melt_pool(contours)[1];
	
			
			double major_axis = this.find_axes(melt_pool_contour)[0];
			double minor_axis = this.find_axes(melt_pool_contour)[1];
			
			
			MeltPool meltpool = new MeltPool(melt_pool_contour, major_axis, minor_axis, max_threshold, min_threshold);
			
			all_meltpool_list.add(meltpool);
			
			ArrayList<Splatter> splatter_list = new ArrayList<Splatter>();
			
			for(Mat splatter_contour: splatters_contours) {
				ArrayList<MatOfPoint> new_splatter_contours = new ArrayList<MatOfPoint>();
				new_splatter_contours.add((MatOfPoint)splatter_contour);
				Splatter splatter = new Splatter(new_splatter_contours, major_axis, minor_axis, max_threshold, min_threshold);
				
				splatter_list.add(splatter);
			}
			
			for(Splatter splatter: splatter_list) {
				all_splatter_list.add(splatter);
			}
			
			dataset_meltpool1.addValue(this.get_melt_pool_average(all_meltpool_list)[2], series1, Integer.toString(i+1));
			dataset_splatter1.addValue(splatter_list.size(), series2, Integer.toString(i+1));
			
			}
			
			System.out.println(i);
			
			i ++;
		} */
		
			while(i < images.size()) {
			
			// Create one table per frame
			this.middle_tier.create_table(i);
			
			Mat image = images.get(i);
			Mat binary_image = binary_images.get(i);
			
			int min_threshold = this.auto_canny(image)[0];
			int max_threshold = this.auto_canny(image)[1];
			
			Mat edges = new Mat();
			Imgproc.Canny(binary_image, edges, min_threshold, max_threshold);
			
			ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
			
			if(contours.size() == 0) {
			//	dataset_meltpool2.addValue(0, series1, "0");
			//	dataset_splatter2.addValue(0, series2, "0");
			}
			else {
			MatOfPoint melt_pool_contour = (MatOfPoint) this.find_melt_pool(contours)[0];
			ArrayList<MatOfPoint> splatters_contours = (ArrayList<MatOfPoint>)this.find_melt_pool(contours)[1];
			
			// Insert values to table in database
			int num = 1;
			for(Point p: melt_pool_contour.toArray()) {
				double x = p.x;
				double y = p.y;
				this.middle_tier.insert_value(i, num, x, y);
				num ++;
			}
	
			
			double major_axis = this.find_axes(melt_pool_contour)[0];
			double minor_axis = this.find_axes(melt_pool_contour)[1];
			
			
			
			MeltPool meltpool = new MeltPool(melt_pool_contour, major_axis, minor_axis, max_threshold, min_threshold);
			
			all_meltpool_list.add(meltpool);
			
			ArrayList<Splatter> splatter_list = new ArrayList<Splatter>();
			
			for(Mat splatter_contour: splatters_contours) {
				ArrayList<MatOfPoint> new_splatter_contours = new ArrayList<MatOfPoint>();
				new_splatter_contours.add((MatOfPoint)splatter_contour);
				Splatter splatter = new Splatter(new_splatter_contours, major_axis, minor_axis, max_threshold, min_threshold);
				
				splatter_list.add(splatter);
			}
			
			for(Splatter splatter: splatter_list) {
				all_splatter_list.add(splatter);
			}
			
			
			/* Update the data set for JFreeChart, Please run the above parts (half of frames) with comments when you plan to create the chart
			   Because the maximum length of the DefaultCategoryDataSet is about 10k, less than the number of frames of this video */
			
			//dataset_meltpool2.addValue(this.get_melt_pool_average(all_meltpool_list)[2], series1, Integer.toString(i+1));
			//dataset_splatter2.addValue(splatter_list.size(), series2, Integer.toString(i+1));
				
			
			
			}
			
			System.out.println(i);
			
			i++;
		}
	}
	

	// Get the melt pool and splatters from one image
	public Object[] find_melt_pool(ArrayList<MatOfPoint> contours) {
		
		
		Object[] result = new Object[2];
		
		MatOfPoint melt_pool = new MatOfPoint();
		ArrayList<MatOfPoint> splatters = new ArrayList<MatOfPoint>();
		
		ArrayList<Double> areas = new ArrayList<Double>();
		for(MatOfPoint contour: contours) {
			double area = Imgproc.contourArea(contour);
			if(contour.toArray().length < 5) {
				areas.add(0.0);
			}
			areas.add(area);	
		}
		
		ArrayList<Double> temList = new ArrayList<Double>(areas);
		temList.sort(null);
		double max_area = temList.get(temList.size()-1);
		
		int max_index = areas.indexOf(max_area);
		
		for(int i = 0; i < contours.size(); i++) {
			if(i == max_index) {
				melt_pool = contours.get(i);
			}
			else {
				splatters.add(contours.get(i));
			}
		}
		
		result[0] = melt_pool;
		result[1] = splatters;
		
		return result;
		
	}
	
	// Get the major and minor axis of a contour
	public double[] find_axes(MatOfPoint contour) {
		
		if(contour.toArray().length < 5) {
			double[] r = new double[]{0.0, 0.0};
			return r;
		}
		
		double[] result = new double[2];
		
		double major_axis = 0;
		double minor_axis = 0;
		
		MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
		RotatedRect ellipse = Imgproc.fitEllipse(contour2f);
		
		major_axis = ellipse.size.width;
		minor_axis = ellipse.size.height;
		
		result[0] = major_axis;
		result[1] = minor_axis;
		
		return result;
	}
	
	// Get the average size of a list of melt pools
	public double[] get_melt_pool_average(ArrayList<MeltPool> all_melt_pools) {
		
		double[] result = new double[3];
		
		double average_major_axis = 0;
		double average_minor_axis = 0;
		double average_area = 0;
		
		for(MeltPool melt_pool: all_melt_pools) {
			average_major_axis += melt_pool.get_major_axis();
			average_minor_axis += melt_pool.get_minor_axis();
			average_area += melt_pool.get_area();
		}
		
		average_major_axis = average_major_axis / all_melt_pools.size();
		average_minor_axis = average_minor_axis / all_melt_pools.size();
		average_area = average_area / all_melt_pools.size();
		
		result[0] = average_major_axis;
		result[1] = average_minor_axis;
		result[2] = average_area;
		
		return result;
	}
	
	public JFreeChart createChart(DefaultCategoryDataset dataset, String title, String x, String y) {
		JFreeChart lineChart = ChartFactory.createLineChart(title, x, y, dataset);
		return lineChart;
	}
	
	public double get_median(ArrayList<Double> list) {
		
		
		if((list.size() % 2) != 0) {
			return list.get((list.size() - 1) / 2);
		}
		
		else {
			double d1 = list.get((list.size()/2) - 1);
			double d2 = list.get(list.size()/2);
			return (d1+d2)/2;
		}
		
	}
	
	// Get data from database and allocate 100 melt pools for each panel, then return the list of the contours of these melt pools
public ArrayList<ArrayList<Point>> get_C(int j) throws Exception {
		
		MiddleTier middleTier = this.middle_tier;
		ArrayList<ArrayList<Point>> list_i_all = new ArrayList<ArrayList<Point>>();
		for(int i = j * 100; i < (j + 1) * 100 && i < 11227; i++) {
			// The location to allocate the centroid of a melt pool
			int m = (i%100)/10 + 1;
			int n = (i%100)%10 + 1;
			double cen_x_final = (2*n-1)*50;
			double cen_y_final = (2*m-1)*50;
			
			ArrayList<Point> list_i = middleTier.get_Points(i);
			double cen_x = 0;
			double cen_y = 0;
			for(Point p: list_i) {
				cen_x = cen_x + p.x;
				cen_y = cen_y + p.y;
			}
			
			// The centroid of a melt pool
			cen_x = cen_x/list_i.size();
			cen_y = cen_y/list_i.size();
			
			// Allocate the melt pool
			for(Point p: list_i) {
				p.x = p.x - (cen_x - cen_x_final);
				p.y = p.y - (cen_y - cen_y_final);
			}
			
			list_i_all.add(list_i);
		}
		return list_i_all;
	}
	
}
