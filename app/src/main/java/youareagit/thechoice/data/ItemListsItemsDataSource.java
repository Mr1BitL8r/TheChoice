/** The MIT License (MIT)

 Copyright (c) 2016 Martin Bölter

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

package youareagit.thechoice.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * The database transformation class for mapping database values and the
 * corresponding <code>ItemListsItems</code> object variables and vice versa.<br />
 * It is responsible for building pairs of <code>ItemList</code> and <code>ITem</code> object
 * which represent the contents of the item lists.<br />
 * One item can belong to multiple item lists but can only be present in an item list once.
 *
 * @author Martin Bölter
 *
 */
public class ItemListsItemsDataSource {
    // Database fields
    /** The database to use. */
    private SQLiteDatabase database;
    /** Database helper. */
    private ItemListsItemsSQLiteHelper dbHelper;

    /**
     * Stores all (relevant) columns of the item lists items database so they can be
     * queried.
     */
    private String[] allColumns = { ItemListsItemsSQLiteHelper.COLUMN_ITEMLIST_ID,
            ItemListsItemsSQLiteHelper.COLUMN_ITEM_ID,};

    /**
     * Constructor.
     *
     * @param context
     *            The <code>Context</code> to use.
     */
    public ItemListsItemsDataSource(Context context) {
        dbHelper = new ItemListsItemsSQLiteHelper(context);
    }

    /**
     * Open a connection to the database.
     *
     * @throws SQLException
     *             If the database cannot be opened.
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        dbHelper.onCreate(database);
    }

    /**
     * Close the database connection.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Creates a new <code>ItemListsItems</code> object with the given values, new
     * id from the database and also inserts the new data into the database
     * under the id if the item list name and item name combination did not
     * already exist.
     *
     * @param itemListId
     *            The id of the item list database entry.
     * @param itemId
     *            The id of the item database entry.
     * @return The newly created <code>ItemListItem</code> object with the given
     *         values or <code>null</code> if the entry already existed.
     *
     * @throws SQLiteConstraintException
     *             Is thrown if the item list id and item id combination already exists.
     */
    public ItemListItem createItem(long itemListId, long itemId)
            throws SQLiteConstraintException {
        ContentValues values = new ContentValues();
        ItemListItem newItemListItem = null;

        // Add the arguments
        values.put(ItemListsItemsSQLiteHelper.COLUMN_ITEMLIST_ID, itemListId);
        values.put(ItemListsItemsSQLiteHelper.COLUMN_ITEM_ID, itemId);
        // Insert the data via an INSERT statement
       database.insert(
                ItemListsItemsSQLiteHelper.TABLE_ITEMLISTS_ITEMS, null, values);
        // Store the cursor belonging to the item list id and item id combination
        Cursor cursor = database.query(
                ItemListsItemsSQLiteHelper.TABLE_ITEMLISTS_ITEMS, allColumns,
                ItemListsItemsSQLiteHelper.COLUMN_ITEMLIST_ID + " = " + itemListId
                        + " AND " + ItemListsItemsSQLiteHelper.COLUMN_ITEM_ID  + " = " + itemId,
                null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                newItemListItem = cursorToItemListItem(cursor);
            }
            cursor.close();
        }
        return newItemListItem;
    }


    /**
     * Delete the given entry from the database.
     *
     * @param itemListItem
     *            The entry to delete from the database.
     */
    public void deleteItemListItem(ItemListItem itemListItem) {
        long itemListId = itemListItem.getItemListId();
        long itemId = itemListItem.getItemId();

        System.out.println(String.format("ListItemList deleted with listItemId: %d and itemId: %d", itemListId, itemId));
        database.delete(ItemListsItemsSQLiteHelper.TABLE_ITEMLISTS_ITEMS,
                ItemListsItemsSQLiteHelper.COLUMN_ITEMLIST_ID + " = " + itemListId
                        + " AND " + ItemListsItemsSQLiteHelper.COLUMN_ITEM_ID  + " = " + itemId,
                null);
    }

    /**
     * Get an item list item from the database by its item list id and item id combination.
     *
     * @param itemListId
     *            The item list id of the entry to search.
     * @param itemId
     *            The item id of the entry to search.
     * @return Return the found item list item with the given id combination or
     *         <code>null</code>.
     */
    public ItemListItem getItemListItem(long itemListId, long itemId) {
        ItemListItem itemListItem = null;
        System.out.println(String.format("Comment get item list item with listItemId: %d and itemId: %d", itemListId, itemId));

        // Search the ID combination in the database and return the (first) cursor entry
        Cursor cursor = getItemListItemCursor(itemListId, itemId);
        itemListItem = cursorToItemListItem(cursor);
        // make sure to close the cursor
        cursor.close();
        // Return the found entry or null
        return itemListItem;
    }

    /**
     * Get a cursor for an item list item from the database by its item list id and item id combination.
     *
     * @param itemListId
     *            The item list id of the entry to search.
     * @param itemId
     *            The item id of the entry to search.
     * @return Return the found cursor for the given ID combination or
     *         <code>null</code>.
     */
    public Cursor getItemListItemCursor(long itemListId, long itemId) {
        System.out.println(String.format("Comment get itemListItemCursor with listItemId: %d and itemId: %d", itemListId, itemId));
        String whereClause = ItemListsItemsSQLiteHelper.COLUMN_ITEMLIST_ID + " = ? AND "
                + ItemListsItemsSQLiteHelper.COLUMN_ITEM_ID + " = ? ";
        String itemListID = String.valueOf(itemListId);
        String itemID = String.valueOf(itemId);
        String[] whereArgs = new String[] { itemListID, itemID };

        // Query the database with the specific ids as a search key
        Cursor cursor = database.query(
                ItemListsItemsSQLiteHelper.TABLE_ITEMLISTS_ITEMS, allColumns,
                whereClause, whereArgs, null, null, null);
        // Move the cursor to the first position, just in case there was more
        // than one result
        cursor.moveToFirst();
        System.out.print("Found: ");
        for (int i = 0; i < allColumns.length; i++) {
            String columnName = allColumns[i];
            System.out.println(columnName + cursor.getString(i));
        }
        // Make sure to close the cursor
        // cursor.close();
        // Return the found item by its id or null
        return cursor;
    }

    /**
     * Return a list of all item lists items which are stored in the database.
     *
     * @return A list of all stored item lists items in the database.
     */
    public List<ItemListItem> getAllItemListsItems() {
        List<ItemListItem> itemListsItems = new ArrayList<>();
        // Get all entries from the database
        Cursor cursor = database.query(
                ItemListsItemsSQLiteHelper.TABLE_ITEMLISTS_ITEMS, allColumns, null,
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ItemListItem itemListItem = cursorToItemListItem(cursor);
            itemListsItems.add(itemListItem);
            itemListItem.printData();
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return itemListsItems;
    }

    /**
     * Returns a list of items which are stored in the database by the item list ID if it exists.
     *
     * @param itemListId
     *            The item list ID to search items for.
     *
     * @return The found <code>List<Item></Item></code> object for the item list ID from the
     * database or <code>null</code> if the item list ID did not exist in the database.
     */
    public List<Long> getItemIds(long itemListId) {
        List<Long> items = new ArrayList<>();
        ItemListItem currentItemListItem = null;
        String whereClause = ItemListsItemsSQLiteHelper.COLUMN_ITEMLIST_ID + "="
                + itemListId;
        // Query the database for the specific items of the item list
        Cursor cursor = database.query(
                ItemListsItemsSQLiteHelper.TABLE_ITEMLISTS_ITEMS, allColumns,
                whereClause, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            currentItemListItem = cursorToItemListItem(cursor);
            //TODO: Query the correct database for the item ID of the ItemListItem object
            items.add(currentItemListItem.getItemId());

            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        // Return null if no entries where found
        if(items.isEmpty()){
            items = null;
        }
        return items;
    }

    /**
     * Transforms data from a database query to a new <code>ItemListItem</code>
     * object with the query result values.
     *
     * @param cursor
     *            The database query result.
     * @return A new <code>ItemListItem</code> object with set values from the
     *         database query result or <code>null</code> if the given cursor
     *         was empty.
     */
    private ItemListItem cursorToItemListItem(Cursor cursor) {
        ItemListItem itemListItem = null;

        if (cursor != null && cursor.getCount() > 0) {
            itemListItem = new ItemListItem();
            itemListItem.setItemListId(cursor.getLong(0));
            itemListItem.setItemId(cursor.getLong(1));
        }
        return itemListItem;
    }

    /**
     * Update the item list item entry in the database via the ID combination of the given newly
     * manipulated item.
     *
     * @param itemListItem
     *            The manipulated <code>ItemListItem</code> object to update in
     *            the database.
     * @throws SQLiteConstraintException
     *             Is thrown if the item list item ID and item ID combination does not exist.
     */
    public void updateItemListItem(ItemListItem itemListItem) {
        if (itemListItem != null) {
            System.out.print("Entry to UPDATE: ");
            getItemListItem(itemListItem.getItemListId(), itemListItem.getItemId()).printData();
            System.out.print("New entry: ");
            itemListItem.printData();
            // Set all values
            ContentValues values = itemListItem.getAllContentValues();

            // Do the database update
            database.update(
                    ItemListsItemsSQLiteHelper.TABLE_ITEMLISTS_ITEMS,
                    values,
                    ItemListsItemsSQLiteHelper.COLUMN_ITEMLIST_ID + " = "
                            + itemListItem.getItemListId() + " AND "
                    + ItemListsItemsSQLiteHelper.COLUMN_ITEM_ID + " = "
                    + itemListItem.getItemId(), null);
        }
    }

    /**
     * Deletes all entries in the database and list.
     */
    public void deleteAllItems() {
        emptyItemListsItemsTable();
    }

    /**
     * Empty the table so that all entries are deleted.
     */
    public void emptyItemListsItemsTable() {
        // TODO Exchange the code so that a table is just updated and filled
        // with null???
        database.execSQL("DROP TABLE IF EXISTS "
                + ItemListsItemsSQLiteHelper.TABLE_ITEMLISTS_ITEMS);
        dbHelper.onCreate(database);
    }
}
