package dev.codescreen.cancelling.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface TimeStampStrategy {
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-M-dd HH:mm:ss");

    default LocalDateTime getTimeStamp(String timestampString){
        return LocalDateTime.parse(timestampString.trim(), format);
    }
}
