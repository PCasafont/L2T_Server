/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor.instance;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldRegion;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.knownlist.GuardKnownList;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages all Guards in the world.
 * It inherits all methods from Attackable and adds some more such as tracking PK and aggressive MonsterInstance.<BR><BR>
 *
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public class GuardInstance extends Attackable {
	private static Logger log = LoggerFactory.getLogger(GuardInstance.class.getName());

	private static final int RETURN_INTERVAL = 60000;
	
	public class ReturnTask implements Runnable {
		@Override
		public void run() {
			if (isDecayed() && !getSpawn().isRespawnEnabled()) {
				return;
			}
			
			if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
				returnHome();
			}
		}
	}
	
	/**
	 * Constructor of GuardInstance (use Creature and NpcInstance constructor).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the Creature constructor to set the template of the GuardInstance (copy skills from template to object and link calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the GuardInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 */
	public GuardInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2GuardInstance);
		
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReturnTask(), RETURN_INTERVAL + Rnd.nextInt(60000), RETURN_INTERVAL);
	}
	
	@Override
	public final GuardKnownList getKnownList() {
		return (GuardKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList() {
		setKnownList(new GuardKnownList(this));
	}
	
	/**
	 * Return True if this attacker is a MonsterInstance.<BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return attacker instanceof MonsterInstance;
	}
	
	/**
	 * Notify the GuardInstance to return to its home location (AI_INTENTION_MOVE_TO) and clear its aggroList.<BR><BR>
	 */
	@Override
	public void returnHome() {
		if (!isInsideRadius(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 1000, true, false)) {
			teleToLocation(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), false);
		} else if (!isInsideRadius(getSpawn().getX(), getSpawn().getY(), 150, false)) {
			clearAggroList();
			
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
		}
	}
	
	/**
	 * Set the home location of its GuardInstance.<BR><BR>
	 */
	@Override
	public void onSpawn() {
		super.onSpawn();
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		WorldRegion region = World.getInstance().getRegion(getX(), getY());
		if (region != null && !region.isActive()) {
			getAI().stopAITask();
		}
	}
	
	/**
	 * Return the pathfile of the selected HTML file in function of the GuardInstance Identifier and of the page number.<BR><BR>
	 * <p>
	 * <B><U> Format of the pathfile </U> :</B><BR><BR>
	 * <li> if page number = 0 : <B>data/html/guard/12006.htm</B> (npcId-page number)</li>
	 * <li> if page number > 0 : <B>data/html/guard/12006-1.htm</B> (npcId-page number)</li><BR><BR>
	 *
	 * @param npcId The Identifier of the NpcInstance whose text must be display
	 * @param val   The number of the page to display
	 */
	@Override
	public String getHtmlPath(int npcId, int val) {
		String pom = "";
		if (val == 0) {
			pom = "" + npcId;
		} else {
			pom = npcId + "-" + val;
		}
		return "guard/" + pom + ".htm";
	}
	
	/**
	 * Manage actions when a player click on the GuardInstance.<BR><BR>
	 * <p>
	 * <B><U> Actions on first click on the GuardInstance (Select it)</U> :</B><BR><BR>
	 * <li>Set the GuardInstance as target of the Player player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the Player player (display the select window)</li>
	 * <li>Set the Player Intention to AI_INTENTION_IDLE </li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the GuardInstance position and heading on the client </li><BR><BR>
	 * <p>
	 * <B><U> Actions on second click on the GuardInstance (Attack it/Interact with it)</U> :</B><BR><BR>
	 * <li>If Player is in the aggroList of the GuardInstance, set the Player Intention to AI_INTENTION_ATTACK</li>
	 * <li>If Player is NOT in the aggroList of the GuardInstance, set the Player Intention to AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param player The Player that start an action on the GuardInstance
	 */
	@Override
	public void onAction(Player player, boolean interact) {
		if (!canTarget(player)) {
			return;
		}
		
		// Check if the Player already target the GuardInstance
		if (getObjectId() != player.getTargetId()) {
			if (Config.DEBUG) {
				log.debug(player.getObjectId() + ": Targetted guard " + getObjectId());
			}
			
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			
			// Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		} else if (interact) {
			// Check if the Player is in the aggroList of the GuardInstance
			if (containsTarget(player) || isAutoAttackable(player)) {
				if (Config.DEBUG) {
					log.debug(player.getObjectId() + ": Attacked guard " + getObjectId());
				}
				
				// Set the Player Intention to AI_INTENTION_ATTACK
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			} else {
				// Calculate the distance between the Player and the NpcInstance
				if (!canInteract(player)) {
					// Set the Player Intention to AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				} else {
					// Send a Server->Client packet SocialAction to the all Player on the knownPlayer of the NpcInstance
					// to display a social action of the GuardInstance on their client
					SocialAction sa = new SocialAction(getObjectId(), Rnd.nextInt(8));
					broadcastPacket(sa);
					
					// Open a chat window on client with the text of the GuardInstance
					Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
					if (qlsa != null && qlsa.length > 0) {
						player.setLastQuestNpcObject(getObjectId());
					}
					Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
					if (qlst != null && qlst.length == 1) {
						qlst[0].notifyFirstTalk(this, player);
					} else {
						showChatWindow(player, 0);
					}
				}
			}
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
