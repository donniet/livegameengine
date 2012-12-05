package com.livegameengine.scxml.model;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.Send;

public class StartGame  extends Action {

	@Override
	public void execute(EventDispatcher arg0, ErrorReporter arg1,
			SCInstance arg2, Log arg3, Collection arg4) throws ModelException,
			SCXMLExpressionException {
		
	}

}
