package org.geworkbenchweb.genspace.rating;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Label;



public class Star extends Label {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1764644076700819427L;

	private static ThemeResource fullStar = new ThemeResource("img/full.png");
	
	private static ThemeResource halfStar = new ThemeResource("img/half.png");

	private static ThemeResource emptyStar = new ThemeResource("img/empty.png");

	public static final int FULL = 1;
	public static final int HALF = 2;
	public static final int EMPTY = 3;

	private int value;
	
	public int getStarValue() {
		return value;
	}

	public void setStar(int star) {
		switch (star) {
		case FULL:
			setIcon(fullStar);
			break;
		case HALF:
			setIcon(halfStar);
			break;
		case EMPTY:
		default:
			setIcon(emptyStar);
			break;
		}
		this.requestRepaint();
	}

	public Star(StarRatingPanel panel, int value) {
		this.value = value;
		setStar(EMPTY);
	}
}
