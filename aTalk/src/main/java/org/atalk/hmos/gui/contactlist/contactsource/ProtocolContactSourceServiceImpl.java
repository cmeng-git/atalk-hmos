/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.atalk.hmos.gui.contactlist.contactsource;

import net.java.sip.communicator.service.contactlist.MetaContact;
import net.java.sip.communicator.service.contactlist.MetaContactListService;
import net.java.sip.communicator.service.contactsource.AsyncContactQuery;
import net.java.sip.communicator.service.contactsource.ContactDetail;
import net.java.sip.communicator.service.contactsource.ContactQuery;
import net.java.sip.communicator.service.contactsource.ContactSourceService;
import net.java.sip.communicator.service.contactsource.SortedGenericSourceContact;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationSet;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;

import org.apache.commons.lang3.StringUtils;
import org.atalk.hmos.gui.AndroidGUIActivator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The <code>ProtocolContactSourceServiceImpl</code>
 *
 * @author Yana Stamcheva
 * @author Eng Chong Meng
 */
public class ProtocolContactSourceServiceImpl implements ContactSourceService
{
    /**
     * The protocol provider, providing the contacts.
     */
    private final ProtocolProviderService protocolProvider;

    /**
     * The operation set class, we use to filter the capabilities of the contacts.
     */
    private final Class<? extends OperationSet> opSetClass;

    /**
     * The <code>MetaContactListService</code>, providing the meta contact list.
     */
    MetaContactListService metaContactListService = AndroidGUIActivator.getContactListService();

    /**
     * The <code>List</code> of <code>ProtocolContactQuery</code> instances which have been started and haven't stopped yet.
     */
    private final List<ProtocolCQuery> queries = new LinkedList<>();

    /**
     * Creates an instance of <code>ProtocolContactSourceServiceImpl</code>.
     *
     * @param protocolProvider the protocol provider which is the contact source
     * @param opSetClass the <code>OperationSet</code> class that is supported by source contacts
     */
    public ProtocolContactSourceServiceImpl(ProtocolProviderService protocolProvider,
            Class<? extends OperationSet> opSetClass)
    {
        this.protocolProvider = protocolProvider;
        this.opSetClass = opSetClass;
    }

    /**
     * Returns the type of this contact source.
     *
     * @return the type of this contact source
     */
    public int getType()
    {
        return DEFAULT_TYPE;
    }

    /**
     * Returns a user-friendly string that identifies this contact source.
     *
     * @return the display name of this contact source
     */
    public String getDisplayName()
    {
        return ContactGroup.ROOT_GROUP_NAME + ":" + protocolProvider.getAccountID().getDisplayName();
    }

    /**
     * Creates query for the given <code>searchPattern</code>.
     *
     * @param queryString the string to search for
     * @return the created query
     */
    public ContactQuery createContactQuery(String queryString)
    {
        return createContactQuery(queryString, -1);
    }

    /**
     * Creates query for the given <code>searchPattern</code>.
     *
     * @param queryString the string to search for
     * @param contactCount the maximum count of result contacts
     * @return the created query
     */
    public ContactQuery createContactQuery(String queryString, int contactCount)
    {
        if (queryString == null)
            queryString = "";

        ProtocolCQuery contactQuery = new ProtocolCQuery(queryString, contactCount);
        synchronized (queries) {
            queries.add(contactQuery);
        }

        return contactQuery;
    }

    /**
     * Removes query from the list.
     *
     * @param contactQuery the query
     */
    public synchronized void removeQuery(ContactQuery contactQuery)
    {
        if (queries.remove(contactQuery))
            queries.notify();
    }

    /**
     * The <code>ProtocolCQuery</code> performing the query for this contact source.
     */
    private class ProtocolCQuery extends AsyncContactQuery<ProtocolContactSourceServiceImpl>
    {
        /**
         * The maximum number of contacts to return as result.
         */
        private int contactCount;

        /**
         * The query string used for filtering the results.
         */
        private final String queryString;

        /**
         * Creates an instance of <code>ProtocolCQuery</code>.
         *
         * @param queryString the query string
         * @param contactCount the maximum number of contacts to return as result
         */
        public ProtocolCQuery(String queryString, int contactCount)
        {
            super(ProtocolContactSourceServiceImpl.this,
                    Pattern.compile(queryString, Pattern.CASE_INSENSITIVE | Pattern.LITERAL), true);

            this.queryString = queryString;
            this.contactCount = contactCount;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Always returns <code>false</code>.
         */
        @Override
        protected boolean phoneNumberMatches(String phoneNumber)
        {
            return false;
        }

        @Override
        public void run()
        {
            Iterator<MetaContact> contactListIter = metaContactListService.findAllMetaContactsForProvider(protocolProvider);

            while (contactListIter.hasNext()) {
                MetaContact metaContact = contactListIter.next();

                if (getStatus() == QUERY_CANCELED)
                    return;

                this.addResultContact(metaContact);
            }

            if (getStatus() != QUERY_CANCELED)
                setStatus(QUERY_COMPLETED);
        }

        @Override
        public synchronized void start()
        {
            boolean queryHasStarted = false;

            try {
                super.start();
                queryHasStarted = true;
            } finally {
                if (!queryHasStarted) {
                    getContactSource().removeQuery(this);
                }
            }

        }

        /**
         * Adds the result for the given group.
         *
         * @param metaContact the metaContact, which child protocol contacts we'll be adding to the result
         */
        private void addResultContact(MetaContact metaContact)
        {
            Iterator<Contact> contacts = metaContact.getContactsForProvider(protocolProvider);

            while (contacts.hasNext()) {
                if (getStatus() == QUERY_CANCELED)
                    return;

                if (contactCount > 0 && getQueryResultCount() > contactCount)
                    break;

                Contact contact = contacts.next();
                String contactAddress = contact.getAddress();
                String contactDisplayName = contact.getDisplayName();
                String queryLowerCase = queryString.toLowerCase();

                if (StringUtils.isEmpty(queryString)
                        || metaContact.getDisplayName().toLowerCase().contains(queryLowerCase)
                        || contactAddress.toLowerCase().contains(queryLowerCase)
                        || contactDisplayName.toLowerCase().contains(queryLowerCase)) {
                    ContactDetail contactDetail = new ContactDetail(contactAddress);
                    List<Class<? extends OperationSet>> supportedOpSets = new ArrayList<Class<?
                            extends OperationSet>>();

                    supportedOpSets.add(opSetClass);
                    contactDetail.setSupportedOpSets(supportedOpSets);

                    List<ContactDetail> contactDetails = new ArrayList<ContactDetail>();
                    contactDetails.add(contactDetail);
                    SortedGenericSourceContact sourceContact = new SortedGenericSourceContact(this,
                            ProtocolContactSourceServiceImpl.this, metaContact.getDisplayName(),
                            contactDetails);

                    if (!contactAddress.equals(contactDisplayName))
                        sourceContact.setDisplayDetails(contactAddress);

                    sourceContact.setImage(metaContact.getAvatar());
                    sourceContact.setPresenceStatus(contact.getPresenceStatus());
                    sourceContact.setContactAddress(contactAddress);

                    addQueryResult(sourceContact);
                }
            }
        }
    }

    /**
     * Returns the index of the contact source in the result list.
     *
     * @return the index of the contact source in the result list
     */
    public int getIndex()
    {
        return 1;
    }
}
