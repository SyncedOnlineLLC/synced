//
// $Id$

package tutorial {

import flash.display.*;
import flash.text.*;
import flash.geom.*;
import flash.events.*;
import flash.filters.*;
import flash.net.*;
import flash.ui.*;
import flash.utils.*;

import com.threerings.util.EmbeddedSwfLoader;
import com.threerings.flash.SimpleTextButton;

public class Swirl extends Sprite
{
    public function Swirl (view :View, swirlBytes :ByteArray, done :Function)
    {
        var loader :EmbeddedSwfLoader = new EmbeddedSwfLoader();
        loader.addEventListener(Event.COMPLETE, handleSwirlLoaded);
        loader.load(swirlBytes);

        _view = view;
        _done = done;

        _swirlState = View.SWIRL_NONE;
    }

    public function isReady () :Boolean
    {
        return _swirl != null;
    }

    public function unload () :void
    {
        if (_swirlHandler) {
            _swirlHandler.unload();
        }
    }

    public function gotoState (state :int) :void
    {
        this.visible = true;

        var first :String = null;
        var then :String = null;

        this.x = this.y = 75;

        switch(state) {
        case View.SWIRL_INTRO:
            if (_swirlState != View.SWIRL_NONE) {
                log.warning("Unexpected transtion [from=" + _swirlState + ", to=" +
                            state + "]");
            }
            // TODO: later we'll do a fancy transtion here, for now just appear
            this.x = 200;
            this.y = 200;

            then = SCN_APPEAR;
            break;
        case View.SWIRL_DEMURE:
            if (_swirlState == View.SWIRL_INTRO) {
                first = SCN_MINIMIZE;
            }
            then = SCN_IDLE;
            break;
        case View.SWIRL_BOUNCY:
            if (_swirlState == View.SWIRL_DEMURE) {
//                first = SCN_MAXIMIZE;
            }
            then = SCN_LOOKATME;
            break;
        default:
            log.warning("Can't goto unknown swirl state [state=" + state + "]");
            return;
        }

        // there is always a 'then' transition, so write code to handle it
        var transition :Function = function () :void {
            _swirlHandler.gotoScene(then, null);
        };

        // then execute that code either as a second-phase callback, or immediately
        if (first) {
            _swirlHandler.gotoScene(first, transition);
        } else {
            transition();
        }

        _swirlState = state;
    }

    protected function handleSwirlLoaded (evt :Event) :void
    {
        var swirlClip :MovieClip = MovieClip(EmbeddedSwfLoader(evt.target).getContent());
        _swirl = new Buttonizer(swirlClip);
        _swirl.addEventListener(MouseEvent.CLICK, function (evt :Event) :void {
            log.debug("Swirly clicked!");
            _view.swirlClicked(_swirlState);
        });

        _swirl.x =  -Content.SWIRL_OFFSET.x;
        _swirl.y =  -Content.SWIRL_OFFSET.y;
        addChild(_swirl);

        _swirlHandler = new ClipHandler(swirlClip);

        this.visible = false;

        _done();
    }

    protected var _view :View;
    protected var _done :Function;

    protected var _swirl :Sprite;
    protected var _swirlHandler :ClipHandler;
    protected var _swirlState :int;

    protected static const log :Log = Log.getLog(Swirl);

    protected static const SCN_APPEAR :String = "appear_text";
    protected static const SCN_MINIMIZE :String = "minimize";
    protected static const SCN_IDLE :String = "idle";
    protected static const SCN_LOOKATME :String = "lookatme";
    protected static const SCN_GOODJOB :String = "goodjob";
}
}

