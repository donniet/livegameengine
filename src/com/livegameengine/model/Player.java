package com.livegameengine.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.livegameengine.config.Config;
import com.livegameengine.persist.PMF;
import com.livegameengine.persist.PersistenceCommand;
import com.livegameengine.persist.PersistenceCommandException;

@PersistenceCapable
public class Player implements Scriptable {
	private static final long serialVersionUID = 1;
	
	@NotPersistent private Scriptable prototype_, parent_;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
		
	@Persistent
	private Key gameUserKey;
	
	@Persistent
	private boolean connected = false;
	
	@Persistent
	private Game game;
	
	@NotPersistent
	transient private GameUser gameUser = null;
	
	@Persistent
	private String role;
	
	public Player() {}
	/*
	public static List<Player> findPlayersByGame(final Game g) {
		List<Player> ret = new ArrayList<Player>();
		
		try {
			ret = (List<Player>)PMF.executeCommandInTransaction(new PersistenceCommand() {
				@Override
				public Object exec(PersistenceManager pm) {
					Query q = pm.newQuery(Player.class);
					q.setFilter("game == gameIn");
					q.declareParameters(Game.class.getName() + " gameIn");
					q.setRange(0, Config.getInstance().getMaxPlayers());
					
					return q.execute(g);
				}
			});
		} catch (PersistenceCommandException e) {
			//TODO: Handle exception
			e.printStackTrace();
		}
		
		return ret;
	}
	*/
	protected Player(Game game, GameUser gameUser, String role) {
		this.game = game;
		this.gameUserKey = gameUser.getKey();
		this.gameUser = gameUser;
		this.role = role;
	}
		
	public Key getKey() { return key; }
	public GameUser getGameUser() {
		if(gameUser == null) {
			try {
				gameUser = (GameUser)PMF.executeCommand(new PersistenceCommand() {
					@Override
					public Object exec(PersistenceManager pm) {
						return pm.getObjectById(GameUser.class, gameUserKey);
					}
				});
			}
			catch(PersistenceCommandException e) {
				e.printStackTrace();
				gameUser = null;
			}			
		}
		
		return gameUser; 
	}
	
	public String getRole() {
		return role;
	}
	
	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	//scriptable object stuff
	@Override
	public String getClassName() {
		return "Player";
	}
	
	public GameUser jsGet_gameUser() {
		return getGameUser();
	}
	
	public String jsGet_role() {
		return getRole();
	}

	@Override
	public void delete(String arg0) {}

	@Override
	public void delete(int arg0) {}

	public void serializeToXml(String elementName, XMLStreamWriter writer) throws XMLStreamException {
		String ns = Config.getInstance().getGameEngineNamespace();
		
		writer.writeStartElement(ns, elementName);
		writer.writeAttribute("key", KeyFactory.keyToString(getKey()));
		getGameUser().serializeToXml("gameUser", writer);
		writer.writeStartElement(ns, "role");
		writer.writeCharacters(getRole());
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		if(arg0.equals("connected"))
			return connected;
		else if(arg0.equals("game"))
			return game;
		else if(arg0.equals("gameUser"))
			return getGameUser();
		else if(arg0.equals("key"))
			return KeyFactory.keyToString(getKey());
		else if(arg0.equals("role"))
			return getRole();
		else if(arg0.equals("userid"))
			return getGameUser().getHashedUserId();
		
		return NOT_FOUND;
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		return NOT_FOUND;
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		return "[object Player]";
	}

	@Override
	public Object[] getIds() {
		return new Object[] { "connected", "game", "gameUser", "key", "role", "userid" };
	}

	@Override
	public Scriptable getParentScope() {
		return parent_;
	}

	@Override
	public Scriptable getPrototype() {
		return prototype_;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		String[] ids = (String[])getIds();
		
		return Arrays.binarySearch(ids, arg0) >= 0;
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable value) {
        Scriptable proto = value.getPrototype();
        while (proto != null) {
            if (proto.equals(this))
                return true;
            proto = proto.getPrototype();
        }

        return false;
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {}

	@Override
	public void setParentScope(Scriptable arg0) {
		parent_ = arg0;
	}

	@Override
	public void setPrototype(Scriptable arg0) {
		prototype_ = arg0;
	}
}
