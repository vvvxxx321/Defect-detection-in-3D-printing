

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;

public class ImageProcess {
	
	ArrayList<Mat> image_list;
	ArrayList<Splatter> all_splatter_list = new ArrayList<Splatter>();
	ArrayList<MeltPool> all_meltpool_list = new ArrayList<MeltPool>();
	JFreeChart lineChart_meltpool;
	JFreeChart lineChart_splatter;
	DefaultCategoryDataset dataset_meltpool = new DefaultCategoryDataset();
	DefaultCategoryDataset dataset_splatter = new DefaultCategoryDataset();
	String series1 = "MeltPool";
	String series2 = "Splatter";
	Imgcodecs imagecodecs = new Imgcodecs();
	
	public ImageProcess(ArrayList<Mat> image_list) {
		this.image_list = image_list;	
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public void load_images(String path) {
		Mat image = Imgcodecs.imread(path);
		this.image_list.add(image);
	}
	
	public void load_images2(Mat image) {
		this.image_list.add(image);
	}
	
	public ArrayList<Mat> get_images(){
		return this.image_list;
	}
	
	public ArrayList<Mat> image_to_binary(ArrayList<Mat> images){
		
		ArrayList<Mat> binarys = new ArrayList<Mat>();
		
		for(Mat image: images){
			Mat dst = new Mat();
			Imgproc.threshold(image, dst, 205, 255, Imgproc.THRESH_BINARY);
			binarys.add(dst);
		}
		
		return binarys;
	}
	
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
			
			//if(contours != null) {
			MatOfPoint melt_pool_contour = (MatOfPoint)this.find_melt_pool(contours)[0];
			ArrayList<MatOfPoint> splatters_contours = (ArrayList<MatOfPoint>)this.find_melt_pool(contours)[1];
	
			
			double major_axis = this.find_axes(melt_pool_contour)[0];
			double minor_axis = this.find_axes(melt_pool_contour)[1];
			
			ArrayList<MatOfPoint> new_melt_pool_contours = new ArrayList<MatOfPoint>();
			new_melt_pool_contours.add((MatOfPoint)melt_pool_contour);
			
			MeltPool meltpool = new MeltPool(new_melt_pool_contours, major_axis, minor_axis, max_threshold, min_threshold);
			
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
			
			dataset_meltpool.addValue(this.get_melt_pool_average(all_meltpool_list)[2], series1, Integer.toString(i+1));
			dataset_splatter.addValue(splatter_list.size(), series2, Integer.toString(i+1));
			//}
		}
		
	}
	
	public Object[] find_melt_pool(ArrayList<MatOfPoint> contours) {
		
		Object[] result = new Object[2];
		
		Mat melt_pool = new Mat();
		ArrayList<MatOfPoint> splatters = new ArrayList<MatOfPoint>();
		
		ArrayList<Double> areas = new ArrayList<Double>();
		for(MatOfPoint contour: contours) {
			double area = Imgproc.contourArea(contour);
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
	
	public double[] find_axes(MatOfPoint contour) {
		
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
	
	
	
}
