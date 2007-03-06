//
// $Id$

package com.threerings.msoy.swiftly.client;        

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.msoy.item.web.MediaDesc;

import com.threerings.msoy.swiftly.data.DocumentUpdatedEvent;
import com.threerings.msoy.swiftly.data.PathElement;
import com.threerings.msoy.swiftly.data.ProjectRoomObject;
import com.threerings.msoy.swiftly.data.SwiftlyCodes;
import com.threerings.msoy.swiftly.data.SwiftlyDocument;
import com.threerings.msoy.swiftly.util.SwiftlyContext;

public class SwiftlyEditor extends PlacePanel
    implements AttributeChangeListener, SetListener
{
    public SwiftlyEditor (ProjectRoomController ctrl, SwiftlyContext ctx)
    {
        super(ctrl);
        _ctx = ctx;

        setLayout(new VGroupLayout(VGroupLayout.STRETCH, VGroupLayout.STRETCH, 5,
                                   VGroupLayout.TOP));
        // let's not jam ourselves up against the edges of the window
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // setup the components
        _editorTabs = new TabbedEditor(_ctx, this);
        _editorTabs.setMinimumSize(new Dimension(400, 400));

        _consoleTabs = new TabbedConsole(_ctx, this);
        _consoleTabs.setMinimumSize(new Dimension(0, 0));

        _projectPanel = new ProjectPanel(_ctx, this);
        _projectPanel.setMinimumSize(new Dimension(0, 0));

        _toolbar = new EditorToolBar(ctrl, _ctx, this);

        _vertSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _editorTabs, _consoleTabs);
        // TODO apparently GTK does not have the graphic for this. What to do?
        _vertSplitPane.setOneTouchExpandable(true);
        _vertSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        _horizSplitPane =
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _vertSplitPane, _projectPanel);
        _horizSplitPane.setOneTouchExpandable(true);
        _horizSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // layout the window
        add(_toolbar, VGroupLayout.FIXED);
        add(_horizSplitPane);

        // TODO this is an ideal way to layout the splits, but is not working. revisit
        // _vertSplitPane.setDividerLocation(0.8);
        // _horizSplitPane.setDividerLocation(0.8);
        _horizSplitPane.setDividerLocation(600);

        initFileTypes();
        consoleMessage(_ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.welcome"));
    }

    public void addEditorTab (PathElement pathElement)
    {
        SwiftlyTextPane pane = _editorTabs.addEditorTab(pathElement); 
        // If this is a new tab, add a documentupdate listener and ask the backend to load
        // the document contents or pull the already opened document from the dset
        if (pane != null) {
            _roomObj.addListener(pane);

            // If the document is already in the dset, load that.
            // TODO: Get rid of the expensive iteration (map of pathElement ->
            // documents?)
            for (SwiftlyDocument doc : _roomObj.documents) {
                if (doc.getPathElement().elementId == pathElement.elementId) {
                    pane.setDocument(doc);
                    return;
                }
            }

            // Otherwise load the document from the backend.
            _roomObj.service.loadDocument(_ctx.getClient(), pathElement);
        }
    }

    public void updateTabTitleAt (PathElement pathElement)
    {
        _editorTabs.updateTabTitleAt(pathElement);
    }

    public void setTabDocument (SwiftlyDocument doc)
    {
        _editorTabs.setTabDocument(doc);
    }

    public void updateCurrentTabTitle ()
    {
        _editorTabs.updateCurrentTabTitle();
    }

    public void closeCurrentTab ()
    {
        _roomObj.removeListener(_editorTabs.getCurrentTextPane());
        _editorTabs.closeCurrentTab();
    }

    /**
     * See {@link TabbedConsole} for documentation.
     */
    public void consoleMessage (String message)
    {
        _consoleTabs.consoleMessage(message);
    }

    public AbstractAction createCloseCurrentTabAction ()
    {
        return _editorTabs.createCloseCurrentTabAction();
    }

    public EditorToolBar getToolbar()
    {
        return _toolbar;
    }

    public ProjectPanel getProjectPanel ()
    {
        return _projectPanel;
    }

    /**
     * Sends a message to the server reporting that the given document element should have its
     * text replaced with the supplied string.
     */
    public void updateDocument (int elementId, String text)
    {
        _roomObj.service.updateDocument(_ctx.getClient(), elementId, text);
    }

    /**
     * Shows a modal, internal frame dialog prompting the user to name a {@link PathElement}
     * @param pathElementType the type of {@link PathElement} to name
     * @return the name of the path element. null if the user clicked cancel
     */
     // TODO: this is only being used to name directories. Consider simplifying
    public String showSelectPathElementNameDialog (PathElement.Type pathElementType)
    {
        String prompt;
        prompt = _ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.dialog.select_name." + pathElementType);
        return JOptionPane.showInternalInputDialog(this, prompt);
    }

    /**
     * Shows a modal, external frame dialog prompting the user to name a {@link PathElement.FILE}
     * and select the mime type for this file.
     * @param parentElement the PathElement that will be the parent of the returned PathElement
     * @return the new path element. null if the user clicked cancel
     */
    public PathElement showCreateFileDialog (PathElement parentElement)
    {
        CreateFileDialog dialog = new CreateFileDialog();
        // return null if the user hit cancelled or did not set a file name
        if (dialog.wasCancelled() || StringUtil.isBlank(dialog.getName())) {
            return null;
        }
        return PathElement.createFile(dialog.getName(), parentElement, dialog.getMimeType());
    }

    /**
     * Shows a modal, internal frame dialog reporting an error to the user.
     * @param the error message to display
     */
    public void showErrorDialog (String message)
    {
        JOptionPane.showInternalMessageDialog(this, message,
            _ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.dialog.error.title"),
            JOptionPane.ERROR_MESSAGE);
    }

    @Override // from PlacePanel
    public void willEnterPlace (PlaceObject plobj)
    {
        _roomObj = (ProjectRoomObject)plobj;
        _roomObj.addListener(this);

        // Raise all any documents from the dead, re-binding transient
        // instance variables.
        for (SwiftlyDocument document : _roomObj.documents) {
            document.lazarus(_roomObj.pathElements);
        }

        // let our project panel know about all the roomy goodness
        _projectPanel.setProject(_roomObj);
    }

    @Override // from PlacePanel
    public void didLeavePlace (PlaceObject plobj)
    {
        if (_roomObj != null) {
            _roomObj.removeListener(this);
            _roomObj = null;
        }

        // TODO: shutdown the project panel?
    }

    // from interface AttributeChangeListener
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(ProjectRoomObject.CONSOLE)) {
            consoleMessage(_ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, _roomObj.console));
        }
    }

    // from interface SetListener
    public void entryAdded (EntryAddedEvent event)
    {
        if (event.getName().equals(ProjectRoomObject.DOCUMENTS)) {
            final SwiftlyDocument element = (SwiftlyDocument)event.getEntry();

            // Re-bind transient instance variables
            element.lazarus(_roomObj.pathElements);

            // set the document if a tab is opened
            setTabDocument(element);
        }
    }

    // from interface SetListener
    public void entryUpdated (EntryUpdatedEvent event)
    {
        // TODO do we actually want to do anything here?
        if (event.getName().equals(ProjectRoomObject.DOCUMENTS)) {
            final SwiftlyDocument element = (SwiftlyDocument)event.getEntry();

            // Re-bind transient instance variables
            element.lazarus(_roomObj.pathElements);
        }
    }

    // from interface SetListener
    public void entryRemoved (EntryRemovedEvent event)
    {
        // TODO: if that document is still open FREAK OUT. Server is going to refcount so shouldn't
        // ever happen.
        if (event.getName().equals(ProjectRoomObject.DOCUMENTS)) {
            final int elementId = (Integer)event.getKey();
        }
    }

    /** Initialize the file types that can be created */
    protected void initFileTypes()
    {
    _createableFileTypes = new ArrayList<FileTypes>();
    _createableFileTypes.add(new FileTypes(_ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.filetypes." +
        MediaDesc.TEXT_PLAIN), MediaDesc.mimeTypeToString(MediaDesc.TEXT_PLAIN)));
    _createableFileTypes.add(new FileTypes(_ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.filetypes." + 
        MediaDesc.TEXT_ACTIONSCRIPT), MediaDesc.mimeTypeToString(MediaDesc.TEXT_ACTIONSCRIPT)));
    }

    /** A dialog window to prompt the user for a file name and file type. */
    protected class CreateFileDialog extends JDialog
    {
        public CreateFileDialog () {
            super(new JFrame(),
                _ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.dialog.create_file.title"), true);
            setLayout(new GridLayout(3, 3));

            // file name input
            add(new JLabel(_ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.dialog.create_file.name")));
            _text = new JTextField();
            _text.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _cancelled = false;
                    setVisible(false);
                }
            });
            _text.setEditable(true);
            add(_text);

            // file type chooser
            add(new JLabel(_ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.dialog.create_file.type")));
            _comboBox = new JComboBox(_createableFileTypes.toArray());
            add(_comboBox);

            // ok/cancel buttons
            JButton button =
                new JButton(_ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.dialog.create_file.create"));
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _cancelled = false;
                    setVisible(false);
                }
            });
            add(button);
            button =
                new JButton(_ctx.xlate(SwiftlyCodes.SWIFTLY_MSGS, "m.dialog.create_file.cancel"));
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _cancelled = true;
                    setVisible(false);
                }
            });
            add(button);

            // display the dialog
            pack();
            setVisible(true);
        }

        public void display ()
        {
        }

        public String getName ()
        {
            return _text.getText();
        }

        public String getMimeType ()
        {
            return ((FileTypes)_comboBox.getSelectedItem()).mimeType;
        }

        public boolean wasCancelled ()
        {
            return _cancelled;
        }

        protected JTextField _text;
        protected JComboBox _comboBox;
        // whether the user clicked cancel. defaults to true to deal with closing the dialog.
        protected boolean _cancelled = true;
    }

    /** A class that maps a human friendly name to a mime type. */
    protected class FileTypes
    {
        public FileTypes (String displayName, String mimeType)
        {
            this.displayName = displayName;
            this.mimeType = mimeType;
        }

        public String toString()
        {
            return displayName;
        }

        public String displayName;
        public String mimeType;
    }

    /** A list of files that can be created by Swiftly. */
    protected ArrayList<FileTypes> _createableFileTypes;

    protected SwiftlyContext _ctx;
    protected ProjectRoomObject _roomObj;
    protected PathElement _project;

    protected TabbedEditor _editorTabs;
    protected TabbedConsole _consoleTabs;
    protected EditorToolBar _toolbar;
    protected ProjectPanel _projectPanel;
    protected JSplitPane _vertSplitPane;
    protected JSplitPane _horizSplitPane;
}
