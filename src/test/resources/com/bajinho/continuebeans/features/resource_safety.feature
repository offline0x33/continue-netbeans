Feature: Resource Safety and Discovery Filters
  As a secure and performant IDE plugin
  I want the codebase scanner to intelligently exclude irrelevant directories
  So that I avoid processing sensitive data and consuming excessive resources

  Scenario Outline: Directory Exclusion Filters
    Given a project structure containing a "<directory>" folder
    And the current working directory is the project root
    When I process the prompt "@codebase find something"
    Then the output should NOT contain any reference to the "<directory>" folder

    Examples:
      | directory    |
      | .git         |
      | target       |
      | node_modules |

  Scenario: Deep Recursion Protection
    Given a recursive loop is detected (simulated)
    When the scanner reaches depth 6
    Then the scan must terminate immediately
    And no stack overflow should occur
