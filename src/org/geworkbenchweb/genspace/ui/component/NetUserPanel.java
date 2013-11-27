package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.Network;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;
import org.geworkbenchweb.utils.LayoutUtil;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class NetUserPanel extends Panel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String netTitle;
	
	private GenSpaceLogin login;
	
	private Network networkFilter;
	
	private List<User> netUserList;
	
	public NetUserPanel(String netName, GenSpaceLogin login, Network networkFilter) {
		this.netTitle = netName;
		this.setCaption(netTitle);
		this.login = login;
		this.networkFilter = networkFilter;
		this.netUserList = login.getGenSpaceServerFactory().getNetworkOps().getProfilesByNetwork(networkFilter.getId());
		this.makeLayout();
	}
	
	private void makeLayout() {
		List<UserWrapper> lst = new ArrayList<UserWrapper>(this.netUserList.size());
		for(User u : this.netUserList)
		{
			lst.add(new UserWrapper(u, login));
		}
		lst.remove(new UserWrapper(login.getGenSpaceServerFactory().getUser(), login));
		Collections.sort(lst,new Comparator<UserWrapper>() {

			@Override
			public int compare(UserWrapper o1, UserWrapper o2) {
				return o1.compareTo(o2);
			}
		});
		
		Panel uwPanel;
		for (final UserWrapper uw: lst) {
			uwPanel = new Panel(uw.getUsername());
			uwPanel.setWidth("200px");
			Label orgPanel = new Label(uw.getLabAffiliation());
			uwPanel.setContent(LayoutUtil.addComponent(orgPanel));
			
			uwPanel.addClickListener(new ClickListener() {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void click(ClickEvent event) {
					// TODO Auto-generated method stub
					if (event.isDoubleClick()) {
						UserPanel uPan = new UserPanel(event.getComponent().getCaption() + "'s genSpace profile", uw.getDelegate());
						NetUserPanel.this.setContent(null);
						NetUserPanel.this.setContent(LayoutUtil.addComponent(uPan));
					}
				}
			});
			
			this.setContent(LayoutUtil.addComponent(uwPanel));
		}
		
	}
}
