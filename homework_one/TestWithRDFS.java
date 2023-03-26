package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResult;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class TestWithRDFS {

	static final String inputData2  = "file:/home/nspanos/Documents/MScComputerScience/Fall2022/KnowledgeTechnologies/schemaorg-current-https.nt";

	public static void main(String[] args) {

		try {
			//Create a new main memory repository
			MemoryStore store = new MemoryStore();
			ForwardChainingRDFSInferencer inferencer = new ForwardChainingRDFSInferencer(store);

			Repository repo = new SailRepository(inferencer); //with inference
			//Repository repo = new SailRepository(store); //without inference
			repo.initialize();

			//Store files (one local and one available through http)
			URL data = new URL(inputData2);

			RDFFormat fileRDFFormat = RDFFormat.NTRIPLES;

			RepositoryConnection con = repo.getConnection();

			//store the files
			con.add(data, null, fileRDFFormat);

			System.out.println("Repository loaded");

			//Sesame supports:
			//Tuple queries: queries that produce sets of value tuples.
			//Graph queries: queries that produce RDF graphs
			//Boolean queries: true/false queries

			//------------------------------------------------------------------------------------------------------------
			// WITH INFERENCE
			// Find all subclasses of class CollegeOrUniversity
			String queryString1 =
					" PREFIX schema: <https://schema.org/>" +
					" SELECT ?subclassName" +
					" WHERE { " +
					"    ?entity rdf:type rdfs:Class ." +
					"    ?type rdfs:subClassOf schema:CollegeOrUniversity ." +
					"    ?type rdfs:label ?subclassName ." +
					"    FILTER ( ?type != schema:CollegeOrUniversity )" +
					" }";

			// WITHOUT INFERENCE
			// Find all subclasses of class CollegeOrUniversity
			String queryString5 =
					" PREFIX schema: <https://schema.org/>" +
					" SELECT ?subclassName" +
					" WHERE { " +
					"    ?entity rdf:type rdfs:Class ." +
					"    ?type rdfs:subClassOf schema:CollegeOrUniversity ." +
					"    ?type rdfs:label ?subclassName ." +
					"    FILTER ( ?type != schema:CollegeOrUniversity )" +
					" }";
			//------------------------------------------------------------------------------------------------------------

			//------------------------------------------------------------------------------------------------------------
			// Find all the superclasses of class CollegeOrUniversity.

			// WITH INFERENCE
			String queryString2 =
					" PREFIX schema: <https://schema.org/>" +
				 	" SELECT ?superclassName " +
				 	" WHERE { " +
				 	"    ?superclass ^rdfs:subClassOf schema:CollegeOrUniversity ." +
				 	"    ?superclass rdfs:label ?superclassName ." +
				 	"    FILTER ( ?superclass != schema:CollegeOrUniversity )" +
				 	" }" ;

			// (Alternative approach) Find all the superclasses of class CollegeOrUniversity.
			String queryString2_1 =
					" PREFIX schema: <https://schema.org/>" +
				 	" SELECT ?className (GROUP_CONCAT(?superclassName; separator=',') AS ?superclassNames) " +
				 	" WHERE { " +
				 	"    schema:CollegeOrUniversity rdfs:label ?className ." +
				 	"    ?superclass ^rdfs:subClassOf schema:CollegeOrUniversity ." +
				 	"    ?superclass rdfs:label ?superclassName ." +
				 	"    FILTER ( ?superclass != schema:CollegeOrUniversity )" +
				 	" } GROUP BY ?className " ;

			// WITHOUT INFERENCE
			// Find all the superclasses of class CollegeOrUniversity.
			String queryString6 =
					" PREFIX schema: <https://schema.org/>" +
				 	" SELECT ?superclassName " +
				 	" WHERE { " +
					"    ?superclass ^rdfs:subClassOf* schema:CollegeOrUniversity ." +
				 	"    ?superclass rdfs:label ?superclassName ." +
				 	"    FILTER ( ?superclass != schema:CollegeOrUniversity )"+
				 	" }" ;

			// (Alternative approach) Find all the superclasses of class CollegeOrUniversity.
			String queryString6_1 =
					" PREFIX schema: <https://schema.org/>" +
				 	" SELECT ?className (GROUP_CONCAT(?superclassName; separator=',') AS ?superclassNames) " +
				 	" WHERE { " +
				 	"    schema:CollegeOrUniversity rdfs:label ?className ." +
				 	"    ?superclass ^rdfs:subClassOf* schema:CollegeOrUniversity ." +
				 	"    ?superclass rdfs:label ?superclassName ." +
				 	"    FILTER ( ?superclass != schema:CollegeOrUniversity )" +
				 	" } GROUP BY ?className " ;
			//------------------------------------------------------------------------------------------------------------

			//------------------------------------------------------------------------------------------------------------
			// Find all properties defined for the class CollegeOrUniversity together with all the properties inherited by its superclasses.
			// WITH INFERENCE
			String queryString3 =
					" PREFIX schema: <https://schema.org/>" +
				 	" SELECT DISTINCT ?superclassName ?property ?propertyValue " +
				 	" WHERE { " +
				 	"    schema:CollegeOrUniversity rdfs:subClassOf ?superclass ." +
				 	"    ?superclass rdfs:label ?superclassName ." +
				 	"    ?property rdfs:label ?propertyValue ." +
				 	"    ?property schema:domainIncludes ?superclass ." +
				 	"    FILTER ( ?property not in ( rdf:type ) )" +
				 	" } ORDER BY ASC(?superclassName) ASC(?property)" ;

			// Find all properties defined for the class CollegeOrUniversity together with all the properties inherited by its superclasses.
			// WITHOUT INFERENCE
			String queryString7 =
					" PREFIX schema: <https://schema.org/>" +
				 	" SELECT DISTINCT ?superclassName ?property ?propertyValue " +
				 	" WHERE { " +
				 	"    schema:CollegeOrUniversity rdfs:subClassOf* ?superclass ." +
				 	"    ?superclass rdfs:label ?superclassName ." +
				 	"    ?property rdfs:label ?propertyValue ." +
				 	"    ?property schema:domainIncludes ?superclass ." +
				 	"    FILTER ( ?property not in ( rdf:type ) )" +
				 	" } ORDER BY ASC(?superclassName) ASC(?property)" ;

			//------------------------------------------------------------------------------------------------------------
			// Find all classes that are subclasses of class Thing and are found in at most 2 levels of subclass relationships away from Thing.
			// WITH INFERENCE - COULD NOT THINK OF AN ACCEPTED SOLUTION
//			String queryString4 =
//					" PREFIX schema: <https://schema.org/>"

			// Find all classes that are subclasses of class Thing and are found in at most 2 levels of subclass relationships away from Thing.
			// WITHOUT INFERENCE
			String queryString8 =
					" PREFIX schema: <https://schema.org/>" +
					" SELECT ?sub_level_one_name ?sub_level_two_name" +
					" WHERE { " +
					"     ?type rdfs:subClassOf schema:Thing ." +
					"     ?type rdfs:label ?sub_level_one_name ." +
					"     OPTIONAL { ?type_levelOne rdfs:subClassOf ?type ." +
					"                ?type_levelOne rdfs:label ?sub_level_two_name . }" +
					"     FILTER ( ?type != schema:Thing )" +
					" } ORDER BY ASC(?sub_level_one_name)";

			//------------------------------------------------------------------------------------------------------------

			String queryString = queryString1;
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			//tupleQuery.setIncludeInferred(false);
			TupleQueryResult result = tupleQuery.evaluate();

			System.out.println("Query:\n" + queryString);
			//PrintWriter stdout = new PrintWriter(new FileWriter("/home/nspanos/Documents/MScComputerScience/Fall2022/KnowledgeTechnologies/results_ex3/queryString8_WithoutInference_results.txt",false), true);

			try {
				//iterate the result set
				//stdout.println("START PRINTING RESULTS ---------------------------------------- \n");

				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					System.out.println(bindingSet.toString());
					//stdout.println(bindingSet.toString());
				}
				//stdout.println("\nFINISHED PRINTING RESULTS ---------------------------------------- \n");

			} finally {
						result.close();
						//stdout.close();
			}

		}
		catch (Exception e) {
			// handle exception
			e.printStackTrace();
		}
	}
}
