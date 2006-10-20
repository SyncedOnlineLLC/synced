//
// $Id$

package com.threerings.msoy.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.threerings.msoy.web.data.MailFolder;
import com.threerings.msoy.web.data.MailMessage;
import com.threerings.msoy.web.data.ServiceException;
import com.threerings.msoy.web.data.WebCreds;

/**
 * Defines mail services available to the GWT/AJAX web client.
 */
public interface MailService extends RemoteService
{
    public MailFolder getFolder (WebCreds creds, int folderId)
        throws ServiceException;
    
    public List getFolders (WebCreds creds)
        throws ServiceException;

    public MailMessage getMessage (WebCreds creds, int folderId, int messageId)
        throws ServiceException;
    
    public List getMessages (WebCreds creds, int folderId)
        throws ServiceException;
    
    public void deliverMail (WebCreds creds, MailMessage msg)
        throws ServiceException;

    public void moveMail (WebCreds creds, MailMessage msg, int newFolderId)
        throws ServiceException;

    public void deleteMail (WebCreds creds, int folderId, int[] msgIdArr)
        throws ServiceException;
}
