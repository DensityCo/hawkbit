/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.hawkbit.dmf.amqp.api.AmqpSettings;
import org.eclipse.hawkbit.dmf.json.model.DownloadResponse;
import org.eclipse.hawkbit.dmf.json.model.TenantSecurityToken;
import org.eclipse.hawkbit.dmf.json.model.TenantSecurityToken.FileResource;
import org.eclipse.hawkbit.repository.model.Artifact;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetWithActionType;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

@Features("Component Tests - Device Management Federation API")
@Stories("Amqp Authentication Message Handler")
public class AmqpAuthenticationMessageHandlerIntegrationTest extends AbstractAmqpIntegrationTest {

    private static final String TARGET_SECRUITY_TOKEN = "12345";
    private static final String TARGET_TOKEN_HEADER = "TargetToken " + TARGET_SECRUITY_TOKEN;
    private static final String TENANT_EXIST = "DEFAULT";
    private static final String TARGET = "NewDmfTarget";

    @Before
    public void testSetup() {
        enableTargetTokenAuthentification();
    }

    @Test
    @Description("Tests wrong content type. This message is invalid and should not be requeued. Additional the receive message is null")
    public void wrongContentType() {
        final Message createAndSendMessage = getDmfClient()
                .sendAndReceive(new Message("".getBytes(), new MessageProperties()));
        assertThat(createAndSendMessage).isNull();
    }

    @Test
    @Description("Tests a null message. This message is invalid and should not be requeued. Additional the receive message is null")
    public void securityTokenIsNull() {
        final Message createAndSendMessage = sendAndReceiveAuthenticationMessage(null);
        assertThat(createAndSendMessage).isNull();
    }

    @Test
    @Description("Tenant in the message is null. This message is invalid and should not be requeued. Additional the receive message is null")
    public void securityTokenTenantIsNull() {
        final TenantSecurityToken securityToken = createTenantSecurityToken(null, TARGET,
                FileResource.createFileResourceBySha1(TARGET_SECRUITY_TOKEN));
        final Message createAndSendMessage = sendAndReceiveAuthenticationMessage(securityToken);
        assertThat(createAndSendMessage).isNull();
    }

    @Test
    @Description("Target in the message is null.This message is invalid and should not requeued. Additional the receive message is null")
    public void securityTokenFileResourceIsNull() {
        enableAnonymousAuthentification();
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET, null);
        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND, null);
    }

    @Test
    @Description("Verify that login fails if the given credential not match.")
    public void loginFailedBadCredentials() {
        tenantConfigurationManagement.addOrUpdateConfiguration(TenantConfigurationKey.ANONYMOUS_DOWNLOAD_MODE_ENABLED,
                false);
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceBySha1(TARGET_SECRUITY_TOKEN));
        final Message createAndSendMessage = sendAndReceiveAuthenticationMessage(securityToken);

        verifyResult(createAndSendMessage, HttpStatus.FORBIDDEN, "Login failed");

    }

    @Test
    @Description("Verify that the receive message contains a 404 code,if the artifact could not found")
    public void fileResourceGetSha1InSecurityTokenIsNull() {
        enableAnonymousAuthentification();
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceBySha1(null));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=null, artifactId=null, filename=null]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 404 code , if the distributionSet is not assigned to the target")
    public void artifactForFileResourceSHA1FoundByTargetIdTargetExistsButIsNotAssigned() {
        final Target target = createTarget(TARGET);

        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final String sha1Hash = artifacts.get(0).getSha1Hash();
        final TenantSecurityToken securityToken = createTenantSecurityToken(target.getTenant(), target.getId(), null,
                FileResource.createFileResourceBySha1(sha1Hash));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=" + sha1Hash + ", artifactId=null, filename=null]not found ");
    }

    @Test
    @Description("Verify that the receive message contains a 404 code, if there is no artifact for the given sha1")
    public void artifactForFileResourceSHA1NotFound() {
        enableAnonymousAuthentification();
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceBySha1(TARGET_SECRUITY_TOKEN));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=12345, artifactId=null, filename=null]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 404 code, if there is no existing target for the given controller id")
    public void artifactForFileResourceSHA1FoundTargetNotExists() {
        enableAnonymousAuthentification();
        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final String sha1Hash = artifacts.get(0).getSha1Hash();
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceBySha1(sha1Hash));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=" + sha1Hash + ", artifactId=null, filename=null]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 404 code, if there is no existing artifact for the target")
    public void artifactForFileResourceSHA1FoundTargetExistsButNotAssigned() {
        createTarget(TARGET);

        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceBySha1(artifacts.get(0).getSha1Hash()));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=2f532b93ed23b4341a81dc9b1ee8a1c44b5526ab, artifactId=null, filename=null]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 200 code and a artifact for the existing controller id ")
    public void artifactForFileResourceSHA1FoundTargetExistsIsAssigned() {
        createTarget(TARGET);

        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final Artifact artifact = artifacts.get(0);
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceBySha1(artifact.getSha1Hash()));

        deploymentManagement.assignDistributionSet(distributionSet.getId(),
                Arrays.asList(new TargetWithActionType(TARGET)));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyOkResult(returnMessage, artifact);

    }

    @Test
    @Description("Verify that the receive message contains a 200 code and a artifact for the existing target id ")
    public void artifactForFileResourceSHA1FoundByTargetIdTargetExistsIsAssigned() {
        final Target target = createTarget(TARGET);

        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final Artifact artifact = artifacts.get(0);
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, target.getId(), null,
                FileResource.createFileResourceBySha1(artifact.getSha1Hash()));

        deploymentManagement.assignDistributionSet(distributionSet.getId(),
                Arrays.asList(new TargetWithActionType(TARGET)));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyOkResult(returnMessage, artifact);

    }

    @Test
    @Description("Verify that the receive message contains a 200 code and a artifact without a controller id (anonymous enabled)")
    public void anonymousAuthentification() {
        enableAnonymousAuthentification();
        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final Artifact artifact = artifacts.get(0);
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, null, null,
                FileResource.createFileResourceBySha1(artifact.getSha1Hash()));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyOkResult(returnMessage, artifact);

    }

    @Test
    @Description("Artifact is downloaded anonymous as there is not target id or controller id assigned to the target.")
    public void targetTokenAuthentification() {
        createTarget(TARGET);

        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final Artifact artifact = artifacts.get(0);
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceBySha1(artifact.getSha1Hash()));

        deploymentManagement.assignDistributionSet(distributionSet.getId(),
                Arrays.asList(new TargetWithActionType(TARGET)));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyOkResult(returnMessage, artifact);

    }

    @Test
    @Description("Verify that the receive message contains a 404, if there is no artifact to the given filename")
    public void artifactForFileResourceFileNameNotFound() {
        enableAnonymousAuthentification();
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceByFilename("Test.txt"));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=null, artifactId=null, filename=Test.txt]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 404, if there is no distribution set assigned to the target")
    public void artifactForFileResourceFileNameFoundTargetExistsButNotAssigned() {
        createTarget(TARGET);

        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.createFileResourceByFilename(artifacts.get(0).getFilename()));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=null, artifactId=null, filename=filename0]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 404, if there is no exisiting target")
    public void artifactForFileResourceArtifactIdFoundTargetNotExists() {
        enableAnonymousAuthentification();
        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final FileResource fileResource = FileResource.createFileResourceByArtifactId(artifacts.get(0).getId());
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET, fileResource);

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND, "Artifact for resource FileResource [sha1=null, artifactId="
                + artifacts.get(0).getId() + ", filename=null]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 200 and a artifact for the given artifact id")
    public void artifactForFileResourceArtifactIdFoundTargetExistsIsAssigned() {
        createTarget(TARGET);

        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);
        final Artifact artifact = artifacts.get(0);
        final FileResource fileResource = FileResource.createFileResourceByArtifactId(artifact.getId());
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET, fileResource);

        deploymentManagement.assignDistributionSet(distributionSet.getId(),
                Arrays.asList(new TargetWithActionType(TARGET)));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyOkResult(returnMessage, artifact);

    }

    @Test
    @Description("Verify that the receive message contains a 404, if there is no artifact to the given softwareModuleFilename")
    public void artifactForFileResourceSoftwareModuleFilenameNotFound() {
        enableAnonymousAuthentification();
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET,
                FileResource.softwareModuleFilename(1L, "Test.txt"));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=null, artifactId=null, filename=null]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 404, if there is no existing target for the file resource")
    public void artifactForFileResourceSoftwareModuleFilenameFoundTargetNotExists() {
        enableAnonymousAuthentification();
        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);

        final SoftwareModule softwareModule = distributionSet.getModules().stream().findFirst().get();
        final Artifact artifact = findArtifactOfSoftwareModule(artifacts, softwareModule);

        final FileResource fileResource = FileResource.softwareModuleFilename(softwareModule.getId(),
                softwareModule.getArtifact(artifact.getId()).get().getFilename());
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET, fileResource);

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyResult(returnMessage, HttpStatus.NOT_FOUND,
                "Artifact for resource FileResource [sha1=null, artifactId=null, filename=null]not found ");

    }

    @Test
    @Description("Verify that the receive message contains a 200 and a artifact, if there is a existing artifct fpt the for the given softwareModuleFilename")
    public void artifactForFileResourceSoftwareModuleFilenameFoundTargetExistsIsAssigned() {
        createTarget(TARGET);

        final DistributionSet distributionSet = createDistributionSet();
        final List<Artifact> artifacts = createArtifacts(distributionSet);

        final SoftwareModule softwareModule = distributionSet.getModules().stream().findFirst().get();
        final Artifact artifact = findArtifactOfSoftwareModule(artifacts, softwareModule);

        final FileResource fileResource = FileResource.softwareModuleFilename(softwareModule.getId(),
                softwareModule.getArtifact(artifact.getId()).get().getFilename());
        final TenantSecurityToken securityToken = createTenantSecurityToken(TENANT_EXIST, TARGET, fileResource);

        deploymentManagement.assignDistributionSet(distributionSet.getId(),
                Arrays.asList(new TargetWithActionType(TARGET)));

        final Message returnMessage = sendAndReceiveAuthenticationMessage(securityToken);
        verifyOkResult(returnMessage, artifact);

    }

    private void verifyOkResult(Message returnMessage, Artifact artifact) {
        final DownloadResponse convertedMessage = verifyResult(returnMessage, HttpStatus.OK, null);
        assertThat(convertedMessage.getDownloadUrl()).isNotNull();
        assertThat(convertedMessage.getArtifact()).isNotNull();
        assertThat(convertedMessage.getArtifact().getHashes().getSha1()).isEqualTo(artifact.getSha1Hash());

    }

    private void enableAnonymousAuthentification() {
        tenantConfigurationManagement.addOrUpdateConfiguration(TenantConfigurationKey.ANONYMOUS_DOWNLOAD_MODE_ENABLED,
                true);
        tenantConfigurationManagement.addOrUpdateConfiguration(
                TenantConfigurationKey.AUTHENTICATION_MODE_TARGET_SECURITY_TOKEN_ENABLED, false);
    }

    private void enableTargetTokenAuthentification() {
        tenantConfigurationManagement.addOrUpdateConfiguration(TenantConfigurationKey.ANONYMOUS_DOWNLOAD_MODE_ENABLED,
                false);
        tenantConfigurationManagement.addOrUpdateConfiguration(
                TenantConfigurationKey.AUTHENTICATION_MODE_TARGET_SECURITY_TOKEN_ENABLED, true);
    }

    private Target createTarget(final String controllerId) {
        return targetManagement.createTarget(
                entityFactory.target().create().controllerId(controllerId).securityToken(TARGET_SECRUITY_TOKEN));
    }

    private TenantSecurityToken createTenantSecurityToken(final String tenant, final String controllerId,
            final FileResource fileResource) {
        final TenantSecurityToken tenantSecurityToken = new TenantSecurityToken(tenant, controllerId, fileResource);
        tenantSecurityToken.putHeader(TenantSecurityToken.AUTHORIZATION_HEADER, TARGET_TOKEN_HEADER);
        return tenantSecurityToken;
    }

    private TenantSecurityToken createTenantSecurityToken(final String tenant, final Long targetId,
            final String controllerId, final FileResource fileResource) {
        final TenantSecurityToken tenantSecurityToken = new TenantSecurityToken(tenant, null, controllerId, targetId,
                fileResource);
        tenantSecurityToken.putHeader(TenantSecurityToken.AUTHORIZATION_HEADER, TARGET_TOKEN_HEADER);
        return tenantSecurityToken;
    }

    private DistributionSet createDistributionSet() {
        return testdataFactory.createDistributionSet("one");
    }

    private List<Artifact> createArtifacts(final DistributionSet distributionSet) {
        final List<Artifact> artifacts = new ArrayList<>();
        for (final SoftwareModule module : distributionSet.getModules()) {
            artifacts.addAll(testdataFactory.createArtifacts(module.getId()));
        }
        return artifacts;
    }

    private DownloadResponse verifyResult(final Message returnMessage, final HttpStatus expectedStatus,
            final String expectedMessage) {
        final DownloadResponse convertedMessage = (DownloadResponse) getDmfClient().getMessageConverter()
                .fromMessage(returnMessage);
        assertThat(convertedMessage.getResponseCode()).isEqualTo(expectedStatus.value());

        if (!StringUtils.isEmpty(expectedMessage)) {
            assertThat(convertedMessage.getMessage()).isEqualTo(expectedMessage);
        }
        return convertedMessage;
    }

    private Message sendAndReceiveAuthenticationMessage(final TenantSecurityToken securityToken) {
        return getDmfClient().sendAndReceive(createMessage(securityToken, new MessageProperties()));
    }

    private Artifact findArtifactOfSoftwareModule(final List<Artifact> artifacts, final SoftwareModule softwareModule) {
        return artifacts.stream().filter(space -> space.getSoftwareModule().getId().equals(softwareModule.getId()))
                .findFirst().get();
    }

    @Override
    protected String getExchange() {
        return AmqpSettings.AUTHENTICATION_EXCHANGE;
    }

}
