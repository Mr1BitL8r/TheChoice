package youareagit.thechoice.data;

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

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

public class ItemList {
    private long id;
    //TODO: Implement list with items for the SpinnerAdapter
    private List<Item> items = new ArrayList<>();
    private String itemName;
    private String listName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Item> getItemList() {
        return items;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }
    /*
    public void removeItemByItemName(String itemNameToRemove) {
        this.items.remove(item);
    }
    */

    public String getListName() { return listName; }
    public String getItemName() {
        return itemName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    // Will be used by the ArrayAdapter in the Spinner
    @Override
    public String toString() {
        return getListName();
    }

    /**
     * This method uses <code>System.out.println()</code> to print all values of the item.
     */
    public void printData() {
        System.out.println(this.toString() + ", ID: " + this.getId() + ", List entries: " + this.getItemName());
    }

    /**
     * Get all the item values' as a <code>ContentValues</code> object
     * with database column names in their definition order.
     *
     * @return The <code>ContentValues</code> object of the item with
     *         database column names.
     */
    ContentValues getAllContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemListsSQLiteHelper.COLUMN_ID, getId());
        contentValues.put(ItemListsSQLiteHelper.COLUMN_NAME, getItemName());
        contentValues.put(ItemListsSQLiteHelper.COLUMN_LISTNAME,
                getListName());
        
        return contentValues;
    }
}
