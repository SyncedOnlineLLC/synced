//
// $Id$

package com.threerings.msoy.fora.server.persist;

import java.util.List;
import java.util.Set;

import java.sql.Timestamp;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.IntSet;

import com.samskivert.depot.DepotRepository;
import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.PersistentRecord;

import com.samskivert.depot.clause.FromOverride;
import com.samskivert.depot.clause.Limit;
import com.samskivert.depot.clause.OrderBy;
import com.samskivert.depot.clause.Where;

import com.samskivert.depot.expression.SQLExpression;

import com.samskivert.depot.operator.Conditionals;
import com.samskivert.depot.operator.Logic;
import com.samskivert.depot.operator.SQLOperator;

import com.threerings.presents.annotation.BlockingThread;

import com.threerings.msoy.server.persist.CountRecord;

import com.threerings.msoy.fora.gwt.Issue;

/**
 * Manages issues.
 */
@Singleton @BlockingThread
public class IssueRepository extends DepotRepository
{
    @Inject public IssueRepository (PersistenceContext ctx)
    {
        super(ctx);
    }

    /**
     * Load issues of specific types and states.
     */
    public List<IssueRecord> loadIssues (IntSet states, int offset, int count)
    {
        return loadIssues(states, 0, offset, count);
    }

    /**
     * Load issues of specific types, states and ownerId.
     */
    public List<IssueRecord> loadIssues (IntSet states, int ownerId, int offset, int count)
    {
        List<SQLOperator> whereBits = Lists.newArrayList();
        whereBits.add(new Conditionals.In(IssueRecord.STATE, states));
        if (ownerId > 0) {
            whereBits.add(new Conditionals.Equals(IssueRecord.OWNER_ID, ownerId));
        }
        OrderBy orderBy;
        if (!states.contains(Issue.STATE_OPEN)) {
            orderBy = OrderBy.descending(IssueRecord.CLOSED_TIME);
        } else {
            orderBy = new OrderBy(
                new SQLExpression[] { IssueRecord.PRIORITY, IssueRecord.CREATED_TIME },
                new OrderBy.Order[] { OrderBy.Order.DESC, OrderBy.Order.DESC });
        }
        return findAll(IssueRecord.class, new Where(new Logic.And(whereBits)),
                       new Limit(offset, count), orderBy);
    }

    /**
     * Load an issue from the issueId.
     */
    public IssueRecord loadIssue (int issueId)
    {
        return load(IssueRecord.class, issueId);
    }

    /**
     * Load the total number of issues with specific types, states.
     */
    public int loadIssueCount (IntSet states)
    {
        List<SQLOperator> whereBits = Lists.newArrayList();
        if (states != null) {
            whereBits.add(new Conditionals.In(IssueRecord.STATE, states));
        }
        return load(CountRecord.class, new FromOverride(IssueRecord.class),
                    new Where(new Logic.And(whereBits))).count;
    }

    /**
     * Creates an issue.
     */
    public IssueRecord createIssue (Issue issue)
    {
        IssueRecord ir = new IssueRecord();
        ir.creatorId = issue.creator.getMemberId();
        ir.ownerId = (issue.owner != null ? issue.owner.getMemberId() : -1);
        ir.summary = issue.summary;
        ir.description = issue.description;
        ir.state = issue.state;
        ir.priority = issue.priority;
        ir.type = issue.type;
        ir.category = issue.category;
        ir.createdTime = new Timestamp(System.currentTimeMillis());
        insert(ir);
        return ir;
    }

    /**
     * Updates an issue.
     */
    public void updateIssue (Issue issue)
    {
        updatePartial(IssueRecord.class, issue.issueId,
                IssueRecord.OWNER_ID, (issue.owner != null ? issue.owner.getMemberId() : -1),
                IssueRecord.SUMMARY, issue.summary,
                IssueRecord.DESCRIPTION, issue.description,
                IssueRecord.STATE, issue.state,
                IssueRecord.PRIORITY, issue.priority,
                IssueRecord.TYPE, issue.type,
                IssueRecord.CATEGORY, issue.category,
                IssueRecord.CLOSE_COMMENT, issue.closeComment,
                IssueRecord.CLOSED_TIME, (issue.closeComment == null ?
                    null : new Timestamp(System.currentTimeMillis())));
    }

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(IssueRecord.class);
    }
}
