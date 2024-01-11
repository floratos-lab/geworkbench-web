package org.geworkbenchweb.layout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.server.ClassResource;
import com.vaadin.server.DownloadStream;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class QuickIntro extends VerticalLayout {

	private static final long serialVersionUID = -85557624336918038L;

	@Override
	public void attach() {
		String text = "Sorry, 'Quick Introduction' text is missing."; // default text

		DownloadStream downloadStream = new ClassResource("Intro.html").getStream();
		InputStream inputstream = downloadStream.getStream();
		StringBuilder sb = new StringBuilder();
		if (inputstream != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputstream));
			try {
				String line = br.readLine();
				while (line != null) {
					sb.append(line);
					line = br.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			text = sb.toString();
		}

		/*
		 * note this is not a full-blown HTML parser, only handling simple text and img
		 * tag
		 */
		Pattern imgPattern = Pattern.compile("\\\".+?\\\"");

		Pattern pattern = Pattern.compile("<img\\s.+?/>");
		Matcher matcher = pattern.matcher(text);

		int index = 0;
		while (matcher.find()) {
			int imgBegin = matcher.start();
			int imgEnd = matcher.end();
			Label label = new Label(text.substring(index, imgBegin), ContentMode.HTML);
			addComponent(label);

			Matcher m = imgPattern.matcher(matcher.group());
			if (m.find()) {
				ClassResource labelimage = new ClassResource(m.group().replace("\"", ""));
				Embedded image = new Embedded(null, labelimage);
				addComponent(image);
			}
			index = imgEnd;
		}

		this.setMargin(true);
	}
}
