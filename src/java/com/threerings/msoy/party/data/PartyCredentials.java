//
// $Id$

package com.threerings.msoy.party.data;

import com.threerings.msoy.data.MsoyCredentials;

/**
 * Used to authenticate a party session.
 */
public class PartyCredentials extends MsoyCredentials
{
    /** The party that the authenticating user wishes to join. */
    public int partyId;
}
