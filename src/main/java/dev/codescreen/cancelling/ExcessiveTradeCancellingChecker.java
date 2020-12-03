package dev.codescreen.cancelling;

import dev.codescreen.cancelling.model.OrderTracker;
import dev.codescreen.cancelling.model.OrderType;
import dev.codescreen.cancelling.model.Tick;
import dev.codescreen.cancelling.model.TickFactory;
import dev.codescreen.cancelling.util.DefaultTimeStampStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Checks which companies from the Trades.data are involved in excessive cancelling.
 */
final class ExcessiveTradeCancellingChecker {

    static Long wellBehavedCount = 0L;
    static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-M-dd HH:mm:ss");


    private ExcessiveTradeCancellingChecker() {
    }

    /**
     * Returns the list of companies that are involved in excessive cancelling.
     */


    static Set<String> processedCompanies = ConcurrentHashMap.newKeySet();

    static Set<String> eliminatedCompanies = ConcurrentHashMap.newKeySet();
    static TickFactory tickFactory = new TickFactory(new DefaultTimeStampStrategy());

    static List<String> companiesInvolvedInExcessiveCancellations() {
        try {
            List<String> records = Files.readAllLines(Paths.get("target\\classes\\Trades.data"));
            Tick tick = tickFactory.createTick(records.get(0));
            processedCompanies.add(tick.getCompanyName());
            verifyOrders(records, tick.getCompanyName(), 0, eliminatedCompanies);

        } catch (IOException e) {
            e.printStackTrace();
        }

        processedCompanies.removeAll(eliminatedCompanies);
        wellBehavedCount = processedCompanies.stream().count();
        return new ArrayList<>(eliminatedCompanies);

    }

    private static void verifyOrders(List<String> records, String currentCompany, int lineNum, Set<String> eliminatedCompanies) {

        System.out.println("Processing: " + currentCompany);
        LinkedList<Integer> lineNumbers = new LinkedList<>();
        lineNumbers.add(lineNum);
        List<Thread> threads = new LinkedList<>();


        while (lineNumbers.peek() != null) {
            int lineNumber = lineNumbers.peek();
            lineNumbers.removeFirst();

            OrderTracker tracker = null;

            if (!eliminatedCompanies.contains(currentCompany)) {
                while (lineNumber < records.size()) {
                    Tick tick = tickFactory.createTick(records.get(lineNumber));
                    if (tick != null) {
                        if (tick.getCompanyName().equals(currentCompany)) {
                            if (tracker == null) {
                                tracker = new OrderTracker();
                                tracker.companyName = tick.getCompanyName();
                                tracker.windowStart = tick.getTimeStamp();
                                updateOrderCounts(tracker, tick);
                            } else {

                                if (tick.getTimeStamp().equals(tracker.windowStart)) {
                                    updateOrderCounts(tracker, tick);
                                } else {
                                    if (validateTimeExpiry(tracker, tick)) {
                                        if (!tracker.isFair()) {
                                            eliminatedCompanies.add(currentCompany);
                                        }
                                        break;
                                    } else {
                                        updateOrderCounts(tracker, tick);
                                        if (lineNumbers.size() > 0) {
                                            if (lineNumbers.getLast() < lineNumber) {
                                                lineNumbers.add(lineNumber);
                                            }
                                        } else {
                                            lineNumbers.add(lineNumber);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (!processedCompanies.contains(tick.getCompanyName())) {
                                int finalLineNumber = lineNumber;
                                processedCompanies.add(tick.getCompanyName());
                                Thread thread = new Thread(() -> {
                                    verifyOrders(records, tick.getCompanyName(), finalLineNumber, eliminatedCompanies);
                                });

                                thread.start();
                                threads.add(thread);
                            }

                            if (tracker != null && validateTimeExpiry(tracker, tick)) {
                                if (!tracker.isFair()) {
                                    eliminatedCompanies.add(currentCompany);
                                }
                                break;
                            }
                        }
                    }


                    lineNumber++;
                }
                if (lineNumbers.size() == 0 && !eliminatedCompanies.contains(currentCompany) && lineNumber < records.size()) {
                    lineNumbers.add(lineNumber);
                }
            } else {
                break;
            }
        }
        threads.forEach(thread -> {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static boolean validateTimeExpiry(OrderTracker tracker, Tick tick) {
        return tick.getTimeStamp().isAfter(tracker.windowStart.plusSeconds(60));
    }

    private static void updateOrderCounts(OrderTracker tracker, Tick tick) {
        if (tick.getOrderType() == OrderType.CANCEL) {
            tracker.cancelledOrders += tick.getCount();
        }

        tracker.totalOrders += tick.getCount();
    }


    /**
     * Returns the total number of companies that are not involved in any excessive cancelling.
     */
    static int totalNumberOfWellBehavedCompanies() {
        return wellBehavedCount.intValue();
    }

}
