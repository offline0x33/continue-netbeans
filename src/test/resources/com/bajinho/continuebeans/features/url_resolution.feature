Feature: URL Resolution
  As a developer using the Continue Beans plugin
  I want the plugin to correctly resolve local URLs
  So that connection issues with LM Studio are avoided

  Scenario: Translate localhost to 127.0.0.1
    Given the configured API URL is "http://localhost:1234/v1"
    When the provider resolves the URL
    Then the resolved URL should be "http://127.0.0.1:1234/v1"

  Scenario: Keep non-localhost URLs unchanged
    Given the configured API URL is "http://192.168.1.10:1234/v1"
    When the provider resolves the URL
    Then the resolved URL should be "http://192.168.1.10:1234/v1"
