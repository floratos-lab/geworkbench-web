//package genspace.ui;
package org.geworkbenchweb.genspace;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.geworkbench.components.genspace.server.stubs.FriendFacade;
import org.geworkbench.components.genspace.server.stubs.FriendFacadeService;
import org.geworkbench.components.genspace.server.stubs.NetworkFacade;
import org.geworkbench.components.genspace.server.stubs.NetworkFacadeService;
import org.geworkbench.components.genspace.server.stubs.PublicFacade;
import org.geworkbench.components.genspace.server.stubs.PublicFacadeService;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.ToolUsageInformation;
import org.geworkbench.components.genspace.server.stubs.ToolUsageInformationService;
import org.geworkbench.components.genspace.server.stubs.UsageInformation;
import org.geworkbench.components.genspace.server.stubs.UsageInformationService;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserFacade;
import org.geworkbench.components.genspace.server.stubs.UserFacadeService;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbench.components.genspace.server.stubs.WorkflowFolder;
import org.geworkbench.components.genspace.server.stubs.WorkflowRepository;
import org.geworkbench.components.genspace.server.stubs.WorkflowRepositoryService;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;


public class GenSpaceServerFactory {


	private User user;
	private String username = null;
	private String password = null;
	private String SERVER_ADDR = "http://" + RuntimeEnvironmentSettings.SERVER + ":8080";
	
	public Logger logger = Logger.getLogger(GenSpaceServerFactory.class);
	private GenSpaceLogin_1 login;
		
	private static Object lock = new Object();

	public void init()
	{
		loadTools();
	}
	
	public static Object readObject(byte[] data)
	{
		ObjectInputStream is;
		try {
			is = new ObjectInputStream(new ByteArrayInputStream(data));
			return is.readObject(); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] writeObject(Object o)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(o);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	
	public void setGenSpaceLogin(GenSpaceLogin_1 login)
	{
		this.login = login;
	}
	
	private void loadTools()
	{
		if(RuntimeEnvironmentSettings.tools == null)
		{
			RuntimeEnvironmentSettings.tools = new HashMap<Integer, Tool>();
				for(Tool t : getUsageOps().getAllTools())
				{
					RuntimeEnvironmentSettings.tools.put(t.getId(), t);
				}
		}
	}
	
	
	public void clearCache()
	{
	}
	
	public static void handleException(Exception e)
	{
		e.printStackTrace();
	}
	
	private void addCredentials(BindingProvider svc)
	{
		if(username != null && password != null)
		{
			((BindingProvider)svc).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
			((BindingProvider)svc).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
		}
	}
	
	public synchronized WorkflowRepository getWorkflowOps()
	{
		WorkflowRepository workflowFacade;
			loadTools();
			try {
				workflowFacade = (new WorkflowRepositoryService(new URL(SERVER_ADDR+"/WorkflowRepositoryService/WorkflowRepository?wsdl"),  new QName("http://server.genspace.components.geworkbench.org/", "WorkflowRepositoryService"))).getWorkflowRepositoryPort();
				addCredentials((BindingProvider) workflowFacade);
				return workflowFacade;

			} catch (MalformedURLException e) {
				return null;
			}
	}
	
	public synchronized ToolUsageInformation getUsageOps()
	{
		loadTools();
		ToolUsageInformation toolUsageFacade;
		try {
			toolUsageFacade = (new ToolUsageInformationService(new URL(SERVER_ADDR+"/ToolUsageInformationService/ToolUsageInformation?wsdl"),  new QName("http://server.genspace.components.geworkbench.org/", "ToolUsageInformationService"))).getToolUsageInformationPort();
			return toolUsageFacade;

		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public synchronized UserFacade getUserOps()
	{

		UserFacade userFacade;
		try {
			userFacade = (new UserFacadeService(new URL(SERVER_ADDR+"/UserFacadeService/UserFacade?wsdl"),  new QName("http://server.genspace.components.geworkbench.org/", "UserFacadeService"))).getUserFacadePort();
			addCredentials((BindingProvider) userFacade);
			return userFacade;
		} catch (MalformedURLException e) {
			return null;
		}
		
	}
	
	public synchronized PublicFacade getPublicFacade()
	{
		loadTools();
		try{
		PublicFacade publicFacade = (new PublicFacadeService(new URL(SERVER_ADDR+"/PublicFacadeService/PublicFacade?wsdl"),  new QName("http://server.genspace.components.geworkbench.org/", "PublicFacadeService"))).getPublicFacadePort();
		addCredentials((BindingProvider) publicFacade);
		return publicFacade;
		} catch(MalformedURLException e)
		{
			return null;
		}
	}
	
	public synchronized UsageInformation getPrivUsageFacade()
	{
		if(user == null)
			return null;
		loadTools();
		try
		{
		UsageInformation usageFacade = (new UsageInformationService(new URL(SERVER_ADDR+"/UsageInformationService/UsageInformation?wsdl"),  new QName("http://server.genspace.components.geworkbench.org/", "UsageInformationService"))).getUsageInformationPort();
			addCredentials((BindingProvider) usageFacade);
		return usageFacade;
		} catch (MalformedURLException e)
		{
			return null;
		}
	}
	
	public synchronized FriendFacade getFriendOps()
	{
		try{
			FriendFacade friendFacade = (new FriendFacadeService(new URL(SERVER_ADDR+"/FriendFacadeService/FriendFacade?wsdl"),  new QName("http://server.genspace.components.geworkbench.org/", "FriendFacadeService"))).getFriendFacadePort();
				addCredentials((BindingProvider) friendFacade);
				return friendFacade;
		} catch (MalformedURLException e)
		{
			return null;
		}
	}
	
	public synchronized NetworkFacade getNetworkOps()
	{
		try{
		NetworkFacade networkFacade = (new NetworkFacadeService(new URL(SERVER_ADDR+"/NetworkFacadeService/NetworkFacade?wsdl"),  new QName("http://server.genspace.components.geworkbench.org/", "NetworkFacadeService"))).getNetworkFacadePort();
		addCredentials((BindingProvider) networkFacade);
		return networkFacade;
		} catch (MalformedURLException e)
		{
			return null;
		}
	}
	
	public GenSpaceServerFactory() {
		super();
	}
	
	public WorkflowFolder rootFolder = null;
	
	public UserWrapper getWrappedUser() {
		return new UserWrapper(user, login);
	}
	
	public User getUser() {
		return user;
	}
	
	public boolean userRegister(User u) {
		try {
			user = getPublicFacade().register(u);
		} catch (Exception e) {
			//handleExecutionException(e);
			return false;
		}

		if(user != null)
			return true;
		return false;
	}
	
	public static String getObjectSize(Serializable s)
	{
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(s);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		 
		 return " " + ((double) baos.size())/(1024) + " KB";
	}

	public boolean userLogin(String username, String password) {
		synchronized(lock)
		{
			System.setProperty("java.security.auth.login.config", "login.conf");
			try {
				logout();
				this.username = username;				
				this.password = UserWrapper.getEncryptedPassword(password.toCharArray());	
				user = getUserOps().getMe();		
				return true;
			} 
			catch (Exception e) {
				return false;
			}
		}
	
	}
	public void updateCachedUser()
	{
		try {
			user = getUserOps().getMe();
		} catch (Exception e) {
			handleException(e);
		}
	}
	public boolean userUpdate() {

		try {
			getUserOps().updateUser(user);
		} catch (Exception e) {
			handleException(e);
			return false;
		}
		return true;
	}

	
	public boolean otherUserUpdate(User otheruser) {
		try {
			getUserOps().updateUser(otheruser);
		} catch (Exception e) {
			handleException(e);
		}
		return true;
	}

	public List<UserNetwork> getAllNetworks() {
		return getUserOps().getMyNetworks();
	}

	public String getUsername() {
		if(user == null)
			return null;
		return user.getUsername();
	}

	public boolean isLoggedIn() {
		return user != null;
	}

	public void logout() {
		rootFolder = null;
		username = null;
		password = null;
		user = null;
	}

	/**
	 * Returns if the currently logged in user may view the profile of the specified user
	 * @param user2
	 * @return
	 */
	public boolean isVisible(User user2) {
		if(user2.getUsername().equals(getUser().getUsername()))
			return true;
		return user2.isVisible();
	}
	

}
