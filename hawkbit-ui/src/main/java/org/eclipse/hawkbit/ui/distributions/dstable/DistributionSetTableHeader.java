/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.table.AbstractDistributionSetTableHeader;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.distributions.event.DistributionsUIEvent;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.dstable.DistributionAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.management.event.DistributionTableFilterEvent;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Distribution table header.
 */
public class DistributionSetTableHeader extends AbstractDistributionSetTableHeader {

    private static final long serialVersionUID = 1L;

    private final DistributionAddUpdateWindowLayout addUpdateWindowLayout;

    DistributionSetTableHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker, final UIEventBus eventbus,
            final ManageDistUIState manageDistUIstate, final DistributionAddUpdateWindowLayout addUpdateWindowLayout) {
        super(i18n, permChecker, eventbus, null, manageDistUIstate, null);
        this.addUpdateWindowLayout = addUpdateWindowLayout;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionsUIEvent event) {
        if (event == DistributionsUIEvent.HIDE_DIST_FILTER_BY_TYPE) {
            setFilterButtonsIconVisible(true);
        }
    }

    @Override
    protected String onLoadSearchBoxValue() {
        return manageDistUIstate.getManageDistFilters().getSearchText().orElse(null);
    }

    @Override
    protected void showFilterButtonsLayout() {
        manageDistUIstate.setDistTypeFilterClosed(false);
        eventbus.publish(this, DistributionsUIEvent.SHOW_DIST_FILTER_BY_TYPE);
    }

    @Override
    protected void resetSearchText() {
        if (manageDistUIstate.getManageDistFilters().getSearchText().isPresent()) {
            manageDistUIstate.getManageDistFilters().setSearchText(null);
            eventbus.publish(this, DistributionTableFilterEvent.REMOVE_FILTER_BY_TEXT);
        }
    }

    @Override
    public void maximizeTable() {
        manageDistUIstate.setDsTableMaximized(Boolean.TRUE);
        eventbus.publish(this, new DistributionTableEvent(BaseEntityEventType.MAXIMIZED));
    }

    @Override
    public void minimizeTable() {
        manageDistUIstate.setDsTableMaximized(Boolean.FALSE);
        eventbus.publish(this, new DistributionTableEvent(BaseEntityEventType.MINIMIZED));
    }

    @Override
    public Boolean onLoadIsTableMaximized() {
        return manageDistUIstate.isDsTableMaximized();
    }

    @Override
    public Boolean onLoadIsShowFilterButtonDisplayed() {
        return manageDistUIstate.isDistTypeFilterClosed();
    }

    @Override
    protected void searchBy(final String newSearchText) {
        manageDistUIstate.getManageDistFilters().setSearchText(newSearchText);
        eventbus.publish(this, DistributionTableFilterEvent.FILTER_BY_TEXT);
    }

    @Override
    protected void addNewItem(final ClickEvent event) {
        final Window newDistWindow = addUpdateWindowLayout.getWindow(null);
        newDistWindow.setCaption(i18n.getMessage(UIComponentIdProvider.DIST_ADD_CAPTION));
        UI.getCurrent().addWindow(newDistWindow);
        newDistWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected Boolean isAddNewItemAllowed() {
        return Boolean.TRUE;
    }

}
