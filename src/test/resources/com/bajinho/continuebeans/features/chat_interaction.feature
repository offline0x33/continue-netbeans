Feature: Professional Chat Interaction
  As a developer using the Continue Beans plugin
  I want the chat interaction to be intelligently tailored to my needs
  So that I receive high-quality, context-aware code and plans

  Scenario Outline: Tailored System Instructions for Different Modes
    Given the plugin is configured in "<mode>" mode
    When a chat request is initiated with prompt "Refactor this method"
    Then the outgoing request should include a system prompt emphasizing "<emphasis>"

    Examples:
      | mode     | emphasis                  |
      | Code     | código limpo              |
      | Planning | Planeje antes de codar    |

  Scenario: Resilient Processing of Fragmented JSON Stream
    Given the LLM server returns a fragmented stream of JSON chunks
    When the plugin processes the incoming stream
    Then the individual pieces should be correctly reassembled into a coherent response
    And each text fragment should be passed to the UI callback
