

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.opencv.core.MatOfPoint;

public class MeltPool {
	
	List<Double> x_list;
	List<Double> y_list;
	MatOfPoint contour;
	double major_axis;
	double minor_axis;
	double max_thr;
	double min_thr;
	double area;
	
	public MeltPool(MatOfPoint contour, double major_axis, double minor_axis, double max_thr, double min_thr) {
		
		this.contour = contour;
		this.major_axis = major_axis;
		this.minor_axis = minor_axis;
		this.max_thr = max_thr;
		this.min_thr = min_thr;
		this.area = Math.PI * (major_axis/2) * (minor_axis/2);
	
	}
	

	
	
	public double get_major_axis() {
		return this.major_axis;
	}
	
	public double get_minor_axis() {
		return this.minor_axis;
	}
	
	public double get_area() {
		return this.area;
	}
}
