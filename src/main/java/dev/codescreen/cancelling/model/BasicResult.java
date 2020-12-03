package dev.codescreen.cancelling.model;

import java.util.HashSet;
import java.util.Set;

public class BasicResult implements Result {

    private Set<String> processedCompanies;
    private Set<String> eliminatedCompanies;
    private Set<String> wellBehavedCompanies;


    public BasicResult(Set<String> processedCompanies, Set<String> eliminatedCompanies) {
        this.processedCompanies = processedCompanies;
        this.eliminatedCompanies = eliminatedCompanies;

        Set<String > processedCompaniesCopy = new HashSet<>(processedCompanies);
        processedCompaniesCopy.removeAll(eliminatedCompanies);

        this.wellBehavedCompanies = processedCompaniesCopy;
    }

    public Set<String> getProcessedCompanies() {
        return processedCompanies;
    }

    public void setProcessedCompanies(Set<String> processedCompanies) {
        this.processedCompanies = processedCompanies;
    }

    public Set<String> getEliminatedCompanies() {
        return eliminatedCompanies;
    }

    public void setEliminatedCompanies(Set<String> eliminatedCompanies) {
        this.eliminatedCompanies = eliminatedCompanies;
    }

    public long getWellBehavedCount() {
        return wellBehavedCompanies.size();
    }

    public void setWellBehavedCompanies(Set<String> wellBehavedCompanies) {
        this.wellBehavedCompanies = wellBehavedCompanies;
    }
}
