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

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import youareagit.thechoice.data.Item;
import youareagit.thechoice.data.ItemsDataSource;

public class ItemDatabaseActivity extends ListActivity {
    private int INVALID_SELECTED_ITEM_POSITION = -1;
    private ArrayAdapter<Item> itemAdapter = null;
    private ItemsDataSource datasource = null;
    private String itemID = null;
    private int selectedItemPosition = INVALID_SELECTED_ITEM_POSITION;
    private EditText editTextItemName = null;
    private EditText editTextItemListName = null;

    /**
     * Create and store the <code>OnItemClickListener</code> for the
     * <code>ListView</code> object. It is called if an item of the
     * <code>ListView</code> object was clicked and returns the id.
     */
    private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // Get the id of the item in the list clicked in
            // We need this so we can update the change later
            itemID = String.valueOf(position);
            selectedItemPosition = position;
            System.out.println("Selected item itemID: " + itemID);
            Item item = (Item) getListAdapter().getItem(
                    selectedItemPosition);
            item.printData();
        }
    };

    /**
     * The <code>OnClickListener</code> for the
     * "Really delete all item entries?" dialog.
     */
    private DialogInterface.OnClickListener reallyDeleteAllEntriesDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Delete all entries in the database and adapter list
                    datasource.deleteAllItems();
                    itemAdapter.clear();
                    initializeAddMode();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // Do nothing
                    break;
            }
        }
    };
    /** The dialog builder for the "Really delete all items?" dialog. */
    private AlertDialog.Builder reallyDeleteAllItemsBuilder;

    /**
     * The <code>OnClickListener</code> for the "Really delete item?"
     * dialog.
     */
    private DialogInterface.OnClickListener reallyDeleteEntryDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Get the item with the selected ID
                    // from the database
                    Item item = (Item) getListAdapter().getItem(
                            selectedItemPosition);
                    System.out.println("Item to DELETE has ID "
                            + item.getId());
                    item.printData();
                    datasource.deleteItem(item);
                    itemAdapter.remove(item);
                    initializeAddMode();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // Do nothing
                    break;
            }
        }
    };
    /** The dialog builder for the "Really delete item?" dialog. */
    private AlertDialog.Builder reallyDeleteItemBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_editor);

        editTextItemName = (EditText) findViewById(R.id.hintTextItemName);
        editTextItemListName = (EditText) findViewById(R.id.hintTextItemListName);

        datasource = new ItemsDataSource(this);
        datasource.open();

        List<Item> items = datasource.getAllItems();

        // use the SimpleCursorAdapter to show the
        // elements in a ListView
        itemAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, items);
        setListAdapter(itemAdapter);

        getListView().setOnItemClickListener(onListClick);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setOnCreateContextMenuListener(
                new OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu,
                                                    View view, ContextMenuInfo menuInfo) {
                        // Set the context menu from the XML file
                        MenuInflater inflater = getMenuInflater();
                        inflater.inflate(R.menu.item_editor_context_menu,
                                menu);

                        // Extract and set the position/index of the list item
                        // so that it can be used for getting the item at
                        // the special position of the adapter
                        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                        selectedItemPosition = info.position;

                        // Get the context menu buttons and set the listeners
                        MenuItem menuItemDeleteItem = menu
                                .findItem(R.id.menuItemDeleteItem);

                        menuItemDeleteItem
                                .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                                    public boolean onMenuItemClick(MenuItem item) {
                                        System.out
                                                .println("ContextCheck: DELETE "
                                                        + selectedItemPosition);
                                        // Check if there are currently
                                        // items in the list
                                        if (getListAdapter().getCount() > 0) {
                                            // Display a re-check dialog
                                            reallyDeleteItemBuilder
                                                    .show();
                                            // Reset the selectedItemPosition

                                        }
                                        return true;
                                    }
                                });
                        MenuItem menuItemEditItem = (menu
                                .findItem(R.id.menuItemEditItem));
                        menuItemEditItem
                                .setIcon(android.R.drawable.ic_menu_upload);// adding
                        // icons
                        menuItemEditItem
                                .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                                    public boolean onMenuItemClick(MenuItem item) {
                                        System.out
                                                .println("ContextCheck: EDIT "
                                                        + selectedItemPosition);
                                        if (getListAdapter().getCount() > 0) {
                                            // Get the item with the
                                            // selected ID
                                            // from the database
                                            Item tempItem = (Item) getListAdapter()
                                                    .getItem(
                                                            selectedItemPosition);

                                            initializeEditMode();

                                            System.out
                                                    .print("Item to EDIT is: ");
                                            tempItem.printData();
                                        }
                                        return true;
                                    }
                                });
                    }
                });
        // Set the builder for the "Really delete all items?" dialog
        reallyDeleteAllItemsBuilder = new AlertDialog.Builder(this)
                .setMessage(
                        getResources()
                                .getString(
                                        R.string.dialog_text_really_delete_all_items))
                .setPositiveButton(getResources().getString(R.string.text_yes),
                        reallyDeleteAllEntriesDialogClickListener)
                .setNegativeButton(getResources().getString(R.string.text_no),
                        reallyDeleteAllEntriesDialogClickListener);

        // Set the builder for the "Really delete item?" dialog
        reallyDeleteItemBuilder = new AlertDialog.Builder(this)
                .setMessage(
                        getResources().getString(
                                R.string.dialog_text_really_delete_item))
                .setPositiveButton(getResources().getString(R.string.text_yes),
                        reallyDeleteEntryDialogClickListener)
                .setNegativeButton(getResources().getString(R.string.text_no),
                        reallyDeleteEntryDialogClickListener);
        initializeAddMode();
    }

    /**
     * Will be called via the onClick attribute.
     *
     * @param view
     *            The view in which the onClickEvent occurred.
     */
    public void onClick(View view) {
        Item item = null;
        switch (view.getId()) {
            case R.id.buttonAddItem:
                String itemName = editTextItemName.getText().toString();
                // Check if the item name was set
                if (!"".contentEquals(itemName)) {
                    try {
                        // Save the new item in the database
                        item = datasource.createItem(itemName);
                        if (item != null) {
                            // And add it to the list of items
                            itemAdapter.add(item);
                        }
                    } catch (SQLiteConstraintException sQLiteConstraintException) {
                        // TODO: Do nothing right now
                    }
                }
                String itemListName = editTextItemListName.getText()
                        .toString();
                //TODO: Implement list name feature so that different lists can be created

                break;
            case R.id.buttonSaveItem:
                try{
                    Item itemToEdit = itemAdapter.getItem(selectedItemPosition);
                    // Update the entries with the values from the edit text fields
                    //TODO: Use list name feature correctly
                    //itemToEdit.setLocation(editTextItemListName.getText().toString());
                    itemToEdit.setName(editTextItemName.getText().toString());
                    // Try to update the entry
                    datasource.updateItem(itemToEdit);
                    // Set the mode to add
                    initializeAddMode();
                } catch(SQLiteConstraintException sqLiteConstraintException) {
                    // Do nothing
                }
                break;
            case R.id.buttonCancelEditItem:
                selectedItemPosition = INVALID_SELECTED_ITEM_POSITION;
                initializeAddMode();
                break;
            case R.id.buttonAddDefaultItems:
                final String[] defaultItemNames = new String[] {
                        "Friend's Burger", "Currywurstbude", "VÖB Kantine",
                        "Boge Kantine", "Mauel Bäckerei", "SES Schnellrestaurant (Döner)",
                        "Supermarkt", "Snackmobil", "'Obstladen'", "Pizzeria Il Tartufo" };
                // Create and add all default items if they have
                // not already been added
                for (String defaultItemName : defaultItemNames) {
                    try {
                        Item defaultItem = null;
                        defaultItem = datasource.createItem(defaultItemName);
                        if (defaultItem != null) {
                            itemAdapter.add(defaultItem);
                        }
                    } catch (SQLiteConstraintException sQLiteConstraintException) {
                        // Do nothing because the default entry already
                        // exists
                    }
                }
                System.out.println("Got called buttonAddDefaultItems");
                break;
            case R.id.deleteAllItems:
                if (getListAdapter().getCount() > 0) {
                    // Display a re-check dialog
                    reallyDeleteAllItemsBuilder.show();
                }
                break;
        }
        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.item_editor_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_back_to_choose_item) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        datasource.close();
    }

    /**
     * De- and enable specific buttons for the add mode and empty the edit text fields.
     */
    void initializeAddMode() {
        // Empty the selected item
        selectedItemPosition = INVALID_SELECTED_ITEM_POSITION;
        // Empty the edit text fields
        editTextItemName.setText("");
        //TODO: Use list name feature correctly
        //editTextItemListName.setText("");

        // Activate the add button
        Button addItemButton = (Button) findViewById(R.id.buttonAddItem);
        addItemButton.setEnabled(true);
        // Deactivate the save button
        Button saveItemButton = (Button) findViewById(R.id.buttonSaveItem);
        saveItemButton.setEnabled(false);
        // Deactivate the cancel button
        Button cancelItemButton = (Button) findViewById(R.id.buttonCancelEditItem);
        cancelItemButton.setEnabled(false);
    }

    /**
     * De- and enable specific buttons for the edit mode and initialize the edit
     * text fields with the values from the selected item.
     */
    void initializeEditMode() {
        // Set the edit text fields to the values of the selected item
        Item item = (Item) getListView().getAdapter().getItem(selectedItemPosition);
        editTextItemName.setText(item.getName());
        //TODO: Use list name feature correctly
        // editTextItemListName.setText(item.getLocation());

        // Deactivate the add button
        Button addItemButton = (Button) findViewById(R.id.buttonAddItem);
        addItemButton.setEnabled(false);
        // Activate the save button
        Button saveItemButton = (Button) findViewById(R.id.buttonSaveItem);
        saveItemButton.setEnabled(true);
        // Activate the cancel button
        Button cancelItemButton = (Button) findViewById(R.id.buttonCancelEditItem);
        cancelItemButton.setEnabled(true);
    }
}