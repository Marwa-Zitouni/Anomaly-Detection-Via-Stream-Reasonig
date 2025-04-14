package csparql.ind;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.ResultFormatter;
import java.util.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class ConsoleFormatter extends ResultFormatter {

    private String situationName;
    private String baseUri;
    private OWLOntology ontology;
    private OWLDataFactory factory;

    public ConsoleFormatter(String situationName, String baseUri, OWLOntology ontology, OWLDataFactory factory) {
        this.situationName = situationName;
        this.baseUri = baseUri;
        this.ontology = ontology;
        this.factory = factory;
    }

    @Override
    public void update(Observable o, Object arg) {
        // Cast the argument to RDFTable
        RDFTable rdfTable = (RDFTable) arg;
        System.out.println();

        // Check if there are no results
        if (rdfTable.size() == 0) {
            // No situation detected: Observation is "Normal"
            System.out.println("NO RESULT - OBSERVATION: NORMAL");
            printSingleObservationRow("Normal", "N/A", "N/A"); // Pass "N/A" for current and soc
        } else {
            // Situation detected: Observation is "Abnormal"
            System.out.println(situationName + " DETECTED. " + rdfTable.size() + " result at SystemTime: " + System.currentTimeMillis());

            // Create a list to store the results
            List<Map<String, String>> results = new ArrayList<>();

            // Process each row in the result table
            rdfTable.stream().forEach((t) -> {
                String cellId = t.get(0);
                String currentValue = getCurrentValue(cellId, "CurrentSensor") + "A"; // Get current value
                String socValue = getCurrentValue(cellId, "SOC1"); // Get SOC value

                // Create a map to store the details of this row
                Map<String, String> row = new HashMap<>();
                row.put("Cell", cellId);
                row.put("Situation", situationName);
                row.put("SystemTime", String.valueOf(System.currentTimeMillis()));
                row.put("TempDiff", calculateTemperatureDifference(cellId)); // Calculate temperature difference
                row.put("Current", currentValue);
                row.put("SOC", socValue);
                row.put("Observation", "Abnormal"); // Set Observation to "Abnormal"

                // Add the row to the results list
                results.add(row);

                // Create OWL constructs for the situation (unchanged from your original code)
                OWLClass Situation = factory.getOWLClass(IRI.create(baseUri + "Situation"));
                OWLIndividual sit = factory.getOWLNamedIndividual(IRI.create(baseUri, situationName + "-" + System.currentTimeMillis()));
                OWLClassAssertionAxiom sitType = factory.getOWLClassAssertionAxiom(Situation, sit);
                ontology.add(sitType);

                OWLClass Cell = factory.getOWLClass(IRI.create(t.get(0)));
                OWLIndividual cell = factory.getOWLNamedIndividual(IRI.create(t.get(0)));
                OWLClassAssertionAxiom cellType = factory.getOWLClassAssertionAxiom(Cell, cell);
                ontology.add(cellType);

                OWLObjectProperty hosts = factory.getOWLObjectProperty(IRI.create(baseUri + "hosts"));
                OWLIndividual tempSensor1 = factory.getOWLNamedIndividual(IRI.create(baseUri + "TempSensor1"));
                OWLIndividual tempSensor2 = factory.getOWLNamedIndividual(IRI.create(baseUri + "TempSensor2"));
                OWLIndividual tempSensor3 = factory.getOWLNamedIndividual(IRI.create(baseUri + "TempSensor3"));
                OWLIndividual currentSensor = factory.getOWLNamedIndividual(IRI.create(baseUri + "CurrentSensor")); // Add current sensor
                OWLIndividual Soc1 = factory.getOWLNamedIndividual(IRI.create(baseUri + "SOC1"));


                OWLObjectPropertyAssertionAxiom hostsAssert1 = factory.getOWLObjectPropertyAssertionAxiom(hosts, cell, tempSensor1);
                OWLObjectPropertyAssertionAxiom hostsAssert2 = factory.getOWLObjectPropertyAssertionAxiom(hosts, cell, tempSensor2);
                OWLObjectPropertyAssertionAxiom hostsAssert3 = factory.getOWLObjectPropertyAssertionAxiom(hosts, cell, tempSensor3);
                OWLObjectPropertyAssertionAxiom hostsAssert4 = factory.getOWLObjectPropertyAssertionAxiom(hosts, cell, currentSensor); // Add current sensor
                OWLObjectPropertyAssertionAxiom hostsAssert5 = factory.getOWLObjectPropertyAssertionAxiom(hosts, cell, Soc1);

                ontology.add(hostsAssert1);
                ontology.add(hostsAssert2);
                ontology.add(hostsAssert3);
                ontology.add(hostsAssert4);
                ontology.add(hostsAssert5);

                OWLObjectProperty concernBy = factory.getOWLObjectProperty(IRI.create(baseUri + "concernBy"));
                OWLObjectPropertyAssertionAxiom concernByAssert = factory.getOWLObjectPropertyAssertionAxiom(concernBy, cell, sit);
                ontology.add(concernByAssert);

                // Save the updated ontology
                try {
                    ontology.saveOntology();
                } catch (OWLOntologyStorageException e) {
                    e.printStackTrace();
                }
            });

            // Print the results in a table format in the terminal with vertical lines
            printTableInTerminal(results);
        }
    }

    // Helper method to calculate the temperature difference
    private String calculateTemperatureDifference(String cellId) {
        // Here you would implement the logic to calculate the temperature difference
        // For example, you might retrieve the current temperature and a reference temperature
        // and then calculate the difference.
        // This is just a placeholder implementation.
        double currentTemperature = getCurrentTemperature(cellId); // Implement this method
        double referenceTemperature = getReferenceTemperature(cellId); // Implement this method
        double difference = currentTemperature - referenceTemperature;
        return String.format("%.2f°C", difference);
    }

    // Placeholder method to get the current temperature (implement as needed)
    private double getCurrentTemperature(String cellId) {
        // Implement logic to retrieve the current temperature for the given cell
        return 25.0; // Example value
    }

    // Placeholder method to get the reference temperature (implement as needed)
    private double getReferenceTemperature(String cellId) {
        // Implement logic to retrieve the reference temperature for the given cell
        return 20.0; // Example value
    }

    // Placeholder method to get the current value of a sensor for a given cell
    private String getCurrentValue(String cellId, String sensorType) {
        // In a real application, you would need a way to access the current sensor readings
        // associated with the detected situation. This might involve querying the
        // underlying data streams or storing relevant information when the situation is detected.
        // For now, we'll return placeholder values.
        if (sensorType.equals("CurrentSensor")) {
            // Replace with actual current retrieval logic
            return "1.5";
        } else if (sensorType.equals("SOC1")) {
            // Replace with actual SOC retrieval logic
            return "85%";
        }
        return "N/A";
    }

    // Helper method to print a single row when no situation is detected
    private void printSingleObservationRow(String observation, String current, String soc) {
        String[] headers = {"Cell", "Situation", "SystemTime", "TempDiff", "Current", "SOC", "Observation"};
        int[] columnWidths = calculateColumnWidths(headers, Collections.emptyList());

        // Print the table header
        printRowWithVerticalLines(headers, columnWidths);

        // Print a separator line
        printSeparatorWithVerticalLines(columnWidths);

        // Print a single row with "Normal" observation
        String[] rowData = {"N/A", "N/A", String.valueOf(System.currentTimeMillis()), "N/A", current, soc, observation};
        printRowWithVerticalLines(rowData, columnWidths);

        // Print a separator line
        printSeparatorWithVerticalLines(columnWidths);
    }

    // Helper method to print the results in a table format in the terminal with vertical lines
    private void printTableInTerminal(List<Map<String, String>> results) {
        // Define the table headers
        String[] headers = {"Cell", "Situation", "SystemTime", "TempDiff", "Current", "SOC", "Observation"};

        // Calculate the maximum width for each column
        int[] columnWidths = calculateColumnWidths(headers, results);

        // Print the table header
        printRowWithVerticalLines(headers, columnWidths);

        // Print a separator line
        printSeparatorWithVerticalLines(columnWidths);

        // Print each row of the table
        for (Map<String, String> row : results) {
            String[] rowData = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                rowData[i] = row.get(headers[i]);
            }
            printRowWithVerticalLines(rowData, columnWidths);
        }

        // Print a separator line
        printSeparatorWithVerticalLines(columnWidths);
    }

    // Helper method to calculate column widths
    private int[] calculateColumnWidths(String[] headers, List<Map<String, String>> results) {
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }
        for (Map<String, String> row : results) {
            for (int i = 0; i < headers.length; i++) {
                String value = row.get(headers[i]);
                if (value != null && value.length() > columnWidths[i]) {
                    columnWidths[i] = value.length();
                }
            }
        }
        return columnWidths;
    }

    // Helper method to print a row of the table with vertical lines
    private void printRowWithVerticalLines(String[] row, int[] columnWidths) {
        System.out.print("|");
        for (int i = 0; i < row.length; i++) {
            System.out.printf(" %-" + columnWidths[i] + "s |", row[i]);
        }
        System.out.println();
    }

    // Helper method to print a separator line with vertical lines
    private void printSeparatorWithVerticalLines(int[] columnWidths) {
        System.out.print("+");
        for (int width : columnWidths) {
            for (int i = 0; i < width + 2; i++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }
}