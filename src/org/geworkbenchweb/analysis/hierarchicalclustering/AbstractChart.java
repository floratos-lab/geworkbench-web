package org.geworkbenchweb.analysis.hierarchicalclustering;

import java.util.Iterator;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractSelect;

@SuppressWarnings( { "serial" })
public abstract class AbstractChart extends AbstractSelect {

	private boolean selectable = false;

	private String[] colors;

	private Object itemValuePropertyId = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.ui.AbstractSelect#paintContent(com.vaadin.terminal.PaintTarget
	 * )
	 */
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		if (selectable) {
			target.addAttribute("selectable", true);
		}
		String[] barColors = getColors();
		if (barColors == null || barColors.length == 0) {
			barColors = new String[] { "" };
		}
		target.startTag("optionvalues");
		@SuppressWarnings("rawtypes")
		final Iterator iter = getItemIds().iterator();
		int i = 0;
		while (iter.hasNext()) {
			final Object id = iter.next();
			target.startTag("so");
			target.addAttribute("color", barColors[i % barColors.length]);
			target.addVariable(this, "value_" + itemIdMapper.key(id),
					getItemValue(id));
			target.endTag("so");
			i++;
		}
		target.endTag("optionvalues");
	}

	public void setItemValuePropertyId(Object propertyId) {
		itemValuePropertyId = propertyId;
		requestRepaint();
	}

	public Object getItemValuePropertyId() {
		return itemValuePropertyId;
	}

	public double getItemValue(Object itemId) {
		return Double.parseDouble(getContainerDataSource().getItem(itemId)
				.getItemProperty(itemValuePropertyId).toString());
	}

	public void setColors(String[] colors) {
		this.colors = colors;
		requestRepaint();
	}

	public String[] getColors() {
		return colors;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public boolean isSelectable() {
		return selectable;
	}

}
