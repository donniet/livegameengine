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

import com.livegameengine.config.Config;
import com.livegameengine.util.Util;

public class SendWatcherEvent extends Send {
	public SendWatcherEvent() {
		super();
		
		setTarget(String.format("'%s'", Util.escapeJS(Config.getInstance().getWatcherEventTarget())));
		setTargettype(String.format("'%s'", Util.escapeJS(Config.getInstance().getGameEventTargetType())));
		setSendid("'game.send.gameEvent.id'");
	}
}
