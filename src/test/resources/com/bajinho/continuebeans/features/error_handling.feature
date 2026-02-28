Feature: System Resilience and Error Handling
  As a developer using the Continue Beans plugin
  I want the system to handle failures gracefully
  So that my IDE remains stable and I am clearly informed of connectivity issues

  Scenario: Graceful Handling of API Server Error
    Given the LM Studio server is returning a 500 internal error
    When I request a code explanation
    Then the plugin should catch the exception
    And it should provide a professional error message starting with "Erro HTTP 500"

  Scenario: Connection Timeout Management
    Given the LM Studio server is unresponsive
    When a request is made with a short timeout
    Then the system should trigger a timeout exception
    And it should not hang the IDE thread
