//
// $Id$

package client.util;

import client.shell.CShell;

import client.util.events.StatusChangeEvent;

import com.threerings.msoy.money.data.all.BalanceInfo;

/**
 * I can't think of a better name.
 */
public class MoneyUtil
{
    /**
     * Update the balances displayed in the header.
     */
    public static void updateBalances (BalanceInfo balances)
    {
        // if the flash client is around, we don't want to update any balances, because the
        // values from the flash client could include earned money from games that have not
        // yet been persisted to the database.
        if (FlashClients.clientExists()) {
            return;
        }

        if (balances.coins != null) {
            CShell.frame.dispatchEvent(new StatusChangeEvent(StatusChangeEvent.COINS,
                balances.coins, balances.coins));
        }
        if (balances.bars != null) {
            CShell.frame.dispatchEvent(new StatusChangeEvent(StatusChangeEvent.BARS,
                balances.bars, balances.bars));
        }
    }
}
