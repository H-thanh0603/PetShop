package Model;

import java.util.List;

public class ProductFilterCriteria {
    private String category;
    private String searchKeyword;
    private String sort;
    private String priceRange;
    private boolean discountOnly;
    private String petTypeCode;
    private List<String> brands;
    private boolean availabilityOnly;
    private int page = 1;
    private int pageSize = 12;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public boolean isDiscountOnly() {
        return discountOnly;
    }

    public void setDiscountOnly(boolean discountOnly) {
        this.discountOnly = discountOnly;
    }

    public String getPetTypeCode() {
        return petTypeCode;
    }

    public void setPetTypeCode(String petTypeCode) {
        this.petTypeCode = petTypeCode;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<String> getBrands() {
        return brands;
    }

    public void setBrands(List<String> brands) {
        this.brands = brands;
    }

    public boolean isAvailabilityOnly() {
        return availabilityOnly;
    }

    public void setAvailabilityOnly(boolean availabilityOnly) {
        this.availabilityOnly = availabilityOnly;
    }
}
