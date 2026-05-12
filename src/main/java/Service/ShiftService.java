package Service;

import DatabaseAccessObject.ShiftDAO;
import Model.ShiftModel;
import java.sql.SQLException;
import java.util.List;

public class ShiftService {
    
    private ShiftDAO shiftDAO;

    public ShiftService() {
        this.shiftDAO = new ShiftDAO();
    }

    public List<ShiftModel> getAllShifts() throws SQLException, ClassNotFoundException {
        return shiftDAO.getAllShifts();
    }
    
    public List<ShiftModel> getActiveShifts() throws SQLException, ClassNotFoundException {
        return shiftDAO.getActiveShift();
    }

    public boolean isShiftNameExists(String tenCa) throws SQLException, ClassNotFoundException {
        return shiftDAO.isShiftNameExists(tenCa);
    }
    
    public boolean isShiftNameExistsExcludeCurrent(String tenCa, int maCaHienTai) throws SQLException, ClassNotFoundException {
        return shiftDAO.isShiftNameExistsExcludeCurrent(tenCa, maCaHienTai);
    }
    
    // [MỚI] Gọi xuống DAO để check xung đột
    public boolean checkUpdateConflict(int maCa, String newGioBatDau, String newGioKetThuc) throws SQLException, ClassNotFoundException {
        return shiftDAO.checkUpdateConflict(maCa, newGioBatDau, newGioKetThuc);
    }

    public boolean addShift(ShiftModel shift) throws SQLException, ClassNotFoundException {
        return shiftDAO.insertShift(shift);
    }

    public boolean updateShift(ShiftModel shift) throws SQLException, ClassNotFoundException {
         return shiftDAO.updateShift(shift);
    }

    public boolean disableShift(int maCa) throws SQLException, ClassNotFoundException {
        return shiftDAO.disableShift(maCa);
    }

    // --- PHẦN XẾP LỊCH ---
    public List<String[]> getWeeklySchedules(java.time.LocalDate startDate, java.time.LocalDate endDate) throws SQLException, ClassNotFoundException {
        return shiftDAO.getWeeklySchedules(startDate, endDate);
    }

    public void deleteSchedules(java.time.LocalDate startDate, java.time.LocalDate endDate, List<Integer> accountIds) throws SQLException, ClassNotFoundException {
        shiftDAO.deleteSchedules(startDate, endDate, accountIds);
    }

    public void insertSchedule(java.time.LocalDate workDate, int maCa, int maTaiKhoan) throws SQLException, ClassNotFoundException {
        shiftDAO.insertSchedule(workDate, maCa, maTaiKhoan);
    }
}