//
// $Id$

package com.threerings.msoy.web.gwt;

/**
 * Names of cookies used by msoy web client and/or server.
 * TODO: move all cookie names here.
 */
public class CookieNames
{
    /** Holds the A/B tests that apply to newly landed users. */
    public static final String LANDING_TEST = "lt";

    /** Holds the account name of the recently logged in user. */
    public static final String WHO = "who";

    /** Holds the id of the visitor. */
    public static final String VISITOR = "vis";

    /** Holds info about who invited the visitor. */
    public static final String AFFILIATE = "a";

    /** Sets where or not the user claims to have bookmarked our FB app. */
    public static final String BOOKMARKED = "bmd";

    /** A session cookie that lets the GWT client know it should supply a real entry vector. */
    public static final String NEED_GWT_VECTOR = "gwtvec";
}
