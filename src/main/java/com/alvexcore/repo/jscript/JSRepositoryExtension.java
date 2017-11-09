package com.alvexcore.repo.jscript;

import com.alvexcore.repo.RepositoryExtension;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.mozilla.javascript.Scriptable;

import java.io.Serializable;

public class JSRepositoryExtension implements Serializable{
	private static final long serialVersionUID = -6284708320747731858L;
	protected ServiceRegistry serviceRegistry;
	protected RepositoryExtension extension;
	protected Scriptable scope;
	protected ValueConverter converter = new ValueConverter();

	public JSRepositoryExtension(ServiceRegistry serviceRegistry, final Scriptable scope,
			RepositoryExtension extension) {
			this.serviceRegistry = serviceRegistry;
			this.extension = extension;
			this.scope = scope;
	}
	
	public String getId() {
		return extension.getId();
	}
	

	public void init(boolean failIfInitialized) throws Exception {
		extension.init(failIfInitialized);
	}
	
	public void init() throws Exception {
		extension.init(true);
	}
	
	public void drop(boolean all) throws Exception {
		extension.drop(all);
	}
	
	public void drop() throws Exception {
		extension.drop(false);
	} 
}
