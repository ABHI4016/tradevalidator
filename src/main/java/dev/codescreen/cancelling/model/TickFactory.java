package dev.codescreen.cancelling.model;

import dev.codescreen.cancelling.util.TimeStampStrategy;

public class TickFactory {
    private TimeStampStrategy timeStampStrategy;

    public TickFactory(TimeStampStrategy timeStampStrategy) {
        this.timeStampStrategy = timeStampStrategy;
    }

    public Tick createTick(String record) {
        String[] recordElements = record.split(",");
        if (recordElements.length < 4)
            return null;

        Tick tick = new Tick();
        tick.setTimeStamp(timeStampStrategy.getTimeStamp(recordElements[0].trim()));
        tick.setCompanyName(recordElements[1].trim().trim());
        tick.setOrderType(recordElements[2].trim().equalsIgnoreCase("F")?OrderType.CANCEL: OrderType.NEW);
        tick.setCount(Integer.parseInt(recordElements[3].trim()));

        return tick;
    }
}
