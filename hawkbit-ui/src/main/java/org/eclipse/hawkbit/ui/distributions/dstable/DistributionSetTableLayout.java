/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TagManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.table.AbstractTableLayout;
import org.eclipse.hawkbit.ui.dd.criteria.DistributionsViewClientCriterion;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.dstable.DistributionAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout
 */
public class DistributionSetTableLayout extends AbstractTableLayout<DistributionSetTable> {

    private static final long serialVersionUID = 6464291374980641235L;

    public DistributionSetTableLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManageDistUIState manageDistUIState,
            final SoftwareManagement softwareManagement, final DistributionSetManagement distributionSetManagement,
            final TargetManagement targetManagement, final EntityFactory entityFactory,
            final UINotification uiNotification, final TagManagement tagManagement,
            final DistributionsViewClientCriterion distributionsViewClientCriterion,
            final SystemManagement systemManagement) {

        final DsMetadataPopupLayout popupLayout = new DsMetadataPopupLayout(i18n, uiNotification, eventBus,
                distributionSetManagement, entityFactory, permissionChecker);

        final DistributionSetTable distributionSetTable = new DistributionSetTable(eventBus, i18n, uiNotification,
                permissionChecker, manageDistUIState, distributionSetManagement, softwareManagement,
                distributionsViewClientCriterion, targetManagement, popupLayout);

        final DistributionAddUpdateWindowLayout distributionAddUpdateWindowLayout = new DistributionAddUpdateWindowLayout(
                i18n, uiNotification, eventBus, distributionSetManagement, systemManagement, entityFactory,
                distributionSetTable);

        super.init(
                new DistributionSetTableHeader(i18n, permissionChecker, eventBus, manageDistUIState,
                        distributionAddUpdateWindowLayout),
                distributionSetTable,
                new DistributionSetDetails(i18n, eventBus, permissionChecker, manageDistUIState, null,
                        distributionAddUpdateWindowLayout, softwareManagement, distributionSetManagement,
                        targetManagement, entityFactory, uiNotification, tagManagement, popupLayout));
    }

}
