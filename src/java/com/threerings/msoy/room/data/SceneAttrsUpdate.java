//
// $Id$

package com.threerings.msoy.room.data;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

import com.threerings.msoy.item.data.all.Decor;

/**
 * Encodes a scene update that updates the attributes in the MsoySceneModel.  Note that this
 * contains all attributes, even ones that have not changed.  In other words, a field being null
 * doesn't mean that the field isn't updated, it means the new value should be null.
 */
@com.threerings.util.ActionScript(omit=true)
public class SceneAttrsUpdate extends SceneUpdate
{
    /** The new name. */
    public String name;

    /** New access control info. */
    public byte accessControl;

    /** New playlist control info. */
    public byte playlistControl;

    /** Full description of the new decor. */
    public Decor decor;

    /** The new entrance location. */
    public MsoyLocation entrance;

    /** The new background color. */
    public int backgroundColor;

    /** If the puppet has been turned off for this scene. */
    public boolean noPuppet;

    @Override
    public void apply (SceneModel model)
    {
        super.apply(model);

        MsoySceneModel mmodel = (MsoySceneModel) model;
        mmodel.name = name;
        mmodel.accessControl = accessControl;
        mmodel.playlistControl = playlistControl;
        mmodel.decor = decor;
        mmodel.entrance = entrance;
        mmodel.backgroundColor = backgroundColor;
        mmodel.noPuppet = noPuppet;
    }

    @Override
    public void validate (SceneModel model)
        throws IllegalStateException
    {
        super.validate(model);
    }
}
