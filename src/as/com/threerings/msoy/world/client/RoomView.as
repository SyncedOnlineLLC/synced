package com.threerings.msoy.world.client {

import flash.display.DisplayObject;
import flash.display.Graphics;

import flash.events.Event;

import flash.geom.Point;
import flash.geom.Rectangle;

import flash.utils.getTimer; // function import

import mx.containers.Canvas;

import mx.controls.VideoDisplay;

import mx.core.UIComponent;
import mx.core.ScrollPolicy;

import mx.events.FlexEvent;
import mx.events.ResizeEvent;

import com.threerings.util.HashMap;
import com.threerings.util.Iterator;

import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.crowd.chat.client.ChatDisplay;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.UserMessage;

import com.threerings.whirled.data.SceneUpdate;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotSceneObject;
import com.threerings.whirled.spot.data.SceneLocation;

import com.threerings.msoy.chat.client.ChatOverlay;
//import com.threerings.msoy.chat.client.ChatPopper;
import com.threerings.msoy.client.ContextMenuProvider;
import com.threerings.msoy.client.MsoyContext;
import com.threerings.msoy.client.Prefs;
import com.threerings.msoy.item.web.MediaDesc;
import com.threerings.msoy.world.data.FurniData;
import com.threerings.msoy.world.data.ModifyFurniUpdate;
import com.threerings.msoy.world.data.MsoyLocation;
import com.threerings.msoy.world.data.MsoyScene;
import com.threerings.msoy.world.data.MsoySceneModel;
import com.threerings.msoy.world.data.SceneAttrsUpdate;
import com.threerings.msoy.world.data.WorldMemberInfo;

public class RoomView extends AbstractRoomView
    implements ContextMenuProvider, SetListener, ChatDisplay, MessageListener
{
    public function RoomView (ctx :MsoyContext, ctrl :RoomController)
    {
        super(ctx);
        _ctrl = ctrl;

        _overlay = new ChatOverlay(ctx);
    }

    override protected function updateComplete (evt :FlexEvent) :void
    {
//        ChatPopper.setChatView(this);
        super.updateComplete(evt);
    }

    override protected function relayout () :void
    {
        super.relayout();

        var sprite :MsoySprite;
        for each (sprite in _avatars.values()) {
            locationUpdated(sprite);
        }
        for each (sprite in _pendingRemoveAvatars.values()) {
            locationUpdated(sprite);
        }
    }

    override public function setEditing (
            editing :Boolean, spriteVisitFn :Function) :void
    {
        super.setEditing(editing, spriteVisitFn);

        // we hide all avatars instead of visiting them.
        if (editing) {
            _roomObj.removeListener(this);
            removeAllOccupants();
            setCenterSprite(null);
            shutdownMusic();

        } else {
            rereadScene();
            _roomObj.addListener(this)
            addAllOccupants();
        }
    }

    /**
     * Re-set our scene to the one that the scene director knows about.
     */
    public function rereadScene () :void
    {
        setScene(_ctx.getSceneDirector().getScene() as MsoyScene);
    }

    /**
     * Called to re-set the scene to the one specified.
     * Only the scene properties are updated, furni and portal
     * changes are not typically noted.
     */
    public function setScene (scene :MsoyScene) :void
    {
        _scene = scene;
        updateDrawnRoom();
        relayout();
    }

    override public function locationUpdated (sprite :MsoySprite) :void
    {
        super.locationUpdated(sprite);

        // if we moved the _centerSprite, possibly update the scroll position
        if (sprite == _centerSprite && ((sprite != _bkg) ||
                _scene.getSceneType() != MsoySceneModel.FIXED_IMAGE)) {
            scrollView();
        }
    }

    /**
     * A callback from avatar sprites.
     */
    public function moveFinished (avatar :AvatarSprite) :void
    {
        if (null != _pendingRemoveAvatars.remove(avatar.getOid())) {
            removeSprite(avatar);
        }
    }

    /**
     * Set the sprite we should be following.
     */
    public function setCenterSprite (center :MsoySprite) :void
    {
        _centerSprite = center;
        scrollView();
    }

    /**
     * Set whether we instantly jump to center, or scroll there.
     */
    public function setFastCentering (fastCentering :Boolean) :void
    {
        _jumpScroll = fastCentering;
    }

    public function dimAvatars (setDim :Boolean) :void
    {
        setActive(_avatars, !setDim);
        setActive(_pendingRemoveAvatars, !setDim);
    }

    public function dimFurni (setDim :Boolean) :void
    {
        setActive(_furni, !setDim);
    }

    // from ContextMenuProvider
    public function populateContextMenu (menuItems :Array) :void
    {
        // Holy flying fistfuck, batman, we're trying to get around some
        // flash madness here. The context menu should pop up with the
        // lowest-hitting object as the target but for some reason it doesn't.
        // Currently the lowest target is this RoomView, so we do a service
        // and check to see if any of our sprites are also ContextMenuProviders.
        var sx :Number = stage.mouseX;
        var sy :Number = stage.mouseY;
        for (var dex :int = numChildren - 1; dex >= 0; dex--) {
            var disp :DisplayObject = getChildAt(dex);
            if (disp.hitTestPoint(sx, sy, true)) {
                if (disp is ContextMenuProvider) {
                    (disp as ContextMenuProvider).populateContextMenu(menuItems);
                }
                break;
            }
        }

        // our controller handles our population
        _ctrl.populateContextMenu(menuItems);
    }

    /**
     * Called by our controller when a scene update is received.
     */
    public function processUpdate (update :SceneUpdate) :void
    {
        if (update is ModifyFurniUpdate) {
            for each (var furni :FurniData in
                    (update as ModifyFurniUpdate).furniRemoved) {
                removeFurni(furni);
            }

        } else if (update is SceneAttrsUpdate) {
            // re-read our scene and that's it
            rereadScene();
            return;

        } else {
            throw new Error();
        }

        // this will take care of anything added
        updateAllFurni();
    }

    override public function scrollViewBy (xpixels :int) :Boolean
    {
        var canScroll :Boolean = super.scrollViewBy(xpixels);
        if (canScroll) {
            // remove any autoscrolling (if tick is not a registered listener
            // this will safely noop)
            removeEventListener(Event.ENTER_FRAME, tick);
            _jumpScroll = false;
        }
        return canScroll;
    }

    protected function scrollView () :void
    {
        if (_centerSprite == null) {
            return;
        }
        var rect :Rectangle = scrollRect;
        if (rect == null) {
            // return if there's nothing to scroll
            return;
        }

        var centerX :int = _centerSprite.x + _centerSprite.getLayoutHotSpot().x;
        var bounds :Rectangle = getScrollBounds();

        var newX :Number = centerX - rect.width/2;
        newX = Math.min(bounds.x + bounds.width - rect.width,
            Math.max(bounds.x, newX));

        if (_jumpScroll) {
            rect.x = newX;

        } else if (Math.abs(rect.x - newX) > MAX_AUTO_SCROLL) {
            if (newX > rect.x) {
                rect.x += MAX_AUTO_SCROLL;
            } else {
                rect.x -= MAX_AUTO_SCROLL;
            }
            addEventListener(Event.ENTER_FRAME, tick);

        } else {
            rect.x = newX;
            removeEventListener(Event.ENTER_FRAME, tick);
            _jumpScroll = true;
        }

        // assign the new scrolling rectangle
        scrollRect = rect;
        scrollRectUpdated();
        _suppressAutoScroll = true;
    }

    protected function tick (event :Event) :void
    {
        if (!_suppressAutoScroll) {
            if (_centerSprite != null) {
                scrollView();

            } else {
                // stop scrolling
                removeEventListener(Event.ENTER_FRAME, tick);
            }
        }

        // and finally, we want ensure it can happen on the next frame if
        // our avatar doesn't move
        _suppressAutoScroll = false;
    }

    public function getMyAvatar () :AvatarSprite
    {
        var oid :int = _ctx.getClient().getClientOid();
        var avatar :AvatarSprite = (_avatars.get(oid) as AvatarSprite);
        if (avatar == null) {
            avatar = (_pendingRemoveAvatars.get(oid) as AvatarSprite);
        }
        return avatar;
    }

    /**
     * Return the current location of the avatar that represents our body.
     */
    public function getMyCurrentLocation () :MsoyLocation
    {
        return getMyAvatar().loc;
    }

    /**
     * @return true if the specified click target should trigger
     * location movements.
     */
    public function isLocationTarget (clickTarget :DisplayObject) :Boolean
    {
        return (clickTarget == this) || (clickTarget == _bkgGraphics) ||
            (_bkg != null && _bkg.contains(clickTarget)) ||
            // scan through the media and see if it was non-interactive
            isNonInteractiveTarget(clickTarget, _furni) /*||
            isNonInteractiveTarget(clickTarget, _portals) ||
            isNonInteractiveTarget(clickTarget, _avatars)*/;
    }

    protected function isNonInteractiveTarget (
            clickTarget :DisplayObject, map :HashMap) :Boolean
    {
        for each (var spr :MsoySprite in map.values()) {
            if (!spr.isInteractive() && spr.contains(clickTarget)) {
                return true;
            }
        }
        return false;
    }

    protected function addBody (bodyOid :int) :void
    {
        var occInfo :WorldMemberInfo =
            (_roomObj.occupantInfo.get(bodyOid) as WorldMemberInfo);
        var sloc :SceneLocation =
            (_roomObj.occupantLocs.get(bodyOid) as SceneLocation);
        var loc :MsoyLocation = (sloc.loc as MsoyLocation);

        // see if the avatar was already created, pending removal
        var avatar :AvatarSprite =
            (_pendingRemoveAvatars.remove(bodyOid) as AvatarSprite);

        if (avatar == null) {
            avatar = _ctx.getMediaDirector().getAvatar(occInfo);
            _avatars.put(bodyOid, avatar);
            addChild(avatar);
            avatar.setEntering(loc);

            // if we ever add ourselves, we follow it
            if (bodyOid == _ctx.getClient().getClientOid()) {
                setFastCentering(true);
                setCenterSprite(avatar);
            }

        } else {
            // place the sprite back into the set of active sprites
            _avatars.put(bodyOid, avatar);
            avatar.moveTo(loc, _scene);
        }
    }

    protected function removeBody (bodyOid :int) :void
    {
        var avatar :AvatarSprite = (_avatars.remove(bodyOid) as AvatarSprite);

        if (avatar != null) {
            if (avatar.isMoving()) {
                _pendingRemoveAvatars.put(bodyOid, avatar);

            } else {
                removeSprite(avatar);
            }
        }
    }

    protected function moveBody (bodyOid :int) :void
    {
        var avatar :AvatarSprite = (_avatars.get(bodyOid) as AvatarSprite);
        var sloc :SceneLocation =
            (_roomObj.occupantLocs.get(bodyOid) as SceneLocation);
        var loc :MsoyLocation = (sloc.loc as MsoyLocation);

        avatar.moveTo(loc, _scene);
    }

    protected function updateBody (occInfo :WorldMemberInfo) :void
    {
        var avatar :AvatarSprite =
            (_avatars.get(occInfo.getBodyOid()) as AvatarSprite);
        if (avatar == null) {
            Log.getLog(this).warning("No avatar for updated occupantInfo? " +
                "[occInfo=" + occInfo + "].");
            return;
        }
        avatar.setOccupantInfo(_ctx, occInfo);
    }

    /**
     * Called when we detect a body being added or removed.
     */
    protected function portalTraversed (loc :Location, entering :Boolean) :void
    {
        var itr :Iterator = _scene.getPortals();
        while (itr.hasNext()) {
            var portal :Portal = (itr.next() as Portal);
            if (loc.equals(portal.loc)) {
                var sprite :FurniSprite =
                    (_furni.get(portal.portalId) as FurniSprite);
                sprite.wasTraversed(entering);
                return;
            }
        }
    }

    override protected function removeSprite (sprite :MsoySprite) :void
    {
        super.removeSprite(sprite);

        if (sprite is AvatarSprite) {
            var avatar :AvatarSprite = (sprite as AvatarSprite);
            portalTraversed(avatar.loc, false);
        }
        if (sprite == _centerSprite) {
            _centerSprite = null;
        }
    }

    // documentation inherited from interface PlaceView
    override public function willEnterPlace (plobj :PlaceObject) :void
    {
        super.willEnterPlace(plobj);

        _roomObj.addListener(this);

//        _ctx.getChatDirector().addChatDisplay(this);
        _overlay.setTarget(_ctx.getTopPanel());

        addAllOccupants();

        // and animate ourselves entering the room (everyone already in the
        // (room will also have seen it)
        portalTraversed(getMyCurrentLocation(), true);
    }

    // documentation inherited from interface PlaceView
    override public function didLeavePlace (plobj :PlaceObject) :void
    {
        _roomObj.removeListener(this);

//        _ctx.getChatDirector().removeChatDisplay(this);
//        ChatPopper.popAllDown();
        _overlay.setTarget(null);

        shutdownMusic();
        removeAllOccupants();

        super.didLeavePlace(plobj);
    }

    override public function updateAllFurni () :void
    {
        super.updateAllFurni();

        var music :FurniData = _scene.getMusic();
        if (music != null) {
            // maybe shutdown old music
            if (_music != null && !_music.getMedia().equals(music.media)) {
                shutdownMusic(); // will set _music = null
            }
            // set up the new music
            if (_music == null) {
                _music = new SoundPlayer(music.media);
                _music.loop();
                //var pos :Number = Prefs.getMediaPosition(music.getMediaId());
                //_music.loop(pos);
                // NOTE: the position argument has been disabled because
                // it causes the flash player to crash, and also seems to booch
                // proper looping.
            }
            // set the volume, even if we're just re-setting it on
            // already-playing music
            _music.setVolume(Number(music.actionData));
        }
    }

    /**
     * Shut-down the background music in the room.
     */
    protected function shutdownMusic () :void
    {
        if (_music != null) {
            //var pos :Number = _music.getPosition();
            //Prefs.setMediaPosition(_music.getMedia().getMediaId(), pos);
            _music.stop();
            _music = null;
        }
    }

    protected function addAllOccupants () :void
    {
        // add all currently present occupants
        for (var ii :int = _roomObj.occupants.size() - 1; ii >= 0; ii--) {
            addBody(_roomObj.occupants.getAt(ii));
        }
    }

    protected function removeAllOccupants () :void
    {
        removeAll(_avatars);
        removeAll(_pendingRemoveAvatars);
    }

    // documentation inherited from interface SetListener
    public function entryAdded (event :EntryAddedEvent) :void
    {
        var name :String = event.getName();

        if (PlaceObject.OCCUPANT_INFO == name) {
            addBody((event.getEntry() as WorldMemberInfo).getBodyOid());

        } else if (SpotSceneObject.OCCUPANT_LOCS == name) {
            var sceneLoc :SceneLocation = (event.getEntry() as SceneLocation);
            portalTraversed(sceneLoc.loc, true);
        }
    }

    // documentation inherited from interface SetListener
    public function entryUpdated (event :EntryUpdatedEvent) :void
    {
        var name :String = event.getName();

        if (PlaceObject.OCCUPANT_INFO == name) {
            updateBody(event.getEntry() as WorldMemberInfo);

        } else if (SpotSceneObject.OCCUPANT_LOCS == name) {
            moveBody((event.getEntry() as SceneLocation).bodyOid);
        }
    }

    // documentation inherited from interface SetListener
    public function entryRemoved (event :EntryRemovedEvent) :void
    {
        var name :String = event.getName();

        if (PlaceObject.OCCUPANT_INFO == name) {
            removeBody((event.getOldEntry() as WorldMemberInfo).getBodyOid());
        }
    }

    // fro interface MessageListener
    public function messageReceived (event :MessageEvent) :void
    {
        var args :Array = event.getArgs();
        switch (event.getName()) {
        case "avAction":
            performAvatarAction(int(args[0]), (args[1] as String));
            break;
        }
    }

    // documentation inherited from interface ChatDisplay
    public function clear () :void
    {
//        ChatPopper.popAllDown();
    }

    // documentation inherited from interface ChatDisplay
    public function displayMessage (
        msg :ChatMessage, alreadyDisplayed :Boolean) :Boolean
    {
        var avatar :AvatarSprite = null;
        if (msg is UserMessage) {
            var umsg :UserMessage = (msg as UserMessage);
            var occInfo :OccupantInfo = _roomObj.getOccupantInfo(umsg.speaker);
            if (occInfo != null) {
                avatar = (_avatars.get(occInfo.bodyOid) as AvatarSprite);
            }
        }

//        ChatPopper.popUp(msg, avatar);
        if (avatar != null) {
            avatar.performAvatarSpoke();
        }
        return true;
    }

    /**
     * Make that avatar dance, or whatever.
     */
    protected function performAvatarAction (bodyOid :int, action :String) :void
    {
        var avatar :AvatarSprite = (_avatars.get(bodyOid) as AvatarSprite);
        if (avatar != null) {
            avatar.performAvatarAction(action);
        }
    }

    protected function removeFurni (furni :FurniData) :void
    {
        var sprite :FurniSprite = (_furni.remove(furni.id) as FurniSprite);
        if (sprite != null) {
            removeSprite(sprite);
        }
    }

    /** Our controller. */
    protected var _ctrl :RoomController;

    /** The background music in the scene. */
    protected var _music :SoundPlayer;

    protected var _overlay :ChatOverlay;

    /** A map of bodyOid -> AvatarSprite. */
    protected var _avatars :HashMap = new HashMap();

    /** The sprite we should center on. */
    protected var _centerSprite :MsoySprite;

    /** A map of bodyOid -> AvatarSprite for those that we'll remove
     * when they stop moving. */
    protected var _pendingRemoveAvatars :HashMap = new HashMap();

    /** If true, the scrolling should simply jump to the right position. */
    protected var _jumpScroll :Boolean = true;

    /** True if autoscroll should be supressed for the current frame. */
    protected var _suppressAutoScroll :Boolean = false;

    /** The maximum number of pixels to autoscroll per frame. */
    protected static const MAX_AUTO_SCROLL :int = 15;
}
}
