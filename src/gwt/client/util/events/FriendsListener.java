//
// $Id$

package client.util.events;

/**
 * Used to notify interested parties of friends list changes.
 */
public interface FriendsListener extends FlashEventListener
{
    /** Called when a friend is added to the friends list. */
    public void friendAdded (FriendEvent event);

    /** Called when a friend is removed from the friends list. */
    public void friendRemoved (FriendEvent event);
}
