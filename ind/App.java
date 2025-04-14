package csparql.ind;

import java.io.File;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.larkc.csparql.common.utils.CsparqlUtils;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import csparql.ind.streamer.SensorsStreamer;

public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        try {
            // Configure log4j logger for the csparql engine
            PropertyConfigurator.configure("log4j_configuration/csparql_readyToGoPack_log4j.properties");

            // Create csparql engine instance
            CsparqlEngineImpl engine = new CsparqlEngineImpl();
            // Initialize the engine instance
            engine.initialize(true);

            String fileOntology = "onto_anomaly.owl";

            // Put static model
            engine.putStaticNamedModel("http://example.org/anomaly.owl", CsparqlUtils.serializeRDFFile(fileOntology));

            String queryhighTemp = "REGISTER QUERY highTemp AS "
            + "PREFIX anomaly: <http://example.org/anomaly.owl#> "
            + "PREFIX sosa: <http://www.w3.org/ns/sosa/> "
            + "SELECT ?c ?vsoc1 "
            + "FROM STREAM <Stream_S_TempSensor1> [RANGE 60s STEP 5s] "
            + "FROM STREAM <Stream_S_TempSensor2> [RANGE 60s STEP 5s] "
            + "FROM STREAM <Stream_S_TempSensor3> [RANGE 60s STEP 5s] "
            + "FROM STREAM <Stream_S_CurrentSensor> [RANGE 60s STEP 5s] "
            + "FROM STREAM <Stream_S_Soc1> [RANGE 60s STEP 5s] "  
            + "FROM <http://example.org/anomaly.owl> "
            + "WHERE { "
            + "  ?c        anomaly:hosts       anomaly:TempSensor1 . "
            + "  anomaly:TempSensor1 anomaly:madeObservation ?o1 . "
            + "  ?o1        anomaly:hasSimpleResult ?v1 . "
            + "  ?m         anomaly:hosts       anomaly:TempSensor2 . "
            + "  anomaly:TempSensor2 anomaly:madeObservation ?o2 . "
            + "  ?o2        anomaly:hasSimpleResult ?v2 . "
            + "  ?c         anomaly:hosts       anomaly:TempSensor3 . "
            + "  anomaly:TempSensor3 anomaly:madeObservation ?o3 . "
            + "  ?o3        anomaly:hasSimpleResult ?v3 . "
            + "  ?c         anomaly:hosts       anomaly:CurrentSensor . "
            + "  anomaly:CurrentSensor anomaly:madeObservation ?oc . "
            + "  ?oc        anomaly:hasSimpleResult ?vc . "
            + "  ?c         anomaly:hosts       anomaly:Soc1 . "  // Added SOC1 sensor
            + "  anomaly:Soc1 anomaly:madeObservation ?osoc1 . "  // SOC1 observation
            + "  ?osoc1      anomaly:hasSimpleResult ?vsoc1 . "   // SOC1 value
            + "  FILTER ( "
            + "    (ABS(?v1 - ?v2) > 0.5 || "
            + "    ABS(?v1 - ?v3) > 0.5 || "
            + "    ABS(?v2 - ?v3) > 0.5) && "
            + "    (?vc > 10.0) && "
            + "    (?vsoc1 > 0.2) "  // SOC1 condition (20%-80% range)
            + "  ) "
            + "}";



            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLDataFactory factory = manager.getOWLDataFactory();
            String ontologyURI = "http://example.org/anomaly.owl";
            String ns = ontologyURI + "#";
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(fileOntology));

            // Path to the Excel file
            String excelFilePath = "data.xlsx";

            // Updated streamers
            SensorsStreamer Stream_TempSensor1 = new SensorsStreamer("Stream_S_TempSensor1", ns, "TempSensor1", 4, excelFilePath, ontology, factory);
            SensorsStreamer Stream_TempSensor2 = new SensorsStreamer("Stream_S_TempSensor2", ns, "TempSensor2", 5, excelFilePath, ontology, factory);
            SensorsStreamer Stream_TempSensor3 = new SensorsStreamer("Stream_S_TempSensor3", ns, "TempSensor3", 6, excelFilePath, ontology, factory);
            SensorsStreamer Stream_CurrentSensor = new SensorsStreamer("Stream_S_CurrentSensor", ns, "CurrentSensor", 6, excelFilePath, ontology, factory);
            SensorsStreamer Stream_S_Soc1 = new SensorsStreamer("Stream_S_Soc1", ns, "Soc1", 6, excelFilePath, ontology, factory);


            // Register new streams in the engine
            engine.registerStream(Stream_TempSensor1);
            engine.registerStream(Stream_TempSensor2);
            engine.registerStream(Stream_TempSensor3);
            engine.registerStream(Stream_CurrentSensor);
            engine.registerStream(Stream_S_Soc1);


            Thread Stream_TempSensor1_Thread = new Thread(Stream_TempSensor1);
            Thread Stream_TempSensor2_Thread = new Thread(Stream_TempSensor2);
            Thread Stream_TempSensor3_Thread = new Thread(Stream_TempSensor3);
            Thread Stream_CurrentSensor_Thread = new Thread(Stream_CurrentSensor);
            Thread Stream_Soc1_Thread = new Thread(Stream_S_Soc1);


            // Register new query in the engine
            CsparqlQueryResultProxy c_highTemp = engine.registerQuery(queryhighTemp, false);

            // Attach a result consumer to the query result proxy to print the results on the console
            c_highTemp.addObserver(new ConsoleFormatter("highTemp", ns, ontology, factory));

            // Start streaming data
            Stream_TempSensor1_Thread.start();
            Stream_TempSensor2_Thread.start();
            Stream_TempSensor3_Thread.start();
            Stream_CurrentSensor_Thread.start();
            Stream_Soc1_Thread.start();



        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
