package core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import com.esri.core.geometry.Point;

import core.coordianteAdapters.UTM;



public abstract class Image
{
	protected Mat RGBmat = null;
	protected Mat grayMat = null;

	protected Point originalSize;
	protected Point scale;
	protected UTM location; /**The center of the image.*/

	protected String fileName;

	protected static ArrayList<Camera> cameraTable;

	public static ArrayList<Camera> getCameraTable()
	{
		return cameraTable;
	}

	protected int myCamera;
	private boolean isVisible;

	
	public UTM getTopLeftCorner()
	{
		UTM topLeftCorner = new UTM();
		topLeftCorner.setNorthing(location.getNorthing() - (originalSize.getY() * scale.getY()));
		topLeftCorner.setEasting(location.getEasting() - (originalSize.getX() * scale.getX()));

		return topLeftCorner;
	}

	
	public UTM getBottomRightCorner()
	{
		UTM bottomRightCorner = new UTM();
		bottomRightCorner.setNorthing(location.getNorthing() - ((originalSize.getY() / 2) * scale.getY()));
		bottomRightCorner.setEasting(location.getEasting() - ((originalSize.getX() / 2) * scale.getX()));

		return bottomRightCorner;
	}

	
	public BufferedImage toBufferedImage()
	{
		Mat matrix = RGBmat;
		int cols = matrix.cols();
		int rows = matrix.rows();
		int elemSize = (int) matrix.elemSize();
		byte[] data = new byte[cols * rows * elemSize];
		int type;

		matrix.get(0, 0, data);
		switch (matrix.channels())
		{
		case 1:
			type = BufferedImage.TYPE_BYTE_GRAY;
			break;
		case 3:
			type = BufferedImage.TYPE_3BYTE_BGR;
			// bgr to rgb
			byte b;
			for (int i = 0; i < data.length; i = i + 3)
			{
				b = data[i];
				data[i] = data[i + 2];
				data[i + 2] = b;
			}
			break;
		default:
			return null;
		}

		BufferedImage img = new BufferedImage(cols, rows, type);
		img.getRaster().setDataElements(0, 0, cols, rows, data);
		return img;
	}

	
	
	public void loadImage(String imagePath)
	{
		RGBmat = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_COLOR); 
		grayMat = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_GRAYSCALE);
		originalSize = new Point(grayMat.rows(), grayMat.cols());
		setVisible(true);
	}
	

	public Mat getMat()
	{
		return RGBmat;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public boolean isVisible()
	{
		return isVisible;
	}

	public void setVisible(boolean isVisible)
	{
		this.isVisible = isVisible;
	}

	public void setCam(Camera cam)
	{
		if (Image.cameraTable == null)
		{
			Image.cameraTable = new ArrayList<Camera>();
		}
		for (int i = 0; i < Image.cameraTable.size(); i++)
		{
			if (Image.cameraTable.get(i).equals(cam))
			{
				this.myCamera = i;
				return;
			}
		}
		Image.cameraTable.add(cam);
		this.myCamera = Image.cameraTable.size() - 1;
	}

	public Camera getCam()
	{
		return Image.cameraTable.get(this.myCamera);
	}

	public UTM getLocation()
	{
		return location;
	}

	public void setLocation(UTM location)
	{
		this.location = location;
	}

}
