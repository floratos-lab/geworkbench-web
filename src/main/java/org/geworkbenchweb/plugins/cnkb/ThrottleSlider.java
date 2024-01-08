package org.geworkbenchweb.plugins.cnkb;

import java.util.Vector;

import org.geworkbenchweb.pojos.CNKBResultSet;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.TextField;

import de.steinwedel.messagebox.MessageBox;

public class ThrottleSlider extends HorizontalLayout {

	private static final long serialVersionUID = -5012881594679568115L;

	private double minValue, maxValue;

	public ThrottleSlider(final CNKBResultSet resultSet, final double minValue, final double maxValue,
			final CNKBResultsUI parent) {

		this.minValue = minValue;
		this.maxValue = maxValue;

		Label thresholdLabel = new Label("Threshold:");
		final TextField tf = new TextField();
		tf.setValue(String.valueOf(minValue));
		final Slider horslider = new Slider();
		horslider.setOrientation(SliderOrientation.HORIZONTAL);
		horslider.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5645102449945182878L;

			public void valueChange(ValueChangeEvent event) {
				double value = (Double) horslider.getValue();
				Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
				for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
					cellularNetWorkElementInformation.setThreshold(value);

				}
				parent.dataTable.setContainerDataSource(parent.getIndexedContainer(resultSet));
				parent.updatePlot(resultSet, value, maxValue);

				tf.setValue(String.valueOf(value));

			}
		});
		horslider.setImmediate(true);

		horslider.setMin(minValue);
		horslider.setMax(maxValue);
		if (maxValue < 10)
			horslider.setResolution(2);
		horslider.setWidth(450, Unit.PIXELS);

		tf.addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 5679169940945634626L;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) tf.getValue();
				if (!isValidThreshold(value)) {
					MessageBox.createInfo().withCaption("Warning")
							.withMessage(
									"Please enter a number between " + minValue + " and " + Math.floor(maxValue) + ".")
							.withOkButton().open();
					return;
				}

				try {
					Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
					double thresholdValue = Double.parseDouble(value);
					for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
						cellularNetWorkElementInformation.setThreshold(thresholdValue);

					}
					parent.dataTable.setContainerDataSource(parent.getIndexedContainer(resultSet));
					parent.updatePlot(resultSet, thresholdValue, maxValue);
					horslider.setValue(thresholdValue);

				} catch (ValueOutOfBoundsException ex) {
					// do nothing
				}

			}
		});

		// Fire value changes immediately when the field loses focus
		tf.setImmediate(true);
		this.setSpacing(true);
		addComponent(thresholdLabel);
		addComponent(tf);
		addComponent(horslider);
		setExpandRatio(horslider, 1);
	}

	private boolean isValidThreshold(String value) {
		try {
			double d = Double.parseDouble(value);
			if (d < minValue || d > maxValue)
				return false;
			return true;
		} catch (NumberFormatException ne) {
			return false;
		}
	}

}
