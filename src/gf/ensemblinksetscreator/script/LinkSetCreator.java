package gf.ensemblinksetscreator.script;

import gf.ensemblinksetscreator.vocabulary.BridgeDbOPS;
import gf.ensemblinksetscreator.vocabulary.Dcat;
import gf.ensemblinksetscreator.vocabulary.Pav;
import gf.ensemblinksetscreator.vocabulary.Void;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.bio.DataSourceTxt;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DCTypes;
import com.hp.hpl.jena.vocabulary.RDF;

public class LinkSetCreator {
	private final static String IDENTIFIERS_ORG_PREFIX = "http://identifiers.org/";
	
	private static String taxon;
	private final static String taxonHuman = "http://purl.obolibrary.org/obo/NCBITaxon_9606";
	private final static String taxonMouse = "http://purl.obolibrary.org/obo/NCBITaxon_10090";

	private static String baseURI = "http://bridgedb.org/data/linksets/";
	private static String root = "/home/*/Downloads/test/";	
	private static String fileTSV = "/home/*/Downloads/test/Ensembl_Hs_dataset.void";
	
	private static String latinName;
	private final static String latinNameHuman = "homo_sapiens";
	private final static String latinNameMouse = "mus_musculus";
	private static String latinNamePath;
	private final static String latinNameHumanPath = "HomoSapiens";
	private final static String latinNameMousePath = "MusMusculus";
	private static String symbolName;
	private final static String symbolNameHuman = "Hs";
	private final static String symbolNameMouse = "Mm";
									
	public static void main(String[] args) throws ClassNotFoundException, IOException, IDMapperException {
		Date date = new Date();	
		
		
		if(args.length > 0)
			fileTSV = args[0];
		if (args.length > 1)
			root = args[1];
		
		taxon=taxonHuman;
		latinName=latinNameHuman;
		latinNamePath=latinNameHumanPath;
		symbolName=symbolNameHuman;
		root=root+latinNamePath+"/";
		baseURI=baseURI+latinNamePath+"/";
		fileTSV = fileTSV+symbolName+".tsv";
		
		DataSourceTxt.init();
		lsCreator();
		
		System.out.println("str: "+date);
		System.out.println("Reading file " + fileTSV);
		System.out.println("Outputing to folder " + root);
		System.out.println("");
		
		Date date2 = new Date();	
		System.out.println("end: "+date2);
	}
		
	public static void lsCreator() throws IOException, ClassNotFoundException, IDMapperException{
		BufferedReader br = new BufferedReader(new FileReader(fileTSV));
		HashMap<String,HashSet<HashMap<String,HashMap<String,HashSet<String>>>>> linkMap = 
				new HashMap<String,HashSet<HashMap<String,HashMap<String,HashSet<String>>>>>();

		try {	        
			String line = br.readLine();
			line = br.readLine();
			while (line != null) {	 
				String[] split=null;
				try{
					if (line.contains("mirbase.mature")){
						line = line.replaceAll("mirbase.mature", "mirbase");
					}
					split = line.split("\t");
				}
				catch(NullPointerException e){
					System.out.println(line);
				}
				
				String ref = split[0].replaceAll("\"", "");
				String related = split[1].replaceAll("[<>]", "");
				String xref = split[2].replaceAll("[<>]", "");
				int index = xref.lastIndexOf("/");

				if (index>0 && !line.contains(IDENTIFIERS_ORG_PREFIX+"go")){
					String tp = xref.substring(0, index);
					String db = tp.substring(tp.lastIndexOf("/")+1);
					DataSource ds = DataSource.getByIdentiferOrgBase(tp);
					if (db.equals("unigene"))
						ds =  DataSource.getExistingByFullName("UniGene");
					
					if ( !linkMap.containsKey(db) && ds!=null ) {
						HashMap<String, HashMap<String, HashSet<String>>> predicate = 
								new HashMap<String, HashMap<String, HashSet<String>>>();

						HashSet<HashMap<String, HashMap<String, HashSet<String>>>> database = 
								new HashSet<HashMap<String, HashMap<String, HashSet<String>>>>();

						HashMap<String,HashSet<String>> mapping = new HashMap<String,HashSet<String>>();

						HashSet<String> xrefSet = 	new HashSet<String>();
						
						xrefSet.add(xref);
						mapping.put(ref, xrefSet);
						predicate.put(related, mapping);
						database.add(predicate);
						linkMap.put(db, database);
					}
					else if (linkMap.containsKey(db) && ds!=null){
						HashSet<HashMap<String, HashMap<String, HashSet<String>>>> database = linkMap.get(db);
						for (HashMap<String, HashMap<String, HashSet<String>>> predicate : database ){
							if (predicate.containsKey(related)){
								HashMap<String, HashSet<String>> mapping = predicate.get(related);
								if (mapping.containsKey(ref))
									mapping.get(ref).add(xref);								
								else {
									HashSet<String> xrefSet = 	new HashSet<String>();
									xrefSet.add(xref);
									mapping.put(ref,xrefSet);
								}
							}
							else{
								HashMap<String,HashSet<String>> mapping = new HashMap<String,HashSet<String>>();
								HashSet<String> xrefSet = 	new HashSet<String>();
								xrefSet.add(xref);
								mapping.put(ref, xrefSet);
								predicate.put(related, mapping);
							}
						}
					}
				}
				line = br.readLine();
			}        
		} finally {
			br.close();
		}		
		
		Model voidDataset = modelVoid();
		Resource dataSet = createDataSetVoid(voidDataset);				
				
		for (Entry<String, HashSet<HashMap<String, HashMap<String, HashSet<String>>>>> database : linkMap.entrySet()){
			HashMap<Integer,Integer> dist = new HashMap<Integer,Integer>();
			for (HashMap<String, HashMap<String, HashSet<String>>> predicate : database.getValue()){
				for ( Entry<String, HashMap<String, HashSet<String>>> mapping : predicate.entrySet()){
					Model model = modelData();
					String term = mapping.getKey();
					String pred_term = term.substring(term.lastIndexOf("/")+1).toLowerCase();
					if (term.equals("")){
						term="http://www.w3.org/2004/02/skos/core#related";
						pred_term = term.substring(term.indexOf("#")+1);
					}
//					term = term.equals("") ? "http://www.w3.org/2004/02/skos/core#related" : mapping.getKey();
					for ( Entry<String, HashSet<String>> uri : mapping.getValue().entrySet()){
						Resource ref = model.createResource(uri.getKey().replaceAll("[<>]", ""));
						Property  pred = model.createProperty(term.replaceAll("[<>]", ""));	
						
//						if (uri.getValue().size()>1000 
////								&& uri.getValue().size()<20
//								)
//							System.out.println(database.getKey()+"\t"+ref+"\t"+uri.getValue().size());
						
						if ( !dist.containsKey(uri.getValue().size())) 							
						dist.put(uri.getValue().size(), 1);
						else
							dist.put(uri.getValue().size(), dist.get(uri.getValue().size()) + 1);
						
//						System.out.println("Database: "+database.getKey()+"\tNb"+uri.getValue().size());
						
						for (String xref : uri.getValue()){			
							if (xref.contains("unigene")){
								xref = xref.replaceAll("http://identifiers.org/unigene",
										"http://www.ncbi.nlm.nih.gov/unigene?term=");								
							}
							Resource externalIdentifierResource = model.createResource(xref);
							ref.addProperty(pred, externalIdentifierResource);
						}
					}
									
//					Model voidheader = modelFact();
					String fileURI = baseURI
								+"Ensembl_"+symbolName+"_"+database.getKey()+"."+pred_term+".LS.ttl";
					
					String filename = root
							+"Ensembl_"+symbolName+"_"+database.getKey()+"."+pred_term+".LS.ttl";
					
					
					Resource voidResource = createSpecificVoid(voidDataset,latinName, 
							database.getKey(),fileURI,term,dataSet);
					
					dataSet.addProperty(Void.subset, voidResource);
//					voidDataset.createResource(voidResource);
					
					File file = new File(filename);
					OutputStream ttl = new FileOutputStream(file);
					model.write(ttl,"TTL");					
					ttl.close();
					String gzname =filename+".gz";
					compressGzipFile(filename,gzname);
				}
			}
//			System.out.println(database.getKey() +"\n"+ dist);
		}
		String filename =root+ "Ensembl_"+symbolName+"_"+"dataset.void.ttl";
		File file = new File(filename);
		OutputStream ttl = new FileOutputStream(file);
		voidDataset.write(ttl,"TTL");
		ttl.close();
		String gzname =filename+".gz";
		compressGzipFile(filename,gzname);

	}
	
	public static Resource createSpecificVoid(Model model, String species, 
			String dataSource, String dump, String linkPredicate,Resource dataSet) 
					throws UnsupportedEncodingException {
		
		String dataSourceName = URLEncoder.encode(dataSource, "UTF-8");
        Resource bridgeDB = model.createResource("http://www.bridgedb.org/");
        Resource ensemblLicense = model.createResource("http://www.ensembl.org/info/about/legal/index.html");
        
        String pred_term = linkPredicate.substring(linkPredicate.lastIndexOf("/")+1);
		Resource specificResource = 
				model.createResource(species + "-ensembl-" + dataSource +"-"+pred_term.toLowerCase()+ "-linkset");	
        
		specificResource.addProperty(RDF.type, Void.Linkset);
		
		specificResource.addProperty(DCTerms.title,  
                model.createLiteral("Ensembl-" + dataSourceName + " Linkset for " + species,"en"));
		
		specificResource.addProperty(DCTerms.description, 
                model.createLiteral("A linkset which links Ensembl with "+dataSourceName+ 
                		" for the species " + species,"en"));
		
		specificResource.addProperty(DCTerms.publisher, bridgeDB);
		specificResource.addProperty(DCTerms.license, ensemblLicense);
		
		specificResource.addProperty(Void.dataDump,model.createResource(dump));
		
		Literal issuedLieteral = model.createTypedLiteral(new XSDDateTime(Calendar.getInstance()));
		specificResource.addProperty(DCTerms.issued, issuedLieteral);   
		
		specificResource.addProperty(Void.subjectsTarget,dataSet);
		
		DataSource ds = DataSource.getByIdentiferOrgBase(IDENTIFIERS_ORG_PREFIX+"ensembl");
		
		specificResource.addProperty(BridgeDbOPS.subjectsDatatype, 
				model.createResource(getType(ds.getType())));
		
		specificResource.addProperty(Void.objectsTarget, dataSet);// TODO 
		
		if (dataSource.equals("unigene"))
			ds =  DataSource.getExistingByFullName("UniGene");
		else
			ds = DataSource.getByIdentiferOrgBase(IDENTIFIERS_ORG_PREFIX+dataSource);
		
		specificResource.addProperty(BridgeDbOPS.objectsDatatype, 
				model.createResource(getType(ds.getType())));
        
		specificResource.addProperty(Void.linkPredicate, model.createResource(linkPredicate));
		
		specificResource.addProperty(BridgeDbOPS.linksetJustification, 
				model.createResource(getlinksetJustification(ds.getType()))); // TODO 
		
		specificResource.addProperty(BridgeDbOPS.assertionMethod,
				model.createResource("http://purl.obolibrary.org/obo/ECO_0000203"));
	
		specificResource.addProperty(BridgeDbOPS.subjectsSpecies,model.createResource(taxon));
		
		specificResource.addProperty(BridgeDbOPS.objectsSpecies,model.createResource(taxon));		
				
		specificResource.addProperty(Pav.createdBy, model.createResource("http://orcid.org/0000-0001-8624-2972"));
		specificResource.addProperty(Pav.createdWith,
				model.createResource("https://raw.githubusercontent.com/JonathanMELIUS/"
						+ "EnsemblLinksetsCreator/master/src/gf/ensemblinksetscreator/script/LinkSetCreator.java"));
		specificResource.addProperty(Pav.createdOn, issuedLieteral);
		
//        //Subset back to the species linkset and indirectly the rest
//        Resource speciesLinkset = model.createResource(species + "-linkset");
//        speciesLinkset.addProperty(Void.subset, specificResource);        
        return specificResource;
	}
	public static Resource createDataSetVoid(Model voidDataset){

		Calendar cal = Calendar.getInstance();
        cal.set(2014, 10, 2);
        Literal issuedLieteral = voidDataset.createTypedLiteral(new XSDDateTime(cal));

		Resource ensemblDataset = voidDataset.createResource("ensembl_"+latinName+"_rdf_dataset");
		Resource ensembl = voidDataset.createResource("http://www.ensembl.org/");
		Resource ensemblLicense = voidDataset.createResource("http://www.ensembl.org/info/about/legal/code_licence.html");
		Resource endpoint = voidDataset.createResource("http://wwwdev.ebi.ac.uk/rdf/services/ensembl/sparql");
		Literal uriSpace = voidDataset.createLiteral("http://rdf.ebi.ac.uk/resource/ensembl/");
			
		ensemblDataset.addProperty(RDF.type, DCTypes.Dataset);
		ensemblDataset.addProperty(DCTerms.title, 
				voidDataset.createLiteral("Ensembl Genome Database"));
 		ensemblDataset.addProperty(DCTerms.description,
 				voidDataset.createLiteral("The Ensembl project produces genome databases "
 						+ "for vertebrates and other eukaryotic species,"
 						+ " and makes this information freely available online."));
        ensemblDataset.addProperty(DCTerms.publisher, ensembl);
        ensemblDataset.addProperty(Dcat.landingPage, ensembl);
        ensemblDataset.addProperty(DCTerms.license, ensemblLicense);
        ensemblDataset.addProperty(DCTerms.issued, issuedLieteral);
        ensemblDataset.addProperty(Void.dataDump,
        voidDataset.createResource("ftp://ftp.ebi.ac.uk/pub/databases/RDF/ensembl/77/"+latinName+"_77.ttl"));
        
        ensemblDataset.addProperty(Pav.version, voidDataset.createLiteral("77"));
        
        ensemblDataset.addProperty(Void.sparqlEndpoint, endpoint);
        ensemblDataset.addProperty(Void.uriSpace, uriSpace);
        ensemblDataset.addProperty(Dcat.theme,
        		voidDataset.createResource("http://semanticscience.org/resource/SIO_010035"));
		return ensemblDataset;		
	}
	public static Model modelVoid(){
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("dcat", Dcat.NS);
		model.setNsPrefix("dcterms", DCTerms.NS);
        model.setNsPrefix("dctypes", DCTypes.NS);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("void", Void.NS);
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("bdb", BridgeDbOPS.NS);
        model.setNsPrefix("pav", Pav.NS);
        model.setNsPrefix("eco", "http://purl.obolibrary.org/obo/");
        model.setNsPrefix("ensemblterms", "http://rdf.ebi.ac.uk/terms/ensembl/");        
        model.setNsPrefix("sio", "http://semanticscience.org/resource/");
        model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        model.setNsPrefix("ensembl", "http://rdf.ebi.ac.uk/resource/ensembl/"); 
        return model;
	}
	public static Model modelData(){
		Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("ensemblterms", "http://rdf.ebi.ac.uk/terms/ensembl/");
        model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        model.setNsPrefix("ensembl", "http://rdf.ebi.ac.uk/resource/ensembl/"); 
        return model;
	}
	
	public static String getType(String type){
		String target="";
		if (type.equals("gene"))
			target ="http://semanticscience.org/resource/SIO_010035";
		if (type.equals("protein"))
			target = "http://semanticscience.org/resource/SIO_010043";
		if (type.equals("ontology"))
			target = "http://semanticscience.org/resource/SIO_001166";
		
		return target;		
	}
	public static String getlinksetJustification(String type){
		String justification="";

		if (type.equals("gene"))
			justification ="http://semanticscience.org/resource/SIO_010035";
		if (type.equals("protein"))
			justification = "http://semanticscience.org/resource/SIO_000985";
		if (type.equals("ontology"))
			justification = "http://semanticscience.org/resource/SIO_001166";
		return justification;
	}
	
	private static void compressGzipFile(String file, String gzipFile) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            gzipOS.close();
            fos.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }         
    }
}

