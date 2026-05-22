package Model;

import java.sql.Date;

public class EmployeeScheduleModel {
    private Integer scheduleId;
    private Integer accountId;
    private String employeeName;
    private Date workDate;
    private String shiftType; // "Sáng" or "Chiều"

    public EmployeeScheduleModel() {
    }

    public EmployeeScheduleModel(Integer scheduleId, Integer accountId, String employeeName, Date workDate, String shiftType) {
        this.scheduleId = scheduleId;
        this.accountId = accountId;
        this.employeeName = employeeName;
        this.workDate = workDate;
        this.shiftType = shiftType;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public String getShiftType() {
        return shiftType;
    }

    public void setShiftType(String shiftType) {
        this.shiftType = shiftType;
    }
}
