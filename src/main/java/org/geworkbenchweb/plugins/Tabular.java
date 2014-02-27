package org.geworkbenchweb.plugins;

import com.vaadin.data.util.IndexedContainer;
import org.geworkbenchweb.utils.PagedTableView;

/* Shared interface between the two tabular viewers. */
public interface Tabular extends Visualizer{
	
    IndexedContainer getIndexedContainer();
    PagedTableView getPagedTableView();
    Long getUserId();    
    void setSearchStr(String search);
    void setPrecisonNumber(int precisonNumber);
    String getSearchStr();
}
