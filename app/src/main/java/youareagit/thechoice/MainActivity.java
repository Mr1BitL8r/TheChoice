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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import youareagit.thechoice.data.ChoiceMethod;
import youareagit.thechoice.data.ItemList;


public class MainActivity extends AppCompatActivity implements
        SensorEventListener {
    /** The shake speed threshold which is used for determining a shake. */
    private static final int SHAKE_SPEED_THRESHOLD = 800;
    /** The time threshold after which a new shake detection check is done. */
    private static final int SHAKE_UPDATE_THRESHOLD_IN_MILLISECONDS = 250;

    // Variables for handling the shake detection
    private SensorManager sensorManager = null;
    private Sensor accelerometer = null;
    private long lastUpdateTimeInMilliSeconds = 0;
    private float lastSensorValueX = 0;
    private float lastSensorValueY = 0;
    private float lastSensorValueZ = 0;

    /** The ChooserDatabaseHandler object which is responsible for storing data and settings. */
    private ChooserDatabaseHandler chooserDatabaseHandler;
    /** The ChooserLogic object which is responsible for random choices and storing the current custom dice maximum range value. */
    private ChooserLogic chooserLogic;

    /** The ItemListAdapter for the Spinner that contains item list entries. */
    private ArrayAdapter<ItemList> itemListAdapter = null;
    /** The Spinner for the item list entries. */
    private Spinner itemListNameSpinner = null;
    /** Define the invalid selected item list entry position. */
    private static int INVALID_SELECTED_ITEM_POSITION = -1;
    /** A buffer for the selected item list index. */
    private int selectedItemPosition = INVALID_SELECTED_ITEM_POSITION;
    /** The item list ID as a <code>String</code>. */
    private String itemListId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: Adjust code or remove it
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        // Get and set the SensorManager and Sensor for the accelerometer (for
        // detecting the shake-motion)
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        // Create a new ChooserLogic object
        chooserLogic = new ChooserLogic();
        // Create a new ChooserDatabaseHandler object which is used for handling database connections, storing
        // settings, choosing and returning results
        chooserDatabaseHandler = new ChooserDatabaseHandler(chooserLogic, this);

        // Get the Spinner for the currently selected item list entry
        itemListNameSpinner = (Spinner) findViewById(R.id.spinner_itemlist_name);
        // Use the Adapter to show the elements in the Spinner
        itemListAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, chooserDatabaseHandler.getItemLists());
        itemListNameSpinner.setAdapter(itemListAdapter);

        // Set the button listener for choosing
        Button buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                choose();
            }
        });

        // Set the text field values to the correct instruction text
        initializeTextFields();
    }

    /**
     * Create and store the <code>OnItemClickListener</code> for the
     * <code>ListView</code> object. It is called if an item of the
     * <code>ListView</code> object was clicked and returns the id.
     */
    private AdapterView.OnItemSelectedListener onSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            // Get the id of the item in the list clicked in
            // We need this so we can update the change later
            itemListId = String.valueOf(position);
            selectedItemPosition = position;
            System.out.println("Selected item list name ID: " + itemListId);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Nothing to do currently
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_edit_items) {
            Intent myIntent = new Intent(this, ItemDatabaseActivity.class);
            startActivityForResult(myIntent, 0);
            return true;
        }
        if (id == R.id.action_edit_itemlists) {
            Intent myIntent = new Intent(this, ItemListDatabaseActivity.class);
            startActivityForResult(myIntent, 0);
            return true;
        }
        if (id == R.id.action_exit) {
            finish();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chooserDatabaseHandler.openDatabaseConnectionsAndLoadOrCreateDefaultEntries();

        // Set the text field values to the correct instruction text
        initializeTextFields();
        // Register the sensor again
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        chooserDatabaseHandler.closeDatabaseConnections();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chooserDatabaseHandler.closeDatabaseConnections();
    }

    /**
     * Is used for closing all database connections and unregistering Handlers, e.g. the
     * SensorManagerListener.
     */
    private void closeDatabaseConnectionsAndUnregisterHandler(){
        chooserDatabaseHandler.closeDatabaseConnections();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // Nothing to do yet
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Handle only an accelerometer event
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Get the current system time
            long currentTimeInMilliSeconds = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((currentTimeInMilliSeconds - lastUpdateTimeInMilliSeconds) > SHAKE_UPDATE_THRESHOLD_IN_MILLISECONDS) {
                // Calculate the time difference between the time now and the
                // last check time
                long timeDifferenceInMilliSeconds = (currentTimeInMilliSeconds - lastUpdateTimeInMilliSeconds);

                // Extract the sensor values for each of the three dimensions
                float sensorValueX = sensorEvent.values[0];
                float sensorValueY = sensorEvent.values[1];
                float sensorValueZ = sensorEvent.values[2];

                // Calculate the speed (regarding the direction) per SECOND for
                // each dimension
                float xSpeed = ((sensorValueX - lastSensorValueX) * 10000)
                        / timeDifferenceInMilliSeconds;
                float ySpeed = ((sensorValueY - lastSensorValueY) * 10000)
                        / timeDifferenceInMilliSeconds;
                float zSpeed = ((sensorValueZ - lastSensorValueZ) * 10000)
                        / timeDifferenceInMilliSeconds;

                // Add the speeds without regarding the direction
                float absoluteSpeed = Math.abs(xSpeed + ySpeed + zSpeed);

                // Check if the shake was fast enough
                if (absoluteSpeed > SHAKE_SPEED_THRESHOLD) {
                    System.out.println("Shake with speed: " + absoluteSpeed);
                    choose();
                }

                // Set the old values to the new ones for the next check
                lastSensorValueX = sensorValueX;
                lastSensorValueY = sensorValueY;
                lastSensorValueZ = sensorValueZ;

                lastUpdateTimeInMilliSeconds = currentTimeInMilliSeconds;
            }
        }
    }

    /**
     * Chooses one item randomly from the specified list from the database, throws a coin or rules
     * a dice or custom dice depending on the RadioButton choice and sets the text field.
     */
    protected void choose() {
        // Get all RadioButtons and check their check state
        final RadioButton radioButtonChooseFromList = (RadioButton) findViewById(R.id.radio_choose_from_list);
        final RadioButton radioButtonThrowCoin = (RadioButton) findViewById(R.id.radio_throw_coin);
        final RadioButton radioButtonRuleDice = (RadioButton) findViewById(R.id.radio_rule_dice);
        final RadioButton radioButtonRuleCustomDice = (RadioButton) findViewById(R.id.radio_rule_custom_dice);

        // Buffer for the choice result
        String choiceResult = null;

        // Check which RadioButton is checked
        if (radioButtonChooseFromList.isChecked()) {
            // Set the value of the setting object accordingly so that it can be stored in the database when closing the app
            chooserDatabaseHandler.setChoiceMethodDefault(ChoiceMethod.FROM_LIST);
            // Get an item text from the item list
            choiceResult = chooseFromItemList();
        } else if (radioButtonThrowCoin.isChecked()) {
            // Set the value of the setting object accordingly so that it can be stored in the database when closing the app
            chooserDatabaseHandler.setChoiceMethodDefault(ChoiceMethod.THROW_COIN);
            choiceResult = throwCoin();
        } else if (radioButtonRuleDice.isChecked()) {
            // Set the value of the setting object accordingly so that it can be stored in the database when closing the app
            chooserDatabaseHandler.setChoiceMethodDefault(ChoiceMethod.RULE_DICE);
            choiceResult = chooserLogic.ruleNormalDice();
        } else if (radioButtonRuleCustomDice.isChecked()) {
            // Set the value of the setting object accordingly so that it can be stored in the database when closing the app
            chooserDatabaseHandler.setChoiceMethodDefault(ChoiceMethod.RULE_CUSTOM_DICE);
            choiceResult = ruleCustomDice();
        } else {
            setTextfieldsForNoChosenChoiceMethod();
        }

        // Set the text field if a choice result text was found
        if(choiceResult != null){
            // Find the text field for the choice result text
            TextView choiceResultTextTextView = (TextView) findViewById(R.id.textViewChoiceResultText);
            // Delete the hint to enable auto resize for the text
            choiceResultTextTextView.setHint("");
            // Overwrite the former text color (might have been red once due to
            // no database entry)
            choiceResultTextTextView.setTextColor(Color.BLACK);
            // Set the choice result text
            choiceResultTextTextView.setText(choiceResult);

            // Find the text field for the second text field
            TextView choiceResultText2TextView = (TextView) findViewById(R.id.textViewChoiceResut2Text);
            // Delete the hint to enable auto resize for the text
            choiceResultText2TextView.setHint("");
            // Overwrite the former text color (might have been red once due to
            // no database entry)
            choiceResultText2TextView.setTextColor(Color.BLACK);
        }
    }

    /**
     * Throw a coin and return the result text.
     * @return The text of the coin throw or <code>null</code> if an error occurred.
     */
    protected String throwCoin() {
        // A coin throw can realistically only be "Heads" or "Tails" ("Coin rim" is not an option here!)
        // Buffer for the coin throw result text
        String choiceResult;
        // Get a Resources object for resolving the coin throw texts
        Resources res = getResources();

        // Get the text for the coin throw, false equals "Heads", true equals "Tails"
        if (chooserLogic.throwCoin()){
            choiceResult = res.getString(R.string.text_coin_heads);
        } else {
            choiceResult = res.getString(R.string.text_coin_tails);
        }
        return choiceResult;
    }

    /**
     * Rule a custom dice and return the result as text.
     * @return The result of ruling a custom dice as a <code>String</code> or <code>null</code>
     * if an error occurred.
     */
    protected String ruleCustomDice() {
        // Get the EditText field and store its text value
        final EditText editTextCustomDiceRange = (EditText) findViewById(R.id.edit_custom_dice_range);
        final String customDiceRangeText = editTextCustomDiceRange.getText().toString();

        // Buffer for the custom dice maximum range value
        int customDiceMaximumRangeValue = chooserLogic.getLastCustomDiceMaximumRangeValue();

        try {
            //Try to convert the text into a number
            Integer tempCustomDiceMaximumRangeValue = Integer.valueOf(customDiceRangeText);
            if (tempCustomDiceMaximumRangeValue != null && tempCustomDiceMaximumRangeValue > 0) {
                int convertedCustomDiceMaximumRangeValue = tempCustomDiceMaximumRangeValue;
                customDiceMaximumRangeValue = convertedCustomDiceMaximumRangeValue;
                // Reset the value of the EditText for the maximum range value
                editTextCustomDiceRange.setText(String.valueOf(convertedCustomDiceMaximumRangeValue));
            } else {
                // Reset the value of the EditText for the maximum range value
                editTextCustomDiceRange.setText(String.valueOf(customDiceMaximumRangeValue));
            }
        } catch (NumberFormatException nfe) {
            // For debugging
            nfe.printStackTrace();
            // Reset the value of the EditText for the maximum range value
            editTextCustomDiceRange.setText(String.valueOf(customDiceMaximumRangeValue));
        }
        // Use a method for simulating the roll of a custom dice with the specified maximum range value and return its result
        return chooserLogic.ruleCustomDice(customDiceMaximumRangeValue);
    }

    /**
     * Choose an item from the currently selected item list and return its text.
     * @return The text of the chosen item or <code>null</code> if there were no entries in
     * the database.
     */
    protected String chooseFromItemList() {
        // Get the currently selected item list name from the corresponding spinner
        final Spinner itemListNameSpinner = (Spinner) findViewById(R.id.spinner_itemlist_name);
        // Buffer for the item name
        String chosenItemName = null;
        if(itemListNameSpinner.getSelectedItem() != null) {
            // Get the text - the item list name
            String itemListName = itemListNameSpinner.getSelectedItem().toString();

            // Try to get a random item name from the selected item list identified via its name
            chosenItemName = chooserDatabaseHandler.chooseFromItemList(itemListName);
            if (chosenItemName == null || chosenItemName.isEmpty()) {
                // No database entries
                // Set the text fields for an empty item list
                setTextfieldsForItemListChoice();
                // Clear it just in case that an empty item list name "" was selected
                chosenItemName = null;
            }
        }
        return chosenItemName;
    }

    /**
     * Initialize the text field values depending on the chosen choice method (via the selected RadioButton).
     * E.g. for no item list entries (database is empty for the chosen list name) or existing entries.
     * For the coin throw and normal dice rule no setting needs to be set, but for the custom dice the maximum value is necessary.
     */
    protected void initializeTextFields() {
        // Set the correct choose RadioButton and corresponding text fields according to the last stored value
        setChoiceMethodRadioButtons(chooserDatabaseHandler.getChoiceMethodDefault());

        // Get and buffer the default custom dice maximum range value from the database
        String customDiceMaximumRangeValue = chooserDatabaseHandler.getCustomDiceMaximumRangeValueDefault();
        // Get the corresponding EditText field and set its text value correctly
        final EditText editTextCustomDiceRange = (EditText) findViewById(R.id.edit_custom_dice_range);
        editTextCustomDiceRange.setText(customDiceMaximumRangeValue);
        // Also set the value for the ChooserLogic object so it can be persisted correctly
        chooserLogic.setLastCustomDiceMaximumRangeValue(Integer.valueOf(customDiceMaximumRangeValue));
    }
    /**
     * Set the text fields for no item list entries (database is empty for the chosen list name)
     * or existing entries.
     */
    protected void setTextfieldsForItemListChoice(){

        if (chooserDatabaseHandler.getItemListNames() != null
                && chooserDatabaseHandler.getItemListNames().size() > 0) {
            setTextfieldsForShakeToChoose();
        } else {
            // No database entries
            setTextfieldsForNoDatabaseEntries();
        }
    }
    /**
     * Set both text fields that normally would be used for displaying the choice result to the
     * "Click on button or shake to choose" text.
     */
    protected void setTextfieldsForShakeToChoose(){
        // Set the hint text with the button texts
        Resources res = getResources();
        String hintPleasePressChooseButton = String
                .format(res.getString(R.string.hint_press_choose_button),
                        res.getString(R.string.button_choose));
        // Find the text field for the result text
        TextView choiceResultTextView = (TextView) findViewById(R.id.textViewChoiceResultText);
        // Set the text color and the hint text
        choiceResultTextView.setTextColor(Color.BLACK);
        choiceResultTextView.setText("");
        choiceResultTextView.setHint(hintPleasePressChooseButton);

        // Set the other text for using shake to choose
        // Find the text field for the results' second text
        TextView choiceResultSecondTextTextView = (TextView) findViewById(R.id.textViewChoiceResut2Text);
        // Set the restaurant location to a hint text
        choiceResultSecondTextTextView.setText("");
        choiceResultSecondTextTextView.setHint(res
                .getString(R.string.hint_shake_to_choose));
    }
    /**
     * Display the text to first add item entries to the database.
     */
    protected void setTextfieldsForNoDatabaseEntries() {
        // Display the text to first add database entries

        // Build the string with the special entry name
        Resources res = getResources();
        String textPleaseAddViaMenuEntry = String
                .format(res.getString(R.string.text_please_add_items),
                        res.getString((R.string.action_edit_items)));
        String textPleaseAddViaMenuEntryItemLists = String
                .format(res.getString(R.string.text_please_add_items),
                        res.getString((R.string.action_edit_itemlists)));

        // Find the text field for the item name
        TextView chosenRestaurantNameTextView = (TextView) findViewById(R.id.textViewChoiceResultText);
        // Set the text color and the warning text
        chosenRestaurantNameTextView.setTextColor(Color.RED);
        chosenRestaurantNameTextView
                .setText(textPleaseAddViaMenuEntry + " " + textPleaseAddViaMenuEntryItemLists);

        // Find the text field for the restaurant location
        TextView chosenRestaurantLocationTextView = (TextView) findViewById(R.id.textViewChoiceResut2Text);
        // Empty the second text field
        chosenRestaurantLocationTextView.setText("");
        chosenRestaurantLocationTextView.setHint("");
    }

    protected void setTextfieldsForNoChosenChoiceMethod(){
        // Find the text field for the item name
        TextView chosenRestaurantNameTextView = (TextView) findViewById(R.id.textViewChoiceResultText);
        // Set the text color and the warning text
        chosenRestaurantNameTextView.setTextColor(Color.RED);
        chosenRestaurantNameTextView
                .setText(R.string.text_please_select_radio_button);
        // Find the text field for the restaurant location
        TextView chosenRestaurantLocationTextView = (TextView) findViewById(R.id.textViewChoiceResut2Text);
        // Empty the second text field
        chosenRestaurantLocationTextView.setText("");
        chosenRestaurantLocationTextView.setHint("");
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Find all other radio buttons
        RadioButton radioButtonChooseFromList = (RadioButton) findViewById(R.id.radio_choose_from_list);
        RadioButton radioButtonThrowCoin = (RadioButton) findViewById(R.id.radio_throw_coin);
        RadioButton radioButtonRuleDice = (RadioButton) findViewById(R.id.radio_rule_dice);
        RadioButton radioButtonRuleCustomDice = (RadioButton) findViewById(R.id.radio_rule_custom_dice);

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_choose_from_list:
                if (checked) {
                    // Set the radio buttons and text fields accordingly
                    setChoiceMethodRadioButtons(ChoiceMethod.FROM_LIST);
                }
                break;
            case R.id.radio_throw_coin:
                if (checked) {
                    // Set the radio buttons and text fields accordingly
                    setChoiceMethodRadioButtons(ChoiceMethod.THROW_COIN);
                }
                break;
            case R.id.radio_rule_dice:
                if (checked) {
                    // Set the radio buttons and text fields accordingly
                    setChoiceMethodRadioButtons(ChoiceMethod.RULE_DICE);
                }
                break;
            case R.id.radio_rule_custom_dice:
                if (checked) {
                    // Set the radio buttons and text fields accordingly
                    setChoiceMethodRadioButtons(ChoiceMethod.RULE_CUSTOM_DICE);
                }
                break;
        }
    }

    /**
     * This helper method sets (checks/unchecks) the choice method RadioButtons and sets the
     * corresponding text fields accordingly to the given parameter.
     * @param choiceMethod The <code>ChoiceMethod</code> value to check/uncheck the choice method
     *                     RadioButtons and corresponding text fields for.
     */
    private void setChoiceMethodRadioButtons(ChoiceMethod choiceMethod){
        // Get all RadioButtons and check their check state
        RadioButton radioButtonChooseFromList = (RadioButton) findViewById(R.id.radio_choose_from_list);
        RadioButton radioButtonThrowCoin = (RadioButton) findViewById(R.id.radio_throw_coin);
        RadioButton radioButtonRuleDice = (RadioButton) findViewById(R.id.radio_rule_dice);
        RadioButton radioButtonRuleCustomDice = (RadioButton) findViewById(R.id.radio_rule_custom_dice);

        // Check the correct choose RadioButton according to the last stored value
        switch(choiceMethod){
            case FROM_LIST:
                // Activate the correct RadioButton and deactivate the others
                radioButtonChooseFromList.setChecked(true);
                radioButtonThrowCoin.setChecked(false);
                radioButtonRuleDice.setChecked(false);
                radioButtonRuleCustomDice.setChecked(false);
                break;
            case THROW_COIN:
                // Activate the correct RadioButton and deactivate the others
                radioButtonChooseFromList.setChecked(false);
                radioButtonThrowCoin.setChecked(true);
                radioButtonRuleDice.setChecked(false);
                radioButtonRuleCustomDice.setChecked(false);
                break;
            case RULE_DICE:
                // Activate the correct RadioButton and deactivate the others
                radioButtonChooseFromList.setChecked(false);
                radioButtonThrowCoin.setChecked(false);
                radioButtonRuleDice.setChecked(true);
                radioButtonRuleCustomDice.setChecked(false);
                break;
            case RULE_CUSTOM_DICE:
                // Activate the correct RadioButton and deactivate the others
                radioButtonChooseFromList.setChecked(false);
                radioButtonThrowCoin.setChecked(false);
                radioButtonRuleDice.setChecked(false);
                radioButtonRuleCustomDice.setChecked(true);
                break;
            default:
                // Deactivate all RadioButtons
                radioButtonChooseFromList.setChecked(false);
                radioButtonThrowCoin.setChecked(false);
                radioButtonRuleDice.setChecked(false);
                radioButtonRuleCustomDice.setChecked(false);
                break;
        }
        // Check which RadioButton is checked and accordingly set the other text fields
        if (radioButtonChooseFromList.isChecked()) {
            setTextfieldsForItemListChoice();
        } else if (radioButtonThrowCoin.isChecked()) {
            setTextfieldsForShakeToChoose();
        } else if (radioButtonRuleDice.isChecked()) {
            setTextfieldsForShakeToChoose();
        } else if (radioButtonRuleCustomDice.isChecked()) {
            setTextfieldsForShakeToChoose();
        } else {
            setTextfieldsForNoChosenChoiceMethod();
        }
    }
}
