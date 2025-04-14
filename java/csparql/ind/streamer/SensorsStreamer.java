package csparql.ind.streamer;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

public class SensorsStreamer extends RdfStream implements Runnable {

    private long sleepTime;
    private String baseUri;
    private String prop;
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private Iterator<Row> rowIterator;

    public SensorsStreamer(String iri, String baseUri, String prop, long sleepTime, String excelFilePath, OWLOntology ontology, OWLDataFactory factory) {
        super(iri);
        this.sleepTime = sleepTime;
        this.baseUri = baseUri;
        this.prop = prop;
        this.ontology = ontology;
        this.factory = factory;

        try {
            // Open the Excel file
            FileInputStream file = new FileInputStream(new File(excelFilePath));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            this.rowIterator = sheet.iterator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int observationIndex = 0;
        int timeIndex = 0;

        String ontologyURI = "http://example.org/anomaly";
        String ns = ontologyURI + "#";
        String pre_SOSAOnt = "http://www.w3.org/ns/sosa/";
        String pre_TIME = "http://www.w3.org/2006/time#";

        OWLClass Sensor = factory.getOWLClass(IRI.create(pre_SOSAOnt + "Sensor"));
        OWLClass Observation = factory.getOWLClass(IRI.create(pre_SOSAOnt + "Observation"));
        OWLClass ObservableProperty = factory.getOWLClass(IRI.create(pre_SOSAOnt + "ObservableProperty"));
        OWLClass Instant = factory.getOWLClass(IRI.create(pre_TIME + "Instant"));

        OWLObjectProperty madeObservation = factory.getOWLObjectProperty(IRI.create(pre_SOSAOnt + "madeObservation"));
        OWLObjectProperty observedProperty = factory.getOWLObjectProperty(IRI.create(pre_SOSAOnt + "observedProperty"));
        OWLDataProperty hasSimpleResult = factory.getOWLDataProperty(IRI.create(pre_SOSAOnt + "hasSimpleResult"));
        OWLObjectProperty hasTime = factory.getOWLObjectProperty(IRI.create(ns, "hasTime"));
        OWLDataProperty inXSDDateTimeStamp = factory.getOWLDataProperty(IRI.create(pre_TIME + "inXSDDateTimeStamp"));

        while (rowIterator.hasNext()) {
            try {
                Row row = rowIterator.next();

                // Read data from Excel columns
                double time = row.getCell(0).getNumericCellValue(); // Time
                double current = row.getCell(1).getNumericCellValue(); // CurrentSensor
                double voltage = row.getCell(2).getNumericCellValue(); // Voltage
                double tempAmbiant = row.getCell(3).getNumericCellValue(); // TempAmbiant
                double tempSensor1 = row.getCell(4).getNumericCellValue(); // TempSensor1
                double tempSensor2 = row.getCell(5).getNumericCellValue(); // TempSensor2
                double tempSensor3 = row.getCell(6).getNumericCellValue(); // TempSensor3
                double Soc = row.getCell(7).getNumericCellValue(); // Soc1



                Timestamp date = new Timestamp((long) time); // Convert time to Timestamp

                // Stream data for each sensor
                streamObservation("CurrentSensor", current, date, observationIndex, timeIndex, ns, pre_SOSAOnt, pre_TIME, Sensor, Observation, ObservableProperty, Instant, madeObservation, observedProperty, hasSimpleResult, hasTime, inXSDDateTimeStamp);
                streamObservation("Voltage", voltage, date, observationIndex, timeIndex, ns, pre_SOSAOnt, pre_TIME, Sensor, Observation, ObservableProperty, Instant, madeObservation, observedProperty, hasSimpleResult, hasTime, inXSDDateTimeStamp);
                streamObservation("TempAmbiant", tempAmbiant, date, observationIndex, timeIndex, ns, pre_SOSAOnt, pre_TIME, Sensor, Observation, ObservableProperty, Instant, madeObservation, observedProperty, hasSimpleResult, hasTime, inXSDDateTimeStamp);
                streamObservation("TempSensor1", tempSensor1, date, observationIndex, timeIndex, ns, pre_SOSAOnt, pre_TIME, Sensor, Observation, ObservableProperty, Instant, madeObservation, observedProperty, hasSimpleResult, hasTime, inXSDDateTimeStamp);
                streamObservation("TempSensor2", tempSensor2, date, observationIndex, timeIndex, ns, pre_SOSAOnt, pre_TIME, Sensor, Observation, ObservableProperty, Instant, madeObservation, observedProperty, hasSimpleResult, hasTime, inXSDDateTimeStamp);
                streamObservation("TempSensor3", tempSensor3, date, observationIndex, timeIndex, ns, pre_SOSAOnt, pre_TIME, Sensor, Observation, ObservableProperty, Instant, madeObservation, observedProperty, hasSimpleResult, hasTime, inXSDDateTimeStamp);
                streamObservation("Soc1", Soc, date, observationIndex, timeIndex, ns, pre_SOSAOnt, pre_TIME, Sensor, Observation, ObservableProperty, Instant, madeObservation, observedProperty, hasSimpleResult, hasTime, inXSDDateTimeStamp);

                TimeUnit.SECONDS.sleep(sleepTime);
                observationIndex++;
                timeIndex++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void streamObservation(String propertyName, double value, Timestamp date, int observationIndex, int timeIndex, String ns, String pre_SOSAOnt, String pre_TIME, OWLClass Sensor, OWLClass Observation, OWLClass ObservableProperty, OWLClass Instant, OWLObjectProperty madeObservation, OWLObjectProperty observedProperty, OWLDataProperty hasSimpleResult, OWLObjectProperty hasTime, OWLDataProperty inXSDDateTimeStamp) {
        try {
            RdfQuadruple q = new RdfQuadruple(baseUri + propertyName, baseUri + "madeObservation", baseUri + "S_" + propertyName + "-Obs-" + observationIndex, System.currentTimeMillis());
            System.out.println(q);
            this.put(q);
            q = new RdfQuadruple(baseUri + "S_" + propertyName + "-Obs-" + observationIndex, baseUri + "observedProperty", baseUri + propertyName, System.currentTimeMillis());
            System.out.println(q);
            this.put(q);
            q = new RdfQuadruple(baseUri + "S_" + propertyName + "-Obs-" + observationIndex, baseUri + "hasSimpleResult", value + "^^http://www.w3.org/2001/XMLSchema#double", System.currentTimeMillis());
            System.out.println(q);
            this.put(q);
            q = new RdfQuadruple(baseUri + "S_" + propertyName + "-Obs-" + observationIndex, baseUri + "hasTime", baseUri + "t-obs-S_" + propertyName + "-" + timeIndex, System.currentTimeMillis());
            System.out.println(q);
            this.put(q);
            q = new RdfQuadruple(baseUri + "t-obs-S_" + propertyName + "-" + timeIndex, baseUri + "inXSDDateTime", date + "^^http://www.w3.org/2001/XMLSchema#dateTimeStamp", System.currentTimeMillis());
            System.out.println(q);
            this.put(q);

            OWLIndividual sensor = factory.getOWLNamedIndividual(IRI.create(ns, propertyName));
            OWLClassAssertionAxiom sensorType = factory.getOWLClassAssertionAxiom(Sensor, sensor);
            ontology.add(sensorType);
            OWLIndividual obs = factory.getOWLNamedIndividual(IRI.create(ns, "S_" + propertyName + "-Obs-" + observationIndex));
            OWLClassAssertionAxiom obsType = factory.getOWLClassAssertionAxiom(Observation, obs);
            ontology.add(obsType);
            OWLIndividual property = factory.getOWLNamedIndividual(IRI.create(ns, propertyName));
            OWLClassAssertionAxiom propType = factory.getOWLClassAssertionAxiom(ObservableProperty, property);
            ontology.add(propType);

            OWLObjectPropertyAssertionAxiom sensormadeobs = factory.getOWLObjectPropertyAssertionAxiom(madeObservation, sensor, obs);
            ontology.add(sensormadeobs);
            OWLObjectPropertyAssertionAxiom observedProp = factory.getOWLObjectPropertyAssertionAxiom(observedProperty, obs, property);
            ontology.add(observedProp);

            OWLIndividual time = factory.getOWLNamedIndividual(IRI.create(pre_TIME, "t-obs-S_" + propertyName + "-" + timeIndex));
            OWLClassAssertionAxiom timeType = factory.getOWLClassAssertionAxiom(Instant, time);
            ontology.add(timeType);
            OWLObjectPropertyAssertionAxiom obshastime = factory.getOWLObjectPropertyAssertionAxiom(hasTime, obs, time);
            ontology.add(obshastime);
            OWLDataPropertyAssertionAxiom timehasdate = factory.getOWLDataPropertyAssertionAxiom(inXSDDateTimeStamp, time, date + "^^http://www.w3.org/2001/XMLSchema#dateTimeStamp");
            ontology.add(timehasdate);

            OWLDataPropertyAssertionAxiom obshassimpleresult = factory.getOWLDataPropertyAssertionAxiom(hasSimpleResult, obs, value + "^^http://www.w3.org/2001/XMLSchema#double");
            ontology.add(obshassimpleresult);

            try {
                ontology.saveOntology();
            } catch (OWLOntologyStorageException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}