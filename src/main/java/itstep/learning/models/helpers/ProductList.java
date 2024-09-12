package itstep.learning.models.helpers;

import java.util.ArrayList;
import java.util.List;

public class ProductList {
    private List<Product> productList;

    public ProductList() {
        this.productList = new ArrayList<Product>();
        this.productList.add(new Product(0, "Lump", 60));
        this.productList.add(new Product(1, "Chair", 330));
        this.productList.add(new Product(2, "Pen", 3));
        this.productList.add(new Product(3, "Table", 639));
        this.productList.add(new Product(4, "Laptop", 8200));
        this.productList.add(new Product(5, "Mouse", 120));
        this.productList.add(new Product(6, "Keyboard", 270));
        this.productList.add(new Product(7, "Display", 2290));
        this.productList.add(new Product(8, "Bag", 89));
    }
    public List<Product> getProductList() {
        return productList;
    }
}
