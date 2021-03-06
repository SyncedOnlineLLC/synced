//
// $Id$

package com.threerings.msoy.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.TypedArray;

import com.threerings.presents.net.BootstrapData;

/**
 * Msoy bootstrap data.
 */
public class MsoyBootstrapData extends BootstrapData
{
    /** An array of memberIds that we've muted in previous sessions. */
    public var mutedMemberIds :TypedArray /* of int */;

    public function MsoyBootstrapData ()
    {
    }

    // documentation inherited
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        mutedMemberIds = TypedArray(ins.readField(TypedArray.getJavaType(int)));
    }
}
}
