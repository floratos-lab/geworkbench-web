package org.geworkbenchweb.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Authentication and Registration are handled here.
 *
 * @author Nikhil
 * @version $Id$
 */
public class UUserAuth extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(UUserAuth.class);
	
	public UUserAuth() {
		setSizeFull();
		addStyleName("background");

		this.addComponent(layout);
		layout.setSizeFull();
		this.setComponentAlignment(layout, Alignment.BOTTOM_CENTER);

		// loginForm must be built after switchToRegisterButton is ready
		loginForm		=	new LoginForm(switchToRegisterButton);
		
		layout.addComponent(loginForm);
		layout.setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);
	}

	final private LoginForm loginForm;

    final private Label aboutMessage = new Label();
	final private Button closeMessageButton = new Button("Close", new Button.ClickListener() {
		private static final long serialVersionUID = 8498615289854877295L;

        public void buttonClick(ClickEvent event) {
        	UUserAuth.this.toggleButtonText();
        }
    });


	// TODO why is the extra layer of outer layout necessary?
	final VerticalLayout layout 		= 	new VerticalLayout();
	
	RegistrationForm registrationForm = new RegistrationForm();
	Button switchToRegisterButton = new Button("Create New Account", new ClickListener() {

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(ClickEvent event) {
			layout.removeAllComponents();
			layout.addComponent(registrationForm);
		}
	});

	
	@Override
	public void attach() {
		super.attach();
		
		/* this needs to be done from attach() instead of constructor because it needs getApplication() */
		Panel aboutPanel = new Panel("About geWorkbench");
		aboutPanel.setStyleName("xpanel");

        VerticalLayout aboutWindowLayout = (VerticalLayout) aboutPanel.getContent();
        aboutWindowLayout.setMargin(true);
        aboutWindowLayout.setSpacing(true);
        
        DownloadStream downloadStream = new ClassResource("aboutMessage.html", getApplication()).getStream();
        InputStream inputstream = downloadStream.getStream();
        String text = "Sorry, 'About' text is missing."; // default text
        StringBuilder sb = new StringBuilder();
        if(inputstream!=null) {
	        BufferedReader br = new BufferedReader(new InputStreamReader (inputstream) );
			try {
				String line = br.readLine();
		        while(line!=null) {
		        	sb.append(line);
		        	line = br.readLine();
		        }
			} catch (IOException e) {
				e.printStackTrace();
			}
			text = sb.toString();
        }

        aboutMessage.setValue(text);
        aboutMessage.setContentMode(Label.CONTENT_XHTML);
        aboutPanel.addComponent(aboutMessage);

        aboutWindowLayout.addComponent(closeMessageButton);
        aboutWindowLayout.setComponentAlignment(closeMessageButton, Alignment.TOP_RIGHT);
        
        aboutPanel.setWidth("50%");
		loginForm.addComponent(aboutPanel);
		loginForm.setComponentAlignment(aboutPanel, Alignment.MIDDLE_CENTER);
		
		log.debug("about message is attached");
	}
	
    private void toggleButtonText() {
		if(closeMessageButton.getCaption().equals("Close")) {
			aboutMessage.setVisible(false);
			closeMessageButton.setCaption("Open");
		} else {
			aboutMessage.setVisible(true);
			closeMessageButton.setCaption("Close");
		}
	}

}
