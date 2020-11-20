package dev.codescreen.cancelling;

import dev.codescreen.cancelling.model.OrderTracker;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    static List<String> companiesInvolvedInExcessiveCancellations() {
        Set<String> processedCompanies = ConcurrentHashMap.newKeySet();
        Set<String> eliminatedCompanies = ConcurrentHashMap.newKeySet();

        Queue<String> toBeProcessed = new ConcurrentLinkedQueue<>();
        Map<String, Integer> toBeProcessedLocation = new ConcurrentHashMap<>();


        Scanner reader = new Scanner(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("Trades.data")));
        String line = reader.nextLine();
        String[] data = line.split(",");


        processedCompanies.add(data[1]);
        checkForOrders(data[1], 1, toBeProcessed, toBeProcessedLocation, eliminatedCompanies, processedCompanies);

        while (toBeProcessed.peek() != null) {
            String company = toBeProcessed.poll();
            processedCompanies.add(company);
            int lineNumber = toBeProcessedLocation.get(company);
            checkForOrders(company, lineNumber, toBeProcessed, toBeProcessedLocation, eliminatedCompanies, processedCompanies);
        }

        return new ArrayList<>(eliminatedCompanies);

    }

    private static void checkForOrders(
            String company,
            int lineNumber,
            Queue<String> toBeProcessed,
            Map<String, Integer> toBeProcessedLocation,
            Set<String> eliminatedCompanies,
            Set<String> processedCompanies
    ) {
        System.out.println("Processing: [" + company + "]");
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
                if (currentLineNumber == lineNumber) {
                    if (currentCompany.equals(company)) {
                        tracker.windowStart = recordTime;
                        updateOrderCounts(tracker, data);
                    } else {
                        updateToBeProcessed(toBeProcessed, toBeProcessedLocation, processedCompanies, currentLineNumber, currentCompany);
                    }
                } else if (currentLineNumber > lineNumber) {
                    if (recordTime.isAfter(tracker.windowStart.plusSeconds(60))) {
                        if (!tracker.isFair() || eliminatedCompanies.contains(company)) {
                            eliminatedCompanies.add(company);
                            break;
                        }

                        if (currentCompany.equals(company)) {
                            is.close();
                            break;
                        } else {
                            updateToBeProcessed(toBeProcessed, toBeProcessedLocation, processedCompanies, currentLineNumber, currentCompany);
                        }
                    } else {
                        if (currentCompany.equals(company)) {
                            int lineNo = currentLineNumber;
                            if (tracker.nextWindowStartLineNo < 0 && recordTime.isAfter(tracker.windowStart)) {
                                Thread t = new Thread(() -> checkForOrders(company, lineNo, toBeProcessed, toBeProcessedLocation, eliminatedCompanies, processedCompanies));
                                t.start();
                                t.join();
                            }
                            updateOrderCounts(tracker, data);
                        } else {
                            updateToBeProcessed(toBeProcessed, toBeProcessedLocation, processedCompanies, currentLineNumber, currentCompany);
                        }
                    }

                }
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
            currentLineNumber++;
        }


    }

    private static void updateToBeProcessed(Queue<String> toBeProcessed, Map<String, Integer> toBeProcessedLocation, Set<String> processedCompanies, int currentLineNumber, String currentCompany) {
        if (!toBeProcessed.contains(currentCompany) && !processedCompanies.contains(currentCompany)) {
            toBeProcessed.add(currentCompany);
            toBeProcessedLocation.put(currentCompany, currentLineNumber);
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
