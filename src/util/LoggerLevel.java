package util;

public enum LoggerLevel {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");

    private String text;

    public String getText() {
        return this.text;
    }

    private LoggerLevel(String text) {
        this.text = text;
    }
}
