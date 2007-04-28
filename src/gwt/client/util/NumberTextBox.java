//
// $Id$

package client.util;

import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/** 
 * A text box that will only accept numbers.
 */
public class NumberTextBox extends TextBox
{
    /**
     * @param allowFloatingPoint If true, a single decimal point is part of the allowed character 
     * set.  Otherwise, only [0-9]* is accepted.
     */
    public NumberTextBox (final boolean allowFloatingPoint) 
    {
        _allowFloatingPoint = allowFloatingPoint;

        addKeyboardListener(new KeyboardListener() {
            public void onKeyUp (Widget sender, char keyCode, int modifiers) { 
                if ((modifiers & KeyboardListener.MODIFIER_SHIFT) != 0 || keyCode > '9' ||
                    keyCode < '0') {
                    String text = getText();
                    boolean foundDecimal = !allowFloatingPoint;
                    for (int ii = 0; ii < text.length(); ii++) {
                        if (text.charAt(ii) > '9' || text.charAt(ii) < '0') {
                            if (text.charAt(ii) == '.' && !foundDecimal) {
                                foundDecimal = true;
                            } else {
                                text = text.substring(0, ii) + text.substring(ii+1);
                                ii--;
                            }
                        }
                    }
                    setText(text);
                }
            }
            public void onKeyPress (Widget sender, char keyCode, int modifiers) { }
            public void onKeyDown (Widget sender, char keyCode, int modifiers) { }
        });
    }

    /**
     * Get the number value for the contents of this box.
     */
    public Number getValue ()
    {
        return _allowFloatingPoint ? (Number)(new Double(getText())) : 
            (Number)(new Integer(getText()));
    }

    protected boolean _allowFloatingPoint;
}
