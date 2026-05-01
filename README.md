# Protégé Modified Date Plugin

A plugin for [Protégé OWL ontology editor](https://protege.stanford.edu/) (5.5.0+) that automatically appends or updates a modified date annotation (e.g., `dcterms:modified`) on an `owl:Class` or `owl:NamedIndividual` whenever its axioms or annotations are changed in the editor.

Developed by [Maksym Shostak](http://orcid.org/0000-0001-8017-8797).

## Features

* **Automatic Timestamping:** Intercepts editor changes and applies an updated timestamp to the modified entity automatically.

* **Smart Filtering:** Ignores newly declared entities until they are actually edited. Prevents timestamping built-in foundational entities like `owl:Thing`.

* **Protégé Preferences Integration:** Fully configurable at runtime via the Protégé Preferences menu (File > Preferences).

* **Configurable Options:**

  * Choose between the current system date (UTC ISO-8601 or Local Offset) or a custom static text string.

  * Define the target Annotation Property IRI (defaults to `http://purl.org/dc/terms/modified`).

  * Toggle tracking for Classes, Named Individuals, or both.

* **Thread-Safe:** Includes pre-emptive handling for Protégé's Swing Event Dispatch Thread (EDT) to prevent `ConcurrentModificationException` during listener broadcasts.

## Requirements

* **Protégé:** 5.5.0 or higher (Tested on 5.6.9)

* **Java:** JDK 11 or higher (The build targets Java 11 release compatibility for Protégé OSGi constraints)

* **Maven:** Apache Maven 3.x

## Compilation and Installation (Windows 11)

1. Clone or download this repository.

2. Open Command Prompt or PowerShell and navigate to the project root directory (where the `pom.xml` is located).

3. Compile the OSGi bundle using Maven: mvn clean package

4. Upon successful compilation, Maven will generate a `.jar` file in the `target/` directory (e.g., `modified-date-plugin-1.0.0.jar`).

5. Copy this `.jar` file.

6. Navigate to your Protégé installation's plugin directory (e.g., `C:\Program Files\Protege-5.6.9\plugins\`).

7. Paste the `.jar` file into the `plugins` folder.

8. Start or restart Protégé.

## Usage

1. Open an ontology in Protégé.

2. Navigate to **File > Preferences** (or **Protégé > Preferences** on macOS).

3. Select the **Modified Date Plugin** tab.

4. Configure your desired timestamp format, target annotation IRI, and target entities.

5. Click **OK**.

6. Edit any Class or Named Individual in the ontology. The plugin will automatically attach or update the configured annotation property.

*Note: Tracing logs for plugin operations are output at the `DEBUG` level. To view them, ensure your Protégé `logback.xml` configuration is set to print `DEBUG` messages.*