# TestGenie

## Description

Here you can find the overall structure of TestGenie plugin. The classes are listed and their purpose is described. This section is intended for developers and contributors to TestGenie plugin.

## Plugin Configuration File

The plugin configuration file is `plugin.xml` which can be found in `src/main/resources/META-INF` directory. All declarations (like actions, services, listeners) are in this file.


## Classes

All the classes can be found in `src/main/kotlin/nl/tudelft/ewi/se/ciselab/testgenie` directory.

### Actions

All the action classes can be found in `actions` directory.

- `GenerateTestsActionClass` contains all the logic related to generating tests for a class. No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available, getting the information about the selected class and passing it to (EvoSuite) Runner. 
- `GenerateTestsActionGroup` is the group class for TestGenieActions.
- `GenerateTestsActionMethod` contains all the logic related to generating tests for a method. No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available, getting the information about the selected method and passing it to (EvoSuite) Runner.
- `GenerateTestsUtils` contains some useful functions for `GenerateTestsActionClass` and `GenerateTestsActionMethod` classes.

### Coverage
All the classes related to the coverage visualisation can be found in `coverage` directory.

- `CoverageRenderer` - adds extra functionality to the default line marker and gutter which is used for the coverage visualisation.

### EvoSuite
All the classes that interact with EvoSuite can be found in `evosuite` directory.

- `ResultWatcher` listens for the results of the generation process by EvoSuite. Used in conjunction with `Runner`.
- `Runner` runs EvoSuite as a separate process in its various modes of operation. 
- `SettingsArguments` is used for constructing the arguments and properties for EvoSuite.
- `TestGenerationResultListener` is a topic interface for sending and receiving test results produced by EvoSuite test generation process.

### Helpers
All the helper classes can be found in `helpers` directory.

- `MethodDescriptorHelper` contains some useful functions for generating *method descriptors* for methods.

### Listeners
All the listener classes can be found in `listeners` directory.

- `TestGenerationResultListenerImpl` is the implementation of `TestGenerationResultListener` topic interface. 

### Services
All the service classes can be found in `services` directory.

- `CoverageToolWindowDisplayService` creates the *"Coverage visualisation"* tool window panel and the coverage table to display the test coverage data of the tests generated by EvoSuite.
- `CoverageVisualisationService` visualises the coverage in the gutter and the editor (by colouring), injects the coverage data into the *"Coverage visualisation"* tool window tab.
- `QuickAccessParametersService` allows to load and get the state of the parameters in the *"Parameters"* tool window panel.
- `TestCaseDisplayService` displays the tests generated by EvoSuite, in the *"Generated tests"* tool window panel.
- `TestGenieSettingsService` allows to load and get the state of the parameters and options in the TestGenie Settings page.
- `TestGenieTelemetryService` sends usage information to CISELab at the Delft University of Technology if the user has opted in.

### Settings
All the classes related to TestGenie `Settings/Preferences` page can be found in `settings` directory.

- `SettingsEvoSuiteComponent` - displays and captures the changes to the values of the entries in the EvoSuite page of the Settings dialog.
- `SettingsEvoSuiteConfigurable` allows to configure some EvoSuite settings via the EvoSuite page in the Settings dialog, observes the changes and manages the UI and state.
- `SettingsPluginComponent` - displays and captures the changes to the values of the entries in the TestGenie main page of the Settings dialog.
- `SettingsPluginConfigurable` allows to configure some Plugin settings via the Plugin page in the Settings dialog, observes the changes and manages the UI and state.
- `TestGenieSettingsState` is responsible for storing the values of the TestGenie Settings entries.

### Tool Window
All the classes related to TestGenie Tool Window (on the right side) can be found in `toolwindow` directory.

- `QuickAccessParameters` stores the main panel and the UI of the "Parameters" tool window tab. 
- `QuickAccessParametersState` is responsible for persisting the values of the parameters in the "Parameters" tool window tab.
- `TestGenieToolWindowFactory` creates the tabs and the UI of the TestGenie tool window.

### TestGenieBundle
A bundle that is used to load messages in the code.


## Tests

The tests for TestGenie can be found in `src/test` directory.

- `resources` directory contains a dummy project(s) used for testing the plugin
- `kotlin/nl/tudelft/ewi/se/ciselab/testgenie` directory contains the actual tests
    - `helpers` directory contains non-UI tests for the logic of our plugin
    - `uiTest` directory contains the tests related to UI.
      - `pages` directory has the frames used for UI testing.
      - `utils` directory contains utils files that are helpful for UI testing.
      - `tests` directory contains the actual UI tests