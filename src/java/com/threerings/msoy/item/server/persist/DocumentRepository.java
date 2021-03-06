//
// $Id$

package com.threerings.msoy.item.server.persist;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.annotation.Entity;

import com.threerings.msoy.item.data.all.Document;
import com.threerings.msoy.server.persist.RatingRecord;
import com.threerings.msoy.server.persist.RatingRepository;
import com.threerings.msoy.server.persist.TagHistoryRecord;
import com.threerings.msoy.server.persist.TagRecord;

/**
 * Manages the persistent store of {@link Document} items.
 */
@Singleton
public class DocumentRepository extends ItemRepository<DocumentRecord>
{
    @Entity(name="DocumentMogMarkRecord")
    public static class DocumentMogMarkRecord extends MogMarkRecord
    {
    }

    @Entity(name="DocumentTagRecord")
    public static class DocumentTagRecord extends TagRecord
    {
    }

    @Entity(name="DocumentTagHistoryRecord")
    public static class DocumentTagHistoryRecord extends TagHistoryRecord
    {
    }

    @Inject public DocumentRepository (PersistenceContext ctx)
    {
        super(ctx);
    }

    @Override
    protected Class<DocumentRecord> getItemClass ()
    {
        return DocumentRecord.class;
    }

    @Override
    protected Class<CatalogRecord> getCatalogClass ()
    {
        return coerceCatalog(DocumentCatalogRecord.class);
    }

    @Override
    protected Class<CloneRecord> getCloneClass ()
    {
        return coerceClone(DocumentCloneRecord.class);
    }

    @Override
    protected Class<RatingRecord> getRatingClass ()
    {
        return RatingRepository.coerceRating(DocumentRatingRecord.class);
    }

    @Override
    protected MogMarkRecord createMogMarkRecord ()
    {
        return new DocumentMogMarkRecord();
    }

    @Override
    protected TagRecord createTagRecord ()
    {
        return new DocumentTagRecord();
    }

    @Override
    protected TagHistoryRecord createTagHistoryRecord ()
    {
        return new DocumentTagHistoryRecord();
    }
}
