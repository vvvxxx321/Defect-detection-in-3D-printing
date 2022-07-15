

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.sql.*;

public class MainClass {
	
	public static void main(String args[]) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		ArrayList<Mat> images = new ArrayList<Mat>();
		
		ImageProcess imp = new ImageProcess(images);
		
		List<Integer> mp_x_list = new ArrayList<Integer>();	
	
		
		List<Double> mp_y_list = new ArrayList<Double>();
	
		
		List<Integer> sp_x_list = new ArrayList<Integer>();
		
		List<Double> sp_y_list = new ArrayList<Double>();
		
		JFrame jf = new JFrame();
		jf.setSize(700, 400);
	
		
		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		JMenuItem m1 = new JMenuItem("Select images");
		JMenuItem m2 = new JMenuItem("Select video");
		
		menu.add(m1);
		menu.add(m2);
		mb.add(menu);
		
		jf.setJMenuBar(mb);
		

		
		ActionListener select_images = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JPanel p = new JPanel();
				p.setLayout(new GridLayout(3, 1));
				
				JFileChooser chooser = new JFileChooser();
						
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "tif", "png", "jpeg");

				chooser.setFileFilter(filter);
				chooser.setMultiSelectionEnabled(true);
				chooser.showDialog(jf, "select");
				
				File[] files;
				
				files = chooser.getSelectedFiles();
			
			
				for(File file: files) {
					String path = file.getPath();
					Mat mat_image = Imgcodecs.imread(path);
					//System.out.println(path);
					imp.load_images(file.getPath());
					System.out.println(mat_image.toString());
				}
				
				System.out.println(0);
					
				
				
				
				
				
				
				//imp.generate_data(images);
				
				//System.out.println(1);
				
			    //JFreeChart chart_meltpool = imp.createChart(imp.dataset_meltpool, "Average Melt Pool Size & Image Number", "Image Number", "Average Melt Pool Sizes");
				//JFreeChart chart_splatter = imp.createChart(imp.dataset_splatter, "Splatter Count & Frame Number", "Frame Number", "Splatter Count");
				
				//System.out.println(2);
				
				//ChartPanel cp_meltpool = new ChartPanel(chart_meltpool);
				//ChartPanel cp_splatter = new ChartPanel(chart_splatter);
				
				//System.out.println(3);
				
				//cp_meltpool.setVisible(true);
				//cp_splatter.setVisible(true);
				
				//System.out.println(4);
				
				//p.add(cp_meltpool);
				//p.add(cp_splatter);
				
				//System.out.println(5);
				
				//p.setVisible(true);
				//jf.add(p);
				
				//System.out.println(6);
				
				//System.out.println(images.toString());
			}
			
		};
		
		ActionListener select_video = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String output_path = "/Users/yuyu/VideoToImages/";
				
				
				JPanel p = new JPanel();
				p.setLayout(new GridLayout(3, 1));
				
				
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Video files", "mp4", "cine", "mpeg", "mov");
				chooser.setFileFilter(filter);
				
				chooser.showDialog(jf, "select");
				
				File file = chooser.getSelectedFile();
				
				VideoCapture capture = new VideoCapture();	
				
				Mat image = new Mat();
				
				capture.open(file.getPath());	
				
				//System.out.println(file.getPath());
				
				if(capture.isOpened()) {
				
				//System.out.println(1);
					
				int video_length = (int) capture.get(Videoio.CAP_PROP_FRAME_COUNT);
				System.out.println(video_length);
				int images_per_second = (int) capture.get(Videoio.CAP_PROP_FPS);
				System.out.println(images_per_second);
				int image_number = 0;
				
				
				if(capture.read(image)) {
					for(int i = 0; i < video_length; i++) {
						//BufferedImage icon = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_3BYTE_BGR);
						//Mat final_image = new Mat(icon.getWidth(), icon.getHeight(), CvType.CV_8UC3);
						//byte[] pixels = ((DataBufferByte) icon.getRaster().getDataBuffer()).getData();
						//final_image.put(0, 0, pixels);
						//imp.load_images2(final_image);
						//MatOfByte buf = new MatOfByte();
						//Imgcodecs.imencode(".jpg", image, buf);
						//byte[] imageData = buf.toArray();
						//System.out.println(image.toString());
						//System.out.println(new_image.toString());
						//System.out.println(image.get(20, 10)[0]);
						//System.out.println(image.get(20, 10)[1]);
						//System.out.println(image.get(20, 10)[2]);
						
						
						String input_path = output_path + Integer.toString(image_number) + ".jpg";
						Imgcodecs.imwrite(input_path, image);
						
						imp.load_images(input_path);
						
						image_number ++;
						//System.out.println(image.toString());
					}
				}
				
				System.out.println(image_number);
				
				capture.release();
				
				//ArrayList<Mat> binary_images = imp.image_to_binary(images);
				
				//for(int i = 0; i < video_length; i++) {
					//Mat binary_image = binary_images.get(i);
					
					//int min_threshold = imp.auto_canny(images.get(i))[0];
					//int max_threshold = imp.auto_canny(images.get(i))[1];
					
					//Mat edges = new Mat();
					//Imgproc.Canny(binary_image, edges, min_threshold, max_threshold);
					
					//ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
					//Mat hierarchy = new Mat();
					//Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
					
					//System.out.println(contours.toString());
				//}
				
			  }
				
				
				//for(Mat im: images) {
					//System.out.println(im.get(20, 10)[0]);
					//System.out.println(im.get(20, 10)[1]);
					//System.out.println(im.get(20, 10)[2]);
				//}
				
				
				//File rootDir = new File(output_path);
				//System.out.println(rootDir.getPath());
				//File[] imageFiles = rootDir.listFiles();
				
				//System.out.println(5);
				
				//for(File imageFile: imageFiles) {
					//imp.load_images(imageFile.getAbsolutePath());
				//}
				
				//System.out.println(images.size());
				
				//System.out.println(images.get(0).toString());
				//System.out.println(images.get(1).toString());
				
				//ArrayList<Mat> new_images = new ArrayList<Mat>();
				
				//new_images.add(images.get(0));
				//new_images.add(images.get(1));
				
				
					
				imp.generate_data(images);
			
				//System.out.println(5);
				
				JFreeChart chart_meltpool = imp.createChart(imp.dataset_meltpool, "Average Melt Pool Size & Image Number", "Image Number", "Average Melt Pool Sizes");
				JFreeChart chart_splatter = imp.createChart(imp.dataset_splatter, "Splatter Count & Frame Number", "Frame Number", "Splatter Count");
				
				ChartPanel cp_meltpool = new ChartPanel(chart_meltpool);
				ChartPanel cp_splatter = new ChartPanel(chart_splatter);
				
				//System.out.println(6);
				
				
				p.add(cp_meltpool);
				p.add(cp_splatter);
				
				//System.out.println(7);
				
				p.setVisible(true);
			    jf.add(p);
				
			}
			
		};
		
		m1.addActionListener(select_images);
		m2.addActionListener(select_video);
		
		
		//JPanel p = new JPanel();
		
		//DefaultCategoryDataset data_set = new DefaultCategoryDataset();
		//String series = "test";
		//data_set.addValue(5, series, "20");
		//data_set.addValue(10, series, "40");
		//JFreeChart chart = ChartFactory.createLineChart("title", "x", "y", data_set);
		
		//ChartPanel test_panel = new ChartPanel(chart);
		//p.add(test_panel);
		
		//p.setVisible(true);
		
		
		
		//jf.add(p);
		
		

		
		//System.out.println(7);
		
		//System.out.println(8);
		
		jf.setVisible(true);
		
		//System.out.println(9);
		
		//System.out.println(images.toString());
	}
	
}
