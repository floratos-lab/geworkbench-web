package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.Comment;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.CurrentContext;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class RemoveButtonListener implements ClickListener {

	private static final long serialVersionUID = -6293811142891534701L;
	private static final Log log = LogFactory.getLog(RemoveButtonListener.class);

	final private UMainLayout mainLayout;

	RemoveButtonListener(UMainLayout mainLayout) {
		this.mainLayout = mainLayout;
	}

	@Override
	public void buttonClick(ClickEvent event) {

		String to_be_deleted = null;
		final Long dataId = mainLayout.getCurrentDatasetId();
		final DataSet data = FacadeFactory.getFacade().find(DataSet.class, dataId);
		final ResultSet result = FacadeFactory.getFacade().find(ResultSet.class, dataId);
		if (data != null) {
			to_be_deleted = "data \"" + data.getName() + "\" and all the results under it";
		} else {
			if (result == null) {
				log.error("error in RemoveButtonListener"); // this should never happen
				return;
			}
			to_be_deleted = "result \"" + result.getName() + "\"";
		}

		MessageBox mbMain = new MessageBox(mainLayout.getWindow(),
				"Information",
				MessageBox.Icon.INFO,
				"This action will delete the selected " + to_be_deleted + ".",
				new MessageBox.ButtonConfig(ButtonType.CANCEL, "Cancel"),
				new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));

		mbMain.show(new MessageBox.EventListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClicked(ButtonType buttonType) {
				if (buttonType != ButtonType.OK) {
					return;
				}

				if (data != null) {

					Map<String, Object> params = new HashMap<String, Object>();
					params.put("parent", dataId);

					List<AbstractPojo> SubSets = FacadeFactory.getFacade()
							.list("Select p from SubSet as p where p.parent =:parent", params);
					if (SubSets.size() != 0) {
						for (int i = 0; i < SubSets.size(); i++) {
							FacadeFactory.getFacade().delete((SubSet) SubSets.get(i));
						}
					}

					Map<String, Object> param = new HashMap<String, Object>();
					param.put("parent", dataId);

					List<AbstractPojo> resultSets = FacadeFactory.getFacade()
							.list("Select p from ResultSet as p where p.parent =:parent", param);
					if (resultSets.size() != 0) {
						for (int i = 0; i < resultSets.size(); i++) {
							Map<String, Object> cParam = new HashMap<String, Object>();
							cParam.put("parent", ((ResultSet) resultSets.get(i)).getId());

							List<AbstractPojo> comments = FacadeFactory.getFacade()
									.list("Select p from Comment as p where p.parent =:parent", cParam);
							if (comments.size() != 0) {
								for (int j = 0; j < comments.size(); j++) {
									FacadeFactory.getFacade().delete((Comment) comments.get(j));
								}
							}
							FacadeFactory.getFacade().delete((ResultSet) resultSets.get(i));
							// delete resultset preference
							PreferenceOperations.deleteAllPreferences(((ResultSet) resultSets.get(i)).getId());
							mainLayout.removeItem(((ResultSet) resultSets.get(i)).getId());
						}
					}
					Map<String, Object> cParam = new HashMap<String, Object>();
					cParam.put("parent", dataId);

					List<AbstractPojo> comments = FacadeFactory.getFacade()
							.list("Select p from Comment as p where p.parent =:parent", cParam);
					if (comments.size() != 0) {
						for (int j = 0; j < comments.size(); j++) {
							FacadeFactory.getFacade().delete((Comment) comments.get(j));
						}
					}

					cParam.clear();
					cParam.put("datasetid", dataId);
					List<DataSetAnnotation> dsannot = FacadeFactory.getFacade()
							.list("Select p from DataSetAnnotation as p where p.datasetid=:datasetid", cParam);
					if (dsannot.size() > 0) {
						Long annotId = dsannot.get(0).getAnnotationId();
						FacadeFactory.getFacade().delete(dsannot.get(0));

						Annotation annot = FacadeFactory.getFacade().find(Annotation.class, annotId);
						if (annot != null && annot.getOwner() != null) {
							cParam.clear();
							cParam.put("annotationid", annotId);
							List<Annotation> annots = FacadeFactory.getFacade().list(
									"select p from DataSetAnnotation as p where p.annotationid=:annotationid",
									cParam);
							if (annots.size() == 0)
								FacadeFactory.getFacade().delete(annot);
						}
					}

					List<Context> contexts = SubSetOperations.getAllContexts(dataId);
					for (Context c : contexts) {
						cParam.clear();
						cParam.put("contextid", c.getId());
						List<SubSetContext> subcontexts = FacadeFactory.getFacade()
								.list("Select a from SubSetContext a where a.contextid=:contextid", cParam);
						FacadeFactory.getFacade().deleteAll(subcontexts);
						FacadeFactory.getFacade().delete(c);
					}

					cParam.clear();
					cParam.put("datasetid", dataId);
					List<CurrentContext> cc = FacadeFactory.getFacade()
							.list("Select p from CurrentContext as p where p.datasetid=:datasetid", cParam);
					if (cc.size() > 0)
						FacadeFactory.getFacade().deleteAll(cc);

					FacadeFactory.getFacade().delete(data);

				} else {
					Map<String, Object> cParam = new HashMap<String, Object>();
					cParam.put("parent", dataId);

					List<AbstractPojo> comments = FacadeFactory.getFacade()
							.list("Select p from Comment as p where p.parent =:parent", cParam);
					if (comments.size() != 0) {
						for (int j = 0; j < comments.size(); j++) {
							FacadeFactory.getFacade().delete((Comment) comments.get(j));
						}
					}
					FacadeFactory.getFacade().delete(result);
					Long resultDataId = result.getDataId();
					if (resultDataId != null) {
						String dataType = result.getType();
						try {
							@SuppressWarnings("unchecked")
							Class<? extends AbstractPojo> resultClazz = (Class<? extends AbstractPojo>) Class
									.forName(dataType);
							AbstractPojo resultData = FacadeFactory.getFacade().find(resultClazz, resultDataId);
							FacadeFactory.getFacade().delete(resultData);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							log.error("result data not removed due to ClassNotFoundException " + e);
						}
					}
				}

				// delete dataset preference
				PreferenceOperations.deleteAllPreferences(dataId);

				mainLayout.removeItem(dataId);
			}
		});

		mainLayout.noSelection();
	}
}
