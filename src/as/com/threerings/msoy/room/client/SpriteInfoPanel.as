//
// $Id$

package com.threerings.msoy.room.client {

import mx.collections.ArrayCollection;
import mx.controls.List;
import mx.core.ClassFactory;
import mx.core.ScrollPolicy;

import com.threerings.util.Util;

import com.threerings.io.TypedArray;

import com.threerings.msoy.client.Msgs;
import com.threerings.msoy.client.Prefs;

import com.threerings.msoy.world.client.WorldContext;

import com.threerings.msoy.item.client.ItemService;
import com.threerings.msoy.item.data.all.ItemIdent;

import com.threerings.msoy.ui.FloatingPanel

/**
 * Shows info on a bunch of sprites.
 */
public class SpriteInfoPanel extends FloatingPanel
{
    public function SpriteInfoPanel (ctx :WorldContext, sprites :Array /* of MsoySprite */)
    {
        super(ctx, Msgs.WORLD.get("t.item_info"));
        showCloseButton = true;

        var idents :TypedArray = TypedArray.create(ItemIdent);
        // wrap each sprite inside an array so that we can fill in the names later
        var data :Array = sprites.map(function (sprite :MsoySprite, ... ignored) :Array {
            // sneak-build the idents array
            var ident :ItemIdent = sprite.getItemIdent();
            if (isRealIdent(ident)) {
                idents.push(ident);
            }
            return [ sprite ];
        });
        _data.source = data;

        _list = new List();
        _list.horizontalScrollPolicy = ScrollPolicy.OFF;
        _list.verticalScrollPolicy = ScrollPolicy.ON;
        _list.selectable = false;
        _list.itemRenderer = new ClassFactory(SpriteInfoRenderer);
        _list.dataProvider = _data;
        addChild(_list);

        // refresh the list when bleeps change
        Prefs.events.addEventListener(Prefs.BLEEPED_MEDIA, bleepChanged);

        var svc :ItemService = ctx.getClient().requireService(ItemService) as ItemService;
        svc.getItemNames(_ctx.getClient(), idents, ctx.resultListener(gotItemNames));
    }

    override public function close () :void
    {
        super.close();
        Prefs.events.removeEventListener(Prefs.BLEEPED_MEDIA, bleepChanged);
    }

    /**
     * Is the specified ident kosher to pass to getItemNames()?
     */
    public static function isRealIdent (ident :ItemIdent) :Boolean
    {
        return (ident != null) && (ident.type > 0) && (ident.itemId != 0);
    }

    /**
     * A result handler for the service request we make.
     */
    protected function gotItemNames (names :Array /* of String */) :void
    {
        // trek through the array, pushing on the name for any idents that we passed to the service
        for each (var data :Array in _data.source) {
            if (isRealIdent(MsoySprite(data[0]).getItemIdent())) {
                data.push(names.shift());
            }
        }
        _data.refresh();
    }

    /**
     * Handle bleep changes. Just globally refresh the list for simplicity.
     * Note: We can't just Util.adapt() _data.refresh as the event listener because then we:
     *  - can't remove it
     *  - or, can't add the listener weakly, because it will get collected while we're up.
     * So we might as well have a real method, this one, to handle it.
     */
    protected function bleepChanged (... ignored) :void
    {
        _data.refresh();
    }

    protected var _list :List;

    protected var _data :ArrayCollection = new ArrayCollection();
}
}

import mx.containers.HBox;
import mx.controls.Label;
import mx.core.ScrollPolicy;

import com.threerings.flex.CommandButton;

import com.threerings.msoy.client.Msgs;
import com.threerings.msoy.client.MsoyController;

import com.threerings.msoy.item.data.all.ItemIdent;

import com.threerings.msoy.world.client.WorldContext;

import com.threerings.msoy.room.client.MsoySprite;
import com.threerings.msoy.room.client.SpriteInfoPanel;

class SpriteInfoRenderer extends HBox
{
    public function SpriteInfoRenderer ()
    {
        horizontalScrollPolicy = ScrollPolicy.OFF;
        verticalScrollPolicy = ScrollPolicy.OFF;

        _type = new Label();
        _type.width = 60;

        _name = new Label();
        _name.width = 160;

        _info = new CommandButton(Msgs.GENERAL.get("b.view_info"));
        _bleep = new CommandButton();
    }

    override public function set data (value :Object) :void
    {
        super.data = value;

        var arr :Array = value as Array;

        var sprite :MsoySprite = arr[0];
        _type.text = Msgs.GENERAL.get(sprite.getDesc());
        _bleep.setCallback(sprite.toggleBleeped);
        _bleep.enabled = sprite.isBleepable();
        _bleep.label = Msgs.GENERAL.get(sprite.isBleeped() ? "b.unbleep" : "b.bleep");

        var ident :ItemIdent = sprite.getItemIdent();
        _info.setCommand(MsoyController.VIEW_ITEM, ident);
        _info.enabled = SpriteInfoPanel.isRealIdent(ident);

        var name :String = arr[1];
        _name.text = name;
    }

    override protected function createChildren () :void
    {
        super.createChildren();

        setStyle("paddingTop", 0);
        setStyle("paddingBottom", 0);
        setStyle("paddingLeft", 3);
        setStyle("paddingRight", 3);
        setStyle("verticalAlign", "middle");

        addChild(_type);
        addChild(_name);
        addChild(_info);
        addChild(_bleep);
    }

    protected var _type :Label;
    protected var _name :Label;
    protected var _info :CommandButton;
    protected var _bleep :CommandButton;
}
