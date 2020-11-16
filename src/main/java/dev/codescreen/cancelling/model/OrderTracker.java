package dev.codescreen.cancelling.model;

import java.time.LocalDateTime;

public class OrderTracker {
    public int cancelledOrders = 0;
    public int newOrders = 0;

    public LocalDateTime windowStart = null;

    public int nextWindowStartLineNo = -1;

    public Boolean isFair(){
        if(newOrders == 0){
            return false;
        }
        return cancelledOrders / newOrders < 1D / 3;
    }
}
