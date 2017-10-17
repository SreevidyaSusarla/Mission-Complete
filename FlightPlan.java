package aamc.flightPlan;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.map.GraphicsLayer;
import com.esri.map.Layer;
import aamc.AAMCOverlay;
import aamc.flightPlan.flightPlanItems.FlightLine;
import aamc.flightPlan.flightPlanItems.FlightPlanItem;
import aamc.flightPlan.flightPlanItems.RectangleSurveyArea;
import aamc.flightPlan.flightPlanItems.SurveyArea;
import aamc.flightPlan.flightPlanItems.Waypoint;
import renderers.PlanningRenderFactory;


public class FLightPlan extends AAMCOverlay{
	
	private static final long serialVersionUID = -7479005263154463514L;
	public static final int TOOL_SELECTED_NONE = 0;
	public static final int TOOL_SELECTED_WAYPOINT = 1;
	public static final int TOOL_SELECTED_FLIGHTLINE = 2;
	public static final int TOOL_SELECTED_SURVEY_POLY = 3;
	public static final int TOOL_SELECTED_SURVEY_RECT = 4;
	
	private final String[] PprzWaypoints = {"HOME", "GCS", "STDBY", "AF", "BUNGEE", "TD"};

	
	
	private ArrayList<FlightPlanItem> items;
	private int state = 0;
    private FlightPlanItem selectedItem = null;
    
    private JPopupMenu popup;
    private JPanel infoPanel;
    private JPanel toolPanel;
	private boolean isPprzFlightPlan = false;
   
    private class Observe implements Observer{
		@Override
		public void update(Observable o, Object arg) {
			//if an object has been passed, it's probably for deleting.
			if(arg != null)
			{
				items.remove(arg);
				selectedItem = null;
			}
			redraw();
		}
    }
    
    private Observe observer;
    
   
	public PlanningOverlay(){
		items = new ArrayList<FlightPlanItem>();
		popup = new JPopupMenu();
		infoPanel = new JPanel();
		observer = new Observe();
		this.setComponentPopupMenu(popup);


	}
	
	public JToolBar createToolbar()
	{
        
        JToolBar toolBar = new JToolBar("Flight Plan Editor");
        JPanel toolBarMenuBar = new JPanel();
        JMenuItem addWaypoint = new JMenuItem("Waypoint");
        JMenuItem addFlightLine = new JMenuItem("Flight Line");
        JMenu addSurveyArea = new JMenu("Survey Area");
        JMenuItem addSurveyRectangle = new JMenuItem("Rectangle Area");
        JMenuItem addSurveyPolygon = new JMenuItem("Polygon Area");
        JMenuItem generateBasic = new JMenuItem("Generate Basic Flight Plan");
        JMenuItem stopEditing = new JMenuItem("Stop Editing");
        
        JPanel infoPanel = new JPanel();
        
        toolBar.setVisible(false);
        toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.Y_AXIS));
        
        toolBarMenuBar.setLayout(new BoxLayout(toolBarMenuBar, BoxLayout.Y_AXIS));
        
        this.setName("Main Window");
        
        infoPanel.setPreferredSize(new Dimension(toolBar.getWidth(), 500));
        
        addSurveyArea.add(addSurveyRectangle);
        addSurveyArea.add(addSurveyPolygon);
        
        addWaypoint.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setEditState(PlanningOverlay.TOOL_SELECTED_WAYPOINT);
			}
        	
        });
        
        addFlightLine.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setEditState(PlanningOverlay.TOOL_SELECTED_FLIGHTLINE);
			}
        	
        });
        addSurveyPolygon.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setEditState(PlanningOverlay.TOOL_SELECTED_SURVEY_POLY);
			}
        	
        });
        addSurveyRectangle.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				setEditState(PlanningOverlay.TOOL_SELECTED_SURVEY_RECT);
				
			}
        	
        });
	    generateBasic.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				togglePprzBasicWaypoints();
			}
	    	
	    });
       
        toolBar.add(addWaypoint);
        toolBar.add(addFlightLine);
        toolBar.add(addSurveyPolygon);
        toolBar.add(generateBasic);
        toolBar.add(stopEditing);
        
        toolBar.add(infoPanel);
        
        toolBar.setVisible(true);
        return toolBar;
	}
	
	
	private void redraw()
	{
		for(Layer l : this.getMap().getLayers()){
			if(l==null)
			{
				System.out.println("Things have gone wrong");
				return;
			}
			if(l.getName().equals("Drawn Graphics"))
			{
				
				GraphicsLayer gl = (GraphicsLayer) l;
				gl.removeAll();
				for(FlightPlanItem current : items)
				{
					PlanningRenderFactory rf = new PlanningRenderFactory(current);
					
					for(Graphic graphic : rf.createGraphic())
					{
						gl.addGraphic(graphic);
					}
				}
				getInfoPanel();

				break;
			}
		}
	}
	

	@Override
	public void onMouseDragged(MouseEvent event)
	{
		java.awt.Point screenWayPoint = event.getPoint();
		com.esri.core.geometry.Point p = this.getMap().toMapPoint(screenWayPoint.x, screenWayPoint.y);

		if(selectedItem != null)
		{
			selectedItem.handleDragged(event, p);
		}
		else
		{
			super.onMouseDragged(event);
		}

		redraw();
	}
	
	@Override
	public void onMousePressed(MouseEvent event)
	{
		java.awt.Point screenWayPoint = event.getPoint();
		com.esri.core.geometry.Point p = this.getMap().toMapPoint(screenWayPoint.x, screenWayPoint.y);
		//if we aren't selecting anything, create a new something based on a selected tool
		if(selectedItem == null && state != TOOL_SELECTED_NONE)
		{
			if(state == TOOL_SELECTED_WAYPOINT)
			{
				selectedItem = new Waypoint(p);
			}
			else if(state == TOOL_SELECTED_FLIGHTLINE)
			{
				selectedItem = new FlightLine(p);
			}
			else if(state == TOOL_SELECTED_SURVEY_RECT)
			{
				selectedItem = new RectangleSurveyArea(p);
			}
			else if(state == TOOL_SELECTED_SURVEY_POLY)
			{
				selectedItem = new SurveyArea(p);
			}
			selectedItem.addObserver(observer);
			items.add(selectedItem);
			selectedItem.setState(FlightPlanItem.CREATE_MODE);	

			selectedItem.handlePressed(event, p);

			state = TOOL_SELECTED_NONE;
		}
		else if(selectedItem != null)
		{
			selectedItem.handlePressed(event, p);
		}
		else
		{
			super.onMousePressed(event);
		}
		for(FlightPlanItem mr: items)
		{
			if(mr.getState() == FlightPlanItem.CREATE_MODE || mr.getState() == FlightPlanItem.EDIT_MODE)
			{
				selectedItem = mr;
				return;
			}
			else if(mr.getState() == FlightPlanItem.SELECTED_MODE && mr.isSelected(p))
			{
				selectedItem = mr;
				return;
			}
		}
		selectedItem = null;
		super.onMousePressed(event);
	}
	
	@Override
	public void onMouseReleased(MouseEvent event) 
	{
		java.awt.Point screenWayPoint = event.getPoint();
		com.esri.core.geometry.Point p = this.getMap().toMapPoint(screenWayPoint.x, screenWayPoint.y);
		
		if(selectedItem != null)
		{
			selectedItem.handleReleased(event, p);
		}

		redraw();
		super.onMouseReleased(event);
	}
	

	
	public void setEditState(int state) {
		this.state = state;
		selectedItem = null;
	}
	
	public void getInfoPanel()
	{
		infoPanel.removeAll();
		toolPanel.removeAll();
		if(selectedItem != null)
		{
			toolPanel.add(selectedItem.getToolTip());
		}
		infoPanel.revalidate();
		infoPanel.repaint();
		toolPanel.revalidate();
		toolPanel.repaint();
	}

	public void setInfoPanel(JPanel infoPanel) {
		this.infoPanel = infoPanel;
	}

	public void addBasicPaparazziWaypoints() 
	{
		Point centerOfExtent = this.getMap().getExtent().getCenter();
		for(String s : this.PprzWaypoints)
		{
			Waypoint wp = new Waypoint(centerOfExtent);
			wp.setName(s);
			items.add(wp);
		}
		redraw();
	}


	public void removeBasicPaparazziWaypoints() {
		for(String s : this.PprzWaypoints)
		{
			for(FlightPlanItem item : items)
			{
				if(item instanceof Waypoint)
				{
					if(((Waypoint) item).getName().equals(s))
					{
						items.remove(item);
						break;
					}
				}
			}
		}
	}
	
	public void togglePprzBasicWaypoints() {
		if(!isPprzFlightPlan)
		{
			addBasicPaparazziWaypoints();
			isPprzFlightPlan = true;
		}
		else
		{
			removeBasicPaparazziWaypoints();
			isPprzFlightPlan = false;
		}
		
	}

	public void setToolInfoPanel(JPanel toolInformation) {
		this.toolPanel = toolInformation;
	}

	

	
}
