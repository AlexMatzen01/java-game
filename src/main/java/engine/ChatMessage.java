package engine;

public record ChatMessage(String text, double timestamp) {
    public ChatMessage(String text) {
        this(text, System.currentTimeMillis() / 1000.0);
    }
}
