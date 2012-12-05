package com.livegameengine.persist;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.jdo.spi.PersistenceCapable;

public final class PMF {
    private static final PersistenceManagerFactory pmfInstance =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    private PMF() {}

    public static PersistenceManagerFactory getInstance() {
        return pmfInstance;
    }
    
    public static void makePersistent(final Object o) throws PersistenceCommandException {
    	PersistenceManager pmf = JDOHelper.getPersistenceManager(o);
    	
    	if(pmf == null) {
    		executeCommandInTransaction(new PersistenceCommand() {
				@Override
				public Object exec(PersistenceManager pm) {
					pm.makePersistent(o);
					return null;
				}
			});
    	}
    	else {
    		pmf.makePersistent(o);
    	}
    }
    
    public static Object executeCommand(PersistenceCommand command) throws PersistenceCommandException {
    	PersistenceManager pm = getInstance().getPersistenceManager();
    	
    	Object ret = null;
    	
    	try {
    		ret = command.exec(pm);
    	}
    	catch(Exception ex) {
    		throw new PersistenceCommandException(ex);
    	}
    	
    	return ret;
    }
    
    public static Object executeCommandInTransaction(PersistenceCommand command) throws PersistenceCommandException {
    	PersistenceManager pm = getInstance().getPersistenceManager();
    	Transaction tx = pm.currentTransaction();
    	
    	Object ret = null;
    	
    	try {
    		tx.begin();
    		
    		ret = command.exec(pm);
    		
    		tx.commit();
    	}
    	catch(Exception e) {
    		throw new PersistenceCommandException(e);
    	}
    	finally {
    		if(tx.isActive()) {
    			tx.rollback();
    		}
    		pm.close();
    	}
    	
    	return ret;
    }
}