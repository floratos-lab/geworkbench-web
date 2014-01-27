package org.geworkbenchweb.plugins.anova.results; 
 
import org.geworkbenchweb.plugins.TableMenuSelector;
import org.geworkbenchweb.plugins.Tabular;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.utils.LayoutUtil;
import org.geworkbenchweb.utils.PreferenceOperations;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;

 
public class AnovaTableMenuSelector extends TableMenuSelector {
	
	 
	private static final long serialVersionUID = 1809903375550657533L;
	
	

	public AnovaTableMenuSelector(Tabular tabular, String name)
	{
		super(tabular, name);
		createDisplayPreferenceItems(getDisplayPreferences());
		
		
	}
	
	@Override
	public void createDisplayPreferenceItems(MenuItem displayPreferences)
	{
	 
	 
		MenuBar.MenuItem selectColumnsItem = displayPreferences.addItem(
				"Select Columns", new Command() {

					private static final long serialVersionUID = 1L;

					@Override
					public void menuSelected(MenuItem selectedItem) {
						final Window displayPrefWindow = new Window();
						displayPrefWindow.setModal(true);
						displayPrefWindow.setClosable(true);
						displayPrefWindow.setWidth("320px");
						displayPrefWindow.setHeight("280px");
						displayPrefWindow.setResizable(false);
						displayPrefWindow.setCaption("Select Columns");
						displayPrefWindow.setImmediate(true);
						
						GridLayout gridLayout = new GridLayout(2, 4);
						gridLayout.setSpacing(true);
						gridLayout.setImmediate(true);
					 
						 
						AnovaTablePreferences p = ((AnovaResultsUI)getTabular()).getAnovaTablePreferences();
						final CheckBox checkbox1 = new CheckBox("Marker ID");
						checkbox1.setValue(p.selectMarker());
						checkbox1.setImmediate(true);
						final CheckBox checkbox2 = new CheckBox("Gene Symbol");
						checkbox2.setValue(p.selectGeneSymbol());
						checkbox2.setImmediate(true);
						final CheckBox checkbox3 = new CheckBox("P-value");
						checkbox3.setValue(p.selectPVal());
						checkbox3.setImmediate(true);
						final CheckBox checkbox4 = new CheckBox("F-statistic");
						checkbox4.setValue(p.selectFStat());
						checkbox4.setImmediate(true);
						final CheckBox checkbox5 = new CheckBox("Mean");
						checkbox5.setValue(p.selectMean());
						checkbox5.setImmediate(true);
						final CheckBox checkbox6 = new CheckBox("Standard Deviation");
						checkbox6.setValue(p.selectStd());
						checkbox6.setImmediate(true);
						
						Button checkAllButton = new Button("Check All",
								new Button.ClickListener() {

									private static final long serialVersionUID = 1L;

									@Override
									public void buttonClick(ClickEvent event) {
										  checkbox1.setValue(true);
										  checkbox2.setValue(true);
										  checkbox3.setValue(true);
										  checkbox4.setValue(true);
										  checkbox5.setValue(true);
										  checkbox6.setValue(true);
									}
								});
						checkAllButton.setStyleName(Reindeer.BUTTON_LINK);
						

						Button clearAllButton = new Button("Clear All",
								new Button.ClickListener() {

									private static final long serialVersionUID = 1L;

									@Override
									public void buttonClick(ClickEvent event) {
										  checkbox1.setValue(false);
										  checkbox2.setValue(false);
										  checkbox3.setValue(false);
										  checkbox4.setValue(false);
										  checkbox5.setValue(false);
										  checkbox6.setValue(false);
									}
								});
						clearAllButton.setStyleName(Reindeer.BUTTON_LINK);						
						
						gridLayout.addComponent(checkAllButton, 0, 0);
						gridLayout.addComponent(clearAllButton, 1, 0);
						gridLayout.addComponent(checkbox1, 0, 1);
						gridLayout.addComponent(checkbox2, 1, 1);
						gridLayout.addComponent(checkbox3, 0, 2);
						gridLayout.addComponent(checkbox4, 1, 2);
						gridLayout.addComponent(checkbox5, 0, 3);
						gridLayout.addComponent(checkbox6, 1, 3);
						 
						
						Button submit = new Button("Submit",
								new Button.ClickListener() {

									private static final long serialVersionUID = -4799561372701936132L;

									@Override
									public void buttonClick(ClickEvent event) {
										try {
											
											AnovaTablePreferences anovaTablePreferences = ((AnovaResultsUI)getTabular()).getAnovaTablePreferences();
											anovaTablePreferences.selectMarker(checkbox1.getValue());
											anovaTablePreferences.selectGeneSymbol(checkbox2.getValue());
											anovaTablePreferences.selectPVal(checkbox3.getValue());
											anovaTablePreferences.selectFStat(checkbox4.getValue());
											anovaTablePreferences.selectMean(checkbox5.getValue());
											anovaTablePreferences.selectStd(checkbox6.getValue());
											Preference p = PreferenceOperations
													.getData(getTabular().getDatasetId(),
															Constants.DISPLAY_CONTROL,
															getTabular().getUserId());
											if (p != null)
												PreferenceOperations.setValue(
														anovaTablePreferences, p);
											else
												PreferenceOperations.storeData(
														anovaTablePreferences,
														AnovaTablePreferences.class.getName(),
														Constants.DISPLAY_CONTROL,
														getTabular().getDatasetId(), getTabular().getUserId());

											getTabular().getPagedTableView()
													.setContainerDataSource(getTabular().getIndexedContainer());		 
															 
											UI.getCurrent().removeWindow(displayPrefWindow);
										    
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
						submit.setClickShortcut(KeyCode.ENTER);
						 
						VerticalLayout layout = LayoutUtil.addComponent(gridLayout);
						layout.addComponent(submit);
						displayPrefWindow.setContent(layout);
						UI.getCurrent().addWindow(displayPrefWindow);
					}
				});

		        selectColumnsItem.setStyleName("plugin");

		 

	 
	}
	
	 
	@Override
	public void createFilterWindow() {
		
		final Window filterWindow = new Window();
		filterWindow.setModal(true);
		filterWindow.setClosable(true);
		filterWindow.setWidth("300px");
		filterWindow.setHeight("200px");
		filterWindow.setResizable(false);
		filterWindow.setCaption("Filter");
		filterWindow.setImmediate(true);

		final OptionGroup og;
		og = new OptionGroup();
		og.setImmediate(true);
		og.addItem(Constants.ThresholdDisplayControl.show_all
				.ordinal());
		og.addItem(Constants.ThresholdDisplayControl.p_value
				.ordinal());
		og.addItem(Constants.ThresholdDisplayControl.f_statistic
				.ordinal());
		og.setItemCaption(
				Constants.ThresholdDisplayControl.show_all.ordinal(),
				"Show All");
		og.setItemCaption(
				Constants.ThresholdDisplayControl.p_value
						.ordinal(), "P-Value Threshold");
		og.setItemCaption(
				Constants.ThresholdDisplayControl.f_statistic.ordinal(),
				"F-Statistic Threshold");

		int thresholdControl = ((AnovaResultsUI)getTabular()).getAnovaTablePreferences().getThresholdControl();
		float thresholdValue= ((AnovaResultsUI)getTabular()).getAnovaTablePreferences().getThresholdValue();
		og.select(thresholdControl);

		final TextField threshold = new TextField();
		threshold.setWidth(200, Unit.PIXELS);
		threshold.setImmediate(true);
		if (thresholdControl == Constants.ThresholdDisplayControl.show_all.ordinal())
			threshold.setEnabled(false);
		else
		{
			threshold.setEnabled(true);
			threshold.setValue(Float.toString(thresholdValue));
		}
		
		og.addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				      threshold.setValue("");
					  if (og.getValue().equals(Constants.ThresholdDisplayControl.show_all.ordinal()) ) 
					  {
						  threshold.setEnabled(false);
						  
					  }
		              else if (og.getValue().equals(Constants.ThresholdDisplayControl.p_value.ordinal()))
		              {
		            	  threshold.setEnabled(true);		            	 
		            	  threshold.setInputPrompt("Please enter P-Value threshold");
		              }
		              else
		              { 
		            	  threshold.setEnabled(true);		            	  
	            	      threshold.setInputPrompt("Please enter F-Statistic threshold");		            	  
		              }
		              
			}
		}); 

		Button submit = new Button("Submit",
				new Button.ClickListener() {

					private static final long serialVersionUID = -4799561372701936132L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {							
							int thresholdControl = new Integer(og.getValue().toString());							 
							AnovaTablePreferences anovaTablePreferences = ((AnovaResultsUI)getTabular()).getAnovaTablePreferences();
							anovaTablePreferences.setThresholdControl(thresholdControl);
							if (thresholdControl == Constants.ThresholdDisplayControl.show_all.ordinal())
								anovaTablePreferences.setThresholdValue(0);
							else if (thresholdControl == Constants.ThresholdDisplayControl.p_value.ordinal())
							{
								Float value = getPvalThreshold(threshold.getValue());
								if (value == null)
								{
									MessageBox.showPlain(
											Icon.WARN,
											"Warning",
											"Please enter a number between 0 and 1 for P-value threshold.",
											ButtonId.OK);
									return;
								}
								anovaTablePreferences.setThresholdValue(value);
							}
							else
							{
								Float value = getFStatThreshold(threshold.getValue());
								if (value == null)
								{
									MessageBox.showPlain(
											Icon.WARN,
											"Warning",
											"Please enter non-negative number for F-statistic threshold.",
											ButtonId.OK);
									return;
								}
								anovaTablePreferences.setThresholdValue(value);
							}
								 
							Preference p = PreferenceOperations
									.getData(getTabular().getDatasetId(),
											Constants.DISPLAY_CONTROL,
											getTabular().getUserId());
							if (p != null)
								PreferenceOperations.setValue(
										anovaTablePreferences, p);
							else
								PreferenceOperations.storeData(
										anovaTablePreferences,
										AnovaTablePreferences.class.getName(),
										Constants.DISPLAY_CONTROL,
										getTabular().getDatasetId(), getTabular().getUserId());

							getTabular().getPagedTableView()
									.setContainerDataSource(getTabular().getIndexedContainer());		 
											 
							UI.getCurrent().removeWindow(filterWindow);
						    
					 
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
		submit.setClickShortcut(KeyCode.ENTER);
		VerticalLayout layout = LayoutUtil.addComponent(og);
		layout.addComponent(threshold);
		layout.addComponent(submit);
		filterWindow.setContent(layout);
		UI.getCurrent().addWindow(filterWindow);

	} 
	
	private Float getPvalThreshold(Object value)
	{
		if (value == null || value.toString().trim().equals(""))
		    return null;
		try
		{
		   float f = new Float(value.toString().trim());
		   if (f < 0 || f > 1)
		      return null;
		   else
			  return f;
		}catch (NumberFormatException nfe)
		{
			return null;
		}
	}
	
	private Float getFStatThreshold(Object value)
	{
		if (value == null || value.toString().trim().equals(""))
		    return null;
		try
		{
		   float f = new Float(value.toString().trim());
		   if ( f < 0 )
			  return null;
		   else
			  return f;
		}catch (NumberFormatException nfe)
		{
			return null;
		}
	}
	

}
