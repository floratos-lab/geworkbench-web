package org.geworkbenchweb.layout;

import org.geworkbenchweb.GeworkbenchRoot;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class AboutInfo extends VerticalLayout {

	private static final long serialVersionUID = -93556097597644299L;

	@Override
	public void attach() {

		String releaseNumber = GeworkbenchRoot.getAppProperty("release.number");
		String releaseDate = GeworkbenchRoot.getAppProperty("release.date");
		String buildTimestamp = GeworkbenchRoot.getAppProperty("build.timestamp");
		String buildVersion = GeworkbenchRoot.getAppProperty("build.version");

		String text = String.format("<b>Current Release</b><ul><li>Release number: %s<br/><li>Release Date: %s"
				+ "<li>Build Timestamp: %s<li>Build version: %s"
				+ "</ul><b>Public project web site: </b><a href='http://www.geworkbench.org' target='_blank'>http://www.geworkbench.org</a>",
				releaseNumber, releaseDate, buildTimestamp, buildVersion);
		Label label = new Label(text, ContentMode.HTML);
		addComponent(label);
		this.setMargin(true);
	}
}
