package com.livegameengine.scxml.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.Send;

import com.livegameengine.config.Config;
import com.livegameengine.util.Util;

public class SendWatcherEvent extends Send {
	private String keyExpr_ = null;
	
	public SendWatcherEvent() {
		super();
		
		setTarget(String.format("'%s'", Util.escapeJS(Config.getInstance().getWatcherEventTarget())));
		setTargettype(String.format("'%s'", Util.escapeJS(Config.getInstance().getGameEventTargetType())));
		setSendid("'game.send.gameEvent.id'");
	}
	
	@Override
	public void execute(final EventDispatcher evtDispatcher, ErrorReporter errRep,
			SCInstance scInstance, Log appLog, Collection derivedEvents)
			throws ModelException, SCXMLExpressionException {
		
		String key = "";
		
		if(keyExpr_ != null) {
			Evaluator eval = scInstance.getEvaluator();
			
			key = eval.eval(scInstance.getRootContext(), keyExpr_).toString();
		}
		
		final String finalkey = key;
				
		super.execute(new EventDispatcher() {
			public void send(String sendId, String target, String targetType,
					String event, Map params, Object hints, long delay, Object content,
					List externalNodes) {
				params.put("key", finalkey);
				evtDispatcher.send(sendId, target, targetType, 
						event, params, hints, delay, content, 
						externalNodes);
			}
			public void cancel(String sendId) {
				evtDispatcher.cancel(sendId);
			}
		}, errRep, scInstance, appLog, derivedEvents);
	}
	
	
	public String getKeyExpr() {
		return keyExpr_;
	}
	
	public void setKeyExpr(String keyExpr) {
		this.keyExpr_ = keyExpr;
	}
}
