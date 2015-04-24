package org.geworkbenchweb.plugins.tabularview;
 
import com.vaadin.terminal.ExternalResource; 
import com.vaadin.ui.Component; 
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.PopupView; 
import com.vaadin.ui.VerticalLayout;

public class PopupWindow implements PopupView.Content {
 
	private static final long serialVersionUID = 1L;
	private VerticalLayout root = new VerticalLayout();
    private final String geneName;
    public PopupWindow(final String geneName, final String entrezId) {
    	this.geneName = geneName;
        root.setSizeUndefined();
        root.setSpacing(true);
        root.setMargin(true);  
        if (!geneName.equals("---"))
        { 
        	Link l1 = new Link("Go to GeneCards",new ExternalResource("http://www.genecards.org/cgi-bin/carddisp.pl?gene="+geneName));
            l1.setTargetName("_blank");
            l1.setTargetBorder(Link.TARGET_BORDER_NONE);
            root.addComponent(l1);
            Link l2 = new Link("Go to Entrez",new ExternalResource("http://www.ncbi.nlm.nih.gov/gene/"+entrezId));
            l2.setTargetName("_blank");
            l2.setTargetBorder(Link.TARGET_BORDER_NONE);
            root.addComponent(l2);
        }
        else
        	root.addComponent(new Label("---"));
    }
 

    public Component getPopupComponent() {
        return root;
    }


	@Override
	public String getMinimizedValueAsHTML() {
		// TODO Auto-generated method stub
		return geneName;
	}
}
