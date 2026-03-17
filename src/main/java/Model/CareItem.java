package Model;



public class CareItem {
    private String title;
    private String content; // Chứa nội dung HTML (các thẻ <p>)

    public CareItem(String title, String content) {
        this.title = title;
        this.content = content;
    }
    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
}
