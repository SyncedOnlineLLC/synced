//
// $Id$

package client.groups;

import com.google.gwt.core.client.GWT;

import com.threerings.msoy.group.gwt.GroupDetail;
import com.threerings.msoy.group.gwt.GroupService;
import com.threerings.msoy.group.gwt.GroupServiceAsync;
import com.threerings.msoy.web.gwt.Args;
import com.threerings.msoy.web.gwt.Pages;

import client.msgs.ForumModels;
import client.msgs.ForumPanel;
import client.msgs.ThreadPanel;
import client.shell.CShell;
import client.shell.Page;
import client.ui.MsoyUI;
import client.util.InfoCallback;

public class GroupsPage extends Page
{
    public enum Nav {
        DETAIL("d"), EDIT("edit"), THEME_EDIT("themed"), MYGROUPS("mygroups"), UNREAD("unread"),
        FRIENDS("friends"), FORUM("f"), POST("p"), THREAD("t"), MEDALS("m"),
        CREATEMEDAL("cm"), EDITMEDAL("em"),  DEFAULT("");

        public static Nav getGroupPage (Args args)
        {
            String action = args.get(0, "");
            for (Nav page : Nav.values()) {
                if (page.getNavToken().equals(action)) {
                    return page;
                }
            }
            return Nav.DEFAULT;
        }

        public String getNavToken () {
            return _navToken;
        }

        public Args composeArgs (Object... exArgs) {
            Args args = new Args();
            args.add(getNavToken());
            for (Object arg : exArgs) {
                args.add(arg);
            }
            return args;
        }

        Nav (String navToken) {
            _navToken = navToken;
        }

        protected String _navToken;
    }

    @Override // from Page
    public void onHistoryChanged (Args args)
    {
        Nav page = Nav.getGroupPage(args);

        if (page == Nav.DETAIL) {
            final GroupDetailPanel panel = new GroupDetailPanel();
            setContent(panel);

            runWithGroup(args.get(1, 0), new Runnable() {
                public void run () {
                    panel.setGroupDetail(_detail);
                }
            });

        } else if (page == Nav.DEFAULT && CShell.frame.getThemeId() != 0) {
            final GroupDetailPanel panel = new GroupDetailPanel();
            setContent(panel);

            runWithGroup(CShell.frame.getThemeId(), new Runnable() {
                public void run () {
                    panel.setGroupDetail(_detail);
                }
            });

        } else if (page == Nav.EDIT) {
            int groupId = args.get(1, 0);
            if (groupId == 0) {
                setContent(new GroupEdit());
            } else {
                runWithGroup(groupId, new Runnable() {
                    public void run () {
                        setContent(new GroupEdit(_detail.group, _detail.extras));
                    }
                });
            }

        } else if (page == Nav.THEME_EDIT) {
            int groupId = args.get(1, 0);
            if (groupId == 0) {
                MsoyUI.error("Need a group to edit theme.");
                return;
            }
            runWithGroup(groupId, new Runnable() {
                public void run () {
                    setContent(new ThemeEdit(_detail.group, _detail.theme));
                }
            });

        } else if (page == Nav.MYGROUPS) {
            setContent(_msgs.myGroupsTitle(), new MyGroupsPanel());

        } else if (page == Nav.UNREAD) {
            showForumPanel(ForumPanel.Mode.UNREAD, 0, args.get(1, ""), args.get(2, 0));

        } else if (page == Nav.FRIENDS) {
            showForumPanel(ForumPanel.Mode.FRIENDS, 0, args.get(1, ""), args.get(2, 0));

        } else if (page == Nav.FORUM) {
            showForumPanel(ForumPanel.Mode.GROUP, args.get(1, 0), args.get(2, ""), args.get(3, 0));

        } else if (page == Nav.POST) {
            showForumPanel(ForumPanel.Mode.NEW_THREAD, args.get(1, 0), null, 0);

        } else if (page == Nav.THREAD) {
            ThreadPanel tpanel;
            if (getContent() instanceof ThreadPanel) {
                tpanel = (ThreadPanel)getContent();
            } else {
                setContent(_msgs.forumsTitle(), tpanel = new ThreadPanel());
            }
            tpanel.showThread(_fmodels, args.get(1, 0), args.get(2, 0), args.get(3, 0));

        } else if (page == Nav.MEDALS) {
            int groupId = args.get(1, 0);
            setContent(_msgs.medalListTitle(), new MedalListPanel(groupId));

        } else if (page == Nav.CREATEMEDAL) {
            int groupId = args.get(1, 0);
            setContent(_msgs.editMedalCreate(), new EditMedalPanel(groupId, 0));

        } else if (page == Nav.EDITMEDAL) {
            int medalId = args.get(1, 0);
            setContent(_msgs.editMedalEdit(), new EditMedalPanel(0, medalId));

        } else if (args.get(0, "").length() > 0) {
            GroupListPanel list;
            if (getContent() instanceof GroupListPanel) {
                list = (GroupListPanel)getContent();
            } else {
                list = new GroupListPanel();
            }
            list.setArgs(args);
            setContent(list);

        } else {
            setContent(new GalaxyPanel());
        }
    }

    @Override
    public Pages getPageId ()
    {
        return Pages.GROUPS;
    }

    protected void showForumPanel (ForumPanel.Mode mode, int groupId, String search, int page)
    {
        if (getContent() instanceof ForumPanel) {
            ForumPanel fpanel = (ForumPanel)getContent();
            if (fpanel.isInMode(mode, groupId, search)) {
                fpanel.setPage(page);
                return;
            }
        }
        setContent(_msgs.forumsTitle(), new ForumPanel(_fmodels, mode, groupId, search, page));
    }

    protected void runWithGroup (int groupId, final Runnable callback)
    {
        if (_detail != null && _detail.group.groupId == groupId) {
            callback.run();
            return;
        }
        _groupsvc.getGroupDetail(groupId, new InfoCallback<GroupDetail>() {
            public void onSuccess (GroupDetail result) {
                _detail = result;
                callback.run();
            }
        });
    }

    protected ForumModels _fmodels = new ForumModels();
    protected GroupDetail _detail;

    protected static final GroupsMessages _msgs = GWT.create(GroupsMessages.class);
    protected static final GroupServiceAsync _groupsvc = GWT.create(GroupService.class);
}
