/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.ee.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.ow2.choreos.chors.EnactmentEngine;
import org.ow2.choreos.chors.ChoreographyNotFoundException;
import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.EEImpl;

/**
 * Enactment Engine REST API. Resource: chors (choreographies).
 * 
 * @author leonardo
 * 
 */
@Path("chors")
public class ChorResource {

    private EnactmentEngine chorDeployer = new EEImpl();

    /**
     * POST /chors
     * 
     * Body: a choreography specification Creates a new choreography that still
     * have to be enacted (POST /chors/{chorID}/enactment).
     * 
     * @param uriInfo
     *            provided by the REST framework
     * @return HTTP code 201 (CREATED) Location header: the just created
     *         choreography URI, containing the choreography ID. HTTP code 400
     *         (BAD_REQUEST) if the chorSpec is not properly provided in the
     *         request body
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response create(ChoreographySpec chor, @Context UriInfo uriInfo) {

        if (chor == null || chor.getDeployableServiceSpecs() == null || chor.getDeployableServiceSpecs().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        String chorId = chorDeployer.createChoreography(chor);

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        uriBuilder = uriBuilder.path(ChorResource.class).path(chorId);
        URI location = uriBuilder.build();

        return Response.created(location).build();
    }

    /**
     * GET /chors/{chorID}
     * 
     * Body: empty Retrieve the choreography information
     * 
     * @param chorId
     *            the choreography id provided in the URI
     * @param uriInfo
     *            provided by the REST framework
     * @return HTTP code 200 (OK). Location header: the choreography URI. Body
     *         response: the Choreography representation HTTP code 400
     *         (BAD_REQUEST) if chorId is not properly provided HTTP code 404
     *         (NOT_FOUND) if choreography does not exist
     */
    @GET
    @Path("{chorID}")
    @Produces(MediaType.APPLICATION_XML)
    public Response get(@PathParam("chorID") String chorId, @Context UriInfo uriInfo) {

        if (chorId == null || chorId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Choreography chor;
        try {
            chor = chorDeployer.getChoreography(chorId);
        } catch (ChoreographyNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        uriBuilder = uriBuilder.path(ChorResource.class).path(chorId);
        URI location = uriBuilder.build();

        return Response.ok(chor).location(location).build();
    }

    /**
     * POST /chors/{chorID}/enactment
     * 
     * Body: empty Enacts a choreography
     * 
     * @param chorId
     *            the choreography id is provided in the URI and the
     *            choreography must be already configured
     * @param uriInfo
     *            provided by the REST framework
     * @return HTTP code 200 (OK). Location header: choreography deployed
     *         services URI. Body response: a Collection of Service representing
     *         deployed services. HTTP code 400 (BAS_REQUEST) if chorId is not
     *         properly provided. HTTP code 404 (NOT_FOUND) if there is no
     *         choreography with id == chorID.
     */
    @POST
    @Path("{chorID}/deployment")
    @Produces(MediaType.APPLICATION_XML)
    public Response deploy(@PathParam("chorID") String chorId, @Context UriInfo uriInfo) {

        if (chorId == null || chorId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        uriBuilder = uriBuilder.path(ChorResource.class).path(chorId);
        URI location = uriBuilder.build();

        Choreography chor;
        try {
            chor = chorDeployer.deployChoreography(chorId);
        } catch (DeploymentException e) {
            return Response.serverError().build();
        } catch (ChoreographyNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(chor).location(location).build();
    }

    @PUT
    @Path("{chorID}/update")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Response update(@PathParam("chorID") String chorId, ChoreographySpec spec, @Context UriInfo uriInfo) {

        if (chorId == null || chorId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        uriBuilder = uriBuilder.path(ChorResource.class).path(chorId);
        URI location = uriBuilder.build();

        Choreography chor;
        try {
            chorDeployer.updateChoreography(chorId, spec);
            chor = chorDeployer.getChoreography(chorId);
        } catch (DeploymentException e) {
            return Response.serverError().build();
        } catch (ChoreographyNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(chor).location(location).build();
    }

    @PUT
    @Path("cleanup")
    @Produces(MediaType.APPLICATION_XML)
    public Response cleanUp(@Context UriInfo uriInfo) {
        return null;
    }
}
