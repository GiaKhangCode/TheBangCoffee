package Service;

import DatabaseAccessObject.EmployeeScheduleDAO;
import DatabaseAccessObject.AccountDAO;
import Model.EmployeeScheduleModel;
import Model.AccountModel;
import java.sql.Date;
import java.util.List;

public class EmployeeScheduleService {
    private EmployeeScheduleDAO scheduleDAO;
    private AccountDAO accountDAO;

    public EmployeeScheduleService() {
        scheduleDAO = new EmployeeScheduleDAO();
        accountDAO = new AccountDAO();
    }

    public List<EmployeeScheduleModel> getSchedulesByMonthYear(int month, int year) {
        return scheduleDAO.getSchedulesByMonthYear(month, year);
    }

    public List<EmployeeScheduleModel> getSchedulesByDateAndShift(Date workDate, String shiftType) {
        return scheduleDAO.getSchedulesByDateAndShift(workDate, shiftType);
    }

    public boolean saveSchedulesForDateAndShift(Date workDate, String shiftType, List<Integer> accountIds) {
        // First delete existing schedules for this date and shift
        scheduleDAO.deleteSchedulesByDateAndShift(workDate, shiftType);
        
        // Then add new schedules
        boolean success = true;
        for (Integer accountId : accountIds) {
            boolean added = scheduleDAO.addSchedule(workDate, shiftType, accountId);
            if (!added) {
                success = false;
            }
        }
        return success;
    }

    public List<AccountModel> getAllActiveAccounts() {
        List<AccountModel> allAccounts = accountDAO.getAllAccountsForManagement();
        // Filter active accounts if needed, but for now return all
        return allAccounts;
    }
}
