/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.push;

import java.util.List;

import org.eclipse.hawkbit.repository.event.remote.entity.TargetTagUpdateEvent;

/**
 * EventHolder for {@link TargetTagUpdateEvent}s.
 *
 */
public class TargetTagUpdatedEventContainer implements EventContainer<TargetTagUpdateEvent> {
    private static final String I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE = "target.tag.updated.event.container.notifcation.message";
    private final List<TargetTagUpdateEvent> events;

    TargetTagUpdatedEventContainer(final List<TargetTagUpdateEvent> events) {
        this.events = events;
    }

    @Override
    public List<TargetTagUpdateEvent> getEvents() {
        return events;
    }

    @Override
    public String getUnreadNotificationMessageKey() {
        return I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE;
    }

}
