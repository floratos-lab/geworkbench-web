/**
 * 
 */
package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.Annotation;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

/**
 * GUI area to support uploading of affymatrix annotation file.
 * 
 */
public class AnnotationUploadLayout extends com.vaadin.ui.HorizontalLayout {

	private static final long serialVersionUID = 1173130742824781309L;

	private static Log log = LogFactory.getLog(AnnotationUploadLayout.class);

	final private Tree annotChoices = createAnnotChoices();
	final private ComboBox annotTypes;
	final private FileUploadLayout annotUploadLayout;

	public static enum Anno {
		NO("No annotation"), NEW("Load new annotation"), PUBLIC(
				"Public annotation files"), PRIVATE("Private annotations files"), DELETE(
				"Delete private annotation files");
		private String value;

		Anno(String v) {
			value = v;
		}

		public String toString() {
			return value;
		}
	};

	AnnotationUploadLayout(final UploadDataUI uploadDataUI) {
		annotUploadLayout = new FileUploadLayout(uploadDataUI, "annotation");
		annotUploadLayout.setVisible(false);

		this.addComponent(annotChoices);

		ArrayList<AnnotationType> atypes = new ArrayList<AnnotationType>();
		atypes.add(AnnotationType.values()[0]);
		atypes.add(AnnotationType.values()[1]);
		annotTypes = new ComboBox("Choose annotation file type", atypes);
		annotTypes.setNullSelectionAllowed(false);
		annotTypes.setWidth(200, 0);
		annotTypes.setValue(AnnotationType.values()[0]);
		annotTypes.setVisible(false);

		VerticalLayout rightSideLayout = new VerticalLayout();
		rightSideLayout.setSpacing(true);
		rightSideLayout.addComponent(annotTypes);
		rightSideLayout.addComponent(annotUploadLayout);
		this.addComponent(rightSideLayout);
	}

	Object getAnnotationChoice() {
		return annotChoices.getValue();
	}

	AnnotationType getAnnotationType() {
		Object obj = annotTypes.getValue();
		if (obj instanceof AnnotationType) {
			return (AnnotationType) obj;
		} else {
			log.error("invalid type of annotation: " + obj.getClass());
			return null;
		}
	}

	/*
	 * this returns non-null only if the annotation is chosen under public or
	 * private sections
	 */
	Anno getAnnotationChoiceGroup() {
		if (annotChoices.getValue() instanceof Anno) {
			log.warn("not valid group applicable");
			return null;
		} else {
			Object parent = annotChoices.getParent(annotChoices.getValue());
			if (parent instanceof Anno) {
				return (Anno) parent;
			} else {
				log.warn("invalid group of annotation choice: "
						+ parent.getClass());
				return null;
			}
		}
	}

	File getAnnotationFile() {
		return annotUploadLayout.getDataFile();
	}

	private Tree createAnnotChoices() {
		final Tree annotChoices = new Tree("Choose annotation");
		annotChoices.setNullSelectionAllowed(false);
		annotChoices.setWidth(220, 0);
		annotChoices.setImmediate(true);
		annotChoices.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 8744518843208040408L;

			public void itemClick(ItemClickEvent event) {
				if (event.getSource() == annotChoices) {
					Object choice = event.getItemId();
					if (choice != null) {
						if (choice == Anno.PUBLIC || choice == Anno.PRIVATE) {
							annotChoices.setSelectable(false);
							annotChoices.setValue(null);
						} else {
							annotChoices.setSelectable(true);
							annotChoices.setValue(choice);
						}
						if (choice == Anno.NEW)
							annotUploadLayout.setVisible(true);
						else
							annotUploadLayout.setVisible(false);
					}
				}
			}
		});

		annotChoices.addItem(Anno.NO);
		annotChoices.setChildrenAllowed(Anno.NO, false);

		annotChoices.addItem(Anno.NEW);
		annotChoices.setChildrenAllowed(Anno.NEW, false);

		annotChoices.addItem(Anno.PUBLIC);
		File dir = new File(GeworkbenchRoot.getPublicAnnotationDirectory());
		if (!dir.exists() || !dir.isDirectory()) {
			log.error("public annotation file directory missing or corrupted");
		} else {
			int cnt = 0;
			for (File f : dir.listFiles()) {
				if (f.isFile() && f.getName().endsWith(".csv")) {
					String fname = f.getName();
					annotChoices.addItem(fname);
					annotChoices.setParent(fname, Anno.PUBLIC);
					annotChoices.setChildrenAllowed(fname, false);
					cnt++;
				}
			}
			if (cnt == 0)
				annotChoices.setChildrenAllowed(Anno.PUBLIC, false);
		}

		annotChoices.addItem(Anno.PRIVATE);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("owner", SessionHandler.get().getId());
		List<Annotation> annots = FacadeFactory
				.getFacade()
				.list("Select a from Annotation as a where a.owner=:owner order by a.name",
						params);
		for (Annotation a : annots) {
			String aname = a.getName();
			annotChoices.addItem(aname);
			annotChoices.setParent(aname, Anno.PRIVATE);
			annotChoices.setChildrenAllowed(aname, false);
		}
		if (annots.isEmpty())
			annotChoices.setChildrenAllowed(Anno.PRIVATE, false);

		annotChoices.addItem(Anno.DELETE);
		annotChoices.setChildrenAllowed(Anno.DELETE, false);

		annotChoices.setValue(Anno.NO);
		return annotChoices;
	}

	public void cancelUpload() {
		annotUploadLayout.interruptUpload();
	}
}
