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

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntLongHashMap;
import gov.nasa.worldwind.formats.dds.DDSConverter;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.*;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2CharacterAI;
import l2server.gameserver.ai.L2PlayerAI;
import l2server.gameserver.ai.L2SummonAI;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.cache.WarehouseCacheManager;
import l2server.gameserver.communitybbs.BB.Forum;
import l2server.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2server.gameserver.datatables.*;
import l2server.gameserver.events.Curfew;
import l2server.gameserver.events.RankingKillInfo;
import l2server.gameserver.events.chess.ChessEvent;
import l2server.gameserver.events.chess.ChessEvent.ChessState;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.events.instanced.types.StalkedStalkers;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.ItemHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.*;
import l2server.gameserver.instancemanager.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2server.gameserver.instancemanager.MainTownManager.MainTownInfo;
import l2server.gameserver.model.*;
import l2server.gameserver.model.L2FlyMove.L2FlyMoveChoose;
import l2server.gameserver.model.L2FlyMove.L2FlyMoveOption;
import l2server.gameserver.model.L2Party.messageType;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.appearance.PcAppearance;
import l2server.gameserver.model.actor.knownlist.PcKnownList;
import l2server.gameserver.model.actor.position.PcPosition;
import l2server.gameserver.model.actor.stat.PcStat;
import l2server.gameserver.model.actor.status.PcStatus;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.model.base.SubClass;
import l2server.gameserver.model.entity.*;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar.WarState;
import l2server.gameserver.model.itemcontainer.*;
import l2server.gameserver.model.multisell.PreparedListContainer;
import l2server.gameserver.model.olympiad.*;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.model.zone.type.L2NoRestartZone;
import l2server.gameserver.model.zone.type.L2TownZone;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.stats.*;
import l2server.gameserver.stats.funcs.*;
import l2server.gameserver.stats.skills.L2SkillSiegeFlag;
import l2server.gameserver.stats.skills.L2SkillSummon;
import l2server.gameserver.stats.skills.L2SkillTrap;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.chars.L2PcTemplate;
import l2server.gameserver.templates.item.*;
import l2server.gameserver.templates.skills.*;
import l2server.gameserver.util.Broadcast;
import l2server.gameserver.util.FloodProtectors;
import l2server.gameserver.util.IllegalPlayerAction;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Point3D;
import l2server.util.Rnd;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * This class represents all player characters in the world.
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).<BR><BR>
 *
 * @version $Revision: 1.66.2.41.2.33 $ $Date: 2005/04/11 10:06:09 $
 */
public class L2PcInstance extends L2Playable
{
    // Character Skill SQL String Definitions:
    private static final String RESTORE_SKILLS_FOR_CHAR =
            "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=?";
    private static final String ADD_NEW_SKILL =
            "REPLACE INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
    private static final String UPDATE_CHARACTER_SKILL_LEVEL =
            "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?";
    private static final String DELETE_SKILL_FROM_CHAR =
            "DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?";
    private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE charId=? AND class_index=?";

    // Character Skill Save SQL String Definitions:
    private static final String ADD_SKILL_SAVE =
            "INSERT INTO character_skills_save (charId,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
    private static final String RESTORE_SKILL_SAVE =
            "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE charId=? AND class_index=? ORDER BY buff_index ASC";
    private static final String DELETE_SKILL_SAVE =
            "DELETE FROM character_skills_save WHERE charId=? AND class_index=?";

    // Character Character SQL String Definitions:
    private static final String INSERT_CHARACTER =
            "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,reputation,fame,pvpkills,pkkills,clanid,templateId,classid,deletetime,cancraft,title,title_color,accesslevel,online,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,createTime) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_CHARACTER =
            "UPDATE characters SET level=?,temporaryLevel=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,reputation=?,fame=?,pvpkills=?,pkkills=?,clanid=?,templateId=?,classid=?,deletetime=?,title=?,title_color=?,accesslevel=?,online=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,newbie=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,bookmarkslot=?,show_hat=?,race_app=? WHERE charId=?";
    private static final String RESTORE_CHARACTER =
            "SELECT account_name, charId, char_name, level, temporaryLevel, curHp, curCp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, expBeforeDeath, sp, reputation, fame, pvpkills, pkkills, clanid, templateId, classid, deletetime, cancraft, title, title_color, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, punish_level, punish_timer, newbie, nobless, power_grade, subpledge, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,bookmarkslot,createTime,show_hat,race_app FROM characters WHERE charId=?";

    // Character Teleport Bookmark:
    private static final String INSERT_TP_BOOKMARK =
            "INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)";
    private static final String UPDATE_TP_BOOKMARK =
            "UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?";
    private static final String RESTORE_TP_BOOKMARK =
            "SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?";
    private static final String DELETE_TP_BOOKMARK = "DELETE FROM character_tpbookmark WHERE charId=? AND Id=?";

    // Character Subclass SQL String Definitions:
    private static final String RESTORE_CHAR_SUBCLASSES =
            "SELECT class_id,exp,sp,level,class_index,is_dual,certificates FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
    private static final String ADD_CHAR_SUBCLASS =
            "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index,certificates) VALUES (?,?,?,?,?,?,?)";
    private static final String UPDATE_CHAR_SUBCLASS =
            "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=?,is_dual=?,certificates=? WHERE charId=? AND class_index =?";
    private static final String DELETE_CHAR_SUBCLASS =
            "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";

    // Character Henna SQL String Definitions:
    private static final String RESTORE_CHAR_HENNAS =
            "SELECT slot,symbol_id,expireTime FROM character_hennas WHERE charId=? AND class_index=?";
    private static final String ADD_CHAR_HENNA =
            "INSERT INTO character_hennas (charId,symbol_id,slot,class_index,expireTime) VALUES (?,?,?,?,?)";
    private static final String DELETE_CHAR_HENNA =
            "DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?";
    private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE charId=? AND class_index=?";

    // Character Shortcut SQL String Definitions:
    private static final String DELETE_CHAR_SHORTCUTS =
            "DELETE FROM character_shortcuts WHERE charId=? AND class_index=?";

    // Character Transformation SQL String Definitions:
    private static final String SELECT_CHAR_TRANSFORM = "SELECT transform_id FROM characters WHERE charId=?";
    private static final String UPDATE_CHAR_TRANSFORM = "UPDATE characters SET transform_id=? WHERE charId=?";

    // Character zone restart time SQL String Definitions - L2Master mod
    private static final String DELETE_ZONE_RESTART_LIMIT =
            "DELETE FROM character_norestart_zone_time WHERE charId = ?";
    private static final String LOAD_ZONE_RESTART_LIMIT =
            "SELECT time_limit FROM character_norestart_zone_time WHERE charId = ?";
    private static final String UPDATE_ZONE_RESTART_LIMIT =
            "REPLACE INTO character_norestart_zone_time (charId, time_limit) VALUES (?,?)";

    // Character account data SQL String Definitions:
    private static final String UPDATE_ACCOUNT_GSDATA =
            "UPDATE account_gsdata SET value=? WHERE account_name=? AND var=?";
    private static final String RESTORE_ACCOUNT_GSDATA =
            "SELECT value from account_gsdata where account_name=? and var=?;";

    public static final int REQUEST_TIMEOUT = 15;
    public static final int STORE_PRIVATE_NONE = 0;
    public static final int STORE_PRIVATE_SELL = 1;
    public static final int STORE_PRIVATE_BUY = 3;
    public static final int STORE_PRIVATE_MANUFACTURE = 5;
    public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
    public static final int STORE_PRIVATE_CUSTOM_SELL = 10;

    /**
     * The table containing all minimum level needed for each Expertise (None, D, C, B, A, S, S80, S84)
     */
    private static final int[] EXPERTISE_LEVELS = {
            SkillTreeTable.getInstance().getExpertiseLevel(0), //NONE
            SkillTreeTable.getInstance().getExpertiseLevel(1), //D
            SkillTreeTable.getInstance().getExpertiseLevel(2), //C
            SkillTreeTable.getInstance().getExpertiseLevel(3), //B
            SkillTreeTable.getInstance().getExpertiseLevel(4), //A
            SkillTreeTable.getInstance().getExpertiseLevel(5), //S
            SkillTreeTable.getInstance().getExpertiseLevel(6), //S80
            SkillTreeTable.getInstance().getExpertiseLevel(7), //S84
            SkillTreeTable.getInstance().getExpertiseLevel(8), //R
            SkillTreeTable.getInstance().getExpertiseLevel(9), //R90
            SkillTreeTable.getInstance().getExpertiseLevel(10) //R99
    };

    private static final int[] COMMON_CRAFT_LEVELS = {5, 20, 28, 36, 43, 49, 55, 62, 70};

    public class AIAccessor extends L2Character.AIAccessor
    {
        protected AIAccessor()
        {

        }

        public L2PcInstance getPlayer()
        {
            return L2PcInstance.this;
        }

        public void doPickupItem(L2Object object)
        {
            L2PcInstance.this.doPickupItem(object);
        }

        public void doInteract(L2Character target)
        {
            L2PcInstance.this.doInteract(target);
        }

        @Override
        public void doAttack(L2Character target)
        {
            super.doAttack(target);

            // cancel the recent fake-death protection instantly if the player attacks or casts spells
            getPlayer().setRecentFakeDeath(false);
        }

        @Override
        public void doCast(L2Skill skill, boolean second)
        {
            super.doCast(skill, second);

            // cancel the recent fake-death protection instantly if the player attacks or casts spells
            getPlayer().setRecentFakeDeath(false);
        }
    }

    private L2GameClient _client;

    private String _accountName;
    private long _deleteTimer;
    private long _creationTime;

    private volatile boolean _isOnline = false;
    private long _onlineTime;
    private long _onlineBeginTime;
    private long _lastAccess;
    private long _uptime;
    private long _zoneRestartLimitTime = 0;

    private final ReentrantLock _subclassLock = new ReentrantLock();
    protected int _templateId;
    protected int _baseClass;
    protected int _activeClass;
    protected int _classIndex = 0;
    private PlayerClass _currentClass;

    /**
     * data for mounted pets
     */
    private int _controlItemId;
    private L2PetData _data;
    private L2PetLevelData _leveldata;
    private int _curFeed;
    protected Future<?> _mountFeedTask;
    private ScheduledFuture<?> _dismountTask;
    private boolean _petItems = false;

    /**
     * The list of sub-classes this character has.
     */
    private Map<Integer, SubClass> _subClasses;

    private PcAppearance _appearance;

    /**
     * The Identifier of the L2PcInstance
     */
    private int _charId = 0x00030b7a;

    /**
     * The Experience of the L2PcInstance before the last Death Penalty
     */
    private long _expBeforeDeath;

    /**
     * The Reputation of the L2PcInstance (if lower than 0, the name of the L2PcInstance appears in red, otherwise if greater the name appears in red)
     */
    private int _reputation;

    /**
     * The number of player killed during a PvP (the player killed was PvP Flagged)
     */
    private int _pvpKills;

    /**
     * The PK counter of the L2PcInstance (= Number of non PvP Flagged player killed)
     */
    private int _pkKills;

    /**
     * The PvP Flag state of the L2PcInstance (0=White, 1=Purple)
     */
    private byte _pvpFlag;

    /**
     * The Fame of this L2PcInstance
     */
    private int _fame;
    private ScheduledFuture<?> _fameTask;

    private ScheduledFuture<?> _teleportWatchdog;

    /**
     * The Siege state of the L2PcInstance
     */
    private byte _siegeState = 0;

    /**
     * The id of castle/fort which the L2PcInstance is registered for siege
     */
    private int _siegeSide = 0;

    private int _curWeightPenalty = 0;

    private int _lastCompassZone; // the last compass zone update send to the client

    private final L2ContactList _contactList = new L2ContactList(this);

    private int _bookmarkslot = 0; // The Teleport Bookmark Slot

    private List<TeleportBookmark> tpbookmark = new ArrayList<>();

    private PunishLevel _punishLevel = PunishLevel.NONE;
    private long _punishTimer = 0;
    private ScheduledFuture<?> _punishTask;

    public enum PunishLevel
    {
        NONE(0, ""), CHAT(1, "chat banned"), JAIL(2, "jailed"), CHAR(3, "banned"), ACC(4, "banned");

        private final int punValue;
        private final String punString;

        PunishLevel(int value, String string)
        {
            punValue = value;
            punString = string;
        }

        public int value()
        {
            return punValue;
        }

        public String string()
        {
            return punString;
        }
    }

    /**
     * Olympiad
     */
    private boolean _inOlympiadMode = false;
    private boolean _OlympiadStart = false;
    private int _olympiadGameId = -1;
    private int _olympiadSide = -1;
    public int olyBuff = 0;

    /**
     * Duel
     */
    private boolean _isInDuel = false;
    private int _duelState = Duel.DUELSTATE_NODUEL;
    private int _duelId = 0;
    private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;

    /**
     * Boat and AirShip
     */
    private L2Vehicle _vehicle = null;
    private Point3D _inVehiclePosition;

    public ScheduledFuture<?> _taskforfish;
    private int _mountType;
    private int _mountNpcId;
    private int _mountLevel;
    /**
     * Store object used to summon the strider you are mounting
     **/
    private int _mountObjectID = 0;

    public int _telemode = 0;

    private boolean _inCrystallize;
    private boolean _inCraftMode;

    private long _offlineShopStart = 0;

    private L2Transformation _transformation;
    private int _transformationId = 0;

    /**
     * The table containing all L2RecipeList of the L2PcInstance
     */
    private Map<Integer, L2RecipeList> _dwarvenRecipeBook = new HashMap<>();
    private Map<Integer, L2RecipeList> _commonRecipeBook = new HashMap<>();

    /**
     * Premium Items
     */
    private Map<Integer, L2PremiumItem> _premiumItems = new HashMap<>();

    /**
     * True if the L2PcInstance is sitting
     */
    private boolean _waitTypeSitting;

    /**
     * Location before entering Observer Mode
     */
    private int _lastX;
    private int _lastY;
    private int _lastZ;
    private boolean _observerMode = false;

    /**
     * Stored from last ValidatePosition
     **/
    private Point3D _lastServerPosition = new Point3D(0, 0, 0);

    /**
     * The number of recommendation obtained by the L2PcInstance
     */
    private int _recomHave; // how much I was recommended by others
    /**
     * The number of recommendation that the L2PcInstance can give
     */
    private int _recomLeft; // how many recommendations I can give to others
    /**
     * Recommendation Bonus task
     **/
    private ScheduledFuture<?> _recoBonusTask;
    /**
     * Recommendation task
     **/
    private ScheduledFuture<?> _recoGiveTask;
    /**
     * Recommendation Two Hours bonus
     **/
    private boolean _recoTwoHoursGiven = false;

    private PcInventory _inventory = new PcInventory(this);
    private final PcAuction _auctionInventory = new PcAuction(this);
    private PcWarehouse _warehouse;
    private PcRefund _refund;

    private PetInventory _petInv;

    /**
     * The Private Store type of the L2PcInstance (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5)
     */
    private int _privatestore;

    private TradeList _activeTradeList;
    private ItemContainer _activeWarehouse;
    private L2ManufactureList _createList;
    private TradeList _sellList;
    private TradeList _buyList;

    // Multisell
    private PreparedListContainer _currentMultiSell = null;

    /**
     * Bitmask used to keep track of one-time/newbie quest rewards
     */
    private int _newbie;

    private boolean _noble = false;
    private boolean _hero = false;
    private boolean _isCoCWinner = false;

    private boolean _hasIdentityCrisis = false;

    public boolean hasIdentityCrisis()
    {
        return _hasIdentityCrisis;
    }

    public void setHasIdentityCrisis(boolean hasIdentityCrisis)
    {
        _hasIdentityCrisis = hasIdentityCrisis;
    }

    /**
     * The L2FolkInstance corresponding to the last Folk wich one the player talked.
     */
    private L2Npc _lastFolkNpc = null;

    /**
     * Last NPC Id talked on a quest
     */
    private int _questNpcObject = 0;

    /**
     * The table containing all Quests began by the L2PcInstance
     */
    private Map<String, QuestState> _quests = new HashMap<>();

    /**
     * The list containing all shortCuts of this L2PcInstance
     */
    private ShortCuts _shortCuts = new ShortCuts(this);

    /**
     * The list containing all macroses of this L2PcInstance
     */
    private MacroList _macroses = new MacroList(this);

    private List<L2PcInstance> _snoopListener = new ArrayList<>();
    private List<L2PcInstance> _snoopedPlayer = new ArrayList<>();

    /**
     * The player stores who is observing his land rates
     */
    private ArrayList<L2PcInstance> _landrateObserver = new ArrayList<>();
    /**
     * The GM stores player references whose land rates he observes
     */
    private ArrayList<L2PcInstance> _playersUnderLandrateObservation = new ArrayList<>();
    /**
     * This flag shows land rate observation activity. Means - either observing or under observation
     */
    private boolean _landrateObservationActive = false;

    // hennas
    private final L2Henna[] _henna = new L2Henna[4];
    private int _hennaSTR;
    private int _hennaINT;
    private int _hennaDEX;
    private int _hennaMEN;
    private int _hennaWIT;
    private int _hennaCON;
    private int _hennaLUC;
    private int _hennaCHA;
    private int[] _hennaElem = new int[6];

    /**
     * The pet of the L2PcInstance
     */
    private L2PetInstance _pet = null;
    private List<L2SummonInstance> _summons = new CopyOnWriteArrayList<>();
    private L2SummonInstance _activeSummon = null;
    private boolean _summonsInDefendingMode = false;
    /**
     * The buff set of a died servitor
     */
    private L2Abnormal[] _summonBuffs = null;
    /**
     * NPC id of died servitor
     */
    private int _lastSummonId = 0;
    /**
     * The L2DecoyInstance of the L2PcInstance
     */
    private L2DecoyInstance _decoy = null;
    /**
     * The L2Trap of the L2PcInstance
     */
    private L2Trap _trap = null;
    /**
     * The L2Agathion of the L2PcInstance
     */
    private int _agathionId = 0;
    // apparently, a L2PcInstance CAN have both a summon AND a tamed beast at the same time!!
    // after Freya players can control more than one tamed beast
    private List<L2TamedBeastInstance> _tamedBeast = null;

    // client radar
    //TODO: This needs to be better intergrated and saved/loaded
    private L2Radar _radar;

    // Party matching
    // private int _partymatching = 0;
    private int _partyroom = 0;
    // private int _partywait = 0;

    // Clan related attributes
    /**
     * The Clan Identifier of the L2PcInstance
     */
    private int _clanId;

    /**
     * The Clan object of the L2PcInstance
     */
    private L2Clan _clan;

    /**
     * Apprentice and Sponsor IDs
     */
    private int _apprentice = 0;
    private int _sponsor = 0;

    private long _clanJoinExpiryTime;
    private long _clanCreateExpiryTime;

    private int _powerGrade = 0;
    private int _clanPrivileges = 0;

    /**
     * L2PcInstance's pledge class (knight, Baron, etc.)
     */
    private int _pledgeClass = 0;
    private int _pledgeType = 0;

    /**
     * Level at which the player joined the clan as an academy member
     */
    private int _lvlJoinedAcademy = 0;

    private int _wantsPeace = 0;

    // Breath of Shilen Debuff Level (Works as the new Death Penalty system)
    private int _breathOfShilenDebuffLevel = 0;

    // charges
    private AtomicInteger _charges = new AtomicInteger();
    private ScheduledFuture<?> _chargeTask = null;

    // Absorbed Souls
    private int _souls = 0;
    private ScheduledFuture<?> _soulTask = null;

    private L2AccessLevel _accessLevel;

    private boolean _messageRefusal = false; // message refusal mode

    private boolean _silenceMode = false; // silence mode
    private boolean _dietMode = false; // ignore weight penalty
    private boolean _tradeRefusal = false; // Trade refusal
    private boolean _exchangeRefusal = false; // Exchange refusal

    private L2Party _party;

    // this is needed to find the inviting player for Party response
    // there can only be one active party request at once
    private L2PcInstance _activeRequester;
    private long _requestExpireTime = 0;
    private L2Request _request = new L2Request(this);
    private L2ItemInstance _arrowItem;
    private L2ItemInstance _boltItem;

    // Used for protection after teleport
    private long _protectEndTime = 0;

    public boolean isSpawnProtected()
    {
        return _protectEndTime > TimeController.getGameTicks();
    }

    private long _teleportProtectEndTime = 0;

    public boolean isTeleportProtected()
    {
        return _teleportProtectEndTime > TimeController.getGameTicks() &&
                (_event == null || _event.isType(EventType.Survival) || _event.isType(EventType.VIP) ||
                        _event.isType(EventType.StalkedSalkers));
    }

    // protects a char from agro mobs when getting up from fake death
    private long _recentFakeDeathEndTime = 0;
    private boolean _isFakeDeath;

    /**
     * The fists L2Weapon of the L2PcInstance (used when no weapon is equiped)
     */
    private L2Weapon _fistsWeaponItem;

    private final Map<Integer, String> _chars = new HashMap<>();

    //private byte _updateKnownCounter = 0;

    /**
     * The current higher Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)
     */
    private int _expertiseIndex; // index in EXPERTISE_LEVELS
    private int _expertiseArmorPenalty = 0;
    private int _expertiseWeaponPenalty = 0;

    private boolean _isEnchanting = false;
    private L2ItemInstance _activeEnchantItem = null;
    private L2ItemInstance _activeEnchantSupportItem = null;
    private L2ItemInstance _activeEnchantAttrItem = null;
    private long _activeEnchantTimestamp = 0;

    protected boolean _inventoryDisable = false;

    protected Map<Integer, L2CubicInstance> _cubics = new ConcurrentHashMap<>();

    /**
     * Active shots.
     */
    private final L2ItemInstance[] _activeSoulShots = new L2ItemInstance[4];
    private final boolean[] _disabledShoulShots = new boolean[4];

    public final ReentrantLock consumableLock = new ReentrantLock();

    private byte _handysBlockCheckerEventArena = -1;

    /**
     * new loto ticket
     **/
    private int _loto[] = new int[5];
    //public static int _loto_nums[] = {0,1,2,3,4,5,6,7,8,9,};
    /**
     * new race ticket
     **/
    private int _race[] = new int[2];

    private final BlockList _blockList = new BlockList(this);

    private int _team = 0;

    /**
     * lvl of alliance with ketra orcs or varka silenos, used in quests and aggro checks
     * [-5,-1] varka, 0 neutral, [1,5] ketra
     */
    private int _alliedVarkaKetra = 0;

    private L2Fishing _fishCombat;
    private boolean _fishing = false;
    private int _fishx = 0;
    private int _fishy = 0;
    private int _fishz = 0;

    private int[] _transformAllowedSkills = {};
    private ScheduledFuture<?> _taskRentPet;
    private ScheduledFuture<?> _taskWater;

    /**
     * Bypass validations
     */
    private List<String> _validBypass = new ArrayList<>();
    private List<String> _validBypass2 = new ArrayList<>();

    private Forum _forumMail;
    private Forum _forumMemo;

    /**
     * Current skill in use. Note that L2Character has _lastSkillCast, but
     * this has the button presses
     */
    private SkillDat _currentSkill;
    private SkillDat _currentPetSkill;

    /**
     * Skills queued because a skill is already in progress
     */
    private SkillDat _queuedSkill;

    private int _cursedWeaponEquippedId = 0;
    private boolean _combatFlagEquippedId = false;

    private boolean _reviveRequested = false;
    private double _revivePower = 0;
    private boolean _revivePet = false;

    private double _cpUpdateIncCheck = .0;
    private double _cpUpdateDecCheck = .0;
    private double _cpUpdateInterval = .0;
    private double _mpUpdateIncCheck = .0;
    private double _mpUpdateDecCheck = .0;
    private double _mpUpdateInterval = .0;

    private boolean _isRidingStrider = false;
    private boolean _isFlyingMounted = false;

    /**
     * Char Coords from Client
     */
    private int _clientX;
    private int _clientY;
    private int _clientZ;
    private int _clientHeading;

    // during fall validations will be disabled for 10 ms.
    private static final int FALLING_VALIDATION_DELAY = 10000;
    private volatile long _fallingTimestamp = 0;

    private int _multiSocialTarget = 0;
    private int _multiSociaAction = 0;

    private int _movieId = 0;

    private String _adminConfirmCmd = null;

    private volatile long _lastItemAuctionInfoRequest = 0;

    private Future<?> _PvPRegTask;

    private long _pvpFlagLasts;

    private long _fightStanceTime;

    public long getFightStanceTime()
    {
        return _fightStanceTime;
    }

    public void setFightStanceTime(long time)
    {
        _fightStanceTime = time;
    }

    public void setPvpFlagLasts(long time)
    {
        _pvpFlagLasts = time;
    }

    public long getPvpFlagLasts()
    {
        return _pvpFlagLasts;
    }

    public void startPvPFlag()
    {
        updatePvPFlag(1);

        _PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000, 1000);
    }

    public void stopPvpRegTask()
    {
        if (_PvPRegTask != null)
        {
            _PvPRegTask.cancel(true);
        }
    }

    public void stopPvPFlag()
    {
        stopPvpRegTask();

        updatePvPFlag(0);

        _PvPRegTask = null;
    }

    /**
     * Task lauching the function stopPvPFlag()
     */
    private class PvPFlag implements Runnable
    {
        public PvPFlag()
        {

        }

        @Override
        public void run()
        {
            try
            {
                if (System.currentTimeMillis() > getPvpFlagLasts())
                {
                    stopPvPFlag();
                }
                else if (System.currentTimeMillis() > getPvpFlagLasts() - 20000)
                {
                    updatePvPFlag(2);
                }
                else
                {
                    updatePvPFlag(1);
                    // Start a new PvP timer check
                    //checkPvPFlag();
                }
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING, "error in pvp flag task:", e);
            }
        }
    }

    // Character UI
    private L2UIKeysSettings _uiKeySettings;

    // Damage that the character gave in olys
    private int _olyGivenDmg = 0;

    private EventTeam _ctfFlag = null;
    private int _eventPoints;

    private final int EVENT_SAVED_EFFECTS_SIZE = 60;
    private int _eventSavedTime = 0;
    private Point3D _eventSavedPosition = null;
    private L2Abnormal[] _eventSavedEffects = new L2Abnormal[EVENT_SAVED_EFFECTS_SIZE];
    private L2Abnormal[] _eventSavedSummonEffects = new L2Abnormal[EVENT_SAVED_EFFECTS_SIZE];
    // TODO for new summon system

    // Has written .itemid?
    private boolean _isItemId = false;

    // Title color
    private String _titleColor;

    //LasTravel
    private Map<String, Boolean> _playerConfigs = new HashMap<>();
    private String _publicIP;
    private String _internalIP;

    //Captcha
    private String _captcha;
    private int _botLevel = 0;

    // CokeMobs
    private boolean _mobSummonRequest = false;
    private L2ItemInstance _mobSummonItem;
    private boolean _mobSummonExchangeRequest = false;
    //private L2MobSummonInstance _mobSummonExchange;

    // Tenkai Events
    private EventInstance _event;
    private boolean _wasInEvent = false;

    // .noexp Command
    private boolean _noExp = false;

    // .landrates Command
    private boolean _landRates = false;

    // .stabs Command
    private boolean _stabs = false;

    // had Store Activity?
    private boolean _hadStoreActivity = false;

    // Stalker Hints Task
    private StalkerHintsTask _stalkerHintsTask;

    // Chess
    private boolean _chessChallengeRequest = false;
    private L2PcInstance _chessChallenger;

    // Magic Gem
    private L2Spawn[] _npcServitors = new L2Spawn[4];

    // Images
    private List<Integer> _receivedImages = new ArrayList<>();

    // Event disarm
    private boolean _eventDisarmed = false;

    // .sell stuff
    private TradeList _customSellList;
    private boolean _isAddSellItem = false;
    private int _addSellPrice = -1;

    /**
     * Herbs Task Time
     **/
    private int _herbstask = 0;

    /**
     * Task for Herbs
     */
    private class HerbTask implements Runnable
    {
        private String _process;
        private int _itemId;
        private long _count;
        private L2Object _reference;
        private boolean _sendMessage;

        HerbTask(String process, int itemId, long count, L2Object reference, boolean sendMessage)
        {
            _process = process;
            _itemId = itemId;
            _count = count;
            _reference = reference;
            _sendMessage = sendMessage;
        }

        @Override
        public void run()
        {
            try
            {
                addItem(_process, _itemId, _count, _reference, _sendMessage);
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING, "", e);
            }
        }
    }

    /**
     * ShortBuff clearing Task
     */
    ScheduledFuture<?> _shortBuffTask = null;

    private class ShortBuffTask implements Runnable
    {
        @Override
        public void run()
        {
            L2PcInstance.this.sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
            setShortBuffTaskSkillId(0);
        }
    }

    // L2JMOD Wedding
    private boolean _married = false;
    private int _partnerId = 0;
    private int _coupleId = 0;
    private boolean _engagerequest = false;
    private int _engageid = 0;
    private boolean _marryrequest = false;
    private boolean _marryaccepted = false;

    /**
     * Skill casting information (used to queue when several skills are cast in a short time)
     **/
    public static class SkillDat
    {
        private L2Skill _skill;
        private boolean _ctrlPressed;
        private boolean _shiftPressed;

        protected SkillDat(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
        {
            _skill = skill;
            _ctrlPressed = ctrlPressed;
            _shiftPressed = shiftPressed;
        }

        public boolean isCtrlPressed()
        {
            return _ctrlPressed;
        }

        public boolean isShiftPressed()
        {
            return _shiftPressed;
        }

        public L2Skill getSkill()
        {
            return _skill;
        }

        public int getSkillId()
        {
            return getSkill() != null ? getSkill().getId() : -1;
        }
    }

    //summon friend
    private SummonRequest _summonRequest = new SummonRequest();

    private static class SummonRequest
    {
        private L2PcInstance _target = null;
        private L2Skill _skill = null;

        public void setTarget(L2PcInstance destination, L2Skill skill)
        {
            _target = destination;
            _skill = skill;
        }

        public L2PcInstance getTarget()
        {
            return _target;
        }

        public L2Skill getSkill()
        {
            return _skill;
        }
    }

    // open/close gates
    private GatesRequest _gatesRequest = new GatesRequest();

    private static class GatesRequest
    {
        private L2DoorInstance _target = null;

        public void setTarget(L2DoorInstance door)
        {
            _target = door;
        }

        public L2DoorInstance getDoor()
        {
            return _target;
        }
    }

    /**
     * Create a new L2PcInstance and add it in the characters table of the database.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Create a new L2PcInstance with an account name </li>
     * <li>Set the name, the Hair Style, the Hair Color and  the Face type of the L2PcInstance</li>
     * <li>Add the player in the characters table of the database</li><BR><BR>
     *
     * @param objectId    Identifier of the object to initialized
     * @param template    The L2PcTemplate to apply to the L2PcInstance
     * @param accountName The name of the L2PcInstance
     * @param name        The name of the L2PcInstance
     * @param hairStyle   The hair style Identifier of the L2PcInstance
     * @param hairColor   The hair color Identifier of the L2PcInstance
     * @param face        The face type Identifier of the L2PcInstance
     * @return The L2PcInstance added to the database or null
     */
    public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, boolean sex, int classId)
    {
        // Create a new L2PcInstance with an account name
        PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
        L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);

        // Set the name of the L2PcInstance
        player.setName(name);

        // Set Character's create time
        player.setCreateTime(System.currentTimeMillis());

        player._templateId = template.getId();

        // Set the base class ID to that of the actual class ID.
        player.setBaseClass(classId);
        player._currentClass = PlayerClassTable.getInstance().getClassById(classId);
        // Kept for backwards compabitility.
        player.setNewbie(1);
        // Add the player in the characters table of the database
        boolean ok = player.createDb();

        if (!ok)
        {
            return null;
        }

        return player;
    }

    public static L2PcInstance createDummyPlayer(int objectId, String name)
    {
        // Create a new L2PcInstance with an account name
        L2PcInstance player = new L2PcInstance(objectId);
        player.setName(name);

        return player;
    }

    public String getAccountName()
    {
        if (getClient() == null)
        {
            return getAccountNamePlayer();
        }
        return getClient().getAccountName();
    }

    public String getAccountNamePlayer()
    {
        return _accountName;
    }

    public Map<Integer, String> getAccountChars()
    {
        return _chars;
    }

    public int getRelation(L2PcInstance target)
    {
        int result = 0;

        if (getClan() != null)
        {
            result |= RelationChanged.RELATION_CLAN_MEMBER;
            if (getClan() == target.getClan())
            {
                result |= RelationChanged.RELATION_CLAN_MATE;
            }
            if (getAllyId() != 0)
            {
                result |= RelationChanged.RELATION_ALLY_MEMBER;
            }
        }
        if (isClanLeader())
        {
            result |= RelationChanged.RELATION_LEADER;
        }
        if (getParty() != null && getParty() == target.getParty())
        {
            result |= RelationChanged.RELATION_HAS_PARTY;
            for (int i = 0; i < getParty().getPartyMembers().size(); i++)
            {
                if (getParty().getPartyMembers().get(i) != this)
                {
                    continue;
                }
                switch (i)
                {
                    case 0:
                        result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
                        break;
                    case 1:
                        result |= RelationChanged.RELATION_PARTY4; // 0x8
                        break;
                    case 2:
                        result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2 +
                                RelationChanged.RELATION_PARTY1; // 0x7
                        break;
                    case 3:
                        result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2; // 0x6
                        break;
                    case 4:
                        result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY1; // 0x5
                        break;
                    case 5:
                        result |= RelationChanged.RELATION_PARTY3; // 0x4
                        break;
                    case 6:
                        result |= RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x3
                        break;
                    case 7:
                        result |= RelationChanged.RELATION_PARTY2; // 0x2
                        break;
                    case 8:
                        result |= RelationChanged.RELATION_PARTY1; // 0x1
                        break;
                }
            }
        }
        if (getSiegeState() != 0)
        {
            result |= RelationChanged.RELATION_INSIEGE;
            if (getSiegeState() != target.getSiegeState())
            {
                result |= RelationChanged.RELATION_ENEMY;
            }
            else
            {
                result |= RelationChanged.RELATION_ALLY;
            }
            if (getSiegeState() == 1)
            {
                result |= RelationChanged.RELATION_ATTACKER;
            }
        }
        if (getClan() != null && target.getClan() != null)
        {
            if ((target.getPledgeType() != L2Clan.SUBUNIT_ACADEMY || target.getLevel() > 70) &&
                    (getPledgeType() != L2Clan.SUBUNIT_ACADEMY || getLevel() > 70))
            {
                if (target.getClan().getClansAtWarQueue().contains(getClan()) &&
                        getClan().getClansAtWarQueue().contains(target.getClan()))
                {
                    result |= RelationChanged.RELATION_WAR_STARTED;
                }
                if (target.getClan().getStartedWarList().contains(getClan()) &&
                        getClan().getStartedWarList().contains(target.getClan()))
                {
                    result |= RelationChanged.RELATION_WAR_ABOUT_TO_BEGIN;
                    result |= RelationChanged.RELATION_WAR_STARTED;
                }
            }
        }
        if (getBlockCheckerArena() != -1)
        {
            result |= RelationChanged.RELATION_INSIEGE;
            ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
            if (holder.getPlayerTeam(this) == 0)
            {
                result |= RelationChanged.RELATION_ENEMY;
            }
            else
            {
                result |= RelationChanged.RELATION_ALLY;
            }
            result |= RelationChanged.RELATION_ATTACKER;
        }
        return result;
    }

    /**
     * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world (call restore method).<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Retrieve the L2PcInstance from the characters table of the database </li>
     * <li>Add the L2PcInstance object in _allObjects </li>
     * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
     * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
     *
     * @param objectId Identifier of the object to initialized
     * @return The L2PcInstance loaded from the database
     */
    public static L2PcInstance load(int objectId)
    {
        return restore(objectId);
    }

    private void initPcStatusUpdateValues()
    {
        _cpUpdateInterval = getMaxCp() / 352.0;
        _cpUpdateIncCheck = getMaxCp();
        _cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
        _mpUpdateInterval = getMaxMp() / 352.0;
        _mpUpdateIncCheck = getMaxMp();
        _mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
    }

    /**
     * Constructor of L2PcInstance (use L2Character constructor).<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2PcInstance </li>
     * <li>Set the name of the L2PcInstance</li><BR><BR>
     * <p>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2PcInstance to 1</B></FONT><BR><BR>
     *
     * @param objectId    Identifier of the object to initialized
     * @param template    The L2PcTemplate to apply to the L2PcInstance
     * @param accountName The name of the account including this L2PcInstance
     */
    protected L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
    {
        super(objectId, template);
        setInstanceType(InstanceType.L2PcInstance);
        super.initCharStatusUpdateValues();
        initPcStatusUpdateValues();

        _accountName = accountName;
        app.setOwner(this);
        _appearance = app;

        // Create an AI
        _ai = new L2PlayerAI(new L2PcInstance.AIAccessor());

        // Create a L2Radar object
        _radar = new L2Radar(this);

        _temporarySkills = new ConcurrentHashMap<>();
    }

    private L2PcInstance(int objectId)
    {
        super(objectId, null);
        setInstanceType(InstanceType.L2PcInstance);
        super.initCharStatusUpdateValues();
        initPcStatusUpdateValues();

        _temporarySkills = new ConcurrentHashMap<>();
    }

    @Override
    public final PcKnownList getKnownList()
    {
        return (PcKnownList) super.getKnownList();
    }

    @Override
    public void initKnownList()
    {
        setKnownList(new PcKnownList(this));
    }

    @Override
    public final PcStat getStat()
    {
        return (PcStat) super.getStat();
    }

    @Override
    public void initCharStat()
    {
        setStat(new PcStat(this));
    }

    @Override
    public final PcStatus getStatus()
    {
        return (PcStatus) super.getStatus();
    }

    @Override
    public void initCharStatus()
    {
        setStatus(new PcStatus(this));
    }

    @Override
    public PcPosition getPosition()
    {
        return (PcPosition) super.getPosition();
    }

    @Override
    public void initPosition()
    {
        setObjectPosition(new PcPosition(this));
    }

    public final PcAppearance getAppearance()
    {
        return _appearance;
    }

    public final L2PcTemplate getOriginalBaseTemplate()
    {
        return CharTemplateTable.getInstance().getTemplate(_templateId);
    }

    /**
     * Return the base L2PcTemplate link to the L2PcInstance.<BR><BR>
     */
    public final L2PcTemplate getBaseTemplate()
    {
        if (_temporaryTemplate != null)
        {
            return _temporaryTemplate;
        }

        return CharTemplateTable.getInstance().getTemplate(_templateId);
    }

    private int _raceAppearance = -1;

    public final L2PcTemplate getVisibleTemplate()
    {
        if (_raceAppearance < 0)
        {
            return getBaseTemplate();
        }

        return CharTemplateTable.getInstance().getTemplate(_raceAppearance);
    }

    public final void setRaceAppearance(int app)
    {
        _raceAppearance = app;
    }

    public final int getRaceAppearance()
    {
        return _raceAppearance;
    }

    /**
     * Return the L2PcTemplate link to the L2PcInstance.
     */
    @Override
    public final L2PcTemplate getTemplate()
    {
        if (_temporaryTemplate != null)
        {
            return _temporaryTemplate;
        }

        return (L2PcTemplate) super.getTemplate();
    }

    /**
     * Return the AI of the L2PcInstance (create it if necessary).<BR><BR>
     */
    @Override
    public L2CharacterAI getAI()
    {
        L2CharacterAI ai = _ai; // copy handle
        if (ai == null)
        {
            synchronized (this)
            {
                if (_ai == null)
                {
                    _ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
                }

                return _ai;
            }
        }
        return ai;
    }

    /**
     * Return the Level of the L2PcInstance.
     */
    @Override
    public final int getLevel()
    {
        if (_temporaryLevel != 0)
        {
            return _temporaryLevel;
        }

        return getStat().getLevel();
    }

    /**
     * For skill learning purposes only. It will return the base class level if the player is on a subclass
     */
    public final int getDualLevel()
    {
        if (isSubClassActive())
        {
            return getStat().getBaseClassLevel();
        }

        if (_subClasses != null)
        {
            for (SubClass sub : _subClasses.values())
            {
                if (sub.isDual())
                {
                    return sub.getLevel();
                }
            }
        }

        return 1;
    }

    /**
     * Return the _newbie rewards state of the L2PcInstance.<BR><BR>
     */
    public int getNewbie()
    {
        return _newbie;
    }

    /**
     * Set the _newbie rewards state of the L2PcInstance.<BR><BR>
     *
     * @param newbieRewards The Identifier of the _newbie state<BR><BR>
     */
    public void setNewbie(int newbieRewards)
    {
        _newbie = newbieRewards;
    }

    public void setBaseClass(int baseClass)
    {
        _baseClass = baseClass;
    }

    public void addRaceSkills()
    {
        // Ertheias get their race skills at lvl 1
        if (getRace() == Race.Ertheia && getLevel() == 1)
        {
            for (int skillId : getTemplate().getSkillIds())
            {
                addSkill(SkillTable.getInstance().getInfo(skillId, 1), true);
            }
        }
        else if (getLevel() >= 85 && (getRace() == Race.Ertheia && getBaseClass() >= 188 ||
                getRace() != Race.Ertheia && getBaseClass() >= 139))
        {
            for (int skillId : getTemplate().getSkillIds())
            {
                int playerSkillLevel = getSkillLevelHash(skillId);
                int maxSkillLevel = SkillTable.getInstance().getMaxLevel(skillId);

                if (playerSkillLevel < maxSkillLevel)
                {
                    addSkill(SkillTable.getInstance().getInfo(skillId, maxSkillLevel), true);
                }
            }
        }
    }

    public void setBaseClass(PlayerClass cl)
    {
        _baseClass = cl.getId();
    }

    public boolean isInStoreMode()
    {
        return getPrivateStoreType() > 0;
    }

    //	public boolean isInCraftMode() { return (getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE); }

    public boolean isInCraftMode()
    {
        return _inCraftMode;
    }

    public void isInCraftMode(boolean b)
    {
        _inCraftMode = b;
    }

    /**
     * Manage Logout Task: <li>Remove player from world <BR>
     * </li> <li>Save player data into DB <BR>
     * </li> <BR>
     * <BR>
     */
    public void logout()
    {
        logout(true);
    }

    /**
     * Manage Logout Task: <li>Remove player from world <BR>
     * </li> <li>Save player data into DB <BR>
     * </li> <BR>
     * <BR>
     *
     * @param closeClient
     */
    public void logout(boolean closeClient)
    {
        try
        {
            closeNetConnection(closeClient);
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Exception on logout(): " + e.getMessage(), e);
        }
    }

    /**
     * Return a table containing all Common L2RecipeList of the L2PcInstance.<BR><BR>
     */
    public L2RecipeList[] getCommonRecipeBook()
    {
        return _commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
    }

    /**
     * Return a table containing all Dwarf L2RecipeList of the L2PcInstance.<BR><BR>
     */
    public L2RecipeList[] getDwarvenRecipeBook()
    {
        return _dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
    }

    /**
     * Add a new L2RecipList to the table _commonrecipebook containing all L2RecipeList of the L2PcInstance <BR><BR>
     *
     * @param recipe The L2RecipeList to add to the _recipebook
     */
    public void registerCommonRecipeList(L2RecipeList recipe, boolean saveToDb)
    {
        if (recipe == null)
        {
            return;
        }

        _commonRecipeBook.put(recipe.getId(), recipe);

        if (saveToDb)
        {
            insertNewRecipeData(recipe.getId(), false);
        }
    }

    /**
     * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2PcInstance <BR><BR>
     *
     * @param recipe The L2RecipeList to add to the _recipebook
     */
    public void registerDwarvenRecipeList(L2RecipeList recipe, boolean saveToDb)
    {
        if (recipe == null)
        {
            return;
        }

        _dwarvenRecipeBook.put(recipe.getId(), recipe);

        if (saveToDb)
        {
            insertNewRecipeData(recipe.getId(), true);
        }
    }

    /**
     * @return <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
     */
    public boolean hasRecipeList(int recipeId)
    {
        if (_dwarvenRecipeBook.containsKey(recipeId))
        {
            return true;
        }
        else if (_commonRecipeBook.containsKey(recipeId))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Tries to remove a L2RecipList from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all L2RecipeList of the L2PcInstance <BR><BR>
     *
     */
    public void unregisterRecipeList(int recipeId)
    {
        if (_dwarvenRecipeBook.remove(recipeId) != null)
        {
            deleteRecipeData(recipeId, true);
        }
        else if (_commonRecipeBook.remove(recipeId) != null)
        {
            deleteRecipeData(recipeId, false);
        }
        else
        {
            Log.warning("Attempted to remove unknown RecipeList: " + recipeId);
        }

        L2ShortCut[] allShortCuts = getAllShortCuts();

        for (L2ShortCut sc : allShortCuts)
        {
            if (sc != null && sc.getId() == recipeId && sc.getType() == L2ShortCut.TYPE_RECIPE)
            {
                deleteShortCut(sc.getSlot(), sc.getPage());
            }
        }
    }

    private void insertNewRecipeData(int recipeId, boolean isDwarf)
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO character_recipebook (charId, id, classIndex, type) values(?,?,?,?)");
            statement.setInt(1, getObjectId());
            statement.setInt(2, recipeId);
            statement.setInt(3, isDwarf ? _classIndex : 0);
            statement.setInt(4, isDwarf ? 1 : 0);
            statement.execute();
            statement.close();
        }
        catch (SQLException e)
        {
            if (Log.isLoggable(Level.SEVERE))
            {
                Log.log(Level.SEVERE,
                        "SQL exception while inserting recipe: " + recipeId + " from character " + getObjectId(), e);
            }
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    private void deleteRecipeData(int recipeId, boolean isDwarf)
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement =
                    con.prepareStatement("DELETE FROM character_recipebook WHERE charId=? AND id=? AND classIndex=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, recipeId);
            statement.setInt(3, isDwarf ? _classIndex : 0);
            statement.execute();
            statement.close();
        }
        catch (SQLException e)
        {
            if (Log.isLoggable(Level.SEVERE))
            {
                Log.log(Level.SEVERE,
                        "SQL exception while deleting recipe: " + recipeId + " from character " + getObjectId(), e);
            }
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * Returns the Id for the last talked quest NPC.<BR><BR>
     */
    public int getLastQuestNpcObject()
    {
        return _questNpcObject;
    }

    public void setLastQuestNpcObject(int npcId)
    {
        _questNpcObject = npcId;
    }

    /**
     * Return the QuestState object corresponding to the quest name.<BR><BR>
     *
     * @param quest The name of the quest
     */
    public QuestState getQuestState(String quest)
    {
        return _quests.get(quest);
    }

    /**
     * Add a QuestState to the table _quest containing all quests began by the L2PcInstance.<BR><BR>
     *
     * @param qs The QuestState to add to _quest
     */
    public void setQuestState(QuestState qs)
    {
        _quests.put(qs.getQuestName(), qs);
    }

    /**
     * Remove a QuestState from the table _quest containing all quests began by the L2PcInstance.<BR><BR>
     *
     * @param quest The name of the quest
     */
    public void delQuestState(String quest)
    {
        _quests.remove(quest);
    }

    private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state)
    {
        int len = questStateArray.length;
        QuestState[] tmp = new QuestState[len + 1];
        for (int i = 0; i < len; i++)
        {
            tmp[i] = questStateArray[i];
        }
        tmp[len] = state;
        return tmp;
    }

    /**
     * Return a table containing all Quest in progress from the table _quests.<BR><BR>
     */
    public Quest[] getAllActiveQuests()
    {
        ArrayList<Quest> quests = new ArrayList<>();

        for (QuestState qs : _quests.values())
        {
            if (qs == null)
            {
                continue;
            }

            if (qs.getQuest() == null)
            {
                continue;
            }

            int questId = qs.getQuest().getQuestIntId();
            if (questId > 19999 || questId < 1)
            {
                continue;
            }

            if (!qs.isStarted() && !Config.DEVELOPER)
            {
                continue;
            }

            quests.add(qs.getQuest());
        }

        return quests.toArray(new Quest[quests.size()]);
    }

    /**
     * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
     *
     */
    public QuestState[] getQuestsForAttacks(L2Npc npc)
    {
        // Create a QuestState table that will contain all QuestState to modify
        QuestState[] states = null;

        // Go through the QuestState of the L2PcInstance quests
        for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
        {
            // Check if the Identifier of the L2Attackable attck is needed for the current quest
            if (getQuestState(quest.getName()) != null)
            {
                // Copy the current L2PcInstance QuestState in the QuestState table
                if (states == null)
                {
                    states = new QuestState[]{getQuestState(quest.getName())};
                }
                else
                {
                    states = addToQuestStateArray(states, getQuestState(quest.getName()));
                }
            }
        }

        // Return a table containing all QuestState to modify
        return states;
    }

    /**
     * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
     *
     */
    public QuestState[] getQuestsForKills(L2Npc npc)
    {
        // Create a QuestState table that will contain all QuestState to modify
        QuestState[] states = null;

        // Go through the QuestState of the L2PcInstance quests
        for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
        {
            // Check if the Identifier of the L2Attackable killed is needed for the current quest
            if (getQuestState(quest.getName()) != null)
            {
                // Copy the current L2PcInstance QuestState in the QuestState table
                if (states == null)
                {
                    states = new QuestState[]{getQuestState(quest.getName())};
                }
                else
                {
                    states = addToQuestStateArray(states, getQuestState(quest.getName()));
                }
            }
        }

        // Return a table containing all QuestState to modify
        return states;
    }

    /**
     * Return a table containing all QuestState from the table _quests in which the L2PcInstance must talk to the NPC.<BR><BR>
     *
     * @param npcId The Identifier of the NPC
     */
    public QuestState[] getQuestsForTalk(int npcId)
    {
        // Create a QuestState table that will contain all QuestState to modify
        QuestState[] states = null;

        // Go through the QuestState of the L2PcInstance quests
        Quest[] quests = NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.ON_TALK);
        if (quests != null)
        {
            for (Quest quest : quests)
            {
                if (quest != null)
                {
                    // Copy the current L2PcInstance QuestState in the QuestState table
                    if (getQuestState(quest.getName()) != null)
                    {
                        if (states == null)
                        {
                            states = new QuestState[]{getQuestState(quest.getName())};
                        }
                        else
                        {
                            states = addToQuestStateArray(states, getQuestState(quest.getName()));
                        }
                    }
                }
            }
        }

        // Return a table containing all QuestState to modify
        return states;
    }

    public QuestState processQuestEvent(String quest, String event)
    {
        QuestState retval = null;
        if (event == null)
        {
            event = "";
        }
        QuestState qs = getQuestState(quest);
        if (qs == null && event.length() == 0)
        {
            return retval;
        }
        if (qs == null)
        {
            Quest q = QuestManager.getInstance().getQuest(quest);
            if (q == null)
            {
                return retval;
            }
            qs = q.newQuestState(this);
        }
        if (qs != null)
        {
            if (getLastQuestNpcObject() > 0)
            {
                L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
                if (object instanceof L2Npc &&
                        isInsideRadius(object, ((L2Npc) object).getInteractionDistance(), false, false))
                {
                    L2Npc npc = (L2Npc) object;
                    QuestState[] states = getQuestsForTalk(npc.getNpcId());

                    if (states != null)
                    {
                        for (QuestState state : states)
                        {
                            if (state.getQuest().getName().equals(qs.getQuest().getName()))
                            {
                                if (qs.getQuest().notifyEvent(event, npc, this))
                                {
                                    showQuestWindow(quest, State.getStateName(qs.getState()));
                                }

                                retval = qs;
                            }
                        }
                    }
                }
            }
        }

        return retval;
    }

    private void showQuestWindow(String questId, String stateId)
    {
        String path = Config.DATA_FOLDER + "scripts/quests/" + questId + "/" + stateId + ".htm";
        String content = HtmCache.getInstance().getHtm(getHtmlPrefix(), path); //TODO path for quests html

        if (content != null)
        {
            if (Config.DEBUG)
            {
                Log.fine("Showing quest window for quest " + questId + " state " + stateId + " html path: " + path);
            }

            NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
            npcReply.setHtml(content);
            sendPacket(npcReply);
        }

        sendPacket(ActionFailed.STATIC_PACKET);
    }

    /**
     * List of all QuestState instance that needs to be notified of this L2PcInstance's or its pet's death
     */
    private List<QuestState> _notifyQuestOfDeathList;

    /**
     * Add QuestState instance that is to be notified of L2PcInstance's death.<BR><BR>
     *
     * @param qs The QuestState that subscribe to this event
     */
    public void addNotifyQuestOfDeath(QuestState qs)
    {
        if (qs == null)
        {
            return;
        }

        if (!getNotifyQuestOfDeath().contains(qs))
        {
            getNotifyQuestOfDeath().add(qs);
        }
    }

    /**
     * Remove QuestState instance that is to be notified of L2PcInstance's death.<BR><BR>
     *
     * @param qs The QuestState that subscribe to this event
     */
    public void removeNotifyQuestOfDeath(QuestState qs)
    {
        if (qs == null || _notifyQuestOfDeathList == null)
        {
            return;
        }

        _notifyQuestOfDeathList.remove(qs);
    }

    /**
     * Return a list of QuestStates which registered for notify of death of this L2PcInstance.<BR><BR>
     */
    public final List<QuestState> getNotifyQuestOfDeath()
    {
        if (_notifyQuestOfDeathList == null)
        {
            synchronized (this)
            {
                if (_notifyQuestOfDeathList == null)
                {
                    _notifyQuestOfDeathList = new ArrayList<>();
                }
            }
        }

        return _notifyQuestOfDeathList;
    }

    public final boolean isNotifyQuestOfDeathEmpty()
    {
        return _notifyQuestOfDeathList == null || _notifyQuestOfDeathList.isEmpty();
    }

    /**
     * Return a table containing all L2ShortCut of the L2PcInstance.<BR><BR>
     */
    public L2ShortCut[] getAllShortCuts()
    {
        return _shortCuts.getAllShortCuts();
    }

    public ShortCuts getShortCuts()
    {
        return _shortCuts;
    }

    /**
     * Return the L2ShortCut of the L2PcInstance corresponding to the position (page-slot).<BR><BR>
     *
     * @param slot The slot in wich the shortCuts is equiped
     * @param page The page of shortCuts containing the slot
     */
    public L2ShortCut getShortCut(int slot, int page)
    {
        return _shortCuts.getShortCut(slot, page);
    }

    /**
     * Add a L2shortCut to the L2PcInstance _shortCuts<BR><BR>
     */
    public void registerShortCut(L2ShortCut shortcut)
    {
        _shortCuts.registerShortCut(shortcut);
    }

    /**
     * Delete the L2ShortCut corresponding to the position (page-slot) from the L2PcInstance _shortCuts.<BR><BR>
     */
    public void deleteShortCut(int slot, int page)
    {
        _shortCuts.deleteShortCut(slot, page);
    }

    /**
     * Add a L2Macro to the L2PcInstance _macroses<BR><BR>
     */
    public void registerMacro(L2Macro macro)
    {
        _macroses.registerMacro(macro);
    }

    /**
     * Delete the L2Macro corresponding to the Identifier from the L2PcInstance _macroses.<BR><BR>
     */
    public void deleteMacro(int id)
    {
        _macroses.deleteMacro(id);
    }

    /**
     * Return all L2Macro of the L2PcInstance.<BR><BR>
     */
    public MacroList getMacroses()
    {
        return _macroses;
    }

    /**
     * Set the siege state of the L2PcInstance.<BR><BR>
     * 1 = attacker, 2 = defender, 0 = not involved
     */
    public void setSiegeState(byte siegeState)
    {
        _siegeState = siegeState;
    }

    /**
     * Get the siege state of the L2PcInstance.<BR><BR>
     * 1 = attacker, 2 = defender, 0 = not involved
     */
    public byte getSiegeState()
    {
        return _siegeState;
    }

    /**
     * Set the siege Side of the L2PcInstance.<BR><BR>
     */
    public void setSiegeSide(int val)
    {
        _siegeSide = val;
    }

    public boolean isRegisteredOnThisSiegeField(int val)
    {
        if (_siegeSide != val && (_siegeSide < 81 || _siegeSide > 89))
        {
            return false;
        }
        return true;
    }

    public int getSiegeSide()
    {
        return _siegeSide;
    }

    /**
     * Set the PvP Flag of the L2PcInstance.<BR><BR>
     */
    public void setPvpFlag(int pvpFlag)
    {
        _pvpFlag = (byte) pvpFlag;
    }

    @Override
    public byte getPvpFlag()
    {
        return _pvpFlag;
    }

    @Override
    public void updatePvPFlag(int value)
    {
        if (getPvpFlag() == value)
        {
            return;
        }

        setPvpFlag(value);

        sendPacket(new UserInfo(this));

        // If this player has a pet update the pets pvp flag as well
        if (getPet() != null)
        {
            sendPacket(new RelationChanged(getPet(), getRelation(this), false));
        }
        for (L2SummonInstance summon : getSummons())
        {
            if (summon instanceof L2MobSummonInstance)
            {
                summon.unSummon(this);
                continue;
            }

            sendPacket(new RelationChanged(summon, getRelation(this), false));
        }

        Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
        //synchronized (getKnownList().getKnownPlayers())
        {
            for (L2PcInstance target : plrs)
            {
                target.sendPacket(new RelationChanged(this, getRelation(target), isAutoAttackable(target)));
                if (getPet() != null)
                {
                    target.sendPacket(new RelationChanged(getPet(), getRelation(target), isAutoAttackable(target)));
                }
            }
        }
    }

    @Override
    public void revalidateZone(boolean force)
    {
        // Cannot validate if not in  a world region (happens during teleport)
        if (getWorldRegion() == null)
        {
            return;
        }

        // This function is called too often from movement code
        if (force)
        {
            _zoneValidateCounter = 4;
        }
        else
        {
            _zoneValidateCounter--;
            if (_zoneValidateCounter < 0)
            {
                _zoneValidateCounter = 4;
            }
            else
            {
                return;
            }
        }

        getWorldRegion().revalidateZones(this);

        if (Config.ALLOW_WATER)
        {
            checkWaterState();
        }

        if (isInsideZone(ZONE_ALTERED))
        {
            if (_lastCompassZone == ExSetCompassZoneCode.ALTEREDZONE)
            {
                return;
            }
            _lastCompassZone = ExSetCompassZoneCode.ALTEREDZONE;
            ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.ALTEREDZONE);
            sendPacket(cz);
        }
        else if (isInsideZone(ZONE_SIEGE))
        {
            if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
            {
                return;
            }
            _lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
            ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2);
            sendPacket(cz);
        }
        else if (isInsideZone(ZONE_PVP))
        {
            if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
            {
                return;
            }
            _lastCompassZone = ExSetCompassZoneCode.PVPZONE;
            ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE);
            sendPacket(cz);
        }
        else if (isInsideZone(ZONE_PEACE))
        {
            if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
            {
                return;
            }
            _lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
            ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE);
            sendPacket(cz);
        }
        else
        {
            if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
            {
                return;
            }
            if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2 && !isDead())
            {
                updatePvPStatus();
            }
            _lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
            ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE);
            sendPacket(cz);
        }
    }

    /**
     * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR><BR>
     */
    public boolean hasDwarvenCraft()
    {
        return getSkillLevelHash(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
    }

    public int getDwarvenCraft()
    {
        return getSkillLevelHash(L2Skill.SKILL_CREATE_DWARVEN);
    }

    /**
     * @return True if the L2PcInstance can Craft Dwarven Recipes.
     */
    public boolean canCrystallize()
    {
        return getSkillLevelHash(L2Skill.SKILL_CRYSTALLIZE) >= 1;
    }

    /**
     * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR><BR>
     */
    public boolean hasCommonCraft()
    {
        return getSkillLevelHash(L2Skill.SKILL_CREATE_COMMON) >= 1;
    }

    public int getCommonCraft()
    {
        return getSkillLevelHash(L2Skill.SKILL_CREATE_COMMON);
    }

    /**
     * Return the PK counter of the L2PcInstance.<BR><BR>
     */
    public int getPkKills()
    {
        return _pkKills;
    }

    /**
     * Set the PK counter of the L2PcInstance.<BR><BR>
     */
    public void setPkKills(int pkKills)
    {
        _pkKills = pkKills;
    }

    /**
     * Return the _deleteTimer of the L2PcInstance.<BR><BR>
     */
    public long getDeleteTimer()
    {
        return _deleteTimer;
    }

    /**
     * Set the _deleteTimer of the L2PcInstance.<BR><BR>
     */
    public void setDeleteTimer(long deleteTimer)
    {
        _deleteTimer = deleteTimer;
    }

    /**
     * Return the current weight of the L2PcInstance.<BR><BR>
     */
    public int getCurrentLoad()
    {
        return _inventory.getTotalWeight();
    }

    /**
     * For Friend Memo
     */
    private HashMap<Integer, String> _friendMemo = new HashMap<>();

    public String getFriendMemo(int objId)
    {
        if (_friendMemo != null && _friendMemo.containsKey(objId))
        {
            return _friendMemo.get(objId);
        }

        return "";
    }

    public void addFriendMemo(int objId, String memo)
    {
        if (_friendMemo != null && _friendMemo.containsKey(objId))
        {
            _friendMemo.remove(objId);
        }
        _friendMemo.put(objId, memo);
    }

    public void removeFriendMemo(int objId)
    {
        if (_friendMemo != null && _friendMemo.containsKey(objId))
        {
            _friendMemo.remove(objId);
        }
    }

    public void broadcastToOnlineFriends(L2GameServerPacket packet)
    {
        for (int objId : getFriendList())
        {
            L2PcInstance friend;
            if (L2World.getInstance().getPlayer(objId) != null)
            {
                friend = L2World.getInstance().getPlayer(objId);
                friend.sendPacket(packet);
            }
        }
    }

    /**
     * For Block Memo
     */
    public HashMap<Integer, String> _blockMemo = new HashMap<>();

    public String getBlockMemo(int objId)
    {
        if (_blockMemo != null)
        {
            if (_blockMemo.containsKey(objId))
            {
                return _blockMemo.get(objId);
            }
        }

        return "";
    }

    public void addBlockMemo(int objId, String memo)
    {
        if (_blockMemo != null)
        {
            if (_blockMemo.containsKey(objId))
            {
                _blockMemo.remove(objId);
            }
        }
        _blockMemo.put(objId, memo);
    }

    public void removeBlockMemo(int objId)
    {
        if (_blockMemo != null)
        {
            if (_blockMemo.containsKey(objId))
            {
                _blockMemo.remove(objId);
            }
        }
    }

    /**
     * Return the number of recommandation obtained by the L2PcInstance.<BR><BR>
     */
    public int getRecomHave()
    {
        return _recomHave;
    }

    /**
     * Increment the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
     */
    protected void incRecomHave()
    {
        if (_recomHave < 255)
        {
            _recomHave++;
        }
    }

    /**
     * Set the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
     */
    public void setRecomHave(int value)
    {
        if (value > 255)
        {
            _recomHave = 255;
        }
        else if (value < 0)
        {
            _recomHave = 0;
        }
        else
        {
            _recomHave = value;
        }
    }

    /**
     * Set the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
     */
    public void setRecomLeft(int value)
    {
        if (value > 255)
        {
            _recomLeft = 255;
        }
        else if (value < 0)
        {
            _recomLeft = 0;
        }
        else
        {
            _recomLeft = value;
        }
    }

    /**
     * Return the number of recommandation that the L2PcInstance can give.<BR><BR>
     */
    public int getRecomLeft()
    {
        return _recomLeft;
    }

    /**
     * Increment the number of recommandation that the L2PcInstance can give.<BR><BR>
     */
    protected void decRecomLeft()
    {
        if (_recomLeft > 0)
        {
            _recomLeft--;
        }
    }

    public void giveRecom(L2PcInstance target)
    {
        target.incRecomHave();
        decRecomLeft();
    }

    /**
     * Set the exp of the L2PcInstance before a death
     *
     * @param exp
     */
    public void setExpBeforeDeath(long exp)
    {
        _expBeforeDeath = exp;
    }

    public long getExpBeforeDeath()
    {
        return _expBeforeDeath;
    }

    /**
     * Return the reputation of the L2PcInstance.<BR><BR>
     */
    @Override
    public int getReputation()
    {
        return _reputation;
    }

    /**
     * Set the Reputation of the L2PcInstance and send a Server->Client packet StatusUpdate (broadcast).<BR><BR>
     */
    public void setReputation(int reputation)
    {
        //if (reputation < 0) reputation = 0;
        if (_reputation == 0 && reputation < 0)
        {
            Collection<L2Object> objs = getKnownList().getKnownObjects().values();
            //synchronized (getKnownList().getKnownObjects())
            {
                for (L2Object object : objs)
                {
                    if (!(object instanceof L2GuardInstance))
                    {
                        continue;
                    }

                    if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
                    {
                        ((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
                    }
                }
            }
        }
        else if (_reputation >= 0 && reputation == 0)
        {
            // Send a Server->Client StatusUpdate packet with Reputation and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
            setReputationFlag(0);
        }

        _reputation = reputation;
        broadcastReputation();
    }

    /**
     * The Map for know which chaotic characters have killed before
     **/
    private TIntLongHashMap _chaoticCharactersKilledBefore = new TIntLongHashMap();

    /**
     * Update reputation for kill a chaotic player
     **/
    private void updateReputationForKillChaoticPlayer(L2PcInstance target)
    {
        long timeRightNow = System.currentTimeMillis(), timeThatCharacterKilledBefore = 0;
        if (_chaoticCharactersKilledBefore.contains(target.getObjectId()))
        {
            timeThatCharacterKilledBefore = _chaoticCharactersKilledBefore.get(target.getObjectId());
            if ((timeRightNow - timeThatCharacterKilledBefore) * 1000 >=
                    Config.REPUTATION_SAME_KILL_INTERVAL) // Seconds
            {
                setReputation(getReputation() + Config.REPUTATION_ACQUIRED_FOR_CHAOTIC_KILL);

                if (_chaoticCharactersKilledBefore.size() >= 10)
                {
                    _chaoticCharactersKilledBefore.clear();
                }
                else
                {
                    _chaoticCharactersKilledBefore.remove(target.getObjectId());
                }
                _chaoticCharactersKilledBefore.put(target.getObjectId(), timeRightNow);
            }
        }
        else
        {
            if (_chaoticCharactersKilledBefore.size() >= 10)
            {
                _chaoticCharactersKilledBefore.clear();
            }

            setReputation(getReputation() + Config.REPUTATION_ACQUIRED_FOR_CHAOTIC_KILL);

            _chaoticCharactersKilledBefore.put(target.getObjectId(), timeRightNow);
        }
    }

    /** **/
    public void updateReputationForHunting(long exp, int sp)
    {
        if (_reputation >= 0)
        {
            return;
        }

        int lvlSq = getLevel() * getLevel();
        int expDivider = Config.REPUTATION_REP_PER_EXP_MULTIPLIER * lvlSq;
        int spDivider = Config.REPUTATION_REP_PER_SP_MULTIPLIER * lvlSq;
        int reputationToAdd = (int) (exp / expDivider + sp / spDivider);
        int reputation = _reputation + reputationToAdd;
        // Check if we went over 0 or an integer overflow happened
        if (reputation > 0 || reputation < _reputation)
        {
            reputation = 0;
        }

        setReputation(reputation);
    }

    /**
     * Return the max weight that the L2PcInstance can load.<BR><BR>
     */
    public int getMaxLoad()
    {
        // Weight Limit = (CON Modifier*69000)*Skills
        // Source http://l2p.bravehost.com/weightlimit.html (May 2007)
        // Fitted exponential curve to the data
        int con = getCON();
        double baseLoad;
        if (con < 1)
        {
            baseLoad = 31000;
        }
        if (con > 59)
        {
            baseLoad = 176000;
        }
        else
        {
            baseLoad = Math.pow(1.029993928, con) * 30495.627366;
        }

        return (int) calcStat(Stats.MAX_LOAD, baseLoad * Config.ALT_WEIGHT_LIMIT, this, null);
    }

    public int getExpertiseArmorPenalty()
    {
        return _expertiseArmorPenalty;
    }

    public int getExpertiseWeaponPenalty()
    {
        return _expertiseWeaponPenalty;
    }

    public int getWeightPenalty()
    {
        if (_dietMode)
        {
            return 0;
        }

        return _curWeightPenalty;
    }

    /**
     * Update the overloaded status of the L2PcInstance.<BR><BR>
     */
    public void refreshOverloaded()
    {
        int maxLoad = getMaxLoad();
        if (maxLoad > 0)
        {
            long weightproc = (long) getCurrentLoad() * 1000 / maxLoad;
            int newWeightPenalty;
            if (weightproc < 500 || _dietMode)
            {
                newWeightPenalty = 0;
            }
            else if (weightproc < 666)
            {
                newWeightPenalty = 1;
            }
            else if (weightproc < 800)
            {
                newWeightPenalty = 2;
            }
            else if (weightproc < 1000)
            {
                newWeightPenalty = 3;
            }
            else
            {
                newWeightPenalty = 4;
            }

            if (_curWeightPenalty != newWeightPenalty)
            {
                _curWeightPenalty = newWeightPenalty;
                if (newWeightPenalty > 0 && !_dietMode)
                {
                    super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
                    setIsOverloaded(getCurrentLoad() > maxLoad);
                }
                else
                {
                    super.removeSkill(getKnownSkill(4270));
                    setIsOverloaded(false);
                }
                sendPacket(new UserInfo(this));
                sendPacket(new ExUserLoad(this));
                sendPacket(new EtcStatusUpdate(this));
                broadcastPacket(new CharInfo(this));
            }
        }
    }

    public void refreshExpertisePenalty()
    {
        if (!Config.EXPERTISE_PENALTY)
        {
            return;
        }

        int armorPenalty = 0;
        int weaponPenalty = 0;

        for (L2ItemInstance item : getInventory().getItems())
        {
            if (item != null && item.isEquipped() && item.getItemType() != L2EtcItemType.ARROW &&
                    item.getItemType() != L2EtcItemType.BOLT)
            {
                int crystaltype = item.getItem().getCrystalType();

                if (crystaltype > getExpertiseIndex())
                {
                    if (item.isWeapon() && crystaltype > weaponPenalty)
                    {
                        weaponPenalty = crystaltype;
                    }
                    else if (crystaltype > armorPenalty)
                    {
                        armorPenalty = crystaltype;
                    }
                }
            }
        }

        boolean changed = false;

        // calc armor penalty
        armorPenalty = armorPenalty - getExpertiseIndex();

        if (armorPenalty < 0)
        {
            armorPenalty = 0;
        }
        else if (armorPenalty > 4)
        {
            armorPenalty = 4;
        }

        if (getExpertiseArmorPenalty() != armorPenalty || getSkillLevelHash(6213) != armorPenalty)
        {
            _expertiseArmorPenalty = armorPenalty;

            if (_expertiseArmorPenalty > 0)
            {
                super.addSkill(
                        SkillTable.getInstance().getInfo(6213, _expertiseArmorPenalty)); // level used to be newPenalty
            }
            else
            {
                super.removeSkill(getKnownSkill(6213));
            }

            changed = true;
        }

        // calc weapon penalty
        weaponPenalty = weaponPenalty - getExpertiseIndex();
        if (weaponPenalty < 0)
        {
            weaponPenalty = 0;
        }
        else if (weaponPenalty > 4)
        {
            weaponPenalty = 4;
        }

        if (getExpertiseWeaponPenalty() != weaponPenalty || getSkillLevelHash(6209) != weaponPenalty)
        {
            _expertiseWeaponPenalty = weaponPenalty;

            if (_expertiseWeaponPenalty > 0)
            {
                super.addSkill(
                        SkillTable.getInstance().getInfo(6209, _expertiseWeaponPenalty)); // level used to be newPenalty
            }
            else
            {
                super.removeSkill(getKnownSkill(6209));
            }

            changed = true;
        }

        if (changed)
        {
            sendPacket(new EtcStatusUpdate(this));
        }
    }

    public void checkIfWeaponIsAllowed()
    {
        // Override for Gamemasters
        if (isGM())
        {
            return;
        }

        // Iterate through all effects currently on the character.
        for (L2Abnormal currenteffect : getAllEffects())
        {
            L2Skill effectSkill = currenteffect.getSkill();

            // Ignore all buff skills that are party related (ie. songs, dances) while still remaining weapon dependant on cast though.
            if (!effectSkill.isOffensive() && !(effectSkill.getTargetType() == L2SkillTargetType.TARGET_PARTY &&
                    effectSkill.getSkillType() == L2SkillType.BUFF))
            {
                // Check to rest to assure current effect meets weapon requirements.
                if (!effectSkill.getWeaponDependancy(this))
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                    sm.addSkillName(effectSkill);
                    sendPacket(sm);

                    if (Config.DEBUG)
                    {
                        Log.info("   | Skill " + effectSkill.getName() + " has been disabled for (" + getName() +
                                "); Reason: Incompatible Weapon Type.");
                    }

                    currenteffect.exit();
                }
            }

            continue;
        }
    }

    public void checkSShotsMatch(L2ItemInstance equipped, L2ItemInstance unequipped)
    {
        if (unequipped == null)
        {
            return;
        }

        unequipped.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
        unequipped.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);

        // on retail auto shots never disabled on uneqip
        /*if (unequipped.getItem().getType2() == L2Item.TYPE2_WEAPON &&
                (equipped == null ? true : equipped.getItem().getItemGradeSPlus() != unequipped.getItem().getItemGradeSPlus()))
		{
			disableAutoShotByCrystalType(unequipped.getItem().getItemGradeSPlus());
		}*/
    }

    public void useEquippableItem(L2ItemInstance item, boolean abortAttack)
    {
        // Equip or unEquip
        L2ItemInstance[] items = null;
        final boolean isEquiped = item.isEquipped();
        final int oldInvLimit = getInventoryLimit();
        SystemMessage sm = null;
        if ((item.getItem().getBodyPart() & L2Item.SLOT_MULTI_ALLWEAPON) != 0)
        {
            L2ItemInstance old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
            checkSShotsMatch(item, old);
        }

        if (isEquiped)
        {
            if (item.getEnchantLevel() > 0)
            {
                sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                sm.addNumber(item.getEnchantLevel());
                sm.addItemName(item);
            }
            else
            {
                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                sm.addItemName(item);
            }
            sendPacket(sm);

            int slot = getInventory().getSlotFromItem(item);
            // we cant unequip talisman/jewel by body slot
            if (slot == L2Item.SLOT_DECO || slot == L2Item.SLOT_JEWELRY)
            {
                items = getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
            }
            else
            {
                items = getInventory().unEquipItemInBodySlotAndRecord(slot);
            }

            switch (item.getItemId())
            {
                case 15393:
                    setPremiumItemId(0);
                    break;
                default:
                    break;
            }
        }
        else
        {
            items = getInventory().equipItemAndRecord(item);

            if (item.isEquipped())
            {
                if (item.getEnchantLevel() > 0)
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
                    sm.addNumber(item.getEnchantLevel());
                    sm.addItemName(item);
                }
                else
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
                    sm.addItemName(item);
                }
                sendPacket(sm);

                // Consume mana - will start a task if required; returns if item is not a shadow item
                item.decreaseMana(false);

                if ((item.getItem().getBodyPart() & L2Item.SLOT_MULTI_ALLWEAPON) != 0)
                {
                    rechargeAutoSoulShot(true, true, false);
                }

                switch (item.getItemId())
                {
                    case 15393:
                        getStat().setVitalityPoints(140000, false, true);
                        setPremiumItemId(item.getItemId());
                        break;
                    default:
                        break;
                }
            }
            else
            {
                sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
            }
        }

        if (!isUpdateLocked())
        {
            refreshExpertisePenalty();

            broadcastUserInfo();

            InventoryUpdate iu = new InventoryUpdate();
            iu.addItems(Arrays.asList(items));
            sendPacket(iu);

            if (abortAttack)
            {
                abortAttack();
            }

            if (getInventoryLimit() != oldInvLimit)
            {
                sendPacket(new ExStorageMaxCount(this));
            }
        }
    }

    /**
     * Return the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
     */
    public int getPvpKills()
    {
        return _pvpKills;
    }

    /**
     * Set the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
     */
    public void setPvpKills(int pvpKills)
    {
        _pvpKills = pvpKills;
    }

    /**
     * Return the Fame of this L2PcInstance <BR><BR>
     *
     * @return
     */
    public int getFame()
    {
        return _fame;
    }

    /**
     * Set the Fame of this L2PcInstane <BR><BR>
     *
     * @param fame
     */
    public void setFame(int fame)
    {
        if (fame > Config.MAX_PERSONAL_FAME_POINTS)
        {
            _fame = Config.MAX_PERSONAL_FAME_POINTS;
        }
        else
        {
            _fame = fame;
        }
    }

    /**
     * Return the ClassId object of the L2PcInstance contained in L2PcTemplate.<BR><BR>
     */
    public int getClassId()
    {
        if (_temporaryClassId != 0)
        {
            return _temporaryClassId;
        }

        return getCurrentClass().getId();
    }

    /**
     * Set the template of the L2PcInstance.<BR><BR>
     *
     * @param id The Identifier of the L2PcTemplate to set to the L2PcInstance
     */
    public void setClassId(int id)
    {
        if (!_subclassLock.tryLock())
        {
            return;
        }

        try
        {
            if (getLvlJoinedAcademy() != 0 && _clan != null &&
                    PlayerClassTable.getInstance().getClassById(id).getLevel() <= 76)
            {
                if (getLvlJoinedAcademy() <= 36)
                {
                    _clan.addReputationScore(Config.JOIN_ACADEMY_MAX_REP_SCORE, true);
                }
                else if (getLvlJoinedAcademy() >= 84)
                {
                    _clan.addReputationScore(Config.JOIN_ACADEMY_MIN_REP_SCORE, true);
                }
                else
                {
                    _clan.addReputationScore(Config.JOIN_ACADEMY_MAX_REP_SCORE - (getLvlJoinedAcademy() - 16) * 20,
                            true);
                }
                setLvlJoinedAcademy(0);
                //oust pledge member from the academy, cuz he has finished his 2nd class transfer
                SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
                msg.addPcName(this);
                _clan.broadcastToOnlineMembers(msg);
                _clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
                _clan.removeClanMember(getObjectId(), 0);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED));

                // receive graduation gift
                getInventory().addItem("Gift", 8181, 1, this, null); // give academy circlet
            }

            if (isSubClassActive())
            {
                getSubClasses().get(_classIndex).setClassId(id);
            }

            if (PlayerClassTable.getInstance().getClassById(id).getLevel() != 85)
            {
                setTarget(this);
                broadcastPacket(new MagicSkillUse(this, 5103, 1, 1000, 0));
            }
            setClassTemplate(id);
            if (getCurrentClass().getLevel() == 85)
            {
                sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
            }
            else if (getCurrentClass().getLevel() == 76)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIRD_CLASS_TRANSFER));
                if (getLevel() >= 85)
                {
                    PlayerClass cl = PlayerClassTable.getInstance().getClassById(getClassId());
                    if (cl.getAwakeningClassId() != -1)
                    {
                        sendPacket(new ExCallToChangeClass(cl.getId(), false));
                    }
                }
            }
            else
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLASS_TRANSFER));
            }

            // Give Mentee Certificate
            if (getCurrentClass().getLevel() == 40 && getClassId() == getBaseClass())
            {
                getInventory().addItem("Mentee's Certificate", 33800, 1, this, null); // give academy circlet
            }

            // Update class icon in party and clan
            if (isInParty())
            {
                getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));
            }

            if (getClan() != null)
            {
                getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
            }

            sendPacket(new ExMentorList(this));

            if (getFriendList().size() > 0)
            {
                for (int i : getFriendList())
                {
                    if (L2World.getInstance().getPlayer(i) != null)
                    {
                        L2PcInstance player = L2World.getInstance().getPlayer(i);
                        player.sendPacket(new FriendList(player));
                        player.sendPacket(new FriendPacket(true, getObjectId(), player));
                    }
                }
            }
            if (isMentee())
            {
                L2PcInstance mentor = L2World.getInstance().getPlayer(getMentorId());
                if (mentor != null)
                {
                    mentor.sendPacket(new ExMentorList(mentor));
                }
            }
            else if (isMentor())
            {
                for (int objId : getMenteeList())
                {
                    L2PcInstance mentee = L2World.getInstance().getPlayer(objId);
                    if (mentee != null)
                    {
                        mentee.sendPacket(new ExMentorList(mentee));
                    }
                }
            }

            rewardSkills();
            if (!isGM() && Config.DECREASE_SKILL_LEVEL)
            {
                checkPlayerSkills();
            }

            for (int i = 0; i < 3; i++)
            {
                if (!getCurrentClass().getAllowedDyes().contains(_henna[i]))
                {
                    removeHenna(i + 1);
                }
            }

            if (getIsSummonsInDefendingMode())
            {
                setIsSummonsInDefendingMode(false);
            }

            int gearGrade = getGearGradeForCurrentLevel();

            int[][] gearPreset = getGearPreset(getClassId(), gearGrade);

            if (gearPreset != null)
            {
                equipGearPreset(gearPreset);
            }
        }
        finally
        {
            _subclassLock.unlock();
        }
    }

    /**
     * Return the Experience of the L2PcInstance.
     */
    public long getExp()
    {
        return getStat().getExp();
    }

    public void setActiveEnchantAttrItem(L2ItemInstance stone)
    {
        _activeEnchantAttrItem = stone;
    }

    public L2ItemInstance getActiveEnchantAttrItem()
    {
        return _activeEnchantAttrItem;
    }

    public void setActiveEnchantItem(L2ItemInstance scroll)
    {
        // If we dont have a Enchant Item, we are not enchanting.
        if (scroll == null)
        {
            setActiveEnchantSupportItem(null);
            setActiveEnchantTimestamp(0);
            setIsEnchanting(false);
        }
        _activeEnchantItem = scroll;
    }

    public L2ItemInstance getActiveEnchantItem()
    {
        return _activeEnchantItem;
    }

    public void setActiveEnchantSupportItem(L2ItemInstance item)
    {
        _activeEnchantSupportItem = item;
    }

    public L2ItemInstance getActiveEnchantSupportItem()
    {
        return _activeEnchantSupportItem;
    }

    public long getActiveEnchantTimestamp()
    {
        return _activeEnchantTimestamp;
    }

    public void setActiveEnchantTimestamp(long val)
    {
        _activeEnchantTimestamp = val;
    }

    public void setIsEnchanting(boolean val)
    {
        _isEnchanting = val;
    }

    public boolean isEnchanting()
    {
        return _isEnchanting;
    }

    /**
     * Set the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR><BR>
     *
     * @param weaponItem The fists L2Weapon to set to the L2PcInstance
     */
    public void setFistsWeaponItem(L2Weapon weaponItem)
    {
        _fistsWeaponItem = weaponItem;
    }

    /**
     * Return the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR><BR>
     */
    public L2Weapon getFistsWeaponItem()
    {
        return _fistsWeaponItem;
    }

    /**
     * Return the fists weapon of the L2PcInstance Class (used when no weapon is equiped).<BR><BR>
     */
    public L2Weapon findFistsWeaponItem(int classId)
    {
        L2Weapon weaponItem = null;
        if (classId >= 0x00 && classId <= 0x09)
        {
            //human fighter fists
            L2Item temp = ItemTable.getInstance().getTemplate(246);
            weaponItem = (L2Weapon) temp;
        }
        else if (classId >= 0x0a && classId <= 0x11)
        {
            //human mage fists
            L2Item temp = ItemTable.getInstance().getTemplate(251);
            weaponItem = (L2Weapon) temp;
        }
        else if (classId >= 0x12 && classId <= 0x18)
        {
            //elven fighter fists
            L2Item temp = ItemTable.getInstance().getTemplate(244);
            weaponItem = (L2Weapon) temp;
        }
        else if (classId >= 0x19 && classId <= 0x1e)
        {
            //elven mage fists
            L2Item temp = ItemTable.getInstance().getTemplate(249);
            weaponItem = (L2Weapon) temp;
        }
        else if (classId >= 0x1f && classId <= 0x25)
        {
            //dark elven fighter fists
            L2Item temp = ItemTable.getInstance().getTemplate(245);
            weaponItem = (L2Weapon) temp;
        }
        else if (classId >= 0x26 && classId <= 0x2b)
        {
            //dark elven mage fists
            L2Item temp = ItemTable.getInstance().getTemplate(250);
            weaponItem = (L2Weapon) temp;
        }
        else if (classId >= 0x2c && classId <= 0x30)
        {
            //orc fighter fists
            L2Item temp = ItemTable.getInstance().getTemplate(248);
            weaponItem = (L2Weapon) temp;
        }
        else if (classId >= 0x31 && classId <= 0x34)
        {
            //orc mage fists
            L2Item temp = ItemTable.getInstance().getTemplate(252);
            weaponItem = (L2Weapon) temp;
        }
        else if (classId >= 0x35 && classId <= 0x39)
        {
            //dwarven fists
            L2Item temp = ItemTable.getInstance().getTemplate(247);
            weaponItem = (L2Weapon) temp;
        }

        return weaponItem;
    }

    /**
     * Give Expertise skill of this level and remove beginner Lucky skill.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Get the Level of the L2PcInstance </li>
     * <li>If L2PcInstance Level is 5, remove beginner Lucky skill </li>
     * <li>Add the Expertise skill corresponding to its Expertise level</li>
     * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
     * <p>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
     */
    public void rewardSkills()
    {
        // Get the Level of the L2PcInstance
        int lvl = getLevel();

        // Remove beginner Lucky skill
        if (lvl == 10)
        {
            L2Skill skill = SkillTable.FrequentSkill.LUCKY.getSkill();
            skill = removeSkill(skill);

            if (Config.DEBUG && skill != null)
            {
                Log.fine("removed skill 'Lucky' from " + getName());
            }
        }

        // Calculate the current higher Expertise of the L2PcInstance
        for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
        {
            if (lvl >= EXPERTISE_LEVELS[i])
            {
                setExpertiseIndex(i);
            }
        }

        // Add the Expertise skill corresponding to its Expertise level
        if (getExpertiseIndex() > 0)
        {
            L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
            addSkill(skill, true);

            if (Config.DEBUG)
            {
                Log.fine("awarded " + getName() + " with new expertise.");
            }
        }
        else
        {
            removeSkill(239);
            if (Config.DEBUG)
            {
                Log.fine("No skills awarded at lvl: " + lvl);
            }
        }

        //Active skill dwarven craft
        if (getSkillLevelHash(172) < 10 && getClassId() == 156) // Tyrr Maestro
        {
            L2Skill skill = SkillTable.FrequentSkill.MAESTRO_CREATE_ITEM.getSkill();
            addSkill(skill, true);
        }

        //Active skill dwarven craft
        if (getSkillLevelHash(1321) < 1 &&
                (getRace() == Race.Dwarf && getCurrentClass().getLevel() < 85 || getCurrentClass().getId() == 156))
        {
            L2Skill skill = SkillTable.FrequentSkill.DWARVEN_CRAFT.getSkill();
            addSkill(skill, true);
        }

        //Active skill common craft
        if (!Config.IS_CLASSIC)
        {
            if (getSkillLevelHash(1322) < 1)
            {
                L2Skill skill = SkillTable.FrequentSkill.COMMON_CRAFT.getSkill();
                addSkill(skill, true);
            }

            for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
            {
                if (lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevelHash(1320) < i + 1)
                {
                    L2Skill skill = SkillTable.getInstance().getInfo(1320, i + 1);
                    addSkill(skill, true);
                }
            }
        }

        // Autoget skills
        for (L2SkillLearn s : SkillTreeTable.getInstance().getAvailableClassSkills(this))
        {
            if (!s.isAutoGetSkill() || s.getMinLevel() > getLevel())
            {
                continue;
            }

            addSkill(SkillTable.getInstance().getInfo(s.getId(), s.getLevel()), true);
        }

        // Auto-Learn skills if activated
        if (Config.AUTO_LEARN_SKILLS)
        {
            giveAvailableSkills(false);
        }

        checkItemRestriction();
        sendSkillList();
    }

    /**
     * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills<BR><BR>
     */
    public void regiveTemporarySkills()
    {
        // Do not call this on enterworld or char load

        // Add noble skills if noble
        if (isNoble())
        {
            setNoble(true);
        }

        // Add Hero skills if hero
        if (isHero())
        {
            setHero(true);
        }

        // Add Mentor skills if player is mentor
        if (isMentor())
        {
            giveMentorSkills();
        }

        // Add Mentee skill if player is mentee
        if (!canBeMentor() && isMentee())
        {
            giveMenteeSkills();
        }

        // Add clan skills
        if (getClan() != null)
        {
            L2Clan clan = getClan();
            clan.addSkillEffects(this);

            if (clan.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel() && isClanLeader())
            {
                SiegeManager.getInstance().addSiegeSkills(this);
            }
            if (getClan().getHasCastle() > 0)
            {
                CastleManager.getInstance().getCastleByOwner(getClan()).giveResidentialSkills(this);
            }
            if (getClan().getHasFort() > 0)
            {
                FortManager.getInstance().getFortByOwner(getClan()).giveResidentialSkills(this);
            }
        }

        // Reload passive skills from armors / jewels / weapons
        getInventory().reloadEquippedItems();
    }

    /**
     * Give all available skills to the player.<br><br>
     */
    public int giveAvailableSkills(boolean forceAll)
    {
        //if (getRace() == Race.Ertheia && !forceAll)
        //	return 0;

        int unLearnable = 0;
        int skillCounter = 0;

        // Get available skills
        L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableClassSkills(this);
        while (skills.length > unLearnable)
        {
            unLearnable = 0;
            for (L2SkillLearn s : skills)
            {
                if (s.getMinLevel() > getLevel() || !s.getCostSkills().isEmpty() && !forceAll || s.isRemember())
                {
                    unLearnable++;
                    continue;
                }

                L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                if (sk == null || (sk.getId() == L2Skill.SKILL_DIVINE_INSPIRATION ||
                        sk.getId() == L2Skill.SKILL_DIVINE_EXPANSION) && !Config.AUTO_LEARN_DIVINE_INSPIRATION &&
                        !isGM())
                {
                    unLearnable++;
                    continue;
                }

                if (getSkillLevelHash(sk.getId()) == -1)
                {
                    skillCounter++;
                }

                // fix when learning toggle skills
                if (sk.isToggle())
                {
                    L2Abnormal toggleEffect = getFirstEffect(sk.getId());
                    if (toggleEffect != null)
                    {
                        // stop old toggle skill effect, and give new toggle skill effect back
                        toggleEffect.exit();
                        sk.getEffects(this, this);
                    }
                }

                List<Integer> reqSkillIds = s.getCostSkills();
                if (reqSkillIds != null && !reqSkillIds.isEmpty())
                {
                    for (L2Skill sk2 : getAllSkills())
                    {
                        for (int reqSkillId : reqSkillIds)
                        {
                            if (sk2.getId() == reqSkillId)
                            {
                                removeSkill(sk2);
                            }
                        }
                    }
                }

                addSkill(sk, true);
            }

            // Get new available skills
            skills = SkillTreeTable.getInstance().getAvailableClassSkills(this);
        }

        if (skillCounter > 0)
        {
            sendMessage("You have learned " + skillCounter + " new skills.");
        }

        return skillCounter;
    }

    @SuppressWarnings("unused")
    public final void giveSkills(boolean learnAvailable)
    {
        // Get the Level of the L2PcInstance
        int lvl = getLevel();

		/*
        // Remove beginner Lucky skill
		if (lvl == 10)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);

			skill = removeSkill(skill);
		}

		// Calculate the current higher Expertise of the L2PcInstance
		for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
				setExpertiseIndex(i);
		}

		// Add the Expertise skill corresponding to its Expertise level
		if (getExpertiseIndex() > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());

			addSkill(skill, true);
		}

		//Active skill dwarven craft
		if (getSkillLevel(1321) < 1 && getRace() == Race.Dwarf)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1321, 1);

			addSkill(skill, true);
		}

		//Active skill common craft
		if (getSkillLevel(1322) < 1)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1322, 1);

			addSkill(skill, true);
		}

		for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
		{
			if (lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < (i + 1))
			{
				L2Skill skill = SkillTable.getInstance().getInfo(1320, (i + 1));

				addSkill(skill, true);
			}
		}*/

        if (learnAvailable)
        {
            int unLearnable = 0;
            int skillCounter = 0;
            boolean forceAll = true;

            // Get available skills
            L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkillsForPlayer(this, true, true);
            int newlyLearnedSkills = 0;
            for (L2SkillLearn skill : skills)
            {
                L2Skill sk = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
                if (sk == null ||
                        sk.getId() == L2Skill.SKILL_DIVINE_INSPIRATION && !Config.AUTO_LEARN_DIVINE_INSPIRATION ||
                        sk.getId() >= 23500 && sk.getId() <= 23511)
                {
                    continue;
                }

                if (getSkillLevelHash(sk.getId()) == -1)
                {
                    newlyLearnedSkills++;
                }

                // fix when learning toggle skills
                if (sk.isToggle())
                {
                    L2Abnormal toggleEffect = getFirstEffect(sk.getId());
                    if (toggleEffect != null)
                    {
                        // stop old toggle skill effect, and give new toggle skill effect back
                        toggleEffect.exit();
                        sk.getEffects(this, this);
                    }
                }

                List<Integer> reqSkillIds = skill.getCostSkills();
                if (reqSkillIds != null && !reqSkillIds.isEmpty())
                {
                    for (L2Skill sk2 : getAllSkills())
                    {
                        for (int reqSkillId : reqSkillIds)
                        {
                            if (sk2.getId() == reqSkillId)
                            {
                                removeSkill(sk2);
                            }
                        }
                    }
                }

                addSkill(sk, true);
            }

            sendMessage("You have learned " + newlyLearnedSkills + " new skills.");
        }

        checkItemRestriction();
        sendSkillList();

        // This function gets called on login, so not such a bad place to check weight
        refreshOverloaded(); // Update the overloaded status of the L2PcInstance
        refreshExpertisePenalty(); // Update the expertise status of the L2PcInstance
    }

    /**
     * Set the Experience value of the L2PcInstance.
     */
    public void setExp(long exp)
    {
        if (exp < 0)
        {
            exp = 0;
        }

        getStat().setExp(exp);
    }

    /**
     * Return the Race object of the L2PcInstance.<BR><BR>
     */
    public Race getRace()
    {
        return getTemplate().race;
    }

    public L2Radar getRadar()
    {
        return _radar;
    }

    /**
     * Return the SP amount of the L2PcInstance.
     */
    public long getSp()
    {
        return getStat().getSp();
    }

    /**
     * Set the SP amount of the L2PcInstance.
     */
    public void setSp(long sp)
    {
        if (sp < 0)
        {
            sp = 0;
        }

        super.getStat().setSp(sp);
    }

    /**
     * Return true if this L2PcInstance is a clan leader in
     * ownership of the passed castle
     */
    public boolean isCastleLord(int castleId)
    {
        L2Clan clan = getClan();

        // player has clan and is the clan leader, check the castle info
        if (clan != null && clan.getLeader().getPlayerInstance() == this)
        {
            // if the clan has a castle and it is actually the queried castle, return true
            Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
            if (castle != null && castle == CastleManager.getInstance().getCastleById(castleId))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Return the Clan Identifier of the L2PcInstance.<BR><BR>
     */
    public int getClanId()
    {
        return _clanId;
    }

    /**
     * Return the Clan Crest Identifier of the L2PcInstance or 0.<BR><BR>
     */
    public int getClanCrestId()
    {
        if (_clan != null)
        {
            return _clan.getCrestId();
        }

        return 0;
    }

    /**
     * @return The Clan CrestLarge Identifier or 0
     */
    public int getClanCrestLargeId()
    {
        if (_clan != null)
        {
            return _clan.getLargeCrestId();
        }

        return 0;
    }

    public long getClanJoinExpiryTime()
    {
        return _clanJoinExpiryTime;
    }

    public void setClanJoinExpiryTime(long time)
    {
        _clanJoinExpiryTime = time;
    }

    public long getClanCreateExpiryTime()
    {
        return _clanCreateExpiryTime;
    }

    public void setClanCreateExpiryTime(long time)
    {
        _clanCreateExpiryTime = time;
    }

    public void setOnlineTime(long time)
    {
        _onlineTime = time;
        _onlineBeginTime = System.currentTimeMillis();
    }

    public long getZoneRestartLimitTime()
    {
        return _zoneRestartLimitTime;
    }

    public void setZoneRestartLimitTime(long time)
    {
        _zoneRestartLimitTime = time;
    }

    public void storeZoneRestartLimitTime()
    {
        if (isInsideZone(L2Character.ZONE_NORESTART))
        {
            L2NoRestartZone zone = null;
            for (L2ZoneType tmpzone : ZoneManager.getInstance().getZones(this))
            {
                if (tmpzone instanceof L2NoRestartZone)
                {
                    zone = (L2NoRestartZone) tmpzone;
                    break;
                }
            }
            if (zone != null)
            {
                Connection con = null;
                try
                {
                    con = L2DatabaseFactory.getInstance().getConnection();
                    final PreparedStatement statement = con.prepareStatement(UPDATE_ZONE_RESTART_LIMIT);
                    statement.setInt(1, getObjectId());
                    statement.setLong(2, System.currentTimeMillis() + zone.getRestartAllowedTime() * 1000);
                    statement.execute();
                    statement.close();
                }
                catch (SQLException e)
                {
                    Log.log(Level.WARNING, "Cannot store zone norestart limit for character " + getObjectId(), e);
                }
                finally
                {
                    L2DatabaseFactory.close(con);
                }
            }
        }
    }

    private void restoreZoneRestartLimitTime()
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(LOAD_ZONE_RESTART_LIMIT);
            statement.setInt(1, getObjectId());
            final ResultSet rset = statement.executeQuery();

            if (rset.next())
            {
                setZoneRestartLimitTime(rset.getLong("time_limit"));
                statement.close();
                statement = con.prepareStatement(DELETE_ZONE_RESTART_LIMIT);
                statement.setInt(1, getObjectId());
                statement.executeUpdate();
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not restore " + this + " zone restart time: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * Return the PcInventory Inventory of the L2PcInstance contained in _inventory.<BR><BR>
     */
    @Override
    public PcInventory getInventory()
    {
        return _inventory;
    }

    public PcAuction getAuctionInventory()
    {
        return _auctionInventory;
    }

    /**
     * Delete a ShortCut of the L2PcInstance _shortCuts.<BR><BR>
     */
    public void removeItemFromShortCut(int objectId)
    {
        _shortCuts.deleteShortCutByObjectId(objectId);
    }

    /**
     * Return True if the L2PcInstance is sitting.<BR><BR>
     */
    public boolean isSitting()
    {
        return _waitTypeSitting;
    }

    /**
     * Set _waitTypeSitting to given value
     */
    public void setIsSitting(boolean state)
    {
        _waitTypeSitting = state;
    }

    /**
     * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
     */
    public void sitDown()
    {
        sitDown(true);
    }

    public void sitDown(boolean checkCast)
    {
        if (checkCast && isCastingNow())
        {
            sendMessage("Cannot sit while casting");
            return;
        }

        if (!_waitTypeSitting && !isAttackingDisabled() && !isOutOfControl() && !isImmobilized())
        {
            breakAttack();
            setIsSitting(true);
            broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
            // Schedule a sit down task to wait for the animation to finish
            ThreadPoolManager.getInstance().scheduleGeneral(new SitDownTask(), 2500);
            setIsParalyzed(true);
        }
    }

    /**
     * Sit down Task
     */
    private class SitDownTask implements Runnable
    {
        @Override
        public void run()
        {
            setIsParalyzed(false);
            getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
        }
    }

    /**
     * Stand up Task
     */
    private class StandUpTask implements Runnable
    {
        @Override
        public void run()
        {
            setIsSitting(false);
            getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }
    }

    /**
     * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
     */
    public void standUp()
    {
        if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead())
        {
            if (_effects.isAffected(L2EffectType.RELAXING.getMask()))
            {
                stopEffects(L2EffectType.RELAXING);
            }

            broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
            // Schedule a stand up task to wait for the animation to finish
            ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(), 2500);
        }
    }

    /**
     * Return the PcWarehouse object of the L2PcInstance.<BR><BR>
     */
    public PcWarehouse getWarehouse()
    {
        if (_warehouse == null)
        {
            _warehouse = new PcWarehouse(this);
            _warehouse.restore();
        }
        if (Config.WAREHOUSE_CACHE)
        {
            WarehouseCacheManager.getInstance().addCacheTask(this);
        }
        return _warehouse;
    }

    /**
     * Free memory used by Warehouse
     */
    public void clearWarehouse()
    {
        if (_warehouse != null)
        {
            _warehouse.deleteMe();
        }
        _warehouse = null;
    }

    /**
     * Returns true if refund list is not empty
     */
    public boolean hasRefund()
    {
        return _refund != null && _refund.getSize() > 0 && Config.ALLOW_REFUND;
    }

    /**
     * Returns refund object or create new if not exist
     */
    public PcRefund getRefund()
    {
        if (_refund == null)
        {
            _refund = new PcRefund(this);
        }
        return _refund;
    }

    /**
     * Clear refund
     */
    public void clearRefund()
    {
        if (_refund != null)
        {
            _refund.deleteMe();
        }
        _refund = null;
    }

    /**
     * Return the Identifier of the L2PcInstance.<BR><BR>
     */
    @Deprecated
    public int getCharId()
    {
        return _charId;
    }

    /**
     * Set the Identifier of the L2PcInstance.<BR><BR>
     */
    public void setCharId(int charId)
    {
        _charId = charId;
    }

    /**
     * Return the Adena amount of the L2PcInstance.<BR><BR>
     */
    public long getAdena()
    {
        return _inventory.getAdena();
    }

    /**
     * Return the Ancient Adena amount of the L2PcInstance.<BR><BR>
     */
    public long getAncientAdena()
    {
        return _inventory.getAncientAdena();
    }

    /**
     * Add adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param count       : int Quantity of adena to be added
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     */
    public void addAdena(String process, long count, L2Object reference, boolean sendMessage)
    {
        if (sendMessage)
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
            sm.addItemNumber(count);
            sendPacket(sm);
        }

        if (count > 0)
        {
            _inventory.addAdena(process, count, this, reference);

            // Send update packet
            if (!Config.FORCE_INVENTORY_UPDATE)
            {
                InventoryUpdate iu = new InventoryUpdate();
                iu.addItem(_inventory.getAdenaInstance());
                sendPacket(iu);
            }
            else
            {
                sendPacket(new ItemList(this, false));
            }

            sendPacket(new ExAdenaInvenCount(getAdena(), getInventory().getSize(false)));
        }
    }

    /**
     * Reduce adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param count       : long Quantity of adena to be reduced
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean reduceAdena(String process, long count, L2Object reference, boolean sendMessage)
    {
        if (count > getAdena())
        {
            if (sendMessage)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
            }
            return false;
        }

        if (count > 0)
        {
            L2ItemInstance adenaItem = _inventory.getAdenaInstance();
            if (!_inventory.reduceAdena(process, count, this, reference))
            {
                return false;
            }

            // Send update packet
            if (!Config.FORCE_INVENTORY_UPDATE)
            {
                InventoryUpdate iu = new InventoryUpdate();
                iu.addItem(adenaItem);
                sendPacket(iu);
            }
            else
            {
                sendPacket(new ItemList(this, false));
            }
            sendPacket(new ExAdenaInvenCount(getAdena(), getInventory().getSize(false)));

            if (sendMessage)
            {
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA);
                sm.addItemNumber(count);
                sendPacket(sm);
            }
        }

        return true;
    }

    /**
     * Add ancient adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param count       : int Quantity of ancient adena to be added
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     */
    public void addAncientAdena(String process, long count, L2Object reference, boolean sendMessage)
    {
        if (sendMessage)
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
            sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
            sm.addItemNumber(count);
            sendPacket(sm);
        }

        if (count > 0)
        {
            _inventory.addAncientAdena(process, count, this, reference);

            if (!Config.FORCE_INVENTORY_UPDATE)
            {
                InventoryUpdate iu = new InventoryUpdate();
                iu.addItem(_inventory.getAncientAdenaInstance());
                sendPacket(iu);
            }
            else
            {
                sendPacket(new ItemList(this, false));
            }
        }
    }

    /**
     * Reduce ancient adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param count       : long Quantity of ancient adena to be reduced
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean reduceAncientAdena(String process, long count, L2Object reference, boolean sendMessage)
    {
        if (count > getAncientAdena())
        {
            if (sendMessage)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
            }

            return false;
        }

        if (count > 0)
        {
            L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
            if (!_inventory.reduceAncientAdena(process, count, this, reference))
            {
                return false;
            }

            if (!Config.FORCE_INVENTORY_UPDATE)
            {
                InventoryUpdate iu = new InventoryUpdate();
                iu.addItem(ancientAdenaItem);
                sendPacket(iu);
            }
            else
            {
                sendPacket(new ItemList(this, false));
            }

            if (sendMessage)
            {
                if (count > 1)
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                    sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
                    sm.addItemNumber(count);
                    sendPacket(sm);
                }
                else
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                    sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
                    sendPacket(sm);
                }
            }
        }

        return true;
    }

    /**
     * Adds item to inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param item        : L2ItemInstance to be added
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     */
    public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
    {
        if (item.getCount() > 0)
        {
            // Sends message to client if requested
            if (sendMessage)
            {
                if (item.getCount() > 1)
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
                    sm.addItemName(item);
                    sm.addItemNumber(item.getCount());
                    sendPacket(sm);
                }
                else if (item.getEnchantLevel() > 0)
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
                    sm.addNumber(item.getEnchantLevel());
                    sm.addItemName(item);
                    sendPacket(sm);
                }
                else
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
                    sm.addItemName(item);
                    sendPacket(sm);
                }
            }

            // Add the item to inventory
            L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);

            // Send inventory update packet
            if (!Config.FORCE_INVENTORY_UPDATE)
            {
                InventoryUpdate playerIU = new InventoryUpdate();
                playerIU.addItem(newitem);
                sendPacket(playerIU);
            }
            else
            {
                sendPacket(new ItemList(this, false));
            }

            if (item.getItemId() == 57)
            {
                sendPacket(new ExAdenaInvenCount(getAdena(), getInventory().getSize(false)));
            }

            // Update current load as well
            StatusUpdate su = new StatusUpdate(this);
            su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
            sendPacket(su);

            // If over capacity, drop the item
            if (!isGM() && !_inventory.validateCapacity(0, item.isQuestItem()) && newitem.isDropable() &&
                    !item.isEquipable() &&
                    (!newitem.isStackable() || newitem.getLastChange() != L2ItemInstance.MODIFIED))
            {
                dropItem("InvDrop", newitem, null, true);
            }

            // Cursed Weapon
            else if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
            {
                CursedWeaponsManager.getInstance().activate(this, newitem);
            }

            // Combat Flag
            else if (FortSiegeManager.getInstance().isCombat(item.getItemId()))
            {
                if (FortSiegeManager.getInstance().activateCombatFlag(this, item))
                {
                    Fort fort = FortManager.getInstance().getFort(this);
                    fort.getSiege()
                            .announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG),
                                    getName());
                }
            }
        }
    }

    /**
     * Adds item to Inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param itemId      : int Item Identifier of the item to be added
     * @param count       : long Quantity of items to be added
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     */
    public L2ItemInstance addItem(String process, int itemId, long count, L2Object reference, boolean sendMessage)
    {
        if (count > 0)
        {
            L2ItemInstance item = null;
            if (ItemTable.getInstance().getTemplate(itemId) != null)
            {
                item = ItemTable.getInstance().createDummyItem(itemId);
            }
            else
            {
                Log.log(Level.SEVERE, "Item doesn't exist so cannot be added. Item ID: " + itemId);
                return null;
            }
            // Sends message to client if requested
            if (sendMessage && (!isCastingNow() && item.getItemType() == L2EtcItemType.HERB ||
                    item.getItemType() != L2EtcItemType.HERB))
            {
                if (count > 1)
                {
                    if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                        sm.addItemName(itemId);
                        sm.addItemNumber(count);
                        sendPacket(sm);
                    }
                    else
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
                        sm.addItemName(itemId);
                        sm.addItemNumber(count);
                        sendPacket(sm);
                    }
                }
                else
                {
                    if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                        sm.addItemName(itemId);
                        sendPacket(sm);
                    }
                    else
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
                        sm.addItemName(itemId);
                        sendPacket(sm);
                    }
                }
            }
            //Auto use herbs - autoloot
            if (item.getItemType() == L2EtcItemType.HERB) //If item is herb dont add it to iv :]
            {
                if (!isCastingNow())
                {
                    L2ItemInstance herb = new L2ItemInstance(_charId, itemId);
                    IItemHandler handler = ItemHandler.getInstance().getItemHandler(herb.getEtcItem());
                    if (handler == null)
                    {
                        Log.warning("No item handler registered for Herb - item ID " + herb.getItemId() + ".");
                    }
                    else
                    {
                        handler.useItem(this, herb, false);
                        if (_herbstask >= 100)
                        {
                            _herbstask -= 100;
                        }
                    }
                }
                else
                {
                    _herbstask += 100;
                    ThreadPoolManager.getInstance()
                            .scheduleAi(new HerbTask(process, itemId, count, reference, sendMessage), _herbstask);
                }
            }
            else
            {
                // Add the item to inventory
                L2ItemInstance createdItem = _inventory.addItem(process, itemId, count, this, reference);

                // If over capacity, drop the item
                if (!isGM() && !_inventory.validateCapacity(0, item.isQuestItem()) && createdItem.isDropable() &&
                        !item.isEquipable() &&
                        (!createdItem.isStackable() || createdItem.getLastChange() != L2ItemInstance.MODIFIED))
                {
                    dropItem("InvDrop", createdItem, null, true);
                }

                // Cursed Weapon
                else if (CursedWeaponsManager.getInstance().isCursed(createdItem.getItemId()))
                {
                    CursedWeaponsManager.getInstance().activate(this, createdItem);
                }

                // Combat Flag
                else if (FortSiegeManager.getInstance().isCombat(createdItem.getItemId()))
                {
                    if (FortSiegeManager.getInstance().activateCombatFlag(this, item))
                    {
                        Fort fort = FortManager.getInstance().getFort(this);
                        fort.getSiege()
                                .announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG),
                                        getName());
                    }
                }

                return createdItem;
            }
        }
        return null;
    }

    /**
     * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param item        : L2ItemInstance to be destroyed
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
    {
        return this.destroyItem(process, item, item.getCount(), reference, sendMessage);
    }

    /**
     * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param item        : L2ItemInstance to be destroyed
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean destroyItem(String process, L2ItemInstance item, long count, L2Object reference, boolean sendMessage)
    {
        item = _inventory.destroyItem(process, item, count, this, reference);

        if (item == null)
        {
            if (sendMessage)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            }
            return false;
        }

        // Send inventory update packet
        if (!Config.FORCE_INVENTORY_UPDATE)
        {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(item);
            sendPacket(playerIU);
        }
        else
        {
            sendPacket(new ItemList(this, false));
        }

        if (item.getItemId() == 57)
        {
            sendPacket(new ExAdenaInvenCount(getAdena(), getInventory().getSize(false)));
        }

        // Update current load as well
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(su);

        // Sends message to client if requested
        if (sendMessage)
        {
            if (count > 1)
            {
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                sm.addItemName(item);
                sm.addItemNumber(count);
                sendPacket(sm);
            }
            else
            {
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                sm.addItemName(item);
                sendPacket(sm);
            }
        }

        return true;
    }

    /**
     * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param objectId    : int Item Instance identifier of the item to be destroyed
     * @param count       : int Quantity of items to be destroyed
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    @Override
    public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
    {
        L2ItemInstance item = _inventory.getItemByObjectId(objectId);
        if (item == null)
        {
            if (sendMessage)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            }

            return false;
        }
        return this.destroyItem(process, item, count, reference, sendMessage);
    }

    /**
     * Destroys shots from inventory without logging and only occasional saving to database.
     * Sends a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param objectId    : int Item Instance identifier of the item to be destroyed
     * @param count       : int Quantity of items to be destroyed
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean destroyItemWithoutTrace(String process, int objectId, long count, L2Object reference, boolean sendMessage)
    {
        L2ItemInstance item = _inventory.getItemByObjectId(objectId);

        if (item == null || item.getCount() < count)
        {
            if (sendMessage)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            }

            return false;
        }

        return destroyItem(process, item, count, reference, sendMessage);
    }

    /**
     * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param itemId      : int Item identifier of the item to be destroyed
     * @param count       : int Quantity of items to be destroyed
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    @Override
    public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
    {
        if (itemId == 57)
        {
            return reduceAdena(process, count, reference, sendMessage);
        }

        L2ItemInstance item = _inventory.getItemByItemId(itemId);

        if (item == null || item.getCount() < count ||
                _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)
        {
            if (sendMessage)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            }

            return false;
        }

        // Send inventory update packet
        if (!Config.FORCE_INVENTORY_UPDATE)
        {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(item);
            sendPacket(playerIU);
        }
        else
        {
            sendPacket(new ItemList(this, false));
        }

        if (item.getItemId() == 57)
        {
            sendPacket(new ExAdenaInvenCount(getAdena(), getInventory().getSize(false)));
        }

        // Update current load as well
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(su);

        // Sends message to client if requested
        if (sendMessage)
        {
            if (count > 1)
            {
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                sm.addItemName(itemId);
                sm.addItemNumber(count);
                sendPacket(sm);
            }
            else
            {
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                sm.addItemName(itemId);
                sendPacket(sm);
            }
        }

        return true;
    }

    /**
     * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process   : String Identifier of process triggering this action
     * @param objectId  : int Item Identifier of the item to be transfered
     * @param count     : long Quantity of items to be transfered
     * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @return L2ItemInstance corresponding to the new item or the updated item in inventory
     */
    public L2ItemInstance transferItem(String process, int objectId, long count, Inventory target, L2Object reference)
    {
        L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
        if (oldItem == null)
        {
            return null;
        }
        L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
        if (newItem == null)
        {
            return null;
        }

        // Send inventory update packet
        if (!Config.FORCE_INVENTORY_UPDATE)
        {
            InventoryUpdate playerIU = new InventoryUpdate();

            if (oldItem.getCount() > 0 && oldItem != newItem)
            {
                playerIU.addModifiedItem(oldItem);
            }
            else
            {
                playerIU.addRemovedItem(oldItem);
            }

            sendPacket(playerIU);
        }
        else
        {
            sendPacket(new ItemList(this, false));
        }

        if (newItem.getItemId() == 57)
        {
            sendPacket(new ExAdenaInvenCount(getAdena(), getInventory().getSize(false)));
        }

        // Update current load as well
        StatusUpdate playerSU = new StatusUpdate(this);
        playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(playerSU);

        // Send target update packet
        if (target instanceof PcInventory)
        {
            L2PcInstance targetPlayer = ((PcInventory) target).getOwner();

            if (!Config.FORCE_INVENTORY_UPDATE)
            {
                InventoryUpdate playerIU = new InventoryUpdate();

                if (newItem.getCount() > count)
                {
                    playerIU.addModifiedItem(newItem);
                }
                else
                {
                    playerIU.addNewItem(newItem);
                }

                targetPlayer.sendPacket(playerIU);
            }
            else
            {
                targetPlayer.sendPacket(new ItemList(targetPlayer, false));
            }

            // Update current load as well
            playerSU = new StatusUpdate(targetPlayer);
            playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
            targetPlayer.sendPacket(playerSU);
        }
        else if (target instanceof PetInventory)
        {
            PetInventoryUpdate petIU = new PetInventoryUpdate();

            if (newItem.getCount() > count)
            {
                petIU.addModifiedItem(newItem);
            }
            else
            {
                petIU.addNewItem(newItem);
            }

            ((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
        }
        return newItem;
    }

    /**
     * Use instead of calling {@link #addItem(String, L2ItemInstance, L2Object, boolean)} and {@link #destroyItemByItemId(String, int, long, L2Object, boolean)}<br>
     * This method validates slots and weight limit, for stackable and non-stackable items.
     *
     * @param process     a generic string representing the process that is exchanging this items
     * @param reference   the (probably NPC) reference, could be null
     * @param coinId      the item Id of the item given on the exchange
     * @param cost        the amount of items given on the exchange
     * @param rewardId    the item received on the exchange
     * @param count       the amount of items received on the exchange
     * @param sendMessage if {@code true} it will send messages to the acting player
     * @return {@code true} if the player successfully exchanged the items, {@code false} otherwise
     */
    public boolean exchangeItemsById(String process, L2Object reference, int coinId, long cost, int rewardId, long count, boolean sendMessage)
    {
        final PcInventory inv = getInventory();
        if (!inv.validateCapacityByItemId(rewardId, count))
        {
            if (sendMessage)
            {
                sendPacket(SystemMessageId.SLOTS_FULL);
            }
            return false;
        }

        if (!inv.validateWeightByItemId(rewardId, count))
        {
            if (sendMessage)
            {
                sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
            }
            return false;
        }

        if (destroyItemByItemId(process, coinId, cost, reference, sendMessage))
        {
            addItem(process, rewardId, count, reference, sendMessage);
            return true;
        }
        return false;
    }

    /**
     * Drop item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param item        : L2ItemInstance to be dropped
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
    {
        item = _inventory.dropItem(process, item, this, reference);

        if (item == null)
        {
            if (sendMessage)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            }

            return false;
        }

        item.dropMe(this, getX() + Rnd.get(50) - 25, getY() + Rnd.get(50) - 25, getZ() + 20);

        if (Config.AUTODESTROY_ITEM_AFTER * 1000 > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM &&
                !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
        {
            if (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
            {
                ItemsAutoDestroy.getInstance().addItem(item);
            }
        }
        if (Config.DESTROY_DROPPED_PLAYER_ITEM)
        {
            if (!item.isEquipable() || item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
            {
                item.setProtected(false);
            }
            else
            {
                item.setProtected(true);
            }
        }
        else
        {
            item.setProtected(true);
        }

        // Send inventory update packet
        if (!Config.FORCE_INVENTORY_UPDATE)
        {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(item);
            sendPacket(playerIU);
        }
        else
        {
            sendPacket(new ItemList(this, false));
        }

        if (item.getItemId() == 57)
        {
            sendPacket(new ExAdenaInvenCount(getAdena(), getInventory().getSize(false)));
        }

        // Update current load as well
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(su);

        // Sends message to client if requested
        if (sendMessage)
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
            sm.addItemName(item);
            sendPacket(sm);
        }

        return true;
    }

    /**
     * Drop item from inventory by using its <B>objectID</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
     *
     * @param process     : String Identifier of process triggering this action
     * @param objectId    : int Item Instance identifier of the item to be dropped
     * @param count       : long Quantity of items to be dropped
     * @param x           : int coordinate for drop X
     * @param y           : int coordinate for drop Y
     * @param z           : int coordinate for drop Z
     * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage : boolean Specifies whether to send message to Client about this action
     * @return L2ItemInstance corresponding to the new item or the updated item in inventory
     */
    public L2ItemInstance dropItem(String process, int objectId, long count, int x, int y, int z, L2Object reference, boolean sendMessage)
    {
        L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
        L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);

        if (item == null)
        {
            if (sendMessage)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            }

            return null;
        }

        item.dropMe(this, x, y, z);

        if (Config.AUTODESTROY_ITEM_AFTER * 1000 > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM &&
                !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
        {
            if (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
            {
                ItemsAutoDestroy.getInstance().addItem(item);
            }
        }
        if (Config.DESTROY_DROPPED_PLAYER_ITEM)
        {
            if (!item.isEquipable() || item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
            {
                item.setProtected(false);
            }
            else
            {
                item.setProtected(true);
            }
        }
        else
        {
            item.setProtected(true);
        }

        // Send inventory update packet
        if (!Config.FORCE_INVENTORY_UPDATE)
        {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(invitem);
            sendPacket(playerIU);
        }
        else
        {
            sendPacket(new ItemList(this, false));
        }

        if (item.getItemId() == 57)
        {
            sendPacket(new ExAdenaInvenCount(getAdena(), getInventory().getSize(false)));
        }

        // Update current load as well
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(su);

        // Sends message to client if requested
        if (sendMessage)
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
            sm.addItemName(item);
            sendPacket(sm);
        }

        return item;
    }

    public L2ItemInstance checkItemManipulation(int objectId, long count, String action)
    {
        //TODO: if we remove objects that are not visisble from the L2World, we'll have to remove this check
        if (L2World.getInstance().findObject(objectId) == null)
        {
            Log.finest(getObjectId() + ": player tried to " + action + " item not available in L2World");
            return null;
        }

        L2ItemInstance item = getInventory().getItemByObjectId(objectId);

        if (item == null || item.getOwnerId() != getObjectId())
        {
            Log.finest(getObjectId() + ": player tried to " + action + " item he is not owner of");
            return null;
        }

        if (count < 0 || count > 1 && !item.isStackable())
        {
            Log.finest(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
            return null;
        }

        if (count > item.getCount())
        {
            Log.finest(getObjectId() + ": player tried to " + action + " more items than he owns");
            return null;
        }

        // Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
        if (getPet() != null && getPet().getControlObjectId() == objectId || getMountObjectID() == objectId)
        {
            if (Config.DEBUG)
            {
                Log.finest(getObjectId() + ": player tried to " + action + " item controling pet");
            }

            return null;
        }

        if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
        {
            if (Config.DEBUG)
            {
                Log.finest(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
            }

            return null;
        }

        // We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
        if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow()))
        {
            return null;
        }

        return item;
    }

    /**
     * Set _protectEndTime according settings.
     */
    public void setProtection(boolean protect)
    {
        if (Config.DEVELOPER && (protect || _protectEndTime > 0))
        {
            Log.warning(getName() + ": Protection " + (protect ? "ON " +
                    (TimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * TimeController.TICKS_PER_SECOND) :
                    "OFF") + " (currently " + TimeController.getGameTicks() + ")");
        }

        _protectEndTime = protect ?
                TimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * TimeController.TICKS_PER_SECOND : 0;
    }

    public void setTeleportProtection(boolean protect)
    {
        if (Config.DEVELOPER && (protect || _teleportProtectEndTime > 0))
        {
            Log.warning(getName() + ": Tele Protection " + (protect ? "ON " + (TimeController.getGameTicks() +
                    Config.PLAYER_TELEPORT_PROTECTION * TimeController.TICKS_PER_SECOND) : "OFF") + " (currently " +
                    TimeController.getGameTicks() + ")");
        }

        _teleportProtectEndTime = protect ?
                TimeController.getGameTicks() + Config.PLAYER_TELEPORT_PROTECTION * TimeController.TICKS_PER_SECOND : 0;
    }

    /**
     * Set protection from agro mobs when getting up from fake death, according settings.
     */
    public void setRecentFakeDeath(boolean protect)
    {
        _recentFakeDeathEndTime = protect ? TimeController.getGameTicks() +
                Config.PLAYER_FAKEDEATH_UP_PROTECTION * TimeController.TICKS_PER_SECOND : 0;
    }

    public boolean isRecentFakeDeath()
    {
        return _recentFakeDeathEndTime > TimeController.getGameTicks();
    }

    public final boolean isFakeDeath()
    {
        return _isFakeDeath;
    }

    public final void setIsFakeDeath(boolean value)
    {
        _isFakeDeath = value;
    }

    @Override
    public final boolean isAlikeDead()
    {
        if (super.isAlikeDead())
        {
            return true;
        }

        return isFakeDeath();
    }

    /**
     * Get the client owner of this char.<BR><BR>
     */
    public L2GameClient getClient()
    {
        return _client;
    }

    public void setClient(L2GameClient client)
    {
        _client = client;
    }

    private void closeNetConnection(boolean closeClient)
    {
        closeNetConnection(closeClient, false, null);
    }

    /**
     * Close the active connection with the client.<BR><BR>
     */
    public void closeNetConnection(boolean closeClient, final boolean blockDisconnectTask, final L2GameServerPacket packet)
    {
        L2GameClient client = _client;
        if (client != null)
        {
            if (client.isDetached())
            {
                client.cleanMe(true);
            }
            else
            {
                if (!client.getConnection().isClosed())
                {
                    if (packet != null)
                    {
                        client.close(packet, blockDisconnectTask);
                    }
                    else if (closeClient)
                    {
                        client.close(LeaveWorld.STATIC_PACKET);
                    }
                    else
                    {
                        client.close(ServerClose.STATIC_PACKET);
                    }
                }
            }
        }
    }

    /**
     * @see l2server.gameserver.model.actor.L2Character#enableSkill(l2server.gameserver.model.L2Skill)
     */
    @Override
    public void enableSkill(L2Skill skill)
    {
        super.enableSkill(skill);
        _reuseTimeStamps.remove(skill.getReuseHashCode());
    }

    /**
     * @see l2server.gameserver.model.actor.L2Character#checkDoCastConditions(l2server.gameserver.model.L2Skill)
     */
    @Override
    protected boolean checkDoCastConditions(L2Skill skill)
    {
        if (!super.checkDoCastConditions(skill))
        {
            return false;
        }

        switch (skill.getSkillType())
        {
            case SUMMON_TRAP:
            {
                if (isInsideZone(ZONE_PEACE))
                {
                    sendPacket(SystemMessage
                            .getSystemMessage(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE));
                    return false;
                }
                if (getTrap() != null && getTrap().getSkill().getId() == ((L2SkillTrap) skill).getTriggerSkillId())
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                    sm.addSkillName(skill);
                    sendPacket(sm);
                    return false;
                }
                break;
            }
            case SUMMON:
            {
                boolean canSummon = true;
                if (!getSummons().isEmpty() && (getMaxSummonPoints() == 0 ||
                        getSpentSummonPoints() + ((L2SkillSummon) skill).getSummonPoints() > getMaxSummonPoints()))
                {
                    canSummon = false;
                }
                if (!((L2SkillSummon) skill).isCubic() && !canSummon)
                {
                    if (Config.DEBUG)
                    {
                        Log.fine("player has a pet already. ignore summon skill");
                    }

                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
                    return false;
                }

                if (isPlayingEvent() && _event.isType(EventType.LuckyChests))
                {
                    sendMessage("During a Lucky Chests event you are not allowed to summon a servitor.");
                    return false;
                }
            }
        }

        // TODO: Should possibly be checked only in L2PcInstance's useMagic
        // Can't use Hero and resurrect skills during Olympiad
        if (isInOlympiadMode() && (skill.isHeroSkill() || skill.getSkillType() == L2SkillType.RESURRECT))
        {
            SystemMessage sm =
                    SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            sendPacket(sm);
            return false;
        }

        final int charges = getCharges();
        // Check if the spell using charges or not in AirShip
        if (skill.getMaxCharges() == 0 && charges < skill.getNumCharges() ||
                isInAirShip() && skill.getSkillType() != L2SkillType.REFUEL)
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
            sendPacket(sm);
            return false;
        }

        return true;
    }

    /**
     * Returns true if cp update should be done, false if not
     *
     * @return boolean
     */
    private boolean needCpUpdate(int barPixels)
    {
        double currentCp = getCurrentCp();

        if (currentCp <= 1.0 || getMaxCp() < barPixels)
        {
            return true;
        }

        if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck)
        {
            if (currentCp == getMaxCp())
            {
                _cpUpdateIncCheck = currentCp + 1;
                _cpUpdateDecCheck = currentCp - _cpUpdateInterval;
            }
            else
            {
                double doubleMulti = currentCp / _cpUpdateInterval;
                int intMulti = (int) doubleMulti;

                _cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
                _cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
            }

            return true;
        }

        return false;
    }

    /**
     * Returns true if mp update should be done, false if not
     *
     * @return boolean
     */
    private boolean needMpUpdate(int barPixels)
    {
        double currentMp = getCurrentMp();

        if (currentMp <= 1.0 || getMaxMp() < barPixels)
        {
            return true;
        }

        if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck)
        {
            if (currentMp == getMaxMp())
            {
                _mpUpdateIncCheck = currentMp + 1;
                _mpUpdateDecCheck = currentMp - _mpUpdateInterval;
            }
            else
            {
                double doubleMulti = currentMp / _mpUpdateInterval;
                int intMulti = (int) doubleMulti;

                _mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
                _mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
            }

            return true;
        }

        return false;
    }

    /**
     * Send packet StatusUpdate with current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance </li><BR>
     * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party </li><BR><BR>
     * <p>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2PcInstance of the _statusListener</B></FONT><BR><BR>
     */
    @Override
    public void broadcastStatusUpdate(L2Character causer, StatusUpdateDisplay display)
    {
        // Send the Server->Client packet StatusUpdate with current HP and MP to all L2PcInstance that must be informed of HP/MP updates of this L2PcInstance
        //super.broadcastStatusUpdate(causer, display);

        // Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance
        StatusUpdate su = new StatusUpdate(this, causer, display);
        su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
        su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
        su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
        su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
        sendPacket(su);

        final boolean needCpUpdate = needCpUpdate(352);
        final boolean needHpUpdate = needHpUpdate(352);

        // Go through the StatusListener
        // Send the Server->Client packet StatusUpdate with current HP and MP
        if (needHpUpdate)
        {
            //for (L2Character temp : getStatus().getStatusListener())
            for (L2PcInstance temp : getKnownList().getKnownPlayersInRadius(600))
            {
                if (temp != null)
                {
                    temp.sendPacket(su);
                }
            }

            for (L2Character temp : getStatus().getStatusListener())
            {
                if (temp != null && !temp.isInsideRadius(this, 600, false, false))
                {
                    temp.sendPacket(su);
                }
            }
        }

        // Check if a party is in progress and party window update is usefull
        L2Party party = _party;
        if (party != null && (needCpUpdate || needHpUpdate || needMpUpdate(352)))
        {
            if (Config.DEBUG)
            {
                Log.fine(
                        "Send status for party window of " + getObjectId() + " (" + getName() + ") to his party. CP: " +
                                getCurrentCp() + " HP: " + getCurrentHp() + " MP: " + getCurrentMp());
            }
            // Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party
            PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
            party.broadcastToPartyMembers(this, update);
            party.broadcastToPartyMembers(this, su);
        }

        if (isInOlympiadMode() && isOlympiadStart() && (needCpUpdate || needHpUpdate))
        {
            final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(getOlympiadGameId());
            if (game != null && game.isBattleStarted())
            {
                game.getZone().broadcastStatusUpdate(this);
            }
        }

        // In duel MP updated only with CP or HP
        if (isInDuel() && (needCpUpdate || needHpUpdate))
        {
            ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
            DuelManager.getInstance().broadcastToOppositTeam(this, update);
            DuelManager.getInstance().broadcastToOppositTeam(this, su);
        }
    }

    /**
     * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR><BR>
     * <p>
     * <B><U> Concept</U> :</B><BR><BR>
     * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
     * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
     * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
     * <p>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
     * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
     */
    public final void broadcastUserInfo()
    {
        // Send a Server->Client packet UserInfo to this L2PcInstance
        sendPacket(new UserInfo(this));
        sendPacket(new ExUserPaperdoll(this));
        sendPacket(new ExUserCubics(this));
        sendPacket(new ExUserLoad(this));
        sendPacket(new ExUserEffects(this));
        // Send also SubjobInfo
        //sendPacket(new ExSubjobInfo(this));
        sendPacket(new ExVitalityEffectInfo(getVitalityPoints(), 200));

        // Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance
        if (Config.DEBUG)
        {
            Log.fine("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] 03 CharInfo");
        }

        broadcastPacket(new CharInfo(this));

        //broadcastAbnormalStatusUpdate();
    }

    public final void broadcastTitleInfo()
    {
        // Send a Server->Client packet UserInfo to this L2PcInstance
        sendPacket(new UserInfo(this));

        // Send a Server->Client packet TitleUpdate to all L2PcInstance in _KnownPlayers of the L2PcInstance
        if (Config.DEBUG)
        {
            Log.fine("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] cc TitleUpdate");
        }

        broadcastPacket(new NicknameChanged(this));
    }

    @Override
    public final void broadcastPacket(L2GameServerPacket mov)
    {
        if (!(mov instanceof CharInfo))
        {
            sendPacket(mov);
        }

        mov.setInvisibleCharacter(getAppearance().getInvisible() ? getObjectId() : 0);

        Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
        //synchronized (getKnownList().getKnownPlayers())
        {
            for (L2PcInstance player : plrs)
            {
                if (player == null)
                {
                    continue;
                }

                player.sendPacket(mov);
                if (mov instanceof CharInfo)
                {
                    int relation = getRelation(player);
                    Integer oldrelation = getKnownList().getKnownRelations().get(player.getObjectId());
                    if (oldrelation != null && oldrelation != relation)
                    {
                        player.sendPacket(new RelationChanged(this, relation, isAutoAttackable(player)));
                        if (getPet() != null)
                        {
                            player.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable(player)));
                        }
                        for (L2SummonInstance summon : getSummons())
                        {
                            player.sendPacket(new RelationChanged(summon, relation, isAutoAttackable(player)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
    {
        if (!(mov instanceof CharInfo))
        {
            sendPacket(mov);
        }

        mov.setInvisibleCharacter(getAppearance().getInvisible() ? getObjectId() : 0);

        boolean isInvisible = getAppearance().getInvisible();

        Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
        //synchronized (getKnownList().getKnownPlayers())
        {
            for (L2PcInstance player : plrs)
            {
                if (player == null)
                {
                    continue;
                }
                else if (!player.isGM() && isInvisible && !isInSameParty(player))
                {
                    continue;
                }

                if (isInsideRadius(player, radiusInKnownlist, false, false))
                {
                    player.sendPacket(mov);
                    if (mov instanceof CharInfo)
                    {
                        int relation = getRelation(player);
                        Integer oldrelation = getKnownList().getKnownRelations().get(player.getObjectId());
                        if (oldrelation != null && oldrelation != relation)
                        {
                            player.sendPacket(new RelationChanged(this, relation, isAutoAttackable(player)));
                            if (getPet() != null)
                            {
                                player.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable(player)));
                            }
                            for (L2SummonInstance summon : getSummons())
                            {
                                player.sendPacket(new RelationChanged(summon, relation, isAutoAttackable(player)));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Return the Alliance Identifier of the L2PcInstance.<BR><BR>
     */
    public int getAllyId()
    {
        if (_clan == null)
        {
            return 0;
        }
        else
        {
            return _clan.getAllyId();
        }
    }

    public int getAllyCrestId()
    {
        if (getClanId() == 0)
        {
            return 0;
        }
        if (getClan().getAllyId() == 0)
        {
            return 0;
        }
        return getClan().getAllyCrestId();
    }

    public void queryGameGuard()
    {
        getClient().setGameGuardOk(false);
        this.sendPacket(new GameGuardQuery());
        if (Config.GAMEGUARD_ENFORCE)
        {
            ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheck(), 30 * 1000);
        }
    }

    private class GameGuardCheck implements Runnable
    {
        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run()
        {
            L2GameClient client = getClient();
            if (client != null && !client.isAuthedGG() && isOnline())
            {
                GmListTable.broadcastMessageToGMs(
                        "Client " + client + " failed to reply GameGuard query and is being kicked!");
                Log.info("Client " + client + " failed to reply GameGuard query and is being kicked!");
                client.close(LeaveWorld.STATIC_PACKET);
            }
        }
    }

    /**
     * Send a Server->Client packet StatusUpdate to the L2PcInstance.<BR><BR>
     */
    @Override
    public void sendPacket(L2GameServerPacket packet)
    {
        if (_client != null)
        {
            _client.sendPacket(packet);
        }
    }

    /**
     * Send SystemMessage packet.<BR><BR>
     *
     * @param id: SystemMessageId
     */
    public void sendPacket(SystemMessageId id)
    {
        sendPacket(SystemMessage.getSystemMessage(id));
    }

    /**
     * Manage Interact Task with another L2PcInstance.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the L2PcInstance</li>
     * <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the L2PcInstance</li>
     * <li>If the private store is a STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the L2PcInstance</li><BR><BR>
     *
     * @param target The L2Character targeted
     */
    public void doInteract(L2Character target)
    {
        if (target instanceof L2PcInstance)
        {
            L2PcInstance temp = (L2PcInstance) target;
            sendPacket(ActionFailed.STATIC_PACKET);

            if (temp.getPrivateStoreType() == STORE_PRIVATE_SELL ||
                    temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL)
            {
                sendPacket(new PrivateStoreListSell(this, temp));
            }
            else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
            {
                sendPacket(new PrivateStoreListBuy(this, temp));
            }
            else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
            {
                sendPacket(new RecipeShopSellList(this, temp));
            }
            else if (temp.getPrivateStoreType() == STORE_PRIVATE_CUSTOM_SELL)
            {
                sendPacket(new PlayerMultiSellList(temp));
            }
        }
        else
        {
            // _interactTarget=null should never happen but one never knows ^^;
            if (target != null)
            {
                target.onAction(this);
            }
        }
    }

    /**
     * Manage AutoLoot Task.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
     * <li>Add the Item to the L2PcInstance inventory</li>
     * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
     * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
     * <p>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
     *
     * @param target The L2ItemInstance dropped
     */
    public void doAutoLoot(L2Attackable target, L2Attackable.RewardItem item)
    {
        L2Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
        if (isInParty() && itemTemplate.getItemType() != L2EtcItemType.HERB)
        {
            getParty().distributeItem(this, item, false, target);
        }
        else if (item.getItemId() == 57)
        {
            addAdena("Loot", item.getCount(), target, true);
        }
        else
        {
            boolean canLootToWarehouse = itemTemplate.isStackable();

            switch (item.getItemId())
            {
                case 5572: // Wind Mantra
                case 5570: // Water Mantra
                case 5574: // Fire Mantra
                case 50007: // Raid Soul
                case 50008: // Raid Feather
                case 50009: // Raid Heart
                    canLootToWarehouse = false;
                    break;
            }

            if (canLootToWarehouse && getConfigValue("autoLootStackableToWH") && getWarehouse().validateCapacity(5))
            {
                getWarehouse().addItem("Loot", item.getItemId(), item.getCount(), this, target);

                SystemMessage s = SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_CHATBOX_S1);

                s.addString(
                        item.getCount() + " " + ItemTable.getInstance().getTemplate(item.getItemId()).getName() + " " +
                                (item.getCount() == 1 ? "was" : "were") + " added to your Warehouse.");
                sendPacket(s);
            }
            else
            {
				/*
				final int itemId = item.getItemId();
				if (CompoundTable.getInstance().isCombinable(item.getItemId()))
				{
					L2ItemInstance sameItem = getInventory().getItemByItemId(itemId);
					if (sameItem != null)
					{
						Combination combination = CompoundTable.getInstance().getCombination(item.getItemId(), sameItem.getItemId());

						int rnd = Rnd.get(100);
						if (rnd >= combination.getChance())
						{
							sendSysMessage(sameItem.getName() + " failed to level up.");
						}
						else
						{
							int newItemId = combination.getResult();

							destroyItem("Compound", sameItem, this, true);

							addItem("Compound", newItemId, 1, this, true);

							sendSysMessage(sameItem.getName() + " turned into a " + ItemTable.getInstance().getTemplate(newItemId).getName());
						}
					}
					else
						addItem("Loot", item.getItemId(), item.getCount(), target, true);

					for (L2ItemInstance i : getInventory().getItemsByItemId(item.getItemId() + 1))
					{
						sameItem = getInventory().getItemByItemId(i.getItemId());

						if (sameItem.getObjectId() == i.getObjectId())
							continue;

						Combination combination = CompoundTable.getInstance().getCombination(sameItem.getItemId(), sameItem.getItemId());

						int rnd = Rnd.get(100);
						if (rnd >= combination.getChance())
						{
							sendSysMessage(sameItem.getName() + " failed to level up.");
							destroyItem("Compound", sameItem, this, true);
							continue;
						}

						int newItemId = combination.getResult();

						destroyItem("Compound", i, this, true);
						destroyItem("Compound", sameItem, this, true);

						addItem("Compound", newItemId, 1, this, true);

						sendSysMessage(sameItem.getName() + " turned into a " + ItemTable.getInstance().getTemplate(newItemId).getName());
					}
				}
				else*/
                addItem("Loot", item.getItemId(), item.getCount(), target, true);
            }
        }
    }

    /**
     * Manage Pickup Task.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Send a Server->Client packet StopMove to this L2PcInstance </li>
     * <li>Remove the L2ItemInstance from the world and send server->client GetItem packets </li>
     * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
     * <li>Add the Item to the L2PcInstance inventory</li>
     * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
     * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
     * <p>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
     *
     * @param object The L2ItemInstance to pick up
     */
    protected void doPickupItem(L2Object object)
    {
        if (isAlikeDead() || isFakeDeath())
        {
            return;
        }

        // Set the AI Intention to AI_INTENTION_IDLE
        getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

        // Check if the L2Object to pick up is a L2ItemInstance
        if (!(object instanceof L2ItemInstance))
        {
            // dont try to pickup anything that is not an item :)
            Log.warning(this + " trying to pickup wrong target." + getTarget());
            return;
        }

        L2ItemInstance target = (L2ItemInstance) object;

        // Send a Server->Client packet ActionFailed to this L2PcInstance
        sendPacket(ActionFailed.STATIC_PACKET);

        // Send a Server->Client packet StopMove to this L2PcInstance
        StopMove sm = new StopMove(this);
        if (Config.DEBUG)
        {
            Log.fine("pickup pos: " + target.getX() + " " + target.getY() + " " + target.getZ());
        }
        sendPacket(sm);

        synchronized (target)
        {
            // Check if the target to pick up is visible
            if (!target.isVisible())
            {
                // Send a Server->Client packet ActionFailed to this L2PcInstance
                sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }

            if ((isInParty() && getParty().getLootDistribution() == L2Party.ITEM_LOOTER || !isInParty()) &&
                    !_inventory.validateCapacity(target))
            {
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
                return;
            }

            if (target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() &&
                    !isInLooterParty(target.getOwnerId()))
            {
                sendPacket(ActionFailed.STATIC_PACKET);

                if (target.getItemId() == 57)
                {
                    SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
                    smsg.addItemNumber(target.getCount());
                    sendPacket(smsg);
                }
                else if (target.getCount() > 1)
                {
                    SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
                    smsg.addItemName(target);
                    smsg.addItemNumber(target.getCount());
                    sendPacket(smsg);
                }
                else
                {
                    SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
                    smsg.addItemName(target);
                    sendPacket(smsg);
                }

                return;
            }

            // You can pickup only 1 combat flag
            if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
            {
                if (!FortSiegeManager.getInstance().checkIfCanPickup(this))
                {
                    return;
                }
            }

            if (target.getItemLootShedule() != null &&
                    (target.getOwnerId() == getObjectId() || isInLooterParty(target.getOwnerId())))
            {
                target.resetOwnerTimer();
            }

            // Remove the L2ItemInstance from the world and send server->client GetItem packets
            target.pickupMe(this);
            if (Config.SAVE_DROPPED_ITEM) // item must be removed from ItemsOnGroundManager if is active
            {
                ItemsOnGroundManager.getInstance().removeObject(target);
            }
        }

        //Auto use herbs - pick up
        if (target.getItemType() == L2EtcItemType.HERB)
        {
            IItemHandler handler = ItemHandler.getInstance().getItemHandler(target.getEtcItem());
            if (handler == null)
            {
                Log.fine("No item handler registered for item ID " + target.getItemId() + ".");
            }
            else
            {
                handler.useItem(this, target, false);
            }
            ItemTable.getInstance().destroyItem("Consume", target, this, null);
        }
        // Cursed Weapons are not distributed
        else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
        {
            addItem("Pickup", target, null, true);
        }
        else if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
        {
            addItem("Pickup", target, null, true);
        }
        else
        {
            // if item is instance of L2ArmorType or L2WeaponType broadcast an "Attention" system message
            if (target.getItemType() instanceof L2ArmorType || target.getItemType() instanceof L2WeaponType)
            {
                if (target.getEnchantLevel() > 0)
                {
                    SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2_S3);
                    msg.addPcName(this);
                    msg.addNumber(target.getEnchantLevel());
                    msg.addItemName(target.getItemId());
                    broadcastPacket(msg, 1400);
                }
                else
                {
                    SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2);
                    msg.addPcName(this);
                    msg.addItemName(target.getItemId());
                    broadcastPacket(msg, 1400);
                }
            }

            // Check if a Party is in progress
            if (isInParty())
            {
                getParty().distributeItem(this, target);
            }
            // Target is adena
            else if (target.getItemId() == 57 && getInventory().getAdenaInstance() != null)
            {
                addAdena("Pickup", target.getCount(), null, true);
                ItemTable.getInstance().destroyItem("Pickup", target, this, null);
            }
            // Target is regular item
            else
            {
                addItem("Pickup", target, null, true);
            }
        }
    }

    public boolean canOpenPrivateStore()
    {
        return !isAlikeDead() && !isInOlympiadMode() && !isMounted() && !isInsideZone(ZONE_NOSTORE) && !isCastingNow();
    }

    public void tryOpenPrivateBuyStore()
    {
        // Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
        if (canOpenPrivateStore())
        {
            if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY ||
                    getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY + 1)
            {
                setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
            }
            if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
            {
                if (isSitting())
                {
                    standUp();
                }
                setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY + 1);
                sendPacket(new PrivateStoreManageListBuy(this));
            }
        }
        else
        {
            if (isInsideZone(ZONE_NOSTORE))
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_PRIVATE_STORE_HERE));
            }
            sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void tryOpenPrivateSellStore(boolean isPackageSale)
    {
        // Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
        if (canOpenPrivateStore())
        {
            if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL ||
                    getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL + 1 ||
                    getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
            {
                setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
            }

            if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
            {
                if (isSitting())
                {
                    standUp();
                }
                setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL + 1);
                sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
            }
        }
        else
        {
            if (isInsideZone(ZONE_NOSTORE))
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_PRIVATE_STORE_HERE));
            }
            sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public final PreparedListContainer getMultiSell()
    {
        return _currentMultiSell;
    }

    public final void setMultiSell(PreparedListContainer list)
    {
        _currentMultiSell = list;
    }

    @Override
    public boolean isTransformed()
    {
        return _transformation != null && !_transformation.isStance();
    }

    public boolean isInStance()
    {
        return _transformation != null && _transformation.isStance();
    }

    public void transform(L2Transformation transformation)
    {
        if (_transformation != null)
        {
            // You already polymorphed and cannot polymorph again.
            SystemMessage msg =
                    SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
            sendPacket(msg);
            return;
        }
        setQueuedSkill(null, false, false);
        if (isMounted())
        {
            // Get off the strider or something else if character is mounted
            dismount();
        }

        _transformation = transformation;
        sendSysMessage("Transformation = " + _transformation);
        //stopAllToggles(); Looks like Toggles aren't removed any more at GoD
        transformation.onTransform();
        sendSkillList();
        sendPacket(new SkillCoolTime(this));
        sendPacket(ExBasicActionList.getStaticPacket(this));

        // To avoid falling underground
        setXYZ(getX(), getY(), GeoData.getInstance().getHeight(getX(), getY(), getZ()) + 20);
        broadcastUserInfo();
    }

    @Override
    public void unTransform(boolean removeEffects)
    {
        if (_transformation != null)
        {
            setTransformAllowedSkills(new int[]{});
            _transformation.onUntransform();
            _transformation = null;
            if (removeEffects)
            {
                stopEffects(L2AbnormalType.MUTATE);
            }
            sendSkillList();
            sendPacket(new SkillCoolTime(this));
            sendPacket(ExBasicActionList.getStaticPacket(this));

            // To avoid falling underground
            //if (!isGM())
            setXYZ(getX(), getY(), GeoData.getInstance().getHeight(getX(), getY(), getZ()) + 20);
            broadcastUserInfo();
        }
    }

    public L2Transformation getTransformation()
    {
        return _transformation;
    }

    /**
     * This returns the transformation Id of the current transformation.
     * For example, if a player is transformed as a Buffalo, and then picks up the Zariche,
     * the transform Id returned will be that of the Zariche, and NOT the Buffalo.
     *
     * @return Transformation Id
     */
    public int getTransformationId()
    {
        return _transformation == null ? 0 : _transformation.getId();
    }

    /**
     * This returns the transformation Id stored inside the character table, selected by the method: transformSelectInfo()
     * For example, if a player is transformed as a Buffalo, and then picks up the Zariche,
     * the transform Id returned will be that of the Buffalo, and NOT the Zariche.
     *
     * @return Transformation Id
     */
    public int transformId()
    {
        return _transformationId;
    }

    /**
     * This is a simple query that inserts the transform Id into the character table for future reference.
     */
    public void transformInsertInfo()
    {
        _transformationId = getTransformationId();

        if (_transformationId == L2Transformation.TRANSFORM_AKAMANAH ||
                _transformationId == L2Transformation.TRANSFORM_ZARICHE)
        {
            return;
        }

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_TRANSFORM);

            statement.setInt(1, _transformationId);
            statement.setInt(2, getObjectId());

            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Transformation insert info: ", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * This selects the current
     *
     * @return transformation Id
     */
    public int transformSelectInfo()
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(SELECT_CHAR_TRANSFORM);

            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();
            rset.next();
            _transformationId = rset.getInt("transform_id");

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Transformation select info: ", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
        return _transformationId;
    }

    /**
     * Set a target.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character </li>
     * <li>Add the L2PcInstance to the _statusListener of the new target if it's a L2Character </li>
     * <li>Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)</li><BR><BR>
     *
     * @param newTarget The L2Object to target
     */
    @Override
    public void setTarget(L2Object newTarget)
    {
        if (newTarget != null)
        {
            boolean isParty = newTarget instanceof L2PcInstance && isInParty() &&
                    getParty().getPartyMembers().contains(newTarget);

            // Check if the new target is visible
            if (!isParty && !newTarget.isVisible())
            {
                newTarget = null;
            }

            // Prevents /target exploiting
            if (newTarget != null && !isParty && Math.abs(newTarget.getZ() - getZ()) > 1000)
            {
                newTarget = null;
            }
        }
        if (!isGM())
        {
            // vehicles cant be targeted
            if (newTarget instanceof L2Vehicle)
            {
                newTarget = null;
            }
        }

        // Get the current target
        L2Object oldTarget = getTarget();

        if (oldTarget != null)
        {
            if (oldTarget.equals(newTarget))
            {
                return; // no target change
            }

            // Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character
            if (oldTarget instanceof L2Character)
            {
                ((L2Character) oldTarget).removeStatusListener(this);
            }
        }

        // Add the L2PcInstance to the _statusListener of the new target if it's a L2Character
        if (newTarget instanceof L2Character)
        {
            ((L2Character) newTarget).addStatusListener(this);
            MyTargetSelected my = new MyTargetSelected(newTarget.getObjectId(), 0);
            sendPacket(my);
            TargetSelected my2 = new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ());
            broadcastPacket(my2);
            sendPacket(new AbnormalStatusUpdateFromTarget((L2Character) newTarget));
        }
        if (newTarget == null && getTarget() != null)
        {
            broadcastPacket(new TargetUnselected(this));
        }

        // Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)
        super.setTarget(newTarget);
    }

    /**
     * Return the active weapon instance (always equiped in the right hand).<BR><BR>
     */
    @Override
    public L2ItemInstance getActiveWeaponInstance()
    {
        return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
    }

    /**
     * Return the active weapon item (always equiped in the right hand).<BR><BR>
     */
    @Override
    public L2Weapon getActiveWeaponItem()
    {
        L2ItemInstance weapon = getActiveWeaponInstance();

        if (weapon == null)
        {
            return getFistsWeaponItem();
        }

        if (!(weapon.getItem() instanceof L2Weapon))
        {
            Log.warning(getName() + " is using " + weapon.getName() + " as Weapon but it isn't one.");
            return null;
        }

        return (L2Weapon) weapon.getItem();
    }

    public L2ItemInstance getChestArmorInstance()
    {
        return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
    }

    public L2ItemInstance getLegsArmorInstance()
    {
        return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
    }

    public L2Armor getActiveChestArmorItem()
    {
        L2ItemInstance armor = getChestArmorInstance();

        if (armor == null)
        {
            return null;
        }

        return (L2Armor) armor.getItem();
    }

    public L2Armor getActiveLegsArmorItem()
    {
        L2ItemInstance legs = getLegsArmorInstance();

        if (legs == null)
        {
            return null;
        }

        return (L2Armor) legs.getItem();
    }

    public boolean isWearingHeavyArmor()
    {
        L2ItemInstance legs = getLegsArmorInstance();
        L2ItemInstance armor = getChestArmorInstance();

        if (armor != null && legs != null)
        {
            if ((L2ArmorType) legs.getItemType() == L2ArmorType.HEAVY &&
                    (L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY)
            {
                return true;
            }
        }
        if (armor != null)
        {
            if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() ==
                    L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isWearingLightArmor()
    {
        L2ItemInstance legs = getLegsArmorInstance();
        L2ItemInstance armor = getChestArmorInstance();

        if (armor != null && legs != null)
        {
            if ((L2ArmorType) legs.getItemType() == L2ArmorType.LIGHT &&
                    (L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT)
            {
                return true;
            }
        }
        if (armor != null)
        {
            if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() ==
                    L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isWearingMagicArmor()
    {
        L2ItemInstance legs = getLegsArmorInstance();
        L2ItemInstance armor = getChestArmorInstance();

        if (armor != null && legs != null)
        {
            if ((L2ArmorType) legs.getItemType() == L2ArmorType.MAGIC &&
                    (L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC)
            {
                return true;
            }
        }
        if (armor != null)
        {
            if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() ==
                    L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isMarried()
    {
        return _married;
    }

    public void setMarried(boolean state)
    {
        _married = state;
    }

    public boolean isEngageRequest()
    {
        return _engagerequest;
    }

    public void setEngageRequest(boolean state, int playerid)
    {
        _engagerequest = state;
        _engageid = playerid;
    }

    public void setMarryRequest(boolean state)
    {
        _marryrequest = state;
    }

    public boolean isMarryRequest()
    {
        return _marryrequest;
    }

    public void setMarryAccepted(boolean state)
    {
        _marryaccepted = state;
    }

    public boolean isMarryAccepted()
    {
        return _marryaccepted;
    }

    public int getEngageId()
    {
        return _engageid;
    }

    public int getPartnerId()
    {
        return _partnerId;
    }

    public void setPartnerId(int partnerid)
    {
        _partnerId = partnerid;
    }

    public int getCoupleId()
    {
        return _coupleId;
    }

    public void setCoupleId(int coupleId)
    {
        _coupleId = coupleId;
    }

    public void engageAnswer(int answer)
    {
        if (_engagerequest == false)
        {
            return;
        }
        else if (_engageid == 0)
        {
            return;
        }
        else
        {
            L2PcInstance ptarget = L2World.getInstance().getPlayer(_engageid);
            setEngageRequest(false, 0);
            if (ptarget != null)
            {
                if (answer == 1)
                {
                    CoupleManager.getInstance().createCouple(ptarget, L2PcInstance.this);
                    ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
                }
                else
                {
                    ptarget.sendMessage("Request to Engage has been >DENIED<!");
                }
            }
        }
    }

    /**
     * Return the secondary weapon instance (always equiped in the left hand).<BR><BR>
     */
    @Override
    public L2ItemInstance getSecondaryWeaponInstance()
    {
        return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
    }

    /**
     * Return the secondary L2Item item (always equiped in the left hand).<BR>
     * Arrows, Shield..<BR>
     */
    @Override
    public L2Item getSecondaryWeaponItem()
    {
        L2ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        if (item != null)
        {
            return item.getItem();
        }
        return null;
    }

    /**
     * Kill the L2Character, Apply Death Penalty, Manage gain/loss Reputation and Item Drop.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty </li>
     * <li>If necessary, unsummon the Pet of the killed L2PcInstance </li>
     * <li>Manage Reputation gain for attacker and Karam loss for the killed L2PcInstance </li>
     * <li>If the killed L2PcInstance has negative Reputation, manage Drop Item</li>
     * <li>Kill the L2PcInstance </li><BR><BR>
     *
     * @see l2server.gameserver.model.actor.L2Playable#doDie(l2server.gameserver.model.actor.L2Character)
     */
    @Override
    public boolean doDie(L2Character killer)
    {
        // Kill the L2PcInstance
        if (!super.doDie(killer))
        {
            return false;
        }

        if (isMounted())
        {
            stopFeed();
        }

        synchronized (this)
        {
            if (isFakeDeath())
            {
                stopFakeDeath(true);
            }
        }

        if (killer != null)
        {
            L2PcInstance pk = killer.getActingPlayer();
            if (getEvent() != null)
            {
                getEvent().onKill(killer, this);
            }

            //if (pk != null && Config.isServer(Config.TENKAI))
            //{
            //	OpenWorldOlympiadsManager.getInstance().onKill(pk, this);
            //}

            if (getIsInsideGMEvent() && pk.getIsInsideGMEvent())
            {
                GMEventManager.getInstance().onKill(killer, this);
            }

            //if (pk != null && getEvent() == null && !isInOlympiadMode())
            //	GmListTable.broadcastMessageToGMs(getName() + " was killed by " + pk.getName());

            //announce pvp/pk
            if (Config.ANNOUNCE_PK_PVP && pk != null && !pk.isGM())
            {
                String msg = "";
                if (getPvpFlag() == 0)
                {
                    msg = Config.ANNOUNCE_PK_MSG.replace("$killer", pk.getName()).replace("$target", getName());
                    if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
                        sm.addString(msg);
                        Announcements.getInstance().announceToAll(sm);
                    }
                    else
                    {
                        Announcements.getInstance().announceToAll(msg);
                    }
                }
                else if (getPvpFlag() != 0)
                {
                    msg = Config.ANNOUNCE_PVP_MSG.replace("$killer", pk.getName()).replace("$target", getName());
                    if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
                        sm.addString(msg);
                        Announcements.getInstance().announceToAll(sm);
                    }
                    else
                    {
                        Announcements.getInstance().announceToAll(msg);
                    }
                }
            }

            // LasTravel
            if (Config.ENABLE_CUSTOM_KILL_INFO && pk != null && getEvent() == null && !isInOlympiadMode() &&
                    !getIsInsideGMEvent())
            {
                RankingKillInfo.getInstance().updateSpecificKillInfo(pk, this);
            }

            if (pk != null && pk.getClan() != null && getClan() != null && getClan() != pk.getClan() &&
                    !isAcademyMember() && !pk.isAcademyMember() &&
                    (_clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(_clan.getClanId()) ||
                            isInSiege() && pk.isInSiege()) && !pk.getIsInsideGMEvent() && !getIsInsideGMEvent() &&
                    !pk.isPlayingEvent() && !isPlayingEvent() && AntiFeedManager.getInstance().check(killer, this))
            {
                // 	when your reputation score is 0 or below, the other clan cannot acquire any reputation points
                if (getClan().getReputationScore() > 0)
                {
                    pk.getClan().addReputationScore(Config.REPUTATION_SCORE_PER_KILL, false);
                    SystemMessage sm = SystemMessage.getSystemMessage(
                            SystemMessageId.BECAUSE_A_CLAN_MEMBER_OF_S1_WAS_KILLED_BY_C2_CLAN_REPUTATION_INCREASED_BY_1);
                    sm.addString(getClan().getName());
                    sm.addCharName(pk);
                    pk.getClan().broadcastToOnlineMembers(sm);
                }
                // 	when the opposing sides reputation score is 0 or below, your clans reputation score does not decrease
                if (pk.getClan().getReputationScore() > 0)
                {
                    _clan.takeReputationScore(Config.REPUTATION_SCORE_PER_KILL, false);
                    SystemMessage sm = SystemMessage.getSystemMessage(
                            SystemMessageId.BECAUSE_C1_WAS_KILLED_BY_A_CLAN_MEMBER_OF_S2_CLAN_REPUTATION_DECREASED_BY_1);
                    sm.addCharName(this);
                    sm.addString(pk.getClan().getName());
                    getClan().broadcastToOnlineMembers(sm);
                }

                for (ClanWar w : getClan().getWars())
                {
                    if (w.getClan1() == pk.getClan() && w.getClan2() == getClan())
                    {
                        w.increaseClan1Score();
                        w.decreaseClan2Score();
                    }
                    else if (w.getClan1() == getClan() && w.getClan2() == pk.getClan())
                    {
                        w.increaseClan2Score();
                        w.decreaseClan1Score();
                    }
                }
            }

            broadcastStatusUpdate();
            // Clear resurrect xp calculation
            setExpBeforeDeath(0);

            // Issues drop of Cursed Weapon.
            if (isCursedWeaponEquipped())
            {
                CursedWeaponsManager.getInstance().drop(_cursedWeaponEquippedId, killer);
            }
            else if (isCombatFlagEquipped())
            {
                Fort fort = FortManager.getInstance().getFort(this);
                if (fort != null)
                {
                    FortSiegeManager.getInstance().dropCombatFlag(this, fort.getFortId());
                }
                else
                {
                    int slot = getInventory().getSlotFromItem(getInventory().getItemByItemId(9819));
                    getInventory().unEquipItemInBodySlot(slot);
                    destroyItem("CombatFlag", getInventory().getItemByItemId(9819), null, true);
                }
            }
            else
            {
                if (pk == null || !pk.isCursedWeaponEquipped())
                {
                    onDieDropItem(killer); // Check if any item should be dropped

                    // Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty
                    // NOTE: deathPenalty +- Exp will update reputation
                    // Penalty is lower if the player is at war with the pk (war has to be declared)
                    if (getSkillLevelHash(L2Skill.SKILL_LUCKY) < 0 || getStat().getLevel() > 9)
                    {
                        boolean siege_npc = false;
                        if (killer instanceof L2DefenderInstance || killer instanceof L2FortCommanderInstance)
                        {
                            siege_npc = true;
                        }

                        boolean pvp = pk != null;
                        boolean atWar = pvp && getClan() != null && getClan().isAtWarWith(pk.getClanId());
                        boolean isWarDeclarator = atWar && getObjectId() == getClan().getWarDeclarator(pk.getClan());
                        deathPenalty(atWar, pvp, siege_npc, isWarDeclarator);
                    }
                }
            }
        }

        setPvpFlag(0); // Clear the pvp flag

        // Unsummon Cubics
        if (!_cubics.isEmpty())
        {
            for (L2CubicInstance cubic : _cubics.values())
            {
                cubic.stopAction();
                cubic.cancelDisappear();
            }

            _cubics.clear();
        }

        if (_fusionSkill != null || _continuousDebuffTargets != null)
        {
            abortCast();
        }

        for (L2Character character : getKnownList().getKnownCharacters())
        {
            if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this ||
                    character.getTarget() == this && character.getLastSkillCast() != null &&
                            (character.getLastSkillCast().getSkillType() == L2SkillType.CONTINUOUS_DEBUFF ||
                                    character.getLastSkillCast().getSkillType() == L2SkillType.CONTINUOUS_DRAIN))
            {
                character.abortCast();
            }
        }

        if (getAgathionId() != 0)
        {
            setAgathionId(0);
        }

        calculateBreathOfShilenDebuff(killer);

        stopRentPet();
        stopWaterTask();

        AntiFeedManager.getInstance().setLastDeathTime(getObjectId());

        if (!isPlayingEvent())
        {
            if (isPhoenixBlessed())
            {
                reviveRequest(this, null, false);
            }
            else if (isAffected(L2EffectType.CHARMOFCOURAGE.getMask()) && isInSiege())
            {
                reviveRequest(this, null, false);
            }
        }

        for (L2Summon summon : getSummons())
        {
            summon.setTarget(null);
            summon.abortAttack();
            summon.abortCast();
            summon.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }

        return true;
    }

    private void onDieDropItem(L2Character killer)
    {
        if (killer == null)
        {
            return;
        }

        L2PcInstance pk = killer.getActingPlayer();
        if (getReputation() >= 0 && pk != null && pk.getClan() != null && getClan() != null &&
                pk.getClan().isAtWarWith(getClanId()))
        {
            return;
        }

        if ((!isInsideZone(ZONE_PVP) || pk == null) && (!isGM() || Config.REPUTATION_DROP_GM))
        {
            boolean isKillerNpc = killer instanceof L2Npc;
            int pkLimit = Config.REPUTATION_PK_LIMIT;

            int dropEquip = 0;
            int dropEquipWeapon = 0;
            int dropItem = 0;
            int dropLimit = 0;
            int dropPercent = 0;

            if (getReputation() < 0 && getPkKills() >= pkLimit)
            {
                //isKarmaDrop = true;
                dropPercent = Config.REPUTATION_KARMA_RATE_DROP;
                dropEquip = Config.REPUTATION_KARMA_RATE_DROP_EQUIP;
                dropEquipWeapon = Config.REPUTATION_KARMA_RATE_DROP_EQUIP_WEAPON;
                dropItem = Config.REPUTATION_KARMA_RATE_DROP_ITEM;
                dropLimit = Config.REPUTATION_KARMA_DROP_LIMIT;
            }
            else if (isKillerNpc && getLevel() > 4)
            {
                dropPercent = Config.PLAYER_RATE_DROP;
                dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
                dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
                dropItem = Config.PLAYER_RATE_DROP_ITEM;
                dropLimit = Config.PLAYER_DROP_LIMIT;
            }

            if (dropPercent > 0 && Rnd.get(100) < dropPercent)
            {
                int dropCount = 0;
                int itemDropPercent = 0;
                for (L2ItemInstance itemDrop : getInventory().getItems())
                {
                    // Don't drop
                    if (itemDrop.isShadowItem() || // Dont drop Shadow Items
                            itemDrop.isTimeLimitedItem() || // Dont drop Time Limited Items
                            !itemDrop.getItem().isDropable() || itemDrop.getItemId() == 57 || // Adena
                            itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST || // Quest Items
                            getPet() != null && getPet().getControlObjectId() == itemDrop.getItemId() ||
                            // Control Item of active pet
                            Arrays.binarySearch(Config.REPUTATION_NONDROPPABLE_ITEMS, itemDrop.getItemId()) >= 0 ||
                            // Item listed in the non droppable item listsd
                            Arrays.binarySearch(Config.REPUTATION_NONDROPPABLE_PET_ITEMS, itemDrop.getItemId()) >= 0 ||
                            // Item listed in the non droppable pet item list
                            itemDrop.isAugmented() && getReputation() < 0)
                    {
                        continue;
                    }

                    if (itemDrop.isEquipped())
                    {
                        // Set proper chance according to Item type of equipped Item
                        itemDropPercent =
                                itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
                    }
                    else
                    {
                        itemDropPercent = dropItem; // Item in inventory
                    }

                    // NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
                    if (Rnd.get(100) < itemDropPercent)
                    {
                        if (itemDrop.isEquipped())
                        {
                            getInventory().unEquipItemInSlot(itemDrop.getLocationSlot());
                        }

                        itemDrop.removeAugmentation();
                        itemDrop.updateDatabase();
                        dropItem("DieDrop", itemDrop, killer, true);

                        if (++dropCount >= dropLimit)
                        {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void onKillUpdatePvPReputation(L2Character target)
    {
        if (target == null || !(target instanceof L2Playable))
        {
            return;
        }

        L2PcInstance targetPlayer = target.getActingPlayer();
        if (targetPlayer == null || targetPlayer == this)
        {
            return;
        }

        //if (!CustomAntiFeedManager.getInstance().isValidPoint(this, targetPlayer, false))
        //return;

        boolean wasSummon = target instanceof L2SummonInstance;
        if (isPlayingEvent())
        {
            return;
        }

        if (isCursedWeaponEquipped())
        {
            CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquippedId);
            // Custom message for time left
            // CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquipedId);
            // SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME);
            // int timeLeftInHours = (int)(((cw.getTimeLeft()/60000)/60));
            // msg.addItemName(_cursedWeaponEquipedId);
            // msg.addNumber(timeLeftInHours);
            // sendPacket(msg);
            return;
        }

        // If in duel and you kill (only can kill l2summon), do nothing
        if (isInDuel() && targetPlayer.isInDuel())
        {
            return;
        }

        // If in Arena, do nothing
        if (Curfew.getInstance().getOnlyPeaceTown() == -1 &&
                (isInsideZone(ZONE_PVP) || targetPlayer.isInsideZone(ZONE_PVP)))
        {
            return;
        }

        // Check if it's pvp
        if (!wasSummon && (checkIfPvP(target) && targetPlayer.getPvpFlag() != 0 ||
                isInsideZone(ZONE_PVP) && targetPlayer.isInsideZone(ZONE_PVP) ||
                targetPlayer.getClan() != null && targetPlayer.getClan().getHasCastle() != 0 &&
                        CastleManager.getInstance().getCastleByOwner(targetPlayer.getClan()).getTendency() ==
                                Castle.TENDENCY_DARKNESS)) //Castle condition should be moved to checkIfPvP ?
        {
            increasePvpKills(target);
            if (Config.isServer(Config.TENKAI) && !targetPlayer.isGM())
            {
                float expReward = targetPlayer.getDeathPenalty(
                        targetPlayer.getClan() != null && targetPlayer.getClan().isAtWarWith(getClanId()));
                if (getLevel() < targetPlayer.getLevel())
                {
                    float winnerLevelExp = Experience.getLevelExp(getLevel());
                    float loserLevelExp = Experience.getLevelExp(targetPlayer.getLevel());
                    expReward *= winnerLevelExp / loserLevelExp;
                }
                if (expReward > 0)
                {
                    addExpAndSp(Math.round(expReward / 2), 0);
                    long adena = Math.round(targetPlayer.getAdena() * 0.05);
                    if (isInParty())
                    {
                        int memberCount = getParty().getMemberCount();
                        for (L2PcInstance partyMember : getParty().getPartyMembers())
                        {
                            partyMember.addAdena("PvP", adena / memberCount, targetPlayer, true);
                        }
                    }
                    else
                    {
                        addAdena("PvP", adena, targetPlayer, true);
                    }
                    targetPlayer.reduceAdena("PvP", adena, this, true);
                }
            }

            //PVP
            if (!isInsideZone(ZONE_PVP) && !targetPlayer.isInsideZone(ZONE_PVP))
            {
                // Clan stuff check
                if (targetPlayer.getClan() != null)
                {
                    boolean atWar = getClan() != null && getClan().isAtWarWith(targetPlayer.getClan()) &&
                            targetPlayer.getClan().isAtWarWith(getClan()) &&
                            targetPlayer.getPledgeType() != L2Clan.SUBUNIT_ACADEMY &&
                            getPledgeType() != L2Clan.SUBUNIT_ACADEMY;

					/*if (!war)
					{
						int castleId = targetPlayer.getClan().getHasCastle();
						Castle castle = CastleManager.getInstance().getCastleById(castleId);
						war = castle != null && castle.getTendency() == Castle.TENDENCY_DARKNESS;
					}*/

                    if (atWar)
                    {
                        // 'Both way war' -> 'PvP Kill'
                        //increasePvpKills(target);
                        return;
                    }

                    if (getClan() != null && !getClan().isAtWarWith(targetPlayer.getClanId()) &&
                            !targetPlayer.getClan().isAtWarWith(getClanId()) &&
                            targetPlayer.getPledgeType() != L2Clan.SUBUNIT_ACADEMY &&
                            getPledgeType() != L2Clan.SUBUNIT_ACADEMY)
                    {
                        for (ClanWar war : targetPlayer.getClan().getWars())
                        {
                            if (war.getClan1() == targetPlayer.getClan() && war.getClan2() == getClan() &&
                                    war.getState() == WarState.DECLARED)
                            {
                                war.increaseClan1DeathsForClanWar();
                                if (war.getClan1DeathsForClanWar() >= 5)
                                {
                                    war.setWarDeclarator(getObjectId());
                                    war.start();
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        // Target player doesn't have pvp flag set
        {
            if (targetPlayer instanceof L2ApInstance)
            {
                return;
            }

            // Clan stuff check
            if (targetPlayer.getClan() != null)
            {
                //@SuppressWarnings("unused")
                boolean war = getClan() != null && getClan().isAtWarWith(targetPlayer.getClan()) &&
                        targetPlayer.getClan().isAtWarWith(getClan()) &&
                        targetPlayer.getPledgeType() != L2Clan.SUBUNIT_ACADEMY &&
                        getPledgeType() != L2Clan.SUBUNIT_ACADEMY;

                if (war)
                {
                    return;
                }
            }

            // 'No war' or 'One way war' -> 'Normal PK'
            if (targetPlayer.getReputation() < 0) // Target player has karma
            {
                if (Config.REPUTATION_AWARD_PK_KILL)
                {
                    increasePvpKills(target);
                    if (Config.isServer(Config.TENKAI))
                    {
                        float expReward = targetPlayer.getDeathPenalty(
                                targetPlayer.getClan() != null && targetPlayer.getClan().isAtWarWith(getClanId()));
                        if (getLevel() < targetPlayer.getLevel())
                        {
                            float winnerLevelExp = Experience.getLevelExp(getLevel());
                            float loserLevelExp = Experience.getLevelExp(targetPlayer.getLevel());
                            expReward *= winnerLevelExp / loserLevelExp;
                        }
                        if (expReward > 0)
                        {
                            addExpAndSp(Math.round(expReward * 0.65), 0);
                            addAdena("PvPK", (int) Math.round(targetPlayer.getAdena() * 0.05), targetPlayer, true);
                            targetPlayer
                                    .reduceAdena("PvPK", (int) Math.round(targetPlayer.getAdena() * 0.05), this, true);
                        }
                    }
                }

                updateReputationForKillChaoticPlayer(targetPlayer);
            }
            else if (targetPlayer.getPvpFlag() == 0) // PK
            {
                if (!wasSummon)
                {
                    increasePkKillsAndDecreaseReputation(target);
                    //Unequip adventurer items
                    checkItemRestriction();
                }
            }
        }
    }

    /**
     * Increase the pvp kills count and send the info to the player
     */
    public void increasePvpKills(L2Character target)
    {
        if (target instanceof L2PcInstance
		/*&& AntiFeedManager.getInstance().check(this, target)*/)
        {
            L2PcInstance targetPlayer = (L2PcInstance) target;
            if (targetPlayer.getClient() == null || targetPlayer.getClient().getConnection() == null ||
                    targetPlayer.getClient().getConnection().getInetAddress() == null || getClient() == null ||
                    getClient().getConnection() == null || getClient().getConnection().getInetAddress() == null ||
                    getClient().getConnection().getInetAddress() ==
                            targetPlayer.getClient().getConnection().getInetAddress())
            {
                return;
            }

            // Add karma to attacker and increase its PK counter
            setPvpKills(getPvpKills() + 1);

            // Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
            sendPacket(new UserInfo(this));

            if (Config.isServer(Config.TENKAI))
            {
                if (getPvpKills() % 100 == 0)
                {
                    Announcements.getInstance()
                            .announceToAll(getName() + " has just accumulated " + getPvpKills() + " victorious PvPs!");
                }
            }
        }
    }

    /**
     * Increase pk count, karma and send the info to the player
     */
    public void increasePkKillsAndDecreaseReputation(L2Character target)
    {
        if (isPlayingEvent())
        {
            return;
        }

        // Never let players get karma in events
        if (target instanceof L2PcInstance && ((L2PcInstance) target).isPlayingEvent())
        {
            return;
        }

        int minReputationToReduce = Config.REPUTATION_MIN_KARMA;
        int reputationToReduce = 0;
        int maxReputationToReduce = Config.REPUTATION_MAX_KARMA;

        int killsCount = getPkKills();

        if (_reputation <= 0 || killsCount >= 31) // Is not a lawful character or pk kills equals or higher than 31
        {
            reputationToReduce =
                    (int) (Math.random() * (maxReputationToReduce - minReputationToReduce)) + minReputationToReduce;
        }
        if (_reputation > 0)
        {
            reputationToReduce += _reputation;
        }

        // Add karma to attacker and increase its PK counter
        setReputation(_reputation - reputationToReduce);
        if (target instanceof L2PcInstance)
        {
            setPkKills(getPkKills() + 1);
        }

        // Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
        sendPacket(new UserInfo(this));
    }

    public int calculateReputationGain(long exp)
    {
        // KARMA LOSS
        // When a PKer gets killed by another player or a L2MonsterInstance, it loses a certain amount of Karma based on their level.
        // this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
        // You lose karma as long as you were not in a pvp zone and you did not kill urself.
        // NOTE: exp for death (if delevel is allowed) is based on the players level

        if (getReputation() < 0)
        {
            long expGained = Math.abs(exp);

            expGained /= Config.REPUTATION_XP_DIVIDER * getLevel() * getLevel();

            // FIXME Micht : Maybe this code should be fixed and karma set to a long value
            int reputationGain = 0;
            if (expGained > Integer.MAX_VALUE)
            {
                reputationGain = Integer.MAX_VALUE;
            }
            else
            {
                reputationGain = (int) expGained;
            }

            if (reputationGain < Config.REPUTATION_LOST_BASE)
            {
                reputationGain = Config.REPUTATION_LOST_BASE;
            }
            if (reputationGain > -getReputation())
            {
                reputationGain = -getReputation();
            }

            return reputationGain;
        }

        return 0;
    }

    public void updatePvPStatus()
    {
        if (isInsideZone(ZONE_PVP) || isPlayingEvent())
        {
            return;
        }

        setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);

        if (getPvpFlag() == 0)
        {
            startPvPFlag();
        }
    }

    public void updatePvPStatus(L2Character target)
    {
        if (isPlayingEvent())
        {
            return;
        }

        L2PcInstance playerTarget = target.getActingPlayer();

        if (playerTarget == null)
        {
            return;
        }

        if (isInDuel() && playerTarget.getDuelId() == getDuelId())
        {
            return;
        }

        if ((!isInsideZone(ZONE_PVP) || !playerTarget.isInsideZone(ZONE_PVP)) && playerTarget.getReputation() >= 0)
        {
            if (checkIfPvP(playerTarget))
            {
                setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
            }
            else
            {
                setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
            }
            if (getPvpFlag() == 0)
            {
                startPvPFlag();
            }
        }

        PlayerAssistsManager.getInstance().updateAttackTimer(this, playerTarget);
    }

    /**
     * Restore the specified % of experience this L2PcInstance has
     * lost and sends a Server->Client StatusUpdate packet.<BR><BR>
     */
    public void restoreExp(double restorePercent)
    {
        if (getExpBeforeDeath() > 0)
        {
            // Restore the specified % of lost experience.
            getStat().addExp(Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100));
            setExpBeforeDeath(0);
        }
    }

    /**
     * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Calculate the Experience loss </li>
     * <li>Set the value of _expBeforeDeath </li>
     * <li>Set the new Experience value of the L2PcInstance and Decrease its level if necessary </li>
     * <li>Send a Server->Client StatusUpdate packet with its new Experience </li><BR><BR>
     */
    public void deathPenalty(boolean atwar, boolean killed_by_pc, boolean killed_by_siege_npc, boolean isWarDeclarator)
    {
        // TODO Need Correct Penalty
        // Get the level of the L2PcInstance
        final int lvl = getLevel();

        int clan_luck = getSkillLevelHash(L2Skill.SKILL_CLAN_LUCK);

        double clan_luck_modificator = 1.0;

        if (!killed_by_pc)
        {
            switch (clan_luck)
            {
                case 3:
                    clan_luck_modificator = 0.8;
                    break;
                case 2:
                    clan_luck_modificator = 0.8;
                    break;
                case 1:
                    clan_luck_modificator = 0.88;
                    break;
                default:
                    clan_luck_modificator = 1.0;
                    break;
            }
        }
        else
        {
            switch (clan_luck)
            {
                case 3:
                    clan_luck_modificator = 0.5;
                    break;
                case 2:
                    clan_luck_modificator = 0.5;
                    break;
                case 1:
                    clan_luck_modificator = 0.5;
                    break;
                default:
                    clan_luck_modificator = 1.0;
                    break;
            }
        }

        //The death steal you some Exp
        double percentLost = Config.PLAYER_XP_PERCENT_LOST[getLevel()] * clan_luck_modificator;

        if (getReputation() < 0)
        {
            percentLost *= Config.RATE_REPUTATION_EXP_LOST;
        }

        if (atwar && !isWarDeclarator)
        {
            percentLost /= 4.0;
        }

        // Calculate the Experience loss
        long lostExp = 0;
        if (lvl < Config.MAX_LEVEL)
        {
            lostExp =
                    Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
        }
        else
        {
            lostExp = Math.round(
                    (getStat().getExpForLevel(Config.MAX_LEVEL + 1) - getStat().getExpForLevel(Config.MAX_LEVEL)) *
                            percentLost / 100);
        }

        // Get the Experience before applying penalty
        setExpBeforeDeath(getExp());

        // No xp loss inside pvp zone unless
        // - it's a siege zone and you're NOT participating
        // - you're killed by a non-pc whose not belong to the siege
        if (isInsideZone(ZONE_PVP))
        {
            // No xp loss for siege participants inside siege zone
            if (isInsideZone(ZONE_SIEGE))
            {
                if (isInSiege() && (killed_by_pc || killed_by_siege_npc))
                {
                    lostExp = 0;
                }
            }
            else if (killed_by_pc)
            {
                lostExp = 0;
            }
        }

        if (!Config.ALT_GAME_DELEVEL && getExp() - lostExp < Experience.getAbsoluteExp(lvl))
        {
            lostExp = getExp() - Experience.getAbsoluteExp(lvl);
        }

        if (Config.DEBUG)
        {
            Log.fine(getName() + " died and lost " + lostExp + " experience.");
        }

        // Set the new Experience value of the L2PcInstance
        getStat().addExp(-lostExp);
    }

    public boolean isPartyWaiting()
    {
        return PartyMatchWaitingList.getInstance().getPlayers().contains(this);
    }

    public void setPartyRoom(int id)
    {
        _partyroom = id;
    }

    public int getPartyRoom()
    {
        return _partyroom;
    }

    public boolean isInPartyMatchRoom()
    {
        return _partyroom > 0;
    }

    /**
     * Manage the increase level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...).<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Send a Server->Client System Message to the L2PcInstance : YOU_INCREASED_YOUR_LEVEL </li>
     * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance with new LEVEL, MAX_HP and MAX_MP </li>
     * <li>Set the current HP and MP of the L2PcInstance, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)</li>
     * <li>Recalculate the party level</li>
     * <li>Recalculate the number of Recommandation that the L2PcInstance can give</li>
     * <li>Give Expertise skill of this level and remove beginner Lucky skill</li><BR><BR>
     */
    public void increaseLevel()
    {
        // Set the current HP and MP of the L2Character, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)
        setCurrentHpMp(getMaxHp(), getMaxMp());
        setCurrentCp(getMaxCp());
    }

    /**
     * Stop the HP/MP/CP Regeneration task.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Set the RegenActive flag to False </li>
     * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
     */
    public void stopAllTimers()
    {
        stopHpMpRegeneration();
        stopWarnUserTakeBreak();
        stopWaterTask();
        stopFeed();
        clearPetData();
        storePetFood(_mountNpcId);
        stopRentPet();
        stopPvpRegTask();
        stopPunishTask(true);
        stopSoulTask();
        stopChargeTask();
        stopFameTask();
        stopRecoBonusTask();
        stopRecoGiveTask();
    }

    public List<L2SummonInstance> getSummons()
    {
        return _summons;
    }

    public L2SummonInstance getSummon(int summon)
    {
        if (_summons.size() <= summon)
        {
            return null;
        }
        return _summons.get(summon);
    }

    /**
     * Return the L2Summon of the L2PcInstance or null.<BR><BR>
     */
    public L2PetInstance getPet()
    {
        return _pet;
    }

    public L2SummonInstance getActiveSummon()
    {
        return _activeSummon;
    }

    /**
     * Return the L2DecoyInstance of the L2PcInstance or null.<BR><BR>
     */
    public L2DecoyInstance getDecoy()
    {
        return _decoy;
    }

    /**
     * Return the L2Trap of the L2PcInstance or null.<BR><BR>
     */
    public L2Trap getTrap()
    {
        return _trap;
    }

    public void addSummon(L2SummonInstance summon)
    {
        _summons.add(summon);
        setActiveSummon(summon);
    }

    public void removeSummon(L2SummonInstance summon)
    {
        _summons.remove(summon);
        // update attack element value display
        if (getCurrentClass().isSummoner() && getAttackElement() != Elementals.NONE)
        {
            sendPacket(new UserInfo(this));
        }
        if (getActiveSummon() == summon)
        {
            setActiveSummon(null);
        }
    }

    /**
     * Set the L2Pet of the L2PcInstance.<BR><BR>
     */
    public void setPet(L2PetInstance pet)
    {
        _pet = pet;
    }

    public void setActiveSummon(L2SummonInstance summon)
    {
        _activeSummon = summon;
    }

    /**
     * Set the L2DecoyInstance of the L2PcInstance.<BR><BR>
     */
    public void setDecoy(L2DecoyInstance decoy)
    {
        _decoy = decoy;
    }

    /**
     * Set the L2Trap of this L2PcInstance<BR><BR>
     *
     * @param trap
     */
    public void setTrap(L2Trap trap)
    {
        _trap = trap;
    }

    /**
     * Return the L2Summon of the L2PcInstance or null.<BR><BR>
     */
    public List<L2TamedBeastInstance> getTrainedBeasts()
    {
        return _tamedBeast;
    }

    /**
     * Set the L2Summon of the L2PcInstance.<BR><BR>
     */
    public void addTrainedBeast(L2TamedBeastInstance tamedBeast)
    {
        if (_tamedBeast == null)
        {
            _tamedBeast = new ArrayList<>();
        }
        _tamedBeast.add(tamedBeast);
    }

    /**
     * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
     */
    public L2Request getRequest()
    {
        return _request;
    }

    /**
     * Set the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
     */
    public void setActiveRequester(L2PcInstance requester)
    {
        _activeRequester = requester;
    }

    /**
     * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
     */
    public L2PcInstance getActiveRequester()
    {
        L2PcInstance requester = _activeRequester;
        if (requester != null)
        {
            if (requester.isRequestExpired() && _activeTradeList == null)
            {
                _activeRequester = null;
            }
        }
        return _activeRequester;
    }

    /**
     * Return True if a transaction is in progress.<BR><BR>
     */
    public boolean isProcessingRequest()
    {
        return getActiveRequester() != null || _requestExpireTime > TimeController.getGameTicks();
    }

    /**
     * Return True if a transaction is in progress.<BR><BR>
     */
    public boolean isProcessingTransaction()
    {
        return getActiveRequester() != null || _activeTradeList != null ||
                _requestExpireTime > TimeController.getGameTicks();
    }

    /**
     * Select the Warehouse to be used in next activity.<BR><BR>
     */
    public void onTransactionRequest(L2PcInstance partner)
    {
        _requestExpireTime = TimeController.getGameTicks() + REQUEST_TIMEOUT * TimeController.TICKS_PER_SECOND;
        partner.setActiveRequester(this);
    }

    /**
     * Return true if last request is expired.
     *
     * @return
     */
    public boolean isRequestExpired()
    {
        return !(_requestExpireTime > TimeController.getGameTicks());
    }

    /**
     * Select the Warehouse to be used in next activity.<BR><BR>
     */
    public void onTransactionResponse()
    {
        _requestExpireTime = 0;
    }

    /**
     * Select the Warehouse to be used in next activity.<BR><BR>
     */
    public void setActiveWarehouse(ItemContainer warehouse)
    {
        _activeWarehouse = warehouse;
    }

    /**
     * Return active Warehouse.<BR><BR>
     */
    public ItemContainer getActiveWarehouse()
    {
        return _activeWarehouse;
    }

    /**
     * Select the TradeList to be used in next activity.<BR><BR>
     */
    public void setActiveTradeList(TradeList tradeList)
    {
        _activeTradeList = tradeList;
    }

    /**
     * Return active TradeList.<BR><BR>
     */
    public TradeList getActiveTradeList()
    {
        return _activeTradeList;
    }

    public void onTradeStart(L2PcInstance partner)
    {
        _activeTradeList = new TradeList(this);
        _activeTradeList.setPartner(partner);

        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_C1);
        msg.addPcName(partner);
        sendPacket(msg);
        sendPacket(new TradeStart(this));
    }

    public void onTradeConfirm(L2PcInstance partner)
    {
        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CONFIRMED_TRADE);
        msg.addPcName(partner);
        sendPacket(msg);
        sendPacket(new TradeOtherDone());
    }

    public void onTradeCancel(L2PcInstance partner)
    {
        if (_activeTradeList == null)
        {
            return;
        }

        _activeTradeList.lock();
        _activeTradeList = null;

        sendPacket(new TradeDone(0));
        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CANCELED_TRADE);
        msg.addPcName(partner);
        sendPacket(msg);
    }

    public void onTradeFinish(boolean successfull)
    {
        _activeTradeList = null;
        sendPacket(new TradeDone(1));
        if (successfull)
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TRADE_SUCCESSFUL));
        }
    }

    public void startTrade(L2PcInstance partner)
    {
        onTradeStart(partner);
        partner.onTradeStart(this);
    }

    public void cancelActiveTrade()
    {
        if (_activeTradeList == null)
        {
            return;
        }

        L2PcInstance partner = _activeTradeList.getPartner();
        if (partner != null)
        {
            partner.onTradeCancel(this);
        }
        onTradeCancel(this);
    }

    /**
     * Return the _createList object of the L2PcInstance.<BR><BR>
     */
    public L2ManufactureList getCreateList()
    {
        return _createList;
    }

    /**
     * Set the _createList object of the L2PcInstance.<BR><BR>
     */
    public void setCreateList(L2ManufactureList x)
    {
        _createList = x;
    }

    /**
     * Return the _buyList object of the L2PcInstance.<BR><BR>
     */
    public TradeList getSellList()
    {
        if (_sellList == null)
        {
            _sellList = new TradeList(this);
        }
        return _sellList;
    }

    /**
     * Return the _buyList object of the L2PcInstance.<BR><BR>
     */
    public TradeList getBuyList()
    {
        if (_buyList == null)
        {
            _buyList = new TradeList(this);
        }
        return _buyList;
    }

    /**
     * Set the Private Store type of the L2PcInstance.<BR><BR>
     * <p>
     * <B><U> Values </U> :</B><BR><BR>
     * <li>0 : STORE_PRIVATE_NONE</li>
     * <li>1 : STORE_PRIVATE_SELL</li>
     * <li>2 : sellmanage</li><BR>
     * <li>3 : STORE_PRIVATE_BUY</li><BR>
     * <li>4 : buymanage</li><BR>
     * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
     */
    public void setPrivateStoreType(int type)
    {
        _privatestore = type;

        if (Config.OFFLINE_DISCONNECT_FINISHED && _privatestore == STORE_PRIVATE_NONE &&
                (getClient() == null || getClient().isDetached()))
        {
            deleteMe();
        }
    }

    /**
     * Return the Private Store type of the L2PcInstance.<BR><BR>
     * <p>
     * <B><U> Values </U> :</B><BR><BR>
     * <li>0 : STORE_PRIVATE_NONE</li>
     * <li>1 : STORE_PRIVATE_SELL</li>
     * <li>2 : sellmanage</li><BR>
     * <li>3 : STORE_PRIVATE_BUY</li><BR>
     * <li>4 : buymanage</li><BR>
     * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
     */
    public int getPrivateStoreType()
    {
        return _privatestore;
    }

    /**
     * Set the _clan object, _clanId, _clanLeader Flag and title of the L2PcInstance.<BR><BR>
     */
    public void setClan(L2Clan clan)
    {
        _clan = clan;
        setTitle("");

        if (clan == null)
        {
            _clanId = 0;
            _clanPrivileges = 0;
            _pledgeType = 0;
            _powerGrade = 0;
            _lvlJoinedAcademy = 0;
            _apprentice = 0;
            _sponsor = 0;
            _activeWarehouse = null;
            return;
        }

        if (!clan.isMember(getObjectId()))
        {
            // char has been kicked from clan
            setClan(null);
            return;
        }

        _clanId = clan.getClanId();
    }

    /**
     * Return the _clan object of the L2PcInstance.<BR><BR>
     */
    public L2Clan getClan()
    {
        return _clan;
    }

    /**
     * Return True if the L2PcInstance is the leader of its clan.<BR><BR>
     */
    public boolean isClanLeader()
    {
        if (getClan() == null)
        {
            return false;
        }
        else
        {
            return getObjectId() == getClan().getLeaderId();
        }
    }

    /**
     * Reduce the number of arrows/bolts owned by the L2PcInstance and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).<BR><BR>
     */
    @Override
    protected void reduceArrowCount(boolean bolts)
    {
        L2ItemInstance arrows = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

        if (arrows == null)
        {
            getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
            if (bolts)
            {
                _boltItem = null;
            }
            else
            {
                _arrowItem = null;
            }
            sendPacket(new ItemList(this, false));
            return;
        }

        if (arrows.getName().contains("Infinite"))
        {
            return;
        }

        // Adjust item quantity
        if (arrows.getCount() > 1)
        {
            synchronized (arrows)
            {
                arrows.changeCountWithoutTrace(-1, this, null);
                arrows.setLastChange(L2ItemInstance.MODIFIED);

                // could do also without saving, but let's save approx 1 of 10
                //if (TimeController.getGameTicks() % 10 == 0)
                //	arrows.updateDatabase();
                _inventory.refreshWeight();
            }
        }
        else
        {
            // Destroy entire item and save to database
            _inventory.destroyItem("Consume", arrows, this, null);

            getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
            if (bolts)
            {
                _boltItem = null;
            }
            else
            {
                _arrowItem = null;
            }

            if (Config.DEBUG)
            {
                Log.fine("removed arrows count");
            }
            sendPacket(new ItemList(this, false));
            return;
        }

        if (!Config.FORCE_INVENTORY_UPDATE)
        {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(arrows);
            sendPacket(iu);
        }
        else
        {
            sendPacket(new ItemList(this, false));
        }
    }

    /**
     * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR><BR>
     */
    @Override
    protected boolean checkAndEquipArrows()
    {
        // Check if nothing is equiped in left hand
        if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
        {
            // Get the L2ItemInstance of the arrows needed for this bow
            _arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());

            if (_arrowItem != null)
            {
                // Equip arrows needed in left hand
                getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);

                // Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
                ItemList il = new ItemList(this, false);
                sendPacket(il);
            }
        }
        else
        {
            // Get the L2ItemInstance of arrows equiped in left hand
            _arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        }

        return _arrowItem != null;
    }

    /**
     * Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR><BR>
     */
    @Override
    protected boolean checkAndEquipBolts()
    {
        // Check if nothing is equiped in left hand
        if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
        {
            // Get the L2ItemInstance of the arrows needed for this bow
            _boltItem = getInventory().findBoltForCrossBow(getActiveWeaponItem());

            if (_boltItem != null)
            {
                // Equip arrows needed in left hand
                getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _boltItem);

                // Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
                ItemList il = new ItemList(this, false);
                sendPacket(il);
            }
        }
        else
        {
            // Get the L2ItemInstance of arrows equiped in left hand
            _boltItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        }

        return _boltItem != null;
    }

    /**
     * Disarm the player's weapon.<BR><BR>
     */
    public boolean disarmWeapons()
    {
        // Don't allow disarming a cursed weapon
        if (isCursedWeaponEquipped())
        {
            return false;
        }

        // Don't allow disarming a Combat Flag or Territory Ward
        if (isCombatFlagEquipped())
        {
            return false;
        }

        // Unequip the weapon
        L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
        if (wpn != null)
        {
            L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
            InventoryUpdate iu = new InventoryUpdate();
            for (L2ItemInstance itm : unequiped)
            {
                iu.addModifiedItem(itm);
            }
            sendPacket(iu);

            abortAttack();
            broadcastUserInfo();

            // this can be 0 if the user pressed the right mousebutton twice very fast
            if (unequiped.length > 0)
            {
                SystemMessage sm = null;
                if (unequiped[0].getEnchantLevel() > 0)
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                    sm.addNumber(unequiped[0].getEnchantLevel());
                    sm.addItemName(unequiped[0]);
                }
                else
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                    sm.addItemName(unequiped[0]);
                }
                sendPacket(sm);
            }
        }
        return true;
    }

    /**
     * Disarm the player's shield.<BR><BR>
     */
    public boolean disarmShield()
    {
        L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        if (sld != null)
        {
            L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
            InventoryUpdate iu = new InventoryUpdate();
            for (L2ItemInstance itm : unequiped)
            {
                iu.addModifiedItem(itm);
            }
            sendPacket(iu);

            abortAttack();
            broadcastUserInfo();

            // this can be 0 if the user pressed the right mousebutton twice very fast
            if (unequiped.length > 0)
            {
                SystemMessage sm = null;
                if (unequiped[0].getEnchantLevel() > 0)
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                    sm.addNumber(unequiped[0].getEnchantLevel());
                    sm.addItemName(unequiped[0]);
                }
                else
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                    sm.addItemName(unequiped[0]);
                }
                sendPacket(sm);
            }
        }
        return true;
    }

    public boolean mount(L2Summon pet)
    {
        if (!disarmWeapons())
        {
            return false;
        }
        if (!disarmShield())
        {
            return false;
        }
        if (isTransformed())
        {
            return false;
        }

        stopAllToggles();
        Ride mount = new Ride(this, true, pet.getTemplate().NpcId);
        setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());
        setMountObjectID(pet.getControlObjectId());
        clearPetData();
        startFeed(pet.getNpcId());
        broadcastPacket(mount);

        // Notify self and others about speed change
        broadcastUserInfo();

        pet.unSummon(this);

        return true;
    }

    public boolean mount(int npcId, int controlItemObjId, boolean useFood)
    {
        if (!disarmWeapons())
        {
            return false;
        }
        if (!disarmShield())
        {
            return false;
        }
        if (isTransformed())
        {
            return false;
        }

        stopAllToggles();
        Ride mount = new Ride(this, true, npcId);
        if (setMount(npcId, getLevel(), mount.getMountType()))
        {
            clearPetData();
            setMountObjectID(controlItemObjId);
            broadcastPacket(mount);

            // Notify self and others about speed change
            broadcastUserInfo();
            if (useFood)
            {
                startFeed(npcId);
            }
            return true;
        }
        return false;
    }

    public boolean mountPlayer(L2Summon pet)
    {
        if (pet != null && pet.isMountable() && !isMounted() && !isBetrayed())
        {
            if (isDead())
            {
                //A strider cannot be ridden when dead
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD));
                return false;
            }
            else if (pet.isDead())
            {
                //A dead strider cannot be ridden.
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN));
                return false;
            }
            else if (pet.isInCombat() || pet.isRooted())
            {
                //A strider in battle cannot be ridden
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN));
                return false;
            }
            else if (isInCombat())
            {
                //A strider cannot be ridden while in battle
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
                return false;
            }
            else if (isSitting())
            {
                //A strider can be ridden only when standing
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING));
                return false;
            }
            else if (isFishing())
            {
                //You can't mount, dismount, break and drop items while fishing
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_2));
                return false;
            }
            else if (isTransformed() || isCursedWeaponEquipped())
            {
                // no message needed, player while transformed doesn't have mount action
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
            else if (getInventory().getItemByItemId(9819) != null)
            {
                sendPacket(ActionFailed.STATIC_PACKET);
                //FIXME: Wrong Message
                sendMessage("You cannot mount a steed while holding a flag.");
                return false;
            }
            else if (pet.isHungry())
            {
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT));
                return false;
            }
            else if (!Util.checkIfInRange(200, this, pet, true))
            {
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT));
                return false;
            }
            else if (!pet.isDead() && !isMounted())
            {
                mount(pet);
            }
        }
        else if (isRentedPet())
        {
            stopRentPet();
        }
        else if (isMounted())
        {
            if (getMountType() == 2 && isInsideZone(L2Character.ZONE_NOLANDING))
            {
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_DISMOUNT_HERE));
                return false;
            }
            else if (isHungry())
            {
                sendPacket(ActionFailed.STATIC_PACKET);
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT));
                return false;
            }
            else
            {
                dismount();
            }
        }
        return true;
    }

    public boolean dismount()
    {
        boolean wasFlying = isFlying();

        sendPacket(new SetupGauge(3, 0, 0));
        int petId = _mountNpcId;
        if (setMount(0, 0, 0))
        {
            stopFeed();
            clearPetData();
            if (wasFlying)
            {
                removeSkill(SkillTable.FrequentSkill.WYVERN_BREATH.getSkill());
            }
            Ride dismount = new Ride(this, false, 0);
            broadcastPacket(dismount);
            setMountObjectID(0);
            storePetFood(petId);
            // Notify self and others about speed change
            broadcastUserInfo();
            return true;
        }
        return false;
    }

    public PetInventory getSummonInv(int summon)
    {
        return _summons.get(summon).getInventory();
    }

    public PetInventory getPetInv()
    {
        return _petInv;
    }

    /**
     * Return True if the L2PcInstance use a dual weapon.<BR><BR>
     */
    @Override
    public boolean isUsingDualWeapon()
    {
        L2Weapon weaponItem = getActiveWeaponItem();
        if (weaponItem == null)
        {
            return false;
        }

        if (weaponItem.getItemType() == L2WeaponType.DUAL)
        {
            return true;
        }
        else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
        {
            return true;
        }
        else if (weaponItem.getItemType() == L2WeaponType.DUALDAGGER)
        {
            return true;
        }
        else if (weaponItem.getItemType() == L2WeaponType.DUALBLUNT)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setUptime(long time)
    {
        _uptime = time;
    }

    public long getUptime()
    {
        return System.currentTimeMillis() - _uptime;
    }

    /**
     * Return True if the L2PcInstance is invulnerable.<BR><BR>
     */
    @Override
    public boolean isInvul()
    {
        return super.isInvul() || isSpawnProtected() || _teleportProtectEndTime > TimeController.getGameTicks();
    }

    /**
     * Return True if the L2PcInstance has a Party in progress.<BR><BR>
     */
    @Override
    public boolean isInParty()
    {
        return _party != null;
    }

    /**
     * Set the _party object of the L2PcInstance (without joining it).<BR><BR>
     */
    public void setParty(L2Party party)
    {
        _party = party;
    }

    /**
     * Set the _party object of the L2PcInstance AND join it.<BR><BR>
     */
    public void joinParty(L2Party party)
    {
        if (party != null)
        {
            // First set the party otherwise this wouldn't be considered
            // as in a party into the L2Character.updateEffectIcons() call.
            _party = party;
            party.addPartyMember(this);
        }
        //onJoinParty();
    }

    /**
     * Manage the Leave Party task of the L2PcInstance.<BR><BR>
     */
    public void leaveParty()
    {
        if (isInParty())
        {
            _party.removePartyMember(this, messageType.Disconnected);
            _party = null;
        }
        //onLeaveParty();
    }

    /**
     * Return the _party object of the L2PcInstance.<BR><BR>
     */
    @Override
    public L2Party getParty()
    {
        return _party;
    }

    /**
     * Return True if the L2PcInstance is a GM.<BR><BR>
     */
    @Override
    public boolean isGM()
    {
        return getAccessLevel().isGm();
    }

    /**
     * Set the _accessLevel of the L2PcInstance.<BR><BR>
     */
    public void setAccessLevel(int level)
    {
        if (level == AccessLevels._masterAccessLevelNum)
        {
            if (!Config.isServer(Config.TENKAI))
            {
                Log.warning("Master access level set for character " + getName() + "! Just a warning to be careful ;)");
            }
            _accessLevel = AccessLevels._masterAccessLevel;
        }
        else if (level == AccessLevels._userAccessLevelNum)
        {
            _accessLevel = AccessLevels._userAccessLevel;
        }
        else
        {
            L2AccessLevel accessLevel = AccessLevels.getInstance().getAccessLevel(level);

            if (accessLevel == null)
            {
                if (level < 0)
                {
                    AccessLevels.getInstance().addBanAccessLevel(level);
                    _accessLevel = AccessLevels.getInstance().getAccessLevel(level);
                }
                else
                {
                    Log.warning("Tryed to set unregistered access level " + level + " to character " + getName() +
                            ". Setting access level without privileges!");
                    _accessLevel = AccessLevels._userAccessLevel;
                }
            }
            else
            {
                _accessLevel = accessLevel;
            }
        }

        getAppearance().setNameColor(_accessLevel.getNameColor());
        getAppearance().setTitleColor(_accessLevel.getTitleColor());
        broadcastUserInfo();

        CharNameTable.getInstance().addName(this);
    }

    public void setAccountAccesslevel(int level)
    {
        LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
    }

    /**
     * Return the _accessLevel of the L2PcInstance.<BR><BR>
     */
    public L2AccessLevel getAccessLevel()
    {
        if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
        {
            return AccessLevels._masterAccessLevel;
        }
        else if (_accessLevel == null) /* This is here because inventory etc. is loaded before access level on login, so it is not null */
        {
            setAccessLevel(AccessLevels._userAccessLevelNum);
        }

        return _accessLevel;
    }

    /**
     * Update Stats of the L2PcInstance client side by sending Server->Client packet UserInfo/StatusUpdate to this L2PcInstance and CharInfo/StatusUpdate to all L2PcInstance in its _KnownPlayers (broadcast).<BR><BR>
     */
    public void updateAndBroadcastStatus(int broadcastType)
    {
        if (!_hasLoaded)
        {
            return;
        }

        refreshOverloaded();
        refreshExpertisePenalty();
        // Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
        if (broadcastType == 1)
        {
            sendPacket(new UserInfo(this));
        }
        if (broadcastType == 2)
        {
            broadcastUserInfo();
        }
    }

    /**
     * Send a Server->Client StatusUpdate packet with Reputation and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
     */
    public void setReputationFlag(int flag)
    {
        sendPacket(new UserInfo(this));
        Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
        //synchronized (getKnownList().getKnownPlayers())
        {
            for (L2PcInstance player : plrs)
            {
                if (player == null)
                {
                    continue;
                }

                player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
                if (getPet() != null)
                {
                    player.sendPacket(new RelationChanged(getPet(), getRelation(player), isAutoAttackable(player)));
                }
                for (L2SummonInstance summon : getSummons())
                {
                    player.sendPacket(new RelationChanged(summon, getRelation(player), isAutoAttackable(player)));
                }
            }
        }
    }

    /**
     * Send a Server->Client StatusUpdate packet with Reputation to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
     */
    public void broadcastReputation()
    {
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.REPUTATION, getReputation());
        sendPacket(su);
        Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
        //synchronized (getKnownList().getKnownPlayers())
        {
            for (L2PcInstance player : plrs)
            {
                player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
                if (getPet() != null)
                {
                    player.sendPacket(new RelationChanged(getPet(), getRelation(player), isAutoAttackable(player)));
                }
                for (L2SummonInstance summon : getSummons())
                {
                    player.sendPacket(new RelationChanged(summon, getRelation(player), isAutoAttackable(player)));
                }
            }
        }
    }

    /**
     * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).<BR><BR>
     */
    public void setOnlineStatus(boolean isOnline, boolean updateInDb)
    {
        if (_isOnline != isOnline)
        {
            _isOnline = isOnline;
        }

        // Update the characters table of the database with online status and lastAccess (called when login and logout)
        if (updateInDb)
        {
            updateOnlineStatus();
        }
    }

    /**
     * Update the characters table of the database with online status and lastAccess of this L2PcInstance (called when login and logout).<BR><BR>
     */
    public void updateOnlineStatus()
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement =
                    con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?");
            statement.setInt(1, isOnlineInt());
            statement.setLong(2, System.currentTimeMillis());
            statement.setInt(3, getObjectId());
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Failed updating character online status.", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * Create a new player in the characters table of the database.<BR><BR>
     */
    private boolean createDb()
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(INSERT_CHARACTER);

            statement.setString(1, _accountName);
            statement.setInt(2, getObjectId());
            statement.setString(3, getName());
            statement.setInt(4, getLevel());
            statement.setInt(5, getMaxHp());
            statement.setDouble(6, getCurrentHp());
            statement.setInt(7, getMaxCp());
            statement.setDouble(8, getCurrentCp());
            statement.setInt(9, getMaxMp());
            statement.setDouble(10, getCurrentMp());
            statement.setInt(11, getAppearance().getFace());
            statement.setInt(12, getAppearance().getHairStyle());
            statement.setInt(13, getAppearance().getHairColor());
            statement.setInt(14, getAppearance().getSex() ? 1 : 0);
            statement.setLong(15, getExp());
            statement.setLong(16, getSp());
            statement.setInt(17, getReputation());
            statement.setInt(18, getFame());
            statement.setInt(19, getPvpKills());
            statement.setInt(20, getPkKills());
            statement.setInt(21, getClanId());
            statement.setInt(22, _templateId);
            statement.setInt(23, getCurrentClass().getId());
            statement.setLong(24, getDeleteTimer());
            statement.setInt(25, hasDwarvenCraft() ? 1 : 0);
            statement.setString(26, getTitle());
            statement.setInt(27, getAppearance().getTitleColor());
            statement.setInt(28, getAccessLevel().getLevel());
            statement.setInt(29, isOnlineInt());
            statement.setInt(30, getClanPrivileges());
            statement.setInt(31, getWantsPeace());
            statement.setInt(32, getBaseClass());
            statement.setInt(33, getNewbie());
            statement.setInt(34, isNoble() ? 1 : 0);
            statement.setLong(35, 0);
            statement.setLong(36, getCreateTime());

            statement.executeUpdate();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not insert char data: " + e.getMessage(), e);
            return false;
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
        return true;
    }

    /**
     * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Retrieve the L2PcInstance from the characters table of the database </li>
     * <li>Add the L2PcInstance object in _allObjects </li>
     * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
     * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
     *
     * @param objectId Identifier of the object to initialized
     * @return The L2PcInstance loaded from the database
     */
    private static L2PcInstance restore(int objectId)
    {
        L2PcInstance player = null;
        Connection con = null;

        try
        {
            // Retrieve the L2PcInstance from the characters table of the database
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
            statement.setInt(1, objectId);
            ResultSet rset = statement.executeQuery();

            double currentCp = 0;
            double currentHp = 0;
            double currentMp = 0;

            byte playerTemporaryLevel = 0;
            while (rset.next())
            {
                final int activeClassId = rset.getInt("classid");
                final boolean female = rset.getInt("sex") != 0;
                int raceId = rset.getInt("templateId") / 2;
                boolean isMage = PlayerClassTable.getInstance().getClassById(activeClassId).isMage();
                final L2PcTemplate template =
                        CharTemplateTable.getInstance().getTemplate(raceId * 2 + (isMage ? 1 : 0));
                PcAppearance app =
                        new PcAppearance(rset.getInt("face"), rset.getInt("hairColor"), rset.getInt("hairStyle"),
                                female);

                if (rset.getString("account_name").startsWith("!"))
                {
                    player = new L2ApInstance(objectId, template, rset.getString("account_name"), app);
                }
                else
                {
                    player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
                }
                player.setName(rset.getString("char_name"));
                player._lastAccess = rset.getLong("lastAccess");

                player.getStat().setExp(rset.getLong("exp"));
                player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
                player.getStat().setLevel(rset.getByte("level"));

                playerTemporaryLevel = rset.getByte("temporaryLevel");

                player.getStat().setSp(rset.getLong("sp"));

                player.setWantsPeace(rset.getInt("wantspeace"));

                player.setHeading(rset.getInt("heading"));

                player.setReputation(rset.getInt("reputation"));
                player.setFame(rset.getInt("fame"));
                player.setPvpKills(rset.getInt("pvpkills"));
                player.setPkKills(rset.getInt("pkkills"));
                player.setOnlineTime(rset.getLong("onlinetime"));
                player.setNewbie(rset.getInt("newbie"));

                player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
                if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
                {
                    player.setClanJoinExpiryTime(0);
                }
                player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
                if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
                {
                    player.setClanCreateExpiryTime(0);
                }

                int clanId = rset.getInt("clanid");
                player.setPowerGrade((int) rset.getLong("power_grade"));
                player.setPledgeType(rset.getInt("subpledge"));
                //player.setApprentice(rset.getInt("apprentice"));

                if (clanId > 0)
                {
                    player.setClan(ClanTable.getInstance().getClan(clanId));
                }

                if (player.getClan() != null)
                {
                    if (player.getClan().getLeaderId() != player.getObjectId())
                    {
                        if (player.getPowerGrade() == 0)
                        {
                            player.setPowerGrade(5);
                        }
                        player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
                    }
                    else
                    {
                        player.setClanPrivileges(L2Clan.CP_ALL);
                        player.setPowerGrade(1);
                    }
                    int pledgeClass = 0;

                    pledgeClass = player.getClan().getClanMember(objectId).calculatePledgeClass(player);

                    if (player.isNoble() && pledgeClass < 5)
                    {
                        pledgeClass = 5;
                    }

                    if (player.isHero() && pledgeClass < 8)
                    {
                        pledgeClass = 8;
                    }

                    player.setPledgeClass(pledgeClass);
                }
                else
                {
                    player.setClanPrivileges(L2Clan.CP_NOTHING);
                }

                player.setDeleteTimer(rset.getLong("deletetime"));

                player.setTitle(rset.getString("title"));
                player.setTitleColor(rset.getInt("title_color"));
                player.setAccessLevel(rset.getInt("accesslevel"));
                player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
                player.setUptime(System.currentTimeMillis());

                currentHp = rset.getDouble("curHp");
                currentCp = rset.getDouble("curCp");
                currentMp = rset.getDouble("curMp");

                player._classIndex = 0;
                try
                {
                    player.setBaseClass(rset.getInt("base_class"));
                }
                catch (Exception e)
                {
                    player.setBaseClass(activeClassId);
                }

                player._templateId = rset.getInt("templateId");

                // Restore Subclass Data (cannot be done earlier in function)
                if (restoreSubClassData(player))
                {
                    if (activeClassId != player.getBaseClass())
                    {
                        for (SubClass subClass : player.getSubClasses().values())
                        {
                            if (subClass.getClassId() == activeClassId)
                            {
                                player._classIndex = subClass.getClassIndex();
                            }
                        }
                    }
                }
                if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass() && playerTemporaryLevel == 0)
                {
                    // Subclass in use but doesn't exist in DB -
                    // a possible restart-while-modifysubclass cheat has been attempted.
                    // Switching to use base class
                    player.setClassId(player.getBaseClass());
                    Log.warning("Player " + player.getName() +
                            " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
                }
                else
                {
                    player._activeClass = activeClassId;
                }
                player._currentClass = PlayerClassTable.getInstance().getClassById(player._activeClass);

                player.setNoble(rset.getBoolean("nobless"));

                player.setApprentice(rset.getInt("apprentice"));
                player.setSponsor(rset.getInt("sponsor"));
                player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
                player.setPunishLevel(rset.getInt("punish_level"));
                if (player.getPunishLevel() != PunishLevel.NONE)
                {
                    player.setPunishTimer(rset.getLong("punish_timer"));
                }
                else
                {
                    player.setPunishTimer(0);
                }

                CursedWeaponsManager.getInstance().checkPlayer(player);

                player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));

                // Set Teleport Bookmark Slot
                player.setBookMarkSlot(rset.getInt("BookmarkSlot"));

                //character creation Time
                player.setCreateTime(rset.getLong("createTime"));

                // Showing hat or not?
                player.setShowHat(rset.getBoolean("show_hat"));

                // Race appearance
                player.setRaceAppearance(rset.getInt("race_app"));

                // Add the L2PcInstance object in _allObjects
                //L2World.getInstance().storeObject(player);

                // Set the x,y,z position of the L2PcInstance and make it invisible
                int x = rset.getInt("x");
                int y = rset.getInt("y");
                int z = rset.getInt("z");
                MainTownInfo mainTown = MainTownManager.getInstance().getCurrentMainTown();
                if (z > 100000 && mainTown != null)
                {
                    z -= 1000000;
                    if (TownManager.getTown(x, y, z) != TownManager.getTown(mainTown.getTownId()))
                    {
                        int[] coords = mainTown.getRandomCoords();
                        x = coords[0];
                        y = coords[1];
                        z = coords[2];
                    }
                }
                // Set the x,y,z position of the L2PcInstance and make it invisible
                player.setXYZInvisible(x, y, z);

                // Retrieve the name and ID of the other characters assigned to this account.
                PreparedStatement stmt = con.prepareStatement(
                        "SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?");
                stmt.setString(1, player._accountName);
                stmt.setInt(2, objectId);
                ResultSet chars = stmt.executeQuery();

                while (chars.next())
                {
                    Integer charId = chars.getInt("charId");
                    String charName = chars.getString("char_name");
                    player._chars.put(charId, charName);
                }

                chars.close();
                stmt.close();
                break;
            }

            rset.close();
            statement.close();

            statement = con.prepareStatement(RESTORE_ACCOUNT_GSDATA);
            statement.setString(1, player.getAccountName());
            statement.setString(2, "vitality");
            rset = statement.executeQuery();
            if (rset.next())
            {
                player.setVitalityPoints(Integer.parseInt(rset.getString("value")), true, true);
            }
            else
            {
                statement.close();
                statement = con.prepareStatement("INSERT INTO account_gsdata(account_name,var,value) VALUES(?,?,?);");
                statement.setString(1, player.getAccountName());
                statement.setString(2, "vitality");
                statement.setString(3, String.valueOf(PcStat.MAX_VITALITY_POINTS));
                statement.execute();
                player.setVitalityPoints(PcStat.MAX_VITALITY_POINTS, true, true);
            }
            rset.close();
            statement.close();

            // Set Hero status if it applies
            if (HeroesManager.getInstance().getHeroes() != null &&
                    HeroesManager.getInstance().getHeroes().containsKey(objectId))
            {
                player.setHero(true);
            }

            // Retrieve from the database all skills of this L2PcInstance and add them to _skills
            // Retrieve from the database all items of this L2PcInstance and add them to _inventory
            player.getInventory().restore();
            if (!Config.WAREHOUSE_CACHE)
            {
                player.getWarehouse();
            }

            // Retrieve from the database all secondary data of this L2PcInstance
            // and reward expertise/lucky skills if necessary.
            // Note that Clan, Noblesse and Hero skills are given separately and not here.
            player.restoreCharData();

            player.giveSkills(false);
            player.rewardSkills();

            if (playerTemporaryLevel != 0)
            {
                player.setTemporaryLevelToApply(playerTemporaryLevel);
            }

            // buff and status icons
            if (Config.STORE_SKILL_COOLTIME)
            {
                player.restoreEffects();
            }

            // Restore current Cp, HP and MP values
            player.setCurrentCp(currentCp);
            player.setCurrentHp(currentHp);
            player.setCurrentMp(currentMp);

            if (currentHp < 0.5)
            {
                player.setIsDead(true);
                player.stopHpMpRegeneration();
            }

            // Restore pet if exists in the world
            player.setPet(L2World.getInstance().getPet(player.getObjectId()));
            if (player.getPet() != null)
            {
                player.getPet().setOwner(player);
            }

            // Update the overloaded status of the L2PcInstance
            player.refreshOverloaded();
            // Update the expertise status of the L2PcInstance
            player.refreshExpertisePenalty();

            player.restoreFriendList();

            player.restoreMenteeList();
            player.restoreMentorInfo();

            player.restoreBlockList();
            if (player.isMentor())
            {
                player.giveMentorSkills();
            }
            if (!player.canBeMentor() && player.isMentee())
            {
                player.giveMenteeSkills();
            }

            if (Config.STORE_UI_SETTINGS)
            {
                player.restoreUISettings();
            }

            player.restoreLastSummons();

            player.restoreZoneRestartLimitTime();
            player.restoreGearPresets();
            //OpenWorldOlympiadsManager.getInstance().onLogin(player);
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Failed loading character.", e);
            e.printStackTrace();
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        return player;
    }

    /**
     * @return
     */
    public Forum getMail()
    {
        if (_forumMail == null)
        {
            setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));

            if (_forumMail == null)
            {
                ForumsBBSManager.getInstance()
                        .createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"),
                                Forum.MAIL, Forum.OWNERONLY, getObjectId());
                setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
            }
        }

        return _forumMail;
    }

    /**
     * @param forum
     */
    public void setMail(Forum forum)
    {
        _forumMail = forum;
    }

    /**
     * @return
     */
    public Forum getMemo()
    {
        if (_forumMemo == null)
        {
            setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));

            if (_forumMemo == null)
            {
                ForumsBBSManager.getInstance()
                        .createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"),
                                Forum.MEMO, Forum.OWNERONLY, getObjectId());
                setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
            }
        }

        return _forumMemo;
    }

    /**
     * @param forum
     */
    public void setMemo(Forum forum)
    {
        _forumMemo = forum;
    }

    /**
     * Restores sub-class data for the L2PcInstance, used to check the current
     * class index for the character.
     */
    private static boolean restoreSubClassData(L2PcInstance player)
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
            statement.setInt(1, player.getObjectId());

            ResultSet rset = statement.executeQuery();

            while (rset.next())
            {
                SubClass subClass = new SubClass();
                subClass.setClassId(rset.getInt("class_id"));
                subClass.setIsDual(rset.getBoolean("is_dual"));
                subClass.setLevel(rset.getByte("level"));
                subClass.setExp(rset.getLong("exp"));
                subClass.setSp(rset.getLong("sp"));
                subClass.setClassIndex(rset.getInt("class_index"));
                subClass.setCertificates(rset.getInt("certificates"));

                // Enforce the correct indexing of _subClasses against their class indexes.
                player.getSubClasses().put(subClass.getClassIndex(), subClass);
            }

            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not restore classes for " + player.getName() + ": " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        return true;
    }

    /**
     * Restores secondary data for the L2PcInstance, based on the current class index.
     */
    private void restoreCharData()
    {
        // Retrieve from the database all skills of this L2PcInstance and add them to _skills.
        restoreSkills();

        // Retrieve from the database all macroses of this L2PcInstance and add them to _macroses.
        _macroses.restore();

        // Retrieve from the database all shortCuts of this L2PcInstance and add them to _shortCuts.
        _shortCuts.restore();

        // Retrieve from the database all henna of this L2PcInstance and add them to _henna.
        restoreHenna();

        // Retrieve from the database all teleport bookmark of this L2PcInstance and add them to _tpbookmark.
        restoreTeleportBookmark();

        // Retrieve from the database the recipe book of this L2PcInstance.
        restoreRecipeBook(true);

        // Restore Recipe Shop list
        if (Config.STORE_RECIPE_SHOPLIST)
        {
            restoreRecipeShopList();
        }

        // Load Premium Item List
        loadPremiumItemList();

        // Check for items in pet inventory
        checkPetInvItems();

        restoreLastSummons();

        restoreAbilities();

        restoreConfigs();
    }

    /**
     * Restore recipe book data for this L2PcInstance.
     */
    private void restoreRecipeBook(boolean loadCommon)
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            String sql = loadCommon ? "SELECT id, type, classIndex FROM character_recipebook WHERE charId=?" :
                    "SELECT id FROM character_recipebook WHERE charId=? AND classIndex=? AND type = 1";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, getObjectId());
            if (!loadCommon)
            {
                statement.setInt(2, _classIndex);
            }
            ResultSet rset = statement.executeQuery();

            _dwarvenRecipeBook.clear();

            L2RecipeList recipe;
            while (rset.next())
            {
                recipe = RecipeController.getInstance().getRecipeList(rset.getInt("id"));

                if (loadCommon)
                {
                    if (rset.getInt(2) == 1)
                    {
                        if (rset.getInt(3) == _classIndex)
                        {
                            registerDwarvenRecipeList(recipe, false);
                        }
                    }
                    else
                    {
                        registerCommonRecipeList(recipe, false);
                    }
                }
                else
                {
                    registerDwarvenRecipeList(recipe, false);
                }
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not restore recipe book data:" + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public Map<Integer, L2PremiumItem> getPremiumItemList()
    {
        return _premiumItems;
    }

    private void loadPremiumItemList()
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            String sql = "SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();
            while (rset.next())
            {
                int itemNum = rset.getInt("itemNum");
                int itemId = rset.getInt("itemId");
                long itemCount = rset.getLong("itemCount");
                String itemSender = rset.getString("itemSender");
                _premiumItems.put(itemNum, new L2PremiumItem(itemId, itemCount, itemSender));
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not restore premium items: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void updatePremiumItem(int itemNum, long newcount)
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=? ");
            statement.setLong(1, newcount);
            statement.setInt(2, getObjectId());
            statement.setInt(3, itemNum);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not update premium items: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void deletePremiumItem(int itemNum)
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement =
                    con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=? ");
            statement.setInt(1, getObjectId());
            statement.setInt(2, itemNum);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not delete premium item: " + e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * Update L2PcInstance stats in the characters table of the database.<BR><BR>
     */
    public synchronized void store(boolean storeActiveEffects)
    {
        //update client coords, if these look like true
        // if (isInsideRadius(getClientX(), getClientY(), 1000, true))
        //	setXYZ(getClientX(), getClientY(), getClientZ());

        storeCharBase();
        storeCharSub();
        storeEffect(storeActiveEffects);
        transformInsertInfo();
        if (Config.STORE_RECIPE_SHOPLIST)
        {
            storeRecipeShopList();
        }
        if (Config.STORE_UI_SETTINGS)
        {
            storeUISettings();
        }
        storeLastSummons();
        storeCharFriendMemos();
    }

    public void store()
    {
        store(true);
    }

    private void storeCharBase()
    {
        Connection con = null;

        try
        {
            // Get the exp, level, and sp of base class to store in base table
            long exp = getStat().getBaseClassExp();
            int level = getStat().getBaseClassLevel();
            long sp = getStat().getBaseClassSp();

            int x = _lastX != 0 ? _lastX :
                    _eventSavedPosition != null && isPlayingEvent() ? _eventSavedPosition.getX() : getX();
            int y = _lastY != 0 ? _lastY :
                    _eventSavedPosition != null && isPlayingEvent() ? _eventSavedPosition.getY() : getY();
            int z = _lastZ != 0 ? _lastZ :
                    _eventSavedPosition != null && isPlayingEvent() ? _eventSavedPosition.getZ() : getZ();

            MainTownInfo mainTown = MainTownManager.getInstance().getCurrentMainTown();
            if (mainTown != null)
            {
                L2TownZone currentTown = TownManager.getTown(x, y, z);
                if (currentTown != null && currentTown.getTownId() == mainTown.getTownId())
                {
                    z += 1000000;
                }
            }

            con = L2DatabaseFactory.getInstance().getConnection();

            // Update base class
            PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER);

            statement.setInt(1, level);
            statement.setInt(2, _temporaryLevel);
            statement.setDouble(3, getMaxHp());
            statement.setDouble(4, getCurrentHp());
            statement.setDouble(5, getMaxCp());
            statement.setDouble(6, getCurrentCp());
            statement.setDouble(7, getMaxMp());
            statement.setDouble(8, getCurrentMp());
            statement.setInt(9, getAppearance().getFace());
            statement.setInt(10, getAppearance().getHairStyle());
            statement.setInt(11, getAppearance().getHairColor());
            statement.setInt(12, getAppearance().getSex() ? 1 : 0);
            statement.setInt(13, getHeading());
            statement.setInt(14, x);
            statement.setInt(15, y);
            statement.setInt(16, z);
            statement.setLong(17, exp);
            statement.setLong(18, getExpBeforeDeath());
            statement.setLong(19, sp);
            statement.setInt(20, getReputation());
            statement.setInt(21, getFame());
            statement.setInt(22, getPvpKills());
            statement.setInt(23, getPkKills());
            statement.setInt(24, getClanId());
            statement.setInt(25, _templateId);
            statement.setInt(26, _currentClass.getId());
            statement.setLong(27, getDeleteTimer());
            statement.setString(28, getTitle());
            statement.setInt(29, getTitleColor());
            statement.setInt(30, getAccessLevel().getLevel());
            statement.setInt(31, isOnlineInt());
            statement.setInt(32, getClanPrivileges());
            statement.setInt(33, getWantsPeace());
            statement.setInt(34, getBaseClass());

            long totalOnlineTime = _onlineTime;

            if (_onlineBeginTime > 0)
            {
                totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;
            }

            statement.setLong(35, totalOnlineTime);
            statement.setInt(36, getPunishLevel().value());
            statement.setLong(37, getPunishTimer());
            statement.setInt(38, getNewbie());
            statement.setInt(39, isNoble() ? 1 : 0);
            statement.setLong(40, getPowerGrade());
            statement.setInt(41, getPledgeType());
            statement.setInt(42, getLvlJoinedAcademy());
            statement.setLong(43, getApprentice());
            statement.setLong(44, getSponsor());
            statement.setInt(45, getAllianceWithVarkaKetra());
            statement.setLong(46, getClanJoinExpiryTime());
            statement.setLong(47, getClanCreateExpiryTime());
            statement.setString(48, getName());
            statement.setInt(49, getBookMarkSlot());
            statement.setInt(50, isShowingHat() ? 1 : 0);
            statement.setInt(51, getRaceAppearance());
            statement.setInt(52, getObjectId());

            statement.execute();
            statement.close();

            if (getLevel() > 1)
            {
                statement = con.prepareStatement(UPDATE_ACCOUNT_GSDATA);
                statement.setString(1, String.valueOf(getVitalityPoints()));
                statement.setString(2, getAccountName());
                statement.setString(3, "vitality");
                statement.execute();
                statement.close();
            }
        }
        catch (Exception e)

        {
            Log.log(Level.WARNING, "Could not store char base data: " + this + " - " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    private void storeCharSub()
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);

            if (getTotalSubClasses() > 0)
            {
                for (SubClass subClass : getSubClasses().values())
                {
                    statement.setLong(1, subClass.getExp());
                    statement.setLong(2, subClass.getSp());
                    statement.setInt(3, subClass.getLevel());
                    statement.setInt(4, subClass.getClassId());
                    statement.setBoolean(5, subClass.isDual());
                    statement.setInt(6, subClass.getCertificates());
                    statement.setInt(7, getObjectId());
                    statement.setInt(8, subClass.getClassIndex());

                    statement.execute();
                }
            }
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not store sub class data for " + getName() + ": " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    private void storeEffect(boolean storeEffects)
    {
        if (!Config.STORE_SKILL_COOLTIME || isPlayingEvent())
        {
            return;
        }

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            // Delete all current stored effects for char to avoid dupe
            PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE);

            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            statement.execute();
            statement.close();

            int buff_index = 0;

            final List<Integer> storedSkills = new ArrayList<>();

            // Store all effect data along with calulated remaining
            // reuse delays for matching skills. 'restore_type'= 0.
            statement = con.prepareStatement(ADD_SKILL_SAVE);

            if (storeEffects)
            {
                for (L2Abnormal effect : getAllEffects())
                {
                    if (effect == null)
                    {
                        continue;
                    }

                    switch (effect.getType())
                    {
                        case HEAL_OVER_TIME:
                            // TODO: Fix me.
                        case HIDE:
                        case MUTATE:
                            continue;
                    }

                    L2Skill skill = effect.getSkill();

                    if (storedSkills.contains(skill.getReuseHashCode()))
                    {
                        continue;
                    }

                    if (skill.getPartyChangeSkill() != -1)
                    {
                        continue;
                    }

                    storedSkills.add(skill.getReuseHashCode());

                    if (!effect.isHerbEffect() && effect.getInUse() &&
                            (!skill.isToggle() || skill.getId() >= 11007 && skill.getId() <= 11010))
                    {
                        statement.setInt(1, getObjectId());
                        statement.setInt(2, skill.getId());
                        statement.setInt(3, skill.getLevelHash());
                        statement.setInt(4, effect.getCount());
                        statement.setInt(5, effect.getTime());

                        if (_reuseTimeStamps.containsKey(skill.getReuseHashCode()))
                        {
                            TimeStamp t = _reuseTimeStamps.get(skill.getReuseHashCode());
                            statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0);
                            statement.setDouble(7, t.hasNotPassed() ? t.getStamp() : 0);
                        }
                        else
                        {
                            statement.setLong(6, 0);
                            statement.setDouble(7, 0);
                        }

                        statement.setInt(8, 0);
                        statement.setInt(9, getClassIndex());
                        statement.setInt(10, ++buff_index);
                        statement.execute();
                    }
                }
            }

            // Store the reuse delays of remaining skills which
            // lost effect but still under reuse delay. 'restore_type' 1.
            for (int hash : _reuseTimeStamps.keySet())
            {
                if (storedSkills.contains(hash))
                {
                    continue;
                }

                TimeStamp t = _reuseTimeStamps.get(hash);
                if (t != null && t.hasNotPassed())
                {
                    storedSkills.add(hash);

                    statement.setInt(1, getObjectId());
                    statement.setInt(2, t.getSkillId());
                    statement.setInt(3, t.getSkillLvl());
                    statement.setInt(4, -1);
                    statement.setInt(5, -1);
                    statement.setLong(6, t.getReuse());
                    statement.setDouble(7, t.getStamp());
                    statement.setInt(8, 1);
                    statement.setInt(9, getClassIndex());
                    statement.setInt(10, ++buff_index);
                    statement.execute();
                }
            }
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not store char effect data: ", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * Return True if the L2PcInstance is on line.<BR>
     * <BR>
     */
    public boolean isOnline()
    {
        return _isOnline;
    }

    public int isOnlineInt()
    {
        if (_isOnline)
        {
            return getClient() == null || getClient().isDetached() ? 2 : 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance and save update in the character_skills table of the database.<BR><BR>
     * <p>
     * <B><U> Concept</U> :</B><BR><BR>
     * All skills own by a L2PcInstance are identified in <B>_skills</B><BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Replace oldSkill by newSkill or Add the newSkill </li>
     * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
     * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
     *
     * @param newSkill The L2Skill to add to the L2Character
     * @return The L2Skill replaced or null if just added a new L2Skill
     */
    public L2Skill addSkill(L2Skill newSkill, boolean store)
    {
        // Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance
        L2Skill oldSkill = super.addSkill(newSkill);

        if (_temporaryLevel != 0)
        {
            return oldSkill;
        }

        // Add or update a L2PcInstance skill in the character_skills table of the database
        if (store)
        {
            storeSkill(newSkill, oldSkill, -1);
        }

        return oldSkill;
    }

    @Override
    public L2Skill removeSkill(L2Skill skill, boolean store)
    {
        if (store)
        {
            return removeSkill(skill);
        }
        else
        {
            return super.removeSkill(skill, true);
        }
    }

    public L2Skill removeSkill(L2Skill skill, boolean store, boolean cancelEffect)
    {
        if (store)
        {
            return removeSkill(skill);
        }
        else
        {
            return super.removeSkill(skill, cancelEffect);
        }
    }

    /**
     * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.<BR><BR>
     * <p>
     * <B><U> Concept</U> :</B><BR><BR>
     * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Remove the skill from the L2Character _skills </li>
     * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
     * <p>
     * <B><U> Overridden in </U> :</B><BR><BR>
     * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
     *
     * @param skill The L2Skill to remove from the L2Character
     * @return The L2Skill removed
     */
    @Override
    public L2Skill removeSkill(L2Skill skill)
    {
        // Remove all the cubics if the user forgot a cubic skill
        if (skill instanceof L2SkillSummon && ((L2SkillSummon) skill).isCubic() && !_cubics.isEmpty())
        {
            for (L2CubicInstance cubic : _cubics.values())
            {
                cubic.stopAction();
                cubic.cancelDisappear();
            }

            _cubics.clear();
            broadcastUserInfo();
        }

        // Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
        L2Skill oldSkill = super.removeSkill(skill);

        Connection con = null;

        try
        {
            // Remove or update a L2PcInstance skill from the character_skills table of the database
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);

            if (oldSkill != null)
            {
                statement.setInt(1, oldSkill.getId());
                statement.setInt(2, getObjectId());
                statement.setInt(3, getClassIndex());
                statement.execute();
            }
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error could not delete skill: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        if (transformId() > 0 || isCursedWeaponEquipped())
        {
            return oldSkill;
        }

        L2ShortCut[] allShortCuts = getAllShortCuts();

        for (L2ShortCut sc : allShortCuts)
        {
            if (sc != null && skill != null && sc.getId() == skill.getId() && sc.getType() == L2ShortCut.TYPE_SKILL)
            {
                deleteShortCut(sc.getSlot(), sc.getPage());
            }
        }

        return oldSkill;
    }

    public void removeSkill(L2Skill skill, int classIndex)
    {
        if (skill == null)
        {
            return;
        }

        Connection con = null;
        try
        {
            // Remove or update a L2PcInstance skill from the character_skills table of the database
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);

            statement.setInt(1, skill.getId());
            statement.setInt(2, getObjectId());
            statement.setInt(3, classIndex);
            statement.execute();

            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error could not delete skill: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * Add or update a L2PcInstance skill in the character_skills table of the database.
     * <BR><BR>
     * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
     */
    public void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
    {
        int classIndex = _classIndex;

        if (newClassIndex > -1)
        {
            classIndex = newClassIndex;
        }

        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement;

            if (oldSkill != null && newSkill != null)
            {
                statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
                statement.setInt(1, newSkill.getLevelHash());
                statement.setInt(2, oldSkill.getId());
                statement.setInt(3, getObjectId());
                statement.setInt(4, classIndex);
                statement.execute();
                statement.close();
            }
            else if (newSkill != null)
            {
                statement = con.prepareStatement(ADD_NEW_SKILL);
                statement.setInt(1, getObjectId());
                statement.setInt(2, newSkill.getId());
                statement.setInt(3, newSkill.getLevelHash());
                statement.setInt(4, classIndex);
                statement.execute();
                statement.close();
            }
            else
            {
                Log.warning("could not store new skill. its NULL");
            }
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error could not store char skills: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * Retrieve from the database all skills of this L2PcInstance and add them to _skills.<BR><BR>
     */
    private void restoreSkills()
    {
        Connection con = null;

        if (getClassIndex() != 0)
        {
            try
            {
                // Retrieve all skills of this L2PcInstance from the database
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=? AND skill_id > ? AND skill_id < ?");

                statement.setInt(1, getObjectId());
                statement.setInt(2, 0);
                statement.setInt(3, 1955); // Certificate Skills, Lowest ID
                statement.setInt(4, 1987); // Certificate Skills, Highest ID
                ResultSet rset = statement.executeQuery();

                // Go though the recordset of this SQL query
                while (rset.next())
                {
                    int id = rset.getInt("skill_id");
                    int level = rset.getInt("skill_level");

                    // Create a L2Skill object for each record
                    L2Skill skill = SkillTable.getInstance().getInfo(id, level);

                    if (id > 1955 && id < 1987)
                    {
                        _certificationSkills.put(id, skill);
                    }

                    // Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
                    super.addSkill(skill);
                }

                rset.close();
                statement.close();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING, "Could not restore character " + this + " certificate skills: " + e.getMessage(),
                        e);
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }
        }

        try
        {
            // Retrieve all skills of this L2PcInstance from the database
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR);

            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            ResultSet rset = statement.executeQuery();

            // Go though the recordset of this SQL query
            while (rset.next())
            {
                int id = rset.getInt("skill_id");
                int level = rset.getInt("skill_level");

                //System.out.println("ID = " + id + " LEVEL = " + level);
                // Create a L2Skill object for each record
                L2Skill skill = SkillTable.getInstance().getInfo(id, level);

                if (id > 1955 && id < 1987)
                {
                    _certificationSkills.put(id, skill);
                }

                //System.out.println("LEVEL = " + skill.getLevel());

                boolean store = false;

                // Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
                addSkill(skill, store);

                if (Config.SKILL_CHECK_ENABLE && (!isGM() || Config.SKILL_CHECK_GM))
                {
                    if (!SkillTreeTable.getInstance().isSkillAllowed(this, skill))
                    {
                        //Util.handleIllegalPlayerAction(this, "Player " + getName() + " has invalid skill " + skill.getName() + " ("+skill.getId() + "/" + skill.getLevel() + "), class:" + getCurrentClass().getName(), 1);
                        if (Config.SKILL_CHECK_REMOVE)
                        {
                            removeSkill(skill);
                        }
                    }
                }
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not restore character " + this + " skills: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        //if (Config.SKILL_CHECK_ENABLE && (!isGM() || Config.SKILL_CHECK_GM))
        //	CertificateSkillTable.getInstance().checkPlayer(this);
    }

    /**
     * Retrieve from the database all skill effects of this L2PcInstance and add them to the player.<BR><BR>
     */
    public void restoreEffects()
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            ResultSet rset;

            statement = con.prepareStatement(RESTORE_SKILL_SAVE);
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            rset = statement.executeQuery();

            while (rset.next())
            {
                int effectCount = rset.getInt("effect_count");
                int effectCurTime = rset.getInt("effect_cur_time");
                long reuseDelay = rset.getLong("reuse_delay");
                long systime = rset.getLong("systime");
                int restoreType = rset.getInt("restore_type");

                final L2Skill skill =
                        SkillTable.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
                if (skill == null)
                {
                    continue;
                }

                final long remainingTime = systime - System.currentTimeMillis();
                if (remainingTime > 10)
                {
                    disableSkill(skill, remainingTime);
                    addTimeStamp(skill, reuseDelay, systime);
                }
                /*
                   Restore Type 1
                   The remaning skills lost effect upon logout but
                   were still under a high reuse delay.
                 */
                if (restoreType > 0)
                {
                    continue;
                }

                /*
                   Restore Type 0
                   These skill were still in effect on the character
                   upon logout. Some of which were self casted and
                   might still have had a long reuse delay which also
                   is restored.

                 */
                if (skill.hasEffects())
                {
                    Env env = new Env();
                    env.player = this;
                    env.target = this;
                    env.skill = skill;
                    L2Abnormal ef;
                    for (L2AbnormalTemplate et : skill.getEffectTemplates())
                    {
                        ef = et.getEffect(env);
                        if (ef != null)
                        {
                            ef.setCount(effectCount);
                            ef.setFirstTime(effectCurTime);
                            ef.scheduleEffect();
                        }
                    }
                }
            }

            rset.close();
            statement.close();

            statement = con.prepareStatement(DELETE_SKILL_SAVE);
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not restore " + this + " active effect data: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * Retrieve from the database all Henna of this L2PcInstance, add them to _henna and calculate stats of the L2PcInstance.<BR><BR>
     */
    private void restoreHenna()
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            ResultSet rset = statement.executeQuery();

            for (int i = 0; i < 4; i++)
            {
                _henna[i] = null;
            }

            while (rset.next())
            {
                int slot = rset.getInt("slot");
                if (slot < 1 || slot > 4)
                {
                    continue;
                }

                int symbol_id = rset.getInt("symbol_id");
                if (symbol_id != 0)
                {
                    L2Henna henna = HennaTable.getInstance().getTemplate(symbol_id);
                    if (henna != null)
                    {
                        //Check the dye time?
                        long expireTime = rset.getLong("expireTime");
                        if (henna.isFourthSlot())
                        {
                            if (expireTime < System.currentTimeMillis())
                            {
                                //In order to delete the dye from the db we should first assing it
                                _henna[slot - 1] = henna;
                                removeHenna(4);
                                continue;
                            }
                            addHennaSkills(henna);
                        }

                        _henna[slot - 1] = henna;
                        if (henna.isFourthSlot())
                        {
                            if (expireTime > 0)
                            {
                                _henna[slot - 1].setExpireTime(expireTime);
                            }
                            addHennaSkills(henna);
                        }
                    }
                }
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Failed restoing character " + this + " hennas.", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        // Calculate Henna modifiers of this L2PcInstance
        recalcHennaStats();
    }

    /**
     * Return the number of Henna empty slot of the L2PcInstance.<BR><BR>
     */
    public int getHennaEmptySlots()
    {
        int totalSlots = 0;
        if (getCurrentClass().level() == 1)
        {
            totalSlots = 2;
        }
        else
        {
            totalSlots = 4;
        }

        for (int i = 0; i < 4; i++)
        {
            if (_henna[i] != null)
            {
                totalSlots--;
            }
        }

        if (totalSlots <= 0)
        {
            return 0;
        }

        return totalSlots;
    }

    /**
     * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
     */
    public boolean removeHenna(int slot)
    {
        if (slot < 1 || slot > 4)
        {
            return false;
        }

        slot--;

        if (_henna[slot] == null)
        {
            return false;
        }

        L2Henna henna = _henna[slot];
        _henna[slot] = null;

        if (henna.isFourthSlot())
        {
            removeHennaSkills(henna);
        }

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);

            statement.setInt(1, getObjectId());
            statement.setInt(2, slot + 1);
            statement.setInt(3, getClassIndex());

            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Failed remocing character henna.", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        // Calculate Henna modifiers of this L2PcInstance
        recalcHennaStats();

        // Send Server->Client HennaInfo packet to this L2PcInstance
        sendPacket(new HennaInfo(this));

        // Send Server->Client UserInfo packet to this L2PcInstance
        sendPacket(new UserInfo(this));
        // Add the recovered dyes to the player's inventory and notify them.
        getInventory().addItem("Henna", henna.getDyeId(), henna.getAmountDyeRequire() / 2, this, null);

        reduceAdena("Henna", henna.getPrice() / 5, this, false);

        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
        sm.addItemName(henna.getDyeId());
        sm.addItemNumber(henna.getAmountDyeRequire() / 2);
        sendPacket(sm);

        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SYMBOL_DELETED));

        return true;
    }

    public void addHennaSkills(L2Henna henna)
    {
        //Add the 4rt slot dye skills
        L2Henna pHenna = getHenna(4);
        if (pHenna != null)
        {
            for (SkillHolder skill : henna.getSkills())
            {
                if (skill == null)
                {
                    continue;
                }
                addSkill(skill.getSkill());
            }
            sendSkillList();
        }
    }

    public void removeHennaSkills(L2Henna henna)
    {
        //Add the 4rt slot dye skills
        for (SkillHolder skill : henna.getSkills())
        {
            if (skill == null)
            {
                continue;
            }
            removeSkill(skill.getSkill());
        }
        sendSkillList();
    }

    /**
     * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
     */
    public boolean addHenna(L2Henna henna)
    {
        for (int i = 0; i < 4; i++)
        {
            if (_henna[i] == null)
            {
                if (henna.isFourthSlot())
                {
                    _henna[3] = henna;
                }
                else
                {
                    _henna[i] = henna;
                }

                // Calculate Henna modifiers of this L2PcInstance
                recalcHennaStats();

                Connection con = null;
                try
                {
                    con = L2DatabaseFactory.getInstance().getConnection();
                    PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);

                    statement.setInt(1, getObjectId());
                    statement.setInt(2, henna.getSymbolId());
                    statement.setInt(3, henna.isFourthSlot() ? 4 : i + 1);
                    statement.setInt(4, getClassIndex());
                    statement.setLong(5, henna.isFourthSlot() ? System.currentTimeMillis() + henna.getMaxTime() : 0);
                    statement.execute();
                    statement.close();
                }
                catch (Exception e)
                {
                    Log.log(Level.SEVERE, "Failed saving character henna.", e);
                }
                finally
                {
                    L2DatabaseFactory.close(con);
                }

                if (henna.isFourthSlot())
                {
                    henna.setExpireTime(System.currentTimeMillis() + henna.getMaxTime());
                }

                // Send Server->Client HennaInfo packet to this L2PcInstance
                sendPacket(new HennaInfo(this));

                // Send Server->Client UserInfo packet to this L2PcInstance
                sendPacket(new UserInfo(this));

                if (henna.isFourthSlot())
                {
                    addHennaSkills(henna);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate Henna modifiers of this L2PcInstance.<BR><BR>
     */
    private void recalcHennaStats()
    {
        _hennaINT = 0;
        _hennaSTR = 0;
        _hennaCON = 0;
        _hennaMEN = 0;
        _hennaWIT = 0;
        _hennaDEX = 0;
        _hennaLUC = 0;
        _hennaCHA = 0;

        for (byte i = 0; i < 6; i++)
        {
            _hennaElem[i] = 0;
        }

        for (int i = 0; i < 3; i++)
        {
            if (_henna[i] == null)
            {
                continue;
            }

            _hennaINT += _henna[i].getStatINT();
            _hennaSTR += _henna[i].getStatSTR();
            _hennaMEN += _henna[i].getStatMEM();
            _hennaCON += _henna[i].getStatCON();
            _hennaWIT += _henna[i].getStatWIT();
            _hennaDEX += _henna[i].getStatDEX();
            _hennaLUC += _henna[i].getStatLUC();
            _hennaCHA += _henna[i].getStatCHA();

            _hennaElem[_henna[i].getStatElemId()] = _henna[i].getStatElemVal();
        }
    }

    /**
     * Return the Henna of this L2PcInstance corresponding to the selected slot.<BR><BR>
     */
    public L2Henna getHenna(int slot)
    {
        if (slot < 1 || slot > 4)
        {
            return null;
        }

        return _henna[slot - 1];
    }

    /**
     * Return the INT Henna modifier of this L2PcInstance.<BR><BR>
     */
    public int getHennaStatINT()
    {
        return _hennaINT;
    }

    /**
     * Return the STR Henna modifier of this L2PcInstance.<BR><BR>
     */
    public int getHennaStatSTR()
    {
        return _hennaSTR;
    }

    /**
     * Return the CON Henna modifier of this L2PcInstance.<BR><BR>
     */
    public int getHennaStatCON()
    {
        return _hennaCON;
    }

    /**
     * Return the MEN Henna modifier of this L2PcInstance.<BR><BR>
     */
    public int getHennaStatMEN()
    {
        return _hennaMEN;
    }

    /**
     * Return the WIT Henna modifier of this L2PcInstance.<BR><BR>
     */
    public int getHennaStatWIT()
    {
        return _hennaWIT;
    }

    /**
     * Return the LUC Henna modifier of this L2PcInstance.<BR><BR>
     */
    public int getHennaStatLUC()
    {
        return _hennaLUC;
    }

    /**
     * Return the CHA Henna modifier of this L2PcInstance.<BR><BR>
     */
    public int getHennaStatCHA()
    {
        return _hennaCHA;
    }

    /**
     * Return the DEX Henna modifier of this L2PcInstance.<BR><BR>
     */
    public int getHennaStatDEX()
    {
        return _hennaDEX;
    }

    public int getHennaStatElem(byte elemId)
    {
        if (elemId < 0)
        {
            return 0;
        }

        return _hennaElem[elemId];
    }

    /**
     * Return True if the L2PcInstance is autoAttackable.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Check if the attacker isn't the L2PcInstance Pet </li>
     * <li>Check if the attacker is L2MonsterInstance</li>
     * <li>If the attacker is a L2PcInstance, check if it is not in the same party </li>
     * <li>Check if the L2PcInstance has negative Reputation </li>
     * <li>If the attacker is a L2PcInstance, check if it is not in the same siege clan (Attacker, Defender) </li><BR><BR>
     */
    @Override
    public boolean isAutoAttackable(L2Character attacker)
    {
        // Check invulnerability
        //if (isInvul())
        //return false;

        if (getIsInsideGMEvent())
        {
            if (!GMEventManager.getInstance().canAttack(this, attacker))
            {
                return false;
            }
            return true;
        }

        // Check if the attacker isn't the L2PcInstance Pet
        if (attacker == this || attacker == getPet() || getSummons().contains(attacker))
        {
            return false;
        }

        // TODO: check for friendly mobs
        // Check if the attacker is a L2MonsterInstance
        if (attacker instanceof L2MonsterInstance)
        {
            return true;
        }

        // Check if the attacker is in olympia and olympia start
        if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isInOlympiadMode())
        {
            if (isInOlympiadMode() && isOlympiadStart() &&
                    ((L2PcInstance) attacker).getOlympiadGameId() == getOlympiadGameId())
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        // Check if the attacker is not in the same clan
        boolean sameClan = getClan() != null && getClan().isMember(attacker.getObjectId());
        boolean sameParty = getParty() != null && getParty().getPartyMembers() != null &&
                getParty().getPartyMembers().contains(attacker);

        // Check if the L2PcInstance has negative Reputation or is engaged in a pvp
        if ((getReputation() < 0 || getPvpFlag() > 0) && !sameClan && !sameParty)
        {
            return true;
        }

        // Check if the attacker is a L2Playable
        if (attacker instanceof L2Playable)
        {
            if (isInsideZone(ZONE_PEACE))
            {
                return false;
            }

            // Get L2PcInstance
            L2PcInstance cha = attacker.getActingPlayer();

            // Check if the attacker is in event and event is started
            if (isPlayingEvent() && EventsManager.getInstance().isPlayerParticipant(cha.getObjectId()))
            {
                EventInstance attackerEvent = cha.getEvent();
                return _event.getConfig().isAllVsAll() || _event == attackerEvent &&
                        _event.getParticipantTeamId(getObjectId()) != _event.getParticipantTeamId(cha.getObjectId());
            }

            if (isInDuel() && attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isInDuel())
            {
                return true;
            }

            // is AutoAttackable if both players are in the same duel and the duel is still going on
            if (getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == cha.getDuelId())
            {
                return true;
            }

            // Check if the attacker is not in the same party
            if (sameParty)
            {
                return false;
            }

            // Check if the attacker is not in the same clan
            if (sameClan)
            {
                return false;
            }

            if (getClan() != null)
            {
                Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
                if (siege != null)
                {
                    // Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
                    if (siege.checkIsDefender(cha.getClan()) && siege.checkIsDefender(getClan()))
                    {
                        return false;
                    }

                    // Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
                    if (siege.checkIsAttacker(cha.getClan()) && siege.checkIsAttacker(getClan()))
                    {
                        return false;
                    }
                }

                // Check if clan is at war
                if (getClan() != null && cha.getClan() != null && getClan().isAtWarWith(cha.getClanId()) &&
                        cha.getClan().isAtWarWith(getClanId()) && getWantsPeace() == 0 && cha.getWantsPeace() == 0 &&
                        !isAcademyMember())
                {
                    return true;
                }
            }

            // Check if the L2PcInstance is in an arena or a siege area
            if (isInsideZone(ZONE_PVP) && cha.isInsideZone(ZONE_PVP))
            {
                return true;
            }
        }
        else if (attacker instanceof L2DefenderInstance)
        {
            if (getClan() != null)
            {
                Siege siege = SiegeManager.getInstance().getSiege(this);
                return siege != null && siege.checkIsAttacker(getClan());
            }
        }

        return false;
    }

    /**
     * Check if the active L2Skill can be casted.<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Check if the skill isn't toggle and is offensive </li>
     * <li>Check if the target is in the skill cast range </li>
     * <li>Check if the skill is Spoil type and if the target isn't already spoiled </li>
     * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill </li>
     * <li>Check if the caster isn't sitting </li>
     * <li>Check if all skills are enabled and this skill is enabled </li><BR><BR>
     * <li>Check if the caster own the weapon needed </li><BR><BR>
     * <li>Check if the skill is active </li><BR><BR>
     * <li>Check if all casting conditions are completed</li><BR><BR>
     * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR><BR>
     *
     * @param skill    The L2Skill to use
     * @param forceUse used to force ATTACK on players
     * @param dontMove used to prevent movement, if not in range
     */
    @Override
    public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
    {
        if (getFirstEffect(30517) != null) // Heavy Hand
        {
            switch (skill.getId())
            {
                case 10529: // Shadow Flash
                case 10267: // Hurricane Rush
                case 11057: // Magical Evasion
                case 11094: // Magical Charge
                case 10805: // Quick Charge
                case 10774: // Quick Evasion
                case 11508: // Assault Rush
                {
                    // These skills canno't be used while Heavy Hand is active.
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            }
        }
        if (isGM())
        {
            sendSysMessage("");
            sendSysMessage("");
            sendSysMessage("");
            sendSysMessage("");
            sendSysMessage("");
            sendSysMessage(
                    "=== Skill [" + skill.getName() + ":" + skill.getId() + "-" + skill.getLevel() + "] Data ===");
            sendSysMessage("Target > " + skill.getTargetType());
            sendSysMessage("TargetDirection > " + skill.getTargetDirection());
            sendSysMessage("BehaviorType > " + skill.getSkillBehavior());
            sendSysMessage("Type > " + skill.getSkillType());
            sendSysMessage(
                    "Reuse > " + skill.getReuseDelay() + " [" + (skill.isStaticReuse() ? "STATIC" : "NOT STATIC") +
                            "]");
            sendSysMessage(
                    "CastTime > " + skill.getHitTime() + " [" + (skill.isStaticHitTime() ? "STATIC" : "NOT STATIC") +
                            "]");
            sendSysMessage("CastRange > " + skill.getCastRange());
            sendSysMessage("EffectRange > " + skill.getEffectRange());
            sendSysMessage("Power > " + skill.getPower());
            sendSysMessage("OverHit > " + skill.isOverhit());
            sendSysMessage("Crit Rate > " + skill.getBaseCritRate());

            if (skill.getEffectTemplates() != null)
            {
                int abnormalId = 0;
                for (L2AbnormalTemplate abnormalTemplate : skill.getEffectTemplates())
                {
                    abnormalId++;

                    Env env = new Env();

                    env.player = this;

                    if (getTarget() instanceof L2Character)
                    {
                        env.target = (L2Character) getTarget();
                    }

                    env.skill = skill;

                    L2Abnormal abnormal = abnormalTemplate.getEffect(env);

                    sendSysMessage("=== Abnormal[" + abnormalId + "] === ");
                    sendSysMessage("- Type = " + abnormal.getType());
                    sendSysMessage("- Level = " + abnormal.getLevel());
                    sendSysMessage("- Land Rate = " + abnormal.getLandRate());
                    sendSysMessage("- Count = " + abnormal.getCount());
                    sendSysMessage("- Duration = " + abnormal.getDuration());
                    sendSysMessage("StackTypes: ");

                    for (String stackType : abnormal.getStackType())
                    {
                        sendSysMessage("- " + stackType);
                    }

                    sendSysMessage("=== EFFECTS ===");

                    for (Func func : abnormal.getStatFuncs())
                    {
                        Env fEnv = new Env();
                        fEnv.value = 0;
                        fEnv.player = this;
                        boolean isPercent = func instanceof FuncAddPercent || func instanceof FuncSubPercent ||
                                func instanceof FuncAddPercentBase ||
                                func instanceof FuncSubPercentBase; // todo more cases
                        if (isPercent)
                        {
                            fEnv.value = 100;
                        }
						/*
						else if (func instanceof FuncMul || func instanceof FuncMulBase || func instanceof FuncSet ||
								func instanceof
								func instanceof FuncBaseAdd ||
								func instanceof FuncBaseSub ||
								func instanceof FuncAdd) // todo more cases
							fEnv.value = 0;*/

                        fEnv.baseValue = fEnv.value;
                        func.calc(fEnv);
                        double val = fEnv.value;
                        if (isPercent)
                        {
                            val -= 100;
                        }

                        sendSysMessage(func.getClass().getSimpleName().substring(4).toUpperCase() + " > " +
                                func.stat.getValue().toUpperCase() + " > " + val);
                    }
                }
            }
        }

        // Check if the skill is active
        if (skill.isPassive())
        {
            // just ignore the passive skill request. why does the client send it anyway ??
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        //************************************* Check Casting in Progress *******************************************

        // If a skill is currently being used, queue this one if this is not the same
        if (!canCastNow(skill))
        {
            //Log.info(getName() + " cant cast now..");
            SkillDat currentSkill = getCurrentSkill();
            // Check if new skill different from current skill in progress
            if (currentSkill != null && (skill.getId() == currentSkill.getSkillId() ||
                    currentSkill.getSkill().getSkillType() == L2SkillType.CLASS_CHANGE))
            {
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }

            if (Config.DEBUG && getQueuedSkill() != null)
            {
                Log.info(getQueuedSkill().getSkill().getName() + " is already queued for " + getName() + ".");
            }

            // Create a new SkillDat object and queue it in the player _queuedSkill
            setQueuedSkill(skill, forceUse, dontMove);
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        if (canDoubleCast() && isCastingNow1())
        {
            setIsCastingNow2(true);
        }
        else
        {
            setIsCastingNow(true);
        }
        // Create a new SkillDat object and set the player _currentSkill
        // This is used mainly to save & queue the button presses, since L2Character has
        // _lastSkillCast which could otherwise replace it
        setCurrentSkill(skill, forceUse, dontMove);

        if (getQueuedSkill() != null) // wiping out previous values, after casting has been aborted
        {
            setQueuedSkill(null, false, false);
        }

        if (!checkUseMagicConditions(skill, forceUse, dontMove))
        {
            if (wasLastCast1())
            {
                setIsCastingNow(false);
            }
            else
            {
                setIsCastingNow2(false);
            }
            return false;
        }

        // Check if the target is correct and Notify the AI with AI_INTENTION_CAST and target
        L2Object target = null;

        switch (skill.getTargetType())
        {
            case TARGET_AURA: // AURA, SELF should be cast even if no target has been found
            case TARGET_FRONT_AURA:
            case TARGET_BEHIND_AURA:
            case TARGET_GROUND:
            case TARGET_GROUND_AREA:
            case TARGET_SELF:
            case TARGET_AURA_CORPSE_MOB:
                target = this;
                break;
            default:
                // Get the first target of the list
                if (skill.isUseableWithoutTarget())
                {
                    target = this;
                }
                else if (skill.getTargetDirection() == L2SkillTargetDirection.CHAIN_HEAL)
                {
                    target = getTarget();
                }
                else
                {
                    target = skill.getFirstOfTargetList(this);
                }
                break;
        }

        // Notify the AI with AI_INTENTION_CAST and target
        getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);

        if (skill.getId() == 30001)
        {
            setQueuedSkill(skill, forceUse, dontMove);
        }

        return true;
    }

    @Override
    public boolean canDoubleCast()
    {
        return _elementalStance >= 10 && isAffected(L2EffectType.DOUBLE_CASTING.getMask());
    }

    private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove)
    {
        L2SkillType sklType = skill.getSkillType();

        //************************************* Check Player State *******************************************

        // Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()
        boolean canCastWhileStun = false;
        switch (skill.getId())
        {
            case 30008: // Wind Blend
            case 19227: // Wind Blend Trigger
            case 30009: // Deceptive Blink
            {
                canCastWhileStun = true;
                break;
            }
            default:
            {
                break;
            }
        }

        if ((isOutOfControl() || isParalyzed() || isStunned() && !canCastWhileStun || isSleeping()) &&
                !skill.canBeUsedWhenDisabled())
        {
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Check if the player is dead
        if (isDead())
        {
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        if (isFishing() && sklType != L2SkillType.PUMPING && sklType != L2SkillType.REELING &&
                sklType != L2SkillType.FISHING)
        {
            //Only fishing skills are available
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_FISHING_SKILLS_NOW));
            return false;
        }

        if (inObserverMode())
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
            abortCast();
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Check if the caster is sitting
        if (isSitting())
        {
            // Send a System Message to the caster
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_MOVE_SITTING));

            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Check if the skill type is TOGGLE
        if (skill.isToggle())
        {
            // Get effects of the skill
            L2Abnormal effect = getFirstEffect(skill.getId());

            if (effect != null)
            {
                effect.exit();

                // Send a Server->Client packet ActionFailed to the L2PcInstance
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }

        // Check if the player uses "Fake Death" skill
        // Note: do not check this before TOGGLE reset
        if (isFakeDeath())
        {
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        //************************************* Check Target *******************************************
        // Create and set a L2Object containing the target of the skill
        L2Object target = null;
        L2SkillTargetType sklTargetType = skill.getTargetType();
        Point3D worldPosition = getSkillCastPosition();

        if ((sklTargetType == L2SkillTargetType.TARGET_GROUND ||
                sklTargetType == L2SkillTargetType.TARGET_GROUND_AREA) && worldPosition == null)
        {
            Log.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        switch (sklTargetType)
        {
            // Target the player if skill type is AURA, PARTY, CLAN or SELF
            case TARGET_AURA:
            case TARGET_FRONT_AURA:
            case TARGET_BEHIND_AURA:
            case TARGET_PARTY:
            case TARGET_ALLY:
            case TARGET_CLAN:
            case TARGET_PARTY_CLAN:
            case TARGET_GROUND:
            case TARGET_GROUND_AREA:
            case TARGET_SELF:
            case TARGET_SUMMON:
            case TARGET_AURA_CORPSE_MOB:
            case TARGET_AREA_SUMMON:
                target = this;
                break;
            default:
                if (skill.isUseableWithoutTarget())
                {
                    target = this;
                }
                else
                {
                    target = getTarget();
                }
                break;
        }

        // Check the validity of the target
        if (target == null)
        {
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // skills can be used on Walls and Doors only during siege
        if (target instanceof L2DoorInstance)
        {
            boolean isCastle = ((L2DoorInstance) target).getCastle() != null &&
                    ((L2DoorInstance) target).getCastle().getCastleId() > 0 &&
                    ((L2DoorInstance) target).getCastle().getSiege().getIsInProgress() &&
                    ((L2DoorInstance) target).getIsShowHp();
            boolean isFort = ((L2DoorInstance) target).getFort() != null &&
                    ((L2DoorInstance) target).getFort().getFortId() > 0 &&
                    ((L2DoorInstance) target).getFort().getSiege().getIsInProgress() &&
                    !((L2DoorInstance) target).getIsShowHp();
            if (!isCastle && !isFort && ((L2DoorInstance) target).isOpenableBySkill() &&
                    skill.getSkillType() != L2SkillType.UNLOCK)
            {
                return false;
            }
        }

        if (target != this && target instanceof L2Playable &&
                ((L2Playable) target).isAffected(L2EffectType.UNTARGETABLE.getMask()))
        {
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Are the target and the player in the same duel?
        if (isInDuel())
        {
            // Get L2PcInstance
            if (target instanceof L2Playable)
            {
                // Get L2PcInstance
                L2PcInstance cha = target.getActingPlayer();
                if (cha.getDuelId() != getDuelId())
                {
                    sendMessage("You cannot do this while duelling.");
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            }
        }

        //************************************* Check skill availability *******************************************

        // Check if it's ok to summon
        //Siege Golems
        if (sklType == L2SkillType.SUMMON)
        {
            int npcId = ((L2SkillSummon) skill).getNpcId();
            L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
            if (summonTemplate != null)
            {
                if (summonTemplate.Type.equalsIgnoreCase("L2SiegeSummon") &&
                        !SiegeManager.getInstance().checkIfOkToSummon(this, false) &&
                        !FortSiegeManager.getInstance().checkIfOkToSummon(this, false))
                {
                    return false;
                }
            }
        }

        // Check if this skill is enabled (ex : reuse time)
        if (isSkillDisabled(skill))
        {
            SystemMessage sm = null;

            if (_reuseTimeStamps.containsKey(skill.getReuseHashCode()))
            {
                int remainingTime = (int) (_reuseTimeStamps.get(skill.getReuseHashCode()).getRemaining() / 1000);
                int hours = remainingTime / 3600;
                int minutes = remainingTime % 3600 / 60;
                int seconds = remainingTime % 60;
                if (hours > 0)
                {
                    sm = SystemMessage
                            .getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
                    sm.addSkillName(skill);
                    sm.addNumber(hours);
                    sm.addNumber(minutes);
                }
                else if (minutes > 0)
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
                    sm.addSkillName(skill);
                    sm.addNumber(minutes);
                }
                else
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
                    sm.addSkillName(skill);
                }

                sm.addNumber(seconds);
            }
            else
            {
                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
                sm.addSkillName(skill);
            }

            sendPacket(sm);
            return false;
        }

        //************************************* Check Consumables *******************************************

        // Check if spell consumes a Soul
        if (skill.getSoulConsumeCount() > 0)
        {
            if (getSouls() < skill.getSoulConsumeCount())
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THERE_IS_NOT_ENOUGH_SOUL));
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }
        //************************************* Check casting conditions *******************************************

        // Check if all casting conditions are completed
        if (!skill.checkCondition(this, target, false))
        {
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        //************************************* Check Skill Type *******************************************

        // Check if this is offensive magic skill
        if (skill.isOffensive())
        {
            if (!isInDuel() && isInsidePeaceZone(this, target) && !getAccessLevel().allowPeaceAttack())
            {
                // If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }

            if (isInOlympiadMode() && !isOlympiadStart())
            {
                // if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }

            if (target.getActingPlayer() != null && getSiegeState() > 0 && isInsideZone(L2Character.ZONE_SIEGE) &&
                    target.getActingPlayer().getSiegeState() == getSiegeState() && target.getActingPlayer() != this &&
                    target.getActingPlayer().getSiegeSide() == getSiegeSide() && !Config.isServer(Config.TENKAI))
            {
                sendPacket(SystemMessage.getSystemMessage(
                        SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS));
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }

            // Check if the target is attackable
            if (target != this && !(target instanceof L2NpcInstance) && !isInDuel() && target instanceof L2PcInstance &&
                    !((L2PcInstance) target).isInDuel() && !target.isAttackable() &&
                    !getAccessLevel().allowPeaceAttack())
            {
                // If target is not attackable, send a Server->Client packet ActionFailed
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }

            // Check if a Forced ATTACK is in progress on non-attackable target
            if (!target.isAutoAttackable(this) && !forceUse)
            {
                switch (sklTargetType)
                {
                    case TARGET_AURA:
                    case TARGET_FRONT_AURA:
                    case TARGET_BEHIND_AURA:
                    case TARGET_CLAN:
                    case TARGET_PARTY_CLAN:
                    case TARGET_ALLY:
                    case TARGET_PARTY:
                    case TARGET_SELF:
                    case TARGET_GROUND:
                    case TARGET_GROUND_AREA:
                    case TARGET_AURA_CORPSE_MOB:
                    case TARGET_AREA_SUMMON:
                    case TARGET_AROUND_CASTER:
                    case TARGET_FRIENDS:
                    case TARGET_AROUND_TARGET:
                        break;
                    default: // Send a Server->Client packet ActionFailed to the L2PcInstance
                        sendPacket(ActionFailed.STATIC_PACKET);
                        return false;
                }
            }

            // Check if the target is in the skill cast range
            if (dontMove)
            {
                // Calculate the distance between the L2PcInstance and the target
                if (sklTargetType == L2SkillTargetType.TARGET_GROUND ||
                        sklTargetType == L2SkillTargetType.TARGET_GROUND_AREA)
                {
                    if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                            skill.getCastRange() + getTemplate().collisionRadius, false, false))
                    {
                        // Send a System Message to the caster
                        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_TOO_FAR));

                        // Send a Server->Client packet ActionFailed to the L2PcInstance
                        sendPacket(ActionFailed.STATIC_PACKET);
                        return false;
                    }
                }
                else if (skill.getCastRange() > 0 &&
                        !isInsideRadius(target, skill.getCastRange() + getTemplate().collisionRadius, false, false))
                {
                    // Send a System Message to the caster
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_TOO_FAR));

                    // Send a Server->Client packet ActionFailed to the L2PcInstance
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            }
        }

        if (skill.getSkillType() == L2SkillType.INSTANT_JUMP)
        {
            if (skill.getId() == 10529) // TODO this should be done in the skill xml...
            {
                SkillTable.getInstance().getInfo(10530, 1).getEffects(this, this);
            }

            // You cannot jump while movement disabled
            if (isMovementDisabled() && !isRooted())
            {
                // Sends message that skill cannot be used...
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                sm.addSkillName(skill.getId());
                sendPacket(sm);

                // Send a Server->Client packet ActionFailed to the L2PcInstance
                sendPacket(ActionFailed.STATIC_PACKET);

                return false;
            }

            // And this skill cannot be used in peace zone, not even on NPCs!
            if (isInsideZone(L2Character.ZONE_PEACE))
            {
                //Sends a sys msg to client
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));

                // Send a Server->Client packet ActionFailed to the L2PcInstance
                sendPacket(ActionFailed.STATIC_PACKET);

                return false;
            }
        }
        // Check if the skill is defensive
        if (skill.getSkillBehavior() != L2SkillBehaviorType.ATTACK && !skill.isOffensive() &&
                target instanceof L2MonsterInstance && !forceUse && !skill.isNeutral())
        {
            // check if the target is a monster and if force attack is set.. if not then we don't want to cast.
            switch (sklTargetType)
            {
                case TARGET_SUMMON:
                case TARGET_AURA:
                case TARGET_FRONT_AURA:
                case TARGET_BEHIND_AURA:
                case TARGET_CLAN:
                case TARGET_PARTY_CLAN:
                case TARGET_SELF:
                case TARGET_PARTY:
                case TARGET_ALLY:
                case TARGET_CORPSE_MOB:
                case TARGET_AURA_CORPSE_MOB:
                case TARGET_AREA_CORPSE_MOB:
                case TARGET_GROUND:
                case TARGET_GROUND_AREA:
                    break;
                default:
                {
                    switch (sklType)
                    {
                        case BEAST_FEED:
                        case DELUXE_KEY_UNLOCK:
                        case UNLOCK:
                            break;
                        default:
                            sendPacket(ActionFailed.STATIC_PACKET);
                            return false;
                    }
                    break;
                }
            }
        }

        // Check if the skill is Spoil type and if the target isn't already spoiled
        if (sklType == L2SkillType.SPOIL)
        {
            if (!(target instanceof L2MonsterInstance))
            {
                // Send a System Message to the L2PcInstance
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));

                // Send a Server->Client packet ActionFailed to the L2PcInstance
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }

        // Check if the skill is Sweep type and if conditions not apply
        if (sklType == L2SkillType.SWEEP && target instanceof L2Attackable)
        {
            int spoilerId = ((L2Attackable) target).getIsSpoiledBy();

            if (((L2Attackable) target).isDead())
            {
                if (!((L2Attackable) target).isSpoil())
                {
                    // Send a System Message to the L2PcInstance
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED));

                    // Send a Server->Client packet ActionFailed to the L2PcInstance
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }

                if (getObjectId() != spoilerId && !isInLooterParty(spoilerId))
                {
                    // Send a System Message to the L2PcInstance
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SWEEP_NOT_ALLOWED));

                    // Send a Server->Client packet ActionFailed to the L2PcInstance
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            }
        }

        // Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
        if (sklType == L2SkillType.DRAIN_SOUL)
        {
            if (!(target instanceof L2MonsterInstance))
            {
                // Send a System Message to the L2PcInstance
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));

                // Send a Server->Client packet ActionFailed to the L2PcInstance
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }

        // Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
        switch (sklTargetType)
        {
            case TARGET_PARTY:
            case TARGET_ALLY: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
            case TARGET_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
            case TARGET_PARTY_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
            case TARGET_AURA:
            case TARGET_FRONT_AURA:
            case TARGET_BEHIND_AURA:
            case TARGET_GROUND:
            case TARGET_GROUND_AREA:
            case TARGET_SELF:
            case TARGET_AURA_CORPSE_MOB:
                break;
            default:
                if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack())
                {
                    // Send a System Message to the L2PcInstance
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));

                    // Send a Server->Client packet ActionFailed to the L2PcInstance
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
        }

        // TODO: Unhardcode skillId 844 which is the outpost construct skill
        if (sklTargetType == L2SkillTargetType.TARGET_HOLY &&
                !checkIfOkToCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill) ||
                sklTargetType == L2SkillTargetType.TARGET_FLAGPOLE &&
                        !checkIfOkToCastFlagDisplay(FortManager.getInstance().getFort(this), false, skill) ||
                sklType == L2SkillType.SIEGEFLAG &&
                        !L2SkillSiegeFlag.checkIfOkToPlaceFlag(this, false, skill.getId() == 844) ||
                sklType == L2SkillType.STRSIEGEASSAULT && !checkIfOkToUseStriderSiegeAssault() ||
                sklType == L2SkillType.SUMMON_FRIEND &&
                        !(checkSummonerStatus(this) && checkSummonTargetStatus(target, this)))
        {
            sendPacket(ActionFailed.STATIC_PACKET);
            abortCast();
            return false;
        }

        // GeoData Los Check here
        if (skill.getCastRange() > 0)
        {
            if (sklTargetType == L2SkillTargetType.TARGET_GROUND ||
                    sklTargetType == L2SkillTargetType.TARGET_GROUND_AREA)
            {
                if (!GeoData.getInstance().canSeeTarget(this, worldPosition))
                {
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            }
            else if (!GeoData.getInstance().canSeeTarget(this, target))
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }
        // finally, after passing all conditions
        return true;
    }

    public boolean checkIfOkToUseStriderSiegeAssault()
    {
        Castle castle = CastleManager.getInstance().getCastle(this);
        Fort fort = FortManager.getInstance().getFort(this);

        if (castle == null && fort == null)
        {
            return false;
        }

        if (castle != null)
        {
            return checkIfOkToUseStriderSiegeAssault(castle);
        }
        else
        {
            return checkIfOkToUseStriderSiegeAssault(fort);
        }
    }

    public boolean checkIfOkToUseStriderSiegeAssault(Castle castle)
    {
        String text = "";

        if (castle == null || castle.getCastleId() <= 0)
        {
            text = "You must be on castle ground to use strider siege assault";
        }
        else if (!castle.getSiege().getIsInProgress())
        {
            text = "You can only use strider siege assault during a siege.";
        }
        else if (!(getTarget() instanceof L2DoorInstance))
        {
            text = "You can only use strider siege assault on doors and walls.";
        }
        else if (!isRidingStrider())
        {
            text = "You can only use strider siege assault when on strider.";
        }
        else
        {
            return true;
        }

        sendMessage(text);

        return false;
    }

    public boolean checkIfOkToUseStriderSiegeAssault(Fort fort)
    {
        String text = "";

        if (fort == null || fort.getFortId() <= 0)
        {
            text = "You must be on fort ground to use strider siege assault";
        }
        else if (!fort.getSiege().getIsInProgress())
        {
            text = "You can only use strider siege assault during a siege.";
        }
        else if (!(getTarget() instanceof L2DoorInstance))
        {
            text = "You can only use strider siege assault on doors and walls.";
        }
        else if (!isRidingStrider())
        {
            text = "You can only use strider siege assault when on strider.";
        }
        else
        {
            return true;
        }

        sendMessage(text);

        return false;
    }

    public boolean checkIfOkToCastSealOfRule(Castle castle, boolean isCheckOnly, L2Skill skill)
    {
        SystemMessage sm;

        if (castle == null || castle.getCastleId() <= 0)
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
        }
        else if (!castle.getArtefacts().contains(getTarget()))
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
        }
        else if (!castle.getSiege().getIsInProgress())
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
        }
        else if (!Util.checkIfInRange(85, this, getTarget(), true))
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
        }
        else if (castle.getSiege().getAttackerClan(getClan()) == null)
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
        }
        else
        {
            if (!isCheckOnly)
            {
                sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING);
                castle.getSiege().announceToPlayer(sm, false);
            }
            return true;
        }

        sendPacket(sm);
        return false;
    }

    public boolean checkIfOkToCastFlagDisplay(Fort fort, boolean isCheckOnly, L2Skill skill)
    {
        SystemMessage sm;

        if (fort == null || fort.getFortId() <= 0)
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
        }
        else if (fort.getFlagPole() != getTarget())
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
        }
        else if (!fort.getSiege().getIsInProgress())
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
        }
        else if (!Util.checkIfInRange(85, this, getTarget(), true))
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
        }
        else if (fort.getSiege().getAttackerClan(getClan()) == null)
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
        }
        else
        {
            if (!isCheckOnly)
            {
                fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_TRYING_RAISE_FLAG),
                        getClan().getName());
            }
            return true;
        }

        sendPacket(sm);
        return false;
    }

    public boolean isInLooterParty(int LooterId)
    {
        L2PcInstance looter = L2World.getInstance().getPlayer(LooterId);

        // if L2PcInstance is in a CommandChannel
        if (isInParty() && getParty().isInCommandChannel() && looter != null)
        {
            return getParty().getCommandChannel().getMembers().contains(looter);
        }

        if (isInParty() && looter != null)
        {
            return getParty().getPartyMembers().contains(looter);
        }

        return false;
    }

    /**
     * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
     *
     * @param target L2Object instance containing the target
     * @param skill  L2Skill instance with the skill being casted
     * @return False if the skill is a pvpSkill and target is not a valid pvp target
     */
    public boolean checkPvpSkill(L2Object target, L2Skill skill)
    {
        return checkPvpSkill(target, skill, false);
    }

    /**
     * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
     *
     * @param target      L2Object instance containing the target
     * @param skill       L2Skill instance with the skill being casted
     * @param srcIsSummon is L2Summon - caster?
     * @return False if the skill is a pvpSkill and target is not a valid pvp target
     */
    public boolean checkPvpSkill(L2Object target, L2Skill skill, boolean srcIsSummon)
    {
        if (isPlayingEvent())
        {
            return true;
        }

        if (!Config.isServer(Config.TENKAI) && skill.getSkillBehavior() == L2SkillBehaviorType.ATTACK)
        {
            return true;
        }

        // check for PC->PC Pvp status
        if (target instanceof L2Summon)
        {
            target = target.getActingPlayer();
        }
        if (target != null && // target not null and
                target != this && // target is not self and
                target instanceof L2PcInstance && // target is L2PcInstance and
                !(isInDuel() && ((L2PcInstance) target).getDuelId() == getDuelId()) &&
                // self is not in a duel and attacking opponent
                !isInsideZone(ZONE_PVP) && // Pc is not in PvP zone
                !((L2PcInstance) target).isInsideZone(ZONE_PVP) // target is not in PvP zone
                )
        {
            if (skill.isOffensive() && ((L2PcInstance) target).isInsidePeaceZone(this))
            {
                return false;
            }

            SkillDat skilldat = getCurrentSkill();
            SkillDat skilldatpet = getCurrentPetSkill();
            if (skill.isPvpSkill()) // pvp skill
            {
                if (getClan() != null && ((L2PcInstance) target).getClan() != null)
                {
                    if (getClan().isAtWarWith(((L2PcInstance) target).getClanId()) &&
                            ((L2PcInstance) target).getClan().isAtWarWith(getClanId()))
                    {
                        return true; // in clan war player can attack whites even with sleep etc.
                    }
                }
                if (((L2PcInstance) target).getPvpFlag() == 0 && //   target's pvp flag is not set and
                        ((L2PcInstance) target).getReputation() >= 0)
                {
                    return false;
                }
            }
            else if (skilldat != null && !skilldat.isCtrlPressed() && skill.isOffensive() && !srcIsSummon ||
                    skilldatpet != null && !skilldatpet.isCtrlPressed() && skill.isOffensive() && srcIsSummon)
            {
                if (getClan() != null && ((L2PcInstance) target).getClan() != null)
                {
                    if (getClan().isAtWarWith(((L2PcInstance) target).getClanId()) &&
                            ((L2PcInstance) target).getClan().isAtWarWith(getClanId()))
                    {
                        return true; // in clan war player can attack whites even without ctrl
                    }
                }
                if (!Config.isServer(Config.TENKAI) && ((L2PcInstance) target).getPvpFlag() == 0 &&
                        //   target's pvp flag is not set and
                        ((L2PcInstance) target).getReputation() >= 0)
                {
                    return false;
                }

                if (Config.isServer(Config.TENKAI) && getPvpFlag() == 0 && ((L2PcInstance) target).getReputation() >= 0)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Return True if the L2PcInstance is a Mage.<BR><BR>
     */
    public boolean isMageClass()
    {
        return getCurrentClass().isMage();
    }

    public boolean isMounted()
    {
        return _mountType > 0;
    }

    /**
     * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the L2PcInstance.<BR><BR>
     */
    public boolean checkLandingState()
    {
        // Check if char is in a no landing zone
        if (isInsideZone(ZONE_NOLANDING))
        {
            return true;
        }
        else
            // if this is a castle that is currently being sieged, and the rider is NOT a castle owner
            // he cannot land.
            // castle owner is the leader of the clan that owns the castle where the pc is
            if (isInsideZone(ZONE_SIEGE) && !(getClan() != null && CastleManager.getInstance().getCastle(this) ==
                    CastleManager.getInstance().getCastleByOwner(getClan()) &&
                    this == getClan().getLeader().getPlayerInstance()))
            {
                return true;
            }

        return false;
    }

    // returns false if the change of mount type fails.
    public boolean setMount(int npcId, int npcLevel, int mountType)
    {
        switch (mountType)
        {
            case 0:
                setIsFlying(false);
                setIsRidingStrider(false);
                break; //Dismounted
            case 1:
                setIsRidingStrider(true);
                if (isNoble())
                {
                    L2Skill striderAssaultSkill = SkillTable.FrequentSkill.STRIDER_SIEGE_ASSAULT.getSkill();
                    addSkill(striderAssaultSkill, false); // not saved to DB
                }
                break;
            case 2:
                setIsFlying(true);
                break; //Flying Wyvern
            case 3:
                break;
        }

        _mountType = mountType;
        _mountNpcId = npcId;
        _mountLevel = npcLevel;

        return true;
    }

    /**
     * Return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern, 3: Wolf).<BR><BR>
     */
    public int getMountType()
    {
        return _mountType;
    }

    @Override
    public final void stopAllEffects()
    {
        super.stopAllEffects();
        updateAndBroadcastStatus(2);
    }

    @Override
    public final void stopAllEffectsExceptThoseThatLastThroughDeath()
    {
        super.stopAllEffectsExceptThoseThatLastThroughDeath();

        if (!getSummons().isEmpty())
        {
            for (L2SummonInstance summon : getSummons())
            {
                if (summon == null)
                {
                    continue;
                }
                summon.stopAllEffectsExceptThoseThatLastThroughDeath();
            }
        }

        if (getPet() != null)
        {
            getPet().stopAllEffectsExceptThoseThatLastThroughDeath();
        }

        updateAndBroadcastStatus(2);
    }

    /**
     * Stop all toggle-type effects
     */
    public final void stopAllToggles()
    {
        _effects.stopAllToggles();
    }

    public final void stopCubics()
    {
        if (getCubics() != null)
        {
            boolean removed = false;
            for (L2CubicInstance cubic : getCubics().values())
            {
                cubic.stopAction();
                delCubic(cubic.getId());
                removed = true;
            }
            if (removed)
            {
                broadcastUserInfo();
            }
        }
    }

    public final void stopCubicsByOthers()
    {
        if (getCubics() != null)
        {
            boolean removed = false;
            for (L2CubicInstance cubic : getCubics().values())
            {
                if (cubic.givenByOther())
                {
                    cubic.stopAction();
                    delCubic(cubic.getId());
                    removed = true;
                }
            }
            if (removed)
            {
                broadcastUserInfo();
            }
        }
    }

    /**
     * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR><BR>
     * <p>
     * <B><U> Concept</U> :</B><BR><BR>
     * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
     * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
     * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
     * <p>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
     * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
     */
    @Override
    public void updateAbnormalEffect()
    {
        broadcastUserInfo();
        for (L2SummonInstance summon : _summons)
        {
            summon.updateAbnormalEffect();
        }
    }

    /**
     * Disable the Inventory and create a new task to enable it after 1.5s.<BR><BR>
     */
    public void tempInventoryDisable()
    {
        _inventoryDisable = true;

        ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
    }

    /**
     * Return True if the Inventory is disabled.<BR><BR>
     */
    public boolean isInventoryDisabled()
    {
        return _inventoryDisable;
    }

    private class InventoryEnable implements Runnable
    {
        @Override
        public void run()
        {
            _inventoryDisable = false;
        }
    }

    public Map<Integer, L2CubicInstance> getCubics()
    {
        return _cubics;
    }

    /**
     * Add a L2CubicInstance to the L2PcInstance _cubics.<BR><BR>
     */
    public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int maxcount, int totalLifetime, boolean givenByOther)
    {
        if (Config.DEBUG)
        {
            Log.info("L2PcInstance(" + getName() + "): addCubic(" + id + "|" + level + "|" + matk + ")");
        }
        L2CubicInstance cubic =
                new L2CubicInstance(this, id, level, (int) matk, activationtime, activationchance, maxcount,
                        totalLifetime, givenByOther);

        _cubics.put(id, cubic);
    }

    /**
     * Remove a L2CubicInstance from the L2PcInstance _cubics.<BR><BR>
     */
    public void delCubic(int id)
    {
        _cubics.remove(id);
    }

    /**
     * Return the L2CubicInstance corresponding to the Identifier of the L2PcInstance _cubics.<BR><BR>
     */
    public L2CubicInstance getCubic(int id)
    {
        return _cubics.get(id);
    }

    /**
     * Return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
     */
    public int getEnchantEffect()
    {
        if (getIsWeaponGlowDisabled())
        {
            return 0;
        }

        L2ItemInstance wpn = getActiveWeaponInstance();
        if (wpn == null)
        {
            return 0;
        }

		/*if (Config.isServer(Config.TENKAI))
		{
			int effect = Math.min(12, wpn.getEnchantLevel());
			int[] effectArray = {0, 1, 2, 3, 4, 6, 9, 11, 13, 14, 15, 16, 17};
			return effectArray[effect];
		}*/

        sendSysMessage("Glow = " + Math.min(127, wpn.getEnchantLevel()));
        return Math.min(127, wpn.getEnchantLevel());
    }

    /**
     * Set the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
     */
    public void setLastFolkNPC(L2Npc folkNpc)
    {
        _lastFolkNpc = folkNpc;
    }

    /**
     * Return the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
     */
    public L2Npc getLastFolkNPC()
    {
        return _lastFolkNpc;
    }

    public void addAutoSoulShot(L2ItemInstance item)
    {
        int shotIndex = item.getItem().getShotTypeIndex();
        _activeSoulShots[shotIndex] = item;
        _disabledShoulShots[shotIndex] = false;
    }

    public boolean removeAutoSoulShot(L2ItemInstance item)
    {
        int shotIndex = item.getItem().getShotTypeIndex();
        if (_activeSoulShots[shotIndex] == item)
        {
            _activeSoulShots[shotIndex] = null;
            return true;
        }

        return false;
    }

    public L2ItemInstance getAutoSoulShot(int slot)
    {
        return _activeSoulShots[slot];
    }

    public boolean hasAutoSoulShot(L2ItemInstance item)
    {
        return _activeSoulShots[item.getItem().getShotTypeIndex()] == item;
    }

    public void rechargeAutoSoulShot(boolean physical, boolean magic, boolean summon)
    {
        try
        {
            for (L2ItemInstance item : _activeSoulShots)
            {
                if (item == null || item.getItem().getShotTypeIndex() < 0)
                {
                    continue;
                }

                if (item.getCount() > 0)
                {
                    IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getEtcItem());
                    if (handler != null)
                    {
                        handler.useItem(this, item, false);
                    }
                }
                else
                {
                    removeAutoSoulShot(item);
                }
            }
        }
        catch (NullPointerException npe)
        {
            Log.log(Level.WARNING, toString(), npe);
        }
    }

    /**
     * Cancel autoshot use for shot itemId
     *
     * @param slot int id to disable
     * @return true if canceled.
     */
    public boolean disableAutoShot(int slot)
    {
        if (_activeSoulShots[slot] != null)
        {
            return disableAutoShot(_activeSoulShots[slot]);
        }

        return false;
    }

    /**
     * Cancel autoshot use for shot itemId
     *
     * @param item item to disable
     * @return true if canceled.
     */
    public boolean disableAutoShot(L2ItemInstance item)
    {
        if (hasAutoSoulShot(item))
        {
            int shotIndex = item.getItem().getShotTypeIndex();
            removeAutoSoulShot(item);
            sendPacket(new ExAutoSoulShot(item.getItemId(), 0, shotIndex));

            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
            sm.addString(item.getItem().getName());
            sendPacket(sm);

            _disabledShoulShots[shotIndex] = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Cancel all autoshots for player
     */
    public void disableAutoShotsAll()
    {
        for (int i = 0; i < 4; i++)
        {
            disableAutoShot(i);
            _disabledShoulShots[i] = true;
        }
    }

    public void checkAutoShots()
    {
        if (_activeSoulShots[0] != null && (getActiveWeaponItem() == null ||
                getActiveWeaponItem().getItemGradePlain() != _activeSoulShots[0].getItem().getItemGradePlain()))
        {
            sendPacket(new ExAutoSoulShot(_activeSoulShots[0].getItemId(), 0, 0));
            _activeSoulShots[0] = null;
        }

        if (_activeSoulShots[1] != null && (getActiveWeaponItem() == null ||
                getActiveWeaponItem().getItemGradePlain() != _activeSoulShots[1].getItem().getItemGradePlain()))
        {
            sendPacket(new ExAutoSoulShot(_activeSoulShots[1].getItemId(), 0, 1));
            _activeSoulShots[1] = null;
        }

        for (L2ItemInstance item : getInventory().getItems())
        {
            int shotIndex = item.getItem().getShotTypeIndex();
            if (shotIndex < 0 || _disabledShoulShots[shotIndex])
            {
                continue;
            }

            if (shotIndex < 2 && (getActiveWeaponItem() == null ||
                    getActiveWeaponItem().getItemGradePlain() != item.getItem().getItemGradePlain()))
            {
                //if (shotIndex == 1)
                //	sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
                //else
                //	sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
                continue;
            }

            // Check if there are summons for the beast ss
            if (shotIndex >= 2 && getPet() == null && getSummons().isEmpty())
            {
                //sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_SERVITOR_CANNOT_AUTOMATE_USE));
                continue;
            }

            addAutoSoulShot(item);
            sendPacket(new ExAutoSoulShot(item.getItemId(), 1, shotIndex));

            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
            sm.addItemName(item);// Update Message by rocknow
            sendPacket(sm);

            rechargeAutoSoulShot(true, true, true);
        }
    }

    private ScheduledFuture<?> _taskWarnUserTakeBreak;

    private class WarnUserTakeBreak implements Runnable
    {
        @Override
        public void run()
        {
            if (isOnline())
            {
                SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PLAYING_FOR_LONG_TIME);
                L2PcInstance.this.sendPacket(msg);
            }
            else
            {
                stopWarnUserTakeBreak();
            }
        }
    }

    private class RentPetTask implements Runnable
    {
        @Override
        public void run()
        {
            stopRentPet();
        }
    }

    private class WaterTask implements Runnable
    {
        @Override
        public void run()
        {
            double reduceHp = getMaxHp() / 100.0;

            if (reduceHp < 1)
            {
                reduceHp = 1;
            }

            reduceCurrentHp(reduceHp, L2PcInstance.this, false, false, null);
            //reduced hp, becouse not rest
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DROWN_DAMAGE_S1);
            sm.addNumber((int) reduceHp);
            sendPacket(sm);
        }
    }

    private class LookingForFishTask implements Runnable
    {
        boolean _isNoob, _isUpperGrade;
        int _fishType, _fishGutsCheck;
        long _endTaskTime;

        protected LookingForFishTask(int fishWaitTime, int fishGutsCheck, int fishType, boolean isNoob, boolean isUpperGrade)
        {
            _fishGutsCheck = fishGutsCheck;
            _endTaskTime = System.currentTimeMillis() + fishWaitTime + 10000;
            _fishType = fishType;
            _isNoob = isNoob;
            _isUpperGrade = isUpperGrade;
        }

        @Override
        public void run()
        {
            if (System.currentTimeMillis() >= _endTaskTime)
            {
                endFishing(false);
                return;
            }
            if (_fishType == -1)
            {
                return;
            }
            int check = Rnd.get(1000);
            if (_fishGutsCheck > check)
            {
                stopLookingForFishTask();
                startFishCombat(_isNoob, _isUpperGrade);
            }
        }
    }

    public int getClanPrivileges()
    {
        return _clanPrivileges;
    }

    public void setClanPrivileges(int n)
    {
        _clanPrivileges = n;
    }

    // baron etc
    public void setPledgeClass(int classId)
    {
        _pledgeClass = classId;
        checkItemRestriction();
    }

    public int getPledgeClass()
    {
        return _pledgeClass;
    }

    public void setPledgeType(int typeId)
    {
        _pledgeType = typeId;
    }

    public int getPledgeType()
    {
        return _pledgeType;
    }

    public int getApprentice()
    {
        return _apprentice;
    }

    public void setApprentice(int apprentice_id)
    {
        _apprentice = apprentice_id;
    }

    public int getSponsor()
    {
        return _sponsor;
    }

    public void setSponsor(int sponsor_id)
    {
        _sponsor = sponsor_id;
    }

    public int getBookMarkSlot()
    {
        return _bookmarkslot;
    }

    public void setBookMarkSlot(int slot)
    {
        _bookmarkslot = slot;
        sendPacket(new ExGetBookMarkInfoPacket(this));
    }

    public L2ContactList getContactList()
    {
        return _contactList;
    }

    @Override
    public void sendMessage(String message)
    {
        sendPacket(SystemMessage.sendString(message));
    }

    public final void sendSysMessage(final String message)
    {
        if (!isGM())
        {
            return;
        }

        sendPacket(new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "", message));
    }

    public void enterObserverMode(int x, int y, int z)
    {
        _lastX = getX();
        _lastY = getY();
        _lastZ = getZ();

        _observerMode = true;
        setTarget(null);
        setIsParalyzed(true);
        startParalyze();
        setIsInvul(true);
        getAppearance().setInvisible();
        //sendPacket(new GMHide(1));
        sendPacket(new ObservationMode(x, y, z));
        getKnownList().removeAllKnownObjects(); // reinit knownlist
        setXYZ(x, y, z);
        broadcastUserInfo();
    }

    public void setLastCords(int x, int y, int z)
    {
        _lastX = getX();
        _lastY = getY();
        _lastZ = getZ();
    }

    public void enterOlympiadObserverMode(Location loc, int id)
    {
        if (getPet() != null)
        {
            getPet().unSummon(this);
        }
        for (L2SummonInstance summon : getSummons())
        {
            summon.unSummon(this);
        }

        if (!getCubics().isEmpty())
        {
            for (L2CubicInstance cubic : getCubics().values())
            {
                cubic.stopAction();
                cubic.cancelDisappear();
            }

            getCubics().clear();
        }

        if (getParty() != null)
        {
            getParty().removePartyMember(this, messageType.Expelled);
        }

        _olympiadGameId = id;
        if (isSitting())
        {
            standUp();
        }
        if (!_observerMode)
        {
            _lastX = getX();
            _lastY = getY();
            _lastZ = getZ();
        }

        _observerMode = true;
        setTarget(null);
        setIsInvul(true);
        getAppearance().setInvisible();
        setInstanceId(id + Olympiad.BASE_INSTANCE_ID);
        //sendPacket(new GMHide(1));
        teleToLocation(loc, false);
        sendPacket(new ExOlympiadMode(3));

        broadcastUserInfo();
    }

    public void leaveObserverMode()
    {
        setTarget(null);
        getKnownList().removeAllKnownObjects(); // reinit knownlist
        setXYZ(_lastX, _lastY, _lastZ);
        setIsParalyzed(false);
        stopParalyze(false);
        //sendPacket(new GMHide(0));
        if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invis", getAccessLevel()))
        {
            getAppearance().setVisible();
        }
        if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invul", getAccessLevel()))
        {
            setIsInvul(false);
        }
        if (getAI() != null)
        {
            getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }

        setFalling(); // prevent receive falling damage
        _observerMode = false;
        setLastCords(0, 0, 0);
        sendPacket(new ObservationReturn(this));
        broadcastUserInfo();
    }

    public void leaveOlympiadObserverMode()
    {
        if (_olympiadGameId == -1)
        {
            return;
        }
        _olympiadGameId = -1;
        _observerMode = false;
        setTarget(null);
        sendPacket(new ExOlympiadMode(0));
        setInstanceId(0);
        teleToLocation(_lastX, _lastY, _lastZ, true);
        //sendPacket(new GMHide(0));
        if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invis", getAccessLevel()))
        {
            getAppearance().setVisible();
        }
        if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invul", getAccessLevel()))
        {
            setIsInvul(false);
        }
        if (getAI() != null)
        {
            getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }
        setLastCords(0, 0, 0);
        broadcastUserInfo();
    }

    public void setOlympiadSide(int i)
    {
        _olympiadSide = i;
    }

    public int getOlympiadSide()
    {
        return _olympiadSide;
    }

    public void setOlympiadGameId(int id)
    {
        _olympiadGameId = id;
    }

    public int getOlympiadGameId()
    {
        return _olympiadGameId;
    }

    public int getLastX()
    {
        return _lastX;
    }

    public int getLastY()
    {
        return _lastY;
    }

    public int getLastZ()
    {
        return _lastZ;
    }

    public boolean inObserverMode()
    {
        return _observerMode;
    }

    public int getTeleMode()
    {
        return _telemode;
    }

    public void setTeleMode(int mode)
    {
        _telemode = mode;
    }

    public void setLoto(int i, int val)
    {
        _loto[i] = val;
    }

    public int getLoto(int i)
    {
        return _loto[i];
    }

    public void setRace(int i, int val)
    {
        _race[i] = val;
    }

    public int getRace(int i)
    {
        return _race[i];
    }

    public boolean getMessageRefusal()
    {
        return _messageRefusal;
    }

    public void setMessageRefusal(boolean mode)
    {
        _messageRefusal = mode;
        sendPacket(new EtcStatusUpdate(this));
    }

    public void setDietMode(boolean mode)
    {
        _dietMode = mode;
    }

    public boolean getDietMode()
    {
        return _dietMode;
    }

    public void setTradeRefusal(boolean mode)
    {
        _tradeRefusal = mode;
    }

    public boolean getTradeRefusal()
    {
        return _tradeRefusal;
    }

    public void setExchangeRefusal(boolean mode)
    {
        _exchangeRefusal = mode;
    }

    public boolean getExchangeRefusal()
    {
        return _exchangeRefusal;
    }

    public BlockList getBlockList()
    {
        return _blockList;
    }

    public void setHero(boolean hero)
    {
        if (hero && _baseClass == _activeClass)
        {
            broadcastPacket(new SocialAction(getObjectId(), 20016));
            for (L2Skill s : HeroSkillTable.getHeroSkills())
            {
                addSkill(s, false); //Dont Save Hero skills to database
            }
        }
        else
        {
            for (L2Skill s : HeroSkillTable.getHeroSkills())
            {
                super.removeSkill(s); //Just Remove skills from nonHero characters
            }
        }
        _hero = hero;

        sendSkillList();
    }

    public void setIsInOlympiadMode(boolean b)
    {
        _inOlympiadMode = b;
    }

    public void setIsOlympiadStart(boolean b)
    {
        _OlympiadStart = b;
    }

    public boolean isOlympiadStart()
    {
        return _OlympiadStart;
    }

    public boolean isHero()
    {
        return _hero;
    }

    public void setHasCoCAura(boolean b)
    {
        _isCoCWinner = b;
    }

    public boolean hasCoCAura()
    {
        return _isCoCWinner;
    }

    public boolean hasHeroAura()
    {
        if (isPlayingEvent())
        {
            return _event.isType(EventType.CaptureTheFlag) && _ctfFlag != null ||
                    _event.isType(EventType.VIP) && _event.getParticipantTeam(getObjectId()) != null &&
                            _event.getParticipantTeam(getObjectId()).getVIP() != null &&
                            _event.getParticipantTeam(getObjectId()).getVIP().getObjectId() == getObjectId();
        }

        return isHero() || isGM() && Config.GM_HERO_AURA;
    }

    public boolean isInOlympiadMode()
    {
        return _inOlympiadMode;
    }

    public boolean isInDuel()
    {
        return _isInDuel;
    }

    public int getDuelId()
    {
        return _duelId;
    }

    public void setDuelState(int mode)
    {
        _duelState = mode;
    }

    public int getDuelState()
    {
        return _duelState;
    }

    /**
     * Sets up the duel state using a non 0 duelId.
     *
     * @param duelId 0=not in a duel
     */
    public void setIsInDuel(int duelId)
    {
        if (duelId > 0)
        {
            _isInDuel = true;
            _duelState = Duel.DUELSTATE_DUELLING;
            _duelId = duelId;
        }
        else
        {
            if (_duelState == Duel.DUELSTATE_DEAD)
            {
                enableAllSkills();
                getStatus().startHpMpRegeneration();
            }
            _isInDuel = false;
            _duelState = Duel.DUELSTATE_NODUEL;
            _duelId = 0;
        }
    }

    /**
     * This returns a SystemMessage stating why
     * the player is not available for duelling.
     *
     * @return S1_CANNOT_DUEL... message
     */
    public SystemMessage getNoDuelReason()
    {
        SystemMessage sm = SystemMessage.getSystemMessage(_noDuelReason);
        sm.addPcName(this);
        _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
        return sm;
    }

    /**
     * Checks if this player might join / start a duel.
     * To get the reason use getNoDuelReason() after calling this function.
     *
     * @return true if the player might join/start a duel.
     */
    public boolean canDuel()
    {
        if (isInCombat() || getPunishLevel() == PunishLevel.JAIL)
        {
            _noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
            return false;
        }
        if (isDead() || isAlikeDead() || getCurrentHp() < getMaxHp() / 2 || getCurrentMp() < getMaxMp() / 2)
        {
            _noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT;
            return false;
        }
        if (isInDuel())
        {
            _noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL;
            return false;
        }
        if (isInOlympiadMode())
        {
            _noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
            return false;
        }
        if (isCursedWeaponEquipped())
        {
            _noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE;
            return false;
        }
        if (getPrivateStoreType() != STORE_PRIVATE_NONE)
        {
            _noDuelReason =
                    SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
            return false;
        }
        if (isMounted() || isInBoat())
        {
            _noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
            return false;
        }
        if (isFishing())
        {
            _noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING;
            return false;
        }
        if (isInsideZone(ZONE_PVP) || isInsideZone(ZONE_PEACE) || isInsideZone(ZONE_SIEGE))
        {
            _noDuelReason =
                    SystemMessageId.C1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
            return false;
        }
        return true;
    }

    public boolean isNoble()
    {
        return _noble;
    }

    public void setNoble(boolean val)
    {
        if (Config.IS_CLASSIC)
        {
            return;
        }

        // On Khadia people are noble from the very beginning
        if (Config.isServer(Config.TENKAI))
        {
            val = true;
        }
        if (val)
        {
            for (L2Skill s : NobleSkillTable.getInstance().getNobleSkills())
            {
                addSkill(s, false); //Dont Save Noble skills to Sql
            }
        }
        else
        {
            for (L2Skill s : NobleSkillTable.getInstance().getNobleSkills())
            {
                super.removeSkill(s); //Just Remove skills without deleting from Sql
            }
        }
        _noble = val;

        sendSkillList();
    }

    public void setLvlJoinedAcademy(int lvl)
    {
        _lvlJoinedAcademy = lvl;
    }

    public int getLvlJoinedAcademy()
    {
        return _lvlJoinedAcademy;
    }

    public boolean isAcademyMember()
    {
        return _lvlJoinedAcademy > 0;
    }

    public void setTeam(int team)
    {
        _team = team;
        if (getPet() != null)
        {
            getPet().broadcastStatusUpdate();
        }
        for (L2SummonInstance summon : getSummons())
        {
            summon.broadcastStatusUpdate();
        }
    }

    public int getTeam()
    {
        return _team;
    }

    public void setWantsPeace(int wantsPeace)
    {
        _wantsPeace = wantsPeace;
    }

    public int getWantsPeace()
    {
        return _wantsPeace;
    }

    public boolean isFishing()
    {
        return _fishing;
    }

    public void setFishing(boolean fishing)
    {
        _fishing = fishing;
    }

    public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
    {
        // [-5,-1] varka, 0 neutral, [1,5] ketra
        _alliedVarkaKetra = sideAndLvlOfAlliance;
    }

    public int getAllianceWithVarkaKetra()
    {
        return _alliedVarkaKetra;
    }

    public boolean isAlliedWithVarka()
    {
        return _alliedVarkaKetra < 0;
    }

    public boolean isAlliedWithKetra()
    {
        return _alliedVarkaKetra > 0;
    }

    public void sendSkillList()
    {
        sendSkillList(this);
    }

    public void sendSkillList(L2PcInstance player)
    {
        boolean isDisabled = false;
        SkillList sl = new SkillList();
        if (player != null)
        {
            for (L2Skill s : player.getAllSkills())
            {
                if (s == null)
                {
                    continue;
                }
                if (s.getId() > 9000 && s.getId() < 9007)
                {
                    continue; // Fake skills to change base stats
                }
                if (_transformation != null && !containsAllowedTransformSkill(s.getId()) && !s.allowOnTransform())
                {
                    int[] specialTransformationSkillIds = {11543, 11540, 11541, 11537, 11580};

                    for (int skillId : specialTransformationSkillIds)
                    {
                        if (getFirstEffect(skillId) == null)
                        {
                            continue;
                        }

                        isDisabled = true;
                        break;
                    }

                    if (!isDisabled)
                    {
                        continue;
                    }
                }
                else if (player.getClan() != null)
                {
                    isDisabled = s.isClanSkill() && player.getClan().getReputationScore() < 0;
                }

                boolean isEnchantable = SkillTable.getInstance().isEnchantable(s.getId());
                if (isEnchantable)
                {
                    L2EnchantSkillLearn esl = EnchantCostsTable.getInstance().getSkillEnchantmentBySkillId(s.getId());
                    if (esl != null)
                    {
                        //if player dont have min level to enchant
                        if (s.getLevelHash() < esl.getBaseLevel())
                        {
                            isEnchantable = false;
                        }
                    }
                    // if no enchant data
                    else
                    {
                        isEnchantable = false;
                    }
                }

                //sendSysMessage(s.getName() + " -- " + s.getLevel() + " -- " + s.getEnchantRouteId() + " -- " + s.getEnchantLevel());
                sl.addSkill(s.getId(), s.getLevelHash(), s.getReuseHashCode(), s.isPassive(), isDisabled,
                        isEnchantable);
            }
        }

        sendPacket(sl);

        sendPacket(new AcquireSkillList(this));
    }

    /**
     * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>)
     * for this character.<BR>
     * 2. This method no longer changes the active _classIndex of the player. This is only
     * done by the calling of setActiveClass() method as that should be the only way to do so.
     *
     * @param classId
     * @param classIndex
     * @return boolean subclassAdded
     */
    public boolean addSubClass(int classId, int classIndex, int certsCount)
    {
        if (!_subclassLock.tryLock())
        {
            return false;
        }

        try
        {
            int maxSubs = Config.MAX_SUBCLASS;
            if (getRace() == Race.Ertheia)
            {
                maxSubs = 1;
            }

            if (getTotalSubClasses() == maxSubs || classIndex == 0)
            {
                return false;
            }

            if (getSubClasses().containsKey(classIndex))
            {
                return false;
            }

            // Note: Never change _classIndex in any method other than setActiveClass().

            SubClass newClass = new SubClass();
            newClass.setClassId(classId);
            newClass.setClassIndex(classIndex);
            newClass.setCertificates(certsCount);
            if (getRace() == Race.Ertheia)
            {
                newClass.setIsDual(true);
                byte level = 85;
                if (Config.STARTING_LEVEL > 85)
                {
                    level = Config.STARTING_LEVEL;
                }
                newClass.setLevel(level);
                newClass.setExp(Experience.getAbsoluteExp(level));
            }

            Connection con = null;
            try
            {
                // Store the basic info about this new sub-class.
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);

                statement.setInt(1, getObjectId());
                statement.setInt(2, newClass.getClassId());
                statement.setLong(3, newClass.getExp());
                statement.setLong(4, newClass.getSp());
                statement.setInt(5, newClass.getLevel());
                statement.setInt(6, newClass.getClassIndex());
                statement.setInt(7, newClass.getCertificates());

                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING,
                        "WARNING: Could not add character sub class for " + getName() + ": " + e.getMessage(), e);
                return false;
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }

            // Commit after database INSERT incase exception is thrown.
            getSubClasses().put(newClass.getClassIndex(), newClass);

            if (Config.DEBUG)
            {
                Log.info(getName() + " added class ID " + classId + " as a sub class at index " + classIndex + ".");
            }

            Collection<L2SkillLearn> skillTree =
                    PlayerClassTable.getInstance().getClassById(classId).getSkills().values();
            if (skillTree == null)
            {
                return true;
            }

            Map<Integer, L2Skill> prevSkillList = new HashMap<>();
            for (L2SkillLearn skillInfo : skillTree)
            {
                if (skillInfo.getMinLevel() <= 40)
                {
                    L2Skill prevSkill = prevSkillList.get(skillInfo.getId());
                    L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());

                    if (prevSkill != null && prevSkill.getLevelHash() > newSkill.getLevelHash())
                    {
                        continue;
                    }

                    prevSkillList.put(newSkill.getId(), newSkill);
                    storeSkill(newSkill, prevSkill, classIndex);
                }
            }

            if (Config.DEBUG)
            {
                Log.info(getName() + " was given " + getAllSkills().length + " skills for their new sub class.");
            }

            return true;
        }
        finally
        {
            _subclassLock.unlock();
        }
    }

    /**
     * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
     * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
     * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
     *
     * @param classIndex
     * @param newClassId
     * @return boolean subclassAdded
     */
    public boolean modifySubClass(int classIndex, int newClassId)
    {
        if (!_subclassLock.tryLock())
        {
            return false;
        }

        int certsCount = 0;
        try
        {
            SubClass oldSub = getSubClasses().get(classIndex);
            int oldClassId = oldSub.getClassId();

            certsCount = oldSub.getCertificates();

            if (Config.DEBUG)
            {
                Log.info(getName() + " has requested to modify sub class index " + classIndex + " from class ID " +
                        oldClassId + " to " + newClassId + ".");
            }

            Connection con = null;
            PreparedStatement statement = null;

            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();

                // Remove all henna info stored for this sub-class.
                statement = con.prepareStatement(DELETE_CHAR_HENNAS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();

                // Remove all shortcuts info stored for this sub-class.
                statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();

                // Remove all effects info stored for this sub-class.
                statement = con.prepareStatement(DELETE_SKILL_SAVE);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();

                // Remove all skill info stored for this sub-class.
                statement = con.prepareStatement(DELETE_CHAR_SKILLS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();

                // Remove all basic info stored about this sub-class.
                statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING,
                        "Could not modify sub class for " + getName() + " to class index " + classIndex + ": " +
                                e.getMessage(), e);

                // This must be done in order to maintain data consistency.
                getSubClasses().remove(classIndex);
                return false;
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }

            getSubClasses().remove(classIndex);
        }
        finally
        {
            _subclassLock.unlock();
        }

        return addSubClass(newClassId, classIndex, certsCount);
    }

    public boolean isSubClassActive()
    {
        return _classIndex > 0;
    }

    public Map<Integer, SubClass> getSubClasses()
    {
        if (_subClasses == null)
        {
            _subClasses = new HashMap<>();
        }

        return _subClasses;
    }

    public int getTotalSubClasses()
    {
        return getSubClasses().size();
    }

    public int getBaseClass()
    {
        return _baseClass;
    }

    public int getBaseClassLevel()
    {
        return getStat().getBaseClassLevel();
    }

    public int getActiveClass()
    {
        if (_temporaryClassId != 0)
        {
            return _temporaryClassId;
        }

        return _activeClass;
    }

    public int getClassIndex()
    {
        return _classIndex;
    }

    public void setClassTemplate(int classId)
    {
        _activeClass = classId;

        PlayerClass cl = PlayerClassTable.getInstance().getClassById(classId);

        if (cl == null)
        {
            Log.severe("Missing template for classId: " + classId);
            throw new Error();
        }
        _currentClass = cl;

        if (_classIndex == 0 && cl.getRace() != null)
        {
            _templateId = cl.getRace().ordinal() * 2 + (cl.isMage() ? 1 : 0);
        }

        // Set the template of the L2PcInstance
        setTemplate(CharTemplateTable.getInstance().getTemplate(_templateId / 2 * 2 + (cl.isMage() ? 1 : 0)));
    }

    public PlayerClass getCurrentClass()
    {
        if (_temporaryPlayerClass != null)
        {
            return _temporaryPlayerClass;
        }

        return _currentClass;
    }

    /**
     * Changes the character's class based on the given class index.
     * <BR><BR>
     * An index of zero specifies the character's original (base) class,
     * while indexes 1-3 specifies the character's sub-classes respectively.
     * <br><br>
     * <font color="00FF00">WARNING: Use only on subclase change</font>
     *
     * @param classIndex
     */
    public boolean setActiveClass(int classIndex)
    {
        if (!_subclassLock.tryLock())
        {
            return false;
        }

        try
        {
            // Cannot switch or change subclasses while transformed
            if (_transformation != null)
            {
                return false;
            }

            // Remove active item skills before saving char to database
            // because next time when choosing this class, weared items can
            // be different
            for (L2ItemInstance item : getInventory().getAugmentedItems())
            {
                if (item != null && item.isEquipped())
                {
                    item.getAugmentation().removeBonus(this);
                }
            }

            // abort any kind of cast.
            abortCast();

            // Stop casting for any player that may be casting a force buff on this l2pcinstance.
            for (L2Character character : getKnownList().getKnownCharacters())
            {
                if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
                {
                    character.abortCast();
                }
            }

			/*
			 * 1. Call store() before modifying _classIndex to avoid skill effects rollover.
			 * 2. Register the correct _classId against applied 'classIndex'.
			 */
            store(Config.SUBCLASS_STORE_SKILL_COOLTIME);
            _reuseTimeStamps.clear();

            // clear charges
            _charges.set(0);
            stopChargeTask();

            if (classIndex != 0 && getSubClasses().get(classIndex) == null)
            {
                Log.warning("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": ");
                return false;
            }

            _classIndex = classIndex;
            if (classIndex == 0)
            {
                setClassTemplate(getBaseClass());
            }
            else
            {
                setClassTemplate(getSubClasses().get(classIndex).getClassId());
            }

            if (isInParty())
            {
                getParty().recalculatePartyLevel();
            }

			/*
			 * Update the character's change in class status.
			 *
			 * 1. Remove any active cubics from the player.
			 * 2. Renovate the characters table in the database with the new class info, storing also buff/effect data.
			 * 3. Remove all existing skills.
			 * 4. Restore all the learned skills for the current class from the database.
			 * 5. Restore effect/buff data for the new class.
			 * 6. Restore henna data for the class, applying the new stat modifiers while removing existing ones.
			 * 7. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes.
			 * 8. Restore shortcut data related to this class.
			 * 9. Resend a class change animation effect to broadcast to all nearby players.
			 * 10.Unsummon any active servitor from the player.
			 */

            for (L2SummonInstance summon : getSummons())
            {
                summon.unSummon(this);
            }

            for (L2Skill oldSkill : getAllSkills())
            {
                super.removeSkill(oldSkill);
            }

            stopAllEffectsExceptThoseThatLastThroughDeath();
            stopCubics();

            restoreRecipeBook(false);

            restoreSkills();
            rewardSkills();
            regiveTemporarySkills();

            // Prevents some issues when changing between subclases that shares skills
            if (_disabledSkills != null && !_disabledSkills.isEmpty())
            {
                _disabledSkills.clear();
            }

            restoreEffects();
            updateEffectIcons();
            sendPacket(new EtcStatusUpdate(this));

            // if player has quest 422: Repent Your Sins, remove it
            QuestState st = getQuestState("422_RepentYourSins");
            if (st != null)
            {
                st.exitQuest(true);
            }

            for (int i = 0; i < 3; i++)
            {
                _henna[i] = null;
            }

            restoreHenna();
            sendPacket(new HennaInfo(this));

            restoreAbilities();

            if (getCurrentHp() > getMaxHp())
            {
                setCurrentHp(getMaxHp());
            }
            if (getCurrentMp() > getMaxMp())
            {
                setCurrentMp(getMaxMp());
            }
            if (getCurrentCp() > getMaxCp())
            {
                setCurrentCp(getMaxCp());
            }

            refreshOverloaded();
            refreshExpertisePenalty();
            broadcastUserInfo();

            // Clear resurrect xp calculation
            setExpBeforeDeath(0);

            _shortCuts.restore();
            sendPacket(new ShortCutInit(this));

            broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
            sendPacket(new SkillCoolTime(this));
            sendPacket(new ExStorageMaxCount(this));

            SkillTable.getInstance().getInfo(1570, 1).getEffects(this, this); // Identity crisis buff Id -> 1570

            setHasIdentityCrisis(true);

            if (classIndex != 0 && _certificationSkills.size() != 0)
            {
                for (L2Skill skill : _certificationSkills.values())
                {
                    addSkill(skill, false);
                }
            }

            if (getClan() != null)
            {
                getClan().broadcastClanStatus();
            }

            return true;
        }
        finally
        {
            _subclassLock.unlock();
        }
    }

    public boolean isLocked()
    {
        return _subclassLock.isLocked();
    }

    public void stopWarnUserTakeBreak()
    {
        if (_taskWarnUserTakeBreak != null)
        {
            _taskWarnUserTakeBreak.cancel(true);
            //ThreadPoolManager.getInstance().removeGeneral((Runnable)_taskWarnUserTakeBreak);
            _taskWarnUserTakeBreak = null;
        }
    }

    public void startWarnUserTakeBreak()
    {
        if (_taskWarnUserTakeBreak == null)
        {
            _taskWarnUserTakeBreak = ThreadPoolManager.getInstance()
                    .scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
        }
    }

    public void stopRentPet()
    {
        if (_taskRentPet != null)
        {
            // if the rent of a wyvern expires while over a flying zone, tp to down before unmounting
            if (checkLandingState() && getMountType() == 2)
            {
                teleToLocation(MapRegionTable.TeleportWhereType.Town);
            }

            if (dismount()) // this should always be true now, since we teleported already
            {
                _taskRentPet.cancel(true);
                _taskRentPet = null;
            }
        }
    }

    public void startRentPet(int seconds)
    {
        if (_taskRentPet == null)
        {
            _taskRentPet = ThreadPoolManager.getInstance()
                    .scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000L);
        }
    }

    public boolean isRentedPet()
    {
        if (_taskRentPet != null)
        {
            return true;
        }

        return false;
    }

    public void stopWaterTask()
    {
        if (_taskWater != null)
        {
            _taskWater.cancel(false);

            _taskWater = null;
            sendPacket(new SetupGauge(2, 0));
        }
    }

    public void startWaterTask()
    {
        if (!isDead() && _taskWater == null)
        {
            int timeinwater = (int) calcStat(Stats.BREATH, 60000, this, null);

            sendPacket(new SetupGauge(2, timeinwater));
            _taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000);
        }
    }

    public boolean isInWater()
    {
        if (_taskWater != null)
        {
            return true;
        }

        return false;
    }

    public void checkWaterState()
    {
        if (isInsideZone(ZONE_WATER))
        {
            startWaterTask();
        }
        else
        {
            stopWaterTask();
        }
    }

    public void onPlayerEnter()
    {
        startWarnUserTakeBreak();

        // jail task
        updatePunishState();

        if (isGM())
        {
            if (isInvul())
            {
                sendMessage("Entering world in Invulnerable mode.");
            }
            if (getAppearance().getInvisible())
            {
                sendMessage("Entering world in Invisible mode.");
            }
            if (isSilenceMode())
            {
                sendMessage("Entering world in Silence mode.");
            }
        }

        revalidateZone(true);

        notifyFriends();
        if (!isGM() && Config.DECREASE_SKILL_LEVEL)
        {
            checkPlayerSkills();
        }
    }

    public long getLastAccess()
    {
        return _lastAccess;
    }

    @Override
    public void doRevive()
    {
        super.doRevive();
        stopEffects(L2EffectType.CHARMOFCOURAGE);
        updateEffectIcons();
        sendPacket(new EtcStatusUpdate(this));
        _reviveRequested = false;
        _revivePower = 0;

        if (isMounted())
        {
            startFeed(_mountNpcId);
        }
    }

    @Override
    public void doRevive(double revivePower)
    {
        // Restore the player's lost experience,
        // depending on the % return of the skill used (based on its power).
        restoreExp(revivePower);
        doRevive();
    }

    public void reviveRequest(L2PcInstance reviver, L2Skill skill, boolean Pet)
    {
        if (isResurrectionBlocked())
        {
            return;
        }

        if (_reviveRequested)
        {
            if (_revivePet == Pet)
            {
                reviver.sendPacket(SystemMessage.getSystemMessage(
                        SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
            }
            else
            {
                if (Pet)
                {
                    reviver.sendPacket(SystemMessage.getSystemMessage(
                            SystemMessageId.CANNOT_RES_PET2)); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
                }
                else
                {
                    reviver.sendPacket(SystemMessage.getSystemMessage(
                            SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
                }
            }
            return;
        }
        if (Pet && getPet() != null && getPet().isDead() || !Pet && isDead())
        {
            _reviveRequested = true;
            int restoreExp = 0;
            if (isPhoenixBlessed())
            {
                _revivePower = 100;
            }
            else if (isAffected(L2EffectType.CHARMOFCOURAGE.getMask()))
            {
                _revivePower = 0;
            }
            else
            {
                _revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), reviver);
            }

            restoreExp = (int) Math.round((getExpBeforeDeath() - getExp()) * _revivePower / 100);

            _revivePet = Pet;

            if (isAffected(L2EffectType.CHARMOFCOURAGE.getMask()))
            {
                ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE.getId());
                dlg.addTime(60000);
                sendPacket(dlg);
                return;
            }
            ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_XP.getId());
            dlg.addPcName(reviver);
            dlg.addString(String.valueOf(restoreExp));
            sendPacket(dlg);
        }

        if (this instanceof L2ApInstance)
        {
            reviveAnswer(1);
        }
    }

    public void reviveAnswer(int answer)
    {
        if (!_reviveRequested || !isDead() && !_revivePet || _revivePet && getPet() != null && !getPet().isDead())
        {
            return;
        }
        //If character refuses a PhoenixBless autoress, cancel all buffs he had
        if (answer == 0 && isPhoenixBlessed())
        {
            stopPhoenixBlessing(null);
            stopAllEffectsExceptThoseThatLastThroughDeath();
        }
        if (answer == 1)
        {
            if (!_revivePet)
            {
                if (_revivePower != 0)
                {
                    doRevive(_revivePower);
                }
                else
                {
                    doRevive();
                }
            }
            else if (getPet() != null)
            {
                if (_revivePower != 0)
                {
                    getPet().doRevive(_revivePower);
                }
                else
                {
                    getPet().doRevive();
                }
            }
        }
        _reviveRequested = false;
        _revivePower = 0;
    }

    public boolean isReviveRequested()
    {
        return _reviveRequested;
    }

    public boolean isRevivingPet()
    {
        return _revivePet;
    }

    public void removeReviving()
    {
        _reviveRequested = false;
        _revivePower = 0;
    }

    public void onActionRequest()
    {
        if (isSpawnProtected())
        {
            sendPacket(SystemMessage
                    .getSystemMessage(SystemMessageId.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS));
        }
        if (isTeleportProtected())
        {
            sendMessage("Teleport spawn protection ended.");
        }
        setProtection(false);
        setTeleportProtection(false);
        if (!_lastSummons.isEmpty())
        {
            summonLastSummons();
        }
        _hasMoved = true;
    }

    /**
     * @param expertiseIndex The expertiseIndex to set.
     */
    public void setExpertiseIndex(int expertiseIndex)
    {
        _expertiseIndex = expertiseIndex;
    }

    /**
     * @return Returns the expertiseIndex.
     */
    public int getExpertiseIndex()
    {
        return _expertiseIndex;
    }

    @Override
    public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
    {
        if (getVehicle() != null && !getVehicle().isTeleporting())
        {
            setVehicle(null);
        }

        if (isFlyingMounted() && z < -1005)
        {
            z = -1005;
        }

        super.teleToLocation(x, y, z, heading, allowRandomOffset);
    }

    @Override
    public final void onTeleported()
    {
        super.onTeleported();

        if (isInAirShip())
        {
            getAirShip().sendInfo(this);
        }

        // Force a revalidation
        revalidateZone(true);

        // Prevent stuck-in-bird-transformation bug
        if (isInsideZone(ZONE_PEACE) && isTransformed() && isFlyingMounted() &&
                MapRegionTable.getInstance().getClosestTownNumber(this) < 32) // Not in Gracia
        {
            unTransform(true);
        }

        checkItemRestriction();

        if (Config.PLAYER_TELEPORT_PROTECTION > 0 && !isInOlympiadMode())
        {
            setTeleportProtection(true);
        }

        // Trained beast is after teleport lost
        if (getTrainedBeasts() != null)
        {
            for (L2TamedBeastInstance tamedBeast : getTrainedBeasts())
            {
                tamedBeast.deleteMe();
            }
            getTrainedBeasts().clear();
        }

        // Modify the position of the pet if necessary
        L2Summon pet = getPet();
        if (pet != null)
        {
            pet.setFollowStatus(false);
            pet.teleToLocation(getPosition().getX(), getPosition().getY(), getPosition().getZ(), false);
            ((L2SummonAI) pet.getAI()).setStartFollowController(true);
            pet.setFollowStatus(true);
            pet.updateAndBroadcastStatus(0);
        }
        for (L2SummonInstance summon : getSummons())
        {
            summon.setFollowStatus(false);
            summon.teleToLocation(getPosition().getX(), getPosition().getY(), getPosition().getZ(), false);
            ((L2SummonAI) summon.getAI()).setStartFollowController(true);
            summon.setFollowStatus(true);
        }
        for (L2SummonInstance summon : getSummons())
        {
            summon.updateAndBroadcastStatus(0);
        }

        if (isPerformingFlyMove())
        {
            setFlyMove(null);
        }

        onEventTeleported();
    }

    @Override
    public void setIsTeleporting(boolean teleport)
    {
        setIsTeleporting(teleport, true);
    }

    public void setIsTeleporting(boolean teleport, boolean useWatchDog)
    {
        super.setIsTeleporting(teleport);
        if (!useWatchDog)
        {
            return;
        }
        if (teleport)
        {
            if (_teleportWatchdog == null && Config.TELEPORT_WATCHDOG_TIMEOUT > 0)
            {
                _teleportWatchdog = ThreadPoolManager.getInstance()
                        .scheduleGeneral(new TeleportWatchdog(), Config.TELEPORT_WATCHDOG_TIMEOUT * 1000);
            }
        }
        else
        {
            if (_teleportWatchdog != null)
            {
                _teleportWatchdog.cancel(false);
                _teleportWatchdog = null;
            }
        }
    }

    private class TeleportWatchdog implements Runnable
    {
        private final L2PcInstance _player;

        TeleportWatchdog()
        {
            _player = L2PcInstance.this;
        }

        @Override
        public void run()
        {
            if (_player == null || !_player.isTeleporting())
            {
                return;
            }

            if (Config.DEBUG)
            {
                Log.warning("Player " + _player.getName() + " teleport timeout expired");
            }
            _player.onTeleported();
        }
    }

    public void setLastServerPosition(int x, int y, int z)
    {
        _lastServerPosition.setXYZ(x, y, z);
    }

    public Point3D getLastServerPosition()
    {
        return _lastServerPosition;
    }

    public boolean checkLastServerPosition(int x, int y, int z)
    {
        return _lastServerPosition.equals(x, y, z);
    }

    public int getLastServerDistance(int x, int y, int z)
    {
        double dx = x - _lastServerPosition.getX();
        double dy = y - _lastServerPosition.getY();
        double dz = z - _lastServerPosition.getZ();

        return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public void addExpAndSp(long addToExp, long addToSp)
    {
        getStat().addExpAndSp(addToExp, addToSp, false);
    }

    public void addExpAndSp(long addToExp, long addToSp, boolean useVitality)
    {
        getStat().addExpAndSp(addToExp, addToSp, useVitality);
    }

    public void removeExpAndSp(long removeExp, long removeSp)
    {
        getStat().removeExpAndSp(removeExp, removeSp, true);
    }

    public void removeExpAndSp(long removeExp, long removeSp, boolean sendMessage)
    {
        getStat().removeExpAndSp(removeExp, removeSp, sendMessage);
    }

    @Override
    public void reduceCurrentHp(double value, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
    {
        if (skill != null)
        {
            getStatus().reduceHp(value, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP(),
                    skill.ignoreImmunity());
        }
        else
        {
            getStatus().reduceHp(value, attacker, awake, isDOT, false, false, false);
        }

        // notify the tamed beast of attacks
        if (getTrainedBeasts() != null)
        {
            for (L2TamedBeastInstance tamedBeast : getTrainedBeasts())
            {
                tamedBeast.onOwnerGotAttacked(attacker);
            }
        }
    }

    public void broadcastSnoop(int type, String name, String _text)
    {
        if (!_snoopListener.isEmpty())
        {
            Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);

            for (L2PcInstance pci : _snoopListener)
            {
                if (pci != null)
                {
                    pci.sendPacket(sn);
                }
            }
        }
    }

    public void addSnooper(L2PcInstance pci)
    {
        if (!_snoopListener.contains(pci))
        {
            _snoopListener.add(pci);
        }
    }

    public void removeSnooper(L2PcInstance pci)
    {
        _snoopListener.remove(pci);
    }

    public void addSnooped(L2PcInstance pci)
    {
        if (!_snoopedPlayer.contains(pci))
        {
            _snoopedPlayer.add(pci);
        }
    }

    public void removeSnooped(L2PcInstance pci)
    {
        _snoopedPlayer.remove(pci);
    }

    /**
     * Tenkai custom - store instance reference when GM observes land rates of this instance
     */
    private void addLandrateObserver(L2PcInstance pci)
    {
        // Only GM can observe land rates
        if (!pci.isGM())
        {
            return;
        }

        if (!_landrateObserver.contains(pci))
        {
            _landrateObserver.add(pci);
            _landrateObservationActive = true;
        }
    }

    /**
     * Tenkai custom - remove instance reference when GM stops observing land rates of this instance
     */
    private void removeLandrateObserver(L2PcInstance pci)
    {
        // Only GM can observe land rates
        if (!pci.isGM())
        {
            return;
        }

        if (_landrateObserver.contains(pci))
        {
            _landrateObserver.remove(pci);
            _landrateObservationActive = false;
        }
    }

    /**
     * Tenkai custom - store instance reference of player whose land rates are under observation
     */
    private void addPlayerUnderLandrateObservation(L2PcInstance pci)
    {
        if (!_playersUnderLandrateObservation.contains(pci))
        {
            _playersUnderLandrateObservation.add(pci);
            _landrateObservationActive = true;
        }
    }

    /**
     * Tenkai custom - remove instance reference of player whose land rates were under observation
     */
    private void removePlayerUnderLandrateObservation(L2PcInstance pci)
    {
        if (_playersUnderLandrateObservation.contains(pci))
        {
            _playersUnderLandrateObservation.remove(pci);
            _landrateObservationActive = false;
        }
    }

    /**
     * Tenkai custom - convenience method for observing land rates of this player instance
     */
    public void registerLandratesObserver(L2PcInstance observingGM)
    {
        if (!observingGM.isGM())
        {
            return;
        }

        addLandrateObserver(observingGM);
        observingGM.addPlayerUnderLandrateObservation(this);
    }

    /**
     * Tenkai custom - convenience method for ending observation of land rates of this player instance
     */
    public void stopLandrateObservation(L2PcInstance observingGM)
    {
        if (!observingGM.isGM())
        {
            return;
        }

        removeLandrateObserver(observingGM);
        observingGM.removePlayerUnderLandrateObservation(this);
    }

    /**
     * Tenkai custom - returns the collection of land rate observing GMs for this player instance
     */
    public ArrayList<L2PcInstance> getLandrateObservers()
    {
        return _landrateObserver;
    }

    /**
     * Tenkai custom - returns the collection of players under land rate observation by this GM
     */
    public ArrayList<L2PcInstance> getPlayersUnderLandrateObservation()
    {
        return _playersUnderLandrateObservation;
    }

    /**
     * Tenkai custom - returns true when this instance is either observing someone's land rates or is under observation
     */
    public boolean isLandrateObservationActive()
    {
        return _landrateObservationActive;
    }

    /**
     * Tenkai custom - returns true when this GM is observing the land rates of the player instance given as argument
     */
    public boolean isLandrateObservationActive(L2PcInstance player)
    {
        for (L2PcInstance p : _playersUnderLandrateObservation)
        {
            if (p.getName().equals(player.getName()))
            {
                return true;
            }
        }

        return false;
    }

    public void addBypass(String bypass)
    {
        if (bypass == null)
        {
            return;
        }

        synchronized (_validBypass)
        {
            _validBypass.add(bypass);
        }
    }

    public void addBypass2(String bypass)
    {
        if (bypass == null)
        {
            return;
        }

        synchronized (_validBypass2)
        {
            _validBypass2.add(bypass);
        }
    }

    public boolean validateBypass(String cmd)
    {
        if (!Config.BYPASS_VALIDATION)
        {
            return true;
        }

        synchronized (_validBypass)
        {
            for (String bp : _validBypass)
            {
                if (bp == null)
                {
                    continue;
                }

                if (bp.equals(cmd))
                {
                    return true;
                }
            }
        }

        synchronized (_validBypass2)
        {
            for (String bp : _validBypass2)
            {
                if (bp == null)
                {
                    continue;
                }

                if (cmd.startsWith(bp))
                {
                    return true;
                }
            }
        }

        Log.warning("[L2PcInstance] player [" + getName() + "] sent invalid bypass '" + cmd + "'.");
        //Util.handleIllegalPlayerAction(this, "Player " + getName() + " sent invalid bypass '"+cmd+"'", Config.DEFAULT_PUNISH);
        return false;
    }

    /**
     * Performs following tests:<br>
     * <li> Inventory contains item
     * <li> Item owner id == this.owner id
     * <li> It isnt pet control item while mounting pet or pet summoned
     * <li> It isnt active enchant item
     * <li> It isnt cursed weapon/item
     * <li> It isnt wear item
     * <br>
     *
     * @param objectId: item object id
     * @param action:   just for login porpouse
     * @return
     */
    public boolean validateItemManipulation(int objectId, String action)
    {
        L2ItemInstance item = getInventory().getItemByObjectId(objectId);

        if (item == null || item.getOwnerId() != getObjectId())
        {
            Log.finest(getObjectId() + ": player tried to " + action + " item he is not owner of");
            return false;
        }

        // Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
        if (getPet() != null && getPet().getControlObjectId() == objectId || getMountObjectID() == objectId)
        {
            if (Config.DEBUG)
            {
                Log.finest(getObjectId() + ": player tried to " + action + " item controling pet");
            }

            return false;
        }

        if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
        {
            if (Config.DEBUG)
            {
                Log.finest(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
            }

            return false;
        }

        if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
        {
            // can not trade a cursed weapon
            return false;
        }

        return true;
    }

    public void clearBypass()
    {
        synchronized (_validBypass)
        {
            _validBypass.clear();
        }
        synchronized (_validBypass2)
        {
            _validBypass2.clear();
        }
    }

    /**
     * @return Returns the inBoat.
     */
    public boolean isInBoat()
    {
        return _vehicle != null && _vehicle.isBoat();
    }

    /**
     * @return
     */
    public L2BoatInstance getBoat()
    {
        return (L2BoatInstance) _vehicle;
    }

    /**
     * @return Returns the inAirShip.
     */
    public boolean isInAirShip()
    {
        return _vehicle != null && _vehicle.isAirShip();
    }

    public L2AirShipInstance getAirShip()
    {
        return (L2AirShipInstance) _vehicle;
    }

    public boolean isInShuttle()
    {
        return _vehicle != null && _vehicle.isShuttle();
    }

    private long _lastGotOnOffShuttle = 0;

    public void gotOnOffShuttle()
    {
        _lastGotOnOffShuttle = System.currentTimeMillis();
    }

    public boolean canGetOnOffShuttle()
    {
        return System.currentTimeMillis() - _lastGotOnOffShuttle > 1000L;
    }

    public L2ShuttleInstance getShuttle()
    {
        return (L2ShuttleInstance) _vehicle;
    }

    public L2Vehicle getVehicle()
    {
        return _vehicle;
    }

    public void setVehicle(L2Vehicle v)
    {
        if (v == null && _vehicle != null)
        {
            _vehicle.removePassenger(this);
        }

        _vehicle = v;
    }

    public void setInCrystallize(boolean inCrystallize)
    {
        _inCrystallize = inCrystallize;
    }

    public boolean isInCrystallize()
    {
        return _inCrystallize;
    }

    /**
     * @return
     */
    public Point3D getInVehiclePosition()
    {
        return _inVehiclePosition;
    }

    public void setInVehiclePosition(Point3D pt)
    {
        _inVehiclePosition = pt;
    }

    /**
     * Manage the delete task of a L2PcInstance (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<BR><BR>
     * <p>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>If the L2PcInstance is in observer mode, set its position to its position before entering in observer mode </li>
     * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess </li>
     * <li>Stop the HP/MP/CP Regeneration task </li>
     * <li>Cancel Crafting, Attak or Cast </li>
     * <li>Remove the L2PcInstance from the world </li>
     * <li>Stop Party and Unsummon Pet </li>
     * <li>Update database with items in its inventory and remove them from the world </li>
     * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI </li>
     * <li>Close the connection with the client </li><BR><BR>
     */
    @Override
    public void deleteMe()
    {
        cleanup();
        store();
        super.deleteMe();
    }

    private synchronized void cleanup()
    {
        // Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
        try
        {
            if (!isOnline())
            {
                Log.log(Level.SEVERE, "deleteMe() called on offline character " + this, new RuntimeException());
            }
            setOnlineStatus(false, true);
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // Remove the player form the events
        try
        {
            EventsManager.getInstance().onLogout(this);
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            if (getFriendList().size() > 0)
            {
                for (int i : getFriendList())
                {
                    L2PcInstance friend = L2World.getInstance().getPlayer(i);
                    if (friend != null)
                    {
                        friend.sendPacket(new FriendPacket(true, getObjectId(), friend));
                        friend.sendPacket(new FriendList(friend));
                    }
                }
            }

            SystemMessage sm;
            if (isMentee() && !canBeMentor())
            {
                for (L2Abnormal e : getAllEffects())
                {
                    if (e.getSkill().getId() >= 9227 && e.getSkill().getId() <= 9233)
                    {
                        e.exit();
                    }
                }

                L2PcInstance mentor = L2World.getInstance().getPlayer(getMentorId());
                if (mentor != null && mentor.isOnline())
                {
                    mentor.sendPacket(new ExMentorList(mentor));
                    sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_MENTEE_S1_HAS_DISCONNECTED);
                    sm.addCharName(this);
                    mentor.sendPacket(sm);
                    mentor.giveMentorBuff();
                }
            }
            else if (isMentor())
            {
                for (int objId : getMenteeList())
                {
                    L2PcInstance mentee = L2World.getInstance().getPlayer(objId);
                    if (mentee != null && mentee.isOnline())
                    {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_MENTOR_S1_HAS_DISCONNECTED);
                        sm.addCharName(this);
                        mentee.sendPacket(sm);
                        mentee.sendPacket(new ExMentorList(mentee));
                        for (L2Abnormal e : mentee.getAllEffects())
                        {
                            if (e.getSkill().getId() >= 9227 && e.getSkill().getId() <= 9233)
                            {
                                e.exit();
                            }
                        }
                    }
                }

                for (L2Abnormal e : getAllEffects())
                {
                    if (e.getSkill().getId() == 9256)
                    {
                        e.exit();
                    }
                }
            }
            PartySearchManager psm = PartySearchManager.getInstance();
            if (psm.getWannaToChangeThisPlayer(getLevel(), getClassId()) != null)
            {
                psm.removeChangeThisPlayer(this);
            }
            if (psm.getLookingForParty(getLevel(), getClassId()) != null)
            {
                psm.removeLookingForParty(this);
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }
        try
        {
            if (Config.ENABLE_BLOCK_CHECKER_EVENT && getBlockCheckerArena() != -1)
            {
                HandysBlockCheckerManager.getInstance().onDisconnect(this);
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            _isOnline = false;
            abortAttack();
            abortCast();
            stopMove(null);
            setDebug(null);
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        //remove combat flag
        try
        {
            if (getInventory().getItemByItemId(9819) != null)
            {
                Fort fort = FortManager.getInstance().getFort(this);
                if (fort != null)
                {
                    FortSiegeManager.getInstance().dropCombatFlag(this, fort.getFortId());
                }
                else
                {
                    int slot = getInventory().getSlotFromItem(getInventory().getItemByItemId(9819));
                    getInventory().unEquipItemInBodySlot(slot);
                    destroyItem("CombatFlag", getInventory().getItemByItemId(9819), null, true);
                }
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            if (getPet() != null)
            {
                getPet().storeEffects();
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            // Block Checker Event is enabled and player is still playing a match
            if (_handysBlockCheckerEventArena != -1)
            {
                int team = HandysBlockCheckerManager.getInstance().getHolder(_handysBlockCheckerEventArena)
                        .getPlayerTeam(this);
                HandysBlockCheckerManager.getInstance().removePlayer(this, _handysBlockCheckerEventArena, team);
                if (getTeam() > 0)
                {
                    // Remove transformation
                    L2Abnormal transform;
                    if ((transform = getFirstEffect(6035)) != null)
                    {
                        transform.exit();
                    }
                    else if ((transform = getFirstEffect(6036)) != null)
                    {
                        transform.exit();
                    }
                    // Remove team aura
                    setTeam(0);

                    // Remove the event items
                    PcInventory inv = getInventory();

                    if (inv.getItemByItemId(13787) != null)
                    {
                        long count = inv.getInventoryItemCount(13787, 0);
                        inv.destroyItemByItemId("Handys Block Checker", 13787, count, this, this);
                    }
                    if (inv.getItemByItemId(13788) != null)
                    {
                        long count = inv.getInventoryItemCount(13788, 0);
                        inv.destroyItemByItemId("Handys Block Checker", 13788, count, this, this);
                    }
                    setInsideZone(L2Character.ZONE_PVP, false);
                    // Teleport Back
                    teleToLocation(-57478, -60367, -2370);
                }
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            PartyMatchWaitingList.getInstance().removePlayer(this);
            if (_partyroom != 0)
            {
                PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
                if (room != null)
                {
                    room.deleteMember(this);
                }
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            if (isFlying())
            {
                removeSkill(SkillTable.getInstance().getInfo(4289, 1));
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // Recommendations must be saved before task (timer) is canceled
        try
        {
            storeRecommendations();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // Stop the HP/MP/CP Regeneration task (scheduled tasks)
        try
        {
            stopAllTimers();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            setIsTeleporting(false);
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // Stop crafting, if in progress
        try
        {
            RecipeController.getInstance().requestMakeItemAbort(this);
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // Cancel Attack or Cast
        try
        {
            setTarget(null);
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            if (_fusionSkill != null || _continuousDebuffTargets != null)
            {
                abortCast();
            }

            for (L2Character character : getKnownList().getKnownCharacters())
            {
                if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
                {
                    character.abortCast();
                }
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        try
        {
            for (L2Abnormal effect : getAllEffects())
            {
                if (effect.getSkill().isToggle() &&
                        !(effect.getSkill().getId() >= 11007 && effect.getSkill().getId() <= 11010))
                {
                    effect.exit();
                    continue;
                }

                switch (effect.getType())
                {
                    case SIGNET_GROUND:
                    case SIGNET_EFFECT:
                        effect.exit();
                        break;
                }
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // Remove from world regions zones
        L2WorldRegion oldRegion = getWorldRegion();

        // Remove the L2PcInstance from the world
        try
        {
            decayMe();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        if (oldRegion != null)
        {
            oldRegion.removeFromZones(this);
        }

        // If a Party is in progress, leave it (and festival party)
        if (isInParty())
        {
            try
            {
                leaveParty();
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "deleteMe()", e);
            }
        }

        if (OlympiadManager.getInstance().isRegistered(this) ||
                getOlympiadGameId() != -1) // handle removal from olympiad game
        {
            OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
        }

        // If the L2PcInstance has Pet, unsummon it
        L2PetInstance pet = getPet();
        if (pet != null)
        {
            try
            {
                L2ItemInstance controlPetItem = pet.getControlItem();
                if (controlPetItem != null)
                {
                    _lastSummons.add(controlPetItem.getItemId());
                }

                pet.unSummon(this);
                pet.broadcastNpcInfo(0);
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "deleteMe()", e);
            }// returns pet to control item
        }

        for (L2SummonInstance summon : getSummons())
        {
            if (summon == null)
            {
                continue;
            }
            try
            {
                _lastSummons.add(summon.getSummonSkillId());
                summon.unSummon(this);
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "deleteMe()", e);
            }// returns pet to control item
        }

        if (getClan() != null)
        {
            // set the status for pledge member list to OFFLINE
            try
            {
                L2ClanMember clanMember = getClan().getClanMember(getObjectId());
                if (clanMember != null)
                {
                    clanMember.setPlayerInstance(null);
                }

                if (isClanLeader())
                {
                    for (L2ClanMember member : getClan().getMembers())
                    {
                        if (member.getPlayerInstance() == null)
                        {
                            continue;
                        }

                        L2Abnormal eff = member.getPlayerInstance().getFirstEffect(19009);
                        if (eff != null)
                        {
                            eff.exit();
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "deleteMe()", e);
            }
        }

        if (getActiveRequester() != null)
        {
            // deals with sudden exit in the middle of transaction
            setActiveRequester(null);
            cancelActiveTrade();
        }

        // Stop possible land rate observations
        if (isLandrateObservationActive())
        {
            if (!isGM())
            {
                for (L2PcInstance gm : _landrateObserver)
                {
                    gm.sendMessage(getName() + " logged out. This ends your land rate observation.");
                    stopLandrateObservation(gm);
                }
            }
            else
            {
                for (L2PcInstance obsP : _playersUnderLandrateObservation)
                {
                    obsP.stopLandrateObservation(this);
                }
            }
        }

        // If the L2PcInstance is a GM, remove it from the GM List
        if (isGM())
        {
            try
            {
                GmListTable.getInstance().deleteGm(this);
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "deleteMe()", e);
            }
        }

        try
        {
            // Check if the L2PcInstance is in observer mode to set its position to its position
            // before entering in observer mode
            if (inObserverMode())
            {
                setXYZInvisible(_lastX, _lastY, _lastZ);
            }

            if (getVehicle() != null)
            {
                getVehicle().oustPlayer(this);
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // remove player from instance and set spawn location if any
        try
        {
            final int instanceId = getInstanceId();
            if (instanceId != 0 && !Config.RESTORE_PLAYER_INSTANCE)
            {
                final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
                if (inst != null)
                {
                    inst.removePlayer(getObjectId());
                    final int[] spawn = inst.getSpawnLoc();
                    if (spawn[0] != 0 && spawn[1] != 0 && spawn[2] != 0)
                    {
                        final int x = spawn[0] + Rnd.get(-30, 30);
                        final int y = spawn[1] + Rnd.get(-30, 30);
                        setXYZInvisible(x, y, spawn[2]);
                        if (getPet() != null) // dead pet
                        {
                            getPet().teleToLocation(x, y, spawn[2]);
                            getPet().setInstanceId(0);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // Update database with items in its inventory and remove them from the world
        try
        {
            getInventory().deleteMe();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }
        try
        {
            if (getPetInv() != null)
            {
                getPetInv().deleteMe();
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        // Update database with items in its warehouse and remove them from the world
        try
        {
            clearWarehouse();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }
        if (Config.WAREHOUSE_CACHE)
        {
            WarehouseCacheManager.getInstance().remCacheTask(this);
        }

        try
        {
            clearRefund();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        if (isCursedWeaponEquipped())
        {
            try
            {
                CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).setPlayer(null);
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "deleteMe()", e);
            }
        }

        // Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
        try
        {
            getKnownList().removeAllKnownObjects();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "deleteMe()", e);
        }

        if (getClanId() > 0)
        {
            getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
            getClan().broadcastToOnlineMembers(new ExPledgeCount(getClan().getOnlineMembersCount()));
        }
        //ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));

        for (L2PcInstance player : _snoopedPlayer)
        {
            player.removeSnooper(this);
        }

        for (L2PcInstance player : _snoopListener)
        {
            player.removeSnooped(this);
        }

        // Remove L2Object object from _allObjects of L2World
        L2World.getInstance().removeObject(this);
        L2World.getInstance().removeFromAllPlayers(this); // force remove in case of crash during teleport

        InstanceManager.getInstance().destroyInstance(getObjectId());

        try
        {
            notifyFriends();
            getBlockList().playerLogout();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Exception on deleteMe() notifyFriends: " + e.getMessage(), e);
        }
    }

    private FishData _fish;

    /*  startFishing() was stripped of any pre-fishing related checks, namely the fishing zone check.
	 * Also worthy of note is the fact the code to find the hook landing position was also striped. The
	 * stripped code was moved into fishing.java. In my opinion it makes more sense for it to be there
	 * since all other skill related checks were also there. Last but not least, moving the zone check
	 * there, fixed a bug where baits would always be consumed no matter if fishing actualy took place.
	 * startFishing() now takes up 3 arguments, wich are acurately described as being the hook landing
	 * coordinates.
	 */
    public void startFishing(int _x, int _y, int _z)
    {
        stopMove(null);
        setIsImmobilized(true);
        _fishing = true;
        _fishx = _x;
        _fishy = _y;
        _fishz = _z;
        //broadcastUserInfo();
        //Starts fishing
        int lvl = GetRandomFishLvl();
        int group = GetRandomGroup();
        int type = GetRandomFishType(group);
        List<FishData> fishs = FishTable.getInstance().getfish(lvl, type, group);
        if (fishs == null || fishs.isEmpty())
        {
            sendMessage("Error - Fishes are not definied");
            endFishing(false);
            return;
        }
        int check = Rnd.get(fishs.size());
        // Use a copy constructor else the fish data may be over-written below
        _fish = new FishData(fishs.get(check));
        fishs.clear();
        fishs = null;
        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CAST_LINE_AND_START_FISHING));
        if (!TimeController.getInstance().isNowNight() && _lure.isNightLure())
        {
            _fish.setType(-1);
        }
        //sendMessage("Hook x,y: " + _x + "," + _y + " - Water Z, Player Z:" + _z + ", " + getZ()); //debug line, uncoment to show coordinates used in fishing.
        broadcastPacket(new ExFishingStart(this, _fish.getType(), _x, _y, _z, _lure.isNightLure()));
        sendPacket(new PlaySound(1, "SF_P_01", 0, 0, 0, 0, 0));
        startLookingForFishTask();
    }

    public void stopLookingForFishTask()
    {
        if (_taskforfish != null)
        {
            _taskforfish.cancel(false);
            _taskforfish = null;
        }
    }

    public void startLookingForFishTask()
    {
        if (!isDead() && _taskforfish == null)
        {
            int checkDelay = 0;
            boolean isNoob = false;
            boolean isUpperGrade = false;

            if (_lure != null)
            {
                int lureid = _lure.getItemId();
                isNoob = _fish.getGroup() == 0;
                isUpperGrade = _fish.getGroup() == 2;
                if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 ||
                        lureid == 8511) //low grade
                {
                    checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.33));
                }
                else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || lureid >= 8505 && lureid <= 8513 ||
                        lureid >= 7610 && lureid <= 7613 || lureid >= 7807 && lureid <= 7809 ||
                        lureid >= 8484 && lureid <= 8486) //medium grade, beginner, prize-winning & quest special bait
                {
                    checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.00));
                }
                else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 ||
                        lureid == 8513) //high grade
                {
                    checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 0.66));
                }
            }
            _taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(
                    new LookingForFishTask(_fish.getWaitTime(), _fish.getFishGuts(), _fish.getType(), isNoob,
                            isUpperGrade), 10000, checkDelay);
        }
    }

    private int GetRandomGroup()
    {
        switch (_lure.getItemId())
        {
            case 7807: //green for beginners
            case 7808: //purple for beginners
            case 7809: //yellow for beginners
            case 8486: //prize-winning for beginners
                return 0;
            case 8485: //prize-winning luminous
            case 8506: //green luminous
            case 8509: //purple luminous
            case 8512: //yellow luminous
                return 2;
            default:
                return 1;
        }
    }

    private int GetRandomFishType(int group)
    {
        int check = Rnd.get(100);
        int type = 1;
        switch (group)
        {
            case 0: //fish for novices
                switch (_lure.getItemId())
                {
                    case 7807: //green lure, preferred by fast-moving (nimble) fish (type 5)
                        if (check <= 54)
                        {
                            type = 5;
                        }
                        else if (check <= 77)
                        {
                            type = 4;
                        }
                        else
                        {
                            type = 6;
                        }
                        break;
                    case 7808: //purple lure, preferred by fat fish (type 4)
                        if (check <= 54)
                        {
                            type = 4;
                        }
                        else if (check <= 77)
                        {
                            type = 6;
                        }
                        else
                        {
                            type = 5;
                        }
                        break;
                    case 7809: //yellow lure, preferred by ugly fish (type 6)
                        if (check <= 54)
                        {
                            type = 6;
                        }
                        else if (check <= 77)
                        {
                            type = 5;
                        }
                        else
                        {
                            type = 4;
                        }
                        break;
                    case 8486: //prize-winning fishing lure for beginners
                        if (check <= 33)
                        {
                            type = 4;
                        }
                        else if (check <= 66)
                        {
                            type = 5;
                        }
                        else
                        {
                            type = 6;
                        }
                        break;
                }
                break;
            case 1: //normal fish
                switch (_lure.getItemId())
                {
                    case 7610:
                    case 7611:
                    case 7612:
                    case 7613:
                        type = 3;
                        break;
                    case 6519: //all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
                    case 8505:
                    case 6520:
                    case 6521:
                    case 8507:
                        if (check <= 54)
                        {
                            type = 1;
                        }
                        else if (check <= 74)
                        {
                            type = 0;
                        }
                        else if (check <= 94)
                        {
                            type = 2;
                        }
                        else
                        {
                            type = 3;
                        }
                        break;
                    case 6522: //all theese lures (purple) are prefered by fat fish (type 0)
                    case 8508:
                    case 6523:
                    case 6524:
                    case 8510:
                        if (check <= 54)
                        {
                            type = 0;
                        }
                        else if (check <= 74)
                        {
                            type = 1;
                        }
                        else if (check <= 94)
                        {
                            type = 2;
                        }
                        else
                        {
                            type = 3;
                        }
                        break;
                    case 6525: //all theese lures (yellow) are prefered by ugly fish (type 2)
                    case 8511:
                    case 6526:
                    case 6527:
                    case 8513:
                        if (check <= 55)
                        {
                            type = 2;
                        }
                        else if (check <= 74)
                        {
                            type = 1;
                        }
                        else if (check <= 94)
                        {
                            type = 0;
                        }
                        else
                        {
                            type = 3;
                        }
                        break;
                    case 8484: //prize-winning fishing lure
                        if (check <= 33)
                        {
                            type = 0;
                        }
                        else if (check <= 66)
                        {
                            type = 1;
                        }
                        else
                        {
                            type = 2;
                        }
                        break;
                }
                break;
            case 2: //upper grade fish, luminous lure
                switch (_lure.getItemId())
                {
                    case 8506: //green lure, preferred by fast-moving (nimble) fish (type 8)
                        if (check <= 54)
                        {
                            type = 8;
                        }
                        else if (check <= 77)
                        {
                            type = 7;
                        }
                        else
                        {
                            type = 9;
                        }
                        break;
                    case 8509: //purple lure, preferred by fat fish (type 7)
                        if (check <= 54)
                        {
                            type = 7;
                        }
                        else if (check <= 77)
                        {
                            type = 9;
                        }
                        else
                        {
                            type = 8;
                        }
                        break;
                    case 8512: //yellow lure, preferred by ugly fish (type 9)
                        if (check <= 54)
                        {
                            type = 9;
                        }
                        else if (check <= 77)
                        {
                            type = 8;
                        }
                        else
                        {
                            type = 7;
                        }
                        break;
                    case 8485: //prize-winning fishing lure
                        if (check <= 33)
                        {
                            type = 7;
                        }
                        else if (check <= 66)
                        {
                            type = 8;
                        }
                        else
                        {
                            type = 9;
                        }
                        break;
                }
        }
        return type;
    }

    private int GetRandomFishLvl()
    {
        int skilllvl = getSkillLevelHash(1315);
        final L2Abnormal e = getFirstEffect(2274);
        if (e != null)
        {
            skilllvl = (int) e.getSkill().getPower();
        }
        if (skilllvl <= 0)
        {
            return 1;
        }
        int randomlvl;
        int check = Rnd.get(100);

        if (check <= 50)
        {
            randomlvl = skilllvl;
        }
        else if (check <= 85)
        {
            randomlvl = skilllvl - 1;
            if (randomlvl <= 0)
            {
                randomlvl = 1;
            }
        }
        else
        {
            randomlvl = skilllvl + 1;
            if (randomlvl > 27)
            {
                randomlvl = 27;
            }
        }

        return randomlvl;
    }

    public void startFishCombat(boolean isNoob, boolean isUpperGrade)
    {
        _fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
    }

    public void endFishing(boolean win)
    {
        _fishing = false;
        _fishx = 0;
        _fishy = 0;
        _fishz = 0;
        //broadcastUserInfo();
        if (_fishCombat == null)
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY));
        }
        _fishCombat = null;
        _lure = null;
        //Ends fishing
        broadcastPacket(new ExFishingEnd(win, this));
        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REEL_LINE_AND_STOP_FISHING));
        setIsImmobilized(false);
        stopLookingForFishTask();
    }

    public L2Fishing getFishCombat()
    {
        return _fishCombat;
    }

    public int getFishx()
    {
        return _fishx;
    }

    public int getFishy()
    {
        return _fishy;
    }

    public int getFishz()
    {
        return _fishz;
    }

    public void setLure(L2ItemInstance lure)
    {
        _lure = lure;
    }

    public L2ItemInstance getLure()
    {
        return _lure;
    }

    public int getInventoryLimit()
    {
        int ivlim;

        if (isGM())
        {
            ivlim = Config.INVENTORY_MAXIMUM_GM;
        }
        else if (getRace() == Race.Dwarf)
        {
            ivlim = Config.INVENTORY_MAXIMUM_DWARF;
        }
        else
        {
            ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
        }

        ivlim += (int) getStat().calcStat(Stats.INVENTORY_LIMIT, 0, null, null);
        return ivlim;
    }

    public int getWareHouseLimit()
    {
        int whlim;
        if (getRace() == Race.Dwarf)
        {
            whlim = Config.WAREHOUSE_SLOTS_DWARF;
        }
        else
        {
            whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
        }

        whlim += (int) getStat().calcStat(Stats.WAREHOUSE_LIMIT, 0, null, null);

        return whlim;
    }

    public int getPrivateSellStoreLimit()
    {
        int pslim;

        if (getRace() == Race.Dwarf)
        {
            pslim = Config.MAX_PVTSTORESELL_SLOTS_DWARF;
        }
        else
        {
            pslim = Config.MAX_PVTSTORESELL_SLOTS_OTHER;
        }

        pslim += (int) getStat().calcStat(Stats.P_SELL_LIMIT, 0, null, null);

        return pslim;
    }

    public int getPrivateBuyStoreLimit()
    {
        int pblim;

        if (getRace() == Race.Dwarf)
        {
            pblim = Config.MAX_PVTSTOREBUY_SLOTS_DWARF;
        }
        else
        {
            pblim = Config.MAX_PVTSTOREBUY_SLOTS_OTHER;
        }
        pblim += (int) getStat().calcStat(Stats.P_BUY_LIMIT, 0, null, null);

        return pblim;
    }

    public int getDwarfRecipeLimit()
    {
        int recdlim = Config.DWARF_RECIPE_LIMIT;
        recdlim += (int) getStat().calcStat(Stats.REC_D_LIMIT, 0, null, null);
        return recdlim;
    }

    public int getCommonRecipeLimit()
    {
        int recclim = Config.COMMON_RECIPE_LIMIT;
        recclim += (int) getStat().calcStat(Stats.REC_C_LIMIT, 0, null, null);
        return recclim;
    }

    /**
     * @return Returns the mountNpcId.
     */
    public int getMountNpcId()
    {
        return _mountNpcId;
    }

    /**
     * @return Returns the mountLevel.
     */
    public int getMountLevel()
    {
        return _mountLevel;
    }

    public void setMountObjectID(int newID)
    {
        _mountObjectID = newID;
    }

    public int getMountObjectID()
    {
        return _mountObjectID;
    }

    private L2ItemInstance _lure = null;
    public int _shortBuffTaskSkillId = 0;

    /**
     * Get the current skill in use or return null.<BR><BR>
     */
    public SkillDat getCurrentSkill()
    {
        return _currentSkill;
    }

    /**
     * Create a new SkillDat object and set the player _currentSkill.<BR><BR>
     */
    public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
    {
        if (currentSkill == null)
        {
            if (Config.DEBUG)
            {
                Log.info("Setting current skill: NULL for " + getName() + ".");
            }

            _currentSkill = null;
            return;
        }

        if (Config.DEBUG)
        {
            Log.info("Setting current skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " +
                    getName() + ".");
        }

        _currentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
    }

    /**
     * Get the current pet skill in use or return null.<br><br>
     */
    public SkillDat getCurrentPetSkill()
    {
        return _currentPetSkill;
    }

    /**
     * Create a new SkillDat object and set the player _currentPetSkill.<br><br>
     */
    public void setCurrentPetSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
    {
        if (currentSkill == null)
        {
            if (Config.DEBUG)
            {
                Log.info("Setting current pet skill: NULL for " + getName() + ".");
            }

            _currentPetSkill = null;
            return;
        }

        if (Config.DEBUG)
        {
            Log.info("Setting current Pet skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() +
                    ") for " + getName() + ".");
        }

        _currentPetSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
    }

    public SkillDat getQueuedSkill()
    {
        return _queuedSkill;
    }

    /**
     * Create a new SkillDat object and queue it in the player _queuedSkill.<BR><BR>
     */
    public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
    {
        if (queuedSkill == null)
        {
            if (Config.DEBUG)
            {
                Log.info("Setting queued skill: NULL for " + getName() + ".");
            }

            _queuedSkill = null;
            return;
        }

        if (Config.DEBUG)
        {
            Log.info("Setting queued skill: " + queuedSkill.getName() + " (ID: " + queuedSkill.getId() + ") for " +
                    getName() + ".");
        }

        _queuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
    }

    /**
     * returns punishment level of player
     *
     * @return
     */
    public PunishLevel getPunishLevel()
    {
        return _punishLevel;
    }

    /**
     * @return True if player is jailed
     */
    public boolean isInJail()
    {
        return _punishLevel == PunishLevel.JAIL;
    }

    /**
     * @return True if player is chat banned
     */
    public boolean isChatBanned()
    {
        return _punishLevel == PunishLevel.CHAT;
    }

    public void setPunishLevel(int state)
    {
        switch (state)
        {
            case 0:
            {
                _punishLevel = PunishLevel.NONE;
                break;
            }
            case 1:
            {
                _punishLevel = PunishLevel.CHAT;
                break;
            }
            case 2:
            {
                _punishLevel = PunishLevel.JAIL;
                break;
            }
            case 3:
            {
                _punishLevel = PunishLevel.CHAR;
                break;
            }
            case 4:
            {
                _punishLevel = PunishLevel.ACC;
                break;
            }
        }
    }

    /**
     * Sets punish level for player based on delay
     *
     * @param state
     * @param delayInMinutes 0 - Indefinite
     */
    public void setPunishLevel(PunishLevel state, int delayInMinutes)
    {
        long delayInMilliseconds = delayInMinutes * 60000L;
        switch (state)
        {
            case NONE: // Remove Punishments
            {
                switch (_punishLevel)
                {
                    case CHAT:
                    {
                        _punishLevel = state;
                        stopPunishTask(true);
                        sendPacket(new EtcStatusUpdate(this));
                        sendMessage("Your Chat ban has been lifted");
                        break;
                    }
                    case JAIL:
                    {
                        _punishLevel = state;
                        // Open a Html message to inform the player
                        NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
                        String jailInfos = HtmCache.getInstance().getHtm(getHtmlPrefix(), "jail_out.htm");
                        if (jailInfos != null)
                        {
                            htmlMsg.setHtml(jailInfos);
                        }
                        else
                        {
                            htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
                        }
                        sendPacket(htmlMsg);
                        stopPunishTask(true);
                        teleToLocation(17836, 170178, -3507, true); // Floran
                        break;
                    }
                }
                break;
            }
            case CHAT: // Chat Ban
            {
                // not allow player to escape jail using chat ban
                if (_punishLevel == PunishLevel.JAIL)
                {
                    break;
                }
                _punishLevel = state;
                _punishTimer = 0;
                sendPacket(new EtcStatusUpdate(this));
                // Remove the task if any
                stopPunishTask(false);

                if (delayInMinutes > 0)
                {
                    _punishTimer = delayInMilliseconds;

                    // start the countdown
                    _punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(), _punishTimer);
                    sendMessage("You are chat banned for " + delayInMinutes + " minutes.");
                }
                else
                {
                    sendMessage("You have been chat banned");
                }
                break;
            }
            case JAIL: // Jail Player
            {
                _punishLevel = state;
                _punishTimer = 0;
                // Remove the task if any
                stopPunishTask(false);

                if (delayInMinutes > 0)
                {
                    _punishTimer = delayInMilliseconds;

                    // start the countdown
                    _punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(), _punishTimer);
                    sendMessage("You are in jail for " + delayInMinutes + " minutes.");
                }

                if (_event != null)
                {
                    _event.removeParticipant(getObjectId());
                }
                if (EventsManager.getInstance().isPlayerParticipant(getObjectId()))
                {
                    EventsManager.getInstance().removeParticipant(getObjectId());
                }
                if (OlympiadManager.getInstance().isRegisteredInComp(this))
                {
                    OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
                }

                // Open a Html message to inform the player
                NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
                String jailInfos = HtmCache.getInstance().getHtm(getHtmlPrefix(), "jail_in.htm");
                if (jailInfos != null)
                {
                    htmlMsg.setHtml(jailInfos);
                }
                else
                {
                    htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
                }
                sendPacket(htmlMsg);
                setInstanceId(0);

                teleToLocation(-114356, -249645, -2984, false); // Jail
                break;
            }
            case CHAR: // Ban Character
            {
                setAccessLevel(-100);
                logout();
                break;
            }
            case ACC: // Ban Account
            {
                setAccountAccesslevel(-100);
                logout();
                break;
            }
            default:
            {
                _punishLevel = state;
                break;
            }
        }

        // store in database
        storeCharBase();
    }

    public long getPunishTimer()
    {
        return _punishTimer;
    }

    public void setPunishTimer(long time)
    {
        _punishTimer = time;
    }

    private void updatePunishState()
    {
        if (getPunishLevel() != PunishLevel.NONE)
        {
            // If punish timer exists, restart punishtask.
            if (_punishTimer > 0)
            {
                _punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(), _punishTimer);
                sendMessage("You are still " + getPunishLevel().string() + " for " + Math.round(_punishTimer / 60000f) +
                        " minutes.");
            }
            if (getPunishLevel() == PunishLevel.JAIL)
            {
                // If player escaped, put him back in jail
                if (!isInsideZone(ZONE_JAIL))
                {
                    teleToLocation(-114356, -249645, -2984, true);
                }
            }
        }
    }

    public void stopPunishTask(boolean save)
    {
        if (_punishTask != null)
        {
            if (save)
            {
                long delay = _punishTask.getDelay(TimeUnit.MILLISECONDS);
                if (delay < 0)
                {
                    delay = 0;
                }
                setPunishTimer(delay);
            }
            _punishTask.cancel(false);
            //ThreadPoolManager.getInstance().removeGeneral((Runnable)_punishTask);
            _punishTask = null;
        }
    }

    private class PunishTask implements Runnable
    {
        @Override
        public void run()
        {
            L2PcInstance.this.setPunishLevel(PunishLevel.NONE, 0);
        }
    }

    public void startFameTask(long delay, int fameFixRate)
    {
        if (getLevel() < 40 || getCurrentClass().level() > 0 && getCurrentClass().level() < 2)
        {
            return;
        }
        if (_fameTask == null)
        {
            _fameTask =
                    ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FameTask(fameFixRate), delay, delay);
        }
    }

    public void stopFameTask()
    {
        if (_fameTask != null)
        {
            _fameTask.cancel(false);
            //ThreadPoolManager.getInstance().removeGeneral((Runnable)_fameTask);
            _fameTask = null;
        }
    }

    private class FameTask implements Runnable
    {
        private final L2PcInstance _player;
        private final int _value;

        protected FameTask(int value)
        {
            _player = L2PcInstance.this;
            _value = value;
        }

        @Override
        public void run()
        {
            if (_player == null || _player.isDead() && !Config.FAME_FOR_DEAD_PLAYERS)
            {
                return;
            }
            if ((_player.getClient() == null || _player.getClient().isDetached()) && !Config.OFFLINE_FAME)
            {
                return;
            }
            _player.setFame(_player.getFame() + _value);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
            sm.addNumber(_value);
            _player.sendPacket(sm);
            _player.sendPacket(new UserInfo(_player));
        }
    }

    /**
     * @return
     */
    public int getPowerGrade()
    {
        return _powerGrade;
    }

    /**
     * @return
     */
    public void setPowerGrade(int power)
    {
        _powerGrade = power;
    }

    public boolean isCursedWeaponEquipped()
    {
        return _cursedWeaponEquippedId != 0;
    }

    public void setCursedWeaponEquippedId(int value)
    {
        _cursedWeaponEquippedId = value;
    }

    public int getCursedWeaponEquippedId()
    {
        return _cursedWeaponEquippedId;
    }

    @Override
    public boolean isAttackingDisabled()
    {
        return super.isAttackingDisabled() || _combatFlagEquippedId;
    }

    public boolean isCombatFlagEquipped()
    {
        return _combatFlagEquippedId;
    }

    public void setCombatFlagEquipped(boolean value)
    {
        _combatFlagEquippedId = value;
    }

    public final void setIsRidingStrider(boolean mode)
    {
        _isRidingStrider = mode;
    }

    public final boolean isRidingStrider()
    {
        return _isRidingStrider;
    }

    /**
     * Returns the Number of Souls this L2PcInstance got.
     *
     * @return
     */
    public int getSouls()
    {
        return _souls;
    }

    /**
     * Absorbs a Soul from a Npc.
     *
     * @param skill The used skill
     * @param npc   The target
     */
    public void absorbSoul(L2Skill skill, L2Npc npc)
    {
        if (_souls >= skill.getNumSouls())
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
            sendPacket(sm);
            return;
        }

        increaseSouls(1);

        if (npc != null)
        {
            broadcastPacket(new ExSpawnEmitter(this, npc), 500);
        }
    }

    /**
     * Increase Souls
     *
     * @param count
     */
    public void increaseSouls(int count)
    {
        if (count < 0 || count > 45)
        {
            return;
        }

        _souls += count;

        if (getSouls() > 45)
        {
            _souls = 45;
        }

        SystemMessage sm =
                SystemMessage.getSystemMessage(SystemMessageId.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
        sm.addNumber(count);
        sm.addNumber(_souls);
        sendPacket(sm);

        restartSoulTask();

        sendPacket(new EtcStatusUpdate(this));
    }

    /**
     * Decreases existing Souls.
     *
     * @param count
     */
    public boolean decreaseSouls(int count, L2Skill skill)
    {
        if (getSouls() <= 0 && skill.getSoulConsumeCount() > 0)
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THERE_IS_NOT_ENOUGH_SOUL));
            return false;
        }

        _souls -= count;

        if (getSouls() < 0)
        {
            _souls = 0;
        }

        if (getSouls() == 0)
        {
            stopSoulTask();
        }
        else
        {
            restartSoulTask();
        }

        sendPacket(new EtcStatusUpdate(this));
        return true;
    }

    /**
     * Clear out all Souls from this L2PcInstance
     */
    public void clearSouls()
    {
        _souls = 0;
        stopSoulTask();
        sendPacket(new EtcStatusUpdate(this));
    }

    /**
     * Starts/Restarts the SoulTask to Clear Souls after 10 Mins.
     */
    private void restartSoulTask()
    {
        synchronized (this)
        {
            if (_soulTask != null)
            {
                _soulTask.cancel(false);
                _soulTask = null;
            }
            _soulTask = ThreadPoolManager.getInstance().scheduleGeneral(new SoulTask(), 600000);
        }
    }

    /**
     * Stops the Clearing Task.
     */
    public void stopSoulTask()
    {
        if (_soulTask != null)
        {
            _soulTask.cancel(false);
            //ThreadPoolManager.getInstance().removeGeneral((Runnable)_soulTask);
            _soulTask = null;
        }
    }

    private class SoulTask implements Runnable
    {
        @Override
        public void run()
        {
            clearSouls();
        }
    }

    /**
     * @param magicId
     * @param level
     * @param time
     */
    public void shortBuffStatusUpdate(int magicId, int level, int time)
    {
        if (_shortBuffTask != null)
        {
            _shortBuffTask.cancel(false);
            _shortBuffTask = null;
        }
        _shortBuffTask = ThreadPoolManager.getInstance().scheduleGeneral(new ShortBuffTask(), time * 1000);
        setShortBuffTaskSkillId(magicId);

        sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
    }

    public void setShortBuffTaskSkillId(int id)
    {
        _shortBuffTaskSkillId = id;
    }

    public void calculateBreathOfShilenDebuff(L2Character killer)
    {
        if (killer instanceof L2MonsterInstance)
        {
            switch (((L2MonsterInstance) killer).getNpcId())
            {
                //Needs those NPC IDs (Spezion, Teredor, Veridan, Michaelo, Fortuna, Felicia, Isadora, Octavis, Istina, Balok, Barler)
                case 25532: // Kechi
                case 29068: // Antharas (Needs to be instanced version, don't know which is it)
                    raiseBreathOfShilenDebuffLevel();
                    break;
            }
        }
    }

    private void raiseBreathOfShilenDebuffLevel()
    {
        if (_breathOfShilenDebuffLevel > 5)
        {
            return;
        }

        _breathOfShilenDebuffLevel++;

        for (L2Abnormal effect : getAllEffects())
        {
            if (effect.getSkill().getId() == 14571)
            {
                effect.exit();
            }
        }

        if (_breathOfShilenDebuffLevel != 0)
        {
            SkillTable.getInstance().getInfo(14571, _breathOfShilenDebuffLevel).getEffects(this, this);
        }
    }

    public void decreaseBreathOfShilenDebuffLevel()
    {
        if (_breathOfShilenDebuffLevel >= 1)
        {
            if (!isDead())
            {
                _breathOfShilenDebuffLevel--;
                if (_breathOfShilenDebuffLevel != 0)
                {
                    SkillTable.getInstance().getInfo(14571, _breathOfShilenDebuffLevel).getEffects(this, this);
                }
            }
        }
    }

    private Map<Integer, TimeStamp> _reuseTimeStamps = new ConcurrentHashMap<>();
    private boolean _canFeed;
    private boolean _isInSiege;

    public Collection<TimeStamp> getReuseTimeStamps()
    {
        return _reuseTimeStamps.values();
    }

    public Map<Integer, TimeStamp> getReuseTimeStamp()
    {
        return _reuseTimeStamps;
    }

    /**
     * Simple class containing all neccessary information to maintain
     * valid timestamps and reuse for skills upon relog. Filter this
     * carefully as it becomes redundant to store reuse for small delays.
     *
     * @author Yesod
     */
    public static class TimeStamp
    {
        private final int _skillId;
        private final int _skillLvl;
        private final long _reuse;
        private final long _stamp;

        public TimeStamp(L2Skill skill, long reuse)
        {
            _skillId = skill.getId();
            _skillLvl = skill.getLevelHash();
            _reuse = reuse;
            _stamp = System.currentTimeMillis() + reuse;
        }

        public TimeStamp(L2Skill skill, long reuse, long systime)
        {
            _skillId = skill.getId();
            _skillLvl = skill.getLevelHash();
            _reuse = reuse;
            _stamp = systime;
        }

        public long getStamp()
        {
            return _stamp;
        }

        public int getSkillId()
        {
            return _skillId;
        }

        public int getSkillLvl()
        {
            return _skillLvl;
        }

        public long getReuse()
        {
            return _reuse;
        }

        public long getRemaining()
        {
            return Math.max(_stamp - System.currentTimeMillis(), 0);
        }

        /* Check if the reuse delay has passed and
		 * if it has not then update the stored reuse time
		 * according to what is currently remaining on
		 * the delay. */
        public boolean hasNotPassed()
        {
            return System.currentTimeMillis() < _stamp;
        }
    }

    /**
     * Index according to skill id the current
     * timestamp of use.
     *
     * @param skill
     * @param reuse delay
     */
    @Override
    public void addTimeStamp(L2Skill skill, long reuse)
    {
        _reuseTimeStamps.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse));
    }

    /**
     * Index according to skill this TimeStamp
     * instance for restoration purposes only.
     */
    public void addTimeStamp(L2Skill skill, long reuse, long systime)
    {
        _reuseTimeStamps.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse, systime));
    }

    @Override
    public L2PcInstance getActingPlayer()
    {
        return this;
    }

    @Override
    public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
    {
        // Check if hit is missed
        if (miss)
        {
            if (target instanceof L2PcInstance)
            {
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_EVADED_C2_ATTACK);
                sm.addPcName((L2PcInstance) target);
                sm.addCharName(this);
                target.sendPacket(sm);
            }
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_ATTACK_WENT_ASTRAY).addPcName(this));
            return;
        }

        // Check if hit is critical
        if (pcrit)
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_HAD_CRITICAL_HIT).addPcName(this));
            if (target instanceof L2Npc && getSkillLevelHash(467) > 0)
            {
                L2Skill skill = SkillTable.getInstance().getInfo(467, getSkillLevelHash(467));
                if (Rnd.get(100) < skill.getCritChance())
                {
                    absorbSoul(skill, (L2Npc) target);
                }
            }
        }
        if (mcrit)
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT_MAGIC));
        }

        if (isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() &&
                ((L2PcInstance) target).getOlympiadGameId() == getOlympiadGameId())
        {
            OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
        }

        final SystemMessage sm;

        int dmgCap = (int) target.getStat().calcStat(Stats.DAMAGE_CAP, 0, null, null);
        if (dmgCap > 0 && damage > dmgCap)
        {
            damage = dmgCap;
        }

        if (damage == -1 || target.isInvul(this) && !(target instanceof L2Npc) ||
                target.getFaceoffTarget() != null && target.getFaceoffTarget() != this)
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
        }
        else
        {
            sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG);
            sm.addNumber(damage);
            sm.addHpChange(target.getObjectId(), getObjectId(), -damage);
        }
        sendPacket(sm);
    }

    /**
     * @param npcId
     */
    public void setAgathionId(int npcId)
    {
        _agathionId = npcId;
    }

    /**
     * @return
     */
    public int getAgathionId()
    {
        return _agathionId;
    }

    public int getVitalityPoints()
    {
        return getStat().getVitalityPoints();
    }

    public void setVitalityPoints(int points, boolean quiet)
    {
        getStat().setVitalityPoints(points, quiet, false);
    }

    public void setVitalityPoints(int points, boolean quiet, boolean allowGM)
    {
        getStat().setVitalityPoints(points, quiet, allowGM);
    }

    public void updateVitalityPoints(float points, boolean useRates, boolean quiet)
    {
        getStat().updateVitalityPoints(points, useRates, quiet);
    }

	/*
	 * Function for skill summon friend or Gate Chant.
	 */

    /**
     * Request Teleport
     **/
    public boolean teleportRequest(L2PcInstance requester, L2Skill skill)
    {
        if (_summonRequest.getTarget() != null && requester != null)
        {
            return false;
        }
        _summonRequest.setTarget(requester, skill);
        return true;
    }

    /**
     * Action teleport
     **/
    public void teleportAnswer(int answer, int requesterId)
    {
        if (_summonRequest.getTarget() == null)
        {
            return;
        }
        if (answer == 1 && _summonRequest.getTarget().getObjectId() == requesterId)
        {
            teleToTarget(this, _summonRequest.getTarget(), _summonRequest.getSkill());
        }
        _summonRequest.setTarget(null, null);
    }

    public static void teleToTarget(L2PcInstance targetChar, L2PcInstance summonerChar, L2Skill summonSkill)
    {
        if (targetChar == null || summonerChar == null || summonSkill == null)
        {
            return;
        }

        if (!checkSummonerStatus(summonerChar))
        {
            return;
        }
        if (!checkSummonTargetStatus(targetChar, summonerChar))
        {
            return;
        }

        int itemConsumeId = summonSkill.getTargetConsumeId();
        int itemConsumeCount = summonSkill.getTargetConsume();
        if (itemConsumeId > 0 && itemConsumeCount > 0)
        {
            //Delete by rocknow
            if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
            {
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
                sm.addItemName(summonSkill.getTargetConsumeId());
                targetChar.sendPacket(sm);
                return;
            }
            targetChar.getInventory()
                    .destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
            sm.addItemName(summonSkill.getTargetConsumeId());
            targetChar.sendPacket(sm);
        }
        targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), true);
    }

    public static boolean checkSummonerStatus(L2PcInstance summonerChar)
    {
        if (summonerChar == null)
        {
            return false;
        }

        if (summonerChar.isInOlympiadMode())
        {
            summonerChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
            return false;
        }

        if (summonerChar.getIsInsideGMEvent())
        {
            return false;
        }

        if (summonerChar.inObserverMode())
        {
            return false;
        }

        if (summonerChar.getEvent() != null && !summonerChar.getEvent().onEscapeUse(summonerChar.getObjectId()))
        {
            summonerChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
            return false;
        }

        if (summonerChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND) || summonerChar.isFlyingMounted())
        {
            summonerChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
            return false;
        }
        return true;
    }

    public static boolean checkSummonTargetStatus(L2Object target, L2PcInstance summonerChar)
    {
        if (target == null || !(target instanceof L2PcInstance))
        {
            return false;
        }

        L2PcInstance targetChar = (L2PcInstance) target;
        if (targetChar.isAlikeDead())
        {
            SystemMessage sm =
                    SystemMessage.getSystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
            sm.addPcName(targetChar);
            summonerChar.sendPacket(sm);
            return false;
        }

        if (targetChar.isInStoreMode())
        {
            SystemMessage sm = SystemMessage.getSystemMessage(
                    SystemMessageId.C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
            sm.addPcName(targetChar);
            summonerChar.sendPacket(sm);
            return false;
        }

        if (targetChar.isRooted() || targetChar.isInCombat())
        {
            SystemMessage sm =
                    SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
            sm.addPcName(targetChar);
            summonerChar.sendPacket(sm);
            return false;
        }

        if (targetChar.isInOlympiadMode())
        {
            summonerChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
            return false;
        }

        if (targetChar.getIsInsideGMEvent())
        {
            return false;
        }

        if (targetChar.isFlyingMounted())
        {
            summonerChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
            return false;
        }

        if (targetChar.inObserverMode())
        {
            summonerChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.C1_STATE_FORBIDS_SUMMONING).addCharName(targetChar));
            return false;
        }

        if (!targetChar.canEscape() || targetChar.isCombatFlagEquipped())
        {
            summonerChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
            return false;
        }

        if (targetChar.getEvent() != null && !targetChar.getEvent().onEscapeUse(targetChar.getObjectId()))
        {
            summonerChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
            return false;
        }

        if (targetChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA);
            sm.addString(targetChar.getName());
            summonerChar.sendPacket(sm);
            return false;
        }

        if (summonerChar.getInstanceId() > 0)
        {
            Instance summonerInstance = InstanceManager.getInstance().getInstance(summonerChar.getInstanceId());
            if (!Config.ALLOW_SUMMON_TO_INSTANCE || !summonerInstance.isSummonAllowed())
            {
                summonerChar.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
                return false;
            }
        }

        return true;
    }

    public void gatesRequest(L2DoorInstance door)
    {
        _gatesRequest.setTarget(door);
    }

    public void gatesAnswer(int answer, int type)
    {
        if (_gatesRequest.getDoor() == null)
        {
            return;
        }

        if (answer == 1 && getTarget() == _gatesRequest.getDoor() && type == 1)
        {
            _gatesRequest.getDoor().openMe();
        }
        else if (answer == 1 && getTarget() == _gatesRequest.getDoor() && type == 0)
        {
            _gatesRequest.getDoor().closeMe();
        }

        _gatesRequest.setTarget(null);
    }

    public void checkItemRestriction()
    {
        for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
        {
            L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);
            if (equippedItem != null && (!equippedItem.getItem().checkCondition(this, this, false) ||
                    isInOlympiadMode() && equippedItem.getItem().isOlyRestricted()))
            {
                getInventory().unEquipItemInSlot(i);

                InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(equippedItem);
                sendPacket(iu);

                SystemMessage sm = null;
                if (equippedItem.getItem().getBodyPart() == L2Item.SLOT_BACK)
                {
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLOAK_REMOVED_BECAUSE_ARMOR_SET_REMOVED));
                    return;
                }

                if (equippedItem.getEnchantLevel() > 0)
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                    sm.addNumber(equippedItem.getEnchantLevel());
                    sm.addItemName(equippedItem);
                }
                else
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                    sm.addItemName(equippedItem);
                }
                sendPacket(sm);
            }
        }
    }

    public void setTransformAllowedSkills(int[] ids)
    {
        _transformAllowedSkills = ids;
    }

    public boolean containsAllowedTransformSkill(int id)
    {
        for (int _transformAllowedSkill : _transformAllowedSkills)
        {
            if (_transformAllowedSkill == id)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Section for mounted pets
     */
    private class FeedTask implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                if (!isMounted() || getMountNpcId() == 0)
                {
                    stopFeed();
                    return;
                }

                if (getCurrentFeed() > getFeedConsume())
                {
                    // eat
                    setCurrentFeed(getCurrentFeed() - getFeedConsume());
                }
                else
                {
                    // go back to pet control item, or simply said, unsummon it
                    setCurrentFeed(0);
                    stopFeed();
                    dismount();
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED));
                }

                L2PetData petData = getPetData(getMountNpcId());
                if (petData == null)
                {
                    return;
                }

                int[] foodIds = petData.getFood();
                if (foodIds.length == 0)
                {
                    return;
                }
                L2ItemInstance food = null;
                for (int id : foodIds)
                {
                    if (getPetInv() != null)
                    {
                        food = getPetInv().getItemByItemId(id);
                    }
                    else
                    {
                        food = getInventory().getItemByItemId(id);
                    }
                    if (food != null)
                    {
                        break;
                    }
                }

                if (food != null && isHungry())
                {
                    IItemHandler handler = ItemHandler.getInstance().getItemHandler(food.getEtcItem());
                    if (handler != null)
                    {
                        handler.useItem(L2PcInstance.this, food, false);
                        SystemMessage sm =
                                SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
                        sm.addItemName(food.getItemId());
                        sendPacket(sm);
                    }
                }
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "Mounted Pet [NpcId: " + getMountNpcId() + "] a feed task error has occurred", e);
            }
        }
    }

    protected synchronized void startFeed(int npcId)
    {
        _canFeed = npcId > 0;
        if (!isMounted())
        {
            return;
        }
        if (getPet() != null)
        {
            setCurrentFeed(getPet().getCurrentFed());
            _controlItemId = getPet().getControlObjectId();
            _petInv = getPet().getInventory();
            sendPacket(new SetupGauge(3, getCurrentFeed() * 10000 / getFeedConsume(),
                    getMaxFeed() * 10000 / getFeedConsume()));
            if (!isDead())
            {
                _mountFeedTask =
                        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
            }
        }
        else if (_canFeed && getFeedConsume() > 0)
        {
            setCurrentFeed(getMaxFeed());
            SetupGauge sg = new SetupGauge(3, getCurrentFeed() * 10000 / getFeedConsume(),
                    getMaxFeed() * 10000 / getFeedConsume());
            sendPacket(sg);
            if (!isDead())
            {
                _mountFeedTask =
                        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
            }
        }
    }

    protected synchronized void stopFeed()
    {
        if (_mountFeedTask != null)
        {
            _mountFeedTask.cancel(false);
            //ThreadPoolManager.getInstance().removeGeneral((Runnable)_mountFeedTask);
            _mountFeedTask = null;
            if (Config.DEBUG)
            {
                Log.fine("Pet [#" + _mountNpcId + "] feed task stop");
            }
        }
    }

    private final void clearPetData()
    {
        _data = null;
    }

    private final L2PetData getPetData(int npcId)
    {
        if (_data == null)
        {
            _data = PetDataTable.getInstance().getPetData(npcId);
        }
        return _data;
    }

    private final L2PetLevelData getPetLevelData(int npcId)
    {
        if (_leveldata == null)
        {
            _leveldata = PetDataTable.getInstance().getPetData(npcId).getPetLevelData(getMountLevel());
        }
        return _leveldata;
    }

    public int getCurrentFeed()
    {
        return _curFeed;
    }

    private int getFeedConsume()
    {
        if (getPetLevelData(_mountNpcId) == null)
        {
            return 0;
        }
        // if pet is attacking
        if (isAttackingNow())
        {
            return getPetLevelData(_mountNpcId).getPetFeedBattle();
        }
        else
        {
            return getPetLevelData(_mountNpcId).getPetFeedNormal();
        }
    }

    public void setCurrentFeed(int num)
    {
        if (getFeedConsume() == 0)
        {
            return;
        }
        _curFeed = num > getMaxFeed() ? getMaxFeed() : num;
        SetupGauge sg =
                new SetupGauge(3, getCurrentFeed() * 10000 / getFeedConsume(), getMaxFeed() * 10000 / getFeedConsume());
        sendPacket(sg);
    }

    private int getMaxFeed()
    {
        if (getPetLevelData(_mountNpcId) == null)
        {
            return 1;
        }
        return getPetLevelData(_mountNpcId).getPetMaxFeed();
    }

    private boolean isHungry()
    {
        if (_canFeed && getPetData(getMountNpcId()) != null && getPetLevelData(getMountNpcId()) != null)
        {
            return getCurrentFeed() < getPetData(getMountNpcId()).getHungry_limit() / 100f *
                    getPetLevelData(getMountNpcId()).getPetMaxFeed();
        }
        return false;
    }

    private class Dismount implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                dismount();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING, "Exception on dismount(): " + e.getMessage(), e);
            }
        }
    }

    public void enteredNoLanding(int delay)
    {
        _dismountTask = ThreadPoolManager.getInstance().scheduleGeneral(new L2PcInstance.Dismount(), delay * 1000);
    }

    public void exitedNoLanding()
    {
        if (_dismountTask != null)
        {
            _dismountTask.cancel(true);
            _dismountTask = null;
        }
    }

    public void storePetFood(int petId)
    {
        if (_controlItemId != 0 && petId != 0)
        {
            String req;
            req = "UPDATE pets SET fed=? WHERE item_obj_id = ?";
            Connection con = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement(req);
                statement.setInt(1, getCurrentFeed());
                statement.setInt(2, _controlItemId);
                statement.executeUpdate();
                statement.close();
                _controlItemId = 0;
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "Failed to store Pet [NpcId: " + petId + "] data", e);
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }
        }
    }

    /**
     * End of section for mounted pets
     */

    @Override
    public int getAttackElementValue(byte attribute)
    {
        int value = super.getAttackElementValue(attribute);

        // 20% if summon exist
        //if (!getSummons().isEmpty() && getCurrentClass().isSummoner())
        //	return value / 5;

        return value;
    }

    public void setIsInSiege(boolean b)
    {
        _isInSiege = b;
    }

    public boolean isInSiege()
    {
        return _isInSiege;
    }

    public FloodProtectors getFloodProtectors()
    {
        return getClient().getFloodProtectors();
    }

    public boolean isFlyingMounted()
    {
        return _isFlyingMounted;
    }

    public void setIsFlyingMounted(boolean val)
    {
        _isFlyingMounted = val;
        setIsFlying(val);
    }

    /**
     * Returns the Number of Charges this L2PcInstance got.
     *
     * @return
     */
    public int getCharges()
    {
        return _charges.get();
    }

    public void increaseCharges(int count, int max)
    {
        if (_charges.get() >= max)
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
            return;
        }
        else
        {
            // if no charges - start clear task
            if (_charges.get() == 0)
            {
                restartChargeTask();
            }
        }

        if (_charges.addAndGet(count) >= max)
        {
            _charges.set(max);
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
        }
        else
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
            sm.addNumber(_charges.get());
            sendPacket(sm);
        }

        sendPacket(new EtcStatusUpdate(this));
    }

    public boolean decreaseCharges(int count)
    {
        if (_charges.get() < count)
        {
            return false;
        }

        if (_charges.addAndGet(-count) == 0)
        {
            stopChargeTask();
        }

        sendPacket(new EtcStatusUpdate(this));
        return true;
    }

    public void clearCharges()
    {
        _charges.set(0);
        sendPacket(new EtcStatusUpdate(this));
    }

    /**
     * Starts/Restarts the ChargeTask to Clear Charges after 10 Mins.
     */
    private void restartChargeTask()
    {
        if (_chargeTask != null)
        {
            _chargeTask.cancel(false);
            _chargeTask = null;
        }
        _chargeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChargeTask(), 600000);
    }

    /**
     * Stops the Charges Clearing Task.
     */
    public void stopChargeTask()
    {
        if (_chargeTask != null)
        {
            _chargeTask.cancel(false);
            //ThreadPoolManager.getInstance().removeGeneral((Runnable)_chargeTask);
            _chargeTask = null;
        }
    }

    private class ChargeTask implements Runnable
    {

        @Override
        public void run()
        {
            clearCharges();
        }
    }

    public static class TeleportBookmark
    {
        public int _id, _x, _y, _z, _icon;
        public String _name, _tag;

        TeleportBookmark(int id, int x, int y, int z, int icon, String tag, String name)
        {
            _id = id;
            _x = x;
            _y = y;
            _z = z;
            _icon = icon;
            _name = name;
            _tag = tag;
        }
    }

    public void teleportBookmarkModify(int Id, int icon, String tag, String name)
    {
        int count = 0;
        int size = tpbookmark.size();
        while (size > count)
        {
            if (tpbookmark.get(count)._id == Id)
            {
                tpbookmark.get(count)._icon = icon;
                tpbookmark.get(count)._tag = tag;
                tpbookmark.get(count)._name = name;

                Connection con = null;

                try
                {

                    con = L2DatabaseFactory.getInstance().getConnection();
                    PreparedStatement statement = con.prepareStatement(UPDATE_TP_BOOKMARK);

                    statement.setInt(1, icon);
                    statement.setString(2, tag);
                    statement.setString(3, name);
                    statement.setInt(4, getObjectId());
                    statement.setInt(5, Id);

                    statement.execute();
                    statement.close();
                }
                catch (Exception e)
                {
                    Log.log(Level.WARNING, "Could not update character teleport bookmark data: " + e.getMessage(), e);
                }
                finally
                {
                    L2DatabaseFactory.close(con);
                }
            }
            count++;
        }

        sendPacket(new ExGetBookMarkInfoPacket(this));
    }

    public void teleportBookmarkDelete(int Id)
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(DELETE_TP_BOOKMARK);

            statement.setInt(1, getObjectId());
            statement.setInt(2, Id);

            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not delete character teleport bookmark data: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        int count = 0;
        int size = tpbookmark.size();

        while (size > count)
        {
            if (tpbookmark.get(count)._id == Id)
            {
                tpbookmark.remove(count);
                break;
            }
            count++;
        }

        sendPacket(new ExGetBookMarkInfoPacket(this));
    }

    public void teleportBookmarkGo(int Id)
    {
        if (!teleportBookmarkCondition(0))
        {
            return;
        }
        if (getInventory().getInventoryItemCount(13016, 0) == 0)
        {
            sendPacket(SystemMessage.getSystemMessage(2359));
            return;
        }
        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
        sm.addItemName(13016);
        sendPacket(sm);
        int count = 0;
        int size = tpbookmark.size();
        while (size > count)
        {
            if (tpbookmark.get(count)._id == Id)
            {
                destroyItem("Consume", getInventory().getItemByItemId(13016).getObjectId(), 1, null, false);
                this.teleToLocation(tpbookmark.get(count)._x, tpbookmark.get(count)._y, tpbookmark.get(count)._z);
                break;
            }
            count++;
        }
        sendPacket(new ExGetBookMarkInfoPacket(this));
    }

    public boolean teleportBookmarkCondition(int type)
    {
        if (isInCombat())
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE));
            return false;
        }
        else if (isInSiege() || getSiegeState() != 0)
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING));
            return false;
        }
        else if (isInDuel())
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL));
            return false;
        }
        else if (isFlying())
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING));
            return false;
        }
        else if (isInOlympiadMode())
        {
            sendPacket(SystemMessage.getSystemMessage(
                    SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH));
            return false;
        }
        else if (isParalyzed())
        {
            sendPacket(SystemMessage
                    .getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_PARALYZED));
            return false;
        }
        else if (isDead())
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD));
            return false;
        }
        else if (isInBoat() || isInAirShip() || isInJail() || isInsideZone(ZONE_NOSUMMONFRIEND))
        {
            if (type == 0)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA));
            }
            else if (type == 1)
            {
                sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA));
            }
            return false;
        }
        else if (isInWater())
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER));
            return false;
        }
        else if (type == 1 && (isInsideZone(ZONE_SIEGE) || isInsideZone(ZONE_CLANHALL) || isInsideZone(ZONE_JAIL) ||
                isInsideZone(ZONE_CASTLE) || isInsideZone(ZONE_NOSUMMONFRIEND) || isInsideZone(ZONE_FORT)))
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA));
            return false;
        }
        else if (isInsideZone(ZONE_NOBOOKMARK))
        {
            if (type == 0)
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA));
            }
            else if (type == 1)
            {
                sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA));
            }
            return false;
        }
		/* TODO: Instant Zone still not implement
		else if (this.isInsideZone(ZONE_INSTANT))
		{
			sendPacket(SystemMessage.getSystemMessage(2357));
			return;
		}
		 */
        else
        {
            return true;
        }
    }

    public void teleportBookmarkAdd(int x, int y, int z, int icon, String tag, String name)
    {
        if (!teleportBookmarkCondition(1))
        {
            return;
        }

        if (tpbookmark.size() >= _bookmarkslot)
        {
            sendPacket(SystemMessage.getSystemMessage(2358));
            return;
        }

        if (getInventory().getInventoryItemCount(20033, 0) == 0)
        {
            sendPacket(SystemMessage.getSystemMessage(6501));
            return;
        }

        int count = 0;
        int id = 1;
        ArrayList<Integer> idlist = new ArrayList<>();

        int size = tpbookmark.size();

        while (size > count)
        {
            idlist.add(tpbookmark.get(count)._id);
            count++;
        }

        for (int i = 1; i < 10; i++)
        {
            if (!idlist.contains(i))
            {
                id = i;
                break;
            }
        }

        TeleportBookmark tpadd = new TeleportBookmark(id, x, y, z, icon, tag, name);
        if (tpbookmark == null)
        {
            tpbookmark = new ArrayList<>();
        }

        tpbookmark.add(tpadd);

        destroyItem("Consume", getInventory().getItemByItemId(20033).getObjectId(), 1, null, false);

        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
        sm.addItemName(20033);
        sendPacket(sm);

        Connection con = null;

        try
        {

            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(INSERT_TP_BOOKMARK);

            statement.setInt(1, getObjectId());
            statement.setInt(2, id);
            statement.setInt(3, x);
            statement.setInt(4, y);
            statement.setInt(5, z);
            statement.setInt(6, icon);
            statement.setString(7, tag);
            statement.setString(8, name);

            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not insert character teleport bookmark data: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        sendPacket(new ExGetBookMarkInfoPacket(this));
    }

    public void restoreTeleportBookmark()
    {
        if (tpbookmark == null)
        {
            tpbookmark = new ArrayList<>();
        }
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(RESTORE_TP_BOOKMARK);
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();

            while (rset.next())
            {
                tpbookmark.add(new TeleportBookmark(rset.getInt("Id"), rset.getInt("x"), rset.getInt("y"),
                        rset.getInt("z"), rset.getInt("icon"), rset.getString("tag"), rset.getString("name")));
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Failed restoing character teleport bookmark.", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    @Override
    public void sendInfo(L2PcInstance activeChar)
    {
        int relation1 = getRelation(activeChar);
        int relation2 = activeChar.getRelation(this);

        if (getAppearance().getInvisible() && !activeChar.isGM() &&
                (relation1 & RelationChanged.RELATION_HAS_PARTY) == 0)
        {
            return;
        }

        if (isInBoat())
        {
            getPosition().setWorldPosition(getBoat().getPosition().getWorldPosition());

            activeChar.sendPacket(new CharInfo(this));
            Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
            if (oldrelation != null && oldrelation != relation1)
            {
                activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
                if (getPet() != null)
                {
                    activeChar.sendPacket(new RelationChanged(getPet(), relation1, isAutoAttackable(activeChar)));
                }
                for (L2SummonInstance summon : getSummons())
                {
                    activeChar.sendPacket(new RelationChanged(summon, relation1, isAutoAttackable(activeChar)));
                }
            }
            oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
            if (oldrelation != null && oldrelation != relation2)
            {
                sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
                if (activeChar.getPet() != null)
                {
                    sendPacket(new RelationChanged(activeChar.getPet(), relation2, activeChar.isAutoAttackable(this)));
                }
                for (L2SummonInstance summon : getSummons())
                {
                    activeChar.sendPacket(new RelationChanged(summon, relation2, activeChar.isAutoAttackable(this)));
                }
            }
            activeChar.sendPacket(new GetOnVehicle(getObjectId(), getBoat().getObjectId(), getInVehiclePosition()));
        }
        else if (isInAirShip())
        {
            getPosition().setWorldPosition(getAirShip().getPosition().getWorldPosition());

            activeChar.sendPacket(new CharInfo(this));
            Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
            if (oldrelation != null && oldrelation != relation1)
            {
                activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
                if (getPet() != null)
                {
                    activeChar.sendPacket(new RelationChanged(getPet(), relation1, isAutoAttackable(activeChar)));
                }
                for (L2SummonInstance summon : getSummons())
                {
                    activeChar.sendPacket(new RelationChanged(summon, relation1, isAutoAttackable(activeChar)));
                }
            }
            oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
            if (oldrelation != null && oldrelation != relation2)
            {
                sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
                if (activeChar.getPet() != null)
                {
                    sendPacket(new RelationChanged(activeChar.getPet(), relation2, activeChar.isAutoAttackable(this)));
                }
                for (L2SummonInstance summon : getSummons())
                {
                    activeChar.sendPacket(new RelationChanged(summon, relation2, activeChar.isAutoAttackable(this)));
                }
            }
            activeChar.sendPacket(new ExGetOnAirShip(this, getAirShip()));
        }
        else
        {
            activeChar.sendPacket(new CharInfo(this));
            Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
            if (oldrelation != null && oldrelation != relation1)
            {
                activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
                if (getPet() != null)
                {
                    activeChar.sendPacket(new RelationChanged(getPet(), relation1, isAutoAttackable(activeChar)));
                }
                for (L2SummonInstance summon : getSummons())
                {
                    activeChar.sendPacket(new RelationChanged(summon, relation1, isAutoAttackable(activeChar)));
                }
            }
            oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
            if (oldrelation != null && oldrelation != relation2)
            {
                sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
                if (activeChar.getPet() != null)
                {
                    sendPacket(new RelationChanged(activeChar.getPet(), relation2, activeChar.isAutoAttackable(this)));
                }
                for (L2SummonInstance summon : getSummons())
                {
                    activeChar.sendPacket(new RelationChanged(summon, relation2, activeChar.isAutoAttackable(this)));
                }
            }
        }
        if (getMountType() == 4)
        {
            // TODO: Remove when horse mounts fixed
            //activeChar.sendPacket(new Ride(this, false, 0));
            activeChar.sendPacket(new Ride(this, true, getMountNpcId()));
        }

        switch (getPrivateStoreType())
        {
            case L2PcInstance.STORE_PRIVATE_SELL:
            case L2PcInstance.STORE_PRIVATE_CUSTOM_SELL:
                activeChar.sendPacket(new PrivateStoreMsgSell(this));
                break;
            case L2PcInstance.STORE_PRIVATE_PACKAGE_SELL:
                activeChar.sendPacket(new ExPrivateStoreSetWholeMsg(this));
                break;
            case L2PcInstance.STORE_PRIVATE_BUY:
                activeChar.sendPacket(new PrivateStoreMsgBuy(this));
                break;
            case L2PcInstance.STORE_PRIVATE_MANUFACTURE:
                activeChar.sendPacket(new RecipeShopMsg(this));
                break;
        }

        if (activeChar.getParty() != null)
        {
            int tag = activeChar.getParty().getTag(getObjectId());
            if (tag > 0)
            {
                activeChar.sendPacket(new ExTacticalSign(getObjectId(), tag));
            }
        }
    }

    public void showQuestMovie(int id)
    {
        if (_movieId > 0) //already in movie
        {
            return;
        }
        abortAttack();
        abortCast();
        stopMove(null);
        _movieId = id;
        sendPacket(new ExStartScenePlayer(id));
    }

    public boolean isAllowedToEnchantSkills()
    {
        if (isLocked())
        {
            return false;
        }
        if (isTransformed())
        {
            return false;
        }
        if (AttackStanceTaskManager.getInstance().getAttackStanceTask(this))
        {
            return false;
        }
        if (isCastingNow() || isCastingSimultaneouslyNow())
        {
            return false;
        }
        if (isInBoat() || isInAirShip())
        {
            return false;
        }
        return true;
    }

    /**
     * Set the _creationTime of the L2PcInstance.<BR><BR>
     */
    public void setCreateTime(long creationTime)
    {
        _creationTime = creationTime;
    }

    /**
     * Return the _creationTime of the L2PcInstance.<BR><BR>
     */
    public long getCreateTime()
    {
        return _creationTime;
    }

    /**
     * @return number of days to char birthday.<BR><BR>
     */
    public int checkBirthDay()
    {
        QuestState _state = getQuestState("CharacterBirthday");
        Calendar now = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        birth.setTimeInMillis(_creationTime);

        if (_state != null && _state.getInt("Birthday") > now.get(Calendar.YEAR))
        {
            return -1;
        }

        // "Characters with a February 29 creation date will receive a gift on February 28."
        if (birth.get(Calendar.DAY_OF_MONTH) == 29 && birth.get(Calendar.MONTH) == 1)
        {
            birth.add(Calendar.HOUR_OF_DAY, -24);
        }

        if (now.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                now.get(Calendar.DAY_OF_MONTH) == birth.get(Calendar.DAY_OF_MONTH) &&
                now.get(Calendar.YEAR) != birth.get(Calendar.YEAR))
        {
            return 0;
        }
        else
        {
            int i;
            for (i = 1; i < 6; i++)
            {
                now.add(Calendar.HOUR_OF_DAY, 24);
                if (now.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                        now.get(Calendar.DAY_OF_MONTH) == birth.get(Calendar.DAY_OF_MONTH) &&
                        now.get(Calendar.YEAR) != birth.get(Calendar.YEAR))
                {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * list of character friends
     */
    private List<Integer> _friendList = new ArrayList<>();

    public List<Integer> getFriendList()
    {
        return _friendList;
    }

    public void restoreFriendList()
    {
        _friendList.clear();

        Connection con = null;

        try
        {
            String sqlQuery = "SELECT friendId, memo FROM character_friends WHERE charId=? AND relation=0";

            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();

            int friendId;
            while (rset.next())
            {
                friendId = rset.getInt("friendId");
                if (friendId == getObjectId())
                {
                    continue;
                }
                _friendList.add(friendId);
                if (rset.getString("memo") != null)
                {
                    _friendMemo.put(friendId, rset.getString("memo"));
                }
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error found in " + getName() + "'s FriendList: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void restoreBlockList()
    {

        Connection con = null;

        try
        {
            String sqlQuery = "SELECT friendId, memo FROM character_friends WHERE charId=? AND relation=1";

            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();

            int friendId;
            while (rset.next())
            {
                friendId = rset.getInt("friendId");
                if (friendId == getObjectId())
                {
                    continue;
                }
                if (rset.getString("memo") != null)
                {
                    _blockMemo.put(friendId, rset.getString("memo"));
                }
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error found in " + getName() + "'s BlockList: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     *
     */
    private void notifyFriends()
    {
        FriendStatusPacket pkt = new FriendStatusPacket(getObjectId());
        for (int id : _friendList)
        {
            L2PcInstance friend = L2World.getInstance().getPlayer(id);
            if (friend != null)
            {
                friend.sendPacket(pkt);
            }
        }
    }

    /**
     * @return the _silenceMode
     */
    public boolean isSilenceMode()
    {
        return _silenceMode;
    }

    /**
     * @param mode the _silenceMode to set
     */
    public void setSilenceMode(boolean mode)
    {
        _silenceMode = mode;
        sendPacket(new EtcStatusUpdate(this));
    }

    private void storeRecipeShopList()
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement;
            L2ManufactureList list = getCreateList();

            if (list != null && list.size() > 0)
            {
                int _position = 1;
                statement = con.prepareStatement("DELETE FROM character_recipeshoplist WHERE charId=? ");
                statement.setInt(1, getObjectId());
                statement.execute();
                statement.close();

                PreparedStatement statement2 = con.prepareStatement(
                        "INSERT INTO character_recipeshoplist (charId, Recipeid, Price, Pos) VALUES (?, ?, ?, ?)");
                for (L2ManufactureItem item : list.getList())
                {
                    statement2.setInt(1, getObjectId());
                    statement2.setInt(2, item.getRecipeId());
                    statement2.setLong(3, item.getCost());
                    statement2.setInt(4, _position);
                    statement2.execute();
                    statement2.clearParameters();
                    _position++;
                }
                statement2.close();
            }
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not store recipe shop for playerID " + getObjectId() + ": ", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    private void restoreRecipeShopList()
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "SELECT Recipeid,Price FROM character_recipeshoplist WHERE charId=? ORDER BY Pos ASC");
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();

            L2ManufactureList createList = new L2ManufactureList();
            while (rset.next())
            {
                createList.add(new L2ManufactureItem(rset.getInt("Recipeid"), rset.getLong("Price")));
            }
            setCreateList(createList);
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not restore recipe shop list data for playerId: " + getObjectId(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public double getCollisionRadius()
    {
        if (getAppearance().getSex())
        {
            return getVisibleTemplate().fCollisionRadiusFemale;
        }
        else
        {
            return getVisibleTemplate().fCollisionRadius;
        }
    }

    public double getCollisionHeight()
    {
        if (getAppearance().getSex())
        {
            return getVisibleTemplate().fCollisionHeightFemale;
        }
        else
        {
            return getVisibleTemplate().fCollisionHeight;
        }
    }

    public final int getClientX()
    {
        return _clientX;
    }

    public final int getClientY()
    {
        return _clientY;
    }

    public final int getClientZ()
    {
        return _clientZ;
    }

    public final int getClientHeading()
    {
        return _clientHeading;
    }

    public final void setClientX(int val)
    {
        _clientX = val;
    }

    public final void setClientY(int val)
    {
        _clientY = val;
    }

    public final void setClientZ(int val)
    {
        _clientZ = val;
    }

    public final void setClientHeading(int val)
    {
        _clientHeading = val;
    }

    /**
     * Return true if character falling now
     * On the start of fall return false for correct coord sync !
     */
    public final boolean isFalling(int z)
    {
        if (isDead() || isFlying() || isFlyingMounted() || isInsideZone(ZONE_WATER))
        {
            return false;
        }

        if (System.currentTimeMillis() < _fallingTimestamp)
        {
            return true;
        }

        final int deltaZ = getZ() - z;
        if (deltaZ <= getBaseTemplate().getFallHeight())
        {
            return false;
        }

        final int damage = (int) Formulas.calcFallDam(this, deltaZ);
        if (damage > 0)
        {
            reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false, true, null);
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
        }

        setFalling();

        return false;
    }

    /**
     * Set falling timestamp
     */
    public final void setFalling()
    {
        _fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
    }

    /**
     * @return the _movieId
     */
    public int getMovieId()
    {
        return _movieId;
    }

    public void setMovieId(int id)
    {
        _movieId = id;
    }

    /**
     * Update last item auction request timestamp to current
     */
    public void updateLastItemAuctionRequest()
    {
        _lastItemAuctionInfoRequest = System.currentTimeMillis();
    }

    /**
     * Returns true if receiving item auction requests
     * (last request was in 2 seconds before)
     */
    public boolean isItemAuctionPolling()
    {
        return System.currentTimeMillis() - _lastItemAuctionInfoRequest < 2000;
    }

    /* (non-Javadoc)
	 * @see l2server.gameserver.model.actor.L2Character#isMovementDisabled()
	 */
    @Override
    public boolean isMovementDisabled()
    {
        return super.isMovementDisabled() || _movieId > 0;
    }

    private void restoreUISettings()
    {
        _uiKeySettings = new L2UIKeysSettings(this);
    }

    private void storeUISettings()
    {
        if (_uiKeySettings == null)
        {
            return;
        }

        if (!_uiKeySettings.isSaved())
        {
            _uiKeySettings.saveInDB();
        }
    }

    public L2UIKeysSettings getUISettings()
    {
        return _uiKeySettings;
    }

    public String getHtmlPrefix()
    {
        return null;
    }

    public long getOfflineStartTime()
    {
        return _offlineShopStart;
    }

    public void setOfflineStartTime(long time)
    {
        _offlineShopStart = time;
    }

    /**
     * Remove player from BossZones (used on char logout/exit)
     */
    public void removeFromBossZone()
    {
        try
        {
            for (L2BossZone _zone : GrandBossManager.getInstance().getZones())
            {
                if (_zone == null)
                {
                    continue;
                }
                _zone.removePlayer(this);
            }
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Exception on removeFromBossZone(): " + e.getMessage(), e);
        }
    }

    /**
     * Check all player skills for skill level. If player level is lower than skill learn level - 9, skill level is decreased to next possible level.
     */
    public void checkPlayerSkills()
    {
        for (int id : _skills.keySet())
        {
            int level = getSkillLevelHash(id);
            if (level >= 100) // enchanted skill
            {
                level = SkillTable.getInstance().getMaxLevel(id);
            }
            L2SkillLearn learn = SkillTreeTable.getInstance().getSkillLearnBySkillIdLevel(getCurrentClass(), id, level);
            // not found - not a learn skill?
            if (learn == null)
            {
                continue;
            }
            else
            {
                // player level is too low for such skill level
                if (getLevel() < learn.getMinLevel() - 9)
                {
                    deacreaseSkillLevel(id);
                }
            }
        }
    }

    private void deacreaseSkillLevel(int id)
    {
        int nextLevel = -1;
        for (L2SkillLearn sl : PlayerClassTable.getInstance().getClassById(getCurrentClass().getId()).getSkills()
                .values())
        {
            if (sl.getId() == id && nextLevel < sl.getLevel() && getLevel() >= sl.getMinLevel() - 9)
            {
                // next possible skill level
                nextLevel = sl.getLevel();
            }
        }

        if (nextLevel == -1) // there is no lower skill
        {
            if (!Config.isServer(Config.TENKAI))
            {
                Log.info("Removing skill id " + id + " level " + getSkillLevelHash(id) + " from player " + this);
            }
            removeSkill(_skills.get(id), true);
        }
        else
        // replace with lower one
        {
            if (!Config.isServer(Config.TENKAI))
            {
                Log.info("Decreasing skill id " + id + " from " + getSkillLevelHash(id) + " to " + nextLevel + " for " +
                        this);
            }
            addSkill(SkillTable.getInstance().getInfo(id, nextLevel), true);
        }
    }

    public boolean canMakeSocialAction()
    {
        if (getPrivateStoreType() == 0 && getActiveRequester() == null && !isAlikeDead() &&
                (!isAllSkillsDisabled() || isInDuel()) && !isCastingNow() && !isCastingSimultaneouslyNow())
        //&& getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE
        //&& !AttackStanceTaskManager.getInstance().getAttackStanceTask(this)
        //&& !isInOlympiadMode())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setMultiSocialAction(int id, int targetId)
    {
        _multiSociaAction = id;
        _multiSocialTarget = targetId;
    }

    public int getMultiSociaAction()
    {
        return _multiSociaAction;
    }

    public int getMultiSocialTarget()
    {
        return _multiSocialTarget;
    }

    public List<TeleportBookmark> getTpbookmark()
    {
        return tpbookmark;
    }

    public int getBookmarkslot()
    {
        return _bookmarkslot;
    }

    /**
     * @return
     */
    public int getQuestInventoryLimit()
    {
        return Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
    }

    public boolean canAttackCharacter(L2Character cha)
    {
        if (cha instanceof L2Attackable)
        {
            return true;
        }
        else if (cha instanceof L2Playable)
        {
            if (cha.isInsideZone(L2Character.ZONE_PVP) && !cha.isInsideZone(L2Character.ZONE_SIEGE))
            {
                return true;
            }

            L2PcInstance target;
            if (cha instanceof L2Summon)
            {
                target = ((L2Summon) cha).getOwner();
            }
            else
            {
                target = (L2PcInstance) cha;
            }

            if (isInDuel() && target.isInDuel() && target.getDuelId() == getDuelId())
            {
                return true;
            }
            else if (isInParty() && target.isInParty())
            {
                if (getParty() == target.getParty())
                {
                    return false;
                }
                if ((getParty().getCommandChannel() != null || target.getParty().getCommandChannel() != null) &&
                        getParty().getCommandChannel() == target.getParty().getCommandChannel())
                {
                    return false;
                }
            }
            else if (getClan() != null && target.getClan() != null)
            {
                if (getClan() == target.getClan())
                {
                    return false;
                }
                if ((getAllyId() > 0 || target.getAllyId() > 0) && getAllyId() == target.getAllyId())
                {
                    return false;
                }
                if (getClan().isAtWarWith(target.getClan()) && target.getClan().isAtWarWith(getClan()))
                {
                    return true;
                }
            }
            else if (getClan() == null || target.getClan() == null)
            {
                if (target.getPvpFlag() == 0 && target.getReputation() == 0)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Test if player inventory is under 90% capacity
     *
     * @param includeQuestInv check also quest inventory
     * @return
     */
    public boolean isInventoryUnder90(boolean includeQuestInv)
    {
        if (getInventory().getSize(false) <= getInventoryLimit() * 0.9)
        {
            if (includeQuestInv)
            {
                if (getInventory().getSize(true) <= getQuestInventoryLimit() * 0.9)
                {
                    return true;
                }
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    public boolean havePetInvItems()
    {
        return _petItems;
    }

    public void setPetInvItems(boolean haveit)
    {
        _petItems = haveit;
    }

    private void checkPetInvItems()
    {
        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "SELECT object_id FROM `items` WHERE `owner_id`=? AND (`loc`='PET' OR `loc`='PET_EQUIP') LIMIT 1;");
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();
            if (rset.next() && rset.getInt("object_id") > 0)
            {
                setPetInvItems(true);
            }
            else
            {
                setPetInvItems(false);
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not check Items in Pet Inventory for playerId: " + getObjectId(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public String getAdminConfirmCmd()
    {
        return _adminConfirmCmd;
    }

    public void setAdminConfirmCmd(String adminConfirmCmd)
    {
        _adminConfirmCmd = adminConfirmCmd;
    }

    public void setBlockCheckerArena(byte arena)
    {
        _handysBlockCheckerEventArena = arena;
    }

    public int getBlockCheckerArena()
    {
        return _handysBlockCheckerEventArena;
    }

    /**
     * Load L2PcInstance Recommendations data.<BR><BR>
     */
    private long loadRecommendations()
    {
        long _time_left = 0;
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "SELECT rec_have,rec_left,time_left FROM character_reco_bonus WHERE charId=? LIMIT 1");
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();

            if (rset.next())
            {
                setRecomHave(rset.getInt("rec_have"));
                setRecomLeft(rset.getInt("rec_left"));
                _time_left = rset.getLong("time_left");
            }
            else
            {
                _time_left = 3600000;
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not restore Recommendations for player: " + getObjectId(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
        return _time_left;
    }

    /**
     * Update L2PcInstance Recommendations data.<BR><BR>
     */
    public void storeRecommendations()
    {
        long _recoTaskEnd = 0;
        if (_recoBonusTask != null)
        {
            _recoTaskEnd = Math.max(0, _recoBonusTask.getDelay(TimeUnit.MILLISECONDS));
        }

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO character_reco_bonus (charId,rec_have,rec_left,time_left) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE rec_have=?, rec_left=?, time_left=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, getRecomHave());
            statement.setInt(3, getRecomLeft());
            statement.setLong(4, _recoTaskEnd);
            // Update part
            statement.setInt(5, getRecomHave());
            statement.setInt(6, getRecomLeft());
            statement.setLong(7, _recoTaskEnd);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Could not update Recommendations for player: " + getObjectId(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void checkRecoBonusTask()
    {
        // Load data
        long _task_time = loadRecommendations();

        if (_task_time > 0)
        {
            // Add 20 recos on first login
            if (_task_time == 3600000)
            {
                setRecomLeft(getRecomLeft() + 20);
            }
            // If player have some timeleft, start bonus task
            _recoBonusTask = ThreadPoolManager.getInstance().scheduleGeneral(new RecoBonusTaskEnd(), _task_time);
        }
        // Create task to give new recommendations
        _recoGiveTask =
                ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RecoGiveTask(), 7200000, 3600000);
        // Store new data
        storeRecommendations();
    }

    public void stopRecoBonusTask()
    {
        if (_recoBonusTask != null)
        {
            _recoBonusTask.cancel(false);
            _recoBonusTask = null;
        }
    }

    public void stopRecoGiveTask()
    {
        if (_recoGiveTask != null)
        {
            _recoGiveTask.cancel(false);
            _recoGiveTask = null;
        }
    }

    private class RecoGiveTask implements Runnable
    {
        @Override
        public void run()
        {
            int reco_to_give;
            // 10 recommendations to give out after 2 hours of being logged in
            // 1 more recommendation to give out every hour after that.
            if (_recoTwoHoursGiven)
            {
                reco_to_give = 1;
            }
            else
            {
                reco_to_give = 10;
            }

            _recoTwoHoursGiven = true;

            setRecomLeft(getRecomLeft() + reco_to_give);

            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS);
            sm.addNumber(reco_to_give);
            L2PcInstance.this.sendPacket(sm);
            L2PcInstance.this.sendPacket(new UserInfo(L2PcInstance.this));
        }
    }

    private class RecoBonusTaskEnd implements Runnable
    {
        @Override
        public void run()
        {
            L2PcInstance.this.sendPacket(new ExVoteSystemInfo(L2PcInstance.this));
        }
    }

    public int getRecomBonusTime()
    {
        if (_recoBonusTask != null)
        {
            return (int) Math.max(0, _recoBonusTask.getDelay(TimeUnit.SECONDS));
        }

        return 0;
    }

    public int getRecomBonusType()
    {
        // Maintain = 1
        //return 0;
        return getRecomBonusTime() == 0 ? 0 : 1;
    }

    // Summons that this character summoned before logging out
    private List<Integer> _lastSummons = new ArrayList<>();

    private void storeLastSummons()
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            // Delete all current stored effects for char to avoid dupe
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_last_summons WHERE charId = ?");

            statement.setInt(1, getObjectId());
            statement.execute();
            statement.close();

            // Store all effect data along with calulated remaining
            // reuse delays for matching skills. 'restore_type'= 0.
            statement = con.prepareStatement(
                    "INSERT INTO character_last_summons (charId, summonIndex, npcId) VALUES (?, ?, ?)");

            int i = 0;
            for (int summonId : _lastSummons)
            {
                statement.setInt(1, getObjectId());
                statement.setInt(2, i);
                statement.setInt(3, summonId);
                statement.execute();
                i++;
            }
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not store last summons data: ", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void restoreLastSummons()
    {
        _lastSummons.clear();

        Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement =
                    con.prepareStatement("SELECT npcId FROM character_last_summons WHERE charId=?");
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();

            while (rset.next())
            {
                int npcId = rset.getInt("npcId");
                _lastSummons.add(npcId);
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error found in " + getName() + "'s last summons' list: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void summonLastSummons()
    {
        for (int petId : _lastSummons)
        {
            int skillLevel = getSkillLevelHash(petId);
            if (skillLevel == -1)
            {
                continue;
            }

            L2Skill skill = SkillTable.getInstance().getInfo(petId, skillLevel);
            if (skill instanceof L2SkillSummon)
            {
                boolean canSummon = true;
                if (((L2SkillSummon) skill).getSummonPoints() > 0 &&
                        getSpentSummonPoints() + ((L2SkillSummon) skill).getSummonPoints() > getMaxSummonPoints())
                {
                    canSummon = false;
                }

                if (getSpentSummonPoints() == 0 && !getSummons().isEmpty())
                {
                    canSummon = false;
                }

                if (canSummon)
                {
                    L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(((L2SkillSummon) skill).getNpcId());
                    L2SummonInstance summon =
                            new L2SummonInstance(IdFactory.getInstance().getNextId(), npcTemplate, this, skill);

                    summon.setName(npcTemplate.Name);
                    summon.setTitle(getName());
                    summon.setExpPenalty(0);
                    if (summon.getLevel() > Config.MAX_LEVEL)
                    {
                        summon.getStat().setExp(Experience.getAbsoluteExp(Config.MAX_LEVEL));
                        Log.warning("Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() +
                                " has a level above " + Config.MAX_LEVEL + ". Please rectify.");
                    }
                    else
                    {
                        summon.getStat().setExp(Experience.getAbsoluteExp(summon.getLevel()));
                    }
                    summon.setCurrentHp(summon.getMaxHp());
                    summon.setCurrentMp(summon.getMaxMp());
                    summon.setHeading(getHeading());
                    summon.setRunning();
                    addSummon(summon);

                    summon.spawnMe(getX() + 50, getY() + 100, getZ());
                    summon.restoreEffects();
                }
            }
        }

        _lastSummons.clear();
    }

    public void setOlyGivenDmg(int olyGivenDmg)
    {
        _olyGivenDmg = olyGivenDmg;
    }

    public int getOlyGivenDmg()
    {
        return _olyGivenDmg;
    }

    public boolean isAtWarWithCastle(int castleId)
    {
        if (getClan() == null)
        {
            return false;
        }
        int clanId = 0;
        String consultaCastell = "SELECT clan_id FROM clan_data WHERE hasCastle=?";
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(consultaCastell);
            statement.setInt(1, castleId);
            ResultSet rset = statement.executeQuery();

            while (rset.next())
            {
                clanId = rset.getInt("clan_id");
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.severe("No s'ha pogut agafar el clan del castell " + castleId + ": " + e);
        }

        finally
        {
            L2DatabaseFactory.close(con);
        }
        return getClan().getWarList().contains(clanId);
    }

    public void setIsItemId(boolean isItemId)
    {
        _isItemId = isItemId;
    }

    public boolean isItemId()
    {
        return _isItemId;
    }

    public EventTeam getCtfFlag()
    {
        return _ctfFlag;
    }

    public void setCtfFlag(EventTeam team)
    {
        _ctfFlag = team;
    }

    public int getEventPoints()
    {
        return _eventPoints;
    }

    public void setEventPoints(int eventPoints)
    {
		/*if (eventPoints < -1) eventPoints += 5000;
		if (eventPoints >= 0)
		{
			if (!_titleModified)
			{
				_originalTitle = getTitle();
			  _titleModified = true;
			}
			int titleId = 40153;
			if (TenkaiEvent.tipus == 1 || TenkaiEvent.tipus == 5)
				titleId = 40154;
			else if (TenkaiEvent.tipus == 6)
				titleId = 40155;
			CoreMessage cm = new CoreMessage(titleId);
			cm.addNumber(eventPoints);
			setTitle(cm.renderMsg(getLang()));
			broadcastTitleInfo();
		}
		else if (_titleModified)
		{
			setTitle(_originalTitle);
			broadcastTitleInfo();
			_titleModified = false;
		}*/
        _eventPoints = eventPoints;
    }

    public void addEventPoints(int eventPoints)
    {
        _eventPoints += eventPoints;
    }

    // Has moved?
    private boolean _hasMoved;

    public boolean hasMoved()
    {
        return _hasMoved;
    }

    public void setHasMoved(boolean hasMoved)
    {
        _hasMoved = hasMoved;
    }

    class HasMovedTask implements Runnable
    {
        @Override
        public void run()
        {
            L2PcInstance player = L2PcInstance.this;
            EventInstance event = player.getEvent();
            if (isPlayingEvent())
            {
                if (!_hasMoved && !isDead() && !event.isType(EventType.VIP) && !event.isType(EventType.KingOfTheHill) &&
                        !event.isType(EventType.SimonSays) && !player.isSleeping())
                {
                    player.sendPacket(
                            new CreatureSay(0, Say2.TELL, "Instanced Events", "We don't like idle participants!"));
                    event.removeParticipant(player.getObjectId());
                    new EventTeleporter(player, new Point3D(0, 0, 0), true, true);
                }
                else
                {
                    _hasMoved = false;
                    ThreadPoolManager.getInstance().scheduleGeneral(this, 120000L);
                }
            }
        }
    }

    public void startHasMovedTask()
    {
        _hasMoved = false;
        ThreadPoolManager.getInstance().scheduleGeneral(new HasMovedTask(), 50000L);
    }

    public void eventSaveData()
    {
        int i = 0;
        for (L2Abnormal effect : getAllEffects())
        {
            if (i >= EVENT_SAVED_EFFECTS_SIZE)
            {
                break;
            }
            _eventSavedEffects[i] = effect;
            i++;
        }
        if (getPet() != null)
        {
            i = 0;
            for (L2Abnormal effect : getPet().getAllEffects())
            {
                if (i >= EVENT_SAVED_EFFECTS_SIZE)
                {
                    break;
                }
                _eventSavedSummonEffects[i] = effect;
                i++;
            }
        }
        _eventSavedPosition = new Point3D(getPosition().getX(), getPosition().getY(), getPosition().getZ());
        _eventSavedTime = TimeController.getGameTicks();

		/*for (L2ItemInstance temp : getInventory().getAugmentedItems())
		{
			if (temp != null && temp.isEquipped())
				removeSkill(temp.getAugmentation().getSkill());
		}*/
    }

    public void eventRestoreBuffs()
    {
        //restoreEffects();
        L2Object[] targets = new L2Character[]{this};
        for (int i = 0; i < EVENT_SAVED_EFFECTS_SIZE; i++)
        {
            if (_eventSavedEffects[i] != null)
            {
                restoreBuff(_eventSavedEffects[i], targets);
            }
        }
        if (getPet() != null)
        {
            targets = new L2Character[]{getPet()};
            for (int i = 0; i < EVENT_SAVED_EFFECTS_SIZE; i++)
            {
                if (_eventSavedSummonEffects[i] != null)
                {
                    restoreBuff(_eventSavedSummonEffects[i], targets);
                }
            }
        }
        setCurrentHp(getMaxHp());
    }

    private void restoreBuff(L2Abnormal buff, L2Object[] targets)
    {
        int skillId = buff.getSkill().getId();
        int skillLvl = buff.getLevelHash();
        int effectCount = buff.getCount();
        int effectCurTime =
                buff.getTime() - (TimeController.getGameTicks() - _eventSavedTime) / TimeController.TICKS_PER_SECOND;

        if (skillId == -1 || effectCount == -1 || effectCurTime < 30 || effectCurTime >= buff.getDuration())
        {
            return;
        }

        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
        ISkillHandler IHand = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
        if (IHand != null)
        {
            IHand.useSkill(this, skill, targets);
        }
        else
        {
            skill.useSkill(this, targets);
        }

        for (L2Abnormal effect : getAllEffects())
        {
            if (effect != null && effect.getSkill() != null && effect.getSkill().getId() == skillId)
            {
                effect.setCount(effectCount);
                effect.setFirstTime(effectCurTime);
            }
        }
    }

    public long getDeathPenalty(boolean atwar)
    {
        // Get the level of the L2PcInstance
        final int lvl = getLevel();

        if (lvl == 85)
        {
            return 0;
        }

        byte level = (byte) getLevel();

        int clan_luck = getSkillLevelHash(L2Skill.SKILL_CLAN_LUCK);

        double clan_luck_modificator = 1.0;
        switch (clan_luck)
        {
            case 3:
                clan_luck_modificator = 0.5;
                break;
            case 2:
                clan_luck_modificator = 0.5;
                break;
            case 1:
                clan_luck_modificator = 0.5;
                break;
            default:
                clan_luck_modificator = 1.0;
                break;
        }

        //The death steal you some Exp
        double percentLost = 1.0 * clan_luck_modificator;

        switch (level)
        {
            case 78:
                percentLost = 1.5 * clan_luck_modificator;
                break;
            case 77:
                percentLost = 2.0 * clan_luck_modificator;
                break;
            case 76:
                percentLost = 2.5 * clan_luck_modificator;
                break;
            default:
                if (level < 40)
                {
                    percentLost = 7.0 * clan_luck_modificator;
                }
                else if (level >= 40 && level <= 75)
                {
                    percentLost = 4.0 * clan_luck_modificator;
                }

                break;
        }

        if (getReputation() < 0)
        {
            percentLost *= Config.RATE_REPUTATION_EXP_LOST;
        }

        if (atwar || isInsideZone(ZONE_SIEGE))
        {
            percentLost /= 4.0;
        }

        // Calculate the Experience loss
        long lostExp = 0;

        if (lvl < Config.MAX_LEVEL)
        {
            lostExp =
                    Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
        }
        else
        {
            lostExp = Math.round(
                    (getStat().getExpForLevel(Config.MAX_LEVEL + 1) - getStat().getExpForLevel(Config.MAX_LEVEL)) *
                            percentLost / 100);
        }

        // No xp loss inside pvp zone unless
        // - it's a siege zone and you're NOT participating
        // - you're killed by a non-pc
        if (isInsideZone(ZONE_PVP))
        {
            // No xp loss for siege participants inside siege zone
            if (isInsideZone(ZONE_SIEGE))
            {
                if (getSiegeState() > 0)
                {
                    lostExp = 0;
                }
            }
            else
            {
                lostExp = 0;
            }
        }

        // Set the new Experience value of the L2PcInstance
        return lostExp;
    }

    public Point3D getEventSavedPosition()
    {
        return _eventSavedPosition;
    }

    public void setTitleColor(String color)
    {
        if (color.length() > 0)
        {
            _titleColor = color;
        }
        if (_titleColor != null && _titleColor.length() > 0)
        {
            getAppearance().setTitleColor(Integer.decode("0x" + _titleColor));
        }
        else
        {
            getAppearance().setTitleColor(Integer.decode("0xFFFF77"));
        }
    }

    public void setTitleColor(int color)
    {
        setTitleColor(Integer.toHexString(color));
    }

    public int getTitleColor()
    {
        if (_titleColor != null && _titleColor.length() > 0)
        {
            return Integer.decode("0x" + _titleColor);
        }
        else
        {
            return Integer.decode("0xFFFF77");
        }
    }

    public boolean isMobSummonRequest()
    {
        return _mobSummonRequest;
    }

    public void setMobSummonRequest(boolean state, L2ItemInstance item)
    {
        _mobSummonRequest = state;
        _mobSummonItem = item;
    }

    public void mobSummonAnswer(int answer)
    {
        if (_mobSummonRequest == false)
        {
            return;
        }
        else
        {
            if (answer == 1)
            {
                confirmUseMobSummonItem(_mobSummonItem);
            }
            setMobSummonRequest(false, null);
        }
    }

    public boolean isMobSummonExchangeRequest()
    {
        return _mobSummonExchangeRequest;
    }

    public void setMobSummonExchangeRequest(boolean state, L2MobSummonInstance mob)
    {
        _mobSummonExchangeRequest = state;
        //_mobSummonExchange = mob;
    }

    public void mobSummonExchangeAnswer(int answer)
    {
        if (_mobSummonExchangeRequest == false)
        {
            return;
        }
        else
        {
			/*if (answer == 1 && getPet() instanceof L2MobSummonInstance
					&& _mobSummonExchange == _mobSummonExchange.getOwner().getPet()
					&& isMobSummonExchangeRequest())
			{
				((L2MobSummonInstance)getSummons().get(0)).exchange(_mobSummonExchange);
			}
			setMobSummonExchangeRequest(false, null);*///TODO
        }
    }

    public void confirmUseMobSummonItem(L2ItemInstance item)
    {
        if (isSitting())
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_MOVE_SITTING));
            return;
        }

        if (inObserverMode() || _event != null)
        {
            return;
        }

        if (isInOlympiadMode())
        {
            sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
            return;
        }

        if (isAllSkillsDisabled() || isCastingNow())
        {
            return;
        }

        if (!getSummons().isEmpty())
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
            return;
        }

        if (isAttackingNow() || isInCombat() || getPvpFlag() != 0)
        {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
            return;
        }

        int mobId = item.getMobId();

        if (mobId == 0)
        {
            L2Object target = getTarget();
            if (target instanceof L2MonsterInstance && !(target instanceof L2ArmyMonsterInstance) &&
                    !(target instanceof L2ChessPieceInstance) && !(target instanceof L2EventGolemInstance) &&
                    !((L2MonsterInstance) target).isRaid() && ((L2MonsterInstance) target).getCollisionHeight() < 49 &&
                    ((L2MonsterInstance) target).getCollisionRadius() < 29)
            {
                L2MonsterInstance mob = (L2MonsterInstance) target;
                stopMove(null, false);

                L2Object oldtarget = getTarget();
                setTarget(this);
                setHeading(Util.calculateHeadingFrom(this, mob));
                setTarget(oldtarget);
                broadcastPacket(new MagicSkillUse(this, 1050, 1, 20000, 0));
                sendPacket(new SetupGauge(0, 20000));
                sendMessage("Preparing the catch item...");

                MobCatchFinalizer mcf = new MobCatchFinalizer(mob, item);
                setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(mcf, 20000));
                forceIsCasting(TimeController.getGameTicks() + 20000 / TimeController.MILLIS_IN_TICK);
            }
            else if (target instanceof L2MonsterInstance && (((L2MonsterInstance) target).getCollisionHeight() >= 50 ||
                    ((L2MonsterInstance) target).getCollisionRadius() >= 30))
            {
                sendMessage("This monster is too big!");
            }
            else
            {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
            }
            return;
        }

        L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(mobId);

        if (npcTemplate == null)
        {
            return;
        }

        stopMove(null, false);

        L2Object oldtarget = getTarget();
        setTarget(this);
        Broadcast.toSelfAndKnownPlayers(this, new MagicSkillUse(this, 2046, 1, 5000, 0));
        setTarget(oldtarget);
        sendPacket(new SetupGauge(0, 5000));
        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUMMON_A_PET));
        setIsCastingNow(true);

        ThreadPoolManager.getInstance().scheduleGeneral(new MobPetSummonFinalizer(npcTemplate, item), 5000);
    }

    class MobPetSummonFinalizer implements Runnable
    {
        private L2ItemInstance _item;
        private L2NpcTemplate _npcTemplate;

        MobPetSummonFinalizer(L2NpcTemplate npcTemplate, L2ItemInstance item)
        {
            _npcTemplate = npcTemplate;
            _item = item;
        }

        @Override
        public void run()
        {
            sendPacket(new MagicSkillLaunched(L2PcInstance.this, 2046, 1));
            setIsCastingNow(false);
            L2MobSummonInstance summon =
                    new L2MobSummonInstance(IdFactory.getInstance().getNextId(), _npcTemplate, L2PcInstance.this,
                            _item);

            summon.setName(_npcTemplate.Name);
            summon.setTitle(getName());
            summon.setExpPenalty(0);
            if (summon.getLevel() > Config.MAX_LEVEL)
            {
                summon.getStat().setExp(Experience.getAbsoluteExp(Config.MAX_LEVEL));
                Log.warning("Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above " +
                        Config.MAX_LEVEL + ". Please rectify.");
            }
            else
            {
                summon.getStat().setExp(Experience.getAbsoluteExp(summon.getLevel()));
            }
            summon.setCurrentHp(summon.getMaxHp());
            summon.setCurrentMp(summon.getMaxMp());
            summon.setHeading(getHeading());
            summon.setRunning();
            addSummon(summon);

            summon.spawnMe(getX() + 50, getY() + 100, getZ());

            if (isPlayingEvent())
            {
                summon.setInsideZone(L2Character.ZONE_PVP, true);
                summon.setInsideZone(L2Character.ZONE_PVP, true);
            }
        }
    }

    class MobCatchFinalizer implements Runnable
    {
        private L2ItemInstance _item;
        private L2MonsterInstance _mob;

        MobCatchFinalizer(L2MonsterInstance mob, L2ItemInstance item)
        {
            _item = item;
            _mob = mob;
        }

        @Override
        public void run()
        {
            sendPacket(new MagicSkillLaunched(L2PcInstance.this, 2046, 1));
            setIsCastingNow(false);

            if (_mob.isDead())
            {
                sendMessage("This monster is already dead!");
            }
            else
            {
                _item.setMobId(_mob.getNpcId());
                _mob.onDecay();
                sendMessage("You have caught " + _mob.getName() + "!!!");
            }
        }
    }

    public EventInstance getEvent()
    {
        return _event;
    }

    public void setEvent(EventInstance event)
    {
        _event = event;
    }

    public boolean isNoExp()
    {
        return _noExp;
    }

    public void setNoExp(boolean noExp)
    {
        _noExp = noExp;
    }

    public boolean isLandRates()
    {
        return _landRates;
    }

    public void setLandRates(boolean landRates)
    {
        _debugger = landRates ? this : null;
        _landRates = landRates;
    }

    public boolean isShowingStabs()
    {
        return _stabs;
    }

    public void setShowStabs(boolean stabs)
    {
        _stabs = stabs;
    }

    private L2Spawn getNpcServitor(int id)
    {
        if (_npcServitors[id] != null)
        {
            return _npcServitors[id];
        }
        L2Spawn spawn = null;
        try
        {
            L2NpcTemplate tmpl;
            switch (id)
            {
                case 0:
                    tmpl = NpcTable.getInstance().getTemplate(40001);
                    break;
                case 1:
                    tmpl = NpcTable.getInstance().getTemplate(40002);
                    break;
                case 2:
                    tmpl = NpcTable.getInstance().getTemplate(40003);
                    break;
                default:
                    tmpl = NpcTable.getInstance().getTemplate(40005);
            }
            spawn = new L2Spawn(tmpl);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        _npcServitors[id] = spawn;
        return _npcServitors[id];
    }

    public void spawnServitors()
    {
        InstanceManager.getInstance().createInstance(getObjectId());
        L2Spawn servitor;
        float angle = Rnd.get(1000);
        int sCount = 4;
        if (Config.isServer(Config.TENKAI_ESTHUS))
        {
            MainTownInfo currentTown = MainTownManager.getInstance().getCurrentMainTown();
            L2TownZone townZone = TownManager.getTown(currentTown.getTownId());
            if (!townZone.isCharacterInZone(this))
            {
                sCount = 2;
            }
        }
        for (int i = 0; i < sCount; i++)
        {
            servitor = getNpcServitor(i);
            if (servitor != null)
            {
                servitor.setInstanceId(getObjectId());
                servitor.setX(Math.round(getX() + (float) Math.cos(angle / 1000 * 2 * Math.PI) * 30));
                servitor.setY(Math.round(getY() + (float) Math.sin(angle / 1000 * 2 * Math.PI) * 30));
                servitor.setZ(getZ() + 75);
                int heading = (int) Math
                        .round(Math.atan2(getY() - servitor.getY(), getX() - servitor.getX()) / Math.PI * 32768);
                if (heading < 0)
                {
                    heading = 65535 + heading;
                }
                servitor.setHeading(heading);

                if (InstanceManager.getInstance().getInstance(getObjectId()) != null)
                {
                    servitor.doSpawn();
                }
            }
            angle += 1000 / sCount;
        }
    }

    private void onEventTeleported()
    {
        if (isPlayingEvent())
        {
            if (_event.isType(EventType.VIP) && _event.getParticipantTeam(getObjectId()) != null &&
                    _event.getParticipantTeam(getObjectId()).getVIP() != null &&
                    _event.getParticipantTeam(getObjectId()).getVIP().getObjectId() == getObjectId())
            {
                _event.setImportant(this, true);
            }
            else
            {
                _event.setImportant(this, false);
            }

            L2NpcBufferInstance.buff(this);
            setCurrentCp(getMaxCp());
            setCurrentHp(getMaxHp());
            setCurrentMp(getMaxMp());

            for (L2SummonInstance summon : _summons)
            {
                L2NpcBufferInstance.buff(summon);
                summon.setCurrentCp(summon.getMaxCp());
                summon.setCurrentHp(summon.getMaxHp());
                summon.setCurrentMp(summon.getMaxMp());

                if (summon instanceof L2MobSummonInstance)
                {
                    summon.unSummon(this);
                }
            }

            setProtection(!_event.isType(EventType.StalkedSalkers));

            broadcastStatusUpdate();
            broadcastUserInfo();

            if (_dwEquipped)
            {
                getInventory().unEquipItemInBodySlot(L2Item.SLOT_LR_HAND);
            }
        }
        else if (_wasInEvent)
        {
            setTeam(0);
            getAppearance().setNameColor(Integer.decode("0xFFFFFF"));
            setTitleColor("");
            setIsEventDisarmed(false);

            int i = 0;
            while (isInsideZone(L2Character.ZONE_PVP) && i < 100)
            {
                setInsideZone(L2Character.ZONE_PVP, false);
                i++;
            }
            eventRestoreBuffs();

            removeSkill(9940);
            stopVisualEffect(VisualEffect.S_AIR_STUN);

            broadcastStatusUpdate();
            broadcastUserInfo();

            _wasInEvent = false;
        }
        setIsCastingNow(false);
    }

    public void returnedFromEvent()
    {
        _wasInEvent = true;
    }

    public void enterEventObserverMode(int x, int y, int z)
    {
        if (getPet() != null)
        {
            getPet().unSummon(this);
        }
        for (L2SummonInstance summon : getSummons())
        {
            summon.unSummon(this);
        }

        if (!getCubics().isEmpty())
        {
            for (L2CubicInstance cubic : getCubics().values())
            {
                cubic.stopAction();
                cubic.cancelDisappear();
            }

            getCubics().clear();
        }

        if (isSitting())
        {
            standUp();
        }

        _lastX = getX();
        _lastY = getY();
        _lastZ = getZ();

        _observerMode = true;
        setTarget(null);
        setIsInvul(true);
        getAppearance().setInvisible();
        teleToLocation(x, y, z, false);
        sendPacket(new ExOlympiadMode(3));

        broadcastUserInfo();
    }

    public void leaveEventObserverMode()
    {
        setTarget(null);
        sendPacket(new ExOlympiadMode(0));
        setInstanceId(0);
        teleToLocation(_lastX, _lastY, _lastZ, true);
        if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invis", getAccessLevel()))
        {
            getAppearance().setVisible();
        }
        if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invul", getAccessLevel()))
        {
            setIsInvul(false);
        }
        if (getAI() != null)
        {
            getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }

        _observerMode = false;
        broadcastUserInfo();
    }

    public boolean hadStoreActivity()
    {
        return _hadStoreActivity;
    }

    public void hasBeenStoreActive()
    {
        if (!_hadStoreActivity)
        {
            _hadStoreActivity = true;
        }
    }

    class StalkerHintsTask implements Runnable
    {
        boolean _stop = false;
        int _level = 0;

        @Override
        public void run()
        {
            if (!_stop && _level < 4 && isPlayingEvent() && getEvent() instanceof StalkedStalkers)
            {
                _level++;
                ((StalkedStalkers) getEvent()).stalkerMessage(L2PcInstance.this, _level);
                int nextHintTimer = 0;
                switch (_level)
                {
                    case 1:
                        nextHintTimer = 15000;
                        break;
                    case 2:
                        nextHintTimer = 30000;
                        break;
                    case 3:
                        nextHintTimer = 45000;
                        break;
                    case 4:
                        nextHintTimer = 60000;
                        break;
                }

                if (nextHintTimer > 0)
                {
                    ThreadPoolManager.getInstance().scheduleGeneral(this, nextHintTimer);
                }
            }
        }

        public void stop()
        {
            _stop = true;
        }
    }

    public void startStalkerHintsTask()
    {
        if (_stalkerHintsTask != null)
        {
            _stalkerHintsTask.stop();
        }

        _stalkerHintsTask = new StalkerHintsTask();
        ThreadPoolManager.getInstance().scheduleGeneral(_stalkerHintsTask, 5000);
    }

    public boolean isChessChallengeRequest()
    {
        return _chessChallengeRequest;
    }

    public void setChessChallengeRequest(boolean state, L2PcInstance challenger)
    {
        _chessChallengeRequest = state;
        _chessChallenger = challenger;
    }

    public void chessChallengeAnswer(int answer)
    {
        if (_chessChallengeRequest == false)
        {
            return;
        }
        else
        {
            if (answer == 1 && isChessChallengeRequest() && !ChessEvent.isState(ChessState.STARTED))
            {
                ChessEvent.startFight(_chessChallenger, this);
            }
            setChessChallengeRequest(false, null);
        }
    }

    public boolean isCastingProtected()
    {
        return isCastingNow() && getLastSkillCast() != null &&
                (getLastSkillCast().getTargetType() == L2SkillTargetType.TARGET_HOLY ||
                        getLastSkillCast().getTargetType() == L2SkillTargetType.TARGET_FLAGPOLE ||
                        getLastSkillCast().getId() == 1050);
    }

    public boolean hasReceivedImage(int id)
    {
        return _receivedImages.contains(id);
    }

    public void setReceivedImage(int id)
    {
        _receivedImages.add(id);
    }

    public void scheduleEffectRecovery(L2Abnormal effect, int seconds, boolean targetWasInOlys)
    {
        //if (Config.isServer(Config.PVP) || Config.isServer(Config.CRAFT))
        //	ThreadPoolManager.getInstance().scheduleGeneral(new EffectRecoveryTask(effect, targetWasInOlys), seconds * 1000L);
    }

    @SuppressWarnings("unused")
    private class EffectRecoveryTask implements Runnable
    {
        private L2Abnormal _effect;
        private int _effectSavedTime;
        boolean _targetWasInOlys;

        public EffectRecoveryTask(L2Abnormal effect, boolean targetWasInOlys)
        {
            _effect = effect;
            _effectSavedTime = TimeController.getGameTicks();
            _targetWasInOlys = targetWasInOlys;
        }

        @Override
        public void run()
        {
            if (!_targetWasInOlys && isInOlympiadMode())
            {
                return;
            }

            L2Object[] targets = new L2Character[]{L2PcInstance.this};
            if (_effect != null)
            {
                int skillId = _effect.getSkill().getId();
                int skillLvl = _effect.getLevel();
                int effectCount = _effect.getCount();
                int effectCurTime = _effect.getTime() -
                        (TimeController.getGameTicks() - _effectSavedTime) / TimeController.TICKS_PER_SECOND;

                if (skillId == -1 || effectCount == -1 || effectCurTime < 30 || effectCurTime >= _effect.getDuration())
                {
                    return;
                }

                L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                ISkillHandler IHand = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
                if (IHand != null)
                {
                    IHand.useSkill(L2PcInstance.this, skill, targets);
                }
                else
                {
                    skill.useSkill(L2PcInstance.this, targets);
                }

                for (L2Abnormal effect : getAllEffects())
                {
                    if (effect != null && effect.getSkill() != null && effect.getSkill().getId() == skillId)
                    {
                        effect.setCount(effectCount);
                        effect.setFirstTime(effectCurTime);
                    }
                }
            }
        }
    }

    public int getStrenghtPoints(boolean randomize)
    {
        final int levelI = 600;
        final int weaponI = 200;
        final int chestI = 50;
        final int miscI = 14;
        final int jewelI = 16;

        int strenghtPoints = getExpertiseIndex() * levelI +
                (getLevel() - EXPERTISE_LEVELS[getExpertiseIndex()]) * levelI /
                        ((getExpertiseIndex() < 7 ? EXPERTISE_LEVELS[getExpertiseIndex() + 1] : 86) -
                                EXPERTISE_LEVELS[getExpertiseIndex()]);

        int[] bestItems = new int[14];
        for (L2ItemInstance item : getInventory().getItems())
        {
            if (item == null)
            {
                continue;
            }
            int influence, index;
            switch (item.getItem().getBodyPart())
            {
                case L2Item.SLOT_L_HAND:
                case L2Item.SLOT_R_HAND:
                case L2Item.SLOT_LR_HAND:
                    influence = weaponI;
                    index = 0;
                    break;
                case L2Item.SLOT_CHEST:
                case L2Item.SLOT_FULL_ARMOR:
                    influence = chestI;
                    index = 1;
                    break;
                case L2Item.SLOT_LEGS:
                    influence = miscI;
                    index = 2;
                    break;
                case L2Item.SLOT_HEAD:
                    influence = miscI;
                    index = 3;
                    break;
                case L2Item.SLOT_GLOVES:
                    influence = miscI;
                    index = 4;
                    break;
                case L2Item.SLOT_FEET:
                    influence = miscI;
                    index = 5;
                    break;
                case L2Item.SLOT_UNDERWEAR:
                    influence = miscI;
                    index = 6;
                    break;
                case L2Item.SLOT_BACK:
                    influence = miscI;
                    index = 7;
                    break;
                case L2Item.SLOT_BELT:
                    influence = miscI;
                    index = 8;
                    break;
                case L2Item.SLOT_NECK:
                    influence = jewelI;
                    index = 9;
                    break;
                case L2Item.SLOT_R_EAR:
                    influence = jewelI;
                    index = 10;
                    break;
                case L2Item.SLOT_L_EAR:
                    influence = jewelI;
                    index = 11;
                    break;
                case L2Item.SLOT_R_FINGER:
                    influence = jewelI;
                    index = 12;
                    break;
                case L2Item.SLOT_L_FINGER:
                    influence = jewelI;
                    index = 13;
                    break;
                default:
                    continue;
            }

            // Check if the item has higher grade than user and it is not equipped. If so, don't count it.
            if (item.getItem().getCrystalType() > getExpertiseIndex() && !item.isEquipped())
            {
                continue;
            }

            int str = Math.min(item.getItem().getCrystalType() + 1, getExpertiseIndex() + 1) * influence +
                    item.getEnchantLevel() * influence / 10;
            if (str > bestItems[index])
            {
                bestItems[index] = str;
            }
        }

        for (int str : bestItems)
        {
            strenghtPoints += str;
        }

        for (L2Skill skill : getAllSkills())
        {
            if (skill.getEnchantRouteId() > 0)
            {
                strenghtPoints += skill.getEnchantLevel() * skill.getEnchantLevel() / 10;
            }
        }

        int rand = randomize ? Rnd.get(3) : 0;

        return strenghtPoints * (10 + rand);
    }

    /**
     * Call this when a summon dies to store its buffs if it had noblesse
     */
    public void storeSummonBuffs(L2Abnormal[] effects)
    {
        // Store buffs
        _summonBuffs = effects;

        if (_pet != null)
        {
            // Only for the same summon buffs shall be restored
            _lastSummonId = _pet.getNpcId();
        }

        return;
    }

    /**
     * Resturns collection of buffs which are possibly stored from last summon
     */
    public L2Abnormal[] restoreSummonBuffs()
    {
        return _summonBuffs;
    }

    /**
     * Returns npc id of last summon
     */
    public int getLastSummonId()
    {
        return _lastSummonId;
    }

    public void setIsEventDisarmed(boolean disarmed)
    {
        _eventDisarmed = disarmed;
    }

    public boolean isEventDisarmed()
    {
        return _eventDisarmed;
    }

    @Override
    public boolean isDisarmed()
    {
        return super.isDisarmed() || _eventDisarmed;
    }

    private int _lastCheckedAwakeningClassId = 0;

    public int getLastCheckedAwakeningClassId()
    {
        return _lastCheckedAwakeningClassId;
    }

    public void setLastCheckedAwakeningClassId(int classId)
    {
        _lastCheckedAwakeningClassId = classId;
    }

    private L2FlyMove _flyMove = null;
    private L2FlyMoveChoose _flyMoveChoose = null;
    private int _flyMoveEndAt = -1;
    private int _flyMoveLast = -1;

    public void startFlyMove()
    {
        if (_flyMove == null)
        {
            return;
        }

        _flyMoveChoose = null;
        _flyMoveEndAt = -1;
        _flyMoveLast = -1;

        disableAllSkills();
        abortAttack();
        abortCast();
        getAI().clientStopAutoAttack();
        getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

        flyMoveStep(0);
    }

    public void flyMoveStep(int stepId)
    {
        if (_flyMove == null)
        {
            return;
        }

        if (stepId == -1)
        {
            setFlyMove(null);
            return;
        }

        L2FlyMoveChoose choose = _flyMove.getChoose(stepId);
        ExFlyMove fm;
        ExFlyMoveBroadcast fmb;
        if (choose != null && choose.getOptions().size() > 0 && choose.getAt() == stepId)
        {
            _flyMoveChoose = choose;
            Map<Integer, Point3D> options = new HashMap<>();
            for (L2FlyMoveOption o : _flyMoveChoose.getOptions())
            {
                options.put(o.getStart(), _flyMove.getStep(o.getStart()));
            }
            fm = new ExFlyMove(this, _flyMove.getId(), options);
            fmb = new ExFlyMoveBroadcast(this, options.keySet().contains(-1));
            _flyMoveEndAt = -1;
            _flyMoveLast = -1;
        }
        else
        {
            if (_flyMoveEndAt == -1)
            {
                if (_flyMoveChoose != null)
                {
                    for (L2FlyMoveOption o : _flyMoveChoose.getOptions())
                    {
                        if (stepId == o.getStart())
                        {
                            _flyMoveEndAt = o.getEnd();
                            _flyMoveLast = o.getLast();
                        }
                    }
                }
                else
                {
                    _flyMoveEndAt = 1000;
                }
            }

            if (!(stepId == _flyMoveEndAt && stepId == _flyMoveLast))
            {
                stepId++;
            }

            if (stepId > _flyMoveEndAt)
            {
                if (_flyMoveLast == -1 || stepId > _flyMoveLast)
                {
                    _flyMove = null;
                    _flyMoveChoose = null;
                    _flyMoveEndAt = -1;
                    enableAllSkills();
                    return;
                }
                stepId = _flyMoveLast;
            }

            Point3D step = _flyMove.getStep(stepId);

            if (step == null)
            {
                return;
            }

            boolean last = stepId == _flyMoveEndAt && _flyMoveLast == -1 || stepId == _flyMoveLast;
            if (last)
            {
                stepId = -1;
            }

            fm = new ExFlyMove(this, _flyMove.getId(), stepId, step.getX(), step.getY(), step.getZ());
            fmb = new ExFlyMoveBroadcast(this, step.getX(), step.getY(), step.getZ());

            if (last)
            {
                _flyMove = null;
                _flyMoveChoose = null;
                enableAllSkills();
            }
        }

        sendPacket(fm);
        fmb.setInvisibleCharacter(getAppearance().getInvisible() ? getObjectId() : 0);
        for (L2PcInstance player : getKnownList().getKnownPlayers().values())
        {
            if (player == null)
            {
                continue;
            }

            player.sendPacket(fmb);
        }
    }

    public void setFlyMove(L2FlyMove move)
    {
        if (move == null)
        {
            enableAllSkills();
            _flyMoveChoose = null;
        }

        _flyMove = move;
    }

    public boolean isPerformingFlyMove()
    {
        return _flyMove != null;
    }

    public boolean isChoosingFlyMove()
    {
        return _flyMoveChoose != null;
    }

    private Map<GlobalQuest, Integer> _mainQuestsState = new HashMap<>();

    private void setGlobalQuestState(GlobalQuest quest, int state)
    {
        _mainQuestsState.put(quest, state);

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement =
                    con.prepareStatement("REPLACE INTO character_quest_global_data (charId,var,value) VALUES (?,?,?)");
            statement.setInt(1, getObjectId());
            statement.setString(2, "GlobalQuest" + quest.ordinal());
            statement.setInt(3, state);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not insert player's global quest variable: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        sendPacket(new QuestList());
    }

    public int getGlobalQuestState(GlobalQuest quest)
    {
        if (_mainQuestsState.containsKey(quest))
        {
            return _mainQuestsState.get(quest);
        }

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement =
                    con.prepareStatement("SELECT value FROM character_quest_global_data WHERE charId = ? AND var = ?");
            statement.setInt(1, getObjectId());
            statement.setString(2, "GlobalQuest" + quest.ordinal());
            ResultSet rs = statement.executeQuery();
            if (rs.first())
            {
                _mainQuestsState.put(quest, rs.getInt(1));
            }
            else
            {
                _mainQuestsState.put(quest, 0);
            }
            rs.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not load player's global quest variable: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        return _mainQuestsState.get(quest);
    }

    public void setGlobalQuestFlag(GlobalQuest quest, int order, boolean value)
    {
        if (order < 1)
        {
            return;
        }

        int state = getGlobalQuestState(quest);
        int flag = 1 << order - 1;

        if (value)
        {
            state |= flag;
        }
        else
        {
            state &= ~flag;
        }

        setGlobalQuestState(quest, state);
    }

    public void setGlobalQuestFlag(GlobalQuest quest, int order)
    {
        setGlobalQuestFlag(quest, order, true);
    }

    public boolean getGlobalQuestFlag(GlobalQuest quest, int order)
    {
        if (order < 1)
        {
            return false;
        }

        int state = getGlobalQuestState(quest);
        int flag = 1 << order - 1;

        return (state & flag) == flag;
    }

    private int _vitalityItemsUsed = -1;

    public int getVitalityItemsUsed()
    {
        if (_vitalityItemsUsed == -1)
        {
            Connection con = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement =
                        con.prepareStatement("SELECT value FROM account_gsdata WHERE account_name=? AND var=?");
                statement.setString(1, getAccountName());
                statement.setString(2, "vit_items_used");
                ResultSet rs = statement.executeQuery();
                if (rs.next())
                {
                    _vitalityItemsUsed = Integer.parseInt(rs.getString("value"));
                }
                else
                {
                    statement.close();
                    statement =
                            con.prepareStatement("INSERT INTO account_gsdata(account_name,var,value) VALUES(?,?,?)");
                    statement.setString(1, getAccountName());
                    statement.setString(2, "vit_items_used");
                    statement.setString(3, String.valueOf(0));
                    statement.execute();
                }
                rs.close();
                statement.close();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING, "Could not load player vitality items used count: " + e.getMessage(), e);
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }
        }
        return _vitalityItemsUsed;
    }

    public void increaseVitalityItemsUsed()
    {
        Connection con = null;
        if (_vitalityItemsUsed > 5)
        {
            _vitalityItemsUsed = 5;
        }
        _vitalityItemsUsed++;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement =
                    con.prepareStatement("UPDATE account_gsdata SET value=? WHERE account_name=? AND var=?");
            statement.setString(1, String.valueOf(_vitalityItemsUsed));
            statement.setString(2, getAccountName());
            statement.setString(3, "vit_items_used");
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.INFO, "Could not store player vitality items used count: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    private int _elementalStance = 0;

    public int getElementalStance()
    {
        return _elementalStance;
    }

    public void setElementalStance(int stanceId)
    {
        _elementalStance = stanceId;
    }

    public int getMaxSummonPoints()
    {
        return (int) getStat().calcStat(Stats.SUMMON_POINTS, 0, null, null);
    }

    public int getSpentSummonPoints()
    {
        int spentSummonPoints = 0;
        for (L2SummonInstance summon : getSummons())
        {
            spentSummonPoints += summon.getSummonPoints();
        }

        return spentSummonPoints;
    }

    private L2ItemInstance _currentUnbindScroll = null;

    public void setCurrentUnbindScroll(L2ItemInstance scroll)
    {
        _currentUnbindScroll = scroll;
    }

    public L2ItemInstance getCurrentUnbindScroll()
    {
        return _currentUnbindScroll;
    }

    private L2ItemInstance _currentBlessingScroll = null;

    public void setCurrentBlessingScroll(L2ItemInstance scroll)
    {
        _currentBlessingScroll = scroll;
    }

    public L2ItemInstance getCurrentBlessingScroll()
    {
        return _currentBlessingScroll;
    }

    private boolean _canEscape = true;

    public void setCanEscape(boolean canEscape)
    {
        _canEscape = canEscape;
    }

    public boolean canEscape()
    {
        if (calcStat(Stats.BLOCK_ESCAPE, 0, this, null) > 0)
        {
            return false;
        }

        return _canEscape;
    }

    private boolean _hasLoaded = false;

    public void hasLoaded()
    {
        _hasLoaded = true;
    }

    public boolean isAlly(L2Character target)
    {
        return target instanceof L2PcInstance &&
                (getParty() != null && ((L2PcInstance) target).getParty() == getParty() ||
                        getClanId() != 0 && ((L2PcInstance) target).getClanId() == getClanId() ||
                        getAllyId() != 0 && ((L2PcInstance) target).getAllyId() == getAllyId()
		/*|| target instanceof L2ApInstance*/);
    }

    public boolean isEnemy(L2Character target)
    {
        return /*(target instanceof L2MonsterInstance && target.isInCombat())
				||*/target instanceof L2PcInstance && target.isAutoAttackable(this) &&
                (getAllyId() == 0 || ((L2PcInstance) target).getAllyId() != getAllyId()) &&
                !target.isInsideZone(L2Character.ZONE_SIEGE) && !((L2PcInstance) target).getAppearance().getInvisible()
		/*&& !(target instanceof L2ApInstance)*/
		/*|| (target instanceof L2ApInstance && !target.isInsidePeaceZone(this))*/;
    }

    private L2ItemInstance _appearanceStone;

    public void setActiveAppearanceStone(L2ItemInstance stone)
    {
        _appearanceStone = stone;
    }

    public L2ItemInstance getActiveAppearanceStone()
    {
        return _appearanceStone;
    }

    boolean isSearchingForParty = false;

    public boolean isSearchingForParty()
    {
        return isSearchingForParty;
    }

    L2PcInstance playerForChange;

    public L2PcInstance getPlayerForChange()
    {
        return playerForChange;
    }

    public void setPlayerForChange(L2PcInstance _playerForChange)
    {
        playerForChange = _playerForChange;
    }

    public void showWaitingSubstitute()
    {
        sendPacket(SystemMessageId.YOU_ARE_REGISTERED_ON_THE_WAITING_LIST);
        PartySearchManager.getInstance().addLookingForParty(this);
        isSearchingForParty = true;
        sendPacket(new ExWaitWaitingSubStituteInfo(true));
    }

    public void closeWaitingSubstitute()
    {
        sendPacket(SystemMessageId.STOPPED_SEARCHING_THE_PARTY);
        PartySearchManager.getInstance().removeLookingForParty(this);
        isSearchingForParty = false;
        sendPacket(new ExWaitWaitingSubStituteInfo(false));
    }

    public ArrayList<Integer> _menteeList = new ArrayList<>();

    public List<Integer> getMenteeList()
    {
        return _menteeList;
    }

    public void restoreMenteeList()
    {
        _menteeList.clear();

        Connection con = null;

        try
        {
            String sqlQuery = "SELECT menteeId FROM character_mentees WHERE charId=?";

            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();

            int menteeId;
            while (rset.next())
            {
                menteeId = rset.getInt("menteeId");
                if (menteeId == getObjectId())
                {
                    continue;
                }
                _menteeList.add(menteeId);
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error found in " + getName() + "'s MenteeList: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public boolean isMentee()
    {
        return getMentorId() != 0;
    }

    public boolean canBeMentor()
    {
        return (getBaseClassLevel() >= 86 || getBaseClassLevel() >= 85 && !isMentee()) && getBaseClass() >= 139;
    }

    public boolean isMentor()
    {
        return !_menteeList.isEmpty();
    }

    int _mentorId;

    public int getMentorId()
    {
        return _mentorId;
    }

    public void setMentorId(int mentorId)
    {
        _mentorId = mentorId;
    }

    public boolean restoreMentorInfo()
    {
        int charId = 0;
        Connection con = null;

        try
        {
            String sqlQuery = "SELECT charId FROM character_mentees WHERE menteeId=?";

            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();

            while (rset.next())
            {
                charId = rset.getInt("charId");
                if (charId == getObjectId())
                {
                    continue;
                }
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error found in " + getName() + "'s MenteeList: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
        if (charId != 0)
        {
            setMentorId(charId);
            return true;
        }
        setMentorId(0);
        return false;
    }

    public void removeMentor()
    {
        SystemMessage sm;
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM character_mentees WHERE (charId=? AND menteeId=?)");
            statement.setInt(1, getMentorId());
            statement.setInt(2, getObjectId());
            statement.execute();
            statement.close();

            // Mentee cancelled mentoring with mentor
            sm = SystemMessage.getSystemMessage(
                    SystemMessageId.YOU_REACHED_LEVEL_86_SO_THE_MENTORING_RELATIONSHIP_WITH_YOUR_MENTOR_S1_CAME_TO_AN_END);
            sm.addString(CharNameTable.getInstance().getNameById(getMentorId()));
            sendPacket(sm);

            if (L2World.getInstance().getPlayer(getMentorId()) != null)
            {
                L2PcInstance player = L2World.getInstance().getPlayer(getMentorId());
                sm = SystemMessage.getSystemMessage(
                        SystemMessageId.THE_MENTEE_S1_REACHED_LEVEL_86_SO_THE_MENTORING_RELATIONSHIP_WAS_ENDED);
                sm.addString(getName());
                player.sendPacket(sm);
                player.sendPacket(new ExMentorList(player));
            }
            sendPacket(new ExMentorList(this));
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "could not delete mentees mentor: ", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void giveMenteeBuffs()
    {
        ArrayList<Integer> alreadyAddedBuffs = new ArrayList<>();
        for (L2Abnormal e : getAllEffects())
        {
            if (e.getSkill().getId() >= 9227 && e.getSkill().getId() <= 9233)
            {
                alreadyAddedBuffs.add(e.getSkill().getId());
            }
        }
        for (int i = 9227; i < 9234; i++)
        {
            if (!alreadyAddedBuffs.contains(i))
            {
                SkillTable.getInstance().getInfo(i, 1).getEffects(this, this);
            }
        }
    }

    public void giveMentorBuff()
    {
        if (!isOnline() || getCurrentHp() < 1)
        {
            return;
        }
        int menteeOnline = 0;
        for (int objectId : getMenteeList())
        {
            if (L2World.getInstance().getPlayer(objectId) != null &&
                    L2World.getInstance().getPlayer(objectId).isOnline())
            {
                L2World.getInstance().getPlayer(objectId).giveMenteeBuffs();
                menteeOnline++;
            }
        }

        for (L2Abnormal e : getAllEffects())
        {
            if (e.getSkill().getId() == 9256)
            {
                if (menteeOnline > 0)
                {
                    return;
                }
                else
                {
                    e.exit();
                }
            }
        }
        if (menteeOnline > 0 && menteeOnline < 4 && getBaseClass() == getClassId())
        {
            SkillTable.getInstance().getInfo(9256, 1).getEffects(this, this);
        }
    }

    public void giveMentorSkills()
    {
        for (int i = 9376; i < 9379; i++)
        {
            L2Skill s = SkillTable.getInstance().getInfo(i, 1);
            addSkill(s, false); //Dont Save Mentor skills to database
        }
    }

    public void giveMenteeSkills()
    {
        L2Skill s = SkillTable.getInstance().getInfo(9379, 1);
        addSkill(s, false); //Dont Save Mentee skill to database
    }

    int _activeForcesCount = 0;

    public int getActiveForcesCount()
    {
        return _activeForcesCount;
    }

    public void setActiveForcesCount(int activeForcesCount)
    {
        _activeForcesCount = activeForcesCount;
    }

    public String getExternalIP()
    {
        if (_publicIP == null)
        {
            if (getClient() != null && getClient().getConnection() != null)
            {
                _publicIP = getClient().getConnection().getInetAddress().getHostAddress();
            }

            else
            {
                return "";
            }
        }
        return _publicIP;
    }

    public String getInternalIP()
    {
        if (_internalIP == null)
        {
            if (getClient() != null && getClient().getTrace() != null)
            {
                _internalIP = getClient().getTrace()[0][0] + "." + getClient().getTrace()[0][1] + "." +
                        getClient().getTrace()[0][2] + "." + getClient().getTrace()[0][3];
            }
            else
            {
                return "";
            }
        }
        return _internalIP;
    }

    public String getHWID()
    {
        if (getClient() == null || getClient().getHWId() == null)
        {
            return "";
        }

        return getClient().getHWId();
    }

    public long getOnlineTime()
    {
        return _onlineTime;
    }

    public void storeCharFriendMemos()
    {
        Connection con = null;
        for (Map.Entry<Integer, String> friendMemo : _friendMemo.entrySet())
        {
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE character_friends SET memo=? WHERE charId=? AND friendId=? AND relation=0");
                statement.setString(1, friendMemo.getValue());
                statement.setInt(2, getObjectId());
                statement.setInt(3, friendMemo.getKey());
                statement.execute();

                statement.close();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING,
                        "Could not update character(" + getObjectId() + ") friend(" + friendMemo.getKey() + ") memo: " +
                                e.getMessage(), e);
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }
        }
        for (Map.Entry<Integer, String> blockMemo : _blockMemo.entrySet())
        {
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE character_friends SET memo=? WHERE charId=? AND friendId=? AND relation=1");
                statement.setString(1, blockMemo.getValue());
                statement.setInt(2, getObjectId());
                statement.setInt(3, blockMemo.getKey());
                statement.execute();

                statement.close();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING,
                        "Could not update character(" + getObjectId() + ") block(" + blockMemo.getKey() + ") memo: " +
                                e.getMessage(), e);
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }
        }
    }

    private ScheduledFuture<?>[] _mezResistTasks = new ScheduledFuture<?>[2];
    private int[] _mezResistLevels = new int[2];

    @Override
    public float getMezMod(final int type)
    {
        if (type == -1)
        {
            return 1.0f;
        }

        float result;
        switch (_mezResistLevels[type])
        {
            case 0:
                result = 1.0f;
                break;
            case 1:
                result = 0.6f;
                break;
            case 2:
                result = 3.0f;
                break;
            default:
                result = 0.0f;
        }

        return result;
    }

    @Override
    public void increaseMezResist(final int type)
    {
        if (type == -1)
        {
            return;
        }

        if (_mezResistLevels[type] < 3)
        {
            if (_mezResistTasks[type] != null)
            {
                _mezResistTasks[type].cancel(false);
            }

            _mezResistTasks[type] = ThreadPoolManager.getInstance().scheduleEffect(() ->
            {
                _mezResistLevels[type] = 0;
                _mezResistTasks[type] = null;
            }, 15000L);
        }

        _mezResistLevels[type]++;
    }

    @Override
    public int getMezType(L2AbnormalType type)
    {
        if (type == null || getClassId() < 139)
        {
            return -1;
        }

        int mezType = -1;
        switch (type)
        {
            case STUN:
            case PARALYZE:
            case KNOCK_BACK:
            case KNOCK_DOWN:
            case HOLD:
            case DISARM:
                mezType = 0;
                break;
            case SLEEP:
            case MUTATE:
            case FEAR:
            case LOVE:
            case AERIAL_YOKE:
            case SILENCE:
                mezType = 1;
                break;
        }

        return mezType;
    }

    public boolean getIsSummonsInDefendingMode()
    {
        return _summonsInDefendingMode;
    }

    public void setIsSummonsInDefendingMode(boolean a)
    {
        _summonsInDefendingMode = a;
    }

    public TradeList getCustomSellList()
    {
        if (_customSellList == null)
        {
            _customSellList = new TradeList(this);
        }

        return _customSellList;
    }

    public void setIsAddSellItem(boolean isSellItem)
    {
        _isAddSellItem = isSellItem;
    }

    public boolean isAddSellItem()
    {
        return _isAddSellItem;
    }

    public void setAddSellPrice(int addSellPrice)
    {
        _addSellPrice = addSellPrice;
    }

    public int getAddSellPrice()
    {
        return _addSellPrice;
    }

    public int getCubicMastery()
    {
        int cubicMastery = getSkillLevelHash(143); //Cubic Mastery

        if (cubicMastery < 0)
        {
            cubicMastery = getSkillLevelHash(10075); //Superior Cubic Mastery

            if (cubicMastery > 0)
            {
                cubicMastery = 2;
            }
        }

        if (cubicMastery < 0)
        {
            cubicMastery = 0;
        }

        return cubicMastery;
    }

    public boolean getIsRefusalKillInfo()
    {
        return getConfigValue("isRefusalKillInfo");
    }

    public void setIsRefusalKillInfo(boolean b)
    {
        setConfigValue("isRefusalKillInfo", b);
    }

    public boolean getIsInsideGMEvent()
    {
        return getConfigValue("isInsideGMEvent");
    }

    public void setIsInsideGMEvent(boolean b)
    {
        setConfigValue("isInsideGMEvent", b);
    }

    public void setIsOfflineBuffer(boolean b)
    {
        setConfigValue("isOfflineBuffer", b);
    }

    public boolean getIsOfflineBuffer()
    {
        return getConfigValue("isOfflineBuffer");
    }

    public boolean getIsWeaponGlowDisabled()
    {
        return getConfigValue("isWeaponGlowDisabled");
    }

    public void setIsWeaponGlowDisabled(boolean b)
    {
        setConfigValue("isWeaponGlowDisabled", b);
        broadcastUserInfo();
    }

    public boolean getIsArmorGlowDisabled()
    {
        return getConfigValue("isArmorGlowDisabled");
    }

    public void setIsArmorGlowDisabled(boolean b)
    {
        setConfigValue("isArmorGlowDisabled", b);
        broadcastUserInfo();
    }

    public boolean getIsRefusingRequests()
    {
        return getConfigValue("isNoRequests");
    }

    public void setIsRefusingRequests(boolean b)
    {
        setConfigValue("isNoRequests", b);
    }

    public boolean isNickNameWingsDisabled()
    {
        return getConfigValue("isNickNameWingsDisabled");
    }

    public void setNickNameWingsDisabled(boolean b)
    {
        setConfigValue("isNickNameWingsDisabled", b);
        broadcastUserInfo();
    }

    public boolean isCloakHidden()
    {
        return getConfigValue("isCloakHidden");
    }

    public void setCloakHidden(boolean b)
    {
        setConfigValue("isCloakHidden", b);
        broadcastUserInfo();
    }

    private void restoreConfigs()
    {
        _playerConfigs.clear();
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement(
                    "SELECT var, value FROM character_quest_global_data WHERE charId = ? AND var LIKE 'Config_%'");
            statement.setInt(1, getObjectId());
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                String var = rs.getString("var").substring(7);
                boolean val = rs.getBoolean("value");
                _playerConfigs.put(var, val);
            }

            rs.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not load player's global quest variable: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void setConfigValue(String config, boolean b)
    {
        _playerConfigs.put(config, b);

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement =
                    con.prepareStatement("REPLACE INTO character_quest_global_data (charId,var,value) VALUES (?,?,?)");
            statement.setInt(1, getObjectId());
            statement.setString(2, "Config_" + config);
            statement.setBoolean(3, b);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Could not insert player's global quest variable: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public boolean getConfigValue(String config)
    {
        if (_playerConfigs.containsKey(config))
        {
            return _playerConfigs.get(config);
        }

        return false;
    }

    public String getShopMessage()
    {
        if (!isInStoreMode())
        {
            return "No Message";
        }

        switch (getPrivateStoreType())
        {
            case 1:
                return getSellList().getTitle();
            case 5:
                return getCreateList().getStoreName();
            case 10:
                return getCustomSellList().getTitle();
            case 3:
                return getBuyList().getTitle();
        }

        return "";
    }

    public String getShopNameType()
    {
        switch (getPrivateStoreType())
        {
            case 1:
                return "<font color=E4A1EE>Sell</font>";
            case 3:
                return "<font color=FDFEA5>Buy</font>";
            case 5:
                return "<font color=FCB932>Manufacture</font>";
            case 10:
                return "<font color=E4A1EE>Custom Sell</font>";
        }

        return "Unknown";
    }

    public void increaseBotLevel()
    {
        _botLevel++;
    }

    public int getBotLevel()
    {
        return _botLevel;
    }

    public void setBotLevel(int level)
    {
        _botLevel = level;
    }

    public void setCaptcha(String captcha)
    {
        _captcha = captcha;
    }

    public String getCaptcha()
    {
        return _captcha;
    }

    public void captcha(String message)
    {
        if (isGM())
        {
            return;
        }

        NpcHtmlMessage captchaMsg = null;
        int imgId = Rnd.get(1000000) + 1000000;
        String html =
                "<html><body><center>" + message + "<img src=\"Crest.pledge_crest_" + Config.SERVER_ID + "_" + imgId +
                        "\" width=256 height=64><br>" + "Enter the above characters:" +
                        "<edit var=text width=130 height=11 length=16><br>" +
                        "<button value=\"Done\" action=\"bypass Captcha $text\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\">" +
                        "</center></body></html>";
        setCaptcha(generateRandomString(4));
        try
        {
            File captcha = new File(Config.DATA_FOLDER + "captcha/" + Rnd.get(100) + ".png");
            captcha.mkdirs();
            ImageIO.write(generateCaptcha(getCaptcha()), "png", captcha);
            PledgeCrest packet = new PledgeCrest(imgId, DDSConverter.convertToDDS(captcha).array());
            sendPacket(packet);
            captchaMsg = new NpcHtmlMessage(0, html);
            sendPacket(captchaMsg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String generateRandomString(int length)
    {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";
        String rndString = "";
        for (int i = 0; i < length; i++)
        {
            rndString += chars.charAt(Rnd.get(chars.length()));
        }
        return rndString;
    }

    private static BufferedImage generateCaptcha(String randomString)
    {
        char[] charString = randomString.toCharArray();
        final int width = 256;
        final int height = 64;
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = bufferedImage.createGraphics();
        final Font font = new Font("verdana", Font.BOLD, 36);
        RenderingHints renderingHints =
                new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(renderingHints);
        g2d.setFont(font);
        g2d.setColor(new Color(255, 255, 0));
        final GradientPaint gradientPaint = new GradientPaint(0, 0, Color.black, 0, height / 2, Color.black, true);
        g2d.setPaint(gradientPaint);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(new Color(255, 153, 0));
        int xCordinate = 0;
        int yCordinate = 0;
        for (int i = 0; i < charString.length; i++)
        {
            xCordinate = 55 * i + Rnd.get(25) + 10;
            if (xCordinate >= width - 5)
            {
                xCordinate = 0;
            }
            yCordinate = 30 + Rnd.get(34);
            g2d.drawChars(charString, i, 1, xCordinate, yCordinate);
        }
        g2d.dispose();
        return bufferedImage;
    }

    private long _lastActionCaptcha = 0L;

    public boolean onActionCaptcha(boolean increase)
    {
        if (_botLevel < 10)
        {
            long curTime = System.currentTimeMillis();
            if (curTime < _lastActionCaptcha + 10000L)
            {
                return true;
            }

            _lastActionCaptcha = curTime;
        }

        if (_botLevel < 100)
        {
            if (increase || _botLevel >= 10)
            {
                _botLevel++;
            }

            if (_botLevel < 4)
            {
                return true;
            }
            else if (_botLevel < 10)
            {
                captcha("Reply at the captcha as soon as possible, before your ability to target characters is disabled!<br>");
                return true;
            }
            else
            {
                captcha("Now you cannot target other characters without replying at the captcha.<br> And don't be persistent, you will be considered as a bot and banned if you don't reply!<br>");
            }
        }
        else
        {
            sendPacket(new CreatureSay(0, Say2.TELL, "AntiBots",
                    "Next time try to play without a bot or looking at what is happening on your screen."));
            Util.handleIllegalPlayerAction(this, "Player " + getName() +
                            " didn't reply to the Captcha but tried to target a lot of times! Character being kicked and account locked.",
                    IllegalPlayerAction.PUNISH_KICK);

            Connection con = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();

                PreparedStatement statement = con.prepareStatement(
                        "REPLACE INTO ban_timers (identity, timer, author, reason) VALUES (?, ?, ?, ?);");

                statement.setString(1, getAccountName());
                statement.setLong(2, System.currentTimeMillis() / 1000 + 200 * 3600);
                statement.setString(3, "AntiBots");
                statement.setString(4, "using bot");
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }
        }
        return false;
    }

    public class CaptchaTask implements Runnable
    {
        //int _times = 0;
        @Override
        public void run()
        {
            if (/*_times == 0 ||*/getTarget() instanceof L2Attackable || isFishing())
            {
                //_times++;
                setBotLevel(0);
                captcha("Please, understand that this is to avoid cheaters on this server.<br>" +
                        "Don't worry if you late to reply, your character will not be kicked ;)");
                int time;

				/*if (_times == 1)
				 	time = (Rnd.get(3 * 3600) + 1800) * 1000;
				 else*/
                time = (Rnd.get(3 * 3600) + 7200) * 1000;

                ThreadPoolManager.getInstance().scheduleGeneral(this, time);
            }
            else
            {
                ThreadPoolManager.getInstance().scheduleGeneral(this, 300 * 1000);
            }
        }
    }

    public void startCaptchaTask()
    {
        if (!isGM() && Config.isServer(Config.TENKAI))
        {
            //ThreadPoolManager.getInstance().scheduleGeneral(new CaptchaTask(), 30 * 1000);
            //ThreadPoolManager.getInstance().scheduleGeneral(new CaptchaTask(), (Rnd.get(3 * 3600) + 7200) * 1000);
        }
    }

    private boolean _showHat = true;

    public boolean isShowingHat()
    {
        return _showHat;
    }

    public void setShowHat(boolean showHat)
    {
        _showHat = showHat;
    }

    public int getArmorEnchant()
    {
        if (getInventory() == null || getIsArmorGlowDisabled())
        {
            return 0;
        }

        L2ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
        if (chest == null || chest.getArmorItem() == null || chest.getArmorItem().getArmorSet() == null)
        {
            return 0;
        }

        int enchant = 0;
        for (int itemId : chest.getArmorItem().getArmorSet())
        {
            L2ArmorSet set = ArmorSetsTable.getInstance().getSet(itemId);
            if (set == null)
            {
                continue;
            }

            int setEnchant = set.getEnchantLevel(this);
            if (setEnchant > enchant)
            {
                enchant = setEnchant;
            }
        }

        return enchant;
    }

    public int _abilityPoints = 0;
    public int _spentAbilityPoints = 0;
    public TIntIntHashMap _abilities = new TIntIntHashMap();

    public int getAbilityPoints()
    {
        return _abilityPoints;
    }

    public int getSpentAbilityPoints()
    {
        return _spentAbilityPoints;
    }

    public TIntIntHashMap getAbilities()
    {
        return _abilities;
    }

    public void restoreAbilities()
    {
        _abilityPoints = 0;
        _spentAbilityPoints = 0;
        for (int skillId : _abilities.keys())
        {
            removeSkill(skillId);
        }

        _abilities.clear();

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "SELECT points FROM character_ability_points WHERE charId = ? AND classIndex = ?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            ResultSet rset = statement.executeQuery();

            if (rset.next())
            {
                _abilityPoints = rset.getInt("points");
            }

            rset.close();
            statement.close();

            statement = con.prepareStatement(
                    "SELECT skillId, level FROM character_abilities WHERE charId = ? AND classIndex = ?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            rset = statement.executeQuery();

            while (rset.next())
            {
                int skillId = rset.getInt("skillId");
                int level = rset.getInt("level");

                _spentAbilityPoints += level;
                _abilities.put(skillId, level);
                addSkill(SkillTable.getInstance().getInfo(skillId, level));
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error found in " + getName() + "'s abilities: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        if (getLevel() < 99 && _spentAbilityPoints > 0)
        {
            setAbilities(new TIntIntHashMap());
        }
    }

    public void setAbilityPoints(int points)
    {
        _abilityPoints = points;
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "REPLACE INTO character_ability_points (charId, classIndex, points) VALUES (?, ?, ?)");
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            statement.setInt(3, _abilityPoints);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error found while storing " + getName() + "'s ability points: " + e.getMessage(),
                    e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void setAbilities(TIntIntHashMap abilities)
    {
        _spentAbilityPoints = 0;
        for (int skillId : _abilities.keys())
        {
            AbilityTable.getInstance().manageHiddenSkill(this, skillId, getSkillLevelHash(skillId), false);
            removeSkill(skillId);
        }
        _abilities = abilities;

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement =
                    con.prepareStatement("DELETE FROM character_abilities WHERE charId = ? AND classIndex = ?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            statement.execute();
            statement.close();

            statement = con.prepareStatement(
                    "INSERT INTO character_abilities (charId, classIndex, skillId, level) VALUES (?, ?, ?, ?)");
            for (int skillId : _abilities.keys())
            {
                int level = _abilities.get(skillId);
                statement.setInt(1, getObjectId());
                statement.setInt(2, getClassIndex());
                statement.setInt(3, skillId);
                statement.setInt(4, level);
                statement.execute();

                _spentAbilityPoints += level;
                addSkill(SkillTable.getInstance().getInfo(skillId, level));
                AbilityTable.getInstance().manageHiddenSkill(this, skillId, level, true);
            }
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "Error found while storing " + getName() + "'s abilities: " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    /**
     * @param itemId
     */
    public void deleteAllItemsById(int itemId)
    {
        long playerItemCount = getInventory().getInventoryItemCount(itemId, 0);

        if (playerItemCount > 0)
        {
            destroyItemByItemId("", itemId, playerItemCount, this, true);
        }

        if (getPet() != null)
        {
            long petItemCount = getPet().getInventory().getInventoryItemCount(itemId, 0);

            if (petItemCount > 0)
            {
                getPet().destroyItemByItemId("", itemId, petItemCount, this, true);
            }
        }
    }

    private int _premiumItemId;

    public final void setPremiumItemId(int premiumItemId)
    {
        _premiumItemId = premiumItemId;
    }

    public final int getPremiumItemId()
    {
        return _premiumItemId;
    }

    private boolean _rememberSkills;

    public final void setRememberSkills(boolean remember)
    {
        _rememberSkills = remember;
    }

    public final boolean isRememberSkills()
    {
        return _rememberSkills && isInsideZone(L2Character.ZONE_TOWN);
    }

    public boolean isMage()
    {
        int playerClasse = getClassId();
        return playerClasse == 143 || playerClasse >= 166 && playerClasse <= 170 || playerClasse == 146 ||
                playerClasse >= 179 && playerClasse <= 181 || playerClasse >= 94 && playerClasse <= 98 ||
                playerClasse >= 103 && playerClasse <= 105 || playerClasse >= 110 && playerClasse <= 112 ||
                playerClasse >= 115 && playerClasse <= 116 || playerClasse >= 10 && playerClasse <= 17 ||
                playerClasse >= 25 && playerClasse <= 30 || playerClasse >= 38 && playerClasse <= 43 ||
                playerClasse >= 49 && playerClasse <= 52;
    }

    public boolean isFighter()
    {
        int playerClasse = getClassId();
        return playerClasse >= 88 && playerClasse <= 93 || playerClasse >= 99 && playerClasse <= 102 ||
                playerClasse >= 106 && playerClasse <= 109 || playerClasse >= 113 && playerClasse <= 114 ||
                playerClasse >= 117 && playerClasse <= 136 || playerClasse >= 0 && playerClasse <= 9 ||
                playerClasse >= 18 && playerClasse <= 24 || playerClasse >= 31 && playerClasse <= 37 ||
                playerClasse >= 44 && playerClasse <= 48 || playerClasse >= 53 && playerClasse <= 57 ||
                playerClasse == 140 || playerClasse >= 152 && playerClasse <= 157 || playerClasse == 141 ||
                playerClasse >= 158 && playerClasse <= 161 || playerClasse == 142 ||
                playerClasse >= 162 && playerClasse <= 165 || playerClasse == 139 ||
                playerClasse >= 148 && playerClasse <= 151;
    }

    public boolean isHybrid()
    {
        int playerClass = getClassId();
        return playerClass == 132 || playerClass == 133 || playerClass == 144 ||
                playerClass >= 171 && playerClass <= 175;
    }

    private int[] _movementTrace = new int[3];
    private int[] _previousMovementTrace = new int[3];

    public final void setMovementTrace(final int[] movementTrace)
    {
        if (_movementTrace != null)
        {
            _previousMovementTrace = _movementTrace;
        }

        _movementTrace = movementTrace;
    }

    public final int[] getMovementTrace()
    {
        return _movementTrace;
    }

    public final int[] getPreviousMovementTrace()
    {
        return _previousMovementTrace;
    }

    public final boolean isPlayingEvent()
    {
        return _event != null && _event.isState(EventState.STARTED);
    }

    public final int getTeamId()
    {
        if (getEvent() == null)
        {
            return -1;
        }

        return getEvent().getParticipantTeamId(getObjectId());
    }

    public void removeSkillReuse(boolean update)
    {
        getReuseTimeStamp().clear();
        if (getDisabledSkills() != null)
        {
            getDisabledSkills().clear();
        }

        if (update)
        {
            sendPacket(new SkillCoolTime(this));
        }
    }

    public void heal()
    {
        setCurrentHp(getMaxHp());
        setCurrentMp(getMaxMp());
        setCurrentCp(getMaxCp());

        for (L2SummonInstance summon : getSummons())
        {
            if (summon == null)
            {
                continue;
            }

            summon.setCurrentHp(summon.getMaxHp());
            summon.setCurrentMp(summon.getMaxMp());
            summon.setCurrentCp(summon.getMaxCp());
        }

        if (getPet() != null)
        {
            getPet().setCurrentHpMp(getPet().getMaxHp(), getPet().getMaxMp());
        }
    }

    boolean _inWatcherMode = false;

    public void startWatcherMode()
    {
        _inWatcherMode = true;
    }

    public void stopWatcherMode()
    {
        _inWatcherMode = false;
    }

    public boolean isInWatcherMode()
    {
        return _inWatcherMode;
    }

    public ScheduledFuture<?> _respawnTask;

    public final ScheduledFuture<?> getRespawnTask()
    {
        return _respawnTask;
    }

    public ScheduledFuture<?> _comboTask;

    public final ScheduledFuture<?> getComboTask()
    {
        return _comboTask;
    }

    public final void setComboTask(ScheduledFuture<?> _task)
    {
        if (_task == null && _comboTask != null)
        {
            _comboTask.cancel(false);
        }

        _comboTask = _task;
    }

    private L2ItemInstance _compoundItem1 = null;
    private L2ItemInstance _compoundItem2 = null;

    public L2ItemInstance getCompoundItem1()
    {
        return _compoundItem1;
    }

    public void setCompoundItem1(L2ItemInstance compoundItem)
    {
        _compoundItem1 = compoundItem;
    }

    public L2ItemInstance getCompoundItem2()
    {
        return _compoundItem2;
    }

    public void setCompoundItem2(L2ItemInstance compoundItem)
    {
        _compoundItem2 = compoundItem;
    }

    protected final Map<Integer, L2Skill> _temporarySkills;

    private byte _temporaryLevel;
    private byte _temporaryLevelToApply;

    public byte getTemporaryLevelToApply()
    {
        return _temporaryLevelToApply;
    }

    public void setTemporaryLevelToApply(final byte level)
    {
        _temporaryLevelToApply = level;
    }

    @SuppressWarnings("unused")
    private int _classIdBeforeDelevel;

    private int _temporaryClassId;
    private PlayerClass _temporaryPlayerClass;
    private int _temporaryTemplateId;
    private L2PcTemplate _temporaryTemplate;

    public synchronized final void setTemporaryLevel(final byte level)
    {
        _isUpdateLocked = true;

        int oldLevel = getOriginalLevel();

        // We remove the active skill effects whenever switching levels...
        for (L2Skill skill : getAllSkills())
        {
            final L2Abnormal activeEffect = getFirstEffect(skill.getId());

            boolean removeActiveEffect = activeEffect == null || !(activeEffect.getEffector() instanceof L2NpcInstance);

            removeSkillEffect(skill.getId(), removeActiveEffect);
        }

        final L2Abnormal elixir = getFirstEffect(9982); // Divine Protection Elixir
        if (level < 85 && elixir != null)
        {
            elixir.exit();
        }

        // When getting on a temporary level,
        // We remove all equipped items skills.
        if (level != 0)
        {
            for (int i = Inventory.PAPERDOLL_TOTALSLOTS; i-- > Inventory.PAPERDOLL_UNDER; )
            {
                L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);

                if (equippedItem == null)
                {
                    continue;
                }

                L2Armor armor = equippedItem.getArmorItem();

                if (armor == null)
                {
                    continue;
                }

                final SkillHolder[] itemSkills = armor.getSkills();

                if (itemSkills == null)
                {
                    continue;
                }

                for (SkillHolder skillInfo : itemSkills)
                {
                    if (skillInfo == null)
                    {
                        continue;
                    }

                    L2Skill skill = skillInfo.getSkill();

                    if (skill == null)
                    {
                        continue;
                    }

                    removeSkill(skill, false, skill.isPassive());
                }
            }
        }

        _temporaryLevel = level;

        if (_temporarySkills.size() != 0)
        {
            _temporarySkills.clear();

            // When switching back to the original level, we re-add the skills effects.
            for (L2Skill skill : getAllSkills())
            {
                addSkillEffect(skill);
            }
        }

        if (_temporaryLevel == 0)
        {
            _temporaryClassId = 0;
            _temporaryPlayerClass = null;
            _temporaryTemplateId = 0;
            _temporaryTemplate = null;

            giveSkills(false);

            // When getting back to the original level,
            // We re-add item skills.
            for (int i = Inventory.PAPERDOLL_TOTALSLOTS; i-- > Inventory.PAPERDOLL_UNDER; )
            {
                L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);

                if (equippedItem == null)
                {
                    continue;
                }

                L2Armor armor = equippedItem.getArmorItem();

                if (armor == null)
                {
                    continue;
                }

                final SkillHolder[] itemSkills = armor.getSkills();

                if (itemSkills == null)
                {
                    continue;
                }

                for (SkillHolder skillInfo : itemSkills)
                {
                    if (skillInfo == null)
                    {
                        continue;
                    }

                    L2Skill skill = skillInfo.getSkill();

                    if (skill == null)
                    {
                        continue;
                    }

                    addSkill(skill, false);

                    if (skill.isActive())
                    {
                        if (getReuseTimeStamp().isEmpty() || !getReuseTimeStamp().containsKey(skill.getReuseHashCode()))
                        {
                            int equipDelay = skill.getEquipDelay();

                            if (equipDelay > 0)
                            {
                                addTimeStamp(skill, equipDelay);
                                disableSkill(skill, equipDelay);
                            }
                        }
                        //updateTimeStamp = true;
                    }
                    //update = true;
                }
            }
        }
        else
        {
            // When deleveling, we unsummon the active summon if any...
            if (getPet() != null)
            {
                getPet().unSummon(this);
            }

            if (getSummons().size() != 0)
            {
                for (L2SummonInstance summon : getSummons())
                {
                    summon.unSummon(this);
                }
            }

            if (getCubics().size() != 0)
            {
                for (L2CubicInstance cubic : getCubics().values())
                {
                    delCubic(cubic.getId());
                }
            }

            int newLevel = _temporaryLevel;

            int[] levels = null;

            if (getRace() != Race.Ertheia)
            {
                levels = new int[]{20, 40, 76, 86};
            }
            else
            {
                levels = new int[]{40, 76, 86};
            }

            int classesChanges = 0;

            for (int level2 : levels)
            {
                if (level2 > oldLevel) // + 1 for ertheia
                {
                    break;
                }
                else if (level2 < newLevel + 1)
                {
                    continue;
                }

                classesChanges++;
            }

            if (classesChanges != 0)
            {
                PlayerClass newClass = null;

                for (int i = 0; i < classesChanges; i++)
                {
                    if (newClass == null)
                    {
                        newClass = getCurrentClass().getParent();
                    }
                    else
                    {
                        newClass = newClass.getParent();
                    }

                    if (newClass != null)
                    {
                        // When a Female Soulhound becomes a Feoh Soulhound...
                        // Parent Class becomes Male Soulhound...
                        switch (newClass.getId())
                        {
                            case 132: // Male Soulhound
                            {
                                if (getAppearance().getSex())
                                {
                                    newClass = PlayerClassTable.getInstance().getClassById(133);
                                }

                                break;
                            }
                            default:
                                break;
                        }
                    }
                }

                if (newClass == null)
                {
                    setTemporaryLevel((byte) 0);
                    return;
                }

                sendMessage("Your class is now " + newClass.getName() + ".");

                _temporaryClassId = newClass.getId();
                _temporaryPlayerClass = newClass;

                _temporaryTemplateId =
                        _temporaryPlayerClass.getRace().ordinal() * 2 + (_temporaryPlayerClass.isMage() ? 1 : 0);

                _temporaryTemplate = CharTemplateTable.getInstance().getTemplate(_temporaryTemplateId);
            }

            // No Brooches for non awakened characters...
            if (getLevel() <= 85)
            {
                ArrayList<L2ItemInstance> modifiedItems = new ArrayList<>();

                for (int i = Inventory.PAPERDOLL_TOTALSLOTS; i-- > Inventory.PAPERDOLL_BROOCH; )
                {
                    L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);

                    if (equippedItem == null)
                    {
                        continue;
                    }

                    getInventory().unEquipItemInSlotAndRecord(i);

                    SystemMessage sm = null;

                    if (equippedItem.getEnchantLevel() > 0)
                    {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                        sm.addNumber(equippedItem.getEnchantLevel());
                        sm.addItemName(equippedItem);
                    }
                    else
                    {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                        sm.addItemName(equippedItem);
                    }

                    sendPacket(sm);

                    modifiedItems.add(equippedItem);
                }

                if (modifiedItems.size() != 0)
                {
                    InventoryUpdate iu = new InventoryUpdate();
                    iu.addItems(modifiedItems);

                    sendPacket(iu);
                }
            }

            giveSkills(true);
        }

        regiveTemporarySkills();
        rewardSkills();

        //initCharStat();

        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.LEVEL, getLevel());
        su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
        su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
        su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());

        sendPacket(su);

        // Update the overloaded status of the L2PcInstance
        refreshOverloaded();
        // Update the expertise status of the L2PcInstance
        refreshExpertisePenalty();
        // Send a Server->Client packet UserInfo to the L2PcInstance
        sendPacket(new UserInfo(this));

        broadcastStatusUpdate();

        if (getClan() != null)
        {
            getClan().broadcastClanStatus();
        }

        int playerLevel = getLevel();
        int maxAllowedGrade = 0;

        if (playerLevel >= 99)
        {
            maxAllowedGrade = L2Item.CRYSTAL_R99;
        }
        else if (playerLevel >= 95)
        {
            maxAllowedGrade = L2Item.CRYSTAL_R95;
        }
        else if (playerLevel >= 85)
        {
            maxAllowedGrade = L2Item.CRYSTAL_R;
        }
        else if (playerLevel >= 80)
        {
            maxAllowedGrade = L2Item.CRYSTAL_S80;
        }
        else if (playerLevel >= 76)
        {
            maxAllowedGrade = L2Item.CRYSTAL_S;
        }
        else if (playerLevel >= 61)
        {
            maxAllowedGrade = L2Item.CRYSTAL_A;
        }
        else if (playerLevel >= 52)
        {
            maxAllowedGrade = L2Item.CRYSTAL_B;
        }
        else if (playerLevel >= 40)
        {
            maxAllowedGrade = L2Item.CRYSTAL_C;
        }
        else if (playerLevel >= 20)
        {
            maxAllowedGrade = L2Item.CRYSTAL_D;
        }
        else
        {
            maxAllowedGrade = L2Item.CRYSTAL_NONE;
        }

        ArrayList<L2ItemInstance> modifiedItems = new ArrayList<>();

        // Remove over enchanted items and hero weapons
        for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
        {
            L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);

            if (equippedItem == null)
            {
                continue;
            }

            if (equippedItem.getItem().getCrystalType() > maxAllowedGrade)
            {
                getInventory().unEquipItemInSlotAndRecord(i);

                SystemMessage sm = null;

                if (equippedItem.getEnchantLevel() > 0)
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                    sm.addNumber(equippedItem.getEnchantLevel());
                    sm.addItemName(equippedItem);
                }
                else
                {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                    sm.addItemName(equippedItem);
                }

                sendPacket(sm);

                modifiedItems.add(equippedItem);
            }
        }

        if (modifiedItems.size() != 0)
        {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItems(modifiedItems);

            sendPacket(iu);
        }

        int gearGrade = getGearGradeForCurrentLevel();

        int[][] gearPreset = getGearPreset(getClassId(), gearGrade);

        if (gearPreset != null)
        {
            equipGearPreset(gearPreset);
        }

        _isUpdateLocked = false;

        sendPacket(new ItemList(this, false));

        refreshExpertisePenalty();

        broadcastUserInfo();

        sendSkillList();

        _shortCuts.restore();
        sendPacket(new ShortCutInit(this));

        sendPacket(new ItemList(this, false));

        sendMessage("Your level has been adjusted.");
        sendMessage("You are now a " + getCurrentClass().getName() + ".");

        setCurrentHp(getMaxHp());
        setCurrentMp(getMaxMp());
        setCurrentCp(getMaxCp());
    }

    private boolean _isUpdateLocked;

    public final boolean isUpdateLocked()
    {
        return _isUpdateLocked;
    }

	/*
	private PlayerClass getDelevelClass(final int oldLevel, final int newLevel, final PlayerClass oldClass)
	{
		int[] levels = new int[]{ 20, 40, 76, 85 };

		int classesChanges = 0;
		for (int i = 0; i < levels.length; i++)
		{
			if (levels[i] > oldLevel)
				break;

			classesChanges++;
		}

		PlayerClass newClass = null;

		for (int i = 0; i < classesChanges; i++)
		{
			newClass = newClass == null ? oldClass.getParent() : newClass.getParent();
		}

		return newClass;
	}*/

    public final byte getTemporaryLevel()
    {
        return _temporaryLevel;
    }

    public final byte getOriginalLevel()
    {
        return getStat().getLevel();
    }

    @Override
    public L2Skill[] getAllSkills()
    {
        if (_temporaryLevel != 0)
        {
            if (_temporarySkills == null)
            {
                return new L2Skill[0];
            }

            try
            {
                synchronized (_temporarySkills)
                {
                    return _temporarySkills.values().toArray(new L2Skill[_temporarySkills.size()]);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return new L2Skill[0];
            }
        }

        return super.getAllSkills();
    }

    @Override
    public final L2Skill getKnownSkill(int skillId)
    {
        if (_temporaryLevel != 0)
        {
            if (_temporarySkills == null)
            {
                return null;
            }

            return _temporarySkills.get(skillId);
        }

        return super.getKnownSkill(skillId);
    }

    @Override
    public int getSkillLevelHash(int skillId)
    {
        if (_temporaryLevel != 0)
        {
            if (_temporarySkills == null)
            {
                return -1;
            }

            L2Skill skill = _temporarySkills.get(skillId);

            if (skill == null)
            {
                return -1;
            }

            return skill.getLevelHash();
        }

        return super.getSkillLevelHash(skillId);
    }

    @Override
    public Map<Integer, L2Skill> getSkills()
    {
        if (_temporaryLevel != 0)
        {
            return _temporarySkills;
        }

        return super.getSkills();
    }

    public final boolean hasAwakaned()
    {
        if (getCurrentClass() == null)
        {
            return false;
        }

        if (getRace() == Race.Ertheia)
        {
            switch (getCurrentClass().getId())
            {
                case 182: // Ertheia Fighter
                case 183: // Ertheia Wizard
                case 184: // Marauder
                case 185: // Cloud Breaker
                case 186: // Ripper
                case 187: // Stratomancer
                    return false;
                default:
                    break;
            }
        }

        return getCurrentClass().getId() >= 139;
    }

    public int _windowId = 0;

    public int getWindowId()
    {
        return _windowId;
    }

    public void setWindowId(int id)
    {
        _windowId = id;
    }

    public PlayerClass getOriginalClass()
    {
        return _currentClass;
    }

    protected final HashMap<Integer, L2Skill> _certificationSkills = new HashMap<>();

    public final void addCertificationSkill(final L2Skill skill)
    {
        _certificationSkills.put(skill.getId(), skill);
    }

    public final void resetCertificationSkills()
    {
        _certificationSkills.clear();
    }

    /**
     * Combat Parameters & Checks...
     */
    public final boolean isAvailableForCombat()
    {
        return getPvpFlag() > 0 || getReputation() < 0;
    }

    /**
     * @param target
     * @return
     */
    public final boolean isInSameChannel(final L2PcInstance target)
    {
        final L2Party activeCharP = getParty();
        final L2Party targetP = target.getParty();
        if (activeCharP != null && targetP != null)
        {
            final L2CommandChannel chan = activeCharP.getCommandChannel();
            if (chan != null && chan.isInChannel(targetP))
            {
                return true;
            }
        }

        return false;
    }

    public final boolean isInSameParty(final L2Character target)
    {
        return getParty() != null && target.getParty() != null &&
                getParty().getLeader() == target.getParty().getLeader();
    }

    /**
     * @param target
     * @return
     */
    public final boolean isInSameClan(final L2PcInstance target)
    {
        return getClanId() != 0 && getClanId() == target.getClanId();
    }

    /**
     * @param target
     * @return
     */
    public final boolean isInSameAlly(final L2PcInstance target)
    {
        return getAllyId() != 0 && getAllyId() == target.getAllyId();
    }

    /**
     * @param target
     * @return
     */
    public final boolean isInSameDuel(final L2PcInstance target)
    {
        return getDuelId() != 0 && getDuelId() == target.getDuelId();
    }

    /**
     * @param target
     * @return
     */
    public final boolean isInSameOlympiadGame(final L2PcInstance target)
    {
        return getOlympiadGameId() != 0 && getOlympiadGameId() == target.getOlympiadGameId();
    }

    /**
     * @param target
     * @return
     */
    public final boolean isInSameClanWar(final L2PcInstance target)
    {
        final L2Clan aClan = getClan();
        final L2Clan tClan = target.getClan();

        if (aClan != null && tClan != null && aClan != tClan)
        {
            if (aClan.isAtWarWith(tClan.getClanId()) && tClan.isAtWarWith(aClan.getClanId()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param target
     * @return
     */
    public final boolean isInSameSiegeSide(final L2PcInstance target)
    {
        if (getSiegeState() == 0 || target.getSiegeState() == 0)
        {
            return false;
        }

        final Siege s = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());

        if (s != null)
        {
            if (s.checkIsDefender(getClan()) && s.checkIsDefender(target.getClan()))
            {
                return true;
            }
            if (s.checkIsAttacker(getClan()) && s.checkIsAttacker(target.getClan()))
            {
                return true;
            }
        }

        return false;
    }

    /***
     *
     * @return
     */
    public final boolean isInsidePvpZone()
    {
        return isInsideZone(L2Character.ZONE_PVP) || isInsideZone(L2Character.ZONE_SIEGE);
    }

    private int _lastPhysicalDamages;

    public final void setLastPhysicalDamages(int lastPhysicalDamages)
    {
        _lastPhysicalDamages = lastPhysicalDamages;
    }

    public final int getLastPhysicalDamages()
    {
        return _lastPhysicalDamages;
    }

    private int _hatersAmount;

    public final void setHatersAmount(final int hatersAmount)
    {
        _hatersAmount = hatersAmount;
    }

    public final int getHatersAmount()
    {
        return _hatersAmount;
    }

    // ClassId, LevelRange, GearPreset
    public final Map<Integer, Map<Integer, int[][]>> _gearPresets = new HashMap<>();

    public final void addGearPreset(final int classId, final int levelRange, final int[][] gearPreset)
    {
        addGearPreset(classId, levelRange, gearPreset, false);
    }

    public final void addGearPreset(final int classId, final int levelRange, final int[][] gearPreset, final boolean store)
    {
        if (!_gearPresets.containsKey(classId))
        {
            _gearPresets.put(classId, new HashMap<>());
        }

        _gearPresets.get(classId).put(levelRange, gearPreset);

        if (store)
        {
            String presetData = "";

            for (int[] element : gearPreset)
            {
                if (element[1] == 0)
                {
                    continue;
                }

                presetData += element[0] + ","; // SlotId
                presetData += element[1] + ","; // ItemId
                presetData += element[2] + ";"; // ItemObjectId
            }

            String playerClassName = PlayerClassTable.getInstance().getClassNameById(classId);
            Connection con = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();

                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO character_gears_presets (playerId, classId, gearGrade, presetData) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE presetData=?");
                statement.setInt(1, getObjectId());
                statement.setInt(2, classId);
                statement.setInt(3, levelRange);
                statement.setString(4, presetData);
                statement.setString(5, presetData);

                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                Log.log(Level.SEVERE, "L2BufferInstance: Could not store gears preset for player : " + getObjectId(),
                        e);
            }
            finally
            {
                try
                {
                    con.close();
                }
                catch (Exception ignored)
                {

                }
            }

            sendMessage("Saved current equipment for future usage on your " + playerClassName + " Level " + getLevel() +
                    ".");
        }
    }

    public final int[][] getGearPreset(final int classId, final int levelRange)
    {
        if (!_gearPresets.containsKey(classId) || !_gearPresets.get(classId).containsKey(levelRange))
        {
            return null;
        }

        return _gearPresets.get(classId).get(levelRange);
    }

    public final Map<Integer, Map<Integer, int[][]>> getGearPresets()
    {
        return _gearPresets;
    }

    public final boolean equipGearPreset(final int classId, final int levelRange)
    {
        int[][] gearPreset = getGearPreset(classId, levelRange);

        if (gearPreset == null)
        {
            return false;
        }

        equipGearPreset(gearPreset);
        return true;
    }

    public final void equipGearPreset(final int[][] gearPreset)
    {
        if (getPrivateStoreType() != 0)
        {
            sendMessage("Gears couldn't be automatically swapped because you are in a private store mode.");
            return;
        }
        else if (isEnchanting() || getActiveEnchantAttrItem() != null || getActiveEnchantItem() != null ||
                getActiveEnchantSupportItem() != null)
        {
            sendMessage("Gears couldn't be automatically swapped because you have an active enchantment going on.");
            return;
        }
        else if (getActiveTradeList() != null)
        {
            sendMessage("Gears couldn't be automatically swapped because you have an active trade going on.");
            return;
        }

        for (int i = 0; i < gearPreset.length; i++)
        {
            getInventory().unEquipItemInSlot(i);
        }

        for (int[] element : gearPreset)
        {
            int itemId = element[1];
            int itemObjectId = element[2];

            if (itemId == 0 || itemObjectId == 0)
            {
                continue;
            }

            L2ItemInstance item = getInventory().getItemByObjectId(itemObjectId);

            if (item == null)
            {
                item = getInventory().getItemByItemId(itemId);

                if (item == null)
                {
                    sendMessage("Couldn't find " + ItemTable.getInstance().getTemplate(itemId).getName() +
                            " in your inventory.");
                    continue;
                }
            }
            //getInventory().unEquipItemInSlot(i);
			/*
			getInventory().unEquipItemInSlot(i);
			getInventory().unEquipItemInSlot(i);
			// If it's equipped, let's un-equip it first?
			if (item.isEquipped())
				useEquippableItem(item, true);
			 */
            useEquippableItem(item, true);
        }
    }

    public final int getGearGradeForCurrentLevel()
    {
        return getGearGradeForCurrentLevel(0);
    }

    public final int getGearGradeForCurrentLevel(int level)
    {
        final int playerLevel = level == 0 ? getLevel() : level;

        int levelRange = 0;

        if (playerLevel >= 99)
        {
            levelRange = L2Item.CRYSTAL_R99;
        }
        else if (playerLevel >= 95)
        {
            levelRange = L2Item.CRYSTAL_R95;
        }
        else if (playerLevel >= 85)
        {
            levelRange = L2Item.CRYSTAL_R;
        }
        else if (playerLevel >= 80)
        {
            levelRange = L2Item.CRYSTAL_S80;
        }
        else if (playerLevel >= 76)
        {
            levelRange = L2Item.CRYSTAL_S;
        }
        else if (playerLevel >= 61)
        {
            levelRange = L2Item.CRYSTAL_A;
        }
        else if (playerLevel >= 52)
        {
            levelRange = L2Item.CRYSTAL_B;
        }
        else if (playerLevel >= 40)
        {
            levelRange = L2Item.CRYSTAL_C;
        }
        else if (playerLevel >= 20)
        {
            levelRange = L2Item.CRYSTAL_D;
        }
        else
        {
            levelRange = L2Item.CRYSTAL_NONE;
        }

        return levelRange;
    }

    public final int[] getMinMaxLevelForGearGrade(final int gearGrade)
    {
        switch (gearGrade)
        {
            case L2Item.CRYSTAL_NONE:
                return new int[]{1, 19};
            case L2Item.CRYSTAL_D:
                return new int[]{20, 39};
            case L2Item.CRYSTAL_C:
                return new int[]{40, 51};
            case L2Item.CRYSTAL_B:
                return new int[]{52, 60};
            case L2Item.CRYSTAL_A:
                return new int[]{61, 75};
            case L2Item.CRYSTAL_S:
                return new int[]{76, 79};
            case L2Item.CRYSTAL_S80:
                return new int[]{80, 84};
            case L2Item.CRYSTAL_R:
                return new int[]{85, 94};
            case L2Item.CRYSTAL_R95:
                return new int[]{95, 98};
            case L2Item.CRYSTAL_R99:
                return new int[]{99, 99};
        }

        return null;
    }

    public final void restoreGearPresets()
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(
                    "SELECT classId, gearGrade, presetData FROM character_gears_presets WHERE playerId=?");
            statement.setInt(1, getObjectId());

            final ResultSet rset = statement.executeQuery();

            while (rset.next())
            {
                int classId = rset.getInt("classId");
                int gearGrade = rset.getInt("gearGrade");
                final String presetData = rset.getString("presetData");

                final String[] presetDataArgs = presetData.split(";");

                int[][] gearPreset = new int[Inventory.PAPERDOLL_TOTALSLOTS][3];

                for (String presetDataArg : presetDataArgs)
                {
                    String[] itemData = presetDataArg.split(",");

                    int slotId = Integer.parseInt(itemData[0]);
                    int itemId = Integer.parseInt(itemData[1]);
                    int itemObjectId = Integer.parseInt(itemData[2]);

                    gearPreset[slotId][0] = slotId;
                    gearPreset[slotId][1] = itemId;
                    gearPreset[slotId][2] = itemObjectId;
                }

                addGearPreset(classId, gearGrade, gearPreset, false);
            }

            statement.close();
        }
        catch (final SQLException e)
        {
            Log.log(Level.WARNING, "Could not restore gear presets for Player(" + getName() + ")...", e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (final Exception ignored)
            {

            }
        }
    }

    private Map<Integer, Integer> _tryingOn = new HashMap<>();

    public int getTryingOn(int slot)
    {
        if (!_tryingOn.containsKey(slot))
        {
            return 0;
        }

        return _tryingOn.get(slot);
    }

    public void tryOn(int itemId)
    {
        final L2Item item = ItemTable.getInstance().getTemplate(itemId);
        int slot = item.getBodyPart();
        if (slot == L2Item.SLOT_FULL_ARMOR || slot == L2Item.SLOT_ALLDRESS)
        {
            slot = L2Item.SLOT_CHEST;
        }

        final int invSlot = Inventory.getPaperdollIndex(slot);
        _tryingOn.put(invSlot, itemId);

        broadcastUserInfo();

        if (Config.isServer(Config.TENKAI) && !Config.isServer(Config.TENKAI_ESTHUS))
        {
            return;
        }

        sendMessage("You have one minute to see how the " + item.getName() + " appearance looks on your char.");
        ThreadPoolManager.getInstance().scheduleGeneral(() ->
        {
            if (!_tryingOn.containsKey(invSlot) || _tryingOn.get(invSlot) != item.getItemId())
            {
                return;
            }

            _tryingOn.remove(invSlot);

            broadcastUserInfo();
            sendMessage("Your minute to see the " + item.getName() + " appearance has expired.");
        }, 60000L);
    }

    private int _luckyEnchantStoneId;

    public final void setLuckyEnchantStoneId(final int stoneId)
    {
        _luckyEnchantStoneId = stoneId;
    }

    public final int getLuckyEnchantStoneId()
    {
        return _luckyEnchantStoneId;
    }

    private boolean _dwEquipped = false;
    private Future<?> _dragonBloodConsumeTask = null;

    public void onDWEquip()
    {
        _dwEquipped = true;
        //if (AttackStanceTaskManager.getInstance().getAttackStanceTask(this))
        //	startDragonBloodConsumeTask();
        ThreadPoolManager.getInstance().scheduleGeneral(() ->
        {
            if (isPlayingEvent() || isInOlympiadMode())
            {
                getInventory().unEquipItemInBodySlot(L2Item.SLOT_LR_HAND);
                broadcastUserInfo();
                sendPacket(new ItemList(L2PcInstance.this, false));
            }
            //sendPacket(new ExShowScreenMessage("Dragonclaw weapons are disabled! Let's test some R99 PvP for now ;)", 5000));
            //getInventory().unEquipItemInBodySlot(L2Item.SLOT_LR_HAND);
            //broadcastUserInfo();
            //sendPacket(new ItemList(L2PcInstance.this, false));
        }, 10L);
    }

    public void onDWUnequip()
    {
        _dwEquipped = false;
        if (_dragonBloodConsumeTask != null)
        {
            _dragonBloodConsumeTask.cancel(false);
            _dragonBloodConsumeTask = null;
        }
    }

    public void onCombatStanceStart()
    {
        //if (_dcEquipped)
        //	startDragonBloodConsumeTask();
    }

    public void onCombatStanceEnd()
    {
        if (_dragonBloodConsumeTask != null)
        {
            _dragonBloodConsumeTask.cancel(false);
            _dragonBloodConsumeTask = null;
        }
    }

    @SuppressWarnings("unused")
    private void startDragonBloodConsumeTask()
    {
        if (_dragonBloodConsumeTask != null)
        {
            return;
        }

        _dragonBloodConsumeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
        {
            if (!isInsideZone(ZONE_PEACE) && !destroyItemByItemId("dcConsume", 36415, 1, L2PcInstance.this, true))
            {
                sendPacket(new ExShowScreenMessage("You don't have Dragon Blood in your inventory!", 5000));
                getInventory().unEquipItemInBodySlot(L2Item.SLOT_LR_HAND);
                broadcastUserInfo();
                sendPacket(new ItemList(L2PcInstance.this, false));
            }
            else
            {
                L2ItemInstance dw = getActiveWeaponInstance();
                sendPacket(new ExShowScreenMessage("Your " + dw.getName() + " has just consumed 1 Dragon Blood",
                        5000));
            }
        }, 10L, 60000L);
    }
}
