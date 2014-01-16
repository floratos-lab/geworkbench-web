package org.geworkbenchweb.plugins;

import com.vaadin.data.util.IndexedContainer;
import org.geworkbenchweb.utils.PagedTableView;

/* class Analysis needs to be renamed to cover both analysis plugin and visualizer plugin */
public interface Tabular extends Visualizer{
	
    IndexedContainer getIndexedContainer();
    PagedTableView getPagedTableView();
    Long getUserId();    
    void setSearchStr(String search);
    void setPrecisonNumber(int precisonNumber);
    String getSearchStr();
}
