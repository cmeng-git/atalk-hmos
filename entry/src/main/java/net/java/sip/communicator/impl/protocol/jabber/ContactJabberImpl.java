/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.java.sip.communicator.service.protocol.AbstractContact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.ContactResource;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.event.ContactResourceEvent;
import net.java.sip.communicator.service.protocol.jabberconstants.JabberStatusEnum;
import net.java.sip.communicator.util.ConfigurationUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.blocking.BlockingCommandManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;

import timber.log.Timber;

/**
 * The Jabber implementation of the service.protocol.Contact interface.
 *
 * @author Damian Minkov
 * @author Lubomir Marinov
 * @author Eng Chong Meng
 */
public class ContactJabberImpl extends AbstractContact {
    private static final String PGP_KEY_ID = "pgp_keyid";

    private static final String TTS_ENABLE = "tts_enable";

    private Boolean isContactBlock = false;

    private Boolean isTtsEnable = null;

    /**
     * A reference to the ServerStoredContactListImpl instance that created us.
     */
    private final ServerStoredContactListJabberImpl ssclCallback;

    /**
     * Contains either the bareJid as retrieved from the Roster Entry, FullJid of ownJid OR
     * the VolatileContact BareJid/FullJid
     */
    private Jid contactJid = null;

    /**
     * The image of the contact.
     */
    private byte[] image = null;

    /**
     * The status of the contact as per the last status update we've received for it.
     */
    private PresenceStatus status;

    /**
     * Whether or not this contact is being stored by the server.
     */
    private boolean isPersistent;

    /**
     * Whether or not this contact has been resolved against the server.
     */
    private boolean isResolved;

    /**
     * Used to store contact id when creating unresolved contacts.
     */
    private final Jid tempId;

    /**
     * The current status message of this contact.
     */
    private String statusMessage = null;

    /**
     * The display name of the roster entry.
     */
    private String serverDisplayName = null;

    /**
     * The contact resources list.
     */
    private Map<FullJid, ContactResourceJabberImpl> resources = null;

    /**
     * Whether this contact is a mobile one.
     */
    private boolean mobile = false;

    /**
     * Indicates whether or not this Contact instance represents the user used by this protocol
     * provider to connect to the service.
     */
    private boolean isLocal = false;

    protected JSONObject keys = new JSONObject();
    protected JSONArray groups = new JSONArray();

    protected int subscription = 0;
    private String photoUri;

    /**
     * Creates an JabberContactImpl
     *
     * @param rosterEntry the RosterEntry object that we will be encapsulating.
     * @param ssclCallback a reference to the ServerStoredContactListImpl instance that created us.
     * @param isPersistent determines whether this contact is persistent or not.
     * @param isResolved specifies whether the contact has been resolved against the server contact list
     */
    ContactJabberImpl(RosterEntry rosterEntry, ServerStoredContactListJabberImpl ssclCallback,
            boolean isPersistent, boolean isResolved) {
        // rosterEntry can be null when creating volatile contact
        if (rosterEntry != null) {
            // RosterEntry contains only BareJid
            contactJid = rosterEntry.getJid();
            this.serverDisplayName = rosterEntry.getName();
        }
        this.tempId = null;
        this.ssclCallback = ssclCallback;
        this.isPersistent = isPersistent;
        this.isResolved = isResolved;

        this.status = ((ProtocolProviderServiceJabberImpl)
                getProtocolProvider()).getJabberStatusEnum().getStatus(JabberStatusEnum.OFFLINE);
    }

    /**
     * Used to create unresolved contacts with specified id.
     *
     * @param id contact id
     * @param ssclCallback the contact list handler that creates us.
     * @param isPersistent is the contact persistent.
     */
    ContactJabberImpl(Jid id, ServerStoredContactListJabberImpl ssclCallback, boolean isPersistent) {
        this.tempId = id;
        this.ssclCallback = ssclCallback;
        this.isPersistent = isPersistent;
        this.isResolved = false;

        this.status = ((ProtocolProviderServiceJabberImpl) getProtocolProvider())
                .getJabberStatusEnum().getStatus(JabberStatusEnum.OFFLINE);
    }

    /**
     * Returns the Jabber UserId of this contact
     *
     * @return the Jabber UserId of this contact
     */
    public String getAddress() {
        if (isResolved && (contactJid != null))
            return contactJid.toString();
        else
            return tempId.toString();
    }

    /**
     * Either return the bareJid of contact as retrieved from the Roster Entry Or The VolatileContact Jid.
     * VolatileContact jid may not have been resolved before it is being requested.
     *
     * @return Either return the bareJid of contact as retrieved from the Roster Entry Or The VolatileContact Jid
     */
    public Jid getJid() {
        if (isResolved && (contactJid != null))
            return contactJid;
        else {
            return tempId;
        }
    }

    /**
     * Determines whether or not this Contact instance represents the user used by this protocol
     * provider to connect to the service.
     *
     * @return true if this Contact represents us (the local user) and false otherwise.
     */
    public boolean isLocal() {
        return isLocal;
    }

    /**
     * Returns an avatar if one is already present or <code>null</code> in case it is not in which case
     * it the method also queues the contact for image updates.
     *
     * @return the avatar of this contact or <code>null</code> if no avatar is currently available.
     */
    public byte[] getImage() {
        return getImage(true);
    }

    /**
     * Returns a reference to the image assigned to this contact. If (image == null) and the
     * retrieveIfNecessary flag is true, we schedule the image for retrieval from the server.
     * (image.length == 0) indicates it has been retrieved before, so to avoid avatar retrieval in endless loop
     *
     * @param retrieveIfNecessary specifies whether the method should queue this contact for avatar update from the server.
     *
     * @return a reference to the image currently stored by this contact.
     *
     * @see ServerStoredContactListJabberImpl.ImageRetriever#run()
     */
    public byte[] getImage(boolean retrieveIfNecessary) {
        if ((image == null) && retrieveIfNecessary)
            ssclCallback.addContactForImageUpdate(this, true);
        return image;
    }

    /**
     * Retrieve avatar from server and update the contact avatar image
     * For user manual download by long click on the avatar if true
     *
     * @param retrieveIfNecessary force to retrieve avatar from server if it is null
     */
    public void getAvatar(boolean retrieveIfNecessary) {
        ssclCallback.addContactForImageUpdate(this, retrieveIfNecessary);
    }

    /**
     * Set the image of the contact
     *
     * @param imgBytes the bytes of the image that we'd like to set.
     */
    public void setImage(byte[] imgBytes) {
        this.image = imgBytes;
    }

    /**
     * Returns a hashCode for this contact. The returned hashcode is actually that of the Contact's Address
     *
     * @return the hashcode of this Contact
     */
    @Override
    public int hashCode() {
        return getAddress().toLowerCase(Locale.US).hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     *
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof String || (obj instanceof ContactJabberImpl)))
            return false;

        if ((obj instanceof ContactJabberImpl)
                && ((ContactJabberImpl) obj).getAddress().equalsIgnoreCase(getAddress())
                && ((ContactJabberImpl) obj).getProtocolProvider() == getProtocolProvider()) {
            return true;
        }

        if (obj instanceof String) {
            int atIndex = getAddress().indexOf("@");
            if (atIndex > 0) {
                return getAddress().equalsIgnoreCase((String) obj)
                        || getAddress().substring(0, atIndex).equalsIgnoreCase((String) obj);
            }
            else
                return getAddress().equalsIgnoreCase((String) obj);
        }
        return false;
    }

    /**
     * Returns a string representation of this contact, containing most of its representative details.
     *
     * @return a string representation of this contact.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder()
                .append("JabberContact[id=").append(getAddress())
                .append(", isPersistent=").append(isPersistent)
                .append(", isResolved=").append(isResolved).append("]");
        return buff.toString();
    }

    /**
     * Sets the status that this contact is currently in. The method is to only be called as a
     * result of a status update received from the server.
     *
     * @param status the JabberStatusEnum that this contact is currently in.
     */
    public void updatePresenceStatus(PresenceStatus status) {
        this.status = status;
    }

    /**
     * Returns the status of the contact as per the last status update we've received for it. Note
     * that this method is not to perform any network operations and will simply return the status
     * received in the last status update message. If you want a reliable way of retrieving
     * someone's status, you should use the <code>queryContactStatus()</code> method in <code>OperationSetPresence</code>.
     *
     * @return the PresenceStatus that we've received in the last status update pertaining to this contact.
     */
    public PresenceStatus getPresenceStatus() {
        return status;
    }

    /**
     * Returns a String that could be used by any user interacting modules for referring to this contact.
     * An alias is not necessarily unique but is often more human readable than an address (or id).
     *
     * @return a String that can be used for referring to this contact when interacting with the user.
     */
    public String getDisplayName() {
        if (isResolved) {
            RosterEntry entry = ssclCallback.getRosterEntry(contactJid.asBareJid());
            String name = null;

            if (entry != null)
                name = entry.getName();

            if (StringUtils.isNotEmpty(name))
                return name;
        }
        return getAddress();
    }

    /**
     * Returns the display name used when the contact was resolved. Used to detect renames.
     *
     * @return the display name.
     */
    String getServerDisplayName() {
        return serverDisplayName;
    }

    /**
     * Changes locally stored server display name.
     *
     * @param newValue new display name
     */
    void setServerDisplayName(String newValue) {
        this.serverDisplayName = newValue;
    }

    /**
     * Returns a reference to the contact group that this contact is currently a child of or
     * null if the underlying protocol does not support persistent presence.
     *
     * @return a reference to the contact group that this contact is currently a child of or
     * null if the underlying protocol does not support persistent presence.
     */
    public ContactGroup getParentContactGroup() {
        return ssclCallback.findContactGroup(this);
    }

    /**
     * Returns a reference to the protocol provider that created the contact.
     *
     * @return a reference to an instance of the ProtocolProviderService
     */
    public ProtocolProviderService getProtocolProvider() {
        return ssclCallback.getParentProvider();
    }

    /**
     * Determines whether or not this contact is being stored by the server. Non persistent contacts
     * are common in the case of simple, non-persistent presence operation sets. They could however
     * also be seen in persistent presence operation sets when for example we have received an event
     * from someone not on our contact list. Non persistent contacts are volatile even when coming
     * from a persistent presence op. set. They would only exist until the application is closed
     * and will not be there next time it is loaded.
     *
     * @return true if the contact is persistent and false otherwise.
     */
    public boolean isPersistent() {
        return isPersistent;
    }

    /**
     * Specifies whether this contact is to be considered persistent or not. The method is to be
     * used _only_ when a non-persistent contact has been added to the contact list and its
     * encapsulated VolatileBuddy has been replaced with a standard buddy.
     *
     * @param persistent true if the buddy is to be considered persistent and false for volatile.
     */
    void setPersistent(boolean persistent) {
        this.isPersistent = persistent;
    }

    /**
     * Returns the persistent data
     *
     * @return the persistent data
     */
    public String getPersistentData() {
        return null;
    }

    /**
     * Determines whether or not this contact has been resolved against the server. Unresolved
     * contacts are used when initially loading a contact list that has been stored in a local file
     * until the presence operation set has managed to retrieve all the contact list from the
     * server and has properly mapped contacts to their on-line buddies.
     *
     * @return true if the contact has been resolved (mapped against a buddy) and false otherwise.
     */
    public boolean isResolved() {
        return isResolved;
    }

    /**
     * Resolve this contact against the given entry
     *
     * @param entry the server stored entry
     */
    void setResolved(RosterEntry entry) {
        if (isResolved)
            return;

        this.isResolved = true;
        this.isPersistent = true;
        contactJid = entry.getJid();
        this.serverDisplayName = entry.getName();
    }

    /**
     * Get source entry
     *
     * @return RosterEntry
     */
    RosterEntry getSourceEntry() {
        return (contactJid == null) ? null : ssclCallback.getRosterEntry(contactJid.asBareJid());
    }

    /**
     * Return the current status message of this contact.
     *
     * @return the current status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Sets the current status message for this contact
     *
     * @param statusMessage the message
     */
    protected void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * Indicates if this contact supports resources.
     *
     * @return <code>false</code> to indicate that this contact doesn't support resources
     */
    @Override
    public boolean supportResources() {
        return true;
    }

    /**
     * Returns an iterator over the resources supported by this contact or null if it doesn't support resources.
     *
     * @return null, as this contact doesn't support resources
     */
    @Override
    public Collection<ContactResource> getResources() {
        if (resources != null) {
            Timber.d("Contact: %s resources %s", getDisplayName(), resources.size());
            return new ArrayList<>(resources.values());
        }
        return null;
    }

    /**
     * Finds the <code>ContactResource</code> corresponding to the given bareJid.
     *
     * @param jid the fullJid for which we're looking for a resource
     *
     * @return the <code>ContactResource</code> corresponding to the given bareJid.
     */
    ContactResource getResourceFromJid(FullJid jid) {
        return ((resources == null) || (jid == null)) ? null : resources.get(jid);
    }

    Map<FullJid, ContactResourceJabberImpl> getResourcesMap() {
        if (resources == null) {
            resources = new ConcurrentHashMap<>();
        }
        return this.resources;
    }

    /**
     * Notifies all registered <code>ContactResourceListener</code>s that an event has occurred.
     *
     * @param event the <code>ContactResourceEvent</code> to fire notification for
     */
    public void fireContactResourceEvent(ContactResourceEvent event) {
        super.fireContactResourceEvent(event);
    }

    /**
     * Used from volatile contacts to handle jid and resources. Volatile contacts are always
     * unavailable so do not remove their resources from the contact as it will be the only
     * resource we will use. Note: volatile contact may not necessary contain a resource part.
     *
     * @param fullJid the fullJid of the volatile contact.
     */
    protected void setJid(Jid fullJid) {
        contactJid = fullJid;
        if (resources == null)
            resources = new ConcurrentHashMap<>();
    }

    /**
     * Whether contact is mobile one. Logged in from mobile device.
     *
     * @return whether contact is mobile one.
     */
    public boolean isMobile() {
        return getPresenceStatus().isOnline() && mobile;
    }

    /**
     * Changes the mobile indicator value.
     *
     * @param mobile is mobile
     */
    void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    /**
     * Changes the isLocal indicator.
     *
     * @param isLocal the new value.
     */
    void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public boolean setPhotoUri(String uri) {
        if (uri != null && !uri.equals(this.photoUri)) {
            this.photoUri = uri;
            return true;
        }
        else if (this.photoUri != null && uri == null) {
            this.photoUri = null;
            return true;
        }
        else {
            return false;
        }
    }

    public long getPgpKeyId() {
        synchronized (this.keys) {
            if (this.keys.has(PGP_KEY_ID)) {
                try {
                    return this.keys.getLong(PGP_KEY_ID);
                } catch (JSONException e) {
                    return 0;
                }
            }
            else {
                return 0;
            }
        }
    }

    public void setPgpKeyId(long keyId) {
        synchronized (this.keys) {
            try {
                this.keys.put(PGP_KEY_ID, keyId);
            } catch (final JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Check this contact blocking status
     *
     * @return true if contact is blocked.
     */
    public boolean isContactBlock() {
        return isContactBlock;
    }

    /**
     * Set contact blocking status.
     *
     * @param value contact block state.
     *
     * @see OperationSetPersistentPresenceJabberImpl#initContactBlockStatus(BlockingCommandManager)
     */
    public void setContactBlock(boolean value) {
        isContactBlock = value;
    }

    /**
     * When access on start-up, return ttsEnable may be null.
     *
     * @return true if contact tts is enabled.
     */
    public boolean isTtsEnable() {
        if (isTtsEnable == null) {
            String val = ConfigurationUtils.getContactProperty(getJid(), TTS_ENABLE);
            // Null value in DB is considered as false
            isTtsEnable = !TextUtils.isEmpty(val) && Boolean.parseBoolean(val);
        }
        return isTtsEnable;
    }

    /**
     * Change contact tts enable value in configuration service.
     * Use getJid() to ensure unresolved contact (contactJid == null) returns a valid values.
     *
     * @param value change of tts enable property.
     */
    public void setTtsEnable(boolean value) {
        if (isTtsEnable != value) {
            isTtsEnable = value;
            ConfigurationUtils.updateContactProperty(getJid(), TTS_ENABLE, Boolean.toString(isTtsEnable));
        }
    }

    /**
     * Unused method, need to clean up if required
     *
     * @param option
     */
    public void setOption(int option) {
        this.subscription |= 1 << option;
    }

    public void resetOption(int option) {
        this.subscription &= ~(1 << option);
    }

    public boolean getOption(int option) {
        return ((this.subscription & (1 << option)) != 0);
    }

    public final class Options {
        public static final int TO = 0;
        public static final int FROM = 1;
        public static final int ASKING = 2;
        public static final int PREEMPTIVE_GRANT = 3;
        public static final int IN_ROSTER = 4;
        public static final int PENDING_SUBSCRIPTION_REQUEST = 5;
        public static final int DIRTY_PUSH = 6;
        public static final int DIRTY_DELETE = 7;
    }
}