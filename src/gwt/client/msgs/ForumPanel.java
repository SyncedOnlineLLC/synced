//
// $Id$

package client.msgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import com.threerings.gwt.ui.Anchor;
import com.threerings.gwt.ui.SmartTable;
import com.threerings.gwt.util.StringUtil;

import com.threerings.msoy.data.all.GroupName;
import com.threerings.msoy.fora.gwt.ForumThread;
import com.threerings.msoy.web.gwt.Pages;

import client.images.msgs.MsgsImages;
import client.shell.CShell;
import client.shell.ShellMessages;
import client.ui.MsoyUI;
import client.ui.SearchBox;
import client.util.InfoCallback;
import client.util.Link;

/**
 * Displays group threads, unread threads, or new thread starter.
 */
public class ForumPanel extends TitledListPanel
{
    /** Possible modes of the panel. */
    public enum Mode
    {
        /** Viewing the user's unread threads. */
        UNREAD,

        /** Viewing the threads of a group. */
        GROUP,

        /** Viewing threads with unread posts from friends. */
        FRIENDS,

        /** Starting a new thread. */
        NEW_THREAD
    }

    /**
     * Creates a new panel in the given mode with the given group id. For UNREAD mode, the groupId
     * should always be 0.
     */
    public ForumPanel (ForumModels fmodels, Mode mode, final int groupId, String query, int page)
    {
        _fmodels = fmodels;
        _mode = mode;
        _groupId = groupId;
        _query = (query == null) ? "" : query;

        switch (mode) {
        case GROUP:
            _threads = new GroupThreadListPanel(this, _fmodels, groupId);
            setContents(createHeader(_mmsgs.groupThreadListHeader(), _threads), _threads);

            // set up a callback to configure our page title when we learn this group's name
            _fmodels.getGroupThreads(groupId).addGotNameListener(new InfoCallback<GroupName>() {
                public void onSuccess (GroupName result) {
                    CShell.frame.setTitle(result.toString());
                    setGroupTitle(groupId, result.toString());
                }
            });
            break;

        case UNREAD:
            _threads = new UnreadThreadListPanel(this, _fmodels);
            String title = StringUtil.isBlank(query) ?
                _mmsgs.msgUnreadThreadsHeader() : _mmsgs.msgSearchMyThreadsHeader();
            setContents(createHeader(title, _threads), _threads);
            break;

        case FRIENDS:
            _threads = new FriendThreadListPanel(this, _fmodels);
            // TODO: enable searching friend threads
            setContents(createHeader(_mmsgs.msgFriendThreadsHeader(), null), _threads);
            break;

        case NEW_THREAD:
            startNewThread(groupId);
            break;
        }

        if (_searchBox != null) {
            _searchBox.setText(query);
        }
        if (_threads != null) {
            _threads.setPage(query, page);
        }
    }

    /**
     * Determines if this panel is in the given mode for the given group id and query.
     */
    public boolean isInMode (Mode mode, int groupId, String query)
    {
        return _mode == mode && _groupId == groupId && _query.equals(query);
    }

    /**
     * Navigates to the given page if we are currently displaying a thread list.
     */
    public void setPage (int page)
    {
        if (_threads != null) {
            _threads.setPage(_query, page);
        }
    }

    protected void startNewThread (final int groupId)
    {
        if (!MsoyUI.requireValidated()) {
            setContents(_mmsgs.ntpTitle(), MsoyUI.createLabel(_cmsgs.requiresValidated(), null));
            return;
        }

        final ForumModels.GroupThreads gthreads = _fmodels.getGroupThreads(groupId);
        if (gthreads.canStartThread()) {
            setContents(_mmsgs.ntpTitle(), new NewThreadPanel(groupId, gthreads.isManager(),
                                                              gthreads.isAnnounce()));
            return;
        }

        if (gthreads.isFetched()) {
            setContents(_mmsgs.ntpTitle(),
                        MsoyUI.createLabel(_mmsgs.errNoPermissionsToPost(), null));
            return;
        }

        gthreads.doFetch(new Command() {
            public void execute () {
                startNewThread(groupId);
            }
        });
    }

    protected SmartTable createHeader (String title, SearchBox.Listener listener)
    {
        SmartTable header = new SmartTable(0, 0);
        header.setWidth("100%");
        int col = 0;
        if (_mode == Mode.GROUP) {
            Anchor rss = new Anchor("/rss/" + _groupId, "", "_blank");
            rss.setHTML(AbstractImagePrototype.create(_images.rss()).getHTML());
            header.setWidget(0, col++, rss, 1, "RSS");
        }
        // default title may be overwritten later
        _title = MsoyUI.createSimplePanel(new Label(title), "TitleBox");
        header.setWidget(0, col++, _title, 1, "Title");

        if (listener != null) {
            header.setWidget(0, col++, _searchBox = new SearchBox(listener), 1, "Search");
        }
        header.setText(0, col++, _mmsgs.groupThreadPosts(), 1, "Posts");
        header.setText(0, col++, _mmsgs.groupThreadLastPost(), 1, "LastPost");
        header.setText(0, col++, "", 1, "IgnoreThread");
        return header;
    }

    /**
     * After _fmodels is filled, override the default title with the group name and a link to it.
     */
    protected void setGroupTitle (int groupId, String groupName)
    {
        if (groupId > 0) {
            _title.setWidget(Link.groupView(groupName, groupId));
        }
    }

    protected void newThreadPosted (ForumThread thread)
    {
        MsoyUI.info(_mmsgs.msgNewThreadPosted());
        _fmodels.newThreadPosted(thread);
        // go back to page 0 so the new thread will show up
        Link.go(Pages.GROUPS, "f", _groupId);
    }

    protected void newThreadCanceled (int groupId)
    {
        History.back();
    }

    /** Our forum model cache. */
    protected ForumModels _fmodels;

    /** The mode we're in. */
    protected Mode _mode;

    /** The group we're looking at, if any. */
    protected int _groupId;

    /** The search box in the header above the thread list. */
    protected SearchBox _searchBox;

    /** The current list of threads, if any. */
    protected ThreadListPanel _threads;

    /** Title for the page, set to group name after data load */
    protected SimplePanel _title;

    /** Query used when constructing the panel. */
    protected String _query;

    protected static final MsgsImages _images = (MsgsImages)GWT.create(MsgsImages.class);
    protected static final MsgsMessages _mmsgs = (MsgsMessages)GWT.create(MsgsMessages.class);
    protected static final ShellMessages _cmsgs = (ShellMessages)GWT.create(ShellMessages.class);
}
