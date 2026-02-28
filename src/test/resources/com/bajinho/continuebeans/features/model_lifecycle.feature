Feature: LLM Model Lifecycle Management
  As a professional AI companion
  I want to flawlessly manage the discovery and activation of models
  So that the user always knows which AI is assisting them

  Scenario: Successful Model Discovery
    Given the LM Studio server has models "llama3", "phi2", and "mistral"
    When I request the list of available models
    Then the plugin should identify 3 available models
    And the list should include "llama3"

  Scenario: Transactional Model Loading
    Given the model "llama3" is available but not loaded
    When I command the plugin to load "llama3"
    Then it should initiate an API call to the load endpoint
    And it should return a successful status once confirmed by the server

  Scenario: Handling Empty Model List
    Given the LM Studio server has no models available
    When I request the list of available models
    Then the plugin should return an empty list without crashing
