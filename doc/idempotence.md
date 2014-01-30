HOW CHOREOGRAPHY DEPLOYMENT WORKS (internals)
=================================

Leonardo Leite, 30/01/14

At ServicesDeployer:
ChorDiffer(chor) returns { new , to update, not modified } services

* new services       -> NewDeploymentPreparing    -> NodesUpdater
* services to update -> UpdateDeploymentPreparing -> NodesUpdater

*Prepare* means to prepare Chef cookbooks and the node.json. 
*Updating* a node just runs "chef-solo".

* NewDeploymentPreparing : for each service -> create service -> ServiceDeploymentPreparer
* UpdateDeploymentPreparing : for each service -> ServiceUpdater ->
  for each UpdateAction retrieved from UpdateActionFactory(service) -> action.aplyUpdate()

Existing update actions:
 
* IncreaseNumberOfReplicas -> ServiceDeploymentPreparer(deltaSpec, service) -> service.selectedNodes.add(new nodes)
* DecreaseNumberOfReplicas -> ServiceUndeploymentPreparer(service, decreaseAmmount)
* Migrate: not implemented
* UpdateVersion: not implemented

IDEMPOTENCE
===========

When performing the enactment operation of a choreography already deployed,
services not previously deployed must be deployed.
I.e. the after-first deployment must recover from errors on previous deployment.

Therefore, we must consider that were problems in each deployment phase, that are:

* Preparing service
* Updating node
* Binding services

Each one has a correspondent implementation to ensure idempotence.

Let's check each one of them.

FAIL ON PREPARING SERVICES
--------------------------

**Consequences**: serviceSpec.numberOfInstances != service.selectedNodes.len.

**Assumption**: if service prepare fails, the node is not added to service.selectedNodes.

**Handling**:

* at ServicesDeployer: not modified -> NotModifiedDeploymentPreparing -> NodesUpdater
* NotModifiedDeploymentPreparing -> ServiceDeploymentPreparer

**Obs**: currently ServiceDeploymentPreparer already receives service spec and service.
So we need just one modification. Rather then creating spec.getNumberOfInstances() instances
of InstanceDeploymentPreparer, create spec.getNumberOfInstances() - service.instances.len of them.

**TODO**: IncreaseNumberOfInstances will must pass newSpec rather than deltaSpec to ServiceDeploymentPreparer.

FAIL ON UPDATE NODES
--------------------

**Consequences**: serviceSpec.numberOfInstances == service.selectedNodes.len != service.instances.len.

**Assumption**: if a node update fails, the correspondent instance is not added to service.instances.

**Handling**: at ServicesDeployer it is enough to run NodesUpdater for all selected nodes of all services,
including not modified services.

FAIL ON BINDING SERVICES
-----------------------

**Consequences**: there are no visible consequences to EE.

**Handling**: just cast all the dependencies again (already implemented).

**Obs**: the setInvocationAddress already has an idempotent design,
since each call describes all the possible endpoints for a single partner.



