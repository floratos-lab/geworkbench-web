package org.geworkbenchweb.plugins.marina;

import java.util.List;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.SubSetOperations;

import com.vaadin.data.Property;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.ComboBox;

public class MicroarrayContext extends ComboBox {

	private static final long serialVersionUID = 6259881233062064390L;

	/* name used in preference only */
	private static final String ARRAYCONTEXT = "ArrayContext";

	private Long dataSetId;
	private long userId;

	private final String parentName; /* only used for preference */
	private boolean isArrayContextSetByApp = false;

	private final MicroarraySetSelect caseSelect;
	private final MicroarraySetSelect controlSelect;

	private final static ThemeResource infoIcon = new ThemeResource(
			"../custom/icons/icon_info.gif");

	public MicroarrayContext(Long dataSetId, Long userId, String parentName,
			MarinaUI parent, MicroarraySetSelect caseSetSelect,
			MicroarraySetSelect controlSetSelect) {
		super("\uFFFD    "+"Array Context");
		
		this.caseSelect = caseSetSelect;
		this.controlSelect = controlSetSelect;

		this.parentName = parentName;
		this.dataSetId = dataSetId;
		this.userId = userId;

		this.setWidth("140px");
		this.setImmediate(true);
		this.setNullSelectionAllowed(false);

		this.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;

			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

				Object val = MicroarrayContext.this.getValue();
				if (val != null) {
					Context context = (Context) val;
					List<SubSet> arraySubSets = SubSetOperations
							.getSubSetsForContext(context);

					caseSelect.reset(arraySubSets);
					controlSelect.reset(arraySubSets);

					if (!isArrayContextSetByApp)
						saveArrayContextPreference();
					isArrayContextSetByApp = false;

				}

			}
		});

//		this.setIcon(infoIcon);
		this.setDescription("The context of microarray sets.");
//		this.addStyleName("style_1");
	}

	public void setData(Long dataSetId, Long userId) {

		this.dataSetId = dataSetId;
		this.userId = userId;

		Context selectedArrayContext = null;
		Preference pref = PreferenceOperations.getData(dataSetId, parentName
				+ "." + ARRAYCONTEXT, userId);
		if (pref != null) {
			selectedArrayContext = (Context) ObjectConversion.toObject(pref
					.getValue());
		}
		if (selectedArrayContext == null)
			selectedArrayContext = SubSetOperations
					.getCurrentArrayContext(dataSetId);
		List<Context> contexts = SubSetOperations.getArrayContexts(dataSetId);
		this.removeAllItems();
		for (Context c : contexts) {
			this.addItem(c);
			if (selectedArrayContext != null
					&& c.getId().longValue() == selectedArrayContext.getId()
							.longValue()) {
				isArrayContextSetByApp = true;
				this.setValue(c);
			}
		}

	}

	private void saveArrayContextPreference() {

		Context markerContext = (Context) this.getValue();
		Preference p = PreferenceOperations.getData(dataSetId, parentName + "."
				+ ARRAYCONTEXT, userId);

		if (p != null)
			PreferenceOperations.setValue(markerContext, p);
		else
			PreferenceOperations.storeData(markerContext,
					Context.class.getName(), parentName + "." + ARRAYCONTEXT,
					dataSetId, userId);
	}
}
