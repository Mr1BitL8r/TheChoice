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

public class ItemListItem {


    private long itemListId;
    private long itemId;
    //TODO: Implement list with items for the SpinnerAdapter
    private List<Item> items = new ArrayList<>();

    public List<Item> getItemList() {
        return items;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return getItemListId() + ", " + getItemId();
    }

    /**
     * This method uses <code>System.out.println()</code> to print all values.
     */
    public void printData() {
        System.out.println(this.toString());
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
        contentValues.put(ItemListsItemsSQLiteHelper.COLUMN_ITEMLIST_ID, getItemListId());
        contentValues.put(ItemListsItemsSQLiteHelper.COLUMN_ITEM_ID, getItemId());

        return contentValues;
    }

    long getItemId() {
        return itemId;
    }

    void setItemId(long itemId) {
        this.itemId = itemId;
    }

    long getItemListId() {
        return itemListId;
    }

    void setItemListId(long itemListId) {
        this.itemListId = itemListId;
    }
}
