/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package org.atalk.hmos.gui.account.settings;

import android.content.SharedPreferences;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import net.java.sip.communicator.plugin.sipaccregwizz.AccountRegistrationImpl;
import net.java.sip.communicator.plugin.sipaccregwizz.SIPAccountRegistrationActivator;
import net.java.sip.communicator.service.certificate.CertificateConfigEntry;
import net.java.sip.communicator.service.certificate.CertificateService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.EncodingsRegistrationUtil;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.SecurityAccountRegistration;
import net.java.sip.communicator.service.protocol.sip.SIPAccountRegistration;

import org.atalk.hmos.R;
import org.atalk.hmos.aTalkApp;
import org.atalk.hmos.gui.settings.util.SummaryMapper;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * The preferences edit fragment for SIP accounts.
 *
 * @author Pawel Domas
 * @author Eng Chong Meng
 */
public class SipPreferenceFragment extends AccountPreferenceFragment
{
    private static final String PREF_KEY_USER_ID = "pref_key_user_id";

    private static final String PREF_KEY_SERVER_ADDRESS = "pref_key_server_address";

    private static final String PREF_KEY_AUTH_NAME = "pref_key_auth_name";

    /*
     * private static final String PREF_KEY_DEFAULT_DOMAIN = "pref_key_default_domain";
     */

    private static final String PREF_KEY_TLS_CERT_ID = "pref_key_client_tls_cert";

    private static final String PREF_KEY_PROXY_AUTO_CONFIG = "pref_key_proxy_auto_config";

    private static final String PREF_KEY_SERVER_PORT = "pref_key_server_port";

    private static final String PREF_KEY_PROXY_ADDRESS = "pref_key_ProxyAddress";

    private static final String PREF_KEY_PREFERRED_TRANSPORT = "pref_key_preferred_transport";

    private static final String PREF_KEY_PROXY_PORT = "pref_key_ProxyPort";

    private static final String PREF_KEY_IS_PRESENCE_EN = "pref_key_is_presence_enabled";

    private static final String PREF_KEY_FORCE_P2P = "pref_key_force_p2p";

    private static final String PREF_KEY_POLLING_PERIOD = "pref_key_polling_period";

    private static final String PREF_KEY_SUBSCRIPTION_PERIOD = "pref_key_subscription_period";

    private static final String PREF_KEY_KEEP_ALIVE_METHOD = "pref_key_keep_alive_method";

    private static final String PREF_KEY_PING_INTERVAL = "pref_key_ping_interval";

    private static final String PREF_KEY_DTMF_METHOD = "pref_key_dtmf_method";

    private static final String PREF_KEY_MWI_EN = "pref_key_mwi_enabled";

    private static final String PREF_KEY_VOICE_MAIL_URI = "pref_key_voicemail_uri";

    private static final String PREF_KEY_VOICE_MAIL_CHECK_URI = "pref_key_voicemail_check_uri";

    private static final String PREF_KEY_CONTACT_LIST_TYPE = "pref_key_contact_list_type";

    private static final String PREF_KEY_CLIST_SERVER_URI = "pref_key_clist_server_uri";

    private static final String PREF_KEY_CLIST_USE_SIP_CREDENTIALS = "pref_key_clist_use_sip_credentials";

    private static final String PREF_KEY_CLIST_USER = "pref_key_clist_user";

    private static final String PREF_KEY_CLIST_PASSWORD = "pref_key_clist_password";

    private static final String PREF_KEY_DISPLAYNAME = "pref_key_display_name";

    private static final String PREF_KEY_PASSWORD = "pref_key_password";

    private static final String PREF_KEY_STORE_PASSWORD = "pref_key_store_password";

    /**
     * Creates new instance.
     */
    public SipPreferenceFragment()
    {
        super(R.xml.acc_sip_preferences);
    }

    /**
     * Returns SIP registration wizard.
     *
     * @return SIP registration wizard.
     */
    private AccountRegistrationImpl getSipWizard()
    {
        return (AccountRegistrationImpl) getWizard();
    }

    /**
     * Returns SIP account registration object.
     *
     * @return SIP account registration object.
     */
    private SIPAccountRegistration getAccountRegistration()
    {
        return getSipWizard().getRegistration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EncodingsRegistrationUtil getEncodingsRegistration()
    {
        return getAccountRegistration().getEncodingsRegistration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SecurityAccountRegistration getSecurityRegistration()
    {
        return getAccountRegistration().getSecurityRegistration();
    }

    /**
     * {@inheritDoc}
     */
    protected void onInitPreferences()
    {
        SIPAccountRegistration registration = getSipWizard().getRegistration();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        SharedPreferences.Editor editor = preferences.edit();

        AccountID account = getAccountID();
        String userId = registration.getServerAddress() != null ? account.getUserID() : account.getAccountPropertyString(ProtocolProviderFactory.USER_ID);

        // User name and password
        String password = registration.getPassword();
        editor.putString(PREF_KEY_USER_ID, userId);
        editor.putString(PREF_KEY_PASSWORD, password);
        boolean storePass = registration.isRememberPassword();
        editor.putBoolean(PREF_KEY_STORE_PASSWORD, storePass);
        editor.putString(PREF_KEY_DISPLAYNAME, registration.getAccountDisplayName());
        // Connection
        editor.putString(PREF_KEY_AUTH_NAME, registration.getAuthorizationName());
        editor.putString(PREF_KEY_SERVER_PORT, registration.getServerPort());
        editor.putString(PREF_KEY_SERVER_ADDRESS, registration.getServerAddress());
        editor.putString(PREF_KEY_TLS_CERT_ID, registration.getTlsClientCertificate());
        editor.putString(PREF_KEY_DTMF_METHOD, registration.getDTMFMethod());
        editor.putString(PREF_KEY_PROXY_ADDRESS, registration.getProxy());
        editor.putString(PREF_KEY_PROXY_PORT, registration.getProxyPort());
        editor.putString(PREF_KEY_PREFERRED_TRANSPORT, registration.getPreferredTransport());
        editor.putString(PREF_KEY_KEEP_ALIVE_METHOD, registration.getKeepAliveMethod());
        editor.putString(PREF_KEY_PING_INTERVAL, registration.getPingInterval());
        editor.putBoolean(PREF_KEY_MWI_EN, registration.isMessageWaitingIndicationsEnabled());
        editor.putString(PREF_KEY_VOICE_MAIL_CHECK_URI, registration.getVoicemailCheckURI());
        editor.putString(PREF_KEY_VOICE_MAIL_URI, registration.getVoicemailURI());
        // Presence
        editor.putBoolean(PREF_KEY_IS_PRESENCE_EN, registration.isEnablePresence());
        editor.putBoolean(PREF_KEY_FORCE_P2P, registration.isForceP2PMode());
        editor.putBoolean(PREF_KEY_PROXY_AUTO_CONFIG, registration.isProxyAutoConfigure());
        editor.putString(PREF_KEY_POLLING_PERIOD, registration.getPingInterval());
        editor.putString(PREF_KEY_SUBSCRIPTION_PERIOD, registration.getSubscriptionExpiration());
        // Contact list
        // 0 - default contact list
        int cListTypeIdx = 0;
        if (registration.isXCapEnable()) {
            cListTypeIdx = 1;
        }
        else if (registration.isXiVOEnable()) {
            cListTypeIdx = 2;
        }
        String cListType = aTalkApp.getAppResources().getStringArray(R.array.pref_sip_clist_type)[cListTypeIdx];
        editor.putString(PREF_KEY_CONTACT_LIST_TYPE, cListType);
        editor.putBoolean(PREF_KEY_CLIST_USE_SIP_CREDENTIALS, registration.isClistOptionUseSipCredentials());
        editor.putString(PREF_KEY_CLIST_SERVER_URI, registration.getClistOptionServerUri());
        editor.putString(PREF_KEY_CLIST_USER, registration.getClistOptionUser());
        editor.putString(PREF_KEY_CLIST_PASSWORD, registration.getClistOptionPassword());

        editor.apply();
    }

    /**
     * {@inheritDoc}
     */
    protected void onPreferencesCreated()
    {
        // Enable/disable contact list items on init
        updateContactListViews();

        List<String> certList = new ArrayList<>();

        CertificateService cvs = SIPAccountRegistrationActivator.getCertificateService();
        List<CertificateConfigEntry> certEntries = cvs.getClientAuthCertificateConfigs();
        certEntries.add(0, CertificateConfigEntry.CERT_NONE);

        for (CertificateConfigEntry e : certEntries) {
            certList.add(e.toString());
        }

        AccountID accountID = getAccountID();

        String currentCert = accountID.getTlsClientCertificate();
        if (!certList.contains(currentCert) && !isInitialized()) {
            // The empty one
            currentCert = certList.get(0);
            getPreferenceManager().getSharedPreferences().edit().putString(PREF_KEY_TLS_CERT_ID, currentCert).apply();
        }

        String[] entries = new String[certList.size()];
        entries = certList.toArray(entries);
        ListPreference certPreference = (ListPreference) findPreference(PREF_KEY_TLS_CERT_ID);
        certPreference.setEntries(entries);
        certPreference.setEntryValues(entries);

        if (!isInitialized())
            certPreference.setValue(currentCert);
    }

    /**
     * {@inheritDoc}
     */
    protected void mapSummaries(SummaryMapper summaryMapper)
    {
        String emptyStr = getEmptyPreferenceStr();

        // User section
        summaryMapper.includePreference(findPreference(PREF_KEY_USER_ID), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_PASSWORD), emptyStr, new SummaryMapper.PasswordMask());
        summaryMapper.includePreference(findPreference(PREF_KEY_DISPLAYNAME), emptyStr);

        // Connection -> General
        summaryMapper.includePreference(findPreference(PREF_KEY_SERVER_ADDRESS), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_SERVER_PORT), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_AUTH_NAME), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_TLS_CERT_ID), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_DTMF_METHOD), emptyStr, input -> {
            ListPreference lp = findPreference(PREF_KEY_DTMF_METHOD);
            return lp.getEntry().toString();
        });

        // Connection -> Keep alive
        summaryMapper.includePreference(findPreference(PREF_KEY_KEEP_ALIVE_METHOD), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_PING_INTERVAL), emptyStr);

        // Connection -> Voicemail
        summaryMapper.includePreference(findPreference(PREF_KEY_VOICE_MAIL_URI), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_VOICE_MAIL_CHECK_URI), emptyStr);

        // Proxy options
        summaryMapper.includePreference(findPreference(PREF_KEY_PROXY_ADDRESS), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_PROXY_PORT), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_PREFERRED_TRANSPORT), emptyStr);

        // Presence -> Presence options
        summaryMapper.includePreference(findPreference(PREF_KEY_POLLING_PERIOD), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_SUBSCRIPTION_PERIOD), emptyStr);

        // Presence -> Contact list options
        summaryMapper.includePreference(findPreference(PREF_KEY_CONTACT_LIST_TYPE), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_CLIST_SERVER_URI), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_CLIST_USER), emptyStr);
        summaryMapper.includePreference(findPreference(PREF_KEY_CLIST_PASSWORD), emptyStr, new SummaryMapper.PasswordMask());
    }

    /**
     * {@inheritDoc}
     */
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
        super.onSharedPreferenceChanged(prefs, key);

        SIPAccountRegistration reg = (SIPAccountRegistration) getAccountRegistration();

        switch (key) {
            case PREF_KEY_PASSWORD:
                reg.setPassword(prefs.getString(PREF_KEY_PASSWORD, null));
                break;
            case PREF_KEY_DISPLAYNAME:
                reg.setAccountDisplayName(prefs.getString(PREF_KEY_DISPLAYNAME, null));
                break;
            case PREF_KEY_STORE_PASSWORD:
                reg.setRememberPassword(prefs.getBoolean(PREF_KEY_STORE_PASSWORD, true));
                break;
            case PREF_KEY_SERVER_ADDRESS:
                // reg.setServerAddress(
                // prefs.getString(PREF_KEY_SERVER_ADDRESS, null));
                // Can not be changed
                break;
            case PREF_KEY_USER_ID:
                // User id can not be changed
                break;
            case PREF_KEY_SERVER_PORT:
                reg.setServerPort(prefs.getString(PREF_KEY_SERVER_PORT, null));
                break;
            case PREF_KEY_AUTH_NAME:
                reg.setAuthorizationName(prefs.getString(PREF_KEY_AUTH_NAME, null));
                break;
            case PREF_KEY_TLS_CERT_ID:
                reg.setTlsClientCertificate(prefs.getString(PREF_KEY_TLS_CERT_ID, null));
                break;
            case PREF_KEY_PROXY_AUTO_CONFIG:
                reg.setProxyAutoConfigure(prefs.getBoolean(PREF_KEY_PROXY_AUTO_CONFIG, true));
                break;
            case PREF_KEY_PROXY_ADDRESS:
                reg.setProxy(prefs.getString(PREF_KEY_PROXY_ADDRESS, null));
                break;
            case PREF_KEY_PROXY_PORT:
                reg.setProxyPort(prefs.getString(PREF_KEY_PROXY_PORT, null));
                break;
            case PREF_KEY_PREFERRED_TRANSPORT:
                reg.setPreferredTransport(prefs.getString(PREF_KEY_PREFERRED_TRANSPORT, null));
                break;
            case PREF_KEY_KEEP_ALIVE_METHOD:
                reg.setKeepAliveMethod(prefs.getString(PREF_KEY_KEEP_ALIVE_METHOD, null));
                break;
            case PREF_KEY_PING_INTERVAL:
                reg.setPingInterval(prefs.getString(PREF_KEY_PING_INTERVAL, null));
                break;
            case PREF_KEY_MWI_EN:
                reg.setMessageWaitingIndications(prefs.getBoolean(PREF_KEY_MWI_EN, true));
                break;
            case PREF_KEY_VOICE_MAIL_URI:
                reg.setVoicemailURI(prefs.getString(PREF_KEY_VOICE_MAIL_URI, null));
                break;
            case PREF_KEY_VOICE_MAIL_CHECK_URI:
                reg.setVoicemailCheckURI(prefs.getString(PREF_KEY_VOICE_MAIL_CHECK_URI, null));
                break;
            case PREF_KEY_DTMF_METHOD:
                reg.setDTMFMethod(prefs.getString(PREF_KEY_DTMF_METHOD, null));
                break;
            case PREF_KEY_IS_PRESENCE_EN:
                reg.setEnablePresence(prefs.getBoolean(PREF_KEY_IS_PRESENCE_EN, true));
                break;
            case PREF_KEY_FORCE_P2P:
                reg.setForceP2PMode(prefs.getBoolean(PREF_KEY_FORCE_P2P, false));
                break;
            case PREF_KEY_POLLING_PERIOD:
                reg.setPollingPeriod(prefs.getString(PREF_KEY_POLLING_PERIOD, null));
                break;
            case PREF_KEY_SUBSCRIPTION_PERIOD:
                reg.setSubscriptionExpiration(prefs.getString(PREF_KEY_SUBSCRIPTION_PERIOD, null));
                break;
            case PREF_KEY_CONTACT_LIST_TYPE:
                updateContactListViews();

                ListPreference lp = findPreference(PREF_KEY_CONTACT_LIST_TYPE);
                int cListTypeIdx = lp.findIndexOfValue(lp.getValue());
                getSipWizard().getRegistration().setXCapEnable(cListTypeIdx == 1);
                getSipWizard().getRegistration().setXiVOEnable(cListTypeIdx == 2);
                break;
            case PREF_KEY_CLIST_SERVER_URI:
                reg.setClistOptionServerUri(prefs.getString(PREF_KEY_CLIST_SERVER_URI, null));
                break;
            case PREF_KEY_CLIST_USE_SIP_CREDENTIALS:
                reg.setClistOptionUseSipCredentials(prefs.getBoolean(PREF_KEY_CLIST_USE_SIP_CREDENTIALS, true));
                break;
            case PREF_KEY_CLIST_USER:
                reg.setClistOptionUser(prefs.getString(PREF_KEY_CLIST_USER, null));
                break;
            case PREF_KEY_CLIST_PASSWORD:
                reg.setClistOptionPassword(prefs.getString(PREF_KEY_CLIST_PASSWORD, null));
                break;
        }
    }

    /**
     * Update widgets responsible for contact list preferences
     */
    private void updateContactListViews()
    {
        ListPreference clistTypePref = findPreference(PREF_KEY_CONTACT_LIST_TYPE);

        boolean enable = clistTypePref.findIndexOfValue(clistTypePref.getValue()) != 0;

        findPreference(PREF_KEY_CLIST_SERVER_URI).setEnabled(enable);
        findPreference(PREF_KEY_CLIST_USE_SIP_CREDENTIALS).setEnabled(enable);
        findPreference(PREF_KEY_CLIST_USER).setEnabled(enable);
        findPreference(PREF_KEY_CLIST_PASSWORD).setEnabled(enable);
    }

    /**
     * {@inheritDoc}
     */
    protected void doCommitChanges()
    {
        try {
            SIPAccountRegistration sipAccReg = getAccountRegistration();
            AccountRegistrationImpl sipWizard = getSipWizard();

            sipWizard.setModification(true);
            sipWizard.signin(sipAccReg.getId(), sipAccReg.getPassword(), sipAccReg.getAccountProperties());
        } catch (OperationFailedException e) {
            Timber.e(e, "Failed to store account modifications: %s", e.getLocalizedMessage());
        }
    }
}
