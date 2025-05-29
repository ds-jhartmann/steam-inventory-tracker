package com.hinderegger.steaminventorytracker.service.strategy;

import com.hinderegger.steaminventorytracker.model.Item;
import java.util.List;

/**
 * Strategy interface for different ways of requesting item data from Steam.
 * This follows the Strategy design pattern to allow for different implementations
 * of how to request and process items.
 */
public interface SteamRequestStrategy {
    
    /**
     * Requests data for a list of items from Steam.
     *
     * @param items The items to request data for
     */
    void requestItems(List<Item> items);
}