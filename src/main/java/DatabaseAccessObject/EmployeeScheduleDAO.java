package DatabaseAccessObject;

import ConnectDatabase.ConnectionUtils;
import Model.EmployeeScheduleModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class EmployeeScheduleDAO {

    public List<EmployeeScheduleModel> getSchedulesByMonthYear(int month, int year) {
        List<EmployeeScheduleModel> list = new ArrayList<>();
        String sql = "SELECT ES.MaLich, ES.MaTaiKhoan, ND.HoTen, ES.NgayLamViec, ES.LoaiCa " +
                     "FROM LICH_LAM_VIEC ES " +
                     "JOIN TAI_KHOAN TK ON ES.MaTaiKhoan = TK.MaTaiKhoan " +
                     "JOIN NGUOI_DUNG ND ON TK.MaNguoiDung = ND.MaNguoiDung " +
                     "WHERE EXTRACT(MONTH FROM ES.NgayLamViec) = ? AND EXTRACT(YEAR FROM ES.NgayLamViec) = ?";
        
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmployeeScheduleModel model = new EmployeeScheduleModel();
                    model.setScheduleId(rs.getInt("MaLich"));
                    model.setAccountId(rs.getInt("MaTaiKhoan"));
                    model.setEmployeeName(rs.getString("HoTen"));
                    model.setWorkDate(rs.getDate("NgayLamViec"));
                    model.setShiftType(rs.getString("LoaiCa"));
                    list.add(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<EmployeeScheduleModel> getSchedulesByDateAndShift(Date workDate, String shiftType) {
        List<EmployeeScheduleModel> list = new ArrayList<>();
        String sql = "SELECT ES.MaLich, ES.MaTaiKhoan, ND.HoTen, ES.NgayLamViec, ES.LoaiCa " +
                     "FROM LICH_LAM_VIEC ES " +
                     "JOIN TAI_KHOAN TK ON ES.MaTaiKhoan = TK.MaTaiKhoan " +
                     "JOIN NGUOI_DUNG ND ON TK.MaNguoiDung = ND.MaNguoiDung " +
                     "WHERE ES.NgayLamViec = ? AND ES.LoaiCa = ?";
        
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, workDate);
            ps.setString(2, shiftType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmployeeScheduleModel model = new EmployeeScheduleModel();
                    model.setScheduleId(rs.getInt("MaLich"));
                    model.setAccountId(rs.getInt("MaTaiKhoan"));
                    model.setEmployeeName(rs.getString("HoTen"));
                    model.setWorkDate(rs.getDate("NgayLamViec"));
                    model.setShiftType(rs.getString("LoaiCa"));
                    list.add(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteSchedulesByDateAndShift(Date workDate, String shiftType) {
        String sql = "DELETE FROM LICH_LAM_VIEC WHERE NgayLamViec = ? AND LoaiCa = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, workDate);
            ps.setString(2, shiftType);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addSchedule(Date workDate, String shiftType, int accountId) {
        String sql = "INSERT INTO LICH_LAM_VIEC (MaTaiKhoan, NgayLamViec, LoaiCa) VALUES (?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setDate(2, workDate);
            ps.setString(3, shiftType);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
