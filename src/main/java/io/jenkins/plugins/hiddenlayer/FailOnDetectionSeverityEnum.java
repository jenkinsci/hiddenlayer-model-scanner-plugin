package io.jenkins.plugins.hiddenlayer;

public enum FailOnDetectionSeverityEnum {
    NONE("Don't Fail Build"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private String value;

    private FailOnDetectionSeverityEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}
