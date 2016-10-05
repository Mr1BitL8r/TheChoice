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

class ItemsSQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String TABLE_ITEMS = "items";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_NAME = "name";
    /**
     * The SQL statement for creating the table with a unique constraint on the
     * item name.
     */
    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_ITEMS + " (" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NAME + " TEXT, "
            + "CONSTRAINT unq UNIQUE (" + COLUMN_NAME + "));";

    public ItemsSQLiteHelper(Context context, String name,
                                   CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    ItemsSQLiteHelper(Context context) {
        super(context, TABLE_ITEMS, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Log.w(ItemsSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // TODO Exchange the code so that a table is just updated and filled with null
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(database);
    }
}
