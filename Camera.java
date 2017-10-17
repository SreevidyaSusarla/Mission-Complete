package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Element;
import com.esri.core.geometry.Point;

import core.coordianteAdapters.Attitude;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Camera
{

    protected double pFocalLength;
    private double pPrincipalPointx;
    private double pPrincipalPointy;
    private double scaling;
    private double affinity;
    private double radialk1;
    private double radialk2;
    private double radialk3;
    private double tant1;
    private double tant2;
    private double unorthogonality;
    private int pWidth;
    private int pHeight;
    protected double sWidth;
    protected double sHeight;
    private double mmOverPix;
    private String name;
    private String make;
    private String model;
    private String serialNum;
    
    private double xRotation;
    private double yRotation;
    private double zRotation;
    
   
    @SuppressWarnings("unused")
	private int version;
    private String SCamera;
    @SuppressWarnings("unused")
	private String owner;
    @SuppressWarnings("unused")
	private double triggerDelay;
    @SuppressWarnings("unused")
	private int numChannels;
    @SuppressWarnings("unused")
	private int channelId;
    
    public static double LON_OVERLAP = 0.5;
    public static double CAMERA_SWADTH_BUFF = 0.7;
    
    public Camera()
    {        
        sWidth = 13.15;
        sHeight = 8.773;
        pWidth = 4242;
        pHeight = 2830;
        
        mmOverPix = sWidth/pWidth;
        
        pFocalLength = 5290.403041825095;
        pPrincipalPointx = pWidth/2;
        pPrincipalPointy = pHeight/2;
        scaling = 0;
        affinity = 0;
        radialk1 = 0;
        radialk2 = 0;
        radialk3 = 0;
        tant1 = 0;
        tant2 = 0;
        unorthogonality = 0;
        
        name = "Default";
        make = "Make";
        model = "Model";
        serialNum = "Number";
        
      
        version = 0;
        SCamera = null;
        owner = null;
        triggerDelay = 0.0;
        numChannels = 0;
        channelId = 0;     
        
    }
    
    public Camera(File file) throws FileNotFoundException, IOException
    {
    	if(FilenameUtils.getExtension(file.getName()).equals("cal"))
    	{
    		fromCalFile(file);
    	}
    }

    
	private Camera fromCalFile(File calFile) throws FileNotFoundException, IOException
    {
        
		BufferedReader reader = new BufferedReader(new FileReader(calFile));
    
		Camera cam = parseCalFile(reader, calFile.getName());

        reader.close(); 
        return cam;
        
    }
	
	private Camera parseCalFile(BufferedReader reader, String fileName) throws IOException
	{
		Camera newCam = new Camera();
        String line = reader.readLine();
        if(line.matches("Version\\s*\\d+\\s*"))
            newCam.version = Integer.parseInt(line.split("Version\\s*")[1]);
        else
            throw new IOException("Version is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        while(!line.matches("Camera\\s*.+/.+/.*"))
        {
            line = reader.readLine();
            if(line == null)
                throw new IOException("Camera name is either missing or formatted incorrectly in the calibration file:" + fileName);
        }
        
        SCamera = line.split("Camera\\s*")[1];
        String[] caminfo = SCamera.split("/");        
        
        if(caminfo.length>0)
            make = caminfo[0];
        if(caminfo.length>1)
            model = caminfo[1];
        if(caminfo.length>2)
            serialNum = caminfo[2];
        
        line = reader.readLine();
        while(!line.matches("Owner\\s*.+"))
        {
            line = reader.readLine();
            if(line == null)
                throw new IOException("The Owner is either missing or formatted incorrectly in the calibration file:" + fileName);
        }
            
        owner = line.split("Owner\\s*")[1];
                
        reader.mark(1000);
        line = reader.readLine();
        while(!line.matches("Trigger_delay\\s*[0-9.]+"))
        {
            line = reader.readLine();
            if(line == null)
                break;
        }
        if(line == null)
        {
            triggerDelay = 0;
            reader.reset();
        }
        else
            triggerDelay = Double.parseDouble(line.split("Trigger_delay\\s*")[1]);
        
        line = reader.readLine();
        while(!line.matches("Number_of_channels\\s*\\d+"))
        {
            line = reader.readLine();
            if(line == null)
                throw new IOException("The Number of channels is either missing or formatted incorrectly in the calibration file:" + fileName);
        }
        
        numChannels = Integer.parseInt(line.split("Number_of_channels\\s*")[1]);
        
        line = reader.readLine();
        while(!line.matches("Channel_id\\s*\\d+"))
        {
            line = reader.readLine();
            if(line == null)
                throw new IOException("The channel ID is either missing or formatted incorrectly in the calibration file:" + fileName);
        }
        
        channelId = Integer.parseInt(line.split("Channel_id\\s*")[1]);
        
        line = reader.readLine(); 
        if(line.matches("Focal_length\\s*[0-9.]+"))
            pFocalLength = Double.parseDouble(line.split("Focal_length\\s*")[1]);
        else
            throw new IOException("Focal_length is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Principal_point_x\\s*[0-9.]+"))
            newCam.pPrincipalPointx = Double.parseDouble(line.split("Principal_point_x\\s*")[1]);
        else
            throw new IOException("Principal_point_x is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Principal_point_y\\s*[0-9.]+"))
            newCam.pPrincipalPointy = Double.parseDouble(line.split("Principal_point_y\\s*")[1]);
        else
            throw new IOException("Principal_point_y is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("General_scaling\\s*[0-9.-]+"))
            newCam.scaling = Double.parseDouble(line.split("General_scaling\\s*")[1]);
        else
            throw new IOException("General_scaling is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Affinity\\s*[0-9.-]+"))
            newCam.affinity = Double.parseDouble(line.split("Affinity\\s*")[1]);
        else
            throw new IOException("Affinity is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Radial_k1\\s*[0-9.-]+"))
            newCam.radialk1 = Double.parseDouble(line.split("Radial_k1\\s*")[1]);
        else
            throw new IOException("Radial_k1 is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Radial_k2\\s*[0-9.-]+"))
            newCam.radialk2 = Double.parseDouble(line.split("Radial_k2\\s*")[1]);
        else
            throw new IOException("Radial_k2 is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Radial_k3\\s*[0-9.-]+"))
            newCam.radialk3 = Double.parseDouble(line.split("Radial_k3\\s*")[1]);
        else
            throw new IOException("Radial_k3 is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Tangential_t1\\s*[0-9.-]+"))
            newCam.tant1 = Double.parseDouble(line.split("Tangential_t1\\s*")[1]);
        else
            throw new IOException("Tangential_t1 is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Tangential_t2\\s*[0-9.-]+"))
            newCam.tant2 = Double.parseDouble(line.split("Tangential_t2\\s*")[1]);
        else
            throw new IOException("Tangential_t2 is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();
        if(line.matches("Un-orthogonality\\s*[0-9.-]+"))
            newCam.unorthogonality = Double.parseDouble(line.split("Un-orthogonality\\s*")[1]);
        else
            throw new IOException("Un-orthogonality is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();        
        if(line.matches("Sensor_width\\s+[0-9]+\\s+[0-9.]+"))
        {
            String[] width = line.split("\\s+");
            newCam.pWidth = Integer.parseInt(width[1]);
            newCam.sWidth = Double.parseDouble(width[2]);
        }
        else
            throw new IOException("Sensor_width is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        line = reader.readLine();        
        if(line.matches("Sensor_height\\s+[0-9]+\\s+[0-9.]+"))
        {
            String[] width = line.split("\\s+");
            newCam.pHeight = Integer.parseInt(width[1]);
            newCam.sHeight = Double.parseDouble(width[2]);
        }
        else
            throw new IOException("Sensor_height is either missing or formatted incorrectly in the calibration file:" + fileName);
        
        newCam.name =fileName.split("\\.")[0];
        newCam.mmOverPix = newCam.sWidth/newCam.pWidth;
		return newCam;
        
	}
    
	
    @SuppressWarnings("unused")
	private void fromXML(Element ugearelement)
    {   
        try
        {
            pFocalLength = Double.parseDouble(ugearelement.getAttribute("pFocalLength").getValue());
            pPrincipalPointx = Double.parseDouble(ugearelement.getAttribute("pPrincipalPointx").getValue());
            pPrincipalPointy = Double.parseDouble(ugearelement.getAttribute("pPrincipalPointy").getValue());
            scaling = Double.parseDouble(ugearelement.getAttribute("scaling").getValue());
            affinity = Double.parseDouble(ugearelement.getAttribute("affinity").getValue());
            radialk1 = Double.parseDouble(ugearelement.getAttribute("radialk1").getValue());
            radialk2 = Double.parseDouble(ugearelement.getAttribute("radialk2").getValue());
            radialk3 = Double.parseDouble(ugearelement.getAttribute("radialk3").getValue());
            tant1 = Double.parseDouble(ugearelement.getAttribute("tant1").getValue());
            tant2 = Double.parseDouble(ugearelement.getAttribute("tant2").getValue());
            unorthogonality = Double.parseDouble(ugearelement.getAttribute("unorthogonality").getValue());
            pWidth = Integer.parseInt(ugearelement.getAttribute("pWidth").getValue());
            pHeight = Integer.parseInt(ugearelement.getAttribute("pHeight").getValue());
            sWidth = Double.parseDouble(ugearelement.getAttribute("sWidth").getValue());
            sHeight = Double.parseDouble(ugearelement.getAttribute("sHeight").getValue());
            mmOverPix = Double.parseDouble(ugearelement.getAttribute("mmOverPix").getValue());
            name = ugearelement.getAttribute("Name").getValue();
            model = ugearelement.getAttribute("Model").getValue();
            if(model.equals(""))
                model = "Model";
            make = ugearelement.getAttribute("Make").getValue();
            if(make.equals(""))
                make = "Make";
            serialNum = ugearelement.getAttribute("SerialNum").getValue();
            if(serialNum.equals(""))
                serialNum = "Number";
        }
 
        catch(NumberFormatException | NullPointerException ex){}        
        
    }
    
    public void toXML(Element element)
    {
        element.setAttribute("pFocalLength", String.valueOf(pFocalLength));
        element.setAttribute("pPrincipalPointx", String.valueOf(pPrincipalPointx));
        element.setAttribute("pPrincipalPointy", String.valueOf(pPrincipalPointy));
        element.setAttribute("scaling", String.valueOf(scaling));
        element.setAttribute("affinity", String.valueOf(affinity));
        element.setAttribute("radialk1", String.valueOf(radialk1));
        element.setAttribute("radialk2", String.valueOf(radialk2));
        element.setAttribute("radialk3", String.valueOf(radialk3));
        element.setAttribute("tant1", String.valueOf(tant1));
        element.setAttribute("tant2", String.valueOf(tant2));
        element.setAttribute("unorthogonality", String.valueOf(unorthogonality));
        element.setAttribute("pWidth", String.valueOf(pWidth));
        element.setAttribute("pHeight", String.valueOf(pHeight));
        element.setAttribute("sWidth", String.valueOf(sWidth));
        element.setAttribute("sHeight", String.valueOf(sHeight));
        element.setAttribute("mmOverPix", String.valueOf(mmOverPix));
        element.setAttribute("Name", String.valueOf(name));
        element.setAttribute("Make", String.valueOf(make));
        element.setAttribute("Model", String.valueOf(model));
        element.setAttribute("SerialNum", String.valueOf(serialNum));
    }
    
    public void testFieldOfView(){
		Camera tester = new Camera();
		tester.pFocalLength = 50;
		tester.sHeight = 24;
		tester.sWidth = 36;
		assert(Math.toDegrees(tester.getFieldOfView().getX()) == 39.59775270904986 && Math.toDegrees(tester.getFieldOfView().getY()) == 26.991466561591626);
	}
    
    public double getSweepWidth(double alt)
    {
        return (alt * (this.pWidth / this.pFocalLength)) * (1 - LON_OVERLAP);
    }
    public double getSwadth(double alt) 
    {
        return CAMERA_SWADTH_BUFF * alt * (this.pWidth / this.pFocalLength);
    }
   
    public double getSwadthInverse(double swadth) 
    {
        return swadth * (pFocalLength / (pWidth*CAMERA_SWADTH_BUFF));
    }
    public void getFieldOfView()
    {
    	StackPane stackPane = new StackPane();
        Scene scene = new Scene(stackPane);
        Stage.setTitle("Basemap");
        Stage.setWidth(800);
        Stage.setHeight(700);
        Stage.setScene(scene);
        Stage.show();
    }
    
    public String toString()
    {
        return name;
    }
    
    public boolean equals(Object c)
    {
        if(!c.getClass().equals(Camera.class))
            return false;
        Camera cam = (Camera)c;
        if(sWidth == cam.sWidth &&
        sHeight == cam.sHeight &&
        pWidth == cam.pWidth &&
        pHeight == cam.pHeight &&        
        pFocalLength == cam.pFocalLength &&
        pPrincipalPointx == cam.pPrincipalPointx &&
        pPrincipalPointy == cam.pPrincipalPointy &&
        scaling == cam.scaling &&
        affinity == cam.affinity &&
        radialk1 == cam.radialk1 &&
        radialk2 == cam.radialk2 &&
        radialk3 == cam.radialk3 &&
        tant1 == cam.tant1 &&
        tant2 == cam.tant2 &&
        unorthogonality == cam.unorthogonality &&
        name.equals(cam.name)&&
        model.equals(cam.model)&&
        make.equals(cam.make)&&
        serialNum.equals(cam.serialNum))
            return true;
        else if(model.equals(cam.model)&& name.equals(cam.name))
        	return true;
        else
            return false;
    }
    
    @SuppressWarnings("rawtypes")
	public Enum[] getProperties()
    {
        return Properties.values();
    }
    
    private enum Properties
    {
        Name,
        Model,
        Make,
        SerialNum,
        CalFile,
        PWidth,
        PHeight,
        PFocalLength,
        PrinciplePointx,
        PrinciplePointy,
        Scaling,
        Affinity,
        Radialk1,
        Radialk2,
        Radialk3,
        Tant1,
        Tant2,
        Unorthogonality,
        mmOverPix,
        SWidth,
        SHeight,
        SFocalLength,
    }

	public String getName()
	{
		return name;
	}

	public int getSensorPixelX()
	{
		return pWidth;
	}

	public int getSensorPixelY()
	{
		return pHeight;
	}

	public double getFocalLength()
	{
		return pFocalLength;
	}

	public void setModel(String attribute)
	{
		model = attribute;
		
	}

	public void setName(String attribute)
	{
		name = attribute;
		
	}

	public double getxRotation()
	{
		return xRotation;
	}

	public double getyRotation()
	{
		return yRotation;
	}

	public double getzRotation()
	{
		return zRotation;
	}

	public void setxRotation(double xRotation)
	{
		this.xRotation = xRotation;
	}

	public void setyRotation(double yRotation)
	{
		this.yRotation = yRotation;
	}

	public void setzRotation(double zRotation)
	{
		this.zRotation = zRotation;
	}
	
	public Attitude getCameraRotation()
	{
		return new Attitude(xRotation, yRotation, zRotation);
	}
}