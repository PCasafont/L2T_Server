package l2server.gameserver.model

enum class InstanceType(val parent: InstanceType?) {

	L2Object(null),
	L2ItemInstance(L2Object),
	L2Character(L2Object),
	L2Npc(L2Character),
	L2Playable(L2Character),
	L2Summon(L2Playable),
	L2Decoy(L2Character),
	L2Trap(L2Character),
	L2PcInstance(L2Playable),
	L2NpcInstance(L2Npc),
	L2MerchantInstance(L2NpcInstance),
	L2WarehouseInstance(L2NpcInstance),
	L2StaticObjectInstance(L2Character),
	L2DoorInstance(L2Character),
	L2NpcWalkerInstance(L2Npc),
	L2TerrainObjectInstance(L2Npc),
	L2EffectPointInstance(L2Npc),
	// Summons, Pets, Decoys and Traps
	L2SummonInstance(L2Summon),
	L2SiegeSummonInstance(L2SummonInstance),
	L2MerchantSummonInstance(L2SummonInstance),
	L2PetInstance(L2Summon),
	L2BabyPetInstance(L2PetInstance),
	L2DecoyInstance(L2Decoy),
	L2TrapInstance(L2Trap),
	// Attackable
	L2Attackable(L2Npc),
	L2GuardInstance(L2Attackable),
	L2QuestGuardInstance(L2GuardInstance),
	L2MonsterInstance(L2Attackable),
	L2ChestInstance(L2MonsterInstance),
	L2ControllableMobInstance(L2MonsterInstance),
	L2FeedableBeastInstance(L2MonsterInstance),
	L2TamedBeastInstance(L2FeedableBeastInstance),
	L2FriendlyMobInstance(L2Attackable),
	L2PenaltyMonsterInstance(L2MonsterInstance),
	L2RaidBossInstance(L2MonsterInstance),
	L2GrandBossInstance(L2RaidBossInstance),
	// FlyMobs
	L2FlyNpcInstance(L2NpcInstance),
	L2FlyMonsterInstance(L2MonsterInstance),
	L2FlyRaidBossInstance(L2RaidBossInstance),
	L2FlyTerrainObjectInstance(L2Npc),
	// Sepulchers
	L2SepulcherNpcInstance(L2NpcInstance),
	L2SepulcherMonsterInstance(L2MonsterInstance),
	// Vehicles
	L2Vehicle(L2Character),
	L2BoatInstance(L2Vehicle),
	L2AirShipInstance(L2Vehicle),
	L2ControllableAirShipInstance(L2AirShipInstance),
	L2ShuttleInstance(L2Vehicle),
	// Siege
	L2DefenderInstance(L2Attackable),
	L2ArtefactInstance(L2NpcInstance),
	L2ControlTowerInstance(L2Npc),
	L2FlameTowerInstance(L2Npc),
	L2SiegeFlagInstance(L2Npc),
	L2SiegeNpcInstance(L2Npc),
	// Fort Siege
	L2FortBallistaInstance(L2Npc),
	L2FortCommanderInstance(L2DefenderInstance),
	// Castle NPCs
	L2CastleBlacksmithInstance(L2NpcInstance),
	L2CastleChamberlainInstance(L2MerchantInstance),
	L2CastleMagicianInstance(L2NpcInstance),
	L2CastleTeleporterInstance(L2Npc),
	L2CastleWarehouseInstance(L2WarehouseInstance),
	L2MercManagerInstance(L2MerchantInstance),
	// Fort NPCs
	L2FortEnvoyInstance(L2Npc),
	L2FortLogisticsInstance(L2MerchantInstance),
	L2FortManagerInstance(L2MerchantInstance),
	L2FortSiegeNpcInstance(L2NpcWalkerInstance),
	L2FortSupportCaptainInstance(L2MerchantInstance),
	// City NPCs
	L2AdventurerInstance(L2NpcInstance),
	L2AuctioneerInstance(L2Npc),
	L2ClanHallManagerInstance(L2MerchantInstance),
	L2ClanTraderInstance(L2Npc),
	L2FameManagerInstance(L2Npc),
	L2FishermanInstance(L2MerchantInstance),
	L2ManorManagerInstance(L2MerchantInstance),
	L2MercenaryManagerInstance(L2Npc),
	L2ObservationInstance(L2Npc),
	L2OlympiadManagerInstance(L2Npc),
	L2PetManagerInstance(L2MerchantInstance),
	L2RaceManagerInstance(L2Npc),
	L2SymbolMakerInstance(L2Npc),
	L2TeleporterInstance(L2Npc),
	L2TownPetInstance(L2Npc),
	L2TrainerInstance(L2NpcInstance),
	L2TransformManagerInstance(L2MerchantInstance),
	L2VillageMasterInstance(L2NpcInstance),
	L2WyvernManagerInstance(L2NpcInstance),
	L2XmassTreeInstance(L2NpcInstance),
	L2AwakeNpcInstance(L2Npc),
	// Doormens
	L2DoormenInstance(L2NpcInstance),
	L2CastleDoormenInstance(L2DoormenInstance),
	L2FortDoormenInstance(L2DoormenInstance),
	L2ClanHallDoormenInstance(L2DoormenInstance),
	// Custom
	L2ClassMasterInstance(L2NpcInstance),
	L2NpcBufferInstance(L2Npc),
	L2InstancedEventNpcInstance(L2Npc),
	L2WeddingManagerInstance(L2Npc),
	L2EventMobInstance(L2Npc),
	L2StatueInstance(L2Npc),
	L2GatekeeperInstance(L2TeleporterInstance),
	L2BufferInstance(L2TeleporterInstance),
	L2CustomMerchantInstance(L2MerchantInstance),
	L2WorldManagerInstance(L2MerchantInstance),
	L2DonationManagerInstance(L2MerchantInstance),
	L2MiniRaidInstance(L2RaidBossInstance),
	L2DeluxeChestInstance(L2MonsterInstance),
	L2MiniGameManagerInstance(L2MerchantInstance),
	L2CloneInstance(L2SummonInstance);

	private val typeL: Long
	private val typeH: Long
	private val maskL: Long
	private val maskH: Long

	init {

		val high = ordinal - (java.lang.Long.SIZE - 1)
		if (high < 0) {
			typeL = 1L shl ordinal
			typeH = 0
		} else {
			typeL = 0
			typeH = 1L shl high
		}

		if (typeL < 0 || typeH < 0) {
			throw Error("Too many instance types, failed to load $name")
		}

		if (parent != null) {
			maskL = typeL or parent.maskL
			maskH = typeH or parent.maskH
		} else {
			maskL = typeL
			maskH = typeH
		}
	}

	fun isType(it: InstanceType): Boolean {
		return maskL and it.typeL > 0 || maskH and it.typeH > 0
	}

	fun isTypes(vararg it: InstanceType): Boolean {
		for (i in it) {
			if (isType(i)) {
				return true
			}
		}
		return false
	}
}
