package dev.codescreen.cancelling.model;

import java.time.LocalDateTime;

public class OrderTracker {
    public String companyName;
    public double cancelledOrders = 0;
    public double totalOrders = 0;

    public LocalDateTime windowStart = null;

    public int nextWindowStartLineNo = -1;

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
}
