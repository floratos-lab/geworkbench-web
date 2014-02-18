package org.geworkbenchweb.layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.CSVUtil;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

public class SaveSetListener implements Button.ClickListener {

	private static final long serialVersionUID = -5239131481750155527L;

	final private UMainLayout mainLayout;
	
	SaveSetListener(final UMainLayout mainLayout) {
		this.mainLayout = mainLayout;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		SetViewLayout setViewLayout = mainLayout.getSetViewLayout();
		if(setViewLayout==null) return;
		
		final Long selectedSetId = setViewLayout.getSelectedSetId();
		final Application application = mainLayout.getApplication();
		
		if (selectedSetId == null)
			return;
		final SubSet subSet = FacadeFactory.getFacade().find(SubSet.class,
				selectedSetId);

		User user = SessionHandler.get();
		String saveSetDir = System.getProperty("user.home") + "/temp/"
				+ user.getUsername() + "/savedSet/";
		if (!new File(saveSetDir).exists())
			new File(saveSetDir).mkdirs();
		String savefname = saveSetDir + subSet.getName() + ".csv";
		CSVUtil.saveSetToFile(savefname, subSet);

		FileResource resource = new FileResource(new File(savefname),
				application) {
			private static final long serialVersionUID = -4237233790958289183L;

			public DownloadStream getStream() {
				try {
					final DownloadStream ds = new DownloadStream(
							new FileInputStream(getSourceFile()),
							getMIMEType(), getFilename());
					ds.setParameter("Content-Disposition",
							"attachment; filename=\"" + getFilename() + "\"");
					ds.setCacheTime(0);
					return ds;
				} catch (final FileNotFoundException e) {
					e.printStackTrace();
					return null;
				}
			}
		};
		application.getMainWindow().open(resource);

	}

}
