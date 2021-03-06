/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.table.AbstractDistributionSetTableHeader;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.management.event.DistributionTableFilterEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Button.ClickEvent;

/**
 * Distribution table header.
 *
 */
public class DistributionTableHeader extends AbstractDistributionSetTableHeader {

    private static final long serialVersionUID = 1L;

    DistributionTableHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventbus, final ManagementUIState managementUIState) {
        super(i18n, permChecker, eventbus, managementUIState, null, null);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.HIDE_DISTRIBUTION_TAG_LAYOUT) {
            setFilterButtonsIconVisible(true);
        } else if (event == ManagementUIEvent.SHOW_DISTRIBUTION_TAG_LAYOUT) {
            setFilterButtonsIconVisible(false);
        }
    }

    @Override
    protected String onLoadSearchBoxValue() {
        return managementUIState.getDistributionTableFilters().getSearchText().orElse(null);
    }

    @Override
    protected void showFilterButtonsLayout() {
        managementUIState.setDistTagFilterClosed(false);
        eventbus.publish(this, ManagementUIEvent.SHOW_DISTRIBUTION_TAG_LAYOUT);
    }

    @Override
    protected void resetSearchText() {
        if (managementUIState.getDistributionTableFilters().getSearchText().isPresent()) {
            managementUIState.getDistributionTableFilters().setSearchText(null);
            eventbus.publish(this, DistributionTableFilterEvent.REMOVE_FILTER_BY_TEXT);
        }
    }

    @Override
    public void maximizeTable() {
        managementUIState.setDsTableMaximized(Boolean.TRUE);
        eventbus.publish(this, new DistributionTableEvent(BaseEntityEventType.MAXIMIZED));
    }

    @Override
    public void minimizeTable() {
        managementUIState.setDsTableMaximized(Boolean.FALSE);
        eventbus.publish(this, new DistributionTableEvent(BaseEntityEventType.MINIMIZED));
    }

    @Override
    public Boolean onLoadIsTableMaximized() {
        return managementUIState.isDsTableMaximized();
    }

    @Override
    public Boolean onLoadIsShowFilterButtonDisplayed() {
        return managementUIState.isDistTagFilterClosed();
    }

    @Override
    protected void searchBy(final String newSearchText) {
        managementUIState.getDistributionTableFilters().setSearchText(newSearchText);
        eventbus.publish(this, DistributionTableFilterEvent.FILTER_BY_TEXT);
    }

    @Override
    protected void addNewItem(final ClickEvent event) {
        // is okay and not supported
    }

    @Override
    protected Boolean isAddNewItemAllowed() {
        return Boolean.FALSE;
    }

}
