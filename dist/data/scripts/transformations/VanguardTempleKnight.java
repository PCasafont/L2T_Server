package transformations;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.TransformationManager;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Transformation;

public class VanguardTempleKnight extends L2Transformation {
	public VanguardTempleKnight() {
		// id
		super(314);
	}

	@Override
	public void onTransform() {
		if (getPlayer().getTransformationId() != 314 || getPlayer().isCursedWeaponEquipped()) {
			return;
		}

		transformedSkills();
	}

	public void transformedSkills() {
		int lvl = 1;
		if (getPlayer().getLevel() > 42) {
			lvl = getPlayer().getLevel() - 42;
		}

		// Two handed mastery
		getPlayer().addSkill(SkillTable.getInstance().getInfo(293, lvl), false);
		// Full Swing
		getPlayer().addSkill(SkillTable.getInstance().getInfo(814, lvl), false);
		// Power Divide
		getPlayer().addSkill(SkillTable.getInstance().getInfo(816, lvl), false);
		// Boost Morale
		getPlayer().addSkill(SkillTable.getInstance().getInfo(956, lvl), false);
		// Guillotine Attack
		getPlayer().addSkill(SkillTable.getInstance().getInfo(957, lvl), false);
		// Switch Stance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(838, 1), false);
		// Set allowed skills
		getPlayer().setTransformAllowedSkills(new int[]{10, 18, 28, 67, 197, 293, 400, 449, 814, 816, 838, 956, 957});
	}

	@Override
	public void onUntransform() {
		removeSkills();

		// Remove Boost Morale effect
		for (L2Abnormal e : getPlayer().getAllEffects()) {
			if (e != null && e.getSkill().getId() == 956) {
				e.exit();
			}
		}
	}

	public void removeSkills() {
		int lvl = 1;
		if (getPlayer().getLevel() > 42) {
			lvl = getPlayer().getLevel() - 42;
		}

		// Two handed mastery
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(293, lvl), false);
		// Full Swing
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(814, lvl), false);
		// Power Divide
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(816, lvl), false);
		// Boost Morale
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(956, lvl), false, false);
		// Guillotine Attack
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(957, lvl), false);
		// Switch Stance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(838, 1), false);

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}

	public static void main(String[] args) {
		TransformationManager.getInstance().registerTransformation(new VanguardTempleKnight());
	}
}
