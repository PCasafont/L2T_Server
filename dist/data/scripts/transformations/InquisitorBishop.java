package transformations;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.TransformationManager;
import l2server.gameserver.model.L2Transformation;

public class InquisitorBishop extends L2Transformation
{
	public InquisitorBishop()
	{
		// id
		super(316);
	}

	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 316 || getPlayer().isCursedWeaponEquipped())
		{
			return;
		}

		transformedSkills();
	}

	public void transformedSkills()
	{
		if (getPlayer().getLevel() > 39)
		{
			// Divine Punishment
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1523, getPlayer().getLevel() - 39), false);
			// Divine Flash
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1528, getPlayer().getLevel() - 39), false);
			// Surrender to the Holy
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1524, getPlayer().getLevel() - 39), false);
			// Divine Curse
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1525, getPlayer().getLevel() - 39), false);
			getPlayer().setTransformAllowedSkills(new int[]{838, 1523, 1528, 1524, 1525, 1430, 1043, 1042, 1400, 1418});
		}
		else
		{
			getPlayer().setTransformAllowedSkills(new int[]{838, 1430, 1043, 1042, 1400, 1418});
		}
		// Switch Stance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(838, 1), false);
	}

	@Override
	public void onUntransform()
	{
		removeSkills();
	}

	public void removeSkills()
	{
		// Divine Punishment
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1523, getPlayer().getLevel() - 39), false);
		// Divine Flash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1528, getPlayer().getLevel() - 39), false);
		// Surrender to the Holy
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1524, getPlayer().getLevel() - 39), false);
		// Divine Curse
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1525, getPlayer().getLevel() - 39), false);
		// Switch Stance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(838, 1), false);

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InquisitorBishop());
	}
}
