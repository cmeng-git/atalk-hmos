/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package org.atalk.ohos.gui.util.event;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.event.DocumentListener;

/**
 * Utility class to that stores the list of {@link EventListener}s. Provides add/remove
 * and notify all operations.
 *
 * @param <T> the event object class
 * @author Pawel Domas
 */
public class EventListenerList<T>
{
    /**
     * The list of {@link EventListener}
     */
    private ArrayList<EventListener<T>> listeners = new ArrayList<EventListener<T>>();

    /**
     * Adds the <code>listener</code> to the list
     *
     * @param listener the {@link EventListener} that will be added to the list
     */
    public void addEventListener(EventListener<T> listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    /**
     * Removes the <code>listener</code> from the list
     *
     * @param listener the {@link EventListener} that will be removed from the list
     */
    public void removeEventListener(EventListener<T> listener)
    {
        listeners.remove(listener);
    }

    /**
     * Runs the event change notification on listeners list
     *
     * @param eventObject the source object of the event
     */
    public void notifyEventListeners(T eventObject)
    {
        for (EventListener<T> l : listeners) {
            l.onChangeEvent(eventObject);
        }
    }

    /**
     * Clears the listeners list
     */
    public void clear()
    {
        listeners.clear();
    }

    public void add(Class<DocumentListener> class1, DocumentListener paramDocumentListener)
    {
        // TODO Auto-generated method stub

    }

    public void add(Class<ActionListener> class1, ActionListener paramActionListener)
    {
        // TODO Auto-generated method stub
    }
}
