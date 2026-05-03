package Service;

import DatabaseAccessObject.ProductDAO;
import Model.ProductListModel;
import Model.VariantModel;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ProductService {
    private ProductDAO productDAO;
    
    public ProductService(){
        productDAO = new ProductDAO();
    }
    
    public ProductListModel getProductList(){
        return productDAO.getAllProduct();
    }
    
    public File chooseImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh để lưu vào Database");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    public void insertProduct(String categoryName, String productName, long dineInPrice, long takeawayPrice, long holidayPrice, double vat, File imageFile, 
                              String status, String description, List<VariantModel> listVariants, List<Integer> listToppingIds){
        productDAO.insertProduct(categoryName, productName, dineInPrice, takeawayPrice, holidayPrice, vat, imageFile, status, description, listVariants, listToppingIds);
    }
    
    public void updateProduct(int productId, String categoryName, String productName, long dineInPrice, long takeawayPrice, long holidayPrice, double vat, File imageFile, 
                              String status, String description, List<VariantModel> listVariants, List<Integer> listToppingIds){
        productDAO.updateProduct(productId, categoryName, productName, dineInPrice, takeawayPrice, holidayPrice, vat, imageFile, status, description, listVariants, listToppingIds);
    }
    
    public void deleteProduct(int id) throws SQLException, ClassNotFoundException{
        productDAO.deleteProduct(id);
    }
}