package dev.codescreen.cancelling;

import dev.codescreen.cancelling.model.OrderTracker;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

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

    static Map<String, Thread> threads = new ConcurrentHashMap<>();

    static List<String> companiesInvolvedInExcessiveCancellations() {
        Set<String> eliminatedCompanies = ConcurrentHashMap.newKeySet();

        Scanner reader = new Scanner(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("Trades.data")));
        String line = reader.nextLine();
        String[] data = line.split(",");


        Thread t = new Thread(() -> checkForOrders(data[1], 1, eliminatedCompanies));
        threads.put(data[1],t);
        t.start();

        threads.values().forEach( thread -> {
            try {
                thread.join();
            }catch (InterruptedException ex){}
        });

        return new ArrayList<>(eliminatedCompanies);

    }

    private static void checkForOrders(
            String company,
            int lineNumber,
            Set<String> eliminatedCompanies
    ) {
        int currentLineNumber = 1;
        InputStream is = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("Trades.data"));
        Scanner reader = new Scanner(is);
        OrderTracker tracker = new OrderTracker();


        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            String[] data = line.split(",");
            LocalDateTime recordTime = LocalDateTime.parse(data[0].trim(), format);
            String currentCompany;

            try {
                currentCompany = data[1];
                if (!currentCompany.equals(company) && !threads.containsKey(currentCompany)) {
                    int currentLineCopy = currentLineNumber;
                    Thread processNewCompany = new Thread(() ->
                            checkForOrders(currentCompany, currentLineCopy, eliminatedCompanies));
                    threads.put(currentCompany,processNewCompany);
                    processNewCompany.start();
                } else {
                    if (currentLineNumber == lineNumber) {
                        tracker.windowStart = recordTime;
                        updateOrderCounts(tracker, data);
                    } else if (currentLineNumber > lineNumber) {
                        if (recordTime.isAfter(tracker.windowStart.plusSeconds(58))) {
                            if (!tracker.isFair() || eliminatedCompanies.contains(company)) {
                                eliminatedCompanies.add(company);
                                break;
                            }
                            is.close();
                            break;
                        } else {
                            int lineNo = currentLineNumber;
                            if (tracker.nextWindowStartLineNo < 0 && recordTime.isAfter(tracker.windowStart)) {
                                tracker.nextWindowStartLineNo = currentLineNumber;
                                Thread t = new Thread(() -> checkForOrders(company, lineNo, eliminatedCompanies));
                                t.start();
                                t.join();
                            }
                            updateOrderCounts(tracker, data);
                        }

                    }

                }
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
            currentLineNumber++;
        }

    }

    private static void updateOrderCounts(OrderTracker tracker, String[] data) {
        if (data[2].equals("F")) {
            tracker.cancelledOrders += Integer.parseInt(data[3].trim());
        } else {
            tracker.newOrders += Integer.parseInt(data[3].trim());
        }
    }


    /**
     * Returns the total number of companies that are not involved in any excessive cancelling.
     */
    static int totalNumberOfWellBehavedCompanies() {
        return wellBehavedCount.intValue();
    }

}
