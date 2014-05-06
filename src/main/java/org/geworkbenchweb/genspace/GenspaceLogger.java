/*
 * The geworkbench project
 * 
 * Copyright (c) 2007 Columbia University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.geworkbenchweb.genspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.events.AnalysisAbortEvent;
import org.geworkbench.events.AnalysisCompleteEvent;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.LogCompleteEvent;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;

/**
 * Captures all events with an associated {@link EventHandler} defined.
 * 
 * @author keshav
 * @version $Id: GenspaceLogger.java,v 1.1 2011/02/07 18:09:54 jsb2125 Exp $
 */
public class GenspaceLogger implements AnalysisSubmissionEventListener {

	private Log log = LogFactory.getLog(GenspaceLogger.class);
	private ObjectLogger objectLogger = new ObjectLogger();
	private ObjectHandler objectHandler = new ObjectHandler(objectLogger);
	private GenSpaceLogin_1 login;

	/**
	 * Intercept all events.
	 * 
	 * @param event
	 * @param source
	 * @throws Exception
	 */
	@Deprecated
	public void getEvent(Object event, Object source) throws Exception {
		if (event != null
				&& event.getClass().equals(AnalysisInvokedEvent.class)) {
			log.info("event: " + event.getClass().getSimpleName());

			objectHandler.handleLogging(event);
		}
		else if (event != null
				&& event.getClass().equals(AnalysisCompleteEvent.class)) {
			objectHandler.eventCompleted(((AnalysisCompleteEvent) event)
					.getInvokeEvent());
		}
		else if (event != null
				&& event.getClass().equals(AnalysisAbortEvent.class)) {
			objectHandler.eventAborted(((AnalysisAbortEvent) event)
					.getInvokeEvent());
		}
	}

	public ObjectLogger getObjectLogger() {
		return objectLogger;
	}
	
	public void setGenSpaceLogin(GenSpaceLogin_1 genSpaceLogin) {
		this.login = genSpaceLogin;
		this.objectLogger.setGenSpaceLogin(this.login);
		this.objectHandler.setGenSpaceLogin(this.login);
		//System.out.println("Login instance: " + this.login);
		//System.out.println("Check login in setGenSpaceLogin: " + this.login.getGenSpaceServerFactory().getUsername());
	}
	
	public GenSpaceLogin_1 getGenSpaceLogin() {
		return this.login;
	}

	@Override
	public void SubmitAnalysis(AnalysisSubmissionEvent event) {
		if (event != null
				&& event.getClass().equals(AnalysisSubmissionEvent.class)) {
			log.info("event: " + event.getClass().getSimpleName());
			objectHandler.handleLogging(event);
			
			if (login != null && login.getParent() != null) {
				//System.out.println("Check login instance: " + this.login);
				//System.out.println("Check from objectlogger: " + this.objectLogger.getGenSpaceLogin());
				//System.out.println("Check from objectHandler: " + this.objectHandler.getGenSpaceLogin());
				//System.out.println("Check which null: " + this.login.getGenSpaceServerFactory());
				//System.out.println("Check user id: " + this.login.getGenSpaceServerFactory().getUser().getId());
				GenSpaceWindow.getGenSpaceBlackboard().fire(new LogCompleteEvent(login.getGenSpaceServerFactory().getUser().getId()));
			}
		}		
	}

}
