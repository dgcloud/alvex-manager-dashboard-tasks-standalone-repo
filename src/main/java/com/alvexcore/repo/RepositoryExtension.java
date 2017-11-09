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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public abstract class RepositoryExtension implements InitializingBean {

	private static final String ID_DATA_PATH = "dataPath";
	protected QName[] CONFIG_PATH = new QName[4];
	protected QName[] CONFIG_TYPES = new QName[4];
	protected QName[] DATA_PATH = new QName[4];
	protected QName[] DATA_TYPES = new QName[4];

	protected ServiceRegistry serviceRegistry;
	protected RepositoryExtensionRegistry extensionRegistry;
	protected String id = null;
	protected String version = null;
	protected String edition = null;
	protected MessageDigest md5;
	protected String fileListPath = null;
	protected String extInfoPath = null;

	private Map<String, Map<String, NodeRef>> nodeCache = new HashMap<String, Map<String, NodeRef>>();

	// constructor
	public RepositoryExtension() throws Exception {

		CONFIG_PATH[0] = AlvexContentModel.ASSOC_NAME_SYSTEM;
		CONFIG_PATH[1] = AlvexContentModel.ASSOC_NAME_ALVEX;
		CONFIG_PATH[2] = AlvexContentModel.ASSOC_NAME_CONFIG;
		
		DATA_PATH[0] = AlvexContentModel.ASSOC_NAME_SYSTEM;
		DATA_PATH[1] = AlvexContentModel.ASSOC_NAME_ALVEX;
		DATA_PATH[2] = AlvexContentModel.ASSOC_NAME_DATA;
		
		CONFIG_TYPES[0] = ContentModel.TYPE_CONTAINER;
		CONFIG_TYPES[1] = ContentModel.TYPE_CONTAINER;
		CONFIG_TYPES[2] = ContentModel.TYPE_CONTAINER;
		CONFIG_TYPES[3] = AlvexContentModel.TYPE_EXTENSION_CONFIG;
		
		DATA_TYPES[0] = ContentModel.TYPE_CONTAINER;
		DATA_TYPES[1] = ContentModel.TYPE_CONTAINER;
		DATA_TYPES[2] = ContentModel.TYPE_CONTAINER;
		DATA_TYPES[3] = ContentModel.TYPE_CONTAINER;
	}

	// dependency injection
	@Override
	public void afterPropertiesSet() throws Exception {
		serviceRegistry = extensionRegistry.getServiceRegistry();
		extensionRegistry.registerExtension(this);
	}

	// returns extension id
	public String getId() {
		return id;
	}


	public void init(boolean failIfInitialized) throws Exception {
		DATA_PATH[3] = CONFIG_PATH[3] = QName.createQName(AlvexContentModel.ALVEX_MODEL_URI, id);
		
		if (isInitialized() && failIfInitialized)
			throw new Exception("Extension has been initialized already");

		// create data folder if needed
		NodeRef dataPath = extensionRegistry.resolvePath(DATA_PATH, null);
		if (dataPath == null)
			dataPath = extensionRegistry.createPath(DATA_PATH, null, DATA_TYPES);
		addNodeToCache(ID_DATA_PATH, dataPath);
	}

	public void drop(boolean all) throws Exception {
		NodeRef ref = extensionRegistry.resolvePath(DATA_PATH, null);
		if (ref != null)
			serviceRegistry.getNodeService().deleteNode(ref);
		if (all) {
			ref = extensionRegistry.resolvePath(CONFIG_PATH, null);
			if (ref != null)
				serviceRegistry.getNodeService().deleteNode(ref);
		}
		removeNodeFromCache(ID_DATA_PATH);
	}

	public boolean isInitialized() {
		return extensionRegistry.resolvePath(DATA_PATH, null) != null
				&& extensionRegistry.resolvePath(CONFIG_PATH, null) != null;
	}

	@Required
	public void setExtensionRegistry(
			RepositoryExtensionRegistry extensionRegistry) {
		this.extensionRegistry = extensionRegistry;
	}
	
	public NodeRef getDataPath() {
		return getNodeFromCache(ID_DATA_PATH);
	}
	
	public void addNodeToCache(String id, NodeRef nodeRef) {
		String domain = getCurrentDomain();
		if (!nodeCache.containsKey(domain))
			nodeCache.put(domain, new HashMap<String, NodeRef>());
		nodeCache.get(domain).put(id, nodeRef);
	}
	
	public NodeRef getNodeFromCache(String id) {
		String domain = getCurrentDomain();
		return nodeCache.containsKey(domain) ?  nodeCache.get(domain).get(id) : null;
	}

	public void removeNodeFromCache(String id) {
		String domain = getCurrentDomain();
		if (nodeCache.containsKey(domain))
			nodeCache.get(domain).remove(id);
	}
	
	public void dropNodeCache() {
		String domain = getCurrentDomain();
		nodeCache.remove(domain);
	}
	
	public String getCurrentDomain() {
		return TenantUtil.getCurrentDomain();
	}
}
