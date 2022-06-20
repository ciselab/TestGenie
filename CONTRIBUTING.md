# TestGenie

## Description

In this document you can find the overall structure of TestGenie plugin. The classes are listed and their purpose is described. This section is intended for developers and contributors to TestGenie plugin.

## Plugin Configuration File

The plugin configuration file is `plugin.xml` which can be found in `src/main/resources/META-INF` directory. All declarations (such as actions, services, listeners) are present in this file.

## Classes

All the classes can be found in `src/main/kotlin/nl/tudelft/ewi/se/ciselab/testgenie` directory.

### Actions

All the action classes can be found in `actions` directory.

- `GenerateTestsActionClass` contains all the logic related to generating tests for a class. No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available, getting the information about the selected class and passing it to (EvoSuite) Pipeline.
- `GenerateTestsActionGroup` is the group class for TestGenie actions.
- `GenerateTestsActionMethod` contains all the logic related to generating tests for a method. No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available, getting the information about the selected method and passing it to (EvoSuite) Pipeline.
- `GenerateTestsActionLine` contains all the logic related to generating tests for a line. No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available, getting the information about the selected line and passing it to (EvoSuite) Pipeline.
- `GenerateTestsUtils` contains useful functions for `GenerateTestsActionClass`, `GenerateTestsActionMethod` and `GenerateTestsActionLine` classes, but one function are also used by `StaticInvalidationService`.

### Coverage

All the classes related to the coverage visualisation can be found in `coverage` directory.

- `CoverageRenderer` adds extra functionality to the default line marker and gutter which is used for the coverage visualisation. It adds tooltips that, when clicking on the gutter, show which tests cover a certain line or mutants (if any) that have been introduced on that line. It also highlights the corresponding tests in the tool window tab.

### Editor

All the editor classes can be found in `editor` directory.

- `Workspace` handles user workspace state and modifications of that state related to test generation. It also sets the event listeners that are triggered whenever the user changes the contents of the editor.

### EvoSuite

All the classes that interact with EvoSuite can be found in `evosuite` directory.

- `Pipeline` runs EvoSuite as a separate process in its various modes of operation (class, method, line), includes static validation of the cache and potential retrieval from the cache.
- `ProjectBuilder` builds the project before running EvoSuite and before validating the tests.
- `ResultWatcher` listens for the results of the generation process by EvoSuite. Used in conjunction with `Runner`.
- `SettingsArguments` is used for constructing the arguments and properties for EvoSuite.
- `TestGenerationResultListener` is a topic interface for sending and receiving test results produced by EvoSuite test generation process.

#### Validation

All the classes related to dynamic cache invalidation can be found in `evosuite/validation` directory.

- `TestCaseEditor.kt` edits the test suite by visiting the test cases and setting the modified body if it has been modified. It also removes scaffolding.
- `ValidationResultListener.kt` is a topic interface for sending and receiving results of test validation.
- `ValidationToolWindowFactory.kt` creates the tabs and the UI of the tool window corresponding to dynamic test validation.
- `Validator.kt` validates and calculates the coverage of an optionally edited set of test cases.

### Helpers

All the helper classes can be found in `helpers` directory.

- `MethodDescriptorHelper` contains helper functions for generating *method descriptors*. It is used by `GenerateTestsActionMethod` class.

### Listeners

All the listener classes can be found in `listeners` directory.

- `TestGenerationResultListenerImpl` is the implementation of `TestGenerationResultListener` topic interface. It notifies `Workspace` of the received test generation result and also puts the generated tests into the cache.
- `TelemetrySubmitListenerImpl`schedules potential submissions of the generated telemetry into a file, which is done every 5 minutes and when the project is closed.

### Services

All the service classes can be found in `services` directory.

- `CoverageSelectionToggleListener` is a topic interface for showing or hiding highlighting when the coverage is toggled for one test or many tests.
- `CoverageToolWindowDisplayService` creates the *"Coverage visualisation"* tool window panel and the coverage table to display the test coverage data of the tests generated by EvoSuite.
- `CoverageVisualisationService` visualises the coverage in the gutter and the editor (by colouring), injects the coverage data into the *"Coverage visualisation"* tool window tab.
- `QuickAccessParametersService` allows to load and get the state of the parameters in the *"Parameters"* tool window panel.
- `RunnerService` is used to limit TestGenie to generate tests only once at a time.
- `SettingsApplicationService` stores the application-level settings persistently. It uses `SettingsApplicationState` class for that.
- `SettingsProjectService` stores the project-level settings persistently. It uses `SettingsProjectState` class for that.
- `StaticInvalidationService` invalidates the cache statically.
- `TestCaseCachingService` contains the data structure for caching the generated test cases and is responsible for adding, retrieving and removing (invalidating) the generated tests.
- `TestCaseDisplayService` displays the tests generated by EvoSuite, in the *"Generated tests"* tool window panel.
- `TestGenieTelemetryService` sends usage information to CISELab at the Delft University of Technology if the user has opted in.

### Settings

All the classes related to TestGenie `Settings/Preferences` page can be found in `settings` directory.

- `SettingsApplicationState` is responsible for storing the values of the EvoSuite Settings entries.
- `SettingsEvoSuiteComponent` displays and captures the changes to the values of the entries in the EvoSuite page of the Settings dialog.
- `SettingsEvoSuiteConfigurable` allows to configure some EvoSuite settings via the EvoSuite page in the Settings dialog, observes the changes and manages the UI and state.
- `SettingsPluginComponent` displays and captures the changes to the values of the entries in the TestGenie main page of the Settings dialog.
- `SettingsPluginConfigurable` allows to configure some Plugin settings via the Plugin page in the Settings dialog, observes the changes and manages the UI and state.
- `SettingsProjectState` is responsible for storing the values of the Plugin Settings entries.

### Tool Window

All the classes related to TestGenie Tool Window (on the right side) can be found in `toolwindow` directory.

- `QuickAccessParameters` stores the main panel and the UI of the "Parameters" tool window tab. 
- `QuickAccessParametersState` is responsible for persisting the values of the parameters in the "Parameters" tool window tab.
- `TestGenieToolWindowFactory` creates the tabs and the UI of the TestGenie tool window.

### Bundles

- `TestGenieBundle` is used to load EvoSuite messages in the code (from `messages.TestGenie` file in the `recourses` directory).
- `TestGenieDefaultsBundle` is used to load the default values of the parameters in the code (from `defaults/TestGenie.properties` file in the `resources` directory).
- `TestGenieLabelsBundle` is used to load the text of various UI labels in the code (from `defaults/Labels.properties` file in the `recourses` directory).
- `TestGenieToolTipsBundle` is used to load the text of various tooltips in the code (from `defaults/Tooltips.properties` file in the `resources` directory).

### Translations
The vast majority of labels, tooltip-texts and messages are saved in their own `.properties` files. This is practical if you wish to translate the plugin to a different language - changing these files should translate most of the plugin elements. The only omitted texts are those which require certain properties set to them (e.g. IntelliJ's `.preferredSize`). Those have to be translated in the code. There also exists a `.properties` file for default TestGenie configurations. It is not related to linguistics, but useful if you wish to change default values of the plugin.

## Tests

The tests for TestGenie can be found in `src/test` directory.

- `resources` directory contains dummy projects used for testing the plugin.
- `kotlin/nl/tudelft/ewi/se/ciselab/testgenie` directory contains the actual tests.
    - `helpers` directory contains tests for the method descriptor helper (`MethodDescriptorHelperTest`).
    - `runner` directory contains tests for the settings arguments that are used when running EvoSuite (`SettingsArgumentTest`).
    - `services` directory contains tests for the coverage visualisation and caching service classes (`CoverageVisualisationServiceTest`, `TestCaseCachingServicePropertyBasedTest`, `TestCaseCachingServiceTest`).
    - `settings` directory contains unit tests for plugin and EvoSuite settings (`SettingsEvoSuiteConfigurableTest`, `SettingsPluginConfigurableTest`, `TestGenieSettingsStateTest`).
    - `toolwindow` directory contains unit tests for tool window tabs (`QuickAccessParametersStateTest`).
    - `uiTest` directory contains the UI tests.
      - `customfixtures` directory contains the custom fixtures that had to be created for testing.
      - `pages` directory has the frames and fixtures that are used for UI testing (`IdeaFrame`, `QuickAccessParametersFixtures`, `SettingsFrame`, `WelcomeFrame`).
      - `utils` directory contains utility files that are helpful for UI testing (`RemoteRobotExtension`, `StepsLogger`).
      - `tests` directory contains the actual UI tests.
      - `CoverageVisualisationToolWindowTest` contains the UI tests for *Coverage Visualisation* tab in the tool window.
      - `PsiSelectionTest` contains the UI tests for PSI element selection logic when generating tests for class, method and line.
      - `QuickAccessParametersTest` contains the UI tests for *Quick Access Parameters* tab in the tool window.
      - `SettingsComponentTest` contains the UI tests for the Settings page of the plugin (both the plugin settings page and EvoSuite settings page).
