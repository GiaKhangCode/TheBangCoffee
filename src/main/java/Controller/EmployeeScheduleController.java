package Controller;

import Model.EmployeeScheduleModel;
import Model.AccountModel;
import Service.EmployeeScheduleService;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeScheduleController {
    private EmployeeScheduleService service;

    public EmployeeScheduleController() {
        service = new EmployeeScheduleService();
    }

    public List<EmployeeScheduleModel> getSchedulesByMonthYear(int month, int year) {
        return service.getSchedulesByMonthYear(month, year);
    }

    public List<AccountModel> getAllAccounts() {
        return service.getAllActiveAccounts();
    }

    public List<Integer> getAssignedAccountIds(Date workDate, String shiftType) {
        List<EmployeeScheduleModel> schedules = service.getSchedulesByDateAndShift(workDate, shiftType);
        return schedules.stream().map(EmployeeScheduleModel::getAccountId).collect(Collectors.toList());
    }

    public boolean saveSchedules(Date workDate, String shiftType, List<Integer> accountIds) {
        return service.saveSchedulesForDateAndShift(workDate, shiftType, accountIds);
    }
}
