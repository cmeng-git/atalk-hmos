/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package org.atalk.hmos.gui.call;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import org.atalk.hmos.R;
import org.atalk.hmos.aTalkApp;
import org.atalk.hmos.gui.util.AndroidImageUtil;
import org.atalk.hmos.gui.util.ViewUtil;
import org.atalk.service.osgi.OSGiFragment;
import org.jivesoftware.smackx.avatar.AvatarManager;
import org.jxmpp.jid.BareJid;

import timber.log.Timber;

/**
 * Fragment displayed in <code>VideoCallActivity</code> when the call has ended.
 *
 * @author Pawel Domas
 */
public class CallEnded extends OSGiFragment implements View.OnClickListener
{
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.call_ended, container, false);

        // Display callPeer avatar; take care NPE from field
        byte[] avatar = null;
        try {
            BareJid bareJid = VideoCallActivity.callState.callPeer.asBareJid();
            avatar = AvatarManager.getAvatarImageByJid(bareJid);
        } catch (Exception e) {
            Timber.w("Failed to find callPeer Jid");
        }
        if (avatar != null)
            ((ImageView) v.findViewById(R.id.calleeAvatar)).setImageBitmap(AndroidImageUtil.bitmapFromBytes(avatar));

        ViewUtil.setTextViewValue(v, R.id.callTime, VideoCallActivity.callState.callDuration);
        String errorReason = VideoCallActivity.callState.errorReason;
        if (!errorReason.isEmpty()) {
            ViewUtil.setTextViewValue(v, R.id.callErrorReason, errorReason);
        }
        else {
            ViewUtil.ensureVisible(v, R.id.callErrorReason, false);
        }

        v.findViewById(R.id.button_call_hangup).setOnClickListener(this);
        v.findViewById(R.id.button_call_back_to_chat).setOnClickListener(this);
        return v;
    }

    /**
     * Handles buttons action events. the <code>ActionEvent</code> that notified us
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.button_call_hangup:
            case R.id.button_call_back_to_chat:
                FragmentActivity ctx = getActivity();
                ctx.finish();
                ctx.startActivity(aTalkApp.getHomeIntent());
        }
    }
}
