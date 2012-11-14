package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="datasetannotation")
public class DataSetAnnotation extends AbstractPojo {
	private static final long serialVersionUID = 6362246241113491972L;
	private Long datasetid;
	private Long annotationid;
	
	public Long getDatasetId(){
		return datasetid;
	}
	public void setDatasetId(Long id){
		datasetid = id;
	}
	public Long getAnnotationId(){
		return annotationid;
	}
	public void setAnnotationId(Long id){
		annotationid = id;
	}
}
