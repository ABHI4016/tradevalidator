package dev.codescreen.cancelling.util.eliminationStrategy;

import dev.codescreen.cancelling.model.*;
import dev.codescreen.cancelling.util.TickFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultEliminationStrategy implements EliminationStrategy {
    private TickFactory tickFactory;
    private long wellBehavedCount = 0;


    public DefaultEliminationStrategy(TickFactory tickFactory) {
        this.tickFactory = tickFactory;
    }

    @Override
    public Result testForElimination(List<String> records, String currentCompany) {
        Set<String> processedCompanies = ConcurrentHashMap.newKeySet();
        Set<String> eliminatedCompanies = ConcurrentHashMap.newKeySet();

        testCompanies(records, currentCompany, 0, processedCompanies, eliminatedCompanies);

        return new BasicResult(processedCompanies, eliminatedCompanies);

    }

    private void testCompanies(List<String> records, String currentCompany,
                               int beginIndex,
                               Set<String> processedCompanies,
                               Set<String> eliminatedCompanies) {
        List<Thread> threads = new ArrayList<>(5);
        if (!eliminatedCompanies.contains(currentCompany)) {
            LinkedList<Integer> lineNumbers = new LinkedList<>();
            lineNumbers.add(beginIndex);

            while (lineNumbers.peek() != null) {
                int lineNumber = lineNumbers.peek();
                lineNumbers.removeFirst();

                OrderTracker tracker = null;
                if (eliminatedCompanies.contains(currentCompany))
                    break;

                while (lineNumber < records.size()) {
                    Tick tick = this.tickFactory.createTick(records.get(lineNumber));
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
                                    testCompanies(
                                            records,
                                            tick.getCompanyName(),
                                            finalLineNumber,
                                            processedCompanies,
                                            eliminatedCompanies
                                    );
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
            }
        }

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                System.out.println("Thread Interrupted");
            }
        });

    }

    private void updateOrderCounts(OrderTracker tracker, Tick tick) {
        if (tick.getOrderType() == OrderType.CANCEL) {
            tracker.cancelledOrders += tick.getCount();
        }

        tracker.totalOrders += tick.getCount();
    }


    private boolean validateTimeExpiry(OrderTracker tracker, Tick tick) {
        return tick.getTimeStamp().isAfter(tracker.windowStart.plusSeconds(60));
    }


}
