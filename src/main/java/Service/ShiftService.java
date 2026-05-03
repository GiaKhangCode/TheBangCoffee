package Service;

import DatabaseAccessObject.ShiftDAO;
import Model.ShiftModel;
import java.util.List;

public class ShiftService {
    
    private ShiftDAO shiftDAO;
    
    public ShiftService() {
        this.shiftDAO = new ShiftDAO();
    }
    
    public boolean insertShift(ShiftModel shift) {
        return this.shiftDAO.insertShift(shift);
    }
    
    public boolean updateShift(ShiftModel shift) {
        return this.shiftDAO.updateShift(shift);
    }
    
    public boolean deleteShift(int maCa) {
        return this.shiftDAO.deleteShift(maCa);
    }
    
    public List<ShiftModel> getShiftsByMonthYear(int month, int year) {
        return this.shiftDAO.getShiftsByMonthYear(month, year);
    }
}