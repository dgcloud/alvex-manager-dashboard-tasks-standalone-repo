/**
 * Copyright © 2012 ITD Systems
 *
 * This file is part of Alvex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.alvexcore.repo;

import com.alvexcore.repo.tools.ClasspathURLStreamHandlerFactory;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.util.ArrayList;
import java.util.List;

public class RepositoryExtensionRegistry extends AbstractLifecycleBean implements TenantDeployer, InitializingBean {

	public static final String ALVEX_GLOBAL_KEY_VALUE_STORE = "ALVEX";

	private static Log logger = LogFactory.getLog(RepositoryExtensionRegistry.class);
	
	final static QName[] ALVEX_PATH = { AlvexContentModel.ASSOC_NAME_SYSTEM,
			AlvexContentModel.ASSOC_NAME_ALVEX };
	
	private Repository repository = null;
	private ServiceRegistry serviceRegistry = null;
	private TenantService tenantService = null;
	private TenantAdminService tenantAdminService = null;

	public RepositoryExtensionRegistry() throws IllegalAccessException, NoSuchFieldException {
		ClasspathURLStreamHandlerFactory.inject();
	}

	// list of extensions
	protected List<RepositoryExtension> extensions = new ArrayList<RepositoryExtension>();

	public Repository getRepository() {
		return repository;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	@Required
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	@Required
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}


	public void initExtensions() throws Exception {
		AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				initContainer();
				for (RepositoryExtension ext : extensions)
					ext.init(false);	
				return null;
			}
		});
	}

	protected void dropExtensionsCache() {
		for (RepositoryExtension ext: extensions)
			ext.dropNodeCache();
	}

	private NodeRef initContainer() throws Exception {
		NodeRef node = createPath(ALVEX_PATH, null, null);
		NodeService nodeService = serviceRegistry.getNodeService();
		nodeService.addAspect(node, ContentModel.ASPECT_AUDITABLE, null);
		return node;
	}

	// creates containers specified by assocs
	public NodeRef createPath(QName[] path, QName[] assocs, QName[] types)
			throws Exception {
		if (path == null || path.length == 0)
			throw new Exception("Path cannot be null or empty");
		if (assocs != null && path.length != assocs.length)
			throw new Exception("Size of path and assocs must be equal");
		if (types != null && path.length != types.length)
			throw new Exception("Size of path and types must be equal");
		NodeService nodeService = serviceRegistry.getNodeService();
		NodeRef node = nodeService
				.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		for (int i = 0; i < path.length; i++) {
			QName assocQName = path[i];
			QName childAssoc = assocs == null ? ContentModel.ASSOC_CHILDREN
					: assocs[i];
			QName childType = types == null ? ContentModel.TYPE_CONTAINER
					: types[i];
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
					node, childAssoc, assocQName);
			if (childAssocs.size() == 0) {
				// create new node
				node = nodeService.createNode(node, childAssoc, assocQName,
						childType).getChildRef();
			} else if (childAssocs.size() == 1)
				// get ref of existent node
				node = childAssocs.get(0).getChildRef();
		}
		return node;
	}

	// resolved container specified by assocs
	public NodeRef resolvePath(QName[] path, QName[] assocs) {
		if (path == null || path.length == 0)
			throw new AlfrescoRuntimeException("Path cannot be null or empty");
		if (assocs != null && path.length != assocs.length)
			throw new AlfrescoRuntimeException("Size of path and assocs must be equal");
		NodeService nodeService = serviceRegistry.getNodeService();
		NodeRef node = repository.getRootHome();
		
		for (int i = 0; i < path.length; i++) {
			QName assocQName = path[i];
			QName childAssoc = assocs == null ? ContentModel.ASSOC_CHILDREN
					: assocs[i];
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
					node, childAssoc, assocQName);
			if (childAssocs.size() == 0) {
				return null;
			} else if (childAssocs.size() == 1)
				// get ref of existent node
				node = childAssocs.get(0).getChildRef();
		}
		return node;		
	}

	public String getSystemId() {
		return repository.getCompanyHome().getId();
	}

	public int getServerCores() {
		return Runtime.getRuntime().availableProcessors();
	}

	// TODO: do not count disabled users
	public long getRegisteredUsers() {
		long users = AuthenticationUtil.runAsSystem(new RunAsWork<Long>() {
			@Override
			public Long doWork() throws Exception {
				return new Long (serviceRegistry.getAuthorityService().countUsers());
			}
		});
		return users - 3; /* '-3' stands for admin, guest, System */
	}

	// registers new extension
	public void registerExtension(RepositoryExtension extension)
			throws Exception {
		for (RepositoryExtension ext : extensions)
			if (ext.getId().equals(extension.getId()))
				throw new Exception("Extension " + ext.getId()
						+ " is registered already");
		// register if it's not registered yet
		extensions.add(extension);
	}

	// returns a list of installed extensions
	public List<RepositoryExtension> getInstalledExtensions() {
		return extensions;
	}

	@Override
	protected void onBootstrap(ApplicationEvent event) {
		try {
			initExtensions();
		} catch (Exception e) {
			throw new AlfrescoRuntimeException("Alvex initialization failed", e);
		}
	}

	@Override
	protected void onShutdown(ApplicationEvent event) {
	}
	
	public RepositoryExtension getExtension(String id) throws Exception{
		for (RepositoryExtension ex: extensions)
			if (ex.getId().equals(id))
				return ex;
		throw new Exception("Extension not found");
	}

	public TenantService getTenantService() {
		return tenantService;
	}

	@Required
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	@Override
	public void onEnableTenant() {
		try {
			initExtensions();
		} catch (Exception e) {
			throw new AlfrescoRuntimeException("Alvex init for tenant failed", e);
		}
		
	}

	@Override
	public void onDisableTenant() {
		dropExtensionsCache();
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init() {
	}

	public TenantAdminService getTenantAdminService() {
		return tenantAdminService;
	}

	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		tenantAdminService.register(this);		
	}
}
