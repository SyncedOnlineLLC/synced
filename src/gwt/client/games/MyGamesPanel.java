//
// $Id$

package client.games;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import com.threerings.msoy.game.data.all.GameGenre;
import com.threerings.msoy.game.gwt.GameInfo;
import com.threerings.msoy.game.gwt.GameService;
import com.threerings.msoy.game.gwt.GameServiceAsync;
import com.threerings.msoy.web.gwt.Args;
import com.threerings.msoy.web.gwt.Pages;

import client.ui.MsoyUI;
import client.util.InfoCallback;
import client.util.Link;
import client.util.ServiceUtil;

/**
 * Displays the games created by the caller.
 */
public class MyGamesPanel extends GameListPanel
{
    public MyGamesPanel (byte sortMethod)
    {
        super(GameGenre.ALL, sortMethod);

        FlowPanel header = MsoyUI.createFlowPanel("gameHeaderPanel");
        FlowPanel absbits = MsoyUI.createFlowPanel("Absolute");
        absbits.add(MsoyUI.createLabel(_msgs.myGames(), "GenreTitle"));
        absbits.add(MsoyUI.createHTML(_msgs.myGamesCreateTip(), "CreateTip"));
        absbits.add(MsoyUI.createButton(MsoyUI.SHORT_THIN, _msgs.myGamesCreate(),
                                        Link.createListener(Pages.GAMES, "create")));
        header.add(absbits);
        header.add(MsoyUI.createLabel(_msgs.myGamesDevTip(), "DevTip"));
        add(header);

        _gamesvc.loadMyGames(new InfoCallback<List<GameInfo>>() {
            public void onSuccess (List<GameInfo> games) {
                add(new GameGrid(games));
            }
        });
    }

    protected void onSortChanged (byte sortMethod)
    {
        Link.go(Pages.GAMES, Args.compose("mine", sortMethod));
    }

    protected static final GameServiceAsync _gamesvc = (GameServiceAsync)
        ServiceUtil.bind(GWT.create(GameService.class), GameService.ENTRY_POINT);
}
