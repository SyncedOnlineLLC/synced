//
// $Id$

package com.threerings.msoy.peer.server;

import com.threerings.msoy.peer.client.PeerGameService;
import com.threerings.msoy.peer.data.PeerGameMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link PeerGameProvider}.
 */
public class PeerGameDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public PeerGameDispatcher (PeerGameProvider provider)
    {
        this.provider = provider;
    }

    @Override // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new PeerGameMarshaller();
    }

    @SuppressWarnings("unchecked")
    @Override // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case PeerGameMarshaller.GAME_RECORD_UPDATED:
            ((PeerGameProvider)provider).gameRecordUpdated(
                source,
                ((Integer)args[0]).intValue()
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
