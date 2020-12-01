package dev.codescreen.cancelling;

import dev.codescreen.cancelling.model.OrderTracker;

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


    static List<String> companiesInvolvedInExcessiveCancellations() {
        try {
            List<String> records = Files.readAllLines(Paths.get("target\\classes\\Trades.data"));
            String[] record = records.get(0).split(",");
            processedCompanies.add(record[1]);
            verifyOrders(records, record[1], 0, eliminatedCompanies);

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
                    String[] record = records.get(lineNumber).split(",");
                    if (record.length == 4) {
                        LocalDateTime recordTime = LocalDateTime.parse(record[0].trim(), format);
                        if (record[1].equals(currentCompany)) {
                            if (tracker == null) {
                                tracker = new OrderTracker();
                                tracker.companyName = record[1];
                                tracker.windowStart = recordTime;
                                updateOrderCounts(tracker, record);
                            } else {

                                if (recordTime.equals(tracker.windowStart)) {
                                    updateOrderCounts(tracker, record);
                                } else {
                                    if (recordTime.isAfter(tracker.windowStart.plusSeconds(60))) {
                                        if (!tracker.isFair()) {
                                            eliminatedCompanies.add(currentCompany);
                                        }
                                        break;
                                    } else {
                                        updateOrderCounts(tracker, record);
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

                            if (!processedCompanies.contains(record[1])) {
                                int finalLineNumber = lineNumber;
                                processedCompanies.add(record[1]);
                                Thread thread = new Thread(() -> {
                                    verifyOrders(records, record[1], finalLineNumber, eliminatedCompanies);
                                });

                                thread.start();
                                threads.add(thread);
                            }

                            if (tracker != null && recordTime.isAfter(tracker.windowStart.plusSeconds(60))) {
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

        System.out.println("Completed: " + currentCompany);

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

    private static void updateOrderCounts(OrderTracker tracker, String[] data) {
        int orderCount = Integer.parseInt(data[3].trim());
        if (data[2].equals("F")) {
            tracker.cancelledOrders += orderCount;
        }

        tracker.totalOrders += orderCount;
    }


    /**
     * Returns the total number of companies that are not involved in any excessive cancelling.
     */
    static int totalNumberOfWellBehavedCompanies() {
        return wellBehavedCount.intValue();
    }

}
