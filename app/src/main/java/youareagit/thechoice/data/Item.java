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

package youareagit.thechoice.data;

import android.content.ContentValues;

public class Item {
    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return getName();
    }

    /**
     * This method uses <code>System.out.println()</code> to print all values of the item.
     */
    public void printData() {
        System.out.println(this.toString() + ", ID: " + this.getId());
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
        contentValues.put(ItemsSQLiteHelper.COLUMN_ID, getId());
        contentValues.put(ItemsSQLiteHelper.COLUMN_NAME, getName());
        
        return contentValues;
    }
}
