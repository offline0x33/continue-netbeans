Feature: API Protocol Contract Compliance
  As an enterprise-grade LLM client
  I want to strictly adhere to OpenAI-compatible protocols
  So that I am compatible with both modern and legacy LM Studio endpoints

  Scenario: Chat API Protocol Compliance
    Given the configured API URL is "http://localhost:1234/api/v1/chat"
    And the plugin is in "Code" mode
    When a request is made with prompt "Fix this bug"
    Then the JSON payload should use the "messages" array format
    And the system role content should be "Você é um AI assistente avançado de programação profissional. Foque em código limpo."

  Scenario: Legacy API Protocol Compliance
    Given the configured API URL is "http://localhost:1234/v1/completions"
    And the plugin is in "Planning" mode
    When a request is made with prompt "Outline the project"
    Then the JSON payload should use a flat "prompt" string format
    And it should be prefixed with "### Instruction:\nVocê é um assistente de programação."
