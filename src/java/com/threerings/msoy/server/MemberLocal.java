//
// $Id$

package com.threerings.msoy.server;

import java.util.List;
import java.util.Set;

import com.threerings.util.StreamableArrayIntSet;

import com.threerings.crowd.server.BodyLocal;

import com.threerings.stats.data.StatSet;

import com.threerings.orth.notify.data.Notification;
import com.threerings.orth.notify.data.NotificationLocal;

import com.threerings.msoy.badge.data.BadgeType;
import com.threerings.msoy.badge.data.EarnedBadgeSet;
import com.threerings.msoy.badge.data.InProgressBadgeSet;
import com.threerings.msoy.badge.data.all.EarnedBadge;
import com.threerings.msoy.badge.data.all.InProgressBadge;
import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.room.data.EntityMemories;
import com.threerings.msoy.room.data.RoomObject;
import com.threerings.msoy.room.server.RoomManager;

/**
 * Contains server-side only information for a member.
 */
public class MemberLocal extends BodyLocal
    implements NotificationLocal
{
    /** The number of non-idle seconds that have elapsed in this member's session. When the member
     * is forwarded between servers, this value is incremented by the time they spent on the server
     * from which they are departing. */
    public int sessionSeconds;

    /** Statistics tracked for this player. */
    public StatSet stats;

    /** Metrics tracked for this player. */
    public PlayerMetrics metrics = new PlayerMetrics();

    /** The set of badges that this player owns. */
    public EarnedBadgeSet badges;

    /** The version of the "Badges set" that this user has seen. If this is behind
     * BadgeType.VERSION, then the member's InProgressBadges will be recomputed. */
    public short badgesVersion;

    /** The set of badges that this player is working towards. */
    public InProgressBadgeSet inProgressBadges;

    /** A list of notifications that will be dispatched when the client's NotificationDirector asks
     * for them. Will be null once the deferred notifications have been dispatched. */
    public List<Notification> deferredNotifications;

    /** The complete list of all your friends. */
    public Set<Integer> friendIds;

    /** Rooms we've visited during our current Whirled Tour. */
    public Set<Integer> touredRooms;

    /** The memories of the member's avatar. */
    public EntityMemories memories;

    public List<Notification> getAndClearDeferredNotifications ()
    {
        List<Notification> notifications = deferredNotifications;
        deferredNotifications = null;
        return notifications;
    }

    public boolean stillDeferringNotifications ()
    {
        return deferredNotifications != null;
    }

    public void deferNotifications (List<Notification> notifications)
    {
        deferredNotifications.addAll(notifications);
    }

    /**
     * Adds an EarnedBadge to the member's BadgeSet (or updates the existing badge if the badge
     * level has increased) and dispatches an event indicating that a new badge was awarded.
     *
     * @return true if the badge was updated in the member's BadgeSet, and false otherwise.
     */
    public boolean badgeAwarded (EarnedBadge badge)
    {
        boolean added = badges.addOrUpdateBadge(badge);

        // remove this badge's associated InProgressBadge if the badge's highest level has
        // been reached
        BadgeType badgeType = BadgeType.getType(badge.badgeCode);
        if (badgeType != null && badge.level >= badgeType.getNumLevels() - 1) {
            inProgressBadges.removeBadge(badge.badgeCode);
        }

        return added;
    }

    /**
     * Adds an InProgressBadge to the member's in-progress badge set (or updates the existing badge
     * if the badge level has increased).
     *
     * @return true if the badge was updated in the member's in-progress badge set, and false
     * otherwise.
     */
    public boolean inProgressBadgeUpdated (InProgressBadge badge)
    {
        return inProgressBadges.addOrUpdateBadge(badge);
    }

    /**
     * Is this user hearing the specified group's chat?
     */
    public boolean isHearingGroupChat (int groupId)
    {
        return (_suppressedGroupChats == null) || !_suppressedGroupChats.contains(groupId);
    }

    /**
     * Sets whether this user hears group chat for the specified group.
     */
    public void setHearingGroupChat (int groupId, boolean hear)
    {
        if (hear) {
            if (_suppressedGroupChats != null) {
                _suppressedGroupChats.remove(groupId);
                if (_suppressedGroupChats.isEmpty()) {
                    _suppressedGroupChats = null;
                }
            }

        } else {
            if (_suppressedGroupChats == null) {
                _suppressedGroupChats = new StreamableArrayIntSet();
            }
            _suppressedGroupChats.add(groupId);
        }
    }

    /**
     * Called when a player has just switched from one avatar to a new one or by {@link
     * #willEnterRoom} below. In either case, {@link #memories} is expected to contain the memories
     * for the avatar; either because it was put there (and possibly serialized in the case of a
     * peer move) when the player left a previous room, or because we put them there manually as
     * part of avatar resolution (see {@link MemberManager#finishSetAvatar}).
     */
    public void putAvatarMemoriesIntoRoom (RoomObject roomObj)
    {
        if (memories != null) {
            roomObj.putMemories(memories);
            memories = null;
        }
    }

    /**
     * Called when we depart a room to remove our avatar memories from the room and store them in
     * this local storage.
     */
    public void takeAvatarMemoriesFromRoom (MemberObject memobj, RoomObject roomObj)
    {
        memories = (memobj.avatar == null) ? null : roomObj.takeMemories(memobj.avatar.getIdent());
    }

    /**
     * Called by the {@link RoomManager} when we're about to enter a room, and also
     * takes care of calling willEnterPartyPlace().
     */
    public void willEnterRoom (MemberObject memobj, RoomObject roomObj)
    {
        putAvatarMemoriesIntoRoom(roomObj);
    }

    /**
     * Called by the {@link RoomManager} when we're about to leave a room, and also
     * takes care of calling willLeavePartyPlace().
     */
    public void willLeaveRoom (MemberObject memobj, RoomObject roomObj)
    {
        // remove our avatar memories from this room
        takeAvatarMemoriesFromRoom(memobj, roomObj);
    }

    /** If not null, contains the set of group ids for which to suppress chat. */
    protected Set<Integer> _suppressedGroupChats;
}
