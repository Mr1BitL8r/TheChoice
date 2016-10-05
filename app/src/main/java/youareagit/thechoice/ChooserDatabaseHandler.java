/** The MIT License (MIT)

 Copyright (c) 2015 Martin Bölter

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE. */
package youareagit.thechoice;

import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import youareagit.thechoice.data.ChoiceMethod;
import youareagit.thechoice.data.Item;
import youareagit.thechoice.data.ItemList;
import youareagit.thechoice.data.ItemListItem;
import youareagit.thechoice.data.ItemListsDataSource;
import youareagit.thechoice.data.ItemListsItemsDataSource;
import youareagit.thechoice.data.ItemsDataSource;
import youareagit.thechoice.data.settings.DefaultSetting;
import youareagit.thechoice.data.settings.Setting;
import youareagit.thechoice.data.settings.SettingsDataSource;

class ChooserDatabaseHandler {
    /** The choice method default value which on startup is used for activating the radio button. */
    static final ChoiceMethod CHOICE_METHOD_DEFAULT = ChoiceMethod.THROW_COIN;

    /** The ChooserLogic object which is responsible for doing the random choices. */
    private final ChooserLogic chooserLogic;

    // Values for handling the database connection and entries
    /** Data source for the items. */
    private ItemsDataSource itemsDataSource = null;
    /** The item entries for the selected item list. */
    private List<Item> itemDatabaseValues = null;
    /** Data source for the item lists */
    private ItemListsDataSource itemListsDataSource = null;

    /** The item list entries. */
    private List<ItemList> itemListDatabaseValues = null;
    /** Data source for the item lists */
    private ItemListsItemsDataSource itemListsItemsDataSource = null;
    /** The item list item entries. */
    private List<ItemListItem> itemListItemsDatabaseValues = null;
    /** Data source for storing the settings */
    private SettingsDataSource settingsDataSource = null;
    /** The setting entries. */
    private List<Setting> settingDatabaseValues = null;

    // Create the different Settings objects without initialized values
    /** The default choice method (e.g. throw coin). The value will be set later, e.g.
     * via loading a stored setting from the database or when the user clicks on one the "choice method"  radio buttons. */
    private Setting choiceMethodDefault = new Setting(DefaultSetting.CHOICEMETHOD.toString());
    /** The custom dice maximum range value <code>Setting</code>. It will be overwritten if the
     * user used a valid value before which got stored in the database or if he chose a new value
     * in a custom dice throw. */
    private Setting customDiceMaximumRangeValueDefault = new Setting(DefaultSetting.CUSTOM_DICE_MAXIMUM_RANGE_VALUE.toString(), Integer.toString(ChooserLogic.CUSTOM_DICE_MAXIMUM_RANGE_VALUE_DEFAULT));
    //TODO: Store the value in a database entry if a user selected one
    /** Default list name for the choice method from list. */
    private Setting listNameDefault = new Setting(DefaultSetting.CHOICEMETHOD_FROM_LIST_LISTNAME.toString());

    /**
     * Constructor for the <code>ChooserDatabaseHandler</code> object.
     * @param appCompatActivity The <code>AppCompatActivity</code> to use.
     */
    ChooserDatabaseHandler(ChooserLogic chooserLogic, AppCompatActivity appCompatActivity){
        // Set the ChooserLogic object which stores the custom dice maximum range value
        this.chooserLogic = chooserLogic;
        // Acquire the database connection for the item lists
        itemListsDataSource = new ItemListsDataSource(appCompatActivity);
        // Acquire the database connection for the items
        itemsDataSource = new ItemsDataSource(appCompatActivity);
        // Acquire the database connection for the items
        itemListsItemsDataSource = new ItemListsItemsDataSource(appCompatActivity);
        // Acquire the database connection for the Settings
        settingsDataSource = new SettingsDataSource(appCompatActivity);
        // Open and load all entries for the database connections
        openDatabaseConnectionsAndLoadOrCreateDefaultEntries();
    }

    /**
     * Choose an item and return its text.
     * @param itemListName The name of the list to choose an item from.
     * @return The text of the chosen item or <code>null</code> if there were no entries in the database.
     */
    String chooseFromItemList(String itemListName) {
        String chosenResultValue = null;
        // Resolve the item list via the item list name
        ItemList itemList = itemListsDataSource.getItemList(itemListName);

        // Search all item IDs belonging to the item list
        List<Long> itemIds = itemListsItemsDataSource.getItemIds(itemList.getId());
        // Check if there are entries in the database under the item list name
        if (itemIds != null
                && itemIds.size() > 0) {
            // Get a random index of the current item list entry in the database
            // (updated in the onResume() method)
            int randomItemIndex = chooserLogic.getRandomNumberInRange(itemIds.size());

            // Resolve the item id of that entry and query the database for that item
            final long itemId = itemIds.get(randomItemIndex);
            final Item item = itemsDataSource.getItem(itemId);
            // Set the result to the item name
            chosenResultValue = item.getName();
        }
        return chosenResultValue;
    }

    /**
     * Open all relevant database connections, e.g. the one for settings and load all entries (or
     * create default entries if they did not exist and load them) into the corresponding list
     * variables.
     */
    void openDatabaseConnectionsAndLoadOrCreateDefaultEntries() {
        // Open the database connection for the ItemLists
        itemListsDataSource.open();
        // Update the list of items
        itemListDatabaseValues = itemListsDataSource.getAllItemLists();
        // Open the database connection for the Items
        itemsDataSource.open();
        // Update the list of items
        itemDatabaseValues = itemsDataSource.getAllItems();
        // Open the database connection for the ItemListItems
        itemListsItemsDataSource.open();
        // Update the list of items
        itemListItemsDatabaseValues = itemListsItemsDataSource.getAllItemListsItems();
        // Open the database connection for the Settings
        settingsDataSource.open();
        // Get a list of the settings from the database
        settingDatabaseValues = settingsDataSource.getAllSettings();
        // Create all necessary settings if they have not been already stored in the database
        if(settingDatabaseValues == null || settingDatabaseValues.isEmpty()){
            // Create the Setting for the custom dice maximum range value
            customDiceMaximumRangeValueDefault = settingsDataSource.createSetting(DefaultSetting.CUSTOM_DICE_MAXIMUM_RANGE_VALUE.toString(), Integer.toString(ChooserLogic.CUSTOM_DICE_MAXIMUM_RANGE_VALUE_DEFAULT));
            // Create the Setting for the default choice method
            choiceMethodDefault = settingsDataSource.createSetting(DefaultSetting.CHOICEMETHOD.toString(), CHOICE_METHOD_DEFAULT.toString());
            //TODO: Initialize default list name value with a correct value, null might not be the best idea...
            // Create the Setting for the default list name entry
            listNameDefault = settingsDataSource.createSetting(DefaultSetting.CHOICEMETHOD_FROM_LIST_LISTNAME.toString(), null);
            // Reload the Settings entries from the database because they should be added now
            settingDatabaseValues = settingsDataSource.getAllSettings();
        } else { // Old settings where stored in the settings database
            // Link the correct Setting object to the corresponding variable
            customDiceMaximumRangeValueDefault = settingsDataSource.getSetting(DefaultSetting.CUSTOM_DICE_MAXIMUM_RANGE_VALUE.toString());
            choiceMethodDefault = settingsDataSource.getSetting(DefaultSetting.CHOICEMETHOD.toString());
            listNameDefault = settingsDataSource.getSetting(DefaultSetting.CHOICEMETHOD_FROM_LIST_LISTNAME.toString());

            // Avoid a possible null values due to old incompatible database values
            //  for the custom dice maximum range value
            if (customDiceMaximumRangeValueDefault == null) {
                // Create the Setting for the custom dice maximum range value
                customDiceMaximumRangeValueDefault = settingsDataSource.createSetting(DefaultSetting.CUSTOM_DICE_MAXIMUM_RANGE_VALUE.toString(), Integer.toString(ChooserLogic.CUSTOM_DICE_MAXIMUM_RANGE_VALUE_DEFAULT));
            }
            //  for the choice method default value
            if (choiceMethodDefault == null) {
                // Create the Setting for the default choice method
                choiceMethodDefault = settingsDataSource.createSetting(DefaultSetting.CHOICEMETHOD.toString(), CHOICE_METHOD_DEFAULT.toString());
            }
            //  for the list_listname
            if (listNameDefault == null) {
                //TODO: Initialize default list name value with a correct value, null might not be the best idea...
                // Create the Setting for the default list name entry
                listNameDefault = settingsDataSource.createSetting(DefaultSetting.CHOICEMETHOD_FROM_LIST_LISTNAME.toString(), null);
            }
            // Reload the Settings entries from the database because they should be added now
            settingDatabaseValues = settingsDataSource.getAllSettings();
        }
    }

    /**
     * Store all setting which are set in the database.
     */
    private void storeSettingsInDatabase(){
        // Get the newest custom dice maximum range value and save it in the corresponding setting
        customDiceMaximumRangeValueDefault.setValue(Integer.toString(chooserLogic.getLastCustomDiceMaximumRangeValue()));

        // Update the setting for the default custom dice maximum value range
        settingsDataSource.updateSetting(customDiceMaximumRangeValueDefault);
        // Update the setting for the default choice method
        settingsDataSource.updateSetting(choiceMethodDefault);
        // Update the setting for the default list name
        settingsDataSource.updateSetting(listNameDefault);
    }

    /**
     * Close all database connections.
     */
    void closeDatabaseConnections(){
        // Store the Settings in the database
        storeSettingsInDatabase();
        // TODO: The closing of the database somehow seems to cause a problem when the application is destroyed because the storeSettingsInDatabase method seems to do some work in parallel when the database is already getting closed by the commented line below
        //settingsDataSource.close();

        // Close all database connections after the Settings where saved
        itemListsDataSource.close();
        itemsDataSource.close();
        itemListsItemsDataSource.close();
    }

    /**
     * Helper function for only returning the names of the item lists.
     * @return The names of the item lists as a <code>String</code> or an empty list if nothing
     * was found.
     */
    List<String> getItemListNames() {
        List<String> itemListNames = new ArrayList<>();
        // Walk over all found database values for itemlists and add only the list names to the returned list
        for (ItemList itemListEntry:itemListDatabaseValues) {
            itemListNames.add(itemListEntry.getListName());
        }
        return itemListNames;
    }

    /**
     * Getter for the item list entries.
     * @return The item list entries as a <code>List<ItemList></code>.
     */
    List<ItemList> getItemLists() {
        return itemListDatabaseValues;
    }

    /**
     * Returns the default custom dice maximum range value as a <code>String</code> or
     * <code>null</code> if the value was not set.
     * @return The default custom dice maximum range value as a <code>String</code> or
     * <code>null</code> if the value was not set.
     */
    String getCustomDiceMaximumRangeValueDefault(){
        return customDiceMaximumRangeValueDefault.getValue();
    }

    /**
     * Returns the stored default choice method or <code>null</code> if none was set.
     * @return The stored default choice method as a <code>ChoiceMethod</code>
     * or <code>null</code> if none was set.
     */
    ChoiceMethod getChoiceMethodDefault() {
        ChoiceMethod defaultChoiceMethodValue = null;
        String defaultChoiceMethodValueAsString = choiceMethodDefault.getValue();
        // Check if the stored value is not null and try to convert it to a ChoiceMethod enum value
        if(defaultChoiceMethodValueAsString != null){
            defaultChoiceMethodValue = ChoiceMethod.valueOf(defaultChoiceMethodValueAsString);
        }
        return defaultChoiceMethodValue;
    }

    /**
     * Set the value of the setting object accordingly so that it can be stored in the database
     * when closing the app
     * @param choiceMethodDefault The choice method to set as default as a <code>ChoiceMethod</code>.
     */
    void setChoiceMethodDefault(ChoiceMethod choiceMethodDefault){
        this.choiceMethodDefault.setValue(choiceMethodDefault.toString());
    }
}
