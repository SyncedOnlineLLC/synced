//
// $Id$

package com.threerings.msoy.web.gwt;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.threerings.msoy.data.all.LaunchConfig;
import com.threerings.msoy.data.all.VisitorInfo;
import com.threerings.msoy.web.gwt.WebUserService.AppResult;

/**
 * The asynchronous (client-side) version of {@link WebUserService}.
 */
public interface WebUserServiceAsync
{
    /**
     * The asynchronous version of {@link WebUserService#logon}.
     */
    void logon (String clientVersion, String username, String password, int expireDays,
                       AsyncCallback<SessionData> callback);

    /**
     * The asynchronous version of {@link WebUserService#externalLogon}.
     */
    void externalLogon (String clientVersion, ExternalCreds creds, VisitorInfo vinfo,
                        int expireDays, AsyncCallback<SessionData> callback);

    /**
     * The asynchronous version of {@link WebUserService#register}.
     */
    void register (String clientVersion, RegisterInfo info, boolean forceValidation,
                   AsyncCallback<WebUserService.RegisterData> callback);

    /**
     * The asynchronous version of {@link WebUserService#validateSession}.
     */
    void validateSession (String clientVersion, String authtok, int expireDays,
        int appId, AsyncCallback<SessionData> callback);

    /**
     * The asynchronous version of {@link WebUserService#linkExternalAccount}.
     */
    void linkExternalAccount (ExternalCreds creds, boolean override,
                              AsyncCallback<Boolean> callback);

    /**
     * The asynchronous version of {@link WebUserService#getConnectConfig}.
     */
    void getConnectConfig (AsyncCallback<ConnectConfig> callback);

    /**
     * The asynchronous version of {@link WebUserService#loadLaunchConfig}.
     */
    void loadLaunchConfig (int gameId, AsyncCallback<LaunchConfig> callback);

    /**
     * The asynchronous version of {@link WebUserService#sendForgotPasswordEmail}.
     */
    void sendForgotPasswordEmail (String email, AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#updateEmail}.
     */
    void updateEmail (String newEmail, AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#updateEmailPrefs}.
     */
    void updateEmailPrefs (boolean emailOnWhirledMail,
                           boolean emailAnnouncements, AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#updatePassword}.
     */
    void updatePassword (String newPassword, AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#resetPassword}.
     */
    void resetPassword (int memberId, String code, String newPassword,
                        AsyncCallback<Boolean> callback);

    /**
     * The asynchronous version of {@link WebUserService#configurePermaName}.
     */
    void configurePermaName (String permaName, AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#getAccountInfo}.
     */
    void getAccountInfo (AsyncCallback<AccountInfo> callback);

    /**
     * The asynchronous version of {@link WebUserService#updateAccountInfo}.
     */
    void updateAccountInfo (AccountInfo info, AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#updateCharity}.
     */
    void updateCharity (int selectedCharityId, AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#resendValidationEmail}.
     */
    void resendValidationEmail (AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#validateEmail}.
     */
    void validateEmail (int memberId, String code, AsyncCallback<SessionData> callback);

    /**
     * The asynchronous version of {@link WebUserService#requestAccountDeletion}.
     */
    void requestAccountDeletion (AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#deleteAccount}.
     */
    void deleteAccount (String password, String code, AsyncCallback<Void> callback);

    /**
     * The asynchronous version of {@link WebUserService#getApp}.
     */
    void getApp (int appId, AsyncCallback<AppResult> callback);

    /**
     * The asynchronous version of {@link WebUserService#getFBConnectApp}.
     */
    void getFBConnectApp (AsyncCallback<AppResult> callback);
}
