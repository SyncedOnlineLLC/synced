//
// $Id$

package client.inventory;

import com.google.gwt.core.client.GWT;

import com.threerings.msoy.item.data.all.Item;

import client.editem.EditorHost;
import client.editem.ItemEditor;
import client.shell.Application;
import client.shell.Args;
import client.shell.Frame;
import client.shell.Page;
import client.util.MsoyUI;

/**
 * Handles the MetaSOY inventory application.
 */
public class index extends Page
    implements EditorHost
{
    /** Required to map this entry point to a page. */
    public static Creator getCreator ()
    {
        return new Creator() {
            public Page createPage () {
                return new index();
            }
        };
    }

    // @Override from Page
    public void onHistoryChanged (Args args)
    {
        if (CInventory.ident == null) {
            // if we have no creds, just display a message saying login
            setTitle(null);
            setContent(MsoyUI.createLabel(CInventory.msgs.logon(), "infoLabel"));
            _inventory = null;
            return;
        }

        // if we're editing an item, display that interface
        if (args.get(0, "").equals("e")) {
            byte type = (byte)args.get(1, Item.AVATAR);
            ItemEditor editor = ItemEditor.createItemEditor(type, this);
            int itemId = args.get(2, 0);
            if (itemId != 0) {
                setTitle(CInventory.msgs.editItemTitle());
                Item item = _models.findItem(type, itemId);
                if (item == null) {
                    editor.setItem(type, itemId);
                } else {
                    editor.setItem(item);
                }
            } else {
                setTitle(CInventory.msgs.createItemTitle());
                editor.setItem(editor.createBlankItem());
            }
            setContent(editor);
            return;
        }

        // otherwise we're viewing our inventory
        displayInventory((byte)args.get(0, Item.AVATAR), args.get(1, -1), args.get(2, 0));
    }

    // from interface EditorHost
    public void editComplete (Item item)
    {
        // TODO: update inventory model
        displayInventory(item.getType(), -1, item.itemId);
    }

    // @Override // from Page
    protected String getPageId ()
    {
        return "inventory";
    }

    // @Override // from Page
    protected void initContext ()
    {
        super.initContext();

        // load up our translation dictionaries
        CInventory.msgs = (InventoryMessages)GWT.create(InventoryMessages.class);
    }

    // @Override // from Page
    protected void didLogoff ()
    {
        // go to whirledwide instead of reloading as a non-member
        Application.go(Page.WHIRLED, "whirledwide");
    }

    protected void displayInventory (byte type, int pageNo, int itemId)
    {
        setTitle(null);
        setContent(_inventory);
        setPageTabs(_inventory.getTabs());
        _inventory.display(type, pageNo, itemId);
    }

    protected void setTitle (String subtitle)
    {
        Frame.setTitle(CInventory.msgs.inventoryTitle(), subtitle);
    }

    protected InventoryModels _models = new InventoryModels();
    protected InventoryPanel _inventory = new InventoryPanel(_models);
}
