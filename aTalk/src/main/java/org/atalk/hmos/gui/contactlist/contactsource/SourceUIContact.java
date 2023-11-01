/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atalk.hmos.gui.contactlist.contactsource;

import android.graphics.drawable.Drawable;

import net.java.sip.communicator.plugin.desktoputil.SIPCommButton;
import net.java.sip.communicator.service.contactsource.ContactDetail;
import net.java.sip.communicator.service.contactsource.ContactSourceService;
import net.java.sip.communicator.service.contactsource.PrefixedContactSourceService;
import net.java.sip.communicator.service.contactsource.SourceContact;
import net.java.sip.communicator.service.gui.UIContactDetail;
import net.java.sip.communicator.service.gui.UIGroup;
import net.java.sip.communicator.service.protocol.OperationNotSupportedException;
import net.java.sip.communicator.service.protocol.OperationSet;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.PhoneNumberI18nService;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.globalstatus.GlobalStatusEnum;
import net.java.sip.communicator.util.ConfigurationUtils;

import org.apache.commons.lang3.StringUtils;
import org.atalk.hmos.R;
import org.atalk.hmos.aTalkApp;
import org.atalk.hmos.gui.AndroidGUIActivator;
import org.atalk.hmos.gui.contactlist.ContactNode;
import org.atalk.hmos.gui.contactlist.UIContactDetailImpl;
import org.atalk.hmos.gui.contactlist.UIContactImpl;
import org.atalk.hmos.gui.util.AndroidImageUtil;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * The <code>SourceUIContact</code> is the implementation of the UIContact for the
 * <code>ExternalContactSource</code>.
 *
 * @author Yana Stamcheva
 * @author Hristo Terezov
 * @author Eng Chong Meng
 */
public class SourceUIContact extends UIContactImpl
{
    /**
     * The corresponding <code>SourceContact</code>, on which this abstraction is based.
     */
    private final SourceContact sourceContact;

    /**
     * The corresponding <code>ContactNode</code> in the contact list component.
     */
    private ContactNode contactNode;

    /**
     * The parent <code>UIGroup</code>.
     */
    // private ExternalContactSource.SourceUIGroup uiGroup;

    /**
     * The search strings for this <code>UIContact</code>.
     */
    private final List<String> searchStrings = new LinkedList<>();

    /**
     * Whether we should filter all call details only to numbers.
     */
    private static final String FILTER_CALL_DETAILS_TO_NUMBERS_PROP
            = "gui.contactlist.contactsource.FILTER_CALL_DETAILS_TO_NUMBERS";

    /**
     * Creates an instance of <code>SourceUIContact</code> by specifying the <code>SourceContact</code>,
     * on which this abstraction is based and the parent <code>UIGroup</code>.
     *
     * @param contact the <code>SourceContact</code>, on which this abstraction is based
     * // @param parentGroup the parent <code>UIGroup</code>
     */
    public SourceUIContact(SourceContact contact)
    // , ExternalContactSource.SourceUIGroup parentGroup)
    {
        this.sourceContact = contact;
        // this.uiGroup = parentGroup;

        if (contact.getContactDetails() != null)
            for (ContactDetail detail : contact.getContactDetails()) {
                if (detail.getDetail() != null)
                    searchStrings.add(detail.getDetail());
            }

        searchStrings.add(contact.getDisplayName());

    }

    /**
     * Returns the display name of the underlying <code>SourceContact</code>.
     *
     * @return the display name
     */
    @Override
    public String getDisplayName()
    {
        return sourceContact.getDisplayName();
    }

    /**
     * Returns the parent <code>UIGroup</code>.
     *
     * @return the parent <code>UIGroup</code>
     */
    @Override
    public UIGroup getParentGroup()
    {
        return null; // uiGroup;
    }

    /**
     * The parent group of source contacts could not be changed.
     *
     * @param parentGroup the parent group to set
     */
    @Override
    public void setParentGroup(UIGroup parentGroup)
    {
    }

    /**
     * Returns -1 to indicate that the source index of the underlying <code>SourceContact</code> is
     * unknown.
     *
     * @return -1
     */
    @Override
    public int getSourceIndex()
    {
        int contactIndex = sourceContact.getIndex();
        int groupIndex = getParentGroup().getSourceIndex();
        return ((contactIndex == -1) ? -1 : ((groupIndex == -1)
                ? contactIndex : groupIndex + contactIndex));
    }

    /**
     * Returns null to indicate unknown status of the underlying <code>SourceContact</code>.
     *
     * @return null
     */
    @Override
    public byte[] getStatusIcon()
    {
        PresenceStatus status = sourceContact.getPresenceStatus();
        if (status != null)
            return status.getStatusIcon();

        return GlobalStatusEnum.OFFLINE.getStatusIcon();
    }

    /**
     * Gets the avatar of a specific <code>UIContact</code> in the form of an <code>ImageIcon</code> value.
     *
     * @return a byte array representing the avatar of this <code>UIContact</code>
     */
    @Override
    public byte[] getAvatar()
    {
        return sourceContact.getImage();
    }

    /**
     * Returns the image corresponding to the underlying <code>SourceContact</code>.
     *
     * @param isSelected indicates if the contact is currently selected in the contact list component
     * @param width the desired image width
     * @param height the desired image height
     * @return the image
     */
    @Override
    public Drawable getScaledAvatar(boolean isSelected, int width, int height)
    {
        byte[] image = sourceContact.getImage();
        return AndroidImageUtil.getScaledRoundedIcon(image, width, height);
    }

    /**
     * Returns the default <code>ContactDetail</code> to use for any operations depending to the given
     * <code>OperationSet</code> class.
     *
     * @param opSetClass the <code>OperationSet</code> class we're interested in
     * @return the default <code>ContactDetail</code> to use for any operations depending to the given
     * <code>OperationSet</code> class
     */
    @Override
    public UIContactDetail getDefaultContactDetail(Class<? extends OperationSet> opSetClass)
    {
        List<UIContactDetail> details = getContactDetailsForOperationSet(opSetClass);
        if (details != null && !details.isEmpty())
            return details.get(0);
        return null;
    }

    /**
     * Returns the underlying <code>SourceContact</code> this abstraction is about.
     *
     * @return the underlying <code>SourceContact</code>
     */
    @Override
    public Object getDescriptor()
    {
        return sourceContact;
    }

    /**
     * Returns the display details for the underlying <code>SourceContact</code>.
     *
     * @return the display details for the underlying <code>SourceContact</code>
     */
    @Override
    public String getDisplayDetails()
    {
        return sourceContact.getDisplayDetails();
    }

    /**
     * Returns a list of all contained <code>UIContactDetail</code>s.
     *
     * @return a list of all contained <code>UIContactDetail</code>s
     */
    @Override
    public List<UIContactDetail> getContactDetails()
    {
        List<UIContactDetail> resultList = new LinkedList<>();

        for (ContactDetail detail : sourceContact.getContactDetails()) {
            resultList.add(new SourceContactDetail(detail, getInternationalizedLabel(detail.getCategory()),
                    getInternationalizedLabels(detail.getSubCategories()), null, sourceContact));
        }
        return resultList;
    }

    /**
     * Returns a list of <code>UIContactDetail</code>s supporting the given <code>OperationSet</code>
     * class.
     *
     * @param opSetClass the <code>OperationSet</code> class we're interested in
     * @return a list of <code>UIContactDetail</code>s supporting the given <code>OperationSet</code> class
     */
    @Override
    public List<UIContactDetail> getContactDetailsForOperationSet(Class<? extends OperationSet> opSetClass)
    {
        List<UIContactDetail> resultList = new LinkedList<>();
        Iterator<ContactDetail> details = sourceContact.getContactDetails().iterator();

        PhoneNumberI18nService phoneNumberService = AndroidGUIActivator.getPhoneNumberI18nService();
        boolean filterToNumbers = AndroidGUIActivator.getConfigurationService()
                .getBoolean(FILTER_CALL_DETAILS_TO_NUMBERS_PROP, false);

        while (details.hasNext()) {
            ContactDetail detail = details.next();

            List<Class<? extends OperationSet>> supportedOperationSets = detail.getSupportedOperationSets();

            if ((supportedOperationSets != null) && supportedOperationSets.contains(opSetClass)) {
                if (filterToNumbers && opSetClass.equals(OperationSetBasicTelephony.class)
                        && !phoneNumberService.isPhoneNumber(detail.getDetail())) {
                    continue;
                }

                resultList.add(new SourceContactDetail(detail, getInternationalizedLabel(detail.getCategory()),
                        getInternationalizedLabels(detail.getSubCategories()), opSetClass, sourceContact));
            }
        }
        return resultList;
    }

    /**
     * Returns an <code>Iterator</code> over a list of strings, which can be used to find this contact.
     *
     * @return an <code>Iterator</code> over a list of search strings
     */
    @Override
    public Iterator<String> getSearchStrings()
    {
        return searchStrings.iterator();
    }

    /**
     * Returns the corresponding <code>ContactNode</code> from the contact list component.
     *
     * @return the corresponding <code>ContactNode</code>
     */
    @Override
    public ContactNode getContactNode()
    {
        return contactNode;
    }

    /**
     * Sets the corresponding <code>ContactNode</code>.
     *
     * @param contactNode the corresponding <code>ContactNode</code>
     */
    @Override
    public void setContactNode(ContactNode contactNode)
    {
        this.contactNode = contactNode;
        if (contactNode == null) {
            // uiGroup.getParentUISource().removeUIContact(sourceContact);
        }
    }

    /**
     * The implementation of the <code>UIContactDetail</code> interface for the external source
     * <code>ContactDetail</code>s.
     */
    protected static class SourceContactDetail extends UIContactDetailImpl
    {
        /**
         * Creates an instance of <code>SourceContactDetail</code> by specifying the underlying
         * <code>detail</code> and the <code>OperationSet</code> class for it.
         *
         * @param detail the underlying <code>ContactDetail</code>
         * @param category detail category string
         * @param subCategories the detail list of sub-categories
         * @param opSetClass the <code>OperationSet</code> class for the preferred protocol provider
         * @param sourceContact the source contact
         */
        public SourceContactDetail(ContactDetail detail, String category, Collection<String> subCategories,
                Class<? extends OperationSet> opSetClass, SourceContact sourceContact)
        {
            super(detail.getDetail(), detail.getDetail(), category, subCategories,
                    null, null, null, detail);

            ContactSourceService contactSource = sourceContact.getContactSource();
            if (contactSource instanceof PrefixedContactSourceService) {
                String prefix = ((PrefixedContactSourceService) contactSource).getPhoneNumberPrefix();

                if (prefix != null)
                    setPrefix(prefix);
            }
            addPreferredProtocolProvider(opSetClass, detail.getPreferredProtocolProvider(opSetClass));
            addPreferredProtocol(opSetClass, detail.getPreferredProtocol(opSetClass));
        }

        /**
         * Creates an instance of <code>SourceContactDetail</code> by specifying the underlying
         * <code>detail</code> and the <code>OperationSet</code> class for it.
         *
         * @param displayName the display name
         * @param sourceContact the source contact
         */
        public SourceContactDetail(String displayName, SourceContact sourceContact)
        {
            super(displayName, displayName, null, null, null, null, null, sourceContact);
        }

        /**
         * Returns null to indicate that this detail doesn't support presence.
         *
         * @return null
         */
        @Override
        public PresenceStatus getPresenceStatus()
        {
            return null;
        }
    }

    /**
     * Returns the <code>JPopupMenu</code> opened on a right button click over this
     * <code>SourceUIContact</code>.
     *
     * @return the <code>JPopupMenu</code> opened on a right button click over this
     * <code>SourceUIContact</code>
     */
    @Override
    public JPopupMenu getRightButtonMenu()
    {
        return null; // new SourceContactRightButtonMenu(this);
    }

    /**
     * Returns the tool tip opened on mouse over.
     *
     * @return the tool tip opened on mouse over
     */

    public class ExtendedTooltip
    {

        public ExtendedTooltip(boolean b)
        {
            // TODO Auto-generated constructor stub
        }

        public void setImage(ImageIcon imageIcon)
        {
            // TODO Auto-generated method stub
        }

        public void addLine(JLabel[] jLabels)
        {
            // TODO Auto-generated method stub
        }

    }

    // @Override
    public ExtendedTooltip getToolTip()
    {
        ExtendedTooltip tip = new ExtendedTooltip(true);

        byte[] avatarImage = sourceContact.getImage();
        if (avatarImage != null && avatarImage.length > 0) {
            // tip.setImage(new ImageIcon(avatarImage));
        }

        // tip.setTitle(sourceContact.getDisplayName());
        String displayDetails = getDisplayDetails();
        if (displayDetails != null)
            tip.addLine(new JLabel[]{new JLabel(getDisplayDetails())});

        try {
            List<ContactDetail> details = sourceContact.getContactDetails(ContactDetail.Category.Phone);
            if (details != null && details.size() > 0)
                addDetailsToToolTip(details, aTalkApp.getResString(R.string.service_gui_PHONES), tip);

            details = sourceContact.getContactDetails(ContactDetail.Category.Email);
            if (details != null && details.size() > 0)
                addDetailsToToolTip(details, aTalkApp.getResString(R.string.service_gui_EMAILS), tip);

            details = sourceContact.getContactDetails(ContactDetail.Category.InstantMessaging);
            if (details != null && details.size() > 0)
                addDetailsToToolTip(details, aTalkApp.getResString(R.string.service_gui_INSTANT_MESSAGINGS), tip);
        } catch (OperationNotSupportedException e) {
            List<ContactDetail> telDetails = sourceContact.getContactDetails(OperationSetBasicTelephony.class);
            // if there is no telephony
            if (telDetails == null || telDetails.isEmpty())
                return tip;

            // Categories aren't supported. This is the case for history records.
            List<ContactDetail> allDetails = sourceContact.getContactDetails();
            addDetailsToToolTip(allDetails, aTalkApp.getResString(R.string.service_gui_CALL_WITH), tip);
        }
        return tip;
    }

    private void addDetailsToToolTip(List<ContactDetail> details, String i18nString, ExtendedTooltip tip)
    {
        // TODO Auto-generated method stub

    }

    private void addDetailsToToolTip(List<ContactDetail> details, String category)
    // , ExtendedTooltip toolTip)
    {
        ContactDetail contactDetail;

        // JLabel categoryLabel = new JLabel(category, null, JLabel.LEFT);
        // categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));
        // categoryLabel.setForeground(Color.DARK_GRAY);

        // toolTip.addLine(null, " ");
        // toolTip.addLine(new JLabel[] { categoryLabel });

        for (ContactDetail detail : details) {
            contactDetail = detail;
            Collection<ContactDetail.SubCategory> subCategories = contactDetail.getSubCategories();

            JLabel[] jLabels = new JLabel[subCategories.size() + 1];
            int i = 0;
            for (ContactDetail.SubCategory subCategory : subCategories) {
                JLabel label = new JLabel(getInternationalizedLabel(subCategory));
                //label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setForeground(Color.GRAY);
                jLabels[i] = label;
                i++;
            }

            String labelText;
            if (ConfigurationUtils.isHideAddressInCallHistoryTooltipEnabled()) {
                labelText = contactDetail.getDisplayName();
                if (StringUtils.isEmpty(labelText))
                    labelText = contactDetail.getDetail();
            }
            else {
                labelText = contactDetail.getDetail();
            }
            jLabels[i] = new JLabel(filterAddressDisplay(labelText));
            // toolTip.addLine(jLabels);
        }
    }

    /**
     * Returns the internationalized category corresponding to the given <code>ContactDetail
     * .Category</code>.
     *
     * @param category the <code>ContactDetail.SubCategory</code>, for which we would like to obtain an
     * internationalized label
     * @return the internationalized label corresponding to the given category
     */
    protected String getInternationalizedLabel(ContactDetail.Category category)
    {
        if (category == null)
            return null;

        String categoryString = null;

        switch (category) {
            case Address:
                categoryString = aTalkApp.getResString(R.string.service_gui_ADDRESS);
                break;
            case Email:
                categoryString = aTalkApp.getResString(R.string.service_gui_EMAIL);
                break;
            case Personal:
                categoryString = aTalkApp.getResString(R.string.service_gui_PERSONAL);
                break;
            case Organization:
                categoryString = aTalkApp.getResString(R.string.service_gui_ORGANIZATION);
                break;
            case Phone:
                categoryString = aTalkApp.getResString(R.string.service_gui_PHONE);
                break;
            case InstantMessaging:
                categoryString = aTalkApp.getResString(R.string.service_gui_IM);
                break;
        }
        return categoryString;
    }

    /**
     * Returns a collection of internationalized string corresponding to the given subCategories.
     *
     * @param subCategories an Iterator over a list of <code>ContactDetail.SubCategory</code>s
     * @return a collection of internationalized string corresponding to the given subCategories
     */
    protected Collection<String> getInternationalizedLabels(Collection<ContactDetail.SubCategory> subCategories)
    {
        Collection<String> labels = new LinkedList<>();

        for (ContactDetail.SubCategory subCategory : subCategories) {
            labels.add(getInternationalizedLabel(subCategory));
        }
        return labels;
    }

    /**
     * Returns the internationalized label corresponding to the given category.
     *
     * @param subCategory the <code>ContactDetail.SubCategory</code>, for which we would like to obtain an
     * internationalized label
     * @return the internationalized label corresponding to the given category
     */
    protected String getInternationalizedLabel(ContactDetail.SubCategory subCategory)
    {
        if (subCategory == null)
            return null;

        String label;
        switch (subCategory) {
            case City:
                label = aTalkApp.getResString(R.string.service_gui_CITY);
                break;
            case Country:
                label = aTalkApp.getResString(R.string.service_gui_COUNTRY);
                break;
            case Fax:
                label = aTalkApp.getResString(R.string.service_gui_FAX);
                break;
            case Home:
                label = aTalkApp.getResString(R.string.service_gui_HOME);
                break;
            case HomePage:
                label = aTalkApp.getResString(R.string.service_gui_HOME_PAGE);
                break;
            case JobTitle:
                label = aTalkApp.getResString(R.string.service_gui_JOB_TITLE);
                break;
            case LastName:
                label = aTalkApp.getResString(R.string.service_gui_LAST_NAME);
                break;
            case Mobile:
                label = aTalkApp.getResString(R.string.service_gui_MOBILE_PHONE);
                break;
            case Name:
                label = aTalkApp.getResString(R.string.service_gui_NAME);
                break;
            case Nickname:
                label = aTalkApp.getResString(R.string.service_gui_NICKNAME);
                break;
            case Other:
                label = aTalkApp.getResString(R.string.service_gui_OTHER);
                break;
            case PostalCode:
                label = aTalkApp.getResString(R.string.service_gui_POSTAL_CODE);
                break;
            case Street:
                label = aTalkApp.getResString(R.string.service_gui_STREET);
                break;
            case Work:
                label = aTalkApp.getResString(R.string.service_gui_WORK_PHONE);
                break;
            case AIM:
            case ICQ:
            case Jabber:
            case Yahoo:
            case Skype:
            case GoogleTalk:
                label = subCategory.value();
                break;
            default:
                label = null;
                break;
        }

        return label;
    }

    /**
     * Returns all custom action buttons for this notification contact.
     *
     * @return a list of all custom action buttons for this notification contact
     */
    @Override
    public Collection<SIPCommButton> getContactCustomActionButtons()
    {
        if (sourceContact != null)
            return null;
        // uiGroup.getParentUISource().getContactCustomActionButtons(sourceContact);

        return null;
    }

    /**
     * Returns all custom action menu items for this contact.
     *
     * @param initActions if <code>true</code> the actions will be reloaded.
     * @return a list of all custom action menu items for this contact.
     */
    @Override
    public Collection<JMenuItem> getContactCustomActionMenuItems(boolean initActions)
    {
        if (sourceContact != null)
            return null;
        // uiGroup.getParentUISource().getContactCustomActionMenuItems(sourceContact, initActions);

        return null;
    }
}
