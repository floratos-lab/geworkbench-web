package org.geworkbenchweb.plugins;

import org.geworkbenchweb.utils.PagedTableView;

import com.vaadin.data.util.IndexedContainer;

/* class Analysis needs to be renamed to cover both analysis plugin and visualizer plugin */
public interface Tabular extends Visualizer{
	
    IndexedContainer getIndexedContainer();
    PagedTableView getPagedTableView();
    Long getUserId();    
    void setSearchStr(String search);
    void setPrecisonNumber(int precisonNumber);
    String getSearchStr();
}
