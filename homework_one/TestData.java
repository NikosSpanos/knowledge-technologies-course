package org.example;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class TestData {

	static final String inputData  = "file:/home/nspanos/Documents/MScComputerScience/Fall2022/KnowledgeTechnologies/sesame-tutorial-new/culture_data.rdf";
	static final String dataUrlString = "file:/home/nspanos/Documents/MScComputerScience/Fall2022/KnowledgeTechnologies/sesame-tutorial-new/data-2-100.rdf";
	static final String inputData2  = "file:/home/nspanos/Dropbox/Kallikratis-Geonames.nt"; //CAREFULL. THIS IS A CUSTOM SPECIFIC PATH

	public static void main(String[] args) {

		try {
			//Create a new main memory repository
			MemoryStore store = new MemoryStore();
			Repository repo = new SailRepository(store);
			repo.initialize();

			//Store file
			try {

				URL file = new URL(inputData2);
				String fileBaseURI = "http://geo.linkedopendata.gr/gag/ontology/";
				RDFFormat fileRDFFormat = RDFFormat.NTRIPLES;

				RepositoryConnection con = repo.getConnection();
				try {
					//store the file
					con.add(file, fileBaseURI, fileRDFFormat);
					System.out.println("Repository loaded");
				}
				finally {
					con.close();
				}
			}
			catch (OpenRDFException e) {
				e.printStackTrace();
			}
			catch (java.io.IOException e) {
				// handle io exception
				e.printStackTrace();
			}

			//Sesame supports:
			//Tuple queries: queries that produce sets of value tuples.
			//Graph queries: queries that produce RDF graphs.
			//Boolean queries: true/false queries.

			//Evaluate a SPARQL tuple query
			try {
				RepositoryConnection con = repo.getConnection();
				try {

					//Give the official name and population of each municipality (δήμος) of Greece.
					String queryString1 =
							" PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>" +
					    " SELECT ?municipalityName ?municipalityPopulation " +
		          " WHERE { " +
			        "     ?municipalityGreece rdf:type gag:Δήμος ." +
			        "     ?municipalityGreece gag:έχει_επίσημο_όνομα ?municipalityName ;" +
			        "                         gag:έχει_πληθυσμό ?municipalityPopulation ." +
			        "}";

					//For each region (περιφέρεια) of Greece, give its official name, the official name of each
					//regional unit (περιφερειακή ενότητα) that belongs to it, and the official name of each
					//municipality (δήμος) in this regional unit. Organize your answer by region, regional unit
					//and municipality.
					String queryString2 =
							" PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>" +
							" SELECT ?regionOfficialName ?regionalUnitOfficialName ?municipalityOfficialName " +
							" WHERE { " +
							" 	?regionURI rdf:type gag:Περιφέρεια ." +
							"   ?regionUnitURI rdf:type gag:Περιφερειακή_Ενότητα ." +
							" 	?regionURI gag:έχει_επίσημο_όνομα ?regionOfficialName ." +
							"   ?regionUnitURI gag:ανήκει_σε ?regionURI ." +
							"	?regionUnitURI gag:έχει_επίσημο_όνομα ?regionalUnitOfficialName ." +
							"   {SELECT ?regionalUnitOfficialName ?municipalityOfficialName " +
							"    WHERE { " +
							"       ?regionUnitURI rdf:type gag:Περιφερειακή_Ενότητα ." +
							"       ?municipalityURI rdf:type gag:Δήμος . " +
							"       ?regionUnitURI gag:έχει_επίσημο_όνομα ?regionalUnitOfficialName ." +
							"	    ?municipalityURI gag:ανήκει_σε ?regionUnitURI ." +
							"	    ?municipalityURI gag:έχει_επίσημο_όνομα ?municipalityOfficialName ." +
							"      }" +
							"    }" +
							"} " +
							"  ORDER BY ASC(?regionOfficialName) ASC(?regionalUnitOfficialNames)";

					//For each municipality of the region Peloponnese with population more than 5,000 people,
					//give its official name, its population, and the regional unit it belongs to. Organize your
					//answer by municipality and regional unit.
					String queryString3 =
							" PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>" +
							" SELECT ?municipalityOfficialName ?regionalUnitOfficialName ?municipalityPopulation" +
							" WHERE { " +
							" 	?municipalityURI rdf:type gag:Δήμος ." +
							" 	?municipalityURI gag:έχει_επίσημο_όνομα ?municipalityOfficialName ." +
							" 	?municipalityURI gag:έχει_πληθυσμό ?municipalityPopulation ." +
							"   ?municipalityURI gag:ανήκει_σε ?regionalUnit ." +
							"	?regionalUnit gag:έχει_επίσημο_όνομα ?regionalUnitOfficialName ." +
							"   ?regionalUnit gag:ανήκει_σε ?region ."	+
							"   ?region gag:έχει_επίσημο_όνομα \"ΠΕΡΙΦΕΡΕΙΑ ΠΕΛΟΠΟΝΝΗΣΟΥ\" ." +
							"   FILTER (?municipalityPopulation > 5000) " +
							"} ORDER BY ASC(?municipalityPopulation) ASC(?regionalUnitOfficialName)";

					//For each municipality of Peloponnese for which we have no seat (έδρα) information in
					//the dataset, give its official name.
					String queryString4 =
							" PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>" +
							" SELECT ?municipalityOfficialName" +
							" WHERE { " +
							" 	?municipalityURI rdf:type gag:Δήμος ." +
							" 	?municipalityURI gag:έχει_επίσημο_όνομα ?municipalityOfficialName ." +
							"   ?municipalityURI gag:ανήκει_σε ?regionalUnit ." +
							"   ?regionalUnit gag:ανήκει_σε ?region ." +
							"   ?region gag:έχει_επίσημο_όνομα \"ΠΕΡΙΦΕΡΕΙΑ ΠΕΛΟΠΟΝΝΗΣΟΥ\" ." +
							"   FILTER NOT EXISTS { ?municipalityURI gag:έχει_έδρα ?hasSeat } " +
							"}";

					//For each municipality of Peloponnese, give its official name and all the administrative
					//divisions of Greece that it belongs to according to Kallikratis. Your query should be the
					//simplest one possible, and it should not use any explicit knowledge of how many levels
					//of administration are imposed by Kallikratis.
					String queryString5 =
							" PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>" +
							" SELECT ?municipalityOfficialName ?superclassNames" +
						    " WHERE " +
							" { " +
							"     { " +
							"         SELECT ?municipalityOfficialName (GROUP_CONCAT(?superclassName; separator=\", \") AS ?superclassNames) "+
							"         WHERE " +
							"         { " +
							" 	          ?municipalityURI rdf:type gag:Δήμος ." +
							" 	          ?municipalityURI gag:έχει_επίσημο_όνομα ?municipalityOfficialName ." +
							"             ?municipalityURI (gag:ανήκει_σε*) ?superclass ." +
							"             ?superclass gag:έχει_επίσημο_όνομα ?superclassName ." +
							"             FILTER( ?superclassName != ?municipalityOfficialName )" +
							"         } GROUP BY ?municipalityOfficialName ORDER BY ASC(?superclassName) " +
							"     } " +
							"     FILTER( REGEX(?superclassNames, \"ΠΕΡΙΦΕΡΕΙΑ ΠΕΛΟΠΟΝΝΗΣΟΥ\") )" +
							" } ORDER BY ASC(?municipalityOfficialName)";

					//For each region of Greece, give its official name, how many municipalities belong to it,
					//the official name of each regional unit (περιφερειακή ενότητα) that belongs to it, and how
					//many municipalities belong to that regional unit.
					String queryString6 =
							" PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>" +
							" SELECT ?regionName ?regionalUnitOfficialName ?NumberRegionMunicipalities (COUNT(?municipalityOfficialName) AS ?NumberRegionalUnitMunicipalities)" +
							" WHERE { " +
							" 	?regionURI rdf:type gag:Περιφέρεια ." +
							" 	?regionalURI rdf:type gag:Περιφερειακή_Ενότητα ." +
							" 	?regionURI gag:έχει_επίσημο_όνομα ?regionName ." +
							"   ?regionalURI gag:ανήκει_σε ?regionURI ." +
							"   ?regionalURI gag:έχει_επίσημο_όνομα ?regionalUnitOfficialName ." +
							" 	?municipality gag:ανήκει_σε ?regionalURI ." +
							"	?municipality gag:έχει_επίσημο_όνομα ?municipalityOfficialName ." +
							"	{ SELECT ?regionName (COUNT(?municipalityOfficialName) AS ?NumberRegionMunicipalities)" +
							"	  WHERE { " +
							" 	    ?regionURI rdf:type gag:Περιφέρεια ." +
							" 		?regionalURI rdf:type gag:Περιφερειακή_Ενότητα ." +
							" 		?regionURI gag:έχει_επίσημο_όνομα ?regionName ." +
							"   	?regionalURI gag:ανήκει_σε ?regionURI ." +
							"   	?regionalURI gag:έχει_επίσημο_όνομα ?regionalUnitOfficialName ." +
							" 		?municipality gag:ανήκει_σε ?regionalURI ." +
							"		?municipality gag:έχει_επίσημο_όνομα ?municipalityOfficialName ." +
							"	  } GROUP BY ?regionName" +
							"   }" +
							" } GROUP BY ?regionName ?regionalUnitOfficialName ?NumberRegionMunicipalities "+
							"   ORDER BY ASC(?regionName) ASC(?regionalUnitOfficialName)";

					//Check the consistency of the dataset regarding stated populations: the sum of the
					//populations of all administrative units A of level L must be equal to the population of the
					//administrative unit B of level L+1 to which all administrative units A belong to. (You
					//have to write one query only.)
					String queryString7 = "PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>" +
						    "SELECT ?child_name ?child_pop ?parent_name (SUM ( xsd:integer(?parent_pop)) AS ?total_parent_pop)"+
						    "WHERE " +
						    "{" +
						    "    ?child gag:ανήκει_σε ?parent; " +
							  "  		      gag:έχει_πληθυσμό ?child_pop;" +
							  "  		      gag:έχει_επίσημο_όνομα ?child_name ." +
							  "    ?parent gag:έχει_επίσημο_όνομα ?parent_name ;" +
						    "            gag:έχει_πληθυσμό ?parent_pop ." +
						    "} GROUP BY ?child_name ?child_pop ?parent_name"+
						    "  ORDER BY ASC(?parent_name) ";

					//verification for query 7
					String queryString7_1 = "PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>" +
							"SELECT ( SUM ( IF (?total_child_pop != ?total_parent_pop, 1, 0) ) as ?conditionCount)" +
						    "WHERE "+
						    "{ " +
						    "    {" +
						    "        SELECT ?parent_name ?total_parent_pop (SUM (?child_pop) AS ?total_child_pop) " +
						    "        WHERE " +
						    "        {"+
						    "            SELECT ?child_name ?child_pop ?parent_name (SUM ( xsd:integer(?parent_pop)) AS ?total_parent_pop)"+
						    "            WHERE " +
						    "            {" +

						    "                ?child gag:ανήκει_σε ?parent; " +
							  "  		                  gag:έχει_πληθυσμό ?child_pop;" +
							  "  		                  gag:έχει_επίσημο_όνομα ?child_name ." +

							  "                ?parent gag:έχει_επίσημο_όνομα ?parent_name ;" +
						    "                        gag:έχει_πληθυσμό ?parent_pop ." +

						    "            } GROUP BY ?child_name ?child_pop ?parent_name"+
						    "              ORDER BY ASC(?parent_name) " +

						    "        } GROUP BY ?parent_name ?total_parent_pop" +
							  "    }" +
						    "}";

					//Give the decentralized administrations (αποκεντρωμένες διοικήσεις) of Greece that
					//consist of more than two regional units. (You cannot use SPARQL 1.1 aggregate
					//operators to express this query.)
					String queryString8 =
						" PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>\n" +
					    " SELECT DISTINCT ?decentralizedAdminName" +
					    " WHERE { " +
					    "	?adminUri rdf:type gag:Αποκεντρωμένη_Διοίκηση . "+
					    "	?adminUri gag:έχει_επίσημο_όνομα ?decentralizedAdminName ." +
					    "	?region gag:ανήκει_σε ?adminUri . " +
					    "	?regionalUnit_1 gag:ανήκει_σε ?region . " +
					    "	?regionalUnit_1 gag:έχει_επίσημο_όνομα ?ru_1 . " +
					    "	?regionalUnit_2 gag:ανήκει_σε ?region . " +
					    "	?regionalUnit_2 gag:έχει_επίσημο_όνομα ?ru_2 . " +
					    "	?regionalUnit_3 gag:ανήκει_σε ?region . " +
					    "	?regionalUnit_3 gag:έχει_επίσημο_όνομα ?ru_3 . " +
					    "	FILTER(?regionalUnit_1 != ?regionalUnit_2 && ?regionalUnit_1 != ?regionalUnit_3 && ?regionalUnit_2 != ?regionalUnit_3)" +
					    "} ";

					//verification_query for query 8
					String queryString8_1 =
							" PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>\n" +
						    " SELECT DISTINCT ?decentralizedAdminName ?ru_1 ?ru_2 ?ru_3" +
						    " WHERE { " +
						    "	?adminUri rdf:type gag:Αποκεντρωμένη_Διοίκηση . "+
						    "	?adminUri gag:έχει_επίσημο_όνομα ?decentralizedAdminName ." +
						    "	?region gag:ανήκει_σε ?adminUri . " +
						    "	?regionalUnit_1 gag:ανήκει_σε ?region . " +
						    "	?regionalUnit_1 gag:έχει_επίσημο_όνομα ?ru_1 . " +
						    "	?regionalUnit_2 gag:ανήκει_σε ?region . " +
						    "	?regionalUnit_2 gag:έχει_επίσημο_όνομα ?ru_2 . " +
						    "	?regionalUnit_3 gag:ανήκει_σε ?region . " +
						    "	?regionalUnit_3 gag:έχει_επίσημο_όνομα ?ru_3 . " +
						    "	FILTER(?regionalUnit_1 != ?regionalUnit_2 && ?regionalUnit_1 != ?regionalUnit_3 && ?regionalUnit_2 != ?regionalUnit_3)" +
						    "} ";

			        String queryString = queryString1; //execute queries
			        System.out.println("Query:\n" + queryString);

					TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
					System.out.println("tupleQuery:\n" + tupleQuery);

					TupleQueryResult result = tupleQuery.evaluate();
					System.out.println("result.hasNext(): " + result.hasNext());

					if (result.hasNext() == false) {
						System.out.println("result has no next iterations");
					}

					//PrintWriter stdout = new PrintWriter(new FileWriter("/home/nspanos/Documents/MScComputerScience/Fall2022/KnowledgeTechnologies/results/queryString2_results.txt",false), true);
					try {
						//iterate the result set
						//stdout.println("START PRINTING RESULTS ---------------------------------------- \n");
						while (result.hasNext()) {
							BindingSet bindingSet = result.next();
							System.out.println(bindingSet.toString());
							//stdout.println(bindingSet.toString());
						}
						//stdout.println("\nFINISHED PRINTING RESULTS ---------------------------------------- \n");
					}
					catch (Exception e) {
						//handle exception
						e.printStackTrace();
					}
					finally {
						result.close();
						//stdout.close();
					}
				}
				catch (Exception e) {
					//handle exception
					e.printStackTrace();
				}
				finally {
					con.close();
				}
			}
			catch (OpenRDFException e) {
				// handle exception
				e.printStackTrace();
			}

		} catch (RepositoryException e) {
			// handle exception
			e.printStackTrace();
		}
	}
}
