/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.repository.jaxrs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.*;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.indy.service.repository.controller.AdminController;
import org.commonjava.indy.service.repository.data.ArtifactStoreValidateData;
import org.commonjava.indy.service.repository.data.StoreValidator;
import org.commonjava.indy.service.repository.exception.IndyDataException;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.commonjava.indy.service.repository.model.dto.StoreListingDTO;
import org.commonjava.indy.service.repository.util.jackson.MapperUtil;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.commonjava.indy.service.repository.model.ArtifactStore.*;

@Api(description = "Resource for accessing and managing artifact store definitions", value = "Store Administration")
@Path("/api/stores/query")
@ApplicationScoped
public class RepositoryQueryResources {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private AdminController adminController;

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private SecurityManager securityManager;

	@Inject
	StoreValidator storeValidator;

	@Inject
	private ResponseHelper responseHelper;

	public RepositoryQueryResources() {
		logger.info("\n\n\n\nStarted StoreAdminHandler\n\n\n\n");
	}

	@ApiOperation("Retrieve the definitions of all artifact stores")
	@ApiResponses({ @ApiResponse(code = 200, response = StoreListingDTO.class, message = "The store definitions"), })
	@Path("/all")
	@GET
	@Produces(APPLICATION_JSON)
	public Response getAll() {

		Response response;
		try {
			final List<ArtifactStore> stores = adminController.getAllStores();

			logger.info("Returning listing containing stores:\n\t{}", new JoinString("\n\t", stores));

			final StoreListingDTO<ArtifactStore> dto = new StoreListingDTO<>(stores);

			response = responseHelper.formatOkResponseWithJsonEntity(dto);
		} catch (final IndyWorkflowException e) {
			logger.error(e.getMessage(), e);
			response = responseHelper.formatResponse(e);
		}

		return response;
	}

	@ApiOperation("Retrieve the definition of a specific artifact store")
	@ApiResponses({ @ApiResponse(code = 200, response = ArtifactStore.class, message = "The store definition"),
		@ApiResponse(code = 404, message = "The store doesn't exist"), })
	@Path("/{name}")
	@GET
	@Produces(APPLICATION_JSON)
	public Response get(final @PathParam("packageType") String packageType,
	                    final @ApiParam(allowableValues = "hosted,group,remote", required = true) @PathParam("type")
		                    String type, final @ApiParam(required = true) @PathParam("name") String name) {
		logger.info("{}:{}:{}", packageType, type, name);
		final StoreType st = StoreType.get(type);
		final StoreKey key = new StoreKey(packageType, st, name);

		Response response;
		try {
			final ArtifactStore store = adminController.get(key);
			logger.info("Returning repository: {}", store);

			if (store == null) {
				response = Response.status(Status.NOT_FOUND).build();
			} else {
				response = responseHelper.formatOkResponseWithJsonEntity(store);
			}
		} catch (final IndyWorkflowException e) {
			logger.error(e.getMessage(), e);
			response = responseHelper.formatResponse(e);
		}
		return response;
	}

	@ApiOperation("Delete an artifact store")
	@ApiResponses({
		@ApiResponse(code = 204, response = ArtifactStore.class, message = "The store was deleted (or didn't exist in the first place)"), })
	@Path("/{name}")
	@DELETE
	public Response delete(final @PathParam("packageType") String packageType,
	                       final @ApiParam(allowableValues = "hosted,group,remote", required = true) @PathParam("type")
		                       String type, final @ApiParam(required = true) @PathParam("name") String name,
	                       final @QueryParam("deleteContent") boolean deleteContent, @Context final HttpRequest request,
	                       final @Context SecurityContext securityContext) {
		final StoreType st = StoreType.get(type);
		final StoreKey key = new StoreKey(packageType, st, name);

		logger.info("Deleting: {}, deleteContent: {}", key, deleteContent);
		Response response;
		try {
			String summary = null;
			try {
				summary = IOUtils.toString(request.getInputStream(), Charset.defaultCharset());
			} catch (final IOException e) {
				// no problem, try to get the summary from a header instead.
				logger.info("store-deletion change summary not in request body, checking headers.");
			}

			if (isEmpty(summary)) {
				summary = request.getHttpHeaders().getHeaderString(METADATA_CHANGELOG);
			}

			if (isEmpty(summary)) {
				summary = "Changelog not provided";
			}
			summary += (", deleteContent:" + deleteContent);

			String user = securityManager.getUser(securityContext, request);

			adminController.delete(key, user, summary, deleteContent);

			response = noContent().build();
		} catch (final IndyWorkflowException e) {
			logger.error(e.getMessage(), e);
			response = responseHelper.formatResponse(e);
		}
		return response;
	}

	@ApiOperation("Retrieve the definition of a remote by specific url")
	@ApiResponses({ @ApiResponse(code = 200, response = ArtifactStore.class, message = "The store definition"),
		@ApiResponse(code = 404, message = "The remote repository doesn't exist"), })
	@Path("/query/byUrl")
	@GET
	public Response getRemoteByUrl(final @PathParam("packageType") String packageType,
	                               final @ApiParam(allowableValues = "remote", required = true) @PathParam("type") String type,
	                               final @QueryParam("url") String url, @Context final HttpRequest request,
	                               final @Context SecurityContext securityContext) {
		if (!"remote".equals(type)) {
			return responseHelper.formatBadRequestResponse(String.format("Not supporte repository type of %s", type));
		}

		logger.info("Get remote repository by url: {}", url);
		Response response;
		try {
			final List<RemoteRepository> remotes = adminController.getRemoteByUrl(url, packageType);
			logger.info("According to url {}, Returning remote listing remote repositories: {}", url, remotes);

			if (remotes == null || remotes.isEmpty()) {
				response = Response.status(Status.NOT_FOUND).build();
			} else {
				final StoreListingDTO<RemoteRepository> dto = new StoreListingDTO<>(remotes);
				response = responseHelper.formatOkResponseWithJsonEntity(dto);
			}
		} catch (final IndyWorkflowException e) {
			logger.error(e.getMessage(), e);
			response = responseHelper.formatResponse(e);
		}
		return response;
	}

	@ApiOperation("Revalidation of Artifacts Stored on demand")
	@ApiResponses({
		@ApiResponse(code = 200, response = ArtifactStore.class, message = "Revalidation for Remote Repositories was successfull"),
		@ApiResponse(code = 404, message = "Revalidation is not successfull"), })
	@Path("/revalidate/all/")
	@POST
	public Response revalidateArtifactStores(@PathParam("packageType") String packageType, @PathParam("type") String type) {

		ArtifactStoreValidateData result;
		Map<String, ArtifactStoreValidateData> results = new HashMap<>();
		Response response;

		try {
			StoreType storeType = StoreType.get(type);

			List<ArtifactStore> allArtifactStores = adminController.getAllOfType(packageType, storeType);

			for (ArtifactStore artifactStore : allArtifactStores) {
				// Validate this Store
				result = adminController.validateStore(artifactStore);
				results.put(artifactStore.getKey().toString(), result);
			}
			response = responseHelper.formatOkResponseWithJsonEntity(results);

		} catch (IndyDataException ide) {
			logger.warn("=> [IndyDataException] exception message: " + ide.getMessage());
			response = responseHelper.formatResponse(ide);

		} catch (MalformedURLException mue) {
			logger.warn("=> [MalformedURLException] Invalid URL exception message: " + mue.getMessage());
			response = responseHelper.formatResponse(mue);

		} catch (IndyWorkflowException iwe) {
			logger.warn("=> [IndyWorkflowException] exception message: " + iwe.getMessage());
			response = responseHelper.formatResponse(iwe);

		}
		return response;
	}

	@ApiOperation("Revalidation of Artifact Stored on demand based on package, type and name")
	@ApiResponses({
		@ApiResponse(code = 200, response = ArtifactStore.class, message = "Revalidation for Remote Repository was successfull"),
		@ApiResponse(code = 404, message = "Revalidation is not successfull"), })
	@Path("/{name}/revalidate")
	@POST
	public Response revalidateArtifactStore(final @PathParam("packageType") String packageType,
	                                        final @ApiParam(allowableValues = "hosted,group,remote", required = true)
	                                        @PathParam("type") String type,
	                                        final @ApiParam(required = true) @PathParam("name") String name) {

		ArtifactStoreValidateData result;

		Response response;

		try {
			final StoreType st = StoreType.get(type);
			final StoreKey key = new StoreKey(packageType, st, name);
			final ArtifactStore store = adminController.get(key);
			logger.info("=> Returning repository: {}", store);

			// Validate this Store
			result = adminController.validateStore(store);

			logger.warn("=> Result from Validating Store: " + result);
			if (result == null) {
				response = Response.status(Status.NOT_FOUND).build();
			} else {
				response = responseHelper.formatOkResponseWithJsonEntity(result);
			}

		} catch (IndyDataException ide) {
			logger.warn("=> [IndyDataException] exception message: " + ide.getMessage());
			response = responseHelper.formatResponse(ide);

		} catch (MalformedURLException mue) {
			logger.warn("=> [MalformedURLException] Invalid URL exception message: " + mue.getMessage());
			response = responseHelper.formatResponse(mue);

		} catch (IndyWorkflowException iwe) {
			logger.warn("=> [IndyWorkflowException] exception message: " + iwe.getMessage());
			response = responseHelper.formatResponse(iwe);
		}

		return response;
	}

	@ApiOperation("Return All Invalidated Remote Repositories")
	@ApiResponses({ @ApiResponse(code = 200, message = "Return All Invalidated Remote Repositories") })
	@Path("/all_invalid")
	@GET
	public Response returnDisabledStores(final @ApiParam(required = true) @PathParam("packageType") String packageType,
	                                     final @ApiParam(allowableValues = "remote", required = true) @PathParam("type")
		                                     String type) {
		if (!"remote".equals(type)) {
			return responseHelper.formatBadRequestResponse(String.format("Not supporte repository type of %s", type));
		}
		return responseHelper.formatOkResponseWithJsonEntity(adminController.getDisabledRemoteRepositories());
	}

}