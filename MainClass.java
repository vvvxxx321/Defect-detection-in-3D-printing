

import java.awt.Graphics;
import java.awt.Graphics2D;
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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
import org.opencv.core.Point;
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
	
		// MenuItems for different functions
		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		JMenuItem m1 = new JMenuItem("Select images");
		JMenuItem m2 = new JMenuItem("Select video");
		JMenuItem m3 = new JMenuItem("Draw contour");
		
		menu.add(m1);
		menu.add(m2);
		menu.add(m3);
		mb.add(menu);
		
		jf.setJMenuBar(mb);
		

		// Implement action listener for processing images
		ActionListener select_images = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JPanel p = new JPanel();
				p.setLayout(new GridLayout(5, 1));
				
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
					imp.load_images(file.getPath());
					//System.out.println(mat_image.toString());
				}
				
				
							
				// Generate the data from the list of images
				imp.generate_data(images);
				
				
				// Create JFreeChart (line chart) to plot the data
			    JFreeChart chart_meltpool = imp.createChart(imp.dataset_meltpool1, "Average Melt Pool Size & Image Number", "Image Number", "Average Melt Pool Sizes");
				JFreeChart chart_splatter = imp.createChart(imp.dataset_splatter1, "Splatter Count & Frame Number", "Frame Number", "Splatter Count");
					
				ChartPanel cp_meltpool = new ChartPanel(chart_meltpool);
				ChartPanel cp_splatter = new ChartPanel(chart_splatter);
					
				cp_meltpool.setVisible(true);
				cp_splatter.setVisible(true);
				
				
				
				p.add(cp_meltpool);
				p.add(cp_splatter);
				
				
				
				p.setVisible(true);
				jf.add(p); 
				
			
			}
			
		};
		
		// Implement action listener for video processing
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
				
				// Use the camera to capture frames
				VideoCapture capture = new VideoCapture();	
				
				Mat image = new Mat();
				
				capture.open(file.getPath());	
				
				int video_length = 0;
				
				//DefaultCategoryDataset ds_melt = new DefaultCategoryDataset();
				//DefaultCategoryDataset ds_splatter = new DefaultCategoryDataset();
				
				//System.out.println(file.getPath());
				
				if(capture.isOpened()) {
				
				//System.out.println(1);
					
				video_length = (int) capture.get(Videoio.CAP_PROP_FRAME_COUNT);
				System.out.println(video_length);
				capture.set(Videoio.CAP_PROP_FPS, 100.0);
				int images_per_second = (int) capture.get(Videoio.CAP_PROP_FPS);
				System.out.println(images_per_second);
				int image_number = 0;
				Mat temp = new Mat();
				
				while(capture.read(image)) {
						if(image_number == 11227) {
							break;
						}
						
						if(image != temp) {
						String input_path = output_path + Integer.toString(image_number) + ".jpg";
						Imgcodecs.imwrite(input_path, image);
						
						imp.load_images(input_path);
						image_number ++;
						System.out.println(image_number);
													}
						
						temp = image.clone();
						
					
				}

				
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
				
				
					
				try {
					imp.generate_data_video(images);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			
				//System.out.println(5);
				
				
				//int i = 0;
				//while(i < video_length) {
					//ds_melt.addValue(imp.dataset_meltpool.getValue(0, i), imp.dataset_meltpool.getRowKey(0), imp.dataset_meltpool.getColumnKey(i));
					//ds_splatter.addValue(imp.dataset_splatter.getValue(0, i), imp.dataset_splatter.getRowKey(0), imp.dataset_splatter.getColumnKey(i));
					
					//i = i+200;
				//}
					
		/*		JFreeChart chart_meltpool1 = imp.createChart(imp.dataset_meltpool1, "Average Melt Pool Size & Image Number part 1", "Image Number", "Average Melt Pool Sizes");
				JFreeChart chart_meltpool2 = imp.createChart(imp.dataset_meltpool2, "Average Melt Pool Size & Image Number part 2", "Image Number", "Average Melt Pool Sizes");
				JFreeChart chart_splatter1 = imp.createChart(imp.dataset_splatter1, "Splatter Count & Frame Number part 1", "Frame Number", "Splatter Count");
				JFreeChart chart_splatter2 = imp.createChart(imp.dataset_splatter2, "Splatter Count & Frame Number part 2", "Frame Number", "Splatter Count");
				
				ChartPanel cp_meltpool1 = new ChartPanel(chart_meltpool1);
				ChartPanel cp_meltpool2 = new ChartPanel(chart_meltpool2);
				ChartPanel cp_splatter1 = new ChartPanel(chart_splatter1);
				ChartPanel cp_splatter2 = new ChartPanel(chart_splatter2);
				
				//System.out.println(6);
				
				
				p.add(cp_meltpool1);
				p.add(cp_meltpool2);
				p.add(cp_splatter1);
				p.add(cp_splatter2);
				
				//System.out.println(7);
				
				p.setVisible(true);
			    jf.add(p); */
				
			}
			
		};
		
		// To draw the melt pools
		ActionListener drawContour = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JPanel p = new JPanel();
				JTextField t = new JTextField(5);
				JButton b = new JButton("Submit");
				b.addActionListener(new ActionListener() {
					// input the number to get one part among the 113 parts of these melt pools
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						String sj = t.getText();
						int j = Integer.parseInt(sj);
						ArrayList<ArrayList<Point>> list_all;
						try {
							list_all = imp.get_C(j);
							CustomPaintComponent cpc = new CustomPaintComponent(list_all);
							cpc.setVisible(true);
							jf.setContentPane(cpc);
							jf.setVisible(true);
							System.out.print(list_all);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					
					}
					
				}); 
				
				p.add(t);
				p.add(b);
				p.setVisible(true);
				jf.add(p);
				jf.setVisible(true);
			}
			
			

		};
		
		
		m1.addActionListener(select_images);
		m2.addActionListener(select_video);
		m3.addActionListener(drawContour);
		
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
		
		
		
		jf.setSize(1100, 1100);
		jf.setLocationRelativeTo(null);
		jf.setVisible(true);
		
		
	}
	
	
	
	
}
