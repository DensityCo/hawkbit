/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.SMFilterEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.event.UploadArtifactUIEvent;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.table.AbstractSoftwareModuleTableHeader;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Header of Software module table.
 */
public class SoftwareModuleTableHeader extends AbstractSoftwareModuleTableHeader {

    private static final long serialVersionUID = 1L;

    SoftwareModuleTableHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventbus, final ArtifactUploadState artifactUploadState,
            final SoftwareModuleAddUpdateWindow softwareModuleAddUpdateWindow) {
        super(i18n, permChecker, eventbus, null, null, artifactUploadState, softwareModuleAddUpdateWindow);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final UploadArtifactUIEvent event) {
        if (event == UploadArtifactUIEvent.HIDE_FILTER_BY_TYPE) {
            setFilterButtonsIconVisible(true);
        }
    }

    @Override
    protected String onLoadSearchBoxValue() {
        return artifactUploadState.getSoftwareModuleFilters().getSearchText().orElse(null);
    }

    @Override
    protected void showFilterButtonsLayout() {
        artifactUploadState.setSwTypeFilterClosed(false);
        eventbus.publish(this, UploadArtifactUIEvent.SHOW_FILTER_BY_TYPE);

    }

    @Override
    protected void resetSearchText() {
        if (artifactUploadState.getSoftwareModuleFilters().getSearchText().isPresent()) {
            artifactUploadState.getSoftwareModuleFilters().setSearchText(null);
            eventbus.publish(this, SMFilterEvent.REMOVER_FILTER_BY_TEXT);
        }
    }

    @Override
    public void maximizeTable() {
        artifactUploadState.setSwModuleTableMaximized(Boolean.TRUE);
        eventbus.publish(this, new SoftwareModuleEvent(BaseEntityEventType.MAXIMIZED));

    }

    @Override
    public void minimizeTable() {
        artifactUploadState.setSwModuleTableMaximized(Boolean.FALSE);
        eventbus.publish(this, new SoftwareModuleEvent(BaseEntityEventType.MINIMIZED));
    }

    @Override
    public Boolean onLoadIsTableMaximized() {
        return artifactUploadState.isSwModuleTableMaximized();
    }

    @Override
    public Boolean onLoadIsShowFilterButtonDisplayed() {
        return artifactUploadState.isSwTypeFilterClosed();
    }

    @Override
    protected void searchBy(final String newSearchText) {
        artifactUploadState.getSoftwareModuleFilters().setSearchText(newSearchText);
        eventbus.publish(this, SMFilterEvent.FILTER_BY_TEXT);
    }

    @Override
    protected boolean isDropHintRequired() {
        /* No dropping on software module table header in Upload View */
        return false;
    }

}
