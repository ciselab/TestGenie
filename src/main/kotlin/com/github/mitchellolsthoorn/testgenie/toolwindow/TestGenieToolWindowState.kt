package com.github.mitchellolsthoorn.testgenie.toolwindow

/**
 * This class is responsible for persisting the values of the parameters in the Quick Access,
 *   which are also present in the settings menu.
 */
data class TestGenieToolWindowState
    constructor(
        var searchBudget: Int = 60,
        var localSearchBudgetType: LocalSearchBudgetType = LocalSearchBudgetType.TIME,
        var localSearchBudgetValue: Int = 5,
        var stoppingCondition: StoppingCondition = StoppingCondition.MAXTIME,
        var initializationTimeout: Int = 120,
        var minimisationTimeout: Int = 60,
        var assertionTimeout: Int = 60,
        var junitCheckTimeout: Int = 60,
        var population: Int = 50,
        var populationLimit: PopulationLimit = PopulationLimit.INDIVIDUALS
    )

/**
 * This enum contains the types for the search budget value parameter.
 */
enum class LocalSearchBudgetType(private val display: String) {
    TIME("Time"),
    STATEMENTS("Statements"),
    TESTS("Tests"),
    SUITES("Suites"),
    FITNESS_EVALUATIONS("Fitness evaluations");

    /**
     * Returns the display name of the given enum value that is shown in the UI elements.
     */
    override fun toString(): String {
        return display
    }
}

/**
 * This enum contains the type for the stopping condition of the algorithm.
 */
enum class StoppingCondition(private val display: String) {
    MAXSTATEMENTS("Max statements"),
    MAXTESTS("Max tests"),
    MAXTIME("Max time"),
    MAXGENERATIONS("Max generations"),
    MAXFITNESSEVALUATIONS("Max fitness evaluations"),
    TIMEDELTA("Time delta");

    /**
     * Returns the display name of the given enum value that is shown in the UI elements.
     */
    override fun toString(): String {
        return display
    }
}

/**
 * This enum contains the type for the limit for the population size.
 */
enum class PopulationLimit(private val display: String) {
    INDIVIDUALS("Individuals"),
    TESTS("Tests"),
    STATEMENTS("Statements");

    /**
     * Returns the display name of the given enum value that is shown in the UI elements.
     */
    override fun toString(): String {
        return display
    }
}