//
// $Id$

package com.threerings.msoy.web.data;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains a snapshot of the user's data delivered when they validate their session.
 */
public class SessionData implements IsSerializable
{
    public WebCreds creds;

    public int flow;
    public int gold;
    public int level;

    public int newMailCount;

    public List friends;
}
