package dev.codescreen.cancelling.util.eliminationStrategy;

import dev.codescreen.cancelling.model.Result;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface EliminationStrategy {

    Result testForElimination(List<String> records, String currentCompany);
}
