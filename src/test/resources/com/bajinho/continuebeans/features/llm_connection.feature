Feature: Context Processing
  As a developer using the Continue Beans plugin
  I want to include file context in my prompts using @file: tags
  So that the LLM has more information to help me

  Scenario: Include local file content in prompt
    Given a file exists at "test_file.txt" with content "Hello BDD World!"
    And the current working directory is the project root
    When I process the prompt "@file:test_file.txt How are you?"
    Then the result should contain "Conteúdo do arquivo @file:test_file.txt:"
    And the result should contain "Hello BDD World!"

  Scenario: Missing file handled gracefully
    Given a file does not exist at "non_existent.txt"
    And the current working directory is the project root
    When I process the prompt "@file:non_existent.txt Tell me about it."
    Then the result should contain "[ERRO: Não foi possível carregar o arquivo: non_existent.txt]"

  Scenario: Codebase scan respects depth limit
    Given a complex project structure with depth 10
    And the current working directory is the project root
    When I process the prompt "@codebase what is this?"
    Then the scan should not exceed depth 5
