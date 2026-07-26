package com.lucasbdourado.autotimemarking.modules.automation.domain;

import java.time.LocalTime;
import java.util.List;

/**
 * Domain port interface exposing time clock platform interactions.
 */
public interface TimeClockClient {

    /**
     * Authenticates with BMAquiosque and retrieves the list of time markings registered today.
     *
     * @param username user login name
     * @param password user login password
     * @return list of local times registered today, sorted chronologically
     * @throws Exception if connection, login, page navigation, or parsing fails
     */
    List<LocalTime> retrieveDailyMarkings(String username, String password) throws Exception;

    /**
     * Authenticates with BMAquiosque and registers a new time marking (punch) for today.
     *
     * @param username user login name
     * @param password user login password
     * @throws Exception if connection, login, or button click verification fails
     */
    void registerMarking(String username, String password) throws Exception;
}
