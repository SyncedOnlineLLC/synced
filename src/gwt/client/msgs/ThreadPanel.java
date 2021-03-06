//
// $Id$

package client.msgs;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;

import com.threerings.gwt.ui.SmartTable;
import com.threerings.gwt.ui.WidgetUtil;

import com.threerings.msoy.fora.gwt.ForumMessage;
import com.threerings.msoy.fora.gwt.ForumService;
import com.threerings.msoy.fora.gwt.ForumServiceAsync;
import com.threerings.msoy.fora.gwt.ForumThread;
import com.threerings.msoy.web.gwt.Pages;

import client.richedit.MessageEditor;
import client.shell.ShellMessages;
import client.ui.BorderedDialog;
import client.ui.MessagePanel;
import client.ui.MiniNowLoadingWidget;
import client.ui.MsoyUI;
import client.ui.SearchBox;
import client.util.ClickCallback;
import client.util.Link;
import client.util.NonCountingDataModel;

/**
 * Displays a thread header and either its messages or a post creation or editing panel.
 */
public class ThreadPanel extends TitledListPanel
    implements SearchBox.Listener
{
    public ThreadPanel ()
    {
        _theader = new SmartTable(0, 0);
        _theader.setWidth("100%");
        _theader.setWidget(0, 0, MsoyUI.createBackArrow(), 1, "Back");
        _theader.setText(0, 1, "", 1, "Whirled");
        _theader.setText(0, 2, "...", 1, "Title");
        _theader.setWidget(0, 3, _search = new SearchBox(this), 1, "Search");
    }

    public void showThread (ForumModels fmodels, int threadId, int page, int scrollToId)
    {
        _threadId = threadId;
        _mpanel.display(fmodels.getThreadMessages(_threadId), page, scrollToId);
        showMessages();
    }

    public void showMessages ()
    {
        setContents(_theader, _mpanel);
    }

    public int getThreadId ()
    {
        return _threadId;
    }

    public void gotThread (ForumThread thread)
    {
        _thread = thread;
        _theader.setText(0, 2, _thread.subject);
        _theader.setWidget(0, 1, Link.create(""+_thread.group, Pages.GROUPS,
                                             "f", _thread.group.getGroupId()));
    }

    public void editThread ()
    {
        if (MsoyUI.requireValidated()) {
            new ThreadEditorPanel().show();
        }
    }

    public void postReply (ForumMessage inReplyTo, boolean quote)
    {
        if (MsoyUI.requireValidated()) {
            setContents(_mmsgs.threadReplyHeader(_thread.subject),
                        new ReplyPanel(inReplyTo, quote));
        }
    }

    public void editPost (ForumMessage message, AsyncCallback<ForumMessage> callback)
    {
        if (MsoyUI.requireValidated()) {
            setContents(_thread.subject, new PostEditorPanel(message, callback));
        }
    }

    // from interface SearchBox.Listener
    public void search (final String query)
    {
        // replace search box with loading animation while fetching
        _theader.setWidget(0, 3, new MiniNowLoadingWidget(), 1, "Search");

        _mpanel.setModel(new NonCountingDataModel<ForumMessage, List<ForumMessage>>() {
            @Override protected void callFetchService (int start, int count, boolean needCount,
                AsyncCallback<List<ForumMessage>> callback) {
                _forumsvc.findMessages(_threadId, query, start, count, callback);
            }
            @Override protected List<ForumMessage> getRows (List<ForumMessage> result) {
                _theader.setWidget(0, 3, _search, 1, "Search");
                return result;
            }
        }, 0);
    }

    // from interface SearchBox.Listener
    public void clearSearch ()
    {
        _mpanel.restoreThread();
    }

    protected void replyPosted (ForumMessage message)
    {
        _mpanel.replyPosted(message);
        showMessages();
    }

    protected static boolean checkMessageText (String text)
    {
        if (text.length() == 0) {
            MsoyUI.error(_mmsgs.errMissingReply());
            return false;
        }
        return true;
    }

    protected class ReplyPanel extends TableFooterPanel
    {
        public ReplyPanel (ForumMessage inReplyTo, boolean quote)
        {
            _editor = MessageEditor.createDefault();
            _content.setWidget(0, 0, _editor.asWidget());

            if (inReplyTo != null) {
                // set the quote text if available
                if (quote) {
                    _editor.setHTML(_mmsgs.replyQuote(
                        MsoyUI.escapeHTML(inReplyTo.poster.name.toString()),
                        inReplyTo.message));
                    DeferredCommand.addCommand(new Command() {
                        public void execute () {
                            _editor.selectAll();
                        }
                    });
                }

                // display the message to which we are replying below everything
                MessagePanel reply = new MessagePanel() {
                    public boolean textIsHTML () {
                        return true;
                    }
                    @Override // from MessagePanel
                    protected boolean shouldShowRoleCaption () {
                        return true;
                    }
                };
                reply.setMessage(inReplyTo.poster, inReplyTo.created, inReplyTo.message);

                int row = getRowCount();
                setWidget(row++, 0, WidgetUtil.makeShim(10, 10));
                getFlexCellFormatter().setStyleName(row, 0, "Header");
                setWidget(row++, 0, MsoyUI.createLabel(_mmsgs.replyInReplyTo(), "Title"));
                setWidget(row++, 0, reply);
            }

            addFooterButton(_editor.getToggler());
            addFooterButton(new Button(_cmsgs.cancel(), new ClickHandler() {
                public void onClick (ClickEvent event) {
                    showMessages();
                }
            }));
            Button submit = new Button(_cmsgs.send());
            final int replyId = (inReplyTo == null) ? 0 : inReplyTo.messageId;
            final int replyMemberId = (inReplyTo == null || inReplyTo.poster == null ||
                    inReplyTo.poster.name == null) ? 0 : inReplyTo.poster.name.getId();
            new ForumCallback<ForumMessage>(submit) {
                @Override protected boolean callService () {
                    String text = _editor.getHTML();
                    if (!checkMessageText(text)) {
                        return false;
                    }
                    _forumsvc.postMessage(_threadId, replyId, replyMemberId, text, this);
                    return true;
                }
                @Override protected boolean gotResult (ForumMessage result) {
                    replyPosted(result);
                    return false;
                }
            };
            addFooterButton(submit);
        }

        @Override // from Widget
        protected void onAttach ()
        {
            super.onAttach();
            _editor.setFocus(true);
        }

        protected MessageEditor.Panel _editor;
    }

    protected class PostEditorPanel extends TableFooterPanel
    {
        public PostEditorPanel (ForumMessage message, AsyncCallback<ForumMessage> callback)
        {
            _messageId = message.messageId;
            _callback = callback;

            _editor = MessageEditor.createDefault();
            _content.setWidget(0, 0, _editor.asWidget());
            _editor.setHTML(message.message);

            addFooterButton(_editor.getToggler());
            addFooterButton(new Button(_cmsgs.cancel(), new ClickHandler() {
                public void onClick (ClickEvent event) {
                    showMessages();
                }
            }));

            Button submit = new Button(_cmsgs.change());
            new ForumCallback<ForumMessage>(submit) {
                @Override protected boolean callService () {
                    _text = _editor.getHTML();
                    if (!checkMessageText(_text)) {
                        return false;
                    }
                    _forumsvc.editMessage(_messageId, _text, this);
                    return true;
                }
                @Override protected boolean gotResult (ForumMessage result) {
                    MsoyUI.info(_mmsgs.msgPostUpdated());
                    _callback.onSuccess(result);
                    showMessages();
                    return false;
                }
                protected String _text;
            };
            addFooterButton(submit);
        }

        @Override // from Widget
        protected void onAttach ()
        {
            super.onAttach();
            _editor.setFocus(true);
        }

        protected int _messageId;
        protected AsyncCallback<ForumMessage> _callback;
        protected MessageEditor.Panel _editor;
    }

    protected class ThreadEditorPanel extends BorderedDialog
    {
        public ThreadEditorPanel ()
        {
            setHeaderTitle(_mmsgs.tfepTitle());

            FlexTable main = new FlexTable();
            main.setCellSpacing(10);

            int row = 0;
            main.setText(row, 0, _mmsgs.tfepIntro());
            main.getFlexCellFormatter().setColSpan(row++, 0, 2);

            main.setText(row, 0, _mmsgs.tfepSubject());
            main.getFlexCellFormatter().setStyleName(row, 0, "rightLabel");
            main.setWidget(row++, 1, _subject = MsoyUI.createTextBox(
                _thread.subject, ForumThread.MAX_SUBJECT_LENGTH, 24));

            main.setText(row, 0, _mmsgs.tfepAnnouncement());
            main.getFlexCellFormatter().setStyleName(row, 0, "rightLabel");
            main.setWidget(row++, 1, _announce = new CheckBox());
            _announce.setValue(_thread.isAnnouncement());

            main.setText(row, 0, _mmsgs.tfepSticky());
            main.getFlexCellFormatter().setStyleName(row, 0, "rightLabel");
            main.setWidget(row++, 1, _sticky = new CheckBox());
            _sticky.setValue(_thread.isSticky());

            main.setText(row, 0, _mmsgs.tfepLocked());
            main.getFlexCellFormatter().setStyleName(row, 0, "rightLabel");
            main.setWidget(row++, 1, _locked = new CheckBox());
            _locked.setValue(_thread.isLocked());
            setContents(main);

            addButton(new Button(_cmsgs.cancel(), new ClickHandler() {
                public void onClick (ClickEvent event) {
                    ThreadEditorPanel.this.hide();
                }
            }));

            Button update = new Button(_cmsgs.update());
            new ClickCallback<Void>(update) {
                @Override protected boolean callService () {
                    _flags |= (_announce.getValue() ? ForumThread.FLAG_ANNOUNCEMENT : 0);
                    _flags |= (_sticky.getValue() ? ForumThread.FLAG_STICKY : 0);
                    _flags |= (_locked.getValue() ? ForumThread.FLAG_LOCKED : 0);
                    _forumsvc.updateThread(_thread.threadId, _flags, _subject.getText(), this);
                    return true;
                }
                @Override protected boolean gotResult (Void result) {
                    _thread.flags = _flags;
                    _thread.subject = _subject.getText();
                    MsoyUI.info(_mmsgs.tfepUpdated());
                    ThreadEditorPanel.this.hide();
                    // update the thread panel title
                    gotThread(_thread);
                    return true;
                }
                protected int _flags;
            };
            addButton(update);
        }

        protected CheckBox _announce, _sticky, _locked;
        protected TextBox _subject;
    }

    protected int _threadId;
    protected ForumThread _thread;
    protected SmartTable _theader;
    protected SearchBox _search;
    protected MessagesPanel _mpanel = new MessagesPanel(this);

    protected static final ShellMessages _cmsgs = GWT.create(ShellMessages.class);
    protected static final MsgsMessages _mmsgs = (MsgsMessages)GWT.create(MsgsMessages.class);
    protected static final ForumServiceAsync _forumsvc = GWT.create(ForumService.class);

    protected static final int MAX_RESULTS = 20;
}
