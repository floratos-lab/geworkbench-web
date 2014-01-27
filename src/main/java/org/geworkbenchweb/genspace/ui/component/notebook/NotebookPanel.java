package org.geworkbenchweb.genspace.ui.component.notebook;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.XMLGregorianCalendar;

import org.geworkbench.components.genspace.server.stubs.AnalysisComment;
import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbenchweb.genspace.NotebookDataListener;
import org.geworkbenchweb.genspace.ui.component.AbstractGenspaceTab;
import org.geworkbenchweb.genspace.ui.component.FBAuthWindow;
import org.geworkbenchweb.genspace.ui.component.FBCommentWindow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.ui.component.GenSpaceTab;
import org.geworkbenchweb.utils.LayoutUtil;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.bibounde.vprotovis.PieChartComponent;
import com.bibounde.vprotovis.chart.pie.PieLabelFormatter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NotebookPanel extends AbstractGenspaceTab implements GenSpaceTab, NotebookDataListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9133747742064932811L;

	private final static SimpleDateFormat format = new SimpleDateFormat(
			"M/d/yy h:mm a");
	
	private final static SimpleDateFormat charFormatter = new SimpleDateFormat("yyyy-MM-dd");

	private final static List<String> sortByOpts = Arrays.asList(new String[] {
			"", "Sort by tool", "Sort by date", "Sort by user" });
	
	private final static int evtBefore = -30;

	private BorderLayout borderLayout;
	private VerticalLayout sortArea = new VerticalLayout();
	private Table table = new Table();
	private Label infoLabel = new Label(
			"Please login to genSpace to access this area.");
	
	private TextField searchBox = new TextField("Filter: ");
	private Button searchButton = new Button("Search");

	private Select dropdown;
	private Select sortByDropdown = new Select("Sort:", sortByOpts);
	private Button researchStats = new Button("Research Comparison");
	private Button fbAuth = new Button("Login Facebook");
	private Label fbUser = new Label();

	public String searchTerm = null;
	public String sortByMethod = null;
	private String endLine = System.getProperty("line.seperator");
	private String myName = null;
	
	private ThemeResource secret = new ThemeResource("img/secret.png");

	public static Date convertToDate(XMLGregorianCalendar cal) {
		return DatatypeConverter.parseDateTime(cal.toXMLFormat()).getTime();
	}

	@SuppressWarnings("serial")
	public NotebookPanel(final GenSpaceLogin_1 login) {
		super(login);
		
		searchBox.setInputPrompt("Enter your search query here " +
				"or use the dropdown below");
		searchBox.setWidth("350px");
		
		fbUser.setContentMode(ContentMode.HTML);

		GridLayout searchPanel = new GridLayout(5, 1);
		searchPanel.setSpacing(true);
		searchPanel.addComponent(searchBox, 0, 0);
		searchPanel.setComponentAlignment(searchBox, Alignment.BOTTOM_LEFT);
		searchPanel.addComponent(searchButton, 1, 0);
		searchPanel.setComponentAlignment(searchButton, Alignment.BOTTOM_RIGHT);
		searchPanel.addComponent(researchStats, 2, 0);
		searchPanel.setComponentAlignment(researchStats, Alignment.BOTTOM_RIGHT);
		searchPanel.addComponent(fbAuth, 3, 0);
		searchPanel.setComponentAlignment(fbAuth, Alignment.BOTTOM_RIGHT);
		searchPanel.addComponent(fbUser, 4, 0);
		searchPanel.setComponentAlignment(fbUser, Alignment.BOTTOM_RIGHT);
		
		searchButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				String query = (String) searchBox.getValue();
				setSearchTerm(query);
				List<AnalysisEvent> searchQueryList = login.getGenSpaceServerFactory().getPrivUsageFacade().getMyNotes(searchTerm,
								sortByMethod); // same
				List<AnalysisEvent> friendQueryList = login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEventsSortOn(searchTerm, sortByMethod);
				
				searchQueryList.addAll(friendQueryList);
				
				Collections.sort(searchQueryList, EventSorter.getDateSorter());
				
				displayTable(searchQueryList);
			}
		});
		
		researchStats.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent evt) {
				String convertString = convertQueryString();
				Window comparisonWindow = new Window("Research Comparsion: Since " + convertString);
				HorizontalLayout hLayout = new HorizontalLayout();
				
				comparisonWindow.setContent(hLayout);
				
				Panel myPanel = new Panel("My Tool Usage");
				Panel friendPanel = new Panel("Friends' Tool Usage");
				//List<AnalysisEvent> searchEvents = login.getGenSpaceServerFactory().getPrivUsageFacade().getMyNotes("", "");
				List<AnalysisEvent> searchEvents = login.getGenSpaceServerFactory().getPrivUsageFacade().getMyNotesByDate(convertString);
				List<AnalysisEvent> friendsEvents = login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEvents(convertString);
				
				PieChartComponent myPie = createPieChart(searchEvents);
				PieChartComponent friendPie = createPieChart(friendsEvents);
				
				myPanel.setContent(LayoutUtil.addComponent(myPie));
				friendPanel.setContent(LayoutUtil.addComponent(friendPie));
				
				hLayout.addComponent(myPanel);
				hLayout.addComponent(friendPanel);
				UI.getCurrent().addWindow(comparisonWindow);
			}
			
			public String convertQueryString() {
				Date currentDate = new Date();
				Calendar d = Calendar.getInstance();
				d.setTime(currentDate);
				d.add(Calendar.DATE, evtBefore);
				String queryLimit = charFormatter.format(d.getTime());
				// System.out.println("DEBUG queryLimit in research notebook: " + queryLimit);
				
				return queryLimit;
			}
			
			private PieChartComponent createPieChart(List<AnalysisEvent> evtList) {
				//Date nowDate = new Date();
				HashMap<String, Integer> analMap = new HashMap<String, Integer>();
				PieChartComponent pie = new PieChartComponent();

				Iterator<AnalysisEvent> eIT = evtList.iterator();
				AnalysisEvent tmp;
				//Date tmpDate;
				while(eIT.hasNext()) {
					tmp = eIT.next();
					
					if (analMap.containsKey(tmp.getToolname())) {
						int curValue = analMap.get(tmp.getToolname()).intValue();
						analMap.put(tmp.getToolname(), curValue + 1);
					} else {
						analMap.put(tmp.getToolname(), 1);
					}
				}
				
				Iterator<String> mapIT = analMap.keySet().iterator();
				while(mapIT.hasNext()) {
					String tmpKey = mapIT.next();
					// System.out.println("Test my analysis: " + tmpKey + " " + analMap.get(tmpKey));
					pie.addSerie(tmpKey, analMap.get(tmpKey).intValue());
				}
				
				final int total = evtList.size();
				
				pie.setChartWidth(450);
				pie.setChartHeight(300);
				
				pie.setMarginLeft(40);
				pie.setMarginTop(40);
				pie.setMarginRight(40);
				pie.setMarginBottom(40);
				
				pie.setLegendVisible(true);
				pie.setLegendAreaWidth(150);
				
				pie.setTooltipEnabled(true);
				
				pie.setLabelVisible(true);
				
				PieLabelFormatter labelFormatter = new PieLabelFormatter() {
					public boolean isVisible(double labelValue) {
						return 0.05 < labelValue/total;
					}
					
					public String format(double labelValue) {
						int percent = Double.valueOf(labelValue/total * 100).intValue();
						return percent + "%";
					}
				};
				
				pie.setLabelColor("#FFFFFF");
				return pie;
			}
			
			
		});
		
		fbAuth.addClickListener(new Button.ClickListener() {
			public void buttonClick(Button.ClickEvent evt) {
				FBAuthWindow authWindow= new FBAuthWindow(login, fbUser);
				UI.getCurrent().addWindow(authWindow);
			}
		});

		List<Tool> toolStrings = login.getGenSpaceServerFactory().getUsageOps()
				.getAllTools();
		String[] toolNames = new String[toolStrings.size()];
		for (int i = 0; i < toolStrings.size(); i++) {
			String name = toolStrings.get(i).getName();
			toolNames[i] = name != null ? name : "";
		}
		dropdown = new Select("Tools:", Arrays.asList(toolNames));
		dropdown.setImmediate(true);
		dropdown.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				setSearchTerm((String) dropdown.getValue());
				List<AnalysisEvent> searchEvents = login.getGenSpaceServerFactory()
						.getPrivUsageFacade().getMyNotes(searchTerm,
								sortByMethod);
				List<AnalysisEvent> friendQueryList = login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEventsSortOn(searchTerm, sortByMethod);
				
				searchEvents.addAll(friendQueryList);
				Collections.sort(searchEvents, EventSorter.getDateSorter());
				displayTable(searchEvents);
			}

		});

		sortByDropdown.setImmediate(true);
		sortByDropdown.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				String value = (String) sortByDropdown.getValue();
				if (sortByOpts.indexOf(value) == 1) {
					setSortBy("tool");
					List<AnalysisEvent> searchEvents = login.getGenSpaceServerFactory()
							.getPrivUsageFacade().getMyNotes(searchTerm,
									sortByMethod);
					
					List<AnalysisEvent> friendEvents = login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEventsSortOn(searchTerm, sortByMethod);
					
					searchEvents.addAll(friendEvents);
					Collections.sort(searchEvents, EventSorter.getToolSorter());
					displayTable(searchEvents);
				}
				if (sortByOpts.indexOf(value) == 2) {
					setSortBy("date");
					List<AnalysisEvent> searchEvents = login.getGenSpaceServerFactory()
							.getPrivUsageFacade().getMyNotes(searchTerm,
									sortByMethod);
					
					List<AnalysisEvent> friendEvents = login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEventsSortOn(searchTerm, sortByMethod);
					
					searchEvents.addAll(friendEvents);
					Collections.sort(searchEvents, EventSorter.getDateSorter());
					displayTable(searchEvents);
				}
				if (sortByOpts.indexOf(value) == 3) {
					setSortBy("date");
					List<AnalysisEvent> searchEvents = login.getGenSpaceServerFactory().getPrivUsageFacade().getMyNotes(searchTerm, sortByMethod);
					List<AnalysisEvent> friendEvents = login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEventsSortOn(searchTerm, sortByMethod);
					
					searchEvents.addAll(friendEvents);
					Collections.sort(searchEvents, EventSorter.getUserSorter());
					displayTable(searchEvents);
				}
			}
		});

		HorizontalLayout dropdowns = new HorizontalLayout();
		dropdowns.setSpacing(true);
		dropdowns.addComponent(dropdown);
		dropdowns.addComponent(sortByDropdown);

		sortArea.setHeight("100px");
		sortArea.setSpacing(true);
		sortArea.addComponent(searchPanel);
		sortArea.addComponent(dropdowns);

		// table.setPageLength(6);
		table.addContainerProperty("My Log:", Component.class, null);
		// table.setVisibleColumns(new Object [] {"column0"});
		table.setSizeFull();
		
		borderLayout = new BorderLayout();
		setCompositionRoot(borderLayout);
		borderLayout.addComponent(infoLabel, BorderLayout.Constraint.NORTH);
	}

	public void displayTable(List<AnalysisEvent> searchEvents) {
		table.removeAllItems();
		for (int i = 0; i < searchEvents.size(); i++) {
			final AnalysisEvent e = searchEvents.get(i);
			final String evtTime = format.format(convertToDate(e.getCreatedAt()));
			final String eUsrname = e.getTransaction().getUser().getUsername();
			
			/*Label noteInfo = new Label(e.getTool().getName() + " at "
					+ format.format(convertToDate(e.getCreatedAt())));*/
			Label noteInfo = new Label(e.getTool().getName() + " at "
					+ evtTime + " User: " + e.getTransaction().getUser().getUsername());
			Label dataSetName = new Label("Dataset: "
					+ e.getTransaction().getDataSetName());
			final TextArea noteText = new TextArea();
			noteText.setSizeFull();
			
			String noteValue = e.getNote();
			if (noteValue == null || noteValue.isEmpty()) {
				noteValue = "";
			}
			
			noteText.setValue(noteValue);
			noteText.setWordwrap(true);
			noteText.setImmediate(true);
			//final String originalNote = e.getNote();
			final String originalNote = noteValue;
			noteText.addValueChangeListener(new Property.ValueChangeListener() {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = 6686529869656235768L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					String value = (String) noteText.getValue();
					//e.setNote(value);
					//GenSpaceServerFactory.getPrivUsageFacade().saveNote(e);					
				}
			});
			
			VerticalLayout panel = new VerticalLayout();
			panel.setSpacing(true);
			panel.addComponent(noteInfo);
			panel.addComponent(dataSetName);
			panel.addComponent(noteText);
			
			HorizontalLayout buttonPanel = new HorizontalLayout();
			Button vParam = new Button("View Parameters");
			vParam.addClickListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				List<AnalysisEventParameter> aepList;
				String message = "";
				public void buttonClick(ClickEvent event){
					
					if (eUsrname.equals(myName)) {
						aepList = login.getGenSpaceServerFactory().getPrivUsageFacade().getAnalysisParameters(e.getId());
					} else {
						aepList = e.getParameters();
					}

					for(AnalysisEventParameter aep: aepList) {
						message = message + aep.getParameterKey() + ":" + aep.getParameterValue();
						message = message + "\n";
					}
					final Window paramWindow = new Window("Parameters for " + e.getToolname());
					UI.getCurrent().addWindow(paramWindow);
					VerticalLayout vLay = new VerticalLayout();
					vLay.addComponent(new Label(message));
					
					Button ok = new Button("OK");
					ok.addClickListener(new Button.ClickListener(){
						private static final long serialVersionUID = 1L;
						
						public void buttonClick(ClickEvent okEvent){
							message = "";
							UI.getCurrent().removeWindow(paramWindow);
						}
					});
					vLay.addComponent(ok);
					paramWindow.setContent(vLay);
				};
				
			});
			
			Button vComment = new Button("View Comments");
			vComment.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = 616778584176565336L;

				public void buttonClick(ClickEvent event) {
					List<AnalysisComment> commentList = login.getGenSpaceServerFactory().getPrivUsageFacade().getAnalysisEventComment(e.getId());
					CommentWindow cWindow = new CommentWindow(e, commentList, login);
					UI.getCurrent().addWindow(cWindow);
				}
			});
			
			Button cancel = new Button("Cancel");
			cancel.addClickListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event){
					e.setNote(originalNote);
					login.getGenSpaceServerFactory().getPrivUsageFacade().saveNote(e);
					noteText.setValue(e.getNote());
				}
			});
			
			Button save = new Button("Save");
			save.addClickListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event){
					String value = (String) noteText.getValue();
					e.setNote(value);
					login.getGenSpaceServerFactory().getPrivUsageFacade().saveNote(e);		
				}
			});
			
			Button fb = new Button("Facebook");
			fb.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent evt) {
					if (login.getFBManager() == null) {
						Notification.show("Plase login Facebook first.");
					} else {
						FBCommentWindow commentWindow = new FBCommentWindow(login, e, fbUser);
						UI.getCurrent().addWindow(commentWindow);
					}
				}
			});
			
			Button privNote = new Button("View Private Notes");
			privNote.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event) {
					Window privNoteWindow = new Window("My Private Notes: " + evtTime);
					privNoteWindow.setWidth("300px");
					privNoteWindow.setHeight("200px");
					BorderLayout bLayout = new BorderLayout();
					privNoteWindow.setContent(bLayout);
					
					VerticalLayout vLayout = new VerticalLayout();
					bLayout.addComponent(vLayout, BorderLayout.Constraint.CENTER);
					
					final TextArea ta = new TextArea();
					ta.setWordwrap(true);
					ta.setWidth("250");
					ta.setSizeFull();
					
					String privValue = e.getPrivNote();
					if (privValue == null) {
						privValue = "";
					}
					
					ta.setValue(privValue);
					

					GridLayout buttonLayout = new GridLayout(4,1);
					Button noteSave = new Button("Save");
					noteSave.addClickListener(new Button.ClickListener() {
						private static final long serialVersionUID = 1L;

						public void buttonClick(ClickEvent event) {
							String privNote = ta.getValue().toString();
							
							e.setPrivNote(privNote);
							login.getGenSpaceServerFactory().getPrivUsageFacade().savePrivNote(e);
						}
					});
					buttonLayout.addComponent(noteSave, 0, 0);
					
					Label emptyLabel = new Label();
					emptyLabel.setHeight("50px");
					emptyLabel.setWidth("10px");
					
					buttonLayout.addComponent(emptyLabel, 1, 0);
					
					Embedded sEmbedded = new Embedded(null, secret);
					buttonLayout.addComponent(sEmbedded, 3, 0);
					//buttonLayout.setComponentAlignment(sEmbedded, Alignment.MIDDLE_CENTER);
					
					vLayout.addComponent(ta);
					vLayout.setComponentAlignment(ta, Alignment.MIDDLE_CENTER);
					
					emptyLabel = new Label();
					emptyLabel.setHeight("20px");

					vLayout.addComponent(emptyLabel);
					
					vLayout.addComponent(buttonLayout);
					
					UI.getCurrent().addWindow(privNoteWindow);
				}
			});

			if (!eUsrname.equals(myName)) {
				noteText.setReadOnly(true);
				cancel.setEnabled(false);
				save.setEnabled(false);
				fb.setEnabled(false);
				privNote.setEnabled(false);
			}
			buttonPanel.setSpacing(true);
			buttonPanel.addComponent(vParam);
			buttonPanel.setComponentAlignment(vParam, Alignment.MIDDLE_CENTER);
			buttonPanel.addComponent(vComment);
			buttonPanel.setComponentAlignment(vComment, Alignment.MIDDLE_CENTER);
			buttonPanel.addComponent(privNote);
			buttonPanel.setComponentAlignment(privNote, Alignment.MIDDLE_CENTER);
			buttonPanel.addComponent(cancel);
			buttonPanel.setComponentAlignment(cancel, Alignment.MIDDLE_CENTER);
			buttonPanel.addComponent(save);
			buttonPanel.setComponentAlignment(save, Alignment.MIDDLE_CENTER);
			buttonPanel.addComponent(fb);
			buttonPanel.setComponentAlignment(fb, Alignment.MIDDLE_CENTER);
			panel.addComponent(buttonPanel);
			
			table.addItem(new Object[] { panel }, i);
		}
	}

	@Override
	public void tabSelected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loggedIn() {
		System.out.println("Reserach notebook logged in");
		borderLayout.removeComponent(infoLabel);
		borderLayout.addComponent(sortArea, BorderLayout.Constraint.NORTH);
		borderLayout.addComponent(table, BorderLayout.Constraint.CENTER);
		myName = login.getGenSpaceServerFactory().getUser().getUsername();
		updateFormFields();
	}

	@Override
	public void updateFormFields() {
		List<AnalysisEvent> searchEvents = login.getGenSpaceServerFactory()
				.getPrivUsageFacade().getMyNotes("", "");
	}

	@Override
	public void loggedOut() {
		borderLayout.removeComponent(sortArea);
		borderLayout.removeComponent(table);
		borderLayout.addComponent(infoLabel, BorderLayout.Constraint.NORTH);
	}

	public void setSearchTerm(String firstParam) {
		this.searchTerm = firstParam;
	}

	public void setSortBy(String secondParam) {
		this.sortByMethod = secondParam;
	}


}
