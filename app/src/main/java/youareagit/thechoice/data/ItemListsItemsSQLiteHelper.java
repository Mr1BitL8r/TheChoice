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

package youareagit.thechoice.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class ItemListsItemsSQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String TABLE_ITEMLISTS_ITEMS = "item_lists_items";
    static final String COLUMN_ITEMLIST_ID = "_itemListId";
    static final String COLUMN_ITEM_ID = "_itemId";

    /**
     * The SQL statement for creating the table with a primary key on the
     * item list id and item id combination.
     */
    private static final String DATABASE_CREATE_ITEMLISTS_ITEMS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_ITEMLISTS_ITEMS + " ("
            + COLUMN_ITEMLIST_ID + " INTEGER NOT NULL, "
            + COLUMN_ITEM_ID + " INTEGER NOT NULL, " + "PRIMARY KEY ("
            + COLUMN_ITEMLIST_ID + ", " + COLUMN_ITEM_ID + "));";

    public ItemListsItemsSQLiteHelper(Context context, String name,
                                      CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    ItemListsItemsSQLiteHelper(Context context) {
        super(context, TABLE_ITEMLISTS_ITEMS, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create the databases
        database.execSQL(DATABASE_CREATE_ITEMLISTS_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Log.w(ItemListsItemsSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // TODO Exchange the code so that a table is just updated and filled with null
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMLISTS_ITEMS);
        onCreate(database);
    }
}
