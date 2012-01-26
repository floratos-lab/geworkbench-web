package org.geworkbenchweb.analysis.hierarchicalclustering.client.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.animation.Animatable;
import org.vaadin.gwtgraphics.client.shape.Rectangle;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VDendrogram extends Composite implements Paintable, ClickHandler, MouseDownHandler, MouseUpHandler, MouseMoveHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-dendrogram";

	public static final String CLICK_EVENT_IDENTIFIER = "click";


	public static final String TAGNAME = "Dendrogram";

	private static final int GRID_MAX_VAL_POS = 30;

	private static final int BAR_WIDTH = 100;

	private static final int BAR_SPACING = 100;

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	private AbsolutePanel panel;

	private DrawingArea canvas;

	private int width = 0;

	private int height = 0;

	private boolean verticalOrientation = true;

	private int animationDelay;

	private double max;

	private double min;

	private Map<Rectangle, Integer> initialHeights = new HashMap<Rectangle, Integer>();

	private Map<Rectangle, Rectangle> rectangleMap = new HashMap<Rectangle, Rectangle>();

	private Map<Rectangle, String> keyMap = new HashMap<Rectangle, String>();

	private Rectangle selectedBar = null;

	private ValueUpdater valueUpdater = new ValueUpdater();

	private boolean skipEvents = false;

	private boolean resizing = false;

	private boolean immediate;

	/** Component identifier in UIDL communications. */
	String uidlId;

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VDendrogram() {

		panel = new AbsolutePanel();
		initWidget(panel);

		canvas = new DrawingArea(width, height);
		setStyleName(CLASSNAME);

		DOM.setStyleAttribute(canvas.getElement(), "border", "0px solid black");
		
	}

	/**
	 * Called whenever an update is received from the server 
	 */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		// This call should be made first. Ensure correct implementation,
		// and let the containing layout manage caption, etc.
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		// Save the UIDL identifier for the component
		uidlId = uidl.getId();

		panel.clear();
		canvas.clear();
		initialHeights.clear();
		rectangleMap.clear();
		keyMap.clear();
		panel.add(canvas, 100, 100);
		selectedBar = null;

		immediate = uidl.getBooleanAttribute("immediate");
		verticalOrientation = uidl.getStringAttribute("orientation").equals(
				"vertical");
		animationDelay = uidl.getIntAttribute("animationdelay");
		max = uidl.getDoubleAttribute("gridmaxvalue");
		min = uidl.getDoubleAttribute("gridminvalue");
		uidl.getIntAttribute("gridmarklinecount");

		width = 850;
		height = 600;
		
		panel.setSize((50 + 1 + width) + "px", (25 + height) + "px");
		canvas.setWidth(width);
		canvas.setHeight(height);
		canvas.getElement().getStyle().setPropertyPx("width", width);
		canvas.getElement().getStyle().setPropertyPx("height", height);

		//drawGrid();

		final Iterator<?> options = uidl.getChildUIDL(0).getChildIterator();
		final Iterator<?> values = uidl.getChildUIDL(1).getChildIterator();
		int i = 0;
		while (options.hasNext()) {
			final UIDL optionUidl = (UIDL) options.next();
			final UIDL valueUidl = (UIDL) values.next();
			final String key = optionUidl.getStringAttribute("key");
			drawBar(i, key, optionUidl.getStringAttribute("caption"), valueUidl
					.getDoubleVariable("value_" + key), valueUidl
					.getStringAttribute("color"), optionUidl
					.hasAttribute("selected"));

			i++;
		}

		if (animationDelay > 0) {
			new FadeInAnimation().run(animationDelay);
		}

	}

	private int getBarCoord(int pos) {
		return BAR_SPACING + ((BAR_WIDTH + BAR_SPACING) * pos);
	}

	private void drawBar(int pos, String key, String caption, double value,
			String color, boolean selected) {
		int coord = getBarCoord(pos);
		int height;
		height = getPixelValue(value);

		Rectangle rect = null;
		if (verticalOrientation) {
			if (animationDelay > 0) {
				rect = new Rectangle(coord, this.height, BAR_WIDTH, 0);
			} else {
				rect = new Rectangle(coord, this.height - height, BAR_WIDTH,
						height);
			}
		} else {
			if (animationDelay > 0) {
				rect = new Rectangle(0, coord, 0, BAR_WIDTH);
			} else {
				rect = new Rectangle(0, coord, height, BAR_WIDTH);
			}
		}

		// TODO
		rect.addClickHandler(this);
		rect.addMouseDownHandler(this);
		rect.addMouseUpHandler(this);
		rect.addMouseMoveHandler(this);

		rect.getElement().setAttribute("shape-rendering", "crispEdges");
		rect.setFillColor(color);
		if (selected) {
			selectedBar = rect;
			selectedBar.setStrokeColor("red");
			selectedBar.setStrokeWidth(2);
		}
		canvas.add(rect);
		keyMap.put(rect, key);
		initialHeights.put(rect, height);

		Rectangle r = null;
		if (verticalOrientation) {
			r = new Rectangle(coord, this.height - height, BAR_WIDTH, 20);
		} else {
			r = new Rectangle(height - 20, coord, 20, BAR_WIDTH);
		}
		// TODO
		r.addClickHandler(this);
		r.addMouseDownHandler(this);
		r.addMouseUpHandler(this);
		r.addMouseMoveHandler(this);

		r.setFillOpacity(0);
		r.setStrokeOpacity(0);
		r.getElement().setAttribute("shape-rendering", "crispEdges");
		canvas.add(r);
		rectangleMap.put(r, rect);

		Label label = new Label(caption);
		if (verticalOrientation) {
			label.setStyleName("vchart-bottom-label");
			label.getElement().getStyle().setPropertyPx("width", BAR_WIDTH);
			panel.add(label, 150 + coord, this.height);
		} else {
			label.setStyleName("hchart-left-label");
			label.getElement().getStyle().setPropertyPx("width", 50);
			label.getElement().getStyle().setPropertyPx("height", BAR_WIDTH);
			panel.add(label, 0, coord);
		}

	}

	private class FadeInAnimation extends Animation {

		@Override
		protected void onUpdate(double progress) {

			for (Entry<Rectangle, Integer> e : initialHeights.entrySet()) {
				Rectangle rect = e.getKey();
				int barHeight = e.getValue();
				if (verticalOrientation) {
					updateValue(rect, "height", 0, barHeight, progress);
					updateValue(rect, "y", height, height - barHeight, progress);
				} else {
					updateValue(rect, "width", 0, barHeight, progress);
				}
				updateValue(rect, "fillopacity", 0, 1, progress);
			}
		}

		@Override
		protected void onStart() {
			skipEvents = true;
			super.onStart();

		}

		@Override
		protected void onComplete() {
			skipEvents = false;
			super.onComplete();
		}

		private void updateValue(Animatable target, String property,
				double startValue, double endValue, double progress) {
			double value = ((endValue - startValue) * progress + startValue);
			if (progress < 1.0 && property.equals("height")) {
				value++;
			}
			target.setPropertyDouble(property, value);
		}
	}

	public void onClick(ClickEvent event) {
		
	}

	public void onMouseDown(MouseDownEvent event) {
		if (skipEvents) {
			return;
		}
		Widget sender = (Widget) event.getSource();
		if (sender instanceof Rectangle && rectangleMap.containsKey(sender)) {
			DOM.setCapture(sender.getElement());
			resizing = true;
		}

	}

	public void onMouseUp(MouseUpEvent event) {
		DOM.releaseCapture(((Widget) event.getSource()).getElement());
		resizing = false;
	}

	public void onMouseMove(MouseMoveEvent event) {
		if (skipEvents || !resizing) {
			return;
		}
		int x = event.getRelativeX(canvas.getElement());
		int y = event.getRelativeY(canvas.getElement());
		Rectangle clickedRectangle = (Rectangle) event.getSource();
		double value = -1;
		if (verticalOrientation) {
			clickedRectangle.setY(y);
			rectangleMap.get(clickedRectangle).setY(y);
			rectangleMap.get(clickedRectangle).setHeight(height - y);
			value = getDoubleValue(rectangleMap.get(clickedRectangle)
					.getHeight());
		} else {
			clickedRectangle.setX(x);
			rectangleMap.get(clickedRectangle).setWidth(x + 20);
			value = getDoubleValue(rectangleMap.get(clickedRectangle)
					.getWidth());
		}
		valueUpdater.deferUpdateVariable("value_"
				+ keyMap.get(rectangleMap.get(clickedRectangle)), value);

	}

	private int getPixelValue(double value) {
		int size = verticalOrientation ? height : width;
		return (int) (value * ((size - GRID_MAX_VAL_POS) / (max - min)));
	}

	private double getDoubleValue(int barSize) {
		int size = verticalOrientation ? height : width;
		return barSize / ((size - GRID_MAX_VAL_POS) / (max - min));
	}

	private class ValueUpdater extends Timer {

		private String key;

		private double value;

		public void deferUpdateVariable(String key, double value) {
			cancel();
			this.key = key;
			this.value = value;
			schedule(200);
		}

		@Override
		public void run() {
			client.updateVariable(uidlId, key, value, immediate);

		}
	} 








}
