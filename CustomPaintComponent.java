import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.opencv.core.Point;

// Paint the contour of a list of melt pools
public class CustomPaintComponent extends JComponent {
	
	ArrayList<ArrayList<Point>> p_list_all;
	
	public CustomPaintComponent(ArrayList<ArrayList<Point>> p_list_all) {
		this.p_list_all = p_list_all;
	}
	
	public void paint(Graphics g) {

		for(ArrayList<Point> p_list: p_list_all) {
			for(Point p: p_list) {
				g.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
			}
		}
	}
}
