package gf.ensemblinksetscreator.vocabulary;

import org.bridgedb.DataSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

public class BridgeDbOPS {
	 /** <p>The RDF model that holds the vocabulary terms</p> */
    private static final Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://vocabularies.bridgedb.org/ops#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    
    public static final Property subjectsDatatype = m_model.createProperty("http://vocabularies.bridgedb.org/ops#subjectsDatatype");

    public static final Property objectsDatatype = m_model.createProperty("http://vocabularies.bridgedb.org/ops#objectsDatatype");
	
    public static final Property linksetJustification = m_model.createProperty("http://vocabularies.bridgedb.org/ops#linksetJustification");
	
    public static final Property assertionMethod = m_model.createProperty("http://vocabularies.bridgedb.org/ops#assertionMethod");
	
    public static final Property subjectsSpecies = m_model.createProperty("http://vocabularies.bridgedb.org/ops#subjectsSpecies");
	
    public static final Property objectsSpecies = m_model.createProperty("http://vocabularies.bridgedb.org/ops#objectsSpecies");
}
