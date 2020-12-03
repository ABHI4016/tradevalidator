package dev.codescreen.cancelling;

import dev.codescreen.cancelling.model.*;
import dev.codescreen.cancelling.util.TickFactory;
import dev.codescreen.cancelling.util.creationStrategy.DefaultTimeStampStrategy;
import dev.codescreen.cancelling.util.eliminationStrategy.DefaultEliminationStrategy;
import dev.codescreen.cancelling.util.eliminationStrategy.EliminationStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        TickFactory tickFactory = new TickFactory(new DefaultTimeStampStrategy());

        EliminationStrategy eliminationStrategy = new DefaultEliminationStrategy(tickFactory);

        try {
            List<String> records = Files.readAllLines(Paths.get("target\\classes\\Trades.data"));
            Tick tick = tickFactory.createTick(records.get(0));

            Result result = eliminationStrategy.testForElimination(records, tick.getCompanyName());

            if (result instanceof BasicResult) {
                BasicResult basicResult = (BasicResult) result;
                wellBehavedCount = basicResult.getWellBehavedCount();
                return new LinkedList<>(basicResult.getEliminatedCompanies());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LinkedList<>();

    }

    /**
     * Returns the total number of companies that are not involved in any excessive cancelling.
     */
    static int totalNumberOfWellBehavedCompanies() {
        return wellBehavedCount.intValue();
    }

}
