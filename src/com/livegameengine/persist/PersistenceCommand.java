package com.livegameengine.persist;

import javax.jdo.PersistenceManager;

public interface PersistenceCommand {
	public Object exec(PersistenceManager pm);
}
