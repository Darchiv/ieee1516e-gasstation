package util;

public class Logger {
    private String subsystemName;

    public Logger(String subsystemName) {
        this.subsystemName = subsystemName;
    }

    public void log(String message) {
        this.log(message, LoggerLevel.INFO);
    }

    public void log(String message, LoggerLevel level) {
        System.out.println("[" + level.getText() + "] " + this.subsystemName + ": " + message);
    }
}
