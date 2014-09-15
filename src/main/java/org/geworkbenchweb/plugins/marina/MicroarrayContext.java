package org.geworkbenchweb.plugins.marina;

import java.util.List;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.SubSetOperations;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class MicroarrayContext extends HorizontalLayout {

	private static final long serialVersionUID = 6259881233062064390L;

	private static final String DESCRIPTION = "Microarray set context.";

	/* name used in preference only */
	private static final String ARRAYCONTEXT = "ArrayContext";

	private final ComboBox arrayContextCB;

	private Long dataSetId;
	private long userId;

	private final String parentName; /* only used for preference */
	private boolean isArrayContextSetByApp = false;

	private final MicroarraySetSelect caseSelect;
	private final MicroarraySetSelect controlSelect;

	public MicroarrayContext(Long dataSetId, Long userId, String parentName,
			MarinaUI parent, MicroarraySetSelect caseSetSelect,
			MicroarraySetSelect controlSetSelect) {
		this.caseSelect = caseSetSelect;
		this.controlSelect = controlSetSelect;

		this.parentName = parentName;
		this.dataSetId = dataSetId;
		this.userId = userId;

		arrayContextCB = new ComboBox();
		arrayContextCB.setWidth("140px");
		arrayContextCB.setImmediate(true);
		arrayContextCB.setNullSelectionAllowed(false);
		arrayContextCB.setDescription("The context of microarray sets.");

		arrayContextCB.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;

			public void valueChange(ValueChangeEvent event) {

				Object val = arrayContextCB.getValue();
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

		this.addComponent(new ParameterDescriptionButton(DESCRIPTION));
		Label caption = new Label("Array Context");
		caption.setWidth("80px");
		this.addComponent(caption);
		this.addComponent(arrayContextCB);
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
		arrayContextCB.removeAllItems();
		for (Context c : contexts) {
			arrayContextCB.addItem(c);
			if (selectedArrayContext != null
					&& c.getId().longValue() == selectedArrayContext.getId()
							.longValue()) {
				isArrayContextSetByApp = true;
				arrayContextCB.setValue(c);
			}
		}

	}

	private void saveArrayContextPreference() {

		Context markerContext = (Context) arrayContextCB.getValue();
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
