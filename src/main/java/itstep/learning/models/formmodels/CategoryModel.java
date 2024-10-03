package itstep.learning.models.formmodels;

public class CategoryModel {
    private String name;
    private String description;
    private String savedFileName;
    private String slug;

    public CategoryModel(){}


    public String getSlug(){
        return slug;
    }
    public CategoryModel setSlug(String slug){
        this.slug = slug;
        return this;
    }

    public String getName() {
        return name;
    }

    public CategoryModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CategoryModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSavedFileName() {
        return savedFileName;
    }

    public CategoryModel setSavedFileName(String savedFileName) {
        this.savedFileName = savedFileName;
        return this;
    }
}
