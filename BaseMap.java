

import java.awt.image.DataBufferByte;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters.ExportBy;
import com.esri.map.JMap;

import aamc.FlightMap;
import core.coordianteAdapters.UTM;
import javafx.scene.layout.StackPane;

import java.util.Scanner;
import java.util.Stack;
public class BaseMap extends Image {


	Envelope mapExtent;
	public BaseMap() {
		scale = new Point(1,1);
		mapExtent = new Envelope();
		mapExtent.setXMax(20);
		mapExtent.setYMax(20);
		mapExtent.setXMin(0);
		mapExtent.setYMin(0);
	}

	
	public BaseMap(String baseMapPath) {
		
		 ListView<Basemap.Type> basemapList = new ListView<>(FXCollections.observableArrayList(Basemap.Type.values()));
	      basemapList.setMaxSize(250, 150);

	      basemapList.getSelectionModel().selectedItemProperty().addListener(o -> {
	        String basemapString = basemapList.getSelectionModel().getSelectedItem().toString();
	        map = new ArcGISMap(Basemap.Type.valueOf(basemapString), LATITUDE, LONGITUDE, LOD);
	        mapView.setMap(map);
	      });
	      basemapList.getSelectionModel().selectFirst();
	      StackPane.setAlignment(basemapList, Pos.TOP_LEFT);
	      StackPane.setMargin(basemapList, new Insets(10, 0, 0, 10));
	}
	
	
	public void exportMap(JMap map) {
		byte[] pixels = ((DataBufferByte) map.exportMapImage().getRaster().getDataBuffer()).getData();
		RGBmat.put(0, 0, pixels);
		mapExtent = map.getExtent();
		Integer mapheight = map.exportMapImage().getHeight();  
		Integer mapwidth = map.exportMapImage().getWidth();  
		Double coordHeight = (map.getExtent().getUpperLeft().getY() - map.getExtent().getLowerLeft().getY());  
		Double coordWidth = (map.getExtent().getUpperRight().getX() - map.getExtent().getUpperLeft().getX());  
		scale.setXY(coordWidth / mapwidth, -coordHeight / mapheight);
		
	}


	
	public Mat getBlankMat(int channels) {
		int type;
		if (channels == 3)
		{
			type = CvType.CV_8UC3;
		}
		else
		{
			type = CvType.CV_8UC1;
		}
		return Mat.zeros(grayMat.rows(), grayMat.cols(), type);
	}

	
	public Point getScale() {

		return scale;
	}

	
	public void resizeImage(double scaleFactor) {
		int originalHeight = grayMat.rows();
		int originalWidth = grayMat.cols();

		int currentWidth = (int) (originalWidth * scaleFactor);
		int currnetHeight = (int) (originalHeight * scaleFactor);

		Size sz = new Size(currentWidth, currnetHeight);
		Imgproc.resize(grayMat, grayMat, sz);
	}
	@Override
	public UTM getTopLeftCorner()
	{
		return new UTM(mapExtent.getUpperLeft(), FlightMap.mainMapSpatialReference);
	}
	
	private void createBaseMap(JMap map)
	{
		double[] levels = {0, 1, 2, 3, 4};

	    // set up parameters
	    ExportTileCacheParameters params = new ExportTileCacheParameters(
	        true, 
	        levels, 
	        ExportBy.ID, 
	        map.getExtent(),
	        map.getSpatialReference());

	    params.setRecompressTileCache(true);
	    params.setRecompressionQuality(90);
	}
	public void image1(Jmap map)
	{
		return;
	}
	
	public void image2(Jmap map)
	{
		return;
	}
	private void createWorldFile(JMap map)
	{
		
			boolean[] seating = new boolean[11]; 
		    Scanner input = new Scanner(System.in);

		    public void start()
		    {       
		        while ( true )
		        {
		            captureImage();
		        }   
		    }

		    public void captureImage()
		    {
		       
		        int section = input.nextInt();
		        if ( section == 1 )
		        {
		            image1();
		        }
		        else
		        {
		            image2();
		        }
		    }

		     

		   
	}
}
