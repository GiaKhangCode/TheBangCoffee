package Service;

import DatabaseAccessObject.VariantDAO;
import Model.VariantModel;
import java.util.List;

public class VariantService {
    private VariantDAO variantDAO;
    
    public VariantService (){
        variantDAO = new VariantDAO();
    }
    
    public boolean deleteVariant(int variantId) {
        return variantDAO.deleteVariant(variantId);
    }
    
    public List<VariantModel> getVariantsByProductId (int productId){
        return variantDAO.getVariantsByProductId(productId);
    }
}