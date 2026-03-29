package Model;

public class HomeCategoryCard {
    private String name;
    private String iconKey;
    private String href;

    public HomeCategoryCard(String name, String iconKey, String href) {
        this.name = name;
        this.iconKey = iconKey;
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getHref() {
        return href;
    }
}
