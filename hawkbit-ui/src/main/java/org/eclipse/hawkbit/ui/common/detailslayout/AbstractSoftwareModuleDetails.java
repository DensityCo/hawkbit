/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareManagement;
import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleAddUpdateWindow;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.distributions.event.MetadataEvent;
import org.eclipse.hawkbit.ui.distributions.smtable.SwMetadataPopupLayout;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Abstract class which contains common code for Software Module Details
 *
 */
public abstract class AbstractSoftwareModuleDetails
        extends AbstractNamedVersionedEntityTableDetailsLayout<SoftwareModule> {

    private static final long serialVersionUID = 1L;

    private final SoftwareModuleMetadatadetailslayout swmMetadataTable;

    private final SoftwareModuleAddUpdateWindow softwareModuleAddUpdateWindow;

    private final transient SoftwareManagement softwareManagement;

    private final SwMetadataPopupLayout swMetadataPopupLayout;

    protected AbstractSoftwareModuleDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManagementUIState managementUIState,
            final SoftwareManagement softwareManagement, final SwMetadataPopupLayout swMetadataPopupLayout,
            final EntityFactory entityFactory, final SoftwareModuleAddUpdateWindow softwareModuleAddUpdateWindow) {
        super(i18n, eventBus, permissionChecker, managementUIState);
        this.softwareModuleAddUpdateWindow = softwareModuleAddUpdateWindow;
        this.softwareManagement = softwareManagement;
        this.swMetadataPopupLayout = swMetadataPopupLayout;

        swmMetadataTable = new SoftwareModuleMetadatadetailslayout();
        swmMetadataTable.init(getI18n(), getPermissionChecker(), softwareManagement, swMetadataPopupLayout,
                entityFactory);

        addDetailsTab();
    }

    /**
     * MetadataEvent.
     *
     * @param event
     *            as instance of {@link MetadataEvent}
     */
    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final MetadataEvent event) {
        UI.getCurrent().access(() -> {
            final MetaData softwareModuleMetadata = event.getMetaData();
            if (softwareModuleMetadata == null || !isSoftwareModuleSelected(event.getModule())) {
                return;
            }
            if (event.getMetadataUIEvent() == MetadataEvent.MetadataUIEvent.CREATE_SOFTWARE_MODULE_METADATA) {
                swmMetadataTable.createMetadata(event.getMetaData().getKey());
            } else if (event.getMetadataUIEvent() == MetadataEvent.MetadataUIEvent.DELETE_SOFTWARE_MODULE_METADATA) {
                swmMetadataTable.deleteMetadata(event.getMetaData().getKey());
            }
        });
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent softwareModuleEvent) {
        onBaseEntityEvent(softwareModuleEvent);
    }

    private final void addDetailsTab() {
        getDetailsTab().addTab(getDetailsLayout(), getI18n().getMessage("caption.tab.details"), null);
        getDetailsTab().addTab(getDescriptionLayout(), getI18n().getMessage("caption.tab.description"), null);
        getDetailsTab().addTab(getLogLayout(), getI18n().getMessage("caption.logs.tab"), null);
        getDetailsTab().addTab(swmMetadataTable, getI18n().getMessage("caption.metadata"), null);
    }

    @Override
    protected void populateMetadataDetails() {
        swmMetadataTable.populateSMMetadata(getSelectedBaseEntity());
    }

    @Override
    protected void onEdit(final ClickEvent event) {
        final Window addSoftwareModule = softwareModuleAddUpdateWindow
                .createUpdateSoftwareModuleWindow(getSelectedBaseEntityId());
        addSoftwareModule.setCaption(getI18n().getMessage("upload.caption.update.swmodule"));
        UI.getCurrent().addWindow(addSoftwareModule);
        addSoftwareModule.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getEditButtonId() {
        return UIComponentIdProvider.UPLOAD_SW_MODULE_EDIT_BUTTON;
    }

    @Override
    protected String getDefaultCaption() {
        return getI18n().getMessage("upload.swModuleTable.header");
    }

    @Override
    protected boolean hasEditPermission() {
        return getPermissionChecker().hasUpdateDistributionPermission();
    }

    @Override
    protected String getDetailsHeaderCaptionId() {
        return UIComponentIdProvider.TARGET_DETAILS_HEADER_LABEL_ID;
    }

    @Override
    protected boolean isMetadataIconToBeDisplayed() {
        return true;
    }

    @Override
    protected void showMetadata(final ClickEvent event) {
        softwareManagement.findSoftwareModuleById(getSelectedBaseEntityId())
                .ifPresent(swmodule -> UI.getCurrent().addWindow(swMetadataPopupLayout.getWindow(swmodule, null)));
    }

    protected void updateSoftwareModuleDetailsLayout(final String type, final String vendor, final String maxAssign) {
        final VerticalLayout detailsTabLayout = getDetailsLayout();

        detailsTabLayout.removeAllComponents();

        final Label vendorLabel = SPUIComponentProvider.createNameValueLabel(
                getI18n().getMessage("label.dist.details.vendor"),
                HawkbitCommonUtil.trimAndNullIfEmpty(vendor) == null ? "" : vendor);
        vendorLabel.setId(UIComponentIdProvider.DETAILS_VENDOR_LABEL_ID);
        detailsTabLayout.addComponent(vendorLabel);

        if (type != null) {
            final Label typeLabel = SPUIComponentProvider
                    .createNameValueLabel(getI18n().getMessage("label.dist.details.type"), type);
            typeLabel.setId(UIComponentIdProvider.DETAILS_TYPE_LABEL_ID);
            detailsTabLayout.addComponent(typeLabel);
        }

        final Label assignLabel = SPUIComponentProvider.createNameValueLabel(
                getI18n().getMessage("label.assigned.type"),
                HawkbitCommonUtil.trimAndNullIfEmpty(maxAssign) == null ? "" : maxAssign);
        assignLabel.setId(UIComponentIdProvider.SWM_DTLS_MAX_ASSIGN);
        detailsTabLayout.addComponent(assignLabel);
    }

    @Override
    protected void populateDetailsWidget() {
        populateDetails();
        populateMetadataDetails();
    }

    protected boolean compareSoftwareModulesById(final SoftwareModule softwareModule,
            final Long selectedBaseSwModuleId) {
        if (softwareModule == null) {
            return false;
        }

        return softwareModule.getId().equals(selectedBaseSwModuleId);
    }

    protected abstract boolean isSoftwareModuleSelected(SoftwareModule softwareModule);

    private void populateDetails() {
        if (getSelectedBaseEntity() != null) {
            String maxAssign;
            if (getSelectedBaseEntity().getType().getMaxAssignments() == 1) {
                maxAssign = getI18n().getMessage("label.singleAssign.type");
            } else {
                maxAssign = getI18n().getMessage("label.multiAssign.type");
            }
            updateSoftwareModuleDetailsLayout(getSelectedBaseEntity().getType().getName(),
                    getSelectedBaseEntity().getVendor(), maxAssign);
        } else {
            updateSoftwareModuleDetailsLayout(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
        }
    }

}
