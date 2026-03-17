package Model;

public class Feature {
    private String icon; // Ví dụ: "bx bxs-graduation"
    private String title;
    private String description;

    public Feature(String icon, String title, String description) {
        this.icon = icon;
        this.title = title;
        this.description = description;
    }
    // Getters
    public String getIcon() { return icon; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}