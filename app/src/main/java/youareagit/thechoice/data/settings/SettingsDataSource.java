/*
 * The MIT License (MIT)
 *
 *  Copyright (c) 2016 Martin Bölter
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package youareagit.thechoice.data.settings;

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
 * corresponding <code>Setting</code> object variables and vice versa.
 *
 * @author M
 *
 */
public class SettingsDataSource {
    // Database fields
    /** The database to use. */
    private SQLiteDatabase database;
    /** Database helper. */
    private SettingsSQLiteHelper dbHelper;
    /**
     * Stores all (relevant) columns of the setting database so they can be
     * queried.
     */
    private String[] allColumns = { SettingsSQLiteHelper.COLUMN_ID,
            SettingsSQLiteHelper.COLUMN_NAME,
            SettingsSQLiteHelper.COLUMN_VALUE };

    /**
     * Constructor.
     *
     * @param context
     *            The <code>Context</code> to use.
     */
    public SettingsDataSource(Context context) {
        dbHelper = new SettingsSQLiteHelper(context);
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
     * Creates a new <code>Setting</code> object with the given values, new
     * id from the database and also inserts the new data into the database
     * under the id if the setting name did not already exist.
     *
     * @param name
     *            The name of the setting.
     * @param value
     *            The value of the setting.
     * @return The newly created <code>Setting</code> object with the given
     *         values and a new id or <code>null</code> if the setting
     *         already existed.
     * @throws SQLiteConstraintException
     *             Is thrown if the setting name already exists.
     */
    public Setting createSetting(String name, String value)
            throws SQLiteConstraintException {
        ContentValues values = new ContentValues();
        Setting newSetting = null;

        // Add the arguments
        values.put(SettingsSQLiteHelper.COLUMN_NAME, name);
        values.put(SettingsSQLiteHelper.COLUMN_VALUE, value);
        // Store the new valid ID value generated by the INSERT statement
        long insertId = database.insert(
                SettingsSQLiteHelper.TABLE_SETTINGS, null, values);
        // Store the cursor belonging to the ID
        Cursor cursor = database.query(
                SettingsSQLiteHelper.TABLE_SETTINGS, allColumns,
                SettingsSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                newSetting = cursorToSetting(cursor);
            }
            cursor.close();
        }
        return newSetting;
    }

    /**
     * Delete the given setting from the database.
     *
     * @param setting
     *            The setting to delete from the database.
     */
    public void deleteSetting(Setting setting) {
        long id = setting.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(SettingsSQLiteHelper.TABLE_SETTINGS,
                SettingsSQLiteHelper.COLUMN_ID + " = " + String.valueOf(id),
                null);
    }

    /**
     * Get a setting from the database by its id.
     *
     * @param id
     *            The id of the setting to search
     * @return Return the found setting with the given id or
     *         <code>null</code>.
     */
    public Setting getSetting(long id) {
        Setting setting = null;
        System.out.println("Comment get Setting with id: " + id);

        // Search the ID in the database and return the (first) cursor entry
        Cursor cursor = getSettingCursor(id);
        setting = cursorToSetting(cursor);
        // make sure to close the cursor
        cursor.close();
        // Return the found setting by its id or null
        return setting;
    }

    /**
     * Get a cursor for a setting from the database by its id.
     *
     * @param id
     *            The id of the setting to search
     * @return Return the found cursor for the given setting id or
     *         <code>null</code>.
     */
    public Cursor getSettingCursor(long id) {
        System.out.println("Comment get settingCursor with id: " + id);
        String whereClause = SettingsSQLiteHelper.COLUMN_ID + " = ?";
        String settingID = String.valueOf(id);
        String[] whereArgs = new String[] { settingID };

        // Query the database with the specific id as a search key
        Cursor cursor = database.query(
                SettingsSQLiteHelper.TABLE_SETTINGS, allColumns,
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
        // Return the found setting by its id or null
        return cursor;
    }

    /**
     * Return a list of all settings which are stored in the database.
     *
     * @return A list of all stored settings in the database.
     */
    public List<Setting> getAllSettings() {
        List<Setting> settings = new ArrayList<>();
        // Get all settings from the database
        Cursor cursor = database.query(
                SettingsSQLiteHelper.TABLE_SETTINGS, allColumns, null,
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Setting setting = cursorToSetting(cursor);
            settings.add(setting);
            setting.printData();
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return settings;
    }

    /**
     * Returns a setting which is stored in the database by name if it exists.
     *
     * @param settingName
     *            The setting name to search.
     *
     * @return The found <code>Setting</code> object with the specific name from the database
     * or <code>null</code> if the setting did not exist in the database.
     */
    public Setting getSetting(String settingName) {
        Setting setting = null;
        String whereClause = SettingsSQLiteHelper.COLUMN_NAME + "='"
                + settingName + "'";
        // Query the database for the specific setting
        Cursor cursor = database.query(
                SettingsSQLiteHelper.TABLE_SETTINGS, allColumns,
                whereClause, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            setting = cursorToSetting(cursor);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return setting;
    }

    /**
     * Transforms data from a database query to a new <code>Setting</code>
     * object with the query result values.
     *
     * @param cursor
     *            The database query result.
     * @return A new <code>Setting</code> object with set values from the
     *         database query result or <code>null</code> if the given cursor
     *         was empty.
     */
    private Setting cursorToSetting(Cursor cursor) {
        Setting setting = null;

        if (cursor != null && cursor.getCount() > 0) {
            setting = new Setting();
            setting.setId(cursor.getLong(0));
            setting.setName(cursor.getString(1));
            setting.setValue(cursor.getString(2));
        }
        return setting;
    }

    /**
     * Update the setting entry in the database via the ID of the given newly
     * manipulated setting.
     *
     * @param setting
     *            The manipulated <code>Setting</code> object to update in
     *            the database.
     * @throws SQLiteConstraintException
     *             Is thrown if the setting name already exists.
     */
    public void updateSetting(Setting setting) {
        if (setting != null) {
            System.out.print("Entry to UPDATE: ");
            getSetting(setting.getId()).printData();
            System.out.print("New entry: ");
            setting.printData();
            // Set all values
            ContentValues values = setting.getAllContentValues();

            // Do the database update
            database.update(
                    SettingsSQLiteHelper.TABLE_SETTINGS,
                    values,
                    SettingsSQLiteHelper.COLUMN_ID + " = "
                            + setting.getId(), null);
        }
    }

    /**
     * Deletes all setting entries in the database and list.
     */
    public void deleteAllSettings() {
        emptySettingsTable();
    }

    /**
     * Empty the settings table so that all entries are deleted.
     */
    public void emptySettingsTable() {
        // TODO Exchange the code so that a table is just updated and filled
        // with null???
        database.execSQL("DROP TABLE IF EXISTS "
                + SettingsSQLiteHelper.TABLE_SETTINGS);
        dbHelper.onCreate(database);
    }
}