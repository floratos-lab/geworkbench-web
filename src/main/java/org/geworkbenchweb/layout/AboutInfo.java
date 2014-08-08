/**
 * 
 */
package org.geworkbenchweb.layout;

import org.geworkbenchweb.GeworkbenchRoot;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author zji
 * 
 */
public class AboutInfo extends VerticalLayout {

	private static final long serialVersionUID = -93556097597644299L;

	@Override
	public void attach() {
		
		String releaseNumber = GeworkbenchRoot.getAppProperty("release.number");
		String releaseDate = GeworkbenchRoot.getAppProperty("release.date");
		
		String text = String.format("<b>Current Release</b><ul><li>Release number: %s<br/><li>Release Date: %s</ul><b>Public project web site: </b><a href='http://www.geworkbench.org' target='_blank'>http://www.geworkbench.org</a>",
				releaseNumber, releaseDate);
		Label label = new Label(text, Label.CONTENT_XHTML);
		addComponent(label);
		this.setMargin(true);
	}
}
