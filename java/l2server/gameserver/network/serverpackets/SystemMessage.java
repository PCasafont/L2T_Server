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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.Elementals;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.SystemMessageId.SMLocalisation;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;

public final class SystemMessage extends L2GameServerPacket
{

	private static final SMParam[] EMPTY_PARAM_ARRAY = new SMParam[0];

	private static final class SMParam
	{
		private final byte _type;
		private final Object _value;

		public SMParam(final byte type, final Object value)
		{
			_type = type;
			_value = value;
		}

		public final byte getType()
		{
			return _type;
		}

		public final Object getValue()
		{
			return _value;
		}

		public final String getStringValue()
		{
			return (String) _value;
		}

		public final int getIntValue()
		{
			return (Integer) _value;
		}

		public final long getLongValue()
		{
			return (Long) _value;
		}

		public final int[] getIntArrayValue()
		{
			return (int[]) _value;
		}
	}

	private static final byte TYPE_HP_CHANGE = 16;
	// id 15 - unknown
	// id 14 - unknown
	private static final byte TYPE_SYSTEM_STRING = 13;
	private static final byte TYPE_PLAYER_NAME = 12;
	// id 11 - unknown
	private static final byte TYPE_INSTANCE_NAME = 10;
	private static final byte TYPE_ELEMENT_NAME = 9;
	// id 8 - same as 3
	private static final byte TYPE_ZONE_NAME = 7;
	private static final byte TYPE_ITEM_NUMBER = 6;
	private static final byte TYPE_CASTLE_NAME = 5;
	private static final byte TYPE_SKILL_NAME = 4;
	private static final byte TYPE_ITEM_NAME = 3;
	private static final byte TYPE_NPC_NAME = 2;
	private static final byte TYPE_NUMBER = 1;
	private static final byte TYPE_TEXT = 0;

	public static SystemMessage sendString(final String text)
	{
		if (text == null)
		{
			throw new NullPointerException();
		}

		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		sm.addString(text);
		return sm;
	}

	public static SystemMessage getSystemMessage(final SystemMessageId smId)
	{
		SystemMessage sm = smId.getStaticSystemMessage();
		if (sm != null)
		{
			return sm;
		}

		sm = new SystemMessage(smId);
		if (smId.getParamCount() == 0)
		{
			smId.setStaticSystemMessage(sm);
		}

		return sm;
	}

	/**
	 * Use {@link #getSystemMessage(SystemMessageId)} where possible instead
	 *
	 * @param id
	 * @return
	 */
	public static SystemMessage getSystemMessage(int id)
	{
		return getSystemMessage(SystemMessageId.getSystemMessageId(id));
	}

	private final SystemMessageId _smId;
	private SMParam[] _params;
	private int _paramIndex;

	private SystemMessage(final SystemMessageId smId)
	{
		final int paramCount = smId.getParamCount();
		_smId = smId;
		_params = paramCount != 0 ? new SMParam[paramCount] : EMPTY_PARAM_ARRAY;
	}

	/**
	 * Use SystemMessage.getSystemMessage(SystemMessageId smId) where possible instead
	 *
	 * @deprecated
	 */
	@Deprecated
	private SystemMessage(final int id)
	{
		this(SystemMessageId.getSystemMessageId(id));
	}

	private void append(final SMParam param)
	{
		if (_paramIndex >= _params.length)
		{
			_params = Arrays.copyOf(_params, _paramIndex + 1);
			_smId.setParamCount(_paramIndex + 1);
			Log.log(Level.INFO, "Wrong parameter count '" + (_paramIndex + 1) + "' for SystemMessageId: " + _smId);
		}

		_params[_paramIndex++] = param;
	}

	public final SystemMessage addString(final String text)
	{
		append(new SMParam(TYPE_TEXT, text));
		return this;
	}

	/**
	 * Castlename-e.dat<br>
	 * 0-9 Castle names<br>
	 * 21-64 CH names<br>
	 * 81-89 Territory names<br>
	 * 101-121 Fortress names<br>
	 *
	 * @param number
	 * @return
	 */
	public final SystemMessage addFortId(final int number)
	{
		append(new SMParam(TYPE_CASTLE_NAME, number));
		return this;
	}

	public final SystemMessage addNumber(final long number)
	{
		append(new SMParam(TYPE_ITEM_NUMBER, number));
		return this;
	}

	public final SystemMessage addItemNumber(final long number)
	{
		append(new SMParam(TYPE_ITEM_NUMBER, number));
		return this;
	}

	public final SystemMessage addCharName(final L2Character cha)
	{
		if (cha instanceof L2Npc)
		{
			if (((L2Npc) cha).getTemplate().ServerSideName)
			{
				return addString(((L2Npc) cha).getTemplate().Name);
			}
			else
			{
				return addNpcName((L2Npc) cha);
			}
		}
		else if (cha instanceof L2PcInstance)
		{
			return addPcName((L2PcInstance) cha);
		}
		else if (cha instanceof L2Summon)
		{
			if (((L2Summon) cha).getTemplate().ServerSideName)
			{
				return addString(((L2Summon) cha).getTemplate().Name);
			}
			else
			{
				return addNpcName((L2Summon) cha);
			}
		}
		return addString(cha.getName());
	}

	public final SystemMessage addPcName(final L2PcInstance pc)
	{
		append(new SMParam(TYPE_PLAYER_NAME, pc.getAppearance().getVisibleName()));
		return this;
	}

	public final SystemMessage addNpcName(final L2Npc npc)
	{
		return addNpcName(npc.getTemplate());
	}

	public final SystemMessage addNpcName(final L2Summon npc)
	{
		return addNpcName(npc.getNpcId());
	}

	public final SystemMessage addNpcName(final L2NpcTemplate template)
	{
		if (template.isCustom())
		{
			return addString(template.Name);
		}
		return addNpcName(template.NpcId);
	}

	public final SystemMessage addNpcName(final int id)
	{
		append(new SMParam(TYPE_NPC_NAME, 1000000 + id));
		return this;
	}

	public final SystemMessage addItemName(final L2ItemInstance item)
	{
		return addItemName(item.getItem().getItemId());
	}

	public final SystemMessage addItemName(final L2Item item)
	{
		return addItemName(item.getItemId());
	}

	public final SystemMessage addItemName(final int id)
	{
		append(new SMParam(TYPE_ITEM_NAME, id));
		return this;
	}

	public final SystemMessage addZoneName(final int x, final int y, final int z)
	{
		append(new SMParam(TYPE_ZONE_NAME, new int[]{x, y, z}));
		return this;
	}

	public final SystemMessage addSkillName(final L2Abnormal effect)
	{
		return addSkillName(effect.getSkill());
	}

	public final SystemMessage addSkillName(final L2Skill skill)
	{
		if (skill.getId() != skill.getDisplayId()) //custom skill -  need nameId or smth like this.
		{
			return addString(skill.getName());
		}
		return addSkillName(skill.getId(), skill.getLevelHash());
	}

	public final SystemMessage addSkillName(final int id)
	{
		return addSkillName(id, 1);
	}

	public final SystemMessage addSkillName(final int id, final int lvl)
	{
		append(new SMParam(TYPE_SKILL_NAME, new int[]{id, lvl}));
		return this;
	}

	/**
	 * Elemental name - 0(Fire) ...
	 *
	 * @param type
	 * @return
	 */
	public final SystemMessage addElemental(final int type)
	{
		append(new SMParam(TYPE_ELEMENT_NAME, type));
		return this;
	}

	/**
	 * ID from sysstring-e.dat
	 *
	 * @param type
	 * @return
	 */
	public final SystemMessage addSystemString(final int type)
	{
		append(new SMParam(TYPE_SYSTEM_STRING, type));
		return this;
	}

	/**
	 * Instance name from instantzonedata-e.dat
	 *
	 * @param type id of instance
	 * @return
	 */
	public final SystemMessage addInstanceName(final int type)
	{
		append(new SMParam(TYPE_INSTANCE_NAME, type));
		return this;
	}

	public final SystemMessage addHpChange(final int charId, final int causerId, final int hpChange)
	{
		append(new SMParam(TYPE_HP_CHANGE, new int[]{charId, causerId, hpChange}));
		return this;
	}

	public final SystemMessageId getSystemMessageId()
	{
		return _smId;
	}

	public final SystemMessage getLocalizedMessage(final String lang)
	{
		if (_smId == SystemMessageId.S1)
		{
			return this;
		}

		final SMLocalisation sml = _smId.getLocalisation(lang);
		if (sml == null)
		{
			return this;
		}

		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		final Object[] params = new Object[_paramIndex];

		SMParam param;
		for (int i = 0; i < _paramIndex; i++)
		{
			param = _params[i];

			switch (param.getType())
			{
				case TYPE_TEXT:
				case TYPE_PLAYER_NAME:
				{
					params[i] = param.getValue();
					break;
				}

				case TYPE_ITEM_NUMBER:
				{
					params[i] = param.getValue();
					break;
				}

				case TYPE_ITEM_NAME:
				{
					final L2Item item = ItemTable.getInstance().getTemplate(param.getIntValue());
					params[i] = item == null ? "Unknown" : item.getName();
					break;
				}

				case TYPE_CASTLE_NAME:
				{
					final Castle castle = CastleManager.getInstance().getCastleById(param.getIntValue());
					params[i] = castle == null ? "Unknown" : castle.getName();
					break;
				}

				case TYPE_NUMBER:
				{
					params[i] = param.getValue();
					break;
				}

				case TYPE_NPC_NAME:
				{
					final L2NpcTemplate template = NpcTable.getInstance().getTemplate(param.getIntValue());
					params[i] = template == null ? "Unknown" : template.getName();
					break;
				}

				case TYPE_ELEMENT_NAME:
				{
					params[i] = Elementals.getElementName((byte) param.getIntValue());
					break;
				}

				case TYPE_SYSTEM_STRING:
				{
					params[i] = "SYS-S-" + param.getIntValue(); //super.writeD(param.getIntValue());
					break;
				}

				case TYPE_INSTANCE_NAME:
				{
					params[i] = "INS-N-" + param.getIntValue(); //super.writeD(param.getIntValue());
					break;
				}

				case TYPE_SKILL_NAME:
				{
					final int[] array = param.getIntArrayValue();
					final L2Skill skill = SkillTable.getInstance().getInfo(array[0], array[1]);
					params[i] = skill == null ? "Unknown" : skill.getName();
					break;
				}

				case TYPE_ZONE_NAME:
				{
					final int[] array = param.getIntArrayValue();
					//super.writeD(array[0]); // x
					//super.writeD(array[1]); // y
					//super.writeD(array[2]); // z
					params[i] = "ZON-N-" + Arrays.toString(array);
					break;
				}
			}
		}

		sm.addString(sml.getLocalisation(params));
		return sm;
	}

	public final void printMe(PrintStream out)
	{
		out.println(0x62);

		out.println(_smId.getId());
		out.println(_paramIndex);

		SMParam param;
		for (int i = 0; i < _paramIndex; i++)
		{
			param = _params[i];
			out.println(param.getType());

			switch (param.getType())
			{
				case TYPE_TEXT:
				case TYPE_PLAYER_NAME:
				{
					out.println(param.getStringValue());
					break;
				}

				case TYPE_ITEM_NUMBER:
				{
					out.println(param.getLongValue());
					break;
				}

				case TYPE_ITEM_NAME:
				case TYPE_CASTLE_NAME:
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ELEMENT_NAME:
				case TYPE_SYSTEM_STRING:
				case TYPE_INSTANCE_NAME:
				{
					out.println(param.getIntValue());
					break;
				}

				case TYPE_SKILL_NAME:
				{
					final int[] array = param.getIntArrayValue();
					out.println(array[0]); // SkillId
					out.println(array[1]); // SkillLevel
					break;
				}

				case TYPE_ZONE_NAME:
				{
					final int[] array = param.getIntArrayValue();
					out.println(array[0]); // x
					out.println(array[1]); // y
					out.println(array[2]); // z
					break;
				}
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_smId.getId());
		writeC(_paramIndex);

		SMParam param;
		for (int i = 0; i < _paramIndex; i++)
		{
			param = _params[i];
			writeC(param.getType());

			switch (param.getType())
			{
				case TYPE_TEXT:
				case TYPE_PLAYER_NAME:
				{
					writeS(param.getStringValue());
					break;
				}

				case TYPE_ITEM_NUMBER:
				{
					writeQ(param.getLongValue());
					break;
				}

				case TYPE_ITEM_NAME:
				case TYPE_CASTLE_NAME:
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ELEMENT_NAME:
				case TYPE_SYSTEM_STRING:
				case TYPE_INSTANCE_NAME:
				{
					writeD(param.getIntValue());
					break;
				}

				case TYPE_SKILL_NAME:
				{
					final int[] array = param.getIntArrayValue();
					writeD(array[0]); // SkillId
					writeH((short) array[1]); // SkillLevel
					break;
				}

				case TYPE_ZONE_NAME:
				{
					final int[] array = param.getIntArrayValue();
					writeD(array[0]); // x
					writeD(array[1]); // y
					writeD(array[2]); // z
					break;
				}

				case TYPE_HP_CHANGE:
				{
					final int[] array = param.getIntArrayValue();
					writeD(array[0]); // charId
					writeD(array[1]); // causerId
					writeD(array[2]); // hpChange
					break;
				}
			}
		}
	}
}
