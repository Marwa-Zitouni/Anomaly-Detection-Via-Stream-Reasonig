# Anomaly Detection in Lithium-Ion Batteries via Stream Reasoning

## Overview

This GitHub project demonstrates an approach for real-time anomaly detection in lithium-ion batteries using stream reasoning and a structured knowledge base (ontology). The system processes continuous data streams from simulated battery sensors, reasons over this data in conjunction with an ontology defining battery components, their properties, and potential anomalies, and identifies abnormal behaviors like significant temperature differences between cells combined with high current and state-of-charge (SOC).

The core of the project utilizes the C-SPARQL engine to execute continuous queries over the incoming sensor data, which is modeled as RDF streams. The `Anomaly Ontology` (defined in `onto_anomaly.owl`) provides the semantic context for understanding the sensor data and defining the conditions for anomaly detection.

## Project Structure

The project consists of the following main components:

-   **`csparql.ind` package:**
    -   **`App.java`:** The main application class responsible for initializing the C-SPARQL engine, registering streams and queries, and starting the data streaming process.
    -   **`streamer.SensorsStreamer.java`:** A class that simulates sensor data streams from an Excel file (`data.xlsx`) and pushes them into the C-SPARQL engine.
-   **`log4j_configuration` directory:**
    -   **`csparql_readyToGoPack_log4j.properties`:** Configuration file for the Log4j logging framework used by the C-SPARQL engine.
-   **`onto_anomaly.owl`:** The OWL ontology defining concepts related to battery components (e.g., `TempSensor`, `CurrentSensor`, `Soc1`), their properties (e.g., `hosts`, `madeObservation`, `hasSimpleResult`), and the relationships between them.
-   **`data.xlsx`:** An Excel file containing simulated sensor data (temperature, current, SOC) that will be streamed into the C-SPARQL engine.

## Requirements

To run this project, you will need:

-   **Java Development Kit (JDK):** Ensure you have a compatible JDK installed on your system.
-   **Apache Maven:** The project is likely built using Maven. Install Maven if you don't have it.
-   **OWL API:** The project uses the OWL API for handling the ontology. Maven should automatically download this dependency.
-   **C-SPARQL Engine:** The core stream processing engine. Maven should handle this dependency.
-   **Apache Log4j:** For logging. Maven should handle this dependency.
-   **SLF4j:** A simple logging facade for Java. Maven should handle this dependency.
-   **eu.larkc.csparql:** Larkc CSPARQL utilities. Maven should handle this dependency.
-   **Apache POI:** For reading data from the Excel file. Maven should handle this dependency.

## Setup and Execution

1.  **Clone the Repository:**
    ```bash
    git clone <repository_url>
    cd <repository_directory>
    ```

2.  **Review Project Dependencies:**
    Ensure that the `pom.xml` file (if present) contains the necessary dependencies for the C-SPARQL engine, OWL API, Log4j, SLF4j, and Apache POI. Maven should automatically download these when building the project.

3.  **Configure Log4j:**
    Verify that the `log4j_configuration/csparql_readyToGoPack_log4j.properties` file is correctly configured for the desired logging level and output.

4.  **Prepare Sensor Data:**
    Ensure that the `data.xlsx` file is in the project's root directory and contains the sensor data in a format that the `SensorsStreamer` class can read. The code expects specific columns for each sensor type. Based on the `SensorsStreamer` instantiation in `App.java`, the columns are likely:
    -   Temperature Sensor 1: Column 4 (index 3)
    -   Temperature Sensor 2: Column 5 (index 4)
    -   Temperature Sensor 3: Column 6 (index 5)
    -   Current Sensor: Column 6 (index 5)
    -   SOC Sensor 1: Column 6 (index 5)
    **Note:** The column index for `CurrentSensor` and `Stream_S_Soc1` seems to be the same (index 5). You might need to adjust the `excelFilePath` or the column indices in the `SensorsStreamer` instantiations if the data is organized differently in your `data.xlsx` file.

5.  **Build the Project (if necessary):**
    If you have a `pom.xml` file, use Maven to build the project:
    ```bash
    mvn clean install
    ```

6.  **Run the Application:**
    Execute the `App.java` main method. You can do this from your Integrated Development Environment (IDE) or by running the compiled JAR file from the command line:
    ```bash
    java -cp target/csparql-ind-1.0-SNAPSHOT.jar csparql.ind.App
    ```
    (Replace `csparql-ind-1.0-SNAPSHOT.jar` with the actual name of your JAR file if different).

