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

package l2server;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntFloatHashMap;
import gnu.trove.TIntIntHashMap;
import l2server.gameserver.util.FloodProtectorConfig;
import l2server.log.Log;
import l2server.util.L2Properties;
import l2server.util.StringUtil;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Config
{
	public static final int DEFAULT = 0x01;
	public static final int TENKAI = 0x02;
	public static final int TENKAI_ESTHUS = 0x20;

	//--------------------------------------------------
	// Temporary Config File
	//--------------------------------------------------

	// Instances
	public static int FRINTEZZA_MIN_PLAYERS;
	public static int FREYA_MIN_PLAYERS;
	public static int ISTINA_MIN_PLAYERS;
	public static int OCTAVIS_MIN_PLAYERS;
	public static int TAUTI_MIN_PLAYERS;
	public static int TRASKEN_MIN_PLAYERS;
	public static int TEREDOR_MIN_PLAYERS;
	public static int SPEZION_MIN_PLAYERS;
	public static int BAYLOR_MIN_PLAYERS;
	public static int BALOK_MIN_PLAYERS;
	public static int ANTHARAS_INSTANCED_MIN_PLAYERS;
	public static int ANAKIM_MIN_PLAYERS;
	public static int LILITH_MIN_PLAYERS;
	public static int ANTHARAS_MIN_PLAYERS;
	public static int LINDVIOR_MIN_PLAYERS;
	public static int VALAKAS_MIN_PLAYERS;
	public static int BAIUM_MIN_PLAYERS;
	public static int KELBIM_MIN_PLAYERS;

	// Donation services
	public static int DONATION_COIN_ID;
	public static int REMOVE_CLAN_PENALTY_FROM_CLAN_PRICE;
	public static int REMOVE_CLAN_PENALTY_FROM_PLAYER_PRICE;
	public static int CHANGE_SEX_PRICE;
	public static int CHANGE_RACE_PRICE;
	public static int INCREASE_CLAN_LEVEL_PRICE;
	public static int CHANGE_CHAR_NAME_PRICE;
	public static int CHANGE_CLAN_NAME_PRICE;
	public static int CHANGE_NAME_COLOR_PRICE;

	//Custom Lottery System (Event)
	public static boolean ENABLE_CUSTOM_LOTTERY;
	public static int CUSTOM_LOTTERY_PRICE_ITEM_ID;
	public static int CUSTOM_LOTTERY_PRICE_AMOUNT;
	public static int CUSTOM_LOTTERY_REWARD_MULTIPLIER;

	//Custom Damage Manager (Event)
	public static boolean ENABLE_CUSTOM_DAMAGE_MANAGER;
	public static int CUSTOM_DAMAGE_MANAGER_REWARD_ID;
	public static int CUSTOM_DAMAGE_MANAGER_REWARD_AMOUNT;

	//Custom Auction
	public static boolean ENABLE_CUSTOM_AUCTIONS;

	//Custom World Altars
	public static boolean ENABLE_WORLD_ALTARS;

	//Kill Info Window
	public static boolean ENABLE_CUSTOM_KILL_INFO;

	//--------------------------------------------------
	// L2J Variable Definitions
	//--------------------------------------------------
	public static int MASTERACCESS_LEVEL;
	public static int MASTERACCESS_NAME_COLOR;
	public static int MASTERACCESS_TITLE_COLOR;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean DECREASE_SKILL_LEVEL;
	public static double ALT_WEIGHT_LIMIT;
	public static int RUN_SPD_BOOST;
	public static int DEATH_PENALTY_CHANCE;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean ALT_GAME_TIREDNESS;
	public static TIntIntHashMap SKILL_DURATION_LIST;
	public static boolean ENABLE_MODIFY_SKILL_REUSE;
	public static TIntIntHashMap SKILL_REUSE_LIST;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LOOT_HERBS;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte DANCES_MAX_AMOUNT;
	public static boolean DANCE_CANCEL_BUFF;
	public static boolean DANCE_CONSUME_ADDITIONAL_MP;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean EFFECT_CANCELING;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALLOW_CLASS_MASTERS;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
	public static int MAX_RUN_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_EVASION;
	public static byte MAX_LEVEL;
	public static byte MAX_PET_LEVEL;
	public static byte MAX_SUBCLASS;
	public static byte MAX_SUBCLASS_LEVEL;
	public static int MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
	public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static int MAX_PERSONAL_FAME_POINTS;
	public static int FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	public static int FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	public static int CASTLE_ZONE_FAME_TASK_FREQUENCY;
	public static int CASTLE_ZONE_FAME_AQUIRE_POINTS;
	public static boolean FAME_FOR_DEAD_PLAYERS;
	public static boolean IS_CRAFTING_ENABLED;
	public static boolean CRAFT_MASTERWORK;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int MAX_MEMBERS_IN_PARTY;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static boolean ALT_LEAVE_PARTY_LEADER;
	public static long STARTING_ADENA;
	public static byte STARTING_LEVEL;
	public static int STARTING_SP;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAIDS;
	public static int LOOT_RAIDS_PRIVILEGE_INTERVAL;
	public static int LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	public static int UNSTUCK_INTERVAL;
	public static int TELEPORT_WATCHDOG_TIMEOUT;
	public static int PLAYER_SPAWN_PROTECTION;
	public static TIntArrayList SPAWN_PROTECTION_ALLOWED_ITEMS;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static boolean RANDOM_RESPAWN_IN_TOWN_ENABLED;
	public static boolean OFFSET_ON_TELEPORT_ENABLED;
	public static int MAX_OFFSET_ON_TELEPORT;
	public static boolean RESTORE_PLAYER_INSTANCE;
	public static boolean ALLOW_SUMMON_TO_INSTANCE;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static int DELETE_DAYS;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static boolean DISABLE_TUTORIAL;
	public static boolean EXPERTISE_PENALTY;
	public static boolean STORE_RECIPE_SHOPLIST;
	public static boolean STORE_UI_SETTINGS;
	public static String[] FORBIDDEN_NAMES;
	public static double MAGE_PDEF_MULTIPLIER;

	//--------------------------------------------------
	// ClanHall Settings
	//--------------------------------------------------
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	public static boolean CH_BUFF_FREE;
	public static int CH_BID_ITEMID;
	public static int CH_BID_PRICE_DIVIDER;

	//--------------------------------------------------
	// Castle Settings
	//--------------------------------------------------
	public static long CS_TELE_FEE_RATIO;
	public static int CS_TELE1_FEE;
	public static int CS_TELE2_FEE;
	public static long CS_MPREG_FEE_RATIO;
	public static int CS_MPREG1_FEE;
	public static int CS_MPREG2_FEE;
	public static int CS_MPREG3_FEE;
	public static int CS_MPREG4_FEE;
	public static long CS_HPREG_FEE_RATIO;
	public static int CS_HPREG1_FEE;
	public static int CS_HPREG2_FEE;
	public static int CS_HPREG3_FEE;
	public static int CS_HPREG4_FEE;
	public static int CS_HPREG5_FEE;
	public static long CS_EXPREG_FEE_RATIO;
	public static int CS_EXPREG1_FEE;
	public static int CS_EXPREG2_FEE;
	public static int CS_EXPREG3_FEE;
	public static int CS_EXPREG4_FEE;
	public static long CS_SUPPORT_FEE_RATIO;
	public static int CS_SUPPORT1_FEE;
	public static int CS_SUPPORT2_FEE;
	public static int CS_SUPPORT3_FEE;
	public static int CS_SUPPORT4_FEE;
	public static ArrayList<String> CL_SET_SIEGE_TIME_LIST;
	public static TIntArrayList SIEGE_HOUR_LIST_MORNING;
	public static TIntArrayList SIEGE_HOUR_LIST_AFTERNOON;

	//--------------------------------------------------
	// Fortress Settings
	//--------------------------------------------------
	public static long FS_TELE_FEE_RATIO;
	public static int FS_TELE1_FEE;
	public static int FS_TELE2_FEE;
	public static long FS_MPREG_FEE_RATIO;
	public static int FS_MPREG1_FEE;
	public static int FS_MPREG2_FEE;
	public static long FS_HPREG_FEE_RATIO;
	public static int FS_HPREG1_FEE;
	public static int FS_HPREG2_FEE;
	public static long FS_EXPREG_FEE_RATIO;
	public static int FS_EXPREG1_FEE;
	public static int FS_EXPREG2_FEE;
	public static long FS_SUPPORT_FEE_RATIO;
	public static int FS_SUPPORT1_FEE;
	public static int FS_SUPPORT2_FEE;
	public static int FS_BLOOD_OATH_COUNT;
	public static int FS_UPDATE_FRQ;
	public static int FS_MAX_SUPPLY_LEVEL;
	public static int FS_FEE_FOR_CASTLE;
	public static int FS_MAX_OWN_TIME;
	public static int FS_SIEGE_DURATION;
	public static int FS_MERCHANT_RESPAWN;
	public static int FS_COUNTDOWN;
	public static int FS_MAX_FLAGS;
	public static int FS_MIN_CLAN_LVL;
	public static int FS_MAX_ATTACKER_CLANS;

	//--------------------------------------------------
	// Feature Settings
	//--------------------------------------------------
	public static int TAKE_FORT_POINTS;
	public static int LOOSE_FORT_POINTS;
	public static int TAKE_CASTLE_POINTS;
	public static int LOOSE_CASTLE_POINTS;
	public static int CASTLE_DEFENDED_POINTS;
	public static int HERO_POINTS;
	public static int ROYAL_GUARD_COST;
	public static int KNIGHT_UNIT_COST;
	public static int KNIGHT_REINFORCE_COST;
	public static int BALLISTA_POINTS;
	public static int BLOODALLIANCE_POINTS;
	public static int BLOODOATH_POINTS;
	public static int KNIGHTSEPAULETTE_POINTS;
	public static int REPUTATION_SCORE_PER_KILL;
	public static int JOIN_ACADEMY_MIN_REP_SCORE;
	public static int JOIN_ACADEMY_MAX_REP_SCORE;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;
	public static int CLAN_LEVEL_6_REQUIREMENT;
	public static int CLAN_LEVEL_7_REQUIREMENT;
	public static int CLAN_LEVEL_8_REQUIREMENT;
	public static int CLAN_LEVEL_9_REQUIREMENT;
	public static int CLAN_LEVEL_10_REQUIREMENT;
	public static int CLAN_LEVEL_11_REQUIREMENT;
	public static boolean ALLOW_WYVERN_DURING_SIEGE;

	//--------------------------------------------------
	// Clan War Settings
	//--------------------------------------------------
	public static int PREPARE_NORMAL_WAR_PERIOD;
	public static int PREPARE_MUTUAL_WAR_PERIOD;
	public static int BATTLE_WAR_PERIOD;
	public static int EXPIRE_NORMAL_WAR_PERIOD;
	public static int CANCEL_CLAN_WAR_REPUTATION_POINTS;
	public static int CLAN_WAR_MIN_CLAN_LEVEL;

	//--------------------------------------------------
	// General Settings
	//--------------------------------------------------
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean DISPLAY_SERVER_VERSION;
	public static boolean SERVER_LIST_BRACKET;
	public static int SERVER_LIST_TYPE;
	public static int SERVER_LIST_AGE;
	public static boolean SERVER_GMONLY;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean GM_STARTUP_DIET_MODE;
	public static String GM_ADMIN_MENU_STYLE;
	public static boolean GM_ITEM_RESTRICTION;
	public static boolean GM_SKILL_RESTRICTION;
	public static boolean GM_TRADE_RESTRICTED_ITEMS;
	public static boolean GM_RESTART_FIGHTING;
	public static boolean GM_ANNOUNCER_NAME;
	public static boolean GM_GIVE_SPECIAL_SKILLS;
	public static boolean BYPASS_VALIDATION;
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean GAMEGUARD_PROHIBITACTION;
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	public static boolean LOG_ITEMS_SMALL_LOG;
	public static boolean LOG_ITEM_ENCHANTS;
	public static boolean LOG_SKILL_ENCHANTS;
	public static boolean GMAUDIT;
	public static boolean LOG_GAME_DAMAGE;
	public static int LOG_GAME_DAMAGE_THRESHOLD;
	public static boolean SKILL_CHECK_ENABLE;
	public static boolean SKILL_CHECK_REMOVE;
	public static boolean SKILL_CHECK_GM;
	public static boolean DEBUG;
	public static boolean PACKET_HANDLER_DEBUG;
	public static boolean DEVELOPER;
	public static boolean ACCEPT_GEOEDITOR_CONN;
	public static boolean ALT_DEV_NO_HANDLERS;
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int AI_MAX_THREAD;
	public static int CLIENT_PACKET_QUEUE_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN;
	public static boolean DEADLOCK_DETECTOR;
	public static int DEADLOCK_CHECK_INTERVAL;
	public static boolean RESTART_ON_DEADLOCK;
	public static boolean ALLOW_DISCARDITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static TIntArrayList LIST_PROTECTED_ITEMS;
	public static int CHAR_STORE_INTERVAL;
	public static boolean LAZY_ITEMS_UPDATE;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean LAZY_CACHE;
	public static boolean CACHE_CHAR_NAMES;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static int COORD_SYNCHRONIZE;
	public static boolean ENABLE_FALLING_DAMAGE;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static int WORLD_X_MIN;
	public static int WORLD_X_MAX;
	public static int WORLD_Y_MIN;
	public static int WORLD_Y_MAX;
	public static int GEODATA;
	public static boolean GEODATA_CELLFINDING;
	public static String PATHFIND_BUFFERS;
	public static float LOW_WEIGHT;
	public static float MEDIUM_WEIGHT;
	public static float HIGH_WEIGHT;
	public static boolean ADVANCED_DIAGONAL_STRATEGY;
	public static float DIAGONAL_WEIGHT;
	public static int MAX_POSTFILTER_PASSES;
	public static boolean DEBUG_PATH;
	public static boolean FORCE_GEODATA;
	public static boolean MOVE_BASED_KNOWNLIST;
	public static long KNOWNLIST_UPDATE_INTERVAL;
	public static int ZONE_TOWN;
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_REFUND;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_ATTACHMENTS;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_BOAT;
	public static int BOAT_BROADCAST_RADIUS;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean ALLOW_NPC_WALKERS;
	public static boolean ALLOW_PET_WALKERS;
	public static boolean SERVER_NEWS;
	public static int COMMUNITY_TYPE;
	public static boolean BBS_SHOW_PLAYERLIST;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_TOKENS_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int ALT_OLY_WAIT_TIME;
	public static boolean ALT_OLY_NPC_REACTS;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static long ALT_LOTTERY_PRIZE;
	public static long ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static int ALT_ITEM_AUCTION_EXPIRED_AFTER;
	public static long ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static boolean JAIL_DISABLE_TRANSACTION;
	public static boolean ENABLE_BLOCK_CHECKER_EVENT;
	public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static boolean HBCE_FAIR_PLAY;
	public static String SERVER_NAME;
	public static String WEB_DB_NAME;
	public static String FORUM_DB_NAME;
	public static String LOGIN_DB_NAME;
	public static int SERVER_NAME_MASK;

	//--------------------------------------------------
	// FloodProtector Settings
	//--------------------------------------------------
	public static FloodProtectorConfig FLOOD_PROTECTOR_PICKUP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON;
	public static FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SHOUT_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRADE_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRANSACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANUFACTURE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANOR;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SENDMAIL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CHARACTER_SELECT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_AUCTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MAGICGEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_EVENTBYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_REPORT_BOT;

	//--------------------------------------------------
	// L2JMods Settings
	//--------------------------------------------------
	public static boolean L2JMOD_CHAMPION_ENABLE;
	public static boolean L2JMOD_CHAMPION_PASSIVE;
	public static int L2JMOD_CHAMPION_FREQUENCY;
	public static String L2JMOD_CHAMP_TITLE;
	public static int L2JMOD_CHAMP_MIN_LVL;
	public static int L2JMOD_CHAMP_MAX_LVL;
	public static int L2JMOD_CHAMPION_HP;
	public static int L2JMOD_CHAMPION_REWARDS;
	public static float L2JMOD_CHAMPION_ADENAS_REWARDS;
	public static float L2JMOD_CHAMPION_HP_REGEN;
	public static float L2JMOD_CHAMPION_ATK;
	public static float L2JMOD_CHAMPION_SPD_ATK;
	public static int L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_ID;
	public static int L2JMOD_CHAMPION_REWARD_QTY;
	public static boolean L2JMOD_CHAMPION_ENABLE_VITALITY;
	public static boolean L2JMOD_CHAMPION_ENABLE_IN_INSTANCES;
	public static boolean INSTANCED_EVENT_ENABLED;
	public static int INSTANCED_EVENT_INTERVAL;
	public static int INSTANCED_EVENT_RUNNING_TIME;
	public static int[] INSTANCED_EVENT_PARTICIPATION_FEE = new int[2];
	public static int INSTANCED_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int INSTANCED_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int INSTANCED_EVENT_START_LEAVE_TELEPORT_DELAY;
	public static boolean INSTANCED_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean INSTANCED_EVENT_SCROLL_ALLOWED;
	public static boolean INSTANCED_EVENT_POTIONS_ALLOWED;
	public static boolean INSTANCED_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static boolean INSTANCED_EVENT_REWARD_TEAM_TIE;
	public static byte INSTANCED_EVENT_MIN_LVL;
	public static byte INSTANCED_EVENT_MAX_LVL;
	public static int INSTANCED_EVENT_EFFECTS_REMOVAL;
	public static TIntIntHashMap INSTANCED_EVENT_FIGHTER_BUFFS;
	public static TIntIntHashMap INSTANCED_EVENT_MAGE_BUFFS;
	public static boolean L2JMOD_ALLOW_WEDDING;
	public static int L2JMOD_WEDDING_PRICE;
	public static boolean L2JMOD_WEDDING_PUNISH_INFIDELITY;
	public static boolean L2JMOD_WEDDING_TELEPORT;
	public static int L2JMOD_WEDDING_TELEPORT_PRICE;
	public static int L2JMOD_WEDDING_TELEPORT_DURATION;
	public static boolean L2JMOD_WEDDING_SAMESEX;
	public static boolean L2JMOD_WEDDING_FORMALWEAR;
	public static int L2JMOD_WEDDING_DIVORCE_COSTS;
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	public static boolean L2JMOD_ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean RESTORE_OFFLINERS;

	public static boolean OFFLINE_BUFFERS_ENABLE;
	public static boolean OFFLINE_BUFFERS_RESTORE;

	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_FAME;
	public static boolean L2JMOD_ENABLE_MANA_POTIONS_SUPPORT;
	public static boolean L2JMOD_DISPLAY_SERVER_TIME;
	public static boolean WELCOME_MESSAGE_ENABLED;
	public static String WELCOME_MESSAGE_TEXT;
	public static int WELCOME_MESSAGE_TIME;
	public static boolean L2JMOD_ANTIFEED_ENABLE;
	public static boolean L2JMOD_ANTIFEED_DUALBOX;
	public static boolean L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int L2JMOD_ANTIFEED_INTERVAL;
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
	public static boolean L2JMOD_CHAT_ADMIN;
	public static boolean L2JMOD_DEBUG_VOICE_COMMAND;
	public static int L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP;
	public static int L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP;
	public static Map<String, Integer> L2JMOD_DUALBOX_CHECK_WHITELIST;

	//--------------------------------------------------
	// NPC Settings
	//--------------------------------------------------
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALT_GAME_VIEWNPC;
	public static int MAX_DRIFT_RANGE;
	public static boolean DEEPBLUE_DROP_RULES;
	public static boolean DEEPBLUE_DROP_RULES_RAID;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_CREST_WITHOUT_QUEST;
	public static boolean ENABLE_RANDOM_ENCHANT_EFFECT;
	public static int MIN_NPC_LVL_DMG_PENALTY;
	public static TIntFloatHashMap NPC_DMG_PENALTY;
	public static TIntFloatHashMap NPC_CRIT_DMG_PENALTY;
	public static TIntFloatHashMap NPC_SKILL_DMG_PENALTY;
	public static int MIN_NPC_LVL_MAGIC_PENALTY;
	public static TIntFloatHashMap NPC_SKILL_CHANCE_PENALTY;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static TIntArrayList LIST_PET_RENT_NPC;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_PDEFENCE_MULTIPLIER;
	public static double RAID_MDEFENCE_MULTIPLIER;
	public static double RAID_PATTACK_MULTIPLIER;
	public static double RAID_MATTACK_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static TIntIntHashMap MINIONS_RESPAWN_TIME;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static boolean RAID_DISABLE_CURSE;
	public static int RAID_CHAOS_TIME;
	public static int GRAND_CHAOS_TIME;
	public static int MINION_CHAOS_TIME;
	public static int INVENTORY_MAXIMUM_PET;
	public static double PET_HP_REGEN_MULTIPLIER;
	public static double PET_MP_REGEN_MULTIPLIER;

	//--------------------------------------------------
	// Reputation Settings
	//--------------------------------------------------
	public static int REPUTATION_MIN_KARMA;
	public static int REPUTATION_MAX_KARMA;
	public static int REPUTATION_XP_DIVIDER;
	public static int REPUTATION_LOST_BASE;
	public static boolean REPUTATION_DROP_GM;
	public static boolean REPUTATION_AWARD_PK_KILL;
	public static int REPUTATION_PK_LIMIT;
	public static int[] REPUTATION_NONDROPPABLE_PET_ITEMS;
	public static int[] REPUTATION_NONDROPPABLE_ITEMS;
	public static int REPUTATION_ACQUIRED_FOR_CHAOTIC_KILL;
	public static int REPUTATION_SAME_KILL_INTERVAL;
	public static int REPUTATION_REP_PER_EXP_MULTIPLIER;
	public static int REPUTATION_REP_PER_SP_MULTIPLIER;

	//--------------------------------------------------
	// Rate Settings
	//--------------------------------------------------
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_EXTR_FISH;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_ITEMS_BY_RAID;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	public static float RATE_QUEST_DROP;
	public static float RATE_QUEST_REWARD;
	public static float RATE_QUEST_REWARD_XP;
	public static float RATE_QUEST_REWARD_SP;
	public static float RATE_QUEST_REWARD_ADENA;
	public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
	public static float RATE_QUEST_REWARD_POTION;
	public static float RATE_QUEST_REWARD_SCROLL;
	public static float RATE_QUEST_REWARD_RECIPE;
	public static float RATE_QUEST_REWARD_MATERIAL;
	public static TIntFloatHashMap RATE_DROP_ITEMS_ID;
	public static float RATE_REPUTATION_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_HP_HERBS;
	public static float RATE_DROP_MP_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int REPUTATION_KARMA_DROP_LIMIT;
	public static int REPUTATION_KARMA_RATE_DROP;
	public static int REPUTATION_KARMA_RATE_DROP_ITEM;
	public static int REPUTATION_KARMA_RATE_DROP_EQUIP;
	public static int REPUTATION_KARMA_RATE_DROP_EQUIP_WEAPON;
	public static double[] PLAYER_XP_PERCENT_LOST;

	//--------------------------------------------------
	// Server Settings
	//--------------------------------------------------
	public static int PORT_GAME;
	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static String GAMESERVER_HOSTNAME;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIME;
	public static int MAXIMUM_ONLINE_USERS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	public static boolean LOG_LOGIN_CONTROLLER;

	//--------------------------------------------------
	// MMO Settings
	//--------------------------------------------------
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;

	//--------------------------------------------------
	// Vitality Settings
	//--------------------------------------------------
	public static boolean ENABLE_VITALITY;
	public static boolean ENABLE_DROP_VITALITY_HERBS;
	public static float RATE_VITALITY_LOST;
	public static float VITALITY_MULTIPLIER;
	public static int STARTING_VITALITY_POINTS;

	//--------------------------------------------------
	// No classification assigned to the following yet
	//--------------------------------------------------
	public static int MAX_ITEM_IN_PACKET;
	public static boolean CHECK_KNOWN;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String[] GAME_SERVER_SUBNETS;
	public static String[] GAME_SERVER_HOSTS;
	public static int NEW_NODE_ID;
	public static int SELECTED_NODE_ID;
	public static int LINKED_NODE_ID;
	public static String NEW_NODE_TYPE;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;

	public enum IdFactoryType
	{
		Compaction, BitSet, Stack
	}

	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;

	public enum ObjectMapType
	{
		L2ObjectHashMap, WorldObjectMap
	}

	public enum ObjectSetType
	{
		L2ObjectHashSet, WorldObjectSet
	}

	public static ObjectMapType MAP_TYPE;
	public static ObjectSetType SET_TYPE;
	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_JEWELRY;
	public static int ENCHANT_CHANCE_ELEMENT_STONE;
	public static int ENCHANT_CHANCE_ELEMENT_CRYSTAL;
	public static int ENCHANT_CHANCE_ELEMENT_JEWEL;
	public static int ENCHANT_CHANCE_ELEMENT_ENERGY;
	public static int CHANGE_CHANCE_ELEMENT;
	public static int BLESSED_ENCHANT_CHANCE_WEAPON;
	public static int BLESSED_ENCHANT_CHANCE_ARMOR;
	public static int BLESSED_ENCHANT_CHANCE_JEWELRY;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static boolean ENCHANT_ALWAYS_SAFE;
	public static float[] ENCHANT_CHANCE_PER_LEVEL;
	public static float[] BLESSED_ENCHANT_CHANCE_PER_LEVEL;
	public static boolean ENABLE_CRYSTALLIZE_REWARDS;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean SHOW_LICENCE;
	public static boolean FORCE_GGAUTH;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;

	// GrandBoss Settings
	public static int BAIUM_INTERVAL_SPAWN;
	public static int BAIUM_RANDOM_SPAWN;
	public static int CORE_INTERVAL_SPAWN;
	public static int CORE_RANDOM_SPAWN;
	public static int ORFEN_INTERVAL_SPAWN;
	public static int ORFEN_RANDOM_SPAWN;
	public static int SAILREN_INTERVAL_SPAWN;
	public static int SAILREN_RANDOM_SPAWN;
	public static int QUEENANT_RANDOM_SPAWN;
	public static int QUEENANT_INTERVAL_SPAWN;
	public static int VALAKAS_WAIT_TIME;
	public static int VALAKAS_INTERVAL_SPAWN;
	public static int VALAKAS_RANDOM_SPAWN;
	public static int ANTHARAS_WAIT_TIME;
	public static int ANTHARAS_INTERVAL_SPAWN;
	public static int ANTHARAS_RANDOM_SPAWN;
	public static int LINDVIOR_INTERVAL_SPAWN;
	public static int LINDVIOR_RANDOM_SPAWN;
	public static int KELBIM_INTERVAL_SPAWN;
	public static int KELBIM_RANDOM_SPAWN;

	// Gracia Seeds Settings
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;

	//chatfilter
	public static ArrayList<String> FILTER_LIST;
	public static ArrayList<String> LANGUAGE_FILTER;

	// Conquerable Halls Settings
	public static int CHS_SIEGE_LENGTH;
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static int CHS_SIEGE_INTERVAL;

	// Security
	public static boolean SECOND_AUTH_ENABLED;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static long SECOND_AUTH_BAN_TIME;
	public static String SECOND_AUTH_REC_LINK;

	public static File DATAPACK_ROOT = new File("./");
	public static String DATA_FOLDER = "data/";
	public static boolean IS_CLASSIC = false;

	//--------------------------------------------------
	// L2J Property File Definitions
	//--------------------------------------------------
	public static final String CONFIG_DIRECTORY = "./config/";
	public static final String HEXID_FILE = "hexid.txt";
	public static final String CHAT_FILTER_FILE = "chatfilter.txt";
	public static final String LANGUAGE_FILTER_FILE = "languagefilter.txt";

	public static String CONFIG_FILE = "default.cfg";

	private static List<ConfigVar> _configs = new ArrayList<>();

	static class ConfigVar
	{
		public String fieldName;
		public String confName;
		public String deflt;
	}

	/**
	 * This class initializes all global variables for configuration.<br>
	 * If the key doesn't appear in properties file, a default value is set by this class.
	 */
	public static void load()
	{
		// Start by loading boot parameters.
		boolean failedLoadingBoot = false;
		try
		{
			File f = new File("./config.cfg");
			if (!f.exists())
			{
				Log.warning("./config.cfg could not be found!!!");
				failedLoadingBoot = true;
			}
			else
			{
				List<String> allLines = Files.readAllLines(Paths.get("./config.cfg"), StandardCharsets.UTF_8);

				for (String line : allLines)
				{
					if (line.startsWith("#"))
					{
						continue;
					}

					if (line.startsWith("ConfigFile="))
					{
						CONFIG_FILE = line.replace("ConfigFile=", "");
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Log.warning("There was an issue loading your config.cfg.");
			failedLoadingBoot = true;
		}

		if (failedLoadingBoot)
		{
			try
			{
				Log.info("DEFAULT CONFIGURATIONS WILL BE LOADED. Press ENTER to continue.");
				CONFIG_FILE = "default.cfg";
				System.in.read();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		if (ServerMode.serverMode == ServerMode.MODE_GAMESERVER)
		{
			Log.info("Loading GameServer[" + CONFIG_FILE + "] Configuration Files...");
			InputStream is = null;
			try
			{
				loadConfigVars("./config/game.xml");
				loadConfigs();

				if (IS_CLASSIC)
				{
					DATA_FOLDER = "data_classic/";
				}

				MAX_ITEM_IN_PACKET =
						Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));

				if (SERVER_NAME.startsWith("tenkai"))
				{
					SERVER_NAME_MASK = TENKAI;
				}
				else
				{
					SERVER_NAME_MASK = DEFAULT;
				}

				if (SERVER_NAME.contains("esthus"))
				{
					SERVER_NAME_MASK |= TENKAI_ESTHUS;
				}

				//TODO data driven pls
				WEB_DB_NAME = "l2" + SERVER_NAME.split("_")[0] + "_web";
				FORUM_DB_NAME = "l2" + SERVER_NAME.split("_")[0] + "_board";
				LOGIN_DB_NAME = "l2" + SERVER_NAME.split("_")[0] + "_common";

				// Chat Filter (default)
				try
				{
					FILTER_LIST = new ArrayList<>();
					LineNumberReader lnr = new LineNumberReader(
							new BufferedReader(new FileReader(new File(CONFIG_DIRECTORY + CHAT_FILTER_FILE))));
					String line = null;
					while ((line = lnr.readLine()) != null)
					{
						if (line.trim().isEmpty() || line.startsWith("#"))
						{
							continue;
						}

						FILTER_LIST.add(line.trim());
					}
					lnr.close();
					Log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CONFIG_DIRECTORY + CHAT_FILTER_FILE + " File.");
				}

				// Language Chat Filter (Tenkai Custom)
				try
				{
					LANGUAGE_FILTER = new ArrayList<>();
					LineNumberReader lnr = new LineNumberReader(
							new BufferedReader(new FileReader(new File(CONFIG_DIRECTORY + LANGUAGE_FILTER_FILE))));
					String line = null;
					while ((line = lnr.readLine()) != null)
					{
						if (line.trim().isEmpty() || line.startsWith("#"))
						{
							continue;
						}

						LANGUAGE_FILTER.add(line.trim());
					}
					Log.info("Loaded " + LANGUAGE_FILTER.size() + " Foreign Keywords.");
					lnr.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CONFIG_DIRECTORY + LANGUAGE_FILTER_FILE + " File.");
				}

				try
				{
					L2Properties Settings = new L2Properties();
					is = new FileInputStream(CONFIG_DIRECTORY + HEXID_FILE);
					Settings.load(is);
					SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
					HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
				}
				catch (Exception e)
				{
					Log.warning("Could not load HexID file (" + CONFIG_DIRECTORY + HEXID_FILE +
							"). Hopefully login will give us one.");
				}
			}
			finally
			{
				try
				{
					is.close();
				}
				catch (Exception ignored)
				{
				}
			}
		}
		else if (ServerMode.serverMode == ServerMode.MODE_LOGINSERVER)
		{
			Log.info("loading login config");
			InputStream is = null;
			try
			{
				loadConfigVars("./config/login.xml");
				loadConfigs();
			}
			finally
			{
				try
				{
					is.close();
				}
				catch (Exception ignored)
				{
				}
			}
		}
		else
		{
			Log.severe("Could not Load Config: server mode was not set");
		}
	}

	/**
	 * Save hexadecimal ID of the server in the L2Properties file.
	 *
	 * @param string (String) : hexadecimal ID of the server to store
	 * @link LoginServerThread
	 */
	public static void saveHexid(int serverId, String string)
	{
		Config.saveHexid(serverId, string, CONFIG_DIRECTORY + HEXID_FILE);
	}

	/**
	 * Save hexadecimal ID of the server in the L2Properties file.
	 *
	 * @param hexId    (String) : hexadecimal ID of the server to store
	 * @param fileName (String) : name of the L2Properties file
	 */
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			L2Properties hexSetting = new L2Properties();
			File file = new File(fileName);
			//Create a new empty file only if it doesn't exist
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			hexSetting.store(out, "the hexID to auth into login");
			out.close();
		}
		catch (Exception e)
		{
			Log.warning(StringUtil.concat("Failed to save hex id to ", fileName, " File."));
			e.printStackTrace();
		}
	}

	private static void loadConfigVars(String fileName)
	{
		try
		{
			XmlDocument doc = new XmlDocument(new File(fileName));
			for (XmlNode n : doc.getFirstChild().getChildren())
			{
				if (n.getName().equalsIgnoreCase("config"))
				{
					ConfigVar conf = new ConfigVar();
					conf.confName = n.getString("name");
					conf.fieldName = n.getString("var");
					conf.deflt = n.getString("default");
					_configs.add(conf);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void loadConfigs()
	{
		InputStream is;
		try
		{
			L2Properties settings = new L2Properties();
			is = new FileInputStream(new File(CONFIG_DIRECTORY + CONFIG_FILE));
			settings.load(is);

			for (ConfigVar conf : _configs)
			{
				Field field;
				try
				{
					field = Config.class.getField(conf.fieldName);
				}
				catch (Exception e)
				{
					Log.warning("Non existing config: " + conf.fieldName);
					e.printStackTrace();
					continue;
				}

				int modifiers = field.getModifiers();
				if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers))
				{
					Log.warning("Cannot modify non public or non static config: " + conf.fieldName);
					continue;
				}

				String value = settings.getProperty(conf.confName, conf.deflt);
				if (field.getType() == int.class)
				{
					if (value.startsWith("0x"))
					{
						field.setInt(field, Integer.parseInt(value.substring(2), 16));
					}
					else
					{
						field.setInt(field, Integer.parseInt(value));
					}
				}
				else if (field.getType() == short.class)
				{
					field.setShort(field, Short.parseShort(value));
				}
				else if (field.getType() == byte.class)
				{
					field.setByte(field, Byte.parseByte(value));
				}
				else if (field.getType() == long.class)
				{
					field.setLong(field, Long.parseLong(value));
				}
				else if (field.getType() == float.class)
				{
					field.setFloat(field, Float.parseFloat(value));
				}
				else if (field.getType() == double.class)
				{
					field.setDouble(field, Double.parseDouble(value));
				}
				else if (field.getType() == boolean.class)
				{
					field.setBoolean(field, Boolean.parseBoolean(value));
				}
				else if (field.getType() == String.class)
				{
					field.set(field, value);
				}
				else if (field.getType() == int[].class)
				{
					field.set(field, parseIntArray(value));
				}
				else if (field.getType() == float[].class)
				{
					field.set(field, parseFloatArray(value));
				}
				else if (field.getType() == double[].class)
				{
					field.set(field, parseDoubleArray(value));
				}
				else if (field.getType() == String[].class)
				{
					field.set(field, value.split(","));
				}
				else if (field.getType() == ArrayList.class)
				{
					String[] split = value.split(",");
					ArrayList<String> list = new ArrayList<>();
					for (String s : split)
					{
						list.add(s);
					}
					field.set(field, list);
				}
				else if (field.getType() == TIntArrayList.class)
				{
					field.set(field, parseIntArrayList(value));
				}
				else if (field.getType() == TIntIntHashMap.class)
				{
					field.set(field, parseIntIntMap(value));
				}
				else if (field.getType() == TIntFloatHashMap.class)
				{
					field.set(field, parseIntFloatMap(value));
				}
				else if (field.getType() == Map.class)
				{
					field.set(field, parseStringIntMap(value));
				}
				else if (field.getType() == int[][].class)
				{
					field.set(field, parseItemsList(value));
				}
				else if (field.getType() == ObjectMapType.class)
				{
					field.set(field, ObjectMapType.valueOf(value));
				}
				else if (field.getType() == ObjectSetType.class)
				{
					field.set(field, ObjectSetType.valueOf(value));
				}
				else if (field.getType() == IdFactoryType.class)
				{
					field.set(field, IdFactoryType.valueOf(value));
				}
				else
				{
					Log.warning("Unsupported field type: " + field.getType());
				}
			}

			loadFloodProtectorConfigs(settings);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + CONFIG_DIRECTORY + CONFIG_FILE + " File.");
		}
	}

	/**
	 * Loads flood protector configurations.
	 */
	private static void loadFloodProtectorConfigs(final L2Properties properties)
	{
		FLOOD_PROTECTOR_PICKUP_ITEM = new FloodProtectorConfig("PickUpItemFloodProtector");
		FLOOD_PROTECTOR_USE_ITEM = new FloodProtectorConfig("UseItemFloodProtector");
		FLOOD_PROTECTOR_ROLL_DICE = new FloodProtectorConfig("RollDiceFloodProtector");
		FLOOD_PROTECTOR_FIREWORK = new FloodProtectorConfig("FireworkFloodProtector");
		FLOOD_PROTECTOR_ITEM_PET_SUMMON = new FloodProtectorConfig("ItemPetSummonFloodProtector");
		FLOOD_PROTECTOR_HERO_VOICE = new FloodProtectorConfig("HeroVoiceFloodProtector");
		FLOOD_PROTECTOR_SHOUT_CHAT = new FloodProtectorConfig("ShoutChatFloodProtector");
		FLOOD_PROTECTOR_TRADE_CHAT = new FloodProtectorConfig("TradeChatFloodProtector");
		FLOOD_PROTECTOR_GLOBAL_CHAT = new FloodProtectorConfig("GlobalChatFloodProtector");
		FLOOD_PROTECTOR_SUBCLASS = new FloodProtectorConfig("SubclassFloodProtector");
		FLOOD_PROTECTOR_DROP_ITEM = new FloodProtectorConfig("DropItemFloodProtector");
		FLOOD_PROTECTOR_SERVER_BYPASS = new FloodProtectorConfig("ServerBypassFloodProtector");
		FLOOD_PROTECTOR_MULTISELL = new FloodProtectorConfig("MultiSellFloodProtector");
		FLOOD_PROTECTOR_TRANSACTION = new FloodProtectorConfig("TransactionFloodProtector");
		FLOOD_PROTECTOR_MANUFACTURE = new FloodProtectorConfig("ManufactureFloodProtector");
		FLOOD_PROTECTOR_MANOR = new FloodProtectorConfig("ManorFloodProtector");
		FLOOD_PROTECTOR_SENDMAIL = new FloodProtectorConfig("SendMailFloodProtector");
		FLOOD_PROTECTOR_CHARACTER_SELECT = new FloodProtectorConfig("CharacterSelectFloodProtector");
		FLOOD_PROTECTOR_ITEM_AUCTION = new FloodProtectorConfig("ItemAuctionFloodProtector");
		FLOOD_PROTECTOR_MAGICGEM = new FloodProtectorConfig("MagicGemFloodProtector");
		FLOOD_PROTECTOR_EVENTBYPASS = new FloodProtectorConfig("EventBypassFloodProtector");
		FLOOD_PROTECTOR_REPORT_BOT = new FloodProtectorConfig("ReportBotFloodProtector");

		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_PICKUP_ITEM, "PickUpItem", "50");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", "0");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", "16");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SHOUT_CHAT, "ShoutChat", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRADE_CHAT, "TradeChat", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL_CHAT, "GlobalChat", "20");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", "20");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", "1");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", "1");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRANSACTION, "Transaction", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANUFACTURE, "Manufacture", "3");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANOR, "Manor", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SENDMAIL, "SendMail", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHARACTER_SELECT, "CharacterSelect", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_AUCTION, "ItemAuction", "9");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MAGICGEM, "MagicGem", "20");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_EVENTBYPASS, "EventBypass", "1");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_REPORT_BOT, "ReportBot", "18000");
	}

	/**
	 * Loads single flood protector configuration.
	 *
	 * @param properties      L2Properties file reader
	 * @param config          flood protector configuration instance
	 * @param configString    flood protector configuration string that determines for which flood protector
	 *                        configuration should be read
	 * @param defaultInterval default flood protector interval
	 */
	private static void loadFloodProtectorConfig(final L2Properties properties, final FloodProtectorConfig config, final String configString, final String defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = Integer.parseInt(
				properties.getProperty(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval));
		config.LOG_FLOODING = Boolean.parseBoolean(
				properties.getProperty(StringUtil.concat("FloodProtector", configString, "LogFlooding"), "False"));
		config.PUNISHMENT_LIMIT = Integer.parseInt(
				properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), "0"));
		config.PUNISHMENT_TYPE =
				properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = Integer.parseInt(
				properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), "0"));
	}

	public static int getServerTypeId(String[] serverTypes)
	{
		int tType = 0;
		for (String cType : serverTypes)
		{
			cType = cType.trim();
			if (cType.equalsIgnoreCase("Normal"))
			{
				tType |= 0x01;
			}
			else if (cType.equalsIgnoreCase("Relax"))
			{
				tType |= 0x02;
			}
			else if (cType.equalsIgnoreCase("Test"))
			{
				tType |= 0x04;
			}
			else if (cType.equalsIgnoreCase("NoLabel"))
			{
				tType |= 0x08;
			}
			else if (cType.equalsIgnoreCase("Restricted"))
			{
				tType |= 0x10;
			}
			else if (cType.equalsIgnoreCase("Event"))
			{
				tType |= 0x20;
			}
			else if (cType.equalsIgnoreCase("Free"))
			{
				tType |= 0x40;
			}
		}
		return tType;
	}

	private static int[] parseIntArray(String line)
	{
		if (line.isEmpty())
		{
			return new int[0];
		}

		String[] split = line.split(",");
		int[] result = new int[split.length];
		int i = 0;
		for (String id : split)
		{
			result[i] = Integer.parseInt(id);
			i++;
		}

		return result;
	}

	private static float[] parseFloatArray(String line)
	{
		if (line.isEmpty())
		{
			return new float[0];
		}

		String[] split = line.split(",");
		float[] result = new float[split.length];
		int i = 0;
		for (String id : split)
		{
			result[i] = Float.parseFloat(id);
			i++;
		}

		return result;
	}

	private static TIntArrayList parseIntArrayList(String line)
	{
		String[] split = line.split(",");
		TIntArrayList result = new TIntArrayList(split.length);
		if (line.isEmpty())
		{
			return result;
		}

		for (String id : split)
		{
			result.add(Integer.parseInt(id));
		}

		return result;
	}

	private static TIntIntHashMap parseIntIntMap(String line)
	{
		String[] propertySplit = line.split(";");
		TIntIntHashMap result = new TIntIntHashMap(propertySplit.length);
		if (line.isEmpty())
		{
			return result;
		}

		for (String prop : propertySplit)
		{
			String[] propSplit = prop.split(",");
			if (propSplit.length != 2)
			{
				Log.warning(StringUtil.concat("Invalid config property -> \"", prop, "\""));
			}

			try
			{
				result.put(Integer.valueOf(propSplit[0]), Integer.valueOf(propSplit[1]));
			}
			catch (NumberFormatException nfe)
			{
				if (!prop.isEmpty())
				{
					Log.warning(StringUtil.concat("Invalid config property -> \"", propSplit[0], "\"", propSplit[1]));
				}
			}
		}

		return result;
	}

	private static TIntFloatHashMap parseIntFloatMap(String line)
	{
		if (line.contains(" "))
		{
			String[] propertySplit = line.split(",");
			TIntFloatHashMap ret = new TIntFloatHashMap(propertySplit.length);
			int i = 1;
			for (String value : propertySplit)
			{
				ret.put(i++, Float.parseFloat(value));
			}
			return ret;
		}

		String[] propertySplit = line.split(";");
		TIntFloatHashMap ret = new TIntFloatHashMap(propertySplit.length);
		for (String value : propertySplit)
		{
			ret.put(Integer.parseInt(value.split(",")[0]), Float.parseFloat(value.split(",")[1]));
		}
		return ret;
	}

	private static Map<String, Integer> parseStringIntMap(String line)
	{
		String[] propertySplit = line.split(";");
		Map<String, Integer> result = new HashMap<>();
		if (line.isEmpty())
		{
			return result;
		}

		for (String prop : propertySplit)
		{
			String[] propSplit = prop.split(",");
			if (propSplit.length != 2)
			{
				Log.warning(StringUtil.concat("Invalid config property -> \"", prop, "\""));
			}

			try
			{
				result.put(propSplit[0], Integer.valueOf(propSplit[1]));
			}
			catch (NumberFormatException nfe)
			{
				if (!prop.isEmpty())
				{
					Log.warning(StringUtil.concat("Invalid config property -> \"", propSplit[0], "\"", propSplit[1]));
				}
			}
		}

		return result;
	}

	public static double[] parseDoubleArray(String value)
	{
		double[] array = new double[Byte.MAX_VALUE + 1];

		// Default value
		for (int i = 0; i <= Byte.MAX_VALUE; i++)
		{
			array[i] = 1.;
		}

		// Now loading into table parsed values
		try
		{
			String[] values = value.split(";");
			for (String s : values)
			{
				int min;
				int max;
				double val;

				String[] vals = s.split("-");
				String[] mM = vals[0].split(",");

				min = Integer.parseInt(mM[0]);
				max = Integer.parseInt(mM[1]);
				val = Double.parseDouble(vals[1]);

				for (int i = min; i <= max; i++)
				{
					array[i] = val;
				}
			}
		}
		catch (Exception e)
		{
			Log.warning("Error while loading double array");
			e.printStackTrace();
		}

		return array;
	}

	/**
	 * itemId1,itemNumber1;itemId2,itemNumber2...
	 * to the int[n][2] = [itemId1][itemNumber1],[itemId2][itemNumber2]...
	 */
	private static int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
		{
			return null;
		}

		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				Log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid entry -> \"", valueSplit[0],
						"\", should be itemId,itemNumber"));
				return null;
			}

			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				Log.warning(
						StringUtil.concat("parseItemsList[Config.load()]: invalid itemId -> \"", valueSplit[0], "\""));
				return null;
			}
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				Log.warning(StringUtil
						.concat("parseItemsList[Config.load()]: invalid item number -> \"", valueSplit[1], "\""));
				return null;
			}
			i++;
		}
		return result;
	}

	public static double[] MOONLAND_EXPERIENCE_RATE_MULTIPLIER = {
			10.0, // Level 1
			10.5, // Level 2
			11.0, // Level 3
			11.5, // Level 4
			12.0, // Level 5
			13.0, // Level 6
			14.0, // Level 7
			16.0, // Level 8
			18.0, // Level 9
			20.0, // Level 10
			22.0, // Level 11
			24.0, // Level 12
			26.0, // Level 13
			28.0, // Level 14
			30.0, // Level 15
			32.0, // Level 16
			34.0, // Level 17
			36.0, // Level 18
			38.0, // Level 19
			40.0, // Level 20
			42.0, // Level 21
			44.0, // Level 22
			46.0, // Level 23
			48.0, // Level 24
			50.0, // Level 25
			52.0, // Level 26
			54.0, // Level 27
			56.0, // Level 28
			58.0, // Level 29
			60.0, // Level 30
			62.0, // Level 31
			64.0, // Level 32
			66.0, // Level 33
			68.0, // Level 34
			70.0, // Level 35
			72.0, // Level 36
			74.0, // Level 37
			76.0, // Level 38
			78.0, // Level 39
			80.0, // Level 40
			82.0, // Level 41
			84.0, // Level 42
			86.0, // Level 43
			88.0, // Level 44
			90.0, // Level 45
			92.0, // Level 46
			94.0, // Level 47
			96.0, // Level 48
			98.0, // Level 49
			100.0, // Level 50
			102.0, // Level 51
			104.0, // Level 52
			106.0, // Level 53
			108.0, // Level 54
			110.0, // Level 55
			112.0, // Level 56
			114.0, // Level 57
			118.0, // Level 58
			120.0, // Level 59
			122.0, // Level 60
			124.0, // Level 61
			126.0, // Level 62
			128.0, // Level 63
			130.0, // Level 64
			132.0, // Level 65
			134.0, // Level 66
			136.0, // Level 67
			138.0, // Level 68
			140.0, // Level 69
			142.0, // Level 70
			144.0, // Level 71
			146.0, // Level 72
			148.0, // Level 73
			150.0, // Level 74
			152.5, // Level 75
			154.0, // Level 76
			156.5, // Level 77
			158.0, // Level 78
			160.0, // Level 79
			162.0, // Level 80
			164.0, // Level 81
			166.0, // Level 82
			168.0, // Level 83
			170.0, // Level 84
			172.0, // Level 85
			174.0, // Level 86
			176.0, // Level 87
			178.0, // Level 88
			180.0, // Level 89
			182.0, // Level 90
			184.0, // Level 91
			186.0, // Level 92
			188.0, // Level 93
			190.0, // Level 94
			192.0, // Level 95
			194.0, // Level 96
			196.0, // Level 97
			198.0, // Level 98
			200.0, // Level 99
	};

	public static double getExperienceMultiplierFor(final int level)
	{
		if (level <= 0)
		{
			return 1.0;
		}

		return MOONLAND_EXPERIENCE_RATE_MULTIPLIER[level - 1];
	}

	public static boolean isServer(int server)
	{
		return (SERVER_NAME_MASK & server) > 0;
	}

	public static File findResource(final String path)
	{
		final File custom = findCustomResource(path);
		if (custom.exists())
		{
			return custom;
		}

		Log.warning("Config: Custom Path doesn't exist: " + path);

		return findNonCustomResource(path);
	}

	public static File findCustomResource(final String path)
	{
		return new File("data_" + SERVER_NAME + "/", path);
	}

	public static File findNonCustomResource(final String path)
	{
		return new File("data/", path);
	}
}
