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

class ItemListsSQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String TABLE_ITEMLISTS = "item_lists";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_LISTNAME = "list_name";
    /**
     * The SQL statement for creating the table with a unique constraint on the
     * item list name.
     */
    private static final String DATABASE_CREATE_ITEMLISTS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_ITEMLISTS + " (" + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_LISTNAME + " TEXT, " + "CONSTRAINT unq UNIQUE ("
            + COLUMN_LISTNAME + "));";

    public ItemListsSQLiteHelper(Context context, String name,
                                 CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    ItemListsSQLiteHelper(Context context) {
        super(context, TABLE_ITEMLISTS, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create the two databases. One for the item list names and one for the item list name and
        // item name combination
        database.execSQL(DATABASE_CREATE_ITEMLISTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Log.w(ItemListsSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // TODO Exchange the code so that a table is just updated and filled with null
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMLISTS);
        onCreate(database);
    }
}
