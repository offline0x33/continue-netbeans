Feature: Enterprise Resiliency and Context Optimization
  As a professional AI assistant
  I want to be resilient to network glitches and smart about my context window
  So that I provide a consistent and cost-effective experience

  Scenario: Automatic Retry on Transient Failures
    Given the LM Studio server is return a 429 rate limit error on first try
    And it returns a 200 success on the second try
    When a chat request is initiated
    Then the user should see a successful response
    And the system should have performed 2 API calls

  Scenario: Smart Context Truncation
    Given a massive codebase scan is performed
    When the generated context exceeds 4000 characters
    Then the plugin should truncate the context to fit within limits
    And it should append a note saying "... [Contexto Truncado]"

  Scenario: Multi-Turn Conversation History
    Given a conversation history exists with 2 previous messages
    When a new prompt "Explain more" is sent
    Then the outgoing JSON should include all 3 messages in the correctly ordered array
