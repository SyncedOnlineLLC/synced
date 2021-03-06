//
// $Id$

package com.threerings.msoy.item.server.persist;

import java.sql.Timestamp;

import com.samskivert.depot.Key;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.TableGenerator;
import com.samskivert.depot.expression.ColumnExp;

import com.threerings.msoy.item.data.all.Decor;
import com.threerings.msoy.item.data.all.Item;
import com.threerings.msoy.item.data.all.MsoyItemType;

/**
 * Represents a piece of decor (any prop really) that a user can place into
 * a virtual world scene and potentially interact with.
 */
@TableGenerator(name="itemId", pkColumnValue="DECOR")
public class DecorRecord extends ItemRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<DecorRecord> _R = DecorRecord.class;
    public static final ColumnExp<Byte> TYPE = colexp(_R, "type");
    public static final ColumnExp<Short> HEIGHT = colexp(_R, "height");
    public static final ColumnExp<Short> WIDTH = colexp(_R, "width");
    public static final ColumnExp<Short> DEPTH = colexp(_R, "depth");
    public static final ColumnExp<Float> HORIZON = colexp(_R, "horizon");
    public static final ColumnExp<Boolean> HIDE_WALLS = colexp(_R, "hideWalls");
    public static final ColumnExp<Float> ACTOR_SCALE = colexp(_R, "actorScale");
    public static final ColumnExp<Float> FURNI_SCALE = colexp(_R, "furniScale");
    public static final ColumnExp<Integer> ITEM_ID = colexp(_R, "itemId");
    public static final ColumnExp<Integer> SOURCE_ID = colexp(_R, "sourceId");
    public static final ColumnExp<Integer> CREATOR_ID = colexp(_R, "creatorId");
    public static final ColumnExp<Integer> OWNER_ID = colexp(_R, "ownerId");
    public static final ColumnExp<Integer> CATALOG_ID = colexp(_R, "catalogId");
    public static final ColumnExp<Integer> RATING_SUM = colexp(_R, "ratingSum");
    public static final ColumnExp<Integer> RATING_COUNT = colexp(_R, "ratingCount");
    public static final ColumnExp<Item.UsedAs> USED = colexp(_R, "used");
    public static final ColumnExp<Integer> LOCATION = colexp(_R, "location");
    public static final ColumnExp<Timestamp> LAST_TOUCHED = colexp(_R, "lastTouched");
    public static final ColumnExp<String> NAME = colexp(_R, "name");
    public static final ColumnExp<String> DESCRIPTION = colexp(_R, "description");
    public static final ColumnExp<Boolean> MATURE = colexp(_R, "mature");
    public static final ColumnExp<byte[]> THUMB_MEDIA_HASH = colexp(_R, "thumbMediaHash");
    public static final ColumnExp<Byte> THUMB_MIME_TYPE = colexp(_R, "thumbMimeType");
    public static final ColumnExp<Byte> THUMB_CONSTRAINT = colexp(_R, "thumbConstraint");
    public static final ColumnExp<byte[]> FURNI_MEDIA_HASH = colexp(_R, "furniMediaHash");
    public static final ColumnExp<Byte> FURNI_MIME_TYPE = colexp(_R, "furniMimeType");
    public static final ColumnExp<Byte> FURNI_CONSTRAINT = colexp(_R, "furniConstraint");
    // AUTO-GENERATED: FIELDS END

    /** Update this version if you change fields specific to this derived class. */
    public static final int ITEM_VERSION = 4;

    /** This combines {@link #ITEM_VERSION} with {@link #BASE_SCHEMA_VERSION} to create a version
     * that allows us to make ItemRecord-wide changes and specific derived class changes. */
    public static final int SCHEMA_VERSION = ITEM_VERSION + BASE_SCHEMA_VERSION * BASE_MULTIPLIER;

    /** Room type. Controls how the background wallpaper image is handled. */
    public byte type;

    /** Room height, in pixels. */
    public short height;

    /** Room width, in pixels. */
    public short width;

    /** Room depth, in pixels. */
    public short depth;

    /** Horizon position, in [0, 1]. */
    public float horizon;

    /** Specifies whether side walls should be displayed. */
    public boolean hideWalls;

    /** The adjusted scale of actors in this room. */
    @Column(defaultValue="1")
    public float actorScale;

    /** The adjusted scale of furni in this room. */
    @Column(defaultValue="1")
    public float furniScale;

    @Override // from ItemRecord
    public MsoyItemType getType ()
    {
        return MsoyItemType.DECOR;
    }

    @Override // from ItemRecord
    public void fromItem (Item item)
    {
        super.fromItem(item);

        Decor decor = (Decor)item;
        type = decor.type;
        height = decor.height;
        width = decor.width;
        depth = decor.depth;
        horizon = decor.horizon;
        hideWalls = decor.hideWalls;
        actorScale = decor.actorScale;
        furniScale = decor.furniScale;
    }

    @Override // from ItemRecord
    protected Item createItem ()
    {
        Decor object = new Decor();
        object.type = type;
        object.height = height;
        object.width = width;
        object.depth = depth;
        object.horizon = horizon;
        object.hideWalls = hideWalls;
        object.actorScale = actorScale;
        object.furniScale = furniScale;
        return object;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link DecorRecord}
     * with the supplied key values.
     */
    public static Key<DecorRecord> getKey (int itemId)
    {
        return newKey(_R, itemId);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(ITEM_ID); }
    // AUTO-GENERATED: METHODS END
}
