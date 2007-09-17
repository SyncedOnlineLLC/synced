//
// $Id$

package com.threerings.msoy.item.server.persist;

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.annotation.TableGenerator;
import com.samskivert.jdbc.depot.expression.ColumnExp;

import com.threerings.msoy.item.data.gwt.CatalogListing;

/** Catalog Records for Decor. */
@TableGenerator(name="catalogId", pkColumnValue="DECOR_CATALOG")
public class DecorCatalogRecord extends CatalogRecord<DecorRecord>
{
    // AUTO-GENERATED: FIELDS START
    /** The qualified column identifier for the {@link #itemId} field. */
    public static final ColumnExp ITEM_ID_C =
        new ColumnExp(DecorCatalogRecord.class, ITEM_ID);

    /** The qualified column identifier for the {@link #listedDate} field. */
    public static final ColumnExp LISTED_DATE_C =
        new ColumnExp(DecorCatalogRecord.class, LISTED_DATE);

    /** The qualified column identifier for the {@link #flowCost} field. */
    public static final ColumnExp FLOW_COST_C =
        new ColumnExp(DecorCatalogRecord.class, FLOW_COST);

    /** The qualified column identifier for the {@link #goldCost} field. */
    public static final ColumnExp GOLD_COST_C =
        new ColumnExp(DecorCatalogRecord.class, GOLD_COST);

    /** The qualified column identifier for the {@link #rarity} field. */
    public static final ColumnExp RARITY_C =
        new ColumnExp(DecorCatalogRecord.class, RARITY);

    /** The qualified column identifier for the {@link #purchases} field. */
    public static final ColumnExp PURCHASES_C =
        new ColumnExp(DecorCatalogRecord.class, PURCHASES);

    /** The qualified column identifier for the {@link #returns} field. */
    public static final ColumnExp RETURNS_C =
        new ColumnExp(DecorCatalogRecord.class, RETURNS);

    /** The qualified column identifier for the {@link #repriceCounter} field. */
    public static final ColumnExp REPRICE_COUNTER_C =
        new ColumnExp(DecorCatalogRecord.class, REPRICE_COUNTER);
    // AUTO-GENERATED: FIELDS END

    public DecorCatalogRecord ()
    {
        super();
    }

    protected DecorCatalogRecord (CatalogListing listing)
    {
        super(listing);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link #DecorCatalogRecord}
     * with the supplied key values.
     */
    public static Key<DecorCatalogRecord> getKey (int itemId)
    {
        return new Key<DecorCatalogRecord>(
                DecorCatalogRecord.class,
                new String[] { ITEM_ID },
                new Comparable[] { itemId });
    }
    // AUTO-GENERATED: METHODS END
}
