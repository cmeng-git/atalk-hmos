/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package org.atalk.crypto.otr;

import net.java.sip.communicator.plugin.otr.OtrActionHandler;
import net.java.sip.communicator.plugin.otr.ScOtrEngineImpl;
import net.java.sip.communicator.plugin.otr.ScSessionID;

import org.atalk.hmos.aTalkApp;

import java.util.UUID;

/**
 * Android <code>OtrActionHandler</code> implementation.
 *
 * @author Pawel Domas
 */
public class AndroidOtrActionHandler implements OtrActionHandler
{
	/**
	 * {@inheritDoc}
	 */
	public void onAuthenticateLinkClicked(UUID uuid)
	{
		ScSessionID scSessionID = ScOtrEngineImpl.getScSessionForGuid(uuid);
		if (scSessionID != null) {
			aTalkApp.getGlobalContext().startActivity(OtrAuthenticateDialog.createIntent(uuid));
		}
		else {
			System.err.println("Session for gui: " + uuid + " no longer exists");
		}
	}
}
