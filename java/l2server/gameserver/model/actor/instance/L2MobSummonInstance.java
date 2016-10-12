package l2server.gameserver.model.actor.instance;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.log.Log;

import java.util.Collection;
import java.util.StringTokenizer;

/**
 * @author Pere
 */
public class L2MobSummonInstance extends L2SummonInstance
{
	private boolean _previousFollowStatus = true;

	private L2ItemInstance _controlItem;

	public L2MobSummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance controlItem)
	{
		super(objectId, template, owner, null);
		_controlItem = controlItem;
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (player == getOwner() && player.getTarget() == this)
		{
			player.sendPacket(new PetStatusShow(this));
			showMobWindow(player);
		}
		else if (player.getTarget() != this)
		{
			if (Config.DEBUG)
			{
				Log.fine("new target selected:" + getObjectId());
			}
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);

			//sends HP/MP status of the summon to other characters
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else if (player.getTarget() == this)
		{
			if (isAutoAttackable(player))
			{
				if (Config.GEODATA > 0)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						player.onActionRequest();
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					player.onActionRequest();
				}
			}
			else
			{
				// This Action Failed packet avoids player getting stuck when clicking three or more times
				player.sendPacket(ActionFailed.STATIC_PACKET);
				if (Config.GEODATA > 0)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
		}
	}

	public void showMobWindow(L2PcInstance player)
	{
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage htmlMessage = new NpcHtmlMessage(getObjectId());
		String html = "<html><body><center><br>" +
				"<button value=\"Attack\" action=\"bypass -h MobSummon Attack\" width=120 height=30 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br>" +
				//"<a action=\"bypass -h MobSummon Unsummon\">Unsummon</a><br>" +
				"Skills:<br>";
		int i = 0;
		if (getTemplate().getSkills() != null)
		{
			L2Skill[] skills = new L2Skill[3];
			for (L2Skill skill : getTemplate().getSkills().values())
			{
				if (skill.isActive())
				{
					if (i == 0)
					{
						html += "<table>";
					}
					if (i % 2 == 0)
					{
						html += "<tr>";
					}
					html += "<td><center>";
					if (skill.getId() < 1000)
					{
						html += "<button action=\"bypass -h MobSummon UseSkill " + skill.getId() + " " +
								skill.getLevelHash() + "\" back=icon.skill0" + skill.getId() + " fore=icon.skill0" +
								skill.getId() + " width=32 height=32>";
					}
					else
					{
						html += "<button action=\"bypass -h MobSummon UseSkill " + skill.getId() + " " +
								skill.getLevelHash() + "\" back=icon.skill" + skill.getId() + " fore=icon.skill" +
								skill.getId() + " width=32 height=32>";
					}
					html += "</center></td>";
					skills[i % 2] = skill;
					i++;
					if (i % 2 == 0)
					{
						html += "</tr><tr>";
						for (int j = 0; j < 2; j++)
						{
							html += "<td><center><a action=\"bypass -h MobSummon UseSkill " + skill.getId() + " " +
									skill.getLevelHash() + "\">" + skills[j].getName() + "</a></center></td>";
						}
						html += "</tr>";
					}
				}
			}
			if (i % 2 > 0)
			{
				html += "</tr><tr>";
				for (int j = 0; j < i % 2; j++)
				{
					html += "<td><center><a action=\"bypass -h MobSummon UseSkill " + skills[j].getId() + " " +
							skills[j].getLevelHash() + "\">" + skills[j].getName() + "</a></center></td>";
				}
				html += "</tr>";
			}
		}
		if (i == 0)
		{
			html += "None.";
		}
		else
		{
			html += "</table>";
		}
		html +=
				"<br><button value=\"Exchange\" action=\"bypass -h MobSummon Exchange\" width=120 height=30 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		html += "</center></body></html>";
		htmlMessage.setHtml(html);
		player.sendPacket(htmlMessage);

		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onBypass(L2PcInstance player, String command)
	{
		if (command.equals("Unsummon"))
		{
			if (isBetrayed())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_REFUSING_ORDER));
			}
			else if (isAttackingNow() || isInCombat())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
			}
			else
			{
				unSummon(player);
			}
		}
		else if (command.equals("Attack"))
		{
			L2Object target = getOwner().getTarget();
			if (target != null && target != this && !isAttackingDisabled() && !isBetrayed())
			{
				if (!getOwner().getAccessLevel().allowPeaceAttack() && getOwner().isInsidePeaceZone(this, target))
				{
					getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
					return;
				}
				//if (target.isAutoAttackable(getOwner()) || _ctrlPressed)
				if (target instanceof L2MobSummonInstance)
				{
					if (target instanceof L2DoorInstance)
					{
						if (((L2DoorInstance) target).isAttackable(getOwner()))
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						}
					}
					// siege golem AI doesn't support attacking other than doors at the moment
					else
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
				}
				else
				{
					getOwner().sendMessage("Your monster can only attack other domesticated monsters");
				}
			}
			showMobWindow(player);
		}
		else if (command.startsWith("UseSkill"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			int skillId = Integer.valueOf(st.nextToken());
			int skillLvl = Integer.valueOf(st.nextToken());
			L2Character target = this;
			if (getOwner().getTarget() instanceof L2Character)
			{
				target = (L2Character) getOwner().getTarget();
			}
			useSkill(skillId, skillLvl, target);
			showMobWindow(player);
		}
		else if (command.equals("Exchange"))
		{
			L2Object target = getOwner().getTarget();
			if (target != null && target != this)
			{
				if (target instanceof L2MobSummonInstance)
				{
					if (isInsideRadius(target, 1500, true, false))
					{
						/*
                        L2PcInstance receptor = ((L2MobSummonInstance)target).getOwner();
						receptor.setMobSummonExchangeRequest(true, this);
						String confirmText = getOwner().getName() + " wants to exchange your " + receptor.getPet().getName() + " for his " + getName() + ". Do you accept?";
						ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId()).addString(confirmText);
						receptor.sendPacket(dlg);
						getOwner().sendMessage("You have asked for the exchange of your " + getName() + " for the " + receptor.getName() + "'s " + receptor.getPet().getName() + ".");
						 */
						//TODO for new summons system
					}
					else
					{
						getOwner().sendMessage("You are too far!");
					}
				}
				else
				{
					getOwner().sendMessage("Your target must be another domesticated monster!");
				}
			}
		}
		else
		{
			showMobWindow(player);
		}
	}

	public void exchange(L2MobSummonInstance mob)
	{
		if (isInsideRadius(mob, 1500, true, false))
		{
			L2PcInstance newOwner = mob.getOwner();
			mob.setOwner(getOwner());
			setOwner(newOwner);
			getOwner().addSummon(this);
			getOwner().removeSummon(mob);
			mob.getOwner().addSummon(mob);
			getOwner().removeSummon(this);
			setFollowStatus(true);
			mob.setFollowStatus(true);
			L2ItemInstance newControlItem = mob.getControlItem();
			mob.setControlItem(getControlItem());
			setControlItem(newControlItem);
			getControlItem().setMobId(getNpcId());
			mob.getControlItem().setMobId(mob.getNpcId());
			CreatureSay cs1 = new CreatureSay(getObjectId(), Say2.TELL, getName(), "Bye!");
			CreatureSay cs2 = new CreatureSay(mob.getObjectId(), Say2.TELL, mob.getName(), "Bye!");
			CreatureSay cs3 = new CreatureSay(getObjectId(), Say2.TELL, getName(), "You are my new boss, true?");
			CreatureSay cs4 =
					new CreatureSay(mob.getObjectId(), Say2.TELL, mob.getName(), "You are my new boss, true?");
			mob.getOwner().sendPacket(cs1);
			getOwner().sendPacket(cs2);
			getOwner().sendPacket(cs3);
			mob.getOwner().sendPacket(cs4);
			updateAndBroadcastStatus(1);
			mob.updateAndBroadcastStatus(1);
		}
		else
		{
			getOwner().sendMessage("You are too far from yourselves!");
		}
	}

	private void useSkill(int skillId, int skillLvl, L2Object target)
	{
		if (getOwner() == null)
		{
			return;
		}

		if (getOwner().getPrivateStoreType() != 0)
		{
			getOwner().sendMessage("Cannot use skills while trading");
			return;
		}

		if (!isBetrayed())
		{
			if (skillLvl == 0)
			{
				return;
			}

			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
			if (skill == null)
			{
				return;
			}

			setTarget(target);
			useMagic(skill, true, false);
		}
	}

	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill == null || isDead())
		{
			return false;
		}

		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			return false;
		}

		//************************************* Check Casting in Progress *******************************************

		// If a skill is currently being used
		if (isCastingNow())
		{
			return false;
		}

		//************************************* Check Target *******************************************

		// Get the target for the skill
		L2Object target = null;

		switch (skill.getTargetType())
		{
			// OWNER_PET should be cast even if no target has been found
			case TARGET_OWNER_PET:
				target = getOwner();
				break;
			// PARTY, AURA, SELF should be cast even if no target has been found
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
				break;
			case TARGET_PARTY:
			case TARGET_SELF:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}

		// Check the validity of the target
		if (target == null)
		{
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
			}
			return false;
		}

		if (!(target instanceof L2MobSummonInstance))
		{
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			}
			return false;
		}

		//************************************* Check skill availability *******************************************

		// Check if this skill is enabled (e.g. reuse time)
		if (isSkillDisabled(skill.getId()) || isAllSkillsDisabled() && !skill.canBeUsedWhenDisabled())
		{
			if (getOwner() != null)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill);
				getOwner().sendPacket(sm);
			}
			return false;
		}

		//************************************* Check Consumables *******************************************

		// Check if the summon has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
			}
			return false;
		}

		// Check if the summon has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
			}
			return false;
		}

		//************************************* Check Summon State *******************************************

		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target) && getOwner() != null &&
					!getOwner().getAccessLevel().allowPeaceAttack())
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				return false;
			}

			if (getOwner() != null && getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// Check if the target is attackable
			if (target instanceof L2DoorInstance)
			{
				if (!((L2DoorInstance) target).isAttackable(getOwner()))
				{
					return false;
				}
			}
			else
			{
				if (!target.isAttackable() && getOwner() != null && !(target instanceof L2MobSummonInstance) &&
						!getOwner().getAccessLevel().allowPeaceAttack())
				{
					return false;
				}

				// Check if a Forced ATTACK is in progress on non-attackable target
				if (!target.isAutoAttackable(this) && !forceUse &&
						skill.getTargetType() != L2SkillTargetType.TARGET_AURA &&
						skill.getTargetType() != L2SkillTargetType.TARGET_FRONT_AURA &&
						skill.getTargetType() != L2SkillTargetType.TARGET_BEHIND_AURA &&
						skill.getTargetType() != L2SkillTargetType.TARGET_CLAN &&
						skill.getTargetType() != L2SkillTargetType.TARGET_ALLY &&
						skill.getTargetType() != L2SkillTargetType.TARGET_PARTY &&
						skill.getTargetType() != L2SkillTargetType.TARGET_SELF)
				{
					return false;
				}
			}
		}
		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);

		return true;
	}

	@Override
	public long getExpForThisLevel()
	{
		if (getLevel() > Config.MAX_LEVEL)
		{
			return 0;
		}

		return Experience.getAbsoluteExp(getLevel());
	}

	@Override
	public long getExpForNextLevel()
	{
		if (getLevel() > Config.MAX_LEVEL)
		{
			return 0;
		}

		return Experience.getAbsoluteExp(getLevel() + 1);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		L2PcInstance owner = getOwner();

		if (owner != null)
		{

		}

		return true;
	}

	public void setControlItem(L2ItemInstance item)
	{
		_controlItem = item;
	}

	public L2ItemInstance getControlItem()
	{
		return _controlItem;
	}

	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);

		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			// if immobilized temporarly disable follow mode
			if (_previousFollowStatus)
			{
				setFollowStatus(false);
			}
		}
		else
		{
			// if not more immobilized restore previous follow mode
			setFollowStatus(_previousFollowStatus);
		}
	}

	@Override
	public void updateAndBroadcastStatus(int val)
	{
		if (!isVisible())
		{
			return;
		}

		getOwner().sendPacket(new PetInfo(this, val));
		getOwner().sendPacket(new PetStatusUpdate(this));
		if (isVisible())
		{
			broadcastNpcInfo(val);
		}
		L2Party party = getOwner().getParty();
		if (party != null)
		{
			party.broadcastToPartyMembers(getOwner(), new ExPartyPetWindowUpdate(this));
		}
		updateEffectIcons(true);
	}

	@Override
	public void broadcastNpcInfo(int val)
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			try
			{
				if (player == getOwner())
				{
					continue;
				}

				if (!player.isGM() && getOwner() != null && getOwner().getAppearance().getInvisible())
				{
					continue;
				}

				player.sendPacket(new ExSummonInfo(this, player, val));
			}
			catch (NullPointerException e)
			{
				// ignore it
			}
		}
	}

	@Override
	public boolean isAttackable()
	{
		return false;
	}

	@Override
	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}
}
