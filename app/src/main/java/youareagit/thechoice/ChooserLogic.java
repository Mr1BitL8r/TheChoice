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
package youareagit.thechoice;

import java.util.Random;

/**
 * This class bundles the logic and
 */
class ChooserLogic {
    // Values for the normal dice and custom dice
    /** The default maximum range value a normal dice (D6). */
    private static final int NORMAL_DICE_MAXIMUM_RANGE_VALUE = 6;
    /** The default custom dice maximum range value. */
    static final int CUSTOM_DICE_MAXIMUM_RANGE_VALUE_DEFAULT = 20;

    /** The last used custom dice maximum range value. */
    private int lastCustomDiceMaximumRangeValue = CUSTOM_DICE_MAXIMUM_RANGE_VALUE_DEFAULT;

    /**
     * Gets a random number in the range [0 ... "given maximum range value - 1" if the
     * specified maximum value is greater than zero. Otherwise it will return 0.
     * @param maximumValue The maximum range value between which it and 0 the returned value will
     *                     be chosen as an <code>int</code> value.
     * @return The randomly chosen value in the range [0 ... "given maximum range value - 1"] as an
     * <code>int</code> value or 0 if the given maximum value was equal to or less than 0.
     */
    int getRandomNumberInRange(int maximumValue){
        // Buffer variable
        int randomNumber = 0;

        // Get a random number in the range 0 ... given maximum range value
        if (isMaximumRangeValueValid(maximumValue)) {
            randomNumber = new Random().nextInt(maximumValue);
        }
        return randomNumber;
    }

    /**
     * Throw a coin and return the result as a boolean.
     * @return The number of the coin throw as an <code>boolean</code> value (only 0 = "Heads"
     * and 1 = "Tails" is the result, "Coin rim" is not an option here!).
     */
    boolean throwCoin() {
        // A coin throw can realistically only be "Heads" or "Tails" ("Coin rim" is not an option here!)
        final int numberOfCoinThrowResults = 2;

        // Get a random number for the coin throw (0 or 1)
        int randomNumber = getRandomNumberInRange(numberOfCoinThrowResults);
        // Translate the chosen random number into the "Heads" or "Tails" boolean value
        if(randomNumber == 0){ // "Heads"
            return false;
        } else { // "Tails"
            return true;
        }
    }

    /**
     * Rule a dice with the specified maximum range value and return the result as a <code>String</code>.
     * If the specified value is not valid <code>null</code> will be returned.
     *
     * @param customDiceMaximumRangeValue The maximum range value of the dice.
     * @return The result of a roll of the dice with the given maximum range value as a
     * <code>String</code> or <code>null</code> if an error occurred or the specified maximum
     * range value was invalid, e.g. negative or zero.
     */
    String ruleCustomDice(int customDiceMaximumRangeValue) {
        // Buffer for the result text
        String choiceResult = null;

        try{
            // Check if the given parameter has a valid value
            isMaximumRangeValueValid(customDiceMaximumRangeValue);
            // Get a random number in the range and add 1 because the range starts with 0 (not 1)
            choiceResult = String.valueOf(getRandomNumberInRange(customDiceMaximumRangeValue) + 1);

            // The value was valid so buffer it for storing it later in the settings database
            lastCustomDiceMaximumRangeValue = customDiceMaximumRangeValue;
        } catch(IllegalArgumentException iAE) {
            iAE.printStackTrace();
            // The value was invalid so reset the internal one to the default value
            lastCustomDiceMaximumRangeValue = CUSTOM_DICE_MAXIMUM_RANGE_VALUE_DEFAULT;
        }
        return choiceResult;
    }

    /**
     * Rule a normal dice (d6) and return the result as a <code>String</code>.
     *
     * @return The result of a roll of the dice (d6) as a
     * <code>String</code> or <code>null</code> if an error occurred.
     */
    String ruleNormalDice() {
        return ruleCustomDice(NORMAL_DICE_MAXIMUM_RANGE_VALUE);
    }

    /**
     * Sets the last custom dice maximum range value if it is valid (greater than zero).
     * @param maximumValue The custom dice maximum range value to set as an <code>int</code>.
     */
    void setLastCustomDiceMaximumRangeValue(int maximumValue) {
        // Check if the given maximum value is valid
        if (isMaximumRangeValueValid(maximumValue)) {
            this.lastCustomDiceMaximumRangeValue = maximumValue;
        }
    }

    /**
     * Checks if the given maximum range value is valid (greater than 0) and returns
     * <code>true</code> or otherwise an <code>IllegalArgumentException</code> is thrown.
     * @param maximumValue The maximum range value to validate as an <code>int</code>.
     * @return If the validation (value is greater than 0) <code>true</code> else
     *          the mentioned exception is thrown.
     * @throws IllegalArgumentException is thrown if the given maximumValue is equal or less than 0.
     */
    private boolean isMaximumRangeValueValid(int maximumValue) throws IllegalArgumentException {
        // Check if the given maximum value is valid
        if (maximumValue > 0) {
            return true;
        } else {
            throw new IllegalArgumentException("The maximum range value for getting a random number cannot be equal to or less than zero but it was: " + maximumValue);
        }
    }
    /**
     * The last used valid value of the custom dice maximum range value.
     * @return The last used valid value of the custom dice maximum range value as a <code>int</code>.
     */
    int getLastCustomDiceMaximumRangeValue() {
        return lastCustomDiceMaximumRangeValue;
    }
}