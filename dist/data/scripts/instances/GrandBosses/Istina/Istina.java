package instances.GrandBosses.Istina;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Istina Boss - Normal/Extreme mode
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=f2O97hNztBs
 *         - http://l2wiki.com/Istina
 *         - http://tw.myblog.yahoo.com/l2_friend/article?mid=28883&next=28764&l=f&fid=148
 */

public class Istina extends L2AttackableAIScript
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "Istina";

    //Id's
    private static final int _acidDummyNpc = 18919;
    private static final int _boxgMagicPower = 30371;
    private static final int _istinaCrystal = 37506;
    private static final int _sealingEnergy = 19036;
    private static final int _energyDevice = 17608;
    private static final int _istinasCreationId = 23125;
    //private static final int	_failedCreation		= 23037;
    private static final int _taklacanId = 23030;
    private static final int _torumbaId = 23031;
    private static final int _dopagen = 23032;
    private static final int _effectRedCircle = 14220101;
    private static final int _effectBlueCircle = 14220102;
    private static final int _effectGreenCircle = 14220103;
    private static final int _ballistaId = 19021;
    private static final int _rumieseEnterId = 33293;
    private static final int _rumieseInnerId = 33151;
    private static final int[] _all_mobs = {29195, 29196, _ballistaId, _sealingEnergy};
    private static final int[] _minionIds = {_taklacanId, _torumbaId, _dopagen};
    private static final int[] _templates = {169, 170};
    private static final int _ZONE_BLUE_ID = 60021;
    private static final int _ZONE_RED_ID = 60022;
    private static final int _ZONE_GREEN_ID = 60020;
    private static final int _manifestation_red_id = 14212;
    private static final int _manifestation_blue_id = 14213;
    private static final int _manifestarion_green_id = 14214;

    //Zones
    private static final L2ZoneType _ZONE_BLUE = ZoneManager.getInstance().getZoneById(_ZONE_BLUE_ID);
    private static final L2ZoneType _ZONE_RED = ZoneManager.getInstance().getZoneById(_ZONE_RED_ID);
    private static final L2ZoneType _ZONE_GREEN = ZoneManager.getInstance().getZoneById(_ZONE_GREEN_ID);

    //Skills
    private static final L2Skill _energyControlDevice = SkillTable.getInstance().getInfo(14224, 1);
    private static final L2Skill _flood = SkillTable.getInstance().getInfo(14220, 1);
    private static final L2Skill _deathBlow = SkillTable.getInstance().getInfo(14219, 1);
    private static final L2Skill _dummyAcidEruption = SkillTable.getInstance().getInfo(14222, 1);
    private static final L2Skill _dummyEndAcidEruption = SkillTable.getInstance().getInfo(14223, 1);
    private static final L2Skill _manifestation_red = SkillTable.getInstance().getInfo(_manifestation_red_id, 1);
    private static final L2Skill _manifestation_blue = SkillTable.getInstance().getInfo(_manifestation_blue_id, 1);
    private static final L2Skill _manifestation_green = SkillTable.getInstance().getInfo(_manifestarion_green_id, 1);
    private static final L2Skill[] _manifestation_of_authority =
            {_manifestation_red, _manifestation_blue, _manifestation_green};

    //Cords
    private static final Location[] _playerEnter = {
            new Location(-177100, 141730, -11264),
            new Location(-176802, 142267, -11269),
            new Location(-177235, 142597, -11264),
            new Location(-177133, 142992, -11269),
            new Location(-177124, 142264, -11269)
    };

    private static final Location[] _minionLocs = {
            new Location(-178695, 147188, -11391, 3992),
            new Location(-175462, 147184, -11391, 27931),
            new Location(-176437, 149527, -11391, 44860)
    };

    //Others
    private enum zoneInUse
    {
        USE_ZONE_BLUE, USE_ZONE_RED, USE_ZONE_GREEN, NONE
    }

    ;

    private class IstinaWorld extends InstanceWorld
    {
        private int IstinaId;
        private double maxBallistDamage;
        private double currBallistDamage;
        private int ballistaSeconds;
        private L2Npc Istina;
        private L2Npc Ballista;
        private zoneInUse zone;
        private L2Skill zoneDebuff;
        private boolean isHardMode;
        private ArrayList<L2PcInstance> rewardedPlayers;

        public IstinaWorld()
        {
            isHardMode = false;
            ballistaSeconds = 30;
            zone = zoneInUse.NONE;
            rewardedPlayers = new ArrayList<L2PcInstance>();
        }
    }

    public Istina(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addEnterZoneId(_ZONE_BLUE_ID);
        addEnterZoneId(_ZONE_RED_ID);
        addEnterZoneId(_ZONE_GREEN_ID);
        addTalkId(_rumieseEnterId);
        addStartNpc(_rumieseEnterId);
        addTalkId(_rumieseInnerId);
        addStartNpc(_rumieseInnerId);
        addFirstTalkId(_rumieseInnerId);
        addSpellFinishedId(_acidDummyNpc);

        for (int mob : _all_mobs)
        {
            addAttackId(mob);
            addSpellFinishedId(mob);
            addSkillSeeId(mob);
        }
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onFirstTalk: " + player.getName());
        }

        InstanceWorld wrld = null;
        if (npc != null)
        {
            wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        }
        else
        {
            wrld = InstanceManager.getInstance().getPlayerWorld(player);
        }

        if (wrld != null && wrld instanceof IstinaWorld)
        {
            IstinaWorld world = (IstinaWorld) wrld;
            if (npc.getNpcId() == _rumieseInnerId)
            {
                if (world.status == 8)
                {
                    return "RumieseInnerBallistaLoaded.html";
                }
                else if (world.status >= 5 && world.status < 7)
                {
                    return "RumieseInnerBallistaPreLoaded.html";
                }
            }
        }
        return super.onFirstTalk(npc, player);
    }

    @Override
    public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSkillSee: " + skill.getName());
        }

        InstanceWorld wrld = null;
        if (npc != null)
        {
            wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        }
        else if (player != null)
        {
            wrld = InstanceManager.getInstance().getPlayerWorld(player);
        }
        else
        {
            Log.warning(getName() + ": onSpellFinished: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof IstinaWorld)
        {
            IstinaWorld world = (IstinaWorld) wrld;
            switch (skill.getId())
            {
                case 14224: //Energy Control Device
                    int casterCount = 0;
                    for (L2PcInstance players : npc.getKnownList().getKnownPlayers().values())
                    {
                        if (players.getTarget() == npc && players.getLastSkillCast() == _energyControlDevice)
                        {
                            casterCount++;
                        }
                    }

                    if (npc == world.Istina)
                    {
                        if (casterCount >= 7)
                        {
                            L2Abnormal eff = npc.getFirstEffect(_flood);
                            if (eff != null)
                            {
                                eff.exit();
                            }

                            InstanceManager.getInstance()
                                    .sendPacket(world.instanceId, new ExShowScreenMessage(1811175, 0, true, 5000));
                        }
                    }
                    else if (npc.getNpcId() == _sealingEnergy)
                    {
                        if (npc.isCastingNow() && casterCount > 0) //Npc should be casting
                        {
                            npc.doDie(player);
                        }
                    }
                    break;
            }
        }
        return super.onSkillSee(npc, player, skill, targets, isPet);
    }

    @Override
    public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSpellFinished: " + skill.getName());
        }

        InstanceWorld wrld = null;
        if (npc != null)
        {
            wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        }
        else if (player != null)
        {
            wrld = InstanceManager.getInstance().getPlayerWorld(player);
        }
        else
        {
            Log.warning(getName() + ": onSpellFinished: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof IstinaWorld)
        {
            IstinaWorld world = (IstinaWorld) wrld;
            if (npc.getNpcId() == world.IstinaId)
            {
                switch (skill.getId())
                {
                    case 14215: //Barrier of Reflection
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new ExShowScreenMessage(1811148, 0, true, 5000));

                        world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_02", 0, 0, 0, 0, 0));
                        break;

                    case 14220: //Flood
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new ExShowScreenMessage(1811141, 0, true, 5000));

                        world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_05", 0, 0, 0, 0, 0));
                        break;

                    case 14221: //Acid Eruption
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new ExShowScreenMessage(1811156, 0, true, 5000));

                        List<L2PcInstance> instPlayers = InstanceManager.getInstance().getPlayers(world.instanceId);
                        if (instPlayers.isEmpty())
                        {
                            break;
                        }

                        for (int i = 1; i <= (world.isHardMode ? Rnd.get(2, 3) : Rnd.get(2, 5)); i++)
                        {
                            L2PcInstance target = instPlayers.get(Rnd.get(instPlayers.size()));
                            if (_debug)
                            {
                                Log.warning(getName() + ": Acid Target: " + target.getName());
                            }

                            L2Npc dummyAcid =
                                    addSpawn(_acidDummyNpc, target.getX(), target.getY(), target.getZ(), -1, true, 0,
                                            false, world.instanceId);
                            dummyAcid.setTarget(dummyAcid);
                            dummyAcid.doCast(_dummyAcidEruption);
                        }
                        break;

                    case 14218: //Istina's Mark
                        istinasMarkAndDeathBlow(world);
                        break;
                }
            }
            else if (npc.getNpcId() == _acidDummyNpc)
            {
                if (skill.getId() == 14222)//Acid Eruption
                {
                    npc.setTarget(npc);
                    npc.doCast(_dummyEndAcidEruption);
                }
            }
        }
        return super.onSpellFinished(npc, player, skill);
    }

    private static void istinasMarkAndDeathBlow(IstinaWorld world)
    {
        Collection<L2Character> players = world.Istina.getKnownList().getKnownCharacters();
        if (players == null || players.isEmpty())
        {
            return;
        }

        for (L2Character player : players)
        {
            if (player == null)
            {
                continue;
            }

            if (player.isAlikeDead())
            {
                continue;
            }

            if (player.getFirstEffect(14218) != null) //Istina Mark
            {
                player.sendPacket(
                        new ExShowScreenMessage(1811187, 0, true, 5000)); //Istina's Mark shines above the head.

                world.Istina.setTarget(player);
                world.Istina.doCast(_deathBlow);
                break;
            }
        }
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAdvEvent: " + event);
        }

        InstanceWorld wrld = null;
        if (npc != null)
        {
            wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        }
        else if (player != null)
        {
            wrld = InstanceManager.getInstance().getPlayerWorld(player);
        }
        else
        {
            Log.warning(getName() + ": onAdvEvent: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof IstinaWorld)
        {
            final IstinaWorld world = (IstinaWorld) wrld;
            if (event.equalsIgnoreCase("stage_0_open_doors"))
            {
                world.status = 1;
                for (L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
                {
                    door.openMe();
                }
                startQuestTimer("stage_1_intro", _debug ? 60000 : 5 * 60000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro"))
            {
                world.status = 2;
                for (L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
                {
                    door.closeMe();
                }

                //Kick retards
                ArrayList<Integer> allowedPlayers = new ArrayList<Integer>(world.allowed);
                for (int objId : allowedPlayers)
                {
                    L2PcInstance pl = L2World.getInstance().getPlayer(objId);
                    if (pl != null && pl.isOnline() && pl.getInstanceId() == world.instanceId)
                    {
                        if (pl.getY() < 145039)
                        {
                            world.allowed.remove((Integer) pl.getObjectId());
                            pl.logout(true);
                        }
                    }
                }

                InstanceManager.getInstance().showVidToInstance(31, world.instanceId); //intro

                startQuestTimer("stage_1_begin", ScenePlayerDataTable.getInstance().getVideoDuration(31), null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_begin"))
            {
                world.status = 3;

                world.Istina =
                        addSpawn(world.IstinaId, -177119, 147857, -11385, 49511, false, 0, false, world.instanceId);
                world.Istina.setIsMortal(false);

                startQuestTimer("stage_all_manifestation_of_authority", Rnd.get(60000 / 2, 60000), world.Istina, null);

                if (world.isHardMode)
                {
                    startQuestTimer("stage_all_epic_sealing_energy_task", 60000, world.Istina, null);
                    startQuestTimer("stage_all_epic_minions_task", 90000, world.Istina, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_manifestation_of_authority"))
            {
                if (world.status < 4)
                {
                    int delay = 0;
                    if (world.Istina.isInCombat())
                    {
                        //Cast a random Manifestation
                        world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_01", 0, 0, 0, 0, 0));

                        final L2Skill randomSkill =
                                _manifestation_of_authority[Rnd.get(_manifestation_of_authority.length)];
                        switch (randomSkill.getId())
                        {
                            case 14212: //Manifestation of Authority (Red)
                                if (!world.isHardMode)
                                {
                                    InstanceManager.getInstance().sendPacket(world.instanceId,
                                            new ExShowScreenMessage(1811138, 0, true, 5000));
                                }

                                InstanceManager.getInstance()
                                        .sendPacket(world.instanceId, new EventTrigger(_effectBlueCircle, true));
                                InstanceManager.getInstance()
                                        .sendPacket(world.instanceId, new EventTrigger(_effectGreenCircle, true));

                                world.zone = zoneInUse.USE_ZONE_RED;
                                break;

                            case 14213: //Manifestation of Authority (Blue)
                                if (!world.isHardMode)
                                {
                                    InstanceManager.getInstance().sendPacket(world.instanceId,
                                            new ExShowScreenMessage(1811139, 0, true, 5000));
                                }

                                InstanceManager.getInstance()
                                        .sendPacket(world.instanceId, new EventTrigger(_effectRedCircle, true));
                                InstanceManager.getInstance()
                                        .sendPacket(world.instanceId, new EventTrigger(_effectGreenCircle, true));

                                world.zone = zoneInUse.USE_ZONE_BLUE;
                                break;

                            case 14214: //Manifestation of Authority (Green)
                                if (!world.isHardMode)
                                {
                                    InstanceManager.getInstance().sendPacket(world.instanceId,
                                            new ExShowScreenMessage(1811140, 0, true, 5000));
                                }

                                InstanceManager.getInstance()
                                        .sendPacket(world.instanceId, new EventTrigger(_effectRedCircle, true));
                                InstanceManager.getInstance()
                                        .sendPacket(world.instanceId, new EventTrigger(_effectBlueCircle, true));

                                world.zone = zoneInUse.USE_ZONE_GREEN;
                                break;
                        }

                        startQuestTimer("stage_all_turnOffCircleEffect", 15000, npc, null);

                        if (_debug)
                        {
                            Log.warning(getName() + ": onSpellFinished: Zone in use is: " + world.zone);
                        }

                        revalidateZone(world);

                        if (world.Istina.isCastingNow())
                        {
                            delay += 5000;
                        }

                        delay += Rnd.get(3, 5) * 1000;

                        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                world.Istina.doCast(randomSkill);
                            }
                        }, delay);
                    }
                    startQuestTimer("stage_all_manifestation_of_authority", Rnd.get(68000 + delay, 68000 + delay * 2),
                            world.Istina, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_epic_sealing_energy_task"))
            {
                if (world.status < 4)
                {
                    int knownPlayers = world.Istina.getKnownList().getKnownPlayers().size();
                    int knownChars = world.Istina.getKnownList().getKnownCharacters().size();
                    if (knownPlayers > 1 && knownChars - knownPlayers < 20 && world.Istina.isInCombat())
                    {
                        for (int a = 0; a < Rnd.get(1, 3); a++)
                        {
                            L2Npc sealingEnergy = addSpawn(_sealingEnergy, world.Istina.getX(), world.Istina.getY(),
                                    world.Istina.getZ(), 0, true, 180000, true, world.instanceId);
                            sealingEnergy.setIsInvul(true);
                        }
                        world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_04", 0, 0, 0, 0, 0));
                    }
                    startQuestTimer("stage_all_epic_sealing_energy_task", Rnd.get(2, 3) * 60000, world.Istina, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_epic_minions_task"))
            {
                if (world.status < 4)
                {
                    int knownPlayers = world.Istina.getKnownList().getKnownPlayers().size();
                    int knownChars = world.Istina.getKnownList().getKnownCharacters().size();
                    if (knownPlayers > 1 && knownChars - knownPlayers < 20 && world.Istina.isInCombat())
                    {
                        if (world.Istina.getCurrentHp() < world.Istina.getMaxHp() * 0.75) //only if have less than 75%
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new ExShowScreenMessage(1811144, 0, true,
                                            2000)); //Istina calls her creatures with tremendous anger.

                            //Taklacan, Torumba, Dopagen
                            for (Location minionLoc : _minionLocs)
                            {
                                for (int a = 0; a < Rnd.get(2, 3); a++)
                                {
                                    addSpawn(_minionIds[Rnd.get(_minionIds.length)], minionLoc.getX(), minionLoc.getY(),
                                            minionLoc.getZ(), minionLoc.getHeading(), false, 0, true, world.instanceId);
                                }
                            }
                            world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_03", 0, 0, 0, 0, 0));
                        }

                        //Istina's Creation
                        for (int a = 0; a < Rnd.get(3, 5); a++)
                        {
                            addSpawn(_istinasCreationId, world.Istina.getX(), world.Istina.getY(), world.Istina.getZ(),
                                    0, true, 0, true, world.instanceId);
                        }
                    }
                    startQuestTimer("stage_all_epic_minions_task", Rnd.get(2, 3) * 60000, world.Istina, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_last_spawns"))
            {
                world.status = 5;

                world.Ballista =
                        addSpawn(_ballistaId, -177119, 146889, -11384, 16571, false, 0, false, world.instanceId);
                world.Ballista.disableCoreAI(true);
                world.Ballista.setIsInvul(true);
                world.Ballista.setIsParalyzed(true);

                addSpawn(_rumieseInnerId, -177028, 146879, -11384, 22754, false, 0, false, world.instanceId);

                //Spam messages
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 3,
                        new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, true, 1000, 0,
                                "After 5 seconds, the charging magic Ballistas starts."));
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 4,
                        new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, true, 1000, 0,
                                "After 4 seconds, the charging magic Ballistas starts."));
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 5,
                        new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, true, 1000, 0,
                                "After 3 seconds, the charging magic Ballistas starts."));
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 6,
                        new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, true, 1000, 0,
                                "After 2 seconds, the charging magic Ballistas starts."));
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 7,
                        new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, true, 1000, 0,
                                "After 1 seconds, the charging magic Ballistas starts."));

                startQuestTimer("stage_last_start_message", 8000, npc, null);
            }
            else if (event.equalsIgnoreCase("stage_last_start_message"))
            {
                InstanceManager.getInstance()
                        .sendPacket(world.instanceId, new ExShowScreenMessage(1811172, 0, true, 2000));

                world.status = 6;

                startQuestTimer("stage_last_check_ballista", 1000, npc, null); //1sec
            }
            else if (event.equalsIgnoreCase("stage_last_check_ballista"))
            {
                if (world.ballistaSeconds > 0)
                {
                    double calculation = world.currBallistDamage / world.maxBallistDamage * 100;

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new ExSendUIEvent(2, world.ballistaSeconds, (int) calculation, 1811347));

                    world.ballistaSeconds -= 1;

                    startQuestTimer("stage_last_check_ballista", 1000, npc, null); //1sec
                }
                else
                {
                    //End here
                    world.status = 7;

                    InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEventRemove());

                    int time = 0;
                    double chanceToKillIstina = world.currBallistDamage * 100 / world.maxBallistDamage;

                    if (_debug)
                    {
                        Log.warning(getName() + ": Chance to kill istina: " + chanceToKillIstina);
                    }

                    if (chanceToKillIstina > 40 && Rnd.get(101) <=
                            chanceToKillIstina)//We want at least 40% on the ballista in order to kill istina
                    {
                        //Success, kill istina
                        world.status = 8;

                        time = ScenePlayerDataTable.getInstance().getVideoDuration(32);

                        //Let's shot
                        InstanceManager.getInstance().showVidToInstance(32, world.instanceId); //End ok
                    }
                    else
                    {
                        world.status = 9;

                        time = ScenePlayerDataTable.getInstance().getVideoDuration(33);

                        InstanceManager.getInstance().showVidToInstance(33, world.instanceId); //End fail
                    }

                    InstanceManager.getInstance().setInstanceReuse(world.instanceId, world.templateId,
                            world.templateId == _templates[0] ? false : true);
                    InstanceManager.getInstance().finishInstance(world.instanceId, true);

                    startQuestTimer("stage_last_boss_drop", time - 1200, npc, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_turnOffCircleEffect"))
            {
                switch (world.zone)
                {
                    case USE_ZONE_RED:
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new EventTrigger(_effectBlueCircle, false));
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new EventTrigger(_effectGreenCircle, false));
                        break;

                    case USE_ZONE_GREEN:
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new EventTrigger(_effectBlueCircle, false));
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new EventTrigger(_effectRedCircle, false));
                        break;

                    case USE_ZONE_BLUE:
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new EventTrigger(_effectRedCircle, false));
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new EventTrigger(_effectGreenCircle, false));
                        break;
                    default:
                }
            }
            else if (event.equalsIgnoreCase("stage_last_boss_drop"))
            {
                //Spawn Rumiese
                addSpawn(_rumieseInnerId, -177120, 147860, -11388, 49201, false, 0, false, world.instanceId);

                if (world.status == 8) //Only if istina is killed
                {
                    L2PcInstance randomPlayer = null;

                    int x = world.allowed.size();
                    if (x > 0)
                    {
                        for (int i = 0; i < x; i++)
                        {
                            int objId = world.allowed.get(Rnd.get(world.allowed.size()));
                            randomPlayer = L2World.getInstance().getPlayer(objId);
                            if (randomPlayer != null && randomPlayer.getInstanceId() == world.instanceId)
                            {
                                if (_debug)
                                {
                                    Log.warning(
                                            getName() + ": " + randomPlayer.getName() + " will be used as a killer!");
                                }

                                world.Istina = addSpawn(world.IstinaId, -177120, 148794, -11229, 49488, false, 0, false,
                                        world.instanceId);
                                world.Istina.reduceCurrentHp(world.Istina.getMaxHp(), randomPlayer, null);
                                world.Istina.deleteMe();
                                break;
                            }
                        }
                    }

                    if (randomPlayer == null)
                    {
                        Log.warning(getName() + ": Cant found an instanced player for kill Istina.");
                    }
                }
            }
            else if (event.equalsIgnoreCase("tryGetReward"))
            {
                if (world.status == 8)
                {
                    synchronized (world.rewardedPlayers)
                    {
                        if (InstanceManager.getInstance().canGetUniqueReward(player, world.rewardedPlayers))
                        {
                            world.rewardedPlayers.add(player);

                            player.addItem(_qn, _boxgMagicPower, 1, npc, true);

                            if (world.isHardMode)
                            {
                                player.addItem(_qn, _istinaCrystal, 1, npc, true);
                            }
                        }
                        else
                        {
                            player.sendMessage("Nice attempt, but you already got a reward!");
                        }
                    }
                }
            }
        }

        if (npc != null && npc.getNpcId() == _rumieseEnterId && Util.isDigit(event) &&
                Util.contains(_templates, Integer.valueOf(event)))
        {
            try
            {
                enterInstance(player, Integer.valueOf(event));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        return null;
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (npc == null || attacker == null)
        {
            return null;
        }

        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + attacker.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof IstinaWorld)
        {
            final IstinaWorld world = (IstinaWorld) tmpWorld;

            if (npc.getNpcId() == world.IstinaId)
            {
                if (world.status == 3 && npc.getCurrentHp() < npc.getMaxHp() * 0.04) //4%?
                {
                    world.status = 4;

                    world.zone = zoneInUse.NONE;

                    //Delete all spawned npcs before ballista spawn
                    InstanceManager.getInstance().despawnAll(world.instanceId);
                    InstanceManager.getInstance().showVidToInstance(34, world.instanceId);

                    startQuestTimer("stage_last_spawns", ScenePlayerDataTable.getInstance().getVideoDuration(34) + 2000,
                            npc, null);
                }
            }
            else if (npc.getNpcId() == _ballistaId)
            {
                if (world.status == 6)
                {
                    if (world.currBallistDamage == world.maxBallistDamage)
                    {
                        return super.onAttack(npc, attacker, damage, isPet);
                    }

                    if (world.currBallistDamage + damage > world.maxBallistDamage)
                    {
                        world.currBallistDamage = world.maxBallistDamage;
                    }
                    else
                    {
                        world.currBallistDamage += damage;
                    }
                }
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        if (npc.getNpcId() == _rumieseEnterId)
        {
            return "Rumiese.html";
        }

        return super.onTalk(npc, player);
    }

    @Override
    public String onEnterZone(L2Character character, L2ZoneType zone)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onEnterZone: " + character.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(character.getInstanceId());
        if (tmpWorld instanceof IstinaWorld)
        {
            final IstinaWorld world = (IstinaWorld) tmpWorld;
            if (world.zone == zoneInUse.NONE || !(character instanceof L2Playable))
            {
                return super.onEnterZone(character, zone);
            }

            switch (zone.getId())
            {
                case _ZONE_GREEN_ID: //Green, center
                    switch (world.zone)
                    {
                        case USE_ZONE_BLUE:
                        case USE_ZONE_RED:
                            world.zoneDebuff.getEffects(character, character);
                            break;
                        default:
                    }
                    break;

                case _ZONE_BLUE_ID: //Blue, second
                    switch (world.zone)
                    {
                        case USE_ZONE_RED:
                        case USE_ZONE_GREEN:
                            world.zoneDebuff.getEffects(character, character);
                            break;
                        default:
                    }
                    break;

                case _ZONE_RED_ID: //Red, last
                    switch (world.zone)
                    {
                        case USE_ZONE_BLUE:
                        case USE_ZONE_GREEN:
                            world.zoneDebuff.getEffects(character, character);
                            break;
                        default:
                    }
                    break;
                default:
            }
        }
        return super.onEnterZone(character, zone);
    }

    private void setupIDs(IstinaWorld world, int template_id)
    {
        if (template_id == 170) //Hard
        {
            world.IstinaId = 29196;
            world.maxBallistDamage = 1600000;
            world.zoneDebuff = SkillTable.getInstance().getInfo(14289, 2);
            world.isHardMode = true;
        }
        else
        {
            world.IstinaId = 29195;
            world.maxBallistDamage = 800000;
            world.zoneDebuff = SkillTable.getInstance().getInfo(14289, 1);
        }
    }

    private final synchronized void enterInstance(L2PcInstance player, int template_id)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof IstinaWorld))
            {
                player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
                return;
            }

            Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if (inst != null)
            {
                if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId()))
                {
                    player.setInstanceId(world.instanceId);
                    player.teleToLocation(-177107, 146576, -11392, true);
                }
            }
            return;
        }
        else
        {
            int minPlayers = template_id == 170 ? Config.ISTINA_MIN_PLAYERS : Config.ISTINA_MIN_PLAYERS / 2;
            int maxLevel = template_id == 170 ? Config.MAX_LEVEL : 99;
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, template_id, minPlayers, 35, 92, maxLevel))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new IstinaWorld();
            world.instanceId = instanceId;
            world.templateId = template_id;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            setupIDs((IstinaWorld) world, template_id);

            List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (_debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(minPlayers > Config.MAX_MEMBERS_IN_PARTY ?
                        player.getParty().getCommandChannel().getMembers() :
                        player.getParty().getCommandChannel() != null ?
                                player.getParty().getCommandChannel().getMembers() :
                                player.getParty().getPartyMembers());
            }

            for (L2PcInstance enterPlayer : allPlayers)
            {
                if (enterPlayer == null)
                {
                    continue;
                }

                world.allowed.add(enterPlayer.getObjectId());

                if (enterPlayer.getInventory().getItemByItemId(_energyDevice) == null)
                {
                    enterPlayer.addItem(_qn, _energyDevice, 1, enterPlayer, true);
                }

                enterPlayer.stopAllEffectsExceptThoseThatLastThroughDeath();
                enterPlayer.setInstanceId(instanceId);
                enterPlayer.teleToLocation(_playerEnter[Rnd.get(0, _playerEnter.length - 1)], true);
            }

            startQuestTimer("stage_0_open_doors", 5000, null, player);

            Log.fine(getName() + ": [" + template_id + "] instance started: " + instanceId + " created by player: " +
                    player.getName());
            return;
        }
    }

    private void revalidateZone(IstinaWorld world)
    {
        for (int objId : world.allowed)
        {
            L2PcInstance player = L2World.getInstance().getPlayer(objId);
            if (player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
            {
                if (_ZONE_BLUE.isCharacterInZone(player))
                {
                    notifyEnterZone(player, _ZONE_BLUE);
                }
                else if (_ZONE_GREEN.isCharacterInZone(player))
                {
                    notifyEnterZone(player, _ZONE_GREEN);
                }
                else if (_ZONE_RED.isCharacterInZone(player))
                {
                    notifyEnterZone(player, _ZONE_RED);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        new Istina(-1, _qn, "instances/GrandBosses");
    }
}
