/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.assetmanager.handlers;

import org.odpi.openmetadata.accessservices.assetmanager.converters.GlossaryConverter;
import org.odpi.openmetadata.accessservices.assetmanager.converters.ExternalIdentifierConverter;
import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.*;
import org.odpi.openmetadata.accessservices.assetmanager.properties.*;
import org.odpi.openmetadata.commonservices.ffdc.InvalidParameterHandler;
import org.odpi.openmetadata.commonservices.generichandlers.ExternalIdentifierHandler;
import org.odpi.openmetadata.commonservices.generichandlers.GlossaryHandler;
import org.odpi.openmetadata.commonservices.generichandlers.OpenMetadataAPIMapper;
import org.odpi.openmetadata.commonservices.repositoryhandler.RepositoryHandler;
import org.odpi.openmetadata.frameworks.auditlog.AuditLog;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.metadatasecurity.server.OpenMetadataServerSecurityVerifier;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryHelper;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class GlossaryExchangeHandler
{
    private InvalidParameterHandler invalidParameterHandler;
    private GlossaryHandler<GlossaryElement> glossaryHandler;
    private ExternalIdentifierHandler<MetadataCorrelationProperties> externalIdentifierHandler;

    private final static String glossaryGUIDParameterName  = "glossaryGUID";


    /**
     * Construct the glossary manager handler with information needed to work with glossary related objects
     * for Asset Manager OMAS.
     *
     * @param serviceName      name of this service
     * @param serverName       name of the local server
     * @param invalidParameterHandler handler for managing parameter errors
     * @param repositoryHandler     manages calls to the repository services
     * @param repositoryHelper provides utilities for manipulating the repository services objects
     * @param localServerUserId userId for this server
     * @param securityVerifier open metadata security services verifier
     * @param supportedZones list of zones that the access service is allowed to serve B instances from.
     * @param defaultZones list of zones that the access service should set in all new B instances.
     * @param publishZones list of zones that the access service sets up in published B instances.
     * @param auditLog destination for audit log events.
     */
    public GlossaryExchangeHandler(String                             serviceName,
                                   String                             serverName,
                                   InvalidParameterHandler            invalidParameterHandler,
                                   RepositoryHandler                  repositoryHandler,
                                   OMRSRepositoryHelper               repositoryHelper,
                                   String                             localServerUserId,
                                   OpenMetadataServerSecurityVerifier securityVerifier,
                                   List<String>                       supportedZones,
                                   List<String>                       defaultZones,
                                   List<String>                       publishZones,
                                   AuditLog                           auditLog)
    {
        glossaryHandler = new GlossaryHandler<>(new GlossaryConverter<>(repositoryHelper, serviceName, serverName),
                                                GlossaryElement.class,
                                                serviceName,
                                                serverName,
                                                invalidParameterHandler,
                                                repositoryHandler,
                                                repositoryHelper,
                                                localServerUserId,
                                                securityVerifier,
                                                supportedZones,
                                                defaultZones,
                                                publishZones,
                                                auditLog);

        externalIdentifierHandler = new ExternalIdentifierHandler<>(new ExternalIdentifierConverter<>(repositoryHelper, serviceName, serverName),
                                                                    MetadataCorrelationProperties.class,
                                                                    serviceName,
                                                                    serverName,
                                                                    invalidParameterHandler,
                                                                    repositoryHandler,
                                                                    repositoryHelper,
                                                                    localServerUserId,
                                                                    securityVerifier,
                                                                    supportedZones,
                                                                    defaultZones,
                                                                    publishZones,
                                                                    auditLog);

        this.invalidParameterHandler = invalidParameterHandler;
    }



    /* ========================================================
     * Managing the externalIds and related correlation properties.
     */


    /**
     * Save the external identifier and related correlation properties.
     *
     * @param userId calling user
     * @param elementGUID open metadata identifier of the element that is to be linked to the external identifier
     * @param elementGUIDParameterName name of the open metadata identifier
     * @param elementTypeName type name of the open metadata element
     * @param correlationProperties properties to store in the external identifier
     * @param methodName calling method
     *
     * @throws InvalidParameterException  the parameters are invalid
     * @throws UserNotAuthorizedException user not authorized to issue this request
     * @throws PropertyServerException    problem detected in the repository services
     */
    private void createExternalIdentifier(String                        userId,
                                          String                        elementGUID,
                                          String                        elementGUIDParameterName,
                                          String                        elementTypeName,
                                          MetadataCorrelationProperties correlationProperties,
                                          String                        methodName) throws InvalidParameterException,
                                                                                           UserNotAuthorizedException,
                                                                                           PropertyServerException
    {
        final String correlationParameterName      = "correlationProperties";
        final String assetManagerGUIDParameterName = "correlationProperties.assetManagerGUID";
        final String assetManagerNameParameterName = "correlationProperties.assetManagerName";
        final String identifierParameterName       = "correlationProperties.assetManagerName";


        invalidParameterHandler.validateObject(correlationProperties, correlationParameterName, methodName);
        invalidParameterHandler.validateGUID(correlationProperties.getAssetManagerGUID(), assetManagerGUIDParameterName, methodName);
        invalidParameterHandler.validateName(correlationProperties.getAssetManagerName(), assetManagerNameParameterName, methodName);
        invalidParameterHandler.validateName(correlationProperties.getExternalIdentifier(), identifierParameterName, methodName);

        externalIdentifierHandler.createExternalIdentifier(userId,
                                                           elementGUID,
                                                           elementGUIDParameterName,
                                                           elementTypeName,
                                                           correlationProperties.getExternalIdentifier(),
                                                           correlationProperties.getKeyPattern().getOpenTypeOrdinal(),
                                                           correlationProperties.getExternalIdentifierName(),
                                                           correlationProperties.getExternalIdentifierUsage(),
                                                           correlationProperties.getAssetManagerName(),
                                                           correlationProperties.getMappingProperties(),
                                                           new Date(),
                                                           correlationProperties.getAssetManagerGUID(),
                                                           assetManagerGUIDParameterName,
                                                           OpenMetadataAPIMapper.ASSET_MANAGER_TYPE_NAME,
                                                           correlationProperties.getPermittedSynchronization().getOpenTypeOrdinal(),
                                                           correlationProperties.getSynchronizationDescription(),
                                                           methodName);
    }



    /**
     * Retrieve the external identifier and check it is correct.
     *
     * @param userId calling user
     * @param elementGUID open metadata identifier of the element that is to be linked to the external identifier
     * @param elementGUIDParameterName name of the open metadata identifier
     * @param elementTypeName type name of the open metadata element
     * @param correlationProperties properties to store in the external identifier
     * @param methodName calling method
     *
     * @throws InvalidParameterException  the parameters are invalid
     * @throws UserNotAuthorizedException user not authorized to issue this request
     * @throws PropertyServerException    problem detected in the repository services
     */
    private void validateExternalIdentifier(String                        userId,
                                            String                        elementGUID,
                                            String                        elementGUIDParameterName,
                                            String                        elementTypeName,
                                            MetadataCorrelationProperties correlationProperties,
                                            String                        methodName) throws InvalidParameterException,
                                                                                             UserNotAuthorizedException,
                                                                                             PropertyServerException
    {

    }



    /**
     * Retrieve the external identifier for the supplied asset manager to pass to the caller. It is OK if there is no
     * external identifier since this is used for retrieve requests.
     *
     * @param userId calling user
     * @param elementGUID open metadata identifier of the element that is to be linked to the external identifier
     * @param elementGUIDParameterName name of the open metadata identifier
     * @param elementTypeName type name of the open metadata element
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param methodName calling method
     *
     * @throws InvalidParameterException  the parameters are invalid
     * @throws UserNotAuthorizedException user not authorized to issue this request
     * @throws PropertyServerException    problem detected in the repository services
     */
    private MetadataCorrelationProperties getCorrelationProperties(String userId,
                                                                   String elementGUID,
                                                                   String elementGUIDParameterName,
                                                                   String elementTypeName,
                                                                   String assetManagerGUID,
                                                                   String assetManagerName,
                                                                   String methodName) throws InvalidParameterException,
                                                                                             UserNotAuthorizedException,
                                                                                             PropertyServerException
    {
        return null;
    }



    /* ========================================================
     * The Glossary entity is the top level element in a glossary.
     */


    /**
     * Create a new metadata element to represent the root of a glossary.  All categories and terms are linked
     * to a single glossary.  They are owned by this glossary and if the glossary is deleted, any linked terms and
     * categories are deleted as well.
     *
     * @param userId calling user
     * @param correlationProperties  properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryProperties properties to store
     * @param methodName calling method
     *
     * @return unique identifier of the new metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createGlossary(String                        userId,
                                 MetadataCorrelationProperties correlationProperties,
                                 GlossaryProperties            glossaryProperties,
                                 String                        methodName) throws InvalidParameterException,
                                                                                  UserNotAuthorizedException,
                                                                                  PropertyServerException
    {
        final String propertiesParameterName    = "glossaryProperties";
        final String qualifiedNameParameterName = "glossaryProperties.qualifiedName";

        invalidParameterHandler.validateUserId(userId, methodName);

        invalidParameterHandler.validateObject(glossaryProperties, propertiesParameterName, methodName);
        invalidParameterHandler.validateName(glossaryProperties.getQualifiedName(), qualifiedNameParameterName, methodName);

        String glossaryGUID = glossaryHandler.createGlossary(userId,
                                                             glossaryProperties.getQualifiedName(),
                                                             glossaryProperties.getDisplayName(),
                                                             glossaryProperties.getDescription(),
                                                             glossaryProperties.getLanguage(),
                                                             glossaryProperties.getUsage(),
                                                             glossaryProperties.getAdditionalProperties(),
                                                             glossaryProperties.getTypeName(),
                                                             glossaryProperties.getExtendedProperties(),
                                                             methodName);

        if (glossaryGUID != null)
        {
            this.createExternalIdentifier(userId,
                                          glossaryGUID,
                                          glossaryGUIDParameterName,
                                          OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                          correlationProperties,
                                          methodName);
        }

        return glossaryGUID;
    }


    /**
     * Create a new metadata element to represent a glossary using an existing metadata element as a template.
     * The template defines additional classifications and relationships that should be added to the new glossary.
     *
     * All categories and terms are linked to a single glossary.  They are owned by this glossary and if the
     * glossary is deleted, any linked terms and categories are deleted as well.
     *
     * @param userId calling user
     * @param correlationProperties  properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param templateGUID unique identifier of the metadata element to copy
     * @param templateProperties properties that override the template
     * @param methodName calling method
     *
     * @return unique identifier of the new metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createGlossaryFromTemplate(String                        userId,
                                             MetadataCorrelationProperties correlationProperties,
                                             String                        templateGUID,
                                             TemplateProperties            templateProperties,
                                             String                        methodName) throws InvalidParameterException,
                                                                                              UserNotAuthorizedException,
                                                                                              PropertyServerException
    {
        final String templateGUIDParameterName   = "templateGUID";
        final String propertiesParameterName     = "templateProperties";

        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(templateGUID, templateGUIDParameterName, methodName);
        invalidParameterHandler.validateObject(templateProperties, propertiesParameterName, methodName);

        String glossaryGUID = glossaryHandler.createGlossaryFromTemplate(userId,
                                                                         templateGUID,
                                                                         templateProperties.getQualifiedName(),
                                                                         templateProperties.getDisplayName(),
                                                                         templateProperties.getDescription(),
                                                                         methodName);
        if (glossaryGUID != null)
        {
            this.createExternalIdentifier(userId,
                                          glossaryGUID,
                                          glossaryGUIDParameterName,
                                          OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                          correlationProperties,
                                          methodName);
        }
        return glossaryGUID;
    }


    /**
     * Update the metadata element representing a glossary.
     *
     * @param userId calling user
     * @param correlationProperties  properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryGUID unique identifier of the metadata element to update
     * @param glossaryProperties new properties for this element
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void updateGlossary(String                        userId,
                               MetadataCorrelationProperties correlationProperties,
                               String                        glossaryGUID,
                               GlossaryProperties            glossaryProperties,
                               String                        methodName) throws InvalidParameterException,
                                                                                UserNotAuthorizedException,
                                                                                PropertyServerException
    {
        final String propertiesParameterName    = "glossaryProperties";
        final String qualifiedNameParameterName = "glossaryProperties.qualifiedName";

        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(glossaryGUID, glossaryGUIDParameterName, methodName);
        invalidParameterHandler.validateObject(glossaryProperties, propertiesParameterName, methodName);
        invalidParameterHandler.validateName(glossaryProperties.getQualifiedName(), qualifiedNameParameterName, methodName);

        this.validateExternalIdentifier(userId,
                                        glossaryGUID,
                                        glossaryGUIDParameterName,
                                        OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                        correlationProperties,
                                        methodName);

        glossaryHandler.updateGlossary(userId,
                                       glossaryGUID,
                                       glossaryProperties.getQualifiedName(),
                                       glossaryProperties.getDisplayName(),
                                       glossaryProperties.getDescription(),
                                       glossaryProperties.getLanguage(),
                                       glossaryProperties.getUsage(),
                                       glossaryProperties.getAdditionalProperties(),
                                       glossaryProperties.getExtendedProperties(),
                                       methodName);


    }


    /**
     * Remove the metadata element representing a glossary.  This will delete the glossary and all categories and terms because
     * the Anchors classifications are set up in these elements.
     *
     * @param userId calling user
     * @param correlationProperties properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryGUID unique identifier of the metadata element to remove
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void removeGlossary(String                        userId,
                               MetadataCorrelationProperties correlationProperties,
                               String                        glossaryGUID,
                               String                        methodName) throws InvalidParameterException,
                                                                                UserNotAuthorizedException,
                                                                                PropertyServerException
    {
        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(glossaryGUID, glossaryGUIDParameterName, methodName);

        this.validateExternalIdentifier(userId,
                                        glossaryGUID,
                                        glossaryGUIDParameterName,
                                        OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                        correlationProperties,
                                        methodName);

        glossaryHandler.deleteBeanInRepository(userId,
                                               null,
                                               null,
                                               glossaryGUID,
                                               glossaryGUIDParameterName,
                                               OpenMetadataAPIMapper.GLOSSARY_TYPE_GUID,
                                               OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                               null,
                                               null,
                                               methodName);
    }


    /**
     * Classify the glossary to indicate that it can be used as a taxonomy.
     * This means each term is attached to one, and only one category and the categories are organized as a hierarchy
     * with a single root category.
     *
     * Taxonomies are used as a way of organizing assets and other related metadata.  The terms in the taxonomy
     * are linked to the assets etc and as such they are logically categorized by the linked category.
     *
     * @param userId calling user
     * @param correlationProperties  properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryGUID unique identifier of the metadata element to remove
     * @param organizingPrinciple description of how the glossary is organized
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setGlossaryAsTaxonomy(String                        userId,
                                      MetadataCorrelationProperties correlationProperties,
                                      String                        glossaryGUID,
                                      String                        organizingPrinciple,
                                      String                        methodName) throws InvalidParameterException,
                                                                                       UserNotAuthorizedException,
                                                                                       PropertyServerException
    {
        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(glossaryGUID, glossaryGUIDParameterName, methodName);

        this.validateExternalIdentifier(userId,
                                        glossaryGUID,
                                        glossaryGUIDParameterName,
                                        OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                        correlationProperties,
                                        methodName);

        glossaryHandler.addTaxonomyClassificationToGlossary(userId, glossaryGUID, glossaryGUIDParameterName, organizingPrinciple, methodName);
    }


    /**
     * Remove the taxonomy designation from the glossary.
     *
     * @param userId calling user
     * @param correlationProperties  properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryGUID unique identifier of the metadata element to remove
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearGlossaryAsTaxonomy(String userId,
                                        MetadataCorrelationProperties correlationProperties,
                                        String glossaryGUID,
                                        String methodName) throws InvalidParameterException,
                                                                  UserNotAuthorizedException,
                                                                  PropertyServerException
    {
        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(glossaryGUID, glossaryGUIDParameterName, methodName);

        this.validateExternalIdentifier(userId,
                                        glossaryGUID,
                                        glossaryGUIDParameterName,
                                        OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                        correlationProperties,
                                        methodName);

        glossaryHandler.removeTaxonomyClassificationFromGlossary(userId, glossaryGUID, glossaryGUIDParameterName, methodName);
    }


    /**
     * Classify a glossary to declare that it has no two GlossaryTerm definitions with
     * the same name.  This means there is only one definition for each term.  Typically the terms are also of a similar
     * level of granularity and are limited to a specific scope of use.
     *
     * Canonical vocabularies are used to semantically classify assets in an unambiguous way.
     *
     * @param userId calling user
     * @param correlationProperties  properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryGUID unique identifier of the metadata element to remove
     * @param scope description of the situations where this glossary is relevant.
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setGlossaryAsCanonical(String                        userId,
                                       MetadataCorrelationProperties correlationProperties,
                                       String                        glossaryGUID,
                                       String                        scope,
                                       String                        methodName) throws InvalidParameterException,
                                                                                        UserNotAuthorizedException,
                                                                                        PropertyServerException
    {
        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(glossaryGUID, glossaryGUIDParameterName, methodName);

        this.validateExternalIdentifier(userId,
                                        glossaryGUID,
                                        glossaryGUIDParameterName,
                                        OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                        correlationProperties,
                                        methodName);

        glossaryHandler.addCanonicalVocabClassificationToGlossary(userId, glossaryGUID, glossaryGUIDParameterName, scope, methodName);

    }


    /**
     * Remove the canonical designation from the glossary.
     *
     * @param userId calling user
     * @param correlationProperties properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryGUID unique identifier of the metadata element to remove
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearGlossaryAsCanonical(String                        userId,
                                         MetadataCorrelationProperties correlationProperties,
                                         String                        glossaryGUID,
                                         String                        methodName) throws InvalidParameterException,
                                                                                          UserNotAuthorizedException,
                                                                                          PropertyServerException
    {
        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(glossaryGUID, glossaryGUIDParameterName, methodName);

        this.validateExternalIdentifier(userId,
                                        glossaryGUID,
                                        glossaryGUIDParameterName,
                                        OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                        correlationProperties,
                                        methodName);

        glossaryHandler.removeCanonicalVocabClassificationFromGlossary(userId, glossaryGUID, glossaryGUIDParameterName, methodName);
    }


    /**
     * Retrieve the list of glossary metadata elements that contain the search string.
     * The search string is treated as a regular expression.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param results list of elements
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    private void addCorrelationPropertiesToGlossaries(String                userId,
                                                      String                assetManagerGUID,
                                                      String                assetManagerName,
                                                      List<GlossaryElement> results,
                                                      String                methodName) throws InvalidParameterException,
                                                                                               UserNotAuthorizedException,
                                                                                               PropertyServerException
    {
        if (results != null)
        {
            for (MetadataElement glossary : results)
            {
                if ((glossary != null) && (glossary.getElementHeader() != null) && (glossary.getElementHeader().getGUID() != null))
                {
                    glossary.setCorrelationProperties(this.getCorrelationProperties(userId,
                                                                                    glossary.getElementHeader().getGUID(),
                                                                                    glossaryGUIDParameterName,
                                                                                    OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                                                                    assetManagerGUID,
                                                                                    assetManagerName,
                                                                                    methodName));
                }
            }
        }
    }

    /**
     * Retrieve the list of glossary metadata elements that contain the search string.
     * The search string is treated as a regular expression.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param searchString string to find in the properties
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryElement> findGlossaries(String userId,
                                                String assetManagerGUID,
                                                String assetManagerName,
                                                String searchString,
                                                int    startFrom,
                                                int    pageSize,
                                                String methodName) throws InvalidParameterException,
                                                                          UserNotAuthorizedException,
                                                                          PropertyServerException
    {
        final String searchStringParameterName  = "searchString";

        List<GlossaryElement> results = glossaryHandler.findGlossaries(userId, searchString, searchStringParameterName, startFrom, pageSize, methodName);

        addCorrelationPropertiesToGlossaries(userId, assetManagerGUID, assetManagerName, results , methodName);

        return results;
    }


    /**
     * Retrieve the list of glossary metadata elements with a matching qualified or display name.
     * There are no wildcards supported on this request.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param name name to search for
     * @param nameParameterName name of parameter supplying name value
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryElement>   getGlossariesByName(String userId,
                                                       String assetManagerGUID,
                                                       String assetManagerName,
                                                       String name,
                                                       String nameParameterName,
                                                       int    startFrom,
                                                       int    pageSize,
                                                       String methodName) throws InvalidParameterException,
                                                                                 UserNotAuthorizedException,
                                                                                 PropertyServerException
    {
        List<GlossaryElement> results = glossaryHandler.getGlossariesByName(userId, name, nameParameterName, startFrom, pageSize, methodName);

        addCorrelationPropertiesToGlossaries(userId, assetManagerGUID, assetManagerName, results , methodName);

        return results;
    }


    /**
     * Retrieve the list of glossaries created by this caller.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryElement>   getGlossariesForAssetManager(String userId,
                                                                String assetManagerGUID,
                                                                String assetManagerName,
                                                                int    startFrom,
                                                                int    pageSize,
                                                                String methodName) throws InvalidParameterException,
                                                                                          UserNotAuthorizedException,
                                                                                          PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Retrieve the glossary metadata element with the supplied unique identifier.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param openMetadataGUID unique identifier of the requested metadata element
     * @param methodName calling method
     *
     * @return matching metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public GlossaryElement getGlossaryByGUID(String userId,
                                             String assetManagerGUID,
                                             String assetManagerName,
                                             String openMetadataGUID,
                                             String methodName) throws InvalidParameterException,
                                                                       UserNotAuthorizedException,
                                                                       PropertyServerException
    {
        final String guidParameterName  = "openMetadataGUID";

        GlossaryElement glossary = glossaryHandler.getGlossaryByGUID(userId, openMetadataGUID, guidParameterName, methodName);

        if (glossary != null)
        {
            glossary.setCorrelationProperties(this.getCorrelationProperties(userId,
                                                                            openMetadataGUID,
                                                                            guidParameterName,
                                                                            OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME,
                                                                            assetManagerGUID,
                                                                            assetManagerName,
                                                                            methodName));
        }

        return glossary;
    }


    /**
     * Retrieve the glossary metadata element with the supplied unique identifier.
     *
     * @param userId calling user
     * @param correlationProperties properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param methodName calling method
     *
     * @return matching metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public GlossaryElement getGlossaryByExternalIdentifier(String                        userId,
                                                           MetadataCorrelationProperties correlationProperties,
                                                           String                        methodName) throws InvalidParameterException,
                                                                                                            UserNotAuthorizedException,
                                                                                                            PropertyServerException
    {
        // todo
        return null;
    }


    /* =====================================================================================================================
     * A glossary may host one or more glossary categories depending on its capability
     */

    /**
     * Create a new metadata element to represent a glossary category.
     *
     * @param userId calling user
     * @param glossaryGUID unique identifier of the glossary where the category is located
     * @param correlationProperties properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryCategoryProperties properties about the glossary category to store
     * @param methodName calling method
     *
     * @return unique identifier of the new glossary category
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createGlossaryCategory(String                        userId,
                                         String                        glossaryGUID,
                                         MetadataCorrelationProperties correlationProperties,
                                         GlossaryCategoryProperties    glossaryCategoryProperties,
                                         String                        methodName) throws InvalidParameterException,
                                                                                          UserNotAuthorizedException,
                                                                                          PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Create a new metadata element to represent a glossary category using an existing metadata element as a template.
     *
     * @param userId calling user
     * @param glossaryGUID unique identifier of the glossary where the category is located
     * @param correlationProperties properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param templateGUID unique identifier of the metadata element to copy
     * @param templateProperties properties that override the template
     * @param methodName calling method
     *
     * @return unique identifier of the new glossary category
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createGlossaryCategoryFromTemplate(String                        userId,
                                                     String                        glossaryGUID,
                                                     MetadataCorrelationProperties correlationProperties,
                                                     String                        templateGUID,
                                                     TemplateProperties            templateProperties,
                                                     String                        methodName) throws InvalidParameterException,
                                                                                                      UserNotAuthorizedException,
                                                                                                      PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Update the metadata element representing a glossary category.
     *
     * @param userId calling user
     * @param correlationProperties properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryCategoryGUID unique identifier of the metadata element to update
     * @param glossaryCategoryProperties new properties for the metadata element
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void updateGlossaryCategory(String                        userId,
                                       MetadataCorrelationProperties correlationProperties,
                                       String                        glossaryCategoryGUID,
                                       GlossaryCategoryProperties    glossaryCategoryProperties,
                                       String                        methodName) throws InvalidParameterException,
                                                                                        UserNotAuthorizedException,
                                                                                        PropertyServerException
    {
        // todo
    }


    /**
     * Create a parent-child relationship between two categories.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryParentCategoryGUID unique identifier of the glossary category in the external asset manager that is to be the super-category
     * @param glossaryChildCategoryGUID unique identifier of the glossary category in the external asset manager that is to be the subcategory
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setupCategoryParent(String userId,
                                    String assetManagerGUID,
                                    String assetManagerName,
                                    String glossaryParentCategoryGUID,
                                    String glossaryChildCategoryGUID,
                                    String methodName) throws InvalidParameterException,
                                                              UserNotAuthorizedException,
                                                              PropertyServerException
    {
        // todo
    }


    /**
     * Remove a parent-child relationship between two categories.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryParentCategoryGUID unique identifier of the glossary category in the external asset manager that is to be the super-category
     * @param glossaryChildCategoryGUID unique identifier of the glossary category in the external asset manager that is to be the subcategory
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearCategoryParent(String userId,
                                    String assetManagerGUID,
                                    String assetManagerName,
                                    String glossaryParentCategoryGUID,
                                    String glossaryChildCategoryGUID,
                                    String methodName) throws InvalidParameterException,
                                                              UserNotAuthorizedException,
                                                              PropertyServerException
    {
        // todo
    }


    /**
     * Remove the metadata element representing a glossary category.
     *
     * @param userId calling user
     * @param correlationProperties properties to help with the mapping of the elements in the external asset manager and open metadata
     * @param glossaryCategoryGUID unique identifier of the metadata element to remove
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void removeGlossaryCategory(String                        userId,
                                       MetadataCorrelationProperties correlationProperties,
                                       String                        glossaryCategoryGUID,
                                       String                        methodName) throws InvalidParameterException,
                                                                                        UserNotAuthorizedException,
                                                                                        PropertyServerException
    {
        // todo
    }


    /**
     * Retrieve the list of glossary category metadata elements that contain the search string.
     * The search string is treated as a regular expression.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param searchString string to find in the properties
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryCategoryElement>   findGlossaryCategories(String userId,
                                                                  String assetManagerGUID,
                                                                  String assetManagerName,
                                                                  String searchString,
                                                                  int    startFrom,
                                                                  int    pageSize,
                                                                  String methodName) throws InvalidParameterException,
                                                                                            UserNotAuthorizedException,
                                                                                            PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Return the list of categories associated with a glossary.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryGUID unique identifier of the glossary to query
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of metadata elements describing the categories associated with the requested glossary
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryCategoryElement>   getCategoriesForGlossary(String userId,
                                                                    String assetManagerGUID,
                                                                    String assetManagerName,
                                                                    String glossaryGUID,
                                                                    int    startFrom,
                                                                    int    pageSize,
                                                                    String methodName) throws InvalidParameterException,
                                                                                              UserNotAuthorizedException,
                                                                                              PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Retrieve the list of glossary category metadata elements with a matching qualified or display name.
     * There are no wildcards supported on this request.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param name name to search for
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryCategoryElement>   getGlossaryCategoriesByName(String userId,
                                                                       String assetManagerGUID,
                                                                       String assetManagerName,
                                                                       String name,
                                                                       int    startFrom,
                                                                       int    pageSize,
                                                                       String methodName) throws InvalidParameterException,
                                                                                                 UserNotAuthorizedException,
                                                                                                 PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Retrieve the glossary category metadata element with the supplied unique identifier.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param openMetadataGUID unique identifier of the requested metadata element
     * @param methodName calling method
     *
     * @return requested metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public GlossaryCategoryElement getGlossaryCategoryByGUID(String userId,
                                                             String assetManagerGUID,
                                                             String assetManagerName,
                                                             String openMetadataGUID,
                                                             String methodName) throws InvalidParameterException,
                                                                                       UserNotAuthorizedException,
                                                                                       PropertyServerException
    {
        // todo
        return null;
    }


    /* ===============================================================================
     * A glossary typically contains many glossary terms, linked with relationships.
     */

    /**
     * Create a new metadata element to represent a glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryGUID unique identifier of the glossary where the term is located
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param mappingProperties additional properties to help with the mapping of the elements in the
     *                          external asset manager and open metadata
     * @param glossaryTermProperties properties for the glossary term
     * @param methodName calling method
     *
     * @return unique identifier of the new metadata element for the glossary term
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createGlossaryTerm(String                 userId,
                                     String                 assetManagerGUID,
                                     String                 assetManagerName,
                                     String                 glossaryGUID,
                                     String                 glossaryTermExternalIdentifier,
                                     Map<String, String>    mappingProperties,
                                     GlossaryTermProperties glossaryTermProperties,
                                     String                 methodName) throws InvalidParameterException,
                                                                               UserNotAuthorizedException,
                                                                               PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Create a new metadata element to represent a glossary term whose lifecycle is managed through a controlled workflow.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryGUID unique identifier of the glossary where the term is located
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param mappingProperties additional properties to help with the mapping of the elements in the
     *                          external asset manager and open metadata
     * @param glossaryTermProperties properties for the glossary term
     * @param initialStatus glossary term status to use when the object is created
     * @param methodName calling method
     *
     * @return unique identifier of the new metadata element for the glossary term
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createControlledGlossaryTerm(String                 userId,
                                               String                 assetManagerGUID,
                                               String                 assetManagerName,
                                               String                 glossaryGUID,
                                               String                 glossaryTermExternalIdentifier,
                                               Map<String, String>    mappingProperties,
                                               GlossaryTermProperties glossaryTermProperties,
                                               GlossaryTermStatus     initialStatus,
                                               String                 methodName) throws InvalidParameterException,
                                                                                         UserNotAuthorizedException,
                                                                                         PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Create a new metadata element to represent a glossary term using an existing metadata element as a template.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param templateGUID unique identifier of the metadata element to copy
     * @param glossaryGUID unique identifier of the glossary where the glossary term is located.
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param mappingProperties additional properties to help with the mapping of the elements in the
     *                          external asset manager and open metadata
     * @param templateProperties properties that override the template
     * @param methodName calling method
     *
     * @return unique identifier of the new metadata element for the glossary term
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createGlossaryTermFromTemplate(String              userId,
                                                 String              assetManagerGUID,
                                                 String              assetManagerName,
                                                 String              templateGUID,
                                                 String              glossaryGUID,
                                                 String              glossaryTermExternalIdentifier,
                                                 Map<String, String> mappingProperties,
                                                 TemplateProperties  templateProperties,
                                                 String              methodName) throws InvalidParameterException,
                                                                                        UserNotAuthorizedException,
                                                                                        PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Update the properties of the metadata element representing a glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the glossary term to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param glossaryTermProperties new properties for the glossary term
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void updateGlossaryTerm(String                 userId,
                                   String                 assetManagerGUID,
                                   String                 assetManagerName,
                                   String                 glossaryTermGUID,
                                   String                 glossaryTermExternalIdentifier,
                                   GlossaryTermProperties glossaryTermProperties,
                                   String                 methodName) throws InvalidParameterException,
                                                                             UserNotAuthorizedException,
                                                                             PropertyServerException
    {
        // todo
    }


    /**
     * Update the status of the metadata element representing a glossary term.  This is only valid on
     * a controlled glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the glossary term to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param glossaryTermStatus new properties for the glossary term
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void updateGlossaryTermStatus(String             userId,
                                         String             assetManagerGUID,
                                         String             assetManagerName,
                                         String             glossaryTermGUID,
                                         String             glossaryTermExternalIdentifier,
                                         GlossaryTermStatus glossaryTermStatus,
                                         String             methodName) throws InvalidParameterException,
                                                                               UserNotAuthorizedException,
                                                                               PropertyServerException
    {
        // todo
    }


    /**
     * Link a term to a category.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryCategoryGUID unique identifier of the glossary category
     * @param glossaryTermGUID unique identifier of the glossary term
     * @param categorizationProperties properties for the categorization relationship
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setupTermCategory(String                     userId,
                                  String                     assetManagerGUID,
                                  String                     assetManagerName,
                                  String                     glossaryCategoryGUID,
                                  String                     glossaryTermGUID,
                                  GlossaryTermCategorization categorizationProperties,
                                  String                     methodName) throws InvalidParameterException,
                                                                                UserNotAuthorizedException,
                                                                                PropertyServerException
    {
        // todo
    }


    /**
     * Unlink a term from a category.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryCategoryGUID unique identifier of the glossary category
     * @param glossaryTermGUID unique identifier of the glossary term
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermCategory(String userId,
                                  String assetManagerGUID,
                                  String assetManagerName,
                                  String glossaryCategoryGUID,
                                  String glossaryTermGUID,
                                  String methodName) throws InvalidParameterException,
                                                            UserNotAuthorizedException,
                                                            PropertyServerException
    {
        // todo
    }


    /**
     * Link two terms together using a specialist relationship.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param relationshipTypeName name of the type of relationship to create
     * @param glossaryTermOneGUID unique identifier of the glossary term at end 1
     * @param glossaryTermTwoGUID unique identifier of the glossary term at end 2
     * @param relationshipsProperties properties for the categorization relationship
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setupTermRelationship(String                   userId,
                                      String                   assetManagerGUID,
                                      String                   assetManagerName,
                                      String                   relationshipTypeName,
                                      String                   glossaryTermOneGUID,
                                      String                   glossaryTermTwoGUID,
                                      GlossaryTermRelationship relationshipsProperties,
                                      String                   methodName) throws InvalidParameterException,
                                                                                  UserNotAuthorizedException,
                                                                                  PropertyServerException
    {
        // todo
    }


    /**
     * Update the relationship properties for the two terms.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param relationshipTypeName name of the type of relationship to create
     * @param glossaryTermOneGUID unique identifier of the glossary term at end 1
     * @param glossaryTermTwoGUID unique identifier of the glossary term at end 2
     * @param relationshipsProperties properties for the categorization relationship
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void updateTermRelationship(String                   userId,
                                       String                   assetManagerGUID,
                                       String                   assetManagerName,
                                       String                   relationshipTypeName,
                                       String                   glossaryTermOneGUID,
                                       String                   glossaryTermTwoGUID,
                                       GlossaryTermRelationship relationshipsProperties,
                                       String                   methodName) throws InvalidParameterException,
                                                                                   UserNotAuthorizedException,
                                                                                   PropertyServerException
    {
        // todo
    }


    /**
     * Remove the relationship between two terms.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param relationshipTypeName name of the type of relationship to create
     * @param glossaryTermOneGUID unique identifier of the glossary term at end 1
     * @param glossaryTermTwoGUID unique identifier of the glossary term at end 2
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermRelationship(String userId,
                                      String assetManagerGUID,
                                      String assetManagerName,
                                      String relationshipTypeName,
                                      String glossaryTermOneGUID,
                                      String glossaryTermTwoGUID,
                                      String methodName) throws InvalidParameterException,
                                                                UserNotAuthorizedException,
                                                                PropertyServerException
    {
        // todo
    }



    /**
     * Classify the glossary term to indicate that it describes an abstract concept.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setTermAsAbstractConcept(String userId,
                                         String assetManagerGUID,
                                         String assetManagerName,
                                         String glossaryTermGUID,
                                         String glossaryTermExternalIdentifier,
                                         String methodName) throws InvalidParameterException,
                                                                   UserNotAuthorizedException,
                                                                   PropertyServerException
    {
        // todo
    }


    /**
     * Remove the abstract concept designation from the glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermAsAbstractConcept(String userId,
                                           String assetManagerGUID,
                                           String assetManagerName,
                                           String glossaryTermGUID,
                                           String glossaryTermExternalIdentifier,
                                           String methodName) throws InvalidParameterException,
                                                                     UserNotAuthorizedException,
                                                                     PropertyServerException
    {
        // todo
    }


    /**
     * Classify the glossary term to indicate that it describes a data value.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setTermAsDataValue(String userId,
                                   String assetManagerGUID,
                                   String assetManagerName,
                                   String glossaryTermGUID,
                                   String glossaryTermExternalIdentifier,
                                   String methodName) throws InvalidParameterException,
                                                             UserNotAuthorizedException,
                                                             PropertyServerException
    {
        // todo
    }


    /**
     * Remove the data value designation from the glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermAsDataValue(String userId,
                                     String assetManagerGUID,
                                     String assetManagerName,
                                     String glossaryTermGUID,
                                     String glossaryTermExternalIdentifier,
                                     String methodName) throws InvalidParameterException,
                                                               UserNotAuthorizedException,
                                                               PropertyServerException
    {
        // todo
    }


    /**
     * Classify the glossary term to indicate that it describes a data value.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param activityType type of activity
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setTermAsActivity(String                   userId,
                                  String                   assetManagerGUID,
                                  String                   assetManagerName,
                                  String                   glossaryTermGUID,
                                  String                   glossaryTermExternalIdentifier,
                                  GlossaryTermActivityType activityType,
                                  String                   methodName) throws InvalidParameterException,
                                                                              UserNotAuthorizedException,
                                                                              PropertyServerException
    {
        // todo
    }


    /**
     * Remove the activity designation from the glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermAsActivity(String userId,
                                    String assetManagerGUID,
                                    String assetManagerName,
                                    String glossaryTermGUID,
                                    String glossaryTermExternalIdentifier,
                                    String methodName) throws InvalidParameterException,
                                                              UserNotAuthorizedException,
                                                              PropertyServerException
    {
        // todo
    }


    /**
     * Classify the glossary term to indicate that it describes a context.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param contextDefinition more details of the context
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setTermAsContext(String                        userId,
                                 String                        assetManagerGUID,
                                 String                        assetManagerName,
                                 String                        glossaryTermGUID,
                                 String                        glossaryTermExternalIdentifier,
                                 GlossaryTermContextDefinition contextDefinition,
                                 String                        methodName) throws InvalidParameterException,
                                                                                  UserNotAuthorizedException,
                                                                                  PropertyServerException
    {
        // todo
    }


    /**
     * Remove the context definition designation from the glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermAsContext(String userId,
                                   String assetManagerGUID,
                                   String assetManagerName,
                                   String glossaryTermGUID,
                                   String glossaryTermExternalIdentifier,
                                   String methodName) throws InvalidParameterException,
                                                             UserNotAuthorizedException,
                                                             PropertyServerException
    {
        // todo
    }


    /**
     * Classify the glossary term to indicate that it describes a spine object.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setTermAsSpineObject(String userId,
                                     String assetManagerGUID,
                                     String assetManagerName,
                                     String glossaryTermGUID,
                                     String glossaryTermExternalIdentifier,
                                     String methodName) throws InvalidParameterException,
                                                               UserNotAuthorizedException,
                                                               PropertyServerException
    {
        // todo
    }


    /**
     * Remove the spine object designation from the glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermAsSpineObject(String userId,
                                       String assetManagerGUID,
                                       String assetManagerName,
                                       String glossaryTermGUID,
                                       String glossaryTermExternalIdentifier,
                                       String methodName) throws InvalidParameterException,
                                                                 UserNotAuthorizedException,
                                                                 PropertyServerException
    {
        // todo
    }



    /**
     * Classify the glossary term to indicate that it describes a spine attribute.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setTermAsSpineAttribute(String userId,
                                        String assetManagerGUID,
                                        String assetManagerName,
                                        String glossaryTermGUID,
                                        String glossaryTermExternalIdentifier,
                                        String methodName) throws InvalidParameterException,
                                                                  UserNotAuthorizedException,
                                                                  PropertyServerException
    {
        // todo
    }


    /**
     * Remove the spine attribute designation from the glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermAsSpineAttribute(String userId,
                                          String assetManagerGUID,
                                          String assetManagerName,
                                          String glossaryTermGUID,
                                          String glossaryTermExternalIdentifier,
                                          String methodName) throws InvalidParameterException,
                                                                    UserNotAuthorizedException,
                                                                    PropertyServerException
    {
        // todo
    }


    /**
     * Classify the glossary term to indicate that it describes an object identifier.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void setTermAsObjectIdentifier(String userId,
                                          String assetManagerGUID,
                                          String assetManagerName,
                                          String glossaryTermGUID,
                                          String glossaryTermExternalIdentifier,
                                          String methodName) throws InvalidParameterException,
                                                                    UserNotAuthorizedException,
                                                                    PropertyServerException
    {
        // todo
    }


    /**
     * Remove the object identifier designation from the glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to update
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void clearTermAsObjectIdentifier(String userId,
                                            String assetManagerGUID,
                                            String assetManagerName,
                                            String glossaryTermGUID,
                                            String glossaryTermExternalIdentifier,
                                            String methodName) throws InvalidParameterException,
                                                                      UserNotAuthorizedException,
                                                                      PropertyServerException
    {
        // todo
    }


    /**
     * Remove the metadata element representing a glossary term.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryTermGUID unique identifier of the metadata element to remove
     * @param glossaryTermExternalIdentifier unique identifier of the glossary term in the external asset manager
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void removeGlossaryTerm(String userId,
                                   String assetManagerGUID,
                                   String assetManagerName,
                                   String glossaryTermGUID,
                                   String glossaryTermExternalIdentifier,
                                   String methodName) throws InvalidParameterException,
                                                             UserNotAuthorizedException,
                                                             PropertyServerException
    {
        // todo
    }


    /**
     * Retrieve the list of glossary term metadata elements that contain the search string.
     * The search string is treated as a regular expression.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param searchString string to find in the properties
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryTermElement>   findGlossaryTerms(String userId,
                                                         String assetManagerGUID,
                                                         String assetManagerName,
                                                         String searchString,
                                                         int    startFrom,
                                                         int    pageSize,
                                                         String methodName) throws InvalidParameterException,
                                                                                   UserNotAuthorizedException,
                                                                                   PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Retrieve the list of glossary terms associated with a glossary category.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param glossaryCategoryGUID unique identifier of the glossary category of interest
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of associated metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryTermElement>    getTermsForGlossaryCategory(String userId,
                                                                    String assetManagerGUID,
                                                                    String assetManagerName,
                                                                    String glossaryCategoryGUID,
                                                                    int    startFrom,
                                                                    int    pageSize,
                                                                    String methodName) throws InvalidParameterException,
                                                                                              UserNotAuthorizedException,
                                                                                              PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Retrieve the list of glossary term metadata elements with a matching qualified or display name.
     * There are no wildcards supported on this request.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param name name to search for
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryTermElement>   getGlossaryTermsByName(String userId,
                                                              String assetManagerGUID,
                                                              String assetManagerName,
                                                              String name,
                                                              int    startFrom,
                                                              int    pageSize,
                                                              String methodName) throws InvalidParameterException,
                                                                                        UserNotAuthorizedException,
                                                                                        PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Retrieve the glossary term metadata element with the supplied unique identifier.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param guid unique identifier of the requested metadata element
     * @param methodName calling method
     *
     * @return matching metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public GlossaryTermElement getGlossaryTermByGUID(String userId,
                                                     String guid,
                                                     String assetManagerGUID,
                                                     String assetManagerName,
                                                     String methodName) throws InvalidParameterException,
                                                                               UserNotAuthorizedException,
                                                                               PropertyServerException
    {
        // todo
        return null;
    }


    /* =========================================================================================
     * Support for linkage to external glossary resources.  These glossary resources are not
     * stored as metadata - they could be web pages, ontologies or some other format.
     * It is possible that the external glossary resource may have been generated by the metadata
     * representation or vice versa.
     */


    /**
     * Create a link to an external glossary resource.  This is associated with a glossary to show that they have equivalent content.
     * It is possible that this resource was generated from the glossary content or was the source for it.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param linkProperties properties of the link
     * @param methodName calling method
     *
     * @return unique identifier of the external reference
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createExternalGlossaryLink(String                         userId,
                                             String                         assetManagerGUID,
                                             String                         assetManagerName,
                                             ExternalGlossaryLinkProperties linkProperties,
                                             String                         methodName) throws InvalidParameterException,
                                                                                               UserNotAuthorizedException,
                                                                                               PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Update the properties of a reference to an external glossary resource.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param externalLinkGUID unique identifier of the external reference
     * @param linkProperties properties of the link
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void updateExternalGlossaryLink(String                         userId,
                                           String                         assetManagerGUID,
                                           String                         assetManagerName,
                                           String                         externalLinkGUID,
                                           ExternalGlossaryLinkProperties linkProperties,
                                           String                         methodName) throws InvalidParameterException,
                                                                                             UserNotAuthorizedException,
                                                                                             PropertyServerException
    {
        // todo
    }


    /**
     * Remove information about a link to an external glossary resource (and the relationships that attached it to the glossaries).
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param externalLinkGUID unique identifier of the external reference
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void removeExternalGlossaryLink(String userId,
                                           String assetManagerGUID,
                                           String assetManagerName,
                                           String externalLinkGUID,
                                           String methodName) throws InvalidParameterException,
                                                                     UserNotAuthorizedException,
                                                                     PropertyServerException
    {
        // todo
    }


    /**
     * Connect a glossary to a reference to an external glossary resource.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param externalLinkGUID unique identifier of the external reference
     * @param glossaryGUID unique identifier of the metadata element to attach
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void attachExternalLinkToGlossary(String userId,
                                             String assetManagerGUID,
                                             String assetManagerName,
                                             String externalLinkGUID,
                                             String glossaryGUID,
                                             String methodName) throws InvalidParameterException,
                                                                       UserNotAuthorizedException,
                                                                       PropertyServerException
    {
        // todo
    }


    /**
     * Disconnect a glossary from a reference to an external glossary resource.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param externalLinkGUID unique identifier of the external reference
     * @param glossaryGUID unique identifier of the metadata element to remove
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void detachExternalLinkFromGlossary(String userId,
                                               String assetManagerGUID,
                                               String assetManagerName,
                                               String externalLinkGUID,
                                               String glossaryGUID,
                                               String methodName) throws InvalidParameterException,
                                                                         UserNotAuthorizedException,
                                                                         PropertyServerException
    {
        // todo
    }


    /**
     * Retrieve the list of links to external glossary resources attached to a glossary.
     *
     * @param userId calling user
     * @param glossaryGUID unique identifier of the metadata element for the glossary of interest
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of attached links to external glossary resources
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<ExternalGlossaryLinkElement> getExternalLinksForGlossary(String userId,
                                                                         String glossaryGUID,
                                                                         int    startFrom,
                                                                         int    pageSize,
                                                                         String methodName) throws InvalidParameterException,
                                                                                                   UserNotAuthorizedException,
                                                                                                   PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Return the glossaries connected to an external glossary source.
     *
     * @param userId calling user
     * @param externalLinkGUID unique identifier of the metadata element for the external glossary link of interest
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of glossaries
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<GlossaryElement> getGlossariesForExternalLink(String userId,
                                                              String externalLinkGUID,
                                                              int    startFrom,
                                                              int    pageSize,
                                                              String methodName) throws InvalidParameterException,
                                                                                        UserNotAuthorizedException,
                                                                                        PropertyServerException
    {
        // todo
        return null;
    }


    /**
     * Create a link to an external glossary category resource.  This is associated with a category to show that they have equivalent content.
     * It is possible that this resource was generated from the glossary content or was the source for it.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param externalLinkGUID unique identifier of the external reference
     * @param glossaryCategoryGUID unique identifier for the the glossary category
     * @param linkProperties properties of the link
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void attachExternalCategoryLink(String                                userId,
                                           String                                assetManagerGUID,
                                           String                                assetManagerName,
                                           String                                externalLinkGUID,
                                           String                                glossaryCategoryGUID,
                                           ExternalGlossaryElementLinkProperties linkProperties,
                                           String                                methodName) throws InvalidParameterException,
                                                                                                    UserNotAuthorizedException,
                                                                                                    PropertyServerException
    {
        // todo
    }


    /**
     * Remove the link to an external glossary category resource.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param externalLinkGUID unique identifier of the external reference
     * @param glossaryCategoryGUID unique identifier for the the glossary category
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void detachExternalCategoryLink(String userId,
                                           String assetManagerGUID,
                                           String assetManagerName,
                                           String externalLinkGUID,
                                           String glossaryCategoryGUID,
                                           String methodName) throws InvalidParameterException,
                                                                     UserNotAuthorizedException,
                                                                     PropertyServerException
    {
        // todo
    }


    /**
     * Create a link to an external glossary term resource.  This is associated with a term to show that they have equivalent content.
     * It is possible that this resource was generated from the glossary content or was the source for it.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param externalLinkGUID unique identifier of the external reference
     * @param glossaryTermGUID unique identifier for the the glossary category
     * @param linkProperties properties of the link
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void attachExternalTermLink(String                                userId,
                                       String                                assetManagerGUID,
                                       String                                assetManagerName,
                                       String                                externalLinkGUID,
                                       String                                glossaryTermGUID,
                                       ExternalGlossaryElementLinkProperties linkProperties,
                                       String                                methodName) throws InvalidParameterException,
                                                                                                UserNotAuthorizedException,
                                                                                                PropertyServerException
    {
        // todo
    }


    /**
     * Remove the link to an external glossary term resource.
     *
     * @param userId calling user
     * @param assetManagerGUID unique identifier of software server capability representing the caller
     * @param assetManagerName unique name of software server capability representing the caller
     * @param externalLinkGUID unique identifier of the external reference
     * @param glossaryTermGUID unique identifier for the the glossary category
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void detachExternalTermLink(String userId,
                                       String assetManagerGUID,
                                       String assetManagerName,
                                       String externalLinkGUID,
                                       String glossaryTermGUID,
                                       String methodName) throws InvalidParameterException,
                                                                 UserNotAuthorizedException,
                                                                 PropertyServerException
    {
        // todo
    }
}
