

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
	ArrayList<MatOfPoint> contours;
	double major_axis;
	double minor_axis;
	double max_thr;
	double min_thr;
	double area;
	
	public MeltPool(ArrayList<MatOfPoint> contours, double major_axis, double minor_axis, double max_thr, double min_thr) {
		
		this.contours = contours;
		this.major_axis = major_axis;
		this.minor_axis = minor_axis;
		this.max_thr = max_thr;
		this.min_thr = min_thr;
		this.area = Math.PI * (major_axis/2) * (minor_axis/2);
		
		this.x_list = this.get_x_and_y().get(0);
		this.y_list = this.get_x_and_y().get(1);
	}
	

	public List<List<Double>> get_x_and_y(){
		
		List<List<Double>> list = new ArrayList<List<Double>>();
		
		List<Double> x_values = new ArrayList<Double>();
		List<Double> y_values = new ArrayList<Double>();
		
		for(int i = 0; i < this.contours.size(); i++) {
			x_values.add(this.contours.get(i).toList().get(0).x);
			y_values.add(this.contours.get(i).toList().get(0).y);
		}
		
		list.add(x_values);
		list.add(y_values);
		
		return list;
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
