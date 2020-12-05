package dev.codescreen.cancelling.model;

import java.time.LocalDateTime;

public class TransactionVolumeTracker {
    private String companyName;
    private double cancelledOrders = 0;
    private  double totalOrders = 0;

    private LocalDateTime windowStart = null;

    public Boolean isFair(){
        if(totalOrders == 0){
            return false;
        }
       if(cancelledOrders / totalOrders > 1D / 3D){
           return false;
       }else {
           return true;
       }
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(double cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public double getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(double totalOrders) {
        this.totalOrders = totalOrders;
    }

    public LocalDateTime getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(LocalDateTime windowStart) {
        this.windowStart = windowStart;
    }
}
