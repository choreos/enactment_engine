package org.ow2.choreos.chors;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ow2.choreos.chors.datamodel.Choreography;

@XmlRootElement
public class ChoreographyList {
	
	@XmlElement
	public List<Choreography> chors;
	
	public ChoreographyList() {
		// JAXB needs a no-args constructor
	}

}
