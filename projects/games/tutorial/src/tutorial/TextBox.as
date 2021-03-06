//
// $Id$

package tutorial {

import flash.display.MovieClip;
import flash.display.SimpleButton;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import flash.utils.ByteArray;
import flash.utils.clearTimeout;
import flash.utils.setTimeout;

import flash.text.AntiAliasType;
import flash.text.StyleSheet;
import flash.text.TextField;
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;
import flash.text.TextFormatAlign;

import com.threerings.flash.AlphaFade;
import com.threerings.flash.SimpleTextButton;
import com.threerings.util.Log;
import com.threerings.util.MultiLoader;

public class TextBox extends Sprite
{
    public function TextBox (swirlBytes :ByteArray, done :Function)
    {
        MultiLoader.getContents(swirlBytes, handleTextboxLoaded);

        _done = done;

        var styleSheet :StyleSheet = new StyleSheet();
        styleSheet.parseCSS(
            "body {" +
            "  color: #000000;" +
            "}" +
            ".title {" +
            "  font-family: SunnySide;" +
            "  font-size: 20;" +
            "  text-align: left;" +
            "}" +
            ".message {" +
            "  font-family: Goudy;" +
            "  font-size: 16;" +
            "  text-align: left;" +
            "}" +
            ".details {" +
            "  font-family: Goudy;" +
            "  font-size: 14;" +
            "  text-align: left;" +
            "}");

        _textField = new TextField();
        _textField.defaultTextFormat = getDefaultFormat();
        _textField.styleSheet = styleSheet;
        _textField.selectable = false;
        _textField.wordWrap = true;
        _textField.multiline = true;
        _textField.embedFonts = true;
        _textField.antiAliasType = AntiAliasType.ADVANCED;
        _textField.autoSize = TextFieldAutoSize.LEFT;
        _textField.width = 300;

        // we start off invisible
        this.visible = false;
    }

    public function unload () :void
    {
    }

    public function isReady () :Boolean
    {
        return _buttons != null;
    }

    public function hideBox () :void
    {
        clearTimer();
        if (_fadeIn.isPlaying()) {
            _fadeIn.stopAnimation();
        }
        _fadeOut.startAnimation();
    }

    public function sizeChanged () :void
    {
        if (stage == null) {
            return; // god knows
        }
        this.x = Math.max(-40, Math.min(10, stage.stageWidth - _backdrop.width));
        this.y = Content.BOX_HAT + 5;
    }

    public function showBox (text :String) :void
    {
        clearTimer();
        if (_fadeOut.isPlaying()) {
            _fadeOut.stopAnimation();
        }

        while (_buttons.numChildren > 0) {
            _buttons.removeChildAt(0);
        }

        _boxHandler.gotoScene(SCN_TEXTBOX_GROW, function () :void {
           _boxClip.stop();
        });

        replaceTimer(function () :void {
            _fadeIn.startAnimation();
            _foreground.visible = true;
        }, 400);

        _textField.htmlText = em(text);

        _rightButtonEdge = _textField.width;
        _leftButtonEdge = 0;

        scaleBackdrop(figureWidth(), figureHeight());

        _textField.y = _backdrop.y + Content.BOX_TOP_PADDING;
        _buttons.y = _textField.y + _textField.height + 5;

        _foreground.visible = false;
        _backdrop.visible = this.visible = true;

        sizeChanged();
    }

    public function addButton (label :String, right :Boolean, onClick :Function) :SimpleButton
    {
        var button :SimpleButton = new SimpleTextButton(
            label, true, 0x003366, 0x6699CC, 0x0066FF, 5, getDefaultFormat());
        button.addEventListener(MouseEvent.CLICK, function (evt :Event) :void {
                onClick();
            });
        _buttons.addChild(button);
        if (right) {
            button.x = _rightButtonEdge - button.width;
            _rightButtonEdge -= button.width + Content.BOX_PADDING;
        } else {
            button.x = _leftButtonEdge;
            _leftButtonEdge += button.width + Content.BOX_PADDING;
        }

        scaleBackdrop(-1, figureHeight());
        sizeChanged();

        return button;
    }

    protected function clearTimer () :void
    {
        if (_timer) {
            clearTimeout(_timer);
        }
    }

    protected function replaceTimer (fun :Function, delay :Number) :void
    {
        clearTimer();
        _timer = setTimeout(function () :void { _timer = 0; fun(); }, delay);
    }

    protected function handleTextboxLoaded (clip :MovieClip) :void
    {
        // create the textbox clip
        _boxClip = clip;
        _boxClip.x = -Content.BOX_OFFSET.x;
        _boxClip.y = -Content.BOX_OFFSET.y;

        // just move the playhead to a 'full textbox' position and stop there
        _boxClip.gotoAndStop(1, SCN_TEXTBOX_SHRINK);

        // so we can measure it
        _boxWidth = _boxClip.width;
        _boxHeight = _boxClip.height - Content.BOX_HAT;

        _backdrop = new Sprite();
        _backdrop.addChild(_boxClip);

        this.addChild(_backdrop);

        _foreground = new Sprite();

        _textField.x = _backdrop.x + Content.BOX_PADDING;
        _foreground.addChild(_textField);

        _buttons = new Sprite();
        _buttons.x = _textField.x;
        _foreground.addChild(_buttons);

        this.addChild(_foreground);

        _boxHandler = new ClipHandler(_boxClip);

        _fadeIn = new AlphaFade(_foreground, 0, 1, 300);
        _fadeOut = new AlphaFade(_foreground, 1, 0, 300, function () :void {
            _foreground.visible = false;
            _boxHandler.gotoScene(SCN_TEXTBOX_SHRINK, _boxClip.stop);
        });

        _done();
    }

    protected function figureWidth () :Number
    {
        return _textField.width + 2*Content.BOX_PADDING;
    }

    protected function figureHeight () :Number
    {
        return _textField.height + _buttons.height + Content.BOX_TOP_PADDING +
            Content.BOX_PADDING;
    }

    protected function scaleBackdrop (x :Number, y :Number) :void
    {
        if (x >= 0) {
            _backdrop.scaleX = x / _boxWidth;
        }
        if (y >= 0) {
            _backdrop.scaleY = y / _boxHeight;
        }
    }

    protected function getDefaultFormat () :TextFormat
    {
        var format :TextFormat = new TextFormat();
        format.font = "SunnySide";
        format.size = 14;
        format.color = 0x000000;
        format.align = TextFormatAlign.LEFT;
        return format;
    }

    protected static function em (text :String) :String
    {
        return text.replace(/\[\[/g, "<b><i>").replace(/\]\]/g, "</i></b>");
    }

    protected var _done :Function;
    protected var _boxClip :MovieClip;
    protected var _boxHandler :ClipHandler;

    protected var _boxWidth :Number;
    protected var _boxHeight :Number;

    protected var _backdrop :Sprite;
    protected var _foreground :Sprite;
    protected var _textField :TextField;

    protected var _buttons :Sprite;
    protected var _leftButtonEdge :int;
    protected var _rightButtonEdge :int;

    protected var _timer :int;
    protected var _fadeOut :AlphaFade;
    protected var _fadeIn :AlphaFade;

    protected static const log :Log = Log.getLog(TextBox);

    protected static const SCN_TEXTBOX_GROW :String = "textbox_grow";
    protected static const SCN_TEXTBOX_SHRINK :String = "textbox_shrink";
}
}
